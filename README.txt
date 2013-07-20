Geopod is an open source, Java-based plugin for the Integrated Data Viewer (IDV), a geoscience visualization software framework created by Unidata. Developed by the Computer Science and Earth Science departments at Millersville University through a National Science Foundation grant, Geopod is a learning tool designed to enhance undergraduate level earth science education by providing students and instructors with a novel and intuitive way to explore meteorological concepts. Geopod presents a new perspective and simplified interface for using some important features of traditional IDV, while including new features to enhance learning and facilitate classroom use.

SETTING UP AN ECLIPSE PROJECT USING EGit

1. Install EGit from the Eclipse marketplace

2. Create a new git project: File -> Import -> Git -> Projects from Git

3. The 'Select Repositotory Source' dialog will appear

  ~Select 'URI'

  ~Click 'Next'

4. The 'Source Git Repository' dialog will appear

  ~Fill in the fields as follows
  
    Location
      URI:  blank (it will be filled in automatically)
      Host: github.com
      Repository path: gzoppetti/geopod.git
      
    Connection
      Protocol: https
      Port:     blank
      
    Authentication
      User:     your github username
      Password: your github password
      Store in secure store: optional

  ~Click 'Next'

5. The 'Branch Selection' dialog will appear

  ~Select 'master'

6. The 'Local Destination' dialog will appear

  ~Fill in the fields as follows
  
    Destination
      Directory: where the remository will be cloned (most likely your Eclipse workspace)
      Initial branch:   master
      Clone submodules: unchecked

    Configuration
      Remote name: origin
      
  ~Click 'Finish'
  
7. Add a Java 7 or higher JRE System Library to the Build Path

8. Add the Java 3D jars to the Build Path. 
