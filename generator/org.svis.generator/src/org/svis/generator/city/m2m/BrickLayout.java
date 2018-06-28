package org.svis.generator.city.m2m;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.svis.generator.SettingsConfiguration;
import org.svis.generator.city.CityDebugUtils;
import org.svis.generator.city.CityUtils;
import org.svis.xtext.city.Building;
import org.svis.xtext.city.BuildingSegment;
import org.svis.xtext.city.CityFactory;
import org.svis.xtext.city.Entity;
import org.svis.xtext.city.Position;
import org.svis.xtext.city.Root;
import org.svis.xtext.city.impl.CityFactoryImpl;

public class BrickLayout {
	private static final boolean DEBUG = false;
	private static final CityFactory cityFactory = new CityFactoryImpl();
	private static SettingsConfiguration config = SettingsConfiguration.getInstance();

	public static void brickLayout(final Root root) {
		if(DEBUG) System.out.println("[ INFO]"+ " brickLayout(root)-arrival.");

		final EList<Entity> entities = root.getDocument().getEntities();
		seperateEntities(entities);

		if(DEBUG) CityDebugUtils.infoEntities(entities, 0, true, true);
		if(DEBUG) System.out.println("[ INFO]"+ " brickLayout(root)-exit.");
	}

	// Recursive lookup for buildings/classes
	private static void seperateEntities(final EList<Entity> entities) {
		for (Entity e : entities) {
			// Search for buildings
			if(e.getType().equals("FAMIX.Class") || e.getType().equals("FAMIX.ParameterizableClass")) {
				seperateBuilding((Building) e);
			}
			// look deeper inside hierarchy
			seperateEntities(e.getEntities());
		}
	}

	// Builds up the bricks for a specific given building/class
	private static void seperateBuilding(final Building b) {
		// Don't build up bricks, if this building isn't visualized or isn't positioned (e.g. is an inner classes)
		if (b.getPosition() == null) {
			return;
		}

		// variables for brick algorithm
		int sc, 	// side capacity
			lc,		// layer capacity
			biws,	// brick index within side
			biwl,	// brick index within layer
			si,		// side index - north,east,...
			bsPosIndex_X, bsPosIndex_Y, bsPosIndex_Z;
		double b_lowerLeftX, b_upperY, b_lowerLeftZ;
		sc = b.getSideCapacity();

		// Get elements for modeling
		EList<BuildingSegment> classElements = new BasicEList<BuildingSegment>();
		switch (config.getClassElementsModeAsString()) {
			case "attributes_only":
				classElements.addAll(b.getData());
				break;
			case "methods_only":
				classElements.addAll(b.getMethods());
				break;
			case "methods_and_attributes":
				classElements.addAll(b.getData());
				classElements.addAll(b.getMethods());
				break;
			default:
				classElements.addAll(b.getMethods());
				break;
		}

		// Sorting elements
		CityUtils.sortBuildingSegments(classElements);
		
		// coordinates of edges of building
		b_lowerLeftX = b.getPosition().getX() - b.getWidth()/2;
		b_lowerLeftZ = b.getPosition().getZ() - b.getLength()/2;
		b_upperY = b.getPosition().getY() + b.getHeight()/2;
//System.out.println("");
		BuildingSegment bs;
		// set positions for all methods in current class
		for(int i=0; i<classElements.size(); ++i) {
			bs = classElements.get(i);
			if(sc <= 1) {
				lc = 1;
				biws = 0;
				si = 0;
			} else {
				lc = (sc-1) * 4;
				biwl = i % lc;
				biws = biwl % (sc-1);
				si = biwl / (sc-1);
			}
//			System.out.println(bs.getType() + " " + bs.getValue() + " " + bs.getModifiers() + " " + bs.getNumberOfStatements());
			// calculating position for brick
			switch(si) {
				case 0:
					bsPosIndex_X = biws;
					bsPosIndex_Z = 0;
					break;
				case 1:
					bsPosIndex_X = sc - 1;
					bsPosIndex_Z = biws;
					break;
				case 2:
					bsPosIndex_X = sc - biws - 1;
					bsPosIndex_Z = sc - 1;
					break;
				default:
					bsPosIndex_X = 0;
					bsPosIndex_Z = sc - biws - 1;
					break;
			}
			bsPosIndex_Y = i / lc;

			// setting position for brick
			Position pos = cityFactory.createPosition();
			pos.setX(b_lowerLeftX + config.getBrickHorizontalMargin()
					+ (config.getBrickHorizontalGap() + config.getBrickSize() * bsPosIndex_X
					+ config.getBrickSize() * 0.5));
			pos.setZ(b_lowerLeftZ + config.getBrickHorizontalMargin()
					+ (config.getBrickHorizontalGap() + config.getBrickSize()) * bsPosIndex_Z
					+ config.getBrickSize() * 0.5);
			pos.setY(b_upperY + config.getBrickVerticalMargin()
					+ (config.getBrickVerticalGap() + config.getBrickSize()) * bsPosIndex_Y
					+ config.getBrickSize() * 0.5);
			bs.setPosition(pos);
		}
	}
}
