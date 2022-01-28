package org.getaviz.generator.city.kotlin

// main interface class of the layout process: this is what goes in, this is what comes out
class Node constructor(
    val id: String,
    val name: String,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 1.0,
    var length: Double = 1.0,
    var children: List<Node> = listOf<Node>(),
    var isDistrict: Boolean = children.isEmpty()
) {
    val centerX: Double
        get() = this.x + (this.width / 2)
    val centerY: Double
        get() = this.y + (this.length / 2)
}

open class Rectangle(
    val width: Double = 1.0,
    val length: Double = 1.0,
    val x: Double = 0.0,
    val y: Double = 0.0,
) {
    // all properties are immutable, so we don't need to compute these dynamically in a getter
    val area: Double = this.length * this.width
    val maxX: Double = this.x + this.width
    val maxY: Double = this.y + this.length
    val centerX: Double = this.x + (this.width / 2)
    val centerY: Double = this.y + (this.length / 2)
}

class CityRectangle(val node: Node, width: Double, length: Double, x: Double = 0.0, y: Double = 0.0)
    : Rectangle(width, length, x, y), Comparable<CityRectangle> {

    override fun compareTo(other: CityRectangle): Int {
        return this.area.compareTo(other.area).takeIf { it != 0 }
            ?: this.width.compareTo(other.width).takeIf { it != 0 }
            ?: this.node.name.compareTo(other.node.name)
    }
}

class KDTreeNode(var rectangle: Rectangle) {
    var leftChild: KDTreeNode? = null
    var rightChild: KDTreeNode? = null
    var occupied: Boolean = false

    fun getFittingNodes(element: Rectangle): List<KDTreeNode> {
        val list = mutableListOf<KDTreeNode>()
        getFittingsNodesMutable(element, list)
        return list
    }

    private fun getFittingsNodesMutable(element: Rectangle, list: MutableList<KDTreeNode>) {
        if (!occupied && rectangle.length >= element.length && rectangle.width >= element.width) {
            list.add(this)
        }
        leftChild?.getFittingsNodesMutable(element, list)
        rightChild?.getFittingsNodesMutable(element, list)
    }
}