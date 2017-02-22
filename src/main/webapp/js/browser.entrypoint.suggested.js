// ########################## RDF Browser Suggested Entry Point ##########################
function getSuggestedEntryPoints() {
	const RANKING_METHOD = 'PageRanking';
	const NUM_SUGGESTIONS = 9;

	console.log('request: ', getEntryPointSuggestionRequest(RANKING_METHOD, NUM_SUGGESTIONS));

	var xhttp = new XMLHttpRequest();

	xhttp.onreadystatechange = function () {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			displaySuggestedEntryPoints(JSON.parse(xhttp.responseText));
		}
	}

	xhttp.open('GET', getEntryPointSuggestionRequest(RANKING_METHOD, NUM_SUGGESTIONS), true);
	xhttp.send();
}

function displaySuggestedEntryPoints(suggestions) {
	console.log('suggestions: ', suggestions);

	// Combine all suggestions to a list.
	var suggestionsHTML = '<ul>';
	$.each(suggestions, function (URI, props) {
		suggestionsHTML += '<li>';
		suggestionsHTML += '<a href="#" onclick="startBrowsingWithSuggestedEntryPoint(\'' + props.name + '\', \'' + URI + '\')">';
		suggestionsHTML += props.name + '<br>(' + props.importance.toFixed(3) + ')';
		suggestionsHTML += '</a></li>';
	});
	suggestionsHTML += '</ul>';

	$('#suggestionsList').html(suggestionsHTML);
}

function startBrowsingWithSuggestedEntryPoint(centralNode, centralNodeURI) {
	clearBrowsingHistory();
	showBrowser(centralNode, centralNodeURI);
}

$(document).ready(function () {
	// 
});