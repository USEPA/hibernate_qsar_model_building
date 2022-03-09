package gov.epa.endpoints.reports.WebTEST.ApplicabilityDomain;
import java.io.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.util.wekalite.*;


/**
 * Finds analogs based on similarity of descriptor values
 *  
 * @author TMARTI02
 *
 */
public class AnalogFinder{

	public static final String typeSimilarityMeasureEuclideanDistance="Euclidean distance";
	public static final String typeSimilarityMeasureCosineSimilarityCoefficient="Cosine Similarity coefficient";
	public static final String typeSimilarityMeasureTanimotoCoefficient="Tanimoto Similarity Coefficient";


	public static float[][] calculateDistancesTrainingTest(int numDescriptors,double[][] descValsTrain, double[][] descValsTest,String typeSimilarityMeasure,double []mean,double []stdDev)  {
		
		float [][] distance=new float [descValsTest.length][descValsTrain.length];

		for (int i=0;i<descValsTest.length;i++) {

			double [] veci=new double [numDescriptors];

			for (int k=0;k<veci.length;k++) {
				veci[k]=descValsTest[i][k];
			}

			for (int j=0;j<descValsTrain.length;j++) {
				double [] vecj=new double [numDescriptors];

				for (int k=0;k<veci.length;k++) {
					vecj[k]=descValsTrain[j][k];
				}
				distance[i][j]=(float)calculateDistance(veci, vecj,typeSimilarityMeasure,mean,stdDev);
				//    			System.out.println(i+"\t"+j+"\t"+distance[i][j]);

			}
		}
		return distance;
	}

	private static double calculateDistance(double []vec1,double []vec2,String typeSimilarityMeasure,double []mean,double []stdDev)  {
		if (typeSimilarityMeasure.contentEquals(typeSimilarityMeasureEuclideanDistance)) {
			return calculateEuclideanDistance(vec1,vec2,mean,stdDev);			
		} else if (typeSimilarityMeasure.contentEquals(typeSimilarityMeasureTanimotoCoefficient)) {
			return calculateTanimotoSimilarityCoefficient(vec1,vec2,mean,stdDev);
		} else if (typeSimilarityMeasure.contentEquals(typeSimilarityMeasureCosineSimilarityCoefficient)) {
			return calculateCosineSimilarityCoefficient(vec1,vec2,mean,stdDev);
		} else {
			System.out.println("unknown simMeasure="+typeSimilarityMeasure);
			return -9999;
		}
	}


	public static float[] calculateDistancesTraining(Instances trainingSet, int numDescriptors,
			double[][] descValsTrain, double[] descValsTest,String typeSimilarityMeasure,double []mean,double[]stdDev)  {

		float [] distance=new float [trainingSet.numInstances()];
		double [] veci=new double [numDescriptors];

		for (int j=0;j<descValsTrain.length;j++) {
			double [] descValsTrain1d=new double [numDescriptors];

			for (int k=0;k<veci.length;k++) {
				descValsTrain1d[k]=descValsTrain[j][k];
			}
			distance[j]=(float)calculateDistance(descValsTest, descValsTrain1d,typeSimilarityMeasure,mean,stdDev);
			//			System.out.println(i+"\t"+j+"\t"+distance[i][j]);
		}

		return distance;
	}



	private static double calculateEuclideanDistance(double []vec1,double []vec2,double []Mean,double []StdDev) {

		double sum=0;

		for (int i=0; i<vec1.length; i++) {

			if (StdDev[i]>0) {
				vec1[i]=(vec1[i]-Mean[i])/StdDev[i];
				vec2[i]=(vec2[i]-Mean[i])/StdDev[i];
			} else {
				vec1[i]=vec1[i]-Mean[i];
				vec2[i]=vec2[i]-Mean[i];
			}

			double diff = (vec1[i]-vec2[i]);
			sum += diff*diff;
		}

		return Math.sqrt(sum);
	}

	/**
	 * Calculate cosine similarity coefficient, assumes not standardized descriptors
	 * 
	 * @param vec1
	 * @param vec2
	 * @param Mean
	 * @param StdDev
	 * @return
	 */
	private static double calculateCosineSimilarityCoefficient(double[] vec1, double[] vec2,double [] Mean,double []StdDev) {

		double SC=0;
		double SumXY=0;
		double SumX2=0;
		double SumY2=0;

		for (int i=0;i<vec1.length;i++) {
			double val1=vec1[i];
			double val2=vec2[i];
			
			if (StdDev[i]>0) {
				val1=(val1-Mean[i])/StdDev[i];
				val2=(val2-Mean[i])/StdDev[i];
			} else {
				val1=val1-Mean[i];
				val2=val2-Mean[i];
			}

			SumXY+=val1*val2;
			SumX2+=val1*val1;
			SumY2+=val2*val2;

		}//end loop over descriptors
		SC=SumXY/Math.sqrt(SumX2*SumY2);
		//		System.out.println(SC);
		return SC;

	}


