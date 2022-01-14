package gov.epa.endpoints.reports.WebTEST;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.PredictionResults;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportMetadata;
import gov.epa.run_from_java.RunFromJava;
import wekalite.CSVLoader;
import wekalite.Instance;
import wekalite.Instances;

import gov.epa.endpoints.reports.predictions.QsarPredictedValue;


public class GenerateWebTestReport {

	//static variables to cache data set data:
	
	
	private static Hashtable<String,Dataset>htDatasets=new Hashtable<>();
	
//	//training and prediction instances with TEST descriptors:
//	private static Hashtable<String, Instances> ht_instancesTraining = new Hashtable<String, Instances>();
//	private static Hashtable<String, Instances> ht_instancesPrediction = new Hashtable<String, Instances>();
//	
//	//Predicted values from consensus method
//	private static Hashtable<String, Hashtable<String,PredictionReportDataPoint>> ht_datapoints = new Hashtable<>();
//		
//	private static Hashtable<String, PredictionReportMetadata> ht_metadata = new Hashtable<>();
	
	static class Dataset {		
		Instances instancesTraining;
		Instances instancesPrediction;
		Hashtable<String, PredictionReportDataPoint>ht_datapoints;
		PredictionReportMetadata metadata;		
		Double maeTraining;
		double maePrediction;
		
	}
	
	public static Double CalculateMAE(List<PredictionReportDataPoint>dataPoints,int splitNum) {
		try {

			double MAE=0;
			double count=0;
			
			for(PredictionReportDataPoint dataPoint:dataPoints) {
	
				Double exp=dataPoint.experimentalPropertyValue;
				if (exp==null) continue;

				if (dataPoint.qsarPredictedValues==null || dataPoint.qsarPredictedValues.size()==0) continue;

				int splitNumCurrent=dataPoint.qsarPredictedValues.get(0).splitNum;
				if (splitNumCurrent!=splitNum) continue;//not in test set so skip
				
				Double pred=PredictToxicityJSONCreator.calculateConsensusToxicityValue(dataPoint.qsarPredictedValues);				
				if (pred==null) continue;
				
//				System.out.println(exp+"\t"+pred);
				
				MAE+=Math.abs(exp-pred);
				count++;
			}
			MAE/=count;
			return MAE;

		} catch (Exception e) {
			return null;
		}

	}
    
    /**
     * Allows one to cache the data for a dataset so new reports can be generated quickly
     * 
     * @param data
     */
    public static void storeDataSetInCache(PredictionReport data) {
    	
    	String dataSetName=data.predictionReportMetadata.datasetName;
    	String descriptorHeader=data.predictionReportMetadata.descriptorSetHeader;
    	
    	//TODO- right now mass units are figured out using PredictToxicityJSONCreator.getMassUnits()
//    	data.predictionReportMetadata.datasetUnitMass=TESTConstants.getMassUnits(data.predictionReportMetadata.datasetProperty);
    	
    	if (htDatasets.get(dataSetName)!=null) {
    		System.out.println("Already loaded data for "+dataSetName);
    		return;
    	}
    	    	
    	Dataset dataset=new Dataset();    	
    	htDatasets.put(dataSetName, dataset);
    	
    	dataset.instancesTraining=createWekaliteInstances(data, descriptorHeader, 0);
    	dataset.instancesPrediction=createWekaliteInstances(data, descriptorHeader, 1);
    	dataset.maeTraining=CalculateMAE(data.predictionReportDataPoints, 0);
    	dataset.maePrediction=CalculateMAE(data.predictionReportDataPoints, 1);
    	
    	dataset.ht_datapoints=new Hashtable<String,PredictionReportDataPoint>();//lookup predictions by smiles	    	
    	//Store predictions by smiles:
		for (PredictionReportDataPoint dataPoint:data.predictionReportDataPoints) {
			dataset.ht_datapoints.put(dataPoint.canonQsarSmiles, dataPoint);

			//delete descriptor info since already storing in the instances...
			dataPoint.descriptorValues=null;
//			System.out.println(dataPoint.canonQsarSmiles+"\t"+ht.get(dataPoint.canonQsarSmiles).qsarPredictedValues.size());
		}    		
    	
    	dataset.metadata=data.predictionReportMetadata;
    	
    }
//
//	private static Double calculatePredConsensus(PredictionReportDataPoint dataPoint) {
//		Double pred=Double.valueOf(0);
//		int count=0;
//		for (PredictionReportData.PredictionReportDataPoint.QSARPredictedValue predValue:dataPoint.qsarPredictedValues) {
//			if (predValue.qsarPredictedValue==null) continue;
//			pred+=predValue.qsarPredictedValue;
//			count++;
//		}
//
//		if (count>0) return pred/(double)count;
//		else return null;
//	}
//
//
//
//	
//	
	private static Instance createInstance(PredictionReportDataPoint dataPoint, String descriptorHeader, String del) {
		String strInstances="ID"+del+"Property"+del+descriptorHeader+"\r\n";    		
		strInstances+=dataPoint.canonQsarSmiles+"\t"+dataPoint.experimentalPropertyValue+"\t"+dataPoint.descriptorValues+"\r\n";
		Instances instances = instancesFromString(strInstances);		
		return instances.firstInstance();
	}
	
