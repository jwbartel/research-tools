<?php 
	session_start();

	$signature = $_POST['participantSig'];
	$date = $_POST['date'];
	$id = time();
	$_SESSION['id'] = $id;
	$_SESSION['calenderNum'] = 0;
	
	$private_folder= '/afs/cs.unc.edu/home/bartel/email_threads/private_data/'.$id;
	if (!file_exists ($private_folder)) {
		mkdir($private_folder);
	}
	
	$signatures_file= '/afs/cs.unc.edu/home/bartel/email_threads/private_data/signatures.txt';
	file_put_contents($signatures_file, "".$signature.", ".$date."=>".$id."\n" , FILE_APPEND | LOCK_EX);
	
	header( 'Location: '.rootAddress().'/?s='.$id ) ;

    function rootAddress () {
        if  (strncmp($_SERVER['HTTP_HOST'], 'localhost',9)==0)
            return 'https://'.$_SERVER['HTTP_HOST'].'/web';
        elseif (strcmp($_SERVER['HTTP_HOST'],'wwwx.cs.unc.edu')==0)
            return 'https://'.$_SERVER['HTTP_HOST'].'/~bartel/cgi-bin/emailsampler';
        return $_SERVER['HTTP_HOST'];
    }
?>