package org.getaviz.generator.rd.m2m

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.generator.database.Labels
import org.getaviz.generator.SettingsConfiguration.OutputFormat
import org.getaviz.generator.rd.RDUtils
import java.util.ArrayList
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.util.GeometricShapeFactory
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.algorithm.MinimumBoundingCircle
import com.vividsolutions.jts.geom.CoordinateList
import com.vividsolutions.jts.geom.Coordinate
import org.getaviz.generator.Helper
import org.apache.commons.logging.LogFactory
import org.getaviz.generator.database.DatabaseConnector
import org.neo4j.driver.v1.types.Node
import java.util.Iterator

class RD2RD {
	val config = SettingsConfiguration.instance
	val connector = DatabaseConnector.instance
	val log = LogFactory::getLog(class)
	extension Helper util = new Helper

// TODO set colors via RGBColor class for all entities
// color scheme
	RGBColor NS_colorStart = new RGBColor(150, 150, 150);
	RGBColor NS_colorEnd = new RGBColor(240, 240, 240); // from CodeCity
	RGBColor[] NS_colors

	new() {
		log.info("RD2RD started")
		var length = connector.executeRead(
			"MATCH p=(n:Package)-[:CONTAINS*]->(m:Package) WHERE NOT (m)-[:CONTAINS]->(:Package) RETURN max(length(p)) AS length")
		val namespaceMaxLevel = length.single.get("length").asLong.intValue + 1
		length = connector.executeRead(
			"MATCH p=(n:RD:Model)-[:CONTAINS*]->(m:RD:Disk) WHERE NOT (m)-[:CONTAINS]->(:RD:Disk) RETURN max(length(p)) AS length")
		val diskMaxLevel = length.single.get("length").asLong.intValue + 1
		NS_colors = createColorGradiant(NS_colorStart, NS_colorEnd, namespaceMaxLevel)

		connector.executeRead(
			"MATCH p = (n:Model:RD)-[:CONTAINS*]->(d:Disk)-[:VISUALIZES]->(e) RETURN d,e,length(p)-1 AS length").forEach [
			var setString = " SET n.maxLevel = " + diskMaxLevel
			val disk = get("d").asNode
			if (get("e").asNode.hasLabel(Labels.Package.name)) {
				setString += ", n.color = \'" + setNamespaceColor(get("length").asLong.intValue) + "\'"
			}
			connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk.id + setString)
		]
		log.debug("vor calulate net area")
		getRootDisks.calculateNetArea
		log.debug("nach calulate net area")

		getDisks.forEach[get("d").asNode.calculateRadius]
		log.debug("nach calulate net radius")

		getRootDisks.calculateLayout
		log.debug("Nach calculate Layout")

		getDisks.forEach[get("d").asNode.postLayout]
		log.debug("Nach postLayout")

		getDisks.forEach[get("d").asNode.postLayout2]
		log.debug("Nach postLayout2")

