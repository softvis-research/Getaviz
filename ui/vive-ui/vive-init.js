/**
 * Sets EventListener for all <a-box> tags. 
 * The Eventlisteners are:
 *  - mouseenter -> when an entity will be hovered
 *  - mouseleave -> when an entity will not be hovered anymore
 *  - click      -> when an entity will be selected
 * 
 *  The Events do also work for the vive-laser-controls
 */
(function(){
    setTimeout(() => {
        var boxes = document.querySelectorAll('a-box')

        boxes.forEach( (box) => {
            var entity = model.getEntityById(box.id)

            box.addEventListener('mouseenter', (evt) => {
                var applicationEvent = {
                    sender		: canvasManipulator,
                    entities	: [entity]
                };
                events.hovered.on.publish(applicationEvent)
                viveTooltipController.showTooltip(box.id)
            })

            box.addEventListener('mouseleave', (evt) => {
                var applicationEvent = {
                    sender		: canvasManipulator,
                    entities	: [entity]
                };
                events.hovered.off.publish(applicationEvent)
                viveTooltipController.hideTooltip()
            })

            box.addEventListener('click', (evt)=> {
                var selectEvent = {
                    sender: canvasManipulator,
                    entities: [entity]
                }
                events.selected.on.publish(selectEvent)
            })
        })
        
    }, 2000);
})()

