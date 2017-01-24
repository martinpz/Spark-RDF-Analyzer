/**
 * Extends sigma with custom shape for central nodes.
 */
sigma.canvas.nodes.centralNode = function(node, context, settings) {
    sigma.canvas.nodes.circle(node, context, settings);

    /*
    var prefix = settings('prefix') || '',
        size = node[prefix + 'size'];

    context.fillStyle = node.color || settings('defaultNodeColor');
    context.beginPath();

    context.rect(
        node[prefix + 'x'] - ( size * 10 ),
        node[prefix + 'y'] - ( size * 4 ),
        size * 20,
        size * 8
    );

    context.closePath();
    context.fill();
    */
};

sigma.canvas.labels.centralNode = function(node, context, settings) {
    var fontSize,
        prefix = settings('prefix') || '',
        size = node[prefix + 'size'];

    if (size < settings('labelThreshold'))
        return;

    if (!node.label || typeof node.label !== 'string')
        return;

    fontSize = (settings('labelSize') === 'fixed')
        ? settings('defaultLabelSize') 
        : settings('labelSizeRatio') * size;

    context.font = (settings('fontStyle') ? settings('fontStyle') + ' ' : '') 
        + fontSize + 'px ' + settings('font');

    context.fillStyle = (settings('labelColor') === 'node') 
        ? (node.color || settings('defaultNodeColor')) 
        : settings('defaultLabelColor');

    context.textBaseline = 'middle';
    context.textAlign = "center";

    // Split the label when it is too long.
    const LINE_WIDTH = 28;
    var label = node.label;
    var label_arr = [];

    while ( label.length > LINE_WIDTH ) {
        label_arr.push( label.substr( 0, LINE_WIDTH ) );
        label = label.substr( LINE_WIDTH );
    }

    // Push the remainder to the array.
    label_arr.push( label );

    var offset = Math.floor( label_arr.length / 2 );

    for ( i = 0; i < label_arr.length; i++ ) {
        context.fillText(
            label_arr[i],
            Math.round(node[prefix + 'x']),
            Math.round(node[prefix + 'y']) + ( fontSize * ( i - offset ) ),
            ( size * 2 ) - 4
        );
    }
}

sigma.canvas.hovers.centralNode = function(node, context, settings) {
    // Render the node and its label again.
    // This is necessary that the edge does not overlay the node.
    sigma.canvas.nodes.centralNode(node, context, settings);
    sigma.canvas.labels.centralNode(node, context, settings);
}
