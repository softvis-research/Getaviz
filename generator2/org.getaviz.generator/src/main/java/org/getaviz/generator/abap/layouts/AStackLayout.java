package org.getaviz.generator.abap.layouts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.repository.ACityElement;

import java.util.Collection;

public class AStackLayout {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private ACityElement rootElement;


    public AStackLayout(ACityElement rootElement, SettingsConfiguration config){
        this.config = config;

        this.rootElement = rootElement;
    }

    public void calculate(){
        Collection<ACityElement> subElements = rootElement.getSubElements();

        stackSubElements(subElements, rootElement.getHeight());
    }

    private void stackSubElements(Collection<ACityElement> elements, double height){
        for (ACityElement element : elements) {

            //stack element
            double stackedYPosition = element.getYPosition() + height;
            element.setYPosition(stackedYPosition);

            //stack sub elements
            Collection<ACityElement> subElements = element.getSubElements();
            if (!subElements.isEmpty()) {
                double stackedHeight = element.getHeight() + height;
                stackSubElements(subElements, stackedHeight);
            }
        }
    }



}
