<!DOCTYPE html>

<!-- pass url parameters to javascript variables -->
<?php 		
	
	$setupUrl = "setups/default.js";
	if (isset($_GET["setup"])) {
		$setupUrl = "setups/" . $_GET["setup"] . ".js";
	}	

	$modelUrl = "data/RD bank/model";
	if (isset($_GET["model"])) {
		$modelUrl = "data/". $_GET["model"] ."/model";
	}

	$multipartX3dUrl = $modelUrl . "/multiPart.x3d";
	$multipartJsonUrl = $modelUrl . "/multiPart.json";
	$metaDataJsonUrl = $modelUrl . "/metaData.json";
?>
<script type="text/javascript">  
	var modelUrl		 = "<?php echo $modelUrl; ?>"; 
	
	var multipartX3dUrl  = "<?php echo $multipartX3dUrl; ?>"; 
	var multipartJsonUrl = "<?php echo $multipartJsonUrl; ?>"; 
	var metaDataJsonUrl  = "<?php echo $metaDataJsonUrl; ?>"; 
</script>


<html>
<head>
	<title>SVIS UI Prototype</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
	
    <!--Main-->
    <script type="text/javascript" src="libs/jquery-1.11.1.js"></script>
    <script type="text/javascript" src="libs/typeahead.bundle.js"></script>
    <script type="text/javascript" src="libs/handlebars-v2.0.0.js"></script>	
	
	<!--x3dom-->
	<script type="text/javascript" src="libs/x3dom/x3dom.debug.js"></script>	
	<link rel="stylesheet" href="libs/x3dom/x3dom.css" type="text/css"/>
	
	<!--jqwidgets-->	
    <script type="text/javascript" src="libs/jqwidgets/jqxcore.js"></script>
	<script type="text/javascript" src="libs/jqwidgets/jqxdata.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxbuttons.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxmenu.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxsplitter.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxscrollbar.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxpanel.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxcheckbox.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxradiobutton.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxexpander.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxinput.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxnavigationbar.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxwindow.js"></script>
    <script type="text/javascript" src="libs/jqwidgets/jqxnotification.js"></script>
	<script type="text/javascript" src="libs/jqwidgets/jqxtextarea.js"></script>
	<script type="text/javascript" src="libs/jqwidgets/jqxswitchbutton.js"></script>
	<script type="text/javascript" src="libs/jqwidgets/jqxprogressbar.js"></script>
	
    <link rel="stylesheet" href="libs/jqwidgets/styles/jqx.base.css" type="text/css" />
    <link rel="stylesheet" href="libs/jqwidgets/styles/jqx.metro.css" type="text/css" />
	
	<script type="text/javascript" src="scripts/CanvasFilter/CanvasFilterController.js"></script>
	<script type="text/javascript" src="scripts/CanvasMark/CanvasMarkController.js"></script>
	<script type="text/javascript" src="scripts/CanvasFlyTo/CanvasFlyToController.js"></script>
	<script type="text/javascript" src="scripts/CanvasHover/CanvasHoverController.js"></script>
	<script type="text/javascript" src="scripts/CanvasSelect/CanvasSelectController.js"></script>
	<script type="text/javascript" src="scripts/CanvasResetView/CanvasResetViewController.js"></script>	
	<script type="text/javascript" src="scripts/CanvasGrid/CanvasGridController.js"></script>	
	<script type="text/javascript" src="scripts/RelationConnector/RelationConnectorController.js"></script>	
	<script type="text/javascript" src="scripts/RelationTransparency/RelationTransparencyController.js"></script>
	<script type="text/javascript" src="scripts/RelationHighlight/RelationHighlightController.js"></script>	
	<script type="text/javascript" src="scripts/PackageExplorer/PackageExplorerController.js"></script>	
	<script type="text/javascript" src="scripts/Search/SearchController.js"></script>
	<script type="text/javascript" src="scripts/Experiment/ExperimentController.js"></script>
	<script type="text/javascript" src="scripts/SourceCode/SourceCodeController.js"></script>
	<script type="text/javascript" src="scripts/InteractionLogger/InteractionLogger.js"></script>
	<script type="text/javascript" src="scripts/Email/EmailController.js"></script>
	<script type="text/javascript" src="scripts/Menu/MenuController.js"></script>
	
	<!--user interface-->		
	<script type="text/javascript" src="scripts/DefaultLogger.js"></script>
	<script type="text/javascript" src="scripts/Model.js"></script>	
	<script type="text/javascript" src="scripts/Events.js"></script>	
	<script type="text/javascript" src="scripts/CanvasManipulator.js"></script>			
	<script type="text/javascript" src="scripts/Application.js"></script>		
	<script type="text/javascript" src="scripts/ActionController.js"></script>		
	
	
	<link rel="stylesheet" href="Style.css" type="text/css" />

	<!--setup-->	
	<script type="text/javascript" src="<?php echo $setupUrl; ?>"></script>
	
</head>
<body>    
	<div id="canvas">                    
		<x3d id="x3dElement" showConsole="true">
			<scene id="scene">
								
				<Transform id="addedElements"></Transform>
				
				<MultiPart id="multiPart" mapDEFToID="true" url="<?php echo $multipartX3dUrl; ?>" urlIDMap="<?php echo $multipartJsonUrl; ?>"></MultiPart>							

			</scene>
		</x3d>                    
    </div>

</body>
</html>
