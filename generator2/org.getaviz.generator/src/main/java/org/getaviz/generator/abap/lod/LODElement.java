package org.getaviz.generator.abap.lod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.abap.repository.ACityElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class LODElement {

    private Log log = LogFactory.getLog(this.getClass());

    private String hash;
    private Long nodeID;

    private double height;
    private double width;
    private double length;

    private double xPosition;
    private double yPosition;
    private double zPosition;

    private String color;

    private String aframeProperty;

    private List<ACityElement> replacedElements;
    private List<LODElement> replacedLODElements;

    public LODElement() {
        replacedElements = new ArrayList<>();

        UUID uuid = UUID.randomUUID();
        hash = "LOD_" + uuid.toString();
    }

    public String getHash() {
        return hash;
    }

    public Long getNodeID() {
        return nodeID;
    }

    public void setNodeID(Long nodeID) {
        this.nodeID = nodeID;
    }

    public double getHeight() { return height; }

    public void setHeight(double height) { this.height = height; }

    public double getWidth() { return width; }

    public void setWidth(double width) { this.width = width; }

    public double getLength() { return length; }

    public void setLength(double length) { this.length = length; }

    public double getXPosition() { return xPosition; }

    public void setXPosition(double xPosition) { this.xPosition = xPosition; }

    public double getYPosition() { return yPosition; }

    public void setYPosition(double yPosition) { this.yPosition = yPosition; }

    public double getZPosition() { return zPosition; }

    public void setZPosition(double zPosition) { this.zPosition = zPosition; }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setAframeProperty(String aframeProperty) { this.aframeProperty = aframeProperty; }

    public String getAframeProperty() { return aframeProperty; }

    public Collection<ACityElement> getReplacedElements() {
        return new ArrayList(replacedElements);
    }

    public void addReplacedElement(ACityElement replacedElement) {
        this.replacedElements.add(replacedElement);
    }

    public void removeReplacedElement(ACityElement replacedElement) {
        this.replacedElements.remove(replacedElement);
    }

    public Collection<LODElement> getReplacedLODElements() {
        return new ArrayList(replacedLODElements);
    }

    public void addReplacedLODElement(LODElement replacedLODElement) { this.replacedLODElements.add(replacedLODElement); }

    public void removeReplacedLODElement(LODElement replacedLODElement) { this.replacedLODElements.remove(replacedLODElement); }

}
