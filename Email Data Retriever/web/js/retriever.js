fileNamesDisabled = true;

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
	showLoading();
	sendData("php/authenticator.php");
}

function collectData() {

	$('#loadingContainer').empty().append($('<div></div>')
			.prop('id', 'loadingMessage')
			.append($('<p></p>')
					.append('Starting collection progress.'))
			.append($('<center></center>').append($('<img></img>')
					.prop('src', 'img/spinner.gif')
					.prop('alt', 'Loading...'))));
	sendData("php/retriever.php");
}

function sendData(address) {
	
	postData = {
		i: $('#imap').val(),
		s: $('#smtp').val(),
		e: $('#username').val(),
		p: $('#password').val(),
		m: $('#messages').val(),
		t: $('#threads').val(),
		subj: $('#subjects').is(":checked"),
		a: $('#addresses').is(":checked"),
		attach: $('#numAttach').is(":checked"),
		f: $('#fileNames').is(":checked"),
	}
	
	$.post(address , postData, function( data ) {
		$('#loadingMessage').html(data);
	});
	
}

function isInt(input){
	return ((input - 0) == input && input % 1==0 && (input - 0) > 0);
}