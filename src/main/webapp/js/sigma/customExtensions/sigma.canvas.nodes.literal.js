/**
 * Extends sigma with custom shape for literal nodes.
 */
sigma.canvas.nodes.literal = function(node, context, settings) {
    sigma.canvas.nodes.star(node, context, settings);
};

sigma.canvas.labels.literal = function(node, context, settings) {
   // Do not draw a label for any literal.
}

sigma.canvas.hovers.literal = function(node, context, settings) {
    console.log('node:', node);

    // Render the node and its label again.
    // This is necessary that the edge does not overlay the node.
    sigma.canvas.nodes.literal(node, context, settings);
    sigma.canvas.labels.literal(node, context, settings);
}
