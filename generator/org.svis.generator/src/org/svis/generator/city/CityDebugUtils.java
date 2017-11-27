package org.svis.generator.city;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.eclipse.emf.common.util.EList;
import org.parboiled.common.StringUtils;
import org.svis.xtext.city.Building;
import org.svis.xtext.city.BuildingSegment;
import org.svis.xtext.city.Entity;

/**
 * This class only is for debugging purposes.
 */
public class CityDebugUtils {

	/**
	 * Writes info about the given entity to console.<br>
	 * Used for debugging purposes.
	 * 
	 * @param entity
	 *            Package, class or method to print the information from.
	 * @param depth
	 *            Number of leading tabulators before the entity info to better
	 *            show the hierarchy.
	 * @see #infoEntities
	 * @see #infoBuildingSegments
	 */
	public static void info(final Entity entity, int depth) {
		StringBuilder sb = new StringBuilder(StringUtils.repeat('\t', depth))
				.append("<").append(entity.getType()).append("> \"")
				.append(entity.getFqn()).append("\"");
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		if (entity.getPosition() == null) {
			sb.append("  no Pos!");
		} else {
			sb.append("  Pos(").append(df.format(entity.getPosition().getX())).append("|")
					.append(df.format(entity.getPosition().getY())).append("|")
					.append(df.format(entity.getPosition().getZ())).append(")");
		}
		sb.append("  Dim(").append(df.format(entity.getWidth())).append("|").append(df.format(entity.getHeight()))
				.append("|").append(df.format(entity.getLength())).append(")");

		if (entity.getType().equals("FAMIX.Class")) {
			sb.append("  Methods:").append(df.format(((Building) entity).getMethodCounter()));
			sb.append("  Data:").append(df.format(((Building) entity).getDataCounter()));
		} else if (entity.getType().equals("FAMIX.Method")) {
			BuildingSegment bs = (BuildingSegment) entity;
			sb.insert(depth, "-> ");
			sb.append(" - returns("
					+ ((Boolean.parseBoolean(bs.getDeclaredType().getIsPrimitiveType())) ? "primitive" : "complex") + " "
					+ bs.getDeclaredType().getValue());
			if (bs.getDeclaredType().getArguments().length() > 0)
				sb.append(" Arguments(" + bs.getDeclaredType().getArguments() + ")");
			sb.append(")");
			sb.append("  NOS:").append(df.format(bs.getNumberOfStatements()));
//			sb.append("  Modifier:\"").append(bs.getModifiers() + "\"");
		} else if (entity.getType().equals("FAMIX.Attribute")) {
			BuildingSegment bs = (BuildingSegment) entity;
			sb.insert(depth, "#  ");
//			sb.append("  Value:").append(bs.getValue()).append("  Modifier:\"")
//					.append(bs.getModifiers() + "\"");
			sb.append(" - returns("
					+ ((Boolean.parseBoolean(bs.getDeclaredType().getIsPrimitiveType())) ? "primitive" : "complex") + " "
					+ bs.getDeclaredType().getValue());
			if (bs.getDeclaredType().getArguments().length() > 0)
				sb.append(" Arguments(" + bs.getDeclaredType().getArguments() + ")");
			sb.append(")");
		}

		System.out.println(sb.toString());
	}

	/**
	 * Writes info about entities to console.<br>
	 * Used for debugging purposes.
	 * 
	 * @param entitiesList
	 *            List of packages, class and methods to print the information
	 *            from.
	 * @param depth
	 *            Number of leading tabulators before the method info to better
	 *            show the hierarchy.
	 * @param showMethods
	 *            If {@code TRUE}, methods are show.
	 * @param showData
	 *            If {@code TRUE}, attributes are show.
	 * @see #infoBuildingSegments
	 * @see #info
	 */
	public static void infoEntities(final EList<Entity> entitiesList, int depth, boolean showMethods, boolean showData) {
		for (Entity e : entitiesList) {
			info(e, depth);
			infoEntities(e.getEntities(), depth + 1, showMethods, showData);
			if (showData && (e.getType().equals("FAMIX.Class") || e.getType().equals("FAMIX.ParameterizableClass"))) {
				infoBuildingSegments(((Building) e).getData(), depth + 1, showMethods, showData);
			}
			if (showMethods && (e.getType().equals("FAMIX.Class") || e.getType().equals("FAMIX.ParameterizableClass"))) {
				infoBuildingSegments(((Building) e).getMethods(), depth + 1, showMethods, showData);
			}
		}
	}

