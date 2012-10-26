package test;

import static org.junit.Assert.*;
import generator.GridGenerator;
import generator.TriangularGridGenerator;

import org.junit.Before;
import org.junit.Test;

public class GridGeneratorTest {
	
	GridGenerator g;
	int rows = 2;
	int cols = 3;

	@Before
	public void setUp(){
		g = new TriangularGridGenerator(rows,cols);
	}
	@Test
	public void testGenerator() {
		int t = g.rows();
		assertEquals(rows, t);
		t = g.columns();
		assertEquals(cols, g.columns());
	}
	
	@Test
	public void testGetAt() {
		for (int r = 0; r < g.rows(); r++) {
			for(int c = 0; c < g.columns(); c++){
				assertEquals(null, g.getAt(r, c));
			}
		}
	}
	
	@Test
	public void testIndex00(){
		assertEquals(0, g.index(0,0));
	}
	
	@Test
	public void testIndex01(){
		assertEquals(1, g.index(0,1));
	}
	
	@Test
	public void testIndex02(){
		assertEquals(2, g.index(0,2));
	}
	
	@Test
	public void testIndex10(){
		assertEquals(3, g.index(1,0));
	}
	
	@Test
	public void testIndex11(){
		assertEquals(4, g.index(1,1));
	}
	
	@Test
	public void testIndex12(){
		assertEquals(5, g.index(1,2));
	}
	
	@Test
	public void testSetTargetSize(){
		g.setTargetSize(9);
		assertEquals(9, g.getTargetSize());
	}
	
	@Test
	public void testSetFileName(){
		String p = new String("output.txt"); 
		g.setFileName(p);
		assertEquals(p, g.getFileName());
	}
}
