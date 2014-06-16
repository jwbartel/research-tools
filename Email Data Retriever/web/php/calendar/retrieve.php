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
        return 'https://'.$_SERVER['HTTP_HOST'].'/web';
    elseif (strcmp($_SERVER['HTTP_HOST'],'wwwx.cs.unc.edu')==0)
        return 'https://'.$_SERVER['HTTP_HOST'].'/~bartel/cgi-bin/emailsampler';
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


if(!isset($_SERVER['HTTPS']) || $_SERVER['HTTPS'] == "") {
	header('Location: '.rootAddress().'/php/calendar/retrieve.php');
}

$maxMonths = 12;
if (isset($_SESSION['m'])) {
	$maxMonths = $_SESSION['m'];
} else {
	$_SESSION['m'] = $maxMonths;
}

$includeEventNames = true;
if (isset($_SESSION['e'])) {
	$includeEventNames = $_SESSION['e'];
} else {
	$_SESSION['e'] = $includeEventNames;
}

$includeAttendeeNames = true;
if (isset($_SESSION['a'])) {
	$includeAttendeeNames = $_SESSION['a'];
} else {
	$_SESSION['a'] = $includeAttendeeNames;
}


// //print 'Your Events: <pre>' . print_r($events_list, true) . '</pre>';
// $events_list = getEvents($maxMonths);
// print 'Your Events: <pre>' . print_r(eventStrList($events_list), true) . '</pre>';

// We're not done yet. Remember to update the cached access token.
// Remember to replace $_SESSION with a real database or memcached.
// $_SESSION['token'] = $client->getAccessToken();

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
			function completeSubmission() {
				<?php print 'location.href = "'.rootAddress().'/complete.html";'; ?>
			}
		</script>
	</head>

	<body>
		<div class="center" id="retriever">
			<h1>Calendar Data Retriever</h1>
			<p>
				The following is your calendar data that we retrieved
			</p>

			<div id="retriever-form">
				<table class='settings'>
					<tr>
						<td>
						Months of data to retrieve:
						<?php
						print "<input class='setting short-input' type='number' id='months' min='1' value='$maxMonths'>"
						?>
						</td>
					</tr>
					</table>
				<div class='settings'>
					<?php
					print "<input class='setting checkbox' type='checkbox' id='event_names' value='include' ";
					if ($includeEventNames){
						print "checked";
					}
					print ">";
					?>
					<label for='event_names'>Include event names</label>
				</div>
				<div class='settings'>
					<?php
					print "<input class='setting checkbox' type='checkbox' id='attendee_names' value='include' ";
					if ($includeAttendeeNames){
						print "checked";
					}
					print ">";
					?>
					<label for='event_names'>Include attendee names</label>
				</div>
				<div class="collector-button">
					<input type="submit" value="Update" onclick="collectCalendarData()"/>
				</div>
			</div>

			<br>

			<?php
			$out_folder = "/afs/cs.unc.edu/home/bartel/email_threads/";
			$private_folder = $out_folder.'private_data/'.$_SESSION['s'].'/';
			if (!file_exists($private_folder)) {
				mkdir($private_folder);
			}

			$calendar_out_file = $private_folder.'calendar_'.$_SESSION['calenderNum'].'.txt';
			$file = fopen($calendar_out_file, 'w');

			$result_str = "";
			$events_list = getEvents($service, $maxMonths);
			foreach(eventStrList($events_list, $includeEventNames, $includeAttendeeNames) as $event) {
				fwrite($file, ''.$event."\n");
				$result_str = $result_str.$event;
				print "\n";
			}
			fclose($file);
			print "<center><textarea id='calendarData'>$result_str</textarea></center>";
			?>

			<div class="collector-button">
				<?php
					$user_id = $_SESSION['s'];
					$add_calendar_url = rootAddress()."/php/calendar?s=$user_id";
					print "<input type=\"submit\" value=\"Add a calendar from another Google account\" onclick=\"location.href='$add_calendar_url'\"/>";
				?>
				
			</div>
			<input type="submit" value="I am done.  Submit my data." onclick="completeSubmission()" />
		</div>
		</div>
	</body>
</html>