package or.util;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import util.FloatUtils;

public class BiIndexedIntVarMap {
	
	private IloModeler modeler;
	private Map<BiIndex,IloIntVar> map=new HashMap<BiIndex,IloIntVar>();
	private int lb,ub;
	private String basename;

	public BiIndexedIntVarMap(IloModeler modeler,int lb,int ub,String basename) {
		this.modeler = modeler;
		this.lb=lb;
		this.ub=ub;
		this.basename=basename;
	}
	
	public IloIntVar get(int i,int j) throws IloException{
		BiIndex key=new BiIndex(i,j);
		IloIntVar var=map.get(key);
		if(var==null){
			var= modeler.intVar(lb, ub, basename+"("+i+","+j+")");
			map.put(key,var);;
		}
		return var;
	}

	public void printNonNegative(IloCplex solver) throws UnknownObjectException, IloException {
		System.out.println("Snapshot of the "+basename+" variables:");
		for(Entry<BiIndex,IloIntVar> e:map.entrySet()){
			double val=solver.getValue(e.getValue());
			if(FloatUtils.gr(val, 0.0))
				System.out.println(this.basename+"("+e.getKey().i1+","+e.getKey().i2+")="+val);
		}
		
	}

}
