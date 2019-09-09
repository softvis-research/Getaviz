var viveUI = (function() {

    function init() {
        AFRAME.registerComponent("vive-legend", {
            init: function () {
                this.el.innerHTML = 
                    `<a-entity text="value: Legend;" position="0.3 0.2 0"></a-entity>`+
                    `<a-image src="#legend-package" position="-0.15 0.1 0" width="0.05" height="0.05"></a-image>`+
                    `<a-entity text="value: package;" position="0.4 0.1 0"></a-entity>`+
                    `<a-image src="#legend-type" position="-0.15 0.05 0" width="0.05" height="0.05"></a-image>`+
                    `<a-entity text="value: type;" position="0.4 0.05 0"></a-entity>`
            }
        })
        
        AFRAME.registerComponent("vive-tooltip", {
            init: function () {}
        })
    }

    return {
        init: init
    }
})()

