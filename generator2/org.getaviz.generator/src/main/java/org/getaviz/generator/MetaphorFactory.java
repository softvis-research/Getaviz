package org.getaviz.generator;

import org.getaviz.generator.city.CityMetaphor;
import org.getaviz.generator.rd.RDMetaphor;

class MetaphorFactory {

    static Metaphor createMetaphor(SettingsConfiguration config) {
        if( config.getMetaphor() == SettingsConfiguration.Metaphor.RD) {
            return new RDMetaphor(config);
        } else {
            return new CityMetaphor(config);
        }
    }
}
