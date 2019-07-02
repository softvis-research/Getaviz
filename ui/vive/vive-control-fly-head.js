AFRAME.registerComponent('vive-control-fly-head', {
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
  },

  tick: function() {
    var cameraRig = document.querySelector('#cameraRig');
    var triggerDown = this.triggerDown;
    var gripDown = this.gripDown;
    var trackpadDown = this.trackpadDown;

    // get direction vr headset is pointing in
    var cameraObject = document.querySelector('a-camera').object3D;
    var cameraDirection = new THREE.Vector3().copy(cameraObject.getWorldDirection());

    // inverse direction and multiply by stepfactor
    cameraDirection.x = cameraDirection.x * -1 * this.stepFactor;
    cameraDirection.y = cameraDirection.y * -1 * this.stepFactor;
    cameraDirection.z = cameraDirection.z * -1 * this.stepFactor;

    if (triggerDown && trackpadDown) {
      cameraRig.object3D.position.sub(cameraDirection);
    }
    else if (triggerDown) {
      cameraRig.object3D.position.add(cameraDirection);
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
