var viveTooltipController = (function(){
    /**
     * Makes the tooltip element in the UI visible and shows the type and name of the focused entity inside
     * 
     * @param {String} id of the focused entity
     */
    function showTooltip(id) {
        var itemString = getItemStringFromID(id)
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

    return {
        showTooltip: showTooltip,
        hideTooltip: hideTooltip
    }
})()