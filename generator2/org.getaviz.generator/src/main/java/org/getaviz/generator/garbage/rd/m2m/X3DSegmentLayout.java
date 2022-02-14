package org.getaviz.generator.garbage.rd.m2m;

import org.getaviz.generator.garbage.rd.DiskSegment;
import org.getaviz.generator.garbage.rd.SubDisk;
import java.util.ArrayList;
import java.util.List;

class X3DSegmentLayout {

    private List<SubDisk> disks;

    X3DSegmentLayout(List<SubDisk> disks) {
        this.disks = disks;
    }

    void calculateOuterSegments() {
        disks.forEach(disk -> {
            double r_data = disk.getInnerSegmentsRadius();
            double r_methods;
            double height = disk.getHeight();
            double radius = disk.getRadius();
            double ringWidth = disk.getBorderWidth();
            ArrayList<DiskSegment> outerSegments = disk.getOuterSegments();
            double outerSegmentsArea  = disk.getOuterSegmentsArea();
            double outerRadius = disk.getInnerRadius();
            double width = r_data - outerRadius;
            if (disk.hasInnerDisks()) {
                r_methods = Math.sqrt((outerSegmentsArea / Math.PI) + (r_data * r_data));
            } else {
                r_methods = radius - ringWidth;
            }
            double meine = 0.5 * r_methods - 0.5 * r_data;
            if (disk.hasOuterSegments()) {
                updateDiskSegment(outerSegments, height, width, meine);
            }
        });
    }

    void calculateInnerSegments() {
        disks.forEach(disk -> {
            double innerRadius = disk.getInnerRadius();
            ArrayList<DiskSegment> innerSegments = disk.getInnerSegments();
            double innerSegmentsArea = disk.getInnerSegmentsArea();
            double r_data = Math.sqrt((innerSegmentsArea/ Math.PI) + (innerRadius * innerRadius));
            disk.setInnerSegmentsRadius(r_data);
            if (disk.hasInnerSegments()) {
                double height = disk.getHeight();
                double width = r_data - innerRadius;
                double factor = 0.5 *  r_data - 0.5 * innerRadius;
                updateDiskSegment(innerSegments, height, width, factor);
            }
        });
    }

    private void updateDiskSegment(ArrayList<DiskSegment> segments, double height, double width, double factor) {
        segments.forEach(segment -> calculateCrossSection(segment, width, height));
        calculateSpines(segments, factor);
    }

    private void calculateSpines(ArrayList<DiskSegment> segments, double factor) {
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
            String spine = " \'" + String.join("", partSpine) + "\'";
            segment.setSpine(spine);
        }
    }

    private void calculateCrossSection(DiskSegment segment, double width, double height) {
        String crossSection = "\'" + (-(width / 2) + " " + (height)) + ", " + ((width / 2) + " " + (height)) + ", "
                + ((width / 2) + " " + 0) + ", " + (-(width / 2) + " " + 0) + ", " + (-(width / 2) + " " + (height)) +
                "\'";
        segment.setCrossSection(crossSection);
    }
}
