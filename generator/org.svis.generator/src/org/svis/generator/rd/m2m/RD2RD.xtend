package org.svis.generator.rd.m2m

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.CoordinateList
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.util.GeometricShapeFactory
import java.util.ArrayList
import java.util.List
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.xtext.EcoreUtil2
import org.svis.generator.FamixUtils
import org.svis.xtext.rd.Disk
import org.svis.xtext.rd.DiskSegment
import org.svis.xtext.rd.Document
import org.svis.xtext.rd.Root
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.svis.generator.SettingsConfiguration
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.apache.commons.logging.LogFactory
import org.svis.generator.SettingsConfiguration.OutputFormat
import org.svis.generator.SettingsConfiguration.EvolutionRepresentation
import org.svis.generator.SettingsConfiguration.MetricRepresentation

class RD2RD extends WorkflowComponentWithModelSlot {
	val config = SettingsConfiguration.instance
	val log = LogFactory::getLog(class)
	// TODO solve it with injection
	// @Inject extension FamixUtils
	extension FamixUtils util = new FamixUtils

	// TODO set colors via RGBColor class for all entities
	// color scheme
	private RGBColor NS_colorStart = new RGBColor(150, 150, 150);
	private RGBColor NS_colorEnd = new RGBColor(240, 240, 240); // from CodeCity
	// color scheme
	private RGBColor[] NS_colors

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("RD2RD has started.")
		val diskRoots = ctx.get("rd") as List<Root>
		val resource = new ResourceImpl()
		val diskList = newArrayList

