package OASPSD;
import java.io.BufferedReader;
import java.io.*;
import java.util.ArrayList;

import ilog.concert.IloConstraint;
import ilog.concert.IloConversion;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import util.fileWrite1;
/**
 * @author： Shih-Hsin Chen
 * @School: Cheng Shiu University
 * @Descriptions：This MILP model solves the Order acceptance and scheduling problem on a single machine.
 * Oguz, C., Salman, F. S., & Yalçın, Z. B. (2010). Order acceptance and scheduling decisions in make-to-order systems. 
 * International Journal of Production Economics, 125(1), 200-211.
 */
public class OASPSDCplex {
	Data data; //定義類Data的對象
	IloCplex model; //定義cplex內部類的對象
	public IloNumVar[] C; //完工時間矩陣
	Solution solution;
	double cost; //目標值object
	
	public IloNumVar[][] y;	//if order i is before order j
	public IloNumVar[] I;	//if order i is selected
	public IloNumVar[] T;	//tardiness
	public IloNumVar[] R;	//Revenue of each order
	
	//Past-sequence-dependent setup, PSD
	double gamma = 0.1;
	public IloNumVar[][] Y;	//if Ci is before Cj
	public IloNumVar[] PSD;	//PSD cost
	
	public OASPSDCplex(Data data, double gamma) {
		this.data = data;
		this.gamma = gamma;
	}
	//函數功能：解模型，並生成排程順序和得到目標值
	public void solve() throws IloException {
		ArrayList<ArrayList<Integer>> routes = new ArrayList<>(); //定義排程順序List
		ArrayList<ArrayList<Double>> servetimes = new ArrayList<>(); //定義花費時間鍊錶
		//初始化車輛路徑和花費時間鍊錶，鍊錶長度為車輛數k
		for (int k = 0; k < 1; k++) {
			ArrayList<Integer> r = new ArrayList<>(); //定義一個對象為int型的鍊錶
			ArrayList<Double> t = new ArrayList<>(); //定義一個對象為double型的鍊錶
			routes.add(r); //將上述定義的鍊錶加入到鍊錶routes中
			servetimes.add(t); //同上
		}
		//判断建立的模型是否可解
		if(model.solve() == false){
			//模型不可解，直接跳出solve函数
			System.out.println("problem should not be solved. False!!!");
			return;
		}
		else{
			//模型可解，生成排程順序
			for(int k = 0; k < 1; k++){
				boolean terminate = true;
				int i = 0;
				int counter = 0;
				routes.get(k).add(0);		
				servetimes.get(k).add(0.0);
				while(terminate){
					for (int j = 1; j < data.jobs - 1; j++) {
						if (data.arcs[i][j]>=0.5 && model.getValue(y[i][j])>=0.5) {
							routes.get(k).add(j);
							servetimes.get(k).add(model.getValue(C[j]));
							i = j;
//							System.out.println("job: "+j);
							break;
						}
					}
					counter ++;
					if (counter == data.jobs) {
						terminate = false;
					}
				}
			}
		}
		solution = new Solution(data,routes,servetimes);
		cost = model.getObjValue();		
//		System.out.println("routes="+solution.routes);	
//		System.out.println("getObjValue="+model.getObjValue());//the best integer,
//		System.out.println("getBestObjValue="+model.getBestObjValue());//best bound		
//		System.out.println("getMIPRelativeGap="+model.getMIPRelativeGap());//Gap	
		
//		if(model.solveFixed()) {//Continuous model
//			System.out.println("solveFixed (Continuous model) getObjValue="+model.getObjValue());
//			System.out.println("solveFixed (Continuous model) getBestObjValue="+model.getBestObjValue());
//			
//		}
//		else {
//			System.out.println("solveFixed is not found");
//		}
//		model.getSlack(model.);
	}
	
