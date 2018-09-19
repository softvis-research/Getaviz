var setup = {

	controllers: [

		{ 	name: 	"defaultLogger",

			logActionConsole	: false,
			logActionEventConsole	:  true,
			logEventConsole		: false,
			logInfoConsole : true,
            logWarningConsole: true,
            logErrorConsole: true,
			logErrorEventConsole: true
		},
		{	name: 	"canvasHoverController",
			hoverColor: "orangered",
			showVersion: false,
			showIssues: true
		},
		{	name: 	"canvasMarkController",
            markingColor: "orangered",
			//TODO pars by config
			eventHandler: [{
				handler : "onEntityMarked",
				event 	: events.marked.on
			},{
				handler : "onEntityUnmarked",
				event 	: events.marked.off
			}],

			//as function
			actionEventCoupling:	function(controller){
				actionController.actions.mouse.key[2].up.subscribe(function(actionEvent){
					if(!actionEvent.entity){
						return;
					}

					if(!actionController.actions.keyboard.key[32].pressed){
						return;
					}

					var entity = actionEvent.entity;

					var applicationEvent = {
						sender		: canvasMarkController,
						entities	: [entity]
					};

					if(entity.marked){
						events.marked.off.publish(applicationEvent);
					} else {
						events.marked.on.publish(applicationEvent);
					}
				});

				events.marked.on.subscribe(controller.onEntityMarked);
				events.marked.off.subscribe(controller.onEntityUnmarked);
			},

			//as config
			//TODO pars by actionEventMapper
			actionEventCouplingConfig: [{
				event		: events.marked.off,

				constraints	: [{
					action : actionController.actions.mouse.key[2].up
				}, {
					entity : [{
						marked	: true
					}]
				}, {
					pressed : actionController.actions.keyboard.key[32].pressed
				}]
			}, {
				event		: events.marked.on,

				constraints	: [{
					action : actionController.actions.mouse.key[2].up
				}, {
					entity : [{
						marked	: false
					}]
				}, {
					pressed : actionController.actions.keyboard.key[32].pressed
				}],
			}],

		},
		{	name: 	"canvasFilterController"
		},
		{ 	name: 	"canvasFlyToController" ,
			targetType: "Namespace"
		},
		{ 	name: 	"canvasResetViewController"
		},
        {
            name: "canvasSelectController",
            color: "orangered"
        },
		{	name: 	"searchController"
		},
        {	name: 	"packageExplorerController",
            projectIcon: "setups/mrt/project.png",
            typeIcon: "setups/mrt/class.png"
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
                        'The color, ranging from gray to blue, represents how often the class has been changed. ' +
                        'The size represents the lines of code.' +
                        'The height depicts the number of issues in which the class is referenced. ' +
                        'So, high blue classes are often changed and error-prone. ',
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
                        'With a double click you can centre the clicked position.',
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
                        'You can hide elements. ' +
                        'By clicking on an element, the corresponding disk is highlighted.' +
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
            ]
        },
        {
            name: "legendController",
            entries: [{
                name: "Project",
                icon: "setups/mrt/project.png"
            }, {
                name: "Package",
                icon: "setups/mrt/package.png"
            }, {
                name: "Class",
                icon: "setups/mrt/class.png",
                entries: [{
                    name: "Number of related issues",
                    icon: "setups/mrt/issues.png"
                }, {
                    name: "Change Frequency",
                    icon: "setups/mrt/frequency.png"
                },{
                    name: "Selection",
                    icon: "setups/mrt/selection.png"
                }]
            }, {
                name: "Navigation",
                icon: "setups/mrt/navigation.png",
                entries: [
                    {
                        name: "Rotate",
                        icon: "setups/mrt/left.png"
                    }, {
                        name: "Center",
                        icon: "setups/mrt/double.png"
                    }, {
                        name: "Move",
                        icon: "setups/mrt/middle.png"
                    }, {
                        name: "Zoom",
                        icon: "setups/mrt/zoom.png"
                    }]
            }
            ],
        }],

	uis: [{
		name: "MRT",
		navigation: { type:	"examine" },
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
                size: "25px",
                collapsible: false,
                area: {
                    name: "top2",
                    orientation: "horizontal",
                    first: {
                        size: "50px",
                        controllers: [{name: "searchController"}]
                    },
                    second: {
                        collapsible: false,
                        size: "80%",
                        area: {
                            //	orientation: "vertical",
                            name: "topDown",
                            first: {
                                size: "20%",
                                collapsible: false,
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
                                            {name: "canvasMarkController"},
                                            {name: "canvasFlyToController"},
                                            {name: "canvasSelectController"}
                                        ]
                                    },
                                    second: {
                                        size: "100%",
                                        collapsible: false,
                                        area: {
                                            orientation: "horizontal",
                                            first: {
                                                size: "65%",
                                                collapsible: false,
                                                expanders: [{
                                                    name: "issueExplorer",
                                                    title: "Issue Explorer",
                                                    controllers: [{name: "issueExplorerController"}]
                                                }],
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
