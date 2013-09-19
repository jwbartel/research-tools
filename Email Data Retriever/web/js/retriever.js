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

function showLoading() {
	$('<div></div>').prop('id','overlay')).appendTo('body');
	$('<div></div>').prop('id', 'loadingContainer').appendTo('body');
			.append($('<p></p>')
					.append('Thank you for contributing.  ')
					.append('Your anonymized data is being retrieved. ')
					.append('You will be redirected to our reviewing tool after retrieval completes.'))
			.append($('<center></center>').append($('<img></img>')
					.prop('src', 'img/spinner.gif')
					.prop('alt', 'Loading...'))
			.append($('<p></p>').append($('<a></a>')
					.append('If you would rather not wait, please click here to be emailed when your data finishes processing.')))))
}