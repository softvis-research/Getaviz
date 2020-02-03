AFRAME.registerComponent('vive-control-menu', {
  schema: {
    controllerID: { default: 'menu' }
  },

  init: function() {
    var menuElement = document.createElement("a-entity");
    menuElement.id = 'menuElement';
    menuElement.innerHTML = '<a-entity id="menuElementText" text="value: fly-hand;" position="0.425 0.1 0"></a-entity>';
    this.el.appendChild(menuElement);
    var self = this;

    var controller = document.getElementById(this.data.controllerID);
    controller.addEventListener('trackpaddown', function(evt) {
      dispatchChangeEvent(self);
    });

    controller.addEventListener('triggerdown', function(evt) {
      changeControlText(self);
    })

    controller.addEventListener('menudown', (evt) => {
      viveLegendController.toggleVisibility();
    })

    controller.addEventListener('gripdown', (evt) => {
      viveSourcecodeController.scrollCode()
    })

  }
});

function dispatchChangeEvent(self) {
  var menuText = document.getElementById('menuElementText').getAttribute('text');
  var event = new CustomEvent('controlchange', {detail: menuText.value, bubbles: true});
  self.el.dispatchEvent(event);
};

function changeControlText(self) {
  var menuText = document.getElementById('menuElementText').getAttribute('text');
  if (menuText.value === 'fly-hand') {
    menuText.value = 'fly-head';
  } else if (menuText.value === 'fly-head') {
    menuText.value = 'laser';
  } else {
    menuText.value = 'fly-hand';
  }
  document.getElementById('menuElementText').setAttribute('text', menuText);
}
