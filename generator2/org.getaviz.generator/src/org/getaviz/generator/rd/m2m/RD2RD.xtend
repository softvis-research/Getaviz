package org.getaviz.generator.rd.m2m

import org.getaviz.generator.SettingsConfiguration
import org.getaviz.lib.database.Database
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Direction
import org.getaviz.lib.database.Rels
import org.getaviz.lib.database.Labels
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

class RD2RD {
	val config = SettingsConfiguration.instance
	val graph = Database::getInstance(config.databaseName)
	val log = LogFactory::getLog(class)
	extension Helper util = new Helper

	// TODO set colors via RGBColor class for all entities
	// color scheme
	RGBColor NS_colorStart = new RGBColor(150, 150, 150);
	RGBColor NS_colorEnd = new RGBColor(240, 240, 240); // from CodeCity
	RGBColor[] NS_colors

	new() {
		log.info("RD2RD started")
		var tx = graph.beginTx
		try {
			var result = graph.execute(
				"MATCH p=(n:Package)-[:CONTAINS*]->(m:Package) WHERE NOT (m)-[:CONTAINS]->(:Package) RETURN max(length(p)) AS length")
			val namespaceMaxLevel = (result.head.get("length") as Long).intValue + 1
			// Returns the longest Path from root to deepest sub package
			result = graph.execute(
				"MATCH p=(n:RD:Model)-[:CONTAINS*]->(m:RD:Disk) WHERE NOT (m)-[:CONTAINS]->(:RD:Disk) RETURN max(length(p)) AS length")
			val diskMaxLevel = (result.head.get("length") as Long).intValue
			NS_colors = createColorGradiant(NS_colorStart, NS_colorEnd, namespaceMaxLevel)
			getDisks.forEach [
				if (getSingleRelationship(Rels.VISUALIZES, Direction.OUTGOING).endNode.hasLabel(Labels.Package)) {
					setNamespaceColor
				}
				setProperty("maxLevel", diskMaxLevel)
			]
			tx.success
		} finally {
			tx.close
		}

		tx = graph.beginTx
		try {
			getRootDisks.calculateNetArea
			tx.success
		} finally {
			tx.close
		}

		tx = graph.beginTx
		try {
			getDisks.forEach[calculateRadius]
			tx.success
		} finally {
			tx.close
		}

		tx = graph.beginTx
		try {
			getRootDisks.calculateLayout
			tx.success
		} finally {
			tx.close
		}

		tx = graph.beginTx
		try {
			getDisks.forEach[postLayout]
			tx.success
		} finally {
			tx.close
		}
		tx = graph.beginTx
		try {
			getDisks.forEach[postLayout2]
			tx.success
		} finally {
			tx.close
		}
		log.info("RD2RD finished")
	}

	def private setNamespaceColor(Node namespaceDisk) {
		var String color
		val result = graph.execute("MATCH p=(n:RD:Model)-[:CONTAINS*]->(m:RD:Disk) where ID(m) = " + namespaceDisk.id +
			" RETURN max(length(p)) AS length")
		val level = (result.head.get("length") as Long).intValue
		if (config.outputFormat == OutputFormat::AFrame) {
			color = config.RDNamespaceColorHex
		} else {
			// namespace.color = NS_colors.get(namespace.getLevel() - 1).asPercentage
			color = NS_colors.get(level - 1).asPercentage
		}
		namespaceDisk.setProperty("color", color)
	}

	def private void calculateNetArea(Iterable <Node> disks) {
		disks.forEach[disk|
			RDUtils::getSubDisks(disk).calculateNetArea
			disk.calculateNetArea
		]
	}

	def private calculateNetArea(Node disk) {
		var netArea = 0.0
		var methodSum = 0.0
		var dataSum = 0.0
		for(field : RDUtils::getData(disk)) {
			val size = field.getProperty("size") as Double * config.RDDataFactor
			field.setProperty("size", size)
			dataSum += size
		}
		for(method : RDUtils::getMethods(disk)){
			val size = method.getProperty("size") as Double * config.RDMethodFactor
			method.setProperty("size", size)
			methodSum += size
		}
		netArea = dataSum + methodSum
		disk.setProperty("netArea", netArea)
	}

	def private calculateRadius(Node disk) {
		val netArea = disk.getProperty("netArea") as Double
		val ringWidth = disk.getProperty("ringWidth") as Double
		var radius = Math::sqrt(netArea / Math::PI) + ringWidth
		disk.setProperty("radius", radius)
	}

	def private calculateLayout(Iterable<Node> disks) {
		val nestedCircles = new ArrayList<CircleWithInnerCircles>
		disks.forEach[disk|nestedCircles += new CircleWithInnerCircles(disk, false)]
		RDLayout::nestedLayout(nestedCircles)
		nestedCircles.forEach[updateDiskNode]
	}

	def private postLayout(Node disk) {
		val data = RDUtils.getData(disk)
		val methods = RDUtils.getMethods(disk)
		disk.fractions(data, methods)
		data.fractions
		methods.fractions
	}

	def private postLayout2(Node disk) {
		RDUtils::getSubDisks(disk).forEach[calculateRings]
		disk.calculateRings
	}

	def private fractions(Node disk, Iterable<Node> data, Iterable<Node> methods) {
		val netArea = disk.getProperty("netArea") as Double
		var currentMethodArea = RDUtils.sum(methods) / netArea
		var currentDataArea = RDUtils.sum(data) / netArea
		disk.setProperty("methodArea", currentMethodArea)
		disk.setProperty("dataArea", currentDataArea)
	}

	def private fractions(Iterable<Node> segments) {
		val sum = RDUtils.sum(segments)
		segments.forEach [
			setProperty("size", getProperty("size") as Double / sum)
		]
	}

