package org.getaviz.generator;

import org.getaviz.generator.SettingsConfiguration.BuildingType;
import org.getaviz.generator.city.m2m.CityLayout;
import org.getaviz.generator.city.m2m.Rectangle;

public class OutputFormatHelper {
	static SettingsConfiguration config = SettingsConfiguration.getInstance();

	public static String X3DHead() {
		StringBuilder builder = new StringBuilder();
		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		builder.append("\n");
		builder.append(
				"<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.3//EN\" \"http://www.web3d.org/specifications/x3d-3.3.dtd\">");
		builder.append("\n");
		builder.append(
				"<X3D profile=\'Immersive\' version=\'3.3\' xmlns:xsd=\'http://www.w3.org/2001/XMLSchema-instance\' xsd:noNamespaceSchemaLocation=\'http://www.web3d.org/specifications/x3d-3.3.xsd\'>");
		builder.append("\n");
		builder.append("   <head>");
		builder.append("\n");
		builder.append("        <meta content=\'model.x3d\' name=\'title\'/>");
		builder.append("\n");
		builder.append("        <meta content=\'SVIS-Generator\' name=\'creator\'/>");
		builder.append("\n");
		builder.append("\t </head>");
		builder.append("\n");
		builder.append("\t   <ContextSetup zWriteTrans=\'false\'/>");
		builder.append("\n");
		builder.append("<Scene>");
		builder.append("\n");
		return builder.toString();
	}

	public static String settingsInfo() {
		StringBuilder builder = new StringBuilder();
		builder.append("<SettingsInfo ClassElements=\'" + config.getClassElementsMode());
		builder.append("\' SortModeCoarse=\'" + config.getClassElementsSortModeCoarse());
		builder.append("\' SortModeFine=\'" + config.getClassElementsSortModeFine());
		builder.append("\' SortModeFineReversed=\'" + config.isClassElementsSortModeFineDirectionReversed());
		builder.append("\' Scheme=\'" + config.getScheme());
		builder.append("\' ShowBuildingBase=\'" + config.isShowBuildingBase() + "\'");
		builder.append("\n");
		if (config.getBuildingType() == BuildingType.CITY_BRICKS) {
			builder.append("BrickLayout=\'" + config.getBrickLayout() + "\'");
			builder.append("\n");
		} else if (config.getBuildingType() == BuildingType.CITY_PANELS) {
			builder.append("AttributesAsCylinders=\'" + config.isShowAttributesAsCylinders());
			builder.append("\' PanelSeparatorMode=\'" + config.getPanelSeparatorMode() + "\'");
			builder.append("\n");
		}
		builder.append("/>");
		builder.append("\n");
		return builder.toString();
	}

	public static String viewports() {
		Rectangle rootEntity = CityLayout.rootRectangle;
		double width = rootEntity.getWidth();
		double length = rootEntity.getLength();
		StringBuilder builder = new StringBuilder();
		builder.append("<Group DEF=\'Viewpoints\'>");
		builder.append("\n");
		builder.append("\t <Viewpoint description=\'Initial\' position=\'" + width * 0.5 + " "
				+ ((width + length) / 2) * 0.25);
		builder.append("\' orientation=\'0 1 0 4\' centerOfRotation=\'" + width / 2 + " 0 " + length / 2 + "\'/>");
		builder.append("\n");
		builder.append("\t Viewpoint description=\'Opposite Side\' position=\'" + width * 1.5 + " "
				+ ((width + length) / 2) * 0.25 + " " + length * 1.5);
		builder.append("\' orientation=\'0 1 0 0.8\' centerOfRotation=\'" + width / 2 + " 0 " + length / 2 + "\'/>");
		builder.append("\n");
		builder.append("\t <Viewpoint description=\'Screenshot\' position=\'" + -width * 0.5 + " "
				+ ((width + length) / 2) * 0.75 + " " + -length * 0.5);
		builder.append(
				"\' orientation=\'0.1 0.95 0.25 3.8\' centerOfRotation=\'" + width / 2 + " 0 " + length / 2 + "\'/>");
		builder.append("\n");
		builder.append("\t <Viewpoint description=\'Screenshot Opposite Side\' position=\'" + width * 1.5 + " "
				+ ((width + length) / 2) * 0.75 + " " + length * 1.5);
		builder.append(
				"\' orientation=\'-0.5 0.85 0.2 0.8\' centerOfRotation=\'" + width / 2 + " 0 " + length / 2 + "\'/>");
		builder.append("\n");
		builder.append("</Group>");
		builder.append("\n");
		return builder.toString();
	}

