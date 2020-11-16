var packageExplorerController = (function () {

	let packageExplorerTreeID = "packageExplorerTree";
	let jQPackageExplorerTree = "#packageExplorerTree";

	let tree;
	let items = [];

	var entityTypesForSearch = ["Namespace", "Class", "Interface", "Report", "FunctionGroup"];
	const domIDs = {
		zTreeDiv: "zTreeDiv",
		searchDiv: "searchDiv",
		searchInput: "searchField"
	}

	let controllerConfig = {
		projectIcon: "scripts/PackageExplorer/images/project.png",
		packageIcon: "scripts/PackageExplorer/images/package.png",
		typeIcon: "scripts/PackageExplorer/images/type.png",
		fieldIcon: "scripts/PackageExplorer/images/field.png",
		methodIcon: "scripts/PackageExplorer/images/method.png",
		elementsSelectable: true,

		showSearchField: true,
		entityTypesForSearch: entityTypesForSearch,

		//abap specific
		abap: true,
		useMultiselect: true,
		color: "darkred",

		namespace: "scripts/PackageExplorer/images/abap/namespace.png",
		class: "scripts/PackageExplorer/images/abap/class.png",
		localClass: "scripts/PackageExplorer/images/abap/localClass.png",
		interface: "scripts/PackageExplorer/images/abap/interface.png",
		localInterface: "scripts/PackageExplorer/images/abap/localInterface.png",
		functionGroup: "scripts/PackageExplorer/images/abap/fugr.png",
		reportDistrict: "scripts/PackageExplorer/images/abap/report_district.png",
		reportbuilding: "scripts/PackageExplorer/images/abap/report_building.png",
		attribute: "scripts/PackageExplorer/images/abap/attribute.png",
		form_fumo_meth: "scripts/PackageExplorer/images/abap/form&&fumo&&method.png"

	};

	var selectedEntities = [];

	function initialize(setupConfig) {
		application.transferConfigParams(setupConfig, controllerConfig);
	}

	function activate(rootDiv) {

		if (controllerConfig.showSearchField) {
			//search field
			let searchDiv = document.createElement("DIV");
			searchDiv.id = domIDs.searchDiv;
			searchDiv.setAttribute("ignoreTheme", "true");

			const cssLink = document.createElement("link");
			cssLink.type = "text/css";
			cssLink.rel = "stylesheet";
			cssLink.href = "scripts/PackageExplorer/tt.css";
			document.getElementsByTagName("head")[0].appendChild(cssLink);

			//create search input field
			const searchInput = document.createElement("INPUT");
			searchInput.id = domIDs.searchInput;
			searchInput.type = "text";

			searchDiv.appendChild(searchInput);
			rootDiv.appendChild(searchDiv);

			$("#" + domIDs.searchInput).jqxInput({ theme: "metro", width: "100%", height: "30px", placeHolder: "Search" });
		}

		//create zTree div-container
		let zTreeDiv = document.createElement("DIV");
		zTreeDiv.id = domIDs.zTreeDiv;

		let packageExplorerTreeUL = document.createElement("UL");
		packageExplorerTreeUL.id = packageExplorerTreeID;
		packageExplorerTreeUL.setAttribute("class", "ztree");

		zTreeDiv.appendChild(packageExplorerTreeUL);
		rootDiv.appendChild(zTreeDiv);

		//create zTree
		prepareTreeView();

		if (controllerConfig.showSearchField) {
			initializeSearch();
		}

		events.selected.on.subscribe(onEntitySelected);
		events.selected.off.subscribe(onEntityUnselected);
	}

	function reset() {
		prepareTreeView();
	}

	function initializeSearch() {
		var relevantEntities = [];

		model.getAllEntities().forEach(function(entity) {
			if (entityTypesForSearch.includes(entity.type)) {
				//ignore local classes and local interfaces
				if ((entity.type === "Class" || entity.type === "Interface") && entity.belongsTo.type !== "Namespace") {
					return;
				}
				relevantEntities.push(entity);
			}
		})

		var suggestions = new Bloodhound({
			local: relevantEntities,
			datumTokenizer: function (entity) {
				return Bloodhound.tokenizers.whitespace(entity.name);
			},
			queryTokenizer: Bloodhound.tokenizers.whitespace,
			limit: 20
		});
		suggestions.initialize();

		$("#" + domIDs.searchInput).typeahead(
			{
				hint: true,
				highlight: true,
				minLength: 3
			}, {
			name: "suggestions",
			// displayKey: "qualifiedName",
			source: suggestions.ttAdapter(),
			templates: {
				empty: Handlebars.compile('<div class="result"><p>no entities found</p></div>'),
				suggestion: Handlebars.compile('<div class="result"><p class="name">{{name}}</p><p class="entityType">{{type}}</p></div>')
			}
		});
	}

	function prepareTreeView() {

		let entities = model.getCodeEntities();

		let items = [];
		//controllerConfig.entries.forEach(createItem);

		//build items for ztree
		entities.forEach(function (entity) {

			var item;

			if (entity.belongsTo === undefined) {
				//rootpackages
				if (entity.type !== "issue" && entity.type !== "Macro"
					&& entity.type !== "And" && entity.type !== "Or"
					&& entity.type !== "Negation") {
					if (entity.type === "Namespace" || entity.type === "TranslationUnit") {
						item = {
							id: entity.id,
							open: false,
							checked: true,
							parentId: "",
							name: entity.name,
							icon: controllerConfig.packageIcon,
							iconSkin: "zt"
						};
					} else {
						item = {
							id: entity.id,
							open: true,
							checked: true,
							parentId: "",
							name: entity.name,
							icon: controllerConfig.projectIcon,
							iconSkin: "zt"
						};
					}
				}
			} else {
				switch (entity.type) {
					case "Project":
						item = { id: entity.id, open: true, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.projectIcon, iconSkin: "zt" };
						break;
					case "Namespace":
						if (entity.abap_type !== "DEVC") {
							item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.packageIcon, iconSkin: "zt" };
							break;
						} else {
							item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.namespace, iconSkin: "zt" };
							break;
						};
					case "Class":

						if (entity.id.endsWith("_2") || entity.id.endsWith("_3")) {
							break;
						};
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.typeIcon, iconSkin: "zt" };
						break;

					case "ParameterizableClass":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.typeIcon, iconSkin: "zt" };
						break;
					case "Enum":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.typeIcon, iconSkin: "zt" };
						break;
					case "EnumValue":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.fieldIcon, iconSkin: "zt" };
						break;
					case "Attribute":
					case "Variable":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.fieldIcon, iconSkin: "zt" };
						break;
					case "Method":
						if (entity.abap_type === "METH") {
							item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.form_fumo_meth, iconSkin: "zt" };
							break;
						} else {
							item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.methodIcon, iconSkin: "zt" };
							break;
						};
					case "Function":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.methodIcon, iconSkin: "zt" };
						break;
					case "Struct":
					case "Union":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.typeIcon, iconSkin: "zt" };
						break;

					case "FunctionGroup":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.functionGroup, iconSkin: "zt" };
						break;
					case "FunctionModule":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.form_fumo_meth, iconSkin: "zt" };
						break;
					case "Report":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.reportbuilding, iconSkin: "zt" };
						break;
					case "Interface":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.interface, iconSkin: "zt" };
						break;
					case "FormRoutine":
						item = { id: entity.id, open: false, checked: true, parentId: entity.belongsTo.id, name: entity.name, icon: controllerConfig.form_fumo_meth, iconSkin: "zt" };
						break;
					default:
						events.log.warning.publish({ text: "FamixElement not in tree: " + entity.type });

						return;
				}
			}
			if (item !== undefined) {
				items.push(item);
			}

		});


		//Sortierung nach Typ und Alphanumerisch
		items.sort(
			function (a, b) {

				var sortStringA = "";
				switch (a.icon) {
					case controllerConfig.packageIcon:
						sortStringA = "1" + a.name.toUpperCase();
						break;
					case controllerConfig.typeIcon:
						sortStringA = "2" + a.name.toUpperCase();
						break;
					case controllerConfig.fieldIcon:
						sortStringA = "3" + a.name.toUpperCase();
						break;
					case controllerConfig.methodIcon:
						sortStringA = "4" + a.name.toUpperCase();
						break;
					default:
						sortStringA = "0" + a.name.toUpperCase();
				}

				var sortStringB = "";
				switch (b.icon) {
					case controllerConfig.packageIcon:
						sortStringB = "1" + b.name.toUpperCase();
						break;
					case controllerConfig.typeIcon:
						sortStringB = "2" + b.name.toUpperCase();
						break;
					case controllerConfig.fieldIcon:
						sortStringB = "3" + b.name.toUpperCase();
						break;
					case controllerConfig.methodIcon:
						sortStringB = "4" + b.name.toUpperCase();
						break;
					default:
						sortStringB = "0" + b.name.toUpperCase();
						break;
				}

				if (sortStringA < sortStringB) {
					return -1;
				}
				if (sortStringA > sortStringB) {
					return 1;
				}

				return 0;
			}
		);

		//zTree settings
		var settings = {
			check: {
				enable: controllerConfig.elementsSelectable,
				chkboxType: { "Y": "ps", "N": "s" }
			},
			data: {
				simpleData: {
					enable: true,
					idKey: "id",
					pIdKey: "parentId",
					rootPId: ""
				}
			},
			callback: {
				onCheck: zTreeOnCheck,
				onClick: zTreeOnClick
			},
			view: {
				showLine: false,
				showIcon: true,
				selectMulti: false
			}

		};

		//create zTree
		tree = $.fn.zTree.init($(jQPackageExplorerTree), settings, items);
	}

	function zTreeOnCheck(event, treeId, treeNode) {
		var nodes = tree.getChangeCheckedNodes();

		var entities = [];
		nodes.forEach(function (node) {
			node.checkedOld = node.checked; //fix zTree bug on getChangeCheckedNodes	
			entities.push(model.getEntityById(node.id));
		});


		var applicationEvent = {
			sender: packageExplorerController,
			entities: entities
		};

		if (!treeNode.checked) {
			events.filtered.on.publish(applicationEvent);
		} else {
			events.filtered.off.publish(applicationEvent);
		}

	}



	function zTreeOnClick(treeEvent, treeId, treeNode, eventObject) {

		var alreadySelected = model.getEntityById(treeNode.id) == selectedEntities[0];

		//always deselect the previously selected entities
		if (selectedEntities.size != 0) {
			var unselectEvent = {
				sender: packageExplorerController,
				entities: selectedEntities
			}

			events.selected.off.publish(unselectEvent);
		};

		//select the clicked entities only if the clicked entities are not already selected
		//otherwise the clicked entities should only be deselected
		if (!alreadySelected) {
			var newSelectedEntities = new Array();

			newSelectedEntities.push(model.getEntityById(treeNode.id));

			if (controllerConfig.useMultiselect) {
				newSelectedEntities = newSelectedEntities.concat(model.getAllChildrenOfEntity(model.getEntityById(treeNode.id)));
			}
			var applicationEvent = {
				sender: packageExplorerController,
				entities: newSelectedEntities

			};
			events.selected.on.publish(applicationEvent);
		}
	}

	function onEntitySelected(applicationEvent) {
		if (applicationEvent.sender !== packageExplorerController) {
			var entity = applicationEvent.entities[0];
			//selectedEntities = applicationEvent.entities;
			var item = tree.getNodeByParam("id", entity.id, null);
			tree.selectNode(item, false);
		}

		var selectedEntity = applicationEvent.entities[0];
		selectedEntities = applicationEvent.entities;

		if (selectedEntity.type == "text") {
			return;
		}

		//highlight multiselected entities with specific color
		canvasManipulator.changeColorOfEntities(selectedEntities.slice(1), controllerConfig.multiselectColor, { name: "packageExplorerController" });
		//higlight selected entity with regular color
		canvasManipulator.changeColorOfEntities([selectedEntity], controllerConfig.color, { name: "packageExplorerController" });

		//center of rotation
		if (controllerConfig.setCenterOfRotation) {
			canvasManipulator.setCenterOfRotation(selectedEntity);
		}
	}



	function onEntityUnselected(applicationEvent) {
		canvasManipulator.resetColorOfEntities(applicationEvent.entities, { name: "packageExplorerController" });
		selectedEntities = new Array();
	}







	/*
	function zTreeOnCheck(event, treeId, treeNode) {
				
		var treeObj = $.fn.zTree.getZTreeObj("packageExplorerTree");
		var nodes = treeObj.getChangeCheckedNodes();
	    
		var entityIds = [];
		for(var i = 0; i < nodes.length;i++) {
			nodes[i].checkedOld = nodes[i].checked; //Need for the ztree to set getChangeCheckedNodes correct
			entityIds.push(nodes[i].id);
		}
		
		publishOnVisibilityChanged(entityIds, treeNode.checked, "packageExplorerTree");
		
	}

	function zTreeOnClick(event, treeId, treeNode) {        
		publishOnEntitySelected(treeNode.id, "packageExplorerTree");
	}
    
	
	function onEntitySelected(event, entity) {
		if(event.sender != "packageExplorerTree") {
			var tree = $.fn.zTree.getZTreeObj("packageExplorerTree");   
			var item = tree.getNodeByParam("id", entity.id, null);
			tree.selectNode(item, false);         
		}   
		interactionLogger.logManipulation("PackageExplorerTree", "highlight", entity.id);
	}
    
	function onVisibilityChanged(event, ids, visible) {
		if(event.sender != "packageExplorerTree") {            
			var tree = $.fn.zTree.getZTreeObj("packageExplorerTree");
		    
			for(var i = 0; i < ids.length;i++) {
				var item = tree.getNodeByParam("id", ids[i], null);
				tree.checkNode(item, visible, false, false);
				item.checkedOld = item.checked;
			}
		}
		
	}
    
	function onRelationsVisibilityChanged(event, entities, visible) {
		var tree = $.fn.zTree.getZTreeObj("packageExplorerTree");
		for(var i = 0; i < entities.length; i++) {
			var id = entities[i];
		    
			var item = tree.getNodeByParam("id", id, null);
			tree.checkNode(item, visible, false, false);
			item.checkedOld = item.checked;
			interactionLogger.logManipulation("PackageExplorerTree", "uncheck", id);
		}
	}
	*/

	return {
		initialize: initialize,
		activate: activate,
		reset: reset
	};
})();
