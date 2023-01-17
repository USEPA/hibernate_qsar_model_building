package gov.epa.run_from_java.scripts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.endpoints.datasets.descriptor_values.DescriptorValuesCalculator;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.endpoints.datasets.descriptor_values.TestDescriptorValuesCalculator;

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
	
	void generateDescriptorsForDataset() {
		
		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="ToxPrints-default";
//		String descriptorSetName="RDKit-default";
//		String descriptorSetName="PaDEL-default";
//		
		String server="https://ccte-cced.epa.gov/";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
		List<String>datasetNames=new ArrayList<>();

		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("ExpProp BCF Fish_TMM");
		
		int batchSize=1;//right now if one chemical in batch fails, the batch run fails, so run 1 at a time
		for (String datasetName:datasetNames) {
			calc.calculateDescriptors_useSqlToExcludeExisting(datasetName,  descriptorSetName, true,batchSize);
		}
	}

	void calcSingleChemical() {
		
//		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="ToxPrints-default";
//		String descriptorSetName="RDKit-default";
		String descriptorSetName="PaDEL-default";
//		
		String server="https://ccte-cced.epa.gov/";
//		String server="https://hazard-dev.sciencedataexperts.com";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
		calc.runSingleChemical(descriptorSetName, "ClC(Cl)(Cl)Cl");
				
		
	}
	public static void main(String[] args) {
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF);
		Unirest.config().connectTimeout(0).socketTimeout(0);

		QsarDescriptorsScriptTodd q=new QsarDescriptorsScriptTodd();
		q.generateDescriptorsForDataset();
		
//		q.calcSingleChemical();
		if(true) return;
		
		
//		String tsv = calculateDescriptorsForDataset(
//				"Standard Water solubility from exp_prop", "PaDEL-default",
//				DevQsarConstants.SERVER_819 + ":443", true, "tmarti02");

		
//		String tsv = calculateDescriptorsForDataset(
//				"Water solubility OPERA", "PaDEL-default",
//				DevQsarConstants.SERVER_819 + ":443", true, "tmarti02");		

		
		
//*********************************************************************************************************		
//		String datasetName = "Henry's law constant OPERA";
//		String datasetName = "LC50 TEST";
//		String datasetName = "LD50 TEST";
//		String datasetName = "Mutagenicity TEST";

//		String datasetName="LogBCF OPERA";
//		String splitting="OPERA";

		
//		String[] sciDataExpertsDescriptorSetNames = {
//				"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default", "Mordred-default"
//		};
		
//		String[] sciDataExpertsDescriptorSetNames = {
//				"RDKit-default", "WebTEST-default", "ToxPrints-default", "Mordred-default"
//		};
		
//		String[] sciDataExpertsDescriptorSetNames = {"WebTEST-default"};
//		String[] sciDataExpertsDescriptorSetNames = {"Mordred-default"};
		
//		String urlSDE=HCD_SCI_DATA_EXPERTS_URL;
//		String urlSDE = DevQsarConstants.SERVER_819 + ":443";
		
//		DescriptorValuesCalculator calc = new DescriptorValuesCalculator("tmarti02");
//		
//		for (String descriptorSetName:sciDataExpertsDescriptorSetNames) {
//			
////			calc.deleteDescriptorSetValuesForDataset(datasetName, descriptorSetName);
//			
//			String tsv = calculateDescriptorsForDataset(datasetName, descriptorSetName, urlSDE, true, "tmarti02");
//			System.out.println(descriptorSetName + "\t" + tsv.split("\r\n").length);
//			String header = tsv.substring(0, tsv.indexOf("\r"));
//			System.out.println(header.split("\t").length);
//			System.out.println(header);
//			System.out.println();
//		}
		
		
//		SciDataExpertsDescriptorValuesCalculator calc = new SciDataExpertsDescriptorValuesCalculator(urlSDE, "tmarti02");
////		String smiles="Cc1c(cc(cc1[N+](=O)[O-])[N+](=O)[O-])[N+](=O)[O-]";
////		String smiles="CC1(C)C(C=C(Cl)Cl)C1C(=O)OCC1=CC(OC2=CC=CC=C2)=CC=C1";
//		
//		String smiles="ClC12C3(Cl)C4(Cl)C5(Cl)C(Cl)(C1(Cl)C4(Cl)Cl)C2(Cl)C(Cl)(Cl)C35Cl";
//		
//		calc.runSingleChemical("RDKit-default", smiles);
//		runSimple();
		
//		compareToGrace();
	}
	
	
	static List<Double> runSimple() {
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
				
				String valuesTsv=calc.runSingleChemical("RDKit-default", smiles);
				
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
