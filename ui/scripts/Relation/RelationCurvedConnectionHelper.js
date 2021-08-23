var createCurvedRelationConnectionHelper = function(controllerConfig) {
    return (function(controllerConfig) {
		
		//only direction is needed to rotate the rings
        function setConnectorMeshProperties(connectorElement, direction) {
            connectorElement.addEventListener("loaded", function () {
                const threeMesh = this.object3DMap.mesh;

                const quaternion = threeMesh.quaternion;
                quaternion.setFromUnitVectors(new THREE.Vector3(1, 0, 0), direction);
            });
        }

        function setCommonConnectorHTMLProperties(connectorElement, rgbColorObj) {
            connectorElement.setAttribute("flat-shading", true);
            connectorElement.setAttribute("shader", "flat");
            const colorArr = [rgbColorObj.r, rgbColorObj.g, rgbColorObj.b];
            connectorElement.setAttribute("color", canvasManipulator.numbersToHexColor(colorArr.map(v => v * 255)));
        }

        function evaluatePositions(entity, relatedEntity) {
            let sourcePosition = canvasManipulator.getCenterOfEntity(entity);
            if (sourcePosition === null) {
                return {};
            }
            let targetPosition = canvasManipulator.getCenterOfEntity(relatedEntity);
            if (targetPosition === null) {
                return {};
            }

            if (controllerConfig.sourceStartAtParentBorder) {
                const sourceParent = entity.belongsTo;
                const targetParent = relatedEntity.belongsTo;
                if (sourceParent != targetParent) {
                    if (controllerConfig.targetEndAtParentBorder) {
                        targetPosition = canvasManipulator.getCenterOfEntity(targetParent);
                    }
                    const intersection = calculateBorderPosition(targetPosition, canvasManipulator.getCenterOfEntity(sourceParent), sourceParent);
                    if (intersection != undefined) {
                        sourcePosition = intersection;
                    } else console.debug("raycasting found no intersection with parent objects surface");
                }
            }

            if (controllerConfig.targetEndAtParentBorder) {
                const targetParent = relatedEntity.belongsTo;
                if (targetParent != entity.belongsTo) {
                    const intersection = calculateBorderPosition(sourcePosition, canvasManipulator.getCenterOfEntity(targetParent), targetParent);
                    if (intersection != undefined) {
                        targetPosition = intersection;
                    } else console.debug("raycasting found no intersection with parent objects surface");
                }
            }

            if (controllerConfig.sourceStartAtBorder) {
                if (controllerConfig.targetEndAtBorder) {
                    targetPosition = canvasManipulator.getCenterOfEntity(relatedEntity);
                }
                // getCenterOfEntity again in-case it got overwritten for sourceStartAtParentBorder
                sourcePosition = calculateBorderPosition(targetPosition, canvasManipulator.getCenterOfEntity(entity), entity);
            }
            if (controllerConfig.targetEndAtBorder) {
                // getCenterOfEntity again in-case it got overwritten for targetEndAtParentBorder
                targetPosition = calculateBorderPosition(sourcePosition, canvasManipulator.getCenterOfEntity(relatedEntity), relatedEntity);
            }

            // suggestion for city model: draw horizontal cylinders on the lower positions level
            if (controllerConfig.fixPositionY) {
                sourcePosition.y = Math.min(sourcePosition.y, targetPosition.y);
                targetPosition.y = sourcePosition.y;
            }
			
			//new 'centralPosition' ist the center of the Ring elements
			let centralPosition = sourcePosition;
			centralPosition.x = ((sourcePosition.x + targetPosition.x) / 2);
			centralPosition.z = ((sourcePosition.z + targetPosition.z) / 2);
			
            return {
                sourcePosition: sourcePosition,
                targetPosition: targetPosition,
				centralPosition: centralPosition,
            };
        }
		
        function calculateBorderPosition(sourceOfRay, targetOfRay, entity) {
            const object = document.getElementById(entity.id);
            const raycaster = new THREE.Raycaster();
            raycaster.set(sourceOfRay, targetOfRay.subVectors(targetOfRay, sourceOfRay).normalize());
            const intersection = raycaster.intersectObject(object.object3DMap.mesh);
            return intersection[0].point;
        }

        function combineObjectProperties(leftObj, rightObj, mergeFunction) {
            const mergedObject = {};
            for (const key of Object.keys(leftObj)) {
                mergedObject[key] = mergeFunction(leftObj[key], rightObj[key]);
            }
            return mergedObject;
        }

        function createConnector(entity, relatedEntity, relationId) {
            const {sourcePosition, targetPosition, centralPosition} = evaluatePositions(entity, relatedEntity);
            if (!sourcePosition || !targetPosition) {
                return null;
            }			
            
			// calculate the distance (= inner radius)
			const src = sourcePosition;
			src.setY(1);
			const dest = targetPosition;
			dest.setY(1);
            const distance = src.distanceTo(dest); 
			const outer = distance + Math.min(0.2*distance, 1.75);
			
			const delta = combineObjectProperties(targetPosition, sourcePosition, (left, right) => left - right);
            const direction = new THREE.Vector3(delta.x, 0, delta.z).normalize();
			
            // create connector
            const connector = document.createElement("a-ring");
            setConnectorMeshProperties(connector, direction);
            setCommonConnectorHTMLProperties(connector, controllerConfig.connectorColor);
			
			connector.setAttribute("position", centralPosition);
			connector.setAttribute("radius-inner", distance);
			if(distance < 5){
				let minimal = distance + 1; 
				connector.setAttribute("radius-outer", minimal);
			} else {
				connector.setAttribute("radius-outer", outer);
			}
            connector.setAttribute("id", relationId);
		//	connector.setAttribute("color", "navy");
			connector.setAttribute("side", "double");
			connector.setAttribute("theta-start", 0);
			connector.setAttribute("theta-length", 180);
			connector.setAttribute("segments-theta", 64);
			
            const scene = document.querySelector("a-scene");
            scene.appendChild(connector);

            const connectorElements = [];
            connectorElements.push(connector);

            return connectorElements;
        }

        return {
            createConnector: createConnector,
        };
    })(controllerConfig);
};