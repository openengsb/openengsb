/*
 * Various algorithms and data structures, licensed under the MIT-license.
 * (c) 2010 by Johann Philipp Strathausen <strathausen@gmail.com>
 * http://strathausen.eu
 *
 */



/*
   Path-finding algorithm Bellman-Ford
   
   finds the shortest paths from one node to all nodes
   - runs in O( |E| · |V| ), where E = edges and V = vertices (nodes)
   - can run on graphs with negative edge weights as long as they do not have
     any negative weight cycles 
 */
function bellman_ford(g, source) {

    /* STEP 1: initialisation */
    for(var n in g.nodes)
        g.nodes[n].distance = Infinity;
        /* predecessors are implicitly null */
    source.distance = 0;
    g.snapShot("Initiallisation: Set all distances are infinite and all predecessors are null.");
    
    /* STEP 2: relax each edge (this is at the heart of Bellman-Ford) */
    /* repeat this for the number of nodes minus one */
    for(var i = 1; i < g.nodes.length; i++)
        /* for each edge */
        for(var e in g.edges) {
            var edge = g.edges[e];
            if(edge.source.distance + edge.weight < edge.target.distance) {
                g.snapShot("Relax edge between "+edge.source.id+" and "+edge.target.id+".");
                edge.target.distance = edge.source.distance + edge.weight;
                edge.target.predecessor = edge.source;
            }
        }
    
    g.snapShot("Ready.");
    
    /* STEP 3: TODO Check for negative cycles */
    /* For now we assume here that the graph does not contain any negative
       weights cycles. (this is left as an excercise to the reader[tm]) */
}



/*
   Path-finding algorithm Dijkstra
   
   - worst-case running time is O( |E| + |V| · log |V| ) thus better than
     Bellman-Ford, but cannot handle negative edge weights
 */
function dijkstra(g, source) {
    /* initially, all distances are infinite and all predecessors are null */
    for(var n in g.nodes)
        g.nodes[n].distance = Infinity;
        /* predecessors are implicitly null */
    source.distance = 0;
    var counter=0;
    /* set of unoptimized nodes, sorted by their distance (but a Fibonacci heap
       would be better) */
    var q = new BinaryMinHeap(g.nodes, "distance");

    var node;
    /* get the node with the smallest distance */
    /* as long as we have unoptimized nodes */

    while(q.min()!=undefined) {
        /* remove the latest */
        node=q.extractMin();
        node.optimized=true;

        /* no nodes accessible from this one, should not happen */
        if(node.distance == Infinity)
            throw "Orphaned node!";

        /* for each neighbour of node */
        for(e in node.edges) {
            if(node.edges[e].target.optimized)
                continue;

            /* look for an alternative route */
            var alt = node.distance + node.edges[e].weight;
            
            /* update distance and route if a better one has been found */
            if (alt < node.edges[e].target.distance) {
            
                /* update distance of neighbour */
                node.edges[e].target.distance = alt;

                /* update priority queue */
                q.heapify();

                /* update path */
                node.edges[e].target.predecessor = node;
            }
        }
    }
}



/* Runs at worst in O(|V|³) and at best in Omega(|V|³) :-)
   complexity Sigma(|V|²) */
/* This implementation is not yet ready for general use, but works with the
   Dracula graph library. */
function floyd_warshall(g, source) {

    /* Step 1: initialising empty path matrix (second dimension is implicit) */
    var path = [];
    var next = [];
    var n = g.nodes.length;

    /* construct path matrix, initialize with Infinity */
    for(j in g.nodes) {
        path[j] = [];
        next[j] = [];
        for(i in g.nodes)
            path[j][i] = j == i ? 0 : Infinity;
    }   
    
    /* initialize path with edge weights */
    for(e in g.edges)
        path[g.edges[e].source.id][g.edges[e].target.id] = g.edges[e].weight;
    
    /* Note: Usually, the initialisation is done by getting the edge weights
       from a node matrix representation of the graph, not by iterating through
       a list of edges as done here. */
    
    /* Step 2: find best distances (the heart of Floyd-Warshall) */
    for(k in g.nodes){
        for(i in g.nodes) {
            for(j in g.nodes)
                if(path[i][j] > path[i][k] + path[k][j]) {
                    path[i][j] = path[i][k] + path[k][j];
                    /* Step 2.b: remember the path */
                    next[i][j] = k;
                }
        }
    }
    /* Step 3: Path reconstruction */
    function getPath(i, j) {
        if(path[i][j] == Infinity)
            throw "There is no path.";
        var intermediate = next[i][j];
        if(intermediate == undefined)
            return null;
        else
            return getPath(i, intermediate)
                .concat([intermediate])
                .concat(getPath(intermediate, j));
    }
//    console&&console.log(path);
//    console&&console.log(next);
    
    /* TODO use the knowledge */
}



