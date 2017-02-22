const SIGMA_GRAPH_SETTINGS = {
    /**
     * RENDERERS SETTINGS:
     * *******************
     */
    defaultEdgeType: 'tapered', // require sigma.renderers.customEdgeShapes

    // Labels:
    font: 'Helvetica',
    defaultLabelColor: '#2e2c2d',
    defaultLabelSize: 10,
    labelThreshold: 0,
    defaultEdgeLabelSize: 10,
    edgeLabelThreshold: 0,
    labelHoverBGColor: 'node',
    labelHoverShadow: 'node',
    labelSize: 'proportional',
    labelSizeRatio: 0.20,
    maxNodeLabelLineLength: 23,
    labelAlignment: 'center',

    // Edges:
    edgeColor: 'default',
    defaultEdgeColor: '#a9a9a9',

    // Nodes:
    defaultNodeColor: '#333333',
    // nodeBorderSize: 2,

    // Hovered nodes:
    hoverFontStyle: '',
    borderSize: 2,
    outerBorderSize: 2,
    nodeBorderColor: 'default',
    defaultNodeBorderColor: '#666',
    defaultNodeOuterBorderColor: '#f65565',

    // Active nodes and edges:
    activeFontStyle: 'bold',
    nodeActiveColor: 'node',
    defaultNodeActiveColor: '#333333',
    edgeActiveColor: 'default',
    defaultEdgeActiveColor: '#f65565',
    edgeHoverExtremities: true,

    /**
     * RESCALE SETTINGS:
     * *****************
     */
    minNodeSize: 10,
    maxNodeSize: 50,
    minEdgeSize: 2,
    maxEdgeSize: 4,

    /**
     * CAPTORS SETTINGS:
     * *****************
     */
    zoomingRatio: 1.382,
    doubleClickZoomingRatio: 1,
    zoomMin: 0.05,
    zoomMax: 5,
    doubleClickZoomDuration: 0,

    /**
     * GLOBAL SETTINGS:
     * ****************
     */
    autoRescale: true,
    doubleClickEnabled: true,
    enableEdgeHovering: true,
    edgeHoverPrecision: 10,

    /**
     * CAMERA SETTINGS:
     * ****************
     */
    nodesPowRatio: 0.8,
    edgesPowRatio: 0.8,

    /**
     * ANIMATIONS SETTINGS:
     * ********************
     */
    animationsTime: 800
};

const LAYOUT_NOVERLAP = {
    nodeMargin: 2.0,
    scaleNodes: 1.0,
    gridSize: 60,
    speed: 1,
    easing: 'quadraticInOut'
};

/*
const LAYOUT_FORCE_LINK = {
    outboundAttractionDistribution: false, // def=false
    autoadjustSizes: false, // def=false
    scaleRatio: 10, // scalingRatio def=1
    strongGravityMode: false, // def=false
    gravity: 3, // def=1
    slowDown: 1, // def=1
    startingIterations: 1, // def=1 number of iterations to be run before the first render.
    iterationsPerRender: 1, // def=1 number of iterations to be run before each render.
    maxIterations: 1000, // def=1000 set a limit if autoStop: true
    avgDistanceThreshold: 0.01, // def=0.01 this is the normal stopping condition of autoStop: true. When the average displacements of nodes is below this threshold, the layout stops.
    autoStop: true, // def=false
    worker: true, // def=true should the layout use a web worker?
    background: false, // def=false run the layout on background, apply the new nodes position on stop.
    easing: 'quadraticInOut',
    duration: 1000
};

const LAYOUT_FRUCHTERMAN_REINGOLD = {
    autoArea: true,
    area: 1,
    gravity: -0.5,
    speed: 0.1,
    iterations: 1000,
    easing: 'quadraticInOut'
};
*/

const EXPORT_PNG = {
    download: true,
    filename: 'graphExport.png',
    size: 500,
    margin: 50,
    background: 'white',
    zoomRatio: 1,
    labels: true
};

const EXPORT_SVG = {
    download: true,
    filename: 'graphExport.svg',
    size: 1000,
    labels: true,
    data: true
};