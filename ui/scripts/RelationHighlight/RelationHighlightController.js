var relationHighlightController = function(){
		
	var relatedEntities = new Array();
	var activated = false;
	
	function initialize(config){		
		events.selected.on.subscribe(onRelationsChanged);
	}
	
	function activate(){	
		
		activated = true;
		if(relatedEntities.length != 0){
			highlightRelatedEntities();
		}
	}

	function deactivate(){	
		reset();
		activated = false;
	}
	
	function reset(){
		canvasManipulator.resetColorOfEntities(relatedEntities);
	}
	
	
	function resetColor(){
		if(relatedEntities.length == 0){	
			return;
		}

		var relatedEntitesMap = new Map();
		
		//highlight related entities
		relatedEntities.forEach(function(relatedEntity){		
			if(relatedEntity.marked){
				return;
			}
			
			if(relatedEntitesMap.has(relatedEntity)){
				return;
			}

			relatedEntitesMap.set(relatedEntity, relatedEntity);
		});

		canvasManipulator.resetColorOfEntities(Array.from(relatedEntitesMap.keys()));
	}
		
	
	function onRelationsChanged(applicationEvent) {
		
		resetColor();
		
		
		//get related entites
		var entity = applicationEvent.entities[0];	
		
		relatedEntities = new Array();
		const typeProject = ["Class", "Interface", "ParameterizableClass", "Attribute", "Method"];
		const ddicElements = ["Domain", "DataElement", "StrucElement", "Table", "TableElement", "TableType", "TableTypeElement"];
		const abapSCElements = ["Report", "Formroutine", "FunctionModule"];

		if (typeProject.includes(entity.type)) {
			if (entity.type == "Class" || entity.type == "ParameterizableClass" || entity.type == "Interface") {
				relatedEntities = relatedEntities.concat(entity.superTypes);
				relatedEntities = relatedEntities.concat(entity.subTypes);
			} else if (entity.type == "Attribute") {
				//relatedEntities = entity.accessedBy;
				relatedEntities = relatedEntities.concat(entity.accessedBy);
				relatedEntities = relatedEntities.concat(entity.typeOf);
			} else if (entity.type == "Method") {
				relatedEntities = relatedEntities.concat(entity.calls);
				relatedEntities = relatedEntities.concat(entity.calledBy);
			}
		} else if (abapSCElements.includes(entity.type)) {
			relatedEntities = relatedEntities.concat(entity.calls);
			relatedEntities = relatedEntities.concat(entity.calledBy);
		} else if (ddicElements.includes(entity.type)) {
			if (entity.type == "Domain" || entity.type == "Table") {
				relatedEntities = relatedEntities.concat(entity.typeUsedBy);
			} else {
				relatedEntities = relatedEntities.concat(entity.typeOf);
				relatedEntities = relatedEntities.concat(entity.typeUsedBy);
			}
		}


		if(relatedEntities.length == 0){
			return;
		}
		
		if(activated){
			highlightRelatedEntities();
		}
		
	}

	function highlightRelatedEntities(){
		var relatedEntitesMap = new Map();
		
		//highlight related entities
		relatedEntities.forEach(function(relatedEntity){		
			if(relatedEntity.marked){
				return;
			}
			
			if(relatedEntitesMap.has(relatedEntity)){
				return;
			}

			relatedEntitesMap.set(relatedEntity, relatedEntity);
		});
			
		canvasManipulator.changeColorOfEntities(Array.from(relatedEntitesMap.keys()), "0 0 0");			
	}

		

	return {
        initialize	: initialize,
		reset		: reset,
		activate	: activate,
		deactivate	: deactivate
    };    

}();