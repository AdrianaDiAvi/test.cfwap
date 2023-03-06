pipeline {
    agent any
    
    environment {
        SIMICS_SCRIPT = '/path/to/simics/script'
        SIMICS_COMMAND_START = 'start recording'
        SIMICS_COMMAND_STOP = 'stop recording'
        WORKLOAD = 'gnr-isa-tests'
        ARTIFACTORY_URL = 'https://artifactory.example.com'
        ARTIFACTORY_REPO = 'simics-data'
        ARTIFACTORY_PATH = 'PAIV/validation'
    }
    
    stages {
        stage('Execute SIMICS Script') {
            steps {
                script {
                    def simicsCommandStart = "${SIMICS_SCRIPT} -e '${SIMICS_COMMAND_START}'"
                    def simicsCommandStop = "${SIMICS_SCRIPT} -e '${SIMICS_COMMAND_STOP}'"
                    def simicsCommandStatus = "${SIMICS_SCRIPT} -e 'status'"
                    def simicsRegionStart = false
                    def simicsDataFilePath
                    
                    // Execute SIMICS script with start command
                    sh simicsCommandStart
                    
                    // Start workload and wait for region-of-interest start notification
                    sh "simics -e 'run ${WORKLOAD}'; echo 'Waiting for region-of-interest start notification...'"
                    sh "tail -f simics.log | while read LOGLINE; do echo \${LOGLINE}; grep -q 'Region-of-Interest start' <(echo \${LOGLINE}) && echo 'Region-of-Interest started' && pkill -P \$\$ tail; done"
                    simicsRegionStart = true
                    
                    // Execute SIMICS command to start recording data
                    sh simicsCommandStatus
                    sh simicsCommandStart
                    
                    // Wait for region-of-interest completion notification
                    sh "tail -f simics.log | while read LOGLINE; do echo \${LOGLINE}; grep -q 'Region-of-Interest complete' <(echo \${LOGLINE}) && echo 'Region-of-Interest completed' && pkill -P \$\$ tail; done"
                    simicsRegionStart = false
                    
                    // Execute SIMICS command to stop recording data
                    sh simicsCommandStatus
                    sh simicsCommandStop
                    
                    // Set the path for the data file
                    simicsDataFilePath = "${env.WORKSPACE}/simics-data.bin"
                    
                    // Upload data file to Artifactory
                    withCredentials([usernamePassword(credentialsId: 'artifactory-creds', passwordVariable: 'ARTIFACTORY_PASSWORD', usernameVariable: 'ARTIFACTORY_USERNAME')]) {
                        sh "curl -u \${ARTIFACTORY_USERNAME}:\${ARTIFACTORY_PASSWORD} -X PUT '${ARTIFACTORY_URL}/artifactory/${ARTIFACTORY_REPO}/${ARTIFACTORY_PATH}/simics-data.bin' -T \${simicsDataFilePath}"
                    }
                }
            }
        }
    }
}
