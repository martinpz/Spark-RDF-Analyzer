// ########################## RDF Browser Suggested Entry Point ##########################
function getSuggestedEntryPoints() {
	const RANKING_METHOD = 'PageRanking';
	const NUM_SUGGESTIONS = 12;

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
	// Combine all suggestions to a HTML list.
	var suggestionsHTML = '<ul>';
	suggestionsHTML += suggestions.map(function (entryJSON) {
		var entryOBJ = JSON.parse(entryJSON);
		var entryHTML = '<li>';
		entryHTML += '<a href="#" onclick="startBrowsingWithSuggestedEntryPoint(\'' + entryOBJ.name + '\', \'' + entryOBJ.URI + '\')">';
		entryHTML += entryOBJ.name + '<br>[' + entryOBJ.importance.toFixed(3) + ']';
		entryHTML += '</a></li>';
		return entryHTML;
	}).join(' ');
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