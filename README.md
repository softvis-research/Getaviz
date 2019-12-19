[![Build Status](https://travis-ci.com/softvis-research/Getaviz.svg?branch=master)](https://travis-ci.com/softvis-research/Getaviz)
[![Generator2](https://img.shields.io/badge/docker-generator2-blue.svg)](https://hub.docker.com/r/getaviz/generator2)
[![UI](https://img.shields.io/badge/docker-ui-blue.svg)](https://hub.docker.com/r/getaviz/evaluationserver)
[![Evaluation Server](https://img.shields.io/badge/docker-evaluationserver-blue.svg)](https://hub.docker.com/r/getaviz/ui)
# Getaviz

With Getaviz you can solve software engineering problems visually by exploring software artifacts in 2D, 3D, and virtual reality. Among other things it provides proper visualizations for identifying and refactoring antipatterns, locating runtime bottlenecks, assessing software quality, and tracking changes across multiple versions. To get a first impression, have a look at [our showcases](http://home.uni-leipzig.de/svis/Showcases/) that are available online. You can adopt the visualizations as well as the user interface according to your needs. Getaviz visualizes the structure and behavior of several programming languages (*Java*, *Ruby*, *PHP*, and *C#*) and can enrich these visualizations with evolutional information from *git* and *svn* repositories. They can be explored via *browser*, *HTC Vive*, or *Oculus Rift*. We support many different visualizations, e.g., Recursive Disk ([2D](https://home.uni-leipzig.de/svis/getaviz/index.php?setup=web/RD%20freemind&model=RD%20freemind) and [3D](https://home.uni-leipzig.de/svis/getaviz/index.php?setup=web/RD%20reek&model=RD%203D%20reek)), [City](https://home.uni-leipzig.de/svis/getaviz/index.php?setup=web/City%20freemind&model=City%20original%20freemind), [City Floors](https://home.uni-leipzig.de/svis/getaviz/index.php?setup=web/City%20freemind&model=City%20floor%20freemind), [City Bricks](https://home.uni-leipzig.de/svis/getaviz/index.php?setup=web/City%20freemind&model=City%20bricks%20freemind), and many more.

## How do I get set up? ###

Clone this repository and follow the instructions under [Installation & Setup](../../wiki/Installation-&-Setup).
Each subdirectory of this repository represents a standalone component of Getaviz. It contains a separate README.md with further instructions and documentation.

Please have a look at our Wiki which contains many additional information. However, documentation is still incomplete. Feel free to open an issue if you have any question!

## Development Team

Getaviz is mainly developed by the research group [Visual Software Analytics](http://softvis.wifa.uni-leipzig.de) at Leipzig University. Currently, four main developers are contributung actively to Getaviz:
* [Richard Müller](https://github.com/rmllr)
* [Jan Schilbach](https://github.com/schilbach)
* [Pascal Kovacs](https://github.com/PascalKovacs)
* [David Baum](http://home.uni-leipzig.de/svis/Research%20Group/#DavidBaum)

Many thanks to all the contributors who have improved Getaviz by implementing new features or fixing bugs, especially: Denise Zilch, [André Naumann](https://github.com/sk2andy), [Stefan Faulhaber](https://github.com/StefanFaulhaber), [Dan Häberlein](https://github.com/dhaeb),  [Lisa Vogelsberg](https://github.com/Valekta/), [Aaron Sillus](https://github.com/AaronSil), [Jens Thomann](https://github.com/jt23coqi), and[Xuefei Gao](https://github.com/SophiaLangheld)

## Publications
* David Baum, Jens Dietrich, Craig Anslow, Richard Müller: [Visualizing Design Erosion: How Big Balls of Mud are Made](https://arxiv.org/abs/1807.06136), IEEE VISSOFT, 2018.
* David Baum. Jan Schilbach, Pascal Kovacs, Ulrich Eisenecker, Richard Müller: [GETAVIZ: Generating Structural, Behavioral, and Evolutionary Views of Software Systems for Empirical Evaluation](https://www.researchgate.net/publication/320083290_GETAVIZ_Generating_Structural_Behavioral_and_Evolutionary_Views_of_Software_Systems_for_Empirical_Evaluation), IEEE VISSOFT, 2017.

A full list of publications you can find on [our website](http://home.uni-leipzig.de/svis/Publications/).
