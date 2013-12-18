<?php
session_start();

require_once 'config.php';
require_once 'google-api-php-client/src/Google_Client.php';
require_once 'google-api-php-client/src/contrib/Google_CalendarService.php';

$_SESSION['s'] = $_GET['s'];
if (!isset($_SESSION['calenderNum'])) {
	$_SESSION['calenderNum'] = 0;
} else {
	$_SESSION['calenderNum'] = $_SESSION['calenderNum'] + 1;
}
$redirect = 'https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler/php/calendar/retrieve.php';

$client = new Google_Client();
$client->setApplicationName($apiConfig['application_name']);
// Visit https://code.google.com/apis/console?api=plus to generate your
// client id, client secret, and to register your redirect uri.
$client->setClientId($apiConfig['oauth2_client_id']);
$client->setClientSecret($apiConfig['oauth2_client_secret']);
$client->setRedirectUri($redirect);
$service = new Google_CalendarService($client);

?>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title>Calendar Data Retriever</title>
		<link rel="stylesheet" href="../../retriever.css">
		<script src='../../js/jquery-1.10.2.min.js' type='text/javascript'></script>
		<script src='../../js/retriever.js' type='text/javascript'></script>
		<script type='text/javascript'>
			var id = $.getUrlVar('s');
			if (id == undefined || id == null || id == "") {
				window.location = "../../consent.html";
			}
			window.onbeforeunload = function() {
			    return "Are you sure you do not want to share any calendar data?  It would be very helpful for our research.";
			}
			function startCalendarCollector() {
				window.onbeforeunload = null;
				<?php 
					$authUrl = $client->createAuthUrl();
					print "location.href='$authUrl';";
				?>
			}
		</script>
	</head>
	
	<body>
		<div class="center" id="retriever">
			<h1>Calendar Data Retriever</h1>
			<p>
				This portion of the tool collects your Google Calendar data. This will help us compare when
				you respond to when you mark your availability on your calendar.
			</p>
			
			<p>
				<input TYPE='button' VALUE='Authorize access to calendar data' onclick="startCalendarCollector()">
			</p>
			</div>
		</div>
		</div>
	</body>
</html>
		