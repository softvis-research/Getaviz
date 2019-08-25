var patternExplorerController;
patternExplorerController = (function () {

    var patternExplorerTreeID = "patternExplorerTree";
    var jQPatternExplorerTree = "#patternExplorerTree";

    var tree;

    function initialize() {
    }

    function activate(rootDiv) {
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

    function reset() {
        prepareTreeView();
    }

    function prepareTreeView() {

        var entities = model.getAllEntities();
        var items = [];
        var components = [];
        var stkItem = {
            id: "stk",
            open: false,
            nocheck: true,
            parentId: "",
            glossary: "stk",
            name: "Subtype Knowledge",
            help: true
        };
        items.push(stkItem);
        var componentItem = {
            id: "component",
            open: false,
            nocheck: true,
            parentId: "",
            glossary: "cd",
            name: "Circular Dependency"
        };
        items.push(componentItem);
        //build items for ztree
        entities.forEach(function (entity) {
                if (entity.type == "stk" || entity.type == "component" || entity.type == "Class") {
                    switch (entity.type) {
                        case "Class":
                            var duplicate = false;
                            items.forEach(function (existingElement) {
                                if (existingElement.name == entity.name) {
                                    duplicate = true;
                                }
                            });
                            if (duplicate) {
                                break;
                            }
                            for (var i = 0; i < entity.antipattern.length; i++) {
                                if (entity.antipattern[i].type == "stk") {
                                    var role = model.getRole(entity.id, entity.antipattern[i].id);
                                    var item = {
                                        id: entity.id,
                                        open: false,
                                        parentId: entity.antipattern[i].id,
                                        name: entity.name + "  «" + role + "»",
                                        nocheck: true,
                                        iconSkin: "zt"
                                    };
                                    items.push(item);
                                }
                            }
                            if (entity.component != "" && entity.component != undefined) {
                                item = {
                                    id: entity.id,
                                    open: false,
                                    parentId: entity.component,
                                    name: entity.name,
                                    nocheck: true,
                                    iconSkin: "zt"
                                };
                                items.push(item);
                            }
                            break;
                        case "stk":
                            item = {
                                id: entity.id,
                                open: false,
                                checked: false,
                                parentId: entity.type,
                                name: entity.name,
                                versions: entity.versions,
                                iconSkin: "zt"
                            };
                            components.push(item);
                            break;
                        case "component":
                            item = {
                                id: entity.id,
                                open: false,
                                checked: false,
                                parentId: entity.type,
                                name: entity.name,
                                version: entity.version,
                                versions: entity.versions,
                                iconSkin: "zt"
                            };
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
            function (a, b) {
                var sortStringA = a.name.toUpperCase()
                var sortStringB = b.name.toUpperCase()

                if (sortStringA < sortStringB) {
                    return -1;
                }
                if (sortStringA > sortStringB) {
                    return 1;
                }

                return 0;
            }
        );

        // Sortierung nach Anzahl Kindelementen
        components.sort(
            function (a, b) {
                var n1 = a.name.indexOf(" ") + 1;
                var n2 = b.name.indexOf(" ") + 1;
                var v1 = a.name.substring(n1);
                var v2 = b.name.substring(n2);
                return v1 - v2;
            }
        );

        items = items.concat(components);

        //zTree settings
        var settings = {
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
            view: {
                showLine: false,
                showIcon: false,
                selectMulti: false,
                nameIsHTML: true,
                fontCss: getFont
            }
        };

        //create zTree
        tree = $.fn.zTree.init($(jQPatternExplorerTree), settings, items);
    }

    function getFont(treeId, node) {
        return node.glossary ? {'text-decoration':'underline', 'font-style':'italic'} : {};
    }

    function zTreeOnClick(treeEvent, treeId, treeNode) {

        var applicationEvent = {
            sender: patternExplorerController,
            entities: [model.getEntityById(treeNode.id)]
        };
        if (treeNode.id == "component") {
            window.open("./glossary/glossary.html#cd", 'glossary').focus();
        }
        if (treeNode.id == "stk") {
            window.open("./glossary/glossary.html#stk", 'glossary');
        }

        if (applicationEvent.entities[0] === undefined) {
            return;
        }

        var type = applicationEvent.entities[0].type;

        switch (type) {
            case "stk":
                events.antipattern.on.publish(applicationEvent);
                break;
            case "component":
                events.componentSelected.on.publish(applicationEvent);
                break;
            default:
                if (applicationEvent.entities[0].id == "stk") {
                    window.open("./glossary/Glossary.html", '_blank');
                }
        }
    }

    function onEntitySelected(applicationEvent) {
        if (applicationEvent.sender !== patternExplorerController) {
            var entity = applicationEvent.entities[0];
            var item = tree.getNodeByParam("name", entity.name, null);
            if (item == null) {
                tree.cancelSelectedNode()
            } else {
                tree.selectNode(item, false);
            }
        }
    }

    function offVersionSelected() {
        var selectedVersions = model.getSelectedVersions();
        var nodes = [];
        tree.getNodesByParam("parentId", "component").forEach(function (node) {
            var hide = true;
            node.versions.forEach(function (version) {
                if (selectedVersions.includes(version)) {
                    hide = false;
                }
            });
            if (hide) {
                nodes.push(node);
            }
        });
        tree.getNodesByParam("parentId", "stk").forEach(function (node) {
            var hide = true;
            node.versions.forEach(function (version) {
                if (selectedVersions.includes(version)) {
                    hide = false;
                }
            });
            if (hide) {
                nodes.push(node);
            }
        });
        tree.hideNodes(nodes);
    }

    function onVersionSelected(applicationEvent) {
        var nodes = [];
        tree.getNodesByParam("parentId", "component").forEach(function (node) {
            if (node.versions.includes(applicationEvent.entities[0])) {
                nodes.push(node);
            }
        });
        tree.getNodesByParam("parentId", "stk").forEach(function (node) {
            if (node.versions.includes(applicationEvent.entities[0])) {
                nodes.push(node);
            }
        });
        tree.showNodes(nodes);
    }

    return {
        initialize: initialize,
        activate: activate,
        reset: reset
    };
})();
