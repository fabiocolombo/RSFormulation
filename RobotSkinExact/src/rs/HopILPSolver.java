package rs;

import java.util.Set;
import java.util.TreeSet;

import or.util.BiIndexedIntVarMap;
import or.util.BiIndexedNumVarMap;
import or.util.IndexedNumVarMap;
import or.util.QuadIndexedNumVarMap;
import or.util.TriIndexedIntVarMap;
import or.util.TriIndexedNumVarMap;
import rs.ILPSolver.Extension;
import util.FloatUtils;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjectiveSense;
import ilog.cplex.IloCplex;

public class HopILPSolver extends ILPSolver {

	@Override
	public double solve(Instance ins, Solution sol, int timeout)
			throws IloException {
		IloCplex solver=new IloCplex();
		TriIndexedIntVarMap y=new TriIndexedIntVarMap(solver, 0, 1, "y"); 					//(k,t,v)   (cluster,time,node)
		IndexedNumVarMap d=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "d");		    //(k)     (cluster)		
		QuadIndexedNumVarMap f=new QuadIndexedNumVarMap(solver, 0, Double.MAX_VALUE, "f"); 	//(k,t,i,j) (cluster,time,tail,head)		
		BiIndexedNumVarMap w=new BiIndexedNumVarMap(solver, 0, 1, "w");			//(i,j)   (node1<node2)
		
		int superRoot=ins.getNumNodes();
		//compute the weights of the objective function
		double w1=ins.getW1() / ins.getNumNodes();
		double w2=ins.getW2() / (ins.getLambda() * ins.getMaxClusterNumber());
		double w3=2*ins.getW3() / (ins.getNumNodes() * (ins.getNumNodes() -1)*(ins.getDMax() - ins.getDMin()));
		
		Set<OrderedPair> fp=new TreeSet<OrderedPair>(ins.getP());		
		if(isActiveExtension(Extension.PFILTER)){
			fp=ins.filterP();
			System.out.println("PFILTER: "+(ins.getP().size() - fp.size())+" pairs filtered");
		}
		
		//compute the objective function expression
		IloNumExpr expr=solver.numExpr();
		
		//O1
		IloNumExpr exprO1=solver.numExpr();
		for(int i=0;i<ins.getNumNodes();++i){
			expr=solver.numExpr();
			expr=solver.sum(expr,1);
			for(int t=1;t<=ins.getMaxClusterSize();++t){
				for(int k=0;k<ins.getMaxClusterNumber();++k){
					expr=solver.diff(expr, y.get(k,t,i));
				}
			}
			exprO1=solver.sum(exprO1,solver.prod(w1,expr));
		}
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
		expr=solver.sum(exprO1,solver.sum(exprO2,exprO3));
		solver.add(solver.objective(IloObjectiveSense.Minimize,expr));
		
		//packing constraints
		for(int i=0;i<ins.getNumNodes();++i){
			expr=solver.numExpr();
			for(int t=1;t<=ins.getMaxClusterSize();++t){
				for(int k=0;k<ins.getMaxClusterNumber();++k){
					expr=solver.sum(expr,y.get(k,t, i));
				}
			}
			solver.addLe(expr, 1);
		}
		
