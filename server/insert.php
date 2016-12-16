<?php

/************************************
Insert measured value into database
modified: mao
*************************************/

$reading =  $_POST["reading"];
$unit =  $_POST["unit"];
$triggerFlag =  $_POST["triggerFlag"];
$angles =  $_POST["angles"];
$batteryVoltage =  $_POST["batteryVoltage"];
$versionFlag =  $_POST["versionFlag"];
$timeInterval =  $_POST["timeInterval"];

/**************************
Prevent sql injection
**************************/
function mssql_escape($data) {
    if(is_numeric($data))
        return $data;
    $unpacked = unpack('H*hex', $data);
    return '0x' . $unpacked['hex'];
}

/*load DB settings*/
include 'settings.php';
/*establish connection object*/
$connectionInfo = array( "UID"=>$uid,  
                         "PWD"=>$pwd,  
                         "Database"=>$dbname,
                         "CharacterSet"=>"UTF-8");  
  
/* Connect using SQL Server Authentication. */  
$conn = sqlsrv_connect( $serverName, $connectionInfo);  
if( $conn === false )  
{  
     echo "Unable to connect.</br>";  
     die( print_r( sqlsrv_errors(), true));  
}

/*prepare sql*/
$sql = "INSERT INTO sensor (reading, unit, triggerFlag, angles, batteryVoltage, versionFlag, timeInterval)
VALUES ('". $reading ."','". $unit ."','". $triggerFlag ."','". $angles ."','". $batteryVoltage ."','". $versionFlag ."','". $timeInterval ."')";

/*execute sql*/
if (sqlsrv_query($conn,$sql) === TRUE) {
    echo "Successfully";
} else {
    echo "Error: " . $sql . "<br>" . $conn->error;
}

$conn->close();

?>
