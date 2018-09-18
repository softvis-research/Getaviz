var legendController = (function() {
    
	let versionExplorerTreeID = "legendTree";
	let jQVersionExplorerTree = "#" + versionExplorerTreeID;
	let tree;
	let items = [];

	let icons = {
        redCircle: 	        "scripts/Legend/images/circle_red.png",
        blueCircle: 	    "scripts/Legend/images/circle_blue.png",
        greenCircle: 	    "scripts/Legend/images/circle_green.png",
        grayCircle: 	    "scripts/Legend/images/circle_gray.png",
        purpleCircle:       "scripts/Legend/images/circle_purple.png",
        yellowCircle:       "scripts/Legend/images/circle_yellow.png",
        lightBlueCircle:    "scripts/Legend/images/circle_blue_light.png",
        greenRedGradient: 	"scripts/Legend/images/gradient_green-red.png",
        circleWidth:        "scripts/Legend/images/circle_width.png",
        navigation:         "scripts/Legend/images/navigation.png",
        leftMouseButton:    "scripts/Legend/images/left.png",
        rightMouseButton:   "scripts/Legend/images/right.png",
        midMouseButton:     "scripts/Legend/images/middle.png",
        doubleClick:        "scripts/Legend/images/double.png",
        scrolling:          "scripts/Legend/images/scrolling.png"
    };
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
        let item = { id: entry.name, parentId: parent, name: entry.name, iconSkin: "zt", icon: icons[entry.icon], open: true, glossary: entry.glossary };
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
