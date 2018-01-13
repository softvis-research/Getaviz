# Getaviz

Getaviz is a toolset for designing and generating software visualizations in 2D and 3D, supporting structural, behavioral and evolutional visualizations. It is continously developed by Leipzig University. An academic publication about Getaviz can be found [here](https://www.researchgate.net/publication/320083290_GETAVIZ_Generating_Structural_Behavioral_and_Evolutionary_Views_of_Software_Systems_for_Empirical_Evaluation).
An online demo of Getaviz you can find [here](https://home.uni-leipzig.de/svis/getaviz/Index.html).

Supported languages: Java, Ruby, and C#  
Supported version control systems: git and svn  
Supported output formats: X3D, X3DOM, A-Frame  
Supported visualization metaphors: Recursive Disk, City, Plant, and MultiSphere including several variants  

* Last build: ![Development](https://codeship.com/projects/409e3130-0a2e-0133-98c2-269fed99bda5/status?branch=development)

### How do I get set up? ###

Getaviz contains multiple standalone components, that are described below. The recommended way is not to clone this repository, but to follow the instructions under [Oomph Setup](NEW LINK). This will clone the repository and install a suitable eclipse installation.

### How do I build and run the generator? ###
* [Maven](NEW LINK) is used as build management system. It is used to build the generator, to run testcases and to run the mwe2 workflows
* All pre-configured Run configurations are accessible via *Run → Run Configurations → Maven Build*
* For a detailed description of the different Maven configurations, click [here](new link)

### How do I build and run the ui? ###

* Run `docker-compose up` in the ui directory
* Open `localhost:8082` in your web browser

### How do I build and run the evaluation server? ###

* Run `docker-compose up` in the evaluationserver directory
* Open `localhost:8081` in your web browser

### Contribution guidelines ###

* Don't commit generated files like in *tmp*, *xtend-gen* and *src-gen*
* Commit only working and tested code
* Write tests and change existing tests if necessary
* Generator Workflows (.mwe2) belong to *org.svis.generator.run*