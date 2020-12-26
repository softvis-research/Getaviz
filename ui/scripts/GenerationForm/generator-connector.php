<?php
$backend = gethostbyname(backend);
$url = "http://$backend:8080";
$model = $_REQUEST['input_name']; // is also used for redirect url
$payload = array(
  'input.name'                      => $model,
  'input.files'                     => $_REQUEST['input_files'],
  'metaphor'                        => $_REQUEST['metaphor'],
  'city.building_type'              => $_REQUEST['city_building_type'],
  'city.scheme'                     => $_REQUEST['city_scheme'],
  'city.class_elements_mode'        => $_REQUEST['city_class_elements_mode'],
  'city.class_elements_sort_mode_coarse'                   => $_REQUEST['city_class_elements_sort_mode_coarse'],
  'city.class_elements_sort_mode_fine'                     => $_REQUEST['city_class_elements_sort_mode_fine'],
  'city.class_elements_sort_mode_fine_direction_reversed'  => $_REQUEST['city_class_elements_sort_mode_fine_direction_reversed'],
  'city.show_building_base'            => $_REQUEST['city_show_building_base'],
  'city.show_attributes_as_cylinders'  => $_REQUEST['city_show_attributes_as_cylinders'],
  'city.brick.layout'                  => $_REQUEST['city_brick_layout'],
  'city.brick.size'                    => $_REQUEST['city_brick_size'],
  'city.brick.horizontal_margin'       => $_REQUEST['city_brick_horizontal_margin'],
  'city.brick.horizontal_gap'          => $_REQUEST['city_brick_horizontal_gap'],
  'city.brick.vertical_margin'         => $_REQUEST['city_brick_vertical_margin'],
  'city.brick.vertical_gap'            => $_REQUEST['city_brick_vertical_gap'],
  'city.panel.separator_mode'          => $_REQUEST['city_panel_separator_mode'],
  'city.panel.height_treshold_nos'     => $_REQUEST['city_panel_height_treshold_nos'],
  'city.panel.height_unit'             => $_REQUEST['city_panel_height_unit'],
  'city.panel.horizontal_margin'       => $_REQUEST['city_panel_horizontal_margin'],
  'city.panel.vertical_margin'         => $_REQUEST['city_panel_vertical_margin'],
  'city.panel.vertical_gap'            => $_REQUEST['city_panel_vertical_gap'],
  'city.panel.separator_height'        => $_REQUEST['city_panel_separator_height'],
  'city.original_building_metric'      => $_REQUEST['city_original_building_metric'],
  'city.width_min'                     => $_REQUEST['city_width_min'],
  'city.height_min'                    => $_REQUEST['city_height_min'],
  'city.building.horizontal_margin'    => $_REQUEST['city_building_horizontal_margin'],
  'city.building.horizontal_gap'       => $_REQUEST['city_building_horizontal_gap'],
  'city.building.vertical_margin'      => $_REQUEST['city_building_vertical_margin'],
  'city.package.color_start'           => $_REQUEST['city_package_color_start'],
  'city.package.color_end'      => $_REQUEST['city_package_color_end'],
  'city.class.color_start'      => $_REQUEST['city_class_color_start'],
  'city.class.color_end'        => $_REQUEST['city_class_color_end'],
  'city.class.color'            => $_REQUEST['city_class_color'],
  'city.color.blue'             => $_REQUEST['city_color_blue'],
  'city.color.aqua'             => $_REQUEST['city_color_aqua'],
  'city.color.light_green'      => $_REQUEST['city_color_light_green'],
  'city.color.dark_green'       => $_REQUEST['city_color_dark_green'],
  'city.color.yellow'           => $_REQUEST['city_color_yellow'],
  'city.color.orange'           => $_REQUEST['city_color_orange'],
  'city.color.red'              => $_REQUEST['city_color_red'],
  'city.color.pink'             => $_REQUEST['city_color_pink'],
  'city.color.violet'           => $_REQUEST['city_color_violet'],
  'city.color.light_grey'       => $_REQUEST['city_color_light_grey'],
  'city.color.dark_grey'        => $_REQUEST['city_color_dark_grey'],
  'city.color.white'            => $_REQUEST['city_color_white'],
  'city.color.black'            => $_REQUEST['city_color_black'],
  'rd.data_factor'              => $_REQUEST['rd_data_factor'],
  'rd.height'                   => $_REQUEST['rd_height'],
  'rd.ring_width'               => $_REQUEST['rd_ring_width'],
  'rd.ring_width_ad'            => $_REQUEST['rd_ring_width_ad'],
  'rd.min_area'                 => $_REQUEST['rd_min_area'],
  'rd.namespace_transparency'   => $_REQUEST['rd_namespace_transparency'],
  'rd.class_transparency'       => $_REQUEST['rd_class_transparency'],
  'rd.method_transparency'      => $_REQUEST['rd_method_transparency'],
  'rd.data_transparency'        => $_REQUEST['rd_data_transparency'],
  'rd.color.class'              => $_REQUEST['rd_color_class'],
  'rd.color.data'               => $_REQUEST['rd_color_data'],
  'rd.color.method'             => $_REQUEST['rd_color_method'],
  'rd.color.namespace'          => $_REQUEST['rd_color_namespace'],
  'rd.method_disks'             => $_REQUEST['rd_method_disks'],
  'rd.data_disks'               => $_REQUEST['rd_data_disks'],
  'rd.method_type_mode'         => $_REQUEST['rd_method_type_mode']
);

$curl = curl_init();
curl_setopt($curl, CURLOPT_URL, $url);
curl_setopt($curl, CURLOPT_POST, 1);
curl_setopt($curl, CURLOPT_POSTFIELDS, http_build_query($payload));
$return = curl_exec($curl);
curl_close ($curl);
if ($return == "OK") {
  header("Location: ../../index.php?aframe=true&model=$model&setup=default&srcDir=data-gen");
  die();
}

// will be seen if something went wrong
var_dump($result);

?>
