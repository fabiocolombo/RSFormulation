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
		BiIndexedIntVarMap r=new BiIndexedIntVarMap(solver, 0, 1, "r");							//(k,i)   (cluster,node)
		IndexedNumVarMap dp=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "dp");		    //(k)     (cluster)
		IndexedNumVarMap dm=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "dm");		    //(k)     (cluster)			
		BiIndexedNumVarMap y=new BiIndexedNumVarMap(solver, 0, 1, "y");							//(i,j)   (node1<node2)
		IndexedNumVarMap u=new IndexedNumVarMap(solver, 0, Double.MAX_VALUE, "u");				//(i)	  (node)
		
		//Build expressions denoting the usage of a node in a group
		IloNumExpr nodeUsage[][]=new IloNumExpr[ins.getMaxClusterNumber()][ins.getNumNodes()];
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int v=0;v<ins.getNumNodes();++v){
				nodeUsage[k][v]=solver.numExpr();
				for(int j:ins.getOutcut(v))
					nodeUsage[k][v]=solver.sum(nodeUsage[k][v],x.get(k,j,v));
				nodeUsage[k][v]=solver.sum(nodeUsage[k][v],r.get(k,v));
			}
		}
		//Build denoting the coverage of a node
		IloNumExpr coverage[]=new IloNumExpr[ins.getNumNodes()];
		for(int v=0;v<ins.getNumNodes();++v){
			coverage[v]=solver.numExpr();
			for(int k=0;k<ins.getMaxClusterNumber();++k)
				coverage[v]=solver.sum(coverage[v],nodeUsage[k][v]);
		}
		
		
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
		IloNumExpr exprO1=solver.numExpr();
		for(int i=0;i<ins.getNumNodes();++i){
			exprO1=solver.sum(exprO1,solver.prod(w1, solver.diff(1, coverage[i])));			
		}
		//O2
		IloNumExpr exprO2=solver.numExpr();
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			exprO2=solver.sum(exprO2,solver.prod(w2,dm.get(k)));
			exprO2=solver.sum(exprO2,solver.prod(w2,dp.get(k)));
		}
		//O3
		IloNumExpr exprO3=solver.numExpr();
		for(OrderedPair p:fp){
			double dmax=ins.getDMax();
			double d=ins.getD(p.i,p.j);
			IloNumVar yvar=y.get(p.i, p.j);
			exprO3=solver.sum(exprO3,solver.prod(w3*(dmax-d),yvar));
		}
		expr=solver.sum(exprO1,exprO2,exprO3);					
		solver.add(solver.objective(IloObjectiveSense.Minimize,expr));
		
		//packing constraints
		for(int i=0;i<ins.getNumNodes();++i){			
			solver.addLe(coverage[i],1);
		}
		
		//arc precedence constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			for(int i=0;i<ins.getNumNodes();++i){
				for(int j:ins.getOutcut(i)){
					//precedence for arc (i,j)					
					solver.addLe(x.get(k, i, j), nodeUsage[k][i]);
					//precedence for arc (j,i)					
					solver.addLe(x.get(k, j, i), nodeUsage[k][j]);
				}
			}		
		}
		
		//root uniqueness constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i){
				expr=solver.sum(expr,r.get(k, i));
			}			
			solver.addLe(expr,1);
		}
		
		//cardinality constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i){				
				expr=solver.sum(expr,nodeUsage[k][i]);
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

		//delta definition constraints
		for(int k=0;k<ins.getMaxClusterNumber();++k){
			expr=solver.numExpr();
			for(int i=0;i<ins.getNumNodes();++i){				
				expr=solver.sum(expr,nodeUsage[k][i]);				
			}
			solver.addLe(solver.diff(expr,ins.getLambda()),dp.get(k));
			solver.addLe(solver.diff(ins.getLambda(),expr),dm.get(k));
		}

		//y definition constraints
		for(int k=0;k<ins.getMaxClusterSize();++k){
			for(OrderedPair p:fp){
				IloNumExpr niExpr=solver.numExpr();
				for(int v:ins.getOutcut(p.i))
					niExpr=solver.sum(niExpr,x.get(k,v,p.i));
				niExpr=solver.sum(niExpr,r.get(k,p.i));
				IloNumExpr njExpr=solver.numExpr();
				for(int v:ins.getOutcut(p.j)){
					njExpr=solver.sum(njExpr,x.get(k,v,p.j));
				}
				njExpr=solver.sum(njExpr,r.get(k,p.j));
				solver.addGe(y.get(p.i,p.j),solver.diff(solver.sum(niExpr,njExpr),1));
			}
		}
		
		if(isActiveExtension(Extension.ROOT_MINIMIZATION_CONSTRAINTS)){
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				for(OrderedPair p: fp){
					expr=solver.sum(nodeUsage[k][p.i],r.get(k,p.j));
					solver.addLe(expr, 1);
				}
			}			
		}
		
		if(isActiveExtension(Extension.POLYNOMIAL_ORBITOPE)){
			//first family
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				expr=solver.numExpr();
				for(int i=0;i<=k-1;++i){					
					expr=solver.sum(expr,nodeUsage[k][i]);
				}
				solver.addEq(expr,0);
			}
			//second family
			for(int k=1;k<ins.getMaxClusterNumber();++k){
				for(int i=k;i<ins.getNumNodes();++i){
					IloNumExpr lhs=solver.numExpr();
					for(int k1=k;k<ins.getMaxClusterNumber();++k){
						lhs=solver.sum(lhs,nodeUsage[k1][i]);
					}
					IloNumExpr rhs=solver.numExpr();
					for(int g=k-1;g<i;++g){
						rhs=solver.sum(rhs,nodeUsage[k-1][g]);
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
					solver.setPriority(r.get(k,i), 100);			
		}
		solver.solve();
		System.out.println("STATE="+solver.getStatus());
		System.out.println("NN="+solver.getNnodes());
		System.out.println("LB="+solver.getBestObjValue());
		
		//If we have a feasible solution we save it
		if(solver.getStatus() == IloCplex.Status.Feasible || solver.getStatus() == IloCplex.Status.Optimal){
			for(int k=0;k<ins.getMaxClusterNumber();++k){
				for(int v=0;v<ins.getNumNodes();++v){
					if(FloatUtils.eq(solver.getValue(nodeUsage[k][v]),1.0)){
						sol.insert(k, v);					
					}
				}
			}
		}
		
//		x.printNonNegative(solver);
//		r.printNonNegative(solver);
//		u.printNonNegative(solver);
//		dp.printNonNegative(solver);
//		dm.printNonNegative(solver);

		return solver.getObjValue();
	}

}
