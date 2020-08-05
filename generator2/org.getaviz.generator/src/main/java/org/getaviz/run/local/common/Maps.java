package org.getaviz.run.local.common;

import java.util.Map;
import java.util.HashMap;

public class Maps {
    private static final Map<String, String> metaDataProperties;

    static{
        metaDataProperties =   new HashMap<String, String>() {{
            put("element_id", "id");
            put("object_name", "qualifiedName");
            put("type_name", "type");
            put("container_id", "belongsTo");

            put("type", "abap_type");
            put("creator", "creator");
            put("created", "created");
            put("changed_by", "changed_by");
            put("changed", "changed");
            put("iteration", "iteration");
            put("datatype", "datatype");
            put("rowtype", "rowtype");
            put("length", "length");
            put("decimals", "decimals");
            put("domname", "domname");
            put("dataelement", "dataelement");
            put("structure", "structure");
            put("modifiers", "modifiers");
            put("numberofstatements", "numberofstatements");
            put("local_class", "local_class");
            put("uses_id", "uses_id");
        }};
    }

    public static Map<String, String> getMetaDataProperties() {
        return metaDataProperties;
    }

    public static String getMetaDataProperty(String key) {
        return metaDataProperties.get(key);
    }
}
