pipeline {
    agent { label '!windows' }
    options { skipStagesAfterUnstable() }
    stages {
        stage('Build') {
            steps { sh 'mvn -B -DskipTests clean package' }
        }
        stage('Test') {
            steps { sh 'mvn -B test' }
            post {
                always { junit 'target/surefire-reports/*.xml' }
            }
        }
        stage('Deliver') {
            steps { sh './jenkins/scripts/deliver.sh' }
        }
    }
    post {
        success { archiveArtifacts artifacts: 'target/programa-6a-*.jar, output/output6.txt', fingerprint: true }
    }
}
