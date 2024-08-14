package gov.epa.run_from_java.scripts.EpiSuite;

import java.util.HashMap;
import java.util.List;
import java.io.*;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import kong.unirest.HttpResponse;

/**
* @author TMARTI02
*/
public class BiodegScript {
	
	
	String workflow = "qsar-ready";
	String serverHost = "https://hcd.rtpnc.epa.gov";
	SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,
			workflow, serverHost);

	
	void getQSARReadySmilesList() {

		boolean useFullStandardize=false;
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\0000 biodegradation OPPT\\biodegradation\\biowin update\\";
		String smilesPath=folder+"smiles list.txt";
		String qsarSmilesPath=folder+"qsar smiles list.txt";
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(smilesPath));
			HashMap<String, String>hmQSARSmiles= getQsarSmilesLookupFromDB();
			
			FileWriter fw=new FileWriter(qsarSmilesPath);
			
			fw.write("index\tsmiles\tqsarSmiles\r\n");
			
			int index=0;
			
			while (true) {
				String smiles=br.readLine();
				if(smiles==null) break;
				
				index++;
				String qsarSmiles=null;
				
				if(hmQSARSmiles.containsKey(smiles)) {
					qsarSmiles=hmQSARSmiles.get(smiles);
//					System.out.println(smiles+"\t"+);
				} else {
					HttpResponse<String> standardizeResponse= standardizer.callQsarReadyStandardizePost(smiles, false);
					
					if (standardizeResponse.getStatus() == 200) {
						String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
						qsarSmiles = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
								useFullStandardize);

//						System.out.println(smiles + "\t" + qsarSmiles);
						hmQSARSmiles.put(smiles, qsarSmiles);// store in map
					} else {
//						System.out.println(smiles + "\tfailed");
					}
				}
				System.out.println(index+"\t"+qsarSmiles);
				fw.write(index+"\t"+smiles+"\t"+qsarSmiles+"\r\n");
				fw.flush();
			}
			
			br.close();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public HashMap<String, String> getQsarSmilesLookupFromDB() {

		HashMap<String, String>htQsarSmiles=new HashMap();
		CompoundServiceImpl compoundService = new CompoundServiceImpl();
				
		List<Compound> standardizedCompounds = compoundService.findAllWithStandardizerSmilesNotNull(standardizer.standardizerName);

		System.out.println("Number of standardized compounds in db:" + standardizedCompounds.size());

		for (Compound compound : standardizedCompounds) {
			if (!compound.getStandardizer().equals(standardizer.standardizerName)) {
//				System.out.println("skip "+compound.getStandardizer()+"\t"+standardizer.standardizerName);
				continue;
			}
//			System.out.println(compound.getKey());
			htQsarSmiles.put(compound.getSmiles(), compound.getCanonQsarSmiles());
		}
		return htQsarSmiles;

	}
	
	
	public static void main(String[] args) {
		BiodegScript b=new BiodegScript();
		b.getQSARReadySmilesList();
	}
}
