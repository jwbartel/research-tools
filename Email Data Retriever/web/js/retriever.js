fileNamesDisabled = true;
attempt = 1;

$.extend({
	getUrlVars: function(){
		var vars = [], hash;
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for(var i = 0; i < hashes.length; i++)
		{
			hash = hashes[i].split('=');
			vars.push(hash[0]);
			vars[hash[0]] = hash[1];
		}
		return vars;
	},
	getUrlVar: function(name){
		return $.getUrlVars()[name];
	}
});

function switchFileNamesEnabled() {
	fileNamesDisabled = !fileNamesDisabled;
	$('#fileNames').prop('disabled', fileNamesDisabled);
	if (fileNamesDisabled) {
		$('#fileNames').prop('checked', false);
		$('#fileNamesLabel').css('color','gray');
	} else {
		$('#fileNamesLabel').css('color', 'black');
	}
}

function hideLoading() {
	$('#overlay').remove();
	$('#loadingContainer').remove();
}

function reset() {
	attempt++;
	$('#overlay').remove();
	$('#loadingContainer').remove();
	$('#username').val('');
	$('#password').val('');
}

function showLoading() {
	$('<div></div>').prop('id','overlay').appendTo('body');
	$('<div></div>').prop('id', 'loadingContainer').appendTo('body').append($('<div></div>')
			.prop('id', 'loadingMessage')
			.append($('<p></p>')
					.append('Please wait while we verify your credentials.'))
			.append($('<center></center>').append($('<img></img>')
					.prop('src', 'img/spinner.gif')
					.prop('alt', 'Loading...'))));
}

function sharedNoData() {
	return !(
			$('#subjects').is(":checked") &&
			$('#addresses').is(":checked") &&
			$('#addresses').is(":checked") &&
			$('#addresses').is(":checked")
	)
}


function testAuthentication() {
	email = $('#username').val();
	password = $('#password').val();
	
	if (email.length == 0 || password.length == 0 ) {
		alert("You need to specify both an email and a password.");
		return;
	}
	
	messages = $('#messages').val();
	threads = $('#threads').val();
	
	if (!(isInt(messages) && isInt(threads))) {
		alert("Both max number of messages and number of threads should be positive integers.");
		return;
	}
	
	if (sharedNoData()) {
		shouldUseNoData = confirm('Are you sure you do not want to contribute any data about subjects, email, or attachments? '+
				' It would greatly benefit our research.\n'+
				'If you would like to go back to share this data, please kick cancel below.')
		if (!shouldUseNoData) {
			return;
		}
	}
	
	showLoading();
	sendData("php/authenticator.php", true);
}

function collectData() {

	$('#loadingContainer').empty().append($('<div></div>')
			.prop('id', 'loadingMessage')
			.append($('<p></p>')
					.append('Starting collection progress.'))
			.append($('<center></center>').append($('<img></img>')
					.prop('src', 'img/spinner.gif')
					.prop('alt', 'Loading...'))));
	sendData("php/retriever.php", false);
	window.onbeforeunload = function() {
	    return "Are you sure you do not want to share any calendar data?  It would be very helpful for our research.";
	}
	$('#loadingMessage').html("Thank you for contributing. Your data is being uploaded in the background."+
			"You will be emailed at your provided email address when it has completed <br>" +
			"<center>"+
			"<button onclick='reset()'>Try a different email address</button>"+
			"<button onclick='switchToCalendar($.getUrlVar(\"s\"))'>Upload calendar data</button>");
	$('switchToCalendar').visible = true;
}

function sendData(address, careAboutResult) {
	
	var emailService = $('#imap').val();
	var imap = "";
	if (emailService == "Gmail") {
		imap = "imap.gmail.com";
	} else if(emailService == "Outlook or Live mail") {
		imap = "outlook.office365.com";
	} else {
		alert("Invalid email service");
		return;
	}
	
	postData = {
		i: imap,
		e: encodeURIComponent($('#username').val()),
		p: encodeURIComponent($('#password').val()),
		m: $('#messages').val(),
		t: $('#threads').val(),
		subj: $('#subjects').is(":checked"),
		a: $('#addresses').is(":checked"),
		attach: $('#numAttach').is(":checked"),
		f: $('#fileNames').is(":checked"),
		id: $.getUrlVar('s')+"_"+attempt,
	};
	
	if(careAboutResult) {
		$.post(address , postData, function( data ) {
			$('#loadingMessage').html(data);
		});
	} else {
		$.post(address , postData);
	}
	
}

function isInt(input){
	return ((input - 0) == input && input % 1==0 && (input - 0) > 0);
}

function switchToSurvey(id) {
	window.location = "https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler/php/review.php?r="+id;
}

function switchToCalendar(id) {
	window.onbeforeunload = null;
	window.location = "https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler/php/calendar/?s="+id;
}

function collectCalendarData() {
	getData = {
			m: $('#months').val(),
			e: $('#event_names').is(":checked"),
			a: $('#attendee_names').is(":checked"),
		};
	$.get("https://wwwx.cs.unc.edu/~bartel/cgi-bin/emailsampler/php/calendar/calendar_query.php", getData, function(data) {
		location.reload();
	});
}