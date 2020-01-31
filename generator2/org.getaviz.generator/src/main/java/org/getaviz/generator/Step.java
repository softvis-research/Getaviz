package org.getaviz.generator;

public interface Step {
    boolean checkRequirements();
    void run();
}
