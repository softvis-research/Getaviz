var sourceCodeController = (function(){    


    // extra Fenster zur Darstellung des Quellcodes, bietet mehr Platz
    let codeWindow = null;
    // Welche Klasse, Type(Klasse, Methode, Attribut), Attributename
    let lastObject = {file: null, classEntity: null, entity: null};
    


    //config parameters	
	let controllerConfig = {
		fileType : "java",
        url: "",
        showCodeWindowButton:true,
        showCode:true,
	};
    
	function initialize(setupConfig){
		application.transferConfigParams(setupConfig, controllerConfig);
	}
	
	function activate(rootDiv){

        //load zTree javascript-files
		$.getScript("node_modules/prismjs/prism.js", function(){
			$.getScript("scripts/SourceCode/CodeHelperFunctions.js", function(){	
				
				//load zTree css-files
				let cssLink = document.createElement("link");
				cssLink.type = "text/css";
				cssLink.rel = "stylesheet";
				cssLink.href = "node_modules/prismjs/themes/prism.css";
				document.getElementsByTagName("head")[0].appendChild(cssLink);
				
				
				cssLink = document.createElement("link");
				cssLink.type = "text/css";
				cssLink.rel = "stylesheet";
				cssLink.href = "scripts/SourceCode/prismPluginCodeController.css";
				document.getElementsByTagName("head")[0].appendChild(cssLink);
				
				//create html elements
				let codeViewDiv = document.createElement("DIV");
				codeViewDiv.id = "codeViewDiv";
                rootDiv.appendChild(codeViewDiv);
                
                if(controllerConfig.showCodeWindowButton===true) {
                    //button
                     var codeWindowId = "jqxCodeWindowButton";
                     var codeWindowButtonType = "button";
                     var jqxCodeWindowButton = document.createElement("BUTTON");
                     jqxCodeWindowButton.type = codeWindowButtonType;
                     jqxCodeWindowButton.id = codeWindowId;
                     var codeWindowtext = document.createTextNode("Show source code");
                     jqxCodeWindowButton.appendChild(codeWindowtext);
                     codeViewDiv.appendChild(jqxCodeWindowButton);
                     
                     $("#jqxCodeWindowButton").jqxButton({ 
                        theme: "metro",
                        width: "98%",
                        height: "24px",
                        textImageRelation: "imageBeforeText", 
                        imgPosition:"left",
                        textPosition: "left", 
                        imgSrc: "scripts/SourceCode/images/open_in_new.png" 
                      });
                      
                     $("#jqxCodeWindowButton").on('click', function (){
                     codeWindow = window.open("scripts/SourceCode/codepage.html", "CodePage", "width=500,"+
                            "height=500, menubar=no, status=no, titlebar=no,"+
                            "toolbar=no, scrollbars");
                        // lade Quellcode, des zuletzt betrachteten Objekts
                     codeWindow.addEventListener('load', displayCodeChild, true);
                     });
                };
               
                if(controllerConfig.showCode===true) {
                
                    //codeField
                    let codeValueDiv = document.createElement("DIV");
                    codeValueDiv.id = "codeValueDiv";
                    let codePre = document.createElement("PRE");
                    codePre.className = "line-numbers language-"+controllerConfig.fileType;
                    codePre.id = "codePre";
                    codePre.style = "overflow:auto;";

                    let codeTag = document.createElement("CODE");
                    codeTag.id = "codeTag";
                    
                    codePre.appendChild(codeTag);
                    codeValueDiv.appendChild(codePre);
                    codeViewDiv.appendChild(codeValueDiv);
                };
            });
		});

        events.selected.on.subscribe(onEntitySelected);
    }


  

    function reset() {                
        resetSourceCode();
		
        if(codeWindow) {
            codeWindow.close();
        }
    }

    function resetSourceCode(){
        lastObject = {file: null, classEntity: null, entity: null};        
        //var codeTag = $("#codeTag")[0].textContent = "";
		
        if(codeWindow) {
            codeWindow.reset();
        }
    }


    function displayCodeChild(){        
        if(codeWindow) {
            let file = lastObject.file;
             if(!file.startsWith("http")) {
                    file = "../" + file;
                }
            codeWindow.displayCode(file, lastObject.classEntity, lastObject.entity, controllerConfig.fileType);
        }
    }

    function onEntitySelected(applicationEvent) {
		
        const entity = applicationEvent.entities[0];

        if(controllerConfig.fileType === "java"){
            if (entity.type === "Namespace"){
                // Package 
                resetSourceCode();
                return;
            }
            // classEntity = Klasse, in der sich das selektierte Element befindet
            // inner Klassen werden auf Hauptklasse aufgeloest
            let classEntity = entity;
            while( classEntity.type !== "Class" ){
                classEntity = classEntity.belongsTo;
            }		
            
            // ersetze . durch / und fuege .java an -> file
            const javaCodeFile = classEntity.qualifiedName.replace(/\./g, "/") + "." + controllerConfig.fileType;
    
            displayCode(javaCodeFile, classEntity, entity);
        } else if(controllerConfig.fileType === "c"){
            const cCodeFile = entity.filename;
            displayCode(cCodeFile);
        }
       	          
    }

    function displayCode(file, classEntity, entity){
        if (controllerConfig.url === "") {
            file = "../../ui/" + modelUrl + "/src/" + file;
        } else {
            file = controllerConfig.url + file;
        }
       
       // fuer das Extrafenster
       lastObject.file = file;
       lastObject.relativeFile = modelUrl + "/src"
       lastObject.classEntity = classEntity;
       lastObject.entity = entity;
       displayCodeChild();

       codeHelperFunction.displayCode(file, classEntity, entity, publishOnEntitySelected, controllerConfig.fileType);                
    }     

    function publishOnEntitySelected(entityId){
        const applicationEvent = {
            sender: sourceCodeController,
            entities: [model.getEntityById(entityId)]
        };
        
        events.selected.on.publish(applicationEvent);	
    } 



    return {
        initialize          : initialize,
        activate            : activate,
        reset               : reset,
    };
})();
