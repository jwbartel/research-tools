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
	$authUrl = $client->createAuthUrl();
	print "<a href='$authUrl'>Connect Me!</a>";
}else {
	//header('Location: https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler') ;
	
	print $_SESSION['token'];
	$calendars = $service->events->listEvents('primary');
	print 'Your Calendars: <pre>' . print_r($calendars, true) . '</pre>';

	// We're not done yet. Remember to update the cached access token.
	// Remember to replace $_SESSION with a real database or memcached.
	$_SESSION['token'] = $client->getAccessToken();
} 

?>