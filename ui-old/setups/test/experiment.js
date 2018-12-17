var setup = {
	
	controllers: [
		
		//experimentController
		{	name: 		"experimentController",
		
			taskTextButtonTime: 10,
			taskTime: 5,
		
			stepOrder:	[ 	10, 20 ],
						
			steps:		[				
							{ 	number:	10,
								
								text: 	[
										"Willkommen beim Experiment LazyDog.",
										"Im Folgenden Verlauf wird die Interaktion mit einer Visualisierung eines Softwaresystems untersucht.",
										"Dazu lösen Sie 6 Aufgaben, bei denen die Zeit und die Fehlerrate gemessen wird.",
										"Zunächst starten Sie jedoch mit einem Tutorial.",
										"Betrachten Sie die Visualisierung kurz im Überblick und betätigen Sie anschließend die \"Aufgabe abgeschlossen!\"-Schaltfläche oben rechts zwei mal."
								],		

								ui: 	"UI0"
							},
							
							{ 	number:	20,
								
								text: 	[
										"Sie sehen die Darstellung des Softwaresystems als Stadt(City).",
										"Klassen sind als dunkelblaue Gebäude in hellgrauen Stadtteilen, den Paketen, dargestellt.",
										"Jedes hellblaue Stockwerk ist eine Methode und jeder gelber Schornstein auf dem Dach des Gebäudes ein Attribut.",
										"Beim überfahren der Elemente mit der Maus wird der Name in einem Tooltip angezeigt.",
										"Versuchen Sie sich in der Visualisierung zu bewegen und beenden Sie die Aufgabe wieder über die Schaltfläche."
								],		

								ui: 	"UI1",

								entities : [
									"edu_umd_cs_findbugs_visitclass_PreorderVisitor_className",
									"edu_umd_cs_findbugs_visitclass_PreorderVisitor_dottedSuperclassName",
									"edu_umd_cs_findbugs_visitclass_PreorderVisitor_superclassName",
									"edu_umd_cs_findbugs_visitclass_PreorderVisitor_sourceFile",
									"edu_umd_cs_findbugs_visitclass_PreorderVisitor_dottedClassName",
									"edu_umd_cs_findbugs_visitclass_PreorderVisitor_thisClassInfo",
									"edu_umd_cs_findbugs_visitclass_PreorderVisitor_packageName",
									"edu_umd_cs_findbugs_FindBugs_isNoAnalysis___",
									"edu_umd_cs_findbugs_classfile_DescriptorFactory_createClassDescriptor__java_lang_String_____"
								]
							},
			]
			
		},

		{ 	name: 	"defaultLogger",

			logActionConsole	: false,
			logEventConsole		: false
		},		

		
		{	name: 	"canvasHoverController",
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
				type:	"none",
				//speed: 10
			},	
					
		
							
			area: {
				name: "top",
				orientation: "horizontal",
				
				first: {			
					size: "200px",	
					
					controllers: [					
						{ name: "experimentController" },
						{ name: "canvasResetViewController" },
					],							
				},
				second: {
					size: "90%",	
					collapsible: false,
					
					
							
					canvas: { },
					
					controllers: [
						
						{ name: "defaultLogger" },											

					],						
				}
			}
			
		},

		{	name: "UI1",
		
			navigation: {
				//examine, walk, fly, helicopter, lookAt, turntable, game
				type:	"examine",
				//speed: 10
			},	
					
		
							
			area: {
				name: "top",
				orientation: "horizontal",
				
				first: {			
					size: "200px",	
					
					controllers: [	
						{ name: "experimentController" },				
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
					],						
				}
			}
			
		}
	
	]
};