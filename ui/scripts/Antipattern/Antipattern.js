var antipatternController = (function() {

    var versionExplorerTreeID = "versionExplorerTree";
    var jQVersionExplorerTree = "#versionExplorerTree";

    function initialize() {
    }

    function activate(rootDiv) {
        var zTreeDiv = document.createElement("DIV");
        zTreeDiv.id = "zTreeDiv";

        var versionExplorerTreeUL = document.createElement("UL");
        versionExplorerTreeUL.id = versionExplorerTreeID;
        versionExplorerTreeUL.setAttribute("class", "ztree");

        zTreeDiv.appendChild(versionExplorerTreeUL);
        rootDiv.appendChild(zTreeDiv);

        prepareAntipattern();
    }

    function reset() {
        prepareAntipattern();
    }

    function prepareAntipattern() {

        var items = [];

        item = {id: "godclass", parentId: "", name: "God Class", iconSkin: "zt", checked: false};
        items.push(item);

        item = {id: "featureenvy", parentId: "", name: "Feature Envy", iconSkin: "zt", checked: false};
        items.push(item);

        item = {id: "dataclass", parentId: "", name: "Data Class", iconSkin: "zt", checked: false};
        items.push(item);

        item = {id: "brainmethod", parentId: "", name: "Brain Method", iconSkin: "zt", checked: false};
        items.push(item);

        item = {id: "brainclass", parentId: "", name: "Brain Class", iconSkin: "zt", checked: false};
        items.push(item);

        var settings = {
            check: {
                enable: true,
                checkboxType: {"Y": "ps", "N": "s"}
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
                onCheck: decide
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

    function decide(treeEvent, treeId, treeNode) {
        switch (treeNode.id){
            case "godclass":
                godclass(treeEvent, treeId, treeNode);
                break;
            case "brainclass":
                brainclass(treeEvent, treeId, treeNode);
                break;
            case "dataclass":
                dataclass(treeEvent, treeId, treeNode);
                break;
        }
    }

    function godclass(treeEvent, treeId, treeNode) {
        if (treeNode.checked){
            model.getAllEntities().forEach(entity => {
                if(entity.godclass === 'TRUE'){
                    canvasManipulator.changeColorOfEntities([entity], "#FF4500");}
            })
        } else {
            model.getAllEntities().forEach(entity => {
                if(entity.godclass === 'TRUE'){
                    canvasManipulator.resetColorOfEntities([entity]);}
            })

        }
    }

    function brainclass(treeEvent, treeId, treeNode) {
        if (treeNode.checked){
            model.getAllEntities().forEach(entity => {
                if(entity.brainclass === 'TRUE'){
                    canvasManipulator.changeColorOfEntities([entity], "#FF4500");}
            })
        } else {
            model.getAllEntities().forEach(entity => {
                if(entity.brainclass === 'TRUE'){
                    canvasManipulator.resetColorOfEntities([entity]);}
            })
        }
    }

    function dataclass(treeEvent, treeId, treeNode) {
        if (treeNode.checked){
            console.log("dataclass checked")
            model.getAllEntities().forEach(entity => {
                if(entity.dataclass === 'TRUE'){
                    console.log("DataclassMethod")
                    canvasManipulator.changeColorOfEntities([entity], "#FF4500");}
            })
        } else {
            model.getAllEntities().forEach(entity => {
                if(entity.dataclass === 'TRUE'){
                    canvasManipulator.resetColorOfEntities([entity]);}
            })
        }
    }

    return {
        initialize: initialize,
        activate: activate,
        reset: reset,
    };


})();

