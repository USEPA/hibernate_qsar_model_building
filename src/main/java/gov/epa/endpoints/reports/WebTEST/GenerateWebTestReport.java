package gov.epa.endpoints.reports.WebTEST;

import java.io.BufferedWriter;
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
    public static boolean storeDataSetInCache(PredictionReport predictionReport,String modelSetName,ModelData md) {
    	
    	String dataSetName=predictionReport.predictionReportMetadata.datasetName;
//    	String descriptorHeader=predictionReport.predictionReportMetadata.descriptorSetHeader;
    	
    	//TODO- right now mass units are figured out using PredictToxicityJSONCreator.getMassUnits()
//    	data.predictionReportMetadata.datasetUnitMass=TESTConstants.getMassUnits(data.predictionReportMetadata.datasetProperty);
    	
    	if (htDatasets.get(dataSetName)!=null) {
    		System.out.println("Already loaded data for "+dataSetName);
    		return true;
    	}
    	    	
    	Dataset dataset=new Dataset();    	
    	htDatasets.put(dataSetName, dataset);
    	
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
    	return true;
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
//			PredictionReportMetadata datasetMetadata=dataset.metadata;
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
			
			if (er.expToxValue!=null) {
				testDataPoint.experimentalPropertyValue=er.expToxValue;//just in case store it
//				System.out.println(testDataPoint.experimentalPropertyValue);
			}

			//Create results as object:
			PredictToxicityJSONCreator jsonCreator=new PredictToxicityJSONCreator();			
			PredictionResults predictionResults = jsonCreator.generatePredictionResultsConsensus(er,testDataPoint, dataset);
			
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
	
	
	public static ModelData loadModelDataFromJson(String jsonfilepath) {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		try {
			Reader reader = Files.newBufferedReader(Paths.get(jsonfilepath));
			ModelData md=gson.fromJson(reader, ModelData.class);
			return md;

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
//		String endpoint=DevQsarConstants.LC50;//done
		String endpoint=DevQsarConstants.LC50DM;//done;
//		String endpoint=DevQsarConstants.LD50;//done
//		String endpoint=DevQsarConstants.IGC50;//done
//		String endpoint=DevQsarConstants.DEV_TOX;//done
//		String endpoint=DevQsarConstants.MUTAGENICITY;
//		String endpoint=DevQsarConstants.LLNA;//need to delete some models!

		String splittingName=sampleSource;
		String datasetName = endpoint +" "+sampleSource;
		
		//***************************************************************
//		datasetName="LLNA from exp_prop, without eChemPortal";
		datasetName="Standard Henry's law constant from exp_prop";
		modelSetName="WebTEST2.0";
		splittingName="RND_REPRESENTATIVE";
		//***************************************************************
		
		generateWebPageReport(modelSetName, splittingName, datasetName);

	}

	/**
	 * Generate webpage report for test chemical
	 * 
	 * @param modelSetName
	 * @param splittingName
	 * @param datasetName
	 */
	private static void generateWebPageReport(String modelSetName, String splittingName, String datasetName) {

		//Get prediction report from database:
		PredictionReport predictionReport = getPredictionReport(modelSetName, splittingName, datasetName);
		if(predictionReport==null) return;
		
		//Get training and test set instances as strings from database:
		ModelData md = getModelData(modelSetName, splittingName, datasetName);
		if(md==null) return;
		
		System.out.println("Start storing report data in cache...");
		boolean cached=storeDataSetInCache(predictionReport,modelSetName,md);//store dataset info in cache
//		System.out.println("number of data points in json file="+data.predictionReportDataPoints.size());
		
		if (!cached) {
			System.out.println("Couldnt cache data");
			return;
		}
		
		System.out.println("Done storing report data in cache");	
		Dataset dataset=htDatasets.get(datasetName);		
//		dataset.instancesTraining.writeToCSVFile("bob.txt");
				
		DataPoint dp=new DataPoint();
		PredictionReportDataPoint prdp=new PredictionReportDataPoint(dp);
		
		storeTestChemicalQSARReadySmiles(dataset, prdp);
		storeTestChemicalDescriptors(dataset, prdp);		
		storeTestChemicalInfo(predictionReport,prdp);
		storeTestChemicalPredictions(predictionReport, prdp);
				
		String outputPath="data/reports/"+predictionReport.predictionReportMetadata.datasetName+".html";
		generateReport(datasetName, prdp,outputPath);
	}

	private static void storeTestChemicalQSARReadySmiles(Dataset dataset, PredictionReportDataPoint dp) {
		//**TODO smiles get from QSAR ready api rather than from dataset object:		
		dp.canonQsarSmiles=dataset.instancesPrediction.instance(0).getName();//for testing purposes just use first instance of test set
	}

	private static void storeTestChemicalDescriptors(Dataset dataset, PredictionReportDataPoint prdp) {
		//***TODO descriptorValues needs to come from descriptors API instead:
		prdp.descriptorValues=dataset.instancesPrediction.instance(0).getDescriptorsValues();
	}

	private static void storeTestChemicalPredictions(PredictionReport predictionReport, 
			PredictionReportDataPoint prdp) {
		//***TODO predictions should come from webservices:
//		QsarPredictedValue qpv=new QsarPredictedValue("svm_regressor_1.1",2.21,1);		
//		prdp.qsarPredictedValues.add(qpv);
//		qpv=new QsarPredictedValue("rf_regressor_1.1",2.05,1);
//		prdp.qsarPredictedValues.add(qpv);
//		qpv=new QsarPredictedValue("xgb_regressor_1.0",1.81,1);
//		prdp.qsarPredictedValues.add(qpv);
//		qpv=new QsarPredictedValue("consensus",2,1);
//		prdp.qsarPredictedValues.add(qpv);

		//For now just use old predictions stored in prediction report:
		for (PredictionReportDataPoint predictionReportDataPoint:predictionReport.predictionReportDataPoints) {
			if (predictionReportDataPoint.canonQsarSmiles.equals(prdp.canonQsarSmiles)) {
				OriginalCompound oc=predictionReportDataPoint.originalCompounds.get(0);
				prdp.qsarPredictedValues=predictionReportDataPoint.qsarPredictedValues;		
			}
		}
	}

	private static void storeTestChemicalInfo(PredictionReport predictionReport, PredictionReportDataPoint prdp) {
		//***TODO retrieve from user interface (need to store dtxcid, casrn, referredName,smiles,molWeight in oc:

//		String dtxcid="DTXCID401735";//need for structure image
//		String casrn="67-68-5";//cas is displayed in report
//		String referredName="Dimethyl sulfoxide";//not used in report yet
//		String smiles=dp.getCanonQsarSmiles();//not used in report yet
//		double molWeight=78.11;//needed to convert units in report		
//		OriginalCompound oc=new OriginalCompound(dtxcid, casrn, referredName, smiles, molWeight);
//		prdp.originalCompounds.add(oc);		

		//For now use values stored in the prediction report for oc 
		for (PredictionReportDataPoint predictionReportDataPoint:predictionReport.predictionReportDataPoints) {
			if (predictionReportDataPoint.canonQsarSmiles.equals(prdp.canonQsarSmiles)) {
				OriginalCompound oc=predictionReportDataPoint.originalCompounds.get(0);
				prdp.originalCompounds.add(oc);		
			}
		}
	}

	private static ModelData getModelData(String modelSetName, String splittingName, String datasetName) {
		String filepathModelData="data/reports/"+datasetName+"_ModelData.json";
		ModelData md=null;//Has instances using training and test sets using TEST descriptors
		if (new File(filepathModelData).exists()) {
			//Load report from json file:
			md=loadModelDataFromJson(filepathModelData);
		} else {			
	    	md=getModelData(modelSetName, datasetName, splittingName,filepathModelData);
		}
		return md;
	}

	private static PredictionReport getPredictionReport(String modelSetName, String splittingName, String datasetName) {
		PredictionReport predictionReport=null;
		String filepathReport="data/reports/"+datasetName+"_PredictionReport.json";		
		if (new File(filepathReport).exists()) {
			//Load report from json file:
			predictionReport=loadDataSetFromJson(filepathReport);
		} else {
			//Create report as json file by querying the postgres db: (takes time- should use storeDataSetData to cache it)
			predictionReport=ReportGenerationScript.reportAllPredictions(datasetName, splittingName,modelSetName,true);
		}
		
		if(predictionReport==null) {
			System.out.println("Couldnt load prediction report");
		}
		
		return predictionReport;
	}

	private static ModelData getModelData(String modelSetName, String datasetName,String splittingName,String filePathJson) {
		ModelData md;
		ModelBuilder mb=new ModelBuilder("tmarti02");
		
		//Use TEST descriptors to find analogs from training and test sets:
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_TEST;    	
		if (!modelSetName.equals("Sample models") && !modelSetName.equals("WebTEST2.0")) {
			descriptorSetName="WebTEST-default";
		}
		
		//Get training and test set instances as strings using TEST descriptors:
		md=ModelData.initModelData(datasetName, descriptorSetName,splittingName, false,false);
		
		if (md==null) {
    		System.out.println("couldnt load training and test set for ");
        	System.out.println("dataSetName="+datasetName);
        	System.out.println("descriptorSetName="+descriptorSetName);
        	System.out.println("splittingName="+splittingName);    	    		
    		return null;
    	}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePathJson))) {
			writer.write(gson.toJson(md));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return md;
	}

}
