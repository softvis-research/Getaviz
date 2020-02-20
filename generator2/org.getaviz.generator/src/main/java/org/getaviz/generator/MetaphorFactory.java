package org.getaviz.generator;

import org.getaviz.generator.city.CityMetaphor;
import org.getaviz.generator.rd.RDMetaphor;

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
