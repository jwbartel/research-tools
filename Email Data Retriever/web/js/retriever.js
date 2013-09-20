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

function showLoading() {
	$('<div></div>').prop('id','overlay').appendTo('body');
	$('<div></div>').prop('id', 'loadingContainer').appendTo('body').append($('<div></div>')
			.prop('id', 'loadingMessage')
			.append($('<p></p>')
					.append('Thank you for contributing.  ')
					.append('Your anonymized data is being retrieved. ')
					.append('You will be redirected to our reviewing tool after retrieval completes.'))
			.append($('<center></center>').append($('<img></img>')
					.prop('src', 'img/spinner.gif')
					.prop('alt', 'Loading...')))
			.append($('<p></p>').append($('<a></a>')
					.append('If you would rather not wait, please click here to be emailed when your data finishes processing.'))));
}

function collectData() {
	
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
	
	if (postData['e'].length == 0 || postData['p'].length == 0 ) {
		alert("You need to specify both an email and a password.");
		return;
	}
	
	if (!(isInt(postData['m']) && isInt(postData['t']))) {
		alert("Both max number of messages and number of threads should be positive integers.");
		return;
	}
	
	showLoading();
	
	$.post( "php/retriever.php", postData, function( data ) {
		$('#loadingMessage').html(data);
	});
	
}

function isInt(input){
	return ((input - 0) == input && input % 1==0 && (input - 0) > 0);
}