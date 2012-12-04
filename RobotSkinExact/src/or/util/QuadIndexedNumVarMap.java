package or.util;

import ilog.concert.IloException;
import ilog.concert.IloModeler;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import util.FloatUtils;

public class QuadIndexedNumVarMap {
	private IloModeler modeler;
	private Map<QuadIndex,IloNumVar> map=new HashMap<QuadIndex,IloNumVar>();
	private double lb,ub;
	private String basename;

	public QuadIndexedNumVarMap(IloModeler modeler,double lb,double ub,String basename) {
		this.modeler = modeler;
		this.lb=lb;
		this.ub=ub;
		this.basename=basename;
	}
	
	public IloNumVar get(int i,int j,int k,int t) throws IloException{
		QuadIndex key=new QuadIndex(i,j,k,t);
		IloNumVar var=map.get(key);
		if(var==null){
			var=modeler.numVar(lb, ub,basename+"("+i+","+j+","+k+","+t+")");
			map.put(key, var);;
		}
		return var;
	}
	
	public void printNonNegative(IloCplex solver) throws UnknownObjectException, IloException {
		System.out.println("Snapshot of the "+basename+" variables:");
		for(Entry<QuadIndex,IloNumVar> e:map.entrySet()){
			double val=solver.getValue(e.getValue());
			if(FloatUtils.gr(val, 0.0))
				System.out.println(this.basename+"("+e.getKey().i1+","+e.getKey().i2+","+e.getKey().i3+"," + e.getKey().i3+")="+val);
		}		
	}

}
