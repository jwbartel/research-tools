<?php
/**
 * Created by PhpStorm.
 * User: andrewwg94
 * Date: 11/20/15
 * Time: 2:43 PM
 */

$record_id = $_POST['r'];
$message_id = $_POST['messageID'];
$private_folder= '/afs/cs.unc.edu/home/andrewwg/email_threads/private_data/'.$record_id;
$groups_file = $private_folder.'/skippedMessages.txt';
$file_handle = fopen($groups_file, "a");
fwrite($file_handle,$message_id."\n");
fclose($file_handle);