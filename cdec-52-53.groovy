pipeline{
    agent {
        label 'Node-Agent'
    }
    environment {
        DOCKER_REPO = "mayurwagh"
        IMAGE_NAME = "node-app"
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/mayurmwagh/node-app.git'
            }
        }
        stage('Verify Environment') {
            steps {
                sh '''
                echo "Node Version:"
                node -v

                echo "NPM Version:"
                npm -v

                echo "Docker Version:"
                docker --version
                '''
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
                docker build -t ${DOCKER_REPO}:${BUILD_NUMBER} .
                ''' 
            }
        }
        // stage('Docker Login') {
        //     steps {
        //         withCredentials([
        //             usernamePassword(
        //                 credentialsId: '626460db-bf4f-41a8-86b7-e03f88673be7',
        //                 usernameVariable: 'DOCKER_USERNAME',
        //                 passwordVariable: 'DOCKER_PASSWORD'
        //             )
        //         ]) {
        //             sh 'docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}'
        //         }
        //     }
        // }
    }
}