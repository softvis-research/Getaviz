/**
 * Sets EventListener for all <a-box> tags. 
 * The Eventlisteners are:
 *  - mouseenter -> when an entity will be hovered
 *  - mouseleave -> when an entity will not be hovered anymore
 *  - click      -> when an entity will be selected
 */
(function(){
    setTimeout(() => {
        model.initialize(metaData);
        var boxes = document.querySelectorAll('a-box')
        boxes.forEach( (box) => {
            var color = box.getAttribute('color')
            var entity = model.getEntityById(box.id)
            box.addEventListener('mouseenter', (evt) => {
                var entities = []
                entities.push(entity)
                
		        canvasManipulator.highlightEntities(entities, 'darkred');
                model.getEntityById(box.id).hovered = true
                showTooltip(getItemStringFromID(box.id))
            })
            box.addEventListener('mouseleave', (evt) => {
                var entities = []
                entities.push(entity)
		        canvasManipulator.unhighlightEntities(entities, color);
                hideTooltip()
            })
            box.addEventListener('click', (evt)=> {
                var entities = []
                entities.push(entity)
                var applicationEvent = {
                    sender		: canvasSelectController,
                    entities	: entities
                };
                entity.selected = true
                canvasSelectController.onEntitySelected(applicationEvent)
                console.log(entity);
            })
        })
        
    }, 1000);
})()

/**
 * Makes the tooltip element in the UI visible and shows the itemString inside
 * 
 * @param {String} itemString should contain the type and the name of the selected Item
 */
function showTooltip(itemString) {
    document.getElementById('vive-tooltip').innerHTML = 
        `<a-entity text="value: ${itemString}; align: center;" position="0 0 0"></a-entity>`
    document.getElementById('vive-tooltip').object3D.visible = true
}

/**
 * Hides the tooltip element in the UI 
 */
function hideTooltip() {
    document.getElementById('vive-tooltip').object3D.visible = false
}

/**
 * Looks for item for specific id and returns a string for the tooltip
 * 
 * @param {String} id id of the selected Element
 */
function getItemStringFromID(id) {
    var object = model.getEntityById(id)
    var name = object.name
    var type = object.type
    if (type === "Namespace") {
        return `Package: ${name}`
    } else if (type === "Class") {
        return `Class: ${name}`
    }
}