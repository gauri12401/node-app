pipeline {

    agent any

    environment {
        DOCKER_USER = "gauri128"
        DOCKER_REPO = "node-app"
        CLUSTER_NAME = "demo-riicluster"
        AWS_REGION = "ap-south-1"
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

        stage('Deploy to Amazon EKS') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-creds']
                ]) {

                    sh '''
                    echo "========== Configuring EKS =========="

                    aws eks update-kubeconfig \
                    --name ${CLUSTER_NAME} \
                    --region ${AWS_REGION}

                    echo "========== Cluster Nodes =========="
                    kubectl get nodes

                    echo "========== Updating Deployment Image =========="

                    sed -i "s|IMAGE_NAME|${DOCKER_USER}/${DOCKER_REPO}:${BUILD_NUMBER}|g" deployment.yaml

                    echo "========== Applying Deployment =========="

                    kubectl apply -f deployment.yaml

                    echo "========== Applying Service =========="

                    kubectl apply -f service.yaml

                    echo "========== Image Used =========="

                    cat deployment.yaml | grep image

                    echo "========== Services =========="

                    kubectl get svc
                    '''
                }
            }
        }
    }

    post {

        always {
            echo '========== Pipeline Execution Completed =========='
        }

        success {
            echo 'Application Successfully Deployed to Amazon EKS.'
        }

        failure {
            echo 'Pipeline Failed. Please Check Jenkins Console Output.'
        }
    }
}