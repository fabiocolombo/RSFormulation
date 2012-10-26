package test;

import static org.junit.Assert.*;

import java.util.HashSet;

import generator.Graph;
import generator.Node;

import org.junit.Before;
import org.junit.Test;

public class GraphTest {

	Graph g;
	
	@Before
	public void setUp(){
		g = new Graph();
	}
		
	@Test
	public void testGrid() {
		assertEquals(0, g.getCapacity());
		assertEquals(0, g.getNumNodes());
	}

	@Test
	public void testSetNumPoints(){
		g.setCapacity(5);
		assertEquals(5, g.getCapacity());
	}
	
	@Test
	public void testGetAt(){
		g.setCapacity(1);
		assertEquals(null, g.getAt(0));
	}
	
	@Test
	public void testSetPoint(){
		g.setCapacity(1);
		Node p = new Node(1,2,3);
		g.setAt(0,p);
		assertEquals(p, g.getAt(0));
	}
	
	@Test
	public void testAppend1(){
		g.setCapacity(1);
		Node p = new Node(1, 2, 3);
		g.append(p);
		assertEquals(g.getAt(0), p);
	}
	
	@Test
	public void testAppend2(){
		g.setCapacity(2);
		Node p = new Node(1, 2, 3);
		g.append(p);
		p = new Node(2,3,4);
		g.append(p);
		Node q = new Node(1,2,3);
		assertEquals(g.getAt(0), q);
		assertEquals(p, g.getAt(1));
		q = new Node(2,3,4);
		assertEquals(q, g.getAt(1));
	}
	
	@Test
	public void testEdges(){
		//create and append nodes
		g.setCapacity(3);
		Node a1 = new Node(1,2,3);
		g.append(a1);
		Node a3 = new Node(3,3,4);
		g.append(a3);
		Node a2 = new Node(2,3,6);
		g.append(a2);
		
		//link nodes
		//(2,3)
		Node a = g.getAt(2); //2
		Node b = g.getAt(1); //3
		a.addNeigh(b);
		//(1,2)
		a = g.getAt(0); //1
		a.addNeigh(b);
		
		HashSet<String> edges = g.getEdges();
		assertEquals(2, edges.size());
		String e = new String("< 2 3 >");
		assertTrue(edges.contains(e));
		e = new String("< 1 3 >");
		assertTrue(edges.contains(e));
	}
	
}
