package gov.epa.run_from_java.data_loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.json.CDL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dev_qsar.exp_prop.service.PublicSourceServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalService;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalServiceImpl;
import gov.epa.endpoints.datasets.dsstox_mapping.DsstoxMapper;
import gov.epa.run_from_java.scripts.DatasetCreatorScript;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.util.ParseStringUtils;

/**
* @author TMARTI02
*/
public class SourceChemicalUtilities {

	static String strFolderNewLists="data\\dev_qsar\\output\\000 new chemreg lists\\";
	
	Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	PublicSourceServiceImpl publicSourceService=new PublicSourceServiceImpl(); 
	SourceChemicalServiceImpl sourceChemicalService=new SourceChemicalServiceImpl();
	
//	Map<String, SourceChemical> sourceChemicalMap = new HashMap<String, SourceChemical>();
//	Map<String, SourceChemical> sourceChemical = new HashMap<String, SourceChemical>();
//	List<SourceChemical> sourceChemicals;
	
//	SourceChemicalUtilities(boolean load) {
//		if(load)loadSourceChemicals();
//	}

	List<SourceChemical> loadSourceChemicalsAll() {
		
		System.out.print("Loading sourceChemical map...");

		List<SourceChemical>sourceChemicals = sourceChemicalService.findAllSql();
		
//		for (SourceChemical sourceChemical:sourceChemicals) {
//			if(sourceChemical.generateSrcChemId().equals("SCH000000571295")) {
//				System.out.println("Found it in map:"+gson.toJson(sourceChemical));
//			}
//			System.out.println(sourceChemical.getId()+"\t"+sourceChemical.getKey());
//		}
		
		System.out.println("Done");
		return sourceChemicals;
	}
	
//	void writeChemRegFileLiteratureSources() {
//		loadSourceChemicals();
//		
//		List<SourceChemical>sourceChemicalsLS=new ArrayList<>();
//	
//		for (String key:sourceChemicalMap.keySet()) {
//			SourceChemical sc=sourceChemicalMap.get(key);
//			
//			if(sc.getLiteratureSource()==null) continue;
//			
//			sourceChemicalsLS.add(sc);
//			
//		}
//		System.out.println(sourceChemicalsLS.size());
//		
//		String filepath="data\\dev_qsar\\output\\new chemreg lists\\2024_02_02_ChemProp_Literature_Sources.txt";
//		
//		DsstoxMapper.writeChemRegImportFile(sourceChemicalsLS, filepath);
//		
//		
//	}
	
	void writeChemRegFileChemProp() {
		//129476
		
		List<SourceChemical>scList=new ArrayList<>();
		
		List<SourceChemical>scAll=loadSourceChemicalsAll();
	
		for (SourceChemical sc:scAll) {
			if (sc.getSourceDtxrid()==null) continue;
			scList.add(sc);
		}
		System.out.println(scList.size());
		
		String filepath=strFolderNewLists+ "exp_prop_2024_02_02_from_prod_chemprop.txt";
		
		DsstoxMapper.writeChemRegImportFile(scList, filepath);
		
		
	}
	
