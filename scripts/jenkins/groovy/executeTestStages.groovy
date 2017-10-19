def call(mode, nodeLabel, changesMap) {

  def MODE_PR_TESTING_CODE = -1
  def MODE_PR_CODE = 0
  def MODE_MASTER_CODE = 1
  def MODE_NIGHTLY_CODE = 2
  def MODES = [
    [name: 'MODE_PR_TESTING', code: MODE_PR_TESTING_CODE],
    [name: 'MODE_PR', code: MODE_PR_CODE],
    [name: 'MODE_MASTER', code: MODE_MASTER_CODE],
    [name: 'MODE_NIGHTLY', code: MODE_NIGHTLY_CODE]
  ]

  // Job will execute PR_STAGES only if these are green.
  def SMOKE_STAGES = [
    [
      stageName: 'Py2.7 Smoke', target: 'test-py-smoke', pythonVersion: '2.7',
      timeoutValue: 8, lang: 'py'
    ],
    [
      stageName: 'R3.4 Smoke', target: 'test-r-smoke', rVersion: '3.4.1',
      timeoutValue: 8, lang: 'r'
    ],
    [
      stageName: 'PhantomJS Smoke', target: 'test-phantom-js-smoke',
      timeoutValue: 10, lang: 'js'
    ]
  ]

  // Stages for PRs in testing phase, executed after each push to PR.
  def PR_TESTING_STAGES = [
    [
      stageName: 'Py2.7 Demos', target: 'test-py-demos', pythonVersion: '2.7',
      timeoutValue: 15, lang: 'py'
    ],
    [
      stageName: 'Py2.7 Init', target: 'test-py-init', pythonVersion: '2.7',
      timeoutValue: 5, hasJUnit: false, lang: 'py'
    ],
    [
      stageName: 'Py2.7 Small', target: 'test-pyunit-small', pythonVersion: '2.7',
      timeoutValue: 45, lang: 'py'
    ],
    [
      stageName: 'Py3.5 Small', target: 'test-pyunit-small', pythonVersion: '3.5',
      timeoutValue: 45, lang: 'py'
    ],
    [
      stageName: 'Py3.6 Small', target: 'test-pyunit-small', pythonVersion: '3.6',
      timeoutValue: 45, lang: 'py'
    ],
    [
      stageName: 'R3.4 Init', target: 'test-r-init', rVersion: '3.4.1',
      timeoutValue: 5, hasJUnit: false, lang: 'r'
    ],
    [
      stageName: 'R3.4 Small', target: 'test-r-small', rVersion: '3.4.1',
      timeoutValue: 90, lang: 'r'
    ],
    [
      stageName: 'R3.4 Medium-large', target: 'test-r-medium-large', rVersion: '3.4.1',
      timeoutValue: 70, lang: 'r'
    ],
    [
      stageName: 'R3.4 CMD Check', target: 'test-r-cmd-check', rVersion: '3.4.1',
      timeoutValue: 15, hasJUnit: false, lang: 'r'
    ],
    [
      stageName: 'R3.4 CMD Check as CRAN', target: 'test-r-cmd-check-as-cran', rVersion: '3.4.1',
      timeoutValue: 10, hasJUnit: false, lang: 'r'
    ],
    [
      stageName: 'R3.4 Demos Small', target: 'test-r-demos-small', rVersion: '3.4.1',
      timeoutValue: 15, lang: 'r'
    ]
  ]

  // Stages executed after each push to PR branch.
  def PR_STAGES = [
    [
      stageName: 'Py2.7 Booklets', target: 'test-py-booklets', pythonVersion: '2.7',
      timeoutValue: 40, lang: 'py'
    ],
    [
      stageName: 'Py2.7 Demos', target: 'test-py-demos', pythonVersion: '2.7',
      timeoutValue: 15, lang: 'py'
    ],
    [
      stageName: 'Py2.7 Init', target: 'test-py-init', pythonVersion: '2.7',
      timeoutValue: 5, hasJUnit: false, lang: 'py'
    ],
    [
      stageName: 'Py2.7 Small', target: 'test-pyunit-small', pythonVersion: '2.7',
      timeoutValue: 45, lang: 'py'
    ],
    [
      stageName: 'Py3.5 Small', target: 'test-pyunit-small', pythonVersion: '3.5',
      timeoutValue: 45, lang: 'py'
    ],
    [
      stageName: 'Py3.6 Small', target: 'test-pyunit-small', pythonVersion: '3.6',
      timeoutValue: 45, lang: 'py'
    ],
    [
      stageName: 'R3.4 Init', target: 'test-r-init', rVersion: '3.4.1',
      timeoutValue: 5, hasJUnit: false, lang: 'r'
    ],
    [
      stageName: 'R3.4 Small', target: 'test-r-small', rVersion: '3.4.1',
      timeoutValue: 90, lang: 'r'
    ],
    [
      stageName: 'R3.4 Small Client Mode', target: 'test-r-small-client-mode', rVersion: '3.4.1',
      timeoutValue: 120, lang: 'r'
    ],
    [
      stageName: 'R3.4 CMD Check', target: 'test-r-cmd-check', rVersion: '3.4.1',
      timeoutValue: 15, hasJUnit: false, lang: 'r'
    ],
    [
      stageName: 'R3.4 CMD Check as CRAN', target: 'test-r-cmd-check-as-cran', rVersion: '3.4.1',
      timeoutValue: 10, hasJUnit: false, lang: 'r'
    ],
    [
      stageName: 'R3.4 Booklets', target: 'test-r-booklets', rVersion: '3.4.1',
      timeoutValue: 50, lang: 'r'
    ],
    [
      stageName: 'R3.4 Demos Small', target: 'test-r-demos-small', rVersion: '3.4.1',
      timeoutValue: 15, lang: 'r'
    ],
    [
      stageName: 'PhantomJS', target: 'test-phantom-js',
      timeoutValue: 45, lang: 'js'
    ],
    [
      stageName: 'Py3.6 Medium-large', target: 'test-pyunit-medium-large', pythonVersion: '3.5',
      timeoutValue: 90, lang: 'py'
    ],
    [
      stageName: 'R3.4 Medium-large', target: 'test-r-medium-large', rVersion: '3.4.1',
      timeoutValue: 70, lang: 'r'
    ],
    [
      stageName: 'R3.4 Demos Medium-large', target: 'test-r-demos-medium-large', rVersion: '3.4.1',
      timeoutValue: 120, lang: 'r'
    ]
  ]

  // Stages executed in addition to PR_STAGES after merge to master.
  def MASTER_STAGES = [
    [
      stageName: 'Py2.7 Medium-large', target: 'test-pyunit-medium-large', pythonVersion: '2.7',
      timeoutValue: 90, lang: 'py'
    ],
    [
      stageName: 'Py3.5 Medium-large', target: 'test-pyunit-medium-large', pythonVersion: '3.5',
      timeoutValue: 90, lang: 'py'
    ],
    [
      stageName: 'R3.4 Datatable', target: 'test-r-datatable', rVersion: '3.4.1',
      timeoutValue: 20, lang: 'r'
    ],
    [
      stageName: 'PhantomJS Small', target: 'test-phantom-js-small',
      timeoutValue: 45, lang: 'js'
    ],
    [
      stageName: 'PhantomJS Medium', target: 'test-phantom-js-medium',
      timeoutValue: 45, lang: 'js'
    ]
  ]

  // Stages executed in addition to MASTER_STAGES, used for nightly builds.
  def NIGHTLY_STAGES = [
    [
      stageName: 'R3.3 Medium-large', target: 'test-r-medium-large', rVersion: '3.3.3',
      timeoutValue: 70, lang: 'r'
    ],
    [
      stageName: 'R3.3 Small', target: 'test-r-small', rVersion: '3.3.3',
      timeoutValue: 90, lang: 'r'
    ],
    [
      stageName: 'R3.3 Small Client Mode', target: 'test-r-small-client-mode', rVersion: '3.3.3',
      timeoutValue: 90, lang: 'r'
    ],
    [
      stageName: 'R3.3 CMD Check', target: 'test-r-cmd-check', rVersion: '3.3.3',
      timeoutValue: 15, hasJUnit: false, lang: 'r'
    ],
    [
      stageName: 'R3.3 CMD Check as CRAN', target: 'test-r-cmd-check-as-cran', rVersion: '3.3.3',
      timeoutValue: 10, hasJUnit: false, lang: 'r'
    ]
  ]

  // run smoke tests, the tests relevant for this mode
  executeInParallel(SMOKE_STAGES, nodeLabel)

  def modeCode = MODES.find{it['name'] == mode}['code']
  // FIXME: Remove the if and KEEP only the else once the initial PR tests in real environment are completed
  def jobs = null
  if (modeCode == MODE_PR_TESTING_CODE) {
    jobs = PR_TESTING_STAGES
  } else {
    jobs = PR_STAGES
    if (modeCode >= MODE_MASTER_CODE) {
      jobs += MASTER_STAGES
    }
    if (modeCode >= MODE_NIGHTLY_CODE) {
      jobs += NIGHTLY_STAGES
    }
  }
  executeInParallel(jobs, nodeLabel, changesMap)
}

