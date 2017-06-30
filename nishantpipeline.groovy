REPOSITORY_URL="https://github.com/NishantSingh135/JenkinDemo"

node {


stage 'checkout' 
log('checking out source')
checkout()
log('checkout success')

    stage'Build'
        echo 'Building....'
    
    stage 'Test' 
        echo 'Building....'
    
    stage 'Deploy' 
        echo 'Deploying....'

        def checkout(){
         	log ('in progress')
        }
    
}