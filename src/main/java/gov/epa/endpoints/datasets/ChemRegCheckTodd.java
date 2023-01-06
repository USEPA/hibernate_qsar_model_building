package gov.epa.endpoints.datasets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.ChemicalList;
import gov.epa.databases.dsstox.service.ChemicalListService;
import gov.epa.databases.dsstox.service.ChemicalListServiceImpl;
import gov.epa.databases.dsstox.service.SourceSubstanceService;
import gov.epa.databases.dsstox.service.SourceSubstanceServiceImpl;

public class ChemRegCheckTodd {

	public static void compare(ArrayList<String> listNameArray,String folderChemregImportFiles) {

		ChemicalListService chemicalListService = new ChemicalListServiceImpl();
		SourceSubstanceService sourceSubstanceService = new SourceSubstanceServiceImpl();
		
		for (String listName:listNameArray) {
			
			ChemicalList chemicalList = chemicalListService.findByName(listName);
			List<DsstoxRecord> dsstoxRecords = null;
			if (chemicalList != null) {
				// If chemical list already added to DSSTox, queries all records from it
				dsstoxRecords = sourceSubstanceService
						.findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(listName);
			}
			
			if (dsstoxRecords==null) {
				System.out.println("Records are null for "+listName);
				continue;
			}
			
			ArrayList<String> externalIds = new ArrayList<String>();
			for (DsstoxRecord dsstoxRecord : dsstoxRecords) {
				externalIds.add(dsstoxRecord.externalId);
			}
			
			ArrayList<String> externalIdsImportFile = new ArrayList<String>();

			try {
				String fileNameChemRegImport=listName.replace("Import","ChemRegImport")+".txt";
				BufferedReader br=new  BufferedReader(new FileReader(folderChemregImportFiles+fileNameChemRegImport));
				br.readLine();//header
				
				while (true) {
					String Line=br.readLine();

					if (Line==null) break;
					String externalID=Line.substring(0,Line.indexOf("\t"));				
					externalIdsImportFile.add(externalID);
//					System.out.println(externalID);					
				}
				
				br.close();
				
				int countMissing=0;
				for (String externalID_ImportFile:externalIdsImportFile ) {
					if (!externalIds.contains(externalID_ImportFile)) {
						countMissing++;
						
					}
				}
				if (countMissing>0) {
					System.out.println("Missing count for "+listName+"="+countMissing);
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}
	
	static void checkBP() {
		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_BP_072522_Import_1_to_20000");
		listNameArray.add("ExpProp_BP_072522_Import_20001_to_40000");
		listNameArray.add("ExpProp_BP_072522_Import_40001_to_60000");
		listNameArray.add("ExpProp_BP_072522_Import_60001_to_80000");
		listNameArray.add("ExpProp_BP_072522_Import_80001_to_100000_2");
		listNameArray.add("ExpProp_BP_072522_Import_100001_to_120000");
		listNameArray.add("ExpProp_BP_072522_Import_120001_to_140000");
		listNameArray.add("ExpProp_BP_072522_Import_140001_to_160000_2");
		listNameArray.add("ExpProp_BP_072522_Import_160001_to_180000");
		listNameArray.add("ExpProp_BP_072522_Import_180001_to_200000");
		listNameArray.add("ExpProp_BP_072522_Import_200001_to_220000");
		listNameArray.add("ExpProp_BP_072522_Import_220001_to_240000");
		listNameArray.add("ExpProp_BP_072522_Import_240001_to_260000");
		listNameArray.add("ExpProp_BP_072522_Import_260001_to_280000");
		listNameArray.add("ExpProp_BP_072522_Import_280001_to_300000");
		listNameArray.add("ExpProp_BP_072522_Import_300001_to_320000");
		listNameArray.add("ExpProp_BP_072522_Import_320001_to_340000");
		listNameArray.add("ExpProp_BP_072522_Import_340001_to_360000");
		listNameArray.add("ExpProp_BP_072522_Import_360001_to_363160");
		
		
		String folderImportFiles="O:\\Public\\Todd Martin\\CR Chemreg\\OutputNovember232022\\output\\Standard_Boiling_Point_from_exp_prop\\";
		
		compare(listNameArray,folderImportFiles);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		checkBP();
	}

}