	public void printResults(IloCplex model) throws UnknownObjectException, IloException {
		for(int i = 0; i < data.jobs-1; i++) {
			for(int j = 1; j < data.jobs-1; j++) {
				if(i == j) {
					System.out.print("--- ");
				}
				else {
					System.out.print(model.getValue(y[i][j])+" ");
				}				
			}
			System.out.println("");
		}	
		System.out.println("Yij");
		for(int i = 0; i < data.jobs-1; i++) {
			for(int j = 1; j < data.jobs-1; j++) {
				if(i == j) {
					System.out.print("--- ");
				}
				else {
					System.out.print(model.getValue(Y[i][j])+" ");
				}				
			}
			System.out.println("");
		}			
		System.out.println("\nRi:1,...,n");
		double reve = 0;
		for(int i = 1; i < data.jobs-1; i++) {
			if(model.getValue(I[i]) > 0.5) {
				reve += model.getValue(R[i]);
				System.out.print(model.getValue(R[i])+" ");	
			}	
			else {
				System.out.print(0+" ");	
			}
		}	
		System.out.println("\nreve: "+reve);
		System.out.println("");		
		
		System.out.print("\nIi ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(model.getValue(I[i])+" ");			
		}		
		System.out.print("\nReleastTime ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(data.releaseTime[i]+" ");			
		}				
		System.out.print("\nPi ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(data.processingTime[i]+" ");			
		}				
		System.out.print("\n(PSDi) ");
		for(int i = 0; i < data.jobs; i++) {
			if(i == 0 || i == data.jobs - 1){
				System.out.print("0 ");	
			}
			else if(model.getValue(I[i])== 1){
				System.out.print(model.getValue(PSD[i])+" ");					
			}
			else {
				System.out.print("0 ");	
			}		
		}		
		
		System.out.print("\nCi ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(model.getValue(C[i])+" ");			
		}		
		System.out.print("\ndi ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(data.dueDay[i]+" ");			
		}			
		System.out.print("\ndbar ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(data.deadline[i]+" ");			
		}		
		System.out.print("\nTi ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(model.getValue(T[i])+" ");			
		}			
		System.out.print("\nProfit ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(data.profit[i]+" ");			
		}			
		System.out.print("\nRi 0 ");
		for(int i = 1; i < data.jobs-1; i++) {
			System.out.print(model.getValue(R[i])+" ");			
		}				
	}
	//函數功能：根據OAS Single machine數學模型建立CPLEX模型
	private void build_model(int executeSeconds) throws IloException {
		//model
		model = new IloCplex();
		model.setOut(null);
//		model.setParam(IloCplex.IntParam.RootAlgorithm, ilog.cplex.IloCplex.Algorithm.Dual);
//		model.setParam(IloCplex.IntParam.NodeAlgorithm, ilog.cplex.IloCplex.Algorithm.Dual);
		model.setParam(IloCplex.Param.TimeLimit, executeSeconds);//Seconds
		model.setParam(IloCplex.Param.Threads, 8);
		
		//variables		
		y = new IloNumVar[data.jobs][data.jobs];
		I = new IloNumVar[data.jobs];
		T = new IloNumVar[data.jobs];
		R = new IloNumVar[data.jobs];
		C = new IloNumVar[data.jobs];				//完工時間
		
		//PSD
		Y = new IloNumVar[data.jobs][data.jobs];
		PSD = new IloNumVar[data.jobs];
		
		//定義cplex變量x和w的數據類型及取值範圍
		for (int i = 0; i < data.jobs; i++) {
			for (int j = 0; j < data.jobs; j++) {
				if (data.arcs[i][j]==0) {
//					y[i][j] = null;
					y[i][j] = model.numVar(0, 0, IloNumVarType.Int, "y" + i + "," + j);//Eq13
					
					Y[i][j] = model.numVar(0, 0, IloNumVarType.Float, "Y" + i + "," + j);
				}
				else{
					y[i][j] = model.numVar(0, 1, IloNumVarType.Int, "y" + i + "," + j);//Eq13	
					Y[i][j] = model.numVar(0, 1, IloNumVarType.Float, "Y" + i + "," + j);
				}
			}
			I[i] = model.numVar(0, 1, IloNumVarType.Int, "I" + i);//Eq13//Relaxed from int to float
			T[i] = model.numVar(0, 1E8, IloNumVarType.Float, "T" + i);
			R[i] = model.numVar(0, 1E8, IloNumVarType.Float, "R" + i);
			C[i] = model.numVar(0, 1E8, IloNumVarType.Float, "C" + i);	
			y[data.jobs-1][i] = model.numVar(0, 0, IloNumVarType.Int, "y" + (data.jobs-1) + "," + i);//Eq13
			
			PSD[i] = model.numVar(0, 1E8, IloNumVarType.Float, "PSD" + i);
		}	

		double maxDeadline = 0;
		for(int i = 1 ; i < data.deadline.length -1; i++) {
			if(maxDeadline < data.deadline[i]) {
				maxDeadline = data.deadline[i];
			}
		}
		
		double minReleaseTime = Double.MAX_VALUE;
		for(int i = 1 ; i < data.deadline.length-1; i++) {
			if(minReleaseTime > data.releaseTime[i]) {
				minReleaseTime = data.releaseTime[i];
			}
		}	
//		System.out.printf("maxDeadline: %s minReleaseTime: %s \n", maxDeadline, minReleaseTime);
		
		//加入目標函數	
		IloNumExpr obj = model.numExpr();
		for(int i = 1; i < data.jobs-1; i++){//i=1,...,n
			obj = model.sum(obj, R[i]);
		}
		model.addMaximize(obj);
		//加入限制式
		//公式(1)
		for(int i= 0; i < data.jobs-1;i++){//i=0,...,n
			IloNumExpr expr1 = model.numExpr();			
			for (int j = 1; j < data.jobs; j++) {//j=1,...,n+1
				if (i != j) {//data.arcs[i][j]==1
					expr1 = model.sum(expr1, y[i][j]);
				}								
			}			
			model.addEq(expr1, I[i], "Eq1");
		}				
		//公式(2)
		for(int i= 1; i < data.jobs;i++){//i=1,...,n+1
			IloNumExpr expr1 = model.numExpr();
			for (int j = 0; j < data.jobs-1; j++) {//j=0,...,n
				if (i != j) {//data.arcs[i][j]==1
					expr1 = model.sum(expr1, y[j][i]);
				}								
			}
			model.addEq(expr1, I[i], "Eq2");
		}
		
		//公式PSD: Yij: Ci is before Cj
		for(int i= 1; i < data.jobs-1;i++){//i=0,...,n+1
			for (int j = 0; j < data.jobs-1; j++) {//j=0,...,n
				if (i != j) {
					IloConstraint ifStatements[] = new IloConstraint[3];
					ifStatements[0] = model.eq(I[i], 1);
					ifStatements[1] = model.eq(I[j], 1);
					ifStatements[2] = model.le(C[i], C[j]);					
					model.add(model.ifThen(model.and(ifStatements), model.eq(Y[i][j], 1)));	
				}								
			}
		}
		
		//PSD Calculation: To sum the jobs processing time before job j
		for(int j= 1; j < data.jobs-1;j++){//j=0,...,n+1
			IloNumExpr expr1 = model.numExpr();	
			
			for (int i = 0; i < data.jobs-1; i++) {//i=0,...,n
				if (i != j) {
					expr1 = model.sum(expr1, model.prod(Y[i][j], data.processingTime[i]*gamma));
				}								
			}
			model.addEq(PSD[j], expr1);
		}
		
		//公式(3)
		for(int i= 0; i < data.jobs-1;i++){//i=0,...,n			
			for (int j = 1; j < data.jobs; j++) {//j=1,...,n+1
				IloNumExpr expr1 = model.numExpr();			
				if (i != j) {//data.arcs[i][j]==1
					expr1 = model.sum(expr1, C[i]);//Ci
					expr1 = model.sum(expr1, model.prod(data.processingTime[j], y[i][j]));//(pj)yij
					expr1 = model.sum(expr1, PSD[j]);//(pj)yij
					expr1 = model.sum(expr1, model.prod(data.deadline[i], model.diff(y[i][j], 1)));//dbar(yij-1)
					model.addLe(expr1, C[j], "Eq3");	
				}				
			}			
		}		
		//公式(4)
		for(int i= 0; i < data.jobs-1;i++){//i=0,...,n			
			for (int j = 1; j < data.jobs; j++) {//j=1,...,n+1
				IloNumExpr expr1 = model.numExpr();			
				if (i != j) {//data.arcs[i][j]==1
					expr1 = model.sum(expr1, model.prod(data.releaseTime[j]+data.processingTime[j],I[j]));//(rj+pj)Ij
					expr1 = model.sum(expr1, PSD[j]);//(PSDj)
					model.addLe(expr1, C[j], "Eq4");
				}												
			}			
		}
		//公式(5)
		for(int i= 0; i < data.jobs;i++){//i=0,...,n+1												
			model.addLe(C[i], model.prod(data.deadline[i], I[i]), "Eq5");//Ci<=dbar*Ii
		}
		//公式(6)
		for(int i= 0; i < data.jobs;i++){//i=0,...,n+1												
			model.addGe(T[i], model.diff(C[i], data.dueDay[i]), "Eq6");//Ti>=Ci-di
		}		
		//公式(7)
		for(int i= 0; i < data.jobs;i++){//i=0,...,n+1											
			model.addLe(T[i], model.prod(data.deadline[i]-data.dueDay[i], I[i]), "Eq7");//Ti<=Ci-di		
		}
		//公式(8)
		for(int i= 0; i < data.jobs;i++){//i=0,...,n+1												
			model.addGe(T[i], 0, "Eq8");//Ti>=0
		}	
		//公式(9)
		for(int i= 1; i < data.jobs-1;i++){//i=1,...,n				
			model.addLe(R[i], model.diff(model.prod(data.profit[i], I[i]), model.prod(T[i], data.weight[i])), "Eq9");//Ri<=reveneuei*Ii-Ti*weighti
		}	
		//公式(10)
		for(int i= 1; i < data.jobs-1;i++){//i=1,...,n												
			model.addGe(R[i], 0, "Eq10");//Ri>=0
		}		
		//公式(11)與公式16： Cn+1=max(dvar) change to Cn+1<=max(dvar) as Eq16
		model.addEq(C[0], 0, "Eq11-1");//Cn+1=max(dvar)
		model.addLe(C[data.jobs-1], maxDeadline, "Eq11-2 and Eq16");//Cn+1=max(dvar)
		//公式(12)
		model.addEq(I[0], 1, "Eq12-1");//I0=1
		model.addEq(I[data.jobs-1], 1, "Eq12-2");//In+1=1	
		//公式(17)
		IloNumExpr expr17 = model.numExpr();			
		expr17 = model.sum(expr17, minReleaseTime);//min(releaseTime)
		for(int i = 1 ; i < data.jobs-1; i ++) {
			expr17 = model.sum(expr17, model.prod(data.processingTime[i], I[i]));
			expr17 = model.sum(expr17, PSD[i]);		
		}		
		model.addGe(C[data.jobs-1], expr17, "Eq17");
		//公式(18)	
		for(int i = 1 ; i < data.jobs-1; i ++) {//i=1,...,n
			IloNumExpr expr18 = model.prod(data.releaseTime[i]+data.processingTime[i], I[i]);						
			expr18 = model.sum(expr18, PSD[i]);		
			model.addGe(C[i], expr18, "Eq18");
		}							
	}
	