/*
   A simple binary min-heap serving as a priority queue
   - takes an array as the input, with elements having a key property
   - elements will look like this:
        {
            key: "... key property ...", 
            value: "... element content ..."
        }
    - provides insert(), min(), extractMin() and heapify()
    - example usage (e.g. via the Firebug console):
        var x = {foo:20,hui:"bla"};
        var a = new BinaryMinHeap([x,{foo:3},{foo:10},{foo:20},{foo:30},{foo:6},{foo:1},{foo:3}],"foo");
        console.log(a.extractMin());
        console.log(a.extractMin());
        x.ma=0;
        a.heapify(); // call this when key updated
        console.log(a.extractMin());
        console.log(a.extractMin());
    - can also be used on a simple array, like [9,7,8,5]
 */
function BinaryMinHeap(array, key) {
    
    /* Binary tree stored in an array, no need for a complicated data structure */
    var tree = [];
    
    var key = key || 'key';
    
    /* Calculate the index of the parent or a child */
    var parent = function(index) { return Math.floor((index - 1)/2); };
    var right = function(index) { return 2 * index + 2; };
    var left = function(index) { return 2 * index + 1; };

    /* Helper function to swap elements with their parent 
       as long as the parent is bigger */
    function bubble_up(i) {
        var p = parent(i);
        while((p >= 0) && (tree[i][key] < tree[p][key])) {
            /* swap with parent */
            tree[i] = tree.splice(p, 1, tree[i])[0];
            /* go up one level */
            i = p;
            p = parent(i);
        }
    }

    /* Helper function to swap elements with the smaller of their children
       as long as there is one */
    function bubble_down(i) {
        var l = left(i);
        var r = right(i);
        
        /* as long as there are smaller children */
        while(tree[l] && (tree[i][key] > tree[l][key]) || tree[r] && (tree[i][key] > tree[r][key])) {
            
            /* find smaller child */
            var child = tree[l] ? tree[r] ? tree[l][key] > tree[r][key] ? r : l : l : l;
            
            /* swap with smaller child with current element */
            tree[i] = tree.splice(child, 1, tree[i])[0];
            
            /* go up one level */
            i = child;
            l = left(i);
            r = right(i);
        }
    }
    
    /* Insert a new element with respect to the heap property
       1. Insert the element at the end
       2. Bubble it up until it is smaller than its parent */
    this.insert = function(element) {
    
        /* make sure there's a key property */
        (element[key] == undefined) && (element = {key:element});
        
        /* insert element at the end */
        tree.push(element);

        /* bubble up the element */
        bubble_up(tree.length - 1);
    }
    
    /* Only show us the minimum */
    this.min = function() {
        return tree.length == 1 ? undefined : tree[0];
    }
    
    /* Return and remove the minimum
       1. Take the root as the minimum that we are looking for
       2. Move the last element to the root (thereby deleting the root) 
       3. Compare the new root with both of its children, swap it with the
          smaller child and then check again from there (bubble down)
    */
    this.extractMin = function() {
        var result = this.min();
        
        /* move the last element to the root or empty the tree completely */
        /* bubble down the new root if necessary */
        (tree.length == 1) && (tree = []) || (tree[0] = tree.pop()) && bubble_down(0);
        
        return result;        
    }
    
    /* currently unused, TODO implement */
    this.changeKey = function(index, key) {
        throw "function not implemented";
    }

    this.heapify = function() {
        for(var start = Math.floor((tree.length - 2) / 2); start >= 0; start--) {
            bubble_down(start);
        }
    }
    
    //this.debug = function() {console&&console.log("----");for(i in tree){console&&console.log(tree[i].id,tree[i].distance);};}
    /* insert the input elements one by one only when we don't have a key property (TODO can be done more elegant) */
//    if (key=="key")
        for(i in (array || []))
            this.insert(array[i]);
//    else {
//        this.tree = array; // TODO there's an error here, maybe the array needs to be cloned or copied, because all reference is lost after this assignment
//        this.heapify();
//    }
}



