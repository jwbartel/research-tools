<?php
session_start();

require_once 'config.php';
require_once 'google-api-php-client/src/Google_Client.php';
require_once 'google-api-php-client/src/contrib/Google_CalendarService.php';

$client = new Google_Client();
$client->setApplicationName($apiConfig['application_name']);
// Visit https://code.google.com/apis/console?api=plus to generate your
// client id, client secret, and to register your redirect uri.
$client->setClientId($apiConfig['oauth2_client_id']);
$client->setClientSecret($apiConfig['oauth2_client_secret']);
$client->setRedirectUri($apiConfig['oauth2_redirect_uri']);
$service = new Google_CalendarService($client);

?>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title>Email Data Point Retriever</title>
		<link rel="stylesheet" href="../../retriever.css">
		<script src='../../js/jquery-1.10.2.min.js' type='text/javascript'></script>
		<script src='../../js/retriever.js' type='text/javascript'></script>
		<script type='text/javascript'>
			var id = $.getUrlVar('s');
			if (id == undefined || id == null || id == "") {
				window.location = "../../consent.html";
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
				<?php 
					$authUrl = $client->createAuthUrl();
					print "<a href='$authUrl'>Please authorize us access to your calendar data with this link</a>";
				?>
			</p>
			</div>
		</div>
		</div>
	</body>
</html>
		