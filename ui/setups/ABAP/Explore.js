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
                    collapsible: false,
                    controllers: [
                        { }
                    ]
                },
                second: {
                    area: {
                        name: "main",
                        orientation: "vertical",
                        first: {
                            size: "20%",
                            controllers: [
                                { },
                            ]
                        },
                        second: {
                            size: "80%",
                            collapsible: false,

                            canvas: {},

                            controllers: [                               
                                { name: "canvasHoverController"},                                
                                { name: "defaultLogger" },
                            ]
                        }
                    }

                }
            }

        }

    ]
};
