var legendController = (function() {
    
	var versionExplorerTreeID = "legendTree";
	var jQVersionExplorerTree = "#" + versionExplorerTreeID
	
	var tree;

    var iconFiles = {
        packageIcon: 	"scripts/Legend/images/package.png",
        typeIcon: 		"scripts/Legend/images/type.png",
        sizeIcon:       "scripts/Legend/images/size.png",
        selectionIcon:  "scripts/Legend/images/selection.png",
        diskIcon:       "scripts/Legend/images/disk.png"
    };

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
        var item = {id: "selection", parentId: "",  name: "<span style='text-decoration: underline; font-style: italic;'>Selection</span>", iconSkin: "zt", icon: iconFiles.selectionIcon};
        items.push(item);
		item = {id: "package", parentId: "", name: "Package", iconSkin: "zt", icon: iconFiles.packageIcon};
		items.push(item);
		item = {id: "type", parentId: "", name: "Class", t: "oho", open:true, collapse:false, collapsable: false, iconSkin: "zt", icon: iconFiles.diskIcon};
		items.push(item);
        item = {id: "type_color", parentId: "type",  name: "<span style='text-decoration: underline; font-style: italic;'>Importance for Circular Dependency</span>", iconSkin: "zt", icon: iconFiles.typeIcon};
        items.push(item);
        item = {id: "type_size", parentId: "type",  name: "<span style='text-decoration: underline; font-style: italic;'>Importance for Subtype Knowledge</span>", iconSkin: "zt", icon: iconFiles.sizeIcon};
        items.push(item);

		//zTree settings
        var settings = {
            data: {
                simpleData: {
                    enable:true,
                    idKey: "id",
                    pIdKey: "parentId",
                    rootPId: ""
                }
            },
            callback: {
                onClick: zTreeOnClick,
                beforeCollapse: beforeCollapse
            },
            view:{
                showLine: false,
                showIcon: true,
                selectMulti: false,
                nameIsHTML: true,
            }
        };		
        tree = $.fn.zTree.init( $(jQVersionExplorerTree), settings, items);
    }

    function beforeCollapse(treeId, treeNode) {
        return (treeNode.collapse !== false);
    }
    
    function zTreeOnClick(treeEvent, treeId, treeNode) {
        var applicationEvent = {			 
            sender: versionExplorerController,
            entities: [treeNode.id]
        };
        switch(treeNode.id) {
            case "type_color": {
                window.open("./glossary.html#bc",'glossary');
                break;
            }
            case "type_size": {
                window.open("./glossary.html#todo", 'glossary');
                break;
            }

            case "selection": {
                window.open("./glossary.html#selection", 'glossary');
                break;
            }
        }
    }

    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();
