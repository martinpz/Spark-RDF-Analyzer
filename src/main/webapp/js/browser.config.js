// ########################## RDF Browser Configuration ##########################
const LOADER = '<div class="progress progress-striped active page-progress-bar"><div class="progress-bar" style="width: 100%;"></div></div>';
const ANIMATION_SPEED = 'fast';
const COLORS = {
	pastel: {
		central: {
			back: '#fbb4ae',
			text: 'black'
		},
		in: {
			back: '#b3cde3',
			text: 'black'
		},
		out: {
			back: '#ccebc5',
			text: 'black'
		},
		literal: {
			back: '#decbe4',
			text: 'black'
		}
	},
	YlGn: {
		central: {
			back: '#ffffcc',
			text: 'black'
		},
		in: {
			back: '#c2e699',
			text: 'black'
		},
		out: {
			back: '#78c679',
			text: 'white'
		},
		literal: {
			back: '#238443',
			text: 'white'
		}
	},
	oranges: {
		central: {
			back: '#feedde',
			text: 'black'
		},
		in: {
			back: '#fdbe85',
			text: 'black'
		},
		out: {
			back: '#fd8d3c',
			text: 'white'
		},
		literal: {
			back: '#d94701',
			text: 'white'
		}
	},
	purples: {
		central: {
			back: '#f2f0f7',
			text: 'black'
		},
		in: {
			back: '#cbc9e2',
			text: 'black'
		},
		out: {
			back: '#9e9ac8',
			text: 'white'
		},
		literal: {
			back: '#6a51a3',
			text: 'white'
		}
	},
	spectral: {
		central: {
			back: '#d7191c',
			text: 'white'
		},
		in: {
			back: '#fdae61',
			text: 'white'
		},
		out: {
			back: '#abdda4',
			text: 'white'
		},
		literal: {
			back: '#2b83ba',
			text: 'white'
		}
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
	const selectedColorScheme = $('#colorSchemeSelection input:checked').val();
	return selectedColorScheme;
}

function getNeighborhoodRequest(centralNodeURI) {
	return REST_API + 'directNeighbors/' + getCookie('graphName') +
		'?centralNode=' + encodeURIComponent(centralNodeURI) +
		'&numNeighbors=' + numNeighbors();
}

function getEntryPointSuggestionRequest(method, numSuggestions) {
	return REST_API + 'suggestedEntryPoints/' + getCookie('graphName') +
		'?method=' + method +
		'&numSuggestions=' + numSuggestions;
}

$(document).ready(function () {
	$('#numNeighbors').slider({
		tooltip: 'hide'
	});

	$('#numNeighbors').on('slide', function (slideEvt) {
		$('#sliderVal').text(slideEvt.value);
	});

	// Enable slider when neighbor limitation is selected.
	$('#limitNeighbors').click(function () {
		if (this.checked) {
			$('#numNeighborsDiv').show(ANIMATION_SPEED);
		} else {
			$('#numNeighborsDiv').hide(ANIMATION_SPEED);
		}
	});

	// Fill color scheme selection with options.
	var colorGroup = '';
	$.each(COLORS, function (name, colorsForWhat) {
		colorGroup += '<div class="radio-inline">';
		colorGroup += '<label><input type="radio" name="colorScheme" value="' + name + '">';

		$.each(colorsForWhat, function (forWhat, hexCode) {
			colorGroup += '<span style="background-color: ' + hexCode.back + '">&nbsp; &nbsp;</span>';
		});

		colorGroup += '</label></div>';
	});
	$('#colorSchemeSelection').html(colorGroup).append('<p>Select the color scheme you want to use.</p>');
	$('#colorSchemeSelection input').first().attr('checked', true);
});