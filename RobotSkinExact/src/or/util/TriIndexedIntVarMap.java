package or.util;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import util.FloatUtils;

public class TriIndexedIntVarMap {
	
	private IloModeler modeler;
	private Map<TriIndex,IloIntVar> map=new HashMap<TriIndex,IloIntVar>();
	private int lb,ub;
	private String basename;

	public TriIndexedIntVarMap(IloModeler modeler,int lb,int ub,String basename) {
		this.modeler = modeler;
		this.lb=lb;
		this.ub=ub;
		this.basename=basename;
	}
	
	public IloNumVar get(int i,int j,int k) throws IloException{
		TriIndex key=new TriIndex(i,j,k);
		IloIntVar var=map.get(key);
		if(var==null){
			var=modeler.intVar(lb, ub, basename+"("+i+","+j+","+k+")");
			map.put(key, var);;
		}
		return var;
	}

	public void printNonNegative(IloCplex solver) throws UnknownObjectException, IloException {
		System.out.println("Snapshot of the "+basename+" variables:");
		for(Entry<TriIndex,IloIntVar> e:map.entrySet()){
			double val=solver.getValue(e.getValue());
			if(FloatUtils.gr(val, 0.0))
				System.out.println(this.basename+"("+e.getKey().i1+","+e.getKey().i2+","+e.getKey().i3+")="+val);
		}
		
	}

}
