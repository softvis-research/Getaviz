var macroExplorerController = (function() {
	
	let tree;
	let jMacroExplorer = "#macroExplorerUL";
	let macroExplorerTreeId = "macroExplorerUL";
	let allTreeNodesById = new Map();
	let selectedMethods =[];
	let selectedFeature;

	let controllerConfig = {
		elementsSelectable: true,
		filterMode: "transparent" //can be "transparent" or "removed"
	};
	
	function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);
    }
	
	function activate(rootDiv){
        //create macro div-container
		let macroDiv = document.createElement("DIV");
		macroDiv.id = "macroDiv";
				
		let macroExplorerTree = document.createElement("UL");
		macroExplorerTree.id = macroExplorerTreeId;
		macroExplorerTree.setAttribute("class", "ztree");
				
		macroDiv.appendChild(macroExplorerTree);
		rootDiv.appendChild(macroDiv);
				
		//create zTree
		prepareMacroView();
    }
	
	function reset(){
		prepareMacroView();
	}
    
    function prepareMacroView() {
        
        let macros = model.getAllMacrosById();
        let items = [];
		
		//build items for ztree
		macros.forEach(function(macro) {
			
			var item;
			
			item = {
				id: macro.id,
				open: false,
				checked: true,
				parentId: "",
				name: macro.name
			};

			if(item !== undefined) {
                items.push(item);
            }
		});
		
		//sort alphanumerically
		items.sort(function(obj1, obj2) {
                    return obj1.name.localeCompare(obj2.name);
                    /*if (obj1.name < obj2.name){
					return -1;
				}
				if (obj1.name > obj1.name){
					return 1;
				}			
				
				return 0;*/
                });;
		
		//settings
		var settings = {
            check: {
                enable: true
            },
            data: {
                simpleData: {
                enable:true,
                idKey: "id"
                }
            },
            callback: {
                onCheck: macroOnCheck,
				onClick: macroOnClick
            },
            view:{
                showLine: false,
                showIcon: false,
                selectMulti: true
            }

        };
		
		tree = $.fn.zTree.init( $(jMacroExplorer), settings, items);
	}
	
	function macroOnCheck(event, treeId, treeNode) {
		var allNodes = tree.getNodes();
		for (var i = 0; i < allNodes.length; i++) {
			var currentNode = allNodes[i];
			allTreeNodesById.set(currentNode.id, currentNode)
		}

		var changedNodes = tree.getChangeCheckedNodes();
		var entities = [];
		changedNodes.forEach(function (node) {
			node.checkedOld = node.checked;
			entities.push(node.id);
		});

		var applicationEvent = {
			sender: macroExplorerController,
			entities: entities,
			allTreeNodesById: allTreeNodesById,
			filterMode: controllerConfig.filterMode
		};

		if (treeNode !== undefined) {
			events.macroChanged.on.publish(applicationEvent);
		}
	}

	function macroOnClick(event, treeId, treeNode) {
		const unMarkEvent = {
			sender: macroExplorerController,
			entities: selectedMethods
		};

		events.marked.off.publish(unMarkEvent);

		if(selectedFeature !== treeNode) {
			let relatedEntities = [];

			const methods = model.getEntitiesByType("Function");
			const maxRandomValue = methods.length;

			for (let i = 0; i < 50; ++i) {
				const randomNumber = Math.floor(Math.random() * maxRandomValue);
				relatedEntities = relatedEntities.concat((methods[randomNumber]));
			}

			const applicationEvent = {
				sender: macroExplorerController,
				entities: relatedEntities,
			};

			events.marked.on.publish(applicationEvent);
			selectedMethods = relatedEntities;
			selectedFeature = treeNode;
		} else {
			selectedFeature = null;
			selectedMethods = null;
		}
	}

	function sendInitialEvent(){
                if(tree == undefined) return;
		var allNodes = tree.getNodes();
		for (var i = 0; i < allNodes.length; i++) {
			var currentNode = allNodes[i];
			allTreeNodesById.set(currentNode.id, currentNode)
		}

		var entities = [];
		allNodes.forEach(function(node){
			entities.push(node.id);
		});
								
		var applicationEvent = {			
			sender: 	macroExplorerController,
			entities:	entities,
			allTreeNodesById: allTreeNodesById,
			filterMode: controllerConfig.filterMode
		};
		
		if(entities.length > 0){
			events.macroChanged.on.publish(applicationEvent);
		}
	}
    
    return {
        initialize: initialize,
		activate: activate,
		reset: reset,
		sendInitialEvent: sendInitialEvent
    };
})();
