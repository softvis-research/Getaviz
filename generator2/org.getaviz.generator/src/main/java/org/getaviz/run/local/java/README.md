<h2>Java Metropolis</h2>
Each next step executes all previous steps.
After each step the data is stored in the database 
and in the repository.

<h4>LoaderStep</h4>
- load java artifacts(JAR-Files) to Neo4j DB.

<h4>CreatorStep</h4>
CreatorStep :
- creates ACityRepo Label with nodes and converting packages, 
classes and interfaces to districts, methods and attributes to buildings

<h4>LayouterStep</h4>
- add (x,y,z) Position in 3D-World
- add length, width and high to created models

<h4>DesignerStep</h4>
- add shapes 
- add colours

<h4>MetaDataExporterStep</h4>
- add metadata for each model
- creates metaData.json

<h4>AFrameExporterStep</h4>
- creates model.html


