var setup = {

    controllers: [
        {
            name: "defaultLogger",
            logActionConsole: false,
            logEventConsole: false
        },

        {
            name: "canvasHoverController"
        },

        {
            name: "canvasSelectController"
        },

        {
            name: "bannerController"
        },

        {
            name: "relationController",
            sourceStartAtBorder: false,
            targetEndAtBorder: false,
            showInnerRelations: true,      

            connectorColor: { r: 0, g: 0, b: 1 },
        },

        {
            name: "packageExplorerController",
            elements: [
                { type: "Package",
                  icon: "scripts/PackageExplorer/images/abap/namespace.png",
                  sortOrder: 1000
                }, 
                { type: "Class",
                  icon: "scripts/PackageExplorer/images/abap/class.png",
                  sortOrder: 1200
                }, 
                { type: "Interface",
                  icon: "scripts/PackageExplorer/images/abap/interface.png",
                  sortOrder: 1300
                },
                { type: "Method",
                  icon: "scripts/PackageExplorer/images/abap/form_fumo_method.png",
                  sortOrder: 1410
                }, 
                { type: "Field",
                  icon: "scripts/PackageExplorer/images/abap/attribute.png",
                  sortOrder: 1430
                },
            ],

            //abap: true,
            elementsSelectable: true 
        },
        {
            name: "canvasFilterController"
        },
        {   name: "legendController",
            entries: [{
                name: "Elements",
                open: true,
                icon: "scripts/Legend/images/category.png",
                entries: [{
                    name: "Package",
                    open: false,
                    icon: "scripts/Legend/images/circle_gray.png",
                },{
                    name: "Class",
                    open: false,
                    icon: "scripts/Legend/images/circle_yellow.png",
                    entries: [{
                        name: "Method",
                        icon: "scripts/Legend/images/cuboid_gray.png",
                        },{
                        name: "Field",
                        icon: "scripts/Legend/images/cylinder_black.png",
                        },
                    ]
                },{
                    name: "Interface",
                    open: false,
                    icon: "scripts/Legend/images/circle_red.png",
                    entries: [{
                        name: "Method",
                        open: false,
                        icon: "scripts/Legend/images/cuboid_gray.png",
                    },{
                        name: "Field",
                        open: false,
                        icon: "scripts/Legend/images/cylinder_black.png",
                    }]
                },{
                    name: "ReferenceBuildings",
                    open: true,
                    icon: "scripts/Legend/images/circle_width.png",
                    entries:[{
                            name: "Mountain - Building Height",
                            icon: "scripts/Legend/images/mountain.png",
                        },{
                            name: "Lake - District Area",
                            icon: "scripts/Legend/images/lake.png",
                    }]
                }]
                },{
                name: "Navigation",
                open: true,
                icon: "scripts/Legend/images/mouse.png",
                entries: [{
                        name: "Move",
                        icon: "scripts/Legend/images/left.png",
                    },{
                        name: "Zoom",
                        icon: "scripts/Legend/images/scrolling.png",
                    },{
                        name: "Rotate",
                        icon: "scripts/Legend/images/right.png",
                }]
                
            }],
		},
    ],


    uis: [
        {
            name: "UI0",
            area: {
                name: "top",
                orientation: "horizontal",
                resizable: false,
                first: {
                    size: "85px",
                    name: "banner",
                    collapsible: true,
                    controllers: [
                        {name: "bannerController" },
                        ],
                    },
                second: {
                    size: "100%",
                    collapsible: false,
                    area: {
                        orientation: "vertical",
                        name: "leftPanel",
                        first: {
                            size: "20%",
                            area: {
                                orientation: "horizontal",
                                name: "left",
                                first: {
                                    size: "55%",
                                    expanders: [
                                        {
                                            name: "packageExplorer",
                                            title: "Package Explorer",
                                            controllers: [
                                                { name: "packageExplorerController" }
                                            ]
                                        }
                                    ]
                                },
                            second: {
                                size: "45%",
                                expanders: [
                                    {
                                        name: "legend",
                                        title: "Legend",
                                        controllers: [
                                            { name: "legendController" }
                                        ],
                                    }    
                                ],
                                },
                            },
                        },
                        second: {
                            size: "80%",
                            collapsible: false,
                            name: "canvas",
                            canvas: {},
                            controllers: [
                                { name: "defaultLogger" },
                                { name: "canvasHoverController" },
                                { name: "canvasSelectController" },
                                { name: "canvasFilterController" },
                                { name: "relationController" }
                            ],
                        }
                    }
                }
            }

        }

    ]
};
