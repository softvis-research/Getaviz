<!DOCTYPE html>

<!-- pass url parameters to javascript variables -->
<?php

$srcDir = "data";
if (isset($_GET["srcDir"])) {
    $srcDir = $_GET["srcDir"];
}

$setupUrl = "setups/web/default.js";
if (isset($_GET["setup"])) {
    $setupUrl = "setups/" . $_GET["setup"] . ".js";
}

$modelUrl = "data/City/model";
if (isset($_GET["model"])) {
    $modelUrl = $srcDir . "/" . $_GET["model"] . "/model";
} else {
    if (!isset($_GET["setup"])) {
        $setupUrl = "setups/web/City bank.js";
    }
}

$metaDataJsonUrl = $modelUrl . "/metaData.json";

$stateHashcode = "";
if (isset($_GET["state"])) {
    $stateHashcode = $_GET["state"];
    $metaStateJsonUrl = "state.php?hash=" . $stateHashcode;
} else {
    $metaStateJsonUrl = "state.php?hash=";
}

$lazyLoadingEnabled = false;
if (isset($_GET["lazy"])) {
    $lazyLoadingEnabled = $_GET["lazy"];
}

?>


<script type="text/javascript">
    var modelUrl = "<?php echo $modelUrl; ?>";
    var stateHashcode = "<?php echo $stateHashcode; ?>";
    var metaStateJsonUrl = "<?php echo $metaStateJsonUrl; ?>";
    var metaDataJsonUrl = "<?php echo $metaDataJsonUrl; ?>";
    var lazyLoadingEnabled = "<?php echo $lazyLoadingEnabled; ?>" === 'true';

    var canvasId = "aframe-canvas";
    var visMode = "aframe";
</script>

<html>
<title>Getaviz</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<link rel="icon" type="image/vnd.microsoft.icon" href="favicon.ico">

<!--Main-->
<script type="text/javascript" src="node_modules/jquery/dist/jquery.min.js"></script>
<script type="text/javascript" src="node_modules/typeahead.js/dist/typeahead.bundle.min.js"></script>
<script type="text/javascript" src="node_modules/handlebars/dist/handlebars.min.js"></script>
<!--<script type="text/javascript" src="node_modules/aframe/dist/aframe-v1.0.0.min.js"></script>-->
<script src="https://aframe.io/releases/1.0.4/aframe.min.js"></script>

<!--jqwidgets-->
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxcore.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxdata.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxbuttons.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxtabs.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxmenu.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxsplitter.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxscrollbar.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxpanel.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxcheckbox.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxradiobutton.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxexpander.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxinput.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxnavigationbar.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxwindow.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxnotification.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxtextarea.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxswitchbutton.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxprogressbar.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxcombobox.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxlistbox.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxdropdownlist.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxslider.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxnumberinput.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxdatetimeinput.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxcalendar.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxform.js"></script>
<script type="text/javascript" src="node_modules/jqwidgets-scripts/jqwidgets/jqxvalidator.js"></script>
<script type="text/javascript" src="node_modules/prismjs/prism.js"></script>
<script type="text/javascript" src="node_modules/prismjs/components/prism-java.js"></script>
<script type="text/javascript" src="node_modules/prismjs/components/prism-c.js"></script>

<link rel="stylesheet" href="node_modules/jqwidgets-scripts/jqwidgets/styles/jqx.base.css" type="text/css"/>
<link rel="stylesheet" href="node_modules/jqwidgets-scripts/jqwidgets/styles/jqx.metro.css" type="text/css"/>

<!-- ztree -->
<script type="text/javascript" src="node_modules/@ztree/ztree_v3/js/jquery.ztree.core.min.js"></script>
<script type="text/javascript" src="node_modules/@ztree/ztree_v3/js/jquery.ztree.exhide.min.js"></script>
<script type="text/javascript" src="node_modules/@ztree/ztree_v3/js/jquery.ztree.excheck.min.js"></script>
<link rel="stylesheet" href="node_modules/@ztree/ztree_v3/css/metroStyle/metroStyle.css" type="text/css">
<link rel="stylesheet" href="scripts/PackageExplorer/zt.css" type="text/css">

<!-- orbitcam -->

<!-- <script type="text/javascript" src="node_modules/aframe-orbit-camera-component/aframe-orbit-camera-component.js"></script> -->
<script type="text/javascript" src="scripts/ABAP/aframe-orbit-camera-component.js"></script>


<!-- controller -->
<script type="text/javascript" src="scripts/CanvasFilter/CanvasFilterController.js"></script>
<script type="text/javascript" src="scripts/CanvasMark/CanvasMarkController.js"></script>
<script type="text/javascript" src="scripts/CanvasHover/AframeCanvasHoverController.js"></script>
<script type="text/javascript" src="scripts/CanvasFlyTo/CanvasFlyToController.js"></script>
<script type="text/javascript" src="scripts/CanvasSelect/CanvasSelectController.js"></script>
<script type="text/javascript" src="scripts/CanvasResetView/CanvasResetViewController.js"></script>
<script type="text/javascript" src="scripts/CanvasGrid/CanvasGridController.js"></script>


<script type="text/javascript" src="scripts/Relation/RelationController.js"></script>
<script type="text/javascript" src="scripts/Relation/RelationConnectionHelper.js"></script>

<script type="text/javascript" src="scripts/Menu/MenuController.js"></script>
<script type="text/javascript" src="scripts/HelpController/HelpController.js"></script>
<script type="text/javascript" src="scripts/InfoController/InfoController.js"></script>
<script type="text/javascript" src="scripts/Email/EmailController.js"></script>
<script type="text/javascript" src="scripts/ShareController/ShareController.js"></script>
<script type="text/javascript" src="scripts/Legend/LegendController.js"></script>


