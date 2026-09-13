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
        stage('Unit Tests') {
                    steps {
                        dir('user-service') {
                            bat 'mvn test -Dtest.groups=unit'
                        }
                    }
                }

        stage('Integration Tests') {
                    steps {
                        dir('user-service') {
                            bat 'mvn test -Dtest.groups=integration'
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