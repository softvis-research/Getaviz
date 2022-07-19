var transactionExplorerController = (function () {

    let transactionExplorerTreeID = "transactionExplorerTree";
	let jQTransactionExplorerTree = "#transactionExplorerTree";

	let tree;

	//var entityTypesForSearch = ["Namespace", "Class", "Interface", "Report", "FunctionGroup"];

    var elementsMap = new Map();

	const domIDs = {
		zTreeDiv: "zTreeDiv",
		//searchDiv: "searchDiv",
		//searchInput: "searchField"
	}

	let controllerConfig = {

        elements: [],
		elementsSelectable: true,

		//showSearchField: true,
		//entityTypesForSearch: entityTypesForSearch,

		useMultiselect: true,

	};

    var selectedEntities = [];

	function initialize(setupConfig) {

        application.transferConfigParams(setupConfig, controllerConfig);

        controllerConfig.elements.forEach(function (element) {
			elementsMap.set(element.type, element);
		});

	}

	function activate(rootDiv) {

        //create zTree div-container
		let zTreeDiv = document.createElement("DIV");
		zTreeDiv.id = domIDs.zTreeDiv;

		let transactionExplorerTreeUL = document.createElement("UL");
		transactionExplorerTreeUL.id = transactionExplorerTreeID;
		transactionExplorerTreeUL.setAttribute("class", "ztree");

		zTreeDiv.appendChild(transactionExplorerTreeUL);
		rootDiv.appendChild(zTreeDiv);

		//create zTree
		prepareTreeView();

        events.selected.on.subscribe(onEntitySelected);
		events.selected.off.subscribe(onEntityUnselected);
		events.loaded.on.subscribe(onEntitiesLoaded);
		events.filtered.off.subscribe(onEntitiesUnfiltered);
        
	}

    function reset() {
		prepareTreeView();
	}

    function prepareTreeView() {

		const entities = model.getCodeEntities();
		const items = createZTreeElements(entities);

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
				onClick: publishSelectEvent,
				onExpand: zTreeOnExpand,
			},
			view: {
				showLine: false,
				showIcon: true,
				selectMulti: false
			}

		};

		//create zTree
		tree = $.fn.zTree.init($(jQTransactionExplorerTree), settings, items);
	}

	function searchTransactionsForZTree(entities) {
		const transactionElements = [];
     
        entities.forEach(function (entity) {

		  if( entity.type == 'Transaction'){

				transactionElements.push(entity)
			
		  }
	  });	
	  
	  return transactionElements;
	}

	function findCalledByElements(entity) {
		const calledByElements = [];

		var calledElements = entity.calls;

		if (!calledElements || calledElements.length < 1){
			return calledByElements;
		}

		calledElements.forEach(function (calledElement) {
			
			calledByElements.push(calledElement)
		});

		return calledByElements;

	}

	function createItemsForTransactions(entity, parentId){

		var transactionItems = [];

        var icon = elementsMap.get(entity.type).icon;

		var item = createItem(entity, icon, parentId);
		transactionItems.push(item);

		var calledByEntities = findCalledByElements(entity);

		if(calledByEntities.length < 1){
			return transactionItems;
		}

		calledByEntities.forEach(function(calledElement){

            var calledItems = createItemsForTransactions(calledElement, entity.id);
            transactionItems.push(...calledItems);
		});	

		return transactionItems;
	}


	function createItem(entity, icon, parentId){

		var item = {
			id: entity.id,
			open: false,
			checked: !entity.filtered,
			parentId: parentId,
			name: entity.name,
			type: entity.type,
			icon: icon,
			iconSkin: "zt"
		};

		return item;

	}

	function createZTreeElements(entities) {

		var items = [];
		var transactionEntities = searchTransactionsForZTree(entities);

		if(transactionEntities.length < 1){
			return items;
		}

		transactionEntities.forEach(function (transactionEntity){

			if(elementsMap.has(transactionEntity.type)){
				
				var transactionItems = createItemsForTransactions(transactionEntity, "");
				items.push(...transactionItems);

			}
		});

		// sort by type, then alphanumerically
		items.sort(function (a, b) {

			var aSortOrder = elementsMap.get(a.type).sortOrder;
			var sortStringA = aSortOrder + a.name.toUpperCase();

			var bSortOrder = elementsMap.get(b.type).sortOrder;
			var sortStringB = bSortOrder + b.name.toUpperCase();

			if (sortStringA < sortStringB) {
				return -1;
			}
			if (sortStringA > sortStringB) {
				return 1;
			}

			return 0;
		}); 

		return items;
	}

	function getAllCalledByElements(entity){

        var calledByItems = [];

		var calledElements = entity.calls;

		if(!calledElements || calledElements.length < 1){
			return calledByItems;
		}

		calledElements.forEach(function (calledElement) {
			calledByItems.push(calledElement);

			const grandChildren = getAllCalledByElements(calledElement);
	        calledByItems = calledByItems.concat(grandChildren);
		});

		return calledByItems;

	}

    function zTreeOnCheck(event, treeId, treeNode) {

		//node.checkedOld = node.checked; //fix zTree bug on getChangeCheckedNodes

		const entity = model.getEntityById(treeNode.id);
		//const children = model.getAllChildrenOfEntity(entity);
		var children = getAllCalledByElements(entity);
		const entities = [entity, ...children];

		const applicationEvent = {
			sender: transactionExplorerController,
			entities: entities
		};

		if (!treeNode.checked) {
			applicationEvent.entities = applicationEvent.entities.filter(entity => !entity.filtered);
			events.filtered.on.publish(applicationEvent);
		} else {
			// ensure that the parents of visible entities are also visible themselves
			const parents = entity.allParents;
			applicationEvent.entities = [...entities, ...parents].filter(entity => entity.filtered);

			events.filtered.off.publish(applicationEvent);

			if (entity.hasUnloadedChildren) {
				neo4jModelLoadController.loadAllChildrenOf(entity.id, false);
			}
		}
	}

    function publishSelectEvent(treeEvent, treeId, treeNode, eventObject) {

		const clickedEntity = model.getEntityById(treeNode.id);
		// do nothing when selecting an invisible entity
		if (clickedEntity.filtered) return;

		const alreadySelected = clickedEntity === selectedEntities[0];

		//always deselect the previously selected entities
		if (selectedEntities.size != 0) {
			const unselectEvent = {
				sender: transactionExplorerController,
				entities: selectedEntities
			}

			events.selected.off.publish(unselectEvent);
		};

		//select the clicked entities only if the clicked entities are not already selected
		//otherwise the clicked entities should only be deselected
		if (!alreadySelected) {
			let newSelectedEntities = [clickedEntity];

			if (controllerConfig.useMultiselect) {
				//const visibleChildren = model.getAllChildrenOfEntity(clickedEntity).filter(entity => !entity.filtered);
				var visibleChildren = getAllCalledByElements(clickedEntity).filter(entity => !entity.filtered);
			
				newSelectedEntities = newSelectedEntities.concat(visibleChildren);
			}

			const selectEvent = {
				sender: transactionExplorerController,
				entities: newSelectedEntities
			};
			events.selected.on.publish(selectEvent);
		}
	}

	function zTreeOnExpand(event, treeId, treeNode) {
		const entity = model.getEntityById(treeNode.id);
		if (!entity.hasUnloadedChildren) return;

		neo4jModelLoadController.loadAllChildrenOf(entity.id, true);
	}

	function onEntitiesLoaded(applicationEvent) {
		if (applicationEvent.parentId) {
			// we were loading child elements
			const parentTreeElem = tree.getNodeByParam('id', applicationEvent.parentId);
			// store the placeholder first and remove it only afterwards, so the tree doesn't collapse due to lack of children
			const placeholderToRemove = parentTreeElem.children[0];
			const newChildTreeElements = createZTreeElements(applicationEvent.entities);
			tree.addNodes(parentTreeElem, 0, newChildTreeElements, true);
			if (placeholderToRemove) {
				tree.removeNode(placeholderToRemove);
			}
		} else {
			// root elements are currently only loaded on startup, which is fixed and doesn't go through the event system
		}
	}

	function onEntitiesUnfiltered(applicationEvent) {
		// only catch events from elsewhere - if they come from here, the tree will already be updated
		if (applicationEvent.sender !== transactionExplorerController) {
			// put all ids into a set, so we can use its constant-time has() to find the matching ZTree objects more efficiently
			const entityIdSet = new Set();
			for (const entity of applicationEvent.entities) {
				entityIdSet.add(entity.id);
			}
			const zTreeNodesToCheck = tree.getNodesByFilter((node) => entityIdSet.has(node.id));
			for (const node of zTreeNodesToCheck) {
				// since we're updating the tree from the model, don't trigger onCheck
				tree.checkNode(node, true, false, false);
			}
		}
	}

	function selectNode(entityID) {
		var item = tree.getNodeByParam("id", entityID, null);
		tree.selectNode(item, false);
	}

	function unselectNodes() {
		tree.cancelSelectedNode();
	}

	function onEntitySelected(applicationEvent) {
		var selectedEntity = applicationEvent.entities[0];
		selectedEntities = applicationEvent.entities;

		selectNode(selectedEntity.id);

		if (controllerConfig.showSearchField) {
			$("#" + domIDs.searchInput).val(selectedEntity.name);
		}
	}

	function onEntityUnselected(applicationEvent) {
		const unselectedEntities = new Set(applicationEvent.entities);
		selectedEntities = selectedEntities.filter(entity => !unselectedEntities.has(entity));

		// only undo the selection in the UI if the root of the selection subtree is getting deselected
		const shouldRemoveUISelection = applicationEvent.entities.some(entity => !entity.belongsTo || !entity.belongsTo.selected);
		if (shouldRemoveUISelection) {
			unselectNodes();
		}

		if (controllerConfig.showSearchField) {
			$("#" + domIDs.searchInput).val("");
		}
	}


	return {
		initialize: initialize,
		activate: activate,
		reset: reset
	};
})();