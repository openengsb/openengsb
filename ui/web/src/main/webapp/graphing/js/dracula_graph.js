/*
 *  Dracula Graph Layout and Drawing Framework 0.0.3alpha
 *  (c) 2010 Philipp Strathausen <strathausen@gmail.com>, http://strathausen.eu
 *
 *  based on the Graph JavaScript framework, version 0.0.1
 *  (c) 2006 Aslak Hellesoy <aslak.hellesoy@gmail.com>
 *  (c) 2006 Dave Hoover <dave.hoover@gmail.com>
 *
 *  Ported from Graph::Layouter::Spring in
 *    http://search.cpan.org/~pasky/Graph-Layderer-0.02/
 *  The algorithm is based on a spring-style layouter of a Java-based social
 *  network tracker PieSpy written by Paul Mutton E<lt>paul@jibble.orgE<gt>.
 *
 *  This code is freely distributable under the terms of an MIT-style license.
 *  For details, see the Graph web site: http://dev.buildpatternd.com/trac
 *
 *  Links:
 *
 *  Graph Dracula JavaScript Framework:
 *      http://graphdracula.net
 *
 *  Demo of the original applet:
 *      http://redsquirrel.com/dave/work/webdep/
 *
 *  Mirrored original source code at snipplr:
 *      http://snipplr.com/view/1950/graph-javascript-framework-version-001/
 *
 *  Original usage example:
 *      http://ajaxian.com/archives/new-javascriptcanvas-graph-library
 *
/*--------------------------------------------------------------------------*/



/*
 * Graph
 */
var Graph = function() {
    this.nodes = [];
    this.nodelist = []; // nodes by index number, only used once TODO use only one node container
    this.edges = [];
    this.snapshots = []; // previous graph states
};
Graph.prototype = {
    /* 
     * add a node
     * @id          the node's ID (string or number)
     * @content     (optional, dictionary) can contain any information that is
     *              being interpreted by the layout algorithm or the graph
     *              representation
     */
    addNode: function(id, content) {
        /* testing if node is already existing in the graph */
        if(this.nodes[id] == undefined) {
                this.nodes[id] = new Graph.Node(id, content || {"id" : id}); /* nodes indexed by node id */
                this.nodelist.push(this.nodes[id]); /* node list indexed by numbers */
        }
        return this.nodes[id];
    },

    // TODO rename style to content
    addEdge: function(source, target, style) {
        var s = this.addNode(source);
        var t = this.addNode(target);
        var edge = { source: s, target: t, style: style, weight: style&&style.weight||1 }; // TODO tidy up here
        s.edges.push(edge);
        this.edges.push(edge);
        /* add an edge back if graph undirected */
        if(!style || !style.directed) {
            var backedge = { source: t, target: s, style: style, weight : style&&style.weight||1, backedge : edge }; // TODO tidy up here
            this.edges.push(backedge);
            t.edges.push(backedge);
        }
    },
    
    /*
     * Preserve a copy of the graph state (nodes, positions, ...)
     * @comment     a comment describing the state
     * @about       a list with objects to be marked as significant in this state (TODO)
     */
    snapShot: function(comment, about) {
        // TODO get rid of the jQuery plugin dependence just for the deep copying
        var graph = new Graph();
        jQuery.extend(true, graph.nodes, this.nodes);
        jQuery.extend(true, graph.nodelist, this.nodelist);
        jQuery.extend(true, graph.edges, this.edges);
        graph.snapShot = null;
        this.snapshots.push({comment: comment, graph: graph});
    }
};

/*
 * Node
 */
Graph.Node = function(id, value){
    value.id = id;
    value.edges = [];
    return value;
};
Graph.Node.prototype = {
};

/*
 * Renderer base class
 */
Graph.Renderer = {};

/*
 * Renderer implementation using RaphaelJS
 */
