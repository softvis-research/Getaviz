AFRAME.registerComponent("vive-package-explorer", {
    init: function () {
        this.el.innerHTML = 
            `<a-entity text="value: Package-Explorer;" position="0.3 0.2 0"></a-entity>`
    }
})

AFRAME.registerComponent("vive-legend", {
    init: function () {
        this.el.innerHTML = 
            `<a-entity text="value: Legend;" position="0.3 0.2 0"></a-entity>`
        this.el.addEventListener('mouseenter', function (evt) {
            console.log(evt.detail.intersection.point);
        });
    }
})

AFRAME.registerComponent("vive-tooltip", {
    init: function () {}
})

