package org.getaviz.generator.garbage;

public interface Step {
    boolean checkRequirements();
    void run();
}
