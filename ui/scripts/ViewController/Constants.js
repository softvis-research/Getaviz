const domIDs = {
    viewControllerHeader: "viewControllerHeader",
    metricDiv: "metricDiv",
    mappingDiv: "mappingDiv",
    
    downloadViewConfigButton: "downloadViewConfigButton",
    executeButton: "executeButton",
    resetButton: "resetButton",
    addLayerButton: "addLayerButton",

    headerTextNode: "headerTextNode",
    metricTextNode: "metricTextNode",
    mappingTextNode: "mappingTextNode",

    viewDropDown: "viewDropDown",
    
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
    downloadViewConfigButton: "#" + domIDs.downloadViewConfigButton,
    executeButton: "#" + domIDs.executeButton,
    resetButton: "#" + domIDs.resetButton,
    addLayerButton: "#" + domIDs.addLayerButton,

    metricTextNode: "#" + domIDs.metricTextNode,
    mappingTextNode: "#" + domIDs.mappingTextNode,

    viewDropDown: "#" + domIDs.viewDropDown,
    
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
    metricDiv: "metricDiv",
    mappingDiv: "mappingDiv",
    
    metricTextNode: "metricTextNode",
    mappingTextNode: "mappingTextNode",

    layer: "layer",

    metricParameter: "metricParameter",
    mappingParameter: "mappingParameter",

    viewDropDown: "viewDropDown",
    
    metricDropDown: "metricDropDown",
    mappingDropDown: "mappingDropDown",

    deleteButton: "deleteButton",

    textLabel: "textLabel"
}

const cssClasses = {
    metricTextNode: "." + domClasses.metricTextNode,
    mappingTextNode: "." + domClasses.mappingTextNode,

    layer: "." + domClasses.layer,
    
    metricParameter: "." + domClasses.metricParameter,
    mappingParameter: "." + domClasses.mappingParameter,

    viewDropDown: "." + domClasses.viewDropDown,

    metricDropDown: "." + domClasses.metricDropDown,
    mappingDropDown: "." + domClasses.mappingDropDown,

    deleteButton: "." + domClasses.deleteButton
}

const metrics = {
    numberOfStatements: "numberOfStatements",
    amountOfResults: "amountOfResults",
    amountOfNamspa: "amountOfNamspa",
    amountOfChnhis: "amountOfChnhis",
    amountOfCodlen: "amountOfCodlen",
    amountOfCommam: "amountOfCommam",
    amountOfDynsta: "amountOfDynsta",
    amountOfEnhmod: "amountOfEnhmod",
    amountOfFormty: "amountOfFormty",
    amountOfNomac: "amountOfNomac",
    amountOfObjnam: "amountOfObjnam",
    amountOfPraefi: "amountOfPraefi",
    amountOfSlin: "amountOfSlin",
    amountOfSql: "amountOfSql",
    amountOfTodo: "amountOfTodo",
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
    buttonHeight: 25,
    headerDropDownHeight: 27,
    dropDownHeight: 25,
    inputHeight: 25,
    deleteButtonHeight: 27,

    dropDownWidth: 150,
    buttonWidth: 125,
    inputWidthMapping: 73,
    inputWidthMetric: 100,
    deleteButtonWidth: "3.6%",
}