def executeInParallel(jobs, nodeLabel, changesMap = null) {
  parallel(jobs.collectEntries { c ->
    [
      c['stageName'], {
        defaultTestPipeline(nodeLabel, changesMap) {
          stageName = c['stageName']
          target = c['target']
          pythonVersion = c['pythonVersion']
          rVersion = c['rVersion']
          timeoutValue = c['timeoutValue']
          hasJUnit = c['hasJUnit']
          lang = c['lang']
        }
      }
    ]
  })
}

def defaultTestPipeline(nodeLabel, changesMap, body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  if (config.pythonVersion == null) {
    config.pythonVersion = '3.5'
  }
  if (config.rVersion == null) {
    config.rVersion = '3.4.1'
  }
  if (config.timeoutValue == null) {
    config.timeoutValue = 60
  }
  if (config.hasJUnit == null) {
    config.hasJUnit = true
  }

  node(nodeLabel) {
    echo "Pulling scripts"
    step ([$class: 'CopyArtifact',
      projectName: env.JOB_NAME,
      filter: "h2o-3/scripts/jenkins/groovy/*",
      selector: [$class: 'SpecificBuildSelector', buildNumber: env.BUILD_ID]
    ]);

    def insideDocker = load('h2o-3/scripts/jenkins/groovy/insideDocker.groovy')
    def buildTarget = load('h2o-3/scripts/jenkins/groovy/buildTarget.groovy')
    def customEnv = load('h2o-3/scripts/jenkins/groovy/customEnv.groovy')

    def buildEnv = customEnv() + ["PYTHON_VERSION=${config.pythonVersion}", "R_VERSION=${config.rVersion}"]

    insideDocker(buildEnv, config.timeoutValue, 'MINUTES') {
      stage(config.stageName) {
        if (changesMap[config.lang] != null && changesMap[config.lang]) {
          echo "###### Changes for ${config.lang} detected, starting ${config.stageName}######"
          def stageDir = stageNameToDirName(config.stageName)
          def h2oFolder = stageDir + '/h2o-3'
          dir(stageDir) {
            deleteDir()
          }

          unpackTestPackage(config.lang, stageDir)

          if (config.lang == 'py') {
            installPythonPackage(h2oFolder)
          }

          if (config.lang == 'r') {
            installRPackage(h2oFolder)
          }

          buildTarget {
            target = config.target
            hasJUnit = config.hasJUnit
            h2o3dir = h2oFolder
          }
        } else {
          echo "###### No changes for ${config.lang}, skipping ${config.stageName}######"
        }
      }
    }
  }
}

