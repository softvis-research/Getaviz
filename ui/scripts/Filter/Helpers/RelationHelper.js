// based on RelationConnectorController.js
class RelationHelper {
	static extractPositions(data, filter) {
		let positions = new Map();
		let allEntities = model.getAllEntities();
		let visualizedEntities = [];

		data.mapping.forEach(mapping => {
			let min = RelationHelper.parseObjectPosition(mapping.min);
			let max = RelationHelper.parseObjectPosition(mapping.max);
			let connectorPosition = [];

			for (let i = 0; i < min.length; ++i)
				connectorPosition[i] = Math.abs(max[i] - min[i]) / 2 + min[i];

			positions.set(mapping.name, connectorPosition);

			if (allEntities.has(mapping.name)) visualizedEntities.push(allEntities.get(mapping.name));
		});

		filter.visualizedEntities = visualizedEntities;
		filter.loadedPositions = positions;
	}

	static parseObjectPosition(positionString) {
		let position = positionString.split(' ');

		for (let i = 0; i < position.length; ++i) position[i] = parseFloat(position[i]);

		return position;
	}

	static getObjectPosition(objectId, loadedPositions) {
		let position = null;

		if (loadedPositions.has(objectId)) {
			position = loadedPositions.get(objectId);
		} else {
			let myElement = jQuery('#' + objectId)[0];
			if (myElement != undefined)
				position = RelationHelper.parseObjectPosition(myElement.getAttribute('translation'));
		}

		return position;
	}

	static createLine(start, end, color, size) {
		let abs = Math.sqrt(
			Math.pow(end[0] - start[0], 2) +
				Math.pow(end[1] - start[1], 2) +
				Math.pow(end[2] - start[2], 2)
		);

		let translation = [];
		translation[0] = start[0] + (end[0] - start[0]) / 2.0;
		translation[1] = start[1] + (end[1] - start[1]) / 2.0;
		translation[2] = start[2] + (end[2] - start[2]) / 2.0;

		let scale = [];
		scale[0] = size;
		scale[1] = abs;
		scale[2] = size;

		let rotation = [];
		rotation[0] = end[2] - start[2];
		rotation[1] = 0;
		rotation[2] = -1.0 * (end[0] - start[0]);
		let angle = Math.acos(
			(end[1] - start[1]) /
				Math.sqrt(
					Math.pow(end[0] - start[0], 2) +
						Math.pow(end[1] - start[1], 2) +
						Math.pow(end[2] - start[2], 2)
				)
		);
		rotation[3] = isNaN(angle) ? 0 : angle;

		let transform = document.createElement('Transform');
		transform.setAttribute('translation', translation.toString());
		transform.setAttribute('scale', scale.toString());
		transform.setAttribute('rotation', rotation.toString());

		let shape = document.createElement('Shape');
		transform.appendChild(shape);

		let appearance = document.createElement('Appearance');
		shape.appendChild(appearance);

		let material = document.createElement('Material');
		material.setAttribute('diffuseColor', color);
		appearance.appendChild(material);

		let cylinder = document.createElement('Cylinder');
		cylinder.setAttribute('radius', '0.25');
		cylinder.setAttribute('height', '1');
		shape.appendChild(cylinder);

		return transform;
	}

	static removeAddedElements() {
		$('.transformContainer').remove();
		$('.expansionLine').remove();
	}
}
