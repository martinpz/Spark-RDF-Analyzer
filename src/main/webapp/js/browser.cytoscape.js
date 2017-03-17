// ########################## RDF Browser Cytoscape Instance Helper ##########################
const LAYOUTS = {
    GRID: {
        name: 'grid',
        fit: true, // whether to fit the viewport to the graph
        padding: 0, // padding used on fit
        avoidOverlap: true, // prevents node overlap, may overflow boundingBox if not enough space
        avoidOverlapPadding: 5, // extra spacing around nodes when avoidOverlap: true
        condense: false, // uses all available space on false, uses minimal space on true
        rows: undefined, // force num of rows in the grid
        cols: 10 // force num of columns in the grid
    },
    COLA: {
        name: 'cola',
        animate: true, // whether to show the layout as it's running
        refresh: 1, // number of ticks per frame; higher is faster but more jerky
        maxSimulationTime: 2000, // max length in ms to run the layout
        ungrabifyWhileSimulating: false, // so you can't drag nodes during layout
        fit: true, // on every layout reposition of nodes, fit the viewport
        padding: 0, // padding around the simulation
        randomize: true, // use random node positions at beginning of layout
        avoidOverlap: true, // if true, prevents overlap of node bounding boxes
        handleDisconnected: true, // if true, avoids disconnected components from overlapping
        nodeSpacing: 10, // extra spacing around nodes
        edgeLength: undefined, // sets edge length directly in simulation
        edgeSymDiffLength: undefined, // symmetric diff edge length in simulation
        edgeJaccardLength: undefined, // jaccard edge length in simulation
        unconstrIter: undefined, // unconstrained initial layout iterations
        userConstIter: undefined, // initial layout iterations with user-specified constraints
        allConstIter: undefined, // initial layout iterations with all constraints including non-overlap
        infinite: false // overrides all other options for a forces-all-the-time mode
    }
};

function getCytoscapeInstance(graphElements, layoutToUse) {
    const colorScheme = getColorScheme();
    return cytoscape({
        container: $('#container'),

        boxSelectionEnabled: false,
        autounselectify: true,

        layout: LAYOUTS[layoutToUse],

        style: [{
            selector: 'node',
            style: {
                'shape': 'rectangle',
                'width': 'label',
                'height': 'label',
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
                'content': 'data(label)',
                'font-size': '8px',
                'edge-text-rotation': 'autorotate',
                'line-color': '#ccc',
                'target-arrow-color': '#ccc',
                'target-arrow-shape': 'triangle'
            }
        }],

        elements: graphElements
    });
}