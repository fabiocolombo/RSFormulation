package rs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import or.util.BiIndexedIntVarMap;
import or.util.BiIndexedNumVarMap;
import or.util.IndexedIntVarMap;
import or.util.IndexedNumVarMap;
import or.util.TriIndexedNumVarMap;
import rs.ILPSolver.Extension;
import util.FloatUtils;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjectiveSense;
import ilog.cplex.IloCplex;

public class NodeMCILPSolver extends ILPSolver {

	@Override
	public double solve(Instance ins, Solution sol, int timeout)
			throws IloException {
		IloCplex solver=new IloCplex();
		
		IndexedIntVarMap x=new IndexedIntVarMap(solver,0,1,"x");						//(v) node covered?
		BiIndexedIntVarMap u=new BiIndexedIntVarMap(solver,0,1,"u");					//(i,j) arcs
		IndexedNumVarMap dp=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "dp");	//(l)     (cluster)
		IndexedNumVarMap dm=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "dm");	//(l)     (cluster)
		TriIndexedNumVarMap f=new TriIndexedNumVarMap(solver, 0, 1, "f");				//(dest,i,j) (cluster,tail,head)		
		BiIndexedNumVarMap y=new BiIndexedNumVarMap(solver, 0, 1, "y");					//(i,j)   (node1<node2)
		
		int superRoot=ins.getNumNodes();
		System.out.println("SUPER ROOT="+superRoot);
		
		//compute the weights of the objective function
		double w1=ins.getW1() / ins.getNumNodes();
		double w2=ins.getW2() / (ins.getLambda() * ins.getMaxClusterNumber());
		double w3=2*ins.getW3() / (ins.getNumNodes() * (ins.getNumNodes() -1)*(ins.getDMax() - ins.getDMin()));
		
		Set<OrderedPair> fp=new TreeSet<OrderedPair>(ins.getP());		
		if(isActiveExtension(Extension.PFILTER)){
			fp=ins.filterP();
			System.out.println("PFILTER: "+(ins.getP().size() - fp.size())+" pairs filtered");
		}
		//build P_l structure
		ArrayList< List<Integer> > pl=new ArrayList< List<Integer> >();
		for(int l=0;l<ins.getNumNodes();++l){
			pl.add(new LinkedList<Integer>());
			pl.get(l).add(l);
			for(int i=l+1;i<ins.getNumNodes();++i){
				if(fp.contains(new OrderedPair(l,i)))
					pl.get(l).add(i);
			}
		}
		
		//compute the objective function expression
		IloNumExpr expr=solver.numExpr();
		
		//O1
		IloNumExpr exprO1=solver.numExpr();
		for(int i=0;i<ins.getNumNodes();++i){
			exprO1=solver.sum(exprO1,solver.prod(w1,solver.diff(1, x.get(i))));
		}
		//O2
		IloNumExpr exprO2=solver.numExpr();
		for(int l=0;l<ins.getNumNodes();++l){
			exprO2=solver.sum(exprO2,solver.prod(w2,dm.get(l)));
			exprO2=solver.sum(exprO2,solver.prod(w2,dp.get(l)));
		}
		//O3
		IloNumExpr exprO3=solver.numExpr();
		for(OrderedPair p:fp){
			double dmax=ins.getDMax();
			double d=ins.getD(p.i,p.j);
			IloNumVar yvar=y.get(p.i, p.j);
			exprO3=solver.sum(exprO3,solver.prod(w3*(dmax-d),yvar));
		}
		expr=solver.sum(exprO1,solver.sum(exprO2,exprO3));
		solver.add(solver.objective(IloObjectiveSense.Minimize,expr));
			
		for(int i=0;i<ins.getNumNodes();++i){
			expr=solver.numExpr();
			expr=solver.sum(expr,u.get(superRoot,i));
			for(int j:ins.getOutcut(i))
				expr=solver.sum(expr,u.get(j,i));
		}
		
		//number of cluster constraint
		expr=solver.numExpr();
		for(int l=0;l<ins.getNumNodes();++l){
			expr=solver.sum(expr,u.get(superRoot,l));
		}
		solver.addLe(expr,ins.getMaxClusterNumber());
		
		
		//flow conservation constraints
		for(int v=0;v<ins.getNumNodes();++v){
			for(int w=0;w<ins.getNumNodes();++w){
				expr=solver.numExpr();
				for(int i:ins.getOutcut(w)){
					expr=solver.diff(expr,f.get(v,w,i));
					expr=solver.sum(expr,f.get(v, i, w));
				}
				if(pl.get(w).contains(v)){
					expr=solver.sum(expr,f.get(v,superRoot,w));
				}
				if(v==w) solver.addEq(expr,1);
				else solver.addEq(expr, 0);
			}
		}
		
		for(int v=0;v<ins.getNumNodes();++v){			
			for(int w=0;w<ins.getNumNodes();++w){
				solver.addLe(f.get(v,superRoot,w),u.get(superRoot,w));
				for(int i:ins.getOutcut(w)){
					solver.addLe(f.get(v,w,i),u.get(w, i));
				}
			}					
		}
		
		expr=solver.numExpr();
		for(int w=0;w<ins.getNumNodes();++w){
			expr=solver.diff(expr,x.get(w));
			expr=solver.sum(expr,u.get(superRoot,w));
			for(int i:ins.getOutcut(w))
				expr=solver.sum(expr,u.get(w, i));
		}
		solver.addEq(expr,0);
		
			
		
		//dp - dm definition constraints
		for(int l=0;l<ins.getNumNodes();++l){
			IloNumExpr e1=solver.numExpr();
			e1=solver.sum(e1,u.get(superRoot, l));
			e1=solver.diff(1, e1);
			e1=solver.prod(ins.getMaxClusterSize(), e1);
			
			IloNumExpr e2=solver.numExpr();
			for(int i:pl.get(l))
				e2=solver.sum(e2,f.get(i,superRoot,l));
			//dp
			expr=solver.numExpr();
			expr=solver.sum(expr,dp.get(l));
			expr=solver.sum(expr, ins.getLambda());
			expr=solver.diff(expr, e2);
			expr=solver.sum(expr,e1);
			solver.addGe(expr, 0);
			//dm
			expr=solver.numExpr();
			expr=solver.sum(expr,dm.get(l));
			expr=solver.diff(expr,ins.getLambda());
			expr=solver.sum(expr,e2);
			expr=solver.sum(expr,e1);
			solver.addGe(expr,0);			
		}

		//y var definition constraints
		for(int l=0;l<ins.getNumNodes();++l){
			for(OrderedPair p:fp){
				expr=solver.numExpr();
				expr=solver.sum(expr,y.get(p.i,p.j));
				expr=solver.diff(expr, f.get(p.i,superRoot,l));
				expr=solver.diff(expr, f.get(p.j,superRoot,l));
				expr=solver.sum(expr,1);
				solver.addGe(expr, 0);				
			}
		}
		
		solver.setParam(IloCplex.DoubleParam.TiLim, timeout);
		solver.solve();
		f.printNonNegative(solver);
		u.printNonNegative(solver);
		x.printNonNegative(solver);
		System.out.println("STATE="+solver.getStatus());
		System.out.println("NN="+solver.getNnodes());
		System.out.println("LB="+solver.getBestObjValue());
		
		//If we have a feasible solution we save it
		if(solver.getStatus() == IloCplex.Status.Feasible || solver.getStatus() == IloCplex.Status.Optimal){		
			int k=0;
			for(int l=0;l<ins.getNumNodes();++l){
				if(FloatUtils.eq(solver.getValue(f.get(l,superRoot,l)), 0.0)) continue;			
				for(int v=l;v<ins.getNumNodes();++v){
					if(FloatUtils.eq(solver.getValue(f.get(v,superRoot,l)),1.0)){
						sol.insert(k, v);
					}
				}				
				++k;
			}
		}
		
		return solver.getObjValue();
	}

}
