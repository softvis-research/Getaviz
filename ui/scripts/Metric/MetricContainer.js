class MetricContainer {
    
    constructor(rootDiv) {
        this.layerCounter = 0;
        this.layers = [];
        this.rootDiv = rootDiv;
    }

    addLayer() {
        var newLayer = new MetricLayer((++this.layerCounter));
        this.layers.push(newLayer);

        newLayer.buildUILayer(this.rootDiv);
    }

    removeLayer() {
        
    }

}