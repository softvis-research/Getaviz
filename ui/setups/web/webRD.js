var setup = {

	loadPopUp: true,

		
	controllers: [	

		{ 	name: 	"defaultLogger",

			logInfoConsole		: false,
			logActionConsole	: false,
			logEventConsole		: false
		},		
		
		{	name: 	"emailController",
			
			createHeadSection: false
		},	

		{	name: 	"canvasHoverController",			
		},	

		{	name: 	"canvasMarkController",
		},	
		
		{	name: 	"canvasSelectController" 
		},	

		{	name: 	"canvasFilterController" 
		},

		{ 	name: 	"canvasFlyToController" 
		},
	
		{	name: 	"searchController" 
		},

		{	name: 	"packageExplorerController",
		},

		
		{	name: 	"sourceCodeController",
		},
		
		{ 	name: 	"relationConnectorController",		
						
			fixPositionZ : 1,
			showInnerRelations : true,
			elementShape : "circle",					
			sourceStartAtParentBorder : true,
			targetEndAtParentBorder : false,
			createEndpoints: true,
		},

		{ 	name: 	"relationTransparencyController",
		},
			
		{ 	name: 	"relationHighlightController" 
		},	

		{	name: 	"menuController",
			menuMapping: [

				{	
					title:		"View",
					subMenu:	true,
					items:		[
						{
							title: 		"FlyTo",
							toggle: 	true,	
							eventOn: 	"canvasFlyToController.activate",
							eventOff: 	"canvasFlyToController.deactivate",									
						},

						{
							title: "Reset Visualization",
							event: "application.reset",
						},
					]
				},

				{	
					title:		"Relations",
					subMenu:	true,
					items:		[
						{
							title: 		"Relation Connectors",
							toggle: 	true,	
							eventOn: 	"relationConnectorController.activate",
							eventOff: 	"relationConnectorController.deactivate",			
						},
						{
							title: 		"Relation Transparency",
							toggle: 	true,	
							eventOn: 	"relationTransparencyController.activate",
							eventOff: 	"relationTransparencyController.deactivate",			
						},
						{
							title: 		"Relation Highlight",
							toggle: 	true,	
							eventOn: 	"relationHighlightController.activate",
							eventOff: 	"relationHighlightController.deactivate",			
						},
					]
				},

				{	
					title:		"Visualizations",
					subMenu:	true,
					items:		[
						{
							title: 	"City Original",
							link: 	true,
							url:	"Index%20mini.php?setup=web/webCity&model=City%20original%20freemind"							
						},
						{
							title: 	"City Bricks",
							link: 	true,
							url:	"Index%20mini.php?setup=web/webCity&model=City%20bricks%20freemind"							
						},						
						{
							title: 	"City Floors",
							link: 	true,
							url:	"Index%20mini.php?setup=web/webCity&model=City%20floor%20findbugs"							
						},
						{
							title: 	"Recursive Disk",
							link: 	true,
							url:	"Index%20mini.php?setup=web/webRD&model=RD%20freemind"							
						},
					]
				},

				{	
					title:		"About",
					subMenu:	true,
					items:		[
						{
							title: 	"University Leipzig",
							link: 	true,
							url:	"https://www.wifa.uni-leipzig.de/en/information-systems-institute/se/research/softwarevisualization-in-3d-and-vr.html"							
						},
						{
							title: 		"Feedback!",
							event: 		"emailController.openMailPopUp",
						},
						{
							title: 		"Impressum",
							popup:		true,
							text: 		"<b>Universität Leipzig</b><br\/\>"+
										" <br\/\>"+										
										"Wirtschaftswissenschaftliche Fakultät<br\/\>"+
										"Institut für Wirtschaftsinformatik<br\/\>"+
										"Grimmaische Straße 12<br\/\>"+
										"D - 04109 Leipzig<br\/\>"+
										" <br\/\>"+
										"<b>Dr. Richard Müller</b><br\/\>"+
										"rmueller(-a-t-)wifa.uni-leipzig.de<br\/\>",
							height: 	200,
							width:		2050,
						},
					]
				},				
			]
		},
		
	],
	
	
	

	uis: [
		
		
		{	name: "UI0",
		
			navigation: {
				//examine, walk, fly, helicopter, lookAt, turntable, game
				type:	"turntable",
				
				//turntable last 2 values - accepted values are between 0 and PI - 0.0 - 1,5 at Y-axis
				typeParams: "0.0 0.0 1.57 3.1",
				
				//speed: 10
			},	


			area: {
				name: "top",
				orientation: "horizontal",
				resizable: false,
				
				first: {			
					size: "75px",	
					
					controllers: [			
						{ name: "menuController" },
						{ name: "searchController" },
						{ name: "emailController" },
					],							
				},
				second: {
					size: "80%",	
					collapsible: false,
					

					area: {
						name: "topDown",
						orientation: "vertical",
						
						first: {			
							size: "20%"	,

							expanders: [
								{
									name: "packageExplorer",
									title: "Package Explorer",
									
									controllers: [
										{ name: "packageExplorerController" }				
									],
								},
							],			
						},
						second: {
							size: "80%",
							min: "200",
							collapsible: false,					

							area: {				
								name: "right",
								orientation: "vertical",
								
								first: {			
									size: "80%",
									collapsible: false,		

									canvas: { },
									
									controllers: [
										{ name: "defaultLogger" },												

										{ name: "canvasSelectController" },
										{ name: "canvasMarkController" },
										{ name: "canvasHoverController" },
										{ name: "canvasFilterController" },
										{ name: "canvasFlyToController" },
										{ name: "relationConnectorController" },
										{ name: "relationTransparencyController" },		
										{ name: "relationHighlightController" },											
									],
												
								},
								second: {
									size: "20%",
									min: "200",

									expanders: [
										{
											name: "CodeViewer",
											title: "CodeViewer",
											
											controllers: [
												{ name: "sourceCodeController" }				
											],
										},
									],			
								}				
							}
						}		
					}	

					
				}
			}				
			
			
		}
	
	]
};