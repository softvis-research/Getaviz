var setup = {

    controllers: [
        { 	name: 	"defaultLogger",
            logActionConsole	: false,
            logEventConsole		: false
        },
        
        {
            name: "canvasHoverController"
        },
        
        {   name: 	"legendController",
            entries: [{
                name: "Package",
                icon: "scripts/Legend/images/circle_gray.png",
            }, {
                name: "Class",
                icon: "scripts/Legend/images/circle_yellow.png",
                entries: [{
                    name: "Method",
                    icon: "scripts/Legend/images/cuboid_gray.png",
                },{
                    name: "Attribute",
                    icon: "scripts/Legend/images/cylinder_black.png",
                }]
            },{
                name: "Interface",
                icon: "scripts/Legend/images/circle_red.png",
                entries: [{
                    name: "Method",
                    icon: "scripts/Legend/images/cuboid_gray.png",
                },{
                    name: "Attribute",
                    icon: "scripts/Legend/images/cylinder_black.png",
                }]
            },{
                name: "Report",
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
                }]
            },{
                name: "Function Group",
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
                }]
            },{
                name: "Mountain",
                icon: "scripts/Legend/images/mountain.png",
            },{
                name: "Cloud",
                icon: "scripts/Legend/images/cloud_black.png",
            },{
                name: "Lake",
                icon: "scripts/Legend/images/lake.png",
            }],
		},
    ],


    uis: [


        {	name: "UI0",
            area: {
                name: "top",
                orientation: "horizontal",
                resizable: false,
                first: {
                    size: "10%",
                    collapsible: false,
                    controllers: [
                        { }
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
							],
                        }
                    }
				}
            }

        }

    ]
};
