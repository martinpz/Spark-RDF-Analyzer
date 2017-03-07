// ########################## RDF Browser Cytoscape Instance Helper ##########################
function getCytoscapeInstance(graphElements) {
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
                'overflow': 'hidden',
                'white-space': 'nowrap',
                'overlay-padding': '6px',
                'background-color': '#555',
                'color': '#fff',
                'content': 'data(label)',
                'font-size': '11px',
                'text-overflow': 'clip',
                'text-valign': 'center',
                'text-halign': 'center',
                'text-outline-color': '#555',
                'text-outline-width': '2px'
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