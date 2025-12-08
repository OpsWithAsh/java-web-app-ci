pipeline {
    agent any

    parameters {
        string(name: 'GIT_REPO', defaultValue: 'https://github.com/OpsWithAsh/java-web-app-ci.git')
        string(name: 'DOCKER_IMAGE', defaultValue: 'ashraw11/java-web-app-ci:latest')
    }

    environment {
        DOCKER_CREDENTIALS = credentials('test')
        SNYK_TOKEN = credentials('snyk-token')
    }

    stages {

        stage('Checkout & Build') {
            steps {
                git branch: 'main', url: "${GIT_REPO}"
                sh "mvn clean package -B"
            }
        }

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
                            withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
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

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Snyk Dependency Scan') {
            steps {
                withEnv(["SNYK_TOKEN=${SNYK_TOKEN}"]) {
                    sh "snyk test --file=pom.xml --severity-threshold=medium"
                }
            }
        }

        stage('Snyk Monitor Upload') {
            steps {
                withEnv(["SNYK_TOKEN=${SNYK_TOKEN}"]) {
                    sh "snyk monitor --file=pom.xml"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Snyk Container Scan') {
            steps {
                withEnv(["SNYK_TOKEN=${SNYK_TOKEN}"]) {
                    sh "snyk container test ${DOCKER_IMAGE} --severity-threshold=medium"
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh '''
                    echo "$DOCKER_CREDENTIALS_PSW" | docker login -u "$DOCKER_CREDENTIALS_USR" --password-stdin
                    docker push ${DOCKER_IMAGE}
                '''
            }
        }

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

        stage('DAST Scan with ZAP') {
            environment {
                ZAP_API = "http://localhost:8090"
                TARGET_URL = "http://localhost:31000"
            }
            steps {
                sh '''
                    spider_id=$(curl -s "${ZAP_API}/JSON/spider/action/scan/?url=${TARGET_URL}" | jq -r '.scan')
                    while true; do
                        sstatus=$(curl -s "${ZAP_API}/JSON/spider/view/status/?scanId=$spider_id" | jq -r '.status')
                        [ "$sstatus" -eq 100 ] && break
                        sleep 5
                    done

                    ascan_id=$(curl -s "${ZAP_API}/JSON/ascan/action/scan/?url=${TARGET_URL}" | jq -r '.scan')
                    while true; do
                        astatus=$(curl -s "${ZAP_API}/JSON/ascan/view/status/?scanId=$ascan_id" | jq -r '.status')
                        [ "$astatus" -eq 100 ] && break
                        sleep 5
                    done

                    curl -s "${ZAP_API}/JSON/core/view/alerts/?baseurl=${TARGET_URL}" > zap-report.json
                    curl -s "${ZAP_API}/OTHER/core/other/htmlreport/" > zap-report.html
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'zap-report.*', allowEmptyArchive: true
            echo "Pipeline completed"
        }
        failure {
            echo "Pipeline failed — check logs"
        }
    }
}
