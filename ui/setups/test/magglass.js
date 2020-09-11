var setup = {

    controllers: [
        { 	name: 	"defaultLogger",
            logActionConsole	: false,
            logEventConsole		: false
        },
        {
            name:   "magGlassController"
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
                },
                second: {
                    size: "90%",
                    collapsible: false,

                    canvas: {},

                    controllers: [
                        { name: "defaultLogger" },
                        { name: "magGlassController"}
                    ],
                }
            }

        }

    ]
};