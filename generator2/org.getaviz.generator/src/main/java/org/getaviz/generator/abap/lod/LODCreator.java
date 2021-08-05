package org.getaviz.generator.abap.lod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.repository.ACityElement;
import org.getaviz.generator.abap.repository.ACityRepository;
import org.getaviz.generator.database.DatabaseConnector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class LODCreator {

    private Log log = LogFactory.getLog(this.getClass());
    private DatabaseConnector connector  = DatabaseConnector.getInstance();
    private SettingsConfiguration config;

    private ACityRepository cityRepository;
    private LODRepository lodRepository;

    private String lodColor;
    private double lodPadding;
    private double lodOpacity;

    public LODCreator(ACityRepository aCityRepository, SettingsConfiguration config) {
        this.config = config;
        this.lodColor = config.getLODColor();
        this.lodPadding = config.getLODPadding();
        this.lodOpacity = config.getLODOpacity();

        this.cityRepository = aCityRepository;
        this.lodRepository = new LODRepository();
    }

    public void createLODElements() {
        // Replace all buildings
        /* Currently unused because buildings in ACity are already atomic
        Collection<ACityElement> buildings = cityRepository.getElementsByType(ACityElement.ACityType.Building);
        buildings.forEach(building -> createBuildingReplacement(building));
        */

        // Replace all districts
        Collection<ACityElement> allDistricts = cityRepository.getElementsByType(ACityElement.ACityType.District);

        // TODO Merge both & check for first round explicitly?
        // Districts without sub-districts
        ArrayList<ACityElement> remainingDistricts = new ArrayList<>();
        allDistricts.forEach(district -> {
            for (ACityElement subElement : district.getSubElements()) {
                // Skip if district has sub-districts
                if (subElement.getType() == ACityElement.ACityType.District) {
                    remainingDistricts.add(district);
                    return;
                }
            }
            createDistrictReplacement(district);
        });

        // Districts without unreplaced sub-districts
        while (remainingDistricts.size() > 0) {
            ArrayList<ACityElement> currentDistricts = new ArrayList<>(remainingDistricts);
            remainingDistricts.clear();
            currentDistricts.forEach(district -> {
                for (ACityElement subElement : district.getSubElements()) {
                    // Skip if district has unreplaced sub-districts
                    if (subElement.getType() == ACityElement.ACityType.District) {
                        if (lodRepository.findReplacementOf(subElement) == null) {
                            remainingDistricts.add(district);
                            return;
                        }
                    }
                }
                createDistrictReplacement(district);
            });
        }

        // Finalize all LOD elements
        lodRepository.getAllElements().forEach(lod -> {
            lod.setColor(lodColor);
            lod.setAframeProperty(getAFrameDataAsJSON(lod));
        });
    }

    public void createFileAFrame() {
        StringBuilder content = new StringBuilder();
        lodRepository.getAllElements().forEach(element -> content.append(getAFrameDataAsXML(element)));
        writeFile("/lodobjects.html", content.toString());
    }

    public void createFileMetadata() {
        StringBuilder content = new StringBuilder();
        content.append("{\"blocks\":[");
        lodRepository.getAllElements().forEach(element -> content.append(getRelationInfo(element)));
        content.setCharAt(content.length() - 1, ']');
        content.append("}");
        writeFile("/lodinfo.json", content.toString());
    }

    public void writeToNeo4j() {
        lodRepository.writeRepositoryToNeo4j();
    }

    private void createBuildingReplacement(ACityElement building) {
        // Create LOD element
        LODElement lod = new LODElement();
        lod.addReplacedElement(building);
        // Bounding Box of replaced element
        double minX = building.getXPosition() - building.getWidth() / 2;
        double maxX = building.getXPosition() + building.getWidth() / 2;
        double minY = building.getYPosition() - building.getHeight() / 2;
        double maxY = building.getYPosition() + building.getHeight() / 2;
        double minZ = building.getZPosition() - building.getLength() / 2;
        double maxZ = building.getZPosition() + building.getLength() / 2;
        // Include all sub-elements except Reference objects
        for (ACityElement element : building.getSubElements()) {
            lod.addReplacedElement(element);
            minX = Math.min(minX, element.getXPosition() - element.getWidth() / 2);
            maxX = Math.max(maxX, element.getXPosition() + element.getWidth() / 2);
            minY = Math.min(minY, element.getYPosition() - element.getHeight() / 2);
            maxY = Math.max(maxY, element.getYPosition() + element.getHeight() / 2);
            minZ = Math.min(minZ, element.getZPosition() - element.getLength() / 2);
            maxZ = Math.max(maxZ, element.getZPosition() + element.getLength() / 2);
        }
        // Apply padding
        minX -= lodPadding;
        maxX += lodPadding;
        minY -= lodPadding;
        maxY += lodPadding;
        minZ -= lodPadding;
        maxZ += lodPadding;
        // Calculate dimensions
        lod.setWidth(maxX - minX);
        lod.setHeight(maxY - minY);
        lod.setLength(maxZ - minZ);
        lod.setXPosition((minX + maxX) / 2);
        lod.setYPosition((minY + maxY) / 2);
        lod.setZPosition((minZ + maxZ) / 2);
        // Add to repo
        lodRepository.addElement(lod);
    }

    private void createDistrictReplacement(ACityElement district) {
        // Create LOD element
        LODElement lod = new LODElement();
        lod.addReplacedElement(district);
        // Bounding Box
        double minY = district.getYPosition() - district.getHeight() / 2;
        double maxY = district.getYPosition() + district.getHeight() / 2;
        // Include all sub-elements
        for (ACityElement element : district.getSubElements()) {
            if (element.getSubType() == ACityElement.ACitySubType.Cloud) continue;
            LODElement replacement = lodRepository.findReplacementOf(element);
            // If element was already replaced use that replacement
            if (replacement == null) {
                lod.addReplacedElement(element);
                minY = Math.min(minY, element.getYPosition() - element.getHeight() / 2);
                maxY = Math.max(maxY, element.getYPosition() + element.getHeight() / 2);
            } else {
                lod.addReplacedLODElement(replacement);
                minY = Math.min(minY, replacement.getYPosition() - replacement.getHeight() / 2);
                maxY = Math.max(maxY, replacement.getYPosition() + replacement.getHeight() / 2);
            }
        }
        // Calculate dimensions
        lod.setWidth(district.getWidth() + 2*lodPadding);
        lod.setHeight(maxY - minY);
        lod.setLength(district.getLength() + 2*lodPadding);
        lod.setXPosition(district.getXPosition());
        lod.setYPosition((minY + maxY) / 2);
        lod.setZPosition(district.getZPosition());
        // Add to repo
        lodRepository.addElement(lod);
    }

    private String getAFrameDataAsXML(LODElement element) {
        // TODO %f inserts unnecessary trailing zeros
        String template = "<a-box id=\"%s\" position=\"%f %f %f\" height=\"%f\" width=\"%f\" depth=\"%f\" color=\"%s\" material=\"opacity: %f\"></a-box>\n";
        return String.format(Locale.ROOT,   // Force decimal point
                template,
                element.getHash(),
                element.getXPosition(),
                element.getYPosition(),
                element.getZPosition(),
                element.getHeight(),
                element.getWidth(),
                element.getLength(),
                element.getColor(),
                lodOpacity);
    }

    private String getAFrameDataAsJSON(LODElement element) {
        return "todo";  // TODO Which properties are required in UI?
    }

    private String getRelationInfo(LODElement element) {
        StringBuilder info = new StringBuilder();
        info.append("\n{\"id\": \"")
                .append(element.getHash())
                .append("\",\n\"replaces\":[");
        element.getReplacedElements().forEach(replaced -> info.append("\n\t\"").append(replaced.getHash()).append("\","));
        element.getReplacedLODElements().forEach(replaced -> info.append("\n\t\"").append(replaced.getHash()).append("\","));
        info.setCharAt(info.length() - 1, ']');
        info.append("},");
        return info.toString();
    }

    private void writeFile(String path, String content) {
        FileWriter fw = null;
        try {
            File file = new File(config.getOutputMap() + path);
            fw = new FileWriter(file);
            fw.write(content);
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
