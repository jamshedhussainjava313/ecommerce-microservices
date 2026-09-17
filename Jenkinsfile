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

        stage('Verify Docker Access') {
            steps {
                bat 'docker version'
            }
        }

        stage('Start CI PostgreSQL') {
            steps {
                bat '''
                    docker rm -f ecommerce-user-service-ci-postgres 2>NUL || exit /B 0

                    docker run -d ^
                      --name ecommerce-user-service-ci-postgres ^
                      -e POSTGRES_USER=ecommerce_user ^
                      -e POSTGRES_PASSWORD=%DB_PASSWORD% ^
                      -e POSTGRES_DB=user_db ^
                      -p 5434:5432 ^
                      postgres:16
                '''

                bat '''
                    echo Waiting for CI PostgreSQL...

                    :waitloop
                    docker exec ecommerce-user-service-ci-postgres ^
                      pg_isready -U ecommerce_user -d user_db

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
                        dir('user-service') {
                            bat 'mvn test -Dtest.groups=integration'
                        }
                    }
                }

        stage('Generate Code Coverage') {
                    steps {
                        dir('user-service') {
                            bat 'mvn jacoco:report'
                        }
                    }
                }

        stage('SonarQube Analysis') {
            steps {
                dir('user-service') {
                    withSonarQubeEnv('SonarQube-Local') {
                        bat 'mvn sonar:sonar -Dsonar.projectKey=ecommerce-user-service -Dsonar.token=%SONAR_TOKEN%'
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
                        archiveArtifacts artifacts: 'user-service/target/site/jacoco/**',
                                         fingerprint: true
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