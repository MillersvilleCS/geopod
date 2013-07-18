1. Install egit from the eclipse marketplace

2. create a new git project: file -> import -> git -> projects from git

3. the 'select repositotory source' screen  will come up

~select 'URI'

~click 'next'

4. the 'source git repository' screen will come up

~Do the following for each of the fields

  Location
    URI: leave blank, it will be filled in automatically
    Host:  enter 'github.com'
    Repository path: enter 'gzoppetti/geopod.git'
    
  Connection
    Protocol: select 'https'
    Port: leave blank
    
  Authentication
    User: your github username
    Password: your github password
    Store in secure store: optional

~click 'next'

5. the 'beanch selection' screen will come up

~make sure 'master' is selected

6. the 'local destination' screen will come up

~Do the following for each of the fields

  Destination
    Directory: where the remository will be cloned (you most likley want your eclipse workspace)
    Initial branch: 'master'
    Clone submodules: leave blank
  Configuration
    Remote name: 'origin'
