package org.getaviz.generator.abap.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeLabels;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.database.DatabaseConnector;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SourceNodeRepository {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;
    private DatabaseConnector connector = DatabaseConnector.getInstance();

    /* Node not implements comparable interface to use Sets
     *   -> use Maps with ID to Node */

    private Map<Long, Node> nodeById;

    private Map<String, Map<Long, Node>> nodesByLabel;

    private Map<String,
            Map<Boolean,
                    Map<Long,
                            Map<Long, Node>> > > nodesByRelation;


    public SourceNodeRepository(){
        nodeById = new HashMap<>();
        nodesByLabel = new HashMap<>();
        nodesByRelation = new HashMap<>();

        log.info("Created");
    }



    public void loadNodesByPropertyValue(SAPNodeProperties property, String value){

        AtomicInteger counter = new AtomicInteger(0);

        connector.executeRead("MATCH (n:Elements {" + property + ": '" + value + "'}) RETURN n")
                .forEachRemaining((result) -> {
                    Node sourceNode = result.get("n").asNode();

                    addNodeByID(sourceNode);
                    addNodesByProperty(sourceNode);

                    counter.addAndGet(1);
                });

        log.info(counter.get() + " Nodes added with property \"" + property + "\" and value \"" + value + "\"");
    }


    public void loadNodesByRelation(SAPRelationLabels relationType){
        loadNodesByRelation(relationType, false);
    }

    public void loadNodesByRelation(SAPRelationLabels relationType, boolean recursive){
        loadNodesByRelation(relationType, recursive, true);
    }

    public void loadNodesByRelation(SAPRelationLabels relationType, boolean recursive, boolean forward){

        Set<Long> nodeIds = nodeById.keySet();

        loadNodesByRelation(nodeIds, relationType, recursive, forward);
    }

    public void loadNodesByRelation(Set<Long> nodeIds, SAPRelationLabels relationType, boolean recursive, boolean forward){

        String nodeIDString = computeNodeIDString(nodeIds);

        String relatedNodesStatement = "";
        if(forward) {
            relatedNodesStatement = "MATCH (m)-[:" + relationType.name() + "]->(n) WHERE ID(m) IN " + nodeIDString + " RETURN m, n";
        } else {
            relatedNodesStatement = "MATCH (m)<-[:" + relationType.name() + "]-(n) WHERE ID(m) IN " + nodeIDString + " RETURN m, n";
        }

        AtomicInteger nodeCounter = new AtomicInteger(0);
        AtomicInteger relationCounter = new AtomicInteger(0);

        Set<Long> newNodeIds = new TreeSet<>();

        int nodesBefore = nodeById.size();
        connector.executeRead(relatedNodesStatement).forEachRemaining((result) -> {
            Node nNode = result.get("n").asNode();

            if(!nodeExist(nNode)){
                addNodeByID(nNode);
                addNodesByProperty(nNode);

                newNodeIds.add(nNode.id());
                nodeCounter.addAndGet(1);
            }

            Node mNode = result.get("m").asNode();
            mNode = nodeById.get(mNode.id());

            addNodesByRelation(mNode, nNode, relationType.name());
            relationCounter.addAndGet(1);
        });
        int nodesAfter = nodeById.size();

        log.info(nodeCounter.get() + " nodes added with relation \"" + relationType + "\" loaded");
        log.info( relationCounter.get() + " relations of type \"" + relationType + "\" loaded");

        if (nodesAfter - nodesBefore != newNodeIds.size()){
            log.warn(newNodeIds.size() - nodesAfter - nodesBefore + " nodes reloaded!");
        }

        if(recursive && !newNodeIds.isEmpty()){
            loadNodesByRelation(newNodeIds, relationType, true, forward);
        }
    }


    private String computeNodeIDString(Set<Long> nodeIds) {

        String nodeIdString = "[";
        for (Long nodeId: nodeIds) {
            nodeIdString += nodeId + ", ";
        }
        nodeIdString = nodeIdString.substring(0, nodeIdString.length()-2);
        nodeIdString += "]";

        return nodeIdString;
    }


    public void loadNodesWithRelation(SAPRelationLabels relationLabel){

        connector.executeRead(
                " MATCH (m)-[:" + relationLabel.name() + "]->(n) RETURN m, n"
        ).forEachRemaining((result) -> {
            Node mNode = result.get("m").asNode();
            Node nNode = result.get("n").asNode();

            addNodeByID(mNode);
            addNodeByID(nNode);

            addNodesByProperty(mNode);
            addNodesByProperty(nNode);

            addNodesByRelation(mNode, nNode, relationLabel.name());

        });

    }


    public Collection<Node> getNodes(){
        return nodeById.values();
    }


    public Collection<Node> getRelatedNodes(Node node, SAPRelationLabels relationLabel, Boolean direction){
        if( !nodesByRelation.containsKey(relationLabel.name())){
            return new TreeSet<>();
        };
        Map<Boolean, Map<Long, Map<Long, Node>>> relationMap = nodesByRelation.get(relationLabel.name());

        Map<Long, Map<Long, Node>> directedRelationMap = relationMap.get(direction);

        Long nodeID = node.id();
        if( !directedRelationMap.containsKey(nodeID)){
            return new TreeSet<>();
        }

        return directedRelationMap.get(nodeID).values();
    }

    public Collection<Node> getNodesByLabel(SAPNodeLabels label){
        if( !nodesByLabel.containsKey(label.name())){
            return new TreeSet<>();
        }
        return nodesByLabel.get(label.name()).values();
    }

    public Collection<Node> getNodesByProperty(SAPNodeProperties property, String value){
        Collection<Node> nodesByID = getNodes();
        List<Node> nodesByProperty = new ArrayList<>();

        for (Node node: nodesByID) {
            Value propertyValue = node.get(property.toString());
            if( propertyValue == null){
                continue;
            }
            String propertyValueString = propertyValue.asString();
            if(!propertyValueString.equals(value)){
                continue;
            }

            nodesByProperty.add(node);
        }

        return nodesByProperty;
    }

    // Laden Property
    public Collection<Node> getNodesByIdenticalPropertyValuesNodes(SAPNodeProperties property, String value){

        Collection<Node> nodesByLabelAndProperty = new ArrayList<>();

        StatementResult results = connector.executeRead("MATCH (n:Elements {" + property + ": '" + value + "'}) RETURN n");
        results.forEachRemaining((result) -> {
            Node propertyValue = result.get("n").asNode();

            nodesByLabelAndProperty.add(propertyValue);

        });

        return nodesByLabelAndProperty;
    }


    public Collection<Node> getNodesByLabelAndProperty(SAPNodeLabels label, String property, String value){
        Collection<Node> nodesByLabel = getNodesByLabel(label);
        List<Node> nodesByLabelAndProperty = new ArrayList<>();

        for (Node node: nodesByLabel){
            Value propertyValue = node.get(property);
            if( propertyValue == null){
                nodesByLabel.remove(node);
            }

            if(propertyValue.asString() != value){
                nodesByLabel.remove(node);
            }

            nodesByLabelAndProperty.add(node);
        }

        return nodesByLabelAndProperty;
    }

    private boolean nodeExist(Node node){
        Long nodeID = node.id();
        if( nodeById.containsKey(nodeID)){
            return true;
        }
        return false;
    }

    private void addNodeByID(Node node) {
        if( !nodeExist(node)){
            nodeById.put(node.id(), node);
        }
    }

    private void addNodesByProperty(Node node) {
        node.labels().forEach( (label)->{
            if( !nodesByLabel.containsKey(label)){
                Map<Long, Node> nodeIDMap = new HashMap<>();
                nodesByLabel.put(label, nodeIDMap);
            }
            Map<Long, Node> nodeIDMap = nodesByLabel.get(label);

            Long nodeID = node.id();
            if( !nodeIDMap.containsValue(nodeID)){
                nodeIDMap.put(nodeID, node);
            }
        });
    }

    private void addNodeByLabel(Node node){
        node.labels().forEach( (label)->{
            if( !nodesByLabel.containsKey(label)){
                Map<Long, Node> nodeIDMap = new HashMap<>();
                nodesByLabel.put(label, nodeIDMap);
            }
            Map<Long, Node> nodeIDMap = nodesByLabel.get(label);

            Long nodeID = node.id();
            if( !nodeIDMap.containsValue(nodeID)){
                nodeIDMap.put(nodeID, node);
            }
        });
    }


   /* private void addNodeByProperty(Node node){
        node.values().forEach( (nodeType)->{
            if( !nodeById.containsKey(nodeType)){
                Map<Long, Node> nodeIDMap = new HashMap<>();
                nodeById.put(nodeType, nodeIDMap);
            }
            Map<Long, Node> nodeIDMap = nodeById.get(nodeType);
            Long nodeID = node.id();
            if( !nodeIDMap.containsValue(nodeID)){
                nodeIDMap.put(nodeID, node);
            }
        });
    }*/

    private void addNodesByRelation(Node mNode, Node nNode, String relationName) {

        if( !nodesByRelation.containsKey(relationName)){
            createRelationMaps(relationName);
        };
        Map<Boolean, Map<Long, Map<Long, Node>>> relationMap = nodesByRelation.get(relationName);

        Map<Long, Map<Long, Node>> forwardRelationMap = relationMap.get(true);
        addNodeToRelationMap(forwardRelationMap, mNode, nNode);

        Map<Long, Map<Long, Node>> backwardRelationMap = relationMap.get(false);
        addNodeToRelationMap(backwardRelationMap, nNode, mNode);
    }

    private void createRelationMaps(String relationName) {
        Map<Boolean, Map<Long, Map<Long, Node>>> relationMap = new HashMap<>();
        Map<Long, Map<Long, Node>> forwardRelationMap = new HashMap<>();
        Map<Long, Map<Long, Node>> backwardRelationMap = new HashMap<>();
        relationMap.put(true, forwardRelationMap);
        relationMap.put(false, backwardRelationMap);
        nodesByRelation.put(relationName, relationMap);
    }

    private void addNodeToRelationMap(Map<Long, Map<Long, Node>> relationMap, Node mNode, Node nNode) {
        Long mNodeID = mNode.id();
        if( !relationMap.containsKey(mNodeID) ){
            Map<Long, Node> nodeIDMap = new HashMap<>();
            relationMap.put(mNodeID, nodeIDMap);
        }
        Map<Long, Node> nodeIDMap = relationMap.get(mNodeID);

        Long nNodeID = nNode.id();
        if( !nodeIDMap.containsKey(nNodeID)){
            nodeIDMap.put(nNodeID, nNode);
        }
    }

}
