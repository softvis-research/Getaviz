# README #

## What is this repository for?

The generator takes a famix, dynamic, and/or hismo model as input and generates a software visualization that can be explored using the ui component.

## How do I build and run the generator?
* [Maven](../wiki/Maven) is used as build management system. It is used to build the generator, to run testcases and to run the mwe2 workflows
* All pre-configured Run configurations are accessible via *Run → Run Configurations → Maven Build*
* For a detailed description of the different Maven configurations, click [here](new link)

## Contribution Guidelines

* Don't commit generated files like in *tmp*, *xtend-gen* and *src-gen*
* Commit only working and tested code
* Write tests and change existing tests if necessary
* Generator Workflows (.mwe2) belong to *org.svis.generator.run*

## Further Documentation

* [Overview](../wiki/Generator%20Overview)
* [Generation Process](../wiki/Generation%20Process)
* [Maven](../wiki/Maven)
* [Testing Process](../wiki/Testing%20Process%20Generator)
* [Xtend Coding Guidelines](../wiki/Xtend%20Coding%20Guidelines)

