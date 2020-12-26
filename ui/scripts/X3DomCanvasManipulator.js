var canvasManipulator = (function() {
    
	let colors = {
		darkred: "darkred",
		black: "black",
		orange: "orange",
		darkorange: "darkorange"
	};

	let x3domRuntime;
	let viewarea;
	let viewpoint;
	let initialCenterOfRotation;


	function initialize(){

		x3domRuntime = document.getElementById('x3dElement').runtime;	
		viewarea = x3domRuntime.canvas.doc._viewarea;	
		viewpoint = viewarea._scene.getViewpoint();

		initialCenterOfRotation = viewpoint.getCenterOfRotation();
	}

	function reset(){
		document.getElementById("x3dElement").runtime.showAll("negZ");
		viewpoint.setCenterOfRotation(initialCenterOfRotation);
	}
	


	//manipulate
	function highlightEntities(entities, color){

		const entityIds = [];
		entities.forEach(function(entity){
			entityIds.push(entity.id);
		});

		const parts = multiPart.getParts(entityIds);
		if(parts === null){
			events.log.error.publish({ text: "CanvasManipualtor - highlightEntities - parts for entityIds not found"});
			return;
		}

		parts.unhighlight();
		parts.highlight(color);
	}


	function unhighlightEntities(entities){

		const entityIds = [];
		entities.forEach(function(entity){
			entityIds.push(entity.id);
		});

		const parts = multiPart.getParts(entityIds);
		if(parts === null){
			events.log.error.publish({ text: "CanvasManipualtor - unhighlightEntities - parts for entityIds not found"});
			return;
		}

		parts.unhighlight();
	}



	function changeTransparencyOfEntities(entities, value){
		let entityIds = [];
		entities.forEach(function(entity){			
			const part = multiPart.getParts([entity.id]);
			if(part == null){
				return;
			}		
			entity.oldTransparency = part.getTransparency();

			entityIds.push(entity.id);
		});

		const parts = multiPart.getParts(entityIds);
		if(parts === null){
			events.log.error.publish({ text: "CanvasManipualtor - changeTransparencyOfEntities - parts for entityIds not found"});
			return;
		}
		setTransparency(parts, value);
	}

	function resetTransparencyOfEntities(entities){

		const oldTransparencyMap = new Map();

		entities.forEach(function(entity){
			
			if(!entity.oldTransparency){
				return;
			}
			const oldTransparency = entity.oldTransparency;
			
			if(oldTransparencyMap.has(oldTransparency)){
				oldTransparencyMap.get(oldTransparency).push(entity.id);
			} else {
				oldTransparencyMap.set(oldTransparency, [entity.id]);
			}
		});

		oldTransparencyMap.forEach(function(entityIds, oldTransparency, map){
			const parts = multiPart.getParts(entityIds);
			if(parts === null){
				events.log.error.publish({ text: "CanvasManipualtor - resetTransparencyOfEntities - parts for entityIds not found"});
				return;
			}	
			setTransparency(parts, oldTransparency);
		});
	}



	function changeColorOfEntities(entities, color){
		const entityIds = [];
		entities.forEach(function(entity){
			const part = multiPart.getParts([entity.id]);
			if(part == null){
				return;
			}	
			if(!entity.oldColor){
				entity.oldColor = part.getDiffuseColor().toString();
			}
			entityIds.push(entity.id);
        });

		const parts = multiPart.getParts(entityIds);
		if(parts === null){
			events.log.error.publish({ text: "CanvasManipualtor - changeColorOfEntities - parts for entityIds not found"});
			return;
		}
		setColor(parts, color);
	}


	function resetColorOfEntities(entities){
		//sort each entity by its old color for performance
		const oldColorMap = new Map();
		entities.forEach(function(entity){
            if(entity.oldColor == null){
				return;
			}
			const oldColor = entity.oldColor;
			
			if(oldColorMap.has(oldColor)){
				oldColorMap.get(oldColor).push(entity.id);
			} else {
			    oldColorMap.set(oldColor, [entity.id]);
			}

			entity.oldColor = null;
		});

		oldColorMap.forEach(function(entitiyIds, oldColor, map){
			const parts = multiPart.getParts(entitiyIds);
			if(parts === undefined){
				events.log.error.publish({ text: "CanvasManipualtor - resetColorOfEntities - parts for entityIds not found"});
				return;
			}
			setColor(parts, oldColor);
		});
	}

	function hideEntities(entities){
		const entityIds = new Array();
		entities.forEach(function(entity){
			entityIds.push(entity.id);
		});

		const parts = multiPart.getParts(entityIds);
		if(parts === undefined){
			events.log.error.publish({ text: "CanvasManipualtor - hideEntities - parts for entityIds not found"});
			return;
		}
		setVisibility(parts, false);
	}


	function showEntities(entities){
		const entityIds = new Array();
		entities.forEach(function(entity){
			entityIds.push(entity.id);
		});

		const parts = multiPart.getParts(entityIds);
		if(parts === undefined){
			events.log.error.publish({ text: "CanvasManipualtor - showEntities - parts for entityIds not found"});
			return;
		}
		setVisibility(parts, true);
	}
	
	

	function flyToEntity(entity){
	    const part = getPart(entity);
		if (part === undefined) {
			events.log.error.publish({ text: "CanvasManipualtor - resetColflyToEntityorOfEntities - parts for entityIds not found"});
			return;
		}
		
		part.fit();		
	}
	


	function addElement(element){
		const addedElements = document.getElementById("addedElements");
		addedElements.appendChild(element);
	}
	
	function removeElement(element){
		const addedElements = document.getElementById("addedElements");
		addedElements.removeChild(element);
	}


	
	//From X3dom coding
	//x3dom.DefaultNavigation.prototype.onDoubleClick = function (view, x, y)
	
	function setCenterOfRotation(entity, setFocus){
		
		const centerOfPart = getCenterOfEntity(entity);

		viewpoint.setCenterOfRotation(centerOfPart);
		if(setFocus){
			let mat = viewarea.getViewMatrix().inverse();

			let from = mat.e3();
			const at = viewarea._pick;
			const up = mat.e1();

			const norm = mat.e0().cross(up).normalize();
			// get distance between look-at point and viewing plane
			const dist = norm.dot(viewarea._pick.subtract(from));
			
			from = at.addScaled(norm, -dist);
			mat = x3dom.fields.SFMatrix4f.lookAt(from, at, up);

			viewarea.animateTo(mat.inverse(), viewpoint);
		}
	}

	
	function getCenterOfEntity(entity){

		const entityPart = getPart(entity);
		const volumeOfPart = entityPart.getVolume();
		const centerOfPart = volumeOfPart.center;

		return centerOfPart;
	}


	//Helper
	function getPart(entity){
		if (entity.part === undefined){
			const part = multiPart.getParts([entity.id]);
			entity.part = part;
		}
		return entity.part;
	}
	
	function setColor(parts, color){
				
		//Fehler in Methode setDiffuseColor bei nur einem übergebenem Part
		//->Heilung durch Dopplung	
		if(parts.ids.length === 1){
			parts.ids.push(parts.ids[0]);	
		}
		parts.setDiffuseColor(color);
	}
	
	function setTransparency(parts, value) {
				
		//Fehler in Methode setTransparency bei nur einem übergebenem Part
		//->Heilung durch Dopplung
		if(parts.ids.length === 1){
			parts.ids.push(parts.ids[0]);	
		}
		parts.setTransparency(value);
    }


	function setVisibility(parts, visibility) {
		//Fehler in Methode setVisibility bei nur einem übergebenem Part
		//->Heilung durch Dopplung
		if(parts.ids.length === 1){
			parts.ids.push(parts.ids[0]);	
		}
		parts.setVisibility(visibility);
	}

	function getElementIds(){
		return multiPart.getIdList();
	}
	
	
	
	return {
		initialize						: initialize,
		reset							: reset,
        colors							: colors,

		highlightEntities 				: highlightEntities,
		unhighlightEntities				: unhighlightEntities,

		changeTransparencyOfEntities	: changeTransparencyOfEntities,
		resetTransparencyOfEntities		: resetTransparencyOfEntities,	

		changeColorOfEntities 			: changeColorOfEntities,
		resetColorOfEntities			: resetColorOfEntities,
	
		hideEntities					: hideEntities,
		showEntities					: showEntities,

		flyToEntity						: flyToEntity,

		addElement						: addElement,
		removeElement					: removeElement,


		setCenterOfRotation				: setCenterOfRotation,
		getCenterOfEntity				: getCenterOfEntity,
		
		getElementIds					: getElementIds,
    };    		 
	    
})();