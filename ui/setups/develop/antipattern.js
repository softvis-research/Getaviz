var setup = {

	controllers: [

		{ 	name: 	"defaultLogger",

			logActionConsole	: false,
			logActionEventConsole	: false,
			logEventConsole		: false,
			logInfoConsole : true,
            logWarningConsole: true,
            logErrorConsole: true
		},
		{	name: 	"canvasHoverController",
			hoverColor: "blue",
            showVersion: true
		},
		{	name: 	"canvasSelectController",
			color: "blue"
		},
		{	name: 	"canvasFilterController"
		},
		{ 	name: 	"canvasFlyToController",
			targetType: "Namespace"
		},
		{ 	name: 	"canvasResetViewController"
		},
		{ 	name: 	"edgeConfiguratorController"
		},
		{	name: 	"searchController"
		},
        {	name: 	"patternExplorerController"
		},
		{	name: 	"versionExplorerController"
		},
        {
            name:   "legendController"
        },
        {
            name:   "systeminfoController",
			system: "antlr",
			link: "http://www.antlr.org/",
			visualization: "Recursive Disk"
        },
		{ 	name: 	"patternConnectorController",
			showInnerRelations: true,
			createEndPoints: true,
            elementShape : "square",					//circle, square
            sourceStartAtParentBorder : false,
            targetEndAtParentBorder : false,
            sourceStartAtBorder: true,
            targetEndAtBorder: true
		},
		{ 	name: 	"patternTransparencyController"
		}
	],

	uis: [{
		name: "Antipattern",
		navigation: { type:	"examine" },
		area: {
			name: "top",
			orientation: "vertical",
			collapsible: false,
			resizable: false,
			first: {
				size: "20%",
				collapsible: false,
				expanders: [{
					name: "patternExplorer",
					title: "Antipattern Explorer",
					controllers: [{ name: "patternExplorerController" }]
				}]
			},
			second: {
				size: "60%",
				collapsible: false,
				area: {
					name: "canvas",
					orientation: "vertical",
					collapsible: false,
					first: {
						size: "80%",
						canvas: { },
						collapsible: false,
						controllers: [
							{ name: "defaultLogger" },
							{ name: "canvasHoverController" },
							{ name: "canvasFilterController" },
							{ name: "canvasSelectController" },
							{ name: "canvasMarkController" },
							{ name: "canvasFlyToController" },
							{ name: "patternConnectorController" },
							{ name: "patternTransparencyController" }
						]
					},
					second: {
					    collabsible: false,
                        resizable: false,
                        area: {
							first: {
                                resizable: false,
                                collapsible: false
                            },
                            second: {
                                collapsible: false,
                                resizable: false,
                                area: {
                                    orientation: "horizontal",
                                    collapsible: false,
                                    size: "0px",
                                    resizable: false,
                                    first: {
                                        collabsible: false
                                    },second: {
                                        collapsible: true,
                                        expanders: [
                                            {
                                                name: "versionExplorer",
                                                title: "Version Selector",
                                                controllers: [{ name: "versionExplorerController" }]
                                            },{
                                                name: "edgeConfigurator",
                                                title: "Configuration",
                                                size: "100px",
                                                controllers: [{name: "edgeConfiguratorController"}]
                                            },
                                            {
                                                name: "legend",
                                                title: "Legend",
                                                controllers: [{name: "legendController"}]
                                            },
                                            {
                                                name: "systeminfo",
                                                title: "Info",
                                                controllers: [{name: "systeminfoController"}]
                                            }]
                                    }
                                }
                            }
						}
					}
				}
			}
		}
	}]
};
