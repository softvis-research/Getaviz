var patternExplorerController = (function() {
    
	var patternExplorerTreeID = "patternExplorerTree";
	var jQPatternExplorerTree = "#patternExplorerTree";
	
	var tree;
	
	var iconFiles = {
		//packageIcon: 	"scripts/PatternExplorer/images/package.png",
		typeIcon: 		"scripts/PatternExplorer/images/type.png",
		//fieldIcon: 		"scripts/PatternExplorer/images/field.png",
		//methodIcon:		"scripts/PatternExplorer/images/method.png",
		antipatternIcon:	"scripts/PatternExplorer/images/antipattern.png"
	};
	
	function initialize(){
	}
	
	function showIconForTree(treeId, treeNode) {
        return treeNode.parentId != "";
    }
	
	function activate(rootDiv){		
		//create zTree div-container
		var zTreeDiv = document.createElement("DIV");
		zTreeDiv.id = "zTreeDiv";
				
		var patternExplorerTreeUL = document.createElement("UL");
		patternExplorerTreeUL.id = patternExplorerTreeID;
		patternExplorerTreeUL.setAttribute("class", "ztree");
				
		zTreeDiv.appendChild(patternExplorerTreeUL);				
		rootDiv.appendChild(zTreeDiv);
				
		//create zTree
		prepareTreeView();
		
		events.selected.on.subscribe(onEntitySelected);
		events.versionSelected.on.subscribe(onVersionSelected);
		events.versionSelected.off.subscribe(offVersionSelected);
    }
	
	function reset(){
        prepareTreeView();
	}
    
    function prepareTreeView() {
        
        var entities = model.getAllEntities();
        var items = [];
        var components = [];
        
        item = { id: "stk", open: false, nocheck: true, parentId: "", name: "Subtype Knowledge"};
        items.push(item);
        item = { id: "component", open: false, nocheck: true, parentId: "", name: "Strongly Connected Components", icon: iconFiles.packageIcon, iconSkin: "zt"};
        items.push(item);
                //build items for ztree
        entities.forEach(function(entity) {
			
            if(entity.type == "stk" || entity.type == "component" || entity.type == "Class") {
				switch(entity.type) {
                    case "Class":
						for(var i = 0; i < entity.antipattern.length; i++) {
							if(entity.antipattern[i].type != "cd") {
								item = { id: entity.id, open: false, parentId: entity.antipattern[i].id, name: entity.name + " <<" + entity.roles[i] + ">>", icon: iconFiles.typeIcon, nocheck: true, iconSkin: "zt"};
								items.push(item);
							}
						}
						if(entity.component != "" && entity.component != undefined) {
							item = { id: entity.id, open: false, parentId: entity.component, name: entity.name, icon: iconFiles.typeIcon, nocheck: true, iconSkin: "zt"};
							items.push(item);
						}
						break;
					case "stk":
				   // case "cd":
						item = { id: entity.id, open: false, checked: false, parentId: entity.type, name: entity.name, icon: iconFiles.antipatternIcon, iconSkin: "zt"};
						items.push(item);
						break;
					case "component":
						item = { id: entity.id, open: false, checked: false, parentId: entity.type, name: entity.name, version: entity.version, icon: iconFiles.antipatternIcon, iconSkin: "zt"};
						components.push(item);
						break;
					default:
						return;
					}
				}
			}
        );
		
        //Sortierung nach Typ und Alphanumerisch
        items.sort(
            function(a,b) {
                var sortStringA = "";
                switch(a.icon){
                    case iconFiles.packageIcon:
                        sortStringA = "1" + a.name.toUpperCase();
                        break;
                    case iconFiles.typeIcon:
                        sortStringA = "2" + a.name.toUpperCase();
                        break;
                    case iconFiles.fieldIcon:
                        sortStringA = "3" + a.name.toUpperCase();
                        break;
                    case iconFiles.methodIcon:
                        sortStringA = "4" + a.name.toUpperCase();
                        break;
                    default:
                        sortStringA = "0" + a.name.toUpperCase();
                    }
                    
                    var sortStringB = "";
                    switch(b.icon){
                        case iconFiles.packageIcon:
                            sortStringB = "1" + b.name.toUpperCase();
                            break;
                        case iconFiles.typeIcon:
                            sortStringB = "2" + b.name.toUpperCase();
                            break;
                        case iconFiles.fieldIcon:
                            sortStringB = "3" + b.name.toUpperCase();
                            break;
                        case iconFiles.methodIcon:
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
        
        // Sortierung nach Anzahl Kindelementen
        components.sort(
            function(a,b) {
                var nocA = 0;
                var nocB = 0;
                for(var i = 0; i < items.length; i++) {
                    if(items[i].parentId == a.id) {
                        nocA++;
                    }
                    if(items[i].parentId == b.id) {
                        nocB++;
                    }
                }
                
                return nocB - nocA;
            }
        );
        items = items.concat(components);
			       
		//zTree settings
		var settings = {
            check: {
                enable: false,
                //chkboxType: {"Y": "ps", "N": "s"},
                chkStyle: "radio",
			radioType: "all"
            },
            data: {
                simpleData: {
                enable: true,
                idKey: "id",
                pIdKey: "parentId",
                rootPId: ""
                }
            },
            callback: {
                onClick: zTreeOnClick
            },
            view:{
                showLine: false,
                showIcon: showIconForTree,
                selectMulti: false
            }
        };

        //create zTree
		
        tree = $.fn.zTree.init( $(jQPatternExplorerTree), settings, items);
    }
    
    function zTreeOnClick(treeEvent, treeId, treeNode) {        
		
		var applicationEvent = {			 
			sender: patternExplorerController,
			entities: [model.getEntityById(treeNode.id)]
		};
		
		treeNode.checked = true;

        var type = applicationEvent.entities[0].type;
		switch(type) {
			case "stk":
				events.antipattern.on.publish(applicationEvent);
				break;
			case "component":
				events.componentSelected.on.publish(applicationEvent);
				break;
			default:
				//events.selected.on.publish(applicationEvent);
		}
    }
	
    function onEntitySelected(applicationEvent) {
        if(applicationEvent.sender !== patternExplorerController) {
            var entity = applicationEvent.entities[0];
            var item = tree.getNodeByParam("id", entity.id, null);            
            tree.selectNode(item, false);         
        }
    }
	
	function offVersionSelected(applicationEvent) {
		var nodes = tree.getNodesByParam("version", applicationEvent.entities[0]);		
		tree.hideNodes(nodes);
	}
	
	function onVersionSelected(applicationEvent) {
		var nodes = tree.getNodesByParam("version", applicationEvent.entities[0]);
		tree.showNodes(nodes);
	}
    
    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();
