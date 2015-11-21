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

$out = shell_exec('java -jar EmailDataRetriever.jar'.$javaParamStr.' &');
$out = shell_exec('java -jar "prediction_jars/Email Predictions.jar" '.$javaParams['id']).' '.$javaParams['messages'].' '.$javaParams['password'].' false';
?>