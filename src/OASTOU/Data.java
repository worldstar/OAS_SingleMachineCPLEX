package OASTOU;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

//定義參數
class Data{
	int[][] arcs; //arcs[i][j]表示i到j點的弧
	//OAS starts here.
	int jobs;            //訂單數
	int releaseTime[];     //抵達時間 release date.
	int[] processingTime;//訂單處理時間 processing time
	int[] dueDay;        //訂單交期 due-date
	int[] deadline;      //訂單交期 deadline
	int[] profit;        //訂單利潤 revenue
	double[] weight;     //延遲懲罰權重 penalty weight
	double[][] setup;    //整備時間 setup times	
	
	//TOU
	double[] EC;         //Electricity cost	
	double[] intervalEndTime;  //The interval time
	double[] unitPowerConsumption;
	
	//CO2
	double[] CO2Emission;         //CO2 emission (kg/kWH)	
	double[] CO2IntervalEndTime;  //The interval time
	double CarbonTax = 0.02673155;       //Average CO2 emission cost per KG of .

	// 函数功能：從txt文件中讀取資料並初始化参数
	public void process_OAS(String path, Data data, int jobs) throws Exception {
		// 初始化参数
		FileInputStream fis = new FileInputStream(path);
		DataInputStream in = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String[] tmp;
		data.jobs = jobs;
		data.releaseTime = new int[jobs];
		data.processingTime = new int[jobs];
		data.dueDay = new int[jobs];
		data.deadline = new int[jobs];
		data.profit = new int[jobs];
		data.weight = new double[jobs];
		data.setup = new double[jobs][jobs];
		data.arcs = new int[jobs][jobs];
		
		//TOU data
		data.intervalEndTime = new double[] {0, 420, 480, 780, 900, 1440};
		data.EC = new double[] {0, 0.0422, 0.0750, 0.1327, 0.0750, 0.0422};
		data.unitPowerConsumption = new double[jobs];
		
		//CO2 data		
		data.CO2IntervalEndTime = new double[] {0, 180, 360, 720, 840, 1020, 1080, 1260, 1380, 1440};
		data.CO2Emission = new double[] {0, 0.725, 0.7, 0.693, 0.682, 0.669, 0.682, 0.693, 0.700, 0.725};
				

		tmp = br.readLine().split(",");// split 0,10,10,2,4,6,4,5,7,3,5,0
		for (int i = 0; i < jobs-1; i++) { // i = orders,test 10
			data.releaseTime[i+1] =  Integer.parseInt(tmp[i + 1]);
		}

		tmp = br.readLine().split(",");//
		for (int i = 0; i < jobs-1; i++) { //
			data.processingTime[i+1] = Integer.parseInt(tmp[i + 1]);
		}

		tmp = br.readLine().split(",");//
		for (int i = 0; i < jobs-1; i++) { // 
			data.dueDay[i+1] = Integer.parseInt(tmp[i + 1]);
		}

		tmp = br.readLine().split(",");// 
		for (int i = 0; i < jobs-1; i++) { // 
			data.deadline[i+1] = Integer.parseInt(tmp[i + 1]);
		}

		tmp = br.readLine().split(",");// 
		for (int i = 0; i < jobs-1; i++) { // 
			data.profit[i+1] = Integer.parseInt(tmp[i + 1]);
		}

		tmp = br.readLine().split(",");// 
		for (int i = 0; i < jobs-1; i++) { 
			data.weight[i+1] = Double.parseDouble(tmp[i + 1]);
		}
		
		tmp = br.readLine().split(",");// Power consumption of each job.
		for (int i = 0; i < jobs-1; i++) {
			data.unitPowerConsumption[i] = Double.parseDouble(tmp[i]);
//			System.out.print(data.unitPowerConsumption[i+1]+", ");
		}		
		System.out.println("\nSetup");
		for (int i = 0; i < jobs-1 ; i++) {			
			tmp = br.readLine().split(",");
			for (int j = 0; j < jobs; j++) {
				data.setup[i][j] = Integer.parseInt(tmp[j]);
//				System.out.print(data.setup[i][j]+", ");
			}
//			System.out.println();
		}
		
		for (int i = 0; i < jobs ; i++) {
			for (int j = 0; j < jobs; j++) {
				if(i != j) {
					data.arcs[i][j] = 1;
				}
				else {
					data.arcs[i][j] = 0;
				}
			}
		}													
	}	
}
