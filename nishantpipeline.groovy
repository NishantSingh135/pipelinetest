REPOSITORY_URL="https://github.com/NishantSingh135/JenkinDemo"
// Type of build
BUILD_TYPE = 'BUILD_DEBUG';

node('android_node'){

stage 'checkout'  
echo('checking out source') 
checkout() 
echo('checkout success')

stage'Build' 
echo 'Building....'
build()
    
stage 'Test'  
echo 'Test....'
unitTest()
    
stage 'Deploy'  
echo 'Deploying....'

}
       
 /*
 this will checkout the source from git
 */      
 def checkout(){  
 	checkout([$class: 'GitSCM', 
 		branches: [[name: '*/master']], 
 		doGenerateSubmoduleConfigurations: false, 
 		extensions: [], 
 		submoduleCfg: [], 
 		userRemoteConfigs: [
 		[url: REPOSITORY_URL]
 		]

 		])

 }

/*
This will build the project
*/
 def build(){
  bat "gradlew clean assemble"
 }

 /*
 This will unit test 
 */
    
 def unitTest(){
 	echo "current directory is :" + pwd()
 	bat "gradlew test"
 	junit '**/test-results/testDebugUnitTest/*.xml'
 	archiveArtifacts '**/test-results/testDebugUnitTest/*.xml'
 }   
