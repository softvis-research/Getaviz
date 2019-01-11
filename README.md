[![GitHub license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/softvis-research/Getaviz/blob/master/LICENSE)
[![Build Status](https://travis-ci.com/softvis-research/Getaviz.svg?branch=master)](https://travis-ci.com/softvis-research/Getaviz)  
# Getaviz

Getaviz is a toolset for designing, generating, and exploring software visualizations in 2D, 3D, and virtual reality (VR), supporting structural, behavioral and evolutional visualizations. An **online demo** of Getaviz you can find [here](https://home.uni-leipzig.de/svis/getaviz/index.php?setup=web/RD%20freemind&model=RD%20freemind).

## Features
* Supported software artifacts for analysis  
![Java](https://img.shields.io/badge/language-Java-blue.svg) ![Ruby](https://img.shields.io/badge/language-Ruby-blue.svg) ![C#](https://img.shields.io/badge/language-C%23-blue.svg)
![git](https://img.shields.io/badge/SCM-git-blue.svg)
![svn](https://img.shields.io/badge/SCM-svn-blue.svg)
* Supported output formats  
![x3d](https://img.shields.io/badge/3D-X3D-blue.svg)
![x3dom](https://img.shields.io/badge/3D-X3Dom-blue.svg)
![aframe](https://img.shields.io/badge/3D-A--Frame-blue.svg)
![aframe](https://img.shields.io/badge/VR-HTC_Vive-blue.svg)
* Supported visualization metaphors: 
  * Recursive Disk
  * City, City Bricks, City Floors, City Panels
  * Plant
  * MultiSphere
  * …
  
An academic publication about Getaviz you can find [here](https://www.researchgate.net/publication/320083290_GETAVIZ_Generating_Structural_Behavioral_and_Evolutionary_Views_of_Software_Systems_for_Empirical_Evaluation).

## How do I get set up? ###

Clone this repository and follow the instructions under [Installation & Setup](../../wiki/Installation-&-Setup).
Each subdirectory of this repository represents a standalone component of Getaviz. It contains a separate README.md with further instructions and documentation.

## Docker ##

Some Getaviz components are available as docker containers.  
[![UI](https://img.shields.io/badge/docker-ui-blue.svg)](https://hub.docker.com/r/getaviz/evaluationserver)  
[![Evaluation Server](https://img.shields.io/badge/docker-evaluationserver-blue.svg)](https://hub.docker.com/r/getaviz/ui)

## Wiki

Please have a look at our [Wiki](../../wiki/Home) which contains many additional information. However, documentation is still incomplete. Feel free to open an issue if you have any question!

## Development Team

### Main Contributors

Getaviz is developed by the research group [Visual Software Analytics](http://home.uni-leipzig.de/svis/) at Leipzig University. It has been developed over several years and is the basic for many scientific publications. In 2018 we released Getaviz as open source to simplify collaboration and practical use of our research prototype. 

Currently, four developers are contributung actively to Getaviz:
* [Richard Müller](https://github.com/rmllr)
* Jan Schilbach
* Pascal Kovacs
* [David Baum](http://home.uni-leipzig.de/svis/Research%20Group/#DavidBaum)

Have a look at our [website](https://home.uni-leipzig.de/svis/) for more information about our research group, visualization examples, and our publications. We are looking for collaborations with researchers and developers/companies. If you are interested, please contact us via email.

### Further Contributors

Many thanks to all the contributors who have improved Getaviz by implementing new features or fixing bugs, especially:

* Denise Zilch
* André Naumann
* [Stefan Faulhaber](https://github.com/StefanFaulhaber)
* [Dan Häberlein](https://github.com/dhaeb)
* Lisa Vogelsberg
