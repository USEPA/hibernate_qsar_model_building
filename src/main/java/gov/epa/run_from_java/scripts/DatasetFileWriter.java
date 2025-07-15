package gov.epa.run_from_java.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.json.CDL;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.splittings.Splitter;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.web_services.SplittingWebService;

public class DatasetFileWriter {
	
	private DatasetService datasetService = new DatasetServiceImpl();
	private DataPointService dataPointService = new DataPointServiceImpl();
	private DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
	
	public String writeWithoutSplitting(String datasetName, String descriptorSetName, String outputFolderPath, boolean fetchDtxcids) {
//		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
//		List<DescriptorValues> descriptorValues = descriptorValuesService.findByDescriptorSetName(descriptorSetName);
		
		String outputFileName = datasetName + "_" + descriptorSetName + "_full.tsv";
		String outputFilePath = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileName;
		File outputFile = new File(outputFilePath);
		outputFile.getParentFile().mkdirs();
		
//		try {
//			Gson gson=new Gson();
//			String json=gson.toJson(dataPoints);
//			String jsonFilePath= outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileName.replace("tsv", ".json");
//			FileWriter fw=new FileWriter(jsonFilePath);
//			fw.write(json);
//			fw.flush();
//			fw.close();
//		} catch(Exception ex) {
//			ex.printStackTrace();
//		}
		
		String instances = ModelData.generateInstancesWithoutSplitting(datasetName,descriptorSetName,fetchDtxcids);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
			bw.write(instances);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instances;
	}
	
