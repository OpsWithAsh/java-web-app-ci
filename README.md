# Java Web App – End-to-End DevSecOps CI/CD Pipeline 🚀

This project demonstrates a complete CI/CD and DevSecOps pipeline using Jenkins, Docker, Kubernetes, Helm, SonarQube, OWASP ZAP, and Snyk.
## 🧠 Problem Statement

Manual builds, testing, security scanning, and deployments are slow, error-prone, and risky.  
This project automates the entire lifecycle of a Java application with built-in security and quality checks.
## 💡 Solution Overview

An automated CI/CD pipeline that:
- Builds and tests code on every commit
- Performs static and dynamic security scans
- Containerizes the application
- Deploys to Kubernetes using Helm
- Fails fast on quality or security issues

## 🔧 Tech Stack

| Category | Tools |
|--------|------|
| CI/CD | Jenkins |
| Build | Maven |
| Container | Docker |
| Orchestration | Kubernetes |
| Deployment | Helm |
| Code Quality | SonarQube |
| Security | OWASP ZAP, Snyk, Dependency Check |
| SCM | GitHub |

## 🏗 Architecture

Developer → GitHub → Jenkins  
Jenkins → Build → Test → SonarQube → Docker → Docker Hub  
Jenkins → Helm → Kubernetes  
Jenkins → OWASP ZAP → Security Reports

## 📂 Repository Structure

```text
appset/           # ArgoCD / ApplicationSet configs
base/             # Base Kubernetes manifests
overlays/dev/     # Kustomize overlays for dev environment
helm-chart/       # Helm chart for Kubernetes deployment
scripts/          # Utility scripts
src/              # Java application source code
yaml/             # Kubernetes YAML manifests
Dockerfile        # Docker image definition
Jenkinsfile       # Jenkins CI/CD pipeline
dependency-check.sh # Dependency vulnerability scan
playbook.yaml     # Ansible automation
pom.xml           # Maven configuration

## 🔁 CI/CD Pipeline Stages

### 1. Code Checkout
- Pulls source code from GitHub

### 2. Build
- Maven compiles and packages the application

### 3. Unit Testing (Parallel)
- Executes automated tests

### 4. SonarQube Analysis
- Performs SAST and code quality checks
- Enforces quality gate

### 5. Docker Build & Push
- Builds Docker image
- Pushes image to Docker Hub

### 6. Kubernetes Deployment (Helm)
- Deploys application using Helm charts

### 7. DAST Scan (OWASP ZAP)
- Spider scan
- Active scan
- Generates HTML & JSON reports

## 🔐 Security Implementation

- **SAST**: SonarQube checks vulnerabilities and code smells
- **Dependency Scan**: Identifies vulnerable libraries
- **DAST**: OWASP ZAP scans running application
- **Secrets Management**: Jenkins credentials binding

## 🚀 How to Run Locally

```bash
mvn clean package
docker build -t java-web-app .
docker run -p 8080:8080 java-web-app

helm upgrade --install demo ./helm-chart

## 🧪 Jenkinsfile Highlights

- Declarative pipeline
- Parallel execution
- Credential masking
- Quality gate enforcement
- Artifact archiving

## 🔮 Future Enhancements

- GitOps deployment using ArgoCD
- Canary / Blue-Green deployments
- Prometheus & Grafana monitoring
- Trivy image scanning
- Slack notifications

# java-web-app-ci-cd
