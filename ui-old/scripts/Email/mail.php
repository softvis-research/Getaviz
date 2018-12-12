<?php 

    if(!$_POST){
        printf("nope!");
    }
    
    $name = $_POST['name'];
    $email = $_POST['email'];
    $message = $_POST['message'];
    $logDump = $_POST['logDump'];
    $logFile = $_POST['logFile'];

  

    if (($name=="")||($email=="")||($message=="")||($logFile==""))  { 
        printf("ERROR"); 
    } 
    else{         
        $from="From: $name<$email>\r\nReturn-path: $email"; 
        
        $subject="Message sent using your contact form"; 

        $message = $message . " \r\n \r\n \r\n" . $logDump;


        echo "open file\n";
        $datei = fopen($logFile, "a");

        echo "write file\n";
        fwrite($datei, $message);
        
        echo "close file\n";
        fclose($datei);
        

        echo "send mail\n";        
        mail("pkovacs@uni-leipzig.de", $subject, $message, $from); 
    } 

?> 
