/**
 * Example component for A-Frame.
 */
AFRAME.registerComponent('orbit-camera', {
  dependencies: ['camera', 'position', 'rotation'],
  schema: {

    enabled: { type: 'boolean', default: true },
    target: { type: 'vec3', default: { x: 0, y: 0, z: 0 } },

    enableRotate: { type: 'boolean', default: true },
    rotateSpeed: { type: 'number', default: 1.0 },

    enableZoom: { type: 'boolean', default: true },
    zoomSpeed: { type: 'number', default: 1.0 },

    enablePan: { type: 'boolean', default: true },
    panSpeed: { type: 'number', default: 1.0 },

    enableDamping: { type: 'boolean', default: false },
    dampingFactor: { type: 'number', default: 0.25 },

    minAzimuthAngle: { type: 'number', default: -Infinity },
    maxAzimuthAngle: { type: 'number', default: Infinity },

    minPolarAngle: { type: 'number', default: 0 },
    maxPolarAngle: { type: 'number', default: Math.PI },

    minZoom: { type: 'number', default: 0 },
    maxZoom: { type: 'number', default: Infinity },

    invertZoom: { type: 'boolean', default: false },

    minDistance: { type: 'number', default: 0 },
    maxDistance: { type: 'number', default: Infinity },

    logPosition: { type: 'boolean', default: false },
  },

  /**
   * Initial Setup
   */
  init: function () {
    globalCamera = this;
    this.sceneEl = this.el.sceneEl;
    this.object = this.el.object3D;
    this.canvasEl = this.sceneEl.canvas;
    var targetVec = this.data.target;
    this.target = new THREE.Vector3(targetVec.x, targetVec.y, targetVec.z);

    console.log('enabled: ', this.data.enabled);

    this.dolly = new THREE.Object3D();
    this.dolly.position.copy(this.object.position);

    this.savedPose = null;

    this.STATE = {
      NONE: -1,
      ROTATE: 0,
      DOLLY: 1,
      PAN: 2,
    };

    this.state = this.STATE.NONE;

    this.EPS = 0.000001;
    this.lastPosition = new THREE.Vector3();
    this.lastQuaternion = new THREE.Quaternion();

    this.spherical = new THREE.Spherical();
    this.sphericalDelta = new THREE.Spherical();

    this.scale = 1.0;
    this.zoomChanged = false;

    this.rotateStart = new THREE.Vector2();
    this.rotateEnd = new THREE.Vector2();
    this.rotateDelta = new THREE.Vector2();

    this.panStart = new THREE.Vector2();
    this.panEnd = new THREE.Vector2();
    this.panDelta = new THREE.Vector2();
    this.panOffset = new THREE.Vector3();

    this.dollyStart = new THREE.Vector2();
    this.dollyEnd = new THREE.Vector2();
    this.dollyDelta = new THREE.Vector2();

    this.vector = new THREE.Vector3();
    this.desiredPosition = new THREE.Vector3();

    this.mouseButtons = {
      LEFT: THREE.MOUSE.LEFT,
      MIDDLE: THREE.MOUSE.MIDDLE,
      RIGHT: THREE.MOUSE.RIGHT
    };

    this.bindMethods();
  },

  /**
   * Called when component is attached and when component data changes.
   * updates the entity based on the data.
   */
  update: function (oldData) {
    console.log('component update');
    this.updateView();
    this.addEventListeners();
  },

  /**
   * Called when a component is removed (e.g., via removeAttribute).
   * Generally undoes all modifications to the entity.
   */
  remove: function () {
    this.removeEventListeners();
  },

  /**
   * Called on each scene tick.
   */
  tick: function (t) {
    var render = this.data.enabled ? this.updateView() : false;
    if (render === true && this.data.logPosition === true) {
      console.log(this.el.object3D.position);
    }
  },

  /*
   * Bind this to all event handlera
   */
  bindMethods: function () {
    this.onContextMenu = this.onContextMenu.bind(this);
    this.onMouseDown = this.onMouseDown.bind(this);
    this.onMouseWheel = this.onMouseWheel.bind(this);
    this.onMouseMove = this.onMouseMove.bind(this);
    this.onMouseUp = this.onMouseUp.bind(this);
  },

  /*
   * Add event listeners
   */
  addEventListeners: function () {
    this.canvasEl.addEventListener('contextmenu', this.onContextMenu, false);
    this.canvasEl.addEventListener('mousedown', this.onMouseDown, false);
    this.canvasEl.addEventListener('mousewheel', this.onMouseWheel, false);
    this.canvasEl.addEventListener('MozMousePixelScroll', this.onMouseWheel, false);  // firefox
  },

  /*
   * Remove event listeners
   */
  removeEventListeners: function () {
    if (this.canvasEl) {
      this.canvasEl.removeEventListener('contextmenu', this.onContextMenu, false);
      this.canvasEl.removeEventListener('mousedown', this.onMouseDown, false);
      this.canvasEl.removeEventListener('mousewheel', this.onMouseWheel, false);
      this.canvasEl.removeEventListener('MozMousePixelScroll', this.onMouseWheel, false);  // firefox

      this.canvasEl.removeEventListener('mousemove', this.onMouseMove, false);
      this.canvasEl.removeEventListener('mouseup', this.onMouseUp, false);
      this.canvasEl.removeEventListener('mouseout', this.onMouseUp, false);
    }
  },

  /*
   * EVENT LISTENERS
   */

  /*
   * Called when right clicking the A-Frame component
   */
  onContextMenu: function (event) {
    event.preventDefault();
  },

  /*
   * MOUSE CLICK EVENT LISTENERS
   */

  onMouseDown: function (event) {

    // console.log('onMouseDown');
    if (!this.data.enabled) return;

    event.preventDefault();

    switch (event.button) {
      case this.mouseButtons.LEFT:
        if (event.ctrlKey || event.shiftKey) {
          if (this.data.enablePan === false) return;
          this.handleMouseDownPan(event);
          this.state = this.STATE.PAN;
        } else {
          this.panOffset.set(0, 0, 0);
          if (this.data.enableRotate === false) return;
          this.handleMouseDownRotate(event);
          this.state = this.STATE.ROTATE;
        }
        break;
      case this.mouseButtons.RIGHT:
        this.panOffset.set(0, 0, 0);
        if (this.data.enableZoom === false) return;
        this.handleMouseDownDolly(event);
        this.state = this.STATE.DOLLY;
        break;
      case this.mouseButtons.MIDDLE:
        if (this.data.enablePan === false) return;
        this.handleMouseDownPan(event);
        this.state = this.STATE.PAN;
        break;
    }

    if (this.state !== this.STATE.NONE) {
      this.canvasEl.addEventListener('mousemove', this.onMouseMove, false);
      this.canvasEl.addEventListener('mouseup', this.onMouseUp, false);
      this.canvasEl.addEventListener('mouseout', this.onMouseUp, false);

    }
  },

  onMouseMove: function (event) {

    // console.log('onMouseMove');
    if (!this.data.enabled) return;

    event.preventDefault();

    switch (this.state) {
      case this.STATE.ROTATE:
        if (this.data.enableRotate === false) return;
        this.handleMouseMoveRotate(event);
        break;
      case this.STATE.DOLLY:
        if (this.data.enableZoom === false) return;
        this.handleMouseMoveDolly(event);
        break;
      case this.STATE.PAN:
        if (this.data.enablePan === false) return;
        this.handleMouseMovePan(event);
        break;
    }

  },

  onMouseUp: function (event) {

    // console.log('onMouseUp');
    if (!this.data.enabled) return;

    event.preventDefault();
    event.stopPropagation();

    this.handleMouseUp(event);

    this.canvasEl.removeEventListener('mousemove', this.onMouseMove, false);
    this.canvasEl.removeEventListener('mouseup', this.onMouseUp, false);
    this.canvasEl.removeEventListener('mouseout', this.onMouseUp, false);

    this.state = this.STATE.NONE;

  },

  /*
   * MOUSE WHEEL EVENT LISTENERS
   */

  onMouseWheel: function (event) {

    // console.log('onMouseWheel');
    if (!this.data.enabled ||
      this.data.enableZoom === false ||
      (this.state !== this.STATE.NONE && this.state !== this.STATE.ROTATE))
      return;

    event.preventDefault();
    event.stopPropagation();
    this.handleMouseWheel(event);
  },


  /*
   * EVENT HANDLERS
   */

  handleMouseDownRotate: function (event) {
    // console.log( 'handleMouseDownRotate' );
    this.rotateStart.set(event.clientX, event.clientY);
  },

  handleMouseDownDolly: function (event) {
    // console.log( 'handleMouseDownDolly' );
    this.dollyStart.set(event.clientX, event.clientY);
  },

  handleMouseDownPan: function (event) {
    // console.log( 'handleMouseDownPan' );
    this.panStart.set(event.clientX, event.clientY);
  },

  handleMouseMoveRotate: function (event) {

    // console.log( 'handleMouseMoveRotate' );
    this.rotateEnd.set(event.clientX, event.clientY);
    this.rotateDelta.subVectors(this.rotateEnd, this.rotateStart);

    var canvas =
      this.canvasEl === document ? this.canvasEl.body : this.canvasEl;

    // rotating across whole screen goes 360 degrees around
    this.rotateLeft(
      2 * Math.PI * this.rotateDelta.x / canvas.clientWidth *
      this.data.rotateSpeed);

    // rotating up and down along whole screen attempts to go 360, but limited
    // to 180
    this.rotateUp(
      2 * Math.PI * this.rotateDelta.y / canvas.clientHeight *
      this.data.rotateSpeed);

    this.rotateStart.copy(this.rotateEnd);

    this.updateView();
  },

  handleMouseMoveDolly: function (event) {

    // console.log( 'handleMouseMoveDolly' );
    this.dollyEnd.set(event.clientX, event.clientY);
    this.dollyDelta.subVectors(this.dollyEnd, this.dollyStart);

    if (this.dollyDelta.y > 0) {
      !this.data.invertZoom ? this.dollyIn(this.getZoomScale()) :
        this.dollyOut(this.getZoomScale());
    } else if (this.dollyDelta.y < 0) {
      !this.data.invertZoom ? this.dollyOut(this.getZoomScale()) :
        this.dollyIn(this.getZoomScale());
    }

    this.dollyStart.copy(this.dollyEnd);

    this.updateView();
  },

  handleMouseMovePan: function (event) {

    // console.log( 'handleMouseMovePan' );
    this.panEnd.set(event.clientX, event.clientY);
    this.panDelta.subVectors(this.panEnd, this.panStart).multiplyScalar(this.data.panSpeed);
    this.pan(this.panDelta.x, this.panDelta.y);
    this.panStart.copy(this.panEnd);

    this.updateView();
  },

  handleMouseUp: function (event) {
    // console.log( 'handleMouseUp' );
  },

  handleMouseWheel: function (event) {

    // console.log( 'handleMouseWheel' );
    var delta = 0;
    if (event.wheelDelta !== undefined) {
      // WebKit / Opera / Explorer 9
      delta = event.wheelDelta;
    } else if (event.detail !== undefined) {
      // Firefox
      delta = -event.detail;
    }

    if (delta > 0) {
      !this.data.invertZoom ? this.dollyOut(this.getZoomScale()) :
        this.dollyIn(this.getZoomScale());
    } else if (delta < 0) {
      !this.data.invertZoom ? this.dollyIn(this.getZoomScale()) :
        this.dollyOut(this.getZoomScale());
    }

    this.updateView();
  },

  /*
   * Internal functions
   */

  getZoomScale: function () {
    return Math.pow(0.95, this.data.zoomSpeed);
  },

  rotateLeft: function (angle) {
    this.sphericalDelta.theta -= angle;
  },

  rotateUp: function (angle) {
    this.sphericalDelta.phi -= angle;
  },

  panLeft: function (distance, objectMatrix) {
    var v = new THREE.Vector3();
    v.setFromMatrixColumn(objectMatrix, 0);  // get X column of objectMatrix
    v.multiplyScalar(-distance);
    this.panOffset.add(v);
  },

  panUp: function (distance, objectMatrix) {
    var v = new THREE.Vector3();
    v.setFromMatrixColumn(objectMatrix, 1);  // get Y column of objectMatrix
    v.multiplyScalar(distance);
    this.panOffset.add(v);
  },

  pan: function (deltaX, deltaY) {  // deltaX and deltaY are in pixels; right and down are positive

    var offset = new THREE.Vector3();
    var canvas = this.canvasEl === document ? this.canvasEl.body : this.canvasEl;

    var position = this.object.position;
    offset.copy(position).sub(this.target);
    var targetDistance = offset.length();
    // half of the fov is center to top of screen
    targetDistance *= Math.tan((this.el.components.camera.data.fov / 2) * Math.PI / 180.0);
    // we actually don't use screenWidth, since
    this.panLeft(2 * deltaX * targetDistance / canvas.clientHeight, this.object.matrix);
    // perspective camera is fixed to screen height
    this.panUp(2 * deltaY * targetDistance / canvas.clientHeight, this.object.matrix);
  },

  dollyIn: function (dollyScale) {
    this.scale *= dollyScale;
  },

  dollyOut: function (dollyScale) {
    this.scale /= dollyScale;
  },

  lookAtTarget: function (object, target) {
    var v = new THREE.Vector3();
    v.subVectors(object.position, target).add(object.position);
    object.lookAt(v);
  },

  /*
   * VIEW UPDATE
   */
  updateView: function () {
    var offset = new THREE.Vector3();

    // so camera.up is the orbit axis
    var quat = new THREE.Quaternion().setFromUnitVectors(this.dolly.up, new THREE.Vector3(0, 1, 0));
    var quatInverse = quat.clone().inverse();

    offset.copy(this.dolly.position).sub(this.target);
    offset.applyQuaternion(quat);  // rotate offset to "y-axis-is-up" space
    this.spherical.setFromVector3(offset);  // angle from z-axis around y-axis

    this.spherical.theta += this.sphericalDelta.theta;
    this.spherical.phi += this.sphericalDelta.phi;

    // restrict theta to be inside desired limits
    this.spherical.theta = Math.max(
      this.data.minAzimuthAngle,
      Math.min(
        this.data.maxAzimuthAngle,
        this.spherical.theta));

    // restrict phi to be inside desired limits
    this.spherical.phi = Math.max(
      this.data.minPolarAngle,
      Math.min(
        this.data.maxPolarAngle,
        this.spherical.phi));

    this.spherical.makeSafe();


    this.spherical.radius *= this.scale;

    // restrict radius to be inside desired limits
    this.spherical.radius = Math.max(
      this.data.minDistance,
      Math.min(
        this.data.maxDistance,
        this.spherical.radius));

    this.target.add(this.panOffset);  // move target to panned location

    offset.setFromSpherical(this.spherical);

    // rotate offset back to "camera-up-vector-is-up" space
    offset.applyQuaternion(quatInverse);

    this.dolly.position.copy(this.target).add(offset);

    this.lookAtTarget(this.dolly, this.target);

    if (this.data.enableDamping === true) {
      this.sphericalDelta.theta *= (1 - this.data.dampingFactor);
      this.sphericalDelta.phi *= (1 - this.data.dampingFactor);
      this.panOffset.multiplyScalar(1 - this.data.dampingFactor);
    } else {
      this.sphericalDelta.set(0, 0, 0);
      this.panOffset.set(0, 0, 0);
    }

    this.scale = 1;

    // update condition is:
    // min(camera displacement, camera rotation in radians)^2 > EPS
    // using small-angle approximation cos(x/2) = 1 - x^2 / 8

    if (this.zoomChanged ||
      this.lastPosition.distanceToSquared(this.dolly.position) > this.EPS ||
      8 * (1 - this.lastQuaternion.dot(this.dolly.quaternion)) > this.EPS) {

      var hmdQuaternion = this.calculateHMDQuaternion();
      var hmdEuler = new THREE.Euler();
      hmdEuler.setFromQuaternion(hmdQuaternion, 'YXZ');

      this.el.setAttribute('position', {
        x: this.dolly.position.x,
        y: this.dolly.position.y,
        z: this.dolly.position.z
      });

      this.el.setAttribute('rotation', {
        x: THREE.Math.radToDeg(hmdEuler.x),
        y: THREE.Math.radToDeg(hmdEuler.y),
        z: THREE.Math.radToDeg(hmdEuler.z)
      });

      this.lastPosition.copy(this.dolly.position);
      this.lastQuaternion.copy(this.dolly.quaternion);

      this.zoomChanged = false;

      return true;
    }

    return false;
  },

  calculateHMDQuaternion: (function () {
    var hmdQuaternion = new THREE.Quaternion();
    return function () {
      hmdQuaternion.copy(this.dolly.quaternion);
      return hmdQuaternion;
    };
  })()

});
