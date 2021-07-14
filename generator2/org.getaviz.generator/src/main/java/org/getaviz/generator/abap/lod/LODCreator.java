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

public class LODCreator {

    private Log log = LogFactory.getLog(this.getClass());
    private DatabaseConnector connector  = DatabaseConnector.getInstance();
    private SettingsConfiguration config;

    private ACityRepository cityRepository;
    private LODRepository lodRepository;

    private double padding = 0.2;   // TODO use config

    public LODCreator(ACityRepository aCityRepository, SettingsConfiguration config) {
        this.config = config;

        this.cityRepository = aCityRepository;
        this.lodRepository = new LODRepository();
    }

    public void createLODElements() {
        // Replace all buildings + sub-elements
        Collection<ACityElement> buildings = cityRepository.getElementsByType(ACityElement.ACityType.Building);
        buildings.forEach(building -> createBuildingReplacement(building));
        log.info("Created " + lodRepository.getAllElements().size() + " building replacements");
        // Replace districts level by level
        Collection<ACityElement> allDistricts = cityRepository.getElementsByType(ACityElement.ACityType.District);
        log.info("Total districts: " + allDistricts.size());
        // 1) Districts without contained districts
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
        log.info("Total LOD elements after first step: " + lodRepository.getAllElements().size());
        // n) Districts without unreplaced subelements: Foreach subelement(except refs like seas) if replacement == null return false
        while (remainingDistricts.size() > 0) {
            log.info("Remaining: " + remainingDistricts.size());
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
            log.info("Total LOD elements: " + lodRepository.getAllElements().size());
        }
        // TODO Merge both & check for first round explicitly?

        lodRepository.getAllElements().forEach(lod -> {
            lod.setColor("#FF0000");
            lod.setAframeProperty(createAFrameAsJSON(lod));
        });
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
        // Include subElements for other metaphors
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
        minX -= padding;
        maxX += padding;
        minY -= padding;
        maxY += padding;
        minZ -= padding;
        maxZ += padding;
        // Calculate properties
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
        // subElements
        for (ACityElement element : district.getSubElements()) {
            // TODO Skip reference objects like clouds?
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
        // Calculate properties
        lod.setWidth(district.getWidth() + 2*padding);
        lod.setHeight(maxY - minY);
        lod.setLength(district.getWidth() + 2*padding);
        lod.setXPosition(district.getXPosition());
        lod.setYPosition((minY + maxY) / 2);
        lod.setZPosition(district.getZPosition());
        // Add to repo
        lodRepository.addElement(lod);
    }

    private String createAFrameAsJSON(LODElement element) {
        return "todo";  // TODO Which properties are required in UI?
    }

    public void createFileAFrame() {
        StringBuilder content = new StringBuilder();
        lodRepository.getAllElements().forEach(element -> content.append(createAFrameAsXML(element)));
        writeFile("/lodobjects.html", content.toString());
    }

    public void createFileMetadata() {
        StringBuilder content = new StringBuilder();
        content.append("{\"blocks\":[");
        lodRepository.getAllElements().forEach(element -> content.append(createRelationInfo(element)));
        content.setCharAt(content.length() - 1, ']');
        content.append("}");
        writeFile("/lodinfo.json", content.toString());
    }

    private String createAFrameAsXML(LODElement element) {
        String template = "<a-box id=\"%s\" position=\"%f %f %f\" height=\"%f\" width=\"%f\" depth=\"%f\" color=\"%s\" material=\"opacity: 0.5\"></a-box>\n";
        return String.format(template,
                element.getHash(),
                element.getXPosition(),
                element.getYPosition(),
                element.getZPosition(),
                element.getHeight(),
                element.getWidth(),
                element.getLength(),
                element.getColor());
    }

    private String createRelationInfo(LODElement element) {
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
