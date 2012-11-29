package rs;

import java.util.Set;
import java.util.TreeSet;

import or.util.BiIndexedIntVarMap;
import or.util.BiIndexedNumVarMap;
import or.util.IndexedNumVarMap;
import or.util.TriIndexedIntVarMap;
import or.util.TriIndexedNumVarMap;
import rs.ILPSolver.Extension;
import util.FloatUtils;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjectiveSense;
import ilog.cplex.IloCplex;

public class TimeILPSolver extends ILPSolver {

	@Override
	public double solve(Instance ins, Solution sol, int timeout)
			throws IloException {
		IloCplex solver=new IloCplex();
		TriIndexedIntVarMap x=new TriIndexedIntVarMap(solver, 0, 1, "x"); 						//(k,v)   (cluster,tail,head)		
		BiIndexedNumVarMap y=new BiIndexedNumVarMap(solver, 0, Double.MAX_VALUE, "y");			//(k,i)   (cluster,node)
		IndexedNumVarMap d=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "d");		    	//(k)     (cluster)			
		BiIndexedNumVarMap w=new BiIndexedNumVarMap(solver, 0, Double.MAX_VALUE, "w");			//(i,j)   (node1<node2)
		IndexedNumVarMap u=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "u");				//(i)	  (node)
		int superRoot=ins.getNumNodes();
				
		
		//compute the weights of the objective function
		double w1=ins.getW1() / ins.getNumNodes();
		double w2=ins.getW2() / (ins.getLambda() * ins.getMaxClusterNumber());
		double w3=2*ins.getW3() / (ins.getNumNodes() * (ins.getNumNodes() -1)*(ins.getDMax() - ins.getDMin()));
		
		//compute the P set
		Set<OrderedPair> fp=new TreeSet<OrderedPair>(ins.getP());		
		if(isActiveExtension(Extension.PFILTER)){
			fp=ins.filterP();
			System.out.println("PFILTER: "+(ins.getP().size() - fp.size())+" pairs filtered");
		}
		
		//compute the objective function expression
		IloNumExpr expr=solver.numExpr();
		
		//O1
		System.out.println("w1="+w1);
		IloNumExpr exprO1=solver.numExpr();
		for(int i=0;i<ins.getNumNodes();++i){
			expr=solver.sum(solver.numExpr(),1);			
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				expr=solver.diff(expr,y.get(k,i));				
			}
			exprO1=solver.sum(exprO1,solver.prod(w1,expr));
		}
		System.out.println(exprO1);
		//O2
		IloNumExpr exprO2=solver.numExpr();
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			exprO2=solver.sum(exprO2,solver.prod(w2,d.get(k)));
		}
		//O3
		IloNumExpr exprO3=solver.numExpr();
		for(OrderedPair p:fp){
			double dmax=ins.getDMax();
			double dist=ins.getD(p.i,p.j);
			IloNumVar wvar=w.get(p.i, p.j);
			exprO3=solver.sum(exprO3,solver.prod(w3*(dmax-dist),wvar));
		}
		expr=solver.sum(exprO1,exprO2,exprO3);					
		solver.add(solver.objective(IloObjectiveSense.Minimize,expr));
		
		//node definition constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int v=0;v<ins.getNumNodes();++v){
				expr=solver.numExpr();
				expr=solver.sum(expr,x.get(k,superRoot,v));
				for(int i:ins.getOutcut(v)){
					expr=solver.sum(expr,x.get(k,i,v));
				}
				solver.addEq(expr,y.get(k,v));
			}
		}
		
		//packing constraints
		for(int i=0;i<ins.getNumNodes();++i){
			expr=solver.numExpr();
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				expr=solver.sum(expr,y.get(k, i));
			}
			solver.addLe(expr,1);
		}
		
		//arc precedence constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int i=0;i<ins.getNumNodes();++i){
				for(int j:ins.getOutcut(i)){
					//precedence for arc (i,j)					
					solver.addLe(x.get(k, i, j), y.get(k,i));
					//precedence for arc (j,i)					
					solver.addLe(x.get(k, j, i), y.get(k,j));
				}
			}		
		}
		
		//root uniqueness constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i){
				expr=solver.sum(expr,x.get(k,superRoot,i));
			}			
			solver.addLe(expr,1);
		}
		
		//cardinality constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i){				
				expr=solver.sum(expr,y.get(k,i));
			}
			solver.addLe(expr, ins.getMaxClusterSize());
		}
		
		//timestamps definition constraints
		for(int i=0;i<ins.getNumNodes();++i){
			for(int j:ins.getOutcut(i)){
				//timestamp definition for arc (i,j)
				IloNumExpr arcUsageExpr=solver.numExpr();
				for(int k=0;k<ins.getMaxClusterNumber();++k){
					arcUsageExpr=solver.sum(arcUsageExpr,x.get(k, i, j));
				}
				IloNumExpr lhs=solver.sum(
								solver.diff(u.get(i),u.get(j)),
								solver.prod(ins.getMaxClusterSize(),arcUsageExpr)
								);
				solver.addLe(lhs,ins.getMaxClusterSize()-1);
				//timestamp definition for arc (j,i)
				arcUsageExpr=solver.numExpr();
				for(int k=0;k<ins.getMaxClusterNumber();++k){
					arcUsageExpr=solver.sum(arcUsageExpr,x.get(k, j, i));
				}
				lhs=solver.sum(
						solver.diff(u.get(j),u.get(i)),
						solver.prod(ins.getMaxClusterSize(),arcUsageExpr)
						);
				solver.addLe(lhs,ins.getMaxClusterSize()-1);
				
			}
		}
