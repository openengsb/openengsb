var OESB = function(){
  this.width = 800;
  this.height = 500;
  this.graph = new Graph();

  this.layouter = new Graph.Layout.Spring(this.graph);
  this.renderer = new Graph.Renderer.Raphael('canvas', this.graph, this.width, this.height),

  this.redraw = function() {
    this.layouter.layout();
    this.renderer.draw();
  };
  this.action_renderer = function(r,n){
    var set = r.set().push(
      r.rect(0, 0, 50, 50).attr({"fill":"#fa8", "stroke-width":2, r:"9px"})
    );
    set.click(function(){
      node = n.oesb.create_action(prompt("ID"))
      n.oesb.connect_nodes(n, node);
      n.oesb.redraw();
    });
    return set;
  }


  this.create_action = function(id){
    var node = this.graph.addNode(id, {render:this.action_renderer});
    node.oesb = this;
    node.graph = this.graph;
    return node;
  }
  this.connect_nodes = function(from, to){
    this.graph.addEdge(from.id, to.id, {directed:true})
  }
  this.create_action("start");
};

var Test = function(name){
  this.name = name;
  this.isName = function(){
    return this.name
  }
}
