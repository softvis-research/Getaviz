package org.getaviz.generator.rd.s2m;

public abstract class CypherCreateNode {

    public static String create(Long parent, Long visualizedNode, String label, String properties) {
        return String.format(
                "MATCH(parent),(s) WHERE ID(parent) = %d AND ID(s) = %d CREATE (parent)-[:CONTAINS]->(n:RD:%s {%s})-[:VISUALIZES]->(s)",
                parent, visualizedNode, label, properties);
    }
}
