<html>
	
	<?php 
		$record_id = $_GET['r'];
		$anonymous_folder= '/afs/cs.unc.edu/home/bartel/email_threads/anonymous_data/'.$record_id;
		$private_folder= '/afs/cs.unc.edu/home/bartel/email_threads/private_data/'.$record_id;
		
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
				
				//Get the original date
				$line = fgets($file_handle);
				$originalDate = substr($line, strpos($line,':')+1);
				
				//Get the recipients
				$line = fgets($file_handle);
				$recipients = substr($line, strpos($line,':')+1);
				$recipients = str_replace('<', '&lt', $recipients);
				$recipients = str_replace('>', '&gt', $recipients);
				
				//Get the response time
				$line = fgets($file_handle);
				$responseTime = substr($line, strpos($line,':')+1);
				
				//Get the response date
				$line = fgets($file_handle);
				$responseDate = substr($line, strpos($line,':')+1);
				
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
		
		function writeSingleQuestion($question, $question_id, $setOnClick, $message_id) {
			print('<tr>');
			print('<td><i>'.$question.'</i></td>');
			if ($setOnClick) {
				print('<td><input style="width:20px" type="radio" name="'.$question_id.'" value="yes" onclick="showTime('.$message_id.')">Yes</td>');
				print('<td><input style="width:20px" type="radio" name="'.$question_id.'" value="no" onclick="showTime('.$message_id.')">No</td>');
				print('<td><input style="width:30px" type="radio" name="'.$question_id.'" value="unknown" onclick="showTime('.$message_id.')">Don\'t know</td>');
			} else {
				print('<td><input style="width:20px" type="radio" name="'.$question_id.'" value="yes">Yes</td>');
				print('<td><input style="width:20px" type="radio" name="'.$question_id.'" value="no">No</td>');
				print('<td><input style="width:30px" type="radio" name="'.$question_id.'" value="unknown">Don\'t know</td>');
			}
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
			writeSingleQuestion('Would it have helped to know that a response was coming?', strval($item_id).'_1', false, strval($item_id));
			writeSingleQuestion('Would it have helped to know when the response would occur?', strval($item_id).'_2', true, strval($item_id));
			print('</table>');
			print('<div style="display:none" id="'.strval($item_id).'_times_list">');
			print('Would the response time still be helpful if it were off by');
			print('<br/> (Please select "Yes", "No", or "Don\'t know" for each of the following)');
			print('<table>');
			writeSingleQuestion('1 minute', strval($item_id).'_3', false, strval($item_id));
			writeSingleQuestion('5 minutes', strval($item_id).'_4', false, strval($item_id));
			writeSingleQuestion('30 minutes', strval($item_id).'_5', false, strval($item_id));
			writeSingleQuestion('1 hour', strval($item_id).'_6', false, strval($item_id));
			writeSingleQuestion('1 day', strval($item_id).'_7', false, strval($item_id));
			writeSingleQuestion('1 week', strval($item_id).'_8', false, strval($item_id));
			print('</table>');
			print('</div>');
			
			print('<br><br>');
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
						alert("Thank you for contributing.  You may close this tab or continue to review your shared data");
						window.location.reload();
					});
			}


			function showTime(id) {
				var div = $('#'+id+'_times_list');
				console.log(div);
				var answers = $("input[type='radio'][name='"+id+"_2']:checked");
				console.log(answers);
				var checkedVal = $("input[type='radio'][name='"+id+"_2']:checked").val();
				console.log(checkedVal);
				if (checkedVal == 'yes') {
					div.css('display', 'block');
				} else {
					div.css('display', 'none');
				}
			}

			function storeSurveyData() {

				surveyData = {};
				<?php 
					$surveyDataStr = 'id='.$_GET['r'];
					$surveyDataStr = $surveyDataStr.'&count='.strval($survey_count);
					
					print "surveyCount = ".strval($survey_count).";\n";
					print "userId = '".strval($_GET['r'])."';\n";
// 					print "surveyData = '".$surveyDataStr."';\n";	
					
// 					for ($i = 0; $i < $survey_count; $i++) {
// 						for ($j =0; $j <= 8; $j++) {
// 							$question_id = strval($i)."_".strval($j);
// 							print "checkedVal = $('input[name=\"".$question_id."\"]:checked').val();\n";
// 							print "if (checkedVal != undefined) {\n";
// 							print "\tsurveyData[\"".$question_id."\"] = checkedVal;\n";
// 							print "} else {\n";
// 							print "\tsurveyData[\"".$question_id."\"] = 'unanswered';\n";
// 							print "}\n";
// 						}
// 					}
				?>

				surveyData.id = userId;
				surveyData.count = surveyCount;
				for (i = 0; i < surveyCount; i++) {
					for (j = 1; j <= 8; j++) {
						question_id = "" + i  + "_" + j;
						checkedVal = $('input[name="'+question_id+'"]:checked').val();
						if (checkedVal != undefined) {
							surveyData[question_id] = checkedVal;;
						} else {
							surveyData[question_id] = 'unanswered';
						}
					}
				}
				surveyData.ifReason = $('#ifReason').val();
				surveyData.whenReason = $('#whenReason').val();
				surveyData.noReason = $('#noReason').val();
				surveyData.selfReason = $('#selfReason').val();
				
				surveyData.wouldDo_nothing = $('#doNothing').prop('checked');
				surveyData.wouldDo_notSend = $('#notSend').prop('checked');
				surveyData.wouldDo_addRecipient = $('#addRecipients').prop('checked');
				surveyData.wouldDo_removeRecipient = $('#removeRecipients').prop('checked');
				surveyData.wouldDo_findAnswer = $('#findAnswer').prop('checked');
				surveyData.wouldDo_other = $('#doOther').prop('checked');
				surveyData.wouldDo_otherVal = $('#otherVal').val();

				console.dir(surveyData);

				dest = "https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler/php/survey.php";
				$.post(dest, surveyData);
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

		if (!$survey_results_exist) {
			print("<br>");
				
			print("<table style='width:700px'>");;
			print('<tr><td>');
			print('<b>Do you recall any situation(s) where you needed a response to an email or a post on a forum such as Piazza or Stack Overflow quickly enough to meet some deadline?  If so, please describe them.</b>');
			print('<br>');
			print('For example, you may have messaged friends or family about meeting for dinner that evening, or you may have needed to contact your professor, TA, boss, colleague, or coworker about an assignment or project shortly before it is due.');
			print('</td></tr>');
			print('<tr><td>');
			print('<table>');
			print('<tr>');
			print('<td><input style="width:20px" type="radio" name="deadlineConfirm" value="yes">Yes</td>');
			print('<td><input style="width:20px" type="radio" name="deadlineConfirm" value="no">No</td>');
			print('</tr>');
			print('</table>');
			print('</td></tr>');
			print('<tr><td>');
			print('<b>Please describe the situations.</b>');
			print('</td></tr>');
			print('<tr><td>');
			print('<textarea style="width:700px;height:50px" id="deadlineSituation"></textarea>');
			print('</td></tr>');
			print('<tr><td>');
			print('<br>');
			print('<b>In the described situation(s), suppose that as you were composing your message, we predicted when you would receive a response, and it would not arrive quickly enough for you to meet your deadline. ');
			print('Would you do any of the following?</b>');
			print('<br>');
			print('(You may select more than one)');
			print('</td></tr>');
			print('<tr><td>');
			print('<table>');
			print("<tr><td><input style='width:10px' type='checkbox' id='doNothing'></td><td>Send the message or post as is</td></tr>");
			print("<tr><td><input style='width:10px' type='checkbox' id='notSend'></td><td>Not send it</td></tr>");
			print("<tr><td><input style='width:10px' type='checkbox' id='addRecipients'></td><td>Add more recipients (e.g. post to other forums or include other TAs, other classmates, etc.)</td></tr>");
			print("<tr><td><input style='width:10px' type='checkbox' id='removeRecipients'></td><td>Remove one or more of the already listed recipients before sending</td></tr>");
			print("<tr><td></td><td>".
					"<div style='margin-left:10px'>Why would you remove them? (e.g. Not bother them, not share sensitive information with them, etc.)".
					"<input style='width:250px' id='removeVal'/></div>".
					"</td></tr>");
			print("<tr><td><input style='width:10px' type='checkbox' id='findAnswer'></td><td>Try to an answer myself</td></tr>");
			print("<tr><td><input style='width:10px' type='checkbox' id='doOther'></td><td>Other (Please specify): <input style='width:250px' id='otherVal'/></td></tr>");
			print('</table>');
			print('</td></tr>');
			print('<tr><td>');
			print('<br><br>');
			print('<b>There may be other reasons other than trying to meet a deadline where you care about when you will receive a response, such as wanting to confirm people are paying attention or to plan your schedule.
					Please list any other reasons you in particular would want to know when you will receive a response.</b>');
			print('</td></tr>');
			print('<tr><td>');
			print('<textarea style="width:700px;height:50px" id="othersSituation"></textarea>');
			print('</td></tr>');
			print('</table>');
				
			print("<br>");
			print("<br>");
			
			print("<table style='width:700px'>");;
			print('<br>');
			print('<b>Would it be helpful if we were able to detect how long it normally takes for you to respond and notify you when you took longer than normal to respond to a post or message? '.
					'Why or why not?</b>');
			print('<br>');
			print('</td></tr>');
			print('<tr><td>');
			print('<textarea style="width:700px;height:50px" id="selfReason"></textarea>');
			print('</td></tr>');
			print('</table>');
				
			print("<br>");
			print("<br>");
			
			print('<br>');
			
			print('<table>');
			print('<tr><td>');
			print('<b>Please list any situations you can think of where it would be harmful or not helpful for the sender or reciever of a message to know when a response will occur.</b>');
			print('</td></tr>');
			print('<tr><td>');
			print('<textarea style="width:700px;height:50px" id="noReason"></textarea>');
			print('</td></tr>');
			print('<tr><td>');
			print('</table>');
			
			print('<br>');

			
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
	
	<input type='submit' value='Submit' style='width:100%' onclick='submitData()'>
	</div>
</html>
