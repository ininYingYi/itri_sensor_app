<?php


header("Content-Type:text/html; charset=utf-8");


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
  
/* Connect using SQL Server Authentication. */  
$conn = sqlsrv_connect( $serverName, $connectionInfo);  
if( $conn === false )  
{  
     echo "Unable to connect.</br>";  
     die( print_r( sqlsrv_errors(), true));  
}

$partId = $_GET["partId"];


$workId = array();

/*prepare sql*/
$getWorkID = "select * from tblpartworkprocessdefine where PartID=".mssql_escape($partId).";";

$res = sqlsrv_query($conn,$getWorkID);
$i = 0;
/*fetch data*/
while($row = sqlsrv_fetch_array($res,SQLSRV_FETCH_ASSOC)){
  $workId[$i] = $row["WorkID"];
  $i = $i + 1;
}
//send data in json format 
echo json_encode($workId);

sqlsrv_close( $conn );
?> 
