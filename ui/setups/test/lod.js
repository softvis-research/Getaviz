var setup = {

    controllers: [
        {
            name: "defaultLogger",
            logActionConsole: false,
            logEventConsole	: false
        },

        {
            name: "canvasHoverController"
        },

        {
            name: "lodController"
        }
    ],

    uis: [
        {
            name: "UI0",
            canvas: {},
			controllers: [
				{ name: "defaultLogger" },
                { name: "canvasHoverController" },
                { name: "lodController" }
			]
        }
    ]

};
