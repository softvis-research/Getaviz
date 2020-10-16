LOAD CSV WITH HEADERS FROM "file:///C:/Users/GPRA443/.Neo4jDesktop/neo4jDatabases/database-86261e4c-9e7b-4942-a1c9-4a1912dd6055/installation-3.5.12/import/l20_export.csv"
AS row FIELDTERMINATOR ';'
MATCH (a:Elements {object_name: row.Objektname})
SET a.migration_finding = 'true'