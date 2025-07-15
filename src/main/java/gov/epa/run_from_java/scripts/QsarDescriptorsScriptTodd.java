package gov.epa.run_from_java.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.endpoints.datasets.descriptor_values.DescriptorValuesCalculator;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.endpoints.datasets.descriptor_values.TestDescriptorValuesCalculator;

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.DashboardPredictionUtilities;
import gov.epa.util.wekalite.*;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsChemical;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsDescriptorResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class QsarDescriptorsScriptTodd {
	
	private static final String ML_SCI_DATA_EXPERTS_URL = "https://ml.sciencedataexperts.com";
	private static final String HCD_SCI_DATA_EXPERTS_URL = "https://hazard-dev.sciencedataexperts.com";
	
	public static String calculateDescriptorsForDataset(String datasetName, String descriptorSetName, String descriptorWebServiceUrl, 
			boolean writeToDatabase, String lanId) {
		DescriptorValuesCalculator calc = null;
		if (descriptorSetName.equals(DevQsarConstants.DESCRIPTOR_SET_TEST)) {
			calc = new TestDescriptorValuesCalculator(descriptorWebServiceUrl, lanId);
		} else {
			calc = new SciDataExpertsDescriptorValuesCalculator(descriptorWebServiceUrl, lanId);
		}
		
		return calc.calculateDescriptors(datasetName, descriptorSetName, writeToDatabase);
	}
	
	public static void deleteDescriptorSetValuesForDataset(String datasetName, String descriptorSetName, String lanId) {
		DescriptorValuesCalculator calc = new DescriptorValuesCalculator(lanId);
		calc.deleteDescriptorSetValuesForDataset(datasetName, descriptorSetName);
	}
	
	void runSingleChemical() {
		String lanId="tmarti02";
		
		
//		CCOC(=O)NCCOC1C=CC(=CC=1)OC1C=CC=CC=1
//		CC(C)C1=CC(=C(C(=C1)[N+]([O-])=O)N(CCC)CCC)[N+]([O-])=O
//		CC(C)CC1=C(C(=O)OC)C(=NC(=C1C1=NCCS1)C(F)(F)F)C(F)F
//		CCCCCCCCCCCCS
//		O=C(NC(=O)NC1C=C(Cl)C(OC(F)(F)C(F)F)=C(Cl)C=1)C1C(F)=CC=CC=1F
//		CC(C)(C)C1C=C(C=CC=1O)C(C)(C)C

		String smiles="C[SH+](=O)C1C=CC(=CC=1)C1=CC(=NN1C1C=CC(=CC=1)OC)C(F)F";
		
		
//		String smiles="CF";
//		String smiles="NC(O)=O";
//		String smiles="O=[SH+]1OCC2C(CO1)C1(Cl)C(Cl)=C(Cl)C2(Cl)C1(Cl)Cl";
//		String smiles="XXX";
//		String smiles="COC(CNC(N)=O)C[Hg+2]Cl";
		
		
//		String smiles="CN(C)CCC[n]1(=O)c2ccccc2[s]c2ccccc12";
		
//		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="PaDEL-default";
//		String descriptorSetName="ToxPrints-default";
		String descriptorSetName="Mordred-default";
		
//		String server="https://hcd.rtpnc.epa.gov";
		String server="https://hazard-dev.sciencedataexperts.com";
//		String server="https://ccte-cced-cheminformatics.epa.gov";
		
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");

		DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		if (descriptorSet==null) {
			System.out.println("No such descriptor set: " + descriptorSetName);
		}

		String tsvValues=calc.calculateDescriptors(smiles,descriptorSet);
		System.out.println("From get:"+tsvValues);
		

//		List<String>smilesList=new ArrayList<>();
//		smilesList.add(smiles);
//		Map<String, String> descriptorsMap=new HashMap<String,String>();
//		calc.calculateDescriptors(smilesList, descriptorSet, descriptorsMap);
//		tsvValues=descriptorsMap.get(smiles);
//		System.out.println("From post:"+tsvValues);
		
		
		
//		List<String>smilesList=new ArrayList<>();
//		smilesList.add(smiles);
//		Map<String,String>mapDescriptors=new HashMap<>();
//		calc.calculateDescriptors(smilesList,descriptorSet,mapDescriptors);
//		System.out.println(mapDescriptors.get(smiles));
		
		
//		HttpResponse<String> response = Unirest.get("https://ccte-cced-cheminformatics.epa.gov/api/descriptors?type=padel&smiles=O%3D%5BSH%2B%5D1OCC2C%28CO1%29C1%28Cl%29C%28Cl%29%3DC%28Cl%29C2%28Cl%29C1%28Cl%29Cl")
//				  .asString();
//				System.out.println(response.getBody().toString());
		
//		DescriptorValues dv=new DescriptorValues(smiles,descriptorSet,tsvValues,lanId);
//		DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
//		descriptorValuesService.create(dv);
		
		
	}
	
	
	void generateDescriptorsForDatasets() {
		
//		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="ToxPrints-default";
//		String descriptorSetName="RDKit-default";
		String descriptorSetName="PaDEL-default";
//		String descriptorSetName="Mordred-default";
//		
//		String server="https://ccte-cced.epa.gov/";
		String server="https://hcd.rtpnc.epa.gov/";
//		String server = "https://hazard-dev.sciencedataexperts.com";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("BP from exp_prop and chemprop");
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("ExpProp BCF Fish_TMM");
//		datasetNames.add("pKa_a from exp_prop and chemprop");
//		datasetNames.add("pKa_b from exp_prop and chemprop");

//		datasetNames.add("MP from exp_prop and chemprop v2");
//		datasetNames.add("WS from exp_prop and chemprop v2");
//		datasetNames.add("BP from exp_prop and chemprop v3");
		
//		datasetNames.add("HLC v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("MP v1");
		
//		datasetNames.add("HLC v1 res_qsar");
//		datasetNames.add("WS v1 res_qsar");
//		datasetNames.add("VP v1 res_qsar");
//		datasetNames.add("LogP v1 res_qsar");
//		datasetNames.add("BP v1 res_qsar");
//		datasetNames.add("MP v1 res_qsar");

//		datasetNames.add("HLC v1 modeling");
//		datasetNames.add("WS v1 modeling");
//		datasetNames.add("VP v1 modeling");
//		datasetNames.add("LogP v1 modeling");
//		datasetNames.add("BP v1 modeling");
//		datasetNames.add("MP v1 modeling");
		
//		datasetNames.add("BP v2 modeling");
//		datasetNames.add("BP OChem_2024_04_03");
//		datasetNames.add("VP OChem_2024_04_03");
//		datasetNames.add("MP OChem_2024_04_03");
		
//		datasetNames.add("exp_prop_96HR_FHM_LC50_v5 modeling");
//		datasetNames.add("exp_prop_96HR_RT_LC50_v5 modeling");
//		datasetNames.add("exp_prop_96HR_BG_LC50_v5 modeling");
//		datasetNames.add("exp_prop_48HR_DM_LC50_v5 modeling");


//		List<String>speciesAbbrevs=Arrays.asList("FHM","BG","RT");
//		for (String speciesAbbrev:speciesAbbrevs) {
//			for (int version=1;version<=5;version++) {
//				datasetNames.add("exp_prop_96HR_"+speciesAbbrev+"_LC50_v"+version+" modeling");
//			}
//		}
		
//		datasetNames.add("VP v2 modeling");
//		datasetNames.add("LogP v2 modeling");
//		datasetNames.add("MP v2 modeling");
		
//		datasetNames.add("exp_prop_BCF_fish_whole_body_v1_modeling_map_by_CAS");
//		datasetNames.add("exp_prop_BCF_fish_whole_body_overall_score_1_v2_modeling_map_by_CAS");
//		datasetNames.add("ECOTOX_2024_12_12_96HR_Fish_LC50_v1 modeling");
//		datasetNames.add("exp_prop_RBIODEG_RIFM_BY_CAS");
//		datasetNames.add("exp_prop_RBIODEG_NITE_OPPT_BY_CAS");
		
//		datasetNames.add("QSAR_Toolbox_96HR_Fish_LC50_v3 modeling");
//		datasetNames.add("exp_prop_LOG_KOW_external_validation");
		datasetNames.add("LogP v1 modeling");
		
		
		int batchSize=1;//right now if one chemical in batch fails, the batch run fails, so run 1 at a time
		for (String datasetName:datasetNames) {
			calc.calculateDescriptors_useSqlToExcludeExisting(datasetName,  descriptorSetName, true,batchSize);
		}
	}
	

	void generateDescriptorsForDataset() {
		
		String server="https://hcd.rtpnc.epa.gov";
//		String server="https://hazard-dev.sciencedataexperts.com";
		
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
//		String[] sciDataExpertsDescriptorSetNames = {
//				"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default", "Mordred-default"
//		};
		
//		String[] sciDataExpertsDescriptorSetNames = {
//				"RDKit-default", "WebTEST-default", "ToxPrints-default", "Mordred-default"
//		};
		
//		String[] sciDataExpertsDescriptorSetNames = {
//		"PaDEL-default", "WebTEST-default", "Mordred-default"};

		
//		String[] sciDataExpertsDescriptorSetNames = {"WebTEST-default"};
//		String[] sciDataExpertsDescriptorSetNames = {"ToxPrints-default"};
//		String[] sciDataExpertsDescriptorSetNames = {"RDKit-default"};//specifying the options make it fail...
//		String[] sciDataExpertsDescriptorSetNames = {"PaDEL-default"};
		String[] sciDataExpertsDescriptorSetNames = {"Mordred-default"};

		int batchSize=1;
//		String datasetName="WS v1 res_qsar";
//		String datasetName="WS v1 modeling";
//		String datasetName="HLC v1 modeling";
		
		String datasetName="exp_prop_96HR_FHM_LC50_v5 modeling";
//		String datasetName="exp_prop_96HR_RT_LC50_v5 modeling";
//		String datasetName="exp_prop_96HR_BG_LC50_v5 modeling";
//		String datasetName="exp_prop_48HR_DM_LC50_v5 modeling";
//		
		
		
//		String datasetName="TTR_Binding_training_remove_bad_max_conc";

//		String datasetName="exp_prop_96HR_scud_v1 modeling";
//		String datasetName="exp_prop_96HR_RT_LC50_v1 modeling";
				
		for (String descriptorSetName:sciDataExpertsDescriptorSetNames) {
			calc.calculateDescriptors_useSqlToExcludeExisting(datasetName,  descriptorSetName, true,batchSize);
		}
		
	}
	
	
	void generateDescriptorsForDsstoxSDF() {

		String server="https://hcd.rtpnc.epa.gov/";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");

		String folderSrc="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\hibernate_qsar_model_building\\data\\dsstox\\sdf\\";
		String fileName="snapshot_compounds1.sdf";
		String filepathSDF=folderSrc+fileName;
		
		int batchSize=200;//if batchsize is too big the API might return null		
		boolean skipMissingSID=true;//skip if SDF chemical has no DSSTOXSID
		int maxCount=-1;//number of chemicals to extract from SDF, -1 to extract all

		
//		String descriptorSetName="WebTEST-default";
		String descriptorSetName="ToxPrints-default";
//		String descriptorSetName="RDKit-default";
//		String descriptorSetName="PaDEL-default";
		

		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();
		AtomContainerSet acs= dpu.readSDFV3000(filepathSDF);
		AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, maxCount);
		
		System.out.println(acs2.getAtomContainerCount()+" chemicals loaded from SDF");
		
		List<String> smilesList = makeSmilesListFromSDF(acs2);

		calc.calculateDescriptors_useSqlToExcludeExisting(smilesList,  descriptorSetName, true,batchSize);
		
	}

	private List<String> makeSmilesListFromSDF(AtomContainerSet acs2) {
		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();
		List<String>smilesList=new ArrayList<>();
		int count=0;
		
		while (iterator.hasNext()) {
			count++;
			AtomContainer ac=(AtomContainer) iterator.next();
			String smiles=ac.getProperty("smiles");
			
			if (smiles==null) {
				String DTXCID=ac.getProperty("DTXCID");
				System.out.println(DTXCID+" smiles is null");
				continue;
			}
			
//			System.out.println(count+"\t"+smiles);
			
			if(!smilesList.contains(smiles))		
				smilesList.add(smiles);
		}
		return smilesList;
	}

	void calcSingleChemical() {
		
//		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="ToxPrints-default";
//		String descriptorSetName="RDKit-default";
//		String descriptorSetName="PaDEL-default";
		String descriptorSetName="Mordred-default";
//		
		String server="https://ccte-cced.epa.gov/";
//		String server="https://hazard-dev.sciencedataexperts.com";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
		calc.runSingleChemicalPost(descriptorSetName, "ClC(Cl)(Cl)Cl");
				
		
	}
	public static void main(String[] args) {
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF);
		
		Unirest.config().connectTimeout(0).socketTimeout(0);
		
		QsarDescriptorsScriptTodd q=new QsarDescriptorsScriptTodd();
		
