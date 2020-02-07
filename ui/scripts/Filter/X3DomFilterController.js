var filterController = (function() {
	let filter = new Filter();
	let domHelper = new DOMHelper(filter);
	let devMode = false;
	let configuration;

	function initialize(setupConfig) {
		// add corresponding CSS file to filter.html
		let cssLink = document.createElement('link');
		cssLink.type = 'text/css';
		cssLink.rel = 'stylesheet';
		cssLink.href = 'scripts/Filter/filter.css';
		document.getElementsByTagName('head')[0].appendChild(cssLink);

		// setup filter configuration
		devMode = setupConfig.devMode != false;
		configuration = setupConfig.configuration
			? setupConfig.configuration
			: Constants.paths.defaultConfig;

		// load entity positions
		$.getJSON(multipartJsonUrl).done(data => RelationHelper.extractPositions(data, filter));
	}

	function activate(rootDiv) {
		// add the DOM elements to the main UI
		domHelper.buildPlugInUI(rootDiv, devMode);

		// load the configuration
		filter.loadConfiguration(configuration);
	}

	function reset() {
		filter.reset();
	}

	return {
		initialize: initialize,
		activate: activate,
		reset: reset
	};
})();
