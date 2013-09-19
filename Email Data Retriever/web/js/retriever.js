fileNamesDisabled = true;

function switchFileNamesEnabled() {
	fileNamesDisabled = !fileNamesDisabled;
	document.getElementById('fileNames').disabled = fileNamesDisabled;
	if (fileNamesDisabled) {
		document.getElementById('fileNames').checked = false;
		document.getElementById('fileNamesLabel').style.color = 'gray';
	} else {
		document.getElementById('fileNamesLabel').style.color = 'black';
	}
}

function showLoading() {
	divElement = document.createElement('div');
	divElement.innerHTML = "<div id='overlay'></div>" +
		"<div id='loadingContainer'><div id='loadingMessage'>" +
		"<p> Thank you for contributing.  Your anonymized data is being retrieved. You will be redirected to our reviewing tool after retrieval completes.</p>" +
		"<center><img src='img/spinner.gif' alt='Loading...'></img></center>" +
		"<p> <a href=''>If you would rather not wait, please click here to be emailed when your data finishes processing.</a></p>" +
		"</div></div>";
	document.body.appendChild(divElement);
}