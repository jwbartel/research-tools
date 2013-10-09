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

if (isset($_GET['code'])) {
	$client->authenticate();
	$_SESSION['token'] = $client->getAccessToken();
	$redirect = 'http://' . $_SERVER['HTTP_HOST'] . $_SERVER['PHP_SELF'];
	header('Location: ' . filter_var($redirect, FILTER_SANITIZE_URL));
}

if (isset($_SESSION['token'])) {
	$client->setAccessToken($_SESSION['token']);
}


if (!($client->getAccessToken())){
	header('Location: https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler/php/google-auth/') ;
	$authUrl = $client->createAuthUrl();
	print "<a href='$authUrl'>Connect Me!</a>";
}else {
	//header('Location: https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler') ;
	
	$maxEvents = $_GET['events'];
	if ( is_null($maxEvents) || strcmp($maxEvents,"") == 0) {
		$maxEvents = 50;
	}
	$optParams = array(
		'timeZone' => 'America/New_York',
 		'timeMax'=>  date("Y-m-d\TH:i:sP"),
// 		'timeMin'=>  date("Y-m-d\TH:i:sP",0),
		'singleEvents' => true,
	);
	$events = $service->events->listEvents('primary', $optParams);
	$events_list = array_reverse($events['items']);
	print 'Your Events: <pre>' . print_r($events, true) . '</pre>';
	
// 	for( $i=0; $i<max_events; $i++) {
// 		print print_r($events_list[$i],true);
// 		print '\n';
// 	}

	// We're not done yet. Remember to update the cached access token.
	// Remember to replace $_SESSION with a real database or memcached.
	$_SESSION['token'] = $client->getAccessToken();
} 

?>