LOAD CSV WITH HEADERS FROM "file:///C:/Users/ToniBeere/Documents/GitHub/Getaviz/generator2/org.getaviz.generator/src/test/neo4jexport/20200214_Test.csv"
AS row FIELDTERMINATOR ';'
CREATE (n:Elements)
SET n = row