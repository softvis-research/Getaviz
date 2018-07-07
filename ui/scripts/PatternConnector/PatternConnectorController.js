var patternConnectorController = function(){

	var callingEntities = [];
    var visitedEntities = [];

	var connectors = new Map();
	var relations = [];

	var loadedMin		= new Map();
	var loadedMax		= new Map();
	var loadedPositions = new Map();
	var loadedDistances = new Map();
    var meta = new Map();
	
	var lastApplicationEvent = null;

	var activated = false;
    var finished = false;
	
	var minWeight = 0.5;
    var index = 0;
	var d3Nodes = [];

	//config parameters
	var controllerConfig = {
		fixPositionZ : false,
		showInnerRelations : false,
		elementShape : "",					//circle, square
		sourceStartAtParentBorder : false,
		targetEndAtParentBorder : false,
		sourceStartAtBorder: false,
		targetEndAtBorder: false,
		createEndpoints : true,
		bundledEdges: false,
	};

	function initialize(setupConfig){

		//Todo Invarianten der Config prüfen
		application.transferConfigParams(setupConfig, controllerConfig);

		loadPositionData(multipartJsonUrl);

		events.componentSelected.on.subscribe(onComponentSelected);
		events.antipattern.on.subscribe(onAntipatternSelected);
		events.versionSelected.off.subscribe(offVersionSelected);
		events.config.weight.subscribe(onWeightChanged);
		events.config.bundledEdges.subscribe(onBundledEdgesChanged);
		events.config.innerClasses.subscribe(onInnerClassesChanged);
		events.selected.on.subscribe(onRelationsChanged);
	}

	function activate(){
		activated = true;
        createTooltipContainer();
		if(callingEntities.length != 0){
			createRelatedConnections();
		}
	}

    function createTooltipContainer(){

        var canvas = document.getElementById("canvas");

        var tooltipDivElement = document.createElement("DIV");
        tooltipDivElement.id = "tooltip";

        var namePElement = document.createElement("P");
        namePElement.id = "tooltipName";
        tooltipDivElement.appendChild(namePElement);

        var qualifiedNamePElement = document.createElement("P");
        qualifiedNamePElement.id = "tooltipQualifiedName";
        tooltipDivElement.appendChild(qualifiedNamePElement);

        canvas.appendChild(tooltipDivElement);
    }

	function deactivate(){
		reset();
		activated = false;
	}

	function reset(){
		removeAllConnectors();
	}
	
	function onRelationsChanged(applicationEvent) {
		var relatedEntity = applicationEvent.entities[0];
		var version = applicationEvent.entities[0].version;
		var myEntities = [];
        removeAllConnectors();
		if(lastApplicationEvent == null) {
			if(finished) {
				callingEntities = [];
				visitedEntities = [];
			}
				// make all connectors transparent
				connectors.forEach(function(version, connector){
					var collection = connector.getElementsByTagName("material");
					for (var i = 0; i < collection.length; i++) {
						collection[i].setAttribute("transparency", "0.85");
						collection[i].setAttribute("emmissivecolor", "1 1 1");
						collection[i].setAttribute("emissiveColor", "1 1 1");
						collection[i].setAttribute("specularcolor", "1 1 1");
					}
				});
				addReachesAndReachedBy(relatedEntity);
				callingEntities.forEach(function(pair){
					if(!myEntities.includes(pair[0])) {
						myEntities.push(pair[0]);
					}
					if(!myEntities.includes(pair[1])) {
						myEntities.push(pair[1]);
					}
				});
            finished = true;
            if(activated){
					if(controllerConfig.bundledEdges) {
						forceLayout(version, myEntities);
					} else {
						createRelatedConnections(minWeight);
					}
				}
		} else {
			if(lastApplicationEvent.entities[0].id === applicationEvent.entities[0].component) {
				if(finished) {
					callingEntities = [];
					visitedEntities = [];
				}
				// make all connectors transparent
				connectors.forEach(function(version, connector){
					var collection = connector.getElementsByTagName("material");
					for (var i = 0; i < collection.length; i++) {
						collection[i].setAttribute("transparency", "0.85");
						collection[i].setAttribute("emmissivecolor", "1 1 1");
						collection[i].setAttribute("emissiveColor", "1 1 1");
						collection[i].setAttribute("specularcolor", "1 1 1");
					}
				});
				addReachesAndReachedBy(relatedEntity);
				callingEntities.forEach(function(pair){
					if(!myEntities.includes(pair[0])) {
						myEntities.push(pair[0]);
					}
					if(!myEntities.includes(pair[1])) {
						myEntities.push(pair[1]);
					}
				});
                finished = true;
                if(activated){
					if(controllerConfig.bundledEdges) {
						//createRelatedConnections();
						forceLayout(version, myEntities);
					} else {
						createRelatedConnections(minWeight);
					}
				}
			} else {
                if(finished) {
                    callingEntities = [];
                    visitedEntities = [];
                }
                finished = true;
            }
		}
    }
	
	function onInnerClassesChanged(applicationEvent) {
		var value = applicationEvent.entities[0];
		controllerConfig.showInnerRelations = value;
		if(lastApplicationEvent != null) {
            switch (lastApplicationEvent.entities[0].type) {
                case "stk": onAntipatternSelected(lastApplicationEvent); break;
                case "component": onComponentSelected(lastApplicationEvent); break;
            }
		}
	}

	function loadPositionData(filePath){
		$.getJSON( filePath, function( data ) {

			events.log.info.publish({ text: "connector - loadPositionData"});

			data.mapping.forEach(function(mapping) {

				var min = parseObjectPosition(mapping.min);
				var max = parseObjectPosition(mapping.max);

				var connectorPosition = [];
				var connectorDistance = [];
				for (var index = 0; index < min.length; ++index) {
					connectorPosition[index] = ( Math.abs( max[index] - min[index] ) / 2 ) + min[index];
					connectorDistance[index] = Math.abs( max[index] - min[index] ) / 2;
				}

				loadedMin.set(mapping.name, min);
				loadedMax.set(mapping.name, max);
				loadedPositions.set(mapping.name, connectorPosition);
				loadedDistances.set(mapping.name, connectorDistance);
			});

		});
	}
	
	function onWeightChanged(applicationEvent) {
		var value = applicationEvent.entities[0];
		minWeight = value;
		if(lastApplicationEvent !== null) {
            switch (lastApplicationEvent.entities[0].type) {
				case "stk": onAntipatternSelected(lastApplicationEvent); break;
                case "component": onComponentSelected(lastApplicationEvent); break;
            }
        }
	}
	
	function onBundledEdgesChanged(applicationEvent) {
		var value = applicationEvent.entities[0];
		controllerConfig.bundledEdges = value;
        if(lastApplicationEvent !== null) {
            switch (lastApplicationEvent.entities[0].type) {
                case "stk": onAntipatternSelected(lastApplicationEvent); break;
                case "component": onComponentSelected(lastApplicationEvent); break;
            }
        }
	}

	function removeAllConnectors() {

        events.log.info.publish({ text: "connector - removeAllConnectors"});

        if(connectors.length == 0){
			return;
        }

		//remove scene elements
		connectors.forEach(function(version, connector){
			canvasManipulator.removeElement(connector);
        });

		connectors = new Map();

		//remove relation entities
		relations.forEach(function(relation){
			model.removeEntity(relation);
		});

		//publish removed entities
		var applicationEvent = {
			sender: patternConnectorController,
			entities: relations
		};
		events.added.off.publish(applicationEvent);
	}
	
	function onAntipatternSelected(applicationEvent) {

		events.log.info.publish({ text: "pattern connector - onAntipatternSelected"});
        lastApplicationEvent = applicationEvent;
		removeAllConnectors();

		//get related entites
		var sourceEntity = applicationEvent.entities[0];
		if(finished) {
            callingEntities = [];
            visitedEntities = [];
        }

        var allRelatedEntities = model.getEntitiesByAntipattern(sourceEntity.id);
		var relatedEntities = [];
        var relatedEntitiesByVersion = new Map();
        var versions = model.getSelectedVersions();
        for (var i = 0; i < allRelatedEntities.length; ++i) {
            if(versions.includes(allRelatedEntities[i].version)){
				relatedEntities.push(allRelatedEntities[i])
            }
		}

        relatedEntities.forEach(function(entity){
            var map = [];
            if(controllerConfig.bundledEdges)  {
                if(relatedEntitiesByVersion.has(entity.version)) {
                    map = relatedEntitiesByVersion.get(entity.version);
                }
                map.push(entity);
                relatedEntitiesByVersion.set(entity.version, map);
            } else {
                addReaches2(entity, sourceEntity);
            }
        });

        if(callingEntities.length == 0 && controllerConfig.bundledEdges == false) {
            return;
        }

        finished = true;

        if(activated){
            if(controllerConfig.bundledEdges) {
                relatedEntitiesByVersion.forEach(callback);
            } else {
                createRelatedConnections(0);
            }
        }
	}
	
	function addReachesAndReachedBy (entity) {
		entity.reaches.forEach(function(element){
			var pair = [entity, element];
			callingEntities.push(pair);
		});
		entity.reachedBy.forEach(function(element){
			var pair = [element, entity];
			var reversePair = [entity, element];
			if(callingEntities.includes(reversePair)){
			} else {
				callingEntities.push(pair);
			}
		});
	}
	
	function addInternalReaches(entities) {
		entities.forEach(function(entity){
			entity.reaches.forEach(function(element){
				if(entities.includes(element)) {
					var pair = [entity, element];
					if(!callingEntities.includes(pair)) {
						callingEntities.push(pair);
					}
				}
			});
		});
	}

	function addReaches2(entity, pattern) {
		var result = model.getPaths(entity.id, pattern.id);
        result.forEach(function(id){
            var element = model.getEntityById(id)
            var pair = [entity, element];
            callingEntities.push(pair);
        });
	}
	
	function addReaches (entity) {
		entity.reaches.forEach(function(element){
			var pair = [entity, element];
			callingEntities.push(pair);
		});
    }
	
	function forceLayout(version, relatedEntities) {
		if(!model.getSelectedVersions().includes(version)) {
			return;
		}
		d3Nodes = [];
		callingEntities = [];
		var i = 0;
		var graph = {
			links: [],
			nodes: {}
		};
		addInternalReaches(relatedEntities);
		createRelatedConnections(minWeight);
		d3Nodes.forEach(function(d3Node){
			var node = {
				id: d3Node.key,
				name: d3Node.key,
				entityID: d3Node.id,
				group: 1,
				index: i,
				x: d3Node.x,
				y: d3Node.y
			};
			graph.nodes[d3Node.key] = node;
			i++;
		});
		i = 0;
		callingEntities.forEach(function(relation){
			var source = relation[0].name.replace("$", ".");
			var target = relation[1].name.replace("$", ".");
			
			var sourceFound = false;
			var targetFound = false;
			for(var node in graph.nodes) {
				if(node == source) {
					sourceFound = true;
				}
				if(node == target) {
					targetFound = true;
				}
			}
			
			if(sourceFound && targetFound) {
				var link = {
					index: i,
					source: source,
					target: target,
					value: 1
				};
				graph.links.push(link);
				i++;
			}
		});
		
		var fbundling = d3.ForceEdgeBundling()
				.step_size(0.1)
				.compatibility_threshold(0.1)
				.nodes(graph.nodes)
				.edges(graph.links);
		var results   = fbundling();
		var height = d3Nodes[0].z + 0.5;
		results.forEach(function(line){
			var  polygonPoints= [];
            id = "line" + index;
			line.forEach(function(point){
				polygonPoints.push(point.x);
				polygonPoints.push(point.y);
			});
			var connector = createPolyline2D(id, polygonPoints.join(","), height);
            canvasManipulator.addElement(connector);
            connectors.set(connector, version);

            var info = {
                start: line[0].id,
				startID: line[0].entityID,
                end: line[line.length - 1].id,
				endID: line[line.length - 1].entityID,
            }
            meta.set(id, info);
            index++;
		});
        connectors.forEach(function(version, connector){
            var collection = connector.getElementsByTagName("Shape")
            collection[0].addEventListener("mouseover", handleOnMouseEnter, false);
            collection[0].addEventListener("mouseout", handleOnMouseOut, false);
        });
    }

    function handleOnMouseOut(multipartEvent) {
        var id = multipartEvent.target.id;
        var info = meta.get(id);
        var entity = model.getEntityById(info.startID)
        var role = model.getRoleBetween(info.startID, info.endID)

        var shape = document.getElementById(id + "m");
        shape.setAttribute("diffuseColor", "1 0 0");
        shape.setAttribute("emissiveColor", "1 0 0");
        shape.setAttribute("specularcolor", "1 0 0");

        $("#tooltipName").text(info.start + " «" + role + "» " + info.end);
        var tooltip = $("#tooltip");
        $("#tooltipVersion").text("Version: " + entity.version);
        tooltip.css("top", multipartEvent.layerY + 25 + "px");
        tooltip.css("left", multipartEvent.layerX + 25 +  "px");
        tooltip.css("display", "block");
    }

    function handleOnMouseEnter(multipartEvent) {
        var id = multipartEvent.target.id;
        var info = meta.get(id);
        var entity = model.getEntityById(info.startID)
        var role = model.getRoleBetween(info.startID, info.endID)

        var shape = document.getElementById(id + "m");
        shape.setAttribute("diffuseColor", "0 0 1");
        shape.setAttribute("emissiveColor", "0 0 1");
        shape.setAttribute("specularcolor", "0 0 1");
        //canvasManipulator.highlightEntities([multipartEvent.target], "blue");

        $("#tooltipName").text(info.start + " «" + role + "» " + info.end);
        var tooltip = $("#tooltip");
        $("#tooltipVersion").text("Version: " + entity.version);
        tooltip.css("top", multipartEvent.layerY + 25 + "px");
        tooltip.css("left", multipartEvent.layerX + 25 + "px");
        tooltip.css("display", "block");
    }
	
	function onComponentSelected (applicationEvent) {
        events.log.info.publish({ text: "pattern connector - onComponentSelected"});
		lastApplicationEvent = applicationEvent;
        removeAllConnectors();

		//get related entites
		var sourceEntity = applicationEvent.entities[0];
			
		if(finished) {
			callingEntities = [];
			visitedEntities = [];
		}
			
		var allRelatedEntities = model.getEntitiesByComponent(sourceEntity.id);
		var relatedEntitiesByVersion = new Map();
        var relatedEntities = [];
        var versions = model.getSelectedVersions();
        for (var i = 0; i < allRelatedEntities.length; ++i) {
            if(versions.includes(allRelatedEntities[i].version)){
                relatedEntities.push(allRelatedEntities[i])
            }
        }
		
		for(i = 0; i < relatedEntities.length; ++i) {
			var map = [];
			if(controllerConfig.bundledEdges)  {
				if(relatedEntitiesByVersion.has(relatedEntities[i].version)) {
					map = relatedEntitiesByVersion.get(relatedEntities[i].version);
					map.push(relatedEntities[i]);
					relatedEntitiesByVersion.set(relatedEntities[i].version, map);
				} else {
					map.push(relatedEntities[i]);
					relatedEntitiesByVersion.set(relatedEntities[i].version, map);
				}
			} else {
				addReaches(relatedEntities[i]);
			}
		}
						
		finished = true;
			
		if(callingEntities.length == 0 && controllerConfig.bundledEdges == false) {
			return;
		}

		if(activated){
			if(controllerConfig.bundledEdges) {
				relatedEntitiesByVersion.forEach(callback);
			} else {
				createRelatedConnections(minWeight);
			}
		}
	}
	
	function callback(entities,version,c) {
		forceLayout(version, entities);
	}
	
	function offVersionSelected(applicationEvent) {
		var version = applicationEvent.entities[0];
		connectors.forEach(function(connectorversion, connector){
			if(connectorversion == version) {
				canvasManipulator.removeElement(connector);
				connectors.delete(connector);
			}
		});
		
		relations.forEach(function(relation){
			if(relation.source.version == version) {
				model.removeEntity(relation);
			}
		});
	}

	function createRelatedConnections(minimalWeight){
		var relatedEntitiesMap = new Map();

		callingEntities.forEach(function(relatedPair){
            // if(relatedEntitiesMap.has(relatedPair[1])){
				//events.log.info.publish({ text: "pattern connector - onRelationsChanged - multiple relation"});
				//return;
			// }
			if(relatedPair[0].version != relatedPair[1].version){
				return;
			}
            if(controllerConfig.showInnerRelations === false){
				if(isInnerClass(relatedPair[1], relatedPair[0])){
					events.log.info.publish({ text: "pattern connector - onRelationsChanged - inner relation"});
					return;
				}
			}
            var weight = (relatedPair[0].betweennessCentrality + relatedPair[1].betweennessCentrality)/2;
			if(weight == 0) {
				//weight = 0.01;
			}
            if(weight < minimalWeight) {
				return;
			}
            //create scene element
			var connector = createConnector(relatedPair[0], relatedPair[1]);

            //target or source not rendered -> no connector -> remove relatation
			if( connector === undefined || connector === null){
				return;
			}
            connectors.set(connector, relatedPair[0].version);
			canvasManipulator.addElement(connector);

            //create model entity
			var relation = model.createEntity(
				"Relation",
				relatedPair[0].id + "--2--" + relatedPair[1].id,
				relatedPair[0].name + " - " + relatedPair[1].name,
				relatedPair[0].name + " - " + relatedPair[1].name,
				relatedPair[0]
			);

            relation.source = relatedPair[0];
			relation.target = relatedPair[1];
			relations.push(relation);
			relatedEntitiesMap.set(relatedPair[0], relatedPair[1]);
        });

		connectors.forEach(function(version, connector){

            var collection = connector.getElementsByTagName("Shape")
            collection[0].addEventListener("mouseover", handleOnMouseEnter, false);
            collection[0].addEventListener("mouseout", handleOnMouseOut, false);

        });

		if(relatedEntitiesMap.size != 0){
			var applicationEvent = {
				sender: patternConnectorController,
				entities: relations
			};
			events.added.on.publish(applicationEvent);
		}
	}

	function createConnector(entity, relatedEntity){
        var weight = (entity.betweennessCentrality + relatedEntity.betweennessCentrality)/2;
		//calculate attributes
		var sourcePosition = calculateSourcePosition(entity, relatedEntity);

        if( sourcePosition === null ){
			return;
		}

        var targetPosition = calculateTargetPosition(entity, relatedEntity);

        if( targetPosition === null ){
			return;
		}
        var connectorColor = "1 0 0";
		var connectorSize = weight;
		if(connectorSize < 0.1) {
			connectorSize = 0.1;
		}
        var sourceKey = entity.name.replace("$", ".");
		var targetKey = relatedEntity.name.replace("$", ".");
		var sourceStatus = searchKey(sourceKey);
		var targetStatus = searchKey(targetKey);
		var d3SourceNode = {};
		var d3TargetNode = {};

        if(isRealInnerClass(entity, relatedEntity)) {
			if(sourceStatus == undefined) {
				d3SourceNode = {
					name : sourceKey,
					key: sourceKey,
					id: entity.id,
					size: 1,
					imports: [targetKey],
					parent: null,
					children: [],
					depth: 2,
					x: sourcePosition[0],
					y: sourcePosition[1],
					z: sourcePosition[2],
				};
				d3Nodes.push(d3SourceNode);
			} else {
				d3SourceNode = d3Nodes[sourceStatus];
				d3SourceNode.imports.push(targetKey);
			}
			if(targetStatus == undefined){
				d3TargetNode = {
					name : targetKey,
					key: targetKey,
                    id: relatedEntity.id,
					size: 1,
					imports: [],
					children: [d3SourceNode],
					parent: null,
					depth: 1,
					x: targetPosition[0],
					y: targetPosition[1],
					z: targetPosition[2],
				};
				d3SourceNode.parent = d3TargetNode;
				d3Nodes.push(d3TargetNode);
			}
			
		} else if (isRealInnerClass(relatedEntity, entity)) {
			if(sourceStatus == undefined) {
				d3SourceNode = {
					name : sourceKey,
					key: sourceKey,
                    id: entity.id,
					size: 1,
					imports: [targetKey],
					parent: d3TargetNode,
					children: [],
					depth: 1,
					x: sourcePosition[0],
					y: sourcePosition[1],
					z: sourcePosition[2],
				};
				d3Nodes.push(d3SourceNode);
			} else {
				d3SourceNode = d3Nodes[sourceStatus];
				d3SourceNode.imports.push(targetKey);
			}
			if(targetStatus == undefined){
				d3TargetNode = {
					name : targetKey,
					key: targetKey,
                    id: relatedEntity.id,
					size: 1,
					imports: [],
					children: [],
					parent: d3SourceNode,
					depth: 2,
					x: targetPosition[0],
					y: targetPosition[1],
					z: targetPosition[2],
				};
				d3Nodes.push(d3TargetNode);
			}
		} else {
			if(sourceStatus == undefined) {
				d3SourceNode = {
					name : sourceKey,
					key: sourceKey,
                    id: entity.id,
					size: 1,
					imports: [targetKey],
					children: [],
					parent: null,
					depth: 1,
					x: sourcePosition[0],
					y: sourcePosition[1],
					z: sourcePosition[2],
				};
				d3Nodes.push(d3SourceNode);
			} else {
				d3SourceNode = d3Nodes[sourceStatus];
				d3SourceNode.imports.push(targetKey);
			}
			
			if(targetStatus == undefined){
				d3TargetNode = {
					name : targetKey,
					key: targetKey,
                    id: relatedEntity.id,
					size: 1,
					imports: [],
					children: [],
					parent: null,
					depth: 1,
					x: targetPosition[0],
					y: targetPosition[1],
					z: targetPosition[2],
				};
				d3Nodes.push(d3TargetNode);
			}
		}

		//config
		if(controllerConfig.fixPositionZ){
			sourcePosition[2] = controllerConfig.fixPositionZ;
			targetPosition[2] = controllerConfig.fixPositionZ;
		}

		if(controllerConfig.bundledEdges == false) {
            var info = {
                start: sourceKey,
                startID: entity.id,
                end: targetKey,
                endID: relatedEntity.id,
            };
            var id = "line" + index;
            index++;
            meta.set(id, info)
			//create element
			var transform = document.createElement('Transform');

            if(isNaN(sourcePosition[0])
				|| isNaN(sourcePosition[1])
                || isNaN(sourcePosition[2])
                || isNaN(targetPosition[0])
                || isNaN(targetPosition[1])
                || isNaN(targetPosition[2])){
                return null;
            }

			transform.appendChild(createLine(sourcePosition, targetPosition, connectorColor, connectorSize, id));
	
			//config
			if(controllerConfig.createEndpoints){
				transform.appendChild(createEndPoint(sourcePosition, targetPosition, "0 0 0", connectorSize * 2));
			}

            return transform;
		}
	}

	function calculateSourcePosition(entity, relatedEntity){

		var sourcePosition = getObjectPosition(entity.id);
		if(controllerConfig.sourceStartAtParentBorder){
			if(!isTargetChildOfSourceParent(relatedEntity, entity)){
				let targetPosition = getObjectPosition(relatedEntity.id);
				if(targetPosition === null){
					return null;
				}
				sourcePosition = calculatePositionFromParent(sourcePosition, targetPosition, entity.belongsTo);
			}
		}

        if(controllerConfig.sourceStartAtBorder){

            let targetPosition = getObjectPosition(relatedEntity.id);

            if(targetPosition === null){
                return null;
			}

            sourcePosition = calculateBorderPosition(sourcePosition, targetPosition, entity);
        }
        return sourcePosition;
	}

	function calculateTargetPosition(entity, relatedEntity){

		var targetPosition = getObjectPosition(relatedEntity.id);
		if(targetPosition === null){
			return null;
		}

		if(controllerConfig.targetEndAtParentBorder){
			if(!isTargetChildOfSourceParent(relatedEntity, entity)){
				let sourcePosition = getObjectPosition(entity.id);
				targetPosition = calculatePositionFromParent(targetPosition, sourcePosition, relatedEntity.belongsTo);
			}
		}

		if(controllerConfig.targetEndAtBorder){
			let sourcePosition = getObjectPosition(entity.id);
			targetPosition = calculateBorderPosition(targetPosition, sourcePosition, relatedEntity);
		}

		return targetPosition;
	}
	
	function isRealInnerClass(source, target) {
		var sourceParent = source.belongsTo;		
		if(sourceParent == target) {
			return true;
		}
		return false;
	}
	
	function isInnerClass(target, source) {
		var targetParent = target.belongsTo;
		var sourceParent = source.belongsTo;
		
		if(targetParent == source || sourceParent == target) {
			return true;
		}
		
		return false;
	}

	function isTargetChildOfSourceParent(target, source){

		var targetParent = target.belongsTo;
		var sourceParent = source.belongsTo;
		
		if(targetParent.type == "Namespace" && sourceParent.type == "Namespace") {
			return false;
		} else {
			events.log.info.publish({text: "type: " + targetParent.type});
		}

		while(targetParent !== undefined) {

			if(targetParent == sourceParent){
				return true;
			}

			targetParent = targetParent.belongsTo;
		}

		return false;
	}

	function calculateBorderPosition(originalSourcePosition, originalTargetPosition, entity){
        if(!loadedMin.has(entity.id) || !loadedMax.has(entity.id)){
			events.log.error.publish({ text: "min max position for " + entity.id + " not loaded!" });
			return;
		}

		//TODO ueber CanvasController ermitteln (Multipart hat Volume Funktion)
		var min = loadedMin.get(entity.id);
		var max = loadedMax.get(entity.id);

		var sourcePosition = originalSourcePosition.slice();
		var targetPosition = originalTargetPosition.slice();

		//calculate the 4 corner points
		var point00 = min.slice();
		var point01 = min.slice();
		var point10 = max.slice();
		var point11 = max.slice();

		point01[2] = max[2];
		point10[2] = min[2];

		//set y value of all points to delta y
		var deltaY = min[1] + (( max[1] - min[1]) / 2);
		point00[1] = deltaY;
		point01[1] = deltaY;
		point10[1] = deltaY;
		point11[1] = deltaY;

		sourcePosition[1] = deltaY;
		targetPosition[1] = deltaY;

        //calculate distances

		var distances = new Map();
		distances.set(calculateDistance(point00, targetPosition), point00);
		distances.set(calculateDistance(point01, targetPosition), point01);
		distances.set(calculateDistance(point10, targetPosition), point10);
		distances.set(calculateDistance(point11, targetPosition), point11);

        //get the two nearest points
		var sortedDistances =  Array.from(distances.keys());
		sortedDistances = sortedDistances.sort(function(a,b){return a-b;});

        var nearestPoint1 = distances.get(sortedDistances[0]);
		var nearestPoint2 = distances.get(sortedDistances[1]);


		var valueUsedToCalculate;
		var valueToCalculate;

        if (nearestPoint1[0] === nearestPoint2[0]) {
			valueUsedToCalculate = 0;
			valueToCalculate = 2;
		} else if (nearestPoint1[2] === nearestPoint2[2]) {
			valueUsedToCalculate = 2;
			valueToCalculate = 0;
		} else {
			events.log.error.publish({text: "border points could not be calcuated"});
			return;
		}

        var riseVector = calculateDistanceVector(sourcePosition, targetPosition);


		if(riseVector[valueUsedToCalculate] == 0){
			var valueSwitch = valueUsedToCalculate;
			valueUsedToCalculate = valueToCalculate;
			valueToCalculate = valueSwitch;
		}

		var riseFactor = ( nearestPoint1[valueUsedToCalculate] - targetPosition[valueUsedToCalculate] ) / riseVector[valueUsedToCalculate];



		var borderPoint = [];
		borderPoint[valueUsedToCalculate] 	= nearestPoint1[valueUsedToCalculate];
		borderPoint[valueToCalculate] 		= targetPosition[valueToCalculate] + ( riseFactor * riseVector[valueToCalculate] );
		borderPoint[1] = deltaY;

		return borderPoint;
	}

	function calculateDistance(point1, point2){
		var distanceVector = calculateDistanceVector(point1, point2);
		return Math.sqrt( Math.pow(distanceVector[0], 2) + Math.pow(distanceVector[1], 2) + Math.pow(distanceVector[2], 2) );
	}

	function calculateDistanceVector(point1, point2){
		var distanceVector = [];
		distanceVector[0] = point1[0] - point2[0];
		distanceVector[1] = point1[1] - point2[1];
		distanceVector[2] = point1[2] - point2[2];

		return distanceVector;
	}

	function calculatePositionFromParent(sourcePosition, targetPosition, sourceParent){
		if(controllerConfig.elementShape == "circle"){
			return calculateCirclePositionFromParent(sourcePosition, targetPosition, sourceParent);
		}
		if(controllerConfig.elementShape == "square"){
			return calculateSquarePositionFromParent(sourcePosition, targetPosition, sourceParent);
		}
		return sourcePosition;
	}

	function calculateCirclePositionFromParent(sourcePosition, targetPosition, sourceParent){
		//calculation derived from http://www.3d-meier.de/tut6/XPresso53.html

		var parentPosition = getObjectPosition(sourceParent.id);

		var parentRadius = loadedDistances.get(sourceParent.id);
		var parentX = parentPosition[0];
		var parentY = parentPosition[1];


		var targetX = targetPosition[0];
		var targetY = targetPosition[1];

		var sourceX = sourcePosition[0];
		var sourceY = sourcePosition[1];

		var deltaX = targetX - sourceX;
		var deltaY = targetY - sourceY;

		var a = deltaY / deltaX;
		var b = (targetY - parentY) - ( a * (targetX - parentX) );

		var r = parentRadius[0];


		var AA = 1 + Math.pow(a, 2);
		var BB = (2 * a * b);
		var CC = Math.pow(b, 2) - Math.pow(r, 2);

		var XX = Math.pow(BB, 2) - 4 * AA * CC;


		var x1 = (-BB + Math.sqrt( XX, 2 )) / ( 2 * AA );
		var x2 = (-BB - Math.sqrt( XX, 2 )) / ( 2 * AA );

		var y1 = a * x1 + b;
		var y2 = a * x2 + b;


		var newSourcePosition;
		if(  	(targetY > sourceY && targetX < sourceX) ||
				(targetY < sourceY && targetX < sourceX) ){
			newSourcePosition	= [x2+parentX, y2+parentY, sourcePosition[2]];
		} else {
			newSourcePosition	= [x1+parentX, y1+parentY, sourcePosition[2]];
		}

		return newSourcePosition;
	}
	
	/*function searchElementsByName(name, version) {
		var entities = model.getEntitiesByVersion(version);
		var result;
		entities.forEach(function(entity) {
			if(entity.name === name) {
				result = entity.id;
			}
		});
		return result;
	}*/

	function getObjectPosition(objectId){

		var position = null;

		if( loadedPositions.has(objectId) ){
			position = loadedPositions.get(objectId);
		} else {
			var myElement = jQuery("#" + objectId)[0];
			if( myElement != undefined ){
				position = parseObjectPosition(myElement.getAttribute("translation"));
			}
		}

		if( position === null){
			events.log.error.publish({ text: objectId + "has no position data" });
		}

		return position;
	}

	function parseObjectPosition(positionString){

		var position = positionString.split(" ");

		for (var index = 0; index < position.length; ++index) {
			position[index] = parseFloat(position[index]);
		}

		return position;
	}
	
	function searchKey(key) {
		for(var i = 0; i < d3Nodes.length; i++) {
			if(d3Nodes[i].key == key) {
				return i;
			}
		}
		return undefined;
	}

	function createEndPoint(source, target, color, size){
		//calculate attributes

		//endPointAngle
		var lineX = target[0]-source[0];
		var lineY = target[1]-source[1];

		var endPointAngle = Math.atan( Math.abs(lineY / lineX) );

		//endPointAmount
		var lineAmount = Math.pow( lineX, 2) + Math.pow( lineY, 2);
		lineAmount = Math.sqrt(lineAmount,2);

		var endPointAmount = lineAmount - 0.5;

		//endPoint positions
		var endPointX = Math.cos(endPointAngle) * endPointAmount;
		var endPointY = Math.sin(endPointAngle) * endPointAmount;

		if( lineX <= 0 && lineY >= 0){
			endPointX = endPointX * -1;
		}
		if( lineX <= 0 && lineY <= 0){
			endPointX = endPointX * -1;
			endPointY = endPointY * -1;
		}
		if( lineX >= 0 && lineY <= 0){
			endPointY = endPointY * -1;
		}

		var translation = [];

		translation[0] = source[0] + endPointX;
		translation[1] = source[1] + endPointY;
		translation[2] = (source[2]+(target[2]-source[2])/2.0);

		var scale = [];
		scale[0] = size;
		scale[1] = 1;
		scale[2] = size;

		var rotation = [];
		rotation[0] = (target[2]-source[2]);
		rotation[1] = 0;
		rotation[2] = (-1.0)*(target[0]-source[0]);
		rotation[3] = Math.acos((target[1] - source[1])/(Math.sqrt( Math.pow(target[0] - source[0], 2) + Math.pow(target[1] - source[1], 2) + Math.pow(target[2] - source[2], 2) )));

		//create element
		var transform = document.createElement('Transform');

		transform.setAttribute("translation", translation.toString());
		transform.setAttribute("scale", scale.toString());
		transform.setAttribute("rotation", rotation.toString());

		var shape = document.createElement('Shape');
		transform.appendChild(shape);

		var appearance = document.createElement('Appearance');
		shape.appendChild(appearance);
		var material = document.createElement('Material');
		material.setAttribute("diffuseColor", color);
		appearance.appendChild(material);


		var cylinder = document.createElement('Cylinder');
		cylinder.setAttribute("radius", "0.25");
		cylinder.setAttribute("height", "1");
		shape.appendChild(cylinder);

		return transform;
	}

    function createPolyline2D(name, lineSegments, height) {
        var transform = document.createElement("transform");
        transform.setAttribute("render", "true");
        transform.setAttribute("translation", "0, 0," + (height + 0.55));

        var shape = document.createElement("Shape");
        shape.setAttribute("id", id);
        transform.appendChild(shape);
        var appearance = document.createElement("Appearance");
        shape.appendChild(appearance);
        var material = document.createElement("Material");
        material.setAttribute("id", id + "m");
        material.setAttribute("diffuseColor", "1 0 0");
        material.setAttribute("emissiveColor", "1 0 0");
        material.setAttribute("ambientintensity", "1");
        material.setAttribute("specularcolor", "1 0 0");
        material.setAttribute("shininess", "1");
        material.setAttribute("ambientintensity", "1");
        //material.setAttribute("transparency", "0.8");
        appearance.appendChild(material);
        var polyline2d = document.createElement("Polyline2D");
        polyline2d.setAttribute("lineSegments", lineSegments);
        shape.appendChild(polyline2d);
        return transform;
    }


	function createLine(source, target, color, size, id){
		//calculate attributes

		var betrag = (Math.sqrt( Math.pow(target[0] - source[0], 2) + Math.pow(target[1] - source[1], 2) + Math.pow(target[2] - source[2], 2) ));
		var translation = [];

		translation[0] = source[0]+(target[0]-source[0])/2.0;
		translation[1] = source[1]+(target[1]-source[1])/2.0;
		translation[2] = source[2]+(target[2]-source[2])/2.0 + 0.5;

		var scale = [];
		scale[0] = size;
		scale[1] = betrag;
		scale[2] = size;

		var rotation = [];
		rotation[0] = (target[2]-source[2]);
		rotation[1] = 0;
		rotation[2] = (-1.0)*(target[0]-source[0]);
		rotation[3] = Math.acos((target[1] - source[1])/(Math.sqrt( Math.pow(target[0] - source[0], 2) + Math.pow(target[1] - source[1], 2) + Math.pow(target[2] - source[2], 2) )));

		//create element
		var transform = document.createElement('transform');

		transform.setAttribute("translation", translation.toString());
		transform.setAttribute("scale", scale.toString());
		transform.setAttribute("rotation", rotation.toString());
		transform.setAttribute("render", true);

		var shape = document.createElement('Shape');
        shape.setAttribute("id", id);
        transform.appendChild(shape);

		var appearance = document.createElement('Appearance');
        //appearance.setAttribute("sortKey", 2);
		shape.appendChild(appearance);
		shape.setAttribute("id", id);
		var material = document.createElement('Material');
		material.setAttribute("id", id + "m");
		material.setAttribute("diffuseColor", color);
	//	material.setAttribute("transparency", transparency);

		appearance.appendChild(material);

		var cylinder = document.createElement('Cylinder');
		cylinder.setAttribute("radius", "0.25");
		cylinder.setAttribute("height", "1");
		shape.appendChild(cylinder);
        return transform;
	}

	return {
        initialize: initialize,
        reset: reset,
        activate: activate,
        deactivate: deactivate
    };

}();
