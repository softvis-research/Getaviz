LOAD CSV WITH HEADERS FROM "file:///C:/Users/GPRA443/IdeaProjects/Getaviz_abap_integration/git/Getaviz/generator2/org.getaviz.generator/src/test/neo4jexport/20201007_Test_TypeOf.csv"
AS row FIELDTERMINATOR ';'
MATCH (a:Elements {element_id: row.element_id}), (b:Elements {element_id: row.type_of_id})
CREATE (a)-[r:TYPEOF]->(b)