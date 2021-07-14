package org.getaviz.generator.abap.lod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.database.DatabaseConnector;

import org.neo4j.driver.v1.types.Node;

import java.util.*;

public class LODRepository {

    private Log log = LogFactory.getLog(this.getClass());
    private DatabaseConnector connector = DatabaseConnector.getInstance();

    private Map<String, LODElement> elementsByHash;
    private Map<ACityElement, LODElement> elementsByReplacedElement;

    public LODRepository() {
        elementsByHash = new HashMap<>();
        elementsByReplacedElement = new HashMap<>();
    }

    public Collection<LODElement> getAllElements() {
        return new ArrayList(elementsByHash.values());
    }

    public LODElement getElementByHash(String hash){
        return elementsByHash.get(hash);
    }

    public LODElement findReplacementOf(ACityElement element) {
        return elementsByReplacedElement.get(element);
    }

    public void addElement(LODElement element) {
        elementsByHash.put(element.getHash(), element);
        // Lookup to avoid full traversal
        element.getReplacedElements().forEach(replaced -> elementsByReplacedElement.put(replaced, element));
    }

    public void deleteElement(LODElement element) {
        elementsByHash.remove(element.getHash(), element);
    }

    private int nodeCounter;
    private int relCounter;

    public void writeRepositoryToNeo4j() {
        nodeCounter = 0;
        relCounter = 0;
        log.info("Writing LOD elements to Neo4j");
        elementsByHash.forEach((id, element) -> createLODElementNode(element));
        elementsByHash.forEach((id, element) -> connectLODElementNode(element));
        log.info("Created " + nodeCounter + " new nodes and " + relCounter + " new relations");
    }

    private void createLODElementNode(LODElement element) {
        Node node = connector.addNode("CREATE ( n:LODElement { " + getProperties(element) + "})", "n");
        element.setNodeID(node.id());
        nodeCounter++;
    }

    private String getProperties(LODElement element) {
        StringBuilder propertyBuilder = new StringBuilder();

        propertyBuilder.append(" hash :  '"+ element.getHash() + "',");
        propertyBuilder.append(" color :  '" + element.getColor() + "',");
        propertyBuilder.append(" height :  " + element.getHeight() + ",");
        propertyBuilder.append(" width :  " + element.getWidth() + ",");
        propertyBuilder.append(" length :  " + element.getLength() + ",");
        propertyBuilder.append(" xPosition :  " + element.getXPosition() + ",");
        propertyBuilder.append(" yPosition :  " + element.getYPosition() + ",");
        propertyBuilder.append(" zPosition :  " + element.getZPosition() + ",");
        propertyBuilder.append(" aframeProperty : '" + element.getAframeProperty() + "'");

        return propertyBuilder.toString();
    }

    private void connectLODElementNode(LODElement element) {
        element.getReplacedElements().forEach(aCityElement -> {
            String statement = "MATCH (lod:LODElement), (replaced:Elements)" +
                    "WHERE ID(lod) =  " + element.getNodeID() +
                    "  AND ID(replaced) =  " + aCityElement.getNodeID() +
                    "  CREATE (lod)-[:REPLACES]->(replaced)";
            connector.executeWrite(statement);
            relCounter++;
        });
        element.getReplacedLODElements().forEach(lodElement -> {
            String statement = "MATCH (lod:LODElement), (replaced:LODElement)" +
                    "WHERE ID(lod) =  " + element.getNodeID() +
                    "  AND ID(replaced) =  " + lodElement.getNodeID() +
                    "  CREATE (lod)-[:REPLACES]->(replaced)";
            connector.executeWrite(statement);
            relCounter++;
        });
    }
}
