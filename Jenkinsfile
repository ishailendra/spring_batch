node {
  stage('SCM') {
    echo "Checkout scm step"
    checkout scm
    echo "Checkout scm complete"
  }
  stage('SonarQube Analysis') {
    def mvn = tool 'maven_tool';
    withSonarQubeEnv() {
      sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=ishailendra_spring_batch_AYno4-j1BJF0MTTHKNId -Dsonar.projectName='spring_batch'"
    }
  }
}
