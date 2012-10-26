package generator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;


public class Graph {
	int capacity;
	int numNodes;
	int MC;
	int Kapacity = 16;
	
	Node nodes[];
	
	HashSet<String> edges = null;
	private int lambda;
	
	public Graph(){
		capacity = 0;
		numNodes = 0;
		MC = 0;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public int getNumNodes() {
		return numNodes;
	}

	public void setCapacity(int n) {
		capacity = n;
		nodes = new Node[capacity];
	}

	public Node getAt(int i) {
		return nodes[i];
	}

	public void setAt(int i, Node p) {
		nodes[i] = p; 	
	}
	
	public void append(Node p){
		nodes[numNodes] = p;
		numNodes++;
	}

	void makeEdges(){
		edges = new LinkedHashSet<String>();
		for(int i = 0; i < numNodes; i++){
			Node a = getAt(i);
			Iterator<Node> it = a.getNeighbors().iterator();
			while(it.hasNext()){
				Node b = it.next();
				int o = Math.min(a.getId(), b.getId());
				int d = Math.max(a.getId(), b.getId());
				String s = new String("< "+o+" "+d+" >");
				edges.add(s);
			}
		}
	}
	
	public HashSet<String> getEdges() {
		if(null == edges){
			makeEdges();
		}
		return edges;
	}

	void estimateMC(){
		MC = (int)Math.ceil((double)numNodes/(double)Kapacity);
	}
	
	public int getMC() {
		if(0 == MC){
			estimateMC();
		}
		return MC;
	}

	public int getLambda() {
		if(0==lambda){
			estimateLambda();
		}
		return lambda;
	}

	private void estimateLambda() {
		lambda = (int)Math.ceil((double)numNodes/(double)getMC());
		
	}
}
