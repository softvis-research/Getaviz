package org.getaviz.generator;

import org.getaviz.generator.city.m2m.City2City;
import org.getaviz.generator.city.m2t.City2AFrame;
import org.getaviz.generator.city.m2t.City2X3D;
import org.getaviz.generator.city.s2m.JQA2City;
import org.getaviz.generator.jqa.Java2JQA;
import org.getaviz.generator.jqa.ScanStep;
import org.getaviz.generator.rd.m2m.RD2RD;
import org.getaviz.generator.rd.m2t.RD2AFrame;
import org.getaviz.generator.rd.m2t.RD2X3D;
import org.getaviz.generator.rd.s2m.JQA2RD;

public class StepFactory {

    private SettingsConfiguration.OutputFormat outputFormat;
    private SettingsConfiguration.Metaphor metaphor;
    private SettingsConfiguration config;

    public StepFactory(SettingsConfiguration config) {
        this.outputFormat = config.getOutputFormat();
        this.metaphor = config.getMetaphor();
        this.config = config;
    }

    public Step createEnhancementStep() {
        return new Java2JQA();
    }

    public Step createScanStep() {
        return new ScanStep(config);
    }

    public Step createSteps2m() {
        if(metaphor == SettingsConfiguration.Metaphor.RD) {
            return new JQA2RD(config);
        } else {
            return new JQA2City(config);
        }
    }

    public Step createStepm2m() {
        if(metaphor == SettingsConfiguration.Metaphor.RD) {
            return new RD2RD(config);
        } else {
            return new City2City(config);
        }
    }

    public Step createStepm2t() {
        if(outputFormat == SettingsConfiguration.OutputFormat.AFrame) {
            if(metaphor == SettingsConfiguration.Metaphor.RD) {
                return new RD2AFrame(config);
            } else {
                return new City2AFrame(config);
            }
        } else {
            if(metaphor == SettingsConfiguration.Metaphor.RD) {
                return new RD2X3D(config);
            } else {
                return new City2X3D(config);
            }
        }
    }
}
