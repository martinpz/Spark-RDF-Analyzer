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
    labelThreshold: 0, // 5
    defaultEdgeLabelSize: 12,
    edgeLabelThreshold: 7,
    labelHoverBGColor: 'node',
    labelHoverShadow: 'node',
    labelSize: 'proportional',
    labelSizeRatio: 0.20,
    maxNodeLabelLineLength: 23,
    labelAlignment: 'center', // center vs. inside

    // Edges:
    edgeColor: 'default',
    defaultEdgeColor: '#a9a9a9',

    // Nodes:
    defaultNodeColor: '#333333',
    nodeBorderSize: 2,

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
    autoRescale: ['nodeSize', 'edgeSize'],
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