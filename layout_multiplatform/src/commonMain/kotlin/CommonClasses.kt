package org.getaviz.generator.city.kotlin

// main interface class of the layout process: this is what goes in, this is what comes out

class Node constructor(
    val id: String,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 1.0,
    var length: Double = 1.0,
    var children: List<Node> = listOf<Node>(),
) {
    override fun toString(): String {
        return "[id $id | x $x | y $y | width $width | length $length | children ${children.map { it.id }}]"
    }
}

open class Rectangle(
    val width: Double = 1.0,
    val length: Double = 1.0,
    val x: Double = 0.0,
    val y: Double = 0.0,
): Comparable<Rectangle> {
    val area: Double = this.length * this.width
    val maxX: Double = this.x + this.width
    val maxY: Double = this.y + this.length
    val centerX: Double = this.x + (this.width / 2)
    val centerY: Double = this.y + (this.length / 2)

    override fun compareTo(other: Rectangle): Int {
        val areaComparison: Int = this.area.compareTo(other.area)
        return if (areaComparison == 0) {
            this.width.compareTo(other.width)
        } else {
            areaComparison
        }
    }

    override fun toString(): String {
        return "[x $x | y $y | width $width | length $length]"
    }
}

class CityRectangle(val node: Node, width: Double, length: Double, x: Double = 0.0, y: Double = 0.0)
    : Rectangle(width, length, x, y) {
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