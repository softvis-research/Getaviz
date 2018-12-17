var setup = {

    controllers: [

        { 	name: 	"defaultLogger",

            logActionConsole	: false,
            logActionEventConsole	:  true,
            logEventConsole		: false,
            logInfoConsole : true,
            logWarningConsole: true,
            logErrorConsole: true,
        },
        {
            name: "configurationController",
            changeFrequency: true,
            issues: true
        },
        {	name: 	"canvasHoverController",
            hoverColor: "#833f88",
            showVersion: false,
            showIssues: true
        },
        {	name: 	"canvasFilterController"
        },
        { 	name: 	"canvasFlyToController" ,
            targetType: "Namespace"
        },
        { 	name: 	"canvasResetViewController"
        },
        {	name: 	"searchController"
        },
        {	name: 	"packageExplorerController",
            projectIcon: "scripts/PackageExplorer/images/circle_black.png",
            typeIcon: "scripts/PackageExplorer/images/type.png",
            elementsSelectable: false
        },
        {	name: 	"issueExplorerController",
        }, {
            name: "legendController",
            entries: [{
                name: "Project",
                icon: "blackCircle"
            }, {
                name: "Package",
                icon: "grayCircle"
            }, {
                name: "Class",
                icon: "purpleCircle",
                entries: [{
                    name: "Lines of Code",
                    icon: "circleWidth"
                },{
                    name: "Number of open issues",
                    icon: "blueCylinderHeight"
                },{
                    name: "Number of open security issues",
                    icon: "orangeCylinderHeight"
                }
                ]
            }, {
                name: "Navigation",
                icon: "navigation",
                entries: [
                    {
                        name: "Rotate",
                        icon: "leftMouseButton"
                    }, {
                        name: "Move",
                        icon: "midMouseButton"
                    }, {
                        name: "Zoom",
                        icon: "scrolling"
                    }]
            }
            ],
        }],

    uis: [{
        name: "MRT",
       // navigation: { type:	"examine" },
        navigation: {
            //examine, walk, fly, helicopter, lookAt, turntable, game
            type: "turntable",

            //turntable last 2 values - accepted values are between 0 and PI - 0.0 - 1,5 at Y-axis
            typeParams: "0.0 0.0 0.001 1.5",

            //speed: 10
        },
        area: {
            name: "top",
            orientation: "horizontal",
            resizable: false,
            collapsible: false,
            first: {
                name: "top",
                size: "50px",
                collapsible: false,
                controllers: [{name: "searchController"}]
            },
            second: {
                size: "80%",
                collapsible: false,
                area: {
                   // orientation: "vertical",
                    name: "topDown",
                    size: "20%",
                    first: {
                        size: "20%",
                        expanders: [{
                            name: "packageExplorer",
                            title: "Package Explorer",
                            controllers: [{name: "packageExplorerController"}]
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
                                canvas: {},
                                collapsible: false,
                                controllers: [
                                    {name: "defaultLogger"},
                                    {name: "canvasHoverController"},
                                    {name: "canvasFilterController"},
                                    {name: "canvasFlyToController"}
                                ]
                            },
                            second: {
                                size: "100%",
                                collapsible: false,
                                area: {
                                    orientation: "horizontal",
                                    first: {
                                        //size: "33%",
                                        collapsible: false,
                                        expanders: [{
                                            name: "issueExplorer",
                                            title: "Issue Explorer",
                                            controllers: [{name: "issueExplorerController"}],
                                        }]
                                    },
                                    second: {
                                        orientation: "horizontal",
                                        area: {
                                            orientation: "horizontal",
                                            collapsible: false,
                                            first: {
                                                size: "45%",
                                                collapsible: false,
                                                expanders: [{
                                                    name: "Configuration",
                                                    title: "Configuration",
                                                    controllers: [{name: "configurationController"}],
                                                }]
                                            },
                                            second: {
                                                orientation: "horizontal",
                                                expanders: [{
                                                    name: "legend",
                                                    title: "Legend",
                                                    controllers: [{name: "legendController"}]
                                                }],
                                            },
                                        },
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },
    }],
};
