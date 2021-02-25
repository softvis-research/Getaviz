package org.getaviz.generator.abap.common.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.getaviz.generator.output.abap_output.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public class AFrameExporter {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;
    private DatabaseConnector connector; // = DatabaseConnector.getInstance(config.getDefaultBoldAddress());

    private ACityRepository repository;

    private ABAP_OutputFormat aFrameOutput;

    public AFrameExporter(ACityRepository aCityRepository, SettingsConfiguration config, String aFrameOutputName) {
        this.config = config;
        this.connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());

        repository = aCityRepository;

        switch(aFrameOutputName){
            case "acity_AFrame":  aFrameOutput = new acity_AFrame(); break;
            case "metropolis_AFrame":  aFrameOutput = new metropolis_AFrame(config); break;
            case "metropolis_AFrame_UI":  aFrameOutput = new metropolis_AFrame_UI(config); break;
            case "acity_AFrame_UI": aFrameOutput = new acity_AFrame_UI(); break;
        }
    }

    public String createAFrameExportString(){

        StringBuilder aFrameExport = new StringBuilder();

        aFrameExport.append(aFrameOutput.head());

        aFrameExport.append(createAFrameCamera());

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

    private String createAFrameCamera() {
        // where we put the camera depends on the size and position of the city model
        double maxX = 0, maxZ = 0, minX = 0, minZ = 0;
        for (ACityElement element : repository.getAllElements()) {
            maxX = Math.max(maxX, element.getXPosition());
            maxZ = Math.max(maxZ, element.getZPosition());
            minX = Math.min(minX, element.getXPosition());
            minZ = Math.min(minZ, element.getZPosition());
        }
        double maxSideLength = Math.max(maxX - minX, maxZ - minZ);

        // these numbers are based on what looks good for a 100x100 city, scaled to match the actual proportions
        double cameraX = minX - (maxSideLength * 0.05);
        double cameraY = (maxSideLength * 0.35);
        double cameraZ = minZ - (maxSideLength * 0.05);
        // the point the camera will be looking at
        double targetX = minX + (maxSideLength * 0.2);
        double targetY = 0;
        double targetZ = minZ + (maxSideLength * 0.2);

        return "\t\t\t <a-entity id=\"camera\" camera=\"fov: 80; zoom: 1;\"\n" +
                "\t\t    \t position=\"" + cameraX + " " + cameraY + " " + cameraZ + "\"\n" +
                "\t\t    \t rotation=\"0 -90 0\"\n" +
                "\t\t    \t orbit-camera=\"\n" +
                "\t\t    \t   \t target: " + targetX + " " + targetY + " " + targetZ + ";\n" +
                "\t\t    \t   \t enableDamping: true;\n" +
                "\t\t    \t   \t dampingFactor: 0.25;\n" +
                "\t\t    \t   \t rotateSpeed: 0.25;\n" +
                "\t\t    \t   \t panSpeed: 0.25;\n" +
                "\t\t    \t   \t invertZoom: true;\n" +
                "\t\t    \t   \t logPosition: false;\n" +
                "\t\t    \t   \t minDistance:0;\n" +
                "\t\t    \t   \t maxDistance:1000;\n" +
                "\t\t    \t   \t \"\n" +
                "\t\t    \t mouse-cursor=\"\"\n" +
                "\t\t   \t\t >" +
                "\n" +
                "\t\t\t </a-entity>\n";
    }

    private String createAFrameRepositoryExport() {
        StringBuilder builder = new StringBuilder();

        Collection<ACityElement> floors = repository.getElementsByType(ACityElement.ACityType.Floor);
        builder.append(createElementsExport(floors));

        if(aFrameOutput.equals("acity_AFrame")) {
            Collection<ACityElement> chimneys = repository.getElementsByType(ACityElement.ACityType.Chimney);
            builder.append(createElementsExport(chimneys));
        }

        Collection<ACityElement> buildings = repository.getElementsByType(ACityElement.ACityType.Building);
        builder.append(createElementsExport(buildings));

        Collection<ACityElement> references = repository.getElementsByType(ACityElement.ACityType.Reference);
        builder.append(createElementsExport(references));

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
        if (element.getTextureSource() != null) {
            builder.append("\"src\": " + "\"" + element.getTextureSource() + "\",");
            builder.append("\n");
        }
        if (element.getRotation() != null) {
            builder.append("\"rotation\": " + "\"" + element.getRotation() + "\",");
            builder.append("\n");
        }
        if (element.getModel() != null) {
            builder.append("\"gltf-model\": " + "\"" + element.getModel() + "\",");
            builder.append("\n");
        }
        if (element.getModelScale() != null) {
            builder.append("\"scale\": " + "\"" + element.getModelScale() + "\",");
            builder.append("\n");
        }
        builder.append("\"shadow\": true");
        builder.append("\n");
        builder.append("}");
        return builder.toString();
    }


    private String createACityElementExport(ACityElement element){
        StringBuilder builder = new StringBuilder();

        builder.append("<" + getShapeExport(element.getShape()) + " id=\"" + element.getHash() + "\"");
        builder.append("\n");
        builder.append("\t position=\"" + element.getXPosition() + " " + element.getYPosition() + " " + element.getZPosition() + "\"");
        builder.append("\n");
        builder.append("\t height=\"" + element.getHeight() + "\"");
        builder.append("\n");

        if(element.getShape() == ACityElement.ACityShape.Box || element.getShape() == ACityElement.ACityShape.Entity){
            builder.append("\t width=\"" + element.getWidth() + "\"");
            builder.append("\n");
            builder.append("\t depth=\"" + element.getLength() + "\"");
            builder.append("\n");
        } else {
            builder.append("\t radius=\"" + (element.getWidth() / 2) + "\"");
            builder.append("\n");
        }

        builder.append("\t color=\"" + element.getColor() + "\"");
        builder.append("\n");

        if (element.getTextureSource() != null){
            builder.append("\t src=\"" + element.getTextureSource() + "\"");
            builder.append("\n");
        }
        if (element.getRotation() != null){
            builder.append("\t rotation=\"" + element.getRotation() + "\"");
            builder.append("\n");
        }
        if(element.getModel() != null){
            builder.append("\t scale=\"" + element.getModelScale() + "\"");
            builder.append("\n");
            builder.append("\t gltf-model=\"" + element.getModel() + "\"");
            builder.append("\n");
        }

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
            case Ring: return "a-ring";
            case Plane: return "a-plane";
            case Circle: return "a-circle";
            case Sphere: return "a-sphere";
            case Entity: return "a-entity";
        }
        return "a-sphere";
    }


}
