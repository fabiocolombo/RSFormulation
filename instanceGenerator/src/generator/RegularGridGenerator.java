package generator;

public abstract class RegularGridGenerator extends GridGenerator {

	double side;
	double height;
	
	RegularGridGenerator(int nRows, int nColumns) {
		super(nRows, nColumns);
	}
	
	@Override
	public abstract void generate();
	
	public void setSide(double side) {
		this.side = side;
	}
	
	public double getSide() {
		return side;
	}
	
	void setHeight(double height) {
		this.height = height;
	}
	
	public double getHeight() {
		return height;
	}

	@Override 
	public void setTargetSize(int desiredNumNodes) {
		targetSize = desiredNumNodes;
		double l = Math.sqrt(desiredNumNodes);
		ro = (int) Math.ceil(l);
		col = ro;
		resetGraph(ro*col);		
	}

	public String getPrefix(){
		return ("r");
	}
}
