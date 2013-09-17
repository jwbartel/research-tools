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

function updateDepthCounts() {
	document.getElementById('maxMessage').innerHTML = document.getElementById('messages').value;
	document.getElementById('maxThread').innerHTML = document.getElementById('threads').value;
}

function showLoading() {
	document.getElementById('overlay').style.visibility = 'visible';
}