def installPythonPackage(String h2o3dir) {
  sh """
    echo "Activating Python ${env.PYTHON_VERSION}"
    . /envs/h2o_env_python${env.PYTHON_VERSION}/bin/activate
    pip install ${h2o3dir}/h2o-py/dist/*.whl
  """
}

def installRPackage(String h2o3dir) {
  sh """
    echo "Activating R ${env.R_VERSION}"
    activate_R_${env.R_VERSION}
    R CMD INSTALL ${h2o3dir}/h2o-r/R/src/contrib/h2o*.tar.gz
  """
}

def unpackTestPackage(lang, String stageDir) {
  echo "Pulling test package"
  step ([$class: 'CopyArtifact',
    projectName: env.JOB_NAME,
    fingerprintArtifacts: true,
    filter: "h2o-3/test-package-${lang}.zip, h2o-3/build/h2o.jar",
    selector: [$class: 'SpecificBuildSelector', buildNumber: env.BUILD_ID],
    target: stageDir + '/'
  ]);
  sh "cd ${stageDir}/h2o-3 && unzip test-package-${lang}.zip && rm test-package-${lang}.zip"
}

def stageNameToDirName(String stageName) {
  if (stageName != null) {
    return stageName.toLowerCase().replace(' ', '-')
  }
  return null
}

return this
