var createRelationConnectionHelper = function(controllerConfig) {
    return (function(controllerConfig) {

        const connectorSize = 0.05;

        function setConnectorMeshProperties(connectorElement, position, direction, width, length) {
            connectorElement.addEventListener("loaded", function () {
                const threeMesh = this.object3DMap.mesh;

                threeMesh.scale.set(width, length, width);
                threeMesh.position.set(position.x, position.y, position.z);

                const quaternion = threeMesh.quaternion;
                quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction);
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

            return {
                sourcePosition: sourcePosition,
                targetPosition: targetPosition,
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
            const {sourcePosition, targetPosition} = evaluatePositions(entity, relatedEntity);
            if (!sourcePosition || !targetPosition) {
                return null;
            }

            const delta = combineObjectProperties(targetPosition, sourcePosition, (left, right) => left - right);
            const distance = sourcePosition.distanceTo(targetPosition);
            const direction = new THREE.Vector3(delta.x, delta.y, delta.z).normalize();

            // create connector
            const connector = document.createElement("a-cylinder");
            const halfwayPoint = combineObjectProperties(sourcePosition, delta, (left, right) => left + right / 2);
            setConnectorMeshProperties(connector, halfwayPoint, direction, connectorSize, distance);
            setCommonConnectorHTMLProperties(connector, controllerConfig.connectorColor);
            connector.setAttribute("radius", 5);
            connector.setAttribute("id", relationId);

            const scene = document.querySelector("a-scene");
            scene.appendChild(connector);

            const connectorElements = [];
            connectorElements.push(connector);

            // create endpoints
            if (controllerConfig.createEndpoints) {
                const size = connectorSize * 1.5;
                const length = size * 6;
                const sourceEndpoint = document.createElement("a-cylinder");
                setConnectorMeshProperties(sourceEndpoint, sourcePosition, direction, size, length);
                setCommonConnectorHTMLProperties(sourceEndpoint, controllerConfig.endpointColor);

                const targetEndpoint = document.createElement("a-cylinder");
                setConnectorMeshProperties(targetEndpoint, targetPosition, direction, size, length);
                setCommonConnectorHTMLProperties(targetEndpoint, controllerConfig.endpointColor);

                scene.appendChild(sourceEndpoint);
                scene.appendChild(targetEndpoint);
                connectorElements.push(sourceEndpoint);
                connectorElements.push(targetEndpoint);
            }
            return connectorElements;
        }

        return {
            createConnector: createConnector,
        };
    })(controllerConfig);
};