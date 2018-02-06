var versionExplorerController = (function() {
    
	var versionExplorerTreeID = "versionExplorerTree";
	var jQVersionExplorerTree = "#versionExplorerTree";
	
	var tree;
	
	function initialize(){
	}
	
	function activate(rootDiv){		
		//create zTree div-container
		var zTreeDiv = document.createElement("DIV");
		zTreeDiv.id = "zTreeDiv";
				
		var versionExplorerTreeUL = document.createElement("UL");
		versionExplorerTreeUL.id = versionExplorerTreeID;
		versionExplorerTreeUL.setAttribute("class", "ztree");
				
		zTreeDiv.appendChild(versionExplorerTreeUL);				
		rootDiv.appendChild(zTreeDiv);
				
		//create zTree
		prepareTreeView();
    }
	
	function reset(){
            prepareTreeView();
	}
    
    function prepareTreeView() {
		var items = [];
        var entities = model.getAllVersions();
        entities.forEach(function(value, key) {
			item = {id: key, parentId: "", name: key, iconSkin: "zt", checked: true};
            items.push(item);
        });
        	       
		//zTree settings
        var settings = {
            check: {
                enable: true,
                chkboxType: {"Y": "ps", "N": "s"}
            },
            data: {
                simpleData: {
                    enable:true,
                    idKey: "id",
                    pIdKey: "parentId",
                    rootPId: ""
                }
            },
            callback: {
                onCheck: zTreeOnCheck,
                onClick: zTreeOnClick
            },
            view:{
                showLine: false,
                showIcon: false,
                selectMulti: false
            }
        };		
        //create zTree
        tree = $.fn.zTree.init( $(jQVersionExplorerTree), settings, items);
    }
    
    function zTreeOnCheck(treeEvent, treeId, treeNode) {
       var applicationEvent = {			 
            sender: versionExplorerController,
            entities: [treeNode.id]
        };
		
		if (treeNode.checked){
			model.addVersion(treeNode.id);
            events.versionSelected.on.publish(applicationEvent);
            //model.selectedVersions.push(treeNode.id);
        } else {
			model.removeVersion(treeNode.id);
            events.versionSelected.off.publish(applicationEvent);
        }
    }
    
    function zTreeOnClick(treeEvent, treeId, treeNode) {
        var applicationEvent = {			 
            sender: versionExplorerController,
            entities: [treeNode.id]
        };
        events.versionSelected.on.publish(applicationEvent);
    }

    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();
