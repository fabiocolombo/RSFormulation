package rs;

import ilog.concert.IloException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RSDriver {

	public static void main(String[] args) throws FileNotFoundException, IOException{
		try{
			if(args.length<2){
				System.out.println("Error, usage: solver format pathname [formulations extension specifications]");
				System.out.println("Available format: OLD NEW");
				System.out.println("Available formulations extensions:");
				for(ILPSolver.Extension e:ILPSolver.Extension.values()){
					System.out.println(e.name());
				}
				return;
			}
			for(int i=2;i<args.length;++i){
				try{
					ILPSolver.Extension ext=ILPSolver.Extension.valueOf(args[i]);
					ILPSolver.insertExtension(ext);
				}catch(IllegalArgumentException e){
					System.out.println("Ignoring wrong extension:"+args[i]);
				}			
			}
			
			File file=new File(args[1]);
			if(!file.exists()){
				System.out.println("Error, file "+file.getAbsolutePath()+" doesn't exists");
				return;
			}
			Instance ins=null;
			if(args[0].equals("OLD"))
				ins=Instance.parseInstanceOld(file);
			else if(args[0].equals("NEW"))
				ins=Instance.parseInstanceNew(file);
			else{
				System.err.println("Invalid format: "+args[0]);
				return;
			}
			System.out.println("Finished Instance Parsing.");
			System.out.println("Number of nodes:"+ins.getNumNodes());
			Solution sol=new Solution(ins);
			long beg=System.currentTimeMillis();			
			double obj=ILPSolver.solve(ins,sol);	
			System.out.println("Elapsed Time:"+(System.currentTimeMillis() - beg)/1000.0);
			sol.isFeasible(ins, obj);		
			System.out.println("Optimal Solution:");
			System.out.println(sol);
			System.out.println("Optimal Objective Value:");
			System.out.println(obj);
		}
		catch(IloException e){
			System.err.println("Raised exception:"+e.getMessage());			
		}
	}

}
