package gov.epa.util;

import java.util.List;

public class MathUtil {

	public static Double stdevS(List<Double> data) {
		double mean = 0.0;
		for (Double d:data) {
			mean += d;
		}
		mean /= data.size();
		
		double stdev = 0.0;
		for (Double d:data) {
			stdev += Math.pow(d - mean, 2);
		}
		stdev /= data.size() - 1;
		stdev = Math.sqrt(stdev);
		
		return stdev;
	}
	
	public static Double stdevP(List<Double> data) {
		double mean = 0.0;
		for (Double d:data) {
			mean += d;
		}
		mean /= data.size();
		
		double stdev = 0.0;
		for (Double d:data) {
			stdev += Math.pow(d - mean, 2);
		}
		stdev /= data.size();
		stdev = Math.sqrt(stdev);
		
		return stdev;
	}

}