Graph.Renderer.Raphael = function(element, graph, width, height) {
    this.width = width||400;
    this.height = height||400;
    var selfRef = this;
    this.r = Raphael(element, this.width, this.height);
    this.radius = 40; /* max dimension of a node */
    this.graph = graph;
    this.mouse_in = false;

    /* TODO default node rendering function */
    if(!this.graph.render) {
        this.graph.render = function() {
            return;
        }
    }
    
    /*
     * Dragging
     */
    this.isDrag = false;
    this.dragger = function (e) {
        this.dx = e.clientX;
        this.dy = e.clientY;
        selfRef.isDrag = this;
        this.set && this.set.animate({"fill-opacity": .1}, 200) && this.set.toFront();
        e.preventDefault && e.preventDefault();
    };

    document.onmousemove = function (e) {
        e = e || window.event;
        if (selfRef.isDrag) {
            var bBox = selfRef.isDrag.set.getBBox();
            // TODO round the coordinates here (eg. for proper image representation)
            var newX = e.clientX - selfRef.isDrag.dx + (bBox.x + bBox.width / 2);
            var newY = e.clientY - selfRef.isDrag.dy + (bBox.y + bBox.height / 2);
            /* prevent shapes from being dragged out of the canvas */
            var clientX = e.clientX - (newX < 20 ? newX - 20 : newX > selfRef.width - 20 ? newX - selfRef.width + 20 : 0);
            var clientY = e.clientY - (newY < 20 ? newY - 20 : newY > selfRef.height - 20 ? newY - selfRef.height + 20 : 0);
            selfRef.isDrag.set.translate(clientX - selfRef.isDrag.dx, clientY - selfRef.isDrag.dy);
            for (var i in selfRef.graph.edges) {
                selfRef.graph.edges[i].connection && selfRef.graph.edges[i].connection.draw();
            }
            //selfRef.r.safari();
            selfRef.isDrag.dx = clientX;
            selfRef.isDrag.dy = clientY;
        }
    };
    document.onmouseup = function () {
        selfRef.isDrag && selfRef.isDrag.set.animate({"fill-opacity": .6}, 500);
        selfRef.isDrag = false;
    };
};
Graph.Renderer.Raphael.prototype = {
    translate: function(point) {
        return [
            Math.round((point[0] - this.graph.layoutMinX) * this.factorX + this.radius),
            Math.round((point[1] - this.graph.layoutMinY) * this.factorY + this.radius)
        ];
    },

    rotate: function(point, length, angle) {
        var dx = length * Math.cos(angle);
        var dy = length * Math.sin(angle);
        return [point[0]+dx, point[1]+dy];
    },

    draw: function() {
        this.factorX = (this.width - 2 * this.radius) / (this.graph.layoutMaxX - this.graph.layoutMinX);
        this.factorY = (this.height - 2 * this.radius) / (this.graph.layoutMaxY - this.graph.layoutMinY);
        for (i in this.graph.nodes) {
            this.drawNode(this.graph.nodes[i]);
        }
        for (var i = 0; i < this.graph.edges.length; i++) {
            this.drawEdge(this.graph.edges[i]);
        }
    },

    drawNode: function(node) {
        var point = this.translate([node.layoutPosX, node.layoutPosY]);
        node.point = point;
        
        /* if node has already been drawn, move the nodes */
        if(node.shape) {
            var oBBox = node.shape.getBBox();
            var opoint = [ oBBox.x + Math.round(oBBox.width / 2) , oBBox.y + Math.round(oBBox.height / 2) ];
            node.shape.translate(point[0]-opoint[0], point[1]-opoint[1]);
            this.r.safari();
            return;
        }/* else, draw new nodes */
        var shape;
        /* if a node renderer function is provided by the user, then use it */
        if(node.render) {
            shape = node.render(this.r, node);
        /* or check for an ajax representation of the nodes */
        } else if(node.shape) {
            // TODO ajax representation
        /* the default node drawing */
        } else {
            var color = Raphael.getColor();
            shape = this.r.set().
                push(this.r.ellipse(point[0], point[1], 30, 20).attr({fill: color, stroke: color, "stroke-width": 2})).
                push(this.r.text(point[0], point[1] + 30, node.label || node.id));
        }
        shape.attr({"fill-opacity": .6});
        /* reference to the node an element belongs to, needed for dragging all elements of a node */
        shape.items.forEach(function(item){ item.set = shape; item.node.style.cursor = "move"; });
        shape.mousedown(this.dragger);
        node.shape = shape;
    },
    drawEdge: function(edge) {
        /* if this edge already exists the other way around and is undirected */
        if(edge.backedge)
            return;
        /* if edge already has been drawn, only refresh the edge */
        edge.connection && edge.connection.draw();
        if(!edge.connection) {
            edge.style && edge.style.callback && edge.style.callback(edge);//TODO move this somewhere else
            edge.connection = this.r.connection(edge.source.shape, edge.target.shape, edge.style);
        }
    }
};
Graph.Layout = {};
Graph.Layout.Spring = function(graph) {
                this.graph = graph;
                this.iterations = 500;
                this.maxRepulsiveForceDistance = 6;
                this.k = 2;
                this.c = 0.01;
                this.maxVertexMovement = 0.5;
        };
