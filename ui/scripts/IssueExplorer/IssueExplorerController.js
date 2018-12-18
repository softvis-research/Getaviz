var issueExplorerController = (function() {
    
	const issueExplorerTreeID = "issueExplorerTree";
	const jQIssueExplorerTree = "#issueExplorerTree";

    let tree;
	
	function initialize(){
    }
	
	function activate(rootDiv){
		//create zTree div-container
		let zTreeDiv = document.createElement("DIV");
		zTreeDiv.id = "zTreeDiv";
				
		let issueExplorerTreeUL = document.createElement("UL");
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
		let items = [];
        const entities = model.getAllIssues();

        const openItem = {id: "openItem", name: "Open issues", iconSkin: "zt", nocheck: true};
        items.push(openItem);

        const closedItem = {id: "closedItem", name: "Closed issues", iconSkin: "zt", nocheck: true};
        items.push(closedItem);

        const openSecurityItem = {id: "openSecurityItem", name: "Security issues", iconSkin: "zt", nocheck: true, parentId: "openItem"};
        items.push(openSecurityItem);

        const openNonSecurityItem = {id: "openNonSecurityItem", name: "Non-security issues", iconSkin: "zt", nocheck: true, parentId: "openItem"};
        items.push(openNonSecurityItem);

        const closedSecurityItem = {id: "closedSecurityItem", name: "Security issues", iconSkin: "zt", nocheck: true, parentId: "closedItem"};
        items.push(closedSecurityItem);

        const closedNonSecurityItem = {id: "closedNonSecurityItem", name: "Non-security issues", iconSkin: "zt", nocheck: true, parentId: "closedItem"};
        items.push(closedNonSecurityItem);

        entities.forEach(function(issue) {
            let parentId = "";
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

			const item = {id: issue.id, name: issue.qualifiedName, iconSkin: "zt", checked: false, parentId: parentId};
            items.push(item);
        });
        	       
		//zTree settings
        const settings = {
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
	    let id = ""
        if (treeNode.checked){
            id = treeNode.id;
            //events.filtered.on.publish(applicationEvent);
        } //else {
            //events.filtered.off.publish(applicationEvent);
        //}

        const applicationEvent = {
	        sender: issueExplorerController,
            issueFilterId: id
        };
        events.config.filterSettings.publish(applicationEvent);


	}

    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();
