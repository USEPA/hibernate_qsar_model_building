package gov.epa.run_from_java.scripts;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.splittings.Splitter;
import gov.epa.web_services.SplittingWebService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class SplittingGeneratorScript {

	private static void splitDatasets() {
		String lanId = "tmarti02";
		int portSplittingWS=DevQsarConstants.PORT_REPRESENTATIVE_SPLIT;//matches value in dataset_splitting_ws.py if running local
		
		
		Unirest.config().connectTimeout(0).socketTimeout(0);
		
		
		SplittingWebService splittingWebService = new SplittingWebService(DevQsarConstants.SERVER_LOCAL, portSplittingWS, 
				DevQsarConstants.SPLITTING_RND_REPRESENTATIVE, 2);
		Splitter splitter = new Splitter(splittingWebService, lanId);
		String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		List<String>datasetNames=new ArrayList<>();
//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("ExpProp BCF Fish_TMM");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop");
//		datasetNames.add("pKa_a from exp_prop and chemprop");
//		datasetNames.add("pKa_b from exp_prop and chemprop");
		
//		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");

//		datasetNames.add("HLC v1 modeling");
//		datasetNames.add("WS v1 modeling");
//		datasetNames.add("VP v1 modeling");
//		datasetNames.add("LogP v1 modeling");
//		datasetNames.add("BP v1 modeling");
//		datasetNames.add("MP v1 modeling");
		
//		datasetNames.add("exp_prop_96HR_FHM_LC50_v1 modeling");
//		datasetNames.add("exp_prop_96HR_FHM_LC50_v2 modeling");
//		datasetNames.add("exp_prop_96HR_FHM_LC50_v3 modeling");
//		datasetNames.add("exp_prop_96HR_FHM_LC50_v4 modeling");
		
		datasetNames.add("exp_prop_96HR_BG_LC50_v1 modeling");
		datasetNames.add("exp_prop_96HR_BG_LC50_v2 modeling");
		datasetNames.add("exp_prop_96HR_BG_LC50_v3 modeling");
		datasetNames.add("exp_prop_96HR_BG_LC50_v4 modeling");
		
//		
		
//		datasetNames.add("exp_prop_96HR_scud_v1 modeling");
		
		System.out.println(splittingWebService.address);
		
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
