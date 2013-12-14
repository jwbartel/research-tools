<?php 

	$id = $_GET['id'];
	$count = intval($_GET['count']);
	
	$anonymous_folder= '/afs/cs.unc.edu/home/bartel/public_html/email_threads/anonymous_data/'.$id;
	$private_folder= '/afs/cs.unc.edu/home/bartel/public_html/email_threads/private_data/'.$id;
	
	$survey_data_file = $private_folder.'/survey_data.txt';
	
	$data_str = "";
	
	for ($i=0; $i < $count; $i++) {
		for ($j=1; $j <= 8; $j++) {
			$question_id = strval($i)."_".strval($j);
			$data_str = $data_str.$_GET[$question_id].",";
		}
		$data_str = $data_str."\n";
	}
	
	$file = fopen($survey_data_file, 'w');
	fwrite($file, $data_str);
	fclose($file);
	

?>