var legendController = (function() {
    
	let versionExplorerTreeID = "legendTree";
	let jQVersionExplorerTree = "#" + versionExplorerTreeID;
	let tree;
	let items = [];

	var controllerConfig = {
        entries: []
    };

	function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);
    }

	function activate(rootDiv){
		//create zTree div-container
		let zTreeDiv = document.createElement("DIV");
		zTreeDiv.id = "zTreeDiv";

		let versionExplorerTreeUL = document.createElement("UL");
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

	function createItem(entry, parent) {
        let item = { id: entry.name, parentId: parent, name: entry.name, iconSkin: "zt", icon: entry.icon, open: entry.open, glossary: entry.glossary };
        items.push(item);
        if(entry.entries !== undefined) {
            let parentid = entry.name;
            entry.entries.forEach(function(entry){
                createItem(entry, parentid);
            });
        }
    }

    function getFont(treeId, node) {
        return node.glossary ? {'text-decoration':'underline', 'font-style':'italic'} : {};
    }
    
    function prepareTreeView() {
        items = [];
		controllerConfig.entries.forEach(createItem);

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
            view:{
                showLine: false,
                showIcon: true,
                selectMulti: false,
                nameIsHTML: true,
                fontCss: getFont
            },
            callback: {
                onClick: zTreeOnClick
            },
        };		
        tree = $.fn.zTree.init( $(jQVersionExplorerTree), settings, items);
    }

    function zTreeOnClick(treeEvent, treeId, treeNode) {
	    if(treeNode.glossary) {
            window.open("./glossary/glossary.html#" + treeNode.glossary,'glossary');
	    }
    }

    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();
