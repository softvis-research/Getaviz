package org.getaviz.generator.city.kotlin

import kotlin.js.JsName
import kotlin.math.*

data class LightMapLayouterConfig(
    @JsName("buildingHorizontalGap")
    var buildingHorizontalGap: Double,
    @JsName("trimEpsilon")
    var trimEpsilon: Double,
    @JsName("emptyDistrictSize")
    var emptyDistrictSize: Double
) {
    constructor() : this(3.0, 0.001, 5.0)
}

class LightMapLayouter(
    @JsName("config")
    val config: LightMapLayouterConfig
) {

    @JsName("calculateWithVirtualRoot")
    fun calculateWithVirtualRoot(nodes: List<Node>): CityRectangle {
        val virtualRoot = Node(id = "root", name = "root", isDistrict = true)
        virtualRoot.children = nodes
        val rootRectangle = arrangeChildren(virtualRoot)
        resolveAbsolutePositions(virtualRoot, 0.0, 0.0)

        return rootRectangle
    }

    @JsName("calculate")
    fun calculate(node: Node): CityRectangle {
        val rootRectangle = arrangeChildren(node)
        resolveAbsolutePositions(node, 0.0, 0.0)

        return rootRectangle
    }

    @JsName("_resolveAbsolutePositions")
    private fun resolveAbsolutePositions(node: Node, absoluteX: Double, absoluteY: Double) {
        node.x += absoluteX
        node.y += absoluteY
        node.children.forEach { child -> resolveAbsolutePositions(child, node.x, node.y) }
    }

    @JsName("_arrangeChildren")
    private fun arrangeChildren(parent: Node): CityRectangle {
        if (parent.children.isEmpty()) {
            if (parent.isDistrict) {
                // don't use transferSizeToNode() because emptyDistrictSize does not include buildingHorizontalGap
                parent.width = config.emptyDistrictSize
                parent.length = config.emptyDistrictSize
                return CityRectangle(parent, config.emptyDistrictSize, config.emptyDistrictSize)
            } else {
                return CityRectangle(parent,
                    parent.width + config.buildingHorizontalGap,
                    parent.length + config.buildingHorizontalGap
                )
            }
        }

        val arrangedChildren = parent.children.map { node -> arrangeChildren(node) }.sortedDescending()

        val maxRectangle = calculateMaxArea(arrangedChildren)
        var covRectangle = Rectangle()
        val tree = KDTreeNode(maxRectangle)

        for (element in arrangedChildren) {
            // get all nodes where the element would fit at all
            val possibleNodes = tree.getFittingNodes(element)
            // find the node that makes the most efficient use of the existing covrec
            val (preservers, expanders) = mapElementsToPreserversExpanders(possibleNodes, element, covRectangle)
            val targetNode = if (preservers.isEmpty()) {
                expanders.first().first
            } else {
                preservers.first().first
            }
            // trim node by shaving off unused space into empty new child nodes
            val fittedNode = trimNode(targetNode, element)
            fittedNode.occupied = true

            covRectangle = expandCovrecToInclude(fittedNode.rectangle, covRectangle)
            transferCoordsToNode(fittedNode.rectangle, element.node)
        }

        // the parent rectangle includes the surrounding gap - the parent node does not include it
        val parentRectangle = CityRectangle(parent,
            covRectangle.width + config.buildingHorizontalGap,
            covRectangle.length + config.buildingHorizontalGap)
        transferSizeToNode(covRectangle, parent)

        return parentRectangle
    }

    @JsName("_expandCovrecToInclude")
    private fun expandCovrecToInclude(newElement: Rectangle, oldCovrec: Rectangle): Rectangle {
        return Rectangle(
            max(oldCovrec.width, newElement.maxX - oldCovrec.x),
            max(oldCovrec.length, newElement.maxY - oldCovrec.y),
            oldCovrec.x,
            oldCovrec.y
        )
    }

    // the node is centered within the rectangle, so offset by half the margin value
    @JsName("_transferCoordsToNode")
    private fun transferCoordsToNode(sourceRectangle: Rectangle, targetNode: Node) {
        targetNode.x = sourceRectangle.x + (config.buildingHorizontalGap / 2)
        targetNode.y = sourceRectangle.y + (config.buildingHorizontalGap / 2)
    }

    @JsName("_transferSizeToNode")
    private fun transferSizeToNode(sourceRectangle: Rectangle, targetNode: Node) {
        targetNode.width = sourceRectangle.width
        targetNode.length = sourceRectangle.length
    }

    // naive calculation of max possible area by adding up all widths, lengths and gaps
    @JsName("_calculateMaxArea")
    private fun calculateMaxArea(children: List<Rectangle>): Rectangle {
        var widthSum = 0.0
        var lengthSum = 0.0
        for (node in children) {
            widthSum += node.width
            lengthSum += node.length
        }

        val totalPadding = config.buildingHorizontalGap * children.size
        widthSum += totalPadding
        lengthSum += totalPadding

        return Rectangle(widthSum, lengthSum)
    }

    @JsName("_mapElementsToPreserversExpanders")
    private fun mapElementsToPreserversExpanders(nodes: List<KDTreeNode>, insertedElement: CityRectangle, covrec: Rectangle)
            : Pair<List<Pair<KDTreeNode, Double>>,
            List<Pair<KDTreeNode, Double>>> {
        // split nodes into preservers (node fits into covrec) and expanders (expansion of covrec needed)
        val (preserverList, expanderList) = nodes.partition {
            it.rectangle.x + insertedElement.width <= covrec.maxX
                    && it.rectangle.y + insertedElement.length <= covrec.maxY
        }
        // sort preservers by the area left uncovered
        val preserverMap = preserverList.map {
            it to (it.rectangle.area - insertedElement.area)
        }.sortedBy { it.second }
        // sort expanders by how close the expanded covrec would be to a square
        val expanderMap = expanderList.map {
            it to (max(it.rectangle.x + insertedElement.width, covrec.maxX)
                    / max(it.rectangle.y + insertedElement.length, covrec.maxY))
        }.sortedBy { abs(it.second - 1) }

        return Pair(preserverMap, expanderMap)
    }

    @JsName("_trimNode")
    private fun trimNode(node: KDTreeNode, insertedElement: CityRectangle): KDTreeNode {
        val nodeRec = node.rectangle
        // if there is a significant difference in length, cut horizontally to split into new node
        if (abs(nodeRec.length - insertedElement.length) > config.trimEpsilon) {
            node.leftChild = KDTreeNode(
                Rectangle(nodeRec.width, insertedElement.length, nodeRec.x, nodeRec.y))
            node.rightChild = KDTreeNode(
                Rectangle(nodeRec.width,nodeRec.length - insertedElement.length, nodeRec.x,nodeRec.y + insertedElement.length)
            )

            node.occupied = true
            return trimNode(node.leftChild!!, insertedElement)
            // otherwise, if there is a significant difference in width, cut vertically
        } else if(abs(nodeRec.width - insertedElement.width) > config.trimEpsilon) {
            node.leftChild = KDTreeNode(
                Rectangle(insertedElement.width, nodeRec.length, nodeRec.x, nodeRec.y))
            node.rightChild = KDTreeNode(
                Rectangle(nodeRec.width - insertedElement.width, nodeRec.length, nodeRec.x + insertedElement.width, nodeRec.y)
            )

            node.occupied = true
            return trimNode(node.leftChild!!, insertedElement)
            // otherwise, the inserted element already fills the node perfectly
        } else {
            return node
        }
    }
}