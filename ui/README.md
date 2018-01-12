# README #


### What is this repository for? ###

This repository holds the generic browser based user interface for [x3dom](https://www.x3dom.org/) software visualiztations build with [GETAVIZ](https://bitbucket.org/rimue/generator).
You find a hosted version at [https://home.uni-leipzig.de/svis/getaviz/Index.html](https://home.uni-leipzig.de/svis/getaviz/Index.html) (webgl supported browser and hardware required).


### How do I get set up? ###

*First Steps*

* Clone the repository into the web documents folder of your web server, for example [XAMPP](https://www.apac* hefriends.org/de/index.html) htdocs folder.
* Try to display the Index.html as a setup-test, that uses a simple user interface and a small visualizatin.

*Following Steps*

* The user interfaces has various definded setup definitions and can be used for any generated visualization of GETAVIZ.
* Use Index.php with the parameters **setup** and **model** to load a specific visualization in a specific user interface setup with the following (PHP)Syntax:
    * Syntax 
        * [*PATH TO SERVER*]**/index.php?setup=**[*PATH TO SETUP FILE AT THE "setups"-FOLDER*]**&model=**[*PATH TO MODEL FOLDER AT THE "data"-FOLDER*]
    * Example
        * [*http://localhost/repository/Generator%20UI*]**/index.php?setup=**[*web/webCity*]**&model=**[*City%20bricks%20freemind*]  
    * Ready to use example (if you have cloned the repository into the repository/Generator UI/ folder of your web documents folder)
        * http://localhost/repository/Generator%20UI/index.php?setup=web/webCity&model=City%20bricks%20freemind

*Additional Steps*

* For a new visualization of GETAVIZ you have to copy the model.x3d-file and the metadata.json-file into a new model folder at the "data"-folder
* After that the batch file "aopt-idmap-sapd.bat" has to be executed (copy it from another model folder)
* Use the index.php file as described above to show the new visualization at the browser user interface
* Optional you could add the sourcecode in a "src"-folder in your model folder


### Contribution guidelines ###

For contribution guidlines see [https://bitbucket.org/rimue/generator](https://bitbucket.org/rimue/generator)

*Architectur concepts of the UI*

* Model, View, Controllers
* Actions and Events

*Files, folders and meaning*

* root
* data
* libs
* scripts
* setups


### Who do I talk to? ###

[Software visualization research group at leipzig university](https://www.wifa.uni-leipzig.de/en/information-systems-institute/se/research/softwarevisualization-in-3d-and-vr.html)