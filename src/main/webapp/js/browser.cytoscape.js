// ########################## RDF Browser Cytoscape Instance Helper ##########################
function getCytoscapeInstance(graphElements) {
    const colorScheme = getColorScheme();
    return cytoscape({
        container: $('#container'),

        boxSelectionEnabled: false,
        autounselectify: true,

        pan: {
            x: 0,
            y: 0
        },

        layout: {
            name: 'spread',
            minDist: 40
        },

        style: [{
            selector: 'node',
            style: {
                'shape': 'rectangle',
                'width': 'label', // 'width': 'mapData(score, 0, 0.006769776522008331, 20, 60)',
                'height': 'label', // 'height': 'mapData(score, 0, 0.006769776522008331, 10, 30)',
                'padding': '7px',
                'overlay-padding': '6px',
                'background-color': COLORS[colorScheme].central.back,
                'color': COLORS[colorScheme].central.text,
                'content': 'data(label)',
                'font-size': '11px',
                'text-valign': 'center',
                'text-halign': 'center'
            }
        }, {
            'selector': 'node.in',
            'style': {
                'background-color': COLORS[colorScheme].in.back,
                'color': COLORS[colorScheme].in.text
            }
        }, {
            'selector': 'node.out',
            'style': {
                'background-color': COLORS[colorScheme].out.back,
                'color': COLORS[colorScheme].out.text
            }
        }, {
            'selector': 'node.literal',
            'style': {
                'background-color': COLORS[colorScheme].literal.back,
                'color': COLORS[colorScheme].literal.text
            }
        }, {
            selector: 'edge',
            style: {
                'width': 3,
                'line-color': '#ccc',
                'target-arrow-color': '#ccc',
                'target-arrow-shape': 'triangle'
            }
        }],

        elements: graphElements
    });
}