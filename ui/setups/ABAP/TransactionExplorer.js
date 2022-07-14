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
                { type: "Namespace",
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
                { type: "Attribute",
                  icon: "scripts/PackageExplorer/images/abap/attribute.png",
                  sortOrder: 1430
                }, 
                { type: "FunctionGroup",
                  icon: "scripts/PackageExplorer/images/abap/fugr.png", 
                  sortOrder: 1400
                },  
                { type: "FunctionModule",
                  icon: "scripts/PackageExplorer/images/abap/form_fumo_method.png", 
                  sortOrder: 1410
                },  
                { type: "FormRoutine",
                  icon: "scripts/PackageExplorer/images/abap/form_fumo_method.png",
                  sortOrder: 1420
                }, 
                { type: "Report",
                  icon: "scripts/PackageExplorer/images/abap/report_district.png", 
                  sortOrder: 1100
                },
            ],

            //abap: true,
            elementsSelectable: true 
        },

        
        {
            name: "transactionExplorerController",
            elements: [                
                { type: "Transaction",
                  icon: "scripts/TransactionExplorer/images/abap/transaction.png",
                  sortOrder: 1000
                }, 
                { type: "Class",
                  icon: "scripts/TransactionExplorer/images/abap/class.png",
                  sortOrder: 1200
                }, 
                { type: "Interface",
                  icon: "scripts/TransactionExplorer/images/abap/interface.png",
                  sortOrder: 1300
                },
                { type: "Method",
                  icon: "scripts/TransactionExplorer/images/abap/form_fumo_method.png",
                  sortOrder: 1410
                }, 
                { type: "Attribute",
                  icon: "scripts/TransactionExplorer/images/abap/attribute.png",
                  sortOrder: 1430
                }, 
                { type: "FunctionGroup",
                  icon: "scripts/TransactionExplorer/images/abap/fugr.png", 
                  sortOrder: 1400
                },  
                { type: "FunctionModule",
                  icon: "scripts/TransactionExplorer/images/abap/form_fumo_method.png", 
                  sortOrder: 1410
                },  
                { type: "FormRoutine",
                  icon: "scripts/TransactionExplorer/images/abap/form_fumo_method.png",
                  sortOrder: 1420
                }, 
                { type: "Report",
                  icon: "scripts/TransactionExplorer/images/abap/report_district.png", 
                  sortOrder: 1100
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
                        name: "Attribute",
                        icon: "scripts/Legend/images/cylinder_black.png",
                        },{
                        name: "LocalClass",
                        icon: "scripts/Legend/images/circle_yellow_light.png",
                        },{
                        name: "LocalInterface",
                        icon: "scripts/Legend/images/circle_red_light.png",
                    }]
                },{
                    name: "Interface",
                    open: false,
                    icon: "scripts/Legend/images/circle_red.png",
                    entries: [{
                        name: "Method",
                        open: false,
                        icon: "scripts/Legend/images/cuboid_gray.png",
                    },{
                        name: "Attribute",
                        open: false,
                        icon: "scripts/Legend/images/cylinder_black.png",
                    },{
                        name: "LocalClass",
                        icon: "scripts/Legend/images/circle_yellow_light.png",
                    },{
                        name: "LocalInterface",
                        icon: "scripts/Legend/images/circle_red_light.png",
                    }]
                },{
                    name: "Report",
                    open: false, 
                    icon: "scripts/Legend/images/circle_blue_light.png",
                    entries: [{
                        name: "Report Building",
                        icon: "scripts/Legend/images/cuboid_blue.png",
                    },{
                        name: "Formroutine",
                        icon: "scripts/Legend/images/cuboid_gray.png",
                    },{
                        name: "Attribute",
                        icon: "scripts/Legend/images/cylinder_black.png",
                    },{
                        name: "LocalClass",
                        icon: "scripts/Legend/images/circle_yellow_light.png",
                    },{
                        name: "LocalInterface",
                        icon: "scripts/Legend/images/circle_red_light.png",
                    }]
                },{
                    name: "Function Group",
                    open: false, 
                    icon: "scripts/Legend/images/circle_violet.png",
                    entries: [{
                        name: "Function Module",
                        icon: "scripts/Legend/images/cuboid_gray.png",
                    },{
                        name: "Formroutine",
                        icon: "scripts/Legend/images/cuboid_gray.png",
                    },{
                        name: "Attribute",
                        icon: "scripts/Legend/images/cylinder_black.png",
                    },{
                        name: "LocalClass",
                        icon: "scripts/Legend/images/circle_yellow_light.png",
                    },{
                        name: "LocalInterface",
                        icon: "scripts/Legend/images/circle_red_light.png",
                    }]
                },{
                    name: "Transactions",
                    open: false,
                    icon: "scripts/Legend/images/circle_green_light.png",
                    entries:[{
                            name: "Transaction Building",
                            icon: "scripts/Legend/images/cuboid_gray.png",
                    }]
                },{
                    name: "ReferenceBuildings",
                    open: true,
                    icon: "scripts/Legend/images/circle_width.png",
                    entries:[{
                            name: "Mountain - Building Height",
                            icon: "scripts/Legend/images/mountain.png",
                        },{
                            name: "Cloud - Migration Findings",
                            icon: "scripts/Legend/images/cloud_black.png",
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
                                    size: "70%",
                                    area: {
                                        orientation: "horizontal",
                                        name: "explorer",
                                        first: {
                                            size: "45%",
                                            expanders: [
                                                {
                                                    name: "transactionExplorer",
                                                    title: "Transaction Explorer",
                                                    
                                                    controllers: [
                                                        { name: "transactionExplorerController" }
                                                    ]
                                                }
                                            ]
                                        },
                                        second: {
                                            size: "45%",
                                            expanders: [
                                                {
                                                    name: "packageExplorer",
                                                    title: "Package Explorer",
                                                    controllers: [
                                                        { name: "packageExplorerController" }
                                                    ]
                                                }
                                            ]
                                        }
                                    }
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
