package org.getaviz.generator;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.getaviz.generator.SettingsConfiguration.BuildingType;
import org.getaviz.generator.city.m2m.CityLayout;
import org.getaviz.generator.city.m2m.Rectangle;

public class OutputFormatHelper {
	static SettingsConfiguration config = SettingsConfiguration.getInstance();

	public static String X3DHead() {
		StringConcatenation builder = new StringConcatenation();
		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		builder.newLine();
		builder.append(
				"<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.3//EN\" \"http://www.web3d.org/specifications/x3d-3.3.dtd\">");
		builder.newLine();
		builder.append(
				"<X3D profile=\'Immersive\' version=\'3.3\' xmlns:xsd=\'http://www.w3.org/2001/XMLSchema-instance\' xsd:noNamespaceSchemaLocation=\'http://www.web3d.org/specifications/x3d-3.3.xsd\'>");
		builder.newLine();
		builder.append("   <head>");
		builder.newLine();
		builder.append("        <meta content=\'model.x3d\' name=\'title\'/>");
		builder.newLine();
		builder.append("        <meta content=\'SVIS-Generator\' name=\'creator\'/>");
		builder.newLine();
		builder.append("\t </head>");
		builder.newLine();
		builder.append("\t   <ContextSetup zWriteTrans=\'false\'/>");
		builder.newLine();
		builder.append("<Scene>");
		builder.newLine();
		return builder.toString();
	}

	public static String settingsInfo() {
		StringConcatenation builder = new StringConcatenation();
		builder.append("<SettingsInfo ClassElements=\'" + config.getClassElementsMode());
		builder.append("\' SortModeCoarse=\'" + config.getClassElementsSortModeCoarse());
		builder.append("\' SortModeFine=\'" + config.getClassElementsSortModeFine());
		builder.append("\' SortModeFineReversed=\'" + config.isClassElementsSortModeFineDirectionReversed());
		builder.append("\' Scheme=\'" + config.getScheme());
		builder.append("\' ShowBuildingBase=\'" + config.isShowBuildingBase() + "\'");
		builder.newLine();
		if (config.getBuildingType() == BuildingType.CITY_BRICKS) {
			builder.append("BrickLayout=\'" + config.getBrickLayout() + "\'");
			builder.newLine();
		} else if (config.getBuildingType() == BuildingType.CITY_PANELS) {
			builder.append("AttributesAsCylinders=\'" + config.isShowAttributesAsCylinders());
			builder.append("\' PanelSeparatorMode=\'" + config.getPanelSeparatorMode() + "\'");
			builder.newLineIfNotEmpty();
		}
		builder.append("/>");
		builder.newLine();
		return builder.toString();
	}

	public static String viewports() {
		Rectangle rootEntity = CityLayout.rootRectangle;
		double width = rootEntity.getWidth();
		double length = rootEntity.getLength();
		StringConcatenation builder = new StringConcatenation();
		builder.append("<Group DEF=\'Viewpoints\'>");
		builder.newLine();
		builder.append("\t <Viewpoint description=\'Initial\' position=\'" + width * 0.5 + " "
				+ ((width + length) / 2) * 0.25);
		builder.append("\' orientation=\'0 1 0 4\' centerOfRotation=\'" + width / 2 + " 0 " + length / 2 + "\'/>");
		builder.newLine();
		builder.append("\t Viewpoint description=\'Opposite Side\' position=\'" + width * 1.5 + " "
				+ ((width + length) / 2) * 0.25 + " " + length * 1.5);
		builder.append("\' orientation=\'0 1 0 0.8\' centerOfRotation=\'" + width / 2 + " 0 " + length / 2 + "\'/>");
		builder.newLine();
		builder.append("\t <Viewpoint description=\'Screenshot\' position=\'" + -width * 0.5 + " "
				+ ((width + length) / 2) * 0.75 + " " + -length * 0.5);
		builder.append(
				"\' orientation=\'0.1 0.95 0.25 3.8\' centerOfRotation=\'" + width / 2 + " 0 " + length / 2 + "\'/>");
		builder.newLine();
		builder.append("\t <Viewpoint description=\'Screenshot Opposite Side\' position=\'" + width * 1.5 + " "
				+ ((width + length) / 2) * 0.75 + " " + length * 1.5);
		builder.append(
				"\' orientation=\'-0.5 0.85 0.2 0.8\' centerOfRotation=\'" + width / 2 + " 0 " + length / 2 + "\'/>");
		builder.newLine();
		builder.append("</Group>");
		builder.newLine();
		return builder.toString();
	}