	private static double calculateTanimotoSimilarityCoefficient(double[] vec1, double[] vec2,double [] Mean,double []StdDev) {
		double SC=0;
		double SumXY=0;
		double SumX2=0;
		double SumY2=0;

		for (int i=0;i<vec1.length;i++) {
			double val1=vec1[i];
			double val2=vec2[i];

			if (StdDev[i]>0) {
				val1=(val1-Mean[i])/StdDev[i];
				val2=(val2-Mean[i])/StdDev[i];
			} else {
				val1=val1-Mean[i];
				val2=val2-Mean[i];
			}

			SumXY+=val1*val2;
			SumX2+=val1*val1;
			SumY2+=val2*val2;

		}//end loop over descriptors
		SC=SumXY/(SumX2+SumY2-SumXY);

		//		System.out.println(SC);
		return SC;
	}





	

	public static List<Analog> findAnalogsWekalite(gov.epa.util.wekalite.Instance chemical,gov.epa.util.wekalite.Instances trainingSet, int MaxCount,double SCmin, boolean excludeID, String sim) {

		Hashtable<Double,Vector<Instance>> ht = new Hashtable<>();//Store instances by similarity, can have multiple instances with same similarity

		for (int i = 0; i < trainingSet.numInstances(); i++) {
			Instance chemicali = trainingSet.instance(i);
			String IDi = chemicali.getName();

			if (excludeID) {//dont want to use test chemical to predict itself which can happen if chemical appears in training set
				String ID=chemical.getName();
				if (IDi.equals(ID)) {
					continue;
				}
			}
			double SimCoeff=-1;

			if (sim.equals(typeSimilarityMeasureCosineSimilarityCoefficient)) {
				SimCoeff=CalculateCosineCoefficientWekalite(chemical,chemicali,trainingSet.getMeans(),trainingSet.getStdDevs());	
			} else if (sim.equals(typeSimilarityMeasureTanimotoCoefficient)) {
				SimCoeff=CalculateTanimotoCoefficientWekalite(chemical,chemicali,trainingSet.getMeans(),trainingSet.getStdDevs());
			}

//			System.out.println(IDi+"\t"+SimCoeff);
			
			if (SimCoeff >= SCmin) {

				if (ht.get(SimCoeff)==null) {
					Vector<Instance>instances=new Vector<>();
					instances.add(chemicali);
					ht.put(SimCoeff, instances);	
				} else {
					Vector<Instance>instances=ht.get(SimCoeff);
					instances.add(chemicali);
				}
			}
		}

		Vector<Double> v = new Vector<Double>(ht.keySet());

		//Sort in descending order (highest similarity first)
		java.util.Collections.sort(v, Collections.reverseOrder());

		Enumeration <Double>e = v.elements();

		List<Analog> analogs_Search=new ArrayList<>();

		int counter = 0;

		while (e.hasMoreElements()) {
			double key = e.nextElement();

			if (key<SCmin) break;

			Vector<Instance> instances = ht.get(key);

			for (Instance instance:instances) {
				Analog analog=new Analog();
				analog.ID=instance.getName();		

				analog.sim=key;

				//			if (chemical.getName().equals("CS(C)=O")) {
				//				System.out.println("*\t"+analog.ID+"\t"+analog.sim);
				//			}

				analog.exp=instance.getClassValue();
				analogs_Search.add(analog);
				counter++;
				if (counter >= MaxCount)
					break;

			}
			if (counter >= MaxCount)
				break;

		}

		return analogs_Search;

	}

	

	private static double CalculateCosineCoefficientWekalite(gov.epa.util.wekalite.Instance c1,gov.epa.util.wekalite.Instance c2,double [] Mean,double [] StdDev) {
		
		double []val1=new double[c1.numAttributes()];
		double []val2=new double[c1.numAttributes()];

		for (int i=0;i<c1.numAttributes();i++) {
			val1[i]=c1.value(i);
			val2[i]=c2.value(i);
		}

		return calculateCosineSimilarityCoefficient(val1, val2, Mean, StdDev);

	}
	
	private static double CalculateTanimotoCoefficientWekalite(gov.epa.util.wekalite.Instance c1,gov.epa.util.wekalite.Instance c2,double [] Mean,double [] StdDev) {
		double []val1=new double[c1.numAttributes()];
		double []val2=new double[c1.numAttributes()];

		for (int i=0;i<c1.numAttributes();i++) {
			val1[i]=c1.value(i);
			val2[i]=c2.value(i);
		}
		return calculateTanimotoSimilarityCoefficient(val1, val2, Mean, StdDev);
	}
	


}

