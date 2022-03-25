package gov.epa.endpoints.reports.WebTEST;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.reports.OriginalCompound;
import gov.epa.endpoints.reports.WebTEST.ApplicabilityDomain.AD_simNN_All_descriptors;
import gov.epa.endpoints.reports.WebTEST.ApplicabilityDomain.AnalogFinder;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.PredictionResults;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelMetadata;
import gov.epa.endpoints.reports.predictions.QsarPredictedValue;
import gov.epa.run_from_java.scripts.ReportGenerationScript;
import gov.epa.util.wekalite.Instances;


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
		List<PredictionReportModelMetadata> predictionReportModelMetadata;
		Double maeTraining;
		double maePrediction;
		public double scFracTraining;
		public String simMeasure;
		public double fracTrainingForAD;
		
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
     * @param predictionReport
     */
    public static void storeDataSetInCache(PredictionReport predictionReport,String modelSetName) {
    	
    	String dataSetName=predictionReport.predictionReportMetadata.datasetName;
//    	String descriptorHeader=predictionReport.predictionReportMetadata.descriptorSetHeader;
    	
    	//TODO- right now mass units are figured out using PredictToxicityJSONCreator.getMassUnits()
//    	data.predictionReportMetadata.datasetUnitMass=TESTConstants.getMassUnits(data.predictionReportMetadata.datasetProperty);
    	
    	if (htDatasets.get(dataSetName)!=null) {
    		System.out.println("Already loaded data for "+dataSetName);
    		return;
    	}
    	    	
    	Dataset dataset=new Dataset();    	
    	htDatasets.put(dataSetName, dataset);
    	
    	ModelBuilder mb=new ModelBuilder("tmarti02");
    	
    	//Use TEST descriptors to find analogs from training and test sets:
    	String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_TEST;    	
    	if (!modelSetName.equals("Sample models") && !modelSetName.equals("WebTEST2.0")) {
    		descriptorSetName="WebTEST-default";
    	}
    	
    	//Get training and test set instances as strings using TEST descriptors:
    	ModelData md=mb.initModelData(dataSetName, descriptorSetName,predictionReport.predictionReportMetadata.splittingName, false);
    	
//    	System.out.println(md.predictionSetInstances);
    	
    	dataset.instancesTraining=createWekaliteInstances(md.trainingSetInstances);
    	dataset.instancesPrediction=createWekaliteInstances(md.predictionSetInstances);
    	
    	dataset.maeTraining=CalculateMAE(predictionReport.predictionReportDataPoints, 0);
    	dataset.maePrediction=CalculateMAE(predictionReport.predictionReportDataPoints, 1);
    	
    	dataset.ht_datapoints=new Hashtable<String,PredictionReportDataPoint>();//lookup predictions by smiles	    	
    	//Store predictions by smiles:
		for (PredictionReportDataPoint dataPoint:predictionReport.predictionReportDataPoints) {
			dataset.ht_datapoints.put(dataPoint.canonQsarSmiles, dataPoint);

			//delete descriptor info since already storing in the instances...
			dataPoint.descriptorValues=null;
//			System.out.println(dataPoint.canonQsarSmiles+"\t"+ht.get(dataPoint.canonQsarSmiles).qsarPredictedValues.size());
		}    		
    	
    	dataset.metadata=predictionReport.predictionReportMetadata;
    	dataset.predictionReportModelMetadata=predictionReport.predictionReportModelMetadata;


		//Data for Applicability domain
		dataset.simMeasure=AnalogFinder.typeSimilarityMeasureCosineSimilarityCoefficient;
		dataset.fracTrainingForAD=0.95;
    	dataset.scFracTraining=AD_simNN_All_descriptors.calculateAvgSC_trainingSet(3, dataset.instancesTraining,dataset.simMeasure,dataset.fracTrainingForAD);
//    	System.out.println("scFracTraining="+dataset.scFracTraining);
    	
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
	
	public static Instances createWekaliteInstances(PredictionReport dataSetData, String descriptorHeader, 
			int splitNum) {

		
		String del=",";
		if (descriptorHeader.contains("\t")) del="\t";
		

//		System.out.println("Creating string instances");
		
		int counter=0;
		

		StringBuilder sb = new StringBuilder();	//use StringBuilder to create tsv since a lot faster than merging strings from each line	
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

		Instances instances = Instances.instancesFromString(strInstances);
		instances.calculateMeans();
		instances.calculateStdDevs();
		return instances;
	}
	
	
	public static Instances createWekaliteInstances(String strInstances) {
		Instances instances = Instances.instancesFromString(strInstances);
		instances.calculateMeans();
		instances.calculateStdDevs();
		return instances;
	}
	
		
	
	static void generateReport(String dataSetName, PredictionReportDataPoint testDataPoint, String outputFilePath) {
		
		try {
			Dataset dataset=htDatasets.get(dataSetName);
			PredictionReportMetadata datasetMetadata=dataset.metadata;
//			System.out.println(datasetMetadata==null);
			
//			String descriptorHeader=datasetMetadata.descriptorSetHeader;
			
//			boolean isBinaryEndpoint=TESTConstants.isBinary(endpoint);//Where should code be to check this? Is it stored in db?
//			System.out.println(descriptorHeader);			
						
			
//			System.out.println(analogsTraining.size());
//			System.out.println(analogsPrediction.size());			
								
			String canonQSARsmiles=testDataPoint.canonQsarSmiles;
		
			//TODO where should look up of experimental value occur???  Is this ok?
			ExpRecord er=LookupExpValBySmiles(canonQSARsmiles, dataset.ht_datapoints);
									
//			System.out.println(er.expCAS+"\t"+er.expSet+"\t"+er.expToxValue);
			
			// following MW is calculated from QSAR ready smiles-need the MW from the original smiles if was desalted structure...
//			double MW=evalInstance2d.value("MW");
//			double MW_Frag=evalInstance2d.value("MW_Frag");


			//Create results as object:
			PredictToxicityJSONCreator jsonCreator=new PredictToxicityJSONCreator();			
			PredictionResults predictionResults = jsonCreator.generatePredictionResultsConsensus(er,testDataPoint, dataset);

//			Gson gson = new GsonBuilder().setPrettyPrinting().create();
//	        String json = gson.toJson(predictionResults);
//	        System.out.println(json);
			
	        //Create webpage from results object:
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

	
	public static PredictionReport loadDataSetFromJson(String jsonfilepath) {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		try {
			Reader reader = Files.newBufferedReader(Paths.get(jsonfilepath));
			PredictionReport predictionReport=gson.fromJson(reader, PredictionReport.class);
			return predictionReport;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void main(String[] args) {

		String modelSetName="Sample models";
		
//		String sampleSource="OPERA";
//		String endpoint=DevQsarConstants.MELTING_POINT;//
//		String endpoint=DevQsarConstants.LOG_HALF_LIFE;//
//		String endpoint=DevQsarConstants.HENRYS_LAW_CONSTANT;//
//		String endpoint=DevQsarConstants.VAPOR_PRESSURE;//
//		String endpoint=DevQsarConstants.MELTING_POINT;//
//		String endpoint=DevQsarConstants.BOILING_POINT;//
//		String endpoint=DevQsarConstants.LOG_KOW;//
//		String endpoint=DevQsarConstants.LOG_KOC;//done
//		String endpoint=DevQsarConstants.LOG_KOA;//done
//		String endpoint=DevQsarConstants.WATER_SOLUBILITY;//done
//		String endpoint=DevQsarConstants.LOG_BCF;//done
//		String endpoint=DevQsarConstants.LOG_KM_HL;//done
//		String endpoint=DevQsarConstants.LOG_OH;//done

		
		String sampleSource="TEST";
		String endpoint=DevQsarConstants.LC50;//done
//		String endpoint=DevQsarConstants.LC50DM;//done;
//		String endpoint=DevQsarConstants.LD50;//done
//		String endpoint=DevQsarConstants.IGC50;//done
//		String endpoint=DevQsarConstants.DEV_TOX;//done
//		String endpoint=DevQsarConstants.MUTAGENICITY;
//		String endpoint=DevQsarConstants.LLNA;//need to delete some models!

		String splittingName=sampleSource;
		String datasetName = endpoint +" "+sampleSource;
		System.out.println("Generating report for "+datasetName);
		
		PredictionReport predictionReport=null;
		String filepathReport="data/reports/"+datasetName+"_PredictionReport.json";		

		
		if (new File(filepathReport).exists()) {
			//Load report from json file:
			predictionReport=loadDataSetFromJson(filepathReport);
		} else {
			//Create report as json file by querying the postgres db: (takes time- should use storeDataSetData to cache it)
			predictionReport=ReportGenerationScript.reportAllPredictions(datasetName, splittingName,modelSetName,true);

		}
		
			
		System.out.print("Storing report data in cache...");
		storeDataSetInCache(predictionReport,modelSetName);//store dataset info in cache
//		System.out.println("number of data points in json file="+data.predictionReportDataPoints.size());
		System.out.print("done\n");
	
		Dataset dataset=htDatasets.get(datasetName);
		
//		dataset.instancesTraining.writeToCSVFile("bob.txt");
				
		DataPoint dp=new DataPoint();
		
		//**TODO smiles get from QSAR ready api:		
		dp.setCanonQsarSmiles(dataset.instancesPrediction.instance(0).getName());//for testing purposes just use first instance of test set
		
		PredictionReportDataPoint prdp=new PredictionReportDataPoint(dp);
		
		//***TODO descriptorValues needs to come from descriptors API instead:
		prdp.descriptorValues=dataset.instancesPrediction.instance(0).getDescriptorsValues();
		
		//***TODO retrieve from user interface:
		String dtxcid="DTXCID401735";
		String casrn="67-68-5";
		String referredName="Dimethyl sulfoxide";
		String smiles="CS(C)=O";
		double molWeight=78.11;
		
		OriginalCompound oc=new OriginalCompound(dtxcid, casrn, referredName, smiles, molWeight);
		prdp.originalCompounds.add(oc);

		
		//***TODO predictions should come from webservices:
		QsarPredictedValue qpv=new QsarPredictedValue("svm_regressor_1.1",2.21,1);		
		prdp.qsarPredictedValues.add(qpv);
				
		qpv=new QsarPredictedValue("rf_regressor_1.1",2.05,1);
		prdp.qsarPredictedValues.add(qpv);
		
		qpv=new QsarPredictedValue("xgb_regressor_1.0",1.81,1);
		prdp.qsarPredictedValues.add(qpv);
		
		//Note: consensus prediction calculated in the report code
				
		String outputPath="data/reports/"+predictionReport.predictionReportMetadata.datasetName+".html";
		generateReport(datasetName, prdp,outputPath);

	}

}
