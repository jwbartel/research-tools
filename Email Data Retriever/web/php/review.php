<html>
	
	<head>
		<title>Email Thread Data Review</title>
		<link rel="stylesheet" href="review.css">
	</head>
	
	<?php 
		$record_id = $_GET['r'];
		$anonymous_folder= '/afs/cs.unc.edu/home/bartel/public_html/email_threads/anonymous data/'.$record_id;
		$private_folder= '/afs/cs.unc.edu/home/bartel/public_html/email_threads/private data/'.$record_id;
		$messages_file = $anonymous_folder.'/messages.txt';
		$addresses_file = $private_folder.'/addresses.txt';
		$addr_exist = file_exists($addresses_file);
		$subjects_file = $anonymous_folder.'/subjects.txt';
		$subj_exist = file_exists($subjects_file);
		$attachments_file = $anonymous_folder.'/attachments.txt';
		$attach_exist = file_exists($attachments_file);
		
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
		
		$col_width = 800/$num_columns;
		
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
	<div class="center" id="reviewer">
	<h1>Review Retrived Thread data</h1>
	<table style='border-spacing:10'>
		<tr>
			<?php 
				writeColumn($addr_exist, $addresses_data, 'Addresses', $col_width);
				writeColumn($subj_exist, $subjects_data, 'Subjects', $col_width);
				writeColumn($attach_exist, $attach_data, 'Attachments', $col_width);
			?>
		
		</tr>
	</table>
	
	<center>
	
		<?php 
			if (!($addr_exist || $subj_exist || $attach_exist)) {
				print('<h3>No data about email addresses, subjects, or attachments were collected.</h3>');
			}
		?>
		<input class="setting checkbox" type="checkbox" id="removeAllData">
		Remove all collected data about messages, threads, and dates.
	</center>
	
	<input type='submit' value='Submit changes to retrieved data' style='width:100%'>
	</div>
</html>