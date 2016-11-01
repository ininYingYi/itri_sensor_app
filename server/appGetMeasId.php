<?php

header("Content-Type:text/html; charset=utf-8");

function mssql_escape($data) {
    if(is_numeric($data))
        return $data;
    $unpacked = unpack('H*hex', $data);
    return '0x' . $unpacked['hex'];
}

$partId = mssql_escape($_GET["partId"]);
$workId = mssql_escape($_GET["workId"]);




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

$measId = array();

$getAllMeasID = "select MeasID,ImagePath,CMM from tblpartqcdefine where PartID=$partId and WorkID=$workId";
$res = sqlsrv_query($conn,$getAllMeasID);

$i = 0;
while($row = sqlsrv_fetch_array($res,SQLSRV_FETCH_ASSOC)){
  $measId[$i] = $row;
  $i = $i + 1;
}

echo json_encode($measId);

sqlsrv_close( $conn );

?> 
