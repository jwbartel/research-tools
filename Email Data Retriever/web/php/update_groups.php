<?php
//header('Location: https://docs.google.com/spreadsheet/viewform?formkey=dGlMUHJYZTlOekZESFF3cjd3b2hpWVE6MA');
		$record_id = $_POST['r'];
        $private_folder= '/afs/cs.unc.edu/home/bartel/email_threads/private_data/'.$record_id;
		$groups_file = $private_folder.'/groups_edited.txt';
		print_r ($_POST);
		$edits = print_r($_POST['edits'], true);
		file_put_contents($groups_file, $edits);
?>