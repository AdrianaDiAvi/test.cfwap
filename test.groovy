#!/usr/bin/env groovy

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
        ONESOURCE_CFWAP_REPO =   "https://github.com/intel-innersource/applications.infrastructure.services-framework.wl-analysis-initial-checkpoint.git"
        ONESOURCE_REPO = "https://github.com/intel-innersource/applications.benchmarking.benchmark.platform-hero-features.git"
        ONESOURCE_DIR = "platform-hero-features"
        MAIN_BRANCH='master'
    }

    stages {
        stage('Setup Repos'){
            steps {
            sh '''
            mkdir ${ONESOURCE_CFWAP_DIR}
            git clone https://${GITHUB_CREDS_USR}:${GITHUB_CREDS_PSW}@${ONESOURCE_CFWAP_REPO} ${ONESOURCE_CFWAP_DIR}
            mkdir ${ONESOURCE_DIR}
            git clone https://${GITHUB_CREDS_USR}:${GITHUB_CREDS_PSW}@${ONESOURCE_REPO} ${ONESOURCE_DIR}
            rsync -av --exclude .git ${ONESOURCE_DIR} ${ONESOURCE_CFWAP_DIR}/
           '''
            }
        }

    }

}