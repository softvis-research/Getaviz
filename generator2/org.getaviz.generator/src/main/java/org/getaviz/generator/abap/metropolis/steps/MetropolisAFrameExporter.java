package org.getaviz.generator.abap.metropolis.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.output.abap_output.ABAP_OutputFormat;
import org.getaviz.generator.output.abap_output.metropolis_AFrame;

import java.util.Collection;

public class MetropolisAFrameExporter {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private ACityRepository repository;

    private ABAP_OutputFormat aFrameOutput;

    public MetropolisAFrameExporter(ACityRepository aCityRepository, SettingsConfiguration config) {
        this.config = config;

        repository = aCityRepository;

        aFrameOutput = new metropolis_AFrame();
    }

    public String createAFrameExportFile(){

        StringBuilder aFrameExport = new StringBuilder();

        aFrameExport.append(aFrameOutput.head());

        aFrameExport.append(createAFrameRepositoryExport());

        aFrameExport.append(aFrameOutput.tail());

        return aFrameExport.toString();
    }

    private String createAFrameRepositoryExport() {
        StringBuilder builder = new StringBuilder();

        Collection<ACityElement> floors = repository.getElementsByType(ACityElement.ACityType.Floor);
        builder.append(createElementsExport(floors));

        //Collection<ACityElement> chimneys = repository.getElementsByType(ACityElement.ACityType.Chimney);
        //builder.append(createElementsExport(chimneys));

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


    private String createACityElementExport(ACityElement element){
        StringBuilder builder = new StringBuilder();

        builder.append("<" + getShapeExport(element.getShape()) + " id=\"" + element.getHash() + "\"");
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
