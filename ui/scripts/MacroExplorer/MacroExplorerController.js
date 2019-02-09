var macroExplorerController = (function() {
	
	let tree;
	let jMacroExplorer = "#macroExplorerUL";
	let macroExplorerULID = "macroExplorerUL";

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
				
		let macroExplorerUL = document.createElement("UL");
		macroExplorerUL.id = macroExplorerULID;
		macroExplorerUL.setAttribute("class", "ztree");
				
		macroDiv.appendChild(macroExplorerUL);
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
		
		//Sortierung Alphanumerisch
		

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
		//TODO: implement
    }
	
	function macroOnClick(treeEvent, treeId, treeNode) {
		//TODO: implement
    }
    
    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();