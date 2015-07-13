<?php
session_start();

require_once 'config.php';
require_once 'google-api-php-client/src/Google_Client.php';
require_once 'google-api-php-client/src/contrib/Google_CalendarService.php';


$oauth_redirect = rootAddress().'/php/calendar/retrieve.php';
$calendar_entry_point = rootAddress().'/php/calendar/index.php';

$client = new Google_Client();
$client->setApplicationName($apiConfig['application_name']);
// Visit https://code.google.com/apis/console?api=plus to generate your
// client id, client secret, and to register your redirect uri.
$client->setClientId($apiConfig['oauth2_client_id']);
$client->setClientSecret($apiConfig['oauth2_client_secret']);
$client->setRedirectUri($oauth_redirect);
$service = new Google_CalendarService($client);

function rootAddress () {
    if  (strncmp($_SERVER['HTTP_HOST'], 'localhost',9)==0)
        return 'http://'.$_SERVER['HTTP_HOST'].'/web';
    elseif (strcmp($_SERVER['HTTP_HOST'],'wwwx.cs.unc.edu')==0)
        return 'https://'.$_SERVER['HTTP_HOST'].'/~andrewwg/emailsampler/web';
    return $_SERVER['HTTP_HOST'];
}

function getEvents($service, $maxMonths) {
	
	$optParams = array(
			'timeZone' => 'America/New_York',
			'timeMin' => date("Y-m-d\TH:i:sP", strtotime('-'.$maxMonths.' months')),
			'timeMax'=>  date("Y-m-d\TH:i:sP"),
			'singleEvents' => true,
	);
	
	
	$events = $service->events->listEvents('primary', $optParams);
	$events_list = $events['items'];
	$prev_events_list = array();
	$events_count = 0;
	while (array_key_exists('nextPageToken', $events)) {
		array_push($prev_events_list, $events_list);
		$optparams['pageToken'] = $events['nextPageToken'];
		$events = $service->events->listEvents('primary', $optParams);
		$events_list = $events['items'];
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

	return $events_list;
}

function g_calendar_date($calendar_date) {
	if (isset($calendar_date['dateTime'])) {
		return $calendar_date['dateTime'];
	} else {
		return $calendar_date['date'];
	}
}

function eventStrList($events_list, $include_event_names, $include_attendee_names) {
	$events_str_list = array();
	foreach ($events_list as $event) {
		$str_val = '';
		if ($include_event_names) {
			$str_val .= 'name: '.$event['summary']."\n";
		}
		$str_val .= 'status: '.$event['status']."\n";
		
		if ($include_attendee_names) {
			if (isset($event['creator']) && isset($event['creator']['email'])) {
				$str_val .= 'creator: '.$event['creator']['email']."\n";
			}
			if (isset($event['organizer']) && isset($event['organizer']['email'])) {
				$str_val .= 'organizer: '.$event['organizer']['email']."\n";
			}
		}
		if (isset($event['creator']) && isset($event['creator']['self'])) {
			$str_val .= 'creator-is-self: '.$event['creator']['self']."\n";
		}
		if (isset($event['organizer']) && isset($event['organizer']['self'])) {
			$str_val .= 'organizer-is-self: '.$event['organizer']['self']."\n";
		}
		$str_val .= 'start: '.g_calendar_date($event['start'])."\n";
		$str_val .= 'end: '.g_calendar_date($event['end'])."\n";
		if (isset($event['endTimeUnspecified'])) {
			$str_val .= 'endTimeUnspecified: '.$event['endTimeUnspecified']."\n";
		}

		$str_val .= 'attendees:'."\n";
		if (isset($event['attendees'])) {
			foreach ($event['attendees'] as $attendee) {
				if (isset($attendee['email']) || isset($attendee['responseStatus'])) {
					if ($include_attendee_names) {
						if (isset($attendee['email'])) {
							$str_val .= "\temail: ".$attendee['email']."\n";
						}
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
		}

		array_push($events_str_list, $str_val);
	}
	return $events_str_list;
}

if (!isset($_SESSION['s'])) {
	header('Location: ' . filter_var($calendar_entry_point, FILTER_SANITIZE_URL));
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
	$authUrl = $client->createAuthUrl();
	$user_id = $_SESSION['s'];
	header('Location: ' . filter_var($calendar_entry_point."?s=$user_id", FILTER_SANITIZE_URL));
}

if (isset($_GET['m'])) {
	$_SESSION['m'] = intval($_GET['m']);
}
if (isset($_GET['e'])) {
	$_SESSION['e'] = $_GET['e'] == 'true';
}
if (isset($_GET['a'])) {
	$_SESSION['a'] = $_GET['a'] == 'true';
}

// We're not done yet. Remember to update the cached access token.
// Remember to replace $_SESSION with a real database or memcached.
$_SESSION['token'] = $client->getAccessToken();

?>