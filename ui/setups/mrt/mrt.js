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
            changeFrequency: true
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
                                            controllers: [{name: "issueExplorerController"}],
                                        }]
                                    },
                                    second: {
                                        orientation: "horizontal",
                                        area: {
                                            orientation: "horizontal", collapsible: false,
                                            first: {
                                                size: "50%",
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
