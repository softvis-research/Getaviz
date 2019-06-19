<?php
$backend = gethostbyname(backend);

$url = "http://$backend:8080";
$model = $_REQUEST['input_name'];
$payload = array(
  'input.name'                      => $model,
  'input.files'                     => $_REQUEST['input_files'],
  'metaphor'                        => $_REQUEST['metaphor'],
  'city.building_type'              => $_REQUEST['city_building_type'],
  'city.building_base'              => $_REQUEST['city_show_building_base'],
  'city.original_building_metric'   => $_REQUEST['city_original_building_metric'],
  'city.width_min'                  => $_REQUEST['city_width_min'],
  'city.height_min'                 => $_REQUEST['city_height_min'],
  'city.building.horizontal_margin' => $_REQUEST['city_building_horizontal_margin'],
  'city.building.horizontal_gap'    => $_REQUEST['city_building_horizontal_gap'],
  'city.building.vertical_margin'   => $_REQUEST['city_building_vertical_margin'],
  'city.package.color_start'        => $_REQUEST['city_package_color_start'],
  'city.package.color_end'          => $_REQUEST['city_package_color_end'],
  'city.class.color_start'          => $_REQUEST['city_class_color_start'],
  'city.class.color_end'            => $_REQUEST['city_class_color_end'],
  'city.class.color'                => $_REQUEST['city_class_color']
);

// foreach($parameters as $key => $value) {
//     echo "$key = $value";
//     echo '</br>';
// }

// use key 'http' even if you send the request to https://...
$options = array(
  'http' => array(
    'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
    'method'  => 'POST',
    'content' => http_build_query($payload)
  )
);
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);

// false, because we receive redirect
if ($result === FALSE) { 
  header("Location: http://localhost:8082/ui/index.php?aframe=true&model=$model&setup=web_a-frame/default&srcDir=data-gen");
  die();
}

var_dump($result);

?>