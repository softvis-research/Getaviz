/**
 * Sets EventListener for all <a-box> tags. 
 * The Eventlisteners are:
 *  - mouseenter -> when an entity will be hovered
 *  - mouseleave -> when an entity will not be hovered anymore
 *  - click      -> when an entity will be selected
 * 
 *  The EventListeners do also work for the vive-laser-controls
 */
(function(){
    setTimeout(() => {
        document.querySelector('.a-enter-vr-button').addEventListener('click', (buttonEvt) => {
            document.getElementById('vive-legend').object3D.visible = true
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

                /* this can be used with the uncomment <a-cursor> from the model to test without vive-device */
                // box.addEventListener('click', (evt)=> {
                //     var selectEvent = {
                //         sender: canvasManipulator,
                //         entities: [entity]
                //     }
                //     events.selected.on.publish(selectEvent)
                // })
            })
            
            /* this can be used with the uncomment <a-cursor> from the model to test without vive-device */
            // document.addEventListener('keyup', (evt)=>{
            //     if (evt.code === 'KeyX') {
            //         viveSourcecodeController.scrollCode()
            //     } else if (evt.code === 'KeyL') {
            //         const element = document.getElementById('vive-legend')
            //         if (element.object3D.visible === true) {
            //             element.object3D.visible = false
            //         } else {
            //             element.object3D.visible = true
            //         }
            //     }
            // })
        })
    }, 4000);
})()

