# README #

### What is this repository for? ###

* Software Visualization Generator (generative and model-driven software visualization)
* Last build: ![Development](https://codeship.com/projects/409e3130-0a2e-0133-98c2-269fed99bda5/status?branch=development)

### How do I get set up? ###
#### Automatically (recommended) ####

* Follow the instructions under [Oomph Setup](https://bitbucket.org/rimue/generator/wiki/Oomph Setup)

#### Manually (not recommended) ####

* Download [Eclipse IDE for Java and DSL Developers](https://www.eclipse.org/downloads/packages/eclipse-ide-java-and-dsl-developers/marsr)
* Set 64-bit JVM in Eclipse (Window → Preferences → Java → Installed JREs)
* Enable "Refresh using native hooks or polling" (Window → Preferences → General → Workspace)
* Set UTF-8 encoding (Window → Preferences → General → Workspace → Text file encoding → Other → UTF-8)
* Set workspace compliance level to 1.8 (Window → Preferences → Java → Compiler)
* Import all projects

### How do I build and run the generator? ###
* [Maven](https://bitbucket.org/rimue/generator/wiki/Maven) is used as build management system. It is used to build the generator, to run testcases and to run the mwe2 workflows
* All pre-configured Run configurations are accessible via *Run → Run Configurations → Maven Build*
* For the initial build of the generator run *Build everything* (Note: Only necessary by manual setup)
* For a detailed description of the different Maven configurations, click [here](https://bitbucket.org/rimue/generator/wiki/Maven)

### Contribution guidelines ###

* Workflow for PhD students: [Feature Branch Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/feature-branch-workflow)
* Workflow for students: [Forking Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/forking-workflow)
* Don't commit generated files like in *tmp*, *xtend-gen* and *src-gen*
* Commit only working and tested code
* Write tests and change existing tests if necessary
* Workflows (.mwe2) belong to *org.svis.generator.run*