	void writeChemRegFilePublicSources() {
		
		List<SourceChemical>sourceChemicalsPS=new ArrayList<>();
		List<SourceChemical>sourceChemicalsPS_ChemProp=new ArrayList<>();
		List<SourceChemical>sourceChemicalsPS_Non_ChemProp=new ArrayList<>();

		Map<String, List<SourceChemical>> mapBySource = new HashMap<>();
		
		List<SourceChemical>scAll=loadSourceChemicalsAll();
		
		for (SourceChemical sc:scAll) {
			if(sc.getPublicSource()==null) continue;
			String sourceName=sc.getPublicSource().getName();
			sourceChemicalsPS.add(sc);
			
			if(sc.getSourceDtxrid()!=null) {
				sourceChemicalsPS_ChemProp.add(sc);
			} else {
				sourceChemicalsPS_Non_ChemProp.add(sc);
				
				if(mapBySource.containsKey(sourceName)) {					
					List<SourceChemical>list=mapBySource.get(sourceName);
					list.add(sc);
					
				} else {
					List<SourceChemical>list=new ArrayList<>();
					list.add(sc);
					mapBySource.put(sourceName, list);
				}				
			}

		}
		
		System.out.println("All public source:"+sourceChemicalsPS.size());
		System.out.println("Public source with dtxrid (ChemProp):"+sourceChemicalsPS_ChemProp.size());
		System.out.println("Public source without dtxrid:"+sourceChemicalsPS_Non_ChemProp.size());

		for (String sourceName:mapBySource.keySet()) {
//			if(sourceName.toLowerCase().equals("lookchem"))continue;
//			if(!sourceName.equals("ChemicalBook"))continue;
			if(!sourceName.equals("OChem"))continue;
			
			System.out.println(sourceName+"\t"+mapBySource.get(sourceName).size());
			String filepath=strFolderNewLists+"exp_prop_2024_02_02_from_"+sourceName+".txt";

			DsstoxMapper.writeChemRegImportFile(mapBySource.get(sourceName), filepath);

//			DsstoxMapper.writeChemRegImportFile(mapBySource.get(sourceName), filepath,20000);

		}
		
		
	}
	
	void writeChemRegFileForPublicSource() {
		
		List<SourceChemical>scList=new ArrayList<>();
//		Hashtable<String,SourceChemical>htSourceChemicals=new Hashtable<>();//avoids duplicates

//		String sourceName="OChem_2024_04_03";
//		String sourceName="PubChem_2024_03_20";
//		String sourceName="OPERA2.8";
		String sourceName="PubChem_2024_11_27";
		
		List<SourceChemical>scAll=loadSourceChemicalsAll();
		
		for (SourceChemical sc:scAll) {
			if(sc.getPublicSource()==null) continue;
			
			if(sc.getPublicSource().getName().equals(sourceName)) {
				scList.add(sc);
			}
//			if(sc.generateSrcChemId().equals("SCH000000571295")) {
//				System.out.println("Found in map again:"+gson.toJson(sc));
//			}
		}
		
		System.out.println(sourceName+":"+scList.size());
//		String filepath=strFolderNewLists+"exp_prop_2024_04_10_from_"+sourceName+".txt";
		String filepath=strFolderNewLists+sourceName+"\\"+sourceName+".txt";
		
		File file=new File(filepath);
		System.out.println(file.getAbsolutePath());
		
		DsstoxMapper.writeChemRegImportFile(scList, filepath,40000);
		
	}

	
	List<SourceChemical> loadSourceChemicalsForPublicSource(String publicSourceName) {
		
		PublicSource ps=this.publicSourceService.findByName(publicSourceName);
		if(ps==null) {
			System.out.println("Public source="+publicSourceName+" doesnt exist");
			return null;
		}
		System.out.print("Loading sourceChemical map for "+ps.getName()+"...");
		//		List<SourceChemical> sourceChemicals = sourceChemicalService.findAllFromSource(ps);//slowwwww
		List<SourceChemical> sourceChemicals = sourceChemicalService.findAllFromSourceSql(ps);
		System.out.println("done");
		return sourceChemicals;
	}

	
	/**
	 * Just retrieves source chemicals for that source to speed retrieval of source chemicals
	 */
	void writeChemRegFileForPublicSource2() {
		
//		String sourceName="OChem_2024_04_03";
//		String sourceName="PubChem_2024_03_20";
//		String sourceName="PubChem_2024_11_27";
//		String sourceName="OPERA2.8";
//		
//		String sourceName="Arnot 2006";
//		String sourceName="ECOTOX_2024_12_12";
		String sourceName="QSAR_Toolbox";

		List<SourceChemical>sourceChemicals=loadSourceChemicalsForPublicSource(sourceName);
		System.out.println(sourceChemicals.size());		
		
		File folder=new File(strFolderNewLists+sourceName);
		folder.mkdirs();
		
//		String filepath=folder.getAbsolutePath()+File.separator+"exp_prop_2025_01_27_from_"+sourceName+".txt";

		String date="2025_03_25";
		String filepath=folder.getAbsolutePath()+File.separator+"exp_prop_"+date+"_"+sourceName+".txt";
		
//		exp_prop_2025_03_25_Arnot 2006
		File file=new File(filepath);
		System.out.println(file.getAbsolutePath());
		
		DsstoxMapper.writeChemRegImportFile(sourceChemicals, filepath,20000);
		
		
	}