	/**
	 * Writes info about the given methods to console.<br>
	 * Used for debugging purposes.
	 * 
	 * @param segmentsList
	 *            List of methods to print the information from.
	 * @param depth
	 *            Number of leading tabulators before the entity info to better
	 *            show the hierarchy.
	 * @param showMethods
	 *            If {@code TRUE}, methods are show.
	 * @param showData
	 *            If {@code TRUE}, attributes are show.
	 * @see #infoEntities
	 * @see #info
	 */
	public static void infoBuildingSegments(final EList<BuildingSegment> segmentsList,
			int depth, boolean showMethods, boolean showData) {
		for (BuildingSegment e : segmentsList) {
			if (showMethods && e.getType().equals("FAMIX.Method"))
				info(e, depth);
			if (showData && (e.getType().equals("FAMIX.Attribute") || e.getType().equals("FAMIX.Enum")))
				info(e, depth);
		}
	}

//	/**
//	 * Writes info about the given attributes to console.<br>
//	 * Used for debugging purposes.
//	 * 
//	 * @param entity
//	 *            List of attributes to print the information from.
//	 * @param depth
//	 *            Number of leading tabulators before the entity info to better
//	 *            show the hierarchy.
//	 * @see #infoEntities
//	 * @see #info
//	 */
//	public static void infoMethods(final EList<BuildingSegment> methodsList, int depth) {
//		for (BuildingSegment e : methodsList) {
//			info(e, depth);
//		}
//	}

	private static class Count {
		int i = 0;
		int pos = 0;
	}

	/**
	 * Counts the entities and whether there position attribute is initialized
	 * or not.<br>
	 * Used for debugging purposes.
	 * @see Count
	 *
	 * @param entities List of entities
	 */
	public static void countSetPositions(final EList<Entity> entities) {
		final Count c = new Count();
		countSetPositionsRecursive(entities, c);
		System.out.println("#Entities: " + c.i + " #Pos: " + c.pos);
	}

	private static void countSetPositionsRecursive(final EList<Entity> entities, final Count c) {
		for (Entity e : entities) {
			++c.i;
			if (e.getPosition() != null)
				++c.pos;
			countSetPositionsRecursive(e.getEntities(), c);
		}
	}

	private static class NosStats {
		private int classesSum = 0;
		private double attributesSum = 0;
		private double attributesMax = 0;
		private double methodsSum = 0;
		private double methodsMax = 0;
		private double nosSum = 0;
		private double nosMax = 0;
	}
	
	public static void nosInfo(final EList<Entity> entities) {
		NosStats stats = new NosStats();
		nosInfoRecursive(entities, stats);
		System.out.println("#Classes: " + stats.classesSum);
		System.out.println("#Data: " + stats.attributesSum + "    Max in Class: " + stats.attributesMax
				+ "    ØData per Class: " + stats.attributesSum/stats.classesSum);
		System.out.println("#Methods: " + stats.methodsSum + "    Max in Class: " + stats.methodsMax
				+ "    ØMethods per Class: " + stats.methodsSum/stats.classesSum);
		System.out.println("#NOS: " + stats.nosSum + "    Max in Method: " + stats.nosMax
				+ "    ØNOS per Method: " + stats.nosSum/stats.methodsSum);
	}

	private static void nosInfoRecursive(final EList<Entity> entities, final NosStats stats) {
		for (Entity e : entities) {
			if ((e.getType().equals("FAMIX.Class") || e.getType().equals("FAMIX.ParameterizableClass"))) {
				Building b = (Building) e;
				++stats.classesSum;
				stats.attributesSum += b.getDataCounter();
				stats.methodsSum += b.getMethodCounter();
				if (b.getDataCounter() > stats.attributesMax)
					stats.attributesMax = b.getDataCounter();
				if (b.getMethodCounter() > stats.methodsMax)
					stats.methodsMax = b.getMethodCounter();
				for (BuildingSegment bs : b.getMethods()) {
					stats.nosSum += bs.getNumberOfStatements();
					if (bs.getNumberOfStatements() > stats.nosMax)
						stats.nosMax = bs.getNumberOfStatements();
				}
			}
			nosInfoRecursive(e.getEntities(), stats);
		}
	}

}
