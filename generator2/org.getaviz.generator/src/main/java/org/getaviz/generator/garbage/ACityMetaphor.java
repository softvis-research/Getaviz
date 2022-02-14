package org.getaviz.generator.garbage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.repository.ACityRepository;
import org.getaviz.generator.repository.SourceNodeRepository;

public class ACityMetaphor implements Metaphor {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;


    private SourceNodeRepository nodeRepository;
    private ACityRepository aCityRepository;

    public ACityMetaphor(SettingsConfiguration config) {
        this.config = config;
    }

    public void createCityElements(){
        log.info("createCityElements started");

        log.info("createCityElements ended");
    }


    public void generate() {
        try {
            //NodeRepository

            //ACityRepository

            //ACityCreator

            //ACityLayouter

            //ACityDesigner

        } catch (Exception e) {
            log.error(e);
        }
    }


}