		//root uniqueness constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i)
				expr=solver.sum(expr,y.get(k,1,i));
			solver.addLe(expr, 1);
		}
		
		//f,y-root linking constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int i=0;i<ins.getNumNodes();++i){
				solver.addLe(f.get(k,1,superRoot,i),solver.prod(ins.getMaxClusterSize(),y.get(k,1,i)));				
			}
			
		}
		
		//f,y linking constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int t=2;t<=ins.getMaxClusterSize();++t){
				for(int i=0;i<ins.getNumNodes();++i){
					expr=solver.numExpr();
					for(Integer j:ins.getOutcut(i)){
						expr=solver.sum(expr,f.get(k, t+1, i, j));					
					}
					expr=solver.diff(expr,solver.prod((ins.getMaxClusterSize()-t), y.get(k, t, i)));
					solver.addLe(expr,0);
				}
			}
		}
		
		//flow conservation constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int t=1;t<=ins.getMaxClusterSize();++t){
				for(int i=0;i<ins.getNumNodes();++i){
					expr=solver.numExpr();
					for(Integer j:ins.getOutcut(i)){
						expr=solver.diff(expr, f.get(k,t+1,i,j));
						if(t!=1)
							expr=solver.sum(expr, f.get(k,t,j,i));
					}
					if(t==1)
						expr=solver.sum(expr,f.get(k, t, superRoot, i));
					solver.addEq(expr,y.get(k,t, i));			
				}
			}
		}
		
		//delta definition constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i){	
				for(int t=1;t<=ins.getMaxClusterSize();++t){
					expr=solver.sum(expr,y.get(k,t,i));			
				}
			}
			solver.addLe(solver.diff(expr,ins.getLambda()),d.get(k));
			solver.addGe(solver.diff(expr,ins.getLambda()),solver.prod(-1,d.get(k)));
		}
		
		//w var definition constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(OrderedPair p:fp){
				expr=solver.numExpr();
				expr=solver.sum(expr,w.get(p.i,p.j));
				for(int t=1;t<=ins.getMaxClusterSize();++t){
					expr=solver.diff(expr, y.get(k,t,p.i));
					expr=solver.diff(expr, y.get(k,t,p.j));
				}
				expr=solver.sum(expr,1);
				solver.addGe(expr, 0);				
			}
		}
		
		if(isActiveExtension(Extension.ROOT_MINIMIZATION_CONSTRAINTS)){
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				for(OrderedPair p: fp){		
					for(int t=1;t<=ins.getMaxClusterNumber();++t)
						expr=solver.sum(expr,y.get(k,t,p.i));					
					expr=solver.sum(expr,y.get(k,1,p.j));
					solver.addLe(expr, 1);
				}
			}			
		}
		
		if(isActiveExtension(Extension.POLYNOMIAL_ORBITOPE)){
			//first family
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				expr=solver.numExpr();
				for(int v=0;v<=k-1;++v){
					for(int t=1;t<=ins.getMaxClusterNumber();++t)
						expr=solver.sum(expr,y.get(k,t,v));
				}
				solver.addEq(expr,0);
			}
			//second family
			for(int k=1;k<ins.getMaxClusterNumber();++k){
				for(int v=k;v<ins.getNumNodes();++v){
					expr=solver.numExpr();
					for(int t=1;t<=ins.getMaxClusterNumber();++t){
						expr=solver.sum(expr,y.get(k, t, v));		
					}
					for(int u=k-1;u<=v-1;++u){
						for(int t=1;t<=ins.getMaxClusterNumber();++t){
							expr=solver.diff(expr, y.get(k-1,t,u));
						}
					}
					solver.addLe(expr, 0);
				}
			}			
		}
		
		if(isActiveExtension(Extension.EXPONENTIAL_ORBITOPE)){
			System.err.println("Extension "+Extension.EXPONENTIAL_ORBITOPE+" not implemented yet");
			throw new IllegalArgumentException();
		}
		
		if(isActiveExtension(Extension.FLOW_LB)){
			//f,y lower bound
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				for(int i=0;i<ins.getNumNodes();++i){
					for(int t=2;t<=ins.getMaxClusterNumber();++t){
						expr=solver.numExpr();
						for(Integer j:ins.getOutcut(i)){
							expr=solver.sum(expr,f.get(k, t, j, i));					
						}
						solver.addGe(expr,y.get(k,t, i));
					}
					solver.addLe(y.get(k,1,i), f.get(k,1,superRoot,i));
				}			
			}
		}
		

		solver.setParam(IloCplex.DoubleParam.TiLim, timeout);
		if(isActiveExtension(Extension.ROOT_BRANCHING_PRIORITY)){
			//Add branching priorities to the root variables
			for(int k=0;k<ins.getMaxClusterNumber();++k)			
				for(int i=0;i<ins.getNumNodes();++i)
					solver.setPriority(y.get(k,1,i), 100);			
		}		
		solver.setParam(IloCplex.IntParam.ParallelMode, 1);
		solver.solve();
		y.printNonNegative(solver);
		f.printNonNegative(solver);
		
		System.out.println("STATE="+solver.getStatus());
		System.out.println("NN="+solver.getNnodes());
		System.out.println("LB="+solver.getBestObjValue());
		
		//If we have a feasible solution we save it
		if(solver.getStatus() == IloCplex.Status.Feasible || solver.getStatus() == IloCplex.Status.Optimal){
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				for(int v=0;v<ins.getNumNodes();++v){
					for(int t=1;t<=ins.getMaxClusterNumber();++t){
						if(FloatUtils.eq(solver.getValue(y.get(k,t,v)),1.0)){
							sol.insert(k, v);
						}
					}
				}
			}
		}
		
		return solver.getObjValue();
	}

}
