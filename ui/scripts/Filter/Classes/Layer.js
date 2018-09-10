class Layer {
	constructor(id) {
		this.id = id;
		this.query = '';
		this.entity;
		this.includeChilds = true;
		this.activated = true;
		this.faulty = false;
	}

	switchActivated() {
		this.activated = !this.activated;
	}
}
