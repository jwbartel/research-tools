<?php 


$javaParams = array(
	'id' => $_POST['id'],
	'imap' => $_POST['i'],
	'email' => $_POST['e'],
	'password' => $_POST['p'],
	'messages' => $_POST['m'],
	'threads' => $_POST['t'],
	'subjects' => $_POST['subj'],
	'addresses' => $_POST['a'],
	'numAttach' => $_POST['attach'],
	'fileNames' => $_POST['f'],
);

$javaParamStr = '';
foreach($javaParams as $key => $value) {
	$javaParamStr .= ' ';
	$javaParamStr .= '-'.$key.' '.$value;
}
$output = exec('java -jar EmailDataRetriever.jar -onlyCheckLogin'.$javaParamStr);
if (strcmp($output, 'Login successful') != 0) {
	error_log($output);
	print $output;
}else{
	print "<script type='text/javascript'>collectData('".$_POST['id']."');</script>";
	print "<a href='javascript:reset()'>Click here to try a different email address</a>";
}

?>