//
		//delta definition constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i){				
				expr=solver.sum(expr,y.get(k,i));				
			}
			solver.addLe(solver.diff(expr,ins.getLambda()),d.get(k));
			solver.addGe(solver.diff(expr,ins.getLambda()),solver.prod(-1,d.get(k)));
		}

		//w definition constraints
		for(int k=0;k<ins.getMaxClusterSize();++k){
			for(OrderedPair p:fp){
				expr=solver.numExpr();
				expr=solver.sum(expr,y.get(k,p.i),y.get(k, p.j));
				expr=solver.diff(expr, 1);
				solver.addGe(w.get(p.i,p.j),expr);
			}
		}
		
		if(isActiveExtension(Extension.ROOT_MINIMIZATION_CONSTRAINTS)){
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				for(int i=0;i<ins.getNumNodes();++i){
					for(int j=i+1;j<ins.getNumNodes();++j){
						solver.addLe(solver.sum(y.get(k, i),x.get(k, superRoot, j)),1);
					}
				}
			}			
		}
		
		if(isActiveExtension(Extension.POLYNOMIAL_ORBITOPE)){
			//first family
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				expr=solver.numExpr();
				for(int i=0;i<=k-1;++i){					
					expr=solver.sum(expr,y.get(k,i));
				}
				solver.addEq(expr,0);
			}
			//second family
			for(int k=1;k<ins.getMaxClusterNumber();++k){
				for(int i=k;i<ins.getNumNodes();++i){
					IloNumExpr lhs=solver.numExpr();
					for(int k1=k;k<ins.getMaxClusterNumber();++k){
						lhs=solver.sum(lhs,y.get(k,i));
					}
					IloNumExpr rhs=solver.numExpr();
					for(int g=k-1;g<i;++g){
						rhs=solver.sum(rhs,y.get(k-1,g));
					}
					solver.addLe(lhs, rhs);
				}
			}			
		}
		
		if(isActiveExtension(Extension.EXPONENTIAL_ORBITOPE)){
			System.err.println("Extension "+Extension.EXPONENTIAL_ORBITOPE+" not implemented yet");
			throw new IllegalArgumentException();
		}
		
		solver.setParam(IloCplex.DoubleParam.TiLim, timeout);
		if(isActiveExtension(Extension.ROOT_BRANCHING_PRIORITY)){
			//Add branching priorities to the root variables
			for(int k=0;k<ins.getMaxClusterNumber();++k)			
				for(int i=0;i<ins.getNumNodes();++i)
					solver.setPriority(x.get(k,superRoot,i), 100);			
		}		
		solver.exportModel("model.lp");
		solver.solve();
		System.out.println("STATE="+solver.getStatus());
		System.out.println("NN="+solver.getNnodes());
		System.out.println("LB="+solver.getBestObjValue());
		
		//If we have a feasible solution we save it
		if(solver.getStatus() == IloCplex.Status.Feasible || solver.getStatus() == IloCplex.Status.Optimal){
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				for(int v=0;v<ins.getNumNodes();++v){
					if(FloatUtils.eq(solver.getValue(y.get(k,v)),1.0)){
						sol.insert(k, v);					
					}
				}
			}
		}
		
		x.printNonNegative(solver);
		d.printNonNegative(solver);
		w.printNonNegative(solver);
//		r.printNonNegative(solver);
//		u.printNonNegative(solver);
//		dp.printNonNegative(solver);
//		dm.printNonNegative(solver);
		

		return solver.getObjValue();
	}

}
