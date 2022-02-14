package org.getaviz.generator.garbage.city;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.*;
import org.getaviz.generator.garbage.Metaphor;
import org.getaviz.generator.garbage.ProgrammingLanguage;
import org.getaviz.generator.garbage.Step;
import org.getaviz.generator.garbage.StepFactory;

import java.util.ArrayList;
import java.util.List;

public class CityMetaphor implements Metaphor {
    private Log log = LogFactory.getLog(this.getClass());
    private List<Step> steps = new ArrayList<>();

    public CityMetaphor(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
        StepFactory factory = new StepFactory(config, languages);
        steps.add(factory.createSteps2m());
        steps.add(factory.createMetadataFileStep());
        steps.add(factory.createStepm2m());
        steps.add(factory.createStepm2t());
    }

    public void generate() {
        try {
            steps.forEach(Step::run);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
