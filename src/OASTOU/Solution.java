package OASTOU;

import java.util.ArrayList;

//類功能：解的可行性判斷
class Solution{
	double epsilon = 0.0001;
	Data data = new Data();
	ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
	ArrayList<ArrayList<Double>> servetimes = new ArrayList<>();
	public Solution(Data data, ArrayList<ArrayList<Integer>> routes, ArrayList<ArrayList<Double>> servetimes) {
		super();
		this.data = data;
		this.routes = routes;
		this.servetimes = servetimes;
	}
	//函數功能：比較兩個數的大小
	public int double_compare(double v1,double v2) {
		if (v1 < v2 - epsilon) {
			return -1;
		}
		if (v1 > v2 + epsilon) {
			return 1;
		}
		return 0;
	}
}
