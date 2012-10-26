package generator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

public abstract class GridGenerator {
	Graph g;
	int ro;
	int col;
	int targetSize;
	String fileName;
	
	GridGenerator(int nRows, int nColumns){
		ro = nRows;
		col = nColumns;
		resetGraph(ro*col);
	}
	
	public void resetGraph(int n){
		g = new Graph();
		g.setCapacity(n);
	}
	
	public int rows(){
		return ro;
	}
	
	public int columns(){
		return col;
	}
	
	abstract public void generate();
	
	public int index(int r, int c){
		if(r < 0 || r >= ro || c < 0 || c >= col ){
			try {
				throw new Exception("Index out of bound!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		int i = r*col + c;
		return i;
	}
	
	public Node getAt(int r, int c){
		return g.getAt(index(r,c));
	}
	
	public void setAt(int r, int c, Node p){
		g.setAt(index(r,c), p);
	}

	public abstract void setTargetSize(int desiredNumNodes);
	
	protected abstract void linkNeighbors();

	public int getTargetSize() {
		return targetSize;
	}

	public void setFileName(String p) {
		fileName = new String(p);
	}

	public String getFileName() {
		return fileName;
	}
	
	
	public void writefile() throws FileNotFoundException{
		FileOutputStream fos = new FileOutputStream(fileName);
		PrintStream p = new PrintStream(fos);
		
		p.println("N = "+g.getNumNodes()+";");
		p.println("C=16;");
		p.println("MC = "+g.getMC()+";");
		p.println("lambda = "+g.getLambda()+";");
		
		p.println("\nw1 = 1000;");
		p.println("w2 = 10;");
		p.println("w3 = 1;\n");
		
		//nodes
		p.println("nodes = {");
		for(int i = 0; i < g.getNumNodes(); i++){
			Node a = g.getAt(i);
			p.println("< "+a.getId()+" "+a.getX()+" "+a.getY()+" >");
		}
		p.println("};\n");
		
		//edges
		p.println("E = {");
		Iterator<String> it = g.getEdges().iterator();
		while(it.hasNext()){
			p.println(it.next());
		}
		p.println("};");
		
		
	}

}
