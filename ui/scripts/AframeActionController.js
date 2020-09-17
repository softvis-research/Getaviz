var actionController = (function () {


//*********************************
//	Constants
//*********************************

    var MOUSE_BUTTON_LEFT = 1;
    var MOUSE_BUTTON_RIGHT = 2;
    var MOUSE_BUTTON_MIDDLE = 3;


//*********************************
//	Variables
//*********************************

    var defaultTickTime = 1;

    var hoveredEntity = null;
    var latestMouseButtonPressed = null;

    var cursorX = 0;
    var cursorY = 0;

    //actions object
    var actions = {
        mouse: {
            key: [],
            down: createActionObject("mouseKeyDown"),
            up: createActionObject("mouseKeyUp"),
            during: createActionObject("mouseKeyDuring"),
            move: createActionObject("mouseMove"),
            doubleClick: createActionObject("mouseDoubleClick"),
            scroll: createActionObject("mouseScroll"),
            hover: createActionObject("mouseHover"),
            unhover: createActionObject("mouseUnhover"),
        },
        keyboard: {
            key: []
        }
    };

    var mouseMovedEvent = {};

    //create mouse action object for every key
    for (let i = 0; i < 5; i = i + 1) {
        actions.mouse.key.push({
            pressed: false,
            bubbles: false,
            startTime: 0,
            lastTick: 0,
            down: createActionObject("mouseKeyDown"),
            during: createActionObject("mouseKeyDuring"),
            up: createActionObject("mouseKeyUp")
        });
    }

    //create key action object for every key
    for (let i = 0; i < 200; i = i + 1) {
        actions.keyboard.key.push({
            pressed: false,
            bubbles: false,
            startTime: 0,
            lastTick: 0,
            down: createActionObject("keyboardKeyDown"),
            during: createActionObject("keyboardKeyDuring"),
            up: createActionObject("keyboardKeyUp")
        });
    }

    function createActionObject(type) {
        var tickTimePerListener = new Map();

        return {
            type: type,
            actionListeners: [],
            tickTimePerListener: tickTimePerListener,
            subscribe: function (listener, tickTime) {
                subscribeAction(this, listener, tickTime);
            },
            unsubscribe: function (listener) {
                unsubscribeAction(this, listener);
            }
        };
    }


//*********************************
//	Initialization
//*********************************	


    function initialize() {
        var canvas = document.getElementById(canvasId);
        AFRAME.registerComponent('mouselistener', {
            init: function () {
                this.el.addEventListener("mouseup", function (eventObject) {
                    //general upAction for controllers
                    if(eventObject.target.id != canvasId) {
                        eventObject.component = hoveredEntity;
                        eventObject.which = latestMouseButtonPressed;
                        upAction(actions.mouse.key[getMouseButton(eventObject)], eventObject);
                        upAction(actions.mouse, eventObject);
                    }

                    /*if (getMouseButton(eventObject) !== undefined) {

                        if (actions.mouse.key[getMouseButton(eventObject)].bubbles) {
                            return true;
                        }
                    }

                    eventObject.cancelBubble = true;
                    eventObject.stopPropagation();
                    return false;*/
                });
                this.el.addEventListener("mousedown", function (eventObject) {
                    //  only process the event with mousebutton (.which-Attribut)
                    if (eventObject.which != null) {

                        eventObject.component = hoveredEntity;
                        latestMouseButtonPressed = eventObject.which;

                        downAction(actions.mouse.key[getMouseButton(eventObject)], eventObject);
                        downAction(actions.mouse, eventObject);


                        eventObject.cancelBubble = true;
                        eventObject.stopPropagation();
                        if (actions.mouse.key[getMouseButton(eventObject)].bubbles) {
                            return true;
                        }

                        eventObject.cancelBubble = true;
                        eventObject.stopPropagation();
                        return false;
                    }
                }, false);
                this.el.addEventListener("mouseenter", function (eventObject) {
                    var component = document.getElementById(eventObject.target.id);
                    if (component != null) {
                        hoveredEntity = component;
                    }
                    if(eventObject.target.id != canvasId) {
                        eventObject.layerX = cursorX;
                        eventObject.layerY = cursorY;
                        hoverAction(actions.mouse, eventObject);
                    }

                    if (actions.mouse.bubbles) {
                        return true;
                    }

                    eventObject.cancelBubble = true;
                    eventObject.stopPropagation();
                    return false;
                });
                this.el.addEventListener("mouseleave", function (eventObject) {
                    if(eventObject.target.id != canvasId) {
                        eventObject.layerX = cursorX;
                        eventObject.layerY = cursorY;
                        unhoverAction(actions.mouse, eventObject);
                    }
                    hoveredEntity = canvas;

                    if (actions.mouse.bubbles) {
                        return true;
                    }

                    eventObject.cancelBubble = true;
                    eventObject.stopPropagation();
                    return false;
                });
                //  interrupts mousedown events somehow
                this.el.addEventListener("mousemove", function (eventObject) {
                    moveAction(actions.mouse.move, eventObject);
                    cursorX = eventObject.layerX;
                    cursorY = eventObject.layerY;

                    if (actions.mouse.move.bubbles) {
                        return true;
                    }

                    eventObject.cancelBubble = true;
                    eventObject.stopPropagation();
                    return false;
                });
                this.el.addEventListener("dblclick", function (eventObject) {
                    eventObject.component = hoveredEntity;

                    doubleClickAction(actions.mouse.doubleClick, eventObject);

                    if (actions.mouse.doubleClick.bubbles) {
                        return true;
                    }

                    eventObject.cancelBubble = true;
                    eventObject.stopPropagation();
                    return false;
                });
                this.el.addEventListener("wheel", function (eventObject) {
                    eventObject.component = hoveredEntity;

                    scrollAction(actions.mouse.scroll, eventObject);

                    if (actions.mouse.scroll.bubbles) {
                        return true;
                    }

                    eventObject.cancelBubble = true;
                    eventObject.stopPropagation();
                    return false;
                });
                //keydown
                this.el.addEventListener("keydown", function (eventObject) {

                    downAction(actions.keyboard.key[eventObject.which], eventObject);

                    if (actions.keyboard.key[eventObject.which].bubbles) {
                        return true;
                    }

                    eventObject.cancelBubble = true;
                    eventObject.stopPropagation();
                    return false;
                });
                //keyup
                this.el.addEventListener("keyup", function (eventObject) {

                    upAction(actions.keyboard.key[eventObject.which], eventObject);

                    if (actions.keyboard.key[eventObject.which].bubbles) {
                        return true;
                    }

                    eventObject.cancelBubble = true;
                    eventObject.stopPropagation();
                    return false;
                });
            }
        });

        canvas.setAttribute("mouselistener", "");
    }


//*********************************
//	Helper
//*********************************

    function getMouseButton(eventObject) {

        if (eventObject.which) {
            switch (eventObject.which) {
                case 1:
                    return MOUSE_BUTTON_LEFT;
                case 3:
                    return MOUSE_BUTTON_RIGHT;
                case 2:
                    return MOUSE_BUTTON_MIDDLE;
                default:
                    events.log.error.publish({text: "mousebutton " + eventObject.button + " not implemented"});
                    return;
            }
        }

        if (eventObject.button) {
            switch (eventObject.button) {
                case 1:
                    return MOUSE_BUTTON_LEFT;
                case 2:
                    return MOUSE_BUTTON_RIGHT;
                case 4:
                    return MOUSE_BUTTON_MIDDLE;
                default:
                    events.log.error.publish({text: "mousebutton " + eventObject.button + " not implemented"});
                    return;
            }
        }

    }

//*********************************
//	Subscribe / Unsubscribe
//*********************************

    function subscribeAction(actionObject, listener, tickTime) {

        var actionListenerArray = actionObject.actionListeners;

        if (listener in actionListenerArray) {
            events.log.error.publish({text: "listener allready subscribes"});
            return;
        }

        actionListenerArray.push(listener);

        if (tickTime) {
            actionObject.tickTimePerListener.set(listener, tickTime);
        } else {
            actionObject.tickTimePerListener.set(listener, defaultTickTime);
        }
    }

    function unsubscribeAction(actionObject, listener) {

        var actionListenerArray = actionObject.actionListeners;

        if (!listener in actionListenerArray) {
            events.log.error.publish({text: "listener not subscribed"});
            return;
        }

        actionListenerArray.splice(actionListenerArray.indexOf(listener), 1);
    }


//*********************************
//	Actions
//*********************************	

    function downAction(action, eventObject) {
        events.log.action.publish({actionObject: action.down, eventObject: eventObject});

        action.pressed = true;
        action.startTime = Date.now();
        action.lastTick = Date.now();

        if(eventObject.component != null && eventObject.component.id != "aframe-canvas") {
            eventObject.entity = model.getEntityById(eventObject.component.id);
        }

        //activate registered down listeners
        var downListeners = action.down.actionListeners;
        if (downListeners === undefined) {
            return;
        }
        downListeners.forEach(function (downListener) {
            try {
                downListener(eventObject, action.startTime);
            } catch (err) {
                events.log.error.publish({text: err.message});
            }
        });

        //activate loop for during listeners
        var duringListeners = action.during.actionListeners;
        duringListeners.forEach(function (duringListener) {
            var tickTime = action.during.tickTimePerListener.get(duringListener);
            setTimeout(function () {
                duringAction(action, duringListener, tickTime);
            }, tickTime);
        });
    }


    function duringAction(action, duringListener, tickTime) {

        if (!action.pressed) {
            return;
        }

        events.log.action.publish({actionObject: action.during, eventObject: {}});

        var timestamp = Date.now();

        var timeSinceStart = timestamp - action.startTime;
        var timeSinceLastTick = timestamp - action.lastTick;
        action.lastTick = timestamp;

        try {
            duringListener(mouseMovedEvent, timestamp, timeSinceStart, timeSinceLastTick);
        } catch (err) {
            events.log.error.publish({text: err.message});
        }


        setTimeout(function () {
            duringAction(action, duringListener, tickTime);
        }, tickTime);
    }


    function upAction(action, eventObject) {

        events.log.action.publish({actionObject: action.up, eventObject: eventObject});

        action.pressed = false;

        var timestamp = Date.now();

        //activate registered up listeners
        var upListeners = action.up.actionListeners;
        if (upListeners === undefined) {
            return;
        }
        upListeners.forEach(function (upListener) {
            try {
                upListener(eventObject, timestamp);
            } catch (err) {
                events.log.error.publish({text: err.message});
            }
        });
    }


    function hoverAction(action, eventObject) {

        events.log.action.publish({actionObject: action.hover, eventObject: eventObject});

        var timestamp = Date.now();

        //identify entity
        if (eventObject.component && eventObject.component.id != "aframe.canvas") {
            eventObject.entity = model.getEntityById(eventObject.component);
        }


        //activate registered hover listeners
        var hoverListeners = action.hover.actionListeners;
        if (hoverListeners === undefined) {
            return;
        }
        hoverListeners.forEach(function (hoverListener) {
            try {
                hoverListener(eventObject, timestamp);
            } catch (err) {
                events.log.error.publish({text: err.message});
            }
        });
    }

    function unhoverAction(action, eventObject) {

        events.log.action.publish({actionObject: action.unhover, eventObject: eventObject});

        action.pressed = false;

        var timestamp = Date.now();

        //activate registered up listeners
        var unhoverListeners = action.unhover.actionListeners;
        if (unhoverListeners === undefined) {
            return;
        }
        unhoverListeners.forEach(function (unhoverListener) {
            try {
                unhoverListener(eventObject, timestamp);
            } catch (err) {
                events.log.error.publish({text: err.message});
            }
        });
    }


    function moveAction(action, eventObject) {

        events.log.action.publish({actionObject: action, eventObject: eventObject});

        mouseMovedEvent = eventObject;

        var timestamp = Date.now();

        var moveListeners = action.actionListeners;

        moveListeners.forEach(function (moveListener) {
            try {
                moveListener(eventObject, timestamp);
            } catch (err) {
                events.log.error.publish({text: err.message});
            }
        });
    }

    function doubleClickAction(action, eventObject) {
        events.log.action.publish({actionObject: action, eventObject: eventObject});

        var timestamp = Date.now();

        var doubleClickListeners = action.actionListeners;

        doubleClickListeners.forEach(function (doubleClickListener) {
            try {
                doubleClickListener(eventObject, timestamp);
            } catch (err) {
                events.log.error.publish({text: err.message});
            }
        });
    }

    function scrollAction(action, eventObject) {

        events.log.action.publish({actionObject: action, eventObject: eventObject});

        var timestamp = Date.now();

        //identify entity
        if (hoveredEntity != null) {
            eventObject.entity = hoveredEntity;
        }

        var scrollListeners = action.actionListeners;

        scrollListeners.forEach(function (scrollListener) {
            try {
                scrollListener(eventObject, timestamp);
            } catch (err) {
                events.log.error.publish({text: err.message});
            }
        });
    }


    return {
        initialize: initialize,
        actions: actions
    };

})();