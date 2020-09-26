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
        events.selected.on.subscribe(onEntitySelected);
    }

    function reset() {
        prepareAntipattern();
    }

    function antipatternFinder(){
        let featureenvy = {id: "featureenvy", children: [],name: "Feature Envy", iconSkin: "zt", checked: false};
        let brainmethod = {id: "brainmethod", children: [],name: "Brain Method", iconSkin: "zt", checked: false};
        let brainclass = {id: "brainclass", children: [], name: "Brain Class", iconSkin: "zt", checked: false}
        let dataclass = {id: "dataclass", children: [], name: "Data Class", iconSkin: "zt", checked: false};
        let godclass  = {id: "godclass", children: [], name: "God Class", iconSkin: "zt", checked: false};
        let items = []

        model.getAllEntities().forEach(entity => {
            let item = {id: entity.id, name: entity.name, iconSkin: "zt", checked: false};
            if(entity.godclass === 'TRUE'){
                if(!items.includes(godclass)){items.push(godclass)}
                godclass.children.push(item);
            } else if (entity.brainclass === "TRUE"){
                if(!items.includes(brainclass)){items.push(brainclass)}
                brainclass.children.push(item);
            } else  if(entity.dataclass === 'TRUE'){
                if(!items.includes(dataclass)){items.push(dataclass)}
                dataclass.children.push(item);
            } else if(entity.brainmethod === 'TRUE'){
                if(!items.includes(brainmethod)){items.push(brainmethod)}
                brainmethod.children.push(item);
            } else if(entity.featureenvy === 'TRUE'){
                if(!items.includes(featureenvy)){items.push(featureenvy)}
                featureenvy.children.push(item);
            }})

        return items
    }

    function prepareAntipattern() {

        var items = antipatternFinder()

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
                onCheck: decide,
                onClick: zTreeOnClick
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

    function zTreeOnClick(treeEvent, treeId, treeNode) {
        var applicationEvent = {
            sender: antipatternController,
            entities: [model.getEntityById(treeNode.id)]
        };
        events.selected.on.publish(applicationEvent);
    }

    function onEntitySelected(applicationEvent) {
        if(applicationEvent.sender !== antipatternController) {
            var entity = applicationEvent.entities[0];
            var item = tree.getNodeByParam("id", entity.id, null);
            tree.selectNode(item, false);
        }
    }

    return {
        initialize: initialize,
        activate: activate,
        reset: reset,
    };


})();


