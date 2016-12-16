<?php
/************************************
Insert measured value into database
modified: mao
*************************************/


/*Prevent sql injection*/
function mssql_escape($data) {
    if(is_numeric($data))
        return $data;
    $unpacked = unpack('H*hex', $data);
    return '0x' . $unpacked['hex'];
}
$partSerialId= mssql_escape($_GET["partSerialId"]);
$measId=mssql_escape($_GET["measId"]);
$value=mssql_escape($_GET["value"]);



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

$now = date("Y-m-d H:i:s");
$sql;

/*check if the entry is already existed*/
$check_sql = "SELECT PartSerialID, MeasID FROM tblPartQCData WHERE PartSerialID=$partSerialId AND MeasID=$measId";
$res = sqlsrv_query($conn,$check_sql);


if($res == false){
	/*insert new data*/
    $sql = "INSERT INTO tblpartqcdata (PartSerialID, MeasID, Value, Status, Grade, CreateDate) VALUES ($partSerialId, $measId, $value, NULL, NULL, '$now')";
}else {
    /*update existing data*/
	$sql = "UPDATE tblpartqcdata SET Value=$value,CreateDate='$now' WHERE PartSerialID=$partSerialId AND MeasID=$measId";
}

//echo $sql;

if (($r = sqlsrv_query($conn,$sql)) != FALSE) {
    echo "Successfully";
} else {
	if( ($errors = sqlsrv_errors() ) != null) {
        foreach( $errors as $error ) {
            echo "SQLSTATE: ".$error[ 'SQLSTATE']."<br />";
            echo "code: ".$error[ 'code']."<br />";
            echo "message: ".$error[ 'message']."<br />";
        }
    }
}


sqlsrv_close( $conn );

?>
