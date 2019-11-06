var shareController = (function() {
    
    //config parameters	
	let controllerConfig = {
        showDebugOutput: false,
	};

    
    function initialize(setupConfig){   
        
        application.transferConfigParams(setupConfig, controllerConfig);
        
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
				sender: shareController,
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
				sender: shareController,
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
				sender: shareController,
				entities: entities
			};
			events.filtered.on.publish(applicationEvent);

            if (visMode.includes( "x3dom")) {
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
           } else {
                //get reference of aframe
                var cameraEl = document.querySelector('#camera');

                if (metaStateJson.position) {
                    cameraEl.setAttribute('position', metaStateJson.position)
                }
                if (metaStateJson.rotation) {
                    cameraEl.setAttribute('rotation', metaStateJson.rotation)
                }
           }
		}
    }
	
	function activate(){
    
        var id = "jqxTextImageButton";
        var buttonType = "button";
        var jqxTextImageButton = document.createElement("BUTTON");
		jqxTextImageButton.type = buttonType;
        jqxTextImageButton.id = id;
        var text = document.createTextNode("share");
        jqxTextImageButton.appendChild(text);
        $("ul.jqx-menu-ul")[0].appendChild(jqxTextImageButton);
        
        $("#jqxTextImageButton").jqxButton({ 
            theme: "metro",
            width: 80, 
            height: 25, 
            textImageRelation: "imageBeforeText", 
            textPosition: "left", 
            imgSrc: "scripts/ShareController/images/icon_share.png" 
        });
        
        $("#jqxTextImageButton").on('click', function (){
        
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
            
            if (visMode.includes( "x3dom")) {
                //get reference of x3dom objects
                var x3domRuntime = document.getElementById('x3dElement').runtime;
                var viewarea = x3domRuntime.canvas.doc._viewarea;
                var viewpoint = viewarea._scene.getViewpoint();
                
                state.viewMatrix = viewarea.getViewMatrix();
                state.centerRotation = viewpoint.getCenterOfRotation();
            }else{
                //get reference of aframe
                var cameraEl = document.querySelector('#camera');
                var position = cameraEl.getAttribute('position');
                var rotation = cameraEl.getAttribute('rotation');

                state.position = position;
                state.rotation = rotation;
            };
            

            var stateHashcode = JSON.stringify(state).hashCode();
            var jsonString = JSON.stringify(state,null,'\t');
            
            var url = window.location.toString().split("&state=")[0].split("?state=")[0];
            if (url.includes( "?")) {
                    var state_N= "&state=";
                } else{
                    var state_N= "?state=";
                };
            var stateID= "<strong>StateID:</strong>" + stateHashcode + "<br /><br />";
            var urlStr= "<strong>URL:</strong>";
            var copyField= "<input id='copyField' style='width:80%' readonly value='" + url + state_N + stateHashcode
                    +"'> <a onclick='copyInput()' href='javascript:void(0);'>";
            var shareLink= "<strong>share link</strong></a><br /><br />";
            var jsonHtml= "<strong>JSON:</strong> <pre style='margin:0'>"+jsonString+"</pre>";
            var popup;
    
            if(controllerConfig.showDebugOutput===true) {
                popup = stateID + urlStr + copyField + shareLink + jsonHtml;
            } else{
                popup = urlStr + copyField + shareLink;           
            }; 

            $("#DisplayWindow").remove();
            var loadPopup = application.createPopup("popup",  
            popup, "DisplayWindow");
            document.body.appendChild(loadPopup);
            $("#DisplayWindow").css("display", "block").jqxWindow({
                    theme: "metro",
                    width: 700,
                    //height: 600,
                    isModal: true,
                    autoOpen: true,
                    resizable: false
            });

            var xhr = new XMLHttpRequest();
            var jsonData = "state.php";
            xhr.open("POST", jsonData, true);
            xhr.setRequestHeader("Content-Type", "application/json"); 
            xhr.onreadystatechange = function () {
                if (xhr.readyState === 4 && xhr.status === 200) {
                console.log("successfull");			 
                }
            }; 
            xhr.send(JSON.stringify({
                stateHashcode: stateHashcode,
                jsonString: jsonString,
            }));
        });
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

    return {
        initialize: initialize,
		activate: activate
	};    
})();

var copyInput = (function() {
	document.getElementById('copyField').select();
	document.execCommand("copy");
});
