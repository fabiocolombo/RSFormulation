package test;

import static org.junit.Assert.*;

import java.util.HashSet;

import generator.Node;

import org.junit.Before;
import org.junit.Test;

public class NodeTest {

	Node p;
	
	@Before
	public void setUp(){
		p = new Node(1,2,3);
	}
	
	@Test
	public void testNode() {
		assertEquals(1, p.getId());
		assertEquals(2, p.getX(),0);
		assertEquals(3, p.getY(),0);
	}

	@Test
	public void testEquals1(){
		Node q = new Node(2,3,4);
		boolean nequ = !q.equals(p);
		assertTrue(nequ);
	}
	
	@Test
	public void testEquals2(){
		Node q = new Node(1,2,3);
		boolean equ = q.equals(p);
		assertTrue(equ);
	}
	
	@Test
	public void testAddNeighbor1(){
		Node q = new Node(2,3,4);
		p.addNeigh(q);
		assertTrue(p.neighborsWith(q));
		assertTrue(q.neighborsWith(p));
	}
	
	@Test
	public void testAddNeighbor2(){
		Node q = new Node(2,3,4);
		assertFalse(p.neighborsWith(q));
		assertFalse(q.neighborsWith(p));
	}
	
	@Test
	public void testGetNeighbors(){
		Node q1 = new Node(0,0,0);
		p.addNeigh(q1);
		Node q2 = new Node(2,4,5);
		p.addNeigh(q2);
		
		HashSet<Node> nei = p.getNeighbors();
		assertEquals(2, nei.size());
		assertTrue(nei.contains(q1));
		assertTrue(nei.contains(q2));
	}
}
