package org.getaviz.generator.spl;

public class FeatureAffiliation {

    public String feature;

    public String traceType;

    public Boolean isRefinement = false;

    public ElementarySet elementarySet;

    enum ElementarySet{
        None,
        Pure,
        And,
        Or
    }
}
