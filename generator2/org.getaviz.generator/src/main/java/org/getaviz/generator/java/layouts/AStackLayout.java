package org.getaviz.generator.java.layouts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.java.enums.JavaNodeTypes;
import org.getaviz.generator.java.repository.ACityElement;

import java.util.Collection;

public class AStackLayout {
    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private ACityElement rootElement;
    private Collection<ACityElement> stackElements;


    public AStackLayout(ACityElement rootElement, Collection<ACityElement> stackElements, SettingsConfiguration config){
        this.config = config;

        this.rootElement = rootElement;
        this.stackElements = stackElements;
    }

    public void calculate(){
        stackSubElements(stackElements, calculateStackYPosition(rootElement));
    }

    private double calculateStackYPosition(ACityElement element) {
        double stackedYPosition = element.getYPosition();
        String path = element.getSourceNode().get("fqn").asString();
        String[] pathElements = path.split("\\.");

        for (int i = 0; pathElements.length > i; i++) {
            stackedYPosition += config.getACityDistrictHeight();
        }

        return stackedYPosition;
    }

    private void stackSubElements(Collection<ACityElement> elements, double stackedYPosition){
        for (ACityElement element : elements) {
            JavaNodeTypes type = element.getSourceNodeType();
            double yPosition = stackedYPosition;

            if (type == JavaNodeTypes.Method || type == JavaNodeTypes.Field) {
                yPosition += element.getYPosition() - config.adjustACityDistrictYPosition();
            }

            element.setYPosition(yPosition);
        }
    }
}
