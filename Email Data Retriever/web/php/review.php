<html>
	
	<?php 
		$record_id = $_GET['r'];
		$anonymous_folder= '/afs/cs.unc.edu/home/bartel/email_threads/anonymous_data/'.$record_id;
		$private_folder= '/afs/cs.unc.edu/home/bartel/email_threads/private_data/'.$record_id;
		
		$messages_file = $anonymous_folder.'/messages.txt';
		$msgs_exist = file_exists($messages_file);
		
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
		
		$messages_data = '';
		if ($msgs_exist) {
			$file_handle = fopen($messages_file, "r");
			while (!feof($file_handle)) {
				$line = fgets($file_handle);
				$messages_data .= $line;
			}
			fclose($file_handle);
		}
		
		$addresses_data = '';
		if ($addr_exist) {
			$file_handle = fopen($addresses_file, "r");
			while (!feof($file_handle)) {
				$line = fgets($file_handle);
				$addresses_data .= $line;
			}
			fclose($file_handle);
		}
		
		$subjects_data = '';
		if ($subj_exist) {
		$file_handle = fopen($subjects_file, "r");
			while (!feof($file_handle)) {
				$line = fgets($file_handle);
				$subjects_data .= $line;
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
		
		$col_width = (($num_columns > 0)? 800/$num_columns: 0).'px';
		
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
			print("<input style='width: 10px' type='checkbox' name='".strval($item_id)."_notAnswer' onclick='hideTimeQuestions(".strval($item_id).")'>");
			print("<i>I would rather not answer questions about these messages</i>");
			print('<table id="'.strval($item_id).'_questions">');
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
		
		function writeColumn($exists, $data, $label, $width, $includeRemove) {
			if ($exists) {
				print('<td style="width:'.$width.'">');
				print('<center>');
				print('<h3>'.$label.'</h3>');
				print('<textarea style="width:100%;height:400px;white-space:nowrap;overflow:auto;" readonly="true">');
				print($data);
				print('</textarea></center>');
				print('<br>');
				if ($includeRemove) {
					print('<input class="setting checkbox" type="checkbox" id="remove'.$label.'">');
					print('Remove '.$label.' Data');
				}
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
				message  = "Thank you for contributing.  You may now close this tab";
				<?php 
					if ($msgs_exist) {
						print('message += " or continue to review your shared data";');
					}
				?>
				$.get(dest, function() {
						alert(message);
						window.location.reload();
					});
			}

			function hideTimeQuestions(id) {
				if ($('input[name="'+id+'_notAnswer"]').prop('checked')) {
					$('#'+id+"_questions").css('display', 'none');
				} else {
					$('#'+id+"_questions").css('display', 'inline');
				}
			}

			function toggleVisible(id, isVisible) {
				var item = $('#'+id);
				if (isVisible) {
					item.css('display', 'block');
				} else {
					item.css('display', 'none');
				}
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
					surveyData[i] = {};
					var question_id = ""+i+"_notAnswer";
					checkedVal = $('input[name="'+question_id+'"]:checked').val();
					if (checkedVal != undefined) {
						surveyData[i]["notAnswered"] = checkedVal;;
					} else {
						surveyData[i]["notAnswered"] = 'unanswered';
					}
					for (j = 1; j <= 8; j++) {
						question_id = "" + i  + "_" + j;
						checkedVal = $('input[name="'+question_id+'"]:checked').val();
						if (checkedVal != undefined) {
							surveyData[i][j] = checkedVal;;
						} else {
							surveyData[i][j] = 'unanswered';
						}
					}
				}

				surveyData.deadline = {};
				surveyData.deadline.meeting = $('#deadline_meeting').prop('checked');
				surveyData.deadline.clarifying = $('#deadline_clarifying').prop('checked');
				surveyData.deadline.collaborate = $('#deadline_collaborate').prop('checked');
				surveyData.deadline.information = $('#deadline_information').prop('checked');
				surveyData.deadline.other = {};
				surveyData.deadline.other.checked = $('#deadline_other').prop('checked');
				surveyData.deadline.other.val = $('#deadline_other_val').val();
				surveyData.deadline.elaboration = $('#deadline_elaboration').val();

				surveyData.reaction = {};
				surveyData.reaction.remove = {};
				surveyData.reaction.remove.checked = $('#reaction_remove').prop('checked');
				surveyData.reaction.keep = {};
				surveyData.reaction.keep.checked = $('#reaction_keep').prop('checked');
				surveyData.reaction.add = $('#reaction_add').prop('checked');
				surveyData.reaction.notSend = $('#reaction_notSend').prop('checked');
				surveyData.reaction.change = $('#reaction_switch').prop('checked');
				surveyData.reaction.find = {};
				surveyData.reaction.find.checked = $('#reaction_find').prop('checked');
				surveyData.reaction.other = {};
				surveyData.reaction.other.checked = $('#reaction_other').prop('checked');
				surveyData.reaction.other.val = $('#reaction_other_val').val();
				surveyData.reaction.elaboration = $('#reaction_elaboration').val();

				
				surveyData.reaction.remove.reason = {};
				surveyData.reaction.remove.reason.bother = $('#removeReason_bother').prop('checked');
				surveyData.reaction.remove.reason.privacy = $('#removeReason_privacy').prop('checked');
				surveyData.reaction.remove.reason.other = {};
				surveyData.reaction.remove.reason.other.checked = $('#removeReason_other').prop('checked');
				surveyData.reaction.remove.reason.other.val = $('#removeReason_other_val').val();

				surveyData.reaction.keep.reason = {};
				surveyData.reaction.keep.reason.error = $('#keepReason_error').prop('checked');
				surveyData.reaction.keep.reason.useful = $('#keepReason_useful').prop('checked');
				surveyData.reaction.keep.reason.other = {};
				surveyData.reaction.keep.reason.other.checked = $('#keepReason_other').prop('checked');
				surveyData.reaction.keep.reason.other.val = $('#keepReason_other_val').val();

				surveyData.reaction.find.search = $('#findAnswer_search').prop('checked');
				surveyData.reaction.find.im = $('#findAnswer_im').prop('checked');
				surveyData.reaction.find.phone = $('#findAnswer_phone').prop('checked');
				surveyData.reaction.find.meetRecipient = $('#findAnswer_meetRecipient').prop('checked');
				surveyData.reaction.find.meetOthers = $('#findAnswer_meetOthers').prop('checked');
				surveyData.reaction.find.other = {};
				surveyData.reaction.find.other.checked = $('#findAnswer_other').prop('checked');
				surveyData.reaction.find.other.val = $('#findAnswer_other_val').val();

				surveyData.other = {};
				surveyData.other.ignored = $('#other_ignored').prop('checked');
				surveyData.other.schedule = $('#other_schedule').prop('checked');
				surveyData.other.ok = $('#other_ok').prop('checked');
				surveyData.other.excitement = $('#other_excitement').prop('checked');
				surveyData.other.reliable = $('#other_reliable').prop('checked');
				surveyData.other.other = {};
				surveyData.other.other.checked = $('#other_other').prop('checked');
				surveyData.other.other.val = $('#other_other_val').val();
				surveyData.other.elaboration = $('#other_elaboration').val();

				surveyData.self = {};
				surveyData.self.judged = $('#self_judged').prop('checked');
				surveyData.self.opportunity = $('#self_opportunity').prop('checked');
				surveyData.self.other = {};
				surveyData.self.other.checked = $('#self_other').prop('checked');
				surveyData.self.other.val = $('#self_other_val').val();
				surveyData.self.elaboration = $('#self_elaboration').val();

				surveyData.harm = {};
				surveyData.harm.privacy = $('#harm_privacy').prop('checked');
				surveyData.harm.error = $('#harm_error').prop('checked');
				surveyData.harm.reminder = $('#harm_reminder').prop('checked');
				surveyData.harm.other = {};
				surveyData.harm.other.checked = $('#harm_other').prop('checked');
				surveyData.harm.other.val = $('#harm_other_val').val();
				surveyData.harm.elaboration = $('#harm_elaboration').val();

				surveyData.comments = $('#additional_comments').val();

				console.dir(surveyData);

				dest = "https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler/php/survey.php";
				$.post(dest, surveyData);
			}

			function submitData() {
				<?php
					if (!$survey_results_exist) {
						print ("storeSurveyData();");
					}
				?>
				removeData();
			}
			
		</script>
	</head>
	<div class="center" id="reviewer">
	<?php 
		if (!$survey_results_exist) {
			print('<h1>Please complete this short survey about response time</h1>');
			
			print('<div style="width:650px">This study is about predicting if and when you will respond or receive a response to an email message or forum post. '.
					'For the purposes of this survey, please assume there is already some tool '.
					'that is able to predict if and when a response will occur and that can appropriately '.
					'display the prediction.</div>');
			
			print ('<br><br>');
			
			if ($survey_exist) {
				for ($i = 0; $i < $survey_count; $i++) {
					writeSurveyQuestions($survey_items[$i], $i);
					print "<br>";
				}
			
			}
		}
	?>
	
	<div id="shortAnswer" style="display:none">

		<table style='width: 700px'>
			<tr>
				<td>
					<b> 
						Have you been in any of following situation(s) where you needed
						a response to an email or a post on an online forum (such as
						Piazza or Stack Overflow) quickly enough to meet some deadline?
						
						<?php
							if ($survey_exist) {
								print('The specific emails above may fit into these situations.');
							}
						?>
					</b>
				</td>
			</tr>
			<tr>
				<td>
					<table>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' name='deadlineSituation'
								onclick="toggleVisible('shortAnswerDeadlines', $(&quot;input[type='checkbox'][name='deadlineSituation']:checked&quot;).length > 0);"
								id='deadline_meeting'>
							<td>
								Coordinating with people about meeting later
							</td>
						</tr>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' name='deadlineSituation'
								onclick="toggleVisible('shortAnswerDeadlines', $(&quot;input[type='checkbox'][name='deadlineSituation']:checked&quot;).length > 0);"
								id='deadline_clarifying'></td>
							<td>
								Clarifying assignments or projects with professors, TAs, bosses, colleagues, coworkers, or others before they were due
							</td>
						</tr>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' name='deadlineSituation'
								onclick="toggleVisible('shortAnswerDeadlines', $(&quot;input[type='checkbox'][name='deadlineSituation']:checked&quot;).length > 0);"
								id='deadline_collaborate'></td>
							<td>
								 Coordinating with colleagues, coworkers, or others about upcoming assignments or projects you are collaborating on
							</td>
						</tr>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' name='deadlineSituation'
								onclick="toggleVisible('shortAnswerDeadlines', $(&quot;input[type='checkbox'][name='deadlineSituation']:checked&quot;).length > 0);"
								id='deadline_information'></td>
							<td>
								Getting necessary information from professors, TAs, bosses, colleagues, coworkers, or others before meetings, presentations, exams, or quizzes
							</td>
						</tr>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' name='deadlineSituation'
								onclick="toggleVisible('shortAnswerDeadlines', $(&quot;input[type='checkbox'][name='deadlineSituation']:checked&quot;).length > 0);"
								id='deadline_other'></td>
							<td>
								Other (Please Specify): <input style='width: 250px' 
									id='deadline_other_val' />
							</td>
						</tr>
					</table>
					</td></tr>
			<tr>
				<td><b>Please elaborate on your answer (e.g. give details about your selected option(s) or reasons why you did not select any of the above options)</b></td>
			</tr>
			<tr>
				<td><textarea style="width: 700px; height: 50px"
						id="deadline_elaboration"></textarea>
				</td>
			</tr>
			<table id="shortAnswerDeadlines" style="margin-top:0px; display:none">
			<tr>
				<td>
					<br> 
					<b> 
						In the above situation(s) that you selected, suppose that as
						you were composing your message or post we predicted if and when
						you would receive a response (with a small chance of error).
						Based on that prediction, assume you determined that the response would not
						arrive quickly enough for you to meet your deadline. Would you do
						any of the following?
					</b>
					<br>
					(You may select more than one)
				</td>
			</tr>
			<tr>
				<td>
					<table>
						<tr>
							<td><input style='width: 10px' type='checkbox' 
								onclick='toggleVisible("removeReason",  $("#reaction_remove").prop("checked"));'
								id='reaction_remove'></td>
							<td>
								I would remove one or more of the already listed recipients
								before sending (you may not want to bother them, share
								sensitive information with them, etc.).
							</td>
						</tr>
						<tr>
							<td></td>
							<td>
								<div id='removeReason' style='display:none; margin: 10px 40px'>
									<b>Why would you remove them?</b>
									<table>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='removeReason_bother'></td>
											<td>I would not want to bother them.</td>
										</tr>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='removeReason_privacy'></td>
											<td>I would want to avoid unnecessarily sharing sensitive or private information with them.</td>
										</tr>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='removeReason_other'></td>
											<td>Other (Please Specify): <input style='width: 250px' 
													id='removeReason_other_val' /></td>
										</tr>
									</table>
								</div>
							</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox'
								onclick='toggleVisible("keepReason",  $("#reaction_keep").prop("checked"));'
								id='reaction_keep'></td>
							<td>
								I would keep one or more of the original recipients (because there is a small
								chance of error, the sent information would still be useful to
								the readers, etc.).
							</td>
						</tr>
						<tr>
							<td></td>
							<td>
								<div id='keepReason' style='display:none; margin: 10px 40px'>
									<b>Why would you keep them?</b>
									<table>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='keepReason_error'></td>
											<td>There is a small chance of error.</td>
										</tr>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='keepReason_useful'></td>
											<td>The sent or posted information would still be useful to the readers.</td>
										</tr>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='keepReason_other'></td>
											<td>Other (Please Specify): <input style='width: 250px' 
													id='keepReason_other_val' /></td>
										</tr>
									</table>
								</div>
							</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox' id='reaction_add'>
							</td>
							<td>
								I would send it to more people. (in forums, post to other forums; in email,
								include other recipients such as TAs, other classmates, etc.)
							</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox' id='reaction_notSend'></td>
							<td>I would not send or post it.</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox' id='reaction_switch'>
							</td>
							<td>
								If the message was an email, I would post it on a forum, and if it was a forum post, I would send it via email.
							</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox'
								onclick='toggleVisible("findAnswer",  $("#reaction_find").prop("checked"));'
								id='reaction_find'></td>
							<td>
								I would use means other than sending an email or posting on forums
								(e.g. searching Google, meeting someone n person, sending an IM) to
								find an answer.
							</td>
						</tr>
						<tr>
							<td></td>
							<td>
								<div id='findAnswer' style='display:none; margin: 10px 40px'>
									<b>How would you find your answer?</b>
									<table>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='findAnswer_search'></td>
											<td>Search engine (Google, etc.)</td>
										</tr>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='findAnswer_im'></td>
											<td>Sending an instant message</td>
										</tr>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='findAnswer_phone'></td>
											<td>Call someone on the phone</td>
										</tr>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='findAnswer_meetRecipient'></td>
											<td>Meet in person with recipient(s)</td>
										</tr>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='findAnswer_meetOthers'></td>
											<td>Meet in person with someone else</td>
										</tr>
										<tr>
											<td><input style='width: 10px' type='checkbox' id='findAnswer_other'></td>
											<td>Other (Please Specify): <input style='width: 250px' 
													id='findAnswer_other_val' />
											</td>
										</tr>
									</table>
								</div>
							</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox' id='reaction_other'></td>
							<td>Other (Please specify): <input style='width: 250px'
								id='reaction_other_val' />
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td><b>Please elaborate on your answer (e.g. give details about your selected option(s) or reasons why you did not select any of the above options)</b></td>
			</tr>
			<tr>
				<td><textarea style="width: 700px; height: 50px"
						id="reaction_elaboration"></textarea>
				</td>
			</tr>
			</table>
				</td>
			</tr>
			<tr>
				<td>
					<br>
					<br>
					<b>
						There may be reasons other than trying to meet a deadline where you care
						about if and when you will receive a response. Please select any other
						reasons why you in particular would want to know if and when you will
						receive a response.
					</b>
				</td>
			</tr>
			<tr>
				<td><table>
						<tr>
							<td><input style='width: 10px' type='checkbox' id='other_ignored'>
							
							<td>I would need to confirm people were not ignoring me.</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox'
								id='other_schedule'>
							<td>Knowing if and when responses will occur would help me plan my schedule.</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox'
								id='other_ok'>
							<td>I would need to make sure everything is ok with my recipient(s).</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox'
								id='other_excitement'>
							<td>I would like to determine if people are excited about my message or post.</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox'
								id='other_reliable'>
							<td>It would help me determine how reliable people are.</td>
						</tr>
						<tr>
							<td><input style='width: 10px' type='checkbox' id='other_other'>
							</td>
							<td>Other (Please Specify): <input style='width: 250px'
								id='other_other_val' />
							</td>
						</tr>
					</table></td>
			</tr>
			<tr>
				<td><b>Please elaborate on your answer (e.g. give details about your selected option(s) or reasons why you did not select any of the above options)</b></td>
			</tr>
			<tr>
				<td><textarea style="width: 700px; height: 50px"
						id="other_elaboration"></textarea>
				</td>
			</tr>
		</table>

		<br> <br>

		<table style='width: 700px'>
			<tr><td>
				<br>
				<b>
					Suppose we were able to predict (with a small chance of error) how long it
					would take you to respond to a particular post or message and notify you when
					you took longer than normal to respond. Which, if any, of the following
					situations have you experienced where that would helpful? 
				</b>
				<br>
			</td></tr>
			<tr><td>
				<table>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' id='self_judged'>
							<td>
								I needed to ensure I would not be judged poorly.
							</td>
						</tr>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' id='self_opportunity'>
							<td>
								I needed to ensure I would not miss some opportunity.
							</td>
						</tr>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' id='self_other'></td>
							<td>
								Other (Please Specify): <input style='width: 250px'	id='self_other_val' />
							</td>
						</tr>
					</table>
			</td></tr>
			<tr>
				<td><b>Please elaborate on your answer (e.g. give details about your selected option(s) or reasons why you did not select any of the above options)</b></td>
			</tr>
			<tr>
				<td><textarea style="width: 700px; height: 50px" id="self_elaboration"></textarea>
				</td>
			</tr>
		</table>

		<br> <br> <br>

		<table>
			<tr>
				<td>
					<b>
						Based on your experience, how might predicting if and when
						responses occur for senders or receivers be useless or harmful?
					</b>
				</td>
			</tr>
			<tr><td>
				<table>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' id='harm_privacy'>
							<td>
								A sender can determine private information about the receiver(s)
								based on the predicted response times.
							</td>
						</tr>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' id='harm_error'>
							<td>
								Because of potential error, senders or receivers may take wrong
								actions or have unreasonable expectations.
							</td>
						</tr>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' id='harm_reminder'>
							<td>
								Others may already remind senders if they miss or are close to missing a deadline. 
							</td>
						</tr>
						<tr>
							<td>
								<input style='width: 10px' type='checkbox' id='harm_other'></td>
							<td>
								Other (Please Specify): <input style='width: 250px'	id='harm_other_val' />
							</td>
						</tr>
					</table>
			</td></tr>
			<tr>
				<td><b>Please elaborate on your answer (e.g. give details about your selected option(s) or reasons why you did not select any of the above options)</b></td>
			</tr>
			<tr>
				<td><textarea style="width: 700px; height: 50px" id="harm_elaboration"></textarea>
				</td>
			</tr>
		
		</table>
		
		<br>

		<table>
			<tr>
				<td>
					<b>
						If you have any additional comments or feedback, please type it in the box below.
					</b>
				</td>
			</tr>
			<tr>
				<td><textarea style="width: 700px; height: 50px" id="additional_comments"></textarea>
				</td>
			</tr>
		</table>
		<br>

	</div>
	
	<script type="text/javascript">
		<?php 
			if (!$survey_results_exist) {
				print('$("#shortAnswer").css("display","inline");');
			}
		?>
	</script>

	
	<div id='submittedData'>
	<h1>Review your retrieved email data below</h1>
	<table style='width:100%'>
		<?php 
			writeColumn($msgs_exist, $messages_data, 'Messages and Threads', '100%', false);
		?>
	</table>
	<table style='border-spacing:10'>
		<tr>
			<?php 
				writeColumn($addr_exist, $addresses_data, 'Addresses', $col_width, true);
				writeColumn($subj_exist, $subjects_data, 'Subjects', $col_width, true);
				writeColumn($attach_exist, $attach_data, 'Attachments', $col_width, true);
			?>
		
		</tr>
	</table>
	
	<input class="setting checkbox" type="checkbox" id="removeAll">
	Remove all data about messages and threads.
	</div>
	
	<?php 
		if ($msgs_exist || !$survey_results_exist) {
			print ("<input type='submit' value='Submit' style='width:100%' onclick='submitData()'>");
		} else {
			print ("Thank you for your contribution.  You may now close this tab or window.");
		}
	?>
	
	</div>
	</div>
	
	<script type="text/javascript">
		<?php 
			if (!$msgs_exist) {
				print("$('#submittedData').css('display', 'none')");
			}
		?>
	</script>
</html>
