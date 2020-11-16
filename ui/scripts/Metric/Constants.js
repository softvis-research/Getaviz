const domIDs = {
    executeButton: "executeButton",
    resetButton: "resetButton",
    addLayerButton: "addLayerButton",

    metrics: "metrics",
    mappings: "mappings",

    metricDropDown: "metricDropDown",
    mappingDropDown: "mappingDropDown",

    mappingColorDropDown: "mappingColorDropDown",
    mappingStartColorDropDown: "mappingStartColorDropDown",
    mappingEndColorDropDown: "mappingEndColorDropDown",

    metricFromInput: "metricFromInput",
    metricToInput: "metricToInput",
    metricFromDateInput: "metricFromDateInput",
    metricToDateInput: "metricToDateInput",

    metricFromText: "metricFromText",
    metricToText: "metricToText",

    mappingFromText: "mappingFromText",
    mappingToText: "mappingToText",

    mappingFromInput: "mappingFromInput",
    mappingToInput: "mappingToInput",

    mappingTransparencyInput: "mappingTransparencyInput",
    mappingPeriodInput: "mappingPeriodInput",
    mappingScaleInput: "mappingScaleInput",

    mappingPeriodText: "mappingPeriodText",
    mappingScaleText: "mappingScaleText",

    deleteButton: "deleteButton"
};

const cssIDs = {
    executeButton: "#" + domIDs.executeButton,
    resetButton: "#" + domIDs.resetButton,
    addLayerButton: "#" + domIDs.addLayerButton,

    metrics: "#" + domIDs.metrics,
    mappings: "#" + domIDs.mappings,

    metricDropDown: "#" + domIDs.metricDropDown,
    mappingDropDown: "#" + domIDs.mappingDropDown,

    mappingColorDropDown: "#" + domIDs.mappingColorDropDown,
    mappingStartColorDropDown: "#" + domIDs.mappingStartColorDropDown,
    mappingEndColorDropDown: "#" + domIDs.mappingEndColorDropDown,

    metricFromInput: "#" + domIDs.metricFromInput,
    metricToInput: "#" + domIDs.metricToInput,
    metricFromDateInput: "#" + domIDs.metricFromDateInput,
    metricToDateInput: "#" + domIDs.metricToDateInput,

    metricFromText: "#" + domIDs.metricFromText,
    metricToText: "#" + domIDs.metricToText,

    mappingFromText: "#" + domIDs.mappingFromText,
    mappingToText: "#" + domIDs.mappingToText,

    mappingFromInput: "#" + domIDs.mappingFromInput,
    mappingToInput: "#" + domIDs.mappingToInput,

    mappingTransparencyInput: "#" + domIDs.mappingTransparencyInput,
    mappingPeriodInput: "#" + domIDs.mappingPeriodInput,
    mappingScaleInput: "#" + domIDs.mappingScaleInput,

    mappingPeriodText: "#" + domIDs.mappingPeriodText,
    mappingScaleText: "#" + domIDs.mappingScaleText,

    deleteButton: "#" + domIDs.deleteButton
};

const domClasses = {
    metrics: "metrics",
    mappings: "mappings",

    layer: "layer",

    metricParameter: "metricParameter",
    mappingParameter: "mappingParameter",

    metricDropDown: "metricDropDown",
    mappingDropDown: "mappingDropDown",

    deleteButton: "deleteButton",

    textLabel: "textLabel"
}

const cssClasses = {
    metrics: "." + domClasses.metrics,
    mappings: "." + domClasses.mappings,

    layer: "." + domClasses.layer,

    metricParameter: "." + domClasses.metricParameter,
    mappingParameter: "." + domClasses.mappingParameter,

    metricDropDown: "." + domClasses.metricDropDown,
    mappingDropDown: "." + domClasses.mappingDropDown,

    deleteButton: "." + domClasses.deleteButton
}

const metrics = {
    numberOfStatements: "numberOfStatements",
    dateOfCreation: "dateOfCreation",
    dateOfLastChange: "dateOfLastChange"
};

const mappings = {
    color: "Color",
    colorGradient: "Color Gradient",
    transparency: "Transparency",
    pulsation: "Pulsation",
    flashing: "Flashing",
    rotation: "Rotation"
};

const colors = [
    "red",
    "blue",
    "green",
    "black",
    "yellow",
    "orange"
];

const widgetSize = {
    buttonHeight: 20,
    dropDownHeight: 30,
    inputHeight: 30,
    deleteButtonHeight: 30,

    dropDownWidth: "15%",
    inputWidth: "8%",
}