Graph.Layout.Spring.prototype = {
        layout: function() {
                this.layoutPrepare();
            for (var i = 0; i < this.iterations; i++) {
                        this.layoutIteration();
                }
                this.layoutCalcBounds();
        },
       
        layoutPrepare: function() {
            for (i in this.graph.nodes) {
                    var node = this.graph.nodes[i];
                        node.layoutPosX = 0;
                        node.layoutPosY = 0;
                        node.layoutForceX = 0;
                        node.layoutForceY = 0;
                }
                
        },
       
        layoutCalcBounds: function() {
                var minx = Infinity, maxx = -Infinity, miny = Infinity, maxy = -Infinity;

            for (i in this.graph.nodes) {
                        var x = this.graph.nodes[i].layoutPosX;
                        var y = this.graph.nodes[i].layoutPosY;
                                               
                        if(x > maxx) maxx = x;
                        if(x < minx) minx = x;
                        if(y > maxy) maxy = y;
                        if(y < miny) miny = y;
                }

                this.graph.layoutMinX = minx;
                this.graph.layoutMaxX = maxx;
                this.graph.layoutMinY = miny;
                this.graph.layoutMaxY = maxy;
        },
       
        layoutIteration: function() {
                // Forces on nodes due to node-node repulsions
            for (var i = 0; i < this.graph.nodelist.length; i++) {
                    var node1 = this.graph.nodelist[i];
                    for (var j = i + 1; j < this.graph.nodelist.length; j++) {
                            var node2 = this.graph.nodelist[j];
                                this.layoutRepulsive(node1, node2);
                        }
                }
                // Forces on nodes due to edge attractions
            for (var i = 0; i < this.graph.edges.length; i++) {
                    var edge = this.graph.edges[i];
                        this.layoutAttractive(edge);             
                }
               
                // Move by the given force
            for (i in this.graph.nodes) {
                    var node = this.graph.nodes[i];
                    var xmove = this.c * node.layoutForceX;
                    var ymove = this.c * node.layoutForceY;

                    var max = this.maxVertexMovement;
                    if(xmove > max) xmove = max;
                    if(xmove < -max) xmove = -max;
                    if(ymove > max) ymove = max;
                    if(ymove < -max) ymove = -max;
                   
                    node.layoutPosX += xmove;
                    node.layoutPosY += ymove;
                    node.layoutForceX = 0;
                    node.layoutForceY = 0;
                }
        },

        layoutRepulsive: function(node1, node2) {
                var dx = node2.layoutPosX - node1.layoutPosX;
                var dy = node2.layoutPosY - node1.layoutPosY;
                var d2 = dx * dx + dy * dy;
                if(d2 < 0.01) {
                        dx = 0.1 * Math.random() + 0.1;
                        dy = 0.1 * Math.random() + 0.1;
                        var d2 = dx * dx + dy * dy;
                }
                var d = Math.sqrt(d2);
                if(d < this.maxRepulsiveForceDistance) {
                        var repulsiveForce = this.k * this.k / d;
                        node2.layoutForceX += repulsiveForce * dx / d;
                        node2.layoutForceY += repulsiveForce * dy / d;
                        node1.layoutForceX -= repulsiveForce * dx / d;
                        node1.layoutForceY -= repulsiveForce * dy / d;
                }
        },

        layoutAttractive: function(edge) {
                var node1 = edge.source;
                var node2 = edge.target;
               
                var dx = node2.layoutPosX - node1.layoutPosX;
                var dy = node2.layoutPosY - node1.layoutPosY;
                var d2 = dx * dx + dy * dy;
                if(d2 < 0.01) {
                        dx = 0.1 * Math.random() + 0.1;
                        dy = 0.1 * Math.random() + 0.1;
                        var d2 = dx * dx + dy * dy;
                }
                var d = Math.sqrt(d2);
                if(d > this.maxRepulsiveForceDistance) {
                        d = this.maxRepulsiveForceDistance;
                        d2 = d * d;
                }
                var attractiveForce = (d2 - this.k * this.k) / this.k;
                if(edge.attraction == undefined) edge.attraction = 1;
                attractiveForce *= Math.log(edge.attraction) * 0.5 + 1;
               
                node2.layoutForceX -= attractiveForce * dx / d;
                node2.layoutForceY -= attractiveForce * dy / d;
                node1.layoutForceX += attractiveForce * dx / d;
                node1.layoutForceY += attractiveForce * dy / d;
        }
};

/*
 * usefull JavaScript extensions, 
 */
//Array.prototype.forEach = function(f) {
//     var l = this.length;
//     for( var i = 0; i < l; i++) f(this[i]);
//};

function log(a) {console.log&&console.log(a);}

// this will eventually mess up array usage with for-in-loops
//Array.prototype.onEach = function(f, arg) {
//     var l = this.length;
//     for( var i = 0; i < l; i++) this[i][f]( arg );
//}

/*
 * Raphael Tooltip Plugin
 * - attaches an element as a tooltip to another element
 *
 * Usage example, adding a rectangle as a tooltip to a circle:
 *
 *      paper.circle(100,100,10).tooltip(paper.rect(0,0,20,30));
 *
 * If you want to use more shapes, you'll have to put them into a set.
 *
 */
Raphael.el.tooltip = function (tp) {
    this.tp = tp;
    this.tp.o = {x: 0, y: 0};
    this.tp.hide();
    this.hover(
        function(event){ 
            this.mousemove(function(event){ 
                this.tp.translate(event.clientX - 
                    this.tp.o.x,event.clientY - this.tp.o.y);
                this.tp.o = {x: event.clientX, y: event.clientY};
            });
            this.tp.show().toFront();
        }, 
        function(event){
            this.tp.hide();
            this.unmousemove();
            });
    return this;
};
