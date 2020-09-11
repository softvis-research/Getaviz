
	/**
	* @author Jens Thomann <jt23coqi@studserv.uni-leipzig.de>
	*/
	
	// Name Conversion Rules from "label" to "bind"
	// Example: city.building_type --> city_building_type	
	// Most options in the file settings.properties contain a "."
	// If "." is used in "bind" the ui does not load ("." is interpreted as a function call)
	
var generationFormController = (function() {

	let settingsForm ;
	
	let controllerConfig = {
		createHeadSection: true
	};

	function initialize(setupConfig){
		application.transferConfigParams(setupConfig, controllerConfig);
	}


	
	function activate(rootDiv){
		
		createSettingPopup(rootDiv);
		
		$("#settingsPopupWindowDiv").jqxWindow({ theme: "metro", width: '33%', minWidth: 650, height: '70%', isModal: true, draggable: false, autoOpen: false, resizable: true, closeButtonSize: 0, initContent: function() {
		   
				// Add Form Inputs and Labels here
 				const template = [
					
					// Independent Options 					
					{
						bind: 'input_name',
						name: 'input.name',
						type: 'text',
						label: 'input.name',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						info: 'Name of the visualization or the visualized system',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},				
					{
						bind: 'input_files',
						name: 'input.files',
						type: 'text',
						label: 'input.files',
						required: true,
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						info: 'URL to a .jar or .war file',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},							
					{
						bind: 'metaphor',
						name: 'metaphor',
						type: 'option',
						label: 'metaphor',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'rd' },
							{ value: 'city'}
						],
						info: 'Visualization metaphor of the generated visualization',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},			
					{
						type: 'blank',
						rowHeight: '25px'
					},
					
					// City Options 
					{
						bind: 'city_building_type',
						name: 'city.building_type',
						type: 'option',
						label: 'city.building_type',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'original' },
							{ value: 'panels'},
							{ value: 'bricks'},
							{ value: 'floor'}
						],
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_scheme',
						name: 'city.scheme',
						type: 'option',
						label: 'city.scheme',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'types' },
							{ value: 'visibility'}
						],
						info:'The active mode to structure and color the methods and attributes. &#013;Possible Values are: &#013;types (default): The class elements are sorted and colored associated to type/functionality of the method. &#013;visibility: The class elements are sorted and colored corresponding to their visibility modifiers.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_class_elements_mode',
						name: 'city.class_elements_mode',
						type: 'option',
						label: 'city.class_elements_mode',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'methods_and_attributes' },
							{ value: 'methods_only'},
							{ value: 'attributes_only'}
						],
						info:'Switch to control the elements of the classes to show.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_class_elements_sort_mode_coarse',
						name: 'city.class_elements_sort_mode_coarse',
						type: 'option',
						label: 'city.class_elements_sort_mode_coarse',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'methods_first' },
							{ value: 'unsorted'},
							{ value: 'attributes_first'}
						],
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_class_elements_sort_mode_fine',
						name: 'city.class_elements_sort_mode_fine',
						type: 'option',
						label: 'city.class_elements_sort_mode_fine',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'scheme' },
							{ value: 'unsorted'},
							{ value: 'alphabetically'},
							{ value: 'nos'}
						],
						info:'The active mode, how to sort the methods or attributes separately among each other. &#013;This means a method is only compared to another method and an attribute is only compared to another attribute in this comparison, according to their values. &#013;If it is set to scheme, a secondary sorting is performed to place methods with high numbers of statements at the bottom.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_class_elements_sort_mode_fine_direction_reversed',
						name: 'city.class_elements_sort_mode_fine_direction_reversed',
						type: 'option',
						label: 'city.class_elements_sort_mode_fine_direction_reversed',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'true' },
							{ value: 'false'}
						],
						info:'If set true, the order of the sorting, defined in class_elements_sort_mode_fine is reversed. &#013;If class_elements_sort_mode_fine is set to scheme, a secondary sorting is performed to place methods with high numbers of statements at the bottom. &#013;This behavior is not influenced by this switch.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_show_building_base',
						name: 'city.show_building_base',
						type: 'option',
						label: 'city.show_building_base',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'true' },
							{ value: 'false'}
						],
						info:'Switch to show or hide building base in panels or bricks mode. &#013;If set to false, only districts and buildingSegments are visible.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_show_attributes_as_cylinders',
						name: 'city.show_attributes_as_cylinders',
						type: 'option',
						label: 'city.show_attributes_as_cylinders',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'true' },
							{ value: 'false'}
						],
						info:'Switch for showing attributes as cylinders instead of boxes. &#013;This setting has only an affect in panels-mode.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_brick_layout',
						name: 'city.brick.layout',
						type: 'option',
						label: 'city.brick.layout',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'progressive' },
							{ value: 'straight'},
							{ value: 'balanced'}
						],
						info:'The active mode for the layout of the bricks/methods. &#013;This setting has only an affect in brick-mode.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_brick_size',
						name: 'city.brick.size',
						type: 'number',
						label: 'city.brick.size',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_brick_horizontal_margin',
						name: 'city.brick.horizontal_margin',
						type: 'number',
						label: 'city.brick.horizontal_margin',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_brick_horizontal_gap',
						name: 'city.brick.horizontal_gap',
						type: 'number',
						label: 'city.brick.horizontal_gap',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_brick_vertical_margin',
						name: 'city.brick.vertical_margin',
						type: 'number',
						label: 'city.brick.vertical_margin',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_brick_vertical_gap',
						name: 'city.brick.vertical_gap',
						type: 'number',
						label: 'city.brick.vertical_gap',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_panel_separator_mode',
						name: 'city.panel.separator_mode',
						type: 'option',
						label: 'city.panel.separator_mode',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'separator' },
							{ value: 'none'},
							{ value: 'gap'}
						],
						info:'The active mode for the area between panels/methods. &#013;Possible values are &#013;separator (default): Between the panels separators are placed with a fixed height and color. &#013;none: No space between the panels and they are placed on top of each other. &#013;gap: The panels have a free space between them and do not touch each other.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_panel_height_treshold_nos',
						name: 'city.panel.height_treshold_nos',
						type: 'option',
						label: 'city.panel.height_treshold_nos',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: '3' },
							{ value: '6'},
							{ value: '12'},
							{ value: '24'},
							{ value: '48'},
							{ value: '96'},
							{ value: '144'},
							{ value: '192'},
							{ value: '240'}
						],
						info:'Multiplier for height of a panel, declared in panel.height_unit. &#013;The elements of this array are threshold values for the number of statements inside the method and are multiplied with the index+1, so the product will be the actual height of the panel. &#013;The values are inclusive.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_panel_height_unit',
						name: 'city.panel.height_unit',
						type: 'number',
						label: 'city.panel.height_unit',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						info:'Height is multiplied by city.panel.height_treshold_nos ',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_panel_horizontal_margin',
						name: 'city.panel.horizontal_margin',
						type: 'number',
						label: 'city.panel.horizontal_margin',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_panel_vertical_margin',
						name: 'city.panel.vertical_margin',
						type: 'number',
						label: 'city.panel.vertical_margin',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_panel_vertical_gap',
						name: 'city.panel.vertical_gap',
						type: 'number',
						label: 'city.panel.vertical_gap',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_panel_separator_height',
						name: 'city.panel.separator_height',
						type: 'number',
						label: 'city.panel.separator_height',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_original_building_metric',
						name: 'city.original_building_metric',
						type: 'option',
						label: 'city.original_building_metric',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'none' },
							{ value: 'nos'}
						],
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_width_min',
						name: 'city.width_min',
						type: 'number',
						label: 'city.width_min',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_height_min',
						name: 'city.height_min',
						type: 'number',
						label: 'city.height_min',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_building_horizontal_margin',
						name: 'city.building.horizontal_margin',
						type: 'number',
						label: 'city.building.horizontal_margin',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_building_horizontal_gap',
						name: 'city.building.horizontal_gap',
						type: 'number',
						label: 'city.building.horizontal_gap',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_building_vertical_margin',
						name: 'city.building.vertical_margin',
						type: 'number',
						label: 'city.building.vertical_margin',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_package_color_start',
						name: 'city.package.color_start',
						type: 'text',
						label: 'city.package.color_start',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_package_color_end',
						name: 'city.package.color_end',
						type: 'text',
						label: 'city.package.color_end',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_class_color_start',
						name: 'city.class.color_start',
						type: 'text',
						label: 'city.class.color_start',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_class_color_end',
						name: 'city.class.color_end',
						type: 'text',
						label: 'city.class.color_end',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_class_color',
						name: 'city.class.color',
						type: 'text',
						label: 'city.class.color',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_blue',
						name: 'city.color.blue',
						type: 'text',
						label: 'city.color.blue',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_aqua',
						name: 'city.color.aqua',
						type: 'text',
						label: 'city.color.aqua',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_light_green',
						name: 'city.color.light_green',
						type: 'text',
						label: 'city.color.light_green',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_dark_green',
						name: 'city.color.dark_green',
						type: 'text',
						label: 'city.color.dark_green',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_yellow',
						name: 'city.color.yellow',
						type: 'text',
						label: 'city.color.yellow',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_orange',
						name: 'city.color.orange',
						type: 'text',
						label: 'city.color.orange',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_red',
						name: 'city.color.red',
						type: 'text',
						label: 'city.color.red',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_pink',
						name: 'city.color.pink',
						type: 'text',
						label: 'city.color.pink',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_violet',
						name: 'city.color.violet',
						type: 'text',
						label: 'city.color.violet',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_light_grey',
						name: 'city.color.light_grey',
						type: 'text',
						label: 'city.color.light_grey',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_dark_grey',
						name: 'city.color.dark_grey',
						type: 'text',
						label: 'city.color.dark_grey',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_white',
						name: 'city.color.white',
						type: 'text',
						label: 'city.color.white',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'city_color_black',
						name: 'city.color.black',
						type: 'text',
						label: 'city.color.black',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},
					
					// Recursive Disk Options 					
					{
						bind: 'rd_data_factor',
						name: 'rd.data_factor',
						type: 'number',
						label: 'rd.data_factor',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},										
					{
						bind: 'rd_height',
						name: 'rd.height',
						type: 'number',
						label: 'rd.height',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},															
					{
						bind: 'rd_ring_width',
						name: 'rd.ring_width',
						type: 'number',
						label: 'rd.ring_width',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},										
					{
						bind: 'rd_ring_width_ad',
						name: 'rd.ring_width_ad',
						type: 'number',
						label: 'rd.ring_width_ad',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						info:'Equal to ring_width_md but for attribute disks',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_min_area',
						name: 'rd.min_area',
						type: 'number',
						label: 'rd.min_area',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_namespace_transparency',
						name: 'rd.namespace_transparency',
						type: 'number',
						label: 'rd.namespace_transparency',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_class_transparency',
						name: 'rd.class_transparency',
						type: 'number',
						label: 'rd.class_transparency',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_method_transparency',
						name: 'rd.method_transparency',
						type: 'number',
						label: 'rd.method_transparency',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_data_transparency',
						name: 'rd.data_transparency',
						type: 'number',
						label: 'rd.data_transparency',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_color_class',
						name: 'rd.color.class',
						type: 'text',
						label: 'rd.color.class',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_color_data',
						name: 'rd.color.data',
						type: 'text',
						label: 'rd.color.data',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_color_method',
						name: 'rd.color.method',
						type: 'text',
						label: 'rd.color.method',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_color_namespace',
						name: 'rd.color.namespace',
						type: 'text',
						label: 'rd.color.namespace',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_method_disks',
						name: 'rd.method_disks',
						type: 'option',
						label: 'rd.method_disks',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'true' },
							{ value: 'false'}
						],
						info:'If true the Methods will be visualized as Disks instead of DiskSegments.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_data_disks',
						name: 'rd.data_disks',
						type: 'option',
						label: 'rd.data_disks',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'true' },
							{ value: 'false'}
						],
						info:'If true Attributes will be visualized as disks.',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_method_type_mode',
						name: 'rd.method_type_mode',
						type: 'option',
						label: 'rd.method_type_mode',
						labelAlign: 'right',
						labelWidth: '325px',
						width: '200px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'true' },
							{ value: 'false'}
						],
						info:'If true visualization will be based on the method type',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},
					{
						type: 'blank',
						rowHeight: '25px',
					},	
					// Buttons
					{
						columns: [
							{
								name: 'resetButton',
								type: 'button',
								text: 'Reset',
								align: 'left',
								width: '131px',
								columnWidth: '50%'
							},
							{
								name: 'submitButton',
								type: 'button',
								text: 'Submit',
								align: 'right',
								width: '131px',
								columnWidth: '25%'
							},
							{
								name: 'cancelButton',
								type: 'button',
								text: 'Cancel',
								align: 'right',
								width: '131px',
								columnWidth: '25%'
							}      
						]
					}
					
				];
				
				// Default Values 				
				const defaultValue = {
					input_name: 'default',
					input_files: '',
					metaphor: 'city',
					city_building_type: 'original',
					city_scheme: 'types',
					city_class_elements_mode: 'methods_and_attributes',
					city_class_elements_sort_mode_coarse: 'methods_first',
					city_class_elements_sort_mode_fine: 'scheme',
					city_class_elements_sort_mode_fine_direction_reversed: false,
					city_show_building_base: true,
					city_show_attributes_as_cylinders: true,
					city_brick_layout: 'progressive',
					city_brick_size: 1.0,
					city_brick_horizontal_margin: 0.5,
					city_brick_horizontal_gap: 0.2,
					city_brick_vertical_margin: 0.2,
					city_brick_vertical_gap: 0.2,
					city_panel_separator_mode: 'separator',
					city_panel_height_treshold_nos: 3,
					city_panel_height_unit: 0.5,
					city_panel_horizontal_margin: 0.5,
					city_panel_vertical_margin: 0.25,
					city_panel_vertical_gap: 0.125,
					city_panel_separator_height: 0.125,
					city_original_building_metric: 'none',
					city_width_min: 1,
					city_height_min: 1,
					city_building_horizontal_margin: 3.0,
					city_building_horizontal_gap: 3.0,
					city_building_vertical_margin: 1.0,
					city_package_color_start: '#969696',
					city_package_color_end: '#B1B1B1',
					city_class_color_start: '#131615',
					city_class_color_end: '#00FF00',
					city_class_color: '#353559',
					city_color_blue: '#99FFCC',
					city_color_aqua: '#99CCFF',
					city_color_light_green: '#CCFF99',
					city_color_dark_green: '#99FF99',	
					city_color_yellow: '#FFFF99',	
					city_color_orange: '#FFCC99',	
					city_color_red: '#FF9999',
					city_color_pink: '#FF99FF',
					city_color_violet: '#9999FF',
					city_color_light_grey: '#CCCCCC',	
					city_color_dark_grey: '#999999',	
					city_color_white: '#FFFFFF',	
					city_color_black: '#000000',	
					rd_data_factor: 4.0,
					rd_method_factor: 1.0,
					rd_height: 1.0,
					rd_height_boost: 8.0,
					rd_height_multiplicator: 50.0,
					rd_ring_width: 2.0,
					rd_ring_width_md: 0,
					rd_ring_width_ad: 0,
					rd_min_area: 10.0,
					rd_namespace_transparency: 0,
					rd_class_transparency: 0,
					rd_method_transparency: 0,
					rd_data_transparency: 0,
					rd_color_class: '#353559',
					rd_color_data: '#FFFC19',
					rd_color_method: '#1485CC',
					rd_color_namespace: '#969696',
					rd_method_disks: false,
					rd_data_disks: false,
					rd_method_type_mode: false
				};	
				
				// Generate Form 
				settingsForm = $('#settingsForm');
				settingsForm.jqxForm({
					template: template,
					value: defaultValue,
					padding: { left: 5, top: 5, right: 5, bottom: 5 },
					theme: "metro"
				});	

				// On first load show only the input fields for initially selected options (city, optional)
				initial_load_city_optional();
				
				// formDataChange Event
				settingsForm.on('formDataChange', function (event) {
					const args = event.args;
					const newValue = args.value;

					// Elements shown/hidden based on choice 'city' vs 'rd' 
					if (newValue.metaphor === 'city') {
						
						toggle_city_visibility('showComponent');
						toggle_city_color_visibility('showComponent');
						toggle_city_class_elements_visibility('showComponent');						
						toggle_city_original_visibility('hideComponent');
						toggle_rd_visibility('hideComponent');
					
						// Elements shown/hidden based on choice 'panels' vs 'bricks' vs 'original' vs 'floor' 
						if (newValue.city_building_type === 'panels') {
						
							toggle_city_original_visibility('hideComponent');
							toggle_city_panels_visibility('showComponent');
							toggle_city_bricks_visibility('hideComponent');
							
						} else if (newValue.city_building_type === 'floor') {
						
							toggle_city_original_visibility('hideComponent');
							toggle_city_panels_visibility('hideComponent');
							toggle_city_bricks_visibility('hideComponent');							
							
						} else if (newValue.city_building_type === 'original') {
							
							toggle_city_class_elements_visibility('hideComponent');
							toggle_city_color_visibility('hideComponent');
							toggle_city_original_visibility('showComponent');
							toggle_city_panels_visibility('hideComponent');
							toggle_city_bricks_visibility('hideComponent');				
							
						} else if (newValue.city_building_type === 'bricks') {
						
							toggle_city_original_visibility('hideComponent');
							toggle_city_panels_visibility('hideComponent');
							toggle_city_bricks_visibility('showComponent');
						}
						
					} else if (newValue.metaphor === 'rd') {
						
						toggle_city_visibility('hideComponent');
						toggle_city_color_visibility('hideComponent');
						toggle_city_class_elements_visibility('hideComponent');						
						toggle_city_original_visibility('hideComponent');
						toggle_city_panels_visibility('hideComponent');
						toggle_city_bricks_visibility('hideComponent');
						toggle_rd_visibility('showComponent');
					}					
				});			
				
				// Manually created vars to validate the data in the form
				const manual_input_name = settingsForm.jqxForm('getComponentByName', 'input.name');
				const manual_input_files = settingsForm.jqxForm('getComponentByName', 'input.files');
				const manual_city_package_color_start = settingsForm.jqxForm('getComponentByName', 'city.package.color_start');
				const manual_city_package_color_end = settingsForm.jqxForm('getComponentByName', 'city.package.color_end');
				const manual_city_class_color_start = settingsForm.jqxForm('getComponentByName', 'city.class.color_start');
				const manual_city_class_color_end = settingsForm.jqxForm('getComponentByName', 'city.class.color_end');
				const manual_city_class_color = settingsForm.jqxForm('getComponentByName', 'city.class.color');
				const manual_city_color_blue = settingsForm.jqxForm('getComponentByName', 'city.color.blue');
				const manual_city_color_aqua = settingsForm.jqxForm('getComponentByName', 'city.color.aqua');
				const manual_city_color_light_green = settingsForm.jqxForm('getComponentByName', 'city.color.light_green');
				const manual_city_color_dark_green = settingsForm.jqxForm('getComponentByName', 'city.color.dark_green');
				const manual_city_color_yellow = settingsForm.jqxForm('getComponentByName', 'city.color.yellow');
				const manual_city_color_orange = settingsForm.jqxForm('getComponentByName', 'city.color.orange');
				const manual_city_color_red = settingsForm.jqxForm('getComponentByName', 'city.color.red');
				const manual_city_color_pink = settingsForm.jqxForm('getComponentByName', 'city.color.pink');
				const manual_city_color_violet = settingsForm.jqxForm('getComponentByName', 'city.color.violet');
				const manual_city_color_light_grey = settingsForm.jqxForm('getComponentByName', 'city.color.light_grey');
				const manual_city_color_dark_grey = settingsForm.jqxForm('getComponentByName', 'city.color.dark_grey');
				const manual_city_color_white = settingsForm.jqxForm('getComponentByName', 'city.color.white');
				const manual_city_color_black = settingsForm.jqxForm('getComponentByName', 'city.color.black');
				const manual_rd_color_class = settingsForm.jqxForm('getComponentByName', 'rd.color.class');
				const manual_rd_color_data = settingsForm.jqxForm('getComponentByName', 'rd.color.data');
				const manual_rd_color_method = settingsForm.jqxForm('getComponentByName', 'rd.color.method');
				const manual_rd_color_namespace = settingsForm.jqxForm('getComponentByName', 'rd.color.namespace');
				
				// Validation rules
				$('#settingsForm').jqxValidator({
					hintType: "label",
					rules: [
						{ input: manual_input_name, message: 'Please enter an input.name!', action: 'keyup', position: 'top:0,15', rule: 'required' },
						{ input: manual_input_files, message: 'Please enter a valid URL!', action: 'keyup, focus, blur, change', position: 'top:0,15', rule: validate_url },
						{ input: manual_city_package_color_start, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_package_color_end, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_class_color_start, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_class_color_end, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_class_color, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex },
						{ input: manual_city_color_blue, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_aqua, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_light_green, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_dark_green, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_yellow, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_orange, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_red, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_pink, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_violet, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_light_grey, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_dark_grey, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_white, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_city_color_black, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_rd_color_class, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_rd_color_data, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_rd_color_method, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
						{ input: manual_rd_color_namespace, message: 'Please enter a valid HEX Color!', action: 'keyup, change', position: 'top:0,15', rule: validate_hex},
					]
				});
				
				// Reset Form Data
				const btn_reset = settingsForm.jqxForm('getComponentByName', 'resetButton');
				btn_reset.on('click', function () {
					reset_form(template, defaultValue);
					initial_load_city_optional();
				});
				
				// Submit Form Data 
				const btn_submit = settingsForm.jqxForm('getComponentByName', 'submitButton');
				btn_submit.on('click', function () {
					settingsForm.jqxValidator('validate');
					settingsForm.jqxForm('submit', "scripts/GenerationForm/generator-connector.php", "_self", 'POST');
				});
				
				// Cancel Form Data 
				const btn_cancel = settingsForm.jqxForm('getComponentByName', 'cancelButton');
				btn_cancel.on('click', function () {
					$("#settingsPopupWindowDiv").jqxWindow("close");
				});
            }
        });
	}
	
	// Validate input of url (jqxValidator has no support for this)
	function validate_url(input) {
		return (input.val().startsWith('http://') || input.val().startsWith('https://') || input.val().startsWith('file://'));
	}
	
	// Validate input of HEX colors (jqxValidator has no support for this)
	function validate_hex(input) {
		
		if (input.val()[0] !== '#')
			return false;
		
		if (input.val().length !== 4 && input.val().length !== 7)
			return false;
		
		for (let i = 1; i < input.val().length; i++) {
			if ((input.val()[i] >= 'a' && input.val()[i] <= 'f') || (input.val()[i] >= 'A' && input.val()[i] <= 'F') || (input.val()[i] >= 0 && input.val()[i] <= 9))
				{}
			else
				return false;			
		}
			
		return true;
	}
	
	// Reset the form
	function reset_form(template, defaultValue) {	
		
		$('#settingsForm').jqxForm('val', defaultValue);
	}
	
	// On first load show only the input fields for initially selected options (city && optional)
	function initial_load_city_optional() {

		toggle_city_class_elements_visibility('hideComponent');
		toggle_city_color_visibility('hideComponent');		
		toggle_city_visibility('showComponent');		
		toggle_city_original_visibility('showComponent');
		toggle_city_panels_visibility('hideComponent');
		toggle_city_bricks_visibility('hideComponent');
		toggle_rd_visibility('hideComponent');
	}
	
	// Toggle visibility of city.scheme && city.class options
	function toggle_city_class_elements_visibility(componentVisibility) {
		settingsForm.jqxForm(componentVisibility, 'city.scheme');
		settingsForm.jqxForm(componentVisibility, 'city.class_elements_mode');
		settingsForm.jqxForm(componentVisibility, 'city.class_elements_sort_mode_coarse');
		settingsForm.jqxForm(componentVisibility, 'city.class_elements_sort_mode_fine');
		settingsForm.jqxForm(componentVisibility, 'city.class_elements_sort_mode_fine_direction_reversed');
	}
	
	// Toggle visibility of city.color options
	function toggle_city_color_visibility(componentVisibility) {
		settingsForm.jqxForm(componentVisibility, 'city.color.blue');
		settingsForm.jqxForm(componentVisibility, 'city.color.aqua');
		settingsForm.jqxForm(componentVisibility, 'city.color.light_green');
		settingsForm.jqxForm(componentVisibility, 'city.color.dark_green');
		settingsForm.jqxForm(componentVisibility, 'city.color.yellow');
		settingsForm.jqxForm(componentVisibility, 'city.color.orange');
		settingsForm.jqxForm(componentVisibility, 'city.color.red');
		settingsForm.jqxForm(componentVisibility, 'city.color.pink');
		settingsForm.jqxForm(componentVisibility, 'city.color.violet');
		settingsForm.jqxForm(componentVisibility, 'city.color.light_grey');
		settingsForm.jqxForm(componentVisibility, 'city.color.dark_grey');
		settingsForm.jqxForm(componentVisibility, 'city.color.white');
		settingsForm.jqxForm(componentVisibility, 'city.color.black');
	}
	
	// Toggle visibility of certain city. options
	function toggle_city_visibility(componentVisibility) {
		settingsForm.jqxForm(componentVisibility, 'city.building_type');
		settingsForm.jqxForm(componentVisibility, 'city.show_building_base');
		settingsForm.jqxForm(componentVisibility, 'city.width_min');
		settingsForm.jqxForm(componentVisibility, 'city.height_min');
		settingsForm.jqxForm(componentVisibility, 'city.building.horizontal_margin');
		settingsForm.jqxForm(componentVisibility, 'city.building.horizontal_gap');
		settingsForm.jqxForm(componentVisibility, 'city.building.vertical_margin');
		settingsForm.jqxForm(componentVisibility, 'city.package.color_start');
		settingsForm.jqxForm(componentVisibility, 'city.package.color_end');
		settingsForm.jqxForm(componentVisibility, 'city.class.color_start');
		settingsForm.jqxForm(componentVisibility, 'city.class.color_end');
		settingsForm.jqxForm(componentVisibility, 'city.class.color');
	}
	
	// Toggle visibility of city.original options
	function toggle_city_original_visibility(componentVisibility) {
		settingsForm.jqxForm(componentVisibility, 'city.original_building_metric');
	}
	
	// Toggle visibility of city.panel options
	function toggle_city_panels_visibility(componentVisibility) {
		settingsForm.jqxForm(componentVisibility, 'city.panel.separator_mode');
		settingsForm.jqxForm(componentVisibility, 'city.panel.height_treshold_nos');
		settingsForm.jqxForm(componentVisibility, 'city.panel.height_unit');
		settingsForm.jqxForm(componentVisibility, 'city.panel.horizontal_margin');
		settingsForm.jqxForm(componentVisibility, 'city.panel.vertical_margin');
		settingsForm.jqxForm(componentVisibility, 'city.panel.vertical_gap');
		settingsForm.jqxForm(componentVisibility, 'city.panel.separator_height');
		settingsForm.jqxForm(componentVisibility, 'city.show_attributes_as_cylinders');
	}
	
	// Toggle visibility of city.brick options
	function toggle_city_bricks_visibility(componentVisibility) {
		settingsForm.jqxForm(componentVisibility, 'city.brick.layout');
		settingsForm.jqxForm(componentVisibility, 'city.brick.size');
		settingsForm.jqxForm(componentVisibility, 'city.brick.horizontal_margin');
		settingsForm.jqxForm(componentVisibility, 'city.brick.horizontal_gap');
		settingsForm.jqxForm(componentVisibility, 'city.brick.vertical_margin');
		settingsForm.jqxForm(componentVisibility, 'city.brick.vertical_gap');
	}
	
	// Toggle visibility of city.floor options (none yet)
	function toggle_city_floor_visibility(componentVisibility) {
		// no components for this choice yet - Anticipation of Change
	}
	
	// Toggle visibility of rd. options
	function toggle_rd_visibility(componentVisibility) {
		settingsForm.jqxForm(componentVisibility, 'rd.data_factor');
		settingsForm.jqxForm(componentVisibility, 'rd.method_factor');
		settingsForm.jqxForm(componentVisibility, 'rd.height');
		settingsForm.jqxForm(componentVisibility, 'rd.height_boost');
		settingsForm.jqxForm(componentVisibility, 'rd.height_multiplicator');
		settingsForm.jqxForm(componentVisibility, 'rd.ring_width');
		settingsForm.jqxForm(componentVisibility, 'rd.ring_width_md');
		settingsForm.jqxForm(componentVisibility, 'rd.ring_width_ad');
		settingsForm.jqxForm(componentVisibility, 'rd.min_area');
		settingsForm.jqxForm(componentVisibility, 'rd.namespace_transparency');
		settingsForm.jqxForm(componentVisibility, 'rd.class_transparency');
		settingsForm.jqxForm(componentVisibility, 'rd.method_transparency');
		settingsForm.jqxForm(componentVisibility, 'rd.data_transparency');
		settingsForm.jqxForm(componentVisibility, 'rd.color.class');
		settingsForm.jqxForm(componentVisibility, 'rd.color.data');
		settingsForm.jqxForm(componentVisibility, 'rd.color.method');
		settingsForm.jqxForm(componentVisibility, 'rd.color.namespace');
		settingsForm.jqxForm(componentVisibility, 'rd.method_disks');
		settingsForm.jqxForm(componentVisibility, 'rd.data_disks');
		settingsForm.jqxForm(componentVisibility, 'rd.method_type_mode');
	}
	
	function reset(){
	}
	
	function openSettingsPopUp(){
		$("#settingsPopupWindowDiv").jqxWindow("open");
	}
	
	function createSettingPopup(rootDiv){

		// Link to CSS File
		 let cssLink = document.createElement("link");
		cssLink.type = "text/css";
		cssLink.rel = "stylesheet";
		cssLink.href = "scripts/GenerationForm/GenerationFormController.css";
		 document.getElementsByTagName("head")[0].appendChild(cssLink);
		
		// The Window
		let settingsPopupWindowDiv = document.createElement("DIV");
		rootDiv.appendChild(settingsPopupWindowDiv);
		settingsPopupWindowDiv.id = "settingsPopupWindowDiv";
		
		// The Windows Title
		let settingsPopupTitleDiv = document.createElement("DIV");
		settingsPopupWindowDiv.appendChild(settingsPopupTitleDiv);
		settingsPopupTitleDiv.innerHTML = "Generate new visualization";
		
		// The Windows DIV
		let settingsPopupContentDiv = document.createElement("DIV");
		settingsPopupWindowDiv.appendChild(settingsPopupContentDiv);
		
		// The Form DIV
		let settingsForm = document.createElement("DIV");
		settingsForm.id = "settingsForm";
		settingsPopupContentDiv.appendChild(settingsForm);
	}
	
	return {
		initialize: initialize,
		activate: activate,
		
		openSettingsPopUp: openSettingsPopUp
	};
	
})();
	
