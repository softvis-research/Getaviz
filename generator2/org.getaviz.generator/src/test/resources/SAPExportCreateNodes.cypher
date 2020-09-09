LOAD CSV WITH HEADERS FROM "file:///C:/Users/G530358/Desktop/Anwendungen/JetBrains/Projekte/GetavizABAPIntegration/generator2/org.getaviz.generator/src/test/neo4jexport/20200214_Test.csv"
AS row FIELDTERMINATOR ';'
CREATE (n:Elements)
SET n = row