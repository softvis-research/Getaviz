package org.getaviz.generator.rd;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.getaviz.generator.rd.m2m.Position;
;
import java.util.ArrayList;
import java.util.List;

public abstract class Disk implements RDElement, Comparable<Disk> {

    protected double height;
    private double transparency;
    double borderWidth;
    double areaWithBorder;
    double areaWithoutBorder;
    protected double radius = 0;
    protected Position position;
    protected String color;
    String spine;
    private long parentVisualizedNodeID;
    long visualizedNodeID;
    long parentID;
    protected long id;
    private ArrayList<Disk> innerDisks = new ArrayList<>();
    private static GeometricShapeFactory shapeFactory = null;
    private double innerSegmentsRadius;
    private double innerRadius = 0;
    boolean wroteToDatabase = false;

    static {
        shapeFactory = new GeometricShapeFactory();
    }

    protected Disk(long visualizedNodeId, double ringWidth, double height) {
        this.visualizedNodeID = visualizedNodeId;
        this.borderWidth = ringWidth;
        this.height = height;
    }

   protected Disk(long visualizedNodeId, long parentVisualizedNodeID, double ringWidth, double height, double transparency) {
        this(visualizedNodeId, ringWidth, height);
        this.parentVisualizedNodeID = parentVisualizedNodeID;
        this.transparency = transparency;
   }

    public int compareTo(Disk disk) {
        return java.lang.Double.compare(disk.getAreaWithoutBorder(), areaWithoutBorder);
    }


    //TODO: move somewhere else
    public void calculateSpines() {
        int spinePointCount = 50;
        List<String> completeSpine = new ArrayList<>();
        double stepX = 2 * Math.PI / spinePointCount;
        for (int i = 0; i < spinePointCount; ++i) {
            completeSpine.add(radius * Math.cos(i * stepX) + " " + radius * Math.sin(i * stepX) + " " + 0.0);
        }
        completeSpine.add(completeSpine.get(0));
        spine = "\'" + String.join(", ", completeSpine) + "\'";
    }
    //TODO: move somewhere else
    String calculateCrossSection() {
        double crossHeight;
        if (borderWidth == 0) {
            crossHeight = 0.0;
        } else {
            crossHeight = this.height;
        }
        return "\'" + (-(borderWidth / 2) + " " + (crossHeight)) + ", " + ((borderWidth / 2) + " " + (crossHeight)) + ", "
                + ((borderWidth / 2) + " " + 0) + ", " + (-(borderWidth / 2) + " " + 0) + ", " + (-(borderWidth / 2) + " " +
                (crossHeight)) + "\'";
    }

  //  abstract void updateNode(DatabaseConnector connector);
  //  abstract void createNode(DatabaseConnector connector);


    String propertiesToString() {
        return String.format("ringWidth: %f, height: %f, transparency: %f, color: \'%s\'", borderWidth,
                height, transparency, color);
    }

    public void setParentID(long id) {
        this.parentID = id;
    }

    public long getParentID() {
        return parentID;
    }

    public void setID(long id) {
        this.id = id;
    }

    public long getID() {
        return id;
    }

    void setAreaWithoutBorder(double areaWithoutBorder) {
        this.areaWithoutBorder = areaWithoutBorder;
    }

    public double getAreaWithoutBorder() {
        return areaWithoutBorder;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
         return radius;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void updatePosition(double x, double y) {
        position.x = x;
        position.y = y;
    }

    public Position getPosition() {
        return position;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setInnerDisks(List<Disk> list) {
        innerDisks.addAll(list);
    }

    public ArrayList<Disk> getInnerDisks() {
        return innerDisks;
    }

    public long getParentVisualizedNodeID() {
        return parentVisualizedNodeID;
    }

    public long getVisualizedNodeID() {
        return visualizedNodeID;
    }

    public double getBorderWidth() {
        return borderWidth;
    }

    public double getHeight() {
        return height;
    }

    public String getSpine() {return spine;}

    public abstract double getMinArea();

    public Coordinate[] getCoordinates() {
        shapeFactory.setNumPoints(64);
        shapeFactory.setCentre(new Coordinate(position.x, position.y));
        shapeFactory.setSize(radius * 2);
        return shapeFactory.createCircle().getCoordinates();
    }

    public double getInnerSegmentsRadius(){
        return innerSegmentsRadius;
    }

    public void setInnerSegmentsRadius(double radius) {
        innerSegmentsRadius = radius;
    }

    public double getInnerRadius() {
        return innerRadius;
    }

    public void setInnerRadius(double radius) {
        innerRadius = radius;
    }

    public boolean hasInnerDisks() {
        return !innerDisks.isEmpty();
    }
}
