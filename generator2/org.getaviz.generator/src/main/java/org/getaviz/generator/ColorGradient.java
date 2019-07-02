package org.getaviz.generator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorGradient {

    public static List<String> createColorGradient(String startHex, String endHex, int maxLevel) {
        Color start = Color.decode(startHex);
        Color end = Color.decode(endHex);

        int steps = maxLevel - 1;
        if (maxLevel == 1) {
            steps++;
        }

        float r_step = (end.getRed() - start.getRed()) / steps;
        float g_step = (end.getGreen() - start.getGreen()) / steps;
        float b_step = (end.getBlue() - start.getBlue()) / steps;

        List<String> colorRange = new ArrayList<>();
        for (int i = 0; i < maxLevel; i++) {
            float newR = (start.getRed() + i * r_step)/255f;
            float newG = (start.getGreen() + i * g_step)/255f;
            float newB = (start.getBlue() + i * b_step)/255f;
            colorRange.add(newR + " " + newG + " " + newB);
        }
        return colorRange;
    }
}
