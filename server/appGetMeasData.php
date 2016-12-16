<?php

header("Content-Type:text/html; charset=utf-8");
$partId = $_GET["partId"];
$partSerialId = $_GET["partSerialId"];
$workId = $_GET["workId"];

/*Prevent sql injection*/
function mssql_escape($data) {
    if(is_numeric($data))
        return $data;
    $unpacked = unpack('H*hex', $data);
    return '0x' . $unpacked['hex'];
}

//load DB settings
include 'settings.php';
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
$measData = array();


//prepare sql
$getAllMeasData = "SELECT tblpartqcdata.MeasID,tblpartqcdata.Value,tblpartqcdata.Status, tblpartqcdefine.WorkID, tblpartqcdefine.NormalSize, tblpartqcdefine.ToleranceU, tblpartqcdefine.ToleranceL, tblpartqcdefine.FinalMeas, tblpartqcdefine.IsKeyMeas FROM tblpartqcdata INNER JOIN tblpartqcdefine ON tblpartqcdata.MeasID = tblpartqcdefine.MeasID WHERE tblpartqcdata.PartSerialID = '$partSerialId' AND tblpartqcdefine.PartID = ". mssql_escape($partId) ." AND tblpartqcdefine.WorkID = " . mssql_escape($workId);
$res = sqlsrv_query($conn,$getAllMeasData);

$i = 0;
while($row = sqlsrv_fetch_array($res,SQLSRV_FETCH_ASSOC)){
  $measData[$i] = $row;
  $i = $i + 1;
}

echo json_encode($measData);

sqlsrv_close( $conn );

?> 
