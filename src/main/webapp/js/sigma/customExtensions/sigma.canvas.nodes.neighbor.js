/**
 * Extends sigma with custom shape for neighbor nodes.
 */
sigma.canvas.nodes.neighbor = function(node, context, settings) {
    var prefix = settings('prefix') || '',
        size = node[prefix + 'size'];

    console.log('NEIGHBOR NODE!');

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

    context.font = (size * 2) + 'px arial';
    context.fillStyle = 'black';
    context.textBaseline = 'middle';
    context.textAlign = "center";
    context.fillText(node.label, node[prefix + 'x'], node[prefix + 'y'], ( size * 18 ));
};
