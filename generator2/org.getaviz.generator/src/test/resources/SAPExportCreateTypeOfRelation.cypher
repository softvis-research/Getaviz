LOAD CSV WITH HEADERS FROM "file:///20200214_Test_TypeOf.csv"
AS row FIELDTERMINATOR ';'
MATCH (a:Elements {element_id: row.element_id}), (b:Elements {element_id: row.type_of_id})
CREATE (a)-[r:TYPEOF]->(b)