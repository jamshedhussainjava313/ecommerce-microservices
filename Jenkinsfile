pipeline {

    agent any

    tools {
            maven 'Maven-3.9.11'
        }

    environment {
            DB_PASSWORD = credentials('DB_PASSWORD')
            SONAR_TOKEN = credentials('SONAR_TOKEN')
    }

    stages {

        stage('Build Order Service') {
                    steps {
                        dir('order-service') {
                            bat 'mvn clean compile'
                        }
                    }
                }
        stage('Unit Tests') {
                    steps {
                        dir('order-service') {
                            bat 'mvn test -Dtest=OrderServiceTest'
                        }
                    }
                }

        stage('Verify Docker Access') {
            steps {
                bat 'docker version'
            }
        }

        stage('Start CI PostgreSQL') {
            steps {
                bat '''
                    docker rm -f ecommerce-order-service-ci-postgres 2>NUL || exit /B 0

                    docker run -d ^
                      --name ecommerce-order-service-ci-postgres ^
                      -e POSTGRES_USER=ecommerce_user ^
                      -e POSTGRES_PASSWORD=%DB_PASSWORD% ^
                      -e POSTGRES_DB=order_db ^
                      -p 5434:5432 ^
                      postgres:16
                '''

                bat '''
                    echo Waiting for CI PostgreSQL...

                    :waitloop
                    docker exec ecommerce-order-service-ci-postgres ^
                      pg_isready -U ecommerce_user -d order_db

                    if %ERRORLEVEL% NEQ 0 (
                        timeout /t 2 /nobreak >NUL
                        goto waitloop
                    )

                    echo CI PostgreSQL is ready.
                '''
            }
        }

        stage('Integration Tests') {
                    steps {
                        dir('order-service') {
                            bat 'mvn test -Dtest=OrderControllerIntegrationTest -Dspring.profiles.active=ci'
                        }
                    }
                }

        stage('Generate Code Coverage') {
                    steps {
                        dir('order-service') {
                            bat 'mvn jacoco:report'
                        }
                    }
                }

        stage('SonarQube Analysis') {
            steps {
                dir('order-service') {
                    withSonarQubeEnv('SonarQube-Local') {
                        bat 'mvn sonar:sonar -Dsonar.projectKey=ecommerce-order-service -Dsonar.token=%SONAR_TOKEN%'
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Archive Code Coverage') {
                    steps {
                        archiveArtifacts artifacts: 'order-service/target/site/jacoco/**',
                                         fingerprint: true
                    }
                }
    }

    post {
         always {
                bat '''
                    docker rm -f ecommerce-order-service-ci-postgres 2>NUL || exit /B 0
                '''
            }

        success {
            echo 'CI Pipeline completed successfully.'
        }

        failure {
            echo 'CI Pipeline failed. Check the console output.'
        }
    }
}