pipeline {

    agent any

    tools {
            maven 'Maven-3.9.11'
        }

    stages {

        stage('Build & Test User Service') {
            steps {
                dir('user-service') {
                    bat 'mvn clean test'
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