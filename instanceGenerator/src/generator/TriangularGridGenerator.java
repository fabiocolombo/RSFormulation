package generator;

public class TriangularGridGenerator extends RegularGridGenerator {

	double halfSide;
	double bHeight;
	
	public TriangularGridGenerator(int nRows, int nColumns) {
		super(nRows, nColumns);
	}
	
	public void generate(){
		double x = 0;
		double y = 0;
		double h = 0;
		boolean evenRow, evenColumn;
		for(int r = 0; r < ro; r++){
			if(0 == r%2){
				evenRow = true;
			} else {
				evenRow = false;
			}
			x = 0;
			for(int c = 0; c < col; c++){
				evenColumn = c%2==0; 
				x+=halfSide;
				if(evenRow){
					if(evenColumn){
						y = bHeight;
					} else {
						y = height - bHeight;
					}
				} else {
					if(evenColumn){
						y = 2 * bHeight;
					} else {
						y = bHeight;
					}
				}
				y += h;
				int i = index(r, c);
				Node p = new Node(i+1, x, y );
				//setAt(r, c, p);
				g.append(p);
			}
			h += height;
		}
		linkNeighbors();
	}
	
	@Override
	protected void linkNeighbors() {
		Node a, b;
		boolean evenRow, evenColumn;
		for(int r = 0; r < ro; r++){
			if(0 == r%2){
				evenRow = true;
			} else {
				evenRow = false;
			}
			for(int c = 0; c < col; c++){
				a = getAt(r, c);
				if(0 == c%2){
					evenColumn = true;
				} else {
					evenColumn = false;
				}
				if(c>0){
					b = getAt(r, c-1);
					a.addNeigh(b);
				}
				if(c < col-1){
					b = getAt(r, c+1);
					a.addNeigh(b);
				}
				if(evenRow){
					if(evenColumn){
						if(r>0){
							b = getAt(r-1, c);
							a.addNeigh(b);
						}
					} else {
						if(r < ro - 1){
							b = getAt(r+1, c);
							a.addNeigh(b);
						}
					}
				} else {
					//odd row
					if(evenColumn){
						if(r > 0){
							b = getAt(r-1, c);
							a.addNeigh(b);
						}
					} else{
						if(r < ro - 1){
							b = getAt(r+1, c);
							a.addNeigh(b);
						}
					}
				}
			}
		}
		
	}

	public void setSide(double side){
		super.setSide(side);
		
		setHeight(Math.sqrt(3) * side / 2);
		halfSide = side/2;
		bHeight = height/3;
	}

	public double getHalfSide() {
		return halfSide;
	}

	public double getBheight() {
		return bHeight;
	}

	public String getPrefix() {
		return (super.getPrefix()+"tf");
	}
}
