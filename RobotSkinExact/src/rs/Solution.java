package rs;
import java.util.*;

import util.FloatUtils;

public class Solution {
	private ArrayList< List<Integer> > clusters;
	private Instance ins;
	
	private int getClusterIndex(int node){
		for(int i=0;i<clusters.size();++i){
//			System.out.println(clusters.get(i)+" contains "+node+"?");
			if(clusters.get(i).contains(node))
				return i;
		}
		return -1;
	}
	
	public Solution(Instance ins){
		this.ins=ins;
		clusters=new ArrayList< List<Integer> >(ins.getNumNodes());
		for(int i=0;i<ins.getNumNodes();++i){
			clusters.add(new LinkedList<Integer>());
		}
	}
	
	public void insert(int cluster, int ni){
		clusters.get(cluster).add(ni);
	}
	
	boolean isFeasible(Instance ins, double objValue){
		//compute the weights of the objective function
		double w1=ins.getW1() / ins.getNumNodes();
		double w2=ins.getW2() / (ins.getLambda() * ins.getMaxClusterNumber());
		double w3=2*ins.getW3() / (ins.getNumNodes() * (ins.getNumNodes() -1)*(ins.getDMax() - ins.getDMin()));
		double objO1=0.0;
		double objO2=0.0;
		double objO3=0.0;
		double realObj=0.0;
		//check packing constraints
		boolean covered[]=new boolean[ins.getNumNodes()];
		int notCoveredNum=ins.getNumNodes();
		for(List<Integer> cluster: clusters){
			for(int i:cluster){
				if(covered[i]){
					System.err.println("Node "+i+" contained in two or more clusters");
					return false;
				}
				covered[i]=true;
				--notCoveredNum;
			}
		}
		objO1+=w1*notCoveredNum;
		//check connectivity
		for(List<Integer> cluster: clusters){
			if(cluster.isEmpty()) continue;
			Set<Integer> remaining=new HashSet<Integer>();
			for(int v:cluster)
				remaining.add(v);
			int root=cluster.get(0);
			LinkedList<Integer> queue=new LinkedList<Integer>();
			queue.add(root);
			remaining.remove(root);
			while(!queue.isEmpty()){
				int v=queue.pollFirst();
				for(int i:ins.getOutcut(v)){
					if(remaining.contains(i)){
						remaining.remove(i);
						queue.add(i);
					}
				}
			}
			if(!remaining.isEmpty()){
				System.err.println("The node "+root+" is not connected to the nodes:");
				System.err.println(remaining);
				System.err.println("The cluster "+cluster+" is not connected");
				return false;
			}
			
			//compute the distance objective function
			for(int i:cluster){
				for(int j:cluster){
					if(i>=j) continue;
					objO3+=w3*(ins.getDMax() - ins.getD(i, j));
				}
			}
			
			//compute the size gap objective function
			objO2+=w2*Math.abs(cluster.size() - ins.getLambda());				
		}
		realObj=objO1+objO2+objO3;
		if(!FloatUtils.eq(objValue, realObj)){
			System.err.println("Error in the objective function values");
			System.err.println("GIVEN OBJ="+objValue);
			System.err.println("REAL OBJ="+realObj);
			return false;
		}
		return true;		
	}
	
	public String toString(){
		StringBuilder sb=new StringBuilder();
		for(List<Integer> cluster:clusters){
			if(cluster.isEmpty()) continue;
			sb.append(cluster.toString()+"\n");
		}
		return sb.toString();
	}
	
	public String toGraphVizDot(){
		StringBuilder sb=new StringBuilder();
		sb.append("graph "+ins.getNumNodes()+"{\n");
		//add nodes
		for(int i=0;i<ins.getNumNodes();++i){
			int index=getClusterIndex(i)+1;
			if(index!=-1)
				sb.append(i+" [style=filled, colorscheme=paired12, color="+index+"];\n");
			else
				sb.append(i+";\n");
		}
		//add edges
		for(int i=0;i<ins.getNumNodes();++i){
			for(int j:ins.getOutcut(i)){
				if(j<i) continue;
				sb.append(i+"--"+j+";\n");
			}
		}
		sb.append("}");
		return sb.toString();
	}
	

}
