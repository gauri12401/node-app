pipeline {

    agent any

    environment {
        DOCKER_USER = "gauri128"
        DOCKER_REPO = "node-app"
    }

    stages {

        stage('Clone Repository') {
            steps {
                checkout scm
            }
        }

        stage('Install Dependencies') {
            steps {
                sh 'npm install'
            }
        }

        stage('Run Tests') {
            steps {
                sh 'npm test'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                docker build -t ${DOCKER_USER}/${DOCKER_REPO}:${BUILD_NUMBER} .
                '''
            }
        }

        stage('Docker Login & Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'docker-hub',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                    echo "$DOCKER_PASSWORD" | docker login \
                    -u "$DOCKER_USERNAME" \
                    --password-stdin

                    docker push ${DOCKER_USER}/${DOCKER_REPO}:${BUILD_NUMBER}
                    '''
                }
            }
        }

        stage('Configure EKS') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-creds']
                ]) {
                    sh '''
                    aws eks update-kubeconfig \
                    --name demo-riicluster \
                    --region ap-south-1

                    kubectl get nodes
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                sed -i "s|IMAGE_NAME|${DOCKER_USER}/${DOCKER_REPO}:${BUILD_NUMBER}|g" deployment.yaml

                kubectl apply -f deployment.yaml

                echo "========= Image Used in Deployment ========="
                cat deployment.yaml | grep image

                kubectl get deployments
                kubectl get pods
                '''
            }
        }
    }
}



