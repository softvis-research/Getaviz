var viveLegendController = (function(){
    
    function showLegend() {
        legend = document.getElementById('vive-legend')
        legend.object3D.visible = true;
    }

    function hideLegend() {
        legend = document.getElementById('vive-legend')
        legend.object3D.visible = false;
    }

    function toggleVisibility() {
        legend = document.getElementById('vive-legend')
        if (legend.object3D.visible === true) {
            legend.object3D.visible = false;
        } else {
            legend.object3D.visible = true;
        }
    }

    return {
        showLegend: showLegend,
        hideLegend: hideLegend,
        toggleVisibility: toggleVisibility
    }
})()