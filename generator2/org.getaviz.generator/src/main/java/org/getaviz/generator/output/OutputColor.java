package org.getaviz.generator.output;
import org.getaviz.generator.SettingsConfiguration;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class OutputColor extends Color {

    public OutputColor(float r, float g, float b) {
        super(r,g,b);
    }

    public OutputColor(String hexValue) {
        this(Color.decode(hexValue).getRed()/255f,Color.decode(hexValue).getGreen()/255f, Color.decode(hexValue).getBlue()/255f);
    }

    public String toString() {
        if(SettingsConfiguration.getInstance().getOutputFormat() == SettingsConfiguration.OutputFormat.AFrame) {
            return toHexValue();
        } else {
            return toX3DValue();
        }
    }

    private String toHexValue() {
        return String.format("#%02x%02x%02x", getRed(), getGreen(), getBlue());
    }

    private String toX3DValue() {
        return getRed()/255f + " " + getGreen()/255f + " " + getBlue()/255f;
    }

    public static List<OutputColor> createColorGradient(OutputColor start, OutputColor end, int maxLevel) {
        int steps = maxLevel - 1;
        if (maxLevel == 1) {
            steps++;
        }

        float r_step = (end.getRed() - start.getRed()) / steps;
        float g_step = (end.getGreen() - start.getGreen()) / steps;
        float b_step = (end.getBlue() - start.getBlue()) / steps;

        List<OutputColor> colorRange = new ArrayList<>();
        for (int i = 0; i < maxLevel; i++) {
            float newR = (start.getRed() + i * r_step)/255;
            float newG = (start.getGreen() + i * g_step)/255;
            float newB = (start.getBlue() + i * b_step)/255;
            colorRange.add(new OutputColor(newR, newG, newB));
        }
        return colorRange;
    }
}
