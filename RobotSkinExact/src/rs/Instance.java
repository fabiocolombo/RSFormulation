package rs;
import ilog.concert.IloException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


public class Instance {
	private int numNodes;
	private Node[] nodes;
	private Map<Integer,Integer> revMap;
	private List<Edge> edges;
	private ArrayList< List<Integer> > outcut;
	private Set<OrderedPair> p;
	private int maxClusterSize;
	private int maxClusterNumber;
	private int lambda;
	private double dMax;
	private double dMin;
	private double d[][];
	private double w1,w2,w3;
	
	private static List<Integer> parseIntegerTuple(String data){
		StringTokenizer tkz=new StringTokenizer(data,"<> ");
		List<Integer> ris=new LinkedList<Integer>();
		while(tkz.hasMoreTokens())
			ris.add(new Integer(tkz.nextToken()));
		return ris;
	}
	
	private static List<Object> parseNodeTuple(String data){
		StringTokenizer tkz=new StringTokenizer(data,"<> ");
		List<Object> ris=new LinkedList<Object>();
		ris.add(new Integer(tkz.nextToken()));
		ris.add(new Double(tkz.nextToken()));
		ris.add(new Double(tkz.nextToken()));		
		return ris;
	}
	
	private static List< List<Object> > parseNodeTuplesSet(String data){
		List< List<Object> > tuples=new LinkedList< List<Object> >();
		while(data.length()!=0){
			int beg=data.indexOf('<');
			int end=data.indexOf('>');
			if(beg==-1) break;			
			tuples.add(parseNodeTuple(data.substring(beg, end+1)));
			data=data.substring(end+1);			
		}
		return tuples;
	}
	
	private static List< List<Integer> > parseIntegerTuplesSet(String data){
		List< List<Integer> > tuples=new LinkedList< List<Integer> >();
		while(data.length()!=0){
			int beg=data.indexOf('<');
			int end=data.indexOf('>');
			if(beg==-1) break;			
			tuples.add(parseIntegerTuple(data.substring(beg, end+1)));
			data=data.substring(end+1);			
		}
		return tuples;
	}
	
	private static String removeComments(InputStream is) throws IOException{
		BufferedReader br=new BufferedReader(new InputStreamReader(is));
		String buf;
		String ris="";
		while((buf=br.readLine())!=null){
			int index=buf.indexOf("/*");
			if(index == -1){
				index=buf.indexOf("//");
				if(index==-1) ris+=buf;
				else ris+=buf.substring(0,index);
			}
			else{
				ris+=buf.substring(0,index);
				int eindex=buf.indexOf("*/");
				while(eindex==-1){
					buf=br.readLine();
					eindex=buf.indexOf("*/");
				}
				ris+=buf.substring(eindex+2);
			}
		}
		return ris;
	}
	
	private static Map<String,String> extractCommands(String data){
		StringTokenizer tkz=new StringTokenizer(data,";");
		Map<String,String> ris=new HashMap<String,String>();
		while(tkz.hasMoreTokens()){
			StringTokenizer tkz2=new StringTokenizer(tkz.nextToken(),"=;");
			ris.put(tkz2.nextToken().trim(),tkz2.nextToken().trim());
		}
		return ris;
	}
	
