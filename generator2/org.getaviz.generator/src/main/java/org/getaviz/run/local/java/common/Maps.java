package org.getaviz.run.local.java.common;

import org.getaviz.generator.java.enums.JavaNodeProperties;

import java.util.*;

public class Maps {
    private static final Map<String, String> metaDataProperties;
    private static final List<String> nodesWithUsesRelationByType;
    private static final List<String> nodesWithExtendsRelationByType;
//    private static final List<String> nodesWithMigrationRelationByType;

    static {
        // Change property names for metaData-output
        metaDataProperties = new HashMap<>();
        metaDataProperties.put(JavaNodeProperties.element_id.name(), "id");
        metaDataProperties.put(JavaNodeProperties.name.name(), "name");
        metaDataProperties.put(JavaNodeProperties.type_name.name(), "type");
        metaDataProperties.put(JavaNodeProperties.effectiveLineCount.name(), "effective_line_count");

        // Elements, for which we want to show USES relation
        nodesWithUsesRelationByType = Collections.singletonList(
                "Method"
        );

        // Elements, for which we want to show INHERIT relation
        nodesWithExtendsRelationByType = Arrays.asList(
                "Class",
                "Interface"
        );

//        nodesWithMigrationRelationByType = Arrays.asList(
//                "CLAS",
//                "INTF"
//        );
    }

    public static Map<String, String> getMetaDataProperties() {
        return metaDataProperties;
    }

    public static String getMetaDataProperty(String key) {
        String propertyName = metaDataProperties.get(key);
        if (propertyName == null) {
            propertyName = key;
        }
        return propertyName;
    }

//    public static List<String> getNodesWithMigrationRelationByType() {
//        return nodesWithMigrationRelationByType;
//    }

    public static List<String> getNodesWithReferencesRelationByType() {
        return nodesWithUsesRelationByType;
    }

    public static List<String> getNodesWithExtendsRelationByType() {
        return nodesWithExtendsRelationByType;
    }
}
