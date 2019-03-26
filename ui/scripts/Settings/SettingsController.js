var settingsController = (function() {
	
	// Conversion of Setting_Name to ID:
	// city.building_type --> city_building_type
	// replace . with _ ("." in ID makes the ID not work)
	
	var logObjectMap = new Map();
	
	var controllerConfig = {
		createHeadSection: true
	}
	
	function initialize(setupConfig){																// finished!
		application.transferConfigParams(setupConfig, controllerConfig);
	}
	
	function activate(rootDiv){
		
		createSettingPopup(rootDiv);
		
		$("#settingsPopupWindowDiv").jqxWindow({ theme: "metro", width: 650, height: 950, isModal: true, autoOpen: false, resizable: true, cancelButton: $("#cancelSetChanges"), initContent: function() {
		   
				// Add Form Inputs and Labels here
 				var template = [
					
					// Independent Options 					
					{
						bind: 'input_name',
						name: 'input_name',
						type: 'text',
						label: 'input.name:',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'input_files',
						name: 'input_files',
						type: 'text',
						label: 'input.files:',
						//required: true,
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'output_format',
						name: 'output_format',
						type: 'option',
						label: 'output.format:',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'aframe' },
							{ value: 'x3d'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'metaphor',
						name: 'metaphor',
						type: 'option',
						label: 'metaphor:',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'rd' },
							{ value: 'city'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'output_path',
						name: 'output_path',
						type: 'text',
						label: 'output.path:',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'database_name',
						name: 'database_name',
						type: 'text',
						label: 'database_name:',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},						
					// only available if output.format == 'x3d' 
					{
						bind: 'convert_to_multipart',
						name: 'convert_to_multipart',
						type: 'option',
						label: 'convert_to_multipart:',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'false' },
							{ value: 'true'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},				
					{
						type: 'blank',
						rowHeight: '25px'
					},
					
					// City Options 
					{
						bind: 'city_building_type',
						name: 'city_building_type',
						type: 'option',
						label: 'city.building_type:',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'original' },
							{ value: 'panels'},
							{ value: 'bricks'},
							{ value: 'floor'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_scheme',
						name: 'city_scheme',
						type: 'option',
						label: 'city.scheme:',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'types' },
							{ value: 'visibility'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_class_elements_mode',
						name: 'city_class_elements_mode',
						type: 'option',
						label: 'city.class_elements_mode:',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'methods_and_attributes' },
							{ value: 'methods_only'},
							{ value: 'attributes_only'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_class_elements_sort_mode_coarse',
						name: 'city_class_elements_sort_mode_coarse',
						type: 'option',
						label: 'city.class_elements_sort_mode_coarse:',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'methods_first' },
							{ value: 'unsorted'},
							{ value: 'attributes_first'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_class_elements_sort_mode_fine',
						name: 'city_class_elements_sort_mode_fine',
						type: 'option',
						label: 'city.class_elements_sort_mode_fine:',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'scheme' },
							{ value: 'unsorted'},
							{ value: 'alphabetically'},
							{ value: 'nos'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_class_elements_sort_mode_fine_direction_reversed',
						name: 'city_class_elements_sort_mode_fine_direction_reversed',
						type: 'option',
						label: 'city.class_elements_sort_mode_fine_direction_reversed',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'false' },
							{ value: 'true'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_show_building_base',
						name: 'city_show_building_base',
						type: 'option',
						label: 'city.show_building_base',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'true' },
							{ value: 'false'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},						
					{
						bind: 'city_blank_node',
						name: 'city_blank_node',
						type: 'blank',
						rowHeight: '25px'
					},					
					{
						bind: 'city_show_attributes_as_cylinders',
						name: 'city_show_attributes_as_cylinders',
						type: 'option',
						label: 'city.show_attributes_as_cylinders',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'true' },
							{ value: 'false'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_brick_layout',
						name: 'city_brick_layout',
						type: 'option',
						label: 'city.brick.layout:',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'progressive' },
							{ value: 'straight'},
							{ value: 'balanced'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_brick_size',
						name: 'city_brick_size',
						type: 'number',
						label: 'city.brick.size',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_brick_horizontal_margin',
						name: 'city_brick_horizontal_margin',
						type: 'number',
						label: 'city.brick.horizontal_margin',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_brick_horizontal_gap',
						name: 'city_brick_horizontal_gap',
						type: 'number',
						label: 'city.brick.horizontal_gap',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_brick_vertical_margin',
						name: 'city_brick_vertical_margin',
						type: 'number',
						label: 'city.brick.vertical_margin',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_brick_vertical_gap',
						name: 'city_brick_vertical_gap',
						type: 'number',
						label: 'city.brick.vertical_gap',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_panel_separator_mode',
						name: 'city_panel_separator_mode',
						type: 'option',
						label: 'city.panel.separator_mode :',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'separator' },
							{ value: 'none'},
							{ value: 'gap'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_panel_height_treshold_nos',
						name: 'city_panel_height_treshold_nos',
						type: 'option',
						label: 'city.panel.height_treshold_nos :',
						labelWidth: '325px',
						width: '250px',
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
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_panel_height_unit',
						name: 'city_panel_height_unit',
						type: 'number',
						label: 'city.panel.height_unit',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_panel_horizontal_margin',
						name: 'city_panel_horizontal_margin',
						type: 'number',
						label: 'city.panel.horizontal_margin',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_panel_vertical_margin',
						name: 'city_panel_vertical_margin',
						type: 'number',
						label: 'city.panel.vertical_margin',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_panel_vertical_gap',
						name: 'city_panel_vertical_gap',
						type: 'number',
						label: 'city.panel.vertical_gap',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_panel_separator_height',
						name: 'city_panel_separator_height',
						type: 'number',
						label: 'city.panel.separator_height',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_original_building_metric',
						name: 'city_original_building_metric',
						type: 'option',
						label: 'city.original_building_metric:',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'none' },
							{ value: 'nos'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_width_min',
						name: 'city_width_min',
						type: 'number',
						label: 'city.width_min',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_height_min',
						name: 'city_height_min',
						type: 'number',
						label: 'city.height_min',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_building_horizontal_margin',
						name: 'city_building_horizontal_margin',
						type: 'number',
						label: 'city.building.horizontal_margin',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_building_horizontal_gap',
						name: 'city_building_horizontal_gap',
						type: 'number',
						label: 'city.building.horizontal_gap',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_building_vertical_margin',
						name: 'city_building_vertical_margin',
						type: 'number',
						label: 'city.building.vertical_margin',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_package_color_start',
						name: 'city_package_color_start',
						type: 'text',
						label: 'city.package.color_start',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_package_color_end',
						name: 'city_package_color_end',
						type: 'text',
						label: 'city.package.color_end',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_class_color_start',
						name: 'city_class_color_start',
						type: 'text',
						label: 'city.class.color_start',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_class_color_end',
						name: 'city_class_color_end',
						type: 'text',
						label: 'city.class.color_end',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_class_color',
						name: 'city_class_color',
						type: 'text',
						label: 'city.class.color',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_blue',
						name: 'city_color_blue',
						type: 'text',
						label: 'city.color_blue',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_aqua',
						name: 'city_color_aqua',
						type: 'text',
						label: 'city.color_aqua',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_light_green',
						name: 'city_color_light_green',
						type: 'text',
						label: 'city.color_light_green',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_dark_green',
						name: 'city_color_dark_green',
						type: 'text',
						label: 'city.color_dark_green',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_yellow',
						name: 'city_color_yellow',
						type: 'text',
						label: 'city.color_yellow',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_orange',
						name: 'city_color_orange',
						type: 'text',
						label: 'city.color_orange',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_red',
						name: 'city_color_red',
						type: 'text',
						label: 'city.color_red',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_pink',
						name: 'city_color_pink',
						type: 'text',
						label: 'city.color_pink',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_violet',
						name: 'city_color_violet',
						type: 'text',
						label: 'city.color_violet',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_light_grey',
						name: 'city_color_light_grey',
						type: 'text',
						label: 'city.color_light_grey',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_dark_grey',
						name: 'city_color_dark_grey',
						type: 'text',
						label: 'city.color_dark_grey',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_white',
						name: 'city_color_white',
						type: 'text',
						label: 'city.color_white',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'city_color_black',
						name: 'city_color_black',
						type: 'text',
						label: 'city.color_black',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},
					
					// Recursive Disk Options 					
					{
						bind: 'rd_data_factor',
						name: 'rd_data_factor',
						type: 'number',
						label: 'rd.data_factor',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_method_factor',
						name: 'rd_method_factor',
						type: 'number',
						label: 'rd.method_factor',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_height',
						name: 'rd_height',
						type: 'number',
						label: 'rd.height',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_height_boost',
						name: 'rd_height_boost',
						type: 'number',
						label: 'rd.height_boost',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_height_multiplicator',
						name: 'rd_height_multiplicator',
						type: 'number',
						label: 'rd.height_multiplicator',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_ring_width',
						name: 'rd_ring_width',
						type: 'number',
						label: 'rd.ring_width',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_ring_width_md',
						name: 'rd_ring_width_md',
						type: 'number',
						label: 'rd.ring_width_md',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_ring_width_ad',
						name: 'rd_ring_width_ad',
						type: 'number',
						label: 'rd.ring_width_ad',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_min_area',
						name: 'rd_min_area',
						type: 'number',
						label: 'rd.min_area',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_namespace_transparency',
						name: 'rd_namespace_transparency',
						type: 'number',
						label: 'rd.namespace_transparency',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_class_transparency',
						name: 'rd_class_transparency',
						type: 'number',
						label: 'rd.class_transparency',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_method_transparency',
						name: 'rd_method_transparency',
						type: 'number',
						label: 'rd.method_transparency',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_data_transparency',
						name: 'rd_data_transparency',
						type: 'number',
						label: 'rd.data_transparency',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '250px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_color_class',
						name: 'rd_color_class',
						type: 'text',
						label: 'rd.color.class',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_color_data',
						name: 'rd_color_data',
						type: 'text',
						label: 'rd.color.data',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_color_method',
						name: 'rd_color_method',
						type: 'text',
						label: 'rd.color.method',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_color_namespace',
						name: 'rd_color_namespace',
						type: 'text',
						label: 'rd.color.namespace',
						labelPosition: 'left',
						labelWidth: '325px',
						align: 'left',
						width: '244px',
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_method_disks',
						name: 'rd_method_disks',
						type: 'option',
						label: 'rd.method_disks',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'false' },
							{ value: 'true'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_data_disks',
						name: 'rd_data_disks',
						type: 'option',
						label: 'rd.data_disks',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'false' },
							{ value: 'true'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},					
					{
						bind: 'rd_method_type_mode',
						name: 'rd_method_type_mode',
						type: 'option',
						label: 'rd.method_type_mode',
						labelWidth: '325px',
						width: '250px',
						component: 'jqxDropDownList',
						options: [
							{ value: 'false' },
							{ value: 'true'}
						],
						padding: {left: 0, top: 0, bottom: 0, right: 0}
					},
					{
						type: 'blank',
						rowHeight: '25px',
					},
					{
						name: 'submitButton',
						type: 'button',
						text: 'Submit',
						align: 'right',
						padding: {left: 20, top: 5, bottom: 5, right: 20}
					}
					
				];
				
				// Default Values 				
				var defaultValue = {
					input_name: 'default',
					output_format: 'aframe',
					metaphor: 'city',
					output_path: '/var/lib/jetty/output/',
					convert_to_multipart: false,
					database_name: '/var/lib/jetty/databases/graph.db',
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
					padding: { left: 10, top: 10, right: 10, bottom: 10 }
					
				});	

				// On first load show only the input fields for initially selected options (aframe, city, optional)
				initial_load_aframe_city_optional();
				
				// formDataChange Event 
				settingsForm.on('formDataChange', function (event) {
					var args = event.args;
					var newValue = args.value;
					var previousValue = args.previousValue;		

					// Elements shown/hidden based on choice 'city' vs 'rd' 
					if (newValue.metaphor == 'city') {
					
						$('#settingsForm').jqxForm('showComponent', 'city_building_type');
						$('#settingsForm').jqxForm('showComponent', 'city_scheme');
						$('#settingsForm').jqxForm('showComponent', 'city_class_elements_mode');
						$('#settingsForm').jqxForm('showComponent', 'city_class_elements_sort_mode_coarse');
						$('#settingsForm').jqxForm('showComponent', 'city_class_elements_sort_mode_fine');
						$('#settingsForm').jqxForm('showComponent', 'city_class_elements_sort_mode_fine_direction_reversed');
						$('#settingsForm').jqxForm('showComponent', 'city_show_building_base');	
						$('#settingsForm').jqxForm('showComponent', 'city_width_min');
						$('#settingsForm').jqxForm('showComponent', 'city_height_min');
						$('#settingsForm').jqxForm('showComponent', 'city_building_horizontal_margin');
						$('#settingsForm').jqxForm('showComponent', 'city_building_horizontal_gap');
						$('#settingsForm').jqxForm('showComponent', 'city_building_vertical_margin');
						$('#settingsForm').jqxForm('showComponent', 'city_package_color_start');
						$('#settingsForm').jqxForm('showComponent', 'city_package_color_end');
						$('#settingsForm').jqxForm('showComponent', 'city_class_color_start');
						$('#settingsForm').jqxForm('showComponent', 'city_class_color_end');
						$('#settingsForm').jqxForm('showComponent', 'city_class_color');
						$('#settingsForm').jqxForm('showComponent', 'city_color_blue');
						$('#settingsForm').jqxForm('showComponent', 'city_color_aqua');
						$('#settingsForm').jqxForm('showComponent', 'city_color_light_green');
						$('#settingsForm').jqxForm('showComponent', 'city_color_dark_green');
						$('#settingsForm').jqxForm('showComponent', 'city_color_yellow');
						$('#settingsForm').jqxForm('showComponent', 'city_color_orange');
						$('#settingsForm').jqxForm('showComponent', 'city_color_red');
						$('#settingsForm').jqxForm('showComponent', 'city_color_pink');
						$('#settingsForm').jqxForm('showComponent', 'city_color_violet');
						$('#settingsForm').jqxForm('showComponent', 'city_color_light_grey');
						$('#settingsForm').jqxForm('showComponent', 'city_color_dark_grey');
						$('#settingsForm').jqxForm('showComponent', 'city_color_white');
						$('#settingsForm').jqxForm('showComponent', 'city_color_black');
						$('#settingsForm').jqxForm('hideComponent', 'rd_data_factor');	
						$('#settingsForm').jqxForm('hideComponent', 'rd_method_factor');
						$('#settingsForm').jqxForm('hideComponent', 'rd_height');
						$('#settingsForm').jqxForm('hideComponent', 'rd_height_boost');
						$('#settingsForm').jqxForm('hideComponent', 'rd_height_multiplicator');
						$('#settingsForm').jqxForm('hideComponent', 'rd_ring_width');
						$('#settingsForm').jqxForm('hideComponent', 'rd_ring_width_md');
						$('#settingsForm').jqxForm('hideComponent', 'rd_ring_width_ad');
						$('#settingsForm').jqxForm('hideComponent', 'rd_min_area');
						$('#settingsForm').jqxForm('hideComponent', 'rd_namespace_transparency');
						$('#settingsForm').jqxForm('hideComponent', 'rd_class_transparency');
						$('#settingsForm').jqxForm('hideComponent', 'rd_method_transparency');
						$('#settingsForm').jqxForm('hideComponent', 'rd_data_transparency');
						$('#settingsForm').jqxForm('hideComponent', 'rd_color_class');
						$('#settingsForm').jqxForm('hideComponent', 'rd_color_data');
						$('#settingsForm').jqxForm('hideComponent', 'rd_color_method');
						$('#settingsForm').jqxForm('hideComponent', 'rd_color_namespace');
						$('#settingsForm').jqxForm('hideComponent', 'rd_method_disks');
						$('#settingsForm').jqxForm('hideComponent', 'rd_data_disks');
						$('#settingsForm').jqxForm('hideComponent', 'rd_method_type_mode');	
					
						// Elements shown/hidden based on choice 'panels' vs 'bricks' vs 'original' || 'floor' 
						if (newValue.city_building_type == 'panels') {	
						
							$('#settingsForm').jqxForm('showComponent', 'city_show_attributes_as_cylinders');
							$('#settingsForm').jqxForm('showComponent', 'city_panel_separator_mode');
							$('#settingsForm').jqxForm('showComponent', 'city_panel_height_treshold_nos');
							$('#settingsForm').jqxForm('showComponent', 'city_panel_height_unit');
							$('#settingsForm').jqxForm('showComponent', 'city_panel_horizontal_margin');
							$('#settingsForm').jqxForm('showComponent', 'city_panel_vertical_margin');
							$('#settingsForm').jqxForm('showComponent', 'city_panel_vertical_gap');
							$('#settingsForm').jqxForm('showComponent', 'city_panel_separator_height');							
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_layout');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_size');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_horizontal_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_horizontal_gap');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_vertical_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_vertical_gap');		
							$('#settingsForm').jqxForm('hideComponent', 'city_original_building_metric');
							
						} else if (newValue.city_building_type == 'floor') {
						
							$('#settingsForm').jqxForm('hideComponent', 'city_show_attributes_as_cylinders');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_separator_mode');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_height_treshold_nos');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_height_unit');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_horizontal_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_vertical_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_vertical_gap');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_separator_height');					
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_layout');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_size');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_horizontal_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_horizontal_gap');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_vertical_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_vertical_gap');									
							$('#settingsForm').jqxForm('hideComponent', 'city_original_building_metric');
							
						} else if (newValue.city_building_type == 'original') {
							
							$('#settingsForm').jqxForm('hideComponent', 'city_show_attributes_as_cylinders');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_separator_mode');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_height_treshold_nos');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_height_unit');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_horizontal_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_vertical_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_vertical_gap');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_separator_height');							
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_layout');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_size');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_horizontal_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_horizontal_gap');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_vertical_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_vertical_gap');							
							$('#settingsForm').jqxForm('showComponent', 'city_original_building_metric');
							
						} else if (newValue.city_building_type == 'bricks') {
						
							$('#settingsForm').jqxForm('hideComponent', 'city_show_attributes_as_cylinders');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_separator_mode');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_height_treshold_nos');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_height_unit');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_horizontal_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_vertical_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_vertical_gap');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_separator_height');
							$('#settingsForm').jqxForm('showComponent', 'city_brick_layout');
							$('#settingsForm').jqxForm('showComponent', 'city_brick_size');
							$('#settingsForm').jqxForm('showComponent', 'city_brick_horizontal_margin');
							$('#settingsForm').jqxForm('showComponent', 'city_brick_horizontal_gap');
							$('#settingsForm').jqxForm('showComponent', 'city_brick_vertical_margin');
							$('#settingsForm').jqxForm('showComponent', 'city_brick_vertical_gap');								
							$('#settingsForm').jqxForm('hideComponent', 'city_original_building_metric');
						}
						
					} else if (newValue.metaphor == 'rd') {
					
						$('#settingsForm').jqxForm('hideComponent', 'city_building_type');
						$('#settingsForm').jqxForm('hideComponent', 'city_scheme');
						$('#settingsForm').jqxForm('hideComponent', 'city_class_elements_mode');
						$('#settingsForm').jqxForm('hideComponent', 'city_class_elements_sort_mode_coarse');
						$('#settingsForm').jqxForm('hideComponent', 'city_class_elements_sort_mode_fine');
						$('#settingsForm').jqxForm('hideComponent', 'city_class_elements_sort_mode_fine_direction_reversed');
						$('#settingsForm').jqxForm('hideComponent', 'city_show_building_base');
						$('#settingsForm').jqxForm('hideComponent', 'city_blank_node');
							$('#settingsForm').jqxForm('hideComponent', 'city_show_attributes_as_cylinders');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_separator_mode');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_height_treshold_nos');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_height_unit');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_horizontal_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_vertical_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_vertical_gap');
							$('#settingsForm').jqxForm('hideComponent', 'city_panel_separator_height');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_layout');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_size');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_horizontal_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_horizontal_gap');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_vertical_margin');
							$('#settingsForm').jqxForm('hideComponent', 'city_brick_vertical_gap');
						$('#settingsForm').jqxForm('hideComponent', 'city_original_building_metric');
						$('#settingsForm').jqxForm('hideComponent', 'city_width_min');
						$('#settingsForm').jqxForm('hideComponent', 'city_height_min');
						$('#settingsForm').jqxForm('hideComponent', 'city_building_horizontal_margin');
						$('#settingsForm').jqxForm('hideComponent', 'city_building_horizontal_gap');
						$('#settingsForm').jqxForm('hideComponent', 'city_building_vertical_margin');
						$('#settingsForm').jqxForm('hideComponent', 'city_package_color_start');
						$('#settingsForm').jqxForm('hideComponent', 'city_package_color_end');
						$('#settingsForm').jqxForm('hideComponent', 'city_class_color_start');
						$('#settingsForm').jqxForm('hideComponent', 'city_class_color_end');
						$('#settingsForm').jqxForm('hideComponent', 'city_class_color');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_blue');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_aqua');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_light_green');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_dark_green');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_yellow');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_orange');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_red');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_pink');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_violet');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_light_grey');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_dark_grey');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_white');
						$('#settingsForm').jqxForm('hideComponent', 'city_color_black');
						$('#settingsForm').jqxForm('showComponent', 'rd_data_factor');
						$('#settingsForm').jqxForm('showComponent', 'rd_method_factor');
						$('#settingsForm').jqxForm('showComponent', 'rd_height');
						$('#settingsForm').jqxForm('showComponent', 'rd_height_boost');
						$('#settingsForm').jqxForm('showComponent', 'rd_height_multiplicator');
						$('#settingsForm').jqxForm('showComponent', 'rd_ring_width');
						$('#settingsForm').jqxForm('showComponent', 'rd_ring_width_md');
						$('#settingsForm').jqxForm('showComponent', 'rd_ring_width_ad');
						$('#settingsForm').jqxForm('showComponent', 'rd_min_area');
						$('#settingsForm').jqxForm('showComponent', 'rd_namespace_transparency');
						$('#settingsForm').jqxForm('showComponent', 'rd_class_transparency');
						$('#settingsForm').jqxForm('showComponent', 'rd_method_transparency');
						$('#settingsForm').jqxForm('showComponent', 'rd_data_transparency');
						$('#settingsForm').jqxForm('showComponent', 'rd_color_class');
						$('#settingsForm').jqxForm('showComponent', 'rd_color_data');
						$('#settingsForm').jqxForm('showComponent', 'rd_color_method');
						$('#settingsForm').jqxForm('showComponent', 'rd_color_namespace');
						$('#settingsForm').jqxForm('showComponent', 'rd_method_disks');
						$('#settingsForm').jqxForm('showComponent', 'rd_data_disks');
						$('#settingsForm').jqxForm('showComponent', 'rd_method_type_mode');
					}	
					
					if (newValue.output_format == 'x3d') {
					
						$('#settingsForm').jqxForm('showComponent', 'convert_to_multipart');
						
					} else if (newValue.output_format == 'aframe') {
					
						$('#settingsForm').jqxForm('hideComponent', 'convert_to_multipart');	
					}
				
				});
				
				// // Submit Button Validator
				// settingsForm.jqxValidator({ rules: [
					// { input: '#input_files', message: 'Please assign a Name', focus: 'true', rule: 'required', hintType: 'label' },
					// // { input: '#input_files', message: 'Please assign a Name', focus: 'true', rule: 'minLength=3' }
				// ]});
				
				// Submit Form Data 
				var btn = settingsForm.jqxForm('getComponentByName', 'submitButton');
				btn.on('click', function () {
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
	
	// On first load show only the input fields for initially selected options (aframe, city, optional)
	function initial_load_aframe_city_optional() {

		$('#settingsForm').jqxForm('showComponent', 'city_building_type');											// Visibility based on 'city'
		$('#settingsForm').jqxForm('showComponent', 'city_scheme');
		$('#settingsForm').jqxForm('showComponent', 'city_class_elements_mode');
		$('#settingsForm').jqxForm('showComponent', 'city_class_elements_sort_mode_coarse');
		$('#settingsForm').jqxForm('showComponent', 'city_class_elements_sort_mode_fine');
		$('#settingsForm').jqxForm('showComponent', 'city_class_elements_sort_mode_fine_direction_reversed');
		$('#settingsForm').jqxForm('showComponent', 'city_show_building_base');	
		$('#settingsForm').jqxForm('showComponent', 'city_width_min');
		$('#settingsForm').jqxForm('showComponent', 'city_height_min');
		$('#settingsForm').jqxForm('showComponent', 'city_building_horizontal_margin');
		$('#settingsForm').jqxForm('showComponent', 'city_building_horizontal_gap');
		$('#settingsForm').jqxForm('showComponent', 'city_building_vertical_margin');
		$('#settingsForm').jqxForm('showComponent', 'city_package_color_start');
		$('#settingsForm').jqxForm('showComponent', 'city_package_color_end');
		$('#settingsForm').jqxForm('showComponent', 'city_class_color_start');
		$('#settingsForm').jqxForm('showComponent', 'city_class_color_end');
		$('#settingsForm').jqxForm('showComponent', 'city_class_color');
		$('#settingsForm').jqxForm('showComponent', 'city_color_blue');
		$('#settingsForm').jqxForm('showComponent', 'city_color_aqua');
		$('#settingsForm').jqxForm('showComponent', 'city_color_light_green');
		$('#settingsForm').jqxForm('showComponent', 'city_color_dark_green');
		$('#settingsForm').jqxForm('showComponent', 'city_color_yellow');
		$('#settingsForm').jqxForm('showComponent', 'city_color_orange');
		$('#settingsForm').jqxForm('showComponent', 'city_color_red');
		$('#settingsForm').jqxForm('showComponent', 'city_color_pink');
		$('#settingsForm').jqxForm('showComponent', 'city_color_violet');
		$('#settingsForm').jqxForm('showComponent', 'city_color_light_grey');
		$('#settingsForm').jqxForm('showComponent', 'city_color_dark_grey');
		$('#settingsForm').jqxForm('showComponent', 'city_color_white');
		$('#settingsForm').jqxForm('showComponent', 'city_color_black');
		$('#settingsForm').jqxForm('hideComponent', 'rd_data_factor');	
		$('#settingsForm').jqxForm('hideComponent', 'rd_method_factor');
		$('#settingsForm').jqxForm('hideComponent', 'rd_height');
		$('#settingsForm').jqxForm('hideComponent', 'rd_height_boost');
		$('#settingsForm').jqxForm('hideComponent', 'rd_height_multiplicator');
		$('#settingsForm').jqxForm('hideComponent', 'rd_ring_width');
		$('#settingsForm').jqxForm('hideComponent', 'rd_ring_width_md');
		$('#settingsForm').jqxForm('hideComponent', 'rd_ring_width_ad');
		$('#settingsForm').jqxForm('hideComponent', 'rd_min_area');
		$('#settingsForm').jqxForm('hideComponent', 'rd_namespace_transparency');
		$('#settingsForm').jqxForm('hideComponent', 'rd_class_transparency');
		$('#settingsForm').jqxForm('hideComponent', 'rd_method_transparency');
		$('#settingsForm').jqxForm('hideComponent', 'rd_data_transparency');
		$('#settingsForm').jqxForm('hideComponent', 'rd_color_class');
		$('#settingsForm').jqxForm('hideComponent', 'rd_color_data');
		$('#settingsForm').jqxForm('hideComponent', 'rd_color_method');
		$('#settingsForm').jqxForm('hideComponent', 'rd_color_namespace');
		$('#settingsForm').jqxForm('hideComponent', 'rd_method_disks');
		$('#settingsForm').jqxForm('hideComponent', 'rd_data_disks');
		$('#settingsForm').jqxForm('hideComponent', 'rd_method_type_mode');	
		$('#settingsForm').jqxForm('hideComponent', 'city_show_attributes_as_cylinders');							// Visibility based on 'optional'
		$('#settingsForm').jqxForm('hideComponent', 'city_panel_separator_mode');
		$('#settingsForm').jqxForm('hideComponent', 'city_panel_height_treshold_nos');
		$('#settingsForm').jqxForm('hideComponent', 'city_panel_height_unit');
		$('#settingsForm').jqxForm('hideComponent', 'city_panel_horizontal_margin');
		$('#settingsForm').jqxForm('hideComponent', 'city_panel_vertical_margin');
		$('#settingsForm').jqxForm('hideComponent', 'city_panel_vertical_gap');
		$('#settingsForm').jqxForm('hideComponent', 'city_panel_separator_height');							
		$('#settingsForm').jqxForm('hideComponent', 'city_brick_layout');
		$('#settingsForm').jqxForm('hideComponent', 'city_brick_size');
		$('#settingsForm').jqxForm('hideComponent', 'city_brick_horizontal_margin');
		$('#settingsForm').jqxForm('hideComponent', 'city_brick_horizontal_gap');
		$('#settingsForm').jqxForm('hideComponent', 'city_brick_vertical_margin');
		$('#settingsForm').jqxForm('hideComponent', 'city_brick_vertical_gap');							
		$('#settingsForm').jqxForm('showComponent', 'city_original_building_metric');		
		$('#settingsForm').jqxForm('hideComponent', 'convert_to_multipart');										// Visibility based on 'aframe'
	}
	
	// function hide_city() {
	// }
	
	// function hide_city_original() {
	// }
	
	// function hide_rd() {		
	// }
	
	function reset(){
	}
	
	function openSettingsPopUp(){
		$("#settingsPopupWindowDiv").jqxWindow("open");
	}
	
	function addLogObject(logObject){																
	}
	
	function createSettingPopup(rootDiv){
		
		// The Window
		var settingsPopupWindowDiv = document.createElement("DIV");
		rootDiv.appendChild(settingsPopupWindowDiv);
		settingsPopupWindowDiv.id = "settingsPopupWindowDiv";
		
		// The Windows Title
		var settingsPopupTitleDiv = document.createElement("DIV");
		settingsPopupWindowDiv.appendChild(settingsPopupTitleDiv);
		settingsPopupTitleDiv.innerHTML = "Change Settings";
		
		// The Windows DIV
		var settingsPopupContentDiv = document.createElement("DIV");
		settingsPopupWindowDiv.appendChild(settingsPopupContentDiv);
		
		// The Form DIV																				Displays the Form
		var settingsForm = document.createElement("DIV");
		settingsForm.id = "settingsForm";
		settingsPopupContentDiv.appendChild(settingsForm);		
		
		// Button to restore default																// WiP
		// var settingsPopupCancelInput = document.createElement("INPUT");
		// settingsPopupContentDiv.appendChild(settingsPopupCancelInput);
		// settingsPopupCancelInput.type = "button";
		// settingsPopupCancelInput.id = "settingsRestoreDef";
		// settingsPopupCancelInput.value = "Restore Default";
	}
	
	return {
		initialize: initialize,
		activate: activate,
		
		openSettingsPopUp: openSettingsPopUp
	};
	
})();
	