    double[] startVal;
    double[] startVal2D;
    
    public void addSolution(IloCplex model) throws UnknownObjectException, IloException {
//      IloNumVar[] startVar = new IloNumVar[data.jobs];        
//       for (int i = 0; i < data.jobs; i++) {
//               startVar[i] = I[i];//Eq13
//       }
//       model.addMIPStart(startVar, startVal, IloCplex.MIPStartEffort.Auto);   

      int idx = 0;
      IloNumVar[] startVar2D = new IloNumVar[data.jobs*data.jobs];        
      for (int i = 0; i < data.jobs; i++) {
          for(int j = 0; j < data.jobs; j++) {
              startVar2D[idx] = y[i][j];
              idx ++;
          }                 
      }
      model.addMIPStart(startVar2D, startVal2D, IloCplex.MIPStartEffort.Auto);        
          
  }	
	
	public void solveRelaxation() throws IloException
	{
		IloConversion relax = model.conversion(I, IloNumVarType.Float);
		model.add(relax);
		
		IloConversion relax2[] = new IloConversion[data.jobs];
		for(int i = 0 ; i < data.jobs; i ++) {//i=0,...,n+1
			relax2[i] = model.conversion(y[i], IloNumVarType.Float);
			model.add(relax2[i]);
		}
		model.solve();
        System.out.println("Relaxed solution status = " + model.getStatus());
        System.out.println("Relaxed solution getObjValue value  = " + model.getObjValue());	
        System.out.println("Relaxed solution getBestObjValue value  = " + model.getBestObjValue());		

        startVal = new double[data.jobs];
        startVal2D = new double[data.jobs*data.jobs];   
        
        int idx = 0;
        for (int i = 0; i < data.jobs; i++) {
                startVal[i] = model.getValue(I[i]) > 0.5 ? 1:0;
                for(int j = 0; j < data.jobs; j++) {
	                	if(model.getValue(y[i][j]) > 0.5) {
	                		startVal2D[idx] = 1;
//	                		break;
	                	}
	                	else {
	                		startVal2D[idx] = 0;
	                	}
                    idx ++;
                }
        }            
		model.delete(relax);
		for(int i = 0 ; i < data.jobs; i ++) {//i=0,...,n+1
			model.delete(relax2[i]);
		}				
}	
	public static void main(String[] args) throws Exception {
		Data data = new Data();
		int jobs = 27;//所有點個數，包括0，n+1兩個虛擬訂單
		int executeSeconds = (int)(jobs*1);
		executeSeconds = 29;
		//讀入不同的測試案例
		String OASpath = "SingleMachineOAS/25orders/Tao1/R1/Dataslack_25orders_Tao1R1_2.txt";
		data.process_OAS(OASpath,data,jobs);
		System.out.println("input succesfully: \n"+OASpath);
//		System.out.println("cplex procedure###########################");
		OASPSDCplex cplex = new OASPSDCplex(data, 0.1);
		cplex.build_model(executeSeconds);
//		cplex.model.exportModel("OASmodel.lp");
		double cplex_time1 = System.nanoTime();		
		cplex.solveRelaxation();
		cplex.addSolution(cplex.model);
		cplex.solve();
//		cplex.solution.fesible();
//		System.out.println(cplex.model);		
//		cplex.printResults(cplex.model);
//		System.out.println();
//		System.out.println("\ngetMIPRelativeGap: "+cplex.model.getMIPRelativeGap());
		double cplex_time2 = System.nanoTime();
		double cplex_time = (cplex_time2 - cplex_time1) / 1e9;//求解時間，單位s
		System.out.println("\ncplex_time " + cplex_time + " bestcost " + cplex.cost+"," + cplex.solution.routes);
		
		int nJobs[] = new int[] {50, 50, 100, 10};//10, 15, 20, 25, 50, 100
		int Tao[] = new int[] {1, 5, 9};
		int R[] = new int[] {1, 5, 9};
		double gamma[] = new double[] {0.1, 0.2, 0.3};
		String results = "";
		
//		for(int i = 0 ; i < nJobs.length; i++) {
//			for(int j = 0 ; j < Tao.length; j++) {
//				for(int k = 0 ; k < R.length; k++) {
//					for(int m = 0 ; m < gamma.length; m++) {
//						for(int repl = 1; repl <= 1; repl++) {
//							OASpath = "SingleMachineOAS/"+nJobs[i]+"orders/Tao"+Tao[j]+"/R"+R[k]
//									+"/Dataslack_"+nJobs[i]+"orders_Tao"+Tao[j]+"R"+R[k]+"_"+repl+".txt";
////							System.out.println("input succesfully: \n"+OASpath);
//							data = new Data();
//							data.process_OAS(OASpath,data,nJobs[i]+2);						
//							executeSeconds = (int)(nJobs[i]*60);
//							
//							if(nJobs[i] == 100) {
//								executeSeconds = 3600;
//							}
//							
//							cplex_time1 = System.nanoTime();
//							cplex = new OASPSDCplex(data, gamma[m]);
//							cplex.build_model(executeSeconds);
////							cplex.solveRelaxation();
//							cplex.solve();
//							cplex_time2 = System.nanoTime();
////							cplex.printResults(cplex.model);
//							cplex_time = (cplex_time2 - cplex_time1) / 1e9;//求解時間，單位s
//							results = nJobs[i]+"-Tao"+Tao[j]+"R"+R[k]+"_"+repl+","+ gamma[m]+","+ cplex.model.getObjValue()+ "," 
//									+ cplex.model.getBestObjValue()+ "," 
//									+ cplex.model.getMIPRelativeGap()+"," + cplex_time+"," + cplex.solution.routes+"\n";
//							System.out.print(results);
//							fileWrite1 fileWriter = new fileWrite1();
//							fileWriter.writeToFile(results, "OAS-PSD-MILP-Solutions.txt");
//							fileWriter.run();						
//						}						
//					}
//				}				
//			}
//		}//end for
	}	
}
