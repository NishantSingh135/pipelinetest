// Repository URL
REPOSITORY_URL = 'https://AbhijeetRukmangad@bitbucket.org/AbhijeetRukmangad/cicdappliaction.git'

// Credentials ID
CREDENTIALS_ID = 'gitaccount'

// Branch to be checked out
REPO_BRANCH = "*/master"

// Constant to define build variant as DEBUG
BUILD_DEBUG = 'debug';

// Constant to define build variant as RELEASE
BUILD_RELEASE= 'release';

// Type of build
BUILD_TYPE = BUILD_DEBUG;

//Path of apk
APK_PATH = '**/build/outputs/apk/*.apk'

// Path for Findbugs results
FINDBUGS_RESULT_PATH = '**/reports/findbugs/findbugs.xml'
FINDBUGS_RESULT_FILE = "findbugs.xml"

// Path for Checkstyle results
CHECKSTYLE_RESULT_PATH = '**/reports/checkstyle/checkstyle.xml'
CHECKSTYLE_RESULT_FILE = "checkstyle.xml"

TEST_RESULTS_PATH_PREFIX = '**/test-results/'

TEST_RESULTS_PATH_POSTFIX = '/*.xml'

// Path for automation unit test results
AUTOMATION_UNIT_TEST_RESULTS_PATH = "test/target/surefire-reports/testng-junit-results"

// Path for automation native test results
AUTOMATION_NATIVE_TEST_RESULTS_PATH = "test/target/surefire-reports/testng-native-results"

// Path of artifcats to be recorded.
RESULT_ARTIFACTS = '**/build/reports/checkstyle/*.xml,**/build/reports/findbugs/*.xml,**/build/test-results/*.*,**/build/outputs/apk/*.apk'


// Constant to define deployment target as Emulator
TARGET_DEVICE = 'd';

// Constant to define deployment target as Device
TARGET_EMULATOR = 'e';

// Type of target
TARGET_TYPE = TARGET_DEVICE;

// DL's for notifications
EMAIL_GROUP_DEV = "abhijeet.rukmangad@infostretch.com"
EMAIL_GROUP_QA = "abhijeet.rukmangad@infostretch.com"
EMAIL_GROUP_MGMT = "abhijeet.rukmangad@infostretch.com"
EMAIL_GROUP_ALL = "abhijeet.rukmangad@infostretch.com"
//"${EMAIL_GROUP_DEV},${EMAIL_GROUP_QA},${EMAIL_GROUP_MGMT}"

node {

    //Notify initiation
    notifyInitiation()
    catchError {

        /*
        Checkout: this stage is resposible for pulling the source code from SCM to workspace.
        */
        stage 'Checkout'
        log('checking out source')
        checkout()
        log('checkout success')

        /*
        Build: Builds the source code and generates APK file. 
        */
        stage 'Build'
        log('Building source')
        build()
        log('Build Success')

        /*
        Unit Test: Execute unit tests
        */
        stage 'Unit Test'
        log('Unit testing')
        unitTest()
        log('Unit test succeeded')

        /*
        Static Analysis: Does static code analysis to verify quality of source code.
        */
        stage 'Static code Analysis'
        log('Static Analysis')
        staticAnalysis()
        log('Analysis succeeded')

        /*
        Smoke Tests: Perform acceptance test.
        */
        stage 'Acceptance tests (Smoke)'
        log('Executing smoke tests')
        executeSmokeTests()
        log('Smoke testing succeeded')

        /*
        Regression Tests: perform accesptance test.
        */
        stage 'Acceptance tests (Regression)'
        log('Executing regression tests')
        executeRegressionTests()
        log('Regression testing succeeded')

        /*
        Release : Deploy the app to playstore
        */
        stage 'Release'
        notifyInputRequest()
        def releaseInput = input id: 'Release_prompt', message: 'Do you want to release build to play store? ',
              parameters: [[$class: 'ChoiceParameterDefinition', choices: 'Alpha\nBeta\nStaging\nUAT', description: 'If yes, which stage?', name: 'Type']]

        if (releaseInput == null) {
            log("Release stage was aborted on user choice.")
        } else {
            log("Releasing the application to play store at ${releaseInput} stage.")
            
            if (!release()) {
                log('stage:Release, failed')
                // Passing of this stage is not mandatory hence allowing script to continue.
            }

            log('stage:Release, succeeded')
        }
    
    }

    // Notify result
    if (currentBuild.result == "FAILURE") {
        echo "Notifying failure"
        notifyFailure()
    } else {
        currentBuild.result= "SUCCESS"
        notifySuccess()
    }

    echo "Result : ${currentBuild.result}"
    echo "description : ${currentBuild.description}"
    

}// End of node


