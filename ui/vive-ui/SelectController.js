(function(){
    setTimeout(() => {
        var boxes = document.querySelectorAll('a-box')
        boxes.forEach( (box) => {
            var color = box.getAttribute('color')
            box.addEventListener('mouseenter', (evt) => {
                box.setAttribute('color', '#FF0000')
                showTooltip(getItemStringFromID(box.id))
            })
            box.addEventListener('mouseleave', (evt) => {
                box.setAttribute('color', color)
                hideTooltip()
            })
        })
    }, 1000);
})()

function showTooltip(name) {
    document.getElementById('vive-tooltip').innerHTML = 
        `<a-entity text="value: ${name}; align: center;" position="0 0 0"></a-entity>`
    document.getElementById('vive-tooltip').object3D.visible = true
}

function hideTooltip() {
    document.getElementById('vive-tooltip').object3D.visible = false
}

function getItemStringFromID(id) {
    var object = metaData.find( (item) => {
        return item.id === id
    })
    var name = object.name
    var type = object.type
    if (type === "FAMIX.Namespace") {
        return `Package: ${name}`
    } else if (type === "FAMIX.Class") {
        return `Class: ${name}`
    }
}