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

public class TriIndexedNumVarMap {
	private IloModeler modeler;
	private Map<TriIndex,IloNumVar> map=new HashMap<TriIndex,IloNumVar>();
	private double lb,ub;
	private String basename;

	public TriIndexedNumVarMap(IloModeler modeler,double lb,double ub,String basename) {
		this.modeler = modeler;
		this.lb=lb;
		this.ub=ub;
		this.basename=basename;
	}
	
	public IloNumVar get(int i,int j,int k) throws IloException{
		TriIndex key=new TriIndex(i,j,k);
		IloNumVar var=map.get(key);
		if(var==null){
			var=modeler.numVar(lb, ub,basename+"("+i+","+j+","+k+")");
			map.put(key, var);;
		}
		return var;
	}
	
	public void printNonNegative(IloCplex solver) throws UnknownObjectException, IloException {
		System.out.println("Snapshot of the "+basename+" variables:");
		for(Entry<TriIndex,IloNumVar> e:map.entrySet()){
			double val=solver.getValue(e.getValue());
			if(FloatUtils.gr(val, 0.0))
				System.out.println(this.basename+"("+e.getKey().i1+","+e.getKey().i2+","+e.getKey().i3+")="+val);
		}		
	}

}
