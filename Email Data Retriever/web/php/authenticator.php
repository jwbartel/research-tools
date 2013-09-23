<?php 


$javaParams = array(
	'id' => time(),
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
	print $output;
}else{
	print "<script type='text/javascript'>collectData();</script>";
	print "<a href='javascript:reset()'>Click here to try a different email address</a>";
}

?>