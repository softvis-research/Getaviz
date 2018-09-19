var events = (function() {

	
	//events
	let events = { };


    //***************
    //state events
    //***************
	const statesArray = Object.keys(model.states);
	
	statesArray.forEach(function(stateName){
	
		let state = model.states[stateName];
		
		let on = {
			name : "on" + state.name.charAt(0).toUpperCase() + state.name.slice(1) + "Event",
			
			subscribe : function(listener){
				subscribeEvent(this, listener);
			},

			unsubscribe: function(listener){
				unsubscribeEvent(this, listener);
			},
			
			publish	: function(applicationEvent){
				publishStateEvent(this, applicationEvent);				
			}	
		};
		
		let off = {
			name : "off" + state.name + "Event",
			
			subscribe : function(listener){
				subscribeEvent(this, listener);
			},

			unsubscribe: function(listener){
				unsubscribeEvent(this, listener);
			},
			
			publish	: function(applicationEvent){
				publishStateEvent(this, applicationEvent);		
			}			
		};
		
		events[stateName] = {
			name 			: stateName,
			on 				: on,
			off 			: off,
			getEntities 	: function(){
				return model.getEntitiesByState(this);
			}
		};
	});

	

    //**************
    //log events
    //**************

    const logTypes = {
		info			: { name: "info"},
		warning			: { name: "warning"},
		error			: { name: "error"},
		controller		: { name: "controller"},
		action			: { name: "action"},
		event			: { name: "event"},
		manipulation	: { name: "manipulation"},
	};	

	events.log = {};

	const logTypeArray = Object.keys(logTypes);

	logTypeArray.forEach(function(logTypeName){
		const logType = logTypes[logTypeName];
		
		const log = {
			type : "log" + logType.name.charAt(0).toUpperCase() + logType.name.slice(1) + "Event",
			
			subscribe : function(listener){
				subscribeEvent(this, listener);
			},
			
			publish	: function(logEvent){
								
				//no listener subscribed? 
				//-> endless loop warning 
				//-> output on console 
				let eventListenerArray = eventMap.get(this);
				if(eventListenerArray === undefined){
					if(logEvent.text){
						console.log("NO LOGGER for " + this.type + " subscribed! - " + logEvent.text); 
					} else {
						console.log("NO LOGGER for " + this.type + " subscribed!"); 
					}
					return;
				}
				publishEvent(this, logEvent);
			}

		};

		events.log[logTypeName] = log;
	});


	//**************
    //UI events
    //**************

	events.ui = {};

	const buttonClick = {
		type : "buttonClickEvent",
		
		subscribe : function(listener){
			subscribeEvent(this, listener);
		},
		
		publish	: function(logEvent){
			publishEvent(this, logEvent);
		}
	};

	events.ui.buttonClick = buttonClick;
	
	//**************
    //Config events
    //**************
	
	const configTypes = {
		weight				: { name: "weight"},
		innerClasses		: { name: "innerClasses"},
		bundledEdges		: { name: "bundledEdges"},
		filterSettings		: { name: "filterSettings"},
        issues              : { name: "issues"}
	};
	events.config = {};
	const configTypeArray = Object.keys(configTypes);

	configTypeArray.forEach(function(configTypeName){
		const configType = configTypes[configTypeName];
	
		const config = {
			type : "config" + configType.name.charAt(0).toUpperCase() + configType.name.slice(1) + "Event",
			
			subscribe : function(listener){
				subscribeEvent(this, listener);
			},
			
			publish	: function(logEvent){
								
				//no listener subscribed? 
				//-> endless loop warning 
				//-> output on console 
				let eventListenerArray = eventMap.get(this);
				if(eventListenerArray === undefined){
					if(logEvent.text){
						console.log("NO LOGGER for " + this.type + " subscribed! - " + logEvent.text); 
					} else {
						console.log("NO LOGGER for " + this.type + " subscribed!"); 
					}
					return;
				}
				publishEvent(this, logEvent);
			}
		};

		events.config[configTypeName] = config;
	});
	
	//event to listener map
	let eventMap = new Map();
	
	//event to model listener map
	let eventModelMap = new Map();
	
	function subscribeEvent(eventType, listener) {
		
		if(!eventType in events){
			events.log.error.publish({ text: "event " + eventType.name +" not in events"});		
			return;
		}		
		
		let eventListenerArray = eventMap.get(eventType);
			
		if(eventListenerArray === undefined){
			eventListenerArray = [];
			eventMap.set(eventType, eventListenerArray);
			
			eventModelMap.set(eventType, listener);
			return;
		}
		
		if(listener in eventListenerArray){
			events.log.warning.publish({ text: "listener already subscribes"});
			return;
		} 
		
		eventListenerArray.push(listener);		
	}


	function unsubscribeEvent(eventType, listener){

		if(!eventType in events){
			events.log.error.publish({ text: "event " + eventType.name +" not in events"});		
			return;
		}	

		let eventListenerArray = eventMap.get(eventType);
		if(eventListenerArray === undefined || !listener in eventListenerArray){
			events.log.warning.publish({ text: "unsubscribe not subscribed listener: " + listener.toString()});
			return;
		}

		eventListenerArray.splice(eventListenerArray.indexOf(listener), 1);

	}


	function publishStateEvent(eventType, applicationEvent){
		events.log.event.publish({ eventTypeName: eventType.name, applicationEvent: applicationEvent});

		try{
			publishEvent(eventType, applicationEvent);		
		} catch(exception){
			events.log.error.publish({text: exception + "[" + exception.fileName + "-" + exception.lineNumber + "-" + exception.columnNumber + "]" });
		}
	}

	
	function publishEvent(eventType, applicationEvent){
						
		let eventListenerArray = eventMap.get(eventType);
		
		if(eventListenerArray === undefined){
			events.log.warning.publish({ text: "no listener subscribed"});
			return;
		}
		
		applicationEvent.eventType = eventType;
		applicationEvent.timeStamp = Date.now();

		//publish to listeners
		eventListenerArray.forEach(function(listener){
			try{
				listener(applicationEvent);	
			} catch(exception){
				events.log.error.publish({text: exception + "[" + exception.fileName + "-" + exception.lineNumber + "-" + exception.columnNumber + "]" });
			}
		});
		
		//change state of entity
		eventModelMap.get(eventType)(applicationEvent);		
	}
		

	return events;
	
})();
