pipeline {
    agent any

    parameters {
        string(name: 'GIT_REPO', defaultValue: 'https://github.com/OpsWithAsh/java-web-app-ci.git')
        string(name: 'DOCKER_IMAGE', defaultValue: 'ashraw11/java-web-app-ci:latest')
    }

    environment {
        DOCKER_CREDENTIALS = credentials('test')   // DockerHub creds
    }

    stages {

        /* ---------------------------
           CHECKOUT & BUILD
        ----------------------------*/
        stage('Checkout & Build') {
            steps {
                git branch: 'main', url: "${GIT_REPO}"
                sh "mvn clean package -B"
            }
        }

        /* ---------------------------
           PARALLEL: TESTS + SONAR
        ----------------------------*/
        stage('Parallel Tests & Sonar') {
            parallel {

                stage('Unit Tests') {
                    agent { label 'qa-tester' }
                    steps {
                        git url: params.GIT_REPO, branch: 'main'
                        sh 'mvn test -q'
                    }
                }

                stage('SonarQube Analysis') {
                    agent { label 'qa-tester' }
                    steps {
                        git url: params.GIT_REPO, branch: 'main'

                        withSonarQubeEnv('test') {
                            withCredentials([
                                string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')
                            ]) {
                                sh """
                                    mvn clean verify -Ddependency-check.skip=true sonar:sonar \
                                        -Dsonar.projectKey=java-web-app-ci \
                                        -Dsonar.java.binaries=target/ \
                                        -Dsonar.login=${SONAR_TOKEN}
                                """
                            }
                        }
                    }
                }
            }
        }

        /* ---------------------------
           QUALITY GATE
        ----------------------------*/
        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        /* ---------------------------
           DOCKER BUILD
        ----------------------------*/
        stage('Build Docker Image') {
            steps {
                dir("${WORKSPACE}") {
                    sh "docker build -t ${DOCKER_IMAGE} ."
                }
            }
        }

        /* ---------------------------
           DOCKER PUSH
        ----------------------------*/
        stage('Push Docker Image') {
            steps {
                sh '''
                    echo "$DOCKER_CREDENTIALS_PSW" | docker login -u "$DOCKER_CREDENTIALS_USR" --password-stdin
                    docker push ${DOCKER_IMAGE}
                '''
            }
        }

        /* ---------------------------
           DEPLOY USING HELM
        ----------------------------*/
        stage('Deploy with Helm') {
            steps {
                sh '''
                    export KUBECONFIG=/home/jenkins/.kube/config

                    helm upgrade --install demo ./helm-chart \
                        --set image.repository=ashraw11/java-web-app-ci \
                        --set image.tag=latest
                '''
            }
        }

        /* ---------------------------
           DAST SCAN USING OWASP ZAP
        ----------------------------*/
        stage('DAST Scan with ZAP') {
            environment {
                ZAP_API = "http://localhost:8090"
                TARGET_URL = "http://localhost:31000"   // Updated for WSL2 port-forward
            }
            steps {
                sh '''
                    echo "========================================="
                    echo " Starting ZAP Spider Scan"
                    echo "========================================="

                    spider_id=$(curl -s "${ZAP_API}/JSON/spider/action/scan/?url=${TARGET_URL}" | jq -r '.scan')
                    echo "Spider ID: $spider_id"

                    echo "Waiting for spider to complete..."
                    while true; do
                        sstatus=$(curl -s "${ZAP_API}/JSON/spider/view/status/?scanId=$spider_id" | jq -r '.status')
                        echo "Spider scan progress: $sstatus%"
                        [ "$sstatus" -eq 100 ] && break
                        sleep 5
                    done

                    echo "========================================="
                    echo " Starting ZAP Active Scan"
                    echo "========================================="

                    ascan_id=$(curl -s "${ZAP_API}/JSON/ascan/action/scan/?url=${TARGET_URL}" | jq -r '.scan')
                    echo "Active Scan ID: $ascan_id"

                    echo "Waiting for active scan to complete..."
                    while true; do
                        astatus=$(curl -s "${ZAP_API}/JSON/ascan/view/status/?scanId=$ascan_id" | jq -r '.status')
                        echo "Active scan progress: $astatus%"
                        [ "$astatus" -eq 100 ] && break
                        sleep 5
                    done

                    echo "========================================="
                    echo " Fetching Alerts"
                    echo "========================================="

                    curl -s "${ZAP_API}/JSON/core/view/alerts/?baseurl=${TARGET_URL}" > zap-report.json
                    curl -s "${ZAP_API}/OTHER/core/other/htmlreport/" > zap-report.html

                    echo "-----------------------------------------"
                    echo " ZAP Scan Completed"
                    echo " Reports saved: zap-report.json, zap-report.html"
                    echo "-----------------------------------------"
                '''
            }
        }
    }

    /* ---------------------------
       POST ACTIONS
    ----------------------------*/
    post {
        always {
            echo "Pipeline completed"
            archiveArtifacts artifacts: 'zap-report.*', allowEmptyArchive: true
        }
        failure {
            echo "Pipeline failed — check logs"
        }
    }
}