	public static String X3DTail() {
		StringConcatenation builder = new StringConcatenation();
		builder.append(
				"\t<Background DEF=\"_Background\" groundColor=\'1.0000000 1.0000000 1.0000000\' skyColor=\'1.0000000 1.0000000 1.0000000\'/>");
		builder.newLine();
		builder.append("\t</Scene>");
		builder.newLine();
		builder.append("</X3D>");
		builder.newLine();
		return builder.toString();
	}

	public static String AFrameHead() {
		StringConcatenation builder = new StringConcatenation();
		builder.append("<!DOCTYPE html>");
		builder.newLine();
		builder.append("<html>");
		builder.newLine();
		builder.append("\t <head>");
		builder.newLine();
		builder.append("\t\t <meta charset=\"utf-8\">");
		builder.newLine();
		builder.append("\t    <title>Ring</title>");
		builder.newLine();
		builder.append("\t    <meta name=\"description\" content=\"Getaviz\">");
		builder.newLine();
		builder.append("\t </head>");
		builder.newLine();
		builder.append("\t <body>");
		builder.newLine();
		builder.append("\t\t <a-scene id=\"aframe-canvas\"");
		builder.newLine();
		builder.append("\t    \t light=\"defaultLightsEnabled: false\"");
		builder.newLine();
		builder.append("\t    \t cursor=\"rayOrigin: mouse\"");
		builder.newLine();
		builder.append("\t    \t embedded=\"true\"");
		builder.newLine();
		builder.append("\t    >");
		builder.newLine();
		builder.append("\t\t    <a-entity");
		builder.newLine();
		builder.append("\t\t    \t id=\"camera\"");
		builder.newLine();
		builder.append("\t\t    \t camera=\"fov: 80; zoom: 1;\"");
		builder.newLine();
		builder.append("\t\t    \t position=\"44.0 20.0 44.0\"");
		builder.newLine();
		builder.append("\t\t    \t");
		builder.append("rotation=\"0 -90 0\"");
		builder.newLine();
		builder.append("\t\t    \t orbit-camera=\"");
		builder.newLine();
		builder.append("\t\t    \t   \t target: 15.0 1.5 15.0;");
		builder.newLine();
		builder.append("\t\t    \t   \t enableDamping: true;");
		builder.newLine();
		builder.append("\t\t    \t   \t dampingFactor: 0.25;");
		builder.newLine();
		builder.append("\t\t    \t   \t rotateSpeed: 0.25;");
		builder.newLine();
		builder.append("\t\t    \t   \t panSpeed: 0.25;");
		builder.newLine();
		builder.append("\t\t    \t   \t invertZoom: true;");
		builder.newLine();
		builder.append("\t\t    \t   \t logPosition: false;");
		builder.newLine();
		builder.append("\t\t    \t   \t minDistance:0;");
		builder.newLine();
		builder.append("\t\t    \t   \t maxDistance:1000;");
		builder.newLine();
		builder.append("\t\t    \t   \t \"");
		builder.newLine();
		builder.append("\t\t    \t mouse-cursor=\"\"");
		builder.newLine();
		builder.append("\t\t   \t\t >");
		builder.newLine();
		builder.append("\t\t     </a-entity>");
		builder.newLine();
		return builder.toString();
	}

	public static String AFrameTail() {
		StringConcatenation builder = new StringConcatenation();
		builder.append("\t\t </a-scene>");
		builder.newLine();
		builder.append(" \t </body>");
		builder.newLine();
		builder.append("</html>");
		builder.newLine();
		return builder.toString();
	}
}
