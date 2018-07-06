package org.svis.generator

import java.text.SimpleDateFormat
import java.util.Date
import org.svis.generator.rd.RDSettings.Variant
import org.svis.generator.city.CitySettings.BuildingType

class X3DUtils {
	val config = SettingsConfiguration.instance

	def String toX3DHead() '''
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

	def String toX3DTail() '''
			<Background DEF="_Background" groundColor='1.0000000 1.0000000 1.0000000' skyColor='1.0000000 1.0000000 1.0000000'/>
			</Scene>
		</X3D>
	'''

	def String getTimeStamp() {
		val formatter = new SimpleDateFormat("YYYY-MM-dd/HH:mm:ss")
		return formatter.format(new Date)
	}

	def toX3DOMHead() '''
		<html> 
		<head> 
		<title>X3DOM-SVIS</title> 			
		<script type='text/javascript' src='http://x3dom.org/release/x3dom-full.js'> </script> 
		<link rel='stylesheet' type='text/css' href='http://www.x3dom.org/download/x3dom.css'></link> 
		«IF (config.variant == Variant::DYNAMIC || config.buildingType == BuildingType::CITY_DYNAMIC)»
			<script src="http://code.jquery.com/jquery-2.1.1.min.js"></script>
			<script type='text/javascript' src='./anifra-minified.js'> </script>
		«ENDIF»
		<style>
		#x3droot {
		width: 100%;
		height: 100%;
		}
		</style>
		</head> 
		<body> 	
		 <x3d showLog='false' id='x3droot'> 
		 <scene id="scene"> 
	'''

	def String toX3DScaleHead(int absoluteLength) '''
		<Transform translation='0 0 «absoluteLength/2»' scale='1 1 «absoluteLength»'> 
	'''

	def String toX3DScaleTail() '''
		</Transform> 
	'''

	def toX3DOMTail() '''
		</scene> 
		</x3d> 
		</body> 
		</html>    
	'''

	def toAFrameHead() '''
		<!DOCTYPE html>
		<html>
		  <head>
		    <meta charset="utf-8">
		    <title>Ring</title>
		    <meta name="description" content="Ring - A-Frame">
		    <script src="https://aframe.io/releases/0.7.0/aframe.min.js"></script>
		  </head>
		  <body>
		    <a-scene altspace scale="0.001 0.001 0.001" light="defaultLightsEnabled: false" stats>
		    <a-entity position="0 0 200">
		      <a-camera></a-camera>
		    </a-entity>
	'''

	def toAFrameTail() '''
			</a-scene>
		  </body>
		</html>
	'''

	def toAnimationFramework() '''
		«IF (config.variant == Variant::DYNAMIC || config.buildingType == BuildingType::CITY_DYNAMIC)»
			«toAnimationFramework("events.js")»
		«ENDIF»
	'''

	def toAnimationFramework(String eventsFileName) '''
		<AnimationFramework id="framework" eventsUrl="./«eventsFileName»">
			  <NodeEventHandler></NodeEventHandler>
			  <EdgeEventHandler></EdgeEventHandler>
			  <NodePropertyChangeHandler></NodePropertyChangeHandler>
			  <MoveNodeHandler></MoveNodeHandler>
			  <LabelEventHandler></LabelEventHandler>
			</AnimationFramework>
	'''
}
