AFRAME.registerComponent("vive-control-rig", {
  dependencies: ["raycaster"],

  init: function() {
    this.el.innerHTML =
      this.el.innerHTML +
      '<a-entity id="control" laser-controls="hand: right" vive-control-fly-hand></a-entity><a-entity id="menu" vive-controls="hand: left"><a-entity vive-control-menu="controllerID: menu"></a-entity></a-entity>';
    var self = this;

    // the vive-control-menu component fires a controlchange event on triggerDown
    this.el.addEventListener("controlchange", function(evt) {
      var type = evt.detail;
      changeControl(self, type);
    });
    
    var cameraRig = document.querySelector('#cameraRig')
    cameraRig.addEventListener("componentchanged", ()=> {
      // TODO Muss noch weg
      console.log(`x: ${cameraRig.object3D.position.x} y: ${cameraRig.object3D.position.y} z: ${cameraRig.object3D.position.z}`)
    });
    document.querySelector("#cameraRig").setAttribute("position", "20 10 60");
    
  }
});

function changeControl(self, type) {
  // remove existing control node from DOM so that event handlers are deleted
  var controlNode = document.getElementById("control");
  self.el.removeChild(controlNode);

  // create new control node
  var newControlNode = document.createElement("a-entity");
  newControlNode.id = "control";

  // set node attributes
  var controlAttributeNode = document.createAttribute("vive-control-" + type);
  var laserAttributeNode = document.createAttribute("laser-controls");
  laserAttributeNode.value = "hand: right";
  newControlNode.setAttributeNode(controlAttributeNode);
  newControlNode.setAttributeNode(laserAttributeNode);

  // add new control node to DOM
  self.el.appendChild(newControlNode);
}
