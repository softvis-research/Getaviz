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
     * READS|WRITES sind nurnoch für TestDB da!
     * Relevant for Godclass
     */
    public void accessToForeignData() {
        connector.executeWrite("MATCH (c:Class)-[:DECLARES|READS|WRITES]->(f:Field) " +
                "    WITH f AS f, c AS c " +
                "    WHERE (f.name CONTAINS '$') " +
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
        connector.executeWrite("match (c:Class)-[:DECLARES]->(m:Method) " +
                "with sum(m.cyclomaticComplexity) as cyclo, count(m.fqn) as nom, sum(m.effectiveLineCount) as loc, c as c " +
                "set c.cyclo=cyclo, c.nom=nom, c.loc=loc ");
    }

    /**
     *  ATOD: Access To Own Data
     *  Number of attributes a method access in its own class
     *  Relevant for Feature Envy
     */
    public void accessToOwnData(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(m:Method)-[:READS|WRITES]->(f:Field), (c:Class)-[:DECLARES]->(f:Field) " +
                "with distinct m as m, f.name as attr " +
                "with m as m, count(m) as atod " +
                "set m.atod = atod ");
    }

    /**
     * ATAD: Access to All Data
     * Total number of attributes that method accesses
     * Relevant for Feature Envy
     */
    public void accesToAllData(){
        connector.executeWrite("match (m:Method)-[:READS|WRITES]->(f:Field) " +
                "with distinct m as m, f.name as attr " +
                "with m as m, count(m) as atad " +
                "set m.atad = atad ");
    }

    /**
     * NOPA: Number Of Public Attributes
     * Count all public attributes a class owns
     * Relevant for Dataclass
     */
    public void numberOfPublicAttributes(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(f:Field) " +
                "WITH c AS c, f AS f " +
                "WHERE f.visibility CONTAINS \"public\" " +
                "WITH COUNT(f.fqn) AS nopa, c AS c " +
                "SET c.nopa = nopa ");
    }

    /**
     * NOAM: Number of Accessor Methods
     * Count methods that begin either wit "get" or "set"
     * Relevant for Dataclass
     */
    public void numberOfAccessorMethods(){
        connector.executeWrite("MATCH (c:Class)-[:DECLARES]->(m:Method) " +
                "WITH c AS c, m AS m " +
                "WHERE m.name STARTS WITH \"get\" OR m.name STARTS WITH \"set\" " +
                "WITH COUNT(m.fqn) AS noam, c AS c " +
                "SET c.noam = noam ");
    }

    /**
     * FDP: Foreign Data Provider
     * Count classes in which accessed attributes are defined
     * Relevant for Feature Envy
     */
    public void foreignDataProviders(){
        connector.executeWrite("match (c1:Class)-[:DECLARES]->(f:Field), (c2:Class)-[:DECLARES]->(m:Method)-[:READS|WRITES]->(f:Field) " +
                "where (NOT c1.fqn = c2.fqn) " +
                "with distinct m as m, c1.name as foreignClass " +
                "with m as m, count(m.name) as fdp " +
                "set m.fdp = fdp ");
    }


    /**
     * NOAV: Number of Accessed Variables
     * Count the Number of Variables a Method uses
     * Relevant for Brain Method
     */
    public void numberOfAccessedVar(){
        connector.executeWrite("MATCH (m:Method)-[:DECLARES]->(v:Variable) " +
                "WITH m AS m, COUNT(v.name) AS noav " +
                "SET m.noav = noav");
    }



}