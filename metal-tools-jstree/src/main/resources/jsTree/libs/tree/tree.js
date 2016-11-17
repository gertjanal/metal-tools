var offset = 100,
	w = window.innerWidth,
	h = window.innerHeight,
	i = 0,
	root;

var tree = d3.layout.tree().size([h, w]);
var diagonal = d3.svg.diagonal().projection(function(d) { return [d.y, d.x]; });

var vis = d3.select('#box').append('svg:svg')
	.attr('class','svg_container')
	.attr('width', w)
	.attr('height', h)
	.style('overflow', 'scroll')
	.append('svg:g')
		.attr('class','drawarea')
		.append('svg:g')
			.attr('transform', 'translate(' + offset + ', 0)');

function loadData(json) {
	root = json;
	root.x0 = h / 2;
	root.y0 = 0;
	update(root);
}

function update(source) {
	var duration = 1000;
	
	// Compute the new tree layout.
	var nodes = tree.nodes(root).reverse();
	
	// Normalize for fixed-depth.
	nodes.forEach(function(d) { d.y = d.depth * 150; });
	
	// Update the nodes…
	var node = vis.selectAll('g.node').data(nodes, function(d) { return d.id || (d.id = ++i); });
	
	// Enter any new nodes at the parent's previous position.
	var nodeEnter = node.enter().append('g')
		.attr('class', function(d){ return 'node hue' + getHue(d.name);})
		.attr('transform', function(d) { return 'translate(' + source.y0 + ',' + source.x0 + ')'; })
		.on('click', function(d) { toggle(d); update(d); });
	
	nodeEnter.append('text')
	    .attr('y', 4)
		.style('fill-opacity', 1e-6)
		.attr('text-anchor', 'start')
		.text(function(d) {
			return d.name;
		})
		.call(getTextBox);
	
	nodeEnter.insert('rect', 'text')
		.attr("x", function(d){return d.bbox.x - 1})
		.attr("y", function(d){return d.bbox.y})
		.attr("width", function(d){return d.bbox.width + 2})
		.attr("height", function(d){return d.bbox.height});

	function getTextBox(selection) {
		selection.each(function(d) {d.bbox = this.getBBox();})
	}
	
	// Transition nodes to their new position.
	var nodeUpdate = node.transition()
		.duration(duration)
		.attr('transform', function(d) { return 'translate(' + d.y + ',' + d.x + ')'; });

	nodeUpdate.select('text').style('fill-opacity', 1);
	
	// Transition exiting nodes to the parent's new position.
	var nodeExit = node.exit().transition()
		.duration(duration)
		.attr('transform', function(d) { return 'translate(' + source.y + ',' + source.x + ')'; })
		.remove();
	
	nodeExit.select('text').style('fill-opacity', 1e-6);

	// Update the links…
	var link = vis.selectAll('path.link').data(tree.links(nodes), function(d) { return d.target.id; });
	
	// Enter any new links at the parent's previous position.
	link.enter().insert('svg:path', 'g')
		.attr('class', 'link')
		.attr('d', function(d) {
			var o = {x: source.x0, y: source.y0};
			return diagonal({source: o, target: o});
		})
		.transition()
		.duration(duration)
		.attr('d', diagonal);
	
	// Transition links to their new position.
	link.transition()
		.duration(duration)
		.attr('d', diagonal);

	// Transition exiting nodes to the parent's new position.
	link.exit().transition()
		.duration(duration)
		.attr('d', function(d) {
			var o = {x: source.x, y: source.y};
			return diagonal({source: o, target: o});
		})
		.remove();

	// Stash the old positions for transition.
	nodes.forEach(function(d) {
		d.x0 = d.x;
		d.y0 = d.y;
	});

	d3.select('svg').call(d3.behavior.zoom().scaleExtent([0.5, 3]).on('zoom', zoom));
}

// Toggle children.
function toggle(d) {
	if (d.children) {
		d._children = d.children;
		d.children = null;
	}
	else {
		d.children = d._children;
		d._children = null;
	}
}

function zoom() {
	var scale = d3.event.scale,
		translation = d3.event.translate,
		tbound = -h * scale,
		bbound = h * scale,
		lbound = -w * scale,
		rbound = w * scale;
	// limit translation to thresholds
	translation = [
		Math.max(Math.min(translation[0], rbound), lbound),
		Math.max(Math.min(translation[1], bbound), tbound)
	];
	d3.select('.drawarea').attr('transform', 'translate(' + translation + ') scale(' + scale + ')');
}

var flip = true;
var lastColor = Math.floor((Math.random() * 180) + 1);
var colors = {};
function getHue(str) {
	if (!colors.hasOwnProperty(str)) {
		lastColor = (lastColor + 13) % 180;
		colors[str] = flip ? lastColor : 360 - lastColor;
		flip = !flip;
	}
	return colors[str];
}