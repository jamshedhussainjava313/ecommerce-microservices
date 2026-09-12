pipeline {

    agent any

    tools {
            maven 'Maven-3.9.11'
        }

    environment {
            DB_PASSWORD = credentials('DB_PASSWORD')
    }

    stages {

        stage('Build User Service') {
                    steps {
                        dir('user-service') {
                            bat 'mvn clean compile'
                        }
                    }
                }
        stage('Test User Service') {
                    steps {
                        dir('user-service') {
                            bat 'mvn test'
                        }
                    }
                }
    }

    post {

        success {
            echo 'CI Pipeline completed successfully.'
        }

        failure {
            echo 'CI Pipeline failed. Check the console output.'
        }
    }
}