//		q.generateDescriptorsForDataset();
		q.generateDescriptorsForDatasets();
//		q.generateDescriptorsForDsstoxSDF();
//		q.runChemicalsFromBatchSearchCSV();
//		q.runSingleChemical();
		
		//*********************************************************************************************************		
//		q.calcSingleChemical();
//		q.runSingleChemical();
//		runSimple();
//		compareToGrace();
	}
	
	
	
	
	/**
	 * Runs chemicals from batch seatch csv- assumes it has a SMILES column
	 * 
	 */
	private void runChemicalsFromBatchSearchCSV() {
		
		
		try {
			
//			String filename="batch search toxprints top20 hazard chemicals.csv";
			String filename="batch search toxprints 5000 random TSCA chemicals.csv";
			
			String descriptorSetName="ToxPrints-default";
			int batchSize=200;
			
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);	         

			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
			
			String json = CDL.toJSONArray(csvAsString).toString();
			inputStream.close();
			
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
//			System.out.println("Number of records in csv:"+ja.size());

			List<String> smilesList=new ArrayList<>();

			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				String SMILES=jo.get("SMILES").getAsString();
//				System.out.println(SMILES);
				
				if (!smilesList.contains(SMILES))
					smilesList.add(SMILES);
			}
			
			String server="https://hcd.rtpnc.epa.gov/";
			SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
			calc.calculateDescriptors_useSqlToExcludeExisting(smilesList,  descriptorSetName, true,batchSize);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	static List<String> runSimple() {
//		Unirest.setTimeouts(0, 0);
		
		
		String body="{\r\n  \"chemicals\": [\r\n    \"Cc1c(cc(cc1[N+](=O)[O-])[N+](=O)[O-])[N+](=O)[O-]\"\r\n  ],\r\n  \"type\": \"rdkit\",\r\n  \"options\": {\r\n    \"headers\": true,\r\n    \"bits\": \"2048\",\r\n    \"radius\": 3,\r\n    \"type\": \"ecfp\"\r\n  }\r\n}\r\n";
//		System.out.println(body);
		HttpResponse<String> response = Unirest.post("http://v2626umcth819.rtord.epa.gov:443/api/descriptors/")
		  .header("Content-Type", "application/json")
		  .body(body)
		  .asString();
		System.out.println(response.getBody());
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		SciDataExpertsDescriptorResponse response2=gson.fromJson(response.getBody(),SciDataExpertsDescriptorResponse.class);
		System.out.println(response2.headers.size());
		System.out.println(response2.chemicals.get(0).descriptors.size());
		
		return response2.chemicals.get(0).descriptors;

	}
	
	static void compareToGrace() {
		String filepath="data\\dev_qsar\\dataset_files\\Testchems_4Todd_220622_v1.txt";
		
		CSVLoader c=new CSVLoader();
		try {
			Instances instances=c.getDataSetFromFileNoTox(filepath, "\t");

			String urlSDE = DevQsarConstants.SERVER_819 + ":443";
			
			SciDataExpertsDescriptorValuesCalculator calc = new SciDataExpertsDescriptorValuesCalculator(urlSDE, "tmarti02");

			
			for (int i=0;i<instances.numInstances();i++) {
				
				if (i%100==0) System.out.println(i);
				
				Instance instance=instances.instance(i);
				String smiles=instance.getName();
				
				String valuesTsv=calc.runSingleChemicalPost("RDKit-default", smiles);
				
				String [] vals=valuesTsv.split("\t");
				
				for (int j=0;j<instance.numAttributes();j++) {
					double val1=instance.value(j);
					double val2=Double.parseDouble(vals[j]);
					
					if (val1!=val2)	{				
						System.out.println(smiles+"\t"+instance.attribute(j)+"\t"+val1+"\t"+val2);
						break;
					}
					
				}
				
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	

}