	public static Instance parseInstance(File file) throws IOException{
		FileInputStream fis=new FileInputStream(file);
		Map<String,String> commands=extractCommands(removeComments(fis));
		Instance ins=new Instance();
		ins.maxClusterSize=Integer.parseInt(commands.get("C"));
		ins.maxClusterNumber=Integer.parseInt(commands.get("MC"));
		ins.lambda=Integer.parseInt(commands.get("lambda"));
		ins.w1=Double.parseDouble(commands.get("w1"));
		ins.w2=Double.parseDouble(commands.get("w2"));
		ins.w3=Double.parseDouble(commands.get("w3"));
		List< List<Object> > nodesData=parseNodeTuplesSet(commands.get("nodes"));
		List< List<Integer> > edgesData=parseIntegerTuplesSet(commands.get("E"));		
		ins.numNodes=nodesData.size();
		ins.nodes=new Node[ins.numNodes];
		ins.revMap=new HashMap<Integer,Integer>();
		ins.outcut=new ArrayList< List<Integer> >(ins.numNodes);
		int i=0;
		for(List<Object> tuple:nodesData){
			Iterator<Object> it=tuple.iterator();
			int id=(Integer) it.next();
			double x=(Double) it.next();
			double y=(Double) it.next();			
			ins.revMap.put(id,i);
			Node node=new Node(id,x,y);
			ins.outcut.add(new LinkedList<Integer>());
			ins.nodes[i++]=node;			
		}		
		ins.edges=new LinkedList<Edge>();
		for(List<Integer> tuple:edgesData){
			Iterator<Integer> it=tuple.iterator();
			int from=it.next();
			int to=it.next();
			int fromId=ins.revMap.get(from);
			int toId=ins.revMap.get(to);
			Edge edge=new Edge(fromId,toId);
			ins.edges.add(edge);
			ins.outcut.get(fromId).add(toId);
			ins.outcut.get(toId).add(fromId);
		}
		
		//preprocessing
		//Compute the euclidean distances
		ins.d=new double[ins.numNodes][];
		for(int ni=0;ni<ins.numNodes;++ni)
			ins.d[ni]=new double[ins.numNodes];
		ins.dMax=0;
		ins.dMin=Double.MAX_VALUE;
		for(int ni=0;ni<ins.numNodes;++ni){			
			for(int nj=ni+1;nj<ins.numNodes;++nj){
				ins.d[ni][nj]=Math.sqrt( Math.pow( ins.nodes[ni].getX() - ins.nodes[nj].getX(),2) +
									     Math.pow( ins.nodes[ni].getY() - ins.nodes[nj].getY(),2) );
				
				ins.d[nj][ni]=ins.d[ni][nj];
				ins.dMax=Math.max(ins.dMax, ins.d[ni][nj]);
				ins.dMin=Math.min(ins.dMin, ins.d[ni][nj]);
			}
		}
		//Compute the ordered pairs set
		ins.p=new TreeSet<OrderedPair>();
		for(int ni=0;ni<ins.numNodes;++ni){
			for(int nj=ni+1;nj<ins.numNodes;++nj){
				ins.p.add(new OrderedPair(ni,nj));
			}
		}
		return ins;
	}
	
	String getOPLInstance(){
		StringBuilder buf=new StringBuilder();
		buf.append("N="+this.nodes.length+";\n");
		buf.append("C="+this.maxClusterSize+";\n");
		buf.append("MC="+this.maxClusterNumber+";\n");
		buf.append("lambda="+this.lambda+";\n");
		buf.append("nodes={\n");
		for(Node no:this.nodes){
			buf.append("< "+no.getId()+" "+no.getX()+" "+no.getY()+">\n");
		}
		buf.append("};\n");
		buf.append("E={\n");
		for(Edge e:this.edges){
			buf.append("< "+this.nodes[e.getFrom()].getId()+" "+this.nodes[e.getTo()].getId()+" >\n");
		}
		buf.append("};\n");
		return buf.toString();
		
	}

	public double getDMax() {
		return dMax;
	}

	public double getDMin() {
		return dMin;
	}
	
	public double getD(Node i, Node j){
		return d[revMap.get(i.getId())][revMap.get(i.getId())];
	}
	
	public double getD(int i, int j){
		return d[i][j];
	}

	public int getNumNodes() {
		return numNodes;
	}
	
	public double getW1(){
		return w1;
	}
	
	public double getW2(){
		return w2;
	}
	
	public double getW3(){
		return w3;
	}

	public int getMaxClusterNumber() {
		return maxClusterNumber;
	}

	public int getLambda() {
		return lambda;
	}
	
	public Set<OrderedPair> getP(){
		return p;
	}

	public int getMaxClusterSize() {
		return maxClusterSize;
	}
	
	public List<Integer> getOutcut(int i){
		return outcut.get(i);
	}

	public Set<OrderedPair> filterP() {
		Set<OrderedPair> fp=new TreeSet<OrderedPair>();
		//for each node do a bfs search
		for(int i=0;i<getNumNodes();++i){		
			boolean [] visited=new boolean[getNumNodes()]; //initialized at false by default
			int[] d=new int[getNumNodes()];
			Arrays.fill(d, Integer.MAX_VALUE);
			d[i]=1;
			visited[i]=true;
			LinkedList<Integer> queue=new LinkedList<Integer>();			
			queue.add(i);
			while(!queue.isEmpty()){
				int node=queue.pop();				
				if(d[node]<=getMaxClusterSize() && i<node)
					fp.add(new OrderedPair(i, node));
				if(d[node]>getMaxClusterSize()){
					break;
				}
				for(int j:getOutcut(node)){
					if(visited[j]) continue;				
					d[j]=d[i] + 1;
					visited[j]=true;
					queue.add(j);
				}
			}			
		}
		return fp;
		
	}
	
	
	
	
	

}
