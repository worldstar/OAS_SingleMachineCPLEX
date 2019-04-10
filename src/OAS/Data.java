package OAS;

//定義參數
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
}