	public static String X3DTail() {
		StringBuilder builder = new StringBuilder();
		builder.append(
				"\t<Background DEF=\"_Background\" groundColor=\'1.0000000 1.0000000 1.0000000\' skyColor=\'1.0000000 1.0000000 1.0000000\'/>");
		builder.append("\n");
		builder.append("\t</Scene>");
		builder.append("\n");
		builder.append("</X3D>");
		builder.append("\n");
		return builder.toString();
	}

	public static String AFrameHead() {
		StringBuilder builder = new StringBuilder();
		builder.append("<!DOCTYPE html>");
		builder.append("\n");
		builder.append("<html>");
		builder.append("\n");
		builder.append("\t <head>");
		builder.append("\n");
		builder.append("\t\t <meta charset=\"utf-8\">");
		builder.append("\n");
		builder.append("\t    <title>Ring</title>");
		builder.append("\n");
		builder.append("\t    <meta name=\"description\" content=\"Getaviz\">");
		builder.append("\n");
		builder.append("\t </head>");
		builder.append("\n");
		builder.append("\t <body>");
		builder.append("\n");
		builder.append("\t\t <a-scene id=\"aframe-canvas\"");
		builder.append("\n");
		builder.append("\t    \t light=\"defaultLightsEnabled: false\"");
		builder.append("\n");
		builder.append("\t    \t cursor=\"rayOrigin: mouse\"");
		builder.append("\n");
		builder.append("\t    \t embedded=\"true\"");
		builder.append("\n");
		builder.append("\t    >");
		builder.append("\n");
		builder.append("\t\t    <a-entity");
		builder.append("\n");
		builder.append("\t\t    \t id=\"camera\"");
		builder.append("\n");
		builder.append("\t\t    \t camera=\"fov: 80; zoom: 1;\"");
		builder.append("\n");
		builder.append("\t\t    \t position=\"44.0 20.0 44.0\"");
		builder.append("\n");
		builder.append("\t\t    \t");
		builder.append("rotation=\"0 -90 0\"");
		builder.append("\n");
		builder.append("\t\t    \t orbit-camera=\"");
		builder.append("\n");
		builder.append("\t\t    \t   \t target: 15.0 1.5 15.0;");
		builder.append("\n");
		builder.append("\t\t    \t   \t enableDamping: true;");
		builder.append("\n");
		builder.append("\t\t    \t   \t dampingFactor: 0.25;");
		builder.append("\n");
		builder.append("\t\t    \t   \t rotateSpeed: 0.25;");
		builder.append("\n");
		builder.append("\t\t    \t   \t panSpeed: 0.25;");
		builder.append("\n");
		builder.append("\t\t    \t   \t invertZoom: true;");
		builder.append("\n");
		builder.append("\t\t    \t   \t logPosition: false;");
		builder.append("\n");
		builder.append("\t\t    \t   \t minDistance:0;");
		builder.append("\n");
		builder.append("\t\t    \t   \t maxDistance:1000;");
		builder.append("\n");
		builder.append("\t\t    \t   \t \"");
		builder.append("\n");
		builder.append("\t\t    \t mouse-cursor=\"\"");
		builder.append("\n");
		builder.append("\t\t   \t\t >");
		builder.append("\n");
		builder.append("\t\t     </a-entity>");
		builder.append("\n");
		return builder.toString();
	}

	public static String AFrameTail() {
		StringBuilder builder = new StringBuilder();
		builder.append("\t\t </a-scene>");
		builder.append("\n");
		builder.append(" \t </body>");
		builder.append("\n");
		builder.append("</html>");
		builder.append("\n");
		return builder.toString();
	}
}
