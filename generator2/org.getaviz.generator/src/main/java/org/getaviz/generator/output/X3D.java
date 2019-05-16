package org.getaviz.generator.output;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.city.m2m.CityLayout;
import org.getaviz.generator.city.m2m.Rectangle;
import org.getaviz.generator.SettingsConfiguration.BuildingType;

import java.awt.*;

public class X3D implements OutputFormat {
    private SettingsConfiguration.ClassElementsModes classElementMode;
    private SettingsConfiguration.ClassElementsSortModesCoarse classElementsSortModesCoarse;
    private SettingsConfiguration.ClassElementsSortModesFine classElementsSortModesFine;
    private boolean showBuildingBase;
    private boolean classElementsSortModeFineDirectionReversed;
    private SettingsConfiguration.BuildingType buildingType;
    private boolean showAttributesAsCylinders;
    private SettingsConfiguration.Panels.SeparatorModes separatorModes;
    private SettingsConfiguration.Bricks.Layout bricksLayout;
    private SettingsConfiguration.Schemes scheme;

    public X3D(SettingsConfiguration config) {
        this.classElementMode = config.getClassElementsMode();
        this.classElementsSortModesCoarse = config.getClassElementsSortModeCoarse();
        this.classElementsSortModesFine = config.getClassElementsSortModeFine();
        this.showBuildingBase = config.isShowBuildingBase();
        this.classElementsSortModeFineDirectionReversed = config.isClassElementsSortModeFineDirectionReversed();
        this.buildingType = config.getBuildingType();
        this.showAttributesAsCylinders = config.isShowAttributesAsCylinders();
        this.separatorModes = config.getPanelSeparatorMode();
        this.bricksLayout = config.getBrickLayout();
        this.scheme = config.getScheme();
    }

    public String head() {
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

    public String printColor(String hexColor) {
        Color color = Color.decode(hexColor);
        return color.getRed()/255f + " " + color.getGreen()/255f + " " + color.getBlue()/255f;
    }

    public String settingsInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("<SettingsInfo ClassElements=\'" + classElementMode);
        builder.append("\' SortModeCoarse=\'" + classElementsSortModesCoarse);
        builder.append("\' SortModeFine=\'" + classElementsSortModesFine);
        builder.append("\' SortModeFineReversed=\'" + classElementsSortModeFineDirectionReversed);
        builder.append("\' Scheme=\'" + scheme);
        builder.append("\' ShowBuildingBase=\'" + showBuildingBase + "\'");
        builder.append("\n");
        if (buildingType == BuildingType.CITY_BRICKS) {
            builder.append("BrickLayout=\'" + bricksLayout + "\'");
            builder.append("\n");
        } else if (buildingType == BuildingType.CITY_PANELS) {
            builder.append("AttributesAsCylinders=\'" + showAttributesAsCylinders);
            builder.append("\' PanelSeparatorMode=\'" + separatorModes + "\'");
            builder.append("\n");
        }
        builder.append("/>");
        builder.append("\n");
        return builder.toString();
    }

    public String viewports() {
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

    public String tail() {
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
}
