package org.getaviz.generator.layouts.kdtree;

import org.neo4j.driver.v1.types.Node;

import java.util.Arrays;

/**
 * Class uses two points to identify a rectangle Class knows two basic ways of
 * creating and initializing a rectangle: – insert two corner points – insert
 * width, length and a corner point/the center point The interface is based on
 * one single overloaded method: changeRectangle No setter for class-attributes
 * available to ensure data validity, for interaction use changeRectangle
 */
public class ACityRectangle implements Comparable<ACityRectangle> {
//	@Accessors(PUBLIC_GETTER) var double width
//	@Accessors(PUBLIC_GETTER) var double length
	double width;
	double length;
	double area;
//	@Accessors(PUBLIC_GETTER, PUBLIC_SETTER) var Node nodeLink
	Node nodeLink;
	double upperLeftX;
	double upperLeftY;
	double bottomRightX;
	double bottomRightY;
	double centerX;
	double centerY;

	public ACityRectangle() {
		super();
		changeRectangle(0, 0, 0, 0);
	}

	public ACityRectangle(double x1, double y1, double x2, double y2) {
		super();
		changeRectangle(x1, y1, x2, y2);
	}

	/**
	 * Constructs a new Rectangle
	 * 
	 * @param pX            x coordinate
	 * @param pY            y coordinate
	 * @param width         width of the new Rectangle
	 * @param length        length of the new Rectangle
	 * @param pointPosition determines how P(x|y) is interpreted: 0: P(x|y) is
	 *                      CenterPoint 1: P(x|y) is UpperLeft 2: P(x|y) is
	 *                      UpperRight 3: P(x|y) is BottomRight 4: P(x|y) is
	 *                      BottomLeft
	 */
	public ACityRectangle(double pX, double pY, double width, double length, int pointPosition) {
		super();
		changeRectangle(pX, pY, width, length, pointPosition);
	}

	/**
	 * Uses two corner points to change values of the rectangle
	 * 
	 * @param x1 corner1 of Rectangle
	 * @param y1 corner1 of Rectangle
	 * @param x2 corner2 of Rectangle
	 * @param y2 corner2 of Rectangle
	 */
	public void changeRectangle(double x1, double y1, double x2, double y2) {
		setCornerPoints(x1, y1, x2, y2);
		update();
	}

	/**
	 * <h1>changeRectangle(double, double, double, double, int)</h1>
	 * 
	 * @param x             coordinate
	 * @param y             coordinate
	 * @param width         width of rectangle
	 * @param length        length of rectangle
	 * @param pointPosition determines how P(x|y) is identified: 0: P(x|y) is
	 *                      CenterPoint 1: P(x|y) is UpperLeft 2: P(x|y) is
	 *                      UpperRight 3: P(x|y) is BottomRight 4: P(x|y) is
	 *                      BottomLeft
	 */
	public void changeRectangle(double x, double y, double width, double length, int pointPosition) {
		// method forces width & length to equal/be greater than zero: using absolute
		// value
		double w = Math.abs(width);
		double h = Math.abs(length);

		switch (pointPosition) {
		case 0:
			setCornerPoints((x - w / 2), (y - h / 2), (x + w / 2), (y + h / 2));
			break; // identifies P(x|y) as CenterPoint
		case 1:
			setCornerPoints(x, y, (x + w), (y + h));
			break; // identifies P(x|y) as UpperLeft
		case 2:
			setCornerPoints((x - w), y, x, (y + h));
			break; // identifies P(x|y) as UpperRight
		case 3:
			setCornerPoints((x - w), (y - h), x, y);
			break; // identifies P(x|y) as BottomRight
		case 4:
			setCornerPoints(x, (y - h), (x + w), y);
			break; // FOUR identifies P(x|y) as BottomLeft
		default:
			setCornerPoints(x, y, (x + w), (y + h));
			break; // UNDEF identifies P(x|y) as UpperLeft
		}
		update();
	}

	public double getPerimeterRadius(){
		return Math.sqrt(Math.pow(this.getWidth() / 2.0, 2) + Math.pow(this.getLength() / 2.0, 2));
	}

	public void setCornerPoints(double x1, double y1, double x2, double y2) {
		// upperLeftCorner of a rectangle always has the leftmost X-coordinate and the
		// smallest Y-coordinate as it's values
		// bottomRightCorner always uses the rightmost X-coordinate and the highest
		// Y-coordinate as it's values
		final double[] xValues = { x1, x2 };
		final double[] yValues = { y1, y2 };
		Arrays.sort(xValues);
		Arrays.sort(yValues);

		upperLeftX = xValues[0];
		upperLeftY = yValues[0];
		bottomRightX = xValues[1];
		bottomRightY = yValues[1];
	}

	private void update() {
		width = bottomRightX - upperLeftX;
		length = bottomRightY - upperLeftY;
		area = width * length;
		centerX = upperLeftX + width / 2;
		centerY = upperLeftY + length / 2;
	}

	public int compareTo(ACityRectangle second) {
		int firstComparison = Double.compare(this.area, second.getArea());
		if (firstComparison == 0) {
			int secondComparison = Double.compare(this.width, second.getWidth());
			if (secondComparison == 0) {
				return 0;
			} else {
				return secondComparison;
			}
		} else {
			return firstComparison;
		}
	}

	public double getArea() {
		return area;
	}
	
	public double getLength() {
		return length;
	}
	
	public double getWidth() {
		return width;
	}
	
	public double getBottomRightX() {
		return bottomRightX;
	}
	
	public double getBottomRightY() {
		return bottomRightY;
	}
	
	public double getCenterX() {
		return centerX;
	}
	
	public double getCenterY() {
		return centerY;
	}
	
	public double getUpperLeftX() {
		return upperLeftX;
	}
	
	public double getUpperLeftY() {
		return upperLeftY;
	}
	
	public Node getNodeLink() {
		return nodeLink;
	}
	
	public void setNodeLink(Node nodeLink) {
		this.nodeLink = nodeLink;
	}
}
