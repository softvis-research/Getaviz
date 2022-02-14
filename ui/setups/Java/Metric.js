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
            name: "metricController"
        },

        {
            name: "bannerController"
        },

        {
            name: "legendController",
            entries: [{
                name: "Elements",
                open: true,
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
                        name: "Field",
                        icon: "scripts/Legend/images/cylinder_black.png",
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
                        name: "Field",
                        open: false,
                        icon: "scripts/Legend/images/cylinder_black.png",
                    }]
                }, {
                    name: "ReferenceBuildings",
                    open: true,
                    icon: "scripts/Legend/images/circle_width.png",
                    entries: [{
                        name: "Mountain",
                        icon: "scripts/Legend/images/mountain.png",
                    }, {
                        name: "Lake",
                        icon: "scripts/Legend/images/lake.png",
                    }]
                }]
            }, {
                name: "Navigation",
                open: true,
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
                    size: "85px",
                    name: "banner",
                    collapsible: true,
                    controllers: [
                        { name: "bannerController" },
                    ],
                },
                second: {
                    size: "100%",
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
                            area: {
                                orientation: "horizontal",
                                name: "rightPanel",
                                first: {
                                    size: "80%",
                                    collapsible: false,
                                    name: "canvas",
                                    canvas: {},
                                    controllers: [
                                        { name: "defaultLogger" },
                                        { name: "canvasHoverController" },
                                        { name: "canvasSelectController" }
                                    ]
                                },
                                second: {
                                    size: "20%",
                                    name: "metric",
                                    controllers: [
                                        { name: "metricController" }
                                    ]
                                }
                            }
                        }
                    }
                }
            }

        }

    ]
};