	public static Instances createWekaliteInstances(PredictionReport dataSetData, String descriptorHeader, 
			int splitNum) {

		
		String del=",";
		if (descriptorHeader.contains("\t")) del="\t";
		

//		System.out.println("Creating string instances");
		
		int counter=0;
		

		StringBuilder sb = new StringBuilder();		
		sb.append("ID"+del+"Property"+del+descriptorHeader+"\r\n");    		
		
		for (PredictionReportDataPoint dataPoint:dataSetData.predictionReportDataPoints) {
			counter++;
//			if (counter%100==0) System.out.println(counter);
			int splitNumCurrent=dataPoint.qsarPredictedValues.get(0).splitNum;
			if (splitNumCurrent!=splitNum) continue;//not in test set so skip
			if (dataPoint.canonQsarSmiles==null || dataPoint.descriptorValues==null) continue;//not a good data point so skip			
			if (dataPoint.experimentalPropertyValue==null) continue;//shouldnt happen
			sb.append(dataPoint.canonQsarSmiles+"\t"+dataPoint.experimentalPropertyValue+"\t"+dataPoint.descriptorValues+"\r\n");
		}
		
		String strInstances=sb.toString();
		
		
//		System.out.println("Done Creating string instances");
		
		//				System.out.println(strInstances);

		Instances instances = instancesFromString(strInstances);
		instances.calculateMeans();
		instances.calculateStdDevs();
		return instances;
	}
	
