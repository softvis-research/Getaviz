class Filter {
	constructor() {
		this.containers = [];
		this.containerCounter = 0;
		this.layerCounter = 0;
		this.loadedPositions;
		this.visualizedEntities;
		this.domHelper = new DOMHelper(this);
	}

	// creates the GUI for the logical filter model
	redraw() {
		// init root container
		let containerDiv = this.domHelper.initRootContainer();

		// add note if filter is empty
		if (this.containers.length < 1) {
			containerDiv.appendChild(this.domHelper.buildEmptyFilterNote());
		}

		// create container elements
		for (let currentContainer of this.containers) {
			// build container
			let container = this.domHelper.buildContainer(currentContainer);
			container.appendChild(this.domHelper.buildContainerHeader(currentContainer));
			container.appendChild(this.domHelper.buildLayerContainer(currentContainer));
			containerDiv.appendChild(container);

			// calculate suggestions
			for (let currentLayer of currentContainer.layers)
				this.setSuggestionsEngine(this, currentContainer, currentLayer);
		}

		// set general widget properties
		this.domHelper.setContainerHeaderProperties(this.containers);

		// set special widget properties
		for (let currentContainer of this.containers) {
			this.domHelper.setContainerProperties(currentContainer);
		}
	}

	// ================================
	// container functionality
	// ================================

	// adds the first container with a search layer
	loadConfiguration(configuration) {
		let path = Constants.paths.folderPath + configuration;

		fetch(path)
			.then(response => {
				if (response.ok) return response.json();
				else throw new Error(Constants.strings.configFileNotFound);
			})
			.then(json => this.buildConfig(json))
			.catch(error => {
				console.error(error.message + ' (' + path + ').');
				this.loadConfiguration(Constants.paths.defaultConfig);
			});
	}

	buildConfig(config) {
		// wait for calculation of visualized entities
		if (!this.visualizedEntities) {
			setTimeout(() => this.buildConfig(config), 100);
			return;
		}

		this.containers = [];

		try {
			config.containers.forEach(configContainer => {
				let c = new Container(++this.containerCounter, configContainer.type);
				c.activated = configContainer.activated;
				c.relations = configContainer.relations;
				c.inverted = configContainer.inverted;
				c.layers = [];

				configContainer.layers.forEach(configLayer => {
					let l = new Layer(++this.layerCounter);
					l.activated = configLayer.activated;
					l.includeChilds = configLayer.includeChilds;
					l.query = configLayer.query;

					c.layers.push(l);
				});

				this.containers.push(c);
			});
		} catch (e) {
			console.error();
		}

		this.apply();
		this.redraw();
	}

	// adds a container
	addContainer(event) {
		let filter = event.target.filter;
		let c = new Container(++filter.containerCounter, 0);
		let l = new Layer(++event.target.filter.layerCounter);
		c.addLayer(l);
		filter.containers.push(c);

		filter.apply(event);
		filter.redraw();
	}

	// removes a container
	removeContainer(event) {
		event.stopPropagation();
		let container = event.target.container;
		let filter = event.target.filter;
		if (filter.containers.length > 0) {
			filter.containers.splice(filter.containers.indexOf(container), 1);
			filter.apply(event);
			filter.redraw();
		}
	}

	// expands/collapses a container
	openContainer(event) {
		let container = event.target.container;
		let filter = event.target.filter;

		if (container) {
			container.switchExpanded();
			filter.redraw();
		}
	}

	// (de)activates a container
	deactivateContainer(event) {
		let container = event.target.container;
		let filter = event.target.filter;

		container.switchActivated();
		filter.apply(event);
		filter.redraw();
	}

	// moves a container up or down
	moveContainer(event) {
		event.stopPropagation();
		let filter = event.target.filter;
		let containers = filter.containers;
		let container = event.target.container;
		let source = containers.indexOf(container);
		let target = event.target.isDirectionUp ? source - 1 : source + 1;

		if (source >= 0 && target >= 0 && target <= containers.length - 1) {
			let swap = containers[target];
			containers[target] = containers[source];
			containers[source] = swap;
			filter.apply(event);
			filter.redraw();
		}
	}

	// ================================
	// layer functionality
	// ================================

	// adds a layer to a container and expands it if its closed
	addLayer(event) {
		let filter = event.target.filter;
		let container = event.target.container;
		let l = new Layer(++filter.layerCounter);
		container.addLayer(l);

		// console.log("added layer:", filter.layerCounter);

		filter.apply(event);
		filter.redraw();
	}

	// removes a layer of a container
	removeLayer(event) {
		let filter = event.target.filter;
		event.target.container.removeLayer(event.target.layer);
		filter.apply(event);
		filter.redraw();
	}

	deactivateLayer(event) {
		let filter = event.target.filter;
		event.target.layer.switchActivated();
		filter.apply(event);
		filter.redraw();
	}

	// moves a container up or down
	moveLayer(event) {
		let filter = event.target.filter;
		let layers = event.target.layers;
		let source = layers.indexOf(event.target.layer);
		let target = event.target.isDirectionUp ? source - 1 : source + 1;

		if (source >= 0 && target >= 0 && target <= layers.length - 1) {
			let swap = layers[target];
			layers[target] = layers[source];
			layers[source] = swap;
			filter.apply(event);
			filter.redraw();
		}
	}

	// ================================
	// toolbar functionality
	// ================================

	log(event) {
		let filter = event.target.filter;
		console.log(filter);
	}

	reset(event) {
		let filter = event.target.filter;

		filter.loadConfiguration(Constants.paths.defaultConfig);

		filter.apply();
		filter.redraw();
	}

	zoom(event) {
		let filter = event.target.filter;
		let runtime = document.getElementById('x3dElement').runtime;
		let viewarea = runtime.canvas.doc._viewarea;
		let viewpoint = viewarea._scene.getViewpoint();
		let globalCenter = { x: 0, y: 0, z: 0 };

		// get visible entities
		let entities = filter.visualizedEntities.filter(entity => !entity.filtered);

		// sum up the center coordinates of the entities
		entities.forEach(entity => {
			let center = canvasManipulator.getCenterOfEntity(entity);
			Object.getOwnPropertyNames(globalCenter).forEach(dim => (globalCenter[dim] += center[dim]));
		});

		// divide through number of entities to get average center
		Object.getOwnPropertyNames(globalCenter).forEach(dim => (globalCenter[dim] /= entities.length));

		// set center of rotation to average center and zoom to fit all entities
		viewpoint.setCenterOfRotation(globalCenter);
		document.getElementById('x3dElement').runtime.showAll('negZ');
	}

	loadConfigurationFile(event) {
		document.getElementById('loadFileInput').click();
	}

	saveConfigurationFile(event) {
		let containers = event.target.filter.containers;
		let config = { containers: [] };

		containers.forEach(c => {
			// map layers of container c
			let configContainerLayers = [];
			c.layers.forEach(l => {
				let configContainerLayer = {
					activated: l.activated,
					query: l.query,
					includeChilds: l.includeChilds
				};
				configContainerLayers.push(configContainerLayer);
			});

			// map container c
			let configContainer = {
				type: c.transformation,
				activated: c.activated,
				relations: c.relations,
				inverted: c.inverted,
				layers: configContainerLayers
			};

			// add mapped container c to config
			config.containers.push(configContainer);
		});

		// set data for file
		let text = JSON.stringify(config);
		let type = 'text/plain';
		let name = 'config.json';

		// save the file
		var a = document.createElement('a');
		var file = new Blob([text], { type: type });
		a.href = URL.createObjectURL(file);
		a.download = name;
		a.click();
	}

	/*
   * ==================================================================================================================
   * filtering logic
   * ==================================================================================================================
   */

	// applies the filter to the visualization
	apply(event) {
		let filter = event ? event.target.filter : this;

		// associate arrays with transformation strings
		let tMap = new Map([
			[Constants.transformations.visible, []],
			[Constants.transformations.invisible, []],
			[Constants.transformations.opaque, []],
			[Constants.transformations.transparent, []],
			[Constants.transformations.selected, []],
			[Constants.transformations.connected, []]
		]);

		// reset transformations
		TransformationHelper.resetTransformations(filter);

		// handle one container after the other
		for (let container of filter.containers) {
			// skip deactivated containers
			if (!container.activated) continue;

			// calculate suggestions
			filter.calculateSuggestions(filter, container);

			// get selected transformation as key
			let tKey = container.transformations[container.transformation];

			// get isolated container selection
			let selection = filter.handleContainer(filter, container);

			// make relations visible
			if (container.relations) TransformationHelper.makeEntitiesVisible(selection);

			// selections of containers add up
			let union = tMap.get(tKey).concat(selection);

			// remove duplicates of selection
			union = union.filter((entity, index) => {
				return union.indexOf(entity) === index;
			});

			tMap.set(tKey, union);

			// apply transformations
			if (container.layers.every(layer => layer.faulty == false)) {
				// console.table(Array.from(tMap).map(entry => [entry[0], entry[1].length]));

				if (tKey == Constants.transformations.visible)
					TransformationHelper.makeEntitiesVisible(tMap.get(Constants.transformations.visible));
				else if (tKey == Constants.transformations.invisible)
					TransformationHelper.makeEntitiesInvisible(tMap.get(Constants.transformations.invisible));
				else if (tKey == Constants.transformations.transparent) {
					TransformationHelper.makeEntitiesTransparent(
						tMap.get(Constants.transformations.transparent)
					);
				} else if (tKey == Constants.transformations.opaque)
					TransformationHelper.makeEntitiesOpaque(tMap.get(Constants.transformations.opaque));
				else if (tKey == Constants.transformations.selected)
					TransformationHelper.selectEntities(tMap.get(Constants.transformations.selected));
				else if (tKey == Constants.transformations.connected) {
					TransformationHelper.connectEntities(
						tMap.get(Constants.transformations.connected),
						filter.loadedPositions
					);
				}
			} else {
				// reset transformations
				TransformationHelper.resetTransformations(filter);

				console.info(Constants.strings.invalidFilter);
			}
		}

		// update filter UI
		filter.redraw();
	}

	// apply() helpers

	calculateSuggestions(filter, container) {
		container.availableSuggestions = [];

		// get all entities
		let entities = filter.visualizedEntities;

		// only use visible entities for suggestions, except the first container
		if (filter.containers.indexOf(container) != 0) {
			entities = entities.filter(entity => !entity.filtered);
		}

		entities.forEach(entity => {
			// extract easily readable text (FQN)
			let suggestion = entity.qualifiedName;

			// extract quickly searchable text (f.q.n.method(args) -> fqnmethodargs)
			let searchable = suggestion.toLowerCase().replace(/[^a-z-]/g, '');

			// fill suggestions array of filter
			container.availableSuggestions.push({ suggestion: suggestion, searchable: searchable });
		});
	}

	// appends selected entityIds of a container to the respective set
	handleContainer(filter, container) {
		let selection = [];
		for (let layer of container.layers) {
			if (!layer.activated) continue;

			if (layer.query == null || layer.query == '') {
				// first container treats all entities as visible, although they are invisible by default
				if (filter.containers.indexOf(container) == 0) {
					selection = filter.visualizedEntities;
				} else {
					selection = filter.visualizedEntities.filter(entity => !entity.filtered);
				}

				layer.entity = null;
				layer.faulty = false;
			} else {
				// get entity with (query == entity.qualifiedName) test
				let entity = FilterHelper.getEntityFromQN(layer.query);
				layer.entity = entity ? entity : null;

				// set error indicator for current layer
				layer.faulty = layer.entity == null;

				if (layer.entity) {
					// add parent
					selection.push(layer.entity);

					// add all children and children of children
					if (layer.includeChilds) {
						let allChildren = [];
						FilterHelper.getAllChildren(layer.entity, allChildren);
						selection = selection.concat(allChildren);
					}
				}
			}
		}

		// subtract selected entities from all entities
		if (container.relations) {
			let relations = [];
			let fields = ['subTypes', 'superTypes', 'accessedBy', 'accesses', 'calls', 'calledBy'];

			for (let entity of selection) {
				for (let field of fields) {
					if (entity[field]) {
						relations = relations.concat(entity[field]);
						// console.log(field, entity[field], entity);
					}
				}
			}

			// remove duplicates
			relations = relations.filter((entity, index) => {
				return relations.indexOf(entity) === index;
			});

			selection = selection.concat(relations);
		}

		// subtract selected entities from all entities
		if (container.inverted)
			selection = filter.visualizedEntities.filter(ent => selection.indexOf(ent) == -1);

		// console.log(container.id, 'selection final', selection);

		return selection.map(entity => entity.id);
	}

	// handlContainer() helpers

	// sets suggestion logic to the searchField
	setSuggestionsEngine(filter, currentContainer, currentLayer) {
		// let rootDiv = document.getElementById("filterContainer");
		let input = $('#searchField-' + currentLayer.id);
		let dataList = $('#suggestionList-' + currentLayer.id);

		let suggestionDebounceTime = 250;
		let applyDebounceTime = 100;
		let suggestionLimit = 15;

		// event when suggestion is selected
		input.on(
			'input',
			FilterHelper.debounce(event => {
				// console.log('input.on(input)', event.originalEvent.inputType);

				let currentValue = input.val();
				let options = dataList[0].childNodes;

				// check if currentValue equals suggestion to determine if suggestion was selected
				for (let i = 0; i < options.length; i++) {
					if (options[i].value === currentValue) {
						// save input value
						currentLayer.entity = null;
						currentLayer.query = currentValue;

						// only apply when suggestion was selected
						if (!event.originalEvent.inputType) {
							event.target.filter = filter;
							filter.apply(event);
						}

						break;
					}
				}
			}, applyDebounceTime)
		);

		// event when query is changed
		input.on(
			'change paste keyup',
			FilterHelper.debounce(event => {
				// console.log('input.on(change, paste, keyup)');

				// react to enter-keypress
				if (event.keyCode === 13) {
					event.target.filter = filter;
					filter.apply(event);
				}

				if (event.keyCode !== 13) {
					let currentValue = input.val();

					// save input value
					currentLayer.query = currentValue;

					// empty datalist
					dataList.empty();

					// fill datalist with suggestions
					if (currentValue.length > 0) {
						let matchingSuggestions = [];

						// create regex: bank.getProduct() -> bankgetproduct
						let regex = new RegExp(currentValue.toLowerCase().replace(/[^a-z-]/g, ''));

						// find matching suggestions
						currentContainer.availableSuggestions.forEach(s => {
							if (regex.test(s.searchable)) matchingSuggestions.push(s);
						});

						// sort suggestions: ascending string length
						matchingSuggestions.sort((a, b) => a.searchable.length - b.searchable.length);

						// add suggestions to datalist
						let options = '';
						for (let i = 0; i < matchingSuggestions.length; i++) {
							if (i > suggestionLimit) break;
							options += '<option value="' + matchingSuggestions[i].suggestion + '">';
						}
						dataList.append(options);
					}
				}
			}, suggestionDebounceTime)
		);
	}
}
