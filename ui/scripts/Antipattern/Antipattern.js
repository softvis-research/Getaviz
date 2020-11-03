var antipatternController = (function() {

    var AntipatternTreeID = "AntipatternTree";
    var jQAntipatternTree = "#AntipatternTree";
    var orange = "#ff5e1f";

    let antipatternConfig = {
        typeIcon: 		"scripts/Antipattern/images/type.png",
        methodIcon:		"scripts/Antipattern/images/method.png",
        colorAnimationColors:                // # available colors for color animation
            [ orange, "red" ],
        minColorChangeFrequency: 1000,       // # milliseconds - min freq for color animation
        maxColorChangeFrequency: 500,        // # milliseconds - max freq for color animation
    };

    var controllerConfig = {

    };

    function initialize() {

    }

    function activate(rootDiv) {
        var zTreeDiv = document.createElement("DIV");
        zTreeDiv.id = "zTreeDiv";

        var AntipatternTreeUL = document.createElement("UL");
        AntipatternTreeUL.id = AntipatternTreeID;
        AntipatternTreeUL.setAttribute("class", "ztree");

        zTreeDiv.appendChild(AntipatternTreeUL);
        rootDiv.appendChild(zTreeDiv);

        prepareAntipattern();
        events.selected.on.subscribe(onEntitySelected);
    }

    function reset() {
        prepareAntipattern();
    }

    function antipatternFinder(){
        let featureenvy = {id: "featureenvy", children: [],name: "Feature Envy", checked: false};
        let brainmethod = {id: "brainmethod", children: [],name: "Brain Method", checked: false};
        let brainclass = {id: "brainclass", children: [], name: "Brain Class", checked: false}
        let dataclass = {id: "dataclass", children: [], name: "Data Class", checked: false};
        let godclass  = {id: "godclass", children: [], name: "God Class", checked: false};
        let items = []

        model.getAllEntities().forEach(entity => {
            let methodAP = {id: entity.id, name: entity.name, iconSkin: "zt", chkDisabled:true, icon: antipatternConfig.methodIcon,checked: false};
            let classAP = {id: entity.id, name: entity.name, iconSkin: "zt", chkDisabled:true, icon: antipatternConfig.typeIcon,checked: false};
            if(entity.godclass === 'TRUE'){
                if(!items.includes(godclass)){items.push(godclass)}
                godclass.children.push(classAP);
            } else if (entity.brainclass === "TRUE"){
                if(!items.includes(brainclass)){items.push(brainclass)}
                brainclass.children.push(classAP);
            } else  if(entity.dataclass === 'TRUE'){
                if(!items.includes(dataclass)){items.push(dataclass)}
                dataclass.children.push(classAP);
            } else if(entity.brainmethod === 'TRUE'){
                if(!items.includes(brainmethod)){items.push(brainmethod)}
                brainmethod.children.push(methodAP);
            } else if(entity.featureenvy === 'TRUE'){
                if(!items.includes(featureenvy)){items.push(featureenvy)}
                featureenvy.children.push(methodAP);
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
                onCheck: checkedOrUnchecked,
                onClick: zTreeOnClick
            },
            view:{
                showLine: false,
                showIcon: true,
                selectMulti: true
            }
        };
        tree = $.fn.zTree.init( $(jQAntipatternTree), settings, items);
    }

    function checkedOrUnchecked(treeEvent, treeId, treeNode){
        if(treeNode.checked){
            decide(treeEvent, treeId, treeNode)
        } else {
            resetMarked()
        }
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
        canvasManipulator.changeColorOfEntities([entity], orange)
        let colorAnimation = new MetricAnimationColor(antipatternConfig.minColorChangeFrequency,
            antipatternConfig.maxColorChangeFrequency);
        let metric;
        colorAnimation.addMetric(metric, antipatternConfig.colorAnimationColors, 1);
        entity.metricAnimationColor = colorAnimation;
        canvasManipulator.startColorAnimationForEntity(entity, colorAnimation);
        setTimeout(() =>  canvasManipulator.stopColorAnimationForEntity(entity), 2150)
    }

    function resetMarked(){
        canvasManipulator.resetColorOfEntities(model.getAllEntities());
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
            tree.checkNode(item, false, false)
        }
    }

    return {
        initialize: initialize,
        activate: activate,
        reset: reset,
    };


})();


