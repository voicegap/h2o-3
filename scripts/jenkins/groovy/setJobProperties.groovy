def call() {
  def commitMessage = load('h2o-3/scripts/jenkins/groovy/commitMessage.groovy')
  properties(
    [
      parameters(
        [
          booleanParam(defaultValue: true, description: 'If NOT checked, execute only failed stages from PREVIOUS build', name: 'runAllStages')
        ]
      ),
      buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '25'))
    ]
  )
  currentBuild.description = commitMessage()
}

return this
