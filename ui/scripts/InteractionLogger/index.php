<?php
	header("Access-Control-Allow-Origin: *");
?>

<?php
	echo "open file\n";
	$datei = fopen($_POST["logFile"], "a");

	echo "write file\n";
	fwrite($datei, $_POST["logText"]);
	
	echo "close file\n";
	fclose($datei);
	
	echo "end of php\n";
?>