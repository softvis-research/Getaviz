var setup = {
		
	controllers: [	

		{ 	name: 	"defaultLogger",

			logActionConsole	: false,
			logEventConsole		: false
		},		

		
		{	name: 	"canvasMarkController",
		},

		{	name: 	"canvasSelectController" 
		},	

		{ 	name: 	"canvasResetViewController" 
		},
	
		{ 	name: 	"emailController" 
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
				resizable: false,
				
				first: {			
					size: "175px",	
					
					controllers: [											
						{ name: "emailController" },
						{ name: "canvasResetViewController" },
					],							
				},
				second: {
					size: "80%",	
					collapsible: false,
					
					
							
					canvas: { },
					
					controllers: [
						{ name: "defaultLogger" },												

						{ name: "canvasMarkController" },
						{ name: "canvasSelectController" },
					],						
				}
			}
			
		}
	
	]
};