var setup = {
		
	controllers: [	

		{ 	name: 	"defaultLogger",

			logActionConsole	: false,
			logEventConsole		: false
		},		

		
		{	name: 	"canvasMarkController",
			selectionMode: "DURATION",					//TODO Constants - UP - DOWN - DURATION
			selectionDurationSeconds: 0.5,
			selectionMoveAllowed: false,
			showProgressBar: true, 
		},

		{	name: 	"canvasSelectController",
			selectionMode: "DURATION",					//TODO Constants - UP - DOWN - DURATION
			selectionDurationSeconds: 0.5,
			selectionMoveAllowed: false,
			showProgressBar: true, 
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

						{ name: "canvasMarkController" },
						{ name: "canvasSelectController" },
					],						
				}
			}
			
		}
	
	]
};