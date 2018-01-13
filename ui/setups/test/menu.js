var setup = {
		
	controllers: [	

		{ 	name: 	"defaultLogger",

			logInfoConsole		: true,		
			logActionConsole	: false,
			logEventConsole		: false
		},		

		
		{	name: 	"canvasFlyToController",
		},
	
		{	name: 	"canvasSelectController" 
		},	
		
		{	name: 	"menuController",
			menuMapping: [
				
				{	
					title:		"FlyTo",
					subMenu:	true,
					items:		[
						{
							title: 		"FlyTo",
							checkBox: 	true,	
							eventOn: 	"canvasFlyToController.activate",
							eventOff: 	"canvasFlyToController.deactivate",									
						},

						{
							title: 		"FlyTo Toggle",
							toggle: 	true,	
							toggled:	false,
							eventOn: 	"canvasFlyToController.activate",
							eventOff: 	"canvasFlyToController.deactivate",									
						},
					]

				},				
				

				{
					title: 	"Link To The Past",
					link: 	true,
					url:	"https://de.wikipedia.org/wiki/The_Legend_of_Zelda:_A_Link_to_the_Past"
							
				},

				{
					title: "Reset",
					event: "application.reset",	//TODO eigenes Event für ButtonClicks einführen			
				},

				{
					title: 	"PopUPPPP",
					popup:	true,
					text: 	"hier kommt das Popup!",
					height: 150,
					width:	150,
				},


				
			]
		},
	

		{ 	name: 	"canvasResetViewController", 

			button: false
		},
		
	],
		

	uis: [
		
		
		{	name: "UI0",
		
			navigation: {
				//examine, walk, fly, helicopter, lookAt, turntable, game
				type:	"examine",
				//speed: 10
			},	
					
		
							
			area: {
				name: "top",
				orientation: "horizontal",
				
				first: {			
					size: "10%",	
					
					controllers: [					
						{ name: "canvasResetViewController" },
						{ name: "menuController" },
					],							
				},
				second: {
					size: "90%",	
					collapsible: false,
					
					
							
					canvas: { },
					
					controllers: [
						{ name: "defaultLogger" },												

						{ name: "canvasFlyToController" },
						{ name: "canvasSelectController" },
					],						
				}
			}
			
		}
	
	]
};