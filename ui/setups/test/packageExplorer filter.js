var setup = {
		
	controllers: [	

		{ 	name: 	"defaultLogger",

			logActionConsole	: false,
			logEventConsole		: false
		},		

		
		{	name: 	"packageExplorerController",
		},

		{	name: 	"canvasSelectController" 
		},	

		{	name: 	"canvasFilterController" 
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
				name: "left",
				orientation: "vertical",
				
				first: {			
					size: "20%"	,
									
					controllers: [
						{ name: "packageExplorerController" }				
					],
												
				},
				second: {
					size: "80%",
					min: "200",
					collapsible: false,
					
										
							
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

								{ name: "canvasSelectController" },
								{ name: "canvasFilterController" },
							],						
						}
					}
					
				}		
			}	
			
		}
	
	]
};