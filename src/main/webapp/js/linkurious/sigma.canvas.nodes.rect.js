;
(function () {
    'use strict';

    sigma.utils.pkg('sigma.canvas.nodes');

    // A custom node renderer. It renders the node as a rectangle.
    sigma.canvas.nodes.rect = function (node, context, settings) {
        var prefix = settings('prefix') || '';
        var nodeH = node[prefix + 'size'] * 0.8;
        var nodeW = node[prefix + 'size'] * 2.5;
        var borderSize = settings('nodeBorderSize') || 0;

        // Node border:
        context.beginPath();
        context.fillStyle = settings('nodeBorderColor') === 'node' ?
            (node.color || settings('defaultNodeColor')) :
            settings('defaultNodeBorderColor');
        context.rect(
            node[prefix + 'x'] - (nodeW / 2) - borderSize,
            node[prefix + 'y'] - (nodeH / 2) - borderSize,
            nodeW + ( 2 * borderSize ),
            nodeH + ( 2 * borderSize )
        );
        context.closePath();
        context.fill();

        context.fillStyle = node.color || settings('defaultNodeColor');
        context.beginPath();
        context.rect(
            node[prefix + 'x'] - (nodeW / 2),
            node[prefix + 'y'] - (nodeH / 2),
            nodeW,
            nodeH
        );
        context.closePath();
        context.fill();
    };
})();