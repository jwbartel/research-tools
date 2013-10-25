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

function eventStrList($events_list) {
	$events_str_list = array();
	foreach ($events_list as $event) {
		$str_val = '';
		if (isset($event['visibility'])) {
			$str_val .= 'visibility: '.$event['visibility']."\n";
		} else {
			$str_val .= 'visibility: default'."\n";
		}
		$str_val .= 'summary: '.$event['summary']."\n";
		$str_val .= 'status: '.$event['status']."\n";
		$str_val .= 'created: '.$event['created']."\n";
		$str_val .= 'updated: '.$event['updated']."\n";
		if (isset($event['creator']) && isset($event['creator']['email'])) {
			$str_val .= 'creator: '.$event['creator']['email']."\n";
		}
		if (isset($event['creator']) && isset($event['creator']['self'])) {
			$str_val .= 'creator-is-self: '.$event['creator']['self']."\n";
		}
		if (isset($event['organizer']) && isset($event['organizer']['email'])) {
			$str_val .= 'organizer: '.$event['organizer']['email']."\n";
		}
		if (isset($event['organizer']) && isset($event['organizer']['self'])) {
			$str_val .= 'organizer-is-self: '.$event['organizer']['self']."\n";
		}
		$str_val .= 'start: '.$event['start']['dateTime']."\n";
		$str_val .= 'end: '.$event['end']['dateTime']."\n";
		if (isset($event['endTimeUnspecified'])) {
			$str_val .= 'endTimeUnspecified: '.$event['endTimeUnspecified']."\n";
		}

		$str_val .= 'attendees:'."\n";
		foreach ($event['attendees'] as $attendee) {
			if (isset($attendee['email']) || isset($attendee['responseStatus'])) {
				if (isset($attendee['email'])) {
					$str_val .= "\temail: ".$attendee['email']."\n";
				}
				if (isset($attendee['responseStatus'])) {
					$str_val .= "\tresponseStatus: ".$attendee['responseStatus']."\n";
				}
				if (isset($attendee['self'])) {
					$str_val .= "\tself: ".$attendee['self']."\n";
				}
				$str_val .= "\t\n";
			}
		}
		
		array_push($events_str_list, $str_val);
	}
	return $events_str_list;
}

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
	
	$maxMonths = 12;
	if (isset($_GET['maxMonths'])) {
		$maxMonths = $_GET['maxMonths'];
	}
	
	$optParams = array(
		'timeZone' => 'America/New_York',
		'timeMin' => date("Y-m-d\TH:i:sP", strtotime('-'.$maxMonths.' months')),
 		'timeMax'=>  date("Y-m-d\TH:i:sP"),
 		'singleEvents' => true,
	);
	print 'retrieving events....';
	$events = $service->events->listEvents('primary', $optParams);
	$events_list = array_reverse($events['items']);
	$prev_events_list = array();
	print 'done<br>';
	$events_count = 0;
	while (array_key_exists('nextPageToken', $events)) {
		print 'retrieving events....';
		array_push($prev_events_list, $events_list);
		$optparams['pageToken'] = $events['nextPageToken'];
		$events = $service->events->listEvents('primary', $optParams);
		$events_list = array_reverse($events['items']);
		print 'done<br>';
		$count++;
		if ($count > 3){
			break;
		}
	}
	
 	if (count($prev_events_list) > 0) {
		$prevEventsPos = count($prev_events_list)-1;
		$pos = 0;
		while ($prevEventsPos >= 0) {
			array_push($events_list, $prev_events_list[$prevEventsPos][$pos]);
			$pos++;
			if ($pos >= count($prev_events_list[$prevEventsPos])) {
				$pos = 0;
				$prevEventsPos --;
			}
		}
	}
	//print 'Your Events: <pre>' . print_r($events_list, true) . '</pre>';
	print 'Your Events: <pre>' . print_r(eventStrList($events_list), true) . '</pre>';

	// We're not done yet. Remember to update the cached access token.
	// Remember to replace $_SESSION with a real database or memcached.
	$_SESSION['token'] = $client->getAccessToken();
} 

?>