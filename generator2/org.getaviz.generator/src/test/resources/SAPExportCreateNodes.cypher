LOAD CSV WITH HEADERS FROM "file:///C:/Users/GPRA443/IdeaProjects/Getaviz_abap_integration/git/Getaviz/generator2/org.getaviz.generator/src/test/neo4jexport/20201007_Test.csv"
AS row FIELDTERMINATOR ';'
CREATE (n:Elements)
SET n = row