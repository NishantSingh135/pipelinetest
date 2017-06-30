REPOSITORY_URL="https://github.com/NishantSingh135/JenkinDemo"
// Type of build
BUILD_TYPE = BUILD_DEBUG;

node {

stage 'checkout'  
echo('checking out source') 
checkout() 
echo('checkout success')

stage'Build' 
echo 'Building....'
build()
    
stage 'Test'  
echo 'Building....'
    
stage 'Deploy'  
echo 'Deploying....'

}
       
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

 def build(){
  bat "gradlew clean assemble${BUILD_TYPE}"
 }
    
