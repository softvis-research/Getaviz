var relayoutController = (function () {
    function initialize() {

    }

    function activate(rootDiv) {
        const button = document.createElement('button');
        button.id = "relayout-button";
        button.innerText = "Re-layout visible entities";
        rootDiv.appendChild(button);
    }


    return {
        initialize: initialize,
        activate: activate,
    }
})();