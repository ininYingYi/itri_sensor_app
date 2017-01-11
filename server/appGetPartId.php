<?php

/*Prevent sql injection*/
function mssql_escape($data) {
    if(is_numeric($data))
        return $data;
    $unpacked = unpack('H*hex', $data);
    return '0x' . $unpacked['hex'];
}

//load DB settings
include 'settings.php';
/*establish connection object*/
$connectionInfo = array( "UID"=>$uid,  
                         "PWD"=>$pwd,  
                         "Database"=>$dbname,
                         "CharacterSet"=>"UTF-8");  

$conn = sqlsrv_connect( $serverName, $connectionInfo);  
if( $conn === false )  
{  
     echo "Unable to connect.</br>";  
     die( print_r( sqlsrv_errors(), true));  
} 



$AllPartID = array();
$AllPartName = array();
//prepare sql
$getAllPartID = "SELECT PartID,PartName FROM tblpartdatadefine";


$res = sqlsrv_query($conn,$getAllPartID);
$partData = array();
$i = 0;
while($row = sqlsrv_fetch_array($res,SQLSRV_FETCH_ASSOC)){
  $partData[$i] = $row;
  $i = $i + 1;
}


echo json_encode($partData);

sqlsrv_close( $conn );

?> 
