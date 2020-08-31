package org.getaviz.generator.analysis;

import org.getaviz.generator.database.DatabaseConnector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MetricQueries {

    private DatabaseConnector connector = DatabaseConnector.getInstance();


    /**
     * MAA: Methods Accessing Attributes
     * Count Method pairs that access the same attribute
     * Relevant for Godclass, Brainclass
     */
    public void countMethodsAccessingAttributes() {
        connector.executeWrite("match (c:Class)-[:DECLARES]->(m:Method)-[:WRITES|READS]->(f:Field) " +
                "    where (not m.name contains '$') " +
                "    with c as c, m.name as m, COUNT(f.name) as maa " +
                "    where maa >= 2 " +
                "    with c as c, count(m) as result " +
                "    set c.maa=result");
    }

    /**
     * ATFD: Access To Foreign Data
     * Number of Attributes a Class accesses that do not belong to it
     * Relevant for Godclass
     */
    public void accessToForeignData() {
        connector.executeWrite("MATCH (c:Class)-[:DECLARES|READS|WRITES]->(f:Field) " +
                "    WITH f AS f, c AS c " +
                "    WHERE (f.name CONTAINS '$') AND (NOT f.name CONTAINS \"$class$java\") " +
                "    WITH c AS c, COUNT(f.name) AS atfd " +
                "    SET c.atfd=atfd");
    }

    /**
     * CYCLO: Cyclomatic Complexity
     * NOM: Number Of Methods
     * LOC: Lines Of Code
     * Count NOM, add up CYCLO and LOC from Methods for each Class
     * Relevant for Godclass, Dataclass, Brainclass
     */
    public void complexityAndNumbOfMethods()  {
        connector.executeWrite("match (c:Class)-[:DECLARES]->(m:Method)\n" +
                "with sum(m.cyclomaticComplexity) as cyclo, count(m.fqn) as nom, sum(m.effectiveLineCount) as loc, c as c \n" +
                "set c.cyclo=cyclo, c.nom=nom, c.loc=loc");
    }

    /**
     *  ATOD: Access To Own Data
     *  Number of attributes a method access in its own class
     *  Relevant for Feature Envy
     */
    public void accessToOwnData(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(m:Method)-[:READS|WRITES]->(f:Field)\n" +
                "WITH c AS c, m AS m, f AS f\n" +
                "WHERE (NOT f.name CONTAINS '$') AND (NOT f.name CONTAINS \"$class$java\")\n" +
                "WITH m AS m, count(f.fqn) AS atod\n" +
                "set m.atod = atod");
    }

    /**
     * ATAD: Access to All Data
     * Total number of attributes that method accesses
     * Relevant for Feature Envy
     */
    public void accesToAllData(){
        connector.executeWrite("MATCH (m:Method)-[:DECLARES|READS|WRITES]->(f:Field)\n" +
                "WITH m AS m, count(f.fqn) AS atad\n" +
                "SET m.atad = atad");
    }

    /**
     * NOPA: Number Of Public Attributes
     * Count all public attributes a class owns
     * Relevant for Dataclass
     */
    public void numberOfPublicAttributes(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(f:Field)\n" +
                "WITH c AS c, f AS f\n" +
                "WHERE f.visibility CONTAINS \"public\"\n" +
                "WITH COUNT(f.fqn) AS nopa, c AS c\n" +
                "SET c.nopa = nopa");
    }

    /**
     * NOAM: Number of Accessor Methods
     * Count methods that begin either wit "get" or "set"
     * Relevant for Dataclass
     */
    public void numberOfAccessorMethods(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(m:Method)\n" +
                "WITH c AS c, m AS m\n" +
                "WHERE m.name STARTS WITH \"get\" OR m.name STARTS WITH \"set\"\n" +
                "WITH COUNT(m.fqn) AS noam, c AS c\n" +
                "SET c.noam = noam");
    }




}