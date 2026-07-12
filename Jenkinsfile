pipeline {

    agent {
        label 'agentt'
    }

    environment {
        DOCKER_USER = "gauri128"
        DOCKER_REPO = "node-app"
        CONTAINER_NAME = "node-container"
    }

    stages {

        stage('Verify Environment') {
            steps {
                sh '''
                echo "Node Version:"
                node -v

                echo "NPM Version:"
                npm -v

                echo "Git Version:"
                git --version

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
                docker build -f dockerfile \
                -t ${DOCKER_USER}/${DOCKER_REPO}:${BUILD_NUMBER} .
                '''
            }
        }

        stage('Docker Login') {
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
                    '''
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh '''
                docker push ${DOCKER_USER}/${DOCKER_REPO}:${BUILD_NUMBER}
                '''
            }
        }

        stage('Deploy Container') {
            steps {
                sh '''
                docker rm -f ${CONTAINER_NAME} || true

                docker run -d \
                --name ${CONTAINER_NAME} \
                -p 3000:3000 \
                ${DOCKER_USER}/${DOCKER_REPO}:${BUILD_NUMBER}
                '''
            }
        }
    }
}





