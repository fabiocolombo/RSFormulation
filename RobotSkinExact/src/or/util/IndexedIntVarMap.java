package or.util;

import ilog.concert.IloException;
import ilog.concert.IloModeler;
import ilog.concert.IloNumVar;

import java.util.HashMap;
import java.util.Map;

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

}
