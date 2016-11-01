<?php

header("Content-Type:text/html; charset=utf-8");

function mssql_escape($data) {
    if(is_numeric($data))
        return $data;
    $unpacked = unpack('H*hex', $data);
    return '0x' . $unpacked['hex'];
}

$partId = $_GET["partId"];


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

$partSerialId = array();

$getWorkSID = "select * from tblpartworklotcreate where PartID=". mssql_escape($partId) .";";
//echo $getWorkSID;
$res = sqlsrv_query($conn,$getWorkSID);
$i = 0;

while($row = sqlsrv_fetch_array($res,SQLSRV_FETCH_ASSOC)){
  $partSerialId[$i] = $row["PartSerialID"];
  $i = $i + 1;
}

echo json_encode($partSerialId);

sqlsrv_close( $conn );

?> 
