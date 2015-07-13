<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title>Email Data Point Retriever</title>
		<link rel="stylesheet" href="retriever.css">
		<script src='js/jquery-1.10.2.min.js' type='text/javascript'></script>
		<script src='js/retriever.js' type='text/javascript'></script>
		<script type='text/javascript'>
			var id = $.getUrlVar('s');
            var state = $.getUrlVar('state');
			if ((id == undefined || id == null || id == "") && (state == undefined || state == null || state == "")) {
				window.location = "consent.html";
			} else if (id == undefined || id == null || id == "") {
                id = state;
            }
		</script>
	</head>
	
	<body style='height:100%'>
		<div class="center" id="retriever">
			<h1>Email Data Point Retriever</h1>
			<p>
				This tool works to study threads by collecting at most header email data, such as to, from, cc, subjects, etc., (and not email message bodies) for research purposes.
				Email threads are groups of email messages that include an original message and all of its replies and forwards.
				<br><br>
				
				We are working to study how different people communicate via  email messages or forum posts and how we can predict
				when posts or messages will receive a response. In order to
				do so, we ask participants to share limited records of how they
				have communicated via email threads in the past and/or complete
				a short survey.
				Thank you for participating
			</p>
			<p>
				We will at most retrieve the data about from, to, cc, bcc, subjects, and attachments in your email messages, and how messages are sorted into threads.
				We will <b>NOT</b> collect the content of your messages.
				<br><br>
				If you choose not to share any recipient, subject, or attachment information with us (using the checkboxes below),
				we will only collect data in the following format, where we only collect anonymous IDs for recipients, messages, and threads.
				<textarea style="display:block; width:99%; height:100px; margin:auto">Message:1 Thread:1 From:[1] Recipients:[2] Date:Jan 10 13:03
Message:1 Thread:1 From:[3] Recipients:[2] Date:Jan 09 16:31
Message:2 Thread:2 From:[2] Recipients:[4,5] Date:Thu Jan 09 16:26
Message:3 Thread:2 From:[4] Recipients:[5,2] Date:Thu Jan 09 16:14
Message:4 Thread:3 From:[6] Recipients:[2] Date:Thu Jan 09 15:54
Message:5 Thread:4 From:[7] Recipients:[2] Date:Thu Jan 09 15:08</textarea>
				<br>
				If you share subjects, recipients, or attachements with us, we will associate email addresses, subjects, or attachments with those IDs.
				<br><br>
				After we retrieve your data, we will send you an email message.  In this message,
				we will provide a link to complete a short follow-up survey and review the data that we 
				collected. This review is so that you will be able to modify and confirm what is stored by the
				researchers to ensure does not contain any sensitive or private information.
				During the review of your data, you will be able to remove part or all of your data.
			</p>
			<p>
				If you would prefer to collect all data locally on your machine, and then choose what to send us at the end, we have a desktop app 
				<a href='EmailDataRetrieverGUI.jar'>here.</a>
			</p>
			<div class='section-border'>
			<div class='section-label'><strong>Credentials</strong></div>
			<div id="retriever-form">

                <table>
					<tr>
						<td>Email Service</td>
						<td class='email-input' id="imap"><input class="selector" type="radio" name="service" onclick="displayGmail();"> Gmail
                            <input class="selector" type="radio" name="service" onclick="displayOutlook();"> Outlook or Live Mail</td>

					</tr>
					<tr>
						<td>Email or Username</td>
						<td class='email-input'><input id='username' ></td>
					</tr>
					<tr>
						<td class="outlook" style="display:none" >Password</td>
						<td class="outlook" style="display:none" class='email-input'><input  id='password' type="password" ></td>
                        <td class="gmail" style="display:none"></td>
                        <td class="gmail" style="display:none"><a id="gmail-link"
                                href=""
                                >Click Here to Login</a>
                        <script> $('#gmail-link').attr('href','https://accounts.google.com/o/oauth2/auth?scope=https://mail.google.com/&redirect_uri=http://localhost/web/index.php&response_type=code&client_id=232589280977-eu9ari61fodl29k4ctc04k4o04dbmg4o.apps.googleusercontent.com&state='+id);</script>
                        </td>
					</tr>
				</table>
				<div class='section-border' style='border-width: 0px'>
					<strong>Collection Settings</strong>
					<table class='settings'>
					<tr>
						<td>
						Max messages:
						<input class='setting short-input' type="number" id="messages" min="1" value="2000">
						</td>
						
						<td>
						Max threads:
						<input class='setting short-input' type="number" id="threads" min="1" value="400">
						</td>
					</tr>
					</table>
					<div class='settings'>
						<input class='setting checkbox' type="checkbox" id="subjects" value="include" checked>
						<label for='subjects'>Include subjects</label>
					</div>
					<div class='settings'>
						<input class='setting checkbox' type="checkbox" id="addresses" value="include" checked>
						Include email addresses
					</div>
					<table class='settings'>
					<tr>
						<td>
						<input class='setting checkbox' type="checkbox" id="numAttach" value="include" onclick='switchFileNamesEnabled()'>
						Include number of attachments
						</td>
						
						<td>
						<input class='setting checkbox' type="checkbox" id="fileNames" value="include" disabled='true'>
						<span id='fileNamesLabel'>Include file names of attachments</span>
						</td>
					</tr>
					</table>
				</div>
				<div class="collector-button">
					<input id="collect-email" type="submit" value="Collect email data"/>
                    <script>$('#collect-email ').attr('onclick','testAuthentication('+id+')');</script>
					<input type="submit" style="display:block;-webkit-appearance: button;;white-space:normal"
					value="I do not want to submit email data, but I am willing to complete a shortened version of the follow-up survey"
					onclick="switchToSurvey(id)">
					<input type="submit" id="switchToCalendar" value="Switch to collecting calendar data" onclick="switchToCalendar(id)"/>
				</div>
			</div>
		</div>
		</div>
	</body>
</html>
<?php
if(isset($_GET['code'])) {
    $data = array(
        'code' => $_GET['code'],
        'redirect_uri' => 'http://localhost/web/index.php',
        'grant_type' => 'authorization_code',
        'client_id' => '232589280977-eu9ari61fodl29k4ctc04k4o04dbmg4o.apps.googleusercontent.com',
        'client_secret' => '3V9Cjqd_Y1EqTMLGf5e8dlij'
    );

    $ch = curl_init();

    curl_setopt($ch, CURLOPT_URL, "https://accounts.google.com/o/oauth2/token");
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($data));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $server_output = curl_exec($ch);

    $result = (array)json_decode($server_output);

    curl_close($ch);

    $email = file_get_contents("https://www.googleapis.com/gmail/v1/users/me/profile?fields=emailAddress&access_token=".$result['access_token']);
    $email = (array) json_decode($email);

    print "<script>console.log('email: ". $email['emailAddress']."');</script><br>";
    print "<script>console.log('token: " .($result['access_token'])."');</script>";
    print "<script>$('#username').attr('value','".$email['emailAddress']."');</script>";
    print "<script>$('#password').attr('value','".$result['access_token']."');</script>";
    print "<script>$('#imap').val('Gmail')</script>";
}
?>
