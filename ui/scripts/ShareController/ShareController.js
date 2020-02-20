var shareController = (function() {
	let controllerConfig = {
        showDebugOutput: false,
	};

    function initialize(setupConfig){   
        application.transferConfigParams(setupConfig, controllerConfig);
    }    

	function activate(){
        createShareButton();
        createSharePopup();
	}

    function createShareButton(){
        var jqxShareButton = document.createElement("BUTTON");
		jqxShareButton.type = "button";
        jqxShareButton.id = "jqxShareButton";
        jqxShareButton.style = "float:right;";
        var text = document.createTextNode("Share");
        jqxShareButton.appendChild(text);
        $("ul.jqx-menu-ul")[0].appendChild(jqxShareButton);
        
        $("#jqxShareButton").jqxButton({ 
            theme: "metro",
            width: 80, 
            height: 25, 
            textImageRelation: "imageBeforeText", 
            textPosition: "left", 
            imgSrc: "scripts/ShareController/images/share_icon.png" 
        });
 	}

    function createSharePopup(){
        $("#jqxShareButton").on('click', function (){
            createCSSLink();
            createPopup();
        });
    }

    function createCSSLink(){
        var cssLink = document.createElement("link");
        cssLink.type = "text/css";
        cssLink.rel = "stylesheet";
        cssLink.href = "scripts/ShareController/style.css";
        document.getElementsByTagName("head")[0].appendChild(cssLink);        
    }  
    
    function createPopup(){
        $("#DisplayWindow").remove();
        var popup = application.createPopup("Share Visualization", createPopupContent(), "DisplayWindow");
        document.body.appendChild(popup);
        createCopyLinkButton();
        $("#DisplayWindow").css("display", "block").jqxWindow({
            theme: "metro",
            width: 600,
            height: 87,
            isModal: true,
            autoOpen: true,
            resizable: false
        });
    }

    function createPopupContent(){
        var state = createState();
        var stateHashcode = JSON.stringify(state).hashCode();
        var jsonString = JSON.stringify(state,null,'\t');
            
        var url = window.location.toString().split("&state=")[0].split("?state=")[0];
        if (url.includes( "?")) {
            var stateKeyword= "&state=";
        } else{
            var stateKeyword= "?state=";
        };
                
        var descriptionText = "Use this URL to share the current state of the visualization."+ "<br />";
        var shareLinkDiv= "<div id='shareLinkDiv' class='shareController'><input id='copyField' class='shareController' readonly value='" + url + stateKeyword + stateHashcode + "'></div> ";
    
        if(controllerConfig.showDebugOutput===true) {
            var stateID= "<span class='shareController titleSpan'>StateID:</span>" + stateHashcode + "<br /><br />";
            var jsonHtml= "<br /><span class='shareController titleSpan'>JSON:</span> <pre class='shareController jsonHtmlPre'>"+jsonString+"</pre>";
            var popup = stateID + descriptionText + shareLinkDiv + jsonHtml;
        } else{
            var popup = descriptionText + shareLinkDiv;
        }; 
        
        return popup;
    }

    function createState(){
        var state = calculateState();
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
            stateHashcode: JSON.stringify(state).hashCode(),
            jsonString: JSON.stringify(state,null,'\t'),
        }));
        
        return state;
    }

    function calculateState(){
        var state = {
            "selected": [],
            "marked": [],
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
        
        return state;
    } 

    function createCopyLinkButton(){
        var shareLinkid = "jqxshareLinkButton";
        var shareLinkbuttonType = "button";
        var jqxshareLinkButton = document.createElement("BUTTON");
        jqxshareLinkButton.type = shareLinkbuttonType;
        jqxshareLinkButton.id = shareLinkid;

        document.getElementById('shareLinkDiv').appendChild(jqxshareLinkButton);

        $("#jqxshareLinkButton").jqxButton({ 
            theme: "metro",
            imgSrc: "scripts/ShareController/images/copy_icon.png"
        });
            
        $("#jqxshareLinkButton").on('click', function (){
            document.getElementById('copyField').select();
            document.execCommand("copy");
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
    
    return {
        initialize: initialize,
		activate: activate
	};    
})();
