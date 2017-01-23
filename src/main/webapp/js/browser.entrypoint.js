// ########################## RDF Browser Entry Point ##########################
function showAutocompletionModal() {
	var input = $('#entryNode').val();

	// Only consider inputs with at least two characters.
	if (input.length < 2) {
		$('#modalTitle').text('Query too short!');
		$('#modalBody').text(
				'Please enter at least two characters before searching!');
		$('#btnStartBrowsing').hide();
	} else {
		$('#modalTitle').text('Select your entry point!');
		$('#modalBody').html(
				'<p>Computing the autocomplete suggestions ...</p>' + LOADER);
		$('#btnStartBrowsing').show();

		var xhttp = new XMLHttpRequest();

		xhttp.onreadystatechange = function() {
			if (xhttp.readyState == 4 && xhttp.status == 200) {
				$('#modalBody').html(xhttp.responseText);
			}
		}

		xhttp.open('GET', REST_API + 'autoComplete/' + getCookie('graphName')
				+ '/' + input + '/Node', true);
		xhttp.send();
	}
}

function startBrowsing(event) {
	var selectedText = $('input[name="optradio"]:checked').val();

	// When no option is selected, we cannot continue.
	if (typeof selectedText === 'undefined') {
		event.stopPropagation();
		return false;
	}

	var selectedText_arr = selectedText.split(':'); // u52,<http,//www.ins.cwi.nl/sib/user/u52>
	var selectedValue = selectedText_arr[0]; // u52
	var selectedURI = selectedText_arr[1] + ':' + selectedText_arr[2]; // <http://www.ins.cwi.nl/sib/user/u52>

	// Close modal.
	$('#btnCloseModal').click();

	clearBrowsingHistory();
	showBrowser(selectedValue, selectedURI);
}

function simulateClickOnSearch() {
	$('#btnSearch').click();
}

function showReturnToBrowser() {
	$('#btnReturnToBrowser').removeClass('invisible');
}

$(document).ready(function() {
	// On 'Enter' in search field simulate click on search.
	$('#entryNode').keypress( function(e) {
		if (e.which == 13) {
			simulateClickOnSearch();
		}
	});
	$('#btnSearch').click( function() {
		showAutocompletionModal();
	});
	$('#btnReturnToBrowser').click( function() {
		returnToBrowser();
	});
});