		for (diskRoot : diskRoots) {
			val disks = EcoreUtil2::getAllContentsOfType(diskRoot, Disk)
			val diskDocument = diskRoot.document
			if (!disks.empty) {

				val rootDisks = diskDocument.disks
				val namespaces = disks.filter[type.equals("FAMIX.Namespace")]
				val diskMaxLevel = disks.sortBy[-getLevel].head.getLevel
				val namespaceMaxLevel = namespaces.sortBy[-level].head.level

				NS_colors = createColorGradiant(NS_colorStart, NS_colorEnd, namespaceMaxLevel)
				namespaces.forEach[setNamespaceColor]

				rootDisks.calculateNetArea
				disks.forEach[calculateRadius]
				disks.forEach[maxLevel = diskMaxLevel]
				diskDocument.calculateLayout
				// calculate label positions for disks
				disks.forEach[postLayout]
			}
			// val diskResource = diskRoot.toResource
			diskList += diskRoot
			resource.contents += diskRoot
		}
		// put created target model in slot
		// put diskroot into list (for writer)
		if (!(config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_TIME_LINE ||
			config.evolutionRepresentation == EvolutionRepresentation::MULTIPLE_DYNAMIC_EVOLUTION)) {
			if (diskList.size == 1) {
				val diskRoot = diskList.get(0)
				ctx.set("rdextendedwriter", diskRoot)
			}
		}
		ctx.set("rdextended", resource)
		log.info("RD2RD has finished.")
	}

	def private void calculateNetArea(List<Disk> disks) {
		for (disk : disks) {
			if (!disk.disks.nullOrEmpty) {
				disk.disks.calculateNetArea
			}
			disk.calculateNetArea
		}
	}

	/**	estimates the net area of disks
	 * 
	 * it doesn't consider RING_WIDTH because the exact circle is not known yet
	 * so the values are too low and gets updated in updateNetArea after the layout calculation
	 */
	def private calculateNetArea(Disk disk) {
		// set sizes of methods and data
		disk.data.forEach[size = size * config.RDDataFactor]
		disk.methods.forEach[size = size * config.RDMethodFactor]
		disk.netArea = disk.methods.sum + disk.data.sum

		if (disk.netArea == 0 && (disk.type.equals("FAMIX.Class ") || disk.type.equals("FAMIX.ParameterizableClass"))) {
			disk.netArea = config.RDMinArea
		// get sizes from nested disks
		// if ((!disk.disks.nullOrEmpty) && (disk.type.equals("FAMIX.Namespace") || disk.type.equals("FAMIX.Class") || disk.type.equals("FAMIX.ParameterizableClass"))) {
		}
	}

	def calculateRadius(Disk disk) {
		disk.radius = Math::sqrt(disk.netArea / Math::PI) + disk.ringWidth
	}

	def private calculateLayout(Document diskDocument) {
		// transform disks into circles
		val nestedCircles = new ArrayList<CircleWithInnerCircles>
		diskDocument.disks.forEach[f|nestedCircles += new CircleWithInnerCircles(f)]
		// calculate disk layout
		RDLayout::nestedLayout(nestedCircles)
		nestedCircles.forEach[updateDisk]
	}

	def setNamespaceColor(Disk namespace) {
		if (config.outputFormat == OutputFormat::AFrame) {
			namespace.color = config.RDNamespaceColorHex
		} else {
			namespace.color = NS_colors.get(namespace.getLevel() - 1).asPercentage
		}
	}

	def private postLayout(Disk disk) {
		disk.fractions
		disk.data.fractions
		if (config.outputFormat != OutputFormat::SimpleGlyphsJson) {
			disk.methods.fractions
		}
		disk.disks.forEach[calculateRings]
		disk.calculateRings
	}

	/**
	 * Calculates proportion of method and data area to the overall disk area  
	 */
	def private fractions(Disk disk) {
		disk.methodArea = disk.methods.sum / disk.netArea
		disk.dataArea = disk.data.sum / disk.netArea
	}

	/**
	 * Calculates the proportion of one method/attribute to all methods/attributes
	 */
	def private fractions(EList<DiskSegment> segments) {
		val sum = segments.sum
		segments.forEach [
			size = size / sum
		]
	}

	def private void calculateRings(Disk disk) {
		// disk rings
		if (disk.ringWidth == 0) {
			calculateCrossSection(disk, disk.ringWidth, 0)
		} else {
			calculateCrossSection(disk, disk.ringWidth, disk.height)
		}
		calculateSpines(disk, disk.radius - (0.5 * disk.ringWidth))
		if (disk.disks.nullOrEmpty) {
			val r_data = Math::sqrt(disk.dataArea * disk.netArea / Math::PI)
			val r_methods = disk.radius - disk.ringWidth
			val b_methods = r_methods - r_data

			if (!disk.methods.nullOrEmpty) {
				disk.methods.calculateCrossSection(b_methods, disk.height)
				calculateSpines(disk.methods, r_methods - 0.5 * b_methods)
				if (config.outputFormat == OutputFormat::X3DOM || config.outputFormat == OutputFormat::AFrame) {
					disk.methods.forEach[m|m.outerRadius = r_methods; m.innerRadius = r_data]
				}
			}
			if (!disk.data.nullOrEmpty) {
				disk.data.calculateCrossSection(r_data, disk.height)
				calculateSpines(disk.data, 0.5 * r_data)
				if (config.outputFormat == OutputFormat::X3DOM || config.outputFormat == OutputFormat::AFrame) {
					disk.data.forEach[d|d.outerRadius = r_data; d.innerRadius = 0]
				}
			}
		} else {
			val outerRadius = disk.calculateOuterRadius
			val r_data = Math::sqrt((disk.dataArea * disk.netArea / Math::PI) + (outerRadius * outerRadius))
			val b_data = r_data - outerRadius
			val r_methods = Math::sqrt((disk.methodArea * disk.netArea / Math::PI) + (r_data * r_data))
			val b_methods = r_methods - r_data

			if (!disk.methods.nullOrEmpty) {
				disk.methods.calculateCrossSection(b_methods, disk.height)
				calculateSpines(disk.methods, r_methods - 0.5 * b_methods)
				if (config.outputFormat == OutputFormat::X3DOM || config.outputFormat == OutputFormat::AFrame) {
					disk.methods.forEach[m|m.outerRadius = r_methods; m.innerRadius = r_data]
				}
			}
			if (!disk.data.nullOrEmpty) {
				disk.data.calculateCrossSection(b_data, disk.height)
				calculateSpines(disk.data, r_data - 0.5 * b_data)
				if (config.outputFormat == OutputFormat::X3DOM || config.outputFormat == OutputFormat::AFrame) {
					disk.data.forEach[d|d.outerRadius = r_data; d.innerRadius = r_data - b_data]
				}
			}
		}
	}

	def private calculateOuterRadius(Disk disk) {
		val coordinates = new CoordinateList()
		for (f : disk.disks) {
			coordinates.add(createCircle(f.position.x, f.position.y, f.radius).coordinates, false)
		}
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

	def private calculateCrossSection(EList<DiskSegment> segments, double width, double height) {
		if (!(config.metricRepresentation == MetricRepresentation::NONE)) {
			segments.forEach [
				val crossSection = (-(width / 2 ) + " " + (it.height) + ", " + (width / 2) + " " + (it.height) + ", " +
					(width / 2 ) + " " + 0) + ", " + -(width / 2) + " " + 0 + ", " + -(width / 2) + " " + (it.height)
				it.crossSection = crossSection
			]
		} else {
			val crossSection = (-(width / 2 ) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", " +
				((width / 2 ) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height))

			segments.forEach[s|s.crossSection = crossSection]
		}
	}

	def private calculateCrossSection(Disk disk, double width, double height) {
		val crossSection = (-(width / 2 ) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", " +
			((width / 2 ) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height))

		disk.crossSection = crossSection
	}

	def private calculateSpines(EList<DiskSegment> segments, double FACTOR) {
		// TODO concrete array
		var spinePointCount = 0

		if (segments.length < 50) {
			spinePointCount = 400
		} else {
			spinePointCount = 1000
		}
		val completeSpine = newArrayOfSize(spinePointCount)
		val stepX = 2 * Math::PI / spinePointCount;

		// var stepY = 2 * Math::PI / SPINEPOINTCOUNT;
		for (i : 0 ..< spinePointCount) {
			// TODO remove
			if (FACTOR <= 0) {
				// println(segments.size)
				// println("Damnit: " + segments.get(i).fqn)
			}
			completeSpine.set(i, FACTOR * Math::cos(i * stepX) + " " + FACTOR * Math::sin(i * stepX) + " " + 0.0)

		// println("Spine: " + i + " " + FACTOR * Math::cos(i * stepX) + " " + FACTOR * Math::sin(i * stepX) + " " + 0.0)
		}

		completeSpine.set(spinePointCount - 1, completeSpine.get(0))

		// calculate spines according  to fractions
		var start = 0
		var end = 0

		// for (i : 0 ..< segments.length) {
		for (segment : segments) {
			start = end;
			end = start + Math::floor(spinePointCount * segment.size).intValue
			// if (end == start) {
			// end = start + 1
			// }
			if (end == 0) {
				println(segment.fqn + " " + segment.size + " " + Math::floor(spinePointCount * segment.size))
			}
			// println("start: " +start+ " end: " +end +" end-start " + (end-start))
			if (end > (completeSpine.length - 1)) {
				end = completeSpine.length - 1
			}
			if (segment == segments.last) {
				end = completeSpine.length - 1
			}
			val partSpine = newArrayOfSize(end - start);
			for (j : 0 ..< end - start) { // for (j : 0 .. ((end - start) - 1)) {
				partSpine.set(j, completeSpine.get(start + j))
			}
			if (partSpine.length > 1) {
				// println("Partspine too short: " + segments.get(i).fqn)
				// partSpine.remove(partSpine.length - 1) // for space between rings
			}
			segment.spine = partSpine.removeBrackets
		}

		if (config.outputFormat == OutputFormat::X3DOM) {
			// set positionAngle and angle of Segments
			val separationFactor = 0.05
			// 5 % of the circle will be used to separate the segments
			if (!(segments.empty)) {
				var completeSeparation = (2 * Math::PI * separationFactor)
				var separationPerSegment = completeSeparation / (segments.length)
				var position = 0.0
				// TODO: find better way to calculate the size sum 
				var sizeSum = 0.0
				for (DiskSegment segment : segments) {
					sizeSum = sizeSum + segment.size
				}
				// println("Neue Segments")
				for (DiskSegment segment : segments) {
					segment.anglePosition = position
					segment.angle = (segment.size / sizeSum) * Math::PI * 2
					position = position + segment.angle + separationPerSegment
				}
			}
		} else if (config.outputFormat == OutputFormat::AFrame) {
			if (!segments.empty) {
				val separationFactor = 1
				var sizeSum = 0.0
				var position = 0.0

				var completeSeparation = (360 * separationFactor)
				var separationPerSegment = completeSeparation / (segments.length)

				for (DiskSegment segment : segments) {
					sizeSum = sizeSum + segment.size
				}
				sizeSum += sizeSum / 360 * segments.length
				for (DiskSegment segment : segments) {
					segment.anglePosition = position
					segment.angle = (segment.size / sizeSum) * 360
					position = position + segment.angle + 1
				}
			}
		}
	}

	def private calculateSpines(Disk disk, double FACTOR) {
		// TODO concrete array
		val spinePointCount = 50
		val completeSpine = newArrayOfSize(spinePointCount)
		val stepX = 2 * Math::PI / spinePointCount;

		// var stepY = 2 * Math::PI / SPINEPOINTCOUNT;
		for (i : 0 ..< spinePointCount) {

			// TODO remove
			if (FACTOR <= 0) {
				println("Damnit: " + disk.getFqn)
			}
			completeSpine.set(i, FACTOR * Math::cos(i * stepX) + " " + FACTOR * Math::sin(i * stepX) + " " + 0.0)
		}

		completeSpine.set(spinePointCount - 1, completeSpine.get(0))
		disk.spine = completeSpine.removeBrackets
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
}
