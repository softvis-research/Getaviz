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
    private Collection<ACityElement> stackElements;


    public AStackLayout(ACityElement rootElement, Collection<ACityElement> stackElements, SettingsConfiguration config){
        this.config = config;

        this.rootElement = rootElement;
        this.stackElements = stackElements;
    }

    public void calculate(){
        stackSubElements(stackElements, rootElement.getHeight());
    }

    private void stackSubElements(Collection<ACityElement> elements, double parentHeight){
        for (ACityElement element : elements) {

            //stack element
            double stackedYPosition = element.getYPosition() + parentHeight;
            element.setYPosition(stackedYPosition);

            //stack sub elements
            Collection<ACityElement> subElements = element.getSubElements();
            if (!subElements.isEmpty()) {
                stackSubElements(subElements, parentHeight);
            }
        }
    }



}
