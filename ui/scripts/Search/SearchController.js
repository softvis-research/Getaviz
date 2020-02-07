var searchController = (function() {
    
	const searchInputId = "searchField";
	const jQsearchInputID = "#searchField";
	
	let suggestions = null;
    
	let rootDivElement;
	
	//config parameters
	let controllerConfig = {
		active : true,
		timeout: false,
		timeoutTime: 1000
	};
	
	
	function initialize(setupConfig){	

		application.transferConfigParams(setupConfig, controllerConfig);
    }
	
	function activate(rootDiv){
		rootDivElement = rootDiv;


		const cssLink = document.createElement("link");
		cssLink.type = "text/css";
		cssLink.rel = "stylesheet";
		cssLink.href = "scripts/Search/tt.css";
		document.getElementsByTagName("head")[0].appendChild(cssLink);


						
		rootDiv.id = "search";
		rootDiv.setAttribute("ignoreTheme", "true");
        
		//create search input field
		const searchInput = document.createElement("INPUT");
		searchInput.id = searchInputId;
		searchInput.type = "text";
		
		rootDiv.appendChild(searchInput);

		$(jQsearchInputID).jqxInput( {theme: "metro", width: "100%", height: "48px", placeHolder: "Search" });

		
		events.selected.on.subscribe(onEntitySelected);

		//load search entries
		if(controllerConfig.active){		
			
			if(controllerConfig.timeout){
				setTimeout(initializeSearch, controllerConfig.timeoutTime);
			} else {
				initializeSearch();
			}
		}
		
	}	
	
	function reset(){
		$(jQsearchInputID).val('');			
	}
    
    function initializeSearch() {

        suggestions = new Bloodhound({
            datumTokenizer: function(entity) {
				const tokenizerQN = Bloodhound.tokenizers.whitespace(entity.qualifiedName);
								
				tokenizerQN.push(entity.name);					
				
				const splits = entity.qualifiedName.split(".").reverse();
				let splitString = "";
				splits.forEach(function(elementString){	
					splitString = elementString + splitString;				
					tokenizerQN.push(splitString);
					splitString = "." + splitString;			
				});
				
				return tokenizerQN;
			},						
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            local: Array.from(model.getCodeEntities().values()),
            limit: 20
        });
		suggestions.initialize();
		
        $(jQsearchInputID).typeahead(
			{
				hint: true,
				highlight: true,
				minLength: 2
			}, {
				name: "suggestions",
				displayKey: "qualifiedName",
				source: suggestions.ttAdapter(),
				templates: {
					empty: Handlebars.compile('<div class="result"><p>no entities found</p></div>'),
					suggestion: Handlebars.compile('<div class="result"><p class="name">{{name}}</p><p class="qualifiedName">{{qualifiedName}}</p></div>')
			}
        });
        $(jQsearchInputID).on("typeahead:selected", function(event, suggestion) {
            selectEntity(suggestion.id);							
        });	
		$(jQsearchInputID).on("typeahead:opened", function() {
            rootDivElement.parentElement.style.overflow = "visible";			
        });	
		$(jQsearchInputID).on("typeahead:closed", function() {
			rootDivElement.parentElement.style.overflow = "hidden";
        });
    }
    
    function selectEntity(id) {     
	
		const applicationEvent = {
			sender: searchController,
			entities: [model.getEntityById(id)]
		};
		
		events.selected.on.publish(applicationEvent);
    }
	
	function onEntitySelected(applicationEvent) {		
		
		const entity = applicationEvent.entities[0];
		$(jQsearchInputID).val(entity.qualifiedName);			
	}    
    
    return {
        initialize: 	initialize,
		activate: 		activate,
		reset: 			reset
    };
})();
