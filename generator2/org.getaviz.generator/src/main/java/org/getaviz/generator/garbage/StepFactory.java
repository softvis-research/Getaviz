package org.getaviz.generator.garbage;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.garbage.city.m2m.City2City;
import org.getaviz.generator.garbage.city.m2t.City2AFrame;
import org.getaviz.generator.garbage.city.m2t.City2X3D;
import org.getaviz.generator.garbage.city.s2m.JQA2City;
import org.getaviz.generator.garbage.jqa.C2JSON;
import org.getaviz.generator.garbage.jqa.JQA2JSON;
import org.getaviz.generator.garbage.rd.m2m.RD2RD;
import org.getaviz.generator.garbage.rd.m2t.RD2AFrame;
import org.getaviz.generator.garbage.rd.m2t.RD2X3D;
import org.getaviz.generator.garbage.rd.s2m.C2RD;
import org.getaviz.generator.garbage.rd.s2m.JQA2RD;

import java.util.List;

public class StepFactory {

    private SettingsConfiguration.OutputFormat outputFormat;
    private SettingsConfiguration.Metaphor metaphor;
    private SettingsConfiguration config;
    private List<ProgrammingLanguage> languages;

    public StepFactory(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
        this.outputFormat = config.getOutputFormat();
        this.metaphor = config.getMetaphor();
        this.config = config;
        this.languages = languages;
    }

    public Step createMetadataFileStep() {
        if(languages.contains(ProgrammingLanguage.JAVA)) {
            return new JQA2JSON(config, languages);
        } else {
            return new C2JSON(config, languages);
        }
    }

    public Step createSteps2m() {
        if(metaphor == SettingsConfiguration.Metaphor.RD) {
            if(languages.contains(ProgrammingLanguage.JAVA)) {
                return new JQA2RD(config, languages);
            } else {
                return new C2RD(config, languages);
            }
        } else {
            return new JQA2City(config, languages);
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
