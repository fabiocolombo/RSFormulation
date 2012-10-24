package rs;

import or.util.*;
import util.FloatUtils;
import ilog.concert.*;
import ilog.cplex.*;

public class ILPSolver {
	
	public static double solve(Instance ins,Solution sol) throws IloException{		
		IloCplex solver=new IloCplex();
		BiIndexedIntVarMap x=new BiIndexedIntVarMap(solver, 0, 1, "x"); 		//(k,v)   (cluster,node)
		IndexedNumVarMap dp=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "dp");		    //(k)     (cluster)
		IndexedNumVarMap dm=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "dm");		    //(k)     (cluster)
		TriIndexedNumVarMap f=new TriIndexedNumVarMap(solver, 0, Double.MAX_VALUE, "f"); 		//(k,i,j) (cluster,tail,head)
		BiIndexedIntVarMap r=new BiIndexedIntVarMap(solver, 0, 1, "r");			//(k,i)   (cluster,node
		BiIndexedNumVarMap y=new BiIndexedNumVarMap(solver, 0, 1, "y");			//(i,j)   (node1<node2)
		
		int superRoot=ins.getNumNodes();
		//compute the weights of the objective function
		double w1=ins.getW1() / ins.getNumNodes();
		double w2=ins.getW2() / (ins.getLambda() * ins.getMaxClusterNumber());
		double w3=2*ins.getW3() / (ins.getNumNodes() * (ins.getNumNodes() -1)*(ins.getDMax() - ins.getDMin()));
		
		//compute the objective function expression
		IloNumExpr expr=solver.numExpr();
		
		//O1
		IloNumExpr exprO1=solver.numExpr();
		for(int i=0;i<ins.getNumNodes();++i){
			expr=solver.numExpr();
			expr=solver.sum(expr,1);
			for(int k=0;k<ins.getMaxClusterNumber();++k)
				expr=solver.diff(expr, x.get(k, i));
			exprO1=solver.sum(exprO1,solver.prod(w1,expr));
		}
		//O2
		IloNumExpr exprO2=solver.numExpr();
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			exprO2=solver.sum(exprO2,solver.prod(w2,dm.get(k)));
			exprO2=solver.sum(exprO2,solver.prod(w2,dp.get(k)));
		}
		//O3
		IloNumExpr exprO3=solver.numExpr();
		for(OrderedPair p:ins.getP()){
			double dmax=ins.getDMax();
			double d=ins.getD(p.i,p.j);
			IloNumVar yvar=y.get(p.i, p.j);
			exprO3=solver.sum(exprO3,solver.prod(w3*(dmax-d),yvar));
		}
		expr=solver.sum(exprO1,solver.sum(exprO2,exprO3));
		solver.add(solver.objective(IloObjectiveSense.Minimize,expr));
		
		//packing constraints
		for(int i=0;i<ins.getNumNodes();++i){
			expr=solver.numExpr();
			for(int k=0;k<ins.getMaxClusterNumber();++k)
				expr=solver.sum(expr,x.get(k, i));
			solver.addLe(expr, 1);
		}
		
		//x,r linking constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i){
				expr=solver.sum(expr,x.get(k,i));
				expr=solver.diff(expr,solver.prod(ins.getMaxClusterSize(), r.get(k, i)));				
			}
			solver.addLe(expr, 0);
		}
		
		//root uniqueness constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i)
				expr=solver.sum(expr,r.get(k,i));
			solver.addLe(expr, 1);
		}
		
		//f,x linking constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int i=0;i<ins.getNumNodes();++i){
				expr=solver.numExpr();
				for(Integer j:ins.getOutcut(i)){
					expr=solver.sum(expr,f.get(k, i, j));					
				}
				expr=solver.diff(expr,solver.prod(ins.getMaxClusterSize(), x.get(k, i)));
				solver.addLe(expr,0);
			}
		}
		
		//f,r linking constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int i=0;i<ins.getNumNodes();++i){
				solver.addLe(f.get(k,superRoot,i), solver.prod(ins.getMaxClusterSize(),r.get(k, i)));
			}
		}
		
		//flow generation constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i){
				expr=solver.sum(expr,f.get(k,superRoot,i));
				expr=solver.diff(expr, x.get(k, i));
			}
			solver.addEq(expr, 0);
		}
		
		//flow conservation constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int i=0;i<ins.getNumNodes();++i){
				expr=solver.numExpr();
				System.out.println("Outcut of node "+i);
				System.out.println(ins.getOutcut(i));
				for(Integer j:ins.getOutcut(i)){
					expr=solver.diff(expr, f.get(k,i,j));
					expr=solver.sum(expr, f.get(k,j,i));
				}
				//i can receive flow also from the super root
				expr=solver.sum(expr,f.get(k,superRoot,i));
				solver.addEq(expr,x.get(k, i));			
			}
		}
		
		//dp - dm definition constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			IloNumExpr e1=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i)
				e1=solver.sum(e1,r.get(k, i));
			e1=solver.diff(1, e1);
			e1=solver.prod(ins.getMaxClusterSize(), e1);
			
			IloNumExpr e2=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i)
				e2=solver.sum(e2,x.get(k,i));
			//dp
			expr=solver.numExpr();
			expr=solver.sum(expr,dp.get(k));
			expr=solver.sum(expr, ins.getLambda());
			expr=solver.diff(expr, e2);
			expr=solver.sum(expr,e1);
			solver.addGe(expr, 0);
			//dm
			expr=solver.numExpr();
			expr=solver.sum(expr,dm.get(k));
			expr=solver.diff(expr,ins.getLambda());
			expr=solver.sum(expr,e2);
			expr=solver.sum(expr,e1);
			solver.addGe(expr,0);			
		}
		
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(OrderedPair p:ins.getP()){
				expr=solver.numExpr();
				expr=solver.sum(expr,y.get(p.i,p.j));
				expr=solver.diff(expr, x.get(k,p.i));
				expr=solver.diff(expr, x.get(k,p.j));
				expr=solver.sum(expr,1);
				solver.addGe(expr, 0);				
			}
		}
		
		solver.solve();
		
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int v=0;v<ins.getNumNodes();++v){
				if(FloatUtils.eq(solver.getValue(x.get(k,v)),1.0)){
					sol.insert(k, v);
				}
			}
		}
		
		return solver.getObjValue();
	}

}
