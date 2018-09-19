class DOMHelper {
	constructor(filter) {
		this.filter = filter;
	}

	initRootContainer() {
		let containerDiv = document.getElementById('containerDiv');
		containerDiv.innerHTML = '';

		return containerDiv;
	}

	buildEmptyFilterNote() {
		var note = document.createElement('div');
		note.classList.add('note');
		note.innerText = Constants.strings.note;

		return note;
	}

	buildPlugInUI(rootDiv, devMode) {
		// identify root
		rootDiv.id = 'filterContainer';

		// layer container
		var containerDiv = document.createElement('div');
		containerDiv.id = 'containerDiv';

		// reset span
		var resetText = document.createElement('span');
		resetText.innerText = devMode ? Constants.strings.devReset : Constants.strings.reset;
		resetText.filter = this.filter;
		resetText.addEventListener('click', this.filter.reset, false);

		// zoom span
		var zoomText = document.createElement('span');
		zoomText.innerText = devMode ? Constants.strings.devZoom : Constants.strings.zoom;
		zoomText.filter = this.filter;
		zoomText.addEventListener('click', this.filter.zoom, false);

		// load span
		var loadText = document.createElement('span');
		loadText.innerText = Constants.strings.load;
		loadText.filter = this.filter;
		loadText.addEventListener('click', this.filter.loadConfigurationFile, false);

		// save span
		var saveText = document.createElement('span');
		saveText.innerText = Constants.strings.save;
		saveText.filter = this.filter;
		saveText.addEventListener('click', this.filter.saveConfigurationFile, false);

		// log span
		var logText = document.createElement('span');
		logText.innerText = Constants.strings.log;
		logText.filter = this.filter;
		logText.addEventListener('click', this.filter.log, false);

		// apply span
		var applyText = document.createElement('span');
		applyText.innerText = Constants.strings.apply;
		applyText.filter = this.filter;
		applyText.addEventListener('click', this.filter.apply, false);

		// toolbar
		var toolbarDiv = document.createElement('div');
		toolbarDiv.id = 'toolbarDiv';
		toolbarDiv.className = 'toolbar';
		toolbarDiv.appendChild(resetText);
		toolbarDiv.appendChild(zoomText);
		if (devMode) toolbarDiv.appendChild(loadText);
		if (devMode) toolbarDiv.appendChild(saveText);
		if (devMode) toolbarDiv.appendChild(applyText);
		if (devMode) toolbarDiv.appendChild(logText);

		// div to add a container
		var plus = document.createElement('img');
		plus.src = './scripts/Filter/Assets/addcontainer.png';
		plus.style.pointerEvents = 'none';
		var text = document.createElement('span');
		text.innerText = Constants.strings.addContainer;
		text.style.pointerEvents = 'none';
		var addDiv = document.createElement('div');
		addDiv.id = 'addDiv';
		addDiv.className = 'bottomAction';
		addDiv.filter = this.filter;
		addDiv.appendChild(plus);
		addDiv.appendChild(text);
		addDiv.addEventListener('click', this.filter.addContainer, false);

		// input to load config files
		var loadFileInput = document.createElement('input');
		loadFileInput.setAttribute('type', 'file');
		loadFileInput.id = 'loadFileInput';
		loadFileInput.style.display = 'none';
		loadFileInput.addEventListener('change', event => {
			var reader = new FileReader();
			reader.onload = event => this.filter.buildConfig(JSON.parse(event.target.result));
			reader.readAsText(event.target.files[0]);
		});

		// build UI
		rootDiv.appendChild(toolbarDiv);
		rootDiv.appendChild(containerDiv);
		rootDiv.appendChild(addDiv);
		rootDiv.appendChild(loadFileInput);
	}

	buildContainerHeader(currentContainer) {
		// container type select
		var transformation = document.createElement('div');
		transformation.className = 'transformationSelect';
		transformation.id = 'transformationSelect-' + currentContainer.id;

		// remove container button
		var removeImage = document.createElement('img');
		removeImage.src = './scripts/Filter/Assets/close.png';
		removeImage.className = 'removeImage';
		removeImage.style.pointerEvents = 'none';
		var removeContainerButton = document.createElement('button');
		removeContainerButton.className = 'removeContainerButton';
		removeContainerButton.appendChild(removeImage);
		removeContainerButton.container = currentContainer; // pass container as parameter of button
		removeContainerButton.filter = this.filter; // pass filter as parameter of button
		removeContainerButton.addEventListener('click', this.filter.removeContainer, false);

		// deactivate button
		var deactivateImage = document.createElement('img');
		deactivateImage.src = './scripts/Filter/Assets/deactivate.png';
		deactivateImage.className = 'deactivateImage';
		deactivateImage.style.pointerEvents = 'none';
		var deactivateButton = document.createElement('button');
		deactivateButton.className = 'deactivateButton';
		deactivateButton.appendChild(deactivateImage);
		deactivateButton.container = currentContainer; // pass container as parameter of button
		deactivateButton.filter = this.filter; // pass filter as parameter of button
		deactivateButton.addEventListener('click', this.filter.deactivateContainer, false);

		// checkbox to invert selection
		var relationsCheckbox = document.createElement('div');
		relationsCheckbox.className = 'relationsCheckbox';
		relationsCheckbox.id = 'relationsCheckbox-' + currentContainer.id;
		relationsCheckbox.innerText = Constants.strings.relations;

		// checkbox to invert selection
		var invertCheckbox = document.createElement('div');
		invertCheckbox.className = 'invertCheckbox';
		invertCheckbox.id = 'invertCheckbox-' + currentContainer.id;
		invertCheckbox.innerText = Constants.strings.invert;

		// checkbox grouper
		var checkboxGroup = document.createElement('div');
		checkboxGroup.className = 'checkboxGroup';
		checkboxGroup.id = 'checkboxGroup-' + currentContainer.id;
		checkboxGroup.appendChild(relationsCheckbox);
		checkboxGroup.appendChild(invertCheckbox);

		// add layer button
		var addButton = document.createElement('button');
		addButton.type = 'button';
		addButton.className = 'addButton';
		addButton.container = currentContainer; // pass container as parameter of button
		addButton.filter = this.filter; // pass filter as parameter of button
		addButton.addEventListener('click', this.filter.addLayer, false);

		// move up button
		var moveUpImage = document.createElement('img');
		moveUpImage.src = './scripts/Filter/Assets/containerup.png';
		moveUpImage.className = 'moveUpImage';
		moveUpImage.style.pointerEvents = 'none';
		var moveUpButton = document.createElement('button');
		moveUpButton.className = 'moveButton';
		moveUpButton.appendChild(moveUpImage);
		moveUpButton.container = currentContainer; // pass container as parameter of button
		moveUpButton.filter = this.filter; // pass filter as parameter of button
		moveUpButton.isDirectionUp = true; // pass direction as parameter of button
		moveUpButton.addEventListener('click', this.filter.moveContainer, false);

		// move down button
		var moveDownImage = document.createElement('img');
		moveDownImage.src = './scripts/Filter/Assets/containerdown.png';
		moveDownImage.className = 'moveDownImage';
		moveDownImage.style.pointerEvents = 'none';
		var moveDownButton = document.createElement('div');
		moveDownButton.className = 'moveButton';
		moveDownButton.appendChild(moveDownImage);
		moveDownButton.container = currentContainer; // pass container as parameter of button
		moveDownButton.filter = this.filter; // pass filter as parameter of button
		moveDownButton.isDirectionUp = false; // pass direction as parameter of button
		moveDownButton.addEventListener('click', this.filter.moveContainer, false);

		// build container header
		var header = document.createElement('div');
		header.className = 'containerHeader';
		var stackedButtons1 = document.createElement('div');
		var stackedButtons2 = document.createElement('div');
		var stackedButtons3 = document.createElement('div');
		stackedButtons1.className = 'stackedElements';
		stackedButtons2.className = 'stackedElements';
		stackedButtons3.className = 'stackedElements';
		stackedButtons1.appendChild(removeContainerButton);
		stackedButtons1.appendChild(deactivateButton);
		stackedButtons2.appendChild(transformation);
		stackedButtons2.appendChild(checkboxGroup);
		stackedButtons3.appendChild(moveUpButton);
		stackedButtons3.appendChild(moveDownButton);
		header.appendChild(stackedButtons3);
		header.appendChild(stackedButtons2);
		header.appendChild(stackedButtons1);

		return header;
	}

	buildLayerContainer(currentContainer) {
		// create layerContainer
		var layerContainer = document.createElement('div');
		layerContainer.className = 'layerContainer';
		layerContainer.hidden = !currentContainer.expanded || !currentContainer.activated;

		// create layers
		for (var j = 0; j < currentContainer.layers.length; j++) {
			var currentLayer = currentContainer.layers[j];

			// layer
			var layer = document.createElement('div');
			layer.classList.add('layer');
			if (currentLayer.faulty) layer.classList.add('faulty');

			// search div
			var searchDiv = document.createElement('div');
			searchDiv.className = 'searchDiv';

			// search input
			var searchField = document.createElement('input');
			searchField.setAttribute('list', 'suggestionList-' + currentLayer.id);
			searchField.autocomplete = 'off';
			searchField.type = 'text';
			searchField.id = 'searchField-' + currentLayer.id;
			searchField.className = 'searchField';
			searchField.value = currentLayer.query;

			// suggestionslist
			var suggestionList = document.createElement('datalist');
			suggestionList.id = 'suggestionList-' + currentLayer.id;
			suggestionList.className = 'suggestionList';

			// checkbox to include all childs
			var includeChildsCheckbox = document.createElement('div');
			includeChildsCheckbox.className = 'includeChildsCheckbox';
			includeChildsCheckbox.id = 'includeChildsCheckbox-' + currentLayer.id;
			includeChildsCheckbox.innerText = Constants.strings.includeChilds;

			// delete button
			var removeLayerImage = document.createElement('img');
			removeLayerImage.src = './scripts/Filter/Assets/close.png';
			removeLayerImage.className = 'removeLayerImage';
			removeLayerImage.style.pointerEvents = 'none';
			var removeLayerButton = document.createElement('button');
			removeLayerButton.className = 'removeLayerButton';
			removeLayerButton.appendChild(removeLayerImage);
			removeLayerButton.container = currentContainer; // pass container as parameter of button
			removeLayerButton.layer = currentLayer; // pass layer as parameter of button
			removeLayerButton.filter = this.filter; // pass filter as parameter of button
			removeLayerButton.addEventListener('click', this.filter.removeLayer, false);

			// deactivate button
			var deactivateLayerImage = document.createElement('img');
			deactivateLayerImage.src = './scripts/Filter/Assets/deactivate.png';
			deactivateLayerImage.className = 'deactivateImage';
			deactivateLayerImage.style.pointerEvents = 'none';
			var deactivateLayerButton = document.createElement('button');
			deactivateLayerButton.className = 'deactivateLayerButton';
			deactivateLayerButton.appendChild(deactivateLayerImage);
			deactivateLayerButton.container = currentContainer; // pass container as parameter of button
			deactivateLayerButton.layer = currentLayer; // pass layer as parameter of button
			deactivateLayerButton.filter = this.filter; // pass filter as parameter of button
			deactivateLayerButton.addEventListener('click', this.filter.deactivateLayer, false);

			// build layerContainer
			var stackedButtons2 = document.createElement('div');
			var stackedButtons3 = document.createElement('div');
			var checkboxes = document.createElement('div');
			stackedButtons2.className = 'stackedElements';
			stackedButtons3.className = 'stackedElements';
			checkboxes.className = 'checkboxes';
			checkboxes.appendChild(includeChildsCheckbox);

			searchDiv.appendChild(searchField);
			searchDiv.appendChild(suggestionList);

			stackedButtons2.appendChild(searchDiv);
			stackedButtons2.appendChild(checkboxes);
			stackedButtons3.appendChild(removeLayerButton);
			stackedButtons3.appendChild(deactivateLayerButton);
			layer.appendChild(stackedButtons2);
			layer.appendChild(stackedButtons3);
			layerContainer.appendChild(layer);
		}

		// create div to add layer
		var addLayer = document.createElement('div');
		if (currentContainer.layers.length == 0 && this.filter.containers.length > 1)
			addLayer.classList.add('actionLayer', 'faulty');
		else addLayer.classList.add('actionLayer');
		addLayer.container = currentContainer; // pass container as parameter of button
		addLayer.filter = this.filter; // pass filter as parameter of button
		addLayer.addEventListener('click', this.filter.addLayer, false);
		var addLayerIcon = document.createElement('img');
		addLayerIcon.src = './scripts/Filter/Assets/addlayer.png';
		addLayerIcon.style.pointerEvents = 'none';
		var addLayerText = document.createElement('span');
		addLayerText.innerText = Constants.strings.addLayer;
		addLayerText.style.pointerEvents = 'none';
		addLayer.appendChild(addLayerIcon);
		addLayer.appendChild(addLayerText);
		layerContainer.appendChild(addLayer);

		return layerContainer;
	}

	buildContainer(currentContainer) {
		var container = document.createElement('div');
		container.classList.add('container');
		for (let layer of currentContainer.layers) {
			if (layer.faulty) {
				container.classList.add('faulty');
				break;
			}
		}

		return container;
	}

	setContainerHeaderProperties(containers) {
		if (containers.length > 0) {
			let transformations = Object.keys(Constants.transformations).map(
				val => Constants.transformations[val]
			);
			$('.transformationSelect').jqxDropDownList({
				theme: 'metro',
				source: transformations,
				height: '15px'
			});
			$('.moveButton').jqxButton({ theme: 'metro', height: '15px', width: '15px' });
			$('.deactivateButton').jqxButton({ theme: 'metro', height: '15px', width: '15px' });
			$('.removeContainerButton').jqxButton({ theme: 'metro', height: '15px', width: '15px' });

			// style layer elements
			let c = 0;
			containers.forEach(container => (c += container.layers.length));
			if (c > 0) {
				$('.removeLayerButton').jqxButton({ theme: 'metro', height: '15px', width: '15px' });
				$('.searchField').jqxInput({ theme: 'metro', height: '15px', placeHolder: '' });
				$('.deactivateLayerButton').jqxButton({ theme: 'metro', height: '15px', width: '15px' });
			}
		}
	}

	setContainerProperties(c) {
		var filterAlias = this.filter;

		// disable elements
		$('#transformationSelect-' + c.id).jqxDropDownList({ disabled: !c.activated });
		$('#invertCheckbox-' + c.id).jqxCheckBox({ disabled: !c.activated });
		$('#relationsCheckbox-' + c.id).jqxCheckBox({ disabled: !c.activated });

		// set select and onSelect function
		$('#transformationSelect-' + c.id).jqxDropDownList({ selectedIndex: c.transformation });
		$('#transformationSelect-' + c.id).on('select', event => {
			var item = $('#transformationSelect-' + c.id).jqxDropDownList('getItem', event.args.index);
			c.transformation = item.index;
			event.target.filter = filterAlias;
			filterAlias.apply(event);
		});

		// set invert checkbox and onChange function
		$('#invertCheckbox-' + c.id).jqxCheckBox({
			theme: 'metro',
			height: '15px',
			checked: c.inverted
		});
		$('#invertCheckbox-' + c.id).on('change', event => {
			// console.log("invertCB " + c.id + " is now: " + (event.args.checked ? "inverted" : "not inverted"));
			c.inverted = event.args.checked;
			event.target.filter = filterAlias;
			filterAlias.apply(event);
		});

		// set relations checkbox and onChange function
		$('#relationsCheckbox-' + c.id).jqxCheckBox({
			theme: 'metro',
			height: '15px',
			checked: c.relations
		});
		$('#relationsCheckbox-' + c.id).on('change', event => {
			// console.log("relationsCB " + c.id + " is now: " + (event.args.checked ? "active" : "not active"));
			c.relations = event.args.checked;
			event.target.filter = filterAlias;
			filterAlias.apply(event);
		});

		for (let l of c.layers) {
			// disable elements
			$('#includeChildsCheckbox-' + l.id).jqxCheckBox({ disabled: !l.activated });
			$('#searchField-' + l.id).jqxInput({ disabled: !l.activated });

			// set includeChilds checkbox and onChange function
			$('#includeChildsCheckbox-' + l.id).jqxCheckBox({
				theme: 'metro',
				height: '15px',
				checked: l.includeChilds
			});
			$('#includeChildsCheckbox-' + l.id).on('change', event => {
				l.includeChilds = event.args.checked;
				event.target.filter = filterAlias;
				filterAlias.apply(event);
			});
		}
	}
}
