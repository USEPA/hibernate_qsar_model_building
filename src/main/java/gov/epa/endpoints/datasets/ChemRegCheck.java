package gov.epa.endpoints.datasets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.ChemicalList;
import gov.epa.databases.dsstox.service.ChemicalListService;
import gov.epa.databases.dsstox.service.ChemicalListServiceImpl;
import gov.epa.databases.dsstox.service.SourceSubstanceService;
import gov.epa.databases.dsstox.service.SourceSubstanceServiceImpl;

public class ChemRegCheck {
	
	
	ArrayList<String> chemRegListNameArray = new ArrayList<String>();
	ArrayList<String> chemRegFileNameArray = new ArrayList<String>();
	private ChemicalListService chemicalListService;
	private SourceSubstanceService sourceSubstanceService;
	String datasetPartialFolderPath;



	public static void main(String[] args) {
		ChemRegCheck check = new ChemRegCheck();
		check.chemicalListService = new ChemicalListServiceImpl();
		check.sourceSubstanceService = new SourceSubstanceServiceImpl();

		addChemRegListNames(check);
		Filecheck(check);
		compare(check);
		
	}
	
	public static void compare(ChemRegCheck chemRegCheck) {
		
		for (int i = 0; i < chemRegCheck.chemRegListNameArray.size(); i++) {
			String checkChemicalList = null;
			checkChemicalList = chemRegCheck.chemRegListNameArray.get(i);
			ChemicalList chemicalList = chemRegCheck.chemicalListService.findByName(checkChemicalList);
			List<DsstoxRecord> dsstoxRecords = null;
			if (chemicalList != null) {
				// If chemical list already added to DSSTox, queries all records from it
				dsstoxRecords = chemRegCheck.sourceSubstanceService
						.findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(checkChemicalList);
			}
			ArrayList<String> externalIds = new ArrayList<String>();
			for (DsstoxRecord dsstoxRecord : dsstoxRecords) {
				externalIds.add(dsstoxRecord.externalId);
			}
			// ok and now we're going back to the list
			File fullFile = new File(chemRegCheck.datasetPartialFolderPath + File.separator + chemRegCheck.chemRegFileNameArray.get(i));
			String firstExternalId = null;
			try {
				Scanner sc = new Scanner(fullFile);
				sc.nextLine(); // because of the headers
				Pattern p = Pattern.compile("(.*?)(\t)(.*)");
				String line = sc.nextLine();
				Matcher m = p.matcher(line);
				while (m.find()) {
					firstExternalId = m.group(1);
				}
				System.out.println(firstExternalId);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int k = 0; k < externalIds.size(); k++) {
				String chemRegExternalId = externalIds.get(k);
				if (chemRegExternalId.equals(firstExternalId)) {
					System.out.println("list" + i + "is good, listname=" + checkChemicalList);
				}
			}

		}

	}
	
	
	/**
	 * @param chemRegCheck hardcode what needs to go here.
	 */
	public static void addChemRegListNames(ChemRegCheck chemRegCheck) {
		ArrayList<String> listNameArray = new ArrayList<String>();
		listNameArray.add("ExpProp_BP_072522_Import_1_to_20000");
		listNameArray.add("ExpProp_BP_072522_Import_20001_to_40000");
		listNameArray.add("ExpProp_BP_072522_Import_40001_to_60000");
		listNameArray.add("ExpProp_BP_072522_Import_60001_to_80000");
		listNameArray.add("ExpProp_BP_072522_Import_80001_to_100000");
		listNameArray.add("ExpProp_BP_072522_Import_100001_to_120000");
		listNameArray.add("ExpProp_BP_072522_Import_140001_to_160000");
		listNameArray.add("ExpProp_BP_072522_Import_160001_to_180000");
		listNameArray.add("ExpProp_BP_072522_Import_180001_to_200000");
		listNameArray.add("ExpProp_BP_072522_Import_200001_to_220000");
		listNameArray.add("ExpProp_BP_072522_Import_240001_to_260000");
		listNameArray.add("ExpProp_BP_072522_Import_260001_to_280000");
		listNameArray.add("ExpProp_BP_072522_Import_280001_to_300000");
		listNameArray.add("ExpProp_BP_072522_Import_300001_to_320000");
		listNameArray.add("ExpProp_BP_072522_Import_340001_to_360000");
		listNameArray.add("ExpProp_BP_072522_Import_360001_to_363160");
		chemRegCheck.chemRegListNameArray = listNameArray;
	}
	
	/**
	 * @param chemRegCheck 
	 * TODO: don't have datasetName (the folder), splitsize, datasetFileName be hardcoded like this
	 */
	public static void Filecheck(ChemRegCheck chemRegCheck) {
		String datasetFileName = "ExpProp_BP_072522_ChemRegImport";
		String listNameExtension = ".txt";
		String datasetFullFileName = datasetFileName + listNameExtension;
		String datasetName = "Standard_Boiling_Point_from_exp_prop";
		// need to read in the full thing in order to get the number of records
		int splitsize = 20000;
		ArrayList<String> fileNames = new ArrayList<String>();
		String datasetFolderPath = DevQsarConstants.OUTPUT_FOLDER_PATH + File.separator + datasetName + File.separator + datasetFullFileName;
		int numRecords = 0;
		
		File fullFile = new File(datasetFolderPath);
		Scanner sc;
		try {
			sc = new Scanner(fullFile);
			while (sc.hasNextLine()) {
				numRecords++;
				sc.nextLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int splits = (numRecords / splitsize) + 1;

		Integer startingRecord = 1;
		for (int i = 0; i < splits; i++) {
			StringBuilder s = new StringBuilder();
			s.append(datasetFileName);
			int endingRecord = (i == splits - 1) ? numRecords - 1 : startingRecord+splitsize - 1;
			s.append("_" + String.valueOf(startingRecord) + "_to_" + String.valueOf(endingRecord) + listNameExtension);
			fileNames.add(s.toString());
			startingRecord = startingRecord + splitsize;
		}
		
		for (int i = 0; i < fileNames.size(); i++) {
			// System.out.println(fileNames.get(i));
		}
		chemRegCheck.chemRegFileNameArray = fileNames;
		chemRegCheck.datasetPartialFolderPath = DevQsarConstants.OUTPUT_FOLDER_PATH + File.separator + datasetName;
	}

}