// Fetch latest source code from repository
def checkout() {

    checkout([$class: 'GitSCM', 
        branches: [
            [name: '*/master']
        ], 
        doGenerateSubmoduleConfigurations: false, 
        extensions: [
            [$class: 'CleanBeforeCheckout'], 
            [$class: 'CheckoutOption', timeout: 10], 
            [$class: 'CloneOption', noTags: true, reference: '', shallow: true]
        ], 
        submoduleCfg: [], 
        userRemoteConfigs: [
            [credentialsId: CREDENTIALS_ID, url: REPOSITORY_URL]
        ]
    ])


    /*
    checkout([$class: 'GitSCM', 
               branches: [[name: REPO_BRANCH]], 
               doGenerateSubmoduleConfigurations: false,
               extensions: [[$class: 'CloneOption',depth: 2, noTags: true,
                              reference: '', shallow: true]],
               submoduleCfg: [],
               userRemoteConfigs: [[credentialsId: CREDENTIALS_ID,
                                                      url: REPOSITORY_URL]]])
    */

}

/* 
 This will build the source code
*/

def build() {
    dir('dev') {
        bat "gradlew clean assemble${BUILD_TYPE}"
    }
        
    //archive apk file
    archiveArtifacts(APK_PATH,"${BUILD_TYPE+".apk"}")
}

/* 
 This will run unit tests
*/
def unitTest() {
    echo "current dir is : " + pwd()
    dir('dev') {
        echo "current dir is : " + pwd()
        bat "gradlew test${BUILD_TYPE}unittest"
    }

    String unittestresultpath = "${TEST_RESULTS_PATH_PREFIX+BUILD_TYPE+TEST_RESULTS_PATH_POSTFIX}"
    
    //publish unit test results
    step([$class: 'JUnitResultArchiver',
        allowEmptyResults: false,
        testResults: unittestresultpath])

    //archive unit test results
    archiveArtifacts(unittestresultpath,"testresults")
}

/* 
 This will build the source code
*/
def staticAnalysis() {

    dir('dev') {
        bat 'injectlibrary.bat'
        bat 'gradlew checkstyle findbugs'
    }

    //publish results for findbugs
    step([
        $class: 'FindBugsPublisher',
         canComputeNew: false,
         defaultEncoding: '',
         excludePattern: '',
         healthy: '',
         canRunOnFailed: true,
         includePattern: '',
         pattern: FINDBUGS_RESULT_PATH,
         unHealthy: ''
    ])

    //publish results for checkstyle
    step([
        $class: 'CheckStylePublisher',
         canComputeNew: false,
         defaultEncoding: '',
         healthy: '',
         canRunOnFailed: true,
         pattern: CHECKSTYLE_RESULT_PATH,
         unHealthy: ''
    ])

    // archive checkstyle results
    archiveArtifacts(CHECKSTYLE_RESULT_FILE,CHECKSTYLE_RESULT_FILE)

    // archive findbugs results
    archiveArtifacts(FINDBUGS_RESULT_PATH,FINDBUGS_RESULT_FILE)
}


//Execute automation tests
def executeRegressionTests() {
    dir('test'){
        bat "mvn test"
    }

    // Publish results

    publishHTML(target:[allowMissing: true, 
        alwaysLinkToLastBuild: false, 
        keepAll: false, 
        reportDir: "${AUTOMATION_UNIT_TEST_RESULTS_PATH}", 
        reportFiles: 'index.html', 
        reportName: 'Automation unit-test report'
    ])
    
    echo "publishing unit test report"

    publishHTML(target:[allowMissing: true, 
        alwaysLinkToLastBuild: false, 
        keepAll: false, 
        reportDir: "${AUTOMATION_NATIVE_TEST_RESULTS_PATH}", 
        reportFiles: 'index.html', 
        reportName: 'Automation native-test report'
    ])


    // archive results
    archiveArtifacts(AUTOMATION_UNIT_TEST_RESULTS_PATH,"AutoUnitTests")

    archiveArtifacts(AUTOMATION_NATIVE_TEST_RESULTS_PATH,"AutoNativeTests")


}

