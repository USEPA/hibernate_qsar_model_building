package gov.epa.endpoints.reports.WebTEST.ApplicabilityDomain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;


import wekalite.Instance;
import wekalite.Instances;

public class AD_simNN_All_descriptors {

	static int countNeighbors=3;//TODO do we want this as global variable???
	static double avgSimCutoff=0.5;//TODO do we want this as global variable???
	
	/**
	 * AD = whether average similarity of k neighbors exceeds a fixed cutoff (all descriptors used to define similarity)
	 * 
	 * @param testInstance
	 * @param instancesTraining
	 * @return
	 */

	static boolean calculateAD_Fixed_Cutoff_wekalite(wekalite.Instance testInstance,wekalite.Instances instancesTraining,String simMeasure) {
		List<Analog>analogsTraining=AnalogFinder.findAnalogsWekalite(testInstance, instancesTraining, countNeighbors, 0, true, simMeasure);
		double avgSim=0;
		for (Analog analog:analogsTraining) {
			avgSim+=analog.sim;
		}
		avgSim/=(double)countNeighbors;	
		
//		System.out.println(testInstance.getName()+"\t"+avgSim);
		
		return avgSim>=avgSimCutoff;
	}
	
	public static boolean calculateAD_frac(Instance testInstance, Instances instancesTraining,String simMeasure,double cutoffTrainingFrac) {

		double avgSim = calculateAverageSimilarity(testInstance, instancesTraining, instancesTraining.getMeans(), instancesTraining.getStdDevs(), simMeasure);		
//		System.out.println(testInstance.stringValue(0)+"\t"+avgSim);		
		return avgSim>=cutoffTrainingFrac;
	}
	
	
	/**
	 * Get the similarity coefficient that gives a certain prediction coverage for
	 * the training set (leave one out calculation)
	 * 
	 * @param or
	 * @param trainingSet
	 * @param simMeasure
	 * @param fracPredTraining
	 * @param mean
	 * @param stdDev
	 * @return
	 */
	public static double calculateAvgSC_trainingSet(int numNeighbors, Instances trainingSet,
			String simMeasure, double fracPredTraining) {
		
		int numDescriptors = trainingSet.numAttributes();

		
		//Use all descriptors:
		int [] descriptorNumbers=new int [numDescriptors];//based on weka core, need to remove attributes for ID and property value
		for (int i=0;i<numDescriptors;i++) {
			descriptorNumbers[i]=i;
		}

		double[][] descValsTrain = createDescriptorArray(descriptorNumbers, trainingSet);

		// Calculate distances between training compounds
		float[][] distance = AnalogFinder.calculateDistancesTrainingTest(numDescriptors, descValsTrain, descValsTrain,
				simMeasure, trainingSet.getMeans(), trainingSet.getStdDevs());

		Vector<Double> avgSCTrain = new Vector<>();

		// find avg similarity for each member of training set for k nearest neighbors
		for (int i = 0; i < trainingSet.numInstances(); i++) {
			TreeMap<Float, Integer> htDist = new TreeMap<>();
			for (int j = 0; j < trainingSet.numInstances(); j++) {
				if (i == j)
					continue;
				addtoMap(htDist, j, distance[i][j], numNeighbors, simMeasure);
			}
			avgSCTrain.add(calculateAvgSC(htDist));// uses square of distances in weights

//			System.out.println(IDsTest[i]+"\t"+adValsTest[i]);
		}

		Collections.sort(avgSCTrain);

//		for (int i=0;i<avgSCTrain.size();i++) {
//			System.out.println(i+"\t"+avgSCTrain.get(i));
//		}

		int index = (int) (avgSCTrain.size() * (1 - fracPredTraining));
		return avgSCTrain.get(index);

	}
	
	public static double calculateAvgSC(TreeMap<Float, Integer> hm) {

		float avgSC = 0;

		for (Map.Entry<Float, Integer> entry : hm.entrySet()) {
			float SC = entry.getKey();
			avgSC += SC;
		}
		avgSC /= (double) hm.size();

//		System.out.println(avgSC);
		return avgSC;

	}
	
	
	/**
	 * 
	 * Only store closest neighbors
	 * 
	 * Treemaps are autosorted by key
	 * 
	 * @param ht
	 * @param j
	 * @param dist
	 */
	public static void addtoMap(TreeMap<Float,Integer>ht, int j, float dist,int k,String typeSimilarityMeasure) {
		
		if (typeSimilarityMeasure.contentEquals(AnalogFinder.typeSimilarityMeasureEuclideanDistance)) {
			
			if (dist<0.0000001) return;
			
			if (ht.size()<k) ht.put(dist,j);
			else {
				if (dist<ht.lastKey()) {
					ht.remove(ht.lastKey());
					ht.put(dist,j);
//					System.out.println("j="+j+", dist="+dist);
					
				}			
//				System.out.println(ht.firstKey()+"\t"+ht.lastKey());
			}

		} else if (typeSimilarityMeasure.contentEquals(AnalogFinder.typeSimilarityMeasureCosineSimilarityCoefficient) || typeSimilarityMeasure.contentEquals(AnalogFinder.typeSimilarityMeasureTanimotoCoefficient)) {
			
			if (dist>0.9999999) {//same compound omit
//				System.out.println("exact match:"+dist);
				return;
			}
			
			if (ht.size()<k) ht.put(dist,j);
			else {
				if (dist>ht.firstKey()) {
					ht.remove(ht.firstKey());
					ht.put(dist,j);
//					System.out.println("j="+j+", dist="+dist);
				}			
//				System.out.println(ht.firstKey()+"\t"+ht.lastKey());
			}

		}
		
				
		
	}
	
	public static double[][] createDescriptorArray(int[] selAttr, Instances instances) {

		double [][] descValsTrain=new double [instances.numInstances()][selAttr.length];

		for (int i=0;i<instances.numInstances();i++) {
			for (int j=0;j<selAttr.length;j++) {
				descValsTrain[i][j]=instances.instance(i).value(selAttr[j]);
			}
		}
		return descValsTrain;
	}

	private static double calculateAverageSimilarity(Instance testInstance,
			Instances instancesTraining, double[] mean, double[] stdev, String simMeasure) {
		List<Analog>analogsTraining=AnalogFinder.findAnalogsWekalite(testInstance, instancesTraining, countNeighbors, 0, true, simMeasure);
		double avgSim=0;
		
//		System.out.println(testInstance.stringValue(0));
		for (Analog analog:analogsTraining) {
			avgSim+=analog.sim;
//			System.out.println(analog.ID+"\t"+analog.sim);
		}
		
//		System.out.println("");
		
		avgSim/=(double)countNeighbors;
		return avgSim;
	}


	
}
