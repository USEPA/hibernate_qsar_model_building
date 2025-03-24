package gov.epa.run_from_java.scripts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.service.ChemicalListService;
import gov.epa.databases.dsstox.service.DsstoxCompoundService;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.databases.dsstox.service.GenericSubstanceService;
import gov.epa.databases.dsstox.service.GenericSubstanceServiceImpl;
import gov.epa.databases.dsstox.service.SourceSubstanceService;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class NaiveMapperScript {
	
	private DsstoxCompoundService dsstoxCompoundService=new DsstoxCompoundServiceImpl();
	private GenericSubstanceService genericSubstanceService=new GenericSubstanceServiceImpl();


	public List<DsstoxRecord> getDsstoxRecords(Collection<String> inputs, String inputType) {
		List<DsstoxRecord> dsstoxRecords = new ArrayList<DsstoxRecord>();
		if (inputs.size() <= 1000) {
			switch (inputType) {
			case DevQsarConstants.INPUT_DTXCID:
				dsstoxRecords = dsstoxCompoundService.findAsDsstoxRecordsByDtxcidIn(inputs);
				break;
			case DevQsarConstants.INPUT_DTXSID:
				dsstoxRecords = genericSubstanceService.findAsDsstoxRecordsByDtxsidIn(inputs);
				break;
			case DevQsarConstants.INPUT_CASRN:
				dsstoxRecords = genericSubstanceService.findAsDsstoxRecordsByCasrnIn(inputs);
				break;
			}
		} else {
			int count = 0;
			Set<String> subset = new HashSet<String>();
			for (String input : inputs) {
				subset.add(input);
				count++;
				if (count == 1000) {
					dsstoxRecords.addAll(getDsstoxRecords(subset, inputType));
					subset = new HashSet<String>();
					count = 0;
				}
			}
			dsstoxRecords.addAll(getDsstoxRecords(subset, inputType));
		}

		return dsstoxRecords;
	}
	
	static class Identifier{
		int ID;
		String casrn;
		String name;
		String dtxsid;
	}
	
	void goThroughGHSIdentifiers () {
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\AA Dashboard\\Data\\dictionary\\text output\\";
		String filepath=folder+"flat file 2025_03_10 identifiers.txt";
		
		try {
			
			HashSet<String>casrns=new HashSet<>();
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			String header=br.readLine();
			
			List<Identifier>identifiers=new ArrayList<>();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				String [] vals=Line.split("\t");
				
				Identifier i=new Identifier();
				i.ID=Integer.parseInt(vals[0]);
				i.casrn=vals[1];
				i.name=vals[2];
				
				if (!(i.casrn.isBlank())) casrns.add(i.casrn);
				identifiers.add(i);
				
			}
			br.close();
			
			List<DsstoxRecord>recs=getDsstoxRecords(casrns, DevQsarConstants.INPUT_CASRN);
			
			Hashtable<String,DsstoxRecord>htCAS=new Hashtable<>();
			for (DsstoxRecord rec:recs) {
				htCAS.put(rec.casrn, rec);
			}
//			System.out.println(casrns.size()+"\t"+recs.size());
			
			List<Identifier>identifiersUnmapped=new ArrayList<>();
			List<Identifier>identifiersMapped=new ArrayList<>();
			
			for (Identifier i:identifiers) {
				if(htCAS.containsKey(i.casrn)) {
					i.dtxsid=htCAS.get(i.casrn).dsstoxSubstanceId;
					identifiersMapped.add(i);
				} else {
					identifiersUnmapped.add(i);
				}
			}
			
//			for (Identifier i:identifiersUnmapped) {
//				if(i.casrn.isBlank())System.out.println(i.name);
//			}
			
			FileWriter fw=new FileWriter(filepath.replace(".txt", ".json"));
			fw.write(Utilities.gson.toJson(identifiersMapped));
			fw.flush();
			fw.close();
//			System.out.println(Utilities.gson.toJson(identifiers));
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) {

		NaiveMapperScript n=new NaiveMapperScript();

//		List<String>casrns=Arrays.asList("71-43-2");
//		List<DsstoxRecord>recs=n.getDsstoxRecords(casrns, DevQsarConstants.INPUT_CASRN);
//		System.out.println(Utilities.gson.toJson(recs));

		n.goThroughGHSIdentifiers();
	}

}
