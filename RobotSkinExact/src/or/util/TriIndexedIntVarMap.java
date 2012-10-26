package or.util;

import ilog.concert.*;

import java.util.HashMap;
import java.util.Map;

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

}
