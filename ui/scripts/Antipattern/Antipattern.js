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

    function  listedAntipattern() {
        var items = [];
        item = {id: "foundGodclass", parentId: "", name: "Eine Gottklasse", iconSkin: "zt", checked: false};
        items.push(item);
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
                chkStyle: "radio",
                radioType: "level"
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
        tree = $.fn.zTree.init( $(jQVersionExplorerTree), settings, items);
    }

    function decide(treeEvent, treeId, treeNode) {
        resetMarked()
        model.getAllEntities().forEach(entity => {
            switch (treeNode.id) {
                case "godclass":
                    if(entity.godclass === 'TRUE'){changeColor(entity)}
                    break;
                case "brainclass":
                    if(entity.brainclass === 'TRUE'){changeColor(entity)}
                    break;
                case "dataclass":
                    if(entity.dataclass === 'TRUE'){changeColor(entity)}
                    break;
                case "brainmethod":
                    if(entity.brainmethod === 'TRUE'){changeColor(entity)}
                    break;
                case "featureenvy":
                    if(entity.featureenvy === 'TRUE'){changeColor(entity)}
                    break;
            }
        })
    }

    function changeColor(entity){
        canvasManipulator.changeColorOfEntities([entity], "#FF4500");
    }

    function resetMarked(){
        model.getAllEntities().forEach(entity => {
            canvasManipulator.resetColorOfEntities([entity]);
        })
    }

    return {
        initialize: initialize,
        activate: activate,
        reset: reset,
    };


})();

