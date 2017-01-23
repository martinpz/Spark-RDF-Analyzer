// ########################## RDF Browser History ##########################
function updateBrowsingHistory(currentName, currentURI) {
	// Remove links from all elements.
	$('#browsingHistory #list li').html('');
	
	// Append new last (=current) element.
	$('#browsingHistory #list').append('<li data-name="' + currentName + '" data-uri="' + currentURI + '"></li>');

	// For all elements equal to the current element:
	// add the 'active' class and only display the name.
	var sameElements = $('#browsingHistory #list li[data-uri=\'' + currentURI + '\']');

	$.each(sameElements, function() {
		$(this).addClass('active');
		$(this).text( $(this).attr('data-name') );
	});

	// For all other elements = not equal to the current one:
	// add the 'data-uri' as the link for the name.
	var others = $('#browsingHistory #list li[data-uri!=\'' + currentURI + '\']');

	$.each(others, function() {
		var name = $(this).attr('data-name');
		var URI = $(this).attr('data-uri');

		$(this).removeClass('active');
		$(this).html('<a href="#" onclick="' + 'prepareBrowser(\'' + name + '\', \'' + URI + '\')' + '">' + name + '</a>');
	});
}

function clearBrowsingHistory() {
	$('#browsingHistory #list').html('');
}
