var setup = {

    loadPopUp: true,


    controllers: [

        {
            name: "defaultLogger",

            logInfoConsole: true,
            logActionConsole: true,
            logEventConsole: true
        },

        {
            name: "emailController",

            createHeadSection: false
        },
        {
            name:	"generationFormController",
        },
        {
            name: 'canvasFilterController'
        },

        {
            name: "canvasHoverController",
            hoverColor: "orange"
        },

        {
            name: "canvasMarkController",
        },

        {
            name: "canvasSelectController",
            color: "orange"
        },
        {
            name: "canvasFlyToController",
            targetType: "TranslationUnit",
            parentLevel: 1
        },

        {
            name: "searchController",
            timeout: true

        },

        {
            name: "packageExplorerController",

            elementsSelectable: false
        },
        {
            name: "sourceCodeController",
            fileType: "c",
            url: "http://home.uni-leipzig.de/svis/getaviz_c/data/busybox/model/src/"
        },
        {
            name: "macroExplorerController",
            //should the filtered elements be "removed" or just "transparent"?
            filterMode: "transparent"
        },
        {
            name: "relationConnectorController",
            fixPositionY: false,
            showInnerRelations: true,
            sourceStartAtParentBorder: true,
            targetEndAtParentBorder: true,
            sourceStartAtBorder: false,
            targetEndAtBorder: false,
            createEndpoints: false
        },

// 		{ 	name: 	"relationTransparencyController",
// 		},

        {
            name: "relationHighlightController"
        },
        {
            name: 'infoController',
            system: "Busybox",
            noc: false,
            not: true,
            loc: 192000
        },
        {
            name: 'helpController',
            metaphor: "RD C"
        },
        {
            name: "menuController",
            menuMapping: [

                {
                    title: "View",
                    subMenu: true,
                    items: [
                        {
                            title: "FlyTo",
                            toggle: true,
                            eventOn: "canvasFlyToController.activate",
                            eventOff: "canvasFlyToController.deactivate",
                        },

                        {
                            title: "Reset Visualization",
                            event: "application.reset",
                        },
                    ]
                },

                {
                    title: "Relations",
                    subMenu: true,
                    items: [
                        {
                            title: "Relation Connectors",
                            toggle: true,
                            eventOn: "relationConnectorController.activate",
                            eventOff: "relationConnectorController.deactivate",
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
							url:	"index.php?setup=web_a-frame/City bank&model=City%20bank%20aframe&aframe=true"
						},
						{
							title: 	"Recursive Disk",
							link: 	true,
							url:	"index.php?setup=web/RD bank&model=RD%20bank"
						},
                        {
                            title: 	"New Visualization",
                            event:	"generationFormController.openSettingsPopUp"
                        },
					]
				},

                {
                    title: "About",
                    subMenu: true,
                    items: [
                        {
                            title: "Research Group",
                            link: true,
                            url: "http://home.uni-leipzig.de/svis/"
                        },
                        {
                            title: "Feedback",
                            event: "emailController.openMailPopUp",
                        },
                        {
                            title: "Impressum",
                            popup: true,
                            text: "<b>Universität Leipzig</b><br\/\>" +
                                " <br\/\>" +
                                "Wirtschaftswissenschaftliche Fakultät<br\/\>" +
                                "Institut für Wirtschaftsinformatik<br\/\>" +
                                "Grimmaische Straße 12<br\/\>" +
                                "D - 04109 Leipzig<br\/\>" +
                                " <br\/\>" +
                                "<b>Dr. Richard Müller</b><br\/\>" +
                                "rmueller(-a-t-)wifa.uni-leipzig.de<br\/\>",
                            height: 200,
                            width: 2050,
                        },
                        {
                            title: "Privacy Policy",
                            link: true,
                            url: "http://home.uni-leipzig.de/svis/privacy-policy/"
                        }
                    ]
                },
            ]
        },
    ],


    uis: [


        {
            name: "UI0",


            area: {
                name: "top",
                orientation: "horizontal",
                resizable: false,
                collapsible: false,
                first: {
                    size: "75px",
                      resizable: false,
                collapsible: false,
                    controllers: [
                        {name: "menuController"},
                         {name: "searchController"},
                        {name: "emailController"},
                    ],
                },
                second: {
                            size: "80%",
                              resizable: false,
                collapsible: false,
                            area: {
                                orientation: "vertical",
                                name: "leftPanel",
                                size: "20%",
                                collapsible: false,
                                resizable: false,
                                first: {
                                    size: "20%",
                                     collapsible: false,
                        resizable: false,
                                    area: {
                                        size: "50%",
                                        collapsible: false,
                                        resizable: false,
                                        orientation: "horizontal",
                                        name: "packagePanel",
                                        first: {
                                            collapsible: false,
                                            size: "33%",
                                            expanders: [
                                                {
                                                    name: "macroExplorer",
                                                    title: "Feature Explorer",
                                                    controllers: [
                                                        {name: "macroExplorerController"}
                                                    ],
                                                }
                                            ]
                                        },
                                        second: {

                                            size: "50%",
                                            area: {
                                                orientation: "horizontal",
                                                name: "legendPanel",
                                                size: "50%",
                                                collapsible: false,
                                                first: {
                                                    size: "100%",
                                                    expanders: [
                                                        {
                                                            name: "packageExplorer",
                                                            title: "Structure Explorer",
                                                            controllers: [
                                                                {name: "packageExplorerController"}
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
                                        resizable: false,
                                        name: "canvas",
                                        size: "50%",
                                        first: {
                                            size: "80%",
                                            collapsible: false,
                                            resizable: false,
                                            canvas: {},
                                            controllers: [
                                                {name: "defaultLogger"},
                                                {name: "canvasSelectController"},
                                                {name: "canvasMarkController"},
                                                {name: "canvasHoverController"},
                                                {name: "canvasFilterController"},
                                                {name: "canvasFlyToController"},
                                                {name: "infoController"},                                                                                                {name: "helpController"},
                                                {name: "relationConnectorController"},
//                                         {name: "relationTransparencyController"},
                                                {name: "relationHighlightController"},                                                                                                        {name: "generationFormController"}
                                            ],
                                        },
                                        second: {
                                             collapsible: false,
                        resizable: false,
                                            area: {
                                                orientation: "horizontal",
                                                collapsible: false,
                                                resizable: false,
                                                name: "rightPael",
                                                size: "80%",
                                                first: {
                                                    size: "100%",
                                                    min: "200",
                                                     collapsible: false,
//                         resizable: false,
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
