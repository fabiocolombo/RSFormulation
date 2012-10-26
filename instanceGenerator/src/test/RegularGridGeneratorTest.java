package test;

import static org.junit.Assert.*;
import generator.TriangularGridGenerator;

import org.junit.Test;

public class RegularGridGeneratorTest {
	
	TriangularGridGenerator g;

	@Test
	public void testSetSide() {
		g = new TriangularGridGenerator(3, 4);
		g.setSide(5);
		assertEquals(5, g.getSide(), 0.0);
	}

}
