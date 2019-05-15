package org.getaviz.generator.tests;

import org.getaviz.generator.output.OutputColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutputColorTest {

    @Test
    void testFloatConstructor() {
        OutputColor color = new OutputColor(0.5f, 0.5f, 0.5f);
        assertEquals(128, color.getRed());
        assertEquals(128, color.getBlue());
        assertEquals(128, color.getGreen());
    }

    @Test
    void testHexConstructor() {
        OutputColor color = new OutputColor("#99CCFF");
        assertEquals(153, color.getRed());
        assertEquals(204, color.getGreen());
        assertEquals(255, color.getBlue());
    }
}

