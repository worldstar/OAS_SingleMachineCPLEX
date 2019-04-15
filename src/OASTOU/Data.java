package OASTOU;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

//摰儔��
class Data{
	int[][] arcs; //arcs[i][j]銵函內i�j暺�憫
	//OAS starts here.
	int jobs;            //閮�
	int releaseTime[];     //������ release date.
	int[] processingTime;//閮������ processing time
	int[] dueDay;        //閮鈭斗�� due-date
	int[] deadline;      //閮鈭斗�� deadline
	int[] profit;        //閮�瞏� revenue
	double[] weight;     //撱園�蝵唳��� penalty weight
	double[][] setup;    //������ setup times	
	
	//TOU
	double[] EC;         //Electricity cost	
	double[] intervalEndTime;  //The interval time
	double[] unitPowerConsumption;

	// ����嚗�xt��辣銝剛�����蒂�����
	public void process_OAS(String path, Data data, int jobs) throws Exception {
		// �����
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
		data.intervalEndTime = new double[] {420, 480, 690, 1110, 1380, 1440};
		data.EC = new double[] {0.443, 0.8451, 1.2447, 0.8451, 1.2447, 0.443};
		data.unitPowerConsumption = new double[jobs];

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
			data.unitPowerConsumption[i+1] = Double.parseDouble(tmp[i]);
			System.out.print(data.unitPowerConsumption[i]+", ");
		}
		System.out.println("\nSetup");
		for (int i = 0; i < jobs ; i++) {
			tmp = br.readLine().split(",");
			for (int j = 0; j < jobs; j++) {
				data.setup[i][j] = Integer.parseInt(tmp[j]);
				System.out.print(data.setup[i][j]+", ");
			}
			System.out.println();
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
