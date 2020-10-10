package org.getaviz.generator.abap.city.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.output.abap_output.acity_AFrame;
import org.getaviz.generator.output.abap_output.acity_AFrame_UI;
import org.getaviz.generator.output.abap_output.ABAP_OutputFormat;
import org.neo4j.driver.v1.types.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public class ACityAFrameExporter {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;
    private DatabaseConnector connector; // = DatabaseConnector.getInstance(config.getDefaultBoldAddress());

    private ACityRepository repository;

    private ABAP_OutputFormat aFrameOutput;

    public ACityAFrameExporter(ACityRepository aCityRepository, SettingsConfiguration config, String aFrameOutputName) {
        this.config = config;
        this.connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());

        repository = aCityRepository;

        if (aFrameOutputName.equals("acity_AFrame_UI")) {
            aFrameOutput = new acity_AFrame_UI();
            return;
        }

        aFrameOutput = new acity_AFrame();

    }

    public String createAFrameExportString(){

        StringBuilder aFrameExport = new StringBuilder();

        aFrameExport.append(aFrameOutput.head());

        aFrameExport.append(createAFrameRepositoryExport());

        aFrameExport.append(aFrameOutput.tail());

        return aFrameExport.toString();
    }

    public void exportAFrame() {
        Writer fw = null;
        try {
            File currentDir = new File(config.getOutputMap());
            String path = currentDir.getAbsolutePath() + "/model.html";
            fw = new FileWriter(path);
            fw.write(createAFrameExportString());
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            if (fw != null)
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void setAframePropToACityElements() {
        Collection<ACityElement> elements = repository.getAllElements();
        for (final ACityElement element : elements) {
            String aframeProperty = AFramePropAsJSON(element);
            element.setAframeProperty(aframeProperty);
        }
    }

    private String createAFrameRepositoryExport() {
        StringBuilder builder = new StringBuilder();

        Collection<ACityElement> floors = repository.getElementsByType(ACityElement.ACityType.Floor);
        builder.append(createElementsExport(floors));

        Collection<ACityElement> chimneys = repository.getElementsByType(ACityElement.ACityType.Chimney);
        builder.append(createElementsExport(chimneys));

        Collection<ACityElement> buildings = repository.getElementsByType(ACityElement.ACityType.Building);
        builder.append(createElementsExport(buildings));

        Collection<ACityElement> districts = repository.getElementsByType(ACityElement.ACityType.District);
        builder.append(createElementsExport(districts));

        return builder.toString();
    }

    private String createElementsExport(Collection<ACityElement> elements) {
        StringBuilder builder = new StringBuilder();
        for (ACityElement element: elements) {
            builder.append(createACityElementExport(element));
        }
        return builder.toString();
    }

    private String AFramePropAsJSON(ACityElement element) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\n");
        builder.append("\"shape\": " + "\"" + getShapeExport(element.getShape()) + "\",");
        builder.append("\n");
        builder.append("\"id\": " + "\"" + element.getHash() + "\",");
        builder.append("\n");
        builder.append("\"position\": " + "\"" + element.getXPosition() + " " + element.getYPosition() + " " + element.getZPosition() + "\",");
        builder.append("\n");
        builder.append("\"height\": " + "\"" + element.getHeight() + "\",");
        builder.append("\n");
        if(element.getShape() == ACityElement.ACityShape.Box){
            builder.append("\"width\": " + "\"" + element.getWidth() + "\",");
            builder.append("\n");
            builder.append("\"depth\": " + "\"" + element.getLength() + "\",");
            builder.append("\n");
        } else {
            builder.append("\"radius\": " + "\"" + (element.getWidth() / 2) + "\",");
            builder.append("\n");
        }

        builder.append("\"color\": " + "\"" + element.getColor() + "\",");
        builder.append("\n");
        builder.append("\"shadow\": true");
        builder.append("\n");
        builder.append("}");
        return builder.toString();
    }


    private String createACityElementExport(ACityElement element){
        StringBuilder builder = new StringBuilder();
        builder.append("<" + getShapeExport(element.getShape()) + " id=" +"\"" + element.getHash() + "\"");
        builder.append("\n");
        builder.append("\t position=\"" + element.getXPosition() + " " + element.getYPosition() + " " + element.getZPosition() + "\"");
        builder.append("\n");
        builder.append("\t height=\"" + element.getHeight() + "\"");
        builder.append("\n");

        if(element.getShape() == ACityElement.ACityShape.Box){
            builder.append("\t width=\"" + element.getWidth() + "\"");
            builder.append("\n");
            builder.append("\t depth=\"" + element.getLength() + "\"");
            builder.append("\n");
        } else {
            builder.append("\t radius=\"" + (element.getWidth() / 2) + "\"");
            builder.append("\n");
        }


        //builder.append("\t shader=\"flat\"");
        //builder.append("\n");
        //builder.append("\t flat-shading=\"true\"");
        //builder.append("\n");

        builder.append("\t color=\"" + element.getColor() + "\"");
        builder.append("\n");
        builder.append("\t shadow");
        builder.append(">");

        builder.append("\n");

        builder.append("</" + getShapeExport(element.getShape()) + ">");
        builder.append("\n");
        return builder.toString();
    }

    private String getShapeExport(ACityElement.ACityShape shape) {
        switch (shape){
            case Box: return "a-box";
            case Cylinder: return "a-cylinder";
            case Cone: return "a-cone";
        }
        return "a-sphere";
    }


}
