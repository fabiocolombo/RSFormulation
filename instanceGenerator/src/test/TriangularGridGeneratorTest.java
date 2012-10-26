package test;

import static org.junit.Assert.*;
import generator.Node;
import generator.TriangularGridGenerator;

import org.junit.Before;
import org.junit.Test;

public class TriangularGridGeneratorTest {

	TriangularGridGenerator g;
	int r;
	int c;
	double r3 = Math.sqrt(3);
	double side = 1;
	double tolerance = 0.0000000000001;
	
	void testEqualNodes(Node a, Node b){
		int ida, idb;
		ida = a.getId();
		idb = b.getId();
		assertEquals(ida, idb);
		
		double xa, xb, ya, yb;
		xa = a.getX();
		xb = b.getX();
		assertEquals(xa, xb, tolerance);
		ya = a.getY();
		yb = b.getY();
		assertEquals(ya, yb, tolerance);
		
		assertEquals(a, b);
	}
	
	@Before
	public void setUp() throws Exception {
		r = 2;
		c = 3;
		
		g = new TriangularGridGenerator(r,c);
		g.setSide(side);
	}

	@Test
	public void testGetHeight() {
		g.setSide(2);
		assertEquals( r3, g.getHeight(), 0.0000000001);
	}
	
	@Test
	public void testGetHalfSide(){
		g.setSide(2);
		assertEquals(1, g.getHalfSide(), 0);
	}
	
	@Test
	public void testGetBHeight(){
		g.setSide(2);
		assertEquals(r3/3, g.getBheight(),0);
	}
	
	@Test
	public void testGenerate() {
		g.generate();
		double halfSide = g.getHalfSide();
		double bHeight = g.getBheight();
		double height = g.getHeight();
		Node p = new Node(1,halfSide, bHeight);
		testEqualNodes(p, g.getAt(0, 0));
		p = new Node(2,side, height - bHeight);
		testEqualNodes(p, g.getAt(0, 1));
		p = new Node(3,side+halfSide, bHeight);
		testEqualNodes(p, g.getAt(0, 2));
		p = new Node(4,halfSide, height*2-bHeight);
		testEqualNodes(p, g.getAt(1, 0));
		p = new Node(5,side, height+bHeight);
		testEqualNodes(p, g.getAt(1, 1));
		p = new Node(6,side+halfSide, height*2-bHeight);
		testEqualNodes(p, g.getAt(1, 2));
	}
	
	@Test
	public void testGetNeighbors(){
		g.generate();
		Node a, b;
		a = g.getAt(0, 0);
		b = g.getAt(0, 1);
		assertTrue(a.neighborsWith(b));
		assertTrue(b.neighborsWith(a));
	}
	
	@Test
	public void testSetTargetsize1(){
		g.setTargetSize(9);
		assertEquals(3, g.rows());
		assertEquals(3, g.columns());
	}
	
	@Test
	public void testSetTargetsize2(){
		g.setTargetSize(10);
		assertEquals(4, g.rows());
		assertEquals(4, g.columns());
	}
	
	@Test
	public void testSetTargetsize3(){
		g.setTargetSize(4);
		assertEquals(2, g.rows());
		assertEquals(2, g.columns());
	}
	
	@Test
	public void testSetTargetsize4(){
		g.setTargetSize(8);
		assertEquals(3, g.rows());
		assertEquals(3, g.columns());
	}
	
	@Test
	public void testGetPrefix(){
		//r regular
		//t triangular
		//f full
		assertEquals("rtf", g.getPrefix());
	}
}
