#!/usr/bin/env groovy
def ion_remote_connection (){
  def remote = [:]
  remote.name = "ion"
  remote.host = "simcloud1.intel.com"
  remote.allowAnyHosts = true
  remote.timeoutSec = 150
  remote.retryCount = 3
  remote.retryWaitSec = 5
  remote.user=AARCEMOR_ZSC2_ION_SSH_KEY
  remote.identityFile=AARCEMOR_ZSC2_ION_SSH_KEY_USR


  return remote
}

pipeline {
    agent {
        label 'service-fw'
    }
    options {
        timestamps()
    }
        environment {
        GITHUB_CREDS = credentials('one-source-token')
        ONESOURCE_CFWAP_DIR = "pre-silicon-cfwap"
        ONESOURCE_CFWAP_REPO =   "github.com/intel-innersource/applications.infrastructure.services-framework.wl-analysis-initial-checkpoint.git"
        ONESOURCE_REPO = "github.com/intel-innersource/applications.benchmarking.benchmark.platform-hero-features.git"
        ONESOURCE_DIR = "platform-hero-features"
        MAIN_BRANCH='master'
    }

    stages {
        stage('Setup Repos'){
            steps {
                script{
                    sh '''
                    git clone https://${GITHUB_CREDS_USR}:${GITHUB_CREDS_PSW}@${ONESOURCE_CFWAP_REPO} ${ONESOURCE_CFWAP_DIR}
                    git clone https://${GITHUB_CREDS_USR}:${GITHUB_CREDS_PSW}@${ONESOURCE_REPO} ${ONESOURCE_DIR}
                    rsync -av --exclude .git ${ONESOURCE_DIR} ${ONESOURCE_CFWAP_DIR}/
                    '''
                    sshCommand remote: ion_remote_connection(), command: "cd /nfs/site/disks/wl_analysis_automation_01/aarcemor/jenkins/"
                }
            }
        }

    }

    post {
    always {
        cleanWs()
        }
    }

}