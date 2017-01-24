// ########################## RDF Browser Configuration ##########################
const LOADER = '<div class="progress progress-striped active page-progress-bar"><div class="progress-bar" style="width: 100%;"></div></div>';
const ANIMATION_SPEED = 'fast';
const COLORS = {
	default: {
		centralNode: '255, 127, 80',
		in: '255, 69, 0',
		out: '0, 139, 0',
		literal: '50, 205, 50'
	},
	contrast: {
		centralNode: '64, 64, 64',
		in: '109, 189, 214',
		out: '183, 20, 39',
		literal: '255, 230, 88'
	},
	mixed: {
		centralNode: '107, 186, 167',
		in: '17, 140, 78',
		out: '193, 225, 166',
		literal: '255, 144, 9'
	},
	light: {
		centralNode: '124, 219, 213',
		in: '245, 50, 64',
		out: '249, 190, 2',
		literal: '2, 200, 167'
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
	return COLORS[$('#colorSchemeSelection input:checked').val()];
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
			colorGroup += '<span style="background-color: rgb(' + hexCode + ')">&nbsp; &nbsp;</span>';
		});

		colorGroup += '</label></div>';
	});
	$('#colorSchemeSelection').html(colorGroup).append('<p>Select the color scheme you want to use.</p>');
	$('#colorSchemeSelection input').first().attr('checked', true);
});
