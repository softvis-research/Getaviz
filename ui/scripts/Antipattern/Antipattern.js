var antipatternController = (function() {

    var AntipatternTreeID = "AntipatternTree";
    var jQAntipatternTree = "#AntipatternTree";

    let antipatternConfig = {
        orange:         "#ff5e1f", // main color
        pink:           "#ff55f0",
        purple:         "#e155ff",
        green:          "#78ff55",
        cyan:           "#55fffe",
        yellow:         "#fffe55",
        typeIcon: 		"scripts/Antipattern/images/type.png",
        methodIcon:		"scripts/Antipattern/images/method.png",
        // colorAnimationColors:                // # available colors for color animation
        //     [ "#ff5e1f", ],
        minColorChangeFrequency: 1000,       // # milliseconds - min freq for color animation
        maxColorChangeFrequency: 500,        // # milliseconds - max freq for color animation
    };

    function initialize() {}

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
        let featureenvy = {id: "featureenvy", children: [],name: "Feature Envy", ap: true};
        let brainmethod = {id: "brainmethod", children: [],name: "Brain Method", ap: true};
        let brainclass = {id: "brainclass", children: [], name: "Brain Class", ap: true};
        let dataclass = {id: "dataclass", children: [], name: "Data Class", ap: true};
        let godclass  = {id: "godclass", children: [], name: "God Class", ap: true};
        let items = []

        model.getAllEntities().forEach(entity => {
            let methodAP = {id: entity.id, name: entity.name, iconSkin: "zt", icon: antipatternConfig.methodIcon,checked: false};
            let classAP = {id: entity.id, name: entity.name, iconSkin: "zt", icon: antipatternConfig.typeIcon,checked: false};
            if(entity.godclass === 'TRUE'){
                if(!items.includes(godclass)){items.push(godclass)}
                godclass.children.push(classAP);
            }
            if (entity.brainclass === "TRUE"){
                if(!items.includes(brainclass)){items.push(brainclass)}
                brainclass.children.push(classAP);
            }
            if(entity.dataclass === 'TRUE'){
                if(!items.includes(dataclass)){items.push(dataclass)}
                dataclass.children.push(classAP);
            }
            if(entity.brainmethod === 'TRUE'){
                if(!items.includes(brainmethod)){items.push(brainmethod)}
                brainmethod.children.push(methodAP);
            }
            if(entity.featureenvy === 'TRUE'){
                if(!items.includes(featureenvy)){items.push(featureenvy)}
                featureenvy.children.push(methodAP);
            }})

        return items
    }

    function prepareAntipattern() {

        var items = antipatternFinder()

        var settings = {
            data: {
                simpleData: {
                    enable:true,
                    idKey: "id",
                    pIdKey: "parentId",
                    rootPId: ""
                }
            },
            callback: {
                onClick: objectOrGroup
            },
            view:{
                showLine: false,
                showIcon: true,
                selectMulti: true
            }
        };
        tree = $.fn.zTree.init( $(jQAntipatternTree), settings, items);
    }

    /**
     * If an Anti-Pattern group is chosen, color all the antipattern objects
     * If a method or class object is chosen, mark them read and flyto
     */
    function objectOrGroup(treeEvent, treeId, treeNode){
        if(treeNode.ap){
            decide(treeEvent, treeId, treeNode)
        } else {
            zTreeOnClick(treeEvent, treeId, treeNode)
        }
    }


    function decide(treeEvent, treeId, treeNode) {
        resetColor();
        model.getAllEntities().forEach(entity => {
            switch (treeNode.id) {
                case "godclass":
                    if(entity.godclass === 'TRUE'){changeColor(entity, antipatternConfig.yellow)}
                    break;
                case "brainclass":
                    if(entity.brainclass === 'TRUE'){changeColor(entity, antipatternConfig.purple)}
                    break;
                case "dataclass":
                    if(entity.dataclass === 'TRUE'){changeColor(entity, antipatternConfig.cyan)}
                    break;
                case "brainmethod":
                    if(entity.brainmethod === 'TRUE'){changeColor(entity, antipatternConfig.pink)}
                    break;
                case "featureenvy":
                    if(entity.featureenvy === 'TRUE'){changeColor(entity, antipatternConfig.green)}
                    break;
            }})
    }

    function changeColor(entity, color){
            console.log(entity)
            let colorAnimation = new MetricAnimationColor(antipatternConfig.minColorChangeFrequency,
                antipatternConfig.maxColorChangeFrequency);
            let metric;
            colorAnimation.addMetric(metric, [antipatternConfig.orange, color], 1);
            entity.metricAnimationColor = colorAnimation;
            canvasManipulator.startColorAnimationForEntity(entity, colorAnimation);
            setTimeout(() => canvasManipulator.stopColorAnimationForEntityColorStays(entity, antipatternConfig.orange), 3000)
            canvasManipulator.changeColorOfEntities([entity], antipatternConfig.orange)
    }

    function resetColor(){
        model.getAllEntities().forEach(entity => {
                canvasManipulator.stopColorAnimationForEntity(entity);
        })
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