	private void removePubChemSIDNames(List<SourceChemical> sourceChemicals) {
		for (int i=0;i<sourceChemicals.size();i++) {
			SourceChemical sc=sourceChemicals.get(i);
			
			if(sc.getSourceChemicalName()==null) continue;
			
			for(int j=1;j<=9;j++) {
				if(sc.getSourceChemicalName().indexOf("SID"+j)==0) {
//					System.out.println(sc.getKey());
					sourceChemicals.remove(i--);
					
//					String sql="delete from exp_prop.source_chemicals where id="+sc.getId()+";";
//					System.out.println(sc.getSourceChemicalName());
//					SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
					
//					System.out.println(+"\t"+sc.getSourceChemicalName());
					break;
				}
			}
		}
	}
	void writeChemRegFileLiteratureSources() {
		
		Map<String, List<SourceChemical>> mapBySource = new HashMap<>();
		
		List<SourceChemical>scAll=loadSourceChemicalsAll();
		
		for (SourceChemical sc:scAll) {
			if(sc.getLiteratureSource()==null) continue;
			String sourceName=sc.getLiteratureSource().getName();
			
			if(sc.getSourceDtxrid()==null) {
				if(mapBySource.containsKey(sourceName)) {					
					List<SourceChemical>list=mapBySource.get(sourceName);
					list.add(sc);
				
				} else {
					List<SourceChemical>list=new ArrayList<>();
					list.add(sc);
					mapBySource.put(sourceName, list);
				}				
			}
		}
		
		System.out.println("#sources:"+mapBySource.size());

		for (String sourceName:mapBySource.keySet()) {
			System.out.println(sourceName+"\t"+mapBySource.get(sourceName).size());
			String filepath=strFolderNewLists+"literature sources\\exp_prop_2024_02_02_from_"+sourceName+".txt";
			DsstoxMapper.writeChemRegImportFile(mapBySource.get(sourceName), filepath,40000);
		}
		
		
	}
	
