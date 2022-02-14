package org.getaviz.generator.garbage;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.garbage.city.CityMetaphor;
import org.getaviz.generator.garbage.rd.RDMetaphor;

import java.util.List;

class MetaphorFactory {

    static Metaphor createMetaphor(SettingsConfiguration config, List<ProgrammingLanguage> languages) {
        if( config.getMetaphor() == SettingsConfiguration.Metaphor.RD) {
            return new RDMetaphor(config, languages);
        } else {
            return new CityMetaphor(config, languages);
        }
    }
}
