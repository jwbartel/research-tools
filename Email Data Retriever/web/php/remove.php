<?php 

	$id = $_GET['id'];
	$all = $_GET['all'];
	$addresses = $_GET['addr'];
	$subjects = $_GET['subj'];
	$attachments = $_GET['attach'];
	
	$anonymous_folder= '/afs/cs.unc.edu/home/bartel/public_html/email_threads/anonymous_data/'.$id;
	$private_folder= '/afs/cs.unc.edu/home/bartel/public_html/email_threads/private_data/'.$id;
	print $anonymous_folder;
	print '<br>';
	print $private_folder;
	
	$messages_file = $anonymous_folder.'/messages.txt';
	$addresses_file = $private_folder.'/addresses.txt';
	$summary_file = $private_folder.'/summary.txt';
	$subjects_file = $anonymous_folder.'/subjects.txt';
	$attachments_file = $anonymous_folder.'/attachments.txt';
	
	function delete_file($file) {
		if (file_exists($file)) {
			unlink($file);
			return ' '.$file;
		}
		return '';
	}
	
	function shouldDelete($val) {
		return strcmp($val, 'yes') == 0;
	}
	
	if (shouldDelete($all)) {
		delete_file($messages_file);
		delete_file($summary_file);
		delete_file($addresses_file);
		delete_file($subjects_file);
		delete_file($attachments_file);
	} else {
		if (shouldDelete($addresses)) {
			delete_file($addresses_file);
		}
		if (shouldDelete($subjects)) {
			delete_file($subjects_file);
		}
		if (shouldDelete($attachments)) {
			delete_file($attachments_file);
		}
	}
	

?>