	/**
	 * @param strTSV
	 * @return
	 * @throws IOException
	 */
	public static wekalite.Instances instancesFromString(String strTSV)  {
		try {
			String del=",";
			if (strTSV.contains("\t")) del="\t";

			InputStream inputStream = new ByteArrayInputStream(strTSV.getBytes());

			CSVLoader atf = new CSVLoader();
			Instances dataset=atf.getDatasetFromInputStream(inputStream, del);
			return dataset;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}
	
	
	static void generateReport(String dataSetName, PredictionReport.PredictionReportDataPoint testDataPoint, String outputFilePath) {
		
		try {
			Dataset dataset=htDatasets.get(dataSetName);
			PredictionReportMetadata datasetMetadata=dataset.metadata;
//			System.out.println(datasetMetadata==null);
			
			String descriptorHeader=datasetMetadata.descriptorSetHeader;
			
//			boolean isBinaryEndpoint=TESTConstants.isBinary(endpoint);//Where should code be to check this? Is it stored in db?
//			System.out.println(descriptorHeader);			
						
			Instance evalInstance2d = createInstance(testDataPoint,descriptorHeader,"\t");			
			
			List<Analog>analogsTraining=AnalogFinder.findAnalogsWekalite(evalInstance2d, dataset.instancesTraining, 10, 0.5, true, AnalogFinder.typeSimilarityMeasureCosineSimilarityCoefficient);
			List<Analog>analogsPrediction=AnalogFinder.findAnalogsWekalite(evalInstance2d, dataset.instancesPrediction, 10, 0.5, true, AnalogFinder.typeSimilarityMeasureCosineSimilarityCoefficient);
						
//			System.out.println(analogsTraining.size());
//			System.out.println(analogsPrediction.size());
			
								
			String canonQSARsmiles=testDataPoint.canonQsarSmiles;

			
			//TODO where should look up of experimental value occur??? 
			ExpRecord er=LookupExpValBySmiles(canonQSARsmiles, dataset.ht_datapoints);
									
//			System.out.println(er.expCAS+"\t"+er.expSet+"\t"+er.expToxValue);
			
			// following MW is calculated from QSAR ready smiles-need the MW from the original smiles if was desalted structure...
//			double MW=evalInstance2d.value("MW");
//			double MW_Frag=evalInstance2d.value("MW_Frag");

			
			addPredictionsToAnalogs(analogsTraining, dataset.ht_datapoints);
			addPredictionsToAnalogs(analogsPrediction, dataset.ht_datapoints);

			//Create results as object:
			PredictToxicityJSONCreator jsonCreator=new PredictToxicityJSONCreator();			
			PredictionResults predictionResults = jsonCreator.generatePredictionResultsConsensus(
					er,datasetMetadata,testDataPoint, analogsPrediction, analogsTraining,dataset.maeTraining,dataset.maePrediction);

//			Gson gson = new GsonBuilder().setPrettyPrinting().create();
//	        String json = gson.toJson(predictionResults);
//	        System.out.println(json);

			
	        //Create webpage:
	        PredictToxicityWebPageCreatorFromJSON htmlCreator=new PredictToxicityWebPageCreatorFromJSON();
	        htmlCreator.writeConsensusResultsWebPages(predictionResults, outputFilePath);	        


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static ExpRecord LookupExpValBySmiles(String canonQSARSmiles, Hashtable<String,PredictionReportDataPoint>ht) {
		
		ExpRecord er = new ExpRecord();

		if (ht.get(canonQSARSmiles)!=null) {
			PredictionReportDataPoint dataPoint=ht.get(canonQSARSmiles);			
			er.expToxValue = dataPoint.experimentalPropertyValue;
			er.expCAS = dataPoint.originalCompounds.get(0).casrn;
			er.canonQSARSmiles=canonQSARSmiles;
			
//			System.out.println(er.expCAS);
			
			if (dataPoint.qsarPredictedValues.get(0).splitNum==0) {
				er.expSet = "Training";	
			} else if (dataPoint.qsarPredictedValues.get(0).splitNum==1) {
				er.expSet = "Test";	
			}
		}
		
		return er;

	}

	
	
//
//
//

	private static void addPredictionsToAnalogs(List<Analog> analogs,
			Hashtable<String, PredictionReportDataPoint> htPredsForDataSet) {
		for (Analog analog:analogs) {

			if (htPredsForDataSet.get(analog.ID)==null) {
				//			System.out.println(analog.ID);
				continue;
			}
			PredictionReportDataPoint dataPoint=htPredsForDataSet.get(analog.ID);		
			//		System.out.println(dataPoint.canonQsarSmiles+"\t"+dataPoint.qsarPredictedValues.size());
			analog.dtxcid=dataPoint.originalCompounds.get(0).dtxcid;
			analog.casrn=dataPoint.originalCompounds.get(0).casrn;

			if (dataPoint.qsarPredictedValues==null) continue;
			analog.pred=PredictToxicityJSONCreator.calculateConsensusToxicityValue(dataPoint.qsarPredictedValues);

		}
	}
	
	

	
//	static TESTPredictedValue getTESTPredictedValue(String endpoint, String method, String CAS, Double ExpToxVal,
//			Double PredToxVal, double MW, String error) {
//
//		TESTPredictedValue v = new TESTPredictedValue(CAS, endpoint, method);
//		if (ExpToxVal !=null)

//			v.expValMolarLog = ExpToxVal;
//		if (PredToxVal !=null)
//			v.predValMolarLog = PredToxVal;
//
//		try {
//			Double ExpToxValMass = null;
//			Double PredToxValMass = null;
//			if (TESTConstants.isLogMolar(endpoint)) {
//				if (PredToxVal != null) {
//					PredToxValMass = PredictToxicityJSONCreator.getToxValMass(endpoint, PredToxVal, MW);
//					v.predValMass = PredToxValMass;
//				}
//
//				if (ExpToxVal != null) {
//					ExpToxValMass = PredictToxicityJSONCreator.getToxValMass(endpoint, ExpToxVal, MW);
//					v.expValMass = ExpToxValMass;
//				}
//			} else {
//				PredToxValMass = PredToxVal;
//				v.predValMass = PredToxValMass;
//
//				ExpToxValMass = ExpToxVal;
//				v.expValMass = ExpToxValMass;
//			}
//
//			v.error = error;
//
//		} catch (Exception ex) {
////			logger.catching(ex);
//		}
//
//		return v;
//	}
//	
//	
//	static TESTPredictedValue getTESTPredictedValueBinary(String endpoint, String method, String CAS, double ExpToxVal,
//			double PredToxVal, double MW, String error) {
//		TESTPredictedValue v = new TESTPredictedValue(CAS, endpoint, method);
//		try {
//
//			if (ExpToxVal == -9999) {
//			} else {
//				v.expValMolarLog = ExpToxVal;
//			}
//			if (PredToxVal == -9999) {
//			} else {
//				v.predValMolarLog = PredToxVal;
//			}
//
//			if (ExpToxVal == -9999) {
//			} else {
//				if (ExpToxVal < 0.5) {
//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						v.message = "Developmental NON-toxicant";
//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
//						v.message = "Mutagenicity Negative";
//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						v.message = "Does NOT bind to estrogen receptor";
//					}
//					v.expActive = false;
//				} else {
//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						v.message = "Developmental toxicant";
//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
//						v.message = "Mutagenicity Positive";
//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						v.message = "Binds to estrogen receptor";
//					}
//					v.expActive = true;
//				}
//			}
//
//			if (PredToxVal == -9999) {
//			} else {
//				if (PredToxVal < 0.5) {
//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						v.message = "Developmental NON-toxicant";
//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
//						v.message = "Mutagenicity Negative";
//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						v.message = "Does NOT bind to estrogen receptor";
//					}
//					v.predActive = false;
//				} else {
//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						v.message = "Developmental toxicant";
//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
//						v.message = "Mutagenicity Positive";
//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						v.message = "Binds to estrogen receptor";
//					}
//					v.predActive = true;
//				}
//			}
//
//			// write error:
//			v.error = error;
//
//		} catch (Exception ex) {
////			logger.catching(ex);
//		}
//
//		return v;
//	}
	
//	static TESTPredictedValue getTESTPredictedValue(String endpoint, String method, String CAS, double ExpToxVal,
//			double PredToxVal, double MW, String error, boolean isBinary) {
//		if (!isBinary) {
//			return getTESTPredictedValue(endpoint, method, CAS, ExpToxVal, PredToxVal, MW, error);
//		} else {
//			return getTESTPredictedValueBinary(endpoint, method, CAS, ExpToxVal, PredToxVal, MW, error);
//		}
//	}
	
	
	
//	static Double calculateConsensusToxicity(ArrayList<Double> preds) {
//		Double pred = Double.valueOf(0.0);
//		int predcount = 0;
//		double minPredCount=2;
//
//		for (int i = 0; i < preds.size(); i++) {
//			if (preds.get(i) > -9999) {
//				predcount++;
//				pred += preds.get(i);
//			}
//		}
//
//		if (predcount < minPredCount)
//			return null;
//
//		pred /= (double) predcount;
//		// System.out.println(pred);
//		return pred;
//	}

	
	public static PredictionReport loadDataSetFromJson(String jsonfilepath) {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		try {
			Reader reader = Files.newBufferedReader(Paths.get(jsonfilepath));
			PredictionReport data=gson.fromJson(reader, PredictionReport.class);
			return data;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void main(String[] args) {

		String descriptorSet="T.E.S.T. 5.1";
		
		String sampleSource="OPERA";
//		String endpoint=DevQsarConstants.MELTING_POINT;//done
//		String endpoint=DevQsarConstants.LOG_BCF;//done
//		String endpoint=DevQsarConstants.LOG_HALF_LIFE;//done
//		String endpoint=DevQsarConstants.HENRYS_LAW_CONSTANT;//done
//		String endpoint=DevQsarConstants.VAPOR_PRESSURE;//done
//		String endpoint=DevQsarConstants.WATER_SOLUBILITY;//done
//		String endpoint=DevQsarConstants.MELTING_POINT;//done
//		String endpoint=DevQsarConstants.LOG_KM_HL;//done
//		String endpoint=DevQsarConstants.LOG_KOA;//done
//		String endpoint=DevQsarConstants.LOG_KOC;//done
//		String endpoint=DevQsarConstants.LOG_OH;//done
		String endpoint=DevQsarConstants.BOILING_POINT;//done
		
//		String sampleSource="TEST";
//		String endpoint=DevQsarConstants.LC50;//done
//		String endpoint=DevQsarConstants.LC50DM//done;
//		String endpoint=DevQsarConstants.LD50;//done
//		String endpoint=DevQsarConstants.IGC50;//done
//		String endpoint=DevQsarConstants.DEV_TOX;//done
//		String endpoint=DevQsarConstants.MUTAGENICITY;
//		String endpoint=DevQsarConstants.LLNA;
		
		String datasetName = endpoint +" "+sampleSource;
		System.out.println("Generating report for "+datasetName);
		
		//Create report as json file by querying the postgres db: (takes time- should use storeDataSetData to cache it)
		RunFromJava.reportAllPredictions(datasetName, descriptorSet);
		
		//Load report from json file:
		String filepath="reports/"+datasetName+"_"+descriptorSet+"_PredictionReport.json";		
		PredictionReport data=loadDataSetFromJson(filepath);	
		
		
		//Use first data point as predicted value to simulate a prediction run:
		PredictionReport data2=loadDataSetFromJson(filepath);//load again so can have basically have clone of first data point
		PredictionReportDataPoint testDataPoint=(PredictionReportDataPoint) data2.predictionReportDataPoints.get(0);		
		
		System.out.print("Storing report data in cache...");
		storeDataSetInCache(data);//store dataset info in cache
//		System.out.println("number of data points in json file="+data.predictionReportDataPoints.size());
		System.out.print("done\n");
	

		String outputPath="reports/"+data.predictionReportMetadata.datasetName+".html";
		generateReport(data.predictionReportMetadata.datasetName, testDataPoint,outputPath);

	}

}
