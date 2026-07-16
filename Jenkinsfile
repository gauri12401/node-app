pipeline {

    agent any

    environment {

        AWS_ACCOUNT_ID = "048674616992"
        AWS_REGION     = "ap-south-1"
        ECR_REPO       = "node-app"
        CLUSTER_NAME   = "demo-ekscluster1"

        IMAGE_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}"
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
                echo "========== Building Docker Image =========="

                docker build -t ${ECR_REPO}:${BUILD_NUMBER} .
                '''
            }
        }

        stage('Login to Amazon ECR') {
            steps {

                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-creds']
                ]) {

                    sh '''
                    echo "========== Login to Amazon ECR =========="

                    aws ecr get-login-password \
                    --region ${AWS_REGION} | docker login \
                    --username AWS \
                    --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                    '''
                }
            }
        }

        stage('Tag Docker Image') {
            steps {

                sh '''
                echo "========== Tagging Docker Image =========="

                docker tag ${ECR_REPO}:${BUILD_NUMBER} \
                ${IMAGE_URI}:${BUILD_NUMBER}
                '''
            }
        }

        stage('Push Docker Image to Amazon ECR') {
            steps {

                sh '''
                echo "========== Pushing Image to Amazon ECR =========="

                docker push ${IMAGE_URI}:${BUILD_NUMBER}
                '''
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

                    sed -i "s|IMAGE_NAME|${IMAGE_URI}:${BUILD_NUMBER}|g" deployment.yaml

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
            echo "========== Pipeline Execution Completed =========="
        }

        success {
            echo "Application Successfully Deployed to Amazon EKS using Amazon ECR."
        }

        failure {
            echo "Pipeline Failed. Please Check Jenkins Console Output."
        }
    }

}