	/**
	 * Compares chemicals in chemreg list with those in the import file(s). 
	 * Missing chemicals are added to a new import file
	 * 
	 * @param file
	 */
	void compareChemRegListToImportFile(File file) {
		if (!file.getName().contains(".txt")) return;
		
		String listName=file.getName().substring(0,file.getName().indexOf(".txt"));
		
		if (file.getName().contains("OPERA.txt")) return;
		if (file.getName().contains("LookChem.txt")) return;
		
		Map<String,SourceChemical>mapList=new TreeMap<>();
		
		String sourceName=file.getName();
		sourceName=sourceName.replace("exp_prop_2024_04_03_from_", "").replace(".txt", "");
		
		if (sourceName.equals("OChem")) {
			for (int i=1;i<=13;i++) {
				Map<String,SourceChemical>mapList2=getMapChemRegListByExternalID(listName+"_40000_"+i);
				System.out.println(listName+"_"+i+"\t"+mapList2.size());
				mapList.putAll(mapList2);
			}
		} else {
			mapList=getMapChemRegListByExternalID(listName);	
		}
		

		Map<String,SourceChemical>mapImport=getMapChemRegImportFile(file);
		
		if (mapImport.size()==mapList.size()) return;
		
		List<SourceChemical>missing=new ArrayList<>();
		
		for (String key:mapImport.keySet()) {
			if(!mapList.containsKey(key)) {
				SourceChemical sc=mapImport.get(key);
				missing.add(sc);
//				System.out.println(sc.generateSrcChemId());
			}
		}

		System.out.println(listName+"\t"+mapImport.size()+"\t"+mapList.size()+"\t"+missing.size());

		File outputFolder=new File(file.getParentFile().getAbsolutePath()+File.separator+"missing");
		
		outputFolder.mkdirs();
		
		String filepath=outputFolder.getAbsolutePath()+File.separator+file.getName();
		DsstoxMapper.writeChemRegImportFile(missing, filepath);

		
	}
	
	
	/**
	 * Compares chemicals in chemreg list with those in the import file(s). 
	 * Missing chemicals are added to a new import file
	 * 
	 * @param file
	 */
	void compareChemRegListToOchemImportFiles(File folderImportFiles,String sourceName) {

		Map<String,SourceChemical>mapList=new TreeMap<>();
		Map<String,SourceChemical>mapImport=new TreeMap<>();

		
		for (File importFile:folderImportFiles.listFiles()) {
			if(!importFile.getName().contains(".txt")) continue;
			
			String [] vals=importFile.getName().replace(".txt","").split("_");
			String fileNum=vals[vals.length-1];
			
//			String listName=importFile.getName().substring(0,importFile.getName().indexOf(".txt"));
			String listName="exp_prop_2024_04_03_from_OChem_40000_"+fileNum;
			
			Map<String,SourceChemical>mapList2=getMapChemRegListByExternalID(listName);
			System.out.println(listName+"\t"+mapList2.size());
			mapList.putAll(mapList2);

			Map<String,SourceChemical>mapImport2=getMapChemRegImportFile(importFile);
			mapImport.putAll(mapImport2);

		}
		
		
		System.out.println(mapImport.size()+"\t"+mapList.size());

		
//		if (mapImport.size()==mapList.size()) return;
//		
		List<SourceChemical>missing=new ArrayList<>();
		
		for (String key:mapImport.keySet()) {
			if(!mapList.containsKey(key)) {
				SourceChemical sc=mapImport.get(key);
				missing.add(sc);
				System.out.println(sc.generateSrcChemId());
			}
		}
//
//		System.out.println(listName+"\t"+mapImport.size()+"\t"+mapList.size()+"\t"+missing.size());
//
//		File outputFolder=new File(file.getParentFile().getAbsolutePath()+File.separator+"missing");
//		
//		outputFolder.mkdirs();
//		
//		String filepath=outputFolder.getAbsolutePath()+File.separator+file.getName();
//		DsstoxMapper.writeChemRegImportFile(missing, filepath);

		
	}
	
	public static Map<String,SourceChemical> getMapChemRegListByExternalID(String listName) {
//		System.out.println(listName);
		
		String sql="select external_id,ssi.identifier_type,ssi.identifier from source_generic_substance_mappings sgsm\n"+
		"join source_substances ss on sgsm.fk_source_substance_id = ss.id\n"+
		"join source_substance_identifiers ssi on ss.id = ssi.fk_source_substance_id\n"+
		"join chemical_lists cl on ss.fk_chemical_list_id = cl.id\n"+
		"where cl.name='"+listName+"' and identifier_type not like '%_INCH%';";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);

		Map<String,SourceChemical> mapList=new HashMap<>();
		
