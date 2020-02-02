var setup = {

    controllers: [
        { 	name: 	"defaultLogger",
            logActionConsole	: false,
            logEventConsole		: false
        },
        {	name: 	"canvasMarkController",
            selectionMode: "DURATION",					//TODO Constants - UP - DOWN - DURATION
            selectionDurationSeconds: 0.5,
            selectionMoveAllowed: false,
            showProgressBar: true,
        },
        {
            name: "canvasSelectController"
        },
        {
            name: "canvasHoverController"
        },
        {
            name: "canvasFilterController"
        },
        {
            name: "packageExplorerController"
        },
        {
            name: "relationTransparencyController",
            fullFadeValue: 0.15,
            halfFadeValue: 0.45
        },
        {
            name: "relationHighlightController",
            unfadeOnHighlight: false
        },
        {
            name: "relationConnectorController",
            fixPositionY : false,
            showInnerRelations : true,
            sourceStartAtParentBorder : false,
            targetEndAtParentBorder : false,
            sourceStartAtBorder: true,
            targetEndAtBorder: true,
            createEndpoints : true
        },
        {
            name: "searchController"
        }
    ],


    uis: [


        {	name: "UI0",
            area: {
                name: "top",
                orientation: "horizontal",
                resizable: false,
                first: {
                    size: "10%",
                    collapsible: false,
                    controllers: [
                        { name: "searchController" }
                    ]
                },
                second: {
                    area: {
                        name: "main",
                        orientation: "vertical",
                        first: {
                            size: "20%",
                            controllers: [
                                { name: "packageExplorerController" },
                            ]
                        },
                        second: {
                            size: "80%",
                            collapsible: false,

                            canvas: {},

                            controllers: [
                                { name: "canvasMarkController" },
                                { name: "canvasFilterController"},
                                { name: "canvasSelectController"},
                                { name: "canvasHoverController"},
                                { name: "relationHighlightController"},
                                { name: "relationTransparencyController"},
                                { name: "relationConnectorController"},
                                { name: "defaultLogger" }
                            ]
                        }
                    }

                }
            }

        }

    ]
};
