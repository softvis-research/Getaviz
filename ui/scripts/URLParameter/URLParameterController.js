var urlParameterController = (function() {
    
    function initialize(){
		$(document).ready(function () {
			$.getJSON( metaStateJsonUrl, initializeApplicationTimeout);
		});

		function initializeApplicationTimeout(metaStateJson){
			setTimeout(() => initializeApplication(metaStateJson), 2000);
		}

		function initializeApplication(metaStateJson){
			let entities = [];
			metaStateJson.selected.forEach(function(element){
				var entity = model.getEntityById(element);
				if (entity) {
					entities.push(entity);
				}
			}); 
		
			var applicationEvent = {			
				sender: urlParameterController,
				entities: entities
			};
			events.selected.on.publish(applicationEvent);

			
			entities = new Array();
			metaStateJson.marked.forEach(function(element){
				var entity = model.getEntityById(element);
				if (entity) {
					entities.push(entity);
					
				}
			});
			
			var applicationEvent = {			
				sender: urlParameterController,
				entities: entities
			};
			events.marked.on.publish(applicationEvent);

			entities = new Array();
			metaStateJson.filtered.forEach(function(element){
				var entity = model.getEntityById(element);
				if (entity) {
					entities.push(entity);
				}
			});
			
			var applicationEvent = {			
				sender: urlParameterController,
				entities: entities
			};
			events.filtered.on.publish(applicationEvent);


			//get reference of x3dom objects
			var x3domRuntime = document.getElementById('x3dElement').runtime;
			var viewarea = x3domRuntime.canvas.doc._viewarea;
			var viewpoint = viewarea._scene.getViewpoint();

			if (metaStateJson.viewMatrix) {
				Object.setPrototypeOf(metaStateJson.viewMatrix, x3dom.fields.SFMatrix4f.prototype);
				viewpoint.setView(metaStateJson.viewMatrix);
				viewarea._needNavigationMatrixUpdate = true;
			}
			if (metaStateJson.centerRotation) {
				Object.setPrototypeOf(metaStateJson.viewMatrix, x3dom.fields.SFVec4f.prototype);
				viewpoint.setCenterOfRotation(metaStateJson.centerRotation);
			}
		}
    }
	
	function activate(){
		console.log("idVariable: "+ idVariable);
        
		//URL-button                            
		var codeWindowButton2 = document.createElement("BUTTON");
		codeWindowButton2.type = "button";
		codeWindowButton2.style = "width: 10%;height: 25px;margin: 2px 0px -2px 2px;";
		codeWindowButton2.addEventListener("click", openWindow2, false);
		
		var fullScreenImage2 = document.createElement("IMG");
		fullScreenImage2.src = "scripts/URLParameter/images/idlink.png";
		fullScreenImage2.style = "width: 25px; height: 20px;";
						
		codeWindowButton2.appendChild(fullScreenImage2);
		$("ul.jqx-menu-ul")[0].appendChild(codeWindowButton2);


	}


	String.prototype.hashCode = function() {
		var hash = 0, i, chr;
		if (this.length === 0) return hash;
		for (i = 0; i < this.length; i++) {
		  chr   = this.charCodeAt(i);
		  hash  = ((hash << 5) - hash) + chr;
		  hash |= hash; // Convert to 32bit integer
		}
		if (hash<0)
			return Math.abs(hash * 10);
		else
			return Math.abs(hash * 10) + 1;	
	};
	

	
	function openWindow2(){
		
		var state = {
			"selected": []
			,
			"marked": []
			,
			"filtered": []
		};
		
		var selectedEntities = events.selected.getEntities();
		state.selected = new Array();
		selectedEntities.forEach(function(element){
			state.selected.push(element.id);
		});
		var markedEntities = events.marked.getEntities();
		state.marked = new Array();
		markedEntities.forEach(function(element){
			state.marked.push(element.id);
		});
		var filteredEntities = events.filtered.getEntities();
		state.filtered = new Array();
		filteredEntities.forEach(function(element){
			state.filtered.push(element.id);
		});
		
		//get reference of x3dom objects
		var x3domRuntime = document.getElementById('x3dElement').runtime;
		var viewarea = x3domRuntime.canvas.doc._viewarea;
		var viewpoint = viewarea._scene.getViewpoint();
		
		state.viewMatrix = viewarea.getViewMatrix();
		state.centerRotation = viewpoint.getCenterOfRotation();

		var myString=JSON.stringify(state,null,'\t');
        var myHashwert=JSON.stringify(state).hashCode();
		console.log("myHashwert: "+myHashwert);


		var url = window.location.toString().split("&state=")[0];

		url ="<strong>StateID:</strong>"+ myHashwert +"<br /><br /><strong>URL:</strong> <input id='copyField' style='width:80%' readonly value='" + url + "&state=" + myHashwert
		+"'> <a onclick='copyInput()' href='javascript:void(0);'><strong>share link</strong></a><br /><br /><strong>Famix:</strong> <pre style='margin:0'>"+myString+"</pre>";

		$("#DisplayWindow").remove();
		var loadPopup = application.createPopup("url",  
		url, "DisplayWindow");
		document.body.appendChild(loadPopup);
		$("#DisplayWindow").css("display", "block").jqxWindow({
				theme: "metro",
				width: 700,
				height: 600,
				isModal: true,
				autoOpen: true,
				resizable: false
		});

        var xhr = new XMLHttpRequest();
        var url = "state.php";
        xhr.open("POST", url, true);
        xhr.setRequestHeader("Content-Type", "application/json"); 
        xhr.onreadystatechange = function () {
	        if (xhr.readyState === 4 && xhr.status === 200) {
 
			  console.log("successfull");
			 
	        }
        }; 
        xhr.send(JSON.stringify({
	        hash: myHashwert,
			state: myString,
		}));
	}

    return {
        initialize: initialize,
		activate: activate
	};    
})();

var copyInput = (function() {
	document.getElementById('copyField').select();
	document.execCommand("copy");
});