		try {

			while (rs.next()) {
				String external_id=rs.getString(1);
				SourceChemical sc=null;
				
				if(mapList.get(external_id)==null) {
					sc=new SourceChemical();
					mapList.put(external_id,sc);	
				} else {
					sc=mapList.get(external_id);
				}
				
				String identifier_type=rs.getString(2);
				String identifier=rs.getString(3);
				
				if(identifier_type.equals("CASRN")) sc.setSourceCasrn(identifier);
				if(identifier_type.equals("NAME")) sc.setSourceChemicalName(identifier);
				if(identifier_type.equals("STRUCTURE")) sc.setSourceSmiles(identifier);
				if(identifier_type.equals("DTXSID")) sc.setSourceDtxsid(identifier);
//				System.out.println(sc.getKey());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapList;
	}
	
	public static Map<Long,SourceChemical> getMapChemRegListBySourceSubstanceID(String listName) {
//		System.out.println(listName);
		
		String sql="select ss.id,ssi.identifier_type,ssi.identifier from source_generic_substance_mappings sgsm\n"+
		"join source_substances ss on sgsm.fk_source_substance_id = ss.id\n"+
		"join source_substance_identifiers ssi on ss.id = ssi.fk_source_substance_id\n"+
		"join chemical_lists cl on ss.fk_chemical_list_id = cl.id\n"+
		"where cl.name='"+listName+"' and identifier_type not like '%_INCH%';";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);

		Map<Long,SourceChemical> mapList=new HashMap<>();
		
		try {

			while (rs.next()) {
				Long ss_id=rs.getLong(1);
				SourceChemical sc=null;
				
				if(mapList.get(ss_id)==null) {
					sc=new SourceChemical();
					mapList.put(ss_id,sc);	
				} else {
					sc=mapList.get(ss_id);
				}
				
				String identifier_type=rs.getString(2);
				String identifier=rs.getString(3);
				
				if(identifier_type.equals("CASRN")) sc.setSourceCasrn(identifier);
				if(identifier_type.equals("NAME")) sc.setSourceChemicalName(identifier);
				if(identifier_type.equals("STRUCTURE")) sc.setSourceSmiles(identifier);
				if(identifier_type.equals("DTXSID")) sc.setSourceDtxsid(identifier);
//				System.out.println(sc.getKey());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapList;
	}
	
	Map<String, SourceChemical> getMapChemRegImportFile(File file) {

		Map<String,SourceChemical> mapList=new HashMap<>();
		
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader(file));

		    
		    
		    ParseStringUtils p=new ParseStringUtils();
		    
		    String[] fieldNames = reader.readLine().split("\t");

		    // Read tsv file line by line
		    String line;
		    while ((line = reader.readLine()) != null) {
		        
		        String[] fieldValues = line.split("\t");
		        

		        SourceChemical sc=new SourceChemical();
		        
		        String external_id=null;
		        
		        for (int i=0;i<fieldValues.length;i++) {
		        	
		        	if(fieldValues[i].isBlank()) continue;
		        	
		        	fieldValues[i]=fieldValues[i].trim();
		        	
		        	if (fieldNames[i].equals("EXTERNAL_ID")) {
		        		external_id=fieldValues[i];
		        		String strId=external_id.replace("SCH", "");
		        		while (strId.substring(0,1).equals("0")) strId=strId.substring(1,strId.length());
		        		sc.setId(Long.parseLong(strId));
		        		
//		        		System.out.println(external_id+"\t"+strId);
		        		
		        	} else if (fieldNames[i].equals("SOURCE_DTXSID")) {
		        		sc.setSourceDtxsid(fieldValues[i]);
		        	} else if (fieldNames[i].equals("SOURCE_DTXCID")) {
		        		sc.setSourceDtxcid(fieldValues[i]);
		        	} else if (fieldNames[i].equals("SOURCE_DTXRID")) {
		        		sc.setSourceDtxrid(fieldValues[i]);
		        	} else if (fieldNames[i].equals("SOURCE_CASRN")) {
		        		sc.setSourceCasrn(fieldValues[i]);
		        	} else if (fieldNames[i].equals("SOURCE_CHEMICAL_NAME")) {
		        		sc.setSourceChemicalName(fieldValues[i]);
		        	} else if (fieldNames[i].equals("SOURCE_SMILES")) {
		        		sc.setSourceSmiles(fieldValues[i]);
		        	}
		        }
				mapList.put(external_id,sc);
				
				if(external_id.equals("PR")) {
					System.out.println(line);
					System.out.println(fieldValues.length);
				}
		    }

		    reader.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mapList;

	}
	
	void findMissingSourceChemicalsForPublicSource() {
		
//		String sourceName="OChem_2024_04_03";
//		String sourceName="PubChem_2024_03_20";
//		String sourceName="OPERA2.8";
		
//		String sourceName="Arnot 2006";
//		String sourceName="ECOTOX_2024_12_12";
		String sourceName="Burkhard";
		
		
		PublicSource ps=publicSourceService.findByName(sourceName);
		
		System.out.println("Finding source chemicals for "+sourceName);
//		List<SourceChemical>sourceChemicals=sourceChemicalService.findAllFromSource(ps);
		List<SourceChemical>sourceChemicals=sourceChemicalService.findAllFromSourceSql(ps);
		
		System.out.println(sourceChemicals.size());
		
		List<SourceChemical>sourceChemicalsMissing=new ArrayList<>();
		Map<String,SourceChemical>mapList=new TreeMap<>();
		
		List<String>listNames=DatasetCreatorScript.getChemRegListNames(Arrays.asList(sourceName));
		
		for (String listName:listNames) {
			Map<String,SourceChemical>mapList2=getMapChemRegListByExternalID(listName);
			System.out.println(listName+"\t"+mapList2.size());
			mapList.putAll(mapList2);
		}
				
		for (SourceChemical sourceChemical:sourceChemicals) {
			
			if(!mapList.containsKey(sourceChemical.generateSrcChemId())) {
//				System.out.println(sourceChemical.generateSrcChemId()+"\tmissing in dsstox chemreg list");
				sourceChemicalsMissing.add(sourceChemical);
			} else {
//				System.out.println(sourceChemical.getSourceCasrn()+"\t"+mapList.get(sourceChemical.generateSrcChemId()).getSourceCasrn());
			}
			
		}
		
		System.out.println(sourceChemicals.size()+"\t"+mapList.size());
		
		if(sourceChemicalsMissing.size()>0) {
			String strFolder=strFolderNewLists+sourceName+"\\";
			File folder=new File(strFolder);
			folder.mkdirs();
			
			String filepath=strFolder+sourceName+"_missing_v2.txt";
			DsstoxMapper.writeChemRegImportFile(sourceChemicalsMissing, filepath,20000);
		} else {
			System.out.println("No missing source chemicals in dsstox");	
		}
		
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SourceChemicalUtilities scu=new SourceChemicalUtilities();//i
		
//		scu.loadSourceChemicalsAll();
		
		
//		scu.writeChemRegFileChemProp();
//		scu.writeChemRegFilePublicSources();
//		scu.writeChemRegFileLiteratureSources();//dont have any without dtxrids- all from chemprop

//		scu.writeChemRegFileForPublicSource2();
		
		scu.findMissingSourceChemicalsForPublicSource();
		
		
//		SourceChemicalUtilities scu=new SourceChemicalUtilities(false);
//		String folder=strFolderNewLists+"check\\";		
//		String folder=folderNewLists+"OChem 40K\\done\\";
//		File FOLDER=new File(folder);
//		for (File file:FOLDER.listFiles()) {
//			scu.compareChemRegListToImportFile(file);
//		}
		
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dev_qsar\\output\\exp_prop_96HR_BG_LC50_v1_modeling\\";
//		File fileChemReg=new File(folder+"exp_prop_ECOTOX_2023_12_14_ChemRegImport.txt");
//		scu.compareChemRegListToImportFile(fileChemReg);

//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dev_qsar\\output\\new chemreg lists\\OChem_2024_04_03\\";
//		File folderOchemImportFiles=new File(folder);
//		scu.compareChemRegListToOchemImportFiles(folderOchemImportFiles,"OChem_2024_04_03");
		
		
		
//		scu.compareChemRegListToImportFile(new File("data\\dev_qsar\\output\\new chemreg lists\\exp_prop_2024_02_02_from_OChem.txt"));
		
	}

}
