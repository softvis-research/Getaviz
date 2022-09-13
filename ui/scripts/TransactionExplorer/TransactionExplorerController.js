var transactionExplorerController = (function () {

  let transactionExplorerTreeID = "transactionExplorerTree";
let jQTransactionExplorerTree = "#transactionExplorerTree";

let tree;

  var elementsMap = new Map();
var ztreeIdMap = new Map();

const domIDs = {
  zTreeDiv: "zTreeDiv",
}

let controllerConfig = {

      elements: [],
  elementsSelectable: true,
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

  var calledByEntities = findCalledByElements(entity);

  var allChildren = getAllCalledByElements(entity).length;

  if (entity.type == "Transaction"){
    //var allChildren = getAllCalledByElements(entity).length;

    if (allChildren == 0) { allChildren = "0"; }

    var newName = entity.name + " (" + allChildren + ")";
  }

  // Eindeutige ID für den ZTree erzeugen
  var ztreeID = generateUniqueID();
  ztreeIdMap.set(ztreeID, entity);

  var item = createItem(entity, ztreeID, icon, parentId, newName);
  transactionItems.push(item);

  if(calledByEntities.length < 1){
    return transactionItems;
  }

  calledByEntities.forEach(function(calledElement){

          var calledItems = createItemsForTransactions(calledElement, item.id); //item.id statt entity.id nutzen
          transactionItems.push(...calledItems);

  });	

  return transactionItems;
}

function generateUniqueID(){

  function chr4(){
    return Math.random().toString(16).slice(-4);
    }
    return chr4() + chr4() +
    '-' + chr4() +
    '-' + chr4() +
    '-' + chr4() +
    '-' + chr4() + chr4() + chr4();
}


function createItem(entity, ztreeID, icon, parentId, newName = entity.name){

  var item = {
    id: ztreeID, 
    open: false,
    parentId: parentId,
    name: newName,
    type: entity.type,
    icon: icon,
    iconSkin: "zt",
  };

  return item;

}



function createItemForCodeTransactions(){

  var itemForCodeTransactions = { id: "dummyCodeID", open: false,	/*checked: true,*/ name: "Code Transaktionen" };

  return itemForCodeTransactions;

}

function createItemForOtherTransaction(){

  var itemForOtherTransactions = { id: "dummyOtherID", open: false, /*checked: true,*/ name: "Customizing Transaktionen" };

  return itemForOtherTransactions;

}

function createItemForSAPTransaction(){

  var itemForSAPTransactions = { id: "dummySAPID", open: false, /*checked: true,*/ name: "SAP Transaktionen" };

  return itemForSAPTransactions;

}

function createZTreeElements(entities) {

  var items = [];
  var transactionEntities = searchTransactionsForZTree(entities);

  itemForCodeTransactions = createItemForCodeTransactions();	
  ztreeIdMap.set(itemForCodeTransactions.id, "dummyElementCodeID");

  itemForOtherTransactions = createItemForOtherTransaction();
  ztreeIdMap.set(itemForOtherTransactions.id, "dummyElementOthersID");

  itemForSAPTransactions = createItemForSAPTransaction();
  ztreeIdMap.set(itemForSAPTransactions.id, "dummyElementSAPID");

  items.push(itemForOtherTransactions);
  items.push(itemForCodeTransactions);
  items.push(itemForSAPTransactions);


  if(transactionEntities.length < 1){
    return items;
  }

  transactionEntities.forEach(function (transactionEntity){

    if(elementsMap.has(transactionEntity.type)){

      if(transactionEntity.name.substring(0, 1) != 'Z' && transactionEntity.name.substring(0, 1) != '/'){
        var parentIdForTransactionCluster = itemForSAPTransactions.id;
      } else {        
        if (transactionEntity.calls != 0){
          parentIdForTransactionCluster = itemForCodeTransactions.id;
        } else {
          parentIdForTransactionCluster = itemForOtherTransactions.id;
        }
      }

      var transactionItems = createItemsForTransactions(transactionEntity, parentIdForTransactionCluster);
      items.push(...transactionItems);

    }
  });

  // sort by type, then alphanumerically
  items.sort(function (a, b) {

    /*if(elementsMap.get(a.type) === undefined){
      var aSortOrder = 1000;
      var sortStringA = aSortOrder + a.name.toUpperCase();
    } else {
        var aSortOrder = elementsMap.get(a.type).sortOrder;
      var sortStringA = aSortOrder + a.name.toUpperCase();
    }

    if(elementsMap.get(b.type) === undefined){
      var bSortOrder = 1000;
      var sortStringB = bSortOrder + b.name.toUpperCase();				
    } else {
      var bSortOrder = elementsMap.get(b.type).sortOrder;
        var sortStringB = bSortOrder + b.name.toUpperCase();
    }*/

    if(elementsMap.get(a.type) === undefined){
      var aSortOrder = 1000;
      var sortStringA = aSortOrder + a.name.toUpperCase();
    } else {
              if (a.type == 'Transaction'){
                var aSortOrder = setNewSortOrderForTransactions(a);
          var sortStringA = aSortOrder;
              } else {
        var aSortOrder = elementsMap.get(a.type).sortOrder;
        var sortStringA = aSortOrder + a.name.toUpperCase();
      }
    }

    if(elementsMap.get(b.type) === undefined){
      var bSortOrder = 1000;
      var sortStringB = bSortOrder + b.name.toUpperCase();				
    } else {
      if (b.type == 'Transaction'){
                var bSortOrder = setNewSortOrderForTransactions(b);
          var sortStringB = bSortOrder;
              } else {
        var bSortOrder = elementsMap.get(b.type).sortOrder;
            var sortStringB = bSortOrder + b.name.toUpperCase();
      }
    }

    
      if (aSortOrder < bSortOrder) {
        return 1;
      }
      if (aSortOrder > bSortOrder) {
        return -1;
      }
    

    

    /*if (sortStringA < sortStringB) {
      return -1;
    }
    if (sortStringA > sortStringB) {
      return 1;
    }*/

    return 0;
  }); 

  return items;
}

function setNewSortOrderForTransactions(item) {

var entityID = ztreeIdMap.get(item.id).id;

var entity = model.getEntityById(entityID);

var sortOrder = getAllCalledByElements(entity).length;

   return sortOrder;

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

  function publishSelectEvent(treeEvent, treeId, treeNode, eventObject) {

  //const clickedEntity = model.getEntityById(treeNode.id);
  const clickedEntity = model.getEntityById(ztreeIdMap.get(treeNode.id).id);
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

      if (entity === undefined) {
        const treeNodeEntity = treeNode.children;

        if (treeNodeEntity.length >= 1){
          neo4jModelLoadController.loadAllChildrenOf(treeNode.id, true);
          return;
        }
      } else {
      if (!entity.hasUnloadedChildren) return;
    } 

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

  /*if (controllerConfig.showSearchField) {
    $("#" + domIDs.searchInput).val(selectedEntity.name);
  }*/
}

function onEntityUnselected(applicationEvent) {
  const unselectedEntities = new Set(applicationEvent.entities);
  selectedEntities = selectedEntities.filter(entity => !unselectedEntities.has(entity));

  // only undo the selection in the UI if the root of the selection subtree is getting deselected
  const shouldRemoveUISelection = applicationEvent.entities.some(entity => !entity.belongsTo || !entity.belongsTo.selected);
  if (shouldRemoveUISelection) {
    unselectNodes();
  }

  /*if (controllerConfig.showSearchField) {
    $("#" + domIDs.searchInput).val("");
  }*/
}


return {
  initialize: initialize,
  activate: activate,
  reset: reset
};
})();