var setup = {
		
	controllers: [	

		{ 	name: 	"defaultLogger",

			logActionConsole	: false,
			logEventConsole		: false
		},		

		
		{	name: 	"sourceCodeController",
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
				name: "left",
				orientation: "vertical",
				
				first: {			
					size: "40%"	,
																	
					controllers: [
						{ name: "sourceCodeController" }				
					],
				},
				second: {
					size: "60%",
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
							],						
						}
					}
					
				}		
			}	
			
		}
	
	]
};