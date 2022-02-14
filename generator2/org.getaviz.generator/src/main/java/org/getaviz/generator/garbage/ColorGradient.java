package org.getaviz.generator.garbage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorGradient {

    public static List<String> createColorGradient(String startHex, String endHex, int maxLevel) {
        Color start = Color.decode(startHex);
        Color end = Color.decode(endHex);
        int steps = calculateSteps(maxLevel);

        int r_step = (end.getRed() - start.getRed()) / steps;
        int g_step = (end.getGreen() - start.getGreen()) / steps;
        int b_step = (end.getBlue() - start.getBlue()) / steps;

        List<String> colorRange = new ArrayList<>();
        for (int i = 0; i < maxLevel; i++) {
            int newRed = (start.getRed() + i * r_step);
            int newGreen = (start.getGreen() + i * g_step);
            int newBlue = (start.getBlue() + i * b_step);
            String hexColor = String.format("#%02x%02x%02x", newRed, newGreen, newBlue);
            colorRange.add(hexColor);
        }
        return colorRange;
    }

    private static int calculateSteps(int maxLevel) {
        int steps = maxLevel - 1;
        if (maxLevel == 1) {
            steps++;
        }
        return steps;
    }
}
