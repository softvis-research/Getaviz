package org.getaviz.generator.rd.m2m;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import org.apache.commons.lang3.StringUtils;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.rd.s2m.Disk;
import org.getaviz.generator.rd.s2m.DiskSegment;

import java.util.ArrayList;
import java.util.List;

class SegmentLayout {

    static void calculateRings(ArrayList<Disk> disks, SettingsConfiguration.OutputFormat outputFormat) {
        disks.forEach(disk -> {
            double r_data;
            double r_methods;
            double b_methods;
            double width;
            double factor;
            double innerRadius;
            double height = disk.getHeight();
            double radius = disk.getRadius();
            double ringWidth = disk.getRingWidth();
            double areaWithoutBorder = disk.getAreaWithoutBorder();
            ArrayList<DiskSegment> innerSegments = disk.getInnerSegments();
            ArrayList<DiskSegment> outerSegments = disk.getOuterSegments();
            ArrayList<Disk> subDisksList = disk.getSubDisksList();
            double innerSegmentsArea = calculateSum(innerSegments, areaWithoutBorder);
            double outerSegmentsArea = calculateSum(outerSegments, areaWithoutBorder);
            disk.setSegmentsArea();
            disk.calculateSpines(radius - (0.5 * ringWidth));
            if (subDisksList.size() == 0) {
                r_data = Math.sqrt(innerSegmentsArea * areaWithoutBorder / Math.PI);
                r_methods = radius - ringWidth;
                b_methods = r_methods - r_data;
                width = r_data;
                factor = 0.5 * r_data;
                innerRadius = 0.0;
            } else {
                double outerRadius = calculateOuterRadius(subDisksList);
                r_data = Math.sqrt((innerSegmentsArea * areaWithoutBorder / Math.PI) + (outerRadius * outerRadius));
                double b_data = r_data - outerRadius;
                r_methods = Math.sqrt((outerSegmentsArea * areaWithoutBorder / Math.PI) + (r_data * r_data));
                b_methods = r_methods - r_data;
                width = b_data;
                factor = r_data - 0.5 * b_data;
                innerRadius = r_data - b_data;
            }
            if (!innerSegments.isEmpty()) {
                updateDiskSegment(innerSegments, height, width, factor, outputFormat, r_data, innerRadius);
            }
            if (!outerSegments.isEmpty()) {
                updateDiskSegment(outerSegments, height, width, r_methods - 0.5 * b_methods, outputFormat,
                        r_methods, r_data);
            }
        });
    }

    private static double calculateSum(ArrayList<DiskSegment> list, double areaWithBorder) {
        return Disk.sum(list) / areaWithBorder;
    }

    private static double calculateOuterRadius(ArrayList<Disk> subDisksList) {
        CoordinateList coordinates = new CoordinateList();
        for (Disk d : subDisksList) {
            double x = d.getPosition().x;
            double y = d.getPosition().y;
            double radius = d.getRadius();
            coordinates.add(RDLayout.createCircle(x, y, radius)
                    .getCoordinates(), false);
        }
        GeometryFactory geoFactory = new GeometryFactory();
        MultiPoint innerCircleMultiPoint = geoFactory.createMultiPoint(coordinates.toCoordinateArray());
        MinimumBoundingCircle mbc = new MinimumBoundingCircle(innerCircleMultiPoint);
        return mbc.getRadius();
    }

    private static void updateDiskSegment(ArrayList<DiskSegment> list, double height, double width, double factor,
                                          SettingsConfiguration.OutputFormat outputFormat, double outer, double inner) {
        list.forEach(segment -> SegmentLayout.calculateCrossSection(segment, width, height));
        if (outputFormat == SettingsConfiguration.OutputFormat.X3D) {
            calculateSpines(list, factor);
        }
        if (outputFormat == SettingsConfiguration.OutputFormat.AFrame) {
            SegmentLayout.calculateAngle(list);
            for (DiskSegment data : list) {
                data.setOuterAndInnerRadius(outer, inner);
            }
        }
    }

    private static void calculateSpines(ArrayList<DiskSegment> segments, double factor) {
        int spinePointCount;
        if (segments.size() < 50) {
            spinePointCount = 400;
        } else {
            spinePointCount = 1000;
        }
        List<String> completeSpine = new ArrayList<>();
        double stepX = 2 * Math.PI / spinePointCount;

        for (int i = 0; i < spinePointCount; ++i) {
            completeSpine.add(factor * Math.cos(i * stepX) + " " + factor * Math.sin(i * stepX) + " " + 0.0);
        }
        completeSpine.add(completeSpine.get(0));
        // calculate spines according to fractions
        int start;
        int end = 0;
        for (DiskSegment segment : segments) {
            double size = segment.getSize();
            start = end;
            end = start + (int) Math.floor(spinePointCount * size);
            if (end > (completeSpine.size() - 1)) {
                end = completeSpine.size() - 1;
            }
            if (segment == segments.get(segments.size() - 1)) {
                end = completeSpine.size() - 1;
            }
            List<String> partSpine = new ArrayList<>();
            for (int j = 0; j < end - start; j++) {
                partSpine.add(completeSpine.get(start + j));
            }
            String spine = " \'" + removeBrackets(partSpine) + "\'";
            segment.setSpine(spine);
        }
    }

    private static void calculateAngle(ArrayList<DiskSegment> segments) {
        if (!segments.isEmpty()) {
            int length = segments.size();

            double position = 0.0;
            double sizeSum = Disk.sum(segments);
            sizeSum += sizeSum / 360 * length;
            for (DiskSegment segment : segments) {
                double angle = (segment.getSize() / sizeSum) * 360;
                segment.setAngle(angle);
                segment.setAnglePosition(position);
                position += angle + 1;
            }
        }
    }

    private static void calculateCrossSection(DiskSegment element, double width, double height) {
        String crossSection = "\'" + (-(width / 2) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", "
                + ((width / 2) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height)) +
                "\'";
        element.setCrossSection(crossSection);
    }

    private static String removeBrackets(List<String> list) {
        return removeBrackets(list.toString());
    }

    private static String removeBrackets(String string) {
        return StringUtils.remove(StringUtils.remove(string, "["), "]");
    }

}
