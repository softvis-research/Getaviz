package org.svis.generator.plant.m2m

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.xtext.EcoreUtil2
import org.svis.xtext.plant.Stem
import org.svis.xtext.plant.Root
import org.svis.xtext.plant.Area
import org.svis.xtext.plant.Petal
import org.svis.xtext.plant.PollStem
import org.svis.generator.plant.WorkflowComponentWithPlantConfig
import org.svis.xtext.plant.Junction
import org.svis.generator.SettingsConfiguration
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.apache.commons.logging.LogFactory

class Plant2Plant extends WorkflowComponentWithModelSlot {

	val config = SettingsConfiguration.instance

	 val log = LogFactory::getLog(class)
	// TODO solve it with injection
	// @Inject extension FamixUtils
//	extension FamixUtils util = new FamixUtils
//	PlantManager pm = new PlantManager()
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Plant2Plant has started.")

		val root = ctx.get("plant") as Root
//		val document = root.document
		var areas = EcoreUtil2::getAllContentsOfType(root, Area)
		var stems = EcoreUtil2::getAllContentsOfType(root, Stem)
		var petals = EcoreUtil2::getAllContentsOfType(root, Petal)
		var pollStems = EcoreUtil2::getAllContentsOfType(root, PollStem)
		var junctions = EcoreUtil2::getAllContentsOfType(root, Junction)

		// Set plant size:
		stems.forEach[calcStemSize]

		// Calculate the lines of code for all packages:
		// Additional information, but not needed for the metaphor anymore:
		areas.forEach[calcLoc]

		// Calculate all Positions:
		PlantLayout::plantLayout(root)

		// After each plant got his position:
		stems.forEach[calcPlantValues]

		// ---------------------------------------------------
		// -------------- Configuration stuff ----------------
		// ---------------------------------------------------
		// configure textures, colors, shapes etc. : 
		areas.forEach[configArea]
		petals.forEach[configPetal]
		stems.forEach[configStem]
		pollStems.forEach[configPollStem]
		junctions.forEach[configJunction]

		// ---------------------------------------------------
		// ----------- End of Configuration stuff ------------
		// ---------------------------------------------------
		// Continue with default stuff:
		var resource = new ResourceImpl()
		resource.contents += root

		// Put created target model in slot
		ctx.set("plantextended", resource)

		// Put diskroot into list (for writer)
		ctx.set("plantextendedwriter", root)

