AFRAME.registerComponent('vive-control-fly-hand', {
  dependencies: ['raycaster'],

  schema: {
    stepFactor: {type: 'number', default: 1}
  },

  init: function() {
    this.stepFactor = this.data.stepFactor;
    this.triggerDown = false;
    this.gripDown = false;
    this.trackpadDown = false;
    var self = this;

    this.el.addEventListener('triggerdown', function(evt) { triggerDown(self) });
    this.el.addEventListener('triggerup', function(evt){ triggerUp(self); });
    this.el.addEventListener('trackpaddown', function(evt) { trackpadDown(self); });
    this.el.addEventListener('trackpadup', function(evt) { trackpadUp(self); });
    this.el.addEventListener('gripup', function(evt) { gripUp(self); });
    this.el.addEventListener('gripdown', function(evt) { gripDown(self); });

    this.el.addEventListener('menudown', (evt) => {
      console.log(this)
    })
  },

  tick: function() {
    var cameraRig = document.querySelector('#cameraRig');
    var triggerDown = this.triggerDown;
    var gripDown = this.gripDown;
    var trackpadDown = this.trackpadDown;

    var rayDirection = new THREE.Vector3().copy(this.el.components.raycaster.raycaster.ray.direction);

    rayDirection.x = rayDirection.x * this.stepFactor;
    rayDirection.y = rayDirection.y * this.stepFactor;
    rayDirection.z = rayDirection.z * this.stepFactor;

    if (triggerDown && trackpadDown) {
      cameraRig.object3D.position.sub(rayDirection);
    }
    else if (triggerDown) {
      cameraRig.object3D.position.add(rayDirection);
    }
    else if (trackpadDown) {
      var rotation = document.querySelector('#cameraRig').object3D.rotation;
      rotation.y += THREE.Math.degToRad(1);
      document.querySelector('#cameraRig').object3D.rotation = rotation;
    }
    else if (gripDown) {
      var rotation = document.querySelector('#cameraRig').object3D.rotation;
      rotation.y -= THREE.Math.degToRad(1);
      document.querySelector('#cameraRig').object3D.rotation = rotation;
    }
  },
});

function triggerDown(self) {
  self.triggerDown = true;
};

function triggerUp(self) {
  self.triggerDown = false;
};

function gripDown(self) {
  self.gripDown = true;
};

function gripUp(self) {
  self.gripDown = false;
};

function trackpadDown(self) {
  self.trackpadDown = true;
};

function trackpadUp(self) {
  self.trackpadDown = false;
};

function menuDown(self) {
  self.menuDown = true
}

function menuUp(self) {
  self.menuDown = false
}