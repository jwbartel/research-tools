<?php 


$javaParams = array(
	'id' => time(),
	'imap' => $_POST['i'],
	'smtp' => $_POST['s'],
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
if (strlen($output) > 0) {
	print $output;
}else{
	print "Thank you for contributing. Your data is being uploaded in the background. ";
	print "You will be mailed at your provided email address when it has completed";
// 	exec('java -jar EmailDataRetriever.jar'.$javaParamStr);
}

?>