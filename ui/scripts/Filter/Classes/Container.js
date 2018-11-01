class Container {
  constructor(id, transformation) {
    this.id = id;
    this.transformation = transformation;
    this.transformations = Object.keys(Constants.transformations).map(key => Constants.transformations[key]);
    this.layers = [];
    this.expanded = true;
    this.activated = true;
    this.inverted = false;
    this.relations = false;
    this.availableSuggestions;
  }

  addLayer(layer) {
    this.layers.push(layer);
  }

  removeLayer(layer) {
    this.layers.splice(this.layers.indexOf(layer), 1);
  }

  switchExpanded() {
    this.expanded = !this.expanded;
  }

  switchActivated() {
    this.activated = !this.activated;
  }
}
