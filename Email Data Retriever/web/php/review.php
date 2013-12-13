<html>
	
	<?php 
		$record_id = $_GET['r'];
		$anonymous_folder= '/afs/cs.unc.edu/home/bartel/public_html/email_threads/anonymous_data/'.$record_id;
		$private_folder= '/afs/cs.unc.edu/home/bartel/public_html/email_threads/private_data/'.$record_id;
		
		$messages_file = $anonymous_folder.'/messages.txt';
		
		$survey_questions_file = $private_folder.'/survey_questions.txt';
		$survey_exist = file_exists($survey_questions_file);
		
		$survey_results_file = $private_folder.'/survey_data.txt';
		$survey_results_exist = file_exists($survey_results_file);
		
		$addresses_file = $private_folder.'/addresses.txt';
		$addr_exist = file_exists($addresses_file);
		
		$subjects_file = $anonymous_folder.'/subjects.txt';
		$subj_exist = file_exists($subjects_file);
		
		$attachments_file = $anonymous_folder.'/attachments.txt';
		$attach_exist = file_exists($attachments_file);
		
		$survey_count = 0;
		$survey_items = array();
		if ($survey_exist) {
			$file_handle = fopen($survey_questions_file, "r");
			while(!feof($file_handle)) {
				
				//Get the subject
				$line = fgets($file_handle);
				if (strlen($line) == 0) {
					break;
				}
				
				$subject = substr($line, strpos($line,':')+1);
				
				//Get the from
				$line = fgets($file_handle);
				$from = substr($line, strpos($line,':')+1);
				$from = str_replace('<', '&lt', $from);
				$from = str_replace('>', '&gt', $from);
				
				//Get the recipients
				$line = fgets($file_handle);
				$recipients = substr($line, strpos($line,':')+1);
				$recipients = str_replace('<', '&lt', $recipients);
				$recipients = str_replace('>', '&gt', $recipients);
				
				//Get the response time
				$line = fgets($file_handle);
				$responseTime = substr($line, strpos($line,':')+1);
				
				//Get the responder
				$line = fgets($file_handle);
				$responder = substr($line, strpos($line,':')+1);
				$responder = str_replace('<', '&lt', $responder);
				$responder = str_replace('>', '&gt', $responder);
				
				$survey_items[$survey_count] = array(
							'subject' => $subject,
							'from' => $from,
							'recipients' => $recipients,
							'responseTime' => $responseTime,
							'responder' => $responder,
						);
				
				$survey_count = $survey_count + 1;
				
			}
		}
		
		$addresses_data = '';
		if ($addr_exist) {
			$file_handle = fopen($addresses_file, "r");
			while (!feof($file_handle)) {
				$line = fgets($file_handle);
				$addresses_data .= substr($line,strpos($line,':')+1);
			}
			fclose($file_handle);
		}
		
		$subjects_data = '';
		if ($subj_exist) {
		$file_handle = fopen($subjects_file, "r");
			while (!feof($file_handle)) {
				$line = fgets($file_handle);
				$subjects_data .= substr($line,strpos($line,'Subject:')+8);
			}
			fclose($file_handle);
		}

		$attach_data = '';
		if ($attach_exist) {
			$file_handle = fopen($attachments_file, "r");
			while (!feof($file_handle)) {
				$attach_data .= fgets($file_handle);
			}
			fclose($file_handle);
			}
		
		$num_columns = 0;
		if ($addr_exist) {
			$num_columns += 1;
		}
		if ($subj_exist) {
			$num_columns += 1;
		}
		if ($attach_exist) {
			$num_columns += 1;
		}
		
		$col_width = ($num_columns > 0)? 800/$num_columns: 0;
		
		function writeSingleQuestion($question, $question_id) {
			print('<tr>');
			print('<td><i>'.$question.'</i></td>');
			print('<td><input style="width:20px" type="radio" name="'.$question_id.'" value="yes">Yes</td>');
			print('<td><input style="width:20px" type="radio" name="'.$question_id.'" value="no">No</td>');
			print('<td><input style="width:30px" type="radio" name="'.$question_id.'" value="unknown">Don\'t know</td>');
			print('</tr>');
		}
		
		function writeSurveyQuestions($surveyItem, $item_id) {
			$message_item_width = "260px";
			$message_item_height = "35px";
			print "<table>";
			print "<tr>";
			print "<td>";
			print "<div style='width:340px'>";
			print "<b>The following message was sent</b><br>";
			print "</div>";
			print "</td>";
			print "<td>";
			print "<div style='width:340px'>";
			print "<b>And the following response was received ".$surveyItem['responseTime']." later</b><br>";
			print "</div>";
			print "</td>";
			print "<tr>";
			print "</tr>";
			print "<td>";
				print "<div style='background-color:white;height:144px;position:inline;border:1px solid'><table>";
				print "<tr>";
					print "<td>From:</td>";
					print "<td><textarea style='width:".$message_item_width.";height:".$message_item_height."'>".$surveyItem['from']."</textarea></td>";
				print "</tr>";
				print "<tr>";
					print "<td>Recipients:</td>";
					print "<td><textarea style='width:".$message_item_width.";height:50px'>".$surveyItem['recipients']."</textarea></td>";
				print "</tr>";
				print "<tr>";
					print "<td>Subject:</td>";
					print "<td><textarea style='width:".$message_item_width.";height:".$message_item_height."'>".$surveyItem['subject']."</textarea></td>";
				print "</tr>";
				print "</table></div>";
			print "</td>";
			print "<td style='height:100%'>";
				print "<div style='background-color:white;height:144px;position:inline;border:1px solid'><table>";
				print "<tr>";
					print "<td>From:</td>";
					print "<td><textarea style='width:".$message_item_width.";height:".$message_item_height."'>".$surveyItem['responder']."</textarea></td>";
				print "</tr>";
					print "<td>Subject:</td>";
					print "<td><textarea style='width:".$message_item_width.";height:".$message_item_height."'>".$surveyItem['subject']."</textarea></td>";
				print "</tr>";
				print "</table></div>";
			print "</td>";
			print "</tr>";
			print "</table>";
			print('<table>');
			writeSingleQuestion('Would it have helped to know that a response was coming?', strval($item_id).'_1');
			writeSingleQuestion('Would it have helped to know a when the response would occur?', strval($item_id).'_2');
			print('</table>');
			print('Would the response time still be helpful if it were off by');
			print('<table>');
			writeSingleQuestion('1 minute', strval($item_id).'_3');
			writeSingleQuestion('5 minutes', strval($item_id).'_4');
			writeSingleQuestion('30 minutes', strval($item_id).'_5');
			writeSingleQuestion('1 hour', strval($item_id).'_6');
			writeSingleQuestion('1 day', strval($item_id).'_7');
			writeSingleQuestion('1 week', strval($item_id).'_8');
			print('</table>');
		}
		
		function writeColumn($exists, $data, $label, $width) {
			if ($exists) {
				print('<td style="width:'.$width.'px">');
				print('<center>');
				print('<h3>'.$label.'</h3>');
				print('<textarea style="width:100%;height:400px;white-space:nowrap;overflow:auto;" readonly="true">');
				print($data);
				print('</textarea></center>');
				print('<br>');
				print('<input class="setting checkbox" type="checkbox" id="remove'.$label.'">');
				print('Remove '.$label.' Data');
				print('</td>');
			}
		}
	?>
	
	<head>
		<title>Email Thread Data Review</title>
		<link rel="stylesheet" href="review.css">
		<script src='../js/jquery-1.10.2.min.js' type='text/javascript'></script>
		<script type='text/javascript'>
			function removeData() {
				<?php 
					print 'deleteOptions = "id='.$_GET['r'].'";';
				?>

				deleteOptions += '&addr=';
				if($('#removeAddresses') != null && $('#removeAddresses').is(":checked")){
					deleteOptions += 'yes';
				} else {
					deleteOptions +='no';
				}

				deleteOptions += '&subj=';
				if($('#removeSubjects') != null && $('#removeSubjects').is(":checked")){
					deleteOptions += 'yes';
				} else {
					deleteOptions +='no';
				}

				deleteOptions += '&attach=';
				if($('#removeAttachments') != null && $('#removeAttachments').is(":checked")){
					deleteOptions += 'yes';
				} else {
					deleteOptions +='no';
				}

				deleteOptions += '&all=';
				if($('#removeAll') != null && $('#removeAll').is(":checked")){
					deleteOptions += 'yes';
				} else {
					deleteOptions +='no';
				}

				dest = "https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler/php/remove.php?" + deleteOptions;
				$.get(dest, function() {
						window.location.reload();
					});
			}


			function storeSurveyData() {

				<?php 
					$surveyDataStr = 'id='.$_GET['r'];
					$surveyDataStr = $surveyDataStr.'&count='.strval($survey_count);
					
					print "surveyData = '".$surveyDataStr."';\n";	
					
					for ($i = 0; $i < $survey_count; $i++) {
						for ($j =0; $j <= 8; $j++) {
							$question_id = strval($i)."_".strval($j);
							print "checkedVal = $('input[name=".$question_id."]').filter(':checked').val();\n";
							print "if (checkedVal != undefined) {";
							print "\tsurveyData += '&".$question_id."='+checkedVal;\n";
							print "} else {";
							print "\tsurveyData += '&".$question_id."=unanswered';\n";
							print "}";
						}
					}
				?>

				dest = "https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler/php/survey.php?"+surveyData;
				$.get(dest);
			}

			function submitData() {
				storeSurveyData();
				removeData();
			}
			
		</script>
	</head>
	<div class="center" id="reviewer">
	<?php 
		if ($survey_exist && !$survey_results_exist) {
			print('<h1>Please complete this short survey about response time</h1>');
			for ($i = 0; $i < $survey_count; $i++) {
				writeSurveyQuestions($survey_items[$i], $i);
				print "<br>";
			}
		}
	?>
	
	
	<h1>Review your retrieved email data below</h1>
	<table style='border-spacing:10'>
		<tr>
			<?php 
				writeColumn($addr_exist, $addresses_data, 'Addresses', $col_width);
				writeColumn($subj_exist, $subjects_data, 'Subjects', $col_width);
				writeColumn($attach_exist, $attach_data, 'Attachments', $col_width);
			?>
		
		</tr>
	</table>
	
	<input class="setting checkbox" type="checkbox" id="removeAll">
	Remove all data about messages and threads.
	
<<<<<<< HEAD
	<input type='submit' value='Submit' style='width:100%' onclick='submitData()'>
=======
	<input type='submit' value='Submit' style='width:100%' onclick='removeData()'>
>>>>>>> Added displaying of survey questions
	</div>
</html>