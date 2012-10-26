package generator;

public class hexoSkinGridGenerator extends RegularGridGenerator {

	//TODO inherit from a hexagonal grid generator
	
	hexoSkinGridGenerator(int nRows, int nColumns) {
		super(nRows, nColumns);
	}

	@Override
	public void generate() {
		// TODO Auto-generated method stub

	}

	@Override
	protected
	void linkNeighbors() {
		// TODO Auto-generated method stub
		//only this is different from the hexagonal skin
	}

}
