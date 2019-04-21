package org.getaviz.generator

import org.getaviz.generator.SettingsConfiguration.BuildingType
import org.getaviz.generator.city.m2m.CityLayout

class OutputFormatHelper {
	val config = SettingsConfiguration.getInstance();

	def String X3DHead() '''
		<?xml version="1.0" encoding="UTF-8"?>
		<!DOCTYPE X3D PUBLIC "ISO//Web3D//DTD X3D 3.3//EN" "http://www.web3d.org/specifications/x3d-3.3.dtd">
		<X3D profile='Immersive' version='3.3' xmlns:xsd='http://www.w3.org/2001/XMLSchema-instance' xsd:noNamespaceSchemaLocation='http://www.web3d.org/specifications/x3d-3.3.xsd'>
		    <head>
		        <meta content='model.x3d' name='title'/>
		        <meta content='SVIS-Generator' name='creator'/>
			</head>
			   <ContextSetup zWriteTrans='false'/>
		<Scene>
	'''

	def String settingsInfo() '''
		<SettingsInfo ClassElements='«config.classElementsMode»' SortModeCoarse='«config.classElementsSortModeCoarse»' SortModeFine='«config.classElementsSortModeFine»' SortModeFineReversed='«config.classElementsSortModeFineDirectionReversed»' Scheme='«config.scheme»' ShowBuildingBase='«config.showBuildingBase»'
		«IF config.buildingType == BuildingType.CITY_BRICKS»
			BrickLayout='«config.brickLayout»'
		«ELSEIF config.buildingType == BuildingType.CITY_PANELS»
			AttributesAsCylinders='«config.showAttributesAsCylinders»' PanelSeparatorMode='«config.panelSeparatorMode»'
		«ELSE»
		«ENDIF»
		/>
	'''

	def String viewports() '''
		«var rootEntity = CityLayout::rootRectangle»
		«var width = rootEntity.width»
		«var length = rootEntity.length»
		<Group DEF='Viewpoints'>
			<Viewpoint description='Initial' position='«-width*0.5 +" "+ ((width+length)/2)*0.25 +" "+ -length*0.5»' orientation='0 1 0 4' centerOfRotation='«width/2 +" 0 "+ length/2»'/>
			<Viewpoint description='Opposite Side' position='«width*1.5 +" "+ ((width+length)/2)*0.25 +" "+ length*1.5»' orientation='0 1 0 0.8' centerOfRotation='«width/2 +" 0 "+ length/2»'/>
			<Viewpoint description='Screenshot' position='«-width*0.5 +" "+ ((width+length)/2)*0.75 +" "+ -length*0.5»' orientation='0.1 0.95 0.25 3.8' centerOfRotation='«width/2 +" 0 "+ length/2»'/>
			<Viewpoint description='Screenshot Opposite Side' position='«width*1.5 +" "+ ((width+length)/2)*0.75 +" "+ length*1.5»' orientation='-0.5 0.85 0.2 0.8' centerOfRotation='«width/2 +" 0 "+ length/2»'/>
		</Group>
	'''

	def String X3DTail() '''
			<Background DEF="_Background" groundColor='1.0000000 1.0000000 1.0000000' skyColor='1.0000000 1.0000000 1.0000000'/>
			</Scene>
		</X3D>
	'''

	def String AFrameHead() '''
		<!DOCTYPE html>
		<html>
			<head>
				<meta charset="utf-8">
			    <title>Ring</title>
			    <meta name="description" content="Getaviz">
			</head>
			<body>
				<a-scene id="aframe-canvas"
			    	light="defaultLightsEnabled: false"
			    	cursor="rayOrigin: mouse"
			    	embedded="true"
			    >
				    <a-entity
				    	id="camera"
				    	camera="fov: 80; zoom: 1;"
				    	position="44.0 20.0 44.0"
				    	rotation="0 -90 0"
				    	orbit-camera="
				    	   	target: 15.0 1.5 15.0;
				    	   	enableDamping: true;
				    	   	dampingFactor: 0.25;
				    	   	rotateSpeed: 0.25;
				    	   	panSpeed: 0.25;
				    	   	invertZoom: true;
				    	   	logPosition: false;
				    	   	minDistance:0;
				    	   	maxDistance:1000;
				    	   	"
				    	mouse-cursor=""
				   		>
				    </a-entity>
	'''

	def String AFrameTail() '''
				</a-scene>
		 	</body>
		</html>
	'''
}
