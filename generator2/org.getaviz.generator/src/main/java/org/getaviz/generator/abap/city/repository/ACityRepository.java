package org.getaviz.generator.abap.city.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.abap.city.enums.SAPNodeProperties;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ACityRepository {

    private Log log = LogFactory.getLog(this.getClass());

    private DatabaseConnector connector = DatabaseConnector.getInstance();

    private Map<Long, ACityElement> elementsBySourceID;

    private Map<String, ACityElement> elementsByHash;

    private Map<ACityElement.ACityType, Map<String, ACityElement> > elementsByType;

    public ACityRepository(){
        elementsBySourceID = new HashMap<>();
        elementsByHash = new HashMap<>();
        elementsByType = new HashMap<>();

        log.info("created");
    }


    public Collection<ACityElement> getAllElements() {
        return new ArrayList(elementsBySourceID.values());
    }

    public Collection<ACityElement> getAllElementsByHash() {
        return new ArrayList(elementsByHash.values());
    }

    public ACityElement getElementBySourceID(Long sourceID){
        return elementsBySourceID.get(sourceID);
    }

    public ACityElement getElementByHash(String hash){
        return elementsByHash.get(hash);
    }

    public Collection<ACityElement> getElementsByType(ACityElement.ACityType type){
        Map<String, ACityElement> elementsByTypeMap = elementsByType.get(type);
        return new ArrayList(elementsByTypeMap.values());
    }

    public Collection<ACityElement> getElementsByTypeAndSourceProperty(ACityElement.ACityType type, SAPNodeProperties sourceProperty, String sourcePropertyValue){
        Collection<ACityElement> elementsByType = getElementsByType(type);
        List<ACityElement> elementsByTypeAndSourceProperty = new ArrayList<>();

        for (ACityElement element: elementsByType){

            Node sourceNode = element.getSourceNode();

            if(sourceNode == null ){
                ACityElement.ACitySubType subType = element.getSubType();
                String subTypeString = subType.toString();
                    if(!subTypeString.equals(sourcePropertyValue)){
                        continue;
                    }
                    elementsByTypeAndSourceProperty.add(element);

              continue;
            }

            Value propertyValue = sourceNode.get(sourceProperty.toString());
            if( propertyValue == null){
                continue;
            }

            String propertyValueString = propertyValue.asString();
            if(!propertyValueString.equals(sourcePropertyValue)){
                continue;
            }

            elementsByTypeAndSourceProperty.add(element);
        }

        return elementsByTypeAndSourceProperty;
    }


    //Schreiben der ACityElemente in die Neo4j-Datenbank
    public void writeRepositoryToNeo4j() {

        AtomicInteger cityBuildingCounter = new AtomicInteger(0);
        AtomicInteger cityFloorCounter = new AtomicInteger(0);
        AtomicInteger cityChimneyCounter = new AtomicInteger(0);
        AtomicInteger cityDistrictCounter = new AtomicInteger(0);

        elementsByHash.forEach((id, element) -> {
            //TODO Node mit Hash bereits in Neo4J vorhanden? -> Update der Properties

            /*if (element.getSourceNode().id() != 0){
                connector.executeWrite("MATCH (n:Elements) WHERE ID(n) = " + element.getNodeID() + " SET  n.properties = {" +  getACityProperties(element) + "}") ;
            }
             */

            Long aCityNodeID = connector.addNode("CREATE ( n:Elements { " + getACityProperties(element) + "})", "n").id();

            element.setNodeID(aCityNodeID);

            switch (element.getType()) {
                case Building:
                    cityBuildingCounter.getAndAdd(1); break;
                case Floor:
                    cityFloorCounter.getAndAdd(1); break;
                case Chimney:
                    cityChimneyCounter.getAndAdd(1); break;
                case District:
                    cityDistrictCounter.getAndAdd(1); break;
            }
        });

        log.info(cityFloorCounter + " new Floors added");
        log.info(cityBuildingCounter + " new Buildings added");
        log.info(cityChimneyCounter + " new Chimneys added");
        log.info(cityDistrictCounter + " new Districts added");

        AtomicInteger sourceRelationCounter = new AtomicInteger(0);
        AtomicInteger childRelationCounter = new AtomicInteger(0);
        elementsByHash.forEach((id, element) -> {
            Node elementsBySourceNode = element.getSourceNode();
            if (elementsBySourceNode != null) {

                String statement = "MATCH (sourceNode:Elements), (acityNode:Elements)" +
                        "WHERE ID(sourceNode) = " + elementsBySourceNode.id() +
                        "  AND ID(acityNode) =  " + element.getNodeID() +
                        "  CREATE (sourceNode)<-[r:SOURCE]-(acityNode)";

                connector.executeWrite(statement);
                sourceRelationCounter.addAndGet(1);
            }

            ACityElement parentElement = element.getParentElement();

            if (parentElement != null){
                String statement = "MATCH (acityNode:Elements), (acityParentNode:Elements)" +
                        "WHERE ID(acityNode) =  " + element.getNodeID() +
                        "  AND ID(acityParentNode) =  " + parentElement.getNodeID() +
                        "  CREATE (acityParentNode)-[r:CHILD]->(acityNode)";

                connector.executeWrite(statement);

                childRelationCounter.addAndGet(1);
            }
        });

        log.info(sourceRelationCounter.get() + " source relations created");
        log.info(childRelationCounter.get() + " child relations created");
    }

    public void writeACityElementsToNeo4j(ACityElement.ACityType aCityElementType){

        elementsByHash.forEach((id, element) -> {
            if(element.getType() == aCityElementType) {
                connector.executeWrite("CREATE ( :Elements { " + getACityProperties(element) + "})");

                Long aCityNodeID = connector.addNode("CREATE ( n:Elements { " + getACityProperties(element) + "})", "n").id();

                element.setNodeID(aCityNodeID);
            }
        });

            elementsByHash.forEach((id, element) -> {
                Node elementsBySourceNode = element.getSourceNode();
                if (elementsBySourceNode != null) {

                    String statement = "MATCH (sourceNode:Elements), (acityNode:Elements)" +
                            "WHERE ID(sourceNode) = " + elementsBySourceNode.id() +
                            "  AND ID(acityNode) =  " + element.getNodeID() +
                            "  CREATE (sourceNode)<-[r:SOURCE]-(acityNode)";

                    connector.executeWrite(statement);
                }
            });
    }

    private String getACityProperties(ACityElement element) {
        StringBuilder propertyBuilder = new StringBuilder();

        propertyBuilder.append(" cityType : '" + element.getType().toString() + "',");
        propertyBuilder.append(" hash :  '"+ element.getHash() + "',");
        propertyBuilder.append(" subType :  '" + element.getSubType() + "',");
        propertyBuilder.append(" color :  '" + element.getColor() + "',");
        propertyBuilder.append(" shape :  '" + element.getShape() + "',");
        propertyBuilder.append(" height :  " + element.getHeight() + ",");
        propertyBuilder.append(" width :  " + element.getWidth() + ",");
        propertyBuilder.append(" length :  " + element.getLength() + ",");
        propertyBuilder.append(" xPosition :  " + element.getXPosition() + ",");
        propertyBuilder.append(" yPosition :  " + element.getYPosition() + ",");
        propertyBuilder.append(" zPosition :  " + element.getZPosition() + "");

        return propertyBuilder.toString();
    }


    public void addElement(ACityElement element) {
        elementsByHash.put(element.getHash(), element);

        //add to type map
        ACityElement.ACityType elementType = element.getType();
        if (!elementsByType.containsKey(elementType)){
            elementsByType.put(elementType, new HashMap<>());
        }
        Map<String, ACityElement> elementsByTypeMap = elementsByType.get(elementType);
        elementsByTypeMap.put(element.getHash(), element);

        //add to source node id map
        if (element.getSourceNode() != null){
            elementsBySourceID.put(element.getSourceNodeID(), element);
        }
    }

    public void addElements(List<ACityElement> elements) {
        for( ACityElement element : elements ){
            addElement(element);
        }
    }

}
