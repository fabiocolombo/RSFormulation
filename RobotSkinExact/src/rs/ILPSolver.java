package rs;

import ilog.concert.IloException;

import java.util.EnumSet;

public abstract class ILPSolver {
	public enum Extension{
		ROOT_MINIMIZATION_CONSTRAINTS,
		POLYNOMIAL_ORBITOPE,
		EXPONENTIAL_ORBITOPE,
		PFILTER,
		ROOT_BRANCHING_PRIORITY,
		FLOW_LB
	};
	
	private static EnumSet<Extension> extension=EnumSet.noneOf(Extension.class);
	
	public static void insertExtension(Extension e){
		extension.add(e);
	}
	
	public static void removeExtension(Extension e){
		extension.remove(e);
	}
	
	public static boolean isActiveExtension(Extension e){
		return extension.contains(e);
	}
	
	public abstract double solve(Instance ins, Solution sol,int timeout) throws IloException;

}
