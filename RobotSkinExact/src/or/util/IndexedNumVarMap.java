package or.util;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.*;
import java.util.Map.Entry;

import util.FloatUtils;

public class IndexedNumVarMap {
	private IloModeler modeler;
	private Map<Integer,IloNumVar> map=new HashMap<Integer,IloNumVar>();
	private double lb,ub;
	private String basename;

	public IndexedNumVarMap(IloModeler modeler,double lb,double ub,String basename) {
		this.modeler = modeler;
		this.lb=lb;
		this.ub=ub;
		this.basename=basename;
	}
	
	public IloNumVar get(int i) throws IloException{
		IloNumVar var=map.get(i);
		if(var==null){
			var=modeler.numVar(lb, ub, basename+"("+i+")");
			map.put(i,var);;
		}
		return var;		
	}
	
	public void printNonNegative(IloCplex solver) throws UnknownObjectException, IloException{
		System.out.println("Snapshot of the "+basename+" variables:");
		for(Entry<Integer,IloNumVar> e:map.entrySet()){
			double val=solver.getValue(e.getValue());
			if(FloatUtils.gr(val, 0.0))
				System.out.println(this.basename+"("+e.getKey()+")="+val);
		}
	}
}