		log.info("Plant2Plant has finished.")
	}

	/**
	 * Calculate the loc.
	 */
	def private void calcLoc(Area area) {
		for (e : area.entities) {
			area.loc = area.loc + e.loc
		}
	}

	/**
	 * Configure area.
	 */
	def private void configArea(Area area) {
		if (config.packageUseTextures) {
			if (area.level % 2 != 0) {
				area.texture = config.packageOddTexture;
			} else {
				area.texture = config.packageEvenTexture;
			}
		} else {
			if (area.level % 2 != 0) {
				area.color = config.packageOddColor;
			} else {
				area.color = config.packageEvenColor;
			}
		}
		area.shapeID = config.packageShape
	}

	/**
	 * Configure petal.
	 */
	def configPetal(Petal petal) {
		if (config.attributeUseTextures) {
			petal.texture = config.attributeTexture;
		} else {
			petal.color = config.attributeColor;
		}
		petal.shapeID = config.attributeShape;
	}

	/**
	 * Configure stem.
	 */
	def private void configStem(Stem stem) {
		if (config.classUseTextures) {
			stem.texture = config.classTexture;
			stem.headTexture = config.classTextureHeadBrown;
			stem.headTopPartTexture = config.classTextureBloom;
		} else {
			stem.color = config.plantClassColor;
			stem.headColor = config.plantClassColor02;
			stem.headTopPartColor = config.plantClassColor03;
		}
		stem.shapeID = config.classShape
	}

	/**
	 * Configure poll stem.
	 */
	def private void configPollStem(PollStem pollStem) {
		if (config.methodUseTextures) {
			pollStem.texture = config.methodTexture;
			pollStem.ballTexture = config.methodTexturePollball;
		} else {
			pollStem.color = config.methodColor;
			pollStem.ballColor = config.methodColor02;
		}
		pollStem.shapeID = config.methodShape
	}

	/**
	 * Configure junction.
	 */
	def private void configJunction(Junction junction) {
		if (config.innerClassUseTextures) {
			junction.texture = config.innerClassTexture;
			junction.headTexture = config.innerClassTextureBloom;
			junction.headTopPartTexture = config.innerClassTextureJunctionHeadTopPart;

			for (var int i = 0; i < junction.petals.size; i++) {
				junction.petals.get(i).texture = config.innerClassAttributeTexture;
			}
			for (var int i = 0; i < junction.pollstems.size; i++) {
				junction.pollstems.get(i).texture = config.innerClassMethodTexture;
			}
		}
		junction.color = config.innerClassColor;
		junction.headColor = config.innerClassColor02;
		junction.headTopPartColor = config.innerClassColor03;

		for (var int i = 0; i < junction.petals.size; i++) {
			junction.petals.get(i).color = config.innerClassAttributeColor;
			junction.petals.get(i).shapeID = config.innerClassAttributeShape
		}
		for (var int i = 0; i < junction.pollstems.size; i++) {
			junction.pollstems.get(i).color = config.innerClassMethodColor;
			junction.pollstems.get(i).shapeID = config.innerClassMethodShape
		}

		junction.shapeID = config.innerClassShape
	}

	/**
	 * Calculate and set stem size. 
	 */
	def private void calcStemSize(Stem stem) {
		if (config.classSize == "count_attributes_and_methods") {
			stem.width = config.stemThickness * 2 + stem.dataCounter + stem.methodCounter + config.stemHeight * 2 + 6;
			stem.length = config.stemThickness * 2 + stem.dataCounter + stem.methodCounter + config.stemHeight * 2 + 6;
			stem.height = config.stemHeight + stem.dataCounter + stem.methodCounter + stem.methodCounter;
		}
		if (config.classSize == "Count_Attributes") {
			stem.width = config.stemThickness * 2 + stem.dataCounter + config.stemHeight * 2 + 6;
			stem.length = config.stemThickness * 2 + stem.dataCounter + config.stemHeight * 2 + 6;
			stem.height = config.stemHeight + stem.dataCounter + stem.methodCounter;
		}
		if (config.classSize == "Count_Methods") {
			stem.width = config.stemThickness * 2 + stem.methodCounter + config.stemHeight * 2 + 6;
			stem.length = config.stemThickness * 2 + stem.methodCounter + config.stemHeight * 2 + 6;
			stem.height = config.stemHeight + stem.methodCounter + stem.methodCounter;
		}
		if (config.classSize == "LOC") {
			stem.width = config.stemThickness * 2 + stem.loc + config.stemHeight * 2 + 6;
			stem.length = config.stemThickness * 2 + stem.loc + config.stemHeight * 2 + 6;
			stem.height = config.stemHeight + stem.loc + stem.methodCounter;
		}
	}

	/**
	 * calculate all necessary plant values. 
	 */
	def private void calcPlantValues(Stem stem) {

		// reduce stem size again:
		stem.width = config.stemThickness
		stem.length = config.stemThickness

		// for all petals:
		for (var int i = 0; i < stem.petals.size; i++) {

			var double[] xyzArray = PlantManager.getPetalPositionForIndex(i)
			stem.petals.get(i).angular = PlantManager.getPetalAngularForIndex(i);

			if (i <= 15) {
				stem.petals.get(i).position.x = stem.position.x + xyzArray.get(0) * config.petalDistanceMultiplier;
				stem.petals.get(i).position.y = stem.height + stem.level + config.cronHeight + 1;
				stem.petals.get(i).position.z = stem.position.z + xyzArray.get(1) * config.petalDistanceMultiplier;
			} else {
				stem.petals.get(i).position.x = stem.position.x +
					xyzArray.get(0) * (config.petalDistanceMultiplier + Math.abs(i / 16));
				stem.petals.get(i).position.y = stem.height + stem.level + config.cronHeight + 1;
				stem.petals.get(i).position.z = stem.position.z +
					xyzArray.get(1) * (config.petalDistanceMultiplier + Math.abs(i / 16));
			}

//			stem.petals.get(i).position.x = stem.position.x + xyzArray.get(0) * PlantSettings::PETAL_DISTANCE_MULTIPLIER;
//			stem.petals.get(i).position.y = stem.position.y + xyzArray.get(1) * PlantSettings::PETAL_DISTANCE_MULTIPLIER;
//			stem.petals.get(i).position.z = (stem.height + stem.height + 2.5);
			// manage different petal arcs for different situations:
			if (xyzArray.get(0) > 0 || xyzArray.get(1) > 0) {
				stem.petals.get(i).currentPetalAngle = -config.petalAngle
				if (!(xyzArray.get(0) < 0 && xyzArray.get(1) > 0)) {
					stem.petals.get(i).currentPetalAngle = (config.petalAngle)
				}
			} else {
				stem.petals.get(i).currentPetalAngle = -config.petalAngle
			}
		} // end petal loop
		// for all pollstems:
		for (var int i = 0; i < stem.pollstems.size; i++) {
			var double[] xyzArray = PlantManager.getPetalPositionForIndex(i)
			stem.pollstems.get(i).angular = PlantManager.getPetalAngularForIndex(i);

			stem.pollstems.get(i).position.x = stem.position.x +
				xyzArray.get(0) * (config.pollstemAngleDistanceMultiplier + (i * 0.01));
			stem.pollstems.get(i).position.y = stem.height + stem.level + config.cronHeight + 1;
			stem.pollstems.get(i).position.z = stem.position.z +
				xyzArray.get(1) * (config.pollstemAngleDistanceMultiplier + (i * 0.01));

			stem.pollstems.get(i).ballPosition.x = stem.position.x +
				xyzArray.get(0) * (config.pollstemAngleDistanceMultiplier + (i * 0.020));
			stem.pollstems.get(i).ballPosition.y = stem.pollstems.get(i).position.y + 0.5;
			stem.pollstems.get(i).ballPosition.z = stem.position.z +
				xyzArray.get(1) * (config.pollstemAngleDistanceMultiplier + (i * 0.020));

			// manage different arcs for different situations:
			if (xyzArray.get(0) > 0 || xyzArray.get(1) > 0) {
				stem.pollstems.get(i).currentPollStemAngle = (config.pollstemAngle)
				if (!(xyzArray.get(0) < 0 && xyzArray.get(1) > 0)) {
					stem.pollstems.get(i).currentPollStemAngle = -(config.pollstemAngle)
				}
			} else {
				stem.pollstems.get(i).currentPollStemAngle = (config.pollstemAngle)
			}
		} // end petal loop	
		// -----------------------------------------------------------------------------------------------------
		// Handle junction stems: ---------------------------------------------------------------------------------------
		for (var int i = 0; i < stem.junctions.size; i++) {

			var Junction jun = stem.junctions.get(i);
			jun.width = config.junctionStemThickness
			jun.length = config.junctionStemThickness
			jun.height = config.stemHeight
			var double[] xyzArray = PlantManager.getPetalPositionForIndex(i);
			jun.angular = PlantManager.getPetalAngularForIndex(i);

			jun.position.x = stem.position.x + xyzArray.get(0) * config.junctionDistanceMultiplier;
			jun.position.y = Math.max(stem.height / 2 + stem.height / 4, stem.position.y + 3);
			jun.position.z = stem.position.z + xyzArray.get(1) * config.junctionDistanceMultiplier;

			jun.headPosition.x = stem.position.x + xyzArray.get(0) *
				(config.junctionDistanceMultiplier + Math.sqrt(stem.junctions.get(i).height) +
					stem.junctions.get(i).height / 2);
			jun.headPosition.y = jun.position.y + config.stemHeight + 2;
			jun.headPosition.z = stem.position.z + xyzArray.get(1) *
				(config.junctionDistanceMultiplier + Math.sqrt(stem.junctions.get(i).height) +
					stem.junctions.get(i).height / 2);

			if (xyzArray.get(0) > 0 || xyzArray.get(1) > 0) {
				stem.junctions.get(i).currentJunctionAngle = (config.junctionAngle)
				if (!(xyzArray.get(0) < 0 && xyzArray.get(1) > 0)) {
					stem.junctions.get(i).currentJunctionAngle = -(config.junctionAngle)
				}
			} else {
				stem.junctions.get(i).currentJunctionAngle = (config.junctionAngle)
			}
		}

		// Handle junction petals: ---------------------------------------------------------------------------------------
		for (var int i = 0; i < stem.junctions.size; i++) {
			var Junction jun = stem.junctions.get(i);
			// petals for all sub plant:	
			var EList<Petal> petalList = stem.junctions.get(i).petals;
			for (var int j = 0; j < petalList.size; j++) {
				var double[] xyzArrayTwo = PlantManager.getPetalPositionForIndex(j);
				petalList.get(j).angular = PlantManager.getPetalAngularForIndex(j);

				if (j <= 15) {
					petalList.get(j).position.x = jun.headPosition.x +
						xyzArrayTwo.get(0) * config.petalDistanceMultiplier;
					petalList.get(j).position.y = jun.headPosition.y + 2;
					petalList.get(j).position.z = jun.headPosition.z +
						xyzArrayTwo.get(1) * config.petalDistanceMultiplier;
				} else {
					petalList.get(j).position.x = jun.headPosition.x +
						xyzArrayTwo.get(0) * (config.petalDistanceMultiplier + j / 16);
					petalList.get(j).position.y = jun.headPosition.y + 2;
					petalList.get(j).position.z = jun.headPosition.z +
						xyzArrayTwo.get(1) * (config.petalDistanceMultiplier + j / 16);
				}

				// manage different petal arcs for different situations:
				if (xyzArrayTwo.get(0) > 0 || xyzArrayTwo.get(1) > 0) {
					petalList.get(j).currentPetalAngle = -config.petalAngle
					if (!(xyzArrayTwo.get(0) < 0 && xyzArrayTwo.get(1) > 0)) {
						petalList.get(j).currentPetalAngle = (config.petalAngle)
					}
				} else {
					petalList.get(j).currentPetalAngle = -config.petalAngle
				}
			} // end sub plant petal loop		
		}
		// Handle junction pollstems: ---------------------------------------------------------------------------------------
		for (var int i = 0; i < stem.junctions.size; i++) {
			var Junction jun = stem.junctions.get(i);
			// pollstems for all sub plant:
			var EList<PollStem> pollstemList = stem.junctions.get(i).pollstems;
			for (var int j = 0; j < pollstemList.size; j++) {
				var PollStem ps = pollstemList.get(j);
				var double[] xyzArrayTwo = PlantManager.getPetalPositionForIndex(j);
				ps.angular = PlantManager.getPetalAngularForIndex(j);

				ps.position.x = jun.headPosition.x +
					xyzArrayTwo.get(0) * (config.pollstemAngleDistanceMultiplier + (i * 0.01));
				ps.position.y = jun.headPosition.y + config.cronHeight + 1;
				ps.position.z = jun.headPosition.z +
					xyzArrayTwo.get(1) * (config.pollstemAngleDistanceMultiplier + (i * 0.01));

				ps.ballPosition.x = ps.position.x +
					xyzArrayTwo.get(0) * (config.junctionPollstemBallMultiplier + (i * 0.02));
				ps.ballPosition.y = ps.position.y + 0.5;
				ps.ballPosition.z = ps.position.z +
					xyzArrayTwo.get(1) * (config.junctionPollstemBallMultiplier + (i * 0.02));

				// manage different arcs for different situations:
				if (xyzArrayTwo.get(0) > 0 || xyzArrayTwo.get(1) > 0) {
					pollstemList.get(j).currentPollStemAngle = (config.pollstemAngle)
					if (!(xyzArrayTwo.get(0) < 0 && xyzArrayTwo.get(1) > 0)) {
						pollstemList.get(j).currentPollStemAngle = -(config.pollstemAngle)
					}
				} else {
					pollstemList.get(j).currentPollStemAngle = (config.pollstemAngle)
				}
			}
		} // end sub pollstem petal loop	
	}
}
