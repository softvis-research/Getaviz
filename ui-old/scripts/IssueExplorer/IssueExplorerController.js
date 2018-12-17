var issueExplorerController = (function() {
    
	var issueExplorerTreeID = "issueExplorerTree";
	var jQIssueExplorerTree = "#issueExplorerTree";
	
	var tree;
	
	function initialize(){
    }
	
	function activate(rootDiv){
		//create zTree div-container
		var zTreeDiv = document.createElement("DIV");
		zTreeDiv.id = "zTreeDiv";
				
		var issueExplorerTreeUL = document.createElement("UL");
		issueExplorerTreeUL.id = issueExplorerTreeID;
		issueExplorerTreeUL.setAttribute("class", "ztree");
				
		zTreeDiv.appendChild(issueExplorerTreeUL);				
		rootDiv.appendChild(zTreeDiv);
				
		//create zTree
		prepareTreeView();
    }
	
	function reset(){
	    prepareTreeView();
	}
    
    function prepareTreeView() {
		var items = [];
        var entities = model.getAllIssues();

        var showAllItem = {id: "showall", name: "Show all classes", iconSkin: "zt", checked: true};
        items.push(showAllItem);

        var showOpenItem = {id: "showopen", name: "Show classes with open issues", iconSkin: "zt", checked: false};
        items.push(showOpenItem);

        var showOpenSecurityItem = {id: "showopensecurity", name: "Show classes with open security issues", iconSkin: "zt", checked: false};
        items.push(showOpenSecurityItem);

        var openItem = {id: "openItem", name: "Open issues", iconSkin: "zt", nocheck: true};
        items.push(openItem);

        var closedItem = {id: "closedItem", name: "Closed issues", iconSkin: "zt", nocheck: true};
        items.push(closedItem);

        var openSecurityItem = {id: "openSecurityItem", name: "Security issues", iconSkin: "zt", nocheck: true, parentId: "openItem"};
        items.push(openSecurityItem);

        var openNonSecurityItem = {id: "openNonSecurityItem", name: "Non-security issues", iconSkin: "zt", nocheck: true, parentId: "openItem"};
        items.push(openNonSecurityItem);

        var closedSecurityItem = {id: "closedSecurityItem", name: "Security issues", iconSkin: "zt", nocheck: true, parentId: "closedItem"};
        items.push(closedSecurityItem);

        var closedNonSecurityItem = {id: "closedNonSecurityItem", name: "Non-security issues", iconSkin: "zt", nocheck: true, parentId: "closedItem"};
        items.push(closedNonSecurityItem);

        entities.forEach(function(issue) {
            var parentId = "";
            if(issue.open === true) {
                if(issue.security === true) {
                    parentId = "openSecurityItem"
                } else {
                    parentId = "openNonSecurityItem"
                }
            } else {
                if(issue.security === true) {
                    parentId = "closedSecurityItem"
                } else {
                    parentId = "closedNonSecurityItem"
                }
            }


			var item = {id: issue.id, name: issue.qualifiedName, iconSkin: "zt", checked: false, parentId: parentId};
            items.push(item);
        });
        	       
		//zTree settings
        var settings = {
            check: {
                enable: true,
                chkStyle: "radio",
                radioType: "all"
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
                onCheck: zTreeOnCheck
            },
            view:{
                showLine: false,
                showIcon: false,
                selectMulti: false
            }
        };		
        //create zTree
        tree = $.fn.zTree.init( $(jQIssueExplorerTree), settings, items);
    }
    
    function zTreeOnCheck(treeEvent, treeId, treeNode) {
       var applicationEvent = {			 
            sender: issueExplorerController,
            entities: [treeNode.id]
        };

        var id = applicationEvent.entities[0];
        var entities = model.getEntitiesByType("Class");

        if(id === "showall") {
            canvasManipulator.changeTransparencyOfEntities(entities, 0.0);
        } else {
            canvasManipulator.changeTransparencyOfEntities(entities, 0.85);
            let relatedEntities = [];
            if(id === "showopen") {
                let issues = model.getAllIssues();
                issues.forEach(function(issue){
                    if(issue.open){
                        let newEntities = model.getEntitiesByIssue(issue.id);
                        relatedEntities = relatedEntities.concat(newEntities);
                    }
                });
            } else {
                if(id === "showopensecurity") {
                    let issues = model.getAllIssues();
                    issues.forEach(function(issue){
                        if(issue.open && issue.security){
                            let newEntities = model.getEntitiesByIssue(issue.id);
                            relatedEntities = relatedEntities.concat(newEntities);
                        }
                    });
                } else {
                    relatedEntities = model.getEntitiesByIssue(id);
                }
            }
            canvasManipulator.changeTransparencyOfEntities(relatedEntities, 0.0);
        }
	}

    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();
