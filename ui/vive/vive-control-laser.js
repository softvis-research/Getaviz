AFRAME.registerComponent('vive-control-laser', {
  dependencies: ['raycaster'],

  schema: {
    stepFactor: {type: 'number', default: 1}
  },

  init: function() {
    this.x = 0;
    this.y = 0;
    this.z = 0;
    this.stepFactor = this.data.stepFactor;
    this.intersecting = false;
    this.gripDown = false;
    this.trackpadDown = false;
    var self = this;

    this.el.addEventListener('raycaster-intersection', function(evt) {
      var intersection = evt.detail.intersections[0];
      this.x = intersection.point.x;
      this.y = intersection.point.y;
      this.z = intersection.point.z
      this.intersecting = true;
    });

    this.el.addEventListener('raycaster-intersection-cleared', function(evt) {
      this.intersecting = false;
    });

    this.el.addEventListener('triggerdown', function(evt) {
      if (this.intersecting) {
        var position = document.querySelector('#cameraRig').getAttribute('position');
        var newPosition = this.x*1000 + " " + (this.y*1000+2) + " " + this.z*1000;
        document.querySelector('#cameraRig').setAttribute('position', newPosition);
      } else {
        console.log('You have to point at something. :)');
      }
    });

    this.el.addEventListener('gripdown', function(evt) { gripDown(self); });
    this.el.addEventListener('gripup', function(evt) { gripUp(self); });
    this.el.addEventListener('trackpaddown', function(evt) { trackpadDown(self); });
    this.el.addEventListener('trackpadup', function(evt) { trackpadUp(self ); });

  },

  tick: function() {
    var cameraRig = document.querySelector('#cameraRig');
    var gripDown = this.gripDown;
    var trackpadDown = this.trackpadDown;

    if (this.gripDown) {
      cameraRig.object3D.position.y += 1 * this.stepFactor;
    } else if (this.trackpadDown) {
      cameraRig.object3D.position.y -= 1 * this.stepFactor;
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
