var codeHelperFunction =(function(){

    //Prism ist hier ur Verfügbar, wenn vorher der Controller SourceCode durchlaufen wurde

    // Prism-Plugin,
    // markiert accessModifier mit spezieller Klasse
    Prism.hooks.add('wrap', function(env){
        if( env.type == "keyword" && (
            env.content == "private" || env.content == "public"
            || env.content == "protected")
        ){
            env.classes.push("accessModifier");
        }
    });

    // display and highlight Code    
    function displayCode(file, classEntity, entity, callBackFunction, fileType){
		const codeTag = $("#codeTag").get(0);
		const xhttp = new XMLHttpRequest();
		

        xhttp.onreadystatechange = function(){
			if (xhttp.readyState === 4 && xhttp.status === 200) {
                            

				codeTag.textContent = xhttp.responseText;

				Prism.highlightElement(codeTag, false, function(){
                    if(fileType === "java") {
                        // alle einfachen Textnodes werden mit einem
                        // span-Tag umgeben, dadurch Selektierung moeglich
                        textNodesToSpan();

                        // alle Vorkommen des selektierten Elements
                        // im QUellcode hervorheben
                        highlightSelectedElement(entity);

                        // versieht Definitionen der Attribute und
                        // Methoden mit einem Klickereignis
                        addInteraction(classEntity, callBackFunction);
                    }
                });     
			} else if (xhttp.readyState === 4 && xhttp.status === 404){
				codeTag.textContent = "Error: " + file + ", file not found!";
			}
		};

		xhttp.open("GET", file, true);
		xhttp.send();
    }



    // alle Textnodes in ein span-Tag integrieren
    function textNodesToSpan(){
        const codeTag = $("#codeTag").get(0);
        const codeTagChilds = codeTag.childNodes;
        for (let i=0; i<codeTagChilds.length; i++){
            if (codeTagChilds[i].nodeName === "#text" &&
                codeTagChilds[i].textContent.trim().length>0){                                                    
                    const span = document.createElement("span");
                    const textNode = document.createTextNode(codeTagChilds[i].textContent);
                    span.appendChild(textNode);                    
                    codeTag.replaceChild(span, codeTagChilds[i]);
            }
        }
    }

    // hebt alle Vorkommen des selektierten Elements besonders hervor 
    function highlightSelectedElement(entity){   
        if(typeof entity === "undefined") {
            return;
        }
        console.log(entity);        
        if ( entity.type === "Attribute" ){                    
            var codeTag = $("#codeTag").get(0);
            var codeTagChilds = codeTag.childNodes;
            
            // RegExp: finde nur 'at': \b -> "whole word only"-Match
            var reg = new RegExp('\\b'+entity.name+'\\b', 'g');
            for( var i=0; i<codeTagChilds.length; i++ ){
                if( codeTagChilds[i].textContent.search(reg)>=0 && codeTagChilds[i].className === "" ){
                    codeTagChilds[i].className += " codeControllerHighlightAttribute";
                }
            }   
			$('html, body, pre, code').animate({
					scrollTop: $($(".codeControllerHighlightAttribute")[0]).position().top - 100
				}, 1000);							
        } else if (entity.type === "Method"){            
            // Methoden werden automatisch durch Prism mit einer Klasse versehen
            var tokens = $(".token.function");                     
            for(var i=0; i<tokens.length; i++){                        
                if (tokens[i].textContent === entity.name + "("){
                    tokens[i].className += " codeControllerHighlightMethod";                          
                }            
            }
			$('html, body, pre, code').animate({
					scrollTop: $($(".codeControllerHighlightMethod")[0]).position().top - 100
				}, 1000);	
        } else {
			// Klasse selektiert, scrolle ganz nach oben
			$('html, body, pre, code').animate( { scrollTop: 0 }, 1000);
		}        
    }
	
	
	// Klick im Quellcode auf Attribut- oder Methodendefinition entspricht einer Selektion 
    function addInteraction(classEntity, callBackFunction){  
        
        var accessModifier = $(".token.keyword.accessModifier");
        // Attribute und Methoden im Quellcode auflisten:
        var attributes = [];
        var methods = [];
		var classes = [];
        
        for(var line=0; line < accessModifier.length; line++){
            // fuer Attribute: fuege alle Elemente bis ; hinzu            
            var element = accessModifier[line]; // Prism-Plugin (public, private, protected)
            var tmpField = [element];
            var isMethodOrClass = 0; // 0 - Attribute, 1 - Method, 2 - Class        
            while( ! (element.nextSibling.textContent == "{"
                   || element.nextSibling.textContent == ";" ) ){                       
                       if (element.className && element.className.includes("function")){
							isMethodOrClass = 1;
                       } else if (element.className && element.className.includes("class-name")){
							isMethodOrClass = 2;
					   }
                       element = element.nextSibling;
                       tmpField.push(element);
            }
            if(isMethodOrClass == 1){
                methods.push(tmpField);
            } else if(isMethodOrClass == 2){
                classes.push(tmpField);
            } else{
				attributes.push(tmpField);
			}			
        }
        
        var elementsOfClass = findAllElementsOfClass(classEntity);   		
        
        addInteractionAttributes(elementsOfClass.attributes, attributes, callBackFunction);
        addInteractionMethods(elementsOfClass.methods, methods, callBackFunction);        
		addInteractionClasses(elementsOfClass.classes, classes, callBackFunction);
		
    }	



    // Listet alle Attribute und Methoden einer Klasse aus dem Modell auf
    function findAllElementsOfClass(classEntity){        
        var attributes = [];
        var methods = [];
		var classes = [];
                
        if(typeof classEntity === "undefined") {
            return;
        }
        
        classEntity.children.forEach(function(child){
            if (child.type == "Attribute" ){
                attributes.push(child);
            } else if (child.type == "Method" ){
                methods.push(child);
            } else if (child.type == "Class" ){
                classes.push(child);
            }
        });
        
        return {'attributes': attributes, 'methods': methods, 'classes': classes};
    }
	



	// mappt Attribute Modell mit Attribute Quellcode 
    function addInteractionAttributes(classAttributes, attributes, callBackFunction){        
        // fuege die id's der Attribute aus dem Modell den Attributen im Quelltext an		
		// Beispiel: attributes[0] = [private, String bankName], attributes[1] = [public, int, anzahl]		
        for(var line=0; line < attributes.length; line++){			
            for(var element=0; element < attributes[line].length; element++){    
                for(var classAttribute=0; classAttribute < classAttributes.length; classAttribute++){
                    // RegExp, da attributes[line] == "nichtStandardDatentyp Attributename" moeglich ist
                    var pattern = new RegExp('\\b'+classAttributes[classAttribute].name+'\\b', 'g');                            					
                    if (attributes[line][element].textContent.search(pattern) >= 0){                        
						// attributes[0][1] = String bankName === /\\bbankName\\b/g = classAttributes[i].name = bankName
                        // id des Attributes im Modell anfuegen
                        attributes[line][element].id = classAttributes[classAttribute].id;
                        attributes[line][element].className += " codeControllerHover";
                        attributes[line][element].onclick = function(){
                            callBackFunction(this.id);
                        }                                        
                    }                     
                }
            }
        }
    }
	
	// mappt Klasse Modell mit Klasse Quellcode, sie addInteractionAttributes()
	function addInteractionClasses(classClasses, classes, callBackFunction){
		for(var line=0; line < classes.length; line++){			
            for(var element=0; element < classes[line].length; element++){    
                for(var classClass=0; classClass < classClasses.length; classClass++){                    
                    var pattern = new RegExp('\\b'+classClasses[classClass].name+'\\b', 'g');                            					
                    if (classes[line][element].textContent.search(pattern) >= 0){                        						
                        classes[line][element].id = classClasses[classClass].id;
                        classes[line][element].className += " codeControllerHover";
                        classes[line][element].onclick = function(){
                            callBackFunction(this.id);	
                        }                                        
                    }                     
                }
            }
        }
	}

	// mappt Methoden Modell mit Methoden Quellcode
    function addInteractionMethods(classMethods, methods, callBackFunction){
        // Schnittstellen der Methoden im Modell
        // in regulaere Ausdruecke umwandeln
        var patterns = [];
        for(var classMethod=0; classMethod < classMethods.length; classMethod++){            
            var signature = ".*" + classMethods[classMethod].signature;          
            signature = signature.replace(/void/, "");
            signature = signature.replace(/,/g, " .*");
            signature = signature.replace(/\(/g, ".*");
			// Leerzeichen beachten, fuer Datentypen als Teil einer Zeichenkette!!!!
			// floatMeiDatentyp points -> float wird nicht erkannt
            signature = signature.replace(/\)/g, " .*");           
            patterns.push({sig:signature, pos:classMethod});                    
        }
        // nach Groesse sortieren, Reihenfolge wichtig bei
        // ueberladenen Methoden
        patterns.sort(function(a, b){
           var eins = (a.sig.match(/\.\*/g) || []).length;
           var zwei = (b.sig.match(/\.\*/g) || []).length;
           
           return -(eins-zwei);
        });
        // fuege die id's der Methoden aus dem Modell
        // den Methoden im Quellcode an 
        for(var i=0; i<patterns.length; i++){
            patterns[i].sig = new RegExp(patterns[i].sig,'g');

            for(var line=0; line < methods.length; line++){
                // HTML-Tags zu einer Methode zu einem String zusammenfuehren
                var quelltextZeile = "";
                for(var element=0; element < methods[line].length; element++){
                    quelltextZeile += methods[line][element].textContent;
                }
                if(quelltextZeile.match(patterns[i]) != null){
                    for(var element=0; element < methods[line].length; element++){
                        var testNamePattern = new RegExp('\\b'+classMethods[patterns[i].pos].name+'\\b', 'g');                            
                        if(!methods[line][element].id  && methods[line][element].textContent.search(testNamePattern) >= 0){                            
                            // ABBRUCH wenn id schon gesetzt!!!
                            methods[line][element].id = classMethods[patterns[i].pos].id;
                            methods[line][element].className += " codeControllerHover";
                            methods[line][element].onclick = function(){
                                callBackFunction(this.id);
                            }
                        }
                    }
                }                                
            }   
        }
    }



    return {
        displayCode: displayCode
    };
    
})();
