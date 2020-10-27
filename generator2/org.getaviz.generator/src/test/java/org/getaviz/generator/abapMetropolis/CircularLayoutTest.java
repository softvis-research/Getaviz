package org.getaviz.generator.abapMetropolis;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.metropolis.steps.MetropolisCreator;
import org.getaviz.generator.abap.metropolis.steps.MetropolisLayouter;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.abap.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;

import java.math.BigDecimal;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CircularLayoutTest {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;

    @BeforeAll
    static void setup() {

        SettingsConfiguration.getInstance("ABAPCityTest.properties");
        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.TYPEOF, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);

        aCityRepository = new ACityRepository();

        MetropolisCreator creator = new MetropolisCreator(aCityRepository, nodeRepository, config);
        creator.createRepositoryFromNodeRepository();

        MetropolisLayouter metropolisLayouter = new MetropolisLayouter(aCityRepository, nodeRepository, config);
        metropolisLayouter.layoutRepository();

        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");

        aCityRepository.writeRepositoryToNeo4j();
    }

    @AfterAll
    static void close() { connector.close(); }

    @Test
    public void districtSizeAndPosition(){

        Collection<ACityElement> districts = aCityRepository.getElementsByType(ACityElement.ACityType.District);

        for (ACityElement district: districts) {
            assertNotEquals(0.0, district.getWidth());
            assertNotEquals(0.0, district.getLength());
            assertNotEquals(0.0, district.getHeight());

            assertNotEquals(0.0, district.getXPosition());
            assertNotEquals(0.0, district.getYPosition());
            assertNotEquals(0.0, district.getZPosition());
        }
    }

    @Test
    public void circularDistrictLayout(){
        Collection<ACityElement> packageDistricts = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.type_name, "Namespace");

        long counterOriginSet = 0;
        long counterCustomCode = 0;
        long counterStandardCode = 0;

        for (ACityElement packageDistrict: packageDistricts) {

            if ( packageDistrict.getSourceNode() != null) {

                String creator = packageDistrict.getSourceNodeProperty(SAPNodeProperties.creator);
                String iterationString = packageDistrict.getSourceNodeProperty(SAPNodeProperties.iteration);
                int iteration = Integer.parseInt(iterationString);

                if (iteration == 0 && (!creator.equals("SAP"))) {
                    counterOriginSet++;
                } else if (iteration >= 1 && (!creator.equals("SAP"))) {
                    counterCustomCode++;
                } else if (iteration >= 1 && creator.equals("SAP")) {
                    counterStandardCode++;
                }
            }
        }

        assertEquals(3, counterOriginSet);
        assertEquals(4, counterCustomCode);
        assertEquals(13, counterStandardCode);
    }

    @Test
    public void specificDistrictPosition(){

    }

    @Test
    public void specificDistrictPosition2(){

        Collection<ACityElement> districts = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.object_name, "/GSA/CL_VISAP_T_TEST_CLASS");

        for (ACityElement dddd: districts) {
            assertEquals(46.5, dddd.getXPosition());
            //assertEquals(4.6000000000000005, dddd.getYPosition());
            assertEquals(10.5, dddd.getZPosition());
        }

    }

    @Test
    public void bigDecimal(){

        double yPosition = 0.887 + config.adjustACityDistrictYPosition();
        double yPositionDelta = yPosition - 0.887;

        BigDecimal numBigDecimal = BigDecimal.valueOf(yPosition);

        BigDecimal yPositionDeltaNEW = numBigDecimal.subtract(BigDecimal.valueOf(0.5));

        double yPositionAsDouble = yPositionDeltaNEW.doubleValue();

        System.out.println(yPosition);
        System.out.println(yPositionDelta);
        System.out.println(yPositionDeltaNEW);
        System.out.println(yPositionAsDouble);
        System.out.println(yPositionDeltaNEW.doubleValue());

    }
}
