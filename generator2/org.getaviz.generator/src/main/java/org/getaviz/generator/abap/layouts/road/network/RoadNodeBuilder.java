package org.getaviz.generator.abap.layouts.road.network;

import java.util.ArrayList;
import java.util.List;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityElement.ACitySubType;

public class RoadNodeBuilder {
	
	private SettingsConfiguration config;

	public RoadNodeBuilder(SettingsConfiguration config) {
		this.config = config;
	}
	
	public List<RoadNode> calculateMarginRoadNodes(ACityElement district) {
		List<RoadNode> marginNodes = new ArrayList<RoadNode>();
		
		double rightX = district.getXPosition() + district.getWidth() / 2.0
				- config.getACityDistrictHorizontalMargin();
		
		double leftX = district.getXPosition() - district.getWidth() / 2.0 
				+ config.getACityDistrictHorizontalMargin();

		double upperY = district.getZPosition() + district.getLength() / 2.0
				- config.getACityDistrictHorizontalMargin();
		
		double lowerY = district.getZPosition() - district.getLength() / 2.0
				+ config.getACityDistrictHorizontalMargin();
		
		RoadNode upperNode = new RoadNode(district.getXPosition(), upperY);
		RoadNode rightNode = new RoadNode(rightX, district.getZPosition());
		RoadNode lowerNode = new RoadNode(district.getXPosition(), lowerY);
		RoadNode leftNode = new RoadNode(leftX, district.getZPosition());
		
		RoadNode upperLeftNode = new RoadNode(leftX, upperY);
		RoadNode upperRightNode = new RoadNode(rightX, upperY);

		RoadNode lowerLeftNode = new RoadNode(leftX, lowerY);
		RoadNode lowerRightNode = new RoadNode(rightX, lowerY);
		
		marginNodes.add(upperNode);
		marginNodes.add(rightNode);
		marginNodes.add(lowerNode);
		marginNodes.add(leftNode);

		marginNodes.add(upperLeftNode);
		marginNodes.add(upperRightNode);
		marginNodes.add(lowerLeftNode);
		marginNodes.add(lowerRightNode);

		return marginNodes;
	}

	public List<RoadNode> calculateSurroundingRoadNodes(ACityElement element) {
		List<RoadNode> surroundingNodes = new ArrayList<RoadNode>();

		surroundingNodes.addAll(this.calculateSlipRoadNodes(element));
		surroundingNodes.addAll(this.calculateCornerRoadNodes(element));

		return surroundingNodes;
	}

	public List<RoadNode> calculateSlipRoadNodes(ACityElement element) {

		// 4 nodes, each per direction
		List<RoadNode> slipNodes = new ArrayList<RoadNode>(4);

		// TODO
		// In ADistrictLightMapLayout/ADistrictCircularLayout Breite der Straßen
		// einbeziehen, dann kann die Breite auch hier berücksichtigt werden
		// Ist das perspektivisch sinnvoll?

		double rightX = element.getXPosition() + element.getWidth() / 2.0
				+ config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		
		double leftX = element.getXPosition() - element.getWidth() / 2.0 
				- config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;

		double upperY = element.getZPosition() + element.getLength() / 2.0
				+ config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		
		double lowerY = element.getZPosition() - element.getLength() / 2.0
				- config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;

		RoadNode upperNode = new RoadNode(element.getXPosition(), upperY);
		RoadNode rightNode = new RoadNode(rightX, element.getZPosition());
		RoadNode lowerNode = new RoadNode(element.getXPosition(), lowerY);
		RoadNode leftNode = new RoadNode(leftX, element.getZPosition());

		slipNodes.add(upperNode);
		slipNodes.add(rightNode);
		slipNodes.add(lowerNode);
		slipNodes.add(leftNode);

		return slipNodes;
	}

	public List<RoadNode> calculateCornerRoadNodes(ACityElement element) {

		// 4 nodes, each per corner of area
		List<RoadNode> cornerNodes = new ArrayList<RoadNode>(4);

		// TODO
		// In ADistrictLightMapLayout/ADistrictCircularLayout Breite der Straßen
		// einbeziehen, dann kann die Breite auch hier berücksichtigt werden
		// Ist das perspektivisch sinnvoll?

		double rightX = element.getXPosition() + element.getWidth() / 2.0
				+ config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		
		double leftX = element.getXPosition() - element.getWidth() / 2.0 
				- config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;

		double upperY = element.getZPosition() + element.getLength() / 2.0
				+ config.getACityDistrictHorizontalGap() / 2.0; // + config.getMetropolisRoadWidth() / 2.0;
		
		double lowerY = element.getZPosition() - element.getLength() / 2.0
				- config.getACityDistrictHorizontalGap() / 2.0; // - config.getMetropolisRoadWidth() / 2.0;

		RoadNode upperLeftNode = new RoadNode(leftX, upperY);
		RoadNode upperRightNode = new RoadNode(rightX, upperY);

		RoadNode lowerLeftNode = new RoadNode(leftX, lowerY);
		RoadNode lowerRightNode = new RoadNode(rightX, lowerY);

		cornerNodes.add(upperLeftNode);
		cornerNodes.add(upperRightNode);
		cornerNodes.add(lowerLeftNode);
		cornerNodes.add(lowerRightNode);

		return cornerNodes;
	}
	
	public RoadNode calculateDistrictSlipRoadNode(ACityElement district, RoadNode slipNode) {
		double x, y;
		ACitySubType roadType;
		
		if (district.getSourceNodeType() == SAPNodeTypes.Namespace) {
			roadType = ACitySubType.Freeway;
		} else {
			roadType = ACitySubType.Street;
		}
		
		
		if (district.getXPosition() == slipNode.getX()) {
			x = district.getXPosition();
			y = district.getZPosition() + Math.signum(slipNode.getY() - district.getZPosition()) * (district.getLength() / 2.0 + config.getMetropolisRoadWidth(roadType) / 2.0);
		} else {
			x = district.getXPosition() + Math.signum(slipNode.getX() - district.getXPosition()) * (district.getWidth() / 2.0 + config.getMetropolisRoadWidth(roadType) / 2.0);
			y = district.getZPosition();
		}
		
		return new RoadNode(x, y);
	}
	
	public RoadNode calculateDistrictMarginRoadNode(ACityElement district, RoadNode slipNode) {
		double x, y;		
		
		if (district.getXPosition() == slipNode.getX()) {
			x = district.getXPosition();
			y = district.getZPosition() + Math.signum(slipNode.getY() - district.getZPosition()) * (district.getLength() / 2.0 - config.getACityDistrictHorizontalMargin());
		} else {
			x = district.getXPosition() + Math.signum(slipNode.getX() - district.getXPosition()) * (district.getWidth() / 2.0 - config.getACityDistrictHorizontalMargin());
			y = district.getZPosition();
		}
		
		return new RoadNode(x, y);
	}

}
