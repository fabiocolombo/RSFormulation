package or.util;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloModeler;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import util.FloatUtils;

public class IndexedIntVarMap {
	private IloModeler modeler;
	private Map<Integer,IloNumVar> map=new HashMap<Integer,IloNumVar>();
	private int lb,ub;
	private String basename;

	public IndexedIntVarMap(IloModeler modeler,int lb,int ub,String basename) {
		this.modeler = modeler;
		this.lb=lb;
		this.ub=ub;
		this.basename=basename;
	}
	
	public IloNumVar get(int i) throws IloException{
		IloNumVar var=map.get(i);
		if(var==null){
			var=modeler.intVar(lb, ub, basename+"("+i+")");
			map.put(i, var);
		}
		return var;
	}

	public void printNonNegative(IloCplex solver) throws UnknownObjectException, IloException {
		System.out.println("Snapshot of the "+basename+" variables:");
		for(Entry<Integer,IloNumVar> e:map.entrySet()){
			double val=solver.getValue(e.getValue());
			if(FloatUtils.gr(val, 0.0))
				System.out.println(this.basename+"("+e.getKey()+")="+val);
		}		
	}

}