/*
    Quick Sort:
        1. Select some random value from the array, the median.
        2. Divide the array in three smaller arrays according to the elements
           being less, equal or greater than the median.
        3. Recursively sort the array containg the elements less than the
           median and the one containing elements greater than the median.
        4. Concatenate the three arrays (less, equal and greater).
        5. One or no element is always sorted.
    Note: This could be implemented more efficiently by using only one array.
*/
function quickSort(arr) {
    /* recursion anchor: one element is always sorted */
    if(arr.length <= 1) return arr;
    /* randomly selecting some value */
    var median = arr[Math.floor(Math.random() * arr.length)];
    var arr1 = [], arr2 = [], arr3 = [];
    for(var i in arr) {
        arr[i] < median && arr1.push(arr[i]);
        arr[i] == median && arr2.push(arr[i]);
        arr[i] > median && arr3.push(arr[i]);
    }
    /* recursive sorting and assembling final result */
    return quickSort(arr1).concat(arr2).concat(quickSort(arr3));
}

/*
    Selection Sort:
        1. Select the minimum and remove it from the array
        2. Sort the rest recursively
        3. Return the minimum plus the sorted rest
        4. An array with only one element is already sorted
*/
function selectionSort(arr) {
    /* recursion anchor: one element is always sorted */
    if(arr.length == 1) return arr;
    var minimum = Infinity;
    var index;
    for(var i in arr) {
        if(arr[i] < minimum) {
            minimum = arr[i];
            index = i; /* remember the minimum index for later removal */
        }
    }
    /* remove the minimum */
    arr.splice(index, 1);
    /* assemble result and sort recursively (could be easily done iteratively as well)*/
    return [minimum].concat(selectionSort(arr));
}

/*
    Merge Sort:
        1. Cut the array in half
        2. Sort each of them recursively
        3. Merge the two sorted arrays
        4. An array with only one element is already sorted

*/
function mergeSort(arr) {
    /* merges two sorted arrays into one sorted array */
    function merge(a, b) {
        /* result set */
        var c = [];
        /* as long as there are elements in the arrays to be merged */
        while(a.length > 0 || b.length > 0){
            /* are there elements to be merged, if yes, compare them and merge */
            var n = a.length > 0 && b.length > 0 ? a[0] < b[0] ? a.shift() : b.shift() : b.length > 0 ? b.shift() : a.length > 0 ? a.shift() : null;
            /* always push the smaller one onto the result set */
            n != null && c.push(n);
        }
        return c;
    }
    /* this mergeSort implementation cuts the array in half, wich should be fine with randomized arrays, but introduces the risk of a worst-case scenario */
    median = Math.floor(arr.length / 2);
    var part1 = arr.slice(0, median); /* for some reason it doesn't work if inserted directly in the return statement (tried so with firefox) */
    var part2 = arr.slice(median - arr.length);
    return arr.length <= 1 ? arr : merge(
        mergeSort(part1), /* first half */
        mergeSort(part2) /* second half */
    );
}

/* Balanced Red-Black-Tree */
function RedBlackTree(arr) {
    
}

function BTree(arr) {
    
}

function NaryTree(n, arr) {
    
}


/**
 * Curry - Function currying
 * Copyright (c) 2008 Ariel Flesler - aflesler(at)gmail(dot)com | http://flesler.blogspot.com
 * Licensed under BSD (http://www.opensource.org/licenses/bsd-license.php)
 * Date: 10/4/2008
 *
 * @author Ariel Flesler
 * @version 1.0.1
 */

function curry( fn ){
	return function(){
		var args = curry.args(arguments),
			master = arguments.callee,
			self = this;

		return args.length >= fn.length ? fn.apply(self,args) :	function(){
			return master.apply( self, args.concat(curry.args(arguments)) );
		};
	};
};

curry.args = function( args ){
	return Array.prototype.slice.call(args);
};

Function.prototype.curry = function(){
	return curry(this);
};
