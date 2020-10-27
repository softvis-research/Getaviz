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
            name: "relationController",
            sourceStartAtBorder: true,
            targetEndAtBorder: true,
            showInnerRelations: true
        },
        
        {
            name: "legendController",
            entries: [{
                name: "Elements",
                open: false,
                icon: "scripts/Legend/images/category.png",
                entries: [{
                    name: "Package",
                    open: false,
                    icon: "scripts/Legend/images/circle_gray.png",
                }, {
                    name: "Class",
                    open: false,
                    icon: "scripts/Legend/images/circle_yellow.png",
                    entries: [{
                        name: "Method",
                        icon: "scripts/Legend/images/cuboid_gray.png",
                    }, {
                        name: "Attribute",
                        icon: "scripts/Legend/images/cylinder_black.png",
                    }, {
                        name: "LocalClass",
                        icon: "scripts/Legend/images/circle_yellow_light.png",
                    }, {
                        name: "LocalInterface",
                        icon: "scripts/Legend/images/circle_red_light.png",
                    }]
                }, {
                    name: "Interface",
                    open: false,
                    icon: "scripts/Legend/images/circle_red.png",
                    entries: [{
                        name: "Method",
                        open: false,
                        icon: "scripts/Legend/images/cuboid_gray.png",
                    }, {
                        name: "Attribute",
                        open: false,
                        icon: "scripts/Legend/images/cylinder_black.png",
                    }, {
                        name: "LocalClass",
                        icon: "scripts/Legend/images/circle_yellow_light.png",
                    }, {
                        name: "LocalInterface",
                        icon: "scripts/Legend/images/circle_red_light.png",
                    }]
                }, {
                    name: "Report",
                    icon: "scripts/Legend/images/circle_blue_light.png",
                    entries: [{
                        name: "Report Building",
                        icon: "scripts/Legend/images/cuboid_blue.png",
                    }, {
                        name: "Formroutine",
                        icon: "scripts/Legend/images/cuboid_gray.png",
                    }, {
                        name: "Attribute",
                        icon: "scripts/Legend/images/cylinder_black.png",
                    }, {
                        name: "LocalClass",
                        icon: "scripts/Legend/images/circle_yellow_light.png",
                    }, {
                        name: "LocalInterface",
                        icon: "scripts/Legend/images/circle_red_light.png",
                    }]
                }, {
                    name: "Function Group",
                    icon: "scripts/Legend/images/circle_violet.png",
                    entries: [{
                        name: "Function Module",
                        icon: "scripts/Legend/images/cuboid_gray.png",
                    }, {
                        name: "Formroutine",
                        icon: "scripts/Legend/images/cuboid_gray.png",
                    }, {
                        name: "Attribute",
                        icon: "scripts/Legend/images/cylinder_black.png",
                    }, {
                        name: "LocalClass",
                        icon: "scripts/Legend/images/circle_yellow_light.png",
                    }, {
                        name: "LocalInterface",
                        icon: "scripts/Legend/images/circle_red_light.png",
                    }]
                }, {
                    name: "ReferenceBuildings",
                    open: true,
                    icon: "scripts/Legend/images/circle_width.png",
                    entries: [{
                        name: "Mountain",
                        icon: "scripts/Legend/images/mountain.png",
                    }, {
                        name: "Cloud",
                        icon: "scripts/Legend/images/cloud_black.png",
                    }, {
                        name: "Lake",
                        icon: "scripts/Legend/images/lake.png",
                    }]
                }]
            }, {
                name: "Navigation",
                open: false,
                icon: "scripts/Legend/images/mouse.png",
                entries: [{
                    name: "Move",
                    icon: "scripts/Legend/images/left.png",
                }, {
                    name: "Zoom",
                    icon: "scripts/Legend/images/scrolling.png",
                }, {
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
                    size: "10%",
                    collapsible: true,
                    controllers: [
                        { name: "bannerController" }
                    ]
                },
                second: {
                    size: "80%",
                    collapsible: false,
                    area: {
                        orientation: "vertical",
                        name: "leftPanel",
                        first: {
                            size: "10%",
                            expanders: [
                                {
                                    name: "legend",
                                    title: "Legend",
                                    controllers: [
                                        { name: "legendController" }
                                    ],
                                },
                            ]
                        },
                        second: {
                            size: "90%",
                            collapsible: false,
                            name: "canvas",
                            canvas: {},
                            controllers: [
                                { name: "defaultLogger" },
                                { name: "canvasHoverController" },
                                { name: "canvasSelectController" },
                                { name: "relationController" }
                            ],
                        }
                    }
                }
            }

        }

    ]
};
