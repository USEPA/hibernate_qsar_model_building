package gov.epa.run_from_java.scripts;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.splittings.Splitter;
import gov.epa.web_services.SplittingWebService;
import kong.unirest.HttpResponse;

public class SplittingGeneratorScript {

	private static void splitDatasets() {
		String lanId = "tmarti02";
		int portSplittingWS=5000;//matches value in dataset_splitting_ws.py if running local
		
		
		SplittingWebService splittingWebService = new SplittingWebService(DevQsarConstants.SERVER_LOCAL, portSplittingWS, 
				DevQsarConstants.SPLITTING_RND_REPRESENTATIVE, 2);
		Splitter splitter = new Splitter(splittingWebService, lanId);
		String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		List<String>datasetNames=new ArrayList<>();
//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("ExpProp BCF Fish_TMM");
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop");
		datasetNames.add("pKa_a from exp_prop and chemprop");
		datasetNames.add("pKa_b from exp_prop and chemprop");

		
//		HttpResponse<String> bob=splittingWebService.callBob();
//		System.out.println(bob.getBody());

				
		for (String datasetName:datasetNames) {
			splitter.split(datasetName, descriptorSetName, 10);
		}
	}

	public static void main(String[] args) {
		splitDatasets();
	}

}
