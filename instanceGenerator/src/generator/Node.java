package generator;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class Node {
	int id;
	double X;
	double Y;
	
	HashSet<Node> neighbors;
	
	public Node(int i, double iX, double iY){
		this.id = i;
		this.X = iX;
		this.Y = iY;
		neighbors = new LinkedHashSet<Node>();
	}
	
	public int getId() {
		return id;
	}
	
	public double getX() {
		return X;
	}
	
	public double getY() {
		return Y;
	}
	
	
	@Override
	public boolean equals(Object o){
		if(null == o){
			return false;
		}
		if(this==o){
			return true;
		}
		
		Node p = (Node)o;
		if(p.id != id){
			return false;
		}
		if(p.X != X ){
			return false;
		}
		if(p.Y != Y ){
			return false;
		}
		return true;
	}

	public void addNeigh(Node q) {
		neighbors.add(q);
		q.neighbors.add(this);
	}

	public boolean neighborsWith(Node q) {
		return neighbors.contains(q);
	}

	public HashSet<Node> getNeighbors() {
		return neighbors;
	}
	
}