	public static void writeWithSplitting(String descriptorSetName,String splittingName,String datasetName,String outputFolderPath, boolean omitBadColumns,boolean useDTXCIDs) {

		//TODO need to add code to figure out which columns have bad columns in either set

		
		String outputFileNameTraining = datasetName + "_" + descriptorSetName+"_"+splittingName + "_training.tsv";
		String outputFilePathTraining = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNameTraining;
		
		String outputFileNamePrediction = datasetName + "_" + descriptorSetName+"_"+splittingName + "_prediction.tsv";
		String outputFilePathPrediction = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNamePrediction;


		ModelBuilder mb=new ModelBuilder("tmarti02");
				
		//Get training and test set instances as strings using TEST descriptors:
		ModelData md=ModelData.initModelData(datasetName, descriptorSetName,splittingName, false,useDTXCIDs);

		System.out.println(datasetName+"\t"+descriptorSetName+"\t"+md.countTraining+"\t"+md.countPrediction);

		File fileTraining=new File(outputFilePathTraining);
		if(fileTraining.exists()) {
//			System.out.println(outputFilePathTraining+" exists");
			return;
		}

		
		File outputFolder = new File(outputFolderPath);
		outputFolder.mkdirs();


		try {
			
			FileWriter fw=new FileWriter(outputFilePathTraining);
			
			
//			System.out.println(outputFilePathTraining);
			
			fw.write(md.trainingSetInstances);
			fw.flush();
			fw.close();
			
			fw=new FileWriter(outputFilePathPrediction);
			fw.write(md.predictionSetInstances);
			fw.flush();
			fw.close();

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		

		
	}
	
	public void writeTrainingSetNotInOperaTestSet(String descriptorSetName,String splittingName,String datasetName,String outputFolderPath) {

		ModelData md=new ModelData(datasetName,descriptorSetName,null,false,false);
				
		//Get training and test set instances as strings using TEST descriptors:
		md.generateInstancesNotinOperaPredictionSet();

		File outputFolder = new File(outputFolderPath);
		outputFolder.mkdirs();

		String outputFileNameTraining = datasetName + "_" + descriptorSetName+ "_NOT_IN_OPERA_PREDICTION_SET_training.tsv";
		String outputFilePathTraining = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNameTraining;

		String outputFileNamePrediction = datasetName + "_" + descriptorSetName+ "_OPERA_prediction.tsv";
		String outputFilePathPrediction = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNamePrediction;
		
		try {
			FileWriter fw=new FileWriter(outputFilePathTraining);
			fw.write(md.trainingSetInstances);
			fw.flush();
			fw.close();
			fw=new FileWriter(outputFilePathPrediction);
			fw.write(md.predictionSetInstances);
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
//	public void writeWithSplitting2(String descriptorSetName,String splittingName,String datasetName,String outputFolderPath,boolean removeNullCols,String lanId) {
//		ModelBuilder mb=new ModelBuilder(lanId);
//				
//		//Get training and test set instances as strings using TEST descriptors:
//		ModelData md=mb.initModelData(datasetName, descriptorSetName,splittingName, false);
//
//		File outputFolder = new File(outputFolderPath);
//		outputFolder.mkdirs();
//
//		String outputFileNameTraining = datasetName + " " + descriptorSetName+" training.tsv";
//		String outputFilePathTraining = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNameTraining;
//		
//		String outputFileNamePrediction = datasetName + " " + descriptorSetName+" prediction.tsv";
//		String outputFilePathPrediction = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNamePrediction;
//
//		try {
//			
//			FileWriter fw=new FileWriter(outputFilePathTraining);
//			
//			if (removeNullCols) {
//				md.trainingSetInstances=removeNulls(md.trainingSetInstances);
//				md.predictionSetInstances=removeNulls(md.predictionSetInstances);
//			}
//			
//			
//			
//			fw.write(md.trainingSetInstances);
//			fw.flush();
//			fw.close();
//			
//			fw=new FileWriter(outputFilePathPrediction);
//			fw.write(md.predictionSetInstances);
//			fw.flush();
//			fw.close();
//
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		
//
//		
//	}
	private String removeNulls(String strDataset) {
		//TODO
		return strDataset;
		
	}
	

	
	public String writeWithoutSplitting(Long datasetId, String descriptorSetName, String outputFolderPath, boolean fetchDtxcids) {
		Dataset dataset = datasetService.findById(datasetId);
		if (dataset==null) {
			return datasetId+" not found";
		}
		
		return writeWithoutSplitting(dataset.getName(), descriptorSetName, outputFolderPath, fetchDtxcids);
	}
	
	public void writeQSARSmilesCID(Long datasetId, String outputFolderPath) {
		Dataset dataset = datasetService.findById(datasetId);
		
		if (dataset==null) {
			return;
		}
		
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(dataset.getName());
		
		String outputFileName = dataset.getName()+ "_SmilesDTXCID.tsv";
		String outputFilePath = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileName;
		File outputFile = new File(outputFilePath);
		outputFile.getParentFile().mkdirs();
		
		String instances = generateSmilesDTXCID(dataPoints);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
			bw.write(instances);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public static String generateSmilesDTXCID(List<DataPoint> dataPoints) {
		Map<String, DataPoint> dataPointsMap = dataPoints.stream().collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));
		
		CompoundService compoundService = new CompoundServiceImpl();
		
		String instanceHeader="QSARSmiles\tDTXCID\tPropertyValue\r\n";
		StringBuilder sbOverall = new StringBuilder(instanceHeader);
		
		for (String smiles:dataPointsMap.keySet()) {
			DataPoint dp = dataPointsMap.get(smiles);
			
			if (dp!=null && !dp.getOutlier()) {
				
				List<Compound> compounds = compoundService.findByCanonQsarSmiles(smiles);
				if (compounds!=null) {
					
					for (Compound compound:compounds) {
						String instance = smiles+"\t"+compound.getDtxcid()+"\t"+dp.getQsarPropertyValue()+"\r\n";
						sbOverall.append(instance);
					}
				}
				
			}
		}
		
		return sbOverall.toString();
	}

	void writeOPERAFiles() {
		String lanId="tmarti02";
		
//		String descriptorSetName="ToxPrints-default";
//		String descriptorSetName="RDKit-default";
//		String descriptorSetName="WebTEST-default";
		String descriptorSetName="PaDEL-default";
//		String descriptorSetName="Padelpy webservice single";
		
//		"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default"
		
		String descriptorSetName2="T.E.S.T. 5.1";
		
		String splittingName="OPERA";
		
		String[] OPERA_ENDPOINTS = { DevQsarConstants.LOG_KOA, DevQsarConstants.LOG_KM_HL,
				DevQsarConstants.HENRYS_LAW_CONSTANT, DevQsarConstants.LOG_BCF, DevQsarConstants.LOG_OH,
				DevQsarConstants.LOG_KOC, DevQsarConstants.VAPOR_PRESSURE, DevQsarConstants.WATER_SOLUBILITY,
				DevQsarConstants.BOILING_POINT, DevQsarConstants.MELTING_POINT, DevQsarConstants.LOG_KOW };

		String server="https://ccte-cced.epa.gov/";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
		for (String endpoint:OPERA_ENDPOINTS) {			
			
			String datasetName=endpoint+" OPERA";

			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\QSAR_Model_Building\\data\\datasets_benchmark\\"+datasetName+"\\";
			
			calc.calculateDescriptors_useSqlToExcludeExisting(datasetName,  descriptorSetName, true,1);

			writeWithSplitting(descriptorSetName, splittingName, datasetName, folder,true ,false);
			
//			if(true)break;
			
//			String training_file_name = endpoint + " OPERA " + descriptorSetName + " training.tsv";
//			String training_file_name2 = endpoint + " OPERA " + descriptorSetName2 + " training.tsv";
//
//			try {
//				List<String> lines= Files.readAllLines(Paths.get(folder+training_file_name));
//				List<String> lines2= Files.readAllLines(Paths.get(folder+training_file_name2));
//				System.out.println(endpoint+"\t"+lines.size()+"\t"+lines2.size());
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			
		}
	}
	
	void writeTEST_Toxicity_Files() {
		String lanId="tmarti02";

//		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="ToxPrints-default";
//		String descriptorSetName="RDKit-default";
//		String descriptorSetName="Padelpy webservice single";
		String descriptorSetName="PaDEL-default";
//		
//		"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default"
		
		String splittingName="TEST";
		
		String[] TEST_ENDPOINTS = { DevQsarConstants.LC50DM, DevQsarConstants.LC50, DevQsarConstants.IGC50,
				DevQsarConstants.LD50, DevQsarConstants.MUTAGENICITY, DevQsarConstants.LLNA};

		
		String server="https://ccte-cced.epa.gov/";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\QSAR_Model_Building\\data\\datasets_benchmark_TEST\\";
		
		for (String endpoint:TEST_ENDPOINTS) {			
			
			String datasetName=endpoint+" TEST";
			String folder=mainFolder+datasetName+"\\";
			calc.calculateDescriptors_useSqlToExcludeExisting(datasetName,  descriptorSetName, true,1);
			writeWithSplitting(descriptorSetName, splittingName, datasetName, folder,true, false);			
		}
	}

	private void writeBatch() {
		
		String server="https://ccte-cced.epa.gov/";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		String splittingName="RND_REPRESENTATIVE";
		String folderMain="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\pf_python_modelbuilding\\datasets\\";

		List<String>datasetNames=new ArrayList<>();
		
//		datasetNames.add("exp_prop_96HR_FHM_LC50_v5 modeling");
//		datasetNames.add("exp_prop_96HR_RT_LC50_v5 modeling");
//		datasetNames.add("exp_prop_96HR_BG_LC50_v5 modeling");
//		datasetNames.add("exp_prop_48HR_DM_LC50_v5 modeling");

		datasetNames.add("exp_prop_RBIODEG_RIFM_BY_CAS");
		
		List<String>descriptorSetNames=new ArrayList<>();

		descriptorSetNames.add(DevQsarConstants.DESCRIPTOR_SET_WEBTEST);
//		descriptorSetNames.add("PaDEL-default");
//		descriptorSetNames.add("Mordred-default");
//		descriptorSetNames.add("ToxPrints-default");
//		descriptorSetNames.add("RDKit-default");

		
		for (String datasetName:datasetNames) {
			for (String descriptorSetName:descriptorSetNames) {
				String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\modeling services\\pf_python_modelbuilding\\datasets_exp_prop\\"+datasetName;
//				String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\0000 biodegradation OPPT\\biodegradation\\RIFM\\datasets\\unvetted with splitting";
				writeWithSplitting(descriptorSetName, splittingName, datasetName, folder,true,false);
				
			}
		}
	}
	
	
	void writeExternalSet() {

		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		String splittingName="RND_REPRESENTATIVE";
		String predSet=ModelData.getExternalPredictionSet(533L, 532L, descriptorSetName, splittingName,true);
		
		String datasetName="exp_prop_RBIODEG_RIFM_BY_CAS";
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\modeling services\\pf_python_modelbuilding\\datasets_exp_prop\\"+datasetName;
		
		
		try {
			
			FileWriter fw=new FileWriter(folder+File.separator+"external set.tsv");
			fw.write(predSet);
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	
	private void write_exp_prop_datasets() {
		String lanId = "tmarti02";
		
		String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		List<String>datasetNames=new ArrayList<>();
//		
		datasetNames.add("HLC v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("MP v1 modeling");

		datasetNames.add("LogP v1 modeling");
		datasetNames.add("WS v1 modeling");
		
//		datasetNames.add("ExpProp BCF Fish_TMM");
//		datasetNames.add("HLC from exp_prop and chemprop");
		//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop");
		
//		String server="https://ccte-cced.epa.gov/";
//		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");

		String splittingName="RND_REPRESENTATIVE";
//		String folderMain="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\pf_python_modelbuilding\\datasets\\";
//		String folderMain="C:\\Users\\lbatts\\OneDrive - Environmental Protection Agency (EPA)\\0 Python\\pf_python_modelbuilding\\datasets_exp_prop\\";

		

		for (String datasetName:datasetNames) {
			System.out.println("writing dataset tsvs for "+datasetName);

//			String folder=folderMain+datasetName+"\\";
			
//			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\modeling services\\pf_python_modelbuilding\\datasets_exp_prop\\"+datasetName;
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\modeling services\\pf_python_modelbuilding\\datasets_v1_modeling\\"+datasetName;

			
			//Just in case run descriptor generation to make sure have descriptor for each datapoint:
//			calc.calculateDescriptors_useSqlToExcludeExisting(datasetName,  descriptorSetName, true,1);

//			writeWithSplitting(descriptorSetName, splittingName, datasetName, folder,true);
//			writeTrainingSetNotInOperaTestSet(descriptorSetName, splittingName, datasetName, folder);
//			writeTestSetNotInOperaTrainingSet(descriptorSetName, splittingName, datasetName, folder);

			writeWithSplitting(descriptorSetName, splittingName, datasetName, folder,true,false);
//			writeTrainingSetNotInOperaTestSet(descriptorSetName, splittingName, datasetName, folder);
			
		}
	}
	
	

	
	
	private void writeTestSetNotInOperaTrainingSet(String descriptorSetName, String splittingName, String datasetName,
			String outputFolderPath) {
		ModelData md=new ModelData(datasetName,descriptorSetName,null,false,false);
		
		//Get training and test set instances as strings using TEST descriptors:
		md.generateInstancesNotinOperaTrainingSet();

		File outputFolder = new File(outputFolderPath);
		outputFolder.mkdirs();


		String outputFileNameTraining = datasetName + "_" + descriptorSetName+ "_OPERA_training.tsv";
		String outputFilePathTraining = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNameTraining;

		String outputFileNamePrediction = datasetName + "_" + descriptorSetName+ "_NOT_IN_OPERA_TRAINING_SET_prediction.tsv";
		String outputFilePathPrediction = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNamePrediction;
		
		try {
			FileWriter fw=new FileWriter(outputFilePathTraining);
			fw.write(md.trainingSetInstances);
			fw.flush();
			fw.close();
			fw=new FileWriter(outputFilePathPrediction);
			fw.write(md.predictionSetInstances);
			fw.flush();
			fw.close();
			
			
			fw=new FileWriter(outputFolderPath+datasetName+"_exp_prop_prediction.smi");

			String [] lines=md.predictionSetInstances.split("\n");
			
			for (int i=1;i<lines.length;i++) {				
				String line=lines[i];
				String smiles=line.substring(0,line.indexOf("\t"));
				fw.write(smiles+"\tC"+i+"\r\n");
			}
			
			fw.flush();
			fw.close();
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

//	private void copyOperaTrainingSet(String descriptorSetName, String datasetName, String folder) {
//
//		String folderMainOpera="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\pf_python_modelbuilding\\datasets_benchmark\\";
//		
//		Connection conn=SqlUtilities.getConnection();
//		
//		String sql="select p.\"name\" from qsar_datasets.datasets d \r\n"
//				+ "inner join qsar_datasets.properties p on p.id =d.fk_property_id \r\n"
//				+ "where d.\"name\"='"+datasetName+"';";
////		System.out.println(sql);
//		String propertyName=DatabaseLookup.runSQL(conn, sql);
//		if(propertyName.equals("LogBCF_Fish_WholeBody")) propertyName="LogBCF";
//		
//		String srcPath=folderMainOpera+propertyName+" OPERA\\"+propertyName+" OPERA "+descriptorSetName+" training.tsv";
//		String destPath=folder+datasetName + "_" + descriptorSetName+"_OPERA_training.tsv";	
//		
//		try {
//			Files.copy(Paths.get(srcPath), Paths.get(destPath),StandardCopyOption.REPLACE_EXISTING);
////			new File(destPath).deleteOnExit();
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(datasetName+"\t"+propertyName);
//
//		
//		
//	}
	
	private static JsonArray readTSVFile(String filePath) throws IOException {
        JsonArray jsonArray = new JsonArray();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine(); // Read the header line
            if (headerLine == null) {
                throw new IOException("The TSV file is empty.");
            }
            
            String[] headers = headerLine.split("\t"); // Split header line by tab
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t"); // Split each line by tab
                JsonObject jsonObject = new JsonObject();
                
                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i];
                    String value = i < fields.length ? fields[i] : ""; // Handle missing values
                    jsonObject.addProperty(key, value);
                }
                
                jsonArray.add(jsonObject);
            }
        }
        
        return jsonArray;
    }
	
	void getDescriptorSubset(String datasetName,String descriptorSetName) {
		
		String folder="data\\dev_qsar\\dataset_files\\";
		String fileName = datasetName + "_" + descriptorSetName + "_full.tsv";

		try {

			JsonArray ja=readTSVFile(folder+fileName);
			
			
//			List<String>fields=Arrays.asList("ID","ALogP","XLogP");
			List<String>fields=Arrays.asList("ID","ALOGP","XLOGP");
			
			FileWriter fw=new FileWriter(folder+fileName.replace(".tsv", "_subset.tsv"));
			
			for (int i=0;i<fields.size();i++) {
				fw.write(fields.get(i));
				if(i<fields.size()-1) fw.write("\t");
				else fw.write("\n");
			}
			
//			System.out.println(ja.size());
			
			for(int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				System.out.println(jo.get("ID").getAsString());
				
				for (int j=0;j<fields.size();j++) {
				
					String field=fields.get(j);
					
					if(jo.get(field)==null || jo.get(field).isJsonNull()) break;
					
					if(field.equals("ID")) {
						fw.write(jo.get(field).getAsString());	
					} else {
						fw.write(jo.get(field).getAsDouble()+"");
					}
					
					if(j<fields.size()-1) fw.write("\t");
					else fw.write("\n");
				}
			}
			 fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		CDL.toJSONArray(fileName);
		
	}
	

	public static void main(String[] args) {
		DatasetFileWriter writer = new DatasetFileWriter();
//		writer.writeOPERAFiles();
//		writer.writeTEST_Toxicity_Files();
//		writer.write_exp_prop_datasets();
//		writer.writeBatch();
		
//		writer.writeExternalSet();
		
		String outputFolderPath="data/dev_qsar/dataset_files/";
//		String datasetName="exp_prop_96HR_FHM_LC50_v1 modeling";
		String datasetName="LogP v1 modeling";
		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="PaDEL-default";
		
		writer.write_exp_prop_datasets();
//		writer.writeWithoutSplitting(datasetName, descriptorSetName, outputFolderPath, true);
//		writer.writeWithoutSplitting(datasetName, descriptorSetName, outputFolderPath, false);
		
//		writer.getDescriptorSubset(datasetName, descriptorSetName);
		
//		writer.writeWithoutSplitting(108L, descriptorSetName, outputFolderPath, true);
				
		//**********************************************************

//		String outputFolderPath="data/dev_qsar/dataset_files/";
//		String descriptorSetName="WebTEST-default";
//		String datasetName="LogP v1 modeling";
//		String outputFolderPath="C:\\Users\\lbatts\\OneDrive - Environmental Protection Agency (EPA)\\0 Python\\pf_python_modelbuilding\\datasets\\"+datasetName;
//		writer.writeWithSplitting(descriptorSetName,"RND_REPRESENTATIVE",datasetName,outputFolderPath,false,true);
		
		
		//		writer.writeWithSplitting(descriptorSetName,PFAS_SplittingGenerator.splittingPFASOnly,"Standard Water solubility from exp_prop",outputFolderPath);
		
//		writer.createAquaticToxFiles();

//		writer.write_exp_prop_datasets();
		
		
//		writer.writeWithSplitting(descriptorSetName,PFAS_SplittingGenerator.splittingPFASOnly,"Standard Water solubility from exp_prop",outputFolderPath);
//		writer.writeWithSplitting(descriptorSetName,PFAS_SplittingGenerator.splittingAll,"Standard Water solubility from exp_prop",outputFolderPath);
//		writer.writeWithSplitting(descriptorSetName,PFAS_SplittingGenerator.splittingAllButPFAS,"Standard Water solubility from exp_prop",outputFolderPath);

//		writer.writeWithSplitting("T.E.S.T. 5.1","TEST","LC50 TEST",outputFolderPath);
		
//		String dataset="LC50 TEST";		
//		String dataset="LD50 TEST";
//		String dataset="Mutagenicity TEST";
//		String splitting="TEST";
		
//		String dataset="LogBCF OPERA";
//		String splitting="OPERA";
//		
//		String[] sciDataExpertsDescriptorSetNames = {
//				"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default", "Mordred-default"
//		};
//
//		for (String descriptorSetName:sciDataExpertsDescriptorSetNames) {
//			System.out.println(descriptorSetName);
//			writer.writeWithSplitting(descriptorSetName,splitting,dataset,outputFolderPath);	
//		}
		

	}

	
	
	private void createAquaticToxFilesMultipleDescriptorSet() {

		String[] sciDataExpertsDescriptorSetNames = {
				"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default", "Mordred-default"};

		for (String descriptorSetName : sciDataExpertsDescriptorSetNames) {
			for (int i = 1; i <= 5; i++) {
//				String datasetName="exp_prop_96HR_FHM_LC50_v"+i+" modeling";
				String datasetName="exp_prop_96HR_BG_LC50_v"+i+" modeling";
//				String datasetName = "exp_prop_48HR_DM_LC50_v" + i + " modeling";

//				String outputFolderPath = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\modeling services\\pf_python_modelbuilding\\datasets_exp_prop\\"
//						+ datasetName;

				String outputFolderPath = "C:\\Users\\lbatts\\OneDrive - Environmental Protection Agency (EPA)\\0 Python\\pf_python_modelbuilding\\datasets_exp_prop\\"
						+ datasetName;

				
				writeWithSplitting(descriptorSetName, "RND_REPRESENTATIVE", datasetName, outputFolderPath, false, true);
			}
		}
	}

	

	private void createAquaticToxFilesV5Sets() {

		
		List<String>dataSets=Arrays.asList("exp_prop_96HR_FHM_LC50_v5 modeling", "exp_prop_96HR_BG_LC50_v5 modeling","exp_prop_96HR_RT_LC50_v5 modeling");
		
		String[] sciDataExpertsDescriptorSetNames = {
				"PaDEL-default", "WebTEST-default", "ToxPrints-default", "Mordred-default"};

		for (String descriptorSetName : sciDataExpertsDescriptorSetNames) {

			for (String dataset:dataSets) {
//				String outputFolderPath = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\modeling services\\pf_python_modelbuilding\\datasets_exp_prop\\"
//						+ datasetName;

				String outputFolderPath = "C:\\Users\\lbatts\\OneDrive - Environmental Protection Agency (EPA)\\0 Python\\pf_python_modelbuilding\\datasets_exp_prop\\"
						+ dataset;

				
				writeWithSplitting(descriptorSetName, "RND_REPRESENTATIVE", dataset, outputFolderPath, false, true);
			}
		}
	}

}


