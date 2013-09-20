Returning this will indicate that the retrieval process has completed.  It should also redirect to a UI to view the submitted data.

<?php 


$javaParams = array(
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

print exec('java -jar EmailDataRetriever.jar'.javaParamStr);

?>