package org.getaviz.generator.abapMetropolis;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.enums.SAPNodeProperties;
import org.getaviz.generator.abap.enums.SAPNodeTypes;
import org.getaviz.generator.abap.enums.SAPRelationLabels;
import org.getaviz.generator.abap.metropolis.steps.MetropolisCreator;
import org.getaviz.generator.abap.metropolis.steps.MetropolisDesigner;
import org.getaviz.generator.repository.ACityElement;
import org.getaviz.generator.repository.ACityRepository;
import org.getaviz.generator.repository.SourceNodeRepository;
import org.getaviz.generator.database.DatabaseConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MetropolisDesignerTest {

    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());
    private static SourceNodeRepository nodeRepository;
    private static ACityRepository aCityRepository;

    @BeforeAll
    static void setup() {
        SettingsConfiguration.getInstance("Generator.properties");
        nodeRepository = new SourceNodeRepository();
        nodeRepository.loadNodesByPropertyValue(SAPNodeProperties.type_name, SAPNodeTypes.Namespace.name());
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.TYPEOF, true);
        nodeRepository.loadNodesByRelation(SAPRelationLabels.CONTAINS, true);

        aCityRepository = new ACityRepository();

        MetropolisCreator creator = new MetropolisCreator(aCityRepository, nodeRepository, config);
        creator.createRepositoryFromNodeRepository();

        MetropolisDesigner designer = new MetropolisDesigner(aCityRepository, nodeRepository, config);
        designer.designRepository();

        connector.executeWrite("MATCH (n:ACityRep) DETACH DELETE n;");

        aCityRepository.writeRepositoryToNeo4j();
    }

    @AfterAll
    static void close() { connector.close(); }

    @Test
    public void districtDesign(){
        Collection<ACityElement> classDistricts = aCityRepository.getElementsByTypeAndSourceProperty(ACityElement.ACityType.District, SAPNodeProperties.type_name, "Class");
        assertNotEquals(0, classDistricts.size());

        for( ACityElement district : classDistricts){
            assertEquals("#F4D03F", district.getColor());
            assertEquals(ACityElement.ACityShape.Box, district.getShape());
        }
    }


    @Test
    public void buildingDesign(){
        Collection<ACityElement> classBuildings =
                aCityRepository.getElementsByTypeAndSourceProperty(
                        ACityElement.ACityType.Building, SAPNodeProperties.type_name, "Method");

        assertNotEquals(0, classBuildings.size());

        for( ACityElement building : classBuildings) {
            assertEquals("#ffffff", building.getColor());
            assertEquals(ACityElement.ACityShape.Box, building.getShape());
        }
    }

    @Test
    public void seaReferenceBuildingDesign(){
        Collection<ACityElement> refBuildings = aCityRepository.getElementsByRefBuildingType(ACityElement.ACitySubType.Sea);
        assertNotEquals(0, refBuildings.size());

        for( ACityElement refBuilding : refBuildings) {
                assertEquals(ACityElement.ACityShape.Circle, refBuilding.getShape());
                assertEquals("-90 0 0", refBuilding.getRotation());
                assertEquals("#sea", refBuilding.getTextureSource());

        }
    }

    @Test
    public void mountainReferenceBuildingDesign(){
        Collection<ACityElement> refBuildings = aCityRepository.getElementsByRefBuildingType(ACityElement.ACitySubType.Mountain);
        assertNotEquals(0, refBuildings.size());

        for( ACityElement refBuilding : refBuildings) {
            assertEquals(ACityElement.ACityShape.Entity, refBuilding.getShape());
            assertEquals("0.05 0.05 0.05", refBuilding.getModelScale());
            assertEquals("#mountain", refBuilding.getModel());


        }
    }


}
