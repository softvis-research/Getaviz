LOAD CSV WITH HEADERS FROM "file:///C:/Users/G530358/Desktop/Anwendungen/JetBrains/Projekte/GetavizABAPIntegration/generator2/org.getaviz.generator/src/test/neo4jexport/20200214_Test_TypeOf.csv"
AS row FIELDTERMINATOR ';'
MATCH (a:Elements {element_id: row.element_id}), (b:Elements {element_id: row.type_of_id})
CREATE (a)-[r:TYPEOF]->(b)