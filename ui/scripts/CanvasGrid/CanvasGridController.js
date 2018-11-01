var canvasGridController = function() {

    const globalExtent = [0.0,0.0,0.0];
    const globalCenter = [0.0,0.0,0.0];

    let minValues = [0.0,0.0,0.0];
    let maxValues = [0.0,0.0,0.0];

    let isGridDrawn = false;

    let lineDistance        = "20";
    let lineThickness       = "0.3";
    let thickLineInterval   = "5";



    function initialize(){
		loadPositionData(multipartJsonUrl);				
	}

	function activate(rootDiv){	
       
        createMenu(rootDiv);

        $("#gridSettingsWindow").jqxWindow({ theme: "metro", width: 200, height: 180, isModal: true, autoOpen: false, resizable: false, cancelButton: $("#cancelGridSettings"), initContent: function() {
            // add UI elements to gridSettingsWindow
            $("#lineDistance").jqxInput({ theme: "metro", width: "48px", placeHolder: "20" });
            $("#lineThickness").jqxInput({ theme: "metro", width: "48px", placeHolder: "0.3" });
            $("#thickLineInterval").jqxInput({ theme: "metro", width: "48px", placeHolder: "5" });
            $("#gridInFront").jqxCheckBox({ theme: "metro", checked: false });
            $("#lineDistance").jqxInput("val", lineDistance);
            $("#lineThickness").jqxInput("val", lineThickness);
            $("#thickLineInterval").jqxInput("val", thickLineInterval);
            $("#gridInFront").jqxCheckBox("val", gridInFront);
            $("#applyGridSettings").jqxButton({ theme: "metro", width: "50px" });
            $("#cancelGridSettings").jqxButton({ theme: "metro", width: "50px" });
            }
        });
        
        //button 71 = "g"
        actionController.actions.keyboard.key[71].down.subscribe(function(actionEvent){
            $("#gridSettingsWindow").jqxWindow("open");
        });

        $("#applyGridSettings").click(function() {

            // format and save inputs
            lineDistance = Math.abs(Math.round($("#lineDistance").jqxInput("val")));
            lineThickness = Math.abs(Math.round($("#lineThickness").jqxInput("val") * 10) / 10);
            thickLineInterval = Math.abs(Math.round($("#thickLineInterval").jqxInput("val")));

            if(isGridDrawn){
                delLines();
                isGridDrawn = false;
               $("#applyGridSettings")[0].value = "Draw";
            } else {
                addLines();
                isGridDrawn = true;
                $("#applyGridSettings")[0].value = "Hide";
            }

            $("#gridSettingsWindow").jqxWindow("close");
        });
	}
	

	function reset(){
		delLines();
	}



    function createMenu(rootDiv){
        /*
        <div id="gridSettingsWindow">
            <div>Grid Settings</div>
            <div>
                <table>
                    <tr>
                        <td style="width: 135px;">Line distance:</td>
                        <td><input type="text" id="lineDistance"></td>
                    </tr>
                    <tr>
                        <td style="width: 135px;">Line thickness:</td>
                        <td><input type="text" id="lineThickness"></td>
                    </tr>
                    <tr>
                        <td style="width: 135px;">Thick line interval:</td>
                        <td><input type="text" id="thickLineInterval"></td>
                    </tr>
                    <tr>
                        <td style="width: 135px;">Show grid in front:</td>
                        <td><div id="gridInFront"></div></td>
                    </tr>
                </table>
                <input type="button" id="applyGridSettings" value="OK">
                <input type="button" id="cancelGridSettings" value="Cancel">
            </div>
        </div>
         
        #applyGridSettings {
            position: absolute;
            left: 90px;
        }

        #cancelGridSettings {
            position: absolute;
            left: 145px;
        }

        // workaround 
        #gridInFront {
            margin-left: -3px;
        }
        
        */

        let gridSettingsWindowDiv = document.createElement("DIV");
        rootDiv.appendChild(gridSettingsWindowDiv);
        gridSettingsWindowDiv.id = "gridSettingsWindow";

            let gridSettingsTitleDiv = document.createElement("DIV");
            gridSettingsWindowDiv.appendChild(gridSettingsTitleDiv);
            gridSettingsTitleDiv.innerHTML = "Grid Settings";

            let gridSettingsContentDiv = document.createElement("DIV");
            gridSettingsWindowDiv.appendChild(gridSettingsContentDiv);

                let gridSettingsTable =  document.createElement("TABLE");
                gridSettingsContentDiv.appendChild(gridSettingsTable);

                    //Line Distance
                    let gridSettingsTableRow = document.createElement("TR");
                    gridSettingsTable.appendChild(gridSettingsTableRow);
                        
                        let gridSettingsTableData = document.createElement("TD");
                        gridSettingsTableRow.appendChild(gridSettingsTableData);

                        gridSettingsTableData.style = "width: 135px;";
                        gridSettingsTableData.innerHTML = "Line distance:";

                        gridSettingsTableData = document.createElement("TD");
                        gridSettingsTableRow.appendChild(gridSettingsTableData);

                            let gridSettingsTableDataInput = document.createElement("INPUT");
                            gridSettingsTableData.appendChild(gridSettingsTableDataInput);

                            gridSettingsTableDataInput.id = "lineDistance";
                            gridSettingsTableDataInput.type = "text";
                    
                    //Line thickness
                    gridSettingsTableRow = document.createElement("TR");
                    gridSettingsTable.appendChild(gridSettingsTableRow);
                        
                        gridSettingsTableData = document.createElement("TD");
                        gridSettingsTableRow.appendChild(gridSettingsTableData);

                        gridSettingsTableData.style = "width: 135px;";
                        gridSettingsTableData.innerHTML = "Line thickness:";

                        gridSettingsTableData = document.createElement("TD");
                        gridSettingsTableRow.appendChild(gridSettingsTableData);

                            gridSettingsTableDataInput = document.createElement("INPUT");
                            gridSettingsTableData.appendChild(gridSettingsTableDataInput);

                            gridSettingsTableDataInput.id = "lineThickness";
                            gridSettingsTableDataInput.type = "text";

                    //Thick line interval
                    gridSettingsTableRow = document.createElement("TR");
                    gridSettingsTable.appendChild(gridSettingsTableRow);

                        gridSettingsTableData = document.createElement("TD");
                        gridSettingsTableRow.appendChild(gridSettingsTableData);

                        gridSettingsTableData.style = "width: 135px;";
                        gridSettingsTableData.innerHTML = "Thick line interval:";

                        gridSettingsTableData = document.createElement("TD");
                        gridSettingsTableRow.appendChild(gridSettingsTableData);

                            gridSettingsTableDataInput = document.createElement("INPUT");
                            gridSettingsTableData.appendChild(gridSettingsTableDataInput);

                            gridSettingsTableDataInput.id = "thickLineInterval";
                            gridSettingsTableDataInput.type = "text";

                    //Show grid in front
                    gridSettingsTableRow = document.createElement("TR");
                    gridSettingsTable.appendChild(gridSettingsTableRow);

                        gridSettingsTableData = document.createElement("TD");
                        gridSettingsTableRow.appendChild(gridSettingsTableData);

                        gridSettingsTableData.style = "width: 135px;";
                        gridSettingsTableData.innerHTML = "Show grid in front:";

                        gridSettingsTableData = document.createElement("TD");
                        gridSettingsTableRow.appendChild(gridSettingsTableData);

                            gridSettingsTableDataInput = document.createElement("DIV");
                            gridSettingsTableData.appendChild(gridSettingsTableDataInput);

                            gridSettingsTableDataInput.id = "gridInFront";

                            

                let gridSettingsApplyInput = document.createElement("INPUT");
                gridSettingsContentDiv.appendChild(gridSettingsApplyInput);
                gridSettingsApplyInput.type = "button";
                gridSettingsApplyInput.id = "applyGridSettings";
                gridSettingsApplyInput.value = "Draw";                

                let gridSettingsCancelInput = document.createElement("INPUT");
                gridSettingsContentDiv.appendChild(gridSettingsCancelInput);
                gridSettingsCancelInput.type = "button";
                gridSettingsCancelInput.id = "cancelGridSettings";
                gridSettingsCancelInput.value = "Cancel";

    }



    function loadPositionData(){
        // calculate maximum extents of model   
       
        // read multipart file and calculate maximum extent
        $.getJSON( multipartJsonUrl, function( data ) {
            // calculate maximum values
            data.mapping.forEach(function(mapping) {
                minValues = checkExtrema(mapping.min.split(", "), minValues, false);
                maxValues = checkExtrema(mapping.max.split(", "), maxValues, true);
            });

            // calculate global center
            for (let i = 0; i < 3; i++) {
                globalCenter[i] = Math.abs(minValues[i] - maxValues[i]) / 2 + minValues[i];
            }

            // calculate extents from maximum
            for(let i = 0; i < 3; i++) {
                globalExtent[i] = Math.abs(minValues[i] - maxValues[i]);
            }

        });
    }

  
    // add lines
    function addLines() {            
        addLinesHelper(minValues, maxValues);        
    }

    function addLinesHelper(minValues, maxValues) {
        // calculate maxExtent of model and overall quantiy of lines
        let maxExtent = calcMaxExtent();

        // round maxExtent up to next multiple of lineDistance
        let finalExtent = Math.ceil(maxExtent/lineDistance) * lineDistance + (2*lineDistance);

        // calculate translation on z axis
        let translZ = "0";
        if(gridInFront)
            translZ = parseFloat(maxValues[2] + lineThickness / 2).toString();
        else
            translZ = parseFloat(minValues[2] - lineThickness / 2).toString();

        // get gridContainer, move it to the center of the model and empty it
        let container = document.getElementById("addedElements");
        container.setAttribute("translation", globalCenter[0] + ", " + globalCenter[1] + ", " + translZ);
        delLines();

        // add new lines
        for (let i = (-(finalExtent/lineDistance/2)); i <= (finalExtent/lineDistance/2); i++) {
            container.appendChild(createLine(true, lineDistance, finalExtent, i));
            container.appendChild(createLine(false, lineDistance, finalExtent, i));
        }
    }

    // delete lines
    function delLines() {
        // get gridContainer
        const container = document.getElementById("addedElements");

        // delete all childs
        while (container.firstChild) {
            container.removeChild(container.firstChild);
        }
    }

    // create line
    function createLine(orientation, interval, maxExtent, lineNumber) {
        // define color and thickness of lines
        let col = "0 0 0";
        let thick = lineThickness;
        let doubleThick = (3 * parseFloat(thick)).toString();

        // create nodes
        let transform = document.createElement('Transform');
        let shape = document.createElement('Shape');
        let appearance = document.createElement('Appearance');
        let material = document.createElement('Material');
        let box = document.createElement('Box');

        // set color and render attribute
        transform.setAttribute("render", "true");
        material.setAttribute("diffuseColor", col);

        // shift fractional line numbers to integers -> modulo still works
        if (lineNumber % 1 !== 0) {
            lineNumber += 0.5;
        }

        // branch for orientation
        if (orientation) {
            // horizontal lines

            // translate line to final position
            transform.setAttribute("translation", "0 " + (i*interval).toString() + " 0");

            // every n-th line is thicker
            if (lineNumber % thickLineInterval === 0)
                box.setAttribute("size", (maxExtent).toString() + ", " + doubleThick + ", " + thick);
            else
                box.setAttribute("size", (maxExtent).toString() + ", " + thick + ", " + thick);
        }
        else {
            // vertical lines

            // translate line to final position
            transform.setAttribute("translation", (i*interval).toString() + " 0 0");

            // every n-th line is thicker
            if (lineNumber % thickLineInterval === 0)
                box.setAttribute("size", doubleThick + ", " + (maxExtent).toString() + ", " + thick);
            else
                box.setAttribute("size", thick + ", " + (maxExtent).toString() + ", " + thick);
        }

        // consolidate nodes to final node structure
        appearance.appendChild(material);
        shape.appendChild(appearance);
        shape.appendChild(box);
        transform.appendChild(shape);

        return transform;
    }

    // select biggest value of all extents
    function calcMaxExtent() {
        let maxExtent = 0.0;

        for (i = 0; i < 3; i++) {
            if (globalExtent[i] > maxExtent)
                maxExtent = globalExtent[i];
        }

        return maxExtent;
    }

    // save biggest values
    function checkExtrema(young, old, par) {

        // get float values from data
        for (i=0; i < 3; i++) {
            young[i] = parseFloat(young[i].trim());
        }

        // branch for negative and positive values and save maxima
        if (par) {
            for (let i = 0; i < 3; i++) {
                if (young[i] > old[i])
                    old[i] = young[i];
            }
        }
        else {
            for (let i = 0; i < 3; i++) {
                if (young[i] < old[i])
                    old[i] = young[i];
            }
        }

        return old;
    }

    return {        
        initialize		: initialize,
		reset			: reset,
		activate		: activate
    };


}();
