package OASTOU;
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
 * Power cost data:Che, A., Zeng, Y., & Lyu, K. (2016). An efficient greedy insertion heuristic for energy-conscious 
 * single machine scheduling problem under time-of-use electricity tariffs. Journal of Cleaner Production, 129, 565-577.
 * We assume we should complete the work before 24:00PM. Thus, the last job C[n+1] <= 1440.
 */
public class OASTOUCplex {
	Data data; //定義類Data的對象
	IloCplex model; //定義cplex內部類的對象
	public IloNumVar[] C; //完工時間矩陣
	Solution solution;
	double cost; //目標值object
	
	public IloNumVar[][] y;	//if order i is before order j
	public IloNumVar[] I;	//if order i is selected
	public IloNumVar[] T;	//tardiness
	public IloNumVar[] R;	//Revenue of each order
	
	//TOU	
	public IloNumVar[][] x;	//if order i is processed at period k
	public IloNumVar[] ST;	//Starting time of each order.	
	
	public OASTOUCplex(Data data) {
		this.data = data;
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
		System.out.println("\ndi:0,...,n+1");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(data.dueDay[i]+" ");			
		}			
		System.out.println("\ndbar:0,...,n+1");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(data.deadline[i]+" ");			
		}		
		System.out.println("\nTi:0,...,n+1");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(model.getValue(T[i])+" ");			
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
		System.out.print ("\nSTi ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(model.getValue(ST[i])+" ");			
		}	
		System.out.print("\nPi ");
		for(int i = 0; i < data.jobs-1; i++) {
			System.out.print(data.processingTime[i]+" ");			
		}		
		System.out.print("\nSji ");
		for(int i = 0; i < data.jobs; i++) {
			double tempSetup = 0;
			for(int j = 0; j < data.jobs-1; j ++) {
				if(i != j && model.getValue(y[j][i]) == 1) {
					tempSetup = data.setup[j][i];
					break;
				}
			}
			System.out.print(tempSetup+" ");					
		}			
		System.out.print("\n(Pi+Sji) ");
		for(int i = 0; i < data.jobs; i++) {
			if(model.getValue(I[i])== 1){
				double tempSum = data.processingTime[i];
				for(int j = 0 ; j < data.jobs; j++) {
					if(i != j && model.getValue(y[j][i]) == 1) {
						tempSum += data.setup[j][i];
					}
				}
				System.out.print(tempSum+" ");					
			}
			else {
				System.out.print("0 ");	
			}		
		}		
		
		System.out.print("\nCi ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(model.getValue(C[i])+" ");			
		}										
		
		System.out.print("\nXik \n");
		for(int k = 1 ; k < data.EC.length-2; k++) {
			for(int i = 0; i < data.jobs; i++) {
				if(i == 0) System.out.print(" ");
				System.out.print(model.getValue(x[i][k])+" ");			
			}	
			System.out.println("");
		}		
		
		System.out.print("\nPower(i) ");
		for(int i = 0; i < data.jobs; i++) {
			System.out.print(data.unitPowerConsumption[i]+" ");			
		}		
