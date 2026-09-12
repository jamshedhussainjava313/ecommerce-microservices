pipeline {

    agent any

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