pipeline {
    agent any

    tools {
        jdk 'jdk17'          // name must match what you configured in Jenkins Global Tools
        maven 'Maven-3.9'    // name must match your Maven in Jenkins Global Tools
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Vinod12024/MSIL-RestAssured.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Allure Report') {
            steps {
                allure([
                    includeProperties: false,
                    jdk: '',
                    results: [[path: 'target/allure-results']]
                ])
            }
        }
    }
}
