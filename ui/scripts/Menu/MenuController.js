var menuController = (function() {
    
	var menuRecords = [];
	var menuMappingMap = new Map;
	var mappingItems = new Array();

	var MENU_BAR_ID = "menuBar"
	var MENU_BAR_JQID = "#" + MENU_BAR_ID;

	var idCounter = 0;

	//config parameters		
	var controllerConfig = {		
		menuMapping : []
	}
	
	
	function initialize(setupConfig){	

		//config
		application.transferConfigParams(setupConfig, controllerConfig);	

		//add css file
		var cssLink = document.createElement("link");
		cssLink.type = "text/css";
		cssLink.rel = "stylesheet";
		cssLink.href = "scripts/Menu/menu.css";
		document.getElementsByTagName("head")[0].appendChild(cssLink);

		
		//create menu structure		
 		mappingItems = createMenuStructure(controllerConfig.menuMapping, "-1");

		//create data adapter
		var source = {
			datatype: "json",
			datafields: [
				{ name: 'id' },
				{ name: 'parentid' },
				{ name: 'title' },
				{ name: 'width'},
				{ name: 'subMenuWidth' }
			],
			id: 'id',
			localdata: mappingItems
		};

		var dataAdapter = new $.jqx.dataAdapter(source);
		dataAdapter.dataBind();

		//create menu record structure
		menuRecords = dataAdapter.getRecordsHierarchy('id', 'parentid', 'items', [{ name: 'title', map: 'label'}]);        
    }
	

	function createMenuStructure(subMenuMapping, parentId){

		var mappingItems = new Array();		

		subMenuMapping.forEach(function(mapping) {
			idCounter = idCounter + 1;

			mapping["id"] = "menuController" + idCounter;
			mapping["jQId"] = "#" + mapping["id"];
			mapping["parentid"] = parentId.toString();	
			mapping["width"] = "250px";		


			if(mapping.checkBox){
				if(mapping.checked !== undefined){
					mapping["checked"] = mapping.checked;
				} else {
					mapping["checked"] = true;
				}
			}
			
			if(mapping.toggle){
				if(mapping.toggled !== undefined){
					mapping["toggled"] = mapping.toggled;
				} else {
					mapping["toggled"] = true;
				}
			}			
			if(mapping.subMenu){
				var subMenuItems = createMenuStructure(mapping.items, mapping.id);
				mappingItems = mappingItems.concat(subMenuItems);
			}
			

			menuMappingMap.set(mapping["id"], mapping);
			mappingItems.push(mapping);		
			
		});

		return mappingItems;
	}



	function activate(parent){

		//create container for menu
		var menuDiv = document.createElement("DIV");		
		menuDiv.id = "menuDiv";
		parent.appendChild(menuDiv);

		var menuBar = document.createElement("DIV");
		menuBar.id = MENU_BAR_ID;
		menuDiv.appendChild(menuBar);
		
		//create jqx menu
		$(MENU_BAR_JQID).jqxMenu({ theme: "metro", width: "100%", source: menuRecords, height: 30 });

		//set output parameter
		mappingItems.forEach(function(mapping) {
			if(mapping.checkBox){
				$(mapping.jQId).jqxCheckBox({ theme: "metro", checked: mapping.checked });
				return;
			}
			if(mapping.toggle){
				$(mapping.jQId).jqxToggleButton({ theme: "metro", width: 125, toggled: mapping.toggled });	
				return;
			}			
			if(mapping.subMenu){	
				return;
			}
			if(mapping.popup){
				var popupId = mapping.id + "PopUp"
				var popupJQId = "#" + popupId;

				var popupOKButtonId = popupId + "OK";
				var popupOKButtonJQId = "#" + popupOKButtonId;

				createPopup(parent, mapping.title, mapping.text, popupId, popupOKButtonId);
				$(popupJQId).jqxWindow({ 
					theme: "metro", 
					width: mapping.width, 
					height: mapping.height, 
					isModal: true, 
					autoOpen: false, 
					okButton: $(popupOKButtonJQId), 
					resizable: false, 
					// initContent: function(){						
					// 	$(popupOKButtonJQId).jqxButton({ theme: "metro", width: "50px" });				
					// }
				});
			}

			$(mapping.jQId).jqxButton({ theme: "metro", width: 125 });

		});

		//set on click handler
		$(MENU_BAR_JQID).on('itemclick', menuItemClick);
	}
	


	function menuItemClick(event){
		var mapping = menuMappingMap.get(event.args.id);

		if(mapping.checkBox){
			if(mapping.checked){
				mapping.checked = false;
				$(mapping.jQId).jqxCheckBox("uncheck"); 
				executeFunctionByName(mapping.eventOff, window, ""); 
			} else {
				mapping.checked = true;
				$(mapping.jQId).jqxCheckBox("check"); 
				executeFunctionByName(mapping.eventOn, window, "");
			}
			return;
		}
		
		if(mapping.toggle){
			if(mapping.toggled){
				mapping.toggled = false;
				//$(mapping.jQId)[0].value = mapping.title + " OFF";	
				executeFunctionByName(mapping.eventOff, window, ""); 
			} else {
				mapping.toggled = true;
				//$(mapping.jQId)[0].value = mapping.title + " ON";	
				executeFunctionByName(mapping.eventOn, window, "");
			}

			return;				
		}

		if(mapping.link){
			window.open(mapping.url, '_blank', ''); 
			return;
		}

		if(mapping.subMenu){	
			return;
		}

		if(mapping.popup){
			var popupId = mapping.id + "PopUp"
			var popupJQId = "#" + popupId;

			$(popupJQId).jqxWindow("open");
		}
		
		executeFunctionByName(mapping.event, window, "");
		
	}



	//Helper to call function by string
	function executeFunctionByName(functionName, context /*, args */) {
		var args = [].slice.call(arguments).splice(2);
		var namespaces = functionName.split(".");
		var func = namespaces.pop();
		for(var i = 0; i < namespaces.length; i++) {
			context = context[namespaces[i]];
		}
		return context[func].apply(context, args);
	}


	function createPopup(rootDiv, title, text, popupId, okButtonId){

		var popupWindowDiv = document.createElement("DIV");
        rootDiv.appendChild(popupWindowDiv);
        popupWindowDiv.id = popupId;

            var popupTitleDiv = document.createElement("DIV");            
            popupWindowDiv.appendChild(popupTitleDiv);
            popupTitleDiv.innerHTML = title;

            var popupContentDiv = document.createElement("DIV");
            popupWindowDiv.appendChild(popupContentDiv);
                
				//Text
				var popupText= document.createElement("DIV");
                popupContentDiv.appendChild(popupText);
				popupText.innerHTML = text;			
	}



    return {
        initialize: initialize,
		activate:	activate
    };    
})();