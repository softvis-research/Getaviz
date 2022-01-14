package org.getaviz.run.local.java;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.java.repository.ACityRepository;
import org.getaviz.generator.java.repository.SourceNodeRepository;
import org.getaviz.generator.loader.database.DatabaseConnector;

public class MetropolisStep implements IMetropolisStep {
    protected Log log = LogFactory.getLog(this.getClass());
    protected static SettingsConfiguration config = SettingsConfiguration.getInstance();
    protected static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    public static SourceNodeRepository nodeRepository;
    public static ACityRepository aCityRepository;
    public static String metropolisProperties = "generator2/org.getaviz.generator/src/test/resources/ABAPCityTest.properties";

    @Override
    public void init() {}

    /**
     * Clear DB, write updated nodes to Neo4j DB and close DB connection
     */
    public void wrapUp() {
        // Delete old ACityRepository Nodes
        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");

        // Update Neo4j with new nodes
        aCityRepository.writeRepositoryToNeo4j();

        connector.close();
    }
}
