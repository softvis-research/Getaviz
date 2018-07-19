var setup = {

	loadPopUp: true,

		
	controllers: [	

		{ 	name: 	"defaultLogger",

			logInfoConsole		: false,
			logActionConsole	: false,
			logEventConsole		: false
		},		
		
		{	name: 	"emailController",
			
			createHeadSection: false
		},	

		{	name: 	"canvasHoverController",			
		},	

		{	name: 	"canvasMarkController",
		},	
		
		{	name: 	"canvasSelectController" 
		},	

		{	name: 	"canvasFilterController" 
		},

		{ 	name: 	"canvasFlyToController" 
		},
	
		{	name: 	"searchController" 
		},

		{	name: 	"packageExplorerController",
		},
		{	name: 	"sourceCodeController",
            url: "https://raw.githubusercontent.com/softvis-research/Bank/master/src/"
		},
		
		{ 	name: 	"relationConnectorController",		
						
			fixPositionZ : 1,
			showInnerRelations : true,
			elementShape : "circle",					
			sourceStartAtParentBorder : true,
			targetEndAtParentBorder : false,
			createEndpoints: true,
		},

		{ 	name: 	"relationTransparencyController",
		},
			
		{ 	name: 	"relationHighlightController" 
		},
        {
            name:   "systeminfoController",
            system: "Bank",
            link: "https://github.com/softvis-research/Bank",
            noc: true,
            loc: 192
        },
		{	name: 	"menuController",
			menuMapping: [

				{	
					title:		"View",
					subMenu:	true,
					items:		[
						{
							title: 		"FlyTo",
							toggle: 	true,	
							eventOn: 	"canvasFlyToController.activate",
							eventOff: 	"canvasFlyToController.deactivate",									
						},

						{
							title: "Reset Visualization",
							event: "application.reset",
						},
					]
				},

				{	
					title:		"Relations",
					subMenu:	true,
					items:		[
						{
							title: 		"Relation Connectors",
							toggle: 	true,	
							eventOn: 	"relationConnectorController.activate",
							eventOff: 	"relationConnectorController.deactivate",			
						},
						{
							title: 		"Relation Transparency",
							toggle: 	true,	
							eventOn: 	"relationTransparencyController.activate",
							eventOff: 	"relationTransparencyController.deactivate",			
						},
						{
							title: 		"Relation Highlight",
							toggle: 	true,	
							eventOn: 	"relationHighlightController.activate",
							eventOff: 	"relationHighlightController.deactivate",			
						},
					]
				},

				{	
					title:		"Visualizations",
					subMenu:	true,
					items:		[
						{
							title: 	"City Original",
							link: 	true,
							url:	"index.php?setup=web/City freemind&model=City%20original%20freemind"
						},
						{
							title: 	"City Bricks",
							link: 	true,
							url:	"index.php?setup=web/City freemind&model=City%20bricks%20freemind"
						},						
						{
							title: 	"City Floors",
							link: 	true,
							url:	"index.php?setup=web/City freemind&model=City%20floor%20freemind"
						},
						{
							title: 	"Recursive Disk",
							link: 	true,
							url:	"index.php?setup=web/RD freemind&model=RD%20freemind"
						},
                        {
                            title: 	"Recursive Disk 3D",
                            link: 	true,
                            url:	"index.php?setup=web/RD reek&model=RD%203D%20reek"
                        },
					]
				},

				{	
					title:		"About",
					subMenu:	true,
					items:		[
						{
							title: 	"University Leipzig",
							link: 	true,
							url:	"https://www.wifa.uni-leipzig.de/en/information-systems-institute/se/research/softwarevisualization-in-3d-and-vr.html"							
						},
						{
							title: 		"Feedback!",
							event: 		"emailController.openMailPopUp",
						},
						{
							title: 		"Impressum",
							popup:		true,
							text: 		"<b>Universität Leipzig</b><br\/\>"+
										" <br\/\>"+										
										"Wirtschaftswissenschaftliche Fakultät<br\/\>"+
										"Institut für Wirtschaftsinformatik<br\/\>"+
										"Grimmaische Straße 12<br\/\>"+
										"D - 04109 Leipzig<br\/\>"+
										" <br\/\>"+
										"<b>Dr. Richard Müller</b><br\/\>"+
										"rmueller(-a-t-)wifa.uni-leipzig.de<br\/\>",
							height: 	200,
							width:		2050,
						},
                        {
                            title: 	"Privacy Policy",
                            link: 	true,
                            url:	"http://home.uni-leipzig.de/svis/privacy-policy/"
                        }
					]
				},				
			]
		},
        {
            name: "legendController",
            entries: [{
                name: "Package",
                icon: "setups/web/package.png"
            }, {
                name: "Type",
                icon: "setups/web/type.png",
            }, {
                name: "Method",
                icon: "setups/web/method.png",
            }, {
                name: "Field",
                icon: "setups/web/field.png",
            }, {
                name: "Navigation",
                icon: "setups/web/navigation.png",
                entries: [
                    {
                        name: "Rotate",
                        icon: "setups/web/left.png"
                    }, {
                        name: "Center",
                        icon: "setups/web/double.png"
                    }, {
                        name: "Move",
                        icon: "setups/web/middle.png"
                    }, {
                        name: "Zoom",
                        icon: "setups/web/zoom.png"
                    }]
            }
            ],
        }
	],
	
	
	

	uis: [


        {
            name: "UI0",

            navigation: {
                //examine, walk, fly, helicopter, lookAt, turntable, game
                type: "turntable",

                //turntable last 2 values - accepted values are between 0 and PI - 0.0 - 1,5 at Y-axis
                typeParams: "0.0 0.0 1.57 3.1",

                //speed: 10
            },


            area: {
                name: "top",
                orientation: "horizontal",
                resizable: false,
                collapsible: false,
                first: {
                    size: "75px",
                    collapsible: false,
                    controllers: [
                        {name: "menuController"},
                        {name: "searchController"},
                        {name: "emailController"},
                    ],
                },
                second: {
                    size: "80%",
                    collapsible: false,
                    area: {
                        orientation: "vertical",
                        name: "leftPanel",
                        size: "20%",
                        first: {
                            size: "20%",
                            area: {
                                size: "50%",
                                collapsible: false,
                                orientation: "horizontal",
                                name: "packagePanel",
                                first: {
                                    collapsible: false,
                                    size: "65%",
                                    expanders: [
                                        {
                                            name: "packageExplorer",
                                            title: "Package Explorer",
                                            controllers: [
                                                {name: "packageExplorerController"}
                                            ],
                                        }
                                    ]
                                },
                                second: {
                                    size: "50%",
                                    area: {
                                        orientation: "horizontal",
                                        name: "legendPanel",
                                        size: "100%%",
                                        collapsible: false,
                                        first: {
                                            size: "100%",
                                            expanders: [
                                                {
                                                    name: "legend",
                                                    title: "Legend",

                                                    controllers: [
                                                        {name: "legendController"}
                                                    ],
                                                },
                                            ]
                                        },
                                        second: {
                                        },
                                    }
                                },
                            },
                        },
                        second: {
                            collapsible: false,
                            area: {
                                orientation: "vertical",
                                collapsible: false,
                                name: "canvas",
                                size: "50%",
                                first: {
                                    size: "80%",
                                    collapsible: false,
                                    canvas: {},
                                    controllers: [
                                        {name: "defaultLogger"},
                                        {name: "canvasSelectController"},
                                        {name: "canvasMarkController"},
                                        {name: "canvasHoverController"},
                                        {name: "canvasFilterController"},
                                        {name: "canvasFlyToController"},
                                        {name: "relationConnectorController"},
                                        {name: "relationTransparencyController"},
                                        {name: "relationHighlightController"},
                                    ],
                                },
                                second: {
                                    area: {
                                        orientation: "horizontal",
                                        collapsible: false,
                                        name: "rightPael",
                                        size: "80%",
                                        first: {
                                            size: "80%",
                                            min: "200",
                                            oriontation: "horizontal",
                                            expanders: [
                                                {
                                                    name: "CodeViewer",
                                                    title: "CodeViewer",
                                                    controllers: [
                                                        {name: "sourceCodeController"}
                                                    ],
                                                },
                                            ],
                                        },
                                        second: {
                                            size: "20%",
                                            min: "200",
                                            oriontation: "horizontal",
                                            expanders: [
                                                {
                                                    name: "systeminfo",
                                                    title: "Info",
                                                    controllers: [
                                                        {name: "systeminfoController"}
                                                    ],
                                                },
                                            ],
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
	]
};
