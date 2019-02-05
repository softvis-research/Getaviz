package org.getaviz.generator.city.m2m

import java.util.Arrays
import org.eclipse.xtend.lib.annotations.Accessors
import org.neo4j.graphdb.Node

//@TODO: implement additional output that uses (2D-)tuples/Pairs/GeoPoints

/**
 * Class uses two points to identify a rectangle
 * Class knows two basic ways of creating and initializing a rectangle:
 * 	– insert two corner points
 * 	– insert width, length and a corner point/the center point
 * The interface is based on one single overloaded method: changeRectangle
 * No setter for class-attributes available to ensure data validity, for interaction use changeRectangle
 */
@Accessors(PUBLIC_GETTER) class Rectangle implements Comparable<Rectangle> {
	@Accessors(PUBLIC_GETTER) var double width
	@Accessors(PUBLIC_GETTER) var double length
	var double area
	@Accessors(PUBLIC_GETTER, PUBLIC_SETTER) var Node nodeLink
	var double upperLeftX
	var double upperLeftY
	var double bottomRightX
	var double bottomRightY
	var double centerX
	var double centerY
	
	
	new() {
		super()
		changeRectangle(0, 0, 0, 0)
	}
	
	new(double x1, double y1, double x2, double y2) {
		super();
		changeRectangle(x1, y1, x2, y2);
	}
	
	/**
	 * Constructs a new Rectangle
	 * 
	 * @param pX x coordinate
	 * @param pY y coordinate
	 * @param width	width of the new Rectangle
	 * @param length length of the new Rectangle
	 * @param pointPosition determines how P(x|y) is interpreted:
	 * 	0:	P(x|y) is CenterPoint
	 * 	1:	P(x|y) is UpperLeft
	 * 	2:	P(x|y) is UpperRight
	 * 	3:	P(x|y) is BottomRight
	 * 	4:	P(x|y) is BottomLeft 
	 */
	new(double pX, double pY, double width, double length, int pointPosition) {
		super();
		changeRectangle(pX, pY, width, length, pointPosition);
	}
		
	/**
	 * Uses two corner points to change values of the rectangle
	 * @param x1 corner1 of Rectangle
	 * @param y1 corner1 of Rectangle
	 * @param x2 corner2 of Rectangle
	 * @param y2 corner2 of Rectangle
	 */
	def void changeRectangle(double x1, double y1, double x2, double y2){
		setCornerPoints(x1, y1, x2, y2)
		update()
	}
	/**
	 * <h1>changeRectangle(double, double, double, double, int)</h1>
	 * @param x coordinate
	 * @param y coordinate 
	 * @param width width of rectangle
	 * @param length length of rectangle
	 * @param pointPosition determines how P(x|y) is identified:
	 * 	0:	P(x|y) is CenterPoint
	 * 	1:	P(x|y) is UpperLeft
	 * 	2:	P(x|y) is UpperRight
	 * 	3:	P(x|y) is BottomRight
	 * 	4:	P(x|y) is BottomLeft 
	 */
	def void changeRectangle(double x, double y, double width, double length, int pointPosition){
		//method forces width & length to equal/be greater than zero: using absolute value
		val w = Math.abs(width)
		val h = Math.abs(length)
		
		switch pointPosition{
			case 0: setCornerPoints((x- w/2), (y- h/2), (x+ w/2), (y+ h/2)) 	// identifies P(x|y) as CenterPoint
			case 1: setCornerPoints(x, y, (x+w), (y+h))							// identifies P(x|y) as UpperLeft
			case 2: setCornerPoints((x-w), y, x, (y+h))							// identifies P(x|y) as UpperRight
			case 3: setCornerPoints((x-w), (y-h), x, y) 						// identifies P(x|y) as BottomRight
			case 4: setCornerPoints(x, (y-h), (x+w), y) 						//FOUR	identifies P(x|y) as BottomLeft
			default:setCornerPoints(x, y, (x+w), (y+h))							//UNDEF	identifies P(x|y) as UpperLeft
		}
		update()
	}
	
	def private void setCornerPoints(double x1, double y1, double x2, double y2){
		//upperLeftCorner of a rectangle always has the leftmost X-coordinate and the smallest Y-coordinate as it's values
		//bottomRightCorner always uses the rightmost X-coordinate and the highest Y-coordinate as it's values
		val double[] xValues = #[x1, x2]
		val double[] yValues = #[y1, y2]
		Arrays.sort(xValues);
		Arrays.sort(yValues);
		
		upperLeftX = xValues.get(0);
		upperLeftY = yValues.get(0);
		bottomRightX = xValues.get(1);
		bottomRightY = yValues.get(1);
	}
	
	def private update() {
		width = bottomRightX - upperLeftX
		length = bottomRightY - upperLeftY
		area = width * length
		centerX = upperLeftX + width/2
		centerY = upperLeftY + length/2
	}

	override compareTo(Rectangle second) {
		val firstComparison = Double.compare(this.area, second.area);
		if(firstComparison == 0){
			val secondComparison = Double.compare(this.width, second.width);
			if(secondComparison == 0){
					return 0;
			} else {
				return secondComparison;
			}
		}else{
			return firstComparison;
		}
	}
}
