// ########################## RDF Browser Configuration ##########################
const LOADER = '<div class="progress progress-striped active page-progress-bar"><div class="progress-bar" style="width: 100%;"></div></div>';
const ANIMATION_SPEED = 'fast';
const COLORS = {
	default: {
		centralNode: 'orangered',
		inEdge: 'coral',
		outEdge: 'lightgreen',
		neighbor: 'seagreen'
	},
	lemonStand: {
		centralNode: '#404040',
		inEdge: '#6DBDD6',
		outEdge: '#B71427',
		neighbor: '#FFE658'
	},
	mintCom: {
		centralNode: '#585858',
		inEdge: '#118C4E',
		outEdge: '#C1E1A6',
		neighbor: '#FF9009'
	},
	rollStudio: {
		centralNode: '#6BBAA7',
		inEdge: '#FBA100',
		outEdge: '#6C648B',
		neighbor: '#B6A19E'
	},
	aQuest: {
		centralNode: '#945D60',
		inEdge: '#626E60',
		outEdge: '#AF473C',
		neighbor: '#3C3C3C'
	},
	fieldWork: {
		centralNode: '#7CDBD5',
		inEdge: '#F53240',
		outEdge: '#F9BE02',
		neighbor: '#02C8A7'
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
			colorGroup += '<span style="background-color: ' + hexCode + '">&nbsp; &nbsp;</span>';
		});

		colorGroup += '</label></div>';
	});
	$('#colorSchemeSelection').html(colorGroup).append('<p>Select the color scheme you want to use.</p>');
	$('#colorSchemeSelection input').first().attr('checked', true);
});
