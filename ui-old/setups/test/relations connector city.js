var setup = {
		
	controllers: [	

		{ 	name: 	"defaultLogger",

			logActionConsole	: false,
			logEventConsole		: false
		},		
		
		
		{	name: 	"canvasSelectController",

			setCenterOfRotation : true,
		},	
	

		{ 	name: 	"canvasResetViewController" 
		},

		{ 	name: 	"relationConnectorController",			
			sourceStartAtBorder: true,
			targetEndAtBorder: true,
		},

		{ 	name: 	"relationTransparencyController" 
		},
					
		{ 	name: 	"relationHighlightController" 
		},	

		{ 	name: 	"canvasHoverController" 
		},	
		
	],
		

	uis: [
		
		
		{	name: "UI0",

			
			navigation: {
				//examine, walk, fly, helicopter, lookAt, turntable, game
				type:	"examine",
				//typeParams: '0.0 0.0 0.2 1.4',		//Turntable seems not to work with 1.7 and dynamic adding
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

						{ name: "canvasHoverController" },
						{ name: "canvasSelectController" },
						{ name: "relationConnectorController" },	
						{ name: "relationHighlightController" },
						{ name: "relationTransparencyController" },
								
					],						
				}
			}				
				
		}
	
	]
};