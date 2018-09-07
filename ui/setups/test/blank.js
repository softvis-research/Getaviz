var setup = {

    controllers: [
        { 	name: 	"defaultLogger",
            logActionConsole	: false,
            logEventConsole		: false
        },

        {	name: 	"searchController",
        },

    ],


    uis: [


        {	name: "UI0",

            navigation: {
                //examine, walk, fly, helicopter, lookAt, turntable, game
                type:	"examine",
                //speed: 10
            },



            area: {
                name: "top",
                orientation: "horizontal",
                resizable: false,
                first: {
                    size: "10%",
                    collapsible: false,

                    controllers: [
                        { name : "searchController" },
                    ],
                },
                second: {
                    size: "90%",
                    collapsible: false,

                    canvas: {},

                    controllers: [
                        { name: "defaultLogger" },
                    ],
                }
            }

        }

    ]
};