<script type="text/javascript" src="scripts/PackageExplorer/PackageExplorerController.js"></script>
<script type="text/javascript" src="scripts/Search/SearchController.js"></script>
<script type="text/javascript" src="scripts/Experiment/ExperimentController.js"></script>
<script type="text/javascript" src="scripts/SourceCode/SourceCodeController.js"></script>
<script type="text/javascript" src="scripts/InteractionLogger/InteractionLogger.js"></script>
<script type="text/javascript" src="scripts/Email/EmailController.js"></script>
<script type="text/javascript" src="scripts/GenerationForm/GenerationFormController.js"></script>
<script type="text/javascript" src="scripts/Menu/MenuController.js"></script>
<script type="text/javascript" src="scripts/PatternConnector/PatternConnectorController.js"></script>
<script type="text/javascript" src="scripts/Configuration/ConfigurationController.js"></script>
<script type="text/javascript" src="scripts/PatternExplorer/PatternExplorerController.js"></script>
<script type="text/javascript" src="scripts/VersionExplorer/VersionExplorerController.js"></script>
<script type="text/javascript" src="scripts/IssueExplorer/IssueExplorerController.js"></script>
<script type="text/javascript" src="scripts/MacroExplorer/MacroExplorerController.js"></script>

<script type="text/javascript" src="scripts/Metric/Constants.js"></script>
<script type="text/javascript" src="scripts/Metric/ColorGradient.js"></script>
<script type="text/javascript" src="scripts/Metric/MetricLayer.js"></script>
<script type="text/javascript" src="scripts/Metric/DomHelper.js"></script>
<script type="text/javascript" src="scripts/Metric/MetricController.js"></script>

<!-- filter -->
<script type="text/javascript" src="scripts/Filter/Helpers/Constants.js"></script>
<script type="text/javascript" src="scripts/Filter/Helpers/DOMHelper.js"></script>
<script type="text/javascript" src="scripts/Filter/Helpers/RelationHelper.js"></script>
<script type="text/javascript" src="scripts/Filter/Helpers/FilterHelper.js"></script>
<script type="text/javascript" src="scripts/Filter/Helpers/TransformationHelper.js"></script>
<script type="text/javascript" src="scripts/Filter/Classes/Filter.js"></script>
<script type="text/javascript" src="scripts/Filter/Classes/Container.js"></script>
<script type="text/javascript" src="scripts/Filter/Classes/Layer.js"></script>
<script type="text/javascript" src="scripts/Filter/X3DomFilterController.js"></script>

<script type="text/javascript" src="scripts/GenerationForm/GenerationFormController.js"></script>


<!-- extension to integrate -->
<script type="text/javascript" src="scripts/MetricAnimation/MetricAnimationController.js"></script>
<script type="text/javascript" src="scripts/MetricAnimation/Classes/MetricAnimation.js"></script>
<script type="text/javascript" src="scripts/MetricAnimation/Classes/MetricAnimationColor.js"></script>
<script type="text/javascript" src="scripts/MetricAnimation/Classes/MetricAnimationExpanding.js"></script>

<script type="text/javascript" src="scripts/MagGlass/MagGlassController.js"></script>

<!-- ABAP Extensions -->
<script type="text/javascript" src="scripts/ABAP/BannerController.js"></script>


<!--user interface-->
<script type="text/javascript" src="scripts/DefaultLogger.js"></script>
<script type="text/javascript" src="scripts/Model.js"></script>
<script type="text/javascript" src="scripts/Events.js"></script>
<script type="text/javascript" src="scripts/AframeCanvasManipulator.js"></script>
<script type="text/javascript" src="scripts/AframeActionController.js"></script>
<script type="text/javascript" src="scripts/ModelLoader.js"></script>

<script type="text/javascript" src="scripts/Application.js"></script>
<link rel="stylesheet" href="Style.css" type="text/css"/>

<!--setup-->
<script type="text/javascript" src="<?php echo $setupUrl; ?>"></script>

</head>
<body>
<div id="canvas">
    <script>
        $(function(){
            if (!lazyLoadingEnabled) {
                $("#canvas").load(encodeURI(modelUrl + "/model.html"));
            } else {
                // add an empty scene that we can fill later
                $('#canvas').append(`<a-scene id="${canvasId}" cursor="rayOrigin: mouse" embedded="true" renderer="logarithmicDepthBuffer: true;">
    <a-assets>
        <img id="sky" crossorigin="anonymous" src="assets/sky_pano.jpg">
        <img id="sea" crossorigin="anonymous" src="assets/pool-water.jpg">
        <img id="ground" crossorigin="anonymous" src="assets/ground.jpg">
        <a-asset-item id="mountain" src="assets/polyMountain_new_Color.glb"></a-asset-item>
        <a-asset-item id="cloud_black" src="assets/cloud_black.glb"></a-asset-item>
    </a-assets>
    <a-sky src="#sky" radius="7000"></a-sky>
    <a-plane src="#ground" height="5000" width="5000" rotation="-90 0 0" position="0 0 0" repeat="30 30"></a-plane>
    <a-entity id="camera" camera="fov: 80; zoom: 1;"
        position="-20 140 -20"
        rotation="0 -90 0"
        orbit-camera="
            target: 80 0.0 80;
            enableDamping: true;
            dampingFactor: 0.25;
            rotateSpeed: 0.25;
            panSpeed: 0.25;
            invertZoom: true;
            logPosition: false;
            minDistance:0;
            maxDistance:1000;
            "
        mouse-cursor="">
    </a-entity>
 </a-scene>`);
            }
        });
        var globalCamera;
    </script>
</div>
</body>
</html>
