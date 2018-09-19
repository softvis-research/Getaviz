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
            name: 'experimentController',
            taskTextButtonTime: 0,
            taskTime: 0,
            stepOrder: [0, 10, 20, 30, 40],
            steps: [
                {
                    number: 0,
                    text: [
                        'Welcome to the Getaviz tutorial',
                        '',
                        '',
                        'Getaviz is an open source application for exploring thee-dimensional software visualizations. ' +
                        'This tutorial will explain the "Software MRT" to you, a visualization for identifying problem areas in complex software visualizations.',
                        '',
                        'Press Done to go to the next step.'
                    ]
                },
                {
                    number: 10,
                    text: [
                        'Tutorial: Visualization',
                        '',
                        'The visualization represents the structure of a software system. ' +
                        'Packages are represented by grey disks, which can contain inner packages as well. ' +
                        'Classes are visualized by smaller disks inside the package disks. ' +
                        // 'The color, ranging from gray to blue, represents how often the class has been changed. ' +
                        'The size represents the lines of code.' +
                        'The height depicts the number of issues in which the class is referenced. ' +
                        'The blue bar represents open issues, that are not security relevant. ' +
                        'The orange bar represents open security issues.' +
                        'If a class has no open issues at all, only the blue class disk is shown. ',
                        '',
                        'Press Done if you are familiar with the visualization and the legend.'

                    ]
                },
                {
                    number: 20,
                    text: [
                        'Tutorial: Navigation',
                        '',
                        '',
                        'While pressing the left mouse button, you can rotate the visualization.',
                        'While pressing the middle mouse button, you can move the visualization without rotation.',
                        'You can zoom in and zoom out by scrolling down and scrolling up.',
                        '',
                        'Press Done if you are familiar with the navigation.'
                    ]
                },
                {
                    number: 30,
                    text: [
                        'Tutorial: Package Explorer and Search',
                        '',
                        'On the left panel you find the Package Explorer which shows all packages and classes. ' +
                        //'You can hide elements. ' +
                        //'By clicking on an element, the corresponding disk is highlighted.' +
                        'Additionally, you can search for elements using the search bar.',
                        '',
                        'Press Done if you are familiar with the Package Explorer.'
                    ]
                },
                {
                    number: 40,
                    text: [
                        'Tutorial: Issue Explorer',
                        '',
                        'On the right panel you find the Issue Explorer which lists all issues from an issue tracker. ' +
                        'The issues are categorized in "open" and "closed" as well as "security relevant" and "not security relevant".' +
                        'By selecting an issue all classes related with this issue are highlighted. ',
                        '',
                        'Congratulations! You have finished the tutorial.'
                    ]
                },
                {
                    number: 50,
                    text: [
                        'Tutorial: Configuration',
                        '',
                        'On the right panel you also find some configuration option to filter which classes are shown' +
                        '' +
                        'By increasing the minimal change classes that have been rarely changed will be hidden.' +
                            '' +
                        'If you only want to see classes that have at least one open issue or at least one security issue, select the corresponding option' +
                        '',
                        'Congratulations! You have finished the tutorial.'
                    ]
                },
            ]
        },{
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
                size: "125px",
                collapsible: false,
                controllers: [
                    {name: "experimentController"}
                ]
            },
            second: {
                size: "50px",
                collapsible: false,
                area: {
                    name: "top2",
                    orientation: "horizontal",
                    resizable: false,
                    collapsible: false,
                    first: {
                        name: "top3",
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
                                                        size: "36%",
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
            },
        },
    }],
};
