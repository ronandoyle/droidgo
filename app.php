<?php
echo "Commands sent from the DROID - GO app.";

if(isset($_POST["message"]))
{
        $message=$_POST["message"];
}

$param1 = "first";
$param2 = "second";
$param3 = "third";


#$command = "python /home/wit/pythonphp.py";
#$command .= " $message";
#$command .= " $param1 $param2 $param3 2>&1";
#echo "<p>$message</p>";

#$filename="android.html";
$filename="test.txt";

#file_put_contents($filename, "Test Message", FILE_APPEND);
#file_put_contents($filename, $message."<br/>", FILE_APPEND);
file_put_contents($filename, $message);

$android=file_get_contents($filename);
#echo "<table border=1  cellspacing=0 cellpadding=0>  
#DROID-GO Commands</table>";

#echo "<table border=1 cellspacing=0 cellpadding=0>  
#<tr> <td><font color=blue>COMMAND : </td> <td>$android</font></td></tr>  
#</table>";

#$command = "python /home/wit/pythonphp.py";
#$command = "python /home/wit/droid_servo_test.py";

### Comenting these out to try the socket method.
#$command = "python /home/wit/nxt-python-2.2.2/my_programs/droid_servo_test.py";
#$command .= " $android";

$host = "127.0.0.1";
$port = 5660;

$socket1 = socket_create(AF_INET, SOCK_STREAM,0) or die("Could not create socket\n");

socket_connect ($socket1 , $host,$port ) ;

socket_write($socket1, $android, strlen ($android)) or die("Could not write output\n");

#socket_close($socket1) ;


header('Content-Type: text/html; charset=utf-8');
echo '<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />';
echo "<style type='text/css'>
 body{
 background:#000;
 color: #7FFF00;
 font-family:'Lucida Console',sans-serif !important;
 font-size: 12px;
 }
 </style>";

$pid = popen( $command,"r");

echo "<body><pre>";
while( !feof( $pid ) )
{
 echo fread($pid, 256);
 flush();
 ob_flush();
 echo "<script>window.scrollTo(0,99999);</script>";
 usleep(100000);
}
pclose($pid);

echo "</pre><script>window.scrollTo(0,99999);</script>";
?>
