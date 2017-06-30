REPOSITORY_URL="https://github.com/NishantSingh135/JenkinDemo"

node {

stage 'checkout'  
log('checking out source') 
checkout() 
log('checkout success')

stage'Build' 
log 'Building....'
    
stage 'Test'  
log 'Building....'
    
stage 'Deploy'  
log 'Deploying....'

}
       
 def checkout(){  
 	echo 'inside checkout method'
 }
    
