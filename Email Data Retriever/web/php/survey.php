<?php 

	$id = $_POST['id'];
	$count = intval($_POST['count']);
	
	$anonymous_folder= '/afs/cs.unc.edu/home/andrewwg/email_threads/anonymous_data/'.$id;
	$private_folder= '/afs/cs.unc.edu/home/andrewwg/email_threads/private_data/'.$id;
	
	if (!file_exists($private_folder)) {
		mkdir($private_folder, 0777, true);
	}
	$survey_data_file = $private_folder.'/survey_data.txt';
	
// 	$data_str = "";
	
// 	for ($i=0; $i < $count; $i++) {
// 		for ($j=1; $j <= 8; $j++) {
// 			$question_id = strval($i)."_".strval($j);
// 			$data_str = $data_str.$_POST[$question_id].",";
// 		}
// 		$data_str = $data_str."\n";
// 	}
	
// 	surveyData.wouldDo_nothing = $('#doNothing').attr('checked');
// 	surveyData.wouldDo_notSend = $('#notSend').attr('checked');
// 	surveyData.wouldDo_addRecipient = $('#addRecipients').attr('checked');
// 	surveyData.wouldDo_removeRecipient = $('#removeRecipients').attr('checked');
// 	surveyData.wouldDo_findAnswer = $('#findAnswer').attr('checked');
// 	surveyData.wouldDo_other = $('#doOther').attr('checked');
// 	surveyData.wouldDo_otherVal = $('#otherVal').val();
	
// 	$data_str = $data_str."Would do the following for predicted respsponse time:\n";
// 	$data_str = $data_str."\tNothing:".$_POST['wouldDo_nothing']."\n";
// 	$data_str = $data_str."\tNot Send:".$_POST['wouldDo_notSend']."\n";
// 	$data_str = $data_str."\tAdd Recipient(s):".$_POST['wouldDo_addRecipient']."\n";
// 	$data_str = $data_str."\tRemove Recipient(s):".$_POST['wouldDo_removeRecipient']."\n";
// 	$data_str = $data_str."\tFind answer on own:".$_POST['wouldDo_findAnswer']."\n";
// 	$data_str = $data_str."\tOther:".$_POST['wouldDo_other']."\n";
// 	$data_str = $data_str."\t\tDescription:".$_POST['wouldDo_otherVal']."\n";
	
// 	$data_str = $data_str."\n";
	
// 	$data_str = $data_str."If reason(s):\n".$_POST['ifReason']."\n";
// 	$data_str = $data_str."When reason(s):\n".$_POST['whenReason']."\n";
// 	$data_str = $data_str."Reason(s) for predictions about self:\n".$_POST['selfReason']."\n";
	
	$data_str = print_r($_POST, true);
	
	$file = fopen($survey_data_file, 'w');
	fwrite($file, $data_str);
	fclose($file);
	

?>