//		System.out.print("\nEC(i) 0 ");
//		for(int i = 1; i < data.jobs-1; i++) {
//			System.out.print(model.getValue(x[i][0])*data.unitPowerConsumption[i]*data.EC[0]/60.0+" ");			
//		}			
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
	/**
	 * @param executeSeconds
	 * @throws IloException
	 */
	private void build_model(int executeSeconds) throws IloException {
		//model
		model = new IloCplex();
		model.setOut(null);
//		model.setParam(IloCplex.IntParam.RootAlgorithm, ilog.cplex.IloCplex.Algorithm.Dual);
//		model.setParam(IloCplex.IntParam.NodeAlgorithm, ilog.cplex.IloCplex.Algorithm.Dual);
		model.setParam(IloCplex.Param.TimeLimit, executeSeconds);//Seconds
		//variables		
		y = new IloNumVar[data.jobs][data.jobs];
		I = new IloNumVar[data.jobs];
		T = new IloNumVar[data.jobs];
		R = new IloNumVar[data.jobs];
		C = new IloNumVar[data.jobs];				//完工時間
		
		//TOU
		x = new IloNumVar[data.jobs][data.intervalEndTime.length];
		ST = new IloNumVar[data.jobs];			
		
		//定義cplex變量x和w的數據類型及取值範圍
		for (int i = 0; i < data.jobs; i++) {
			for (int j = 0; j < data.jobs; j++) {
				if (data.arcs[i][j]==0) {
//					y[i][j] = null;
					y[i][j] = model.numVar(0, 0, IloNumVarType.Int, "y" + i + "," + j);//Eq13
				}
				else{
					y[i][j] = model.numVar(0, 1, IloNumVarType.Int, "y" + i + "," + j);//Eq13									
				}
			}
			I[i] = model.numVar(0, 1, IloNumVarType.Int, "I" + i);//Eq13//Relaxed from int to float
			T[i] = model.numVar(0, 1E8, IloNumVarType.Float, "T" + i);
			R[i] = model.numVar(0, 1E8, IloNumVarType.Float, "R" + i);
			C[i] = model.numVar(0, 1E8, IloNumVarType.Float, "C" + i);	
			y[data.jobs-1][i] = model.numVar(0, 0, IloNumVarType.Int, "y" + (data.jobs-1) + "," + i);//Eq13		
		}	
		
		//TOU
		for (int i = 0; i < data.jobs; i++) {
			for(int k = 0 ; k < data.intervalEndTime.length; k++) {
				x[i][k] = model.numVar(0, 1E8, IloNumVarType.Float, "x" + i + "," + k);
			}
			ST[i] = model.numVar(0, data.deadline[i]-data.processingTime[i], IloNumVarType.Float, "ST" + i);	
		}				

		double maxDeadline = 0;
		for(int i = 1 ; i < data.deadline.length -1; i++) {
			if(maxDeadline < data.deadline[i]) {
				maxDeadline = data.deadline[i];
			}
		}
		
		//24 hours production
		if(maxDeadline > 1440) {
			maxDeadline = 1440;
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
		//公式(3)
		for(int i= 0; i < data.jobs-1;i++){//i=0,...,n			
			for (int j = 1; j < data.jobs; j++) {//j=1,...,n+1
				IloNumExpr expr1 = model.numExpr();			
				if (i != j) {//data.arcs[i][j]==1
					expr1 = model.sum(expr1, C[i]);//Ci
					expr1 = model.sum(expr1, model.prod(data.setup[i][j]+data.processingTime[j], y[i][j]));//(sij+pi)yij
					expr1 = model.sum(expr1, model.prod(data.deadline[i], model.diff(y[i][j], 1)));//dbar(yij-1)
					model.addLe(expr1, C[j], "Eq3");	
				}				
			}			
		}		
		//公式(4): Revised by Shih-Hsin Chen. Ii*sij*yij
		for(int i= 0; i < data.jobs-1;i++){//i=0,...,n			
			for (int j = 1; j < data.jobs; j++) {//j=1,...,n+1
				IloNumExpr expr1 = model.numExpr();			
				if (i != j) {//data.arcs[i][j]==1
					expr1 = model.sum(expr1, model.prod(data.releaseTime[j]+data.processingTime[j],I[j]));//(rj+pj)Ij
					expr1 = model.sum(expr1, model.prod(data.setup[i][j], y[i][j]));//(sij)yij
//					expr1 = model.sum(expr1, model.prod(I[i], model.prod(data.setup[i][j], y[i][j])));//(sij)yij*Ii
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
//		for(int i= 1; i < data.jobs-1;i++){//i=1,...,n				
//			model.addLe(R[i], model.diff(model.prod(data.profit[i], I[i]), model.prod(T[i], data.weight[i])), "Eq9");//Ri<=reveneuei*Ii-Ti*weighti
//		}	
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
		for(int i = 1 ; i < data.jobs; i ++) {
			expr17 = model.sum(expr17, model.prod(data.processingTime[i], I[i]));
			for(int j = 1 ; j < data.jobs-1; j ++) {				
				if(i != j) {
					expr17 = model.sum(expr17, model.prod(data.setup[j][i], y[j][i]));
				}
			}		
		}		
		model.addGe(C[data.jobs-1], expr17, "Eq17");
		//公式(18)	
		for(int i = 1 ; i < data.jobs-1; i ++) {//i=1,...,n
			IloNumExpr expr18 = model.prod(data.releaseTime[i]+data.processingTime[i], I[i]);						
			for(int j = 1 ; j < data.jobs-1; j ++) {				
				if(i != j) {
					expr18 = model.sum(expr18, model.prod(data.setup[j][i], y[j][i]));
				}
			}		
			model.addGe(C[i], expr18, "Eq18");
		}								
	}
	
	public void buildTOU(IloCplex model) throws UnknownObjectException, IloException {
		//TOU1
//		for(int i = 0 ; i < data.jobs; i ++) {//i=0,...,n+1
//			IloNumExpr expr = model.diff(C[i], data.processingTime[i]);			
//			for(int j = 0 ; j < data.jobs; j ++) {		
//				if(i != j) {
//					expr = model.diff(expr, model.prod(y[j][i], data.setup[j][i]));					
//				}
//			}		
//			model.addGe(ST[i], expr, "TOU1");		
//		}
		for(int i = 0 ; i < data.jobs; i ++) {//i=1,...,n+1					
			for(int j = 0 ; j < data.jobs; j ++) {		
				if(i != j) {
					IloConstraint ifStatements[] = new IloConstraint[3];
					ifStatements[0] = model.eq(I[i], 1);
					ifStatements[1] = model.eq(y[j][i], 1);
					IloConstraint jBeforeI = model.and(ifStatements);						
					IloConstraint STiConstraint = model.eq(ST[i], model.max(C[j], data.releaseTime[i]));
					model.add(model.ifThen(jBeforeI , STiConstraint));				
				}
			}			
		}		
		
		//Paper Eq2.
		for(int k = 1 ; k < data.intervalEndTime.length; k ++) {		
			IloNumExpr expr = model.numExpr();		
			for(int i = 0 ; i < data.jobs; i ++) {//i=0,...,n+1
				expr = model.sum(expr, x[i][k]);				
			}
			model.addLe(expr, data.intervalEndTime[k]-data.intervalEndTime[k-1], "TOU-xik");
		}
		//Paper Eq3.
//		for(int i = 0 ; i < data.jobs; i ++) {//i=0,...,n+1		
//			IloNumExpr expr = model.numExpr();
//			for(int k = 1 ; k < data.intervalEndTime.length; k ++) {						
//				expr = model.sum(expr, model.prod(I[i], x[i][k]));	
//			}						
//			model.addGe(expr, model.sum(0.01, model.diff(C[i], ST[i])), "TOU-Eq3");
//		}		
		//TOU2
//		for(int i = 0 ; i < data.jobs; i ++) {//i=0,...,n+1			
//			for(int k = 1 ; k < data.intervalEndTime.length; k ++) {
//				IloNumExpr expr = model.min(C[i], data.intervalEndTime[k]);
//				IloNumExpr expr2 = model.max(ST[i], data.intervalEndTime[k-1]);
//				expr = model.diff(expr, expr2);		
//				model.addGe(x[i][k], expr, "TOU2");
//			}	
//		}			
		
		for(int i = 0 ; i < data.jobs; i ++) {//i=0,...,n+1			
			for(int k = 1 ; k < data.intervalEndTime.length; k ++) {						
				//theSameZoneCondition	
				IloConstraint ifStatements[] = new IloConstraint[3];
				ifStatements[0] = model.eq(I[i], 1);
				ifStatements[1] = model.ge(ST[i], data.intervalEndTime[k-1]);
				ifStatements[2] = model.le(C[i], data.intervalEndTime[k]);
				IloConstraint zoneCondition = model.and(ifStatements);				
				//Please notice the le and eq of the xik. eq will let the STi = Ci
				IloConstraint timeCalc = model.ge(x[i][k], model.diff(C[i], ST[i]));
				model.add(model.ifThen(zoneCondition , timeCalc));
				
				//Across two time zones: For the part of ST to bk
				IloConstraint ifStatements2[] = new IloConstraint[3];
				ifStatements2[0] = model.eq(I[i], 1);
				ifStatements2[1] = model.ge(ST[i], data.intervalEndTime[k-1]);
				ifStatements2[2] = model.ge(C[i], data.intervalEndTime[k]);
				IloConstraint zoneCondition2 = model.and(ifStatements2);												
				IloConstraint timeCalc2 = model.le(x[i][k], model.diff(data.intervalEndTime[k], ST[i]));
				model.add(model.ifThen(zoneCondition2 , timeCalc2));		
				
				//Across two time zones: For the part of bk to Ci
				zoneCondition = model.and(model.le(ST[i], data.intervalEndTime[k-1]), model.and(model.ge(C[i], data.intervalEndTime[k-1]), model.le(C[i], data.intervalEndTime[k])));				
				timeCalc = model.le(x[i][k], model.diff(C[i], data.intervalEndTime[k-1]));
				model.add(model.ifThen(zoneCondition , timeCalc));	
				
				//Rejected order xik = 0
				IloConstraint ifStatements3[] = new IloConstraint[1];
				ifStatements3[0] = model.eq(I[i], 0);
				IloConstraint zoneCondition3 = model.and(ifStatements3);												
				IloConstraint timeCalc3 = model.le(x[i][k], 0);
				model.add(model.ifThen(zoneCondition3 , timeCalc3));						
			}	
		}
				
//		//公式(9) with TOU. R[i] minus the electricity cost.
		for(int i= 1; i < data.jobs-1;i++){//i=1,...,n	
			IloNumExpr expr = model.diff(model.prod(data.profit[i], I[i]), model.prod(T[i], data.weight[i]));
			for(int k = 1 ; k < data.intervalEndTime.length; k ++) {	
				expr = model.diff(expr, model.prod(x[i][k], data.EC[k]*data.unitPowerConsumption[i]/60.0));
			}			
			model.addLe(R[i], expr, "Eq9TOU");//Ri<=reveneuei*Ii-Ti*weight_i-xik*eck*power_i/60
		}			
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
//        System.out.println("Relaxed solution status = " + model.getStatus());
//        System.out.println("Relaxed solution getObjValue value  = " + model.getObjValue());	
//        System.out.println("Relaxed solution getBestObjValue value  = " + model.getBestObjValue());		
        
//		model.delete(relax);
//		for(int i = 0 ; i < data.jobs; i ++) {//i=0,...,n+1
//			model.delete(relax2[i]);
//		}				
	}	
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Data data = new Data();
		int jobs = 12;//所有點個數，包括0，n+1兩個虛擬訂單
		int executeSeconds = (int)(jobs*60);
		double cplex_time1 = System.nanoTime();
		double cplex_time2 = 0, cplex_time = 0;
		//讀入不同的測試案例
//		String OASpath = "SingleMachineOAS/10orders/Tao1/R1/Dataslack_10orders_Tao1R1_1.txt";
		String OASpath = "SingleMachineOASWithTOU/10orders/Tao1/R1/Dataslack_10orders_Tao1R1_1.txt";
				
		data.process_OAS(OASpath,data,jobs);
		System.out.println("input succesfully: \n"+OASpath);
		System.out.println("cplex procedure###########################");
		OASTOUCplex cplex = new OASTOUCplex(data);
		cplex.build_model(executeSeconds);
		cplex.buildTOU(cplex.model);		
		/*	
//		cplex.model.exportModel("OASmodel.lp");						
//		cplex.solveRelaxation();
		cplex.solve();
//		cplex.solution.fesible();
//		System.out.println(cplex.model);		
//		cplex.printResults(cplex.model);
//		System.out.println();
		
//		System.out.println("\ngetMIPRelativeGap: "+cplex.model.getMIPRelativeGap());
		cplex_time2 = System.nanoTime();
		cplex_time = (cplex_time2 - cplex_time1) / 1e9;//求解時間，單位s
		System.out.println(cplex.model.getObjValue()+ "," + cplex.model.getBestObjValue()+ "," 
				+ cplex.model.getMIPRelativeGap()+"," + cplex_time+"," + cplex.solution.routes);
*/
		int nJobs[] = new int[] {10, 15, 20, 25, 50, 100};//10, 15, 20, 25, 50, 100
		int Tao[] = new int[] {1, 3, 5, 7, 9};
		int R[] = new int[] {1, 3, 5, 7, 9};
		String results = "";
		
		for(int i = 0 ; i < nJobs.length; i++) {
			for(int j = 0 ; j < Tao.length; j++) {
				for(int k = 0 ; k < R.length; k++) {
					for(int repl = 1; repl <= 10; repl++) {
						OASpath = "SingleMachineOASWithTOU/"+nJobs[i]+"orders/Tao"+Tao[j]+"/R"+R[k]
								+"/Dataslack_"+nJobs[i]+"orders_Tao"+Tao[j]+"R"+R[k]+"_"+repl+".txt";
						data = new Data();
						data.process_OAS(OASpath,data,nJobs[i]+2);						
						executeSeconds = 3600;
						cplex_time1 = System.nanoTime();
						cplex = new OASTOUCplex(data);
						cplex.build_model(executeSeconds);
						cplex.buildTOU(cplex.model);	
//						cplex.solveRelaxation();
						cplex.solve();
						cplex_time2 = System.nanoTime();
						cplex_time = (cplex_time2 - cplex_time1) / 1e9;//求解時間，單位s
						results = nJobs[i]+"-Tao"+Tao[j]+"R"+R[k]+"_"+repl+","+ cplex.model.getObjValue()+ "," 
								+ cplex.model.getBestObjValue()+ "," 
								+ cplex.model.getMIPRelativeGap()+"," + cplex_time+"," + cplex.solution.routes;
						System.out.println(results);
						fileWrite1 fileWriter = new fileWrite1();
						fileWriter.writeToFile(results, "OAS-TOU-MILP-Solutions.txt");
						fileWriter.run();
					}
				}				
			}
		}//end for
	}	
}
