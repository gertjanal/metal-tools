var realWidth = window.innerWidth;
var realHeight = window.innerHeight;

var m = [40, 240, 40, 240],
	w = realWidth,
	h = realHeight,
	i = 0,
	root;

var tree = d3.layout.tree()
	.size([h, w]);

var diagonal = d3.svg.diagonal()
	.projection(function(d) { return [d.y, d.x]; });

var vis = d3.select("#box").append("svg:svg")
	.attr("class","svg_container")
	.attr("width", w)
	.attr("height", h)
	.style("overflow", "scroll")
	.style("background-color","rgb(51, 51, 51)")
  .append("svg:g")
	.attr("class","drawarea")
  .append("svg:g")
	.attr("transform", "translate(" + m[3] + "," + m[0] + ")");

function loadData(json) {
	root = json;
	d3.select("#processName").html(root.text);
	root.x0 = h / 2;
	root.y0 = 0;
	update(root);
}

function update(source) {
	var duration = d3.event && d3.event.altKey ? 5000 : 500;
	
	// Compute the new tree layout.
	var nodes = tree.nodes(root).reverse();
	
	// Normalize for fixed-depth.
	nodes.forEach(function(d) { d.y = d.depth * 150; });
	
	// Update the nodes…
	var node = vis.selectAll("g.node")
	    .data(nodes, function(d) { return d.id || (d.id = ++i); });
	
	// Enter any new nodes at the parent's previous position.
	var nodeEnter = node.enter().append("svg:g")
    	.attr("class", function(d){ return 'node hue' + getHue(d.name);})
    	.attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
    	.on("click", function(d) { toggle(d); update(d); });
	
	nodeEnter.append("svg:text")
    	.attr("text-anchor", function(d) { return 'start' })
    	.text(function(d) { 
    	    return d.name;
    	})
    	.style("fill-opacity", 1e-6);
	
	// Transition nodes to their new position.
	var nodeUpdate = node.transition()
	.duration(duration)
	.attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });
	

	nodeUpdate.select("text")
	.style("fill-opacity", 1);
	
	// Transition exiting nodes to the parent's new position.
	var nodeExit = node.exit().transition()
    	.duration(duration)
    	.attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
    	.remove();
	
	nodeExit.select("text")
	    .style("fill-opacity", 1e-6);

	// Update the links…
	var link = vis.selectAll("path.link")
	.data(tree.links(nodes), function(d) { return d.target.id; });
	
	// Enter any new links at the parent's previous position.
	link.enter().insert("svg:path", "g")
	.attr("class", "link")
	.attr("d", function(d) {
		var o = {x: source.x0, y: source.y0};
		return diagonal({source: o, target: o});
	})
	.transition()
	.duration(duration)
	.attr("d", diagonal);
	
	// Transition links to their new position.
	link.transition()
	.duration(duration)
	.attr("d", diagonal);

	// Transition exiting nodes to the parent's new position.
	link.exit().transition()
	.duration(duration)
	.attr("d", function(d) {
		var o = {x: source.x, y: source.y};
		return diagonal({source: o, target: o});
	})
	.remove();

	// Stash the old positions for transition.
	nodes.forEach(function(d) {
		d.x0 = d.x;
		d.y0 = d.y;
	});

	d3.select("svg")
		.call(d3.behavior.zoom()
			  .scaleExtent([0.5, 3])
			  .on("zoom", zoom));
}

// Toggle children.
function toggle(d) {
	if (d.children) {
		d._children = d.children;
		d.children = null;
	} else {
		d.children = d._children;
		d._children = null;
	}
}

function zoom() {
	var scale = d3.event.scale,
		translation = d3.event.translate,
		tbound = -h * scale,
		bbound = h * scale,
		lbound = (-w + m[1]) * scale,
		rbound = (w - m[3]) * scale;
	// limit translation to thresholds
	translation = [
		Math.max(Math.min(translation[0], rbound), lbound),
		Math.max(Math.min(translation[1], bbound), tbound)
	];
	d3.select(".drawarea")
		.attr("transform", "translate(" + translation + ")" +
			  " scale(" + scale + ")");
}

