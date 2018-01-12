var setup = {
		
	controllers: [	

		{ 	name: 	"defaultLogger",

			logActionConsole	: true,
			logEventConsole		: true
		},		

		
		{	name: 	"canvasFlyToController",
			
			parentLevel: 1
		},

		{	name: 	"canvasSelectController" 
		},	

		{ 	name: 	"canvasResetViewController" 
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