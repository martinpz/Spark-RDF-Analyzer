// ########################## RDF Browser Configuration ##########################
const LOADER = '<div class="progress progress-striped active page-progress-bar"><div class="progress-bar" style="width: 100%;"></div></div>';
const ANIMATION_SPEED = 'fast';
const LAYOUT_ALGORITHMS = { 
	noverlap: 'NOverlap',
	forcelink: 'ForceLink',
	fruchterman: 'Fruchterman-Reingold',
	none: 'None'
};
const COLORS = {
	pastel: {
		'central' : '#fbb4ae',
		'in': '#b3cde3',
		'out': '#ccebc5',
		'literal': '#decbe4'
	},
	YlGn: {
		'central' : '#ffffcc',
		'in': '#c2e699',
		'out': '#78c679',
		'literal': '#238443'
	},
	oranges: {
		'central' : '#feedde',
		'in': '#fdbe85',
		'out': '#fd8d3c',
		'literal': '#d94701'
	},
	purples: {
		'central' : '#f2f0f7',
		'in': '#cbc9e2',
		'out': '#9e9ac8',
		'literal': '#6a51a3'
	},
	spectral: {
		'central' : '#d7191c',
		'in': '#fdae61',
		'out': '#abdda4',
		'literal': '#2b83ba'
	}
};

function getBrowsingType() {
	return $('#browsingType').val();
}

function numNeighbors() {
	if ($('#limitNeighbors').prop('checked')) {
		return $('#numNeighbors').val();
	} else {
		return 0;
	}
}

function getColorScheme() {
	return $('#colorSchemeSelection input:checked').val();
}

function getLayoutAlgorithm() {
	return $('#layoutAlgorithmSelection input:checked').val();
}

function getNeighborhoodRequest(centralNodeURI) {
    return REST_API + 'directNeighbors/' + getCookie('graphName')
		+ '?centralNode=' + encodeURIComponent(centralNodeURI)
		+ '&numNeighbors=' + numNeighbors();
}

$(document).ready( function() {
    $('#numNeighbors').slider({
        tooltip: 'hide'
    });
	
	$('#numNeighbors').on('slide', function(slideEvt) {
		$('#sliderVal').text(slideEvt.value);
	});

	// Enable slider when neighbor limitation is selected.
	$('#limitNeighbors').click( function() {
		if (this.checked) {
			$('#numNeighborsDiv').show(ANIMATION_SPEED);
		} else {
			$('#numNeighborsDiv').hide(ANIMATION_SPEED);
		}
	});

	// Fill color scheme selection with options.
	var colorGroup = '';
	$.each(COLORS, function(name, colorsForWhat) {
		colorGroup += '<div class="radio-inline">';
		colorGroup += '<label><input type="radio" name="colorScheme" value="' + name + '">';

		$.each(colorsForWhat, function(forWhat, hexCode) {
			colorGroup += '<span style="background-color: ' + hexCode + '">&nbsp; &nbsp;</span>';
		});

		colorGroup += '</label></div>';
	});
	$('#colorSchemeSelection').html(colorGroup).append('<p>Select the color scheme you want to use.</p>');
	$('#colorSchemeSelection input').first().attr('checked', true);

	// Fill layout algorithm selection with options.
	var layoutGroup = '';
	$.each(LAYOUT_ALGORITHMS, function(layoutShortName, layoutName) {
		layoutGroup += '<div class="radio-inline">';
		layoutGroup += '<label><input type="radio" name="layoutAlgorithm" value="' + layoutShortName + '">';
		layoutGroup += '<span>' + layoutName + '</span>';
		layoutGroup += '</label></div>';
	});
	$('#layoutAlgorithmSelection').html(layoutGroup).append('<p>Select the graph layout algorithm you want to use.</p>');
	$('#layoutAlgorithmSelection input').first().attr('checked', true);
});
