<?php
if ($_SERVER['REQUEST_METHOD'] == "POST") {
    $data = json_decode(file_get_contents('php://input'), true);
    
    echo "\nopen file\n";
    if (!file_exists('./state_data')) {
        mkdir('./state_data', 0777, true);
    }
    $datei = fopen("./state_data/" . $data["stateHashcode"], "x");
    if (!$datei) die("Datei existiert bereits.");
	echo "write file\n";
	fwrite($datei, $data["jsonString"]);
	
	echo "close file\n";
	fclose($datei);
	
	echo "end of php\n";
} else {
	if (isset($_GET["hash"])) {
        $filename="./state_data/" . $_GET["hash"];
        $data = file_get_contents($filename);
        echo "$data";
	}
}
	
?>
