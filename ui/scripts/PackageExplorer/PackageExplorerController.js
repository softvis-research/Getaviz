var packageExplorerController = (function() {
    
	let packageExplorerTreeID = "packageExplorerTree";
	let jQPackageExplorerTree = "#packageExplorerTree";
	
	let tree;

	let controllerConfig = {
		projectIcon: 	"scripts/PackageExplorer/images/project.png",
		packageIcon: 	"scripts/PackageExplorer/images/package.png",
		typeIcon: 		"scripts/PackageExplorer/images/type.png",
		fieldIcon: 		"scripts/PackageExplorer/images/field.png",
		methodIcon:		"scripts/PackageExplorer/images/method.png",
        elementsSelectable: true
	};
	
	function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);
    }
	
	function activate(rootDiv){
        //create zTree div-container
		let zTreeDiv = document.createElement("DIV");
		zTreeDiv.id = "zTreeDiv";
				
		let packageExplorerTreeUL = document.createElement("UL");
		packageExplorerTreeUL.id = packageExplorerTreeID;
		packageExplorerTreeUL.setAttribute("class", "ztree");
				
		zTreeDiv.appendChild(packageExplorerTreeUL);
		rootDiv.appendChild(zTreeDiv);
				
		//create zTree
		prepareTreeView();
		events.selected.on.subscribe(onEntitySelected);
    }
	
	function reset(){
		prepareTreeView();
	}
    
    function prepareTreeView() {
        
        let entities = model.getAllEntities();
        let items = [];
		
		//build items for ztree
		entities.forEach(function(entity) {
			
			var item;
			
			if(entity.belongsTo === undefined){
				//rootpackages
				if(entity.type !== "issue") {
					if(entity.type === "Namespace") {
                        item = {
                            id: entity.id,
                            open: false,
                            checked: true,
                            parentId: "",
                            name: entity.name,
                            icon: controllerConfig.packageIcon,
                            iconSkin: "zt"
                        };
                    } else {
                        item = {
                            id: entity.id,
                            open: true,
                            checked: true,
                            parentId: "",
                            name: entity.name,
                            icon: controllerConfig.projectIcon,
                            iconSkin: "zt"
                        };
                    }
                }
            } else {	
				switch(entity.type) {
					case "Project":
						item = { id: entity.id, open: true, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.projectIcon, iconSkin: "zt"};
						break;
					case "Namespace":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.packageIcon, iconSkin: "zt"};
						break;
					case "Class":
                        if(entity.id.endsWith("_2") || entity.id.endsWith("_3")){
                            break;
                        };
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.typeIcon, iconSkin: "zt"};
						break;
					case  "ParameterizableClass":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.typeIcon, iconSkin: "zt"};
						break;
					case "Enum":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.typeIcon, iconSkin: "zt"};
						break;
					case "EnumValue":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.fieldIcon, iconSkin: "zt"};
						break;
					case "Attribute":
					case "Variable":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.fieldIcon, iconSkin: "zt"};
						break;
					case "Method":
					case "Function":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.methodIcon, iconSkin: "zt"};
						break;
					
					default: 
						events.log.warning.publish({ text: "FamixElement not in tree: " + entity.type});

						return;
				}
           }
			if(item !== undefined) {
                items.push(item);
            }
		});
		
		//Sortierung nach Typ und Alphanumerisch
		items.sort(
			function(a,b) {
				
				var sortStringA = "";
				switch(a.icon){
					case controllerConfig.packageIcon:
						sortStringA = "1" + a.name.toUpperCase();
						break;
					case controllerConfig.typeIcon:
						sortStringA = "2" + a.name.toUpperCase();
						break;
					case controllerConfig.fieldIcon:
						sortStringA = "3" + a.name.toUpperCase();
						break;
					case controllerConfig.methodIcon:
						sortStringA = "4" + a.name.toUpperCase();
						break;
					default:
						sortStringA = "0" + a.name.toUpperCase();
				}
				
				var sortStringB = "";
				switch(b.icon){
					case controllerConfig.packageIcon:
						sortStringB = "1" + b.name.toUpperCase();
						break;
					case controllerConfig.typeIcon:
						sortStringB = "2" + b.name.toUpperCase();
						break;
					case controllerConfig.fieldIcon:
						sortStringB = "3" + b.name.toUpperCase();
						break;
					case controllerConfig.methodIcon:
						sortStringB = "4" + b.name.toUpperCase();
						break;
					default:
						sortStringB = "0" + b.name.toUpperCase();
						break;
				}

				if (sortStringA < sortStringB){
					return -1;
				}
				if (sortStringA > sortStringB){
					return 1;
				}			
				
				return 0;
			}
		);

		//zTree settings
		var settings = {
            check: {
                enable: controllerConfig.elementsSelectable,
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
                showIcon: true,
                selectMulti: false
            }

        };		
		
		//create zTree
        tree = $.fn.zTree.init( $(jQPackageExplorerTree), settings, items);
    }
    
	
	function zTreeOnCheck(event, treeId, treeNode) {
        var nodes = tree.getChangeCheckedNodes();
        
		var entities = [];
		nodes.forEach(function(node){
			node.checkedOld = node.checked; //fix zTree bug on getChangeCheckedNodes	
			entities.push(model.getEntityById(node.id));
		});
								
		var applicationEvent = {			
			sender: 	packageExplorerController,
			entities:	entities
		};
		
		if (!treeNode.checked){
			events.filtered.on.publish(applicationEvent);
		} else {
			events.filtered.off.publish(applicationEvent);
		}
		
    }

    function zTreeOnClick(treeEvent, treeId, treeNode) {
        var applicationEvent = {
			sender: packageExplorerController,
			entities: [model.getEntityById(treeNode.id)]
		};
		events.selected.on.publish(applicationEvent);
    }
	
	function onEntitySelected(applicationEvent) {
        if(applicationEvent.sender !== packageExplorerController) {
			var entity = applicationEvent.entities[0];
			var item = tree.getNodeByParam("id", entity.id, null);            
			tree.selectNode(item, false);
        }
	}
	
	
	
	
	
	
	
	/*
    function zTreeOnCheck(event, treeId, treeNode) {
        		
		var treeObj = $.fn.zTree.getZTreeObj("packageExplorerTree");
        var nodes = treeObj.getChangeCheckedNodes();
        
		var entityIds = [];
		for(var i = 0; i < nodes.length;i++) {
			nodes[i].checkedOld = nodes[i].checked; //Need for the ztree to set getChangeCheckedNodes correct
			entityIds.push(nodes[i].id);
		}
		
		publishOnVisibilityChanged(entityIds, treeNode.checked, "packageExplorerTree");
		
    }

    function zTreeOnClick(event, treeId, treeNode) {        
		publishOnEntitySelected(treeNode.id, "packageExplorerTree");
    }
    
	
    function onEntitySelected(event, entity) {
        if(event.sender != "packageExplorerTree") {
			var tree = $.fn.zTree.getZTreeObj("packageExplorerTree");   
            var item = tree.getNodeByParam("id", entity.id, null);
            tree.selectNode(item, false);         
        }   
		interactionLogger.logManipulation("PackageExplorerTree", "highlight", entity.id);
    }
    
    function onVisibilityChanged(event, ids, visible) {
        if(event.sender != "packageExplorerTree") {            
			var tree = $.fn.zTree.getZTreeObj("packageExplorerTree");
            
			for(var i = 0; i < ids.length;i++) {
				var item = tree.getNodeByParam("id", ids[i], null);
				tree.checkNode(item, visible, false, false);
				item.checkedOld = item.checked;
			}
        }
		
    }
    
    function onRelationsVisibilityChanged(event, entities, visible) {
        var tree = $.fn.zTree.getZTreeObj("packageExplorerTree");
        for(var i = 0; i < entities.length; i++) {
            var id = entities[i];
            
			var item = tree.getNodeByParam("id", id, null);
            tree.checkNode(item, visible, false, false);
            item.checkedOld = item.checked;
			interactionLogger.logManipulation("PackageExplorerTree", "uncheck", id);
        }
    }
	*/
    
    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();