node {
  stage('SCM') {
    checkout scm
  }
  stage('SonarQube Analysis') {
    def mvn = tool 'maven_tool';
    withSonarQubeEnv() {
      sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=ishailendra_spring_batch_AYno4-j1BJF0MTTHKNId -Dsonar.projectName='spring_batch'"
    }
  }
}
