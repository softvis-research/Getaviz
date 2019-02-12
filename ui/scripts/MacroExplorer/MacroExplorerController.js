var macroExplorerController = (function() {
	
	let tree;
	let jMacroExplorer = "#macroExplorerUL";
	let macroExplorerTreeId = "macroExplorerUL";
	let allTreeNodesById = new Map();

	let controllerConfig = {
        elementsSelectable: true
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
		items.sort();
		
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
                onCheck: macroOnCheck
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
		changedNodes.forEach(function(node){
			node.checkedOld = node.checked;	
			entities.push(node.id);
		});
								
		var applicationEvent = {			
			sender: 	macroExplorerController,
			entities:	entities,
			allTreeNodesById: allTreeNodesById
		};
		
		if (treeNode !== undefined){
			events.macroChanged.on.publish(applicationEvent);
		} 
	}
    
    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();