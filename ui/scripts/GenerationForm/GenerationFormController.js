var generationFormController = (function() {
	
	// Name Conversion Rules from "label" to "bind"
	// Example: city.building_type --> city_building_type	
	// Most options in the file settings.properties contain a "."
	// If "." is used in "bind" the ui does not load ("." is interpreted as a function call)	
	
	// width for DropDownLists = 200px
	// to align simple InputFields with the DDLs, choose 200px-6px = 194px
	
	// Order of Options:
	// input.name
	// input.files
	// metaphor
	// city.building_type
	// city.scheme
	// city.class_elements_mode
	// city.class_elements_sort_mode_coarse
	// city.class_elements_sort_mode_fine
	// city.class_elements_sort_mode_fine_direction_reversed
	// city.show_building_base
	// 		city_blank_node
	// city.show_attributes_as_cylinders
	// city.brick.layout
	// city.brick.size
	// city.brick.horizontal_margin
	// city.brick.horizontal_gap
	// city.brick.vertical_margin
	// city.brick.vertical_gap
	// city.panel.separator_mode
	// city.panel.height_treshold_nos
	// city.panel.height_unit
	// city.panel.horizontal_margin
	// city.panel.vertical_margin
	// city.panel.vertical_gap
	// city.panel.separator_height
	// city.original_building_metric
	// city.width_min
	// city.height_min
	// city.building.horizontal_margin
	// city.building.horizontal_gap
	// city.building.vertical_margin
	// city.package.color_start
	// city.package.color_end
	// city.class.color_start
	// city.class.color_end
	// city.class.color
	// city.color.blue
	// city.color.aqua
	// city.color.light_green
	// city.color.dark_green
	// city.color.yellow
	// city.color.orange
	// city.color.red
	// city.color.pink
	// city.color.violet
	// city.color.light_grey
	// city.color.dark_grey
	// city.color.white
	// city.color.black
	// rd.data_factor
	// rd.method_factor
	// rd.height
	// rd.height_boost
	// rd.height_multiplicator
	// rd.ring_width
	// rd.ring_width_md
	// rd.ring_width_ad
	// rd.min_area
	// rd.namespace_transparency
	// rd.class_transparency
	// rd.method_transparency
	// rd.data_transparency
	// rd.color.class
	// rd.color.data
	// rd.color.method
	// rd.color.namespace
	// rd.method_disks
	// rd.data_disks
	// rd.method_type_mode
	
	var logObjectMap = new Map();
	
	var controllerConfig = {
		createHeadSection: true
	}
	
	function initialize(setupConfig){
		application.transferConfigParams(setupConfig, controllerConfig);
	}
	
	function activate(rootDiv){
		
		createSettingPopup(rootDiv);
		
		$("#settingsPopupWindowDiv").jqxWindow({ theme: "metro", width: 650, height: 950, isModal: true, autoOpen: false, resizable: true, cancelButton: $("#defaultButton"), initContent: function() {
		   
				// Add Form Inputs and Labels here
 				var template = [
					
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
							{ value: 'false' },
							{ value: 'true'}
						],
						info:'If set true, the order of the sorting, defined in class_elements_sort_mode_fine is reversed. &#013;If class_elements_sort_mode_fine is set to scheme, a secondary sorting is performed to place methods with high numbers of statements at the bottom. &#013;This behaviour is not influenced by this switch.',
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
						bind: 'city_blank_node',
						name: 'city_blank_node',
						type: 'blank',
						rowHeight: '25px'
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
						bind: 'rd_method_factor',
						name: 'rd.method_factor',
						type: 'number',
						label: 'rd.method_factor',
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
						bind: 'rd_height_boost',
						name: 'rd.height_boost',
						type: 'number',
						label: 'rd.height_boost',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						padding: {left: 8, top: 0, bottom: 0, right: 8}
					},					
					{
						bind: 'rd_height_multiplicator',
						name: 'rd.height_multiplicator',
						type: 'number',
						label: 'rd.height_multiplicator',
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
						bind: 'rd_ring_width_md',
						name: 'rd.ring_width_md',
						type: 'number',
						label: 'rd.ring_width_md',
						labelAlign: 'right',
						labelWidth: '325px',
						align: 'left',
						width: '200px',
						info:'Sets the ring width of the method disks &#013;Only relevant if disk of type FAMIX.Method exist',
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
							{ value: 'false' },
							{ value: 'true'}
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
							{ value: 'false' },
							{ value: 'true'}
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
							{ value: 'false' },
							{ value: 'true'}
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
								name: 'defaultButton',
								type: 'button',
								text: 'Load Default',
								width: '262px',
								columnWidth: '50%'
							},
							{
								name: 'submitButton',
								type: 'button',
								text: 'Submit',
								width: '263px',
								columnWidth: '50%'
							}             
						]
					}
					
				];
				
				// Default Values 				
				var defaultValue = {
					input_name: 'default',
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
					city_package_color_end: '#F0F0F0',
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
				
				// generate Form 
				var settingsForm = $('#settingsForm');
				settingsForm.jqxForm({
					template: template,
					value: defaultValue,
					padding: { left: 5, top: 5, right: 5, bottom: 5 }
					
				});	

				// On first load show only the input fields for initially selected options (city, optional)
				initial_load_city_optional();
				
				// formDataChange Event
				settingsForm.on('formDataChange', function (event) {
					var args = event.args;
					var newValue = args.value;
					var previousValue = args.previousValue;		

					// Elements shown/hidden based on choice 'city' vs 'rd' 
					if (newValue.metaphor == 'city') {
						show_city();
						hide_city_original();
						hide_rd();
					
						// Elements shown/hidden based on choice 'panels' vs 'bricks' vs 'original' vs 'floor' 
						if (newValue.city_building_type == 'panels') {	
						
							hide_city_original();
							show_city_panels();
							hide_city_bricks();
							
						} else if (newValue.city_building_type == 'floor') {
						
							hide_city_original();
							hide_city_panels();
							hide_city_bricks();								
							
						} else if (newValue.city_building_type == 'original') {
							
							$('#settingsForm').jqxForm('hideComponent', 'city.scheme');
							$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_mode');
							$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_sort_mode_coarse');
							$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_sort_mode_fine');
							$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_sort_mode_fine_direction_reversed');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.blue');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.aqua');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.light_green');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.dark_green');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.yellow');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.orange');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.red');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.pink');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.violet');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.light_grey');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.dark_grey');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.white');
							$('#settingsForm').jqxForm('hideComponent', 'city.color.black');
							show_city_original();
							hide_city_panels();	
							hide_city_bricks();					
							
						} else if (newValue.city_building_type == 'bricks') {
						
							hide_city_original();
							hide_city_panels();			
							show_city_bricks();
						}
						
					} else if (newValue.metaphor == 'rd') {
						
						hide_city();
						hide_city_original();
						hide_city_panels();	
						hide_city_bricks();
						show_rd();
					}					
				});			
				
				// Manually created vars to access the data in the form
				var manual_input_name = settingsForm.jqxForm('getComponentByName', 'input.name');
				var manual_input_files = settingsForm.jqxForm('getComponentByName', 'input.files');
				var manual_city_package_color_start = settingsForm.jqxForm('getComponentByName', 'city.package.color_start');
				var manual_city_package_color_end = settingsForm.jqxForm('getComponentByName', 'city.package.color_end');
				var manual_city_class_color_start = settingsForm.jqxForm('getComponentByName', 'city.class.color_start');
				var manual_city_class_color_end = settingsForm.jqxForm('getComponentByName', 'city.class.color_end');
				var manual_city_class_color = settingsForm.jqxForm('getComponentByName', 'city.class.color');
				var manual_city_color_blue = settingsForm.jqxForm('getComponentByName', 'city.color.blue');
				var manual_city_color_aqua = settingsForm.jqxForm('getComponentByName', 'city.color.aqua');
				var manual_city_color_light_green = settingsForm.jqxForm('getComponentByName', 'city.color.light_green');
				var manual_city_color_dark_green = settingsForm.jqxForm('getComponentByName', 'city.color.dark_green');
				var manual_city_color_yellow = settingsForm.jqxForm('getComponentByName', 'city.color.yellow');
				var manual_city_color_orange = settingsForm.jqxForm('getComponentByName', 'city.color.orange');
				var manual_city_color_red = settingsForm.jqxForm('getComponentByName', 'city.color.red');
				var manual_city_color_pink = settingsForm.jqxForm('getComponentByName', 'city.color.pink');
				var manual_city_color_violet = settingsForm.jqxForm('getComponentByName', 'city.color.violet');
				var manual_city_color_light_grey = settingsForm.jqxForm('getComponentByName', 'city.color.light_grey');
				var manual_city_color_dark_grey = settingsForm.jqxForm('getComponentByName', 'city.color.dark_grey');
				var manual_city_color_white = settingsForm.jqxForm('getComponentByName', 'city.color.white');
				var manual_city_color_black = settingsForm.jqxForm('getComponentByName', 'city.color.black');
				var manual_rd_color_class = settingsForm.jqxForm('getComponentByName', 'rd.color.class');
				var manual_rd_color_data = settingsForm.jqxForm('getComponentByName', 'rd.color.data');
				var manual_rd_color_method = settingsForm.jqxForm('getComponentByName', 'rd.color.method');
				var manual_rd_color_namespace = settingsForm.jqxForm('getComponentByName', 'rd.color.namespace');
				
				// Validation rules
				$('#settingsForm').jqxValidator({
					hintType: "label",
					rules: [
						{ input: manual_input_name, message: 'Please enter an input.name!', action: 'keyup', position: 'top:0,15', rule: 'required' },
						{ input: manual_input_files, message: 'Please enter the path to your input.files!', action: 'keyup, focus, blur, valuechanged', position: 'top:0,15', rule: 'required' },
						{ input: manual_city_package_color_start, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_package_color_end, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_class_color_start, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_class_color_end, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_class_color, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_blue, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_aqua, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_light_green, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_dark_green, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_yellow, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_orange, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_red, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_pink, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_violet, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_light_grey, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_dark_grey, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_white, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_city_color_black, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_rd_color_class, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_rd_color_data, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_rd_color_method, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
						{ input: manual_rd_color_namespace, message: 'Please enter a HEX Color!', action: 'keyup, valuechanged', position: 'top:0,15', rule: 'length=7,7',},
					]
				});
				
				// Load Default Values 
				var btn_default = settingsForm.jqxForm('getComponentByName', 'defaultButton');
				btn_default.on('click', function () {
										
				});
				
				// Submit Form Data 
				var btn_submit = settingsForm.jqxForm('getComponentByName', 'submitButton');
				btn_submit.on('click', function () {
					$('#settingsForm').jqxValidator('validate');
					settingsForm.jqxForm('submit', "http://" + BACKEND +":8080", "_self", 'POST');
				});
            }
        });
		
		events.log.info.subscribe(addLogObject);
		events.log.warning.subscribe(addLogObject);
		events.log.error.subscribe(addLogObject);
		events.log.action.subscribe(addLogObject);
		events.log.event.subscribe(addLogObject);
		events.log.manipulation.subscribe(addLogObject);
	}
	
	// On first load show only the input fields for initially selected options (city, optional)
	function initial_load_city_optional() {

		$('#settingsForm').jqxForm('showComponent', 'city.building_type');
		$('#settingsForm').jqxForm('hideComponent', 'city.scheme');
		$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_mode');
		$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_sort_mode_coarse');
		$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_sort_mode_fine');
		$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_sort_mode_fine_direction_reversed');
		$('#settingsForm').jqxForm('showComponent', 'city.show_building_base');						
		$('#settingsForm').jqxForm('showComponent', 'city.original_building_metric');
		$('#settingsForm').jqxForm('showComponent', 'city.width_min');
		$('#settingsForm').jqxForm('showComponent', 'city.height_min');
		$('#settingsForm').jqxForm('showComponent', 'city.building.horizontal_margin');
		$('#settingsForm').jqxForm('showComponent', 'city.building.horizontal_gap');
		$('#settingsForm').jqxForm('showComponent', 'city.building.vertical_margin');
		$('#settingsForm').jqxForm('showComponent', 'city.package.color_start');
		$('#settingsForm').jqxForm('showComponent', 'city.package.color_end');
		$('#settingsForm').jqxForm('showComponent', 'city.class.color_start');
		$('#settingsForm').jqxForm('showComponent', 'city.class.color_end');
		$('#settingsForm').jqxForm('showComponent', 'city.class.color');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.blue');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.aqua');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.light_green');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.dark_green');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.yellow');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.orange');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.red');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.pink');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.violet');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.light_grey');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.dark_grey');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.white');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.black');	
		hide_rd();
		hide_city_panels();
		hide_city_bricks();
	}
	
	function show_city() {
		$('#settingsForm').jqxForm('showComponent', 'city.building_type');
		$('#settingsForm').jqxForm('showComponent', 'city.scheme');
		$('#settingsForm').jqxForm('showComponent', 'city.class_elements_mode');
		$('#settingsForm').jqxForm('showComponent', 'city.class_elements_sort_mode_coarse');
		$('#settingsForm').jqxForm('showComponent', 'city.class_elements_sort_mode_fine');
		$('#settingsForm').jqxForm('showComponent', 'city.class_elements_sort_mode_fine_direction_reversed');
		$('#settingsForm').jqxForm('showComponent', 'city.show_building_base');	
		$('#settingsForm').jqxForm('showComponent', 'city.width_min');
		$('#settingsForm').jqxForm('showComponent', 'city.height_min');
		$('#settingsForm').jqxForm('showComponent', 'city.building.horizontal_margin');
		$('#settingsForm').jqxForm('showComponent', 'city.building.horizontal_gap');
		$('#settingsForm').jqxForm('showComponent', 'city.building.vertical_margin');
		$('#settingsForm').jqxForm('showComponent', 'city.package.color_start');
		$('#settingsForm').jqxForm('showComponent', 'city.package.color_end');
		$('#settingsForm').jqxForm('showComponent', 'city.class.color_start');
		$('#settingsForm').jqxForm('showComponent', 'city.class.color_end');
		$('#settingsForm').jqxForm('showComponent', 'city.class.color');
		$('#settingsForm').jqxForm('showComponent', 'city.color.blue');
		$('#settingsForm').jqxForm('showComponent', 'city.color.aqua');
		$('#settingsForm').jqxForm('showComponent', 'city.color.light_green');
		$('#settingsForm').jqxForm('showComponent', 'city.color.dark_green');
		$('#settingsForm').jqxForm('showComponent', 'city.color.yellow');
		$('#settingsForm').jqxForm('showComponent', 'city.color.orange');
		$('#settingsForm').jqxForm('showComponent', 'city.color.red');
		$('#settingsForm').jqxForm('showComponent', 'city.color.pink');
		$('#settingsForm').jqxForm('showComponent', 'city.color.violet');
		$('#settingsForm').jqxForm('showComponent', 'city.color.light_grey');
		$('#settingsForm').jqxForm('showComponent', 'city.color.dark_grey');
		$('#settingsForm').jqxForm('showComponent', 'city.color.white');
		$('#settingsForm').jqxForm('showComponent', 'city.color.black');
	}
	
	function hide_city() {
		$('#settingsForm').jqxForm('hideComponent', 'city.building_type');
		$('#settingsForm').jqxForm('hideComponent', 'city.scheme');
		$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_mode');
		$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_sort_mode_coarse');
		$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_sort_mode_fine');
		$('#settingsForm').jqxForm('hideComponent', 'city.class_elements_sort_mode_fine_direction_reversed');
		$('#settingsForm').jqxForm('hideComponent', 'city.show_building_base');
		$('#settingsForm').jqxForm('hideComponent', 'city.width_min');
		$('#settingsForm').jqxForm('hideComponent', 'city.height_min');
		$('#settingsForm').jqxForm('hideComponent', 'city.building.horizontal_margin');
		$('#settingsForm').jqxForm('hideComponent', 'city.building.horizontal_gap');
		$('#settingsForm').jqxForm('hideComponent', 'city.building.vertical_margin');
		$('#settingsForm').jqxForm('hideComponent', 'city.package.color_start');
		$('#settingsForm').jqxForm('hideComponent', 'city.package.color_end');
		$('#settingsForm').jqxForm('hideComponent', 'city.class.color_start');
		$('#settingsForm').jqxForm('hideComponent', 'city.class.color_end');
		$('#settingsForm').jqxForm('hideComponent', 'city.class.color');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.blue');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.aqua');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.light_green');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.dark_green');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.yellow');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.orange');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.red');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.pink');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.violet');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.light_grey');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.dark_grey');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.white');
		$('#settingsForm').jqxForm('hideComponent', 'city.color.black');	
	}
	
	function show_city_original() {
		$('#settingsForm').jqxForm('showComponent', 'city.original_building_metric');
	}
	
	function hide_city_original() {
		$('#settingsForm').jqxForm('hideComponent', 'city.original_building_metric');
	}
	
	function show_city_panels() {
		$('#settingsForm').jqxForm('showComponent', 'city.panel.separator_mode');
		$('#settingsForm').jqxForm('showComponent', 'city.panel.height_treshold_nos');
		$('#settingsForm').jqxForm('showComponent', 'city.panel.height_unit');
		$('#settingsForm').jqxForm('showComponent', 'city.panel.horizontal_margin');
		$('#settingsForm').jqxForm('showComponent', 'city.panel.vertical_margin');
		$('#settingsForm').jqxForm('showComponent', 'city.panel.vertical_gap');
		$('#settingsForm').jqxForm('showComponent', 'city.panel.separator_height');
		$('#settingsForm').jqxForm('showComponent', 'city.show_attributes_as_cylinders');
	}
	
	function hide_city_panels() {
		$('#settingsForm').jqxForm('hideComponent', 'city.panel.separator_mode');
		$('#settingsForm').jqxForm('hideComponent', 'city.panel.height_treshold_nos');
		$('#settingsForm').jqxForm('hideComponent', 'city.panel.height_unit');
		$('#settingsForm').jqxForm('hideComponent', 'city.panel.horizontal_margin');
		$('#settingsForm').jqxForm('hideComponent', 'city.panel.vertical_margin');
		$('#settingsForm').jqxForm('hideComponent', 'city.panel.vertical_gap');
		$('#settingsForm').jqxForm('hideComponent', 'city.panel.separator_height');
		$('#settingsForm').jqxForm('hideComponent', 'city.show_attributes_as_cylinders');
	}
	
	function show_city_bricks() {
		$('#settingsForm').jqxForm('showComponent', 'city.brick.layout');
		$('#settingsForm').jqxForm('showComponent', 'city.brick.size');
		$('#settingsForm').jqxForm('showComponent', 'city.brick.horizontal_margin');
		$('#settingsForm').jqxForm('showComponent', 'city.brick.horizontal_gap');
		$('#settingsForm').jqxForm('showComponent', 'city.brick.vertical_margin');
		$('#settingsForm').jqxForm('showComponent', 'city.brick.vertical_gap');	
	}
	
	function hide_city_bricks() {
		$('#settingsForm').jqxForm('hideComponent', 'city.brick.layout');
		$('#settingsForm').jqxForm('hideComponent', 'city.brick.size');
		$('#settingsForm').jqxForm('hideComponent', 'city.brick.horizontal_margin');
		$('#settingsForm').jqxForm('hideComponent', 'city.brick.horizontal_gap');
		$('#settingsForm').jqxForm('hideComponent', 'city.brick.vertical_margin');
		$('#settingsForm').jqxForm('hideComponent', 'city.brick.vertical_gap');	
	}
	
	function show_city_floor() {
	}
	
	function hide_city_floor() {
	}
	
	function show_rd() {
		$('#settingsForm').jqxForm('showComponent', 'rd.data_factor');
		$('#settingsForm').jqxForm('showComponent', 'rd.method_factor');
		$('#settingsForm').jqxForm('showComponent', 'rd.height');
		$('#settingsForm').jqxForm('showComponent', 'rd.height_boost');
		$('#settingsForm').jqxForm('showComponent', 'rd.height_multiplicator');
		$('#settingsForm').jqxForm('showComponent', 'rd.ring_width');
		$('#settingsForm').jqxForm('showComponent', 'rd.ring_width_md');
		$('#settingsForm').jqxForm('showComponent', 'rd.ring_width_ad');
		$('#settingsForm').jqxForm('showComponent', 'rd.min_area');
		$('#settingsForm').jqxForm('showComponent', 'rd.namespace_transparency');
		$('#settingsForm').jqxForm('showComponent', 'rd.class_transparency');
		$('#settingsForm').jqxForm('showComponent', 'rd.method_transparency');
		$('#settingsForm').jqxForm('showComponent', 'rd.data_transparency');
		$('#settingsForm').jqxForm('showComponent', 'rd.color.class');
		$('#settingsForm').jqxForm('showComponent', 'rd.color.data');
		$('#settingsForm').jqxForm('showComponent', 'rd.color.method');
		$('#settingsForm').jqxForm('showComponent', 'rd.color.namespace');
		$('#settingsForm').jqxForm('showComponent', 'rd.method_disks');
		$('#settingsForm').jqxForm('showComponent', 'rd.data_disks');
		$('#settingsForm').jqxForm('showComponent', 'rd.method_type_mode');
		$('#settingsForm').jqxForm('hideComponent', 'city_blank_node');
	}
	
	function hide_rd() {		
		$('#settingsForm').jqxForm('hideComponent', 'rd.data_factor');
		$('#settingsForm').jqxForm('hideComponent', 'rd.method_factor');
		$('#settingsForm').jqxForm('hideComponent', 'rd.height');
		$('#settingsForm').jqxForm('hideComponent', 'rd.height_boost');
		$('#settingsForm').jqxForm('hideComponent', 'rd.height_multiplicator');
		$('#settingsForm').jqxForm('hideComponent', 'rd.ring_width');
		$('#settingsForm').jqxForm('hideComponent', 'rd.ring_width_md');
		$('#settingsForm').jqxForm('hideComponent', 'rd.ring_width_ad');
		$('#settingsForm').jqxForm('hideComponent', 'rd.min_area');
		$('#settingsForm').jqxForm('hideComponent', 'rd.namespace_transparency');
		$('#settingsForm').jqxForm('hideComponent', 'rd.class_transparency');
		$('#settingsForm').jqxForm('hideComponent', 'rd.method_transparency');
		$('#settingsForm').jqxForm('hideComponent', 'rd.data_transparency');
		$('#settingsForm').jqxForm('hideComponent', 'rd.color.class');
		$('#settingsForm').jqxForm('hideComponent', 'rd.color.data');
		$('#settingsForm').jqxForm('hideComponent', 'rd.color.method');
		$('#settingsForm').jqxForm('hideComponent', 'rd.color.namespace');
		$('#settingsForm').jqxForm('hideComponent', 'rd.method_disks');
		$('#settingsForm').jqxForm('hideComponent', 'rd.data_disks');
		$('#settingsForm').jqxForm('hideComponent', 'rd.method_type_mode');	
		$('#settingsForm').jqxForm('showComponent', 'city_blank_node');
	}
	
	function reset(){
	}
	
	function openSettingsPopUp(){
		$("#settingsPopupWindowDiv").jqxWindow("open");
	}
	
	function addLogObject(logObject){																
	}
	
	function createSettingPopup(rootDiv){

		// Link to CSS File
		var cssLink = document.createElement("link");
		cssLink.type = "text/css";
		cssLink.rel = "stylesheet";
		cssLink.href = "scripts/GenerationForm/GenerationFormController.css";
		document.getElementsByTagName("head")[0].appendChild(cssLink);
		
		// The Window
		var settingsPopupWindowDiv = document.createElement("DIV");
		rootDiv.appendChild(settingsPopupWindowDiv);
		settingsPopupWindowDiv.id = "settingsPopupWindowDiv";
		
		// The Windows Title
		var settingsPopupTitleDiv = document.createElement("DIV");
		settingsPopupWindowDiv.appendChild(settingsPopupTitleDiv);
		settingsPopupTitleDiv.innerHTML = "New Visualization";
		
		// The Windows DIV
		var settingsPopupContentDiv = document.createElement("DIV");
		settingsPopupWindowDiv.appendChild(settingsPopupContentDiv);
		
		// The Form DIV
		var settingsForm = document.createElement("DIV");
		settingsForm.id = "settingsForm";
		settingsPopupContentDiv.appendChild(settingsForm);
	}
	
	return {
		initialize: initialize,
		activate: activate,
		
		openSettingsPopUp: openSettingsPopUp
	};
	
})();
	
