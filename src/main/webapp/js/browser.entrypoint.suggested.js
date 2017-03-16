// ########################## RDF Browser Suggested Entry Point ##########################
function getSuggestedEntryPoints() {
	const NUM_SUGGESTIONS = 6;
	const RANKING_METHODS = [{
		name: 'PageRanking',
		displayTarget: 'suggestionsLeft'
	}, {
		name: 'ClosenessRanking',
		displayTarget: 'suggestionsRight'
	}];

	RANKING_METHODS.forEach(function (method) {
		getSuggestedEntryPointsWithMethod(method.name, NUM_SUGGESTIONS, method.displayTarget);
	});
}

function getSuggestedEntryPointsWithMethod(rankingMethod, numSuggestions, displayTarget) {
	var xhttp = new XMLHttpRequest();

	xhttp.onreadystatechange = function () {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			displaySuggestedEntryPoints(JSON.parse(xhttp.responseText), displayTarget);
		}
	}

	xhttp.open('GET', getEntryPointSuggestionRequest(rankingMethod, numSuggestions), true);
	xhttp.send();
}

function displaySuggestedEntryPoints(suggestions, displayTarget) {
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

	$('#' + displayTarget).html(suggestionsHTML);
}

function startBrowsingWithSuggestedEntryPoint(centralNode, centralNodeURI) {
	clearBrowsingHistory();
	showBrowser(centralNode, centralNodeURI);
}