AFRAME.registerComponent("vive-legend", {
    init: function () {
        this.el.innerHTML = 
            `<a-entity text="value: Legend; align: center; anchor: center; baseline: top;" position="0 0.5 0"></a-entity>`+
            `<a-image src="#legend-package"                 position="-0.3  0.3   0" width="0.05" height="0.05"></a-image>`+
            `<a-entity text="value: package;"               position=" 0.3  0.305 0"></a-entity>`+
            `<a-image src="#legend-type"                    position="-0.3  0.2   0" width="0.05" height="0.05"></a-image>`+
            `<a-entity text="value: type;"                  position=" 0.3  0.205 0"></a-entity>`+
            `<a-image src="#legend-control-left-menu"       position="-0.3  0.1   0" width="0.1" height="0.1"></a-image>`+
            `<a-entity text="value: show/hide Legend;"      position=" 0.3  0.105 0"></a-entity>`+
            `<a-image src="#legend-control-left-grip"       position="-0.3  0     0" width="0.1" height="0.1"></a-image>`+
            `<a-entity text="value: scroll Sourcecode"      position=" 0.3  0.005 0"></a-entity>`+
            `<a-image src="#legend-control-right-trackpad"  position="-0.3 -0.1   0" width="0.1" height="0.1"></a-image>`+
            `<a-entity text="value: rotate left;"           position=" 0.3 -0.095 0"></a-entity>`+
            `<a-image src="#legend-control-right-grip"      position="-0.3 -0.2   0" width="0.1" height="0.1"></a-image>`+
            `<a-entity text="value: rotate right;"          position=" 0.3 -0.195 0"></a-entity>`+
            `<a-image src="#legend-control-right-trigger"   position="-0.3 -0.3   0" width="0.1" height="0.1"></a-image>`+
            `<a-entity text="value: fly forward;"           position=" 0.3 -0.295 0"></a-entity>`+
            `<a-image src="#legend-control-right-menu"      position="-0.3 -0.4   0" width="0.1" height="0.1"></a-image>`+
            `<a-entity text="value: select entity;"         position=" 0.3 -0.395 0"></a-entity>`
    }
})

AFRAME.registerComponent("vive-tooltip", {
    init: function () {}
})