		log.info("RD2RD finished")
	}

	def private String setNamespaceColor(int level) {
		if (config.outputFormat == OutputFormat::AFrame) {
			return config.RDNamespaceColorHex
		} else {
			// namespace.color = NS_colors.get(namespace.getLevel() - 1).asPercentage
			return NS_colors.get(level - 1).asPercentage
		}
	}

	def private void calculateNetArea(Iterator<Node> disks) {
		disks.forEach [ disk |
			getSubDisks(disk.id).calculateNetArea
			calculateNetArea(disk.id)
		]
	}

	def private void calculateNetArea(Long disk) {
		var netArea = 0.0
		val dataSum = connector.executeRead(
			"MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Field) WHERE ID(n) = " + disk +
				" SET d.size = d.size * " + config.RDDataFactor + " RETURN SUM(d.size) AS sum").single.get("sum").
			asDouble
		val methodSum = connector.executeRead(
			"MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Method) WHERE ID(n) = " + disk +
				" SET d.size = d.size * " + config.RDDataFactor + " RETURN SUM(d.size) AS sum").single.get("sum").
			asDouble
		netArea = dataSum + methodSum
		connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk + " SET n.netArea = " + netArea)
	}

	def private calculateRadius(Node disk) {
		val netArea = disk.get("netArea").asDouble
		val ringWidth = disk.get("ringWidth").asDouble
		val radius = Math::sqrt(netArea / Math::PI) + ringWidth
		connector.executeWrite("MATCH(n) WHERE ID(n) = " + disk.id + " SET n.radius = " + radius)
	}

	def private calculateLayout(Iterator<Node> disks) {
		val nestedCircles = new ArrayList<CircleWithInnerCircles>
		disks.forEach[disk|nestedCircles += new CircleWithInnerCircles(disk, false)]
		RDLayout::nestedLayout(nestedCircles)
		nestedCircles.forEach[updateDiskNode]
	}

	def private postLayout(Node disk) {
		val data = RDUtils.getData(disk.id)
		val methods = RDUtils.getMethods(disk.id)
		disk.fractions(data, methods)
		data.fractions
		methods.fractions
	}

	def private postLayout2(Node disk) {
		RDUtils::getSubDisks(disk.id).forEach [
			get("d").asNode.calculateRings
		]
		disk.calculateRings
	}

	def private fractions(Node disk, Iterator<Node> data, Iterator<Node> methods) {
		val netArea = disk.get("netArea").asDouble
		var currentMethodArea = RDUtils.sum(methods) / netArea
		var currentDataArea = RDUtils.sum(data) / netArea
		connector.executeWrite(
			"MATCH (n) WHERE ID(n) = " + disk.id + " SET n.methodArea = " + currentMethodArea + ", n.dataArea = " +
				currentDataArea)
	}

	def private fractions(Iterator<Node> segments) {
		val sum = RDUtils.sum(segments)
		segments.forEach [
			connector.executeWrite("MATCH (n) WHERE ID(n) = " + id + " SET n.size = n.size/" + sum)
		]
	}

	def private calculateRings(Node disk) {
		val ringWidth = disk.get("ringWidth").asDouble
		val height = disk.get("height").asDouble
		val radius = disk.get("radius").asDouble
		var methodArea = disk.get("methodArea").asDouble
		var dataArea = disk.get("dataArea").asDouble
		val netArea = disk.get("netArea").asDouble
		if (ringWidth == 0) {
			calculateCrossSection(disk.id, ringWidth, 0)
		} else {
			calculateCrossSection(disk.id, ringWidth, height)
		}
		calculateSpines(disk.id, radius - (0.5 * ringWidth))
		log.debug("in calculate rings")
		if (RDUtils::getSubDisks(disk.id).nullOrEmpty) {
			val r_data = Math::sqrt(dataArea * netArea / Math::PI)
			val r_methods = radius - ringWidth
			val b_methods = r_methods - r_data
			val diskMethods = RDUtils.getMethods(disk.id)
			val diskData = RDUtils.getData(disk.id)
			if (!diskMethods.nullOrEmpty) {
				log.debug("Wenn in calc rings methods nicht leer")
				diskMethods.calculateCrossSection(b_methods, height)
//				val crossSection = calculateCrossSection(b_methods, height)
//				connector.executeWrite(
//					"MATCH (n)-[:CONTAINS]->(d:DiskSegment)-[:VISUALIZES]->(:Method) WHERE ID(n) = " + disk.id +
//						" SET d.crossSection = \'" + crossSection + "\'")
				log.debug("Ist das durch?")
				calculateSpines(diskMethods, r_methods - 0.5 * b_methods)
				if (config.outputFormat == OutputFormat::AFrame) {
					diskMethods.forEach [
						connector.executeWrite(
							"MATCH (n) WHERE ID(n) = " + id + " SET n.outerRadius = " + r_methods +
								", n.innerRadius = " + r_data)
					]
				}
			}
			if (!diskData.nullOrEmpty) {
				log.debug("Wenn in calc rings data nicht leer")
				diskData.calculateCrossSection(r_data, height)
				calculateSpines(diskData, 0.5 * r_data)
				if (config.outputFormat == OutputFormat::AFrame) {
					diskData.forEach [
						connector.executeWrite(
							"MATCH (n) WHERE ID(n) = " + id + " SET n.outerRadius = " + r_data + ", n.innerRadius = " +
								0.0)
					]
				}
			}
		} else {
			val outerRadius = disk.id.calculateOuterRadius
			val r_data = Math::sqrt((dataArea * netArea / Math::PI) + (outerRadius * outerRadius))
			val b_data = r_data - outerRadius
			val r_methods = Math::sqrt((methodArea * netArea / Math::PI) + (r_data * r_data))
			val b_methods = r_methods - r_data
			val diskMethods = RDUtils.getMethods(disk.id)
			if (!diskMethods.nullOrEmpty) {
				diskMethods.calculateCrossSection(b_methods, height)
				calculateSpines(diskMethods, r_methods - 0.5 * b_methods)
				if (config.outputFormat == OutputFormat::AFrame) {
					diskMethods.forEach [
						connector.executeWrite("MATCH (n) WHERE ID(n) = " + id + " SET n.outerRadius = " + r_methods + ",

n.innerRadius = " + r_data)
					]
				}
			}
			val diskData = RDUtils.getData(disk.id)
			if (!diskData.nullOrEmpty) {
				diskData.calculateCrossSection(b_data, height)
				calculateSpines(diskData, r_data - 0.5 * b_data)
				if (config.outputFormat == OutputFormat::AFrame) {
					diskData.forEach [
						connector.executeWrite("
MATCH
(n)WHERE ID
(n)= " + id + " SET
n.outerRadius = " + r_data + "
, n.innerRadius = " + (r_data - b_data))
					]
				}
			}
		}
	}

	def private calculateOuterRadius(Long disk) {
		log.debug("In calculate outer radius: " + disk)
		val coordinates = new CoordinateList()
		RDUtils::getSubDisks(disk).forEach [
			val node = get("d").asNode
			val position = connector.executeRead("MATCH (n)-[:HAS]->(p:Position) WHERE ID(n) = " + node.id +
				" RETURN p")
			var x = 0.0
			var y = 0.0
			if (!position.nullOrEmpty) {
				val posNode = position.single.get("p").asNode
				x = posNode.get("x").asDouble
				y = posNode.get("y").asDouble
			}
			coordinates.add(createCircle(x, y, node.get("radius").asDouble).coordinates, false)
		]
		val geoFactory = new GeometryFactory()
		val innerCircleMultiPoint = geoFactory.createMultiPoint(coordinates.toCoordinateArray)
		val mbc = new MinimumBoundingCircle(innerCircleMultiPoint)

		return mbc.radius
	}

	def private Geometry createCircle(double x, double y, double radius) {
		val shapeFactory = new GeometricShapeFactory
		shapeFactory.numPoints = 64
		shapeFactory.centre = new Coordinate(x, y)
		shapeFactory.size = radius * 2
		return shapeFactory.createCircle
	}

	def private calculateCrossSection(Iterator<Node> segments, double width, double height) {
		val crossSection = (-(width / 2 ) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", " +
			((width / 2 ) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height))
		val statementList = newArrayList
		segments.forEach [
			statementList += "MATCH (n) WHERE ID(n) = " + id + " SET n.crossSection = \'" + crossSection + "\'"
		]
		connector.executeWrite(statementList)
	}

	def private calculateCrossSection(double width, double height) {
		val crossSection = (-(width / 2 ) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", " +
			((width / 2 ) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height))
		return crossSection
	}

	def private calculateCrossSection(Long disk, double width, double height) {
		val crossSection = (-(width / 2 ) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", " +
			((width / 2 ) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height))
		connector.executeWrite("MATCH (n)  WHERE ID(n) = " + disk + " SET n.crossSection = \'" + crossSection + "\'")
	}

	def private calculateSpines(Iterator<Node> segments, double factor) {
		log.debug("in calculate spines")
		if (config.outputFormat == OutputFormat::X3D) {
			var spinePointCount = 0
			if (segments.size < 50) {
				spinePointCount = 400
			} else {
				spinePointCount = 1000
			}
			val completeSpine = newArrayOfSize(spinePointCount)
			val stepX = 2 * Math::PI / spinePointCount;

			for (i : 0 ..< spinePointCount) {
				completeSpine.set(i, factor * Math::cos(i * stepX) + " " + factor * Math::sin(i * stepX) + " " + 0.0)
			}
			completeSpine.set(spinePointCount - 1, completeSpine.get(0))
			// calculate spines according  to fractions
			var start = 0
			var end = 0
			val statementList = newArrayList
			while (segments.hasNext) {
				val segment = segments.next
				val size = segment.get("size").asDouble
				// val entity = segment.getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode
				start = end;
				end = start + Math::floor(spinePointCount * size).intValue
				if (end > (completeSpine.length - 1)) {
					end = completeSpine.length - 1
				}
				if (segment == segments.last) {
					end = completeSpine.length - 1
				}
				val partSpine = newArrayOfSize(end - start);
				for (j : 0 ..< end - start) {
					partSpine.set(j, completeSpine.get(start + j))
				}
				statementList +=
					"MATCH (n) WHERE ID(n) = " + segment.id + " SET n.spine = \'" + partSpine.removeBrackets + "\'"
			}
			connector.executeWrite(statementList)
		}
		if (config.outputFormat == OutputFormat::AFrame) {
			if (!segments.empty) {
				var length = segments.size
				var sizeSum = 0.0
				var position = 0.0
				while (segments.hasNext) {
					val segment = segments.next
					val size = segment.get("size").asDouble
					sizeSum += size
				}
				sizeSum += sizeSum / 360 * length
				while (segments.hasNext) {
					log.info("Nach erstem Iterator-Durchlauf sind wir wieder am Anfang")
					val segment = segments.next
					val angle = (segment.get("size").asDouble / sizeSum) * 360
					connector.executeWrite(
						"MATCH (n) WHERE ID(n) = " + segment.id + " SET n.angle = " + angle + ", n.anglePosition = " +
							position)
					position += angle + 1
				}
			}
		}
	}

	def private calculateSpines(Long disk, double factor) {
		val spinePointCount = 50
		val completeSpine = newArrayOfSize(spinePointCount)
		val stepX = 2 * Math::PI / spinePointCount;
		for (i : 0 ..< spinePointCount) {
			completeSpine.set(i, factor * Math::cos(i * stepX) + " " + factor * Math::sin(i * stepX) + " " + 0.0)
		}
		completeSpine.set(spinePointCount - 1, completeSpine.get(0))
		connector.executeWrite("MATCH (n) WHERE ID(n) = " + disk + " SET n.spine = \'" + completeSpine.removeBrackets +
			"\'")
	}

	def private RGBColor[] createColorGradiant(RGBColor start, RGBColor end, int maxLevel) {
		var steps = maxLevel
		val r_step = (end.r - start.r) / steps
		val g_step = (end.g - start.g) / steps
		val b_step = (end.b - start.b) / steps

		val colorRange = newArrayOfSize(maxLevel)
		for (i : 0 ..< maxLevel) {
			val newR = start.r + i * r_step
			val newG = start.g + i * g_step
			val newB = start.b + i * b_step

			colorRange.set(i, new RGBColor(newR, newG, newB))
		}
		return colorRange
	}

	def private getDisks() {
		return connector.executeRead("MATCH (n:Model:RD)-[:CONTAINS*]->(d:Disk) RETURN d")
	}

	def private getSubDisks(Long entity) {
		return connector.executeRead("MATCH (n)-[:CONTAINS]->(d:Disk) WHERE ID(n) = " + entity + " RETURN d").map [
			return get("d").asNode
		]
	}

	def private getRootDisks() {
		return connector.executeRead("MATCH (n:RD:Model)-[:CONTAINS]->(d:Disk) RETURN d").map [
			return get("d").asNode
		]
	}
}