var flip = true;
var lastColor = Math.floor((Math.random() * 180) + 1);
var colors = {};
function getHue(str) {
    console.log(str);
    if (!colors.hasOwnProperty(str)) {
        lastColor = (lastColor + 13) % 180;
        colors[str] = flip ? lastColor : 360 - lastColor;
        flip = !flip;
        console.log('new color ' + str + ': ' + colors[str])
    }
    return colors[str];
}

loadData({"name":"Seq","children":[{"name":"Seq: fileIdentifier","children":[{"name":"Def: Signature","value":"dmhkeGZpbGU="},{"name":"Cho","children":[{"name":"Seq","children":[{"name":"Def: Creator","value":"TQBpAGMAcgBvAHMAbwBmAHQAIABXAGkAbgBkAG8AdwBzACAANgAuADMALgA5ADYAMAAwAC4AMQA3ADMAOQA2AA=="},{"name":"Def: nod","value":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}]}]}]},{"name":"Cho","children":[{"name":"Seq","children":[{"name":"Sub: oldHeader","children":[{"name":"Seq","children":[{"name":"Def: Signature","value":"aGVhZA=="},{"name":"Def: Checksum","value":"aSZacA=="},{"name":"Def: SequenceNumber","value":"CAAAAAAAAAA="},{"name":"Def: FileWriteGuid","value":"F03jOIaHcUy+5i0Ve65eXQ=="},{"name":"Def: DataWriteGuid","value":"BmjCDfHcR0CF+tcNp9+3PA=="},{"name":"Def: LogGuid","value":"AAAAAAAAAAAAAAAAAAAAAA=="},{"name":"Def: LogVersion","value":"AAA="},{"name":"Def: Version","value":"AQA="},{"name":"Def: LogLength","value":"AAAQAA=="},{"name":"Def: LogOffset","value":"AAAQAAAAAAA="}]}]},{"name":"Sub: header","children":[{"name":"Seq","children":[{"name":"Def: Signature","value":"aGVhZA=="},{"name":"Def: Checksum","value":"Hrqi8g=="},{"name":"Def: SequenceNumber","value":"CQAAAAAAAAA="},{"name":"Def: FileWriteGuid","value":"F03jOIaHcUy+5i0Ve65eXQ=="},{"name":"Def: DataWriteGuid","value":"BmjCDfHcR0CF+tcNp9+3PA=="},{"name":"Def: LogGuid","value":"AAAAAAAAAAAAAAAAAAAAAA=="},{"name":"Def: LogVersion","value":"AAA="},{"name":"Def: Version","value":"AQA="},{"name":"Def: LogLength","value":"AAAQAA=="},{"name":"Def: LogOffset","value":"AAAQAAAAAAA="}]}]},{"name":"Sub: oldRegion","children":[{"name":"Seq","children":[{"name":"Seq: regionTableHeader","children":[{"name":"Def: Signature","value":"cmVnaQ=="},{"name":"Def: Checksum","value":"roxrxg=="},{"name":"Def: EntryCount","value":"AgAAAA=="},{"name":"Def: Signature","value":"cmVnaQ=="},{"name":"Def: Checksum","value":"roxrxg=="},{"name":"Def: EntryCount","value":"AgAAAA=="}]},{"name":"RepN","children":[{"name":"Cho","children":[{"name":"Seq: metadata","children":[{"name":"Def: Guid","value":"BqJ8i5BHmku4/ldfBQ+Ibg=="},{"name":"Def: FileOffset","value":"AAAgAAAAAAA="},{"name":"Def: Length","value":"AAAQAA=="},{"name":"Def: Required","value":"AQAAAA=="},{"name":"Sub"}]},{"name":"Seq: bat","children":[{"name":"Def: Guid","value":"ZnfCLSP2AEKdZBFem/1KCA=="},{"name":"Def: FileOffset","value":"AAAwAAAAAAA="},{"name":"Def: Length","value":"AAAQAA=="},{"name":"Def: Required","value":"AQAAAA=="},{"name":"Sub"}]}]},{"name":"Cho"}]}]}]},{"name":"Sub: region","children":[{"name":"Seq","children":[{"name":"Seq: regionTableHeader"},{"name":"RepN","children":[{"name":"Cho","children":[{"name":"Seq: metadata","children":[{"name":"Def: Guid","value":"BqJ8i5BHmku4/ldfBQ+Ibg=="},{"name":"Def: FileOffset","value":"AAAgAAAAAAA="},{"name":"Def: Length","value":"AAAQAA=="},{"name":"Def: Required","value":"AQAAAA=="},{"name":"Sub","children":[{"name":"Seq: metadataTable","children":[{"name":"Def: metadataTableAnchor","value":""},{"name":"Def: Signature","value":"bWV0YWRhdGE="},{"name":"Def: EntryCount","value":"BQA="},{"name":"RepN","children":[{"name":"Cho","children":[{"name":"Str: page38Data","children":[{"name":"Seq","children":[{"name":"Def: ItemId","value":"qxLKvuayI0WT78MJ4ADHRg=="},{"name":"Def: Offset","value":"GAABAA=="},{"name":"Def: Length","value":"EAAAAA=="},{"name":"Def: Is","value":"BgAAAA=="},{"name":"Pre","children":[{"name":"Sub: data","children":[{"name":"Def: Page83Data","value":"067pWwDdJESLYR4kKC4yOA=="}]}]}]}]},{"name":"Str: physicalSectorSize","children":[{"name":"Seq","children":[{"name":"Def: ItemId","value":"x0ijzV1EcUScyemIUlHFVg=="},{"name":"Def: Offset","value":"FAABAA=="},{"name":"Def: Length","value":"BAAAAA=="},{"name":"Def: Is","value":"BgAAAA=="},{"name":"Pre","children":[{"name":"Sub: data","children":[{"name":"Def: PhysicalSectorSize","value":"ABAAAA=="}]}]}]}]},{"name":"Str: logicalSectorSize","children":[{"name":"Seq","children":[{"name":"Def: ItemId","value":"Hb9BgW+pCUe6R/IzqPqrXw=="},{"name":"Def: Offset","value":"EAABAA=="},{"name":"Def: Length","value":"BAAAAA=="},{"name":"Def: Is","value":"BgAAAA=="},{"name":"Pre","children":[{"name":"Sub: data","children":[{"name":"Def: LogicalSectorSize","value":"AAIAAA=="}]}]}]}]},{"name":"Str: virtualDiskSize","children":[{"name":"Seq","children":[{"name":"Def: ItemId","value":"JEKlLxvNdkiyEV2+2Dv0uA=="},{"name":"Def: Offset","value":"CAABAA=="},{"name":"Def: Length","value":"CAAAAA=="},{"name":"Def: Is","value":"BgAAAA=="},{"name":"Pre","children":[{"name":"Sub: data","children":[{"name":"Def: VirtualDiskSize","value":"AAAAAQAAAAA="}]}]}]}]},{"name":"Str: fileParameters","children":[{"name":"Seq","children":[{"name":"Def: ItemId","value":"N2ehyjb6Q02ztjPwqkTnaw=="},{"name":"Def: Offset","value":"AAABAA=="},{"name":"Def: Length","value":"CAAAAA=="},{"name":"Def: Is","value":"BAAAAA=="},{"name":"Pre","children":[{"name":"Sub: data","children":[{"name":"Seq","children":[{"name":"Def: BlockSize","value":"AAAAAg=="},{"name":"Def: LeaveBlocksAllocated_HasParent","value":"AAAAAA=="}]}]}]}]}]}]},{"name":"Cho"},{"name":"Cho"},{"name":"Cho"},{"name":"Cho"}]}]}]}]},{"name":"Seq: bat","children":[{"name":"Def: Guid","value":"ZnfCLSP2AEKdZBFem/1KCA=="},{"name":"Def: FileOffset","value":"AAAwAAAAAAA="},{"name":"Def: Length","value":"AAAQAA=="},{"name":"Def: Required","value":"AQAAAA=="},{"name":"Sub"}]}]},{"name":"Cho"}]}]}]}]}]},{"name":"Sub","children":[{"name":"Seq","children":[{"name":"Pre","children":[{"name":"RepN","children":[{"name":"Cho","children":[{"name":"Seq","children":[{"name":"Def: payload_block_fully_present","value":"BgBAAAAAAAA="},{"name":"Sub","children":[{"name":"Seq","children":[{"name":"Def: payload_block_start","value":"Mw=="},{"name":"Def: payload_block_end","value":"AA=="}]}]}]}]}]}]}]}]}]}); /* generated */