	def private calculateRings(Node disk) {
		val ringWidth = disk.getProperty("ringWidth") as Double
		val height = disk.getProperty("height") as Double
		val radius = disk.getProperty("radius") as Double
		var methodArea = disk.getProperty("methodArea") as Double
		var dataArea = disk.getProperty("dataArea") as Double
		val netArea = disk.getProperty("netArea") as Double
		if (ringWidth == 0) {
			calculateCrossSection(disk, ringWidth, 0)
		} else {
			calculateCrossSection(disk, ringWidth, height)
		}
		calculateSpines(disk, radius - (0.5 * ringWidth))
		if (RDUtils::getSubDisks(disk).nullOrEmpty) {
			val r_data = Math::sqrt(dataArea * netArea / Math::PI)
			val r_methods = radius - ringWidth
			val b_methods = r_methods - r_data
			val diskMethods = RDUtils.getMethods(disk)
			val diskData = RDUtils.getData(disk)
			if (!diskMethods.nullOrEmpty) {
				diskMethods.calculateCrossSection(b_methods, height)
				calculateSpines(diskMethods, r_methods - 0.5 * b_methods)
				if (config.outputFormat == OutputFormat::AFrame) {
					diskMethods.forEach [
						setProperty("outerRadius", r_methods)
						setProperty("innerRadius", r_data)
					]
				}
			}
			if (!diskData.nullOrEmpty) {
				diskData.calculateCrossSection(r_data, height)
				calculateSpines(diskData, 0.5 * r_data)
				if (config.outputFormat == OutputFormat::AFrame) {
					diskData.forEach [
						setProperty("outerRadius", r_data)
						setProperty("innerRadius", 0.0)
					]
				}
			}
		} else {
			val outerRadius = disk.calculateOuterRadius
			val r_data = Math::sqrt((dataArea * netArea / Math::PI) + (outerRadius * outerRadius))
			val b_data = r_data - outerRadius
			val r_methods = Math::sqrt((methodArea * netArea / Math::PI) + (r_data * r_data))
			val b_methods = r_methods - r_data
			val diskMethods = RDUtils.getMethods(disk)
			if (!diskMethods.nullOrEmpty) {
				diskMethods.calculateCrossSection(b_methods, height)
				calculateSpines(diskMethods, r_methods - 0.5 * b_methods)				
				if (config.outputFormat == OutputFormat::AFrame) {
					diskMethods.forEach [
						setProperty("outerRadius", r_methods)
						setProperty("innerRadius", r_data)
					]
				}
			}
			val diskData = RDUtils.getData(disk)
			if (!diskData.nullOrEmpty) {
				diskData.calculateCrossSection(b_data, height)
				calculateSpines(diskData, r_data - 0.5 * b_data)
				if (config.outputFormat == OutputFormat::AFrame) {
					diskData.forEach [
						setProperty("outerRadius", r_data)
						setProperty("innerRadius", r_data - b_data)
					]
				}
			}
		}
	}

	def private calculateOuterRadius(Node disk) {
		val coordinates = new CoordinateList()
		RDUtils::getSubDisks(disk).forEach [
			val position = getSingleRelationship(Rels.HAS, Direction.OUTGOING)
			var x = 0.0
			var y = 0.0
			if (position !== null) {
				x = position.endNode.getProperty("x") as Double
				y = position.endNode.getProperty("y") as Double
			}
			coordinates.add(createCircle(x, y, getProperty("radius") as Double).coordinates, false)
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

	def private calculateCrossSection(Iterable<Node> segments, double width, double height) {
		val crossSection = (-(width / 2 ) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", " +
				((width / 2 ) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height))
			segments.forEach[setProperty("crossSection", crossSection)]
	}

	def private calculateCrossSection(Node disk, double width, double height) {
		val crossSection = (-(width / 2 ) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", " +
			((width / 2 ) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height))

		disk.setProperty("crossSection", crossSection)
	}

	def private calculateSpines(Iterable<Node> segments, double factor) {
		if (config.outputFormat == OutputFormat::X3D) {
			var spinePointCount = 0
			if (segments.length < 50) {
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
	
			for (segment : segments) {
				val size = segment.getProperty("size") as Double
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
				segment.setProperty("spine", partSpine.removeBrackets)
			}
		}
		if (config.outputFormat == OutputFormat::AFrame) {
			if (!segments.empty) {
				var length = segments.length
				var sizeSum = 0.0
				var position = 0.0
				for (segment : segments) {
					val size = segment.getProperty("size") as Double
					sizeSum += size
				}
				sizeSum += sizeSum / 360 * length
				for (segment : segments) {
					val angle = (segment.getProperty("size") as Double / sizeSum) * 360
					segment.setProperty("angle", angle)
					segment.setProperty("anglePosition", position)
					position += angle + 1
				}
			}
		}
	}

	def private calculateSpines(Node disk, double factor) {
		val spinePointCount = 50
		val completeSpine = newArrayOfSize(spinePointCount)
		val stepX = 2 * Math::PI / spinePointCount;
		for (i : 0 ..< spinePointCount) {
			completeSpine.set(i, factor * Math::cos(i * stepX) + " " + factor * Math::sin(i * stepX) + " " + 0.0)
		}
		completeSpine.set(spinePointCount - 1, completeSpine.get(0))
		disk.setProperty("spine", completeSpine.removeBrackets)
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
		return graph.execute("MATCH (n:Model:RD)-[:CONTAINS*]->(m:Disk) RETURN m").map[return get("m") as Node]
	}

	def private getRootDisks() {
		val rootDisks = graph.execute("MATCH (n:Model:RD)-[:CONTAINS]->(m:Disk) RETURN m").map[return get("m") as Node].toList
		return rootDisks
	}
}