//Execute smoke tests
def executeSmokeTests() {
    // Execute smoke tests
}


def release() {
        //Release app to store     
}


def notifyInitiation() {

    String msgbody = """
Hello,
    
    New build has been initiated for ${env.BUILD_TAG}. 
    Build URL: ${env.BUILD_URL}

Thanks,
Jenkins Team"""

    String msgsub = "[Jenkins:${env.BUILD_TAG}] Initiated."

    //Notify over slack
    sendMessageOverSlack("Build initiated for ${env.BUILD_TAG}. URL:${env.BUILD_URL}")

    //Notify over email
    sendSimpleMail(EMAIL_GROUP_ALL,msgsub, msgbody)

}


def notifyFailure() {

    String msgbody = """
Hello,

    ${env.BUILD_TAG} has failed due to error. ${env.description}

    For further details checkout ${env.BUILD_URL}

Thanks,
Jenkins Team"""

    String msgsub = "[Jenkins:${env.BUILD_TAG}] Failed."

    //Notify over slack
    sendMessageOverSlack("${env.BUILD_TAG} failed. URL:${env.BUILD_URL}")
    //Notify over email
    sendSimpleMail(EMAIL_GROUP_ALL,msgsub, msgbody)
}


def notifySuccess() {

    String msgbody = """
Hello,

    ${env.BUILD_TAG} was built successfully. Checkout results at ${env.BUILD_URL}

Thanks,
Jenkins Team""" 

    String msgsubject = "[Jenkins:${env.BUILD_TAG}] completed."

    //Notify over slack
    sendMessageOverSlack("${env.BUILD_TAG} successfully completed. URL:${env.BUILD_URL}")
    //Notify over email
    sendSimpleMail(EMAIL_GROUP_ALL,msgsubject,msgbody)        
}

def notifyInputRequest() {
    String msgbody = """
Hello,

    Build ${env.BUILD_TAG} is awaiting your input. Link ${env.BUILD_URL}.

Thanks,
Jenkins Team
    """
    String msgsubject = "Build ${env.BUILD_TAG} waiting for input."

    //Notify over slack
    sendMessageOverSlack(msgsubject)
    //Notify over email
    sendSimpleMail(EMAIL_GROUP_ALL,msgsubject,msgbody)
}


def archiveArtifacts(includes, targetfilename) {
    try {
        
        def server = Artifactory.server('CICDArtifactory')

        echo "uploading to ${server}"
        echo "includes : ${includes}"
        echo "target: ${env.BUILD_TAG+'_'+targetfilename}"
        def uploadSpec = "{\"files\": [{\"pattern\": \"${includes}\", \"target\": \"android_pipeline/${env.BUILD_TAG+'_'+targetfilename}\"}]}"
        echo "uploading ${uploadSpec}"
        server.upload(uploadSpec)
    } catch (err) {
        log("Error in archiving ${includes}");
    }
}


def log(message) {
    echo message
    sendMessageOverSlack(message)
}


def sendSimpleMail(email,subject,body) {
    try {
        mail bcc: '', 
        body: body, 
        cc: '', 
        charset: 'UTF-8', 
        from: '',
        mimeType: 'text/plain', replyTo: '', 
        subject: subject, 
        to: email
    } catch (err){
        echo err
        echo 'Failed to send email.'
    }
}


def sendMessageOverSlack(message) {
    try {
        slackSend channel: 'jenkins', color: 'green', message: message, teamDomain: 'cicd-abhijeet', token: 'X1jCRRRnOsTcbwy6R9BbCd5R'
    } catch (err) {
        echo err
        echo "failed to send message over slack"
    }
    
}