package org.getaviz.generator.abap.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import java.util.*;

public class ACityElement {

    private Log log = LogFactory.getLog(this.getClass());

    public ACityShape getShape() {
        return shape;
    }

    public void setShape(ACityShape shape) {
        this.shape = shape;
    }

    public ACitySubType getSubType() {
        return subType;
    }

    public void setSubType(ACitySubType subType) {
        this.subType = subType;
    }

    public enum ACityType {
        District, Building, Floor, Chimney,

        //additional for Metropolis
        FactoryHall, //Report
        FactoryBuilding_Form, //Form
        FactoryBuilding_Fumo, //Fumo
        RadioTower, //Interface
        SkyScraper, //Method
        CarPark, //Attribute_Class
        Container, //Attribute_Table
        Tube //Attribute_FuGr
    }

    public enum ACitySubType {
        Class, Report, FunctionGroup, Table, DDIC

        // additional subTypes for metropolis
        , Interface, DataElement, Structure

        , Sea, Mountain
    }


    public enum ACityShape {
        Box, Cylinder, Cone

        // alternative shapes
        , Sphere, Ring, Plane, Circle, Tetrahedron, Entity
    }

    private String hash;



    private Long nodeID;
    private Long sourceNodeID;

    private Node sourceNode;

    private List<ACityElement> subElements;
    private ACityElement parentElement;

    private ACityType type;
    private ACitySubType subType;

    private String color;
    private ACityShape shape;
    private String source;
    private String model;
    private String modelScale;

    private double height;
    private double width;
    private double length;

    private double xPosition;
    private double yPosition;
    private double zPosition;

    private String rotation;

    public ACityElement(ACityType type) {
        this.type = type;
        subElements = new ArrayList<>();

        UUID uuid = UUID.randomUUID();
        hash = "ID_" + uuid.toString();
    }

    public Long getSourceNodeID() {
        return sourceNode.id();
    }


    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public SAPNodeTypes getSourceNodeType(){
        Node sourceNode = getSourceNode();
        if( sourceNode == null){
            return null;
        }
        return SAPNodeTypes.valueOf(sourceNode.get(SAPNodeProperties.type_name.name()).asString());
    }


    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getXPosition() {
        return xPosition;
    }

    public void setXPosition(double xPosition) {
        this.xPosition = xPosition;
    }

    public double getYPosition() {
        return yPosition;
    }

    public void setYPosition(double yPosition) {
        this.yPosition = yPosition;
    }

    public double getZPosition() {
        return zPosition;
    }

    public void setZPosition(double zPosition) {
        this.zPosition = zPosition;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTextureSource() {
        return source;
    }

    public void setTextureSource(String src) {
        this.source = src;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    public String getModelScale() {
        return modelScale;
    }

    public void setModelScale(String scale) {
        this.modelScale = scale;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String gltfModel) {
        this.model = gltfModel;
    }


    public ACityElement getParentElement() {
        return parentElement;
    }

    public void setParentElement(ACityElement parentElement) {
        this.parentElement = parentElement;
    }

    public Collection<ACityElement> getSubElements() {
        return new ArrayList(subElements);
    }

    public Collection<ACityElement> getSubElementsOfType(ACityType elementType) {
        List<ACityElement> subElementsOfType = new ArrayList<>();

        Collection<ACityElement> subElements = getSubElements();
        for(ACityElement element : subElements){

            if( element.getType() == elementType){
                subElementsOfType.add(element);
            }
        }
        return subElementsOfType;
    }

    public String getSourceNodeProperty(SAPNodeProperties sapNodeProperties) {

        Node sourceNode = getSourceNode();

        try{
        if(sourceNode == null){
            //TODO Exception oder log fatal?
            throw new Exception("sourceNode is equal null");
        }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        Value propertyValue = sourceNode.get(sapNodeProperties.name());
        try {
            if (propertyValue == null) {
                //TODO Exception oder lof fatal?
                //throw new Exception("propertyValue is equal null");
            }
        } catch (Exception e) {
            //log.error(e.getMessage());
            log.error(e + "propertyValue is equal null"); // Fehler führt schon in viel eherer Verarbeitung zum Abbruch
        }

        String sourceNodeProperty = propertyValue.asString();

        return sourceNodeProperty;
    }

    public void addSubElement(ACityElement subElement) {
        this.subElements.add(subElement);
    }

    public void removeSubElement(ACityElement subElement) {
        this.subElements.remove(subElement);
    }

    public String getHash() {
        return hash;
    }

    public ACityType getType() {
        return type;
    }

    public Long getNodeID() {
        return nodeID;
    }

    public void setNodeID(Long nodeID) {
        this.nodeID = nodeID;
    }
}