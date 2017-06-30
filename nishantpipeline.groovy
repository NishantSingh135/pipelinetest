REPOSITORY_URL="https://github.com/NishantSingh135/JenkinDemo"

node {

stage 'checkout'  
echo('checking out source') 
checkout() 
echo('checkout success')

stage'Build' 
echo 'Building....'
    
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
    
