package generator;

import java.io.FileNotFoundException;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int min = Integer.parseInt(args[0]);
		int step = Integer.parseInt(args[1]);
		int max = Integer.parseInt(args[2]);
		double sideLen = Double.parseDouble(args[3]);

		String suffix = new String(".dat");
		
		//TODO choose generator depending on command line
		//grids to implement
			//TODO regular hex-o-skin full
			//TODO regular hexagonal full
			//TODO regular triangular pierced
			//TODO regular hex-o-skin pierced
			//TODO regular hexagonal pierced
			//TODO random
		TriangularGridGenerator g = null;
		int gridType = Integer.parseInt(args[4]);
		switch(gridType){
		case 0:
		default:
			g = new TriangularGridGenerator(2,2);
			break;
		}
		g.setSide(sideLen);
		
		String path = args[5];
		
		
		for( int i = min; i <= max; i+=step){
			g.setTargetSize(i);
			g.generate();
			String fileName = new String(path+g.getPrefix() + i + suffix); 
			g.setFileName(fileName);
			try {
				g.writefile();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}
