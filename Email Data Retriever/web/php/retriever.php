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

$email = $_POST['e'];
$logFile = '/afs/cs.unc.edu/home/bartel/public_html/email_threads/logs/'.$email.'_'.$javaParams['id'].'.txt';
exec('java -jar EmailDataRetriever.jar'.$javaParamStr.' >& '.$logFile);


print "Thank you for contributing. Your data is being uploaded in the background. ";
print "You will be emailed at your provided email address when it has completed";
print "<br>";
print "<a href='javascript:reset()'>Click here to try a different email address</a>";

?>