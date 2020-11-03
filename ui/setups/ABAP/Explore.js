var setup = {

    controllers: [
        { 	name: 	"defaultLogger",
            logActionConsole	: false,
            logEventConsole		: false
        },
        
        {
            name: "canvasHoverController"
        },
    ],


    uis: [


        {	name: "UI0",
            area: {
                name: "top",
                orientation: "horizontal",
                resizable: false,
                first: {
                    size: "10%",
                    collapsible: true,
                    controllers: [
                        {},
                        ],
                    },
                second: {
                    size: "90%",
                    collapsible: false,
                    area: {
                        orientation: "vertical",
                        name: "leftPanel",
                        first: {                            
							size: "10%",
							controllers: [
                                {},
                                ],
                        },
                        second: {
							size: "90%",
							collapsible: false,
                            name: "canvas",
                            canvas: {},
							controllers: [
								{ name: "defaultLogger" },
                                { name: "canvasHoverController" },
							],
                        }
                    }
				}
            }

        }

    ]
};
