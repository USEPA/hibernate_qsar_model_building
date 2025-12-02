package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.DashboardPredictionUtilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF.APIMolecule;
import ToxPredictor.Application.model.PredictionResults;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;


/**
* @author TMARTI02
*/
public class DSSTOX_Name_Script {

//	Connection conn=SqlUtilities.getConnectionDSSTOX();
	Connection conn=null;
	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	
	static class DSSTOX_Name {
		String dtxsid;
		String dtxcid;
		
		String IUPAC_Name;
		String INDEX_Name;
		
		String IUPAC_Name2;
		String INDEX_Name2;

		String smiles;
		String filename;
		String created_by;
		String updated_by;
		String software_version;
		String file_name;
		
		
		//Constructor for create
		public DSSTOX_Name(String dtxsid,String dtxcid,String iupac_name, String index_name,String smiles,String filename, String created_by,String software_version) {

			this.dtxsid=dtxsid;
			this.dtxcid=dtxcid;
			this.smiles=smiles;

			this.IUPAC_Name=iupac_name;
			this.INDEX_Name=index_name;
			
			this.filename=filename;
			this.created_by=created_by;
			this.updated_by=created_by;
			this.software_version=software_version;
		}
		
		public DSSTOX_Name(String dtxcid,String iupac_name, String index_name,String iupac_name2, String index_name2,String file_name) {
			this.dtxcid=dtxcid;
			this.IUPAC_Name=iupac_name;
			this.INDEX_Name=index_name;
			this.IUPAC_Name2=iupac_name2;
			this.INDEX_Name2=index_name2;

			this.file_name=file_name;
		}
		
		public DSSTOX_Name(String dtxcid,String iupac_name, String index_name,String file_name) {
			this.dtxcid=dtxcid;
			this.IUPAC_Name=iupac_name;
			this.INDEX_Name=index_name;
			this.file_name=file_name;
		}
				
	}
	
	
	
	
	
	private void lookForCID(File file,String dtxcid) {
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(file));
			
			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				
				if(Line.equals(dtxcid)) {
					System.out.println(file.getName()+"\tFound "+dtxcid);
				}
			}
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	private boolean lookForField(File file,String fieldName) {
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(file));
			
			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				
				if(Line.equals(">  <"+fieldName+">")) {
					return true;
				}
			}
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
		
	}
	
private boolean lookForField(File file,String fieldName,String fieldValue) {
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(file));
			
			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				
				if(Line.equals(">  <"+fieldName+">")) {
					String Line2=br.readLine();
					if(Line2.equals(fieldValue))
					return true;
				}
			}
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
		
	}

//	DTXCID
	
	
	private void loadNamesInFile(String lanId, String software_version, File file) {
		
		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();

		AtomContainerSet acs=dpu.readSDFV3000(file.getAbsolutePath());
		
//		if(true) return;
		
		boolean skipMissingSID=false;
		int maxCount=99999999;
//		int maxCount=1;
		
		AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, maxCount);

		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

		
		System.out.println(file.getName()+"\t"+acs2.getAtomContainerCount());

		int count=0;
		
		Connection conn=SqlUtilities.getConnectionDSSTOX();
		
//		List<dsstox_name>names_update=new ArrayList<>();
		List<DSSTOX_Name>names_create=new ArrayList<>();
		
		while (iterator.hasNext()) {
			count++;
			
			AtomContainer ac=(AtomContainer) iterator.next();
			String smiles=ac.getProperty("smiles");
			String DTXCID=ac.getProperty("DTXCID");
			String DTXSID=ac.getProperty("DTXSID");
			String IUPAC_Name=ac.getProperty("IUPAC_Name");
		
			String INDEX_Name=ac.getProperty("INDEX_Name");

			
//			if (DTXCID!=null && DTXCID.equals("DTXCID909")) {
//				System.out.println(DTXCID+"\t"+smiles+"\t"+INDEX_Name+"\t"+IUPAC_Name);
//			} else {
//				continue;
//			}
				
			if (DTXCID==null) {
				System.out.println("Null CID for "+DTXSID);
				continue;
			}
			names_create.add(new DSSTOX_Name(DTXSID,DTXCID,IUPAC_Name, INDEX_Name,smiles,file.getName(), lanId,software_version));
		}

//		System.out.println(Utilities.gson.toJson(names_create));
		createSQL(names_create);//add to dsstox_names table in snapshot
		
//		System.out.println(names_create.size()+"\t"+names_update.size());
//		System.out.println(names_create.size());
	}

	
	

	private void getNamesInFile(String lanId, String software_version, File file) {
		
		DashboardPredictionUtilities dpu = new DashboardPredictionUtilities();

		
//		if(true) return;
		
		boolean skipMissingSID=false;
		int maxCount=99999999;
//		int maxCount=1;

//		AtomContainerSet acs=dpu.readSDFV3000(file.getAbsolutePath());
//		AtomContainerSet acs2 = dpu.filterAtomContainerSet(acs, skipMissingSID, maxCount);
//		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

		List<APIMolecule>molecules=RunFromSDF.readSDF_to_API_Molecules(file.getAbsolutePath(), -1, false);
		
		
		System.out.println(file.getName()+"\t"+molecules.size());
//		System.out.println(file.getName()+"\t"+acs2.getAtomContainerCount());

		int count=0;
		
		Connection conn=SqlUtilities.getConnectionDSSTOX();
		
//		List<dsstox_name>names_update=new ArrayList<>();
		List<DSSTOX_Name>names_create=new ArrayList<>();
		
		
		for(APIMolecule molecule:molecules) {
			
//		while (iterator.hasNext()) {
			count++;
			
//			AtomContainer ac=(AtomContainer) iterator.next();
			
			
//			String smiles=ac.getProperty("smiles");
//			String DTXCID=ac.getProperty("DTXCID");
//			String DTXSID=ac.getProperty("DTXSID");
//			String IUPAC_Name=ac.getProperty("IUPAC_Name");
//			String INDEX_Name=ac.getProperty("INDEX_Name");
			
			String SMILES=molecule.htProperties.get("SMILES")+"";
			String DTXCID=molecule.htProperties.get("DTXCID")+"";
			String DTXSID=molecule.htProperties.get("DTXSID")+"";
			String IUPAC_NAME=molecule.htProperties.get("IUPAC_NAME")+"";
			String INDEX_NAME=molecule.htProperties.get("INDEX_NAME")+"";
			
			
			
			
			IUPAC_NAME=fixName(DTXSID, IUPAC_NAME);
//			INDEX_NAME=fixName(DTXSID,INDEX_NAME);
			
//			if (DTXCID!=null && DTXCID.equals("DTXCID909")) {
//				System.out.println(DTXCID+"\t"+smiles+"\t"+INDEX_Name+"\t"+IUPAC_Name);
//			} else {
//				continue;
//			}
				
//			if (DTXCID==null) {
//				System.out.println("Null CID for "+DTXSID);
//				continue;
//			}
			names_create.add(new DSSTOX_Name(DTXSID,DTXCID,IUPAC_NAME, INDEX_NAME,SMILES,file.getName(), lanId,software_version));
		}
		
		
//		System.out.println(gson.toJson(names_create));
		
		

//		System.out.println(Utilities.gson.toJson(names_create));
//		createSQL(names_create);//add to dsstox_names table in snapshot
		
//		System.out.println(names_create.size()+"\t"+names_update.size());
//		System.out.println(names_create.size());
	}
	
	
	String fixName(String dtxcid, String name) {
		
		
		while(name.contains("\n")) name=name.replace("\n", "");//get rid of carriage returns
		
		String nameOriginal=name;

		Hashtable<String,String>ht=new Hashtable<>();
		ht.put("$a","alpha");
		ht.put("$b","beta");
		ht.put("$c","chi");//doesnt happen?
		ht.put("$d","delta");
		ht.put("$e","epsilon");
		ht.put("$g","gamma");
		ht.put("$h","eta");
		ht.put("$i","iota");
		ht.put("$j","phi");
		ht.put("$k","kappa");
		ht.put("$l","lambda");
		ht.put("$m","mu");
		ht.put("$n","nu");
		ht.put("$q","theta");
		ht.put("$r","rho");
		ht.put("$s","sigma");
		ht.put("$u","upsilon");
		ht.put("$x","xi");
		ht.put("$y","psi");
		ht.put("$z","zeta");
		ht.put("$>","->");
		ht.put("(incorrect configuration definition!)","");		
		ht.put("(non-preferred name)","");
		ht.put("(incorrect configuration definition!, non-preferred name)","");
		ht.put("%{}", "");
		
//		ht.put("$p","pi");
//		ht.put("$o","omega");
//		ht.put("$o","omicron");
//		ht.put("$t","tau");

		for(String key:ht.keySet()) {
			name=name.replace(key,ht.get(key));	
		}
				
        
		return name;
	}

	private String fixNameRegex(String dtxcid, String name) {
		
		String regexPattern = "%\\{([^}]+)\\}";
        name = name.replaceAll(regexPattern, "$1");//get rid of italics tag indicated by %, e.g. DTXCID4074279

        
        String regexPatternCarret0="\\^\\{(.)\\}";//instances of ^{x} where x is a single character
        name = name.replaceAll(regexPatternCarret0, "^$1");
        
//        String regexPatternCarret1="\\^\\{([^\\{]+)\\^\\{([^}]+)\\}\\}";//nested superscripts!
////        name = name.replaceAll(regexPatternCarret1, "~$1~$2~~");
//        name = name.replaceAll(regexPatternCarret1, "~$1^$2~");//convert inner superscript to ^
//
//        String regexPatternCarret2 = "\\^\\{([^}]+)\\}";
//        name = name.replaceAll(regexPatternCarret2, "~$1~");//instances of ^{x} where x is one or more characters, convert to ~x~, e.g. DTXCID701372215
        
        
//        for(int i=1;i<=5;i++) {
//        	String regexPatternCarret3="~(.+)~(.+)~~";
//        	name = name.replaceAll(regexPatternCarret3, "~$1^$2~");
//        }
        
        name=name.replace("a^{}", "'a");
        
        
        //TODO what to do ones like DTXCID00631353, has "^{1^4}"
        
        String regexPattern3 = "_\\{([^}]+)\\}";
        name = name.replaceAll(regexPattern3, "_$1_");//convert subscript indicated by _ to _#_, e.g. DTXCID30696778

		if(name.contains("%")) {
			System.out.println("percentage\t"+dtxcid+"\t"+name);
		}

//		if(name.contains("^")) {
//			System.out.println("\ncaret\t"+dtxcid);
//			System.out.println(nameOriginal);
//			System.out.println(name);
//		}
		
		if(name.contains("$")) {
			System.out.println("dollar sign\t"+dtxcid+"\t"+name);
		}
		
		if(name!=null) name=name.trim();
		
//		if(dtxcid.equals("DTXCID401865988")) {
//			System.out.println("DTXCID401865988:");
//			System.out.println(nameOriginal);
//			System.out.println(name);
//		}
		
		if(name.contains("~~")) {
			System.out.println("tildetilde\t"+dtxcid+"\t"+name);
		}
		
//		if(name.contains("{")) {
//			System.out.println("curly brace\t"+dtxcid+"\t"+name);
//		}
		return name;
	}
	
	
	List<DsstoxCompound> getCompoundsBySQL(int offset,int limit) {

		List<DsstoxCompound>compounds=new ArrayList<>();

		String sql="SELECT dsstox_compound_id,c.acd_index_name,c.acd_iupac_name, gs.dsstox_substance_id, gs.casrn\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				

				compound.setDsstoxCompoundId(rs.getString(1));

				if (rs.getString(2)!=null)
					compound.setAcdIndexName(rs.getString(2));

				if (rs.getString(3)!=null)
					compound.setAcdIupacName(rs.getString(3));

				if (rs.getString(4)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(4));

					if (rs.getString(5)!=null) {
						gs.setCasrn(rs.getString(5));
					}

				}

				compounds.add(compound);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return compounds;
	}

	/**
	 * Writes compounds table to a single tsv file (pulls from database 50K at a time)
	 * 
	 */
	void backupCompoundsNames() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		int batchSize=50000;
		int i=0;

		File file=new File("data/dsstox/backup/snapshot_compounds_names_backup.tsv");

		try {

			FileWriter fw=new FileWriter(file);

			fw.write("cid\tsid\tacd_index_name\tacd_iupac_name\r\n");

			while(true) {

				List<DsstoxCompound>compounds=getCompoundsBySQL(i*batchSize, batchSize);

				if(compounds.size()==0) {
					break;
				} else {

					System.out.println((i+1)+"\t"+compounds.size());

					for (DsstoxCompound compound:compounds) {

						fw.write(compound.getDsstoxCompoundId()+"\t");

						if (compound.getGenericSubstanceCompound()!=null) {
							fw.write(compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\t");
						} else {
							fw.write("N/A\t");
						}

						fw.write(compound.getAcdIndexName()+"\t"+compound.getAcdIupacName()+"\r\n");
					}

					fw.flush();
					i++;
				}
			}


			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public void createSQL (List<DSSTOX_Name> names) {

		Connection conn=SqlUtilities.getConnectionDSSTOX();
		
		String [] fieldNames= {"dtxsid","dtxcid","IUPAC_Name","INDEX_Name","smiles",
				"filename","created_by","updated_by","software_version","created_at"};
		int batchSize=1000;
		
		String sql="INSERT INTO dsstox_names (";
		
		for (int i=0;i<fieldNames.length;i++) {
			
			if (fieldNames[i].contains(" ")) {
				sql+="\""+fieldNames[i]+"\"";	
			} else {
				sql+=fieldNames[i];
			}
			
			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}
		
		for (int i=0;i<fieldNames.length-1;i++) {
			sql+="?";
			if (i<fieldNames.length-1)sql+=",";			 		
		}
		sql+="current_timestamp)";	
//		System.out.println(sql);
		
		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(sql);
			
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < names.size(); counter++) {
				DSSTOX_Name name=names.get(counter);
				prep.setString(1, name.dtxsid);
				prep.setString(2, name.dtxcid);
				prep.setString(3, name.IUPAC_Name);
				prep.setString(4, name.INDEX_Name);
				prep.setString(5, name.smiles);
				prep.setString(6, name.filename);
				prep.setString(7, name.created_by);
				prep.setString(8, name.updated_by);
				prep.setString(9, name.software_version);
				
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+names.size()+" names using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	void lookForChemicalInSDFs() {
//		String cid="DTXCID001087944";
//		String cid="DTXCID3065293";//dont have record in dsstox_names
		String cid="DTXCID701284656";
		
		String folder="data\\dsstox\\sdf";
		File [] files=new File(folder).listFiles();
		for (File file:files) {
			if (!file.getName().contains("sdf")) continue;
			System.out.println(file.getName());
			lookForCID(file, cid);
		}
	}
	
	void lookForFieldInSDFs() {
		
		String folder="data\\dsstox\\names";
		File [] files=new File(folder).listFiles();
		for (File file:files) {
			if (!file.getName().contains("sdf")) continue;
		
			boolean haveIUPAC=lookForField(file, "IUPAC_Name");
			boolean haveIndex=lookForField(file, "INDEX_Name");
			
			if(!haveIndex || !haveIUPAC) {
				System.out.println(file.getName()+"\t"+haveIndex+"\t"+haveIUPAC);
			}
			
		}
	}
	
	void lookForFieldInSDFs2() {
		
		String folder="data\\dsstox\\names";
		File [] files=new File(folder).listFiles();
		for (File file:files) {
			if (!file.getName().contains("sdf")) continue;
		
			boolean found=lookForField(file, "DTXCID", "DTXCID10197031");
			
			if(found) {
				System.out.println(file.getName());
			}
			
		}
	}
	
	private void loadNames() {
		String lanId="tmarti02";
		String folder="data\\dsstox\\names";
		String software_version="ACD/Name Batch 2020.2.1";
		
		File [] files=new File(folder).listFiles();

//		File file=files[0];
//		loadNamesInFile(lanId, software_version, file);

		for (File file:files) {
			System.out.println(file.getName());
			loadNamesInFile(lanId, software_version, file);
		}
		
	}
	
	private void loadNameFile() {
		String lanId="tmarti02";
		String folder="data\\dsstox\\names";
		String software_version="ACD/Name Batch 2020.2.1";

//		File file=new File(folder+"\\snapshot_compounds12_NAMES.sdf");
		File file=new File(folder+"\\snapshot_compounds6_NAMES.sdf");
		
		loadNamesInFile(lanId, software_version, file);
		
	}
	
	void convertSDF_To_Excel() {
		
//        System.setProperty("org.apache.logging.log4j.simplelog.StatusLogger.level", "INFO");

		
		String folder="data\\dsstox\\snapshot-2025-07-30\\";
		String inputFilePath = folder+"sampleOut.sdf"; // Replace with your actual SDF file path
        String outputFilePath = inputFilePath.replace(".sdf",".xlsx"); // Replace with your desired Excel file path

        try {
        	
        	InputStream inputStream = new FileInputStream(inputFilePath);
        
            IteratingSDFReader reader = new IteratingSDFReader(inputStream, DefaultChemObjectBuilder.getInstance());

            XSSFWorkbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet("Molecules");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("DTXSID");
            headerRow.createCell(1).setCellValue("DTXCID");
            headerRow.createCell(2).setCellValue("PREFERRED_NAME");
            headerRow.createCell(3).setCellValue("CASRN");
            headerRow.createCell(4).setCellValue("INCHIKEY");
            headerRow.createCell(5).setCellValue("INCHI_STRING");
            headerRow.createCell(6).setCellValue("SMILES");
            headerRow.createCell(7).setCellValue("IUPAC_NAME");
            headerRow.createCell(8).setCellValue("INDEX_NAME");

            int rowNum = 1;

            while (reader.hasNext()) {
                IAtomContainer molecule = reader.next();

                // Extract properties
                String dtxsid = (String) molecule.getProperty("DTXSID");
                String dtxcid = (String) molecule.getProperty("DTXCID");
                String preferredName = (String) molecule.getProperty("PREFERRED_NAME");
                String casrn = (String) molecule.getProperty("CASRN");
                String inchiKey = (String) molecule.getProperty("INCHIKEY");
                String inchiString = (String) molecule.getProperty("INCHI_STRING");
                String smiles = (String) molecule.getProperty("SMILES");
                String iupacName = (String) molecule.getProperty("IUPAC_NAME");
                String indexName = (String) molecule.getProperty("INDEX_NAME");

                // Create a new row in the Excel sheet
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dtxsid);
                row.createCell(1).setCellValue(dtxcid);
                row.createCell(2).setCellValue(preferredName);
                row.createCell(3).setCellValue(casrn);
                row.createCell(4).setCellValue(inchiKey);
                row.createCell(5).setCellValue(inchiString);
                row.createCell(6).setCellValue(smiles);
                row.createCell(7).setCellValue(iupacName);
                row.createCell(8).setCellValue(indexName);
            }

            // Write the workbook to a file
            FileOutputStream fileOut = new FileOutputStream(outputFilePath);
            
            workbook.write(fileOut);
            workbook.close();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	
	private void getMissingMolecules(String folderInput,String folderOutput) {
		try  {
            // Define the type for the Hashtable
            Type hashtableType = new TypeToken<Hashtable<String,Hashtable<String,String>>>(){}.getType();

            // Parse the JSON file into a Hashtable
            FileReader reader = new FileReader(folderOutput+"htOutput.json");
            Hashtable<String,Hashtable<String,String>> htOutput = gson.fromJson(reader, hashtableType);
            reader.close();
            
            reader = new FileReader(folderInput+"htInput.json");
            Hashtable<String,Hashtable<String,String>> htInput = gson.fromJson(reader, hashtableType);
            reader.close();
            
//            System.out.println(Utilities.gson.toJson(htOutput.get("DTXCID1082722")));
            
            HashSet<String> missingDtxcids = getMissingDtxcids(htOutput, htInput);
            
            //Get the molecules for the missing dtxcids from the input sdfs
            List<APIMolecule>missingMols=getMissingMolecules(missingDtxcids, folderInput);

            FileWriter fw=new FileWriter(folderInput+"50k_missing2.sdf");
            
            for(APIMolecule mol:missingMols) {
            	fw.write(mol.strStructure);
            	for(String prop:mol.htProperties.keySet()) {
            		fw.write("><"+prop+">\r\n");
            		fw.write(mol.htProperties.get(prop)+"\r\n\r\n");
            	}
        		fw.write("$$$$\r\n");
            }
            
            fw.flush();
            fw.close();
            
            System.out.println(htOutput.size()+"\t"+missingDtxcids.size()+"\t"+missingMols.size());
            

        } catch (Exception e) {
            e.printStackTrace();
        }
	}


	
	void exportNames(String folderOut) {
		
//		getMoleculeHashtables(folder);
		
		try  {
            // Define the type for the Hashtable
            Type hashtableType = new TypeToken<Hashtable<String,Hashtable<String,String>>>(){}.getType();

            // Parse the JSON file into a Hashtable
            FileReader reader = new FileReader(folderOut+"htOutput.json");
            Hashtable<String,Hashtable<String,String>> htOutput = gson.fromJson(reader, hashtableType);
            reader.close();
            
            
            System.out.println(htOutput.size());
            
            
//            if(true)return;
            
//    		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//    		System.out.println(gson.toJson(htOutput.get("DTXCID201776444")));

                        
            List<DSSTOX_Name>names=new ArrayList<>();            
            
            int counter=0;
            
            for(String DTXCID:htOutput.keySet()) {
            	
            	counter++;
            	
            	if(counter%100000==0) System.out.println(counter);
            	
            	Hashtable<String,String>htVals=htOutput.get(DTXCID);

            	String INDEX_NAME=htVals.get("INDEX_NAME");
            	String IUPAC_NAME=htVals.get("IUPAC_NAME");
            	String FILE_NAME=htVals.get("FILE_NAME");
            	
    			INDEX_NAME=fixName("INDEX_NAME",INDEX_NAME,DTXCID,FILE_NAME);
    			IUPAC_NAME=fixName("IUPAC_NAME",IUPAC_NAME,DTXCID,FILE_NAME);
//            	System.out.println(DTXCID+"\t"+INDEX_NAME+"\t"+IUPAC_NAME);
            	
            	DSSTOX_Name name=new DSSTOX_Name(DTXCID, IUPAC_NAME, INDEX_NAME,FILE_NAME);
            	names.add(name);
            	
            }
            
            
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Names");

                // Create header row
                Row headerRow = sheet.createRow(0);
                
                int col=0;
                
                headerRow.createCell(col++).setCellValue("DTXCID");

                headerRow.createCell(col++).setCellValue("IUPAC_NAME");
                headerRow.createCell(col++).setCellValue("INDEX_NAME");
                
//                headerRow.createCell(col++).setCellValue("IUPAC_NAME2");
//                headerRow.createCell(col++).setCellValue("INDEX_NAME2");

                
                headerRow.createCell(col++).setCellValue("FILE_NAME");

                // Populate data rows
                int rowNum = 1;
                
                for (DSSTOX_Name name:names) {
                	
                	
                	if(rowNum%100000==0) System.out.println(rowNum);
                	
                    Row row = sheet.createRow(rowNum++);
                    
                    col=0;
                    
                    row.createCell(col++).setCellValue(name.dtxcid);
                    
                    row.createCell(col++).setCellValue(name.IUPAC_Name);
                    row.createCell(col++).setCellValue(name.INDEX_Name);
                    
//                    row.createCell(col++).setCellValue(name.IUPAC_Name2);
//                    row.createCell(col++).setCellValue(name.INDEX_Name2);

                    row.createCell(col++).setCellValue(name.file_name);
                }

                // Write the workbook to a file
                try (FileOutputStream fileOut = new FileOutputStream(folderOut+"Names snapshot-2025-07-30.xlsx")) {
                    workbook.write(fileOut);
                } catch (IOException e) {
                    System.err.println("Failed to write Excel file: " + e.getMessage());
                }
            } catch (IOException e) {
                System.err.println("Failed to create workbook: " + e.getMessage());
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
		
		
	}
	
	
	/**
	 * Get dtxcids where either the molecule is missing entirely or one of the names is missing
	 * 
	 * @param htOutput
	 * @param htInput
	 * @return
	 */
	private HashSet<String> getMissingDtxcids(Hashtable<String, Hashtable<String, String>> htOutput,
			Hashtable<String, Hashtable<String, String>> htInput) {
		HashSet<String>dtxcids=new HashSet<>();
		
		for(String DTXCID:htInput.keySet()) {
			
//			if(htOutput.containsKey(DTXCID)) {
//				Hashtable<String,String>htProps=htOutput.get(DTXCID);
//
//		    	String IUPAC_NAME=htProps.get("IUPAC_NAME");
//		    	String INDEX_NAME=htProps.get("INDEX_NAME");
//		    	
//		    	if(IUPAC_NAME==null || INDEX_NAME==null) {
//		    		dtxcids.add(DTXCID);
//		    	}
//
//			} else {
//				dtxcids.add(DTXCID);
//			}
			
			if(!htOutput.containsKey(DTXCID)) {
				dtxcids.add(DTXCID);
			}
			
		}
		return dtxcids;
	}


		
	String fixSymbolsInName(String dtxcid,String name) {
		String nameOriginal=name;
		
//		if(name.contains("—")) {
//			System.out.println(name);
//			
//		}
		Hashtable<String,String>ht=new Hashtable<>();
		
//		ht.put("\u2014", "--");
		ht.put("\uFFFD", "--");
		ht.put("\u0000", "");
		
		ht.put("—", "--");
//		ht.put("â€”", "--");
		
		ht.put("→","->");
		ht.put("α","alpha");
		ht.put("β","beta");
		ht.put("χ","chi");//doesnt happen?
		ht.put("δ","delta");
		ht.put("ε","epsilon");
		ht.put("γ","gamma");
		ht.put("η","eta");
		ht.put("ι","iota");
		ht.put("φ","phi");
		ht.put("κ","kappa");
		ht.put("λ","lambda");
		ht.put("μ","mu");
		ht.put("ν","nu");
		ht.put("θ","theta");
		ht.put("ρ","rho");
		ht.put("σ","sigma");
		ht.put("υ","upsilon");
		ht.put("ξ","xi");
		ht.put("ψ","psi");
		
		ht.put("ζ","zeta");
		
		for(String key:ht.keySet()) {
			name=name.replace(key,ht.get(key));	
		}
		
//		if(dtxcid.equals("DTXCID201776444")) {
//			System.out.println("\n"+dtxcid+"\t"+nameOriginal+"\t"+name);
//		}
//			System.out.println("\n"+dtxcid+"\t"+nameOriginal+"\t"+name);
//			for (char ch : name.toCharArray()) {
//	            // Print the Unicode code 
//	            System.out.println(ch+"\t"+String.format("\\u%04X", (int) ch));
//	        }
//		}
		
//		if(!name.equals(nameOriginal)) {
//			System.out.println(id+"\t"+nameOriginal+"\t"+name);
//		}
		
		return name;
	}
	
	
	public Set<Character> getBadChars(String str) {
        // Define the set of characters to exclude
        Set<Character> excludeSet = new HashSet<>();
        
        //TODO is regex needed? This runs fast and easy to edit
        
        for (char c : "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789>^'*+_:;/~{}[](),-. ".toCharArray()) {
            excludeSet.add(c);
        }
        
        // Create a set to store unique characters found in the string
        Set<Character> uniqueChars = new HashSet<>();
        
        // Iterate through each character in the string
        for (char c : str.toCharArray()) {
            // Check if the character is not in the exclude set
            if (!excludeSet.contains(c)) {
                uniqueChars.add(c);
            }
        }
        
        
        // Print the unique characters
//        System.out.println("Unique characters: " + uniqueChars);
        
        
        return uniqueChars;
    }
	
	
	private void getOutputMoleculeHashtable(String folder) {

		String encodingUTF8="UTF-8";
		String encodingANSI="Windows-1252";
				
		//Some of the sdfs were stored as ANSI instead of UTF-8:
		List<String>ansiFiles=Arrays.asList("50k_chunk_from_1_out.sdf","50k_chunk_from_100001_out.sdf","50k_chunk_from_350001_out.sdf",
				"50k_chunk_from_450001_out.sdf","50k_missing_4_out.sdf");
					
		Hashtable<String,Hashtable<String,Object>>htOutput=new Hashtable<>();
		
		
		String encoding=encodingANSI;
		
		
		for(File file:new File(folder).listFiles()) {
			if(!file.getName().contains(".sdf")) continue;
			if(!file.getName().contains("50k")) continue;
			if(!file.getName().contains("_out")) continue;
			
//			if(!file.getName().equals("50k_chunk_from_50001_out.sdf")) continue;
			System.out.println(file.getName());
						
//			String encoding=encodingUTF8;
//			if(ansiFiles.contains(file.getName())) encoding=encodingANSI;

			List<APIMolecule>molecules=RunFromSDF.readSDF_to_API_Molecules(file.getAbsolutePath(), -1, false,encoding);
			
			storeOutputNames(htOutput, file, molecules);
			 
		}

//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		System.out.println(gson.toJson(htOutput.get("DTXCID201776444")));

		saveJson(htOutput, folder+"htOutput.json");
		
	}
	
	private void getInputMoleculeHashtable(String folder) {

		String encodingUTF8="UTF-8";
		String encodingANSI="Windows-1252";
				
		//Some of the sdfs were stored as ANSI instead of UTF-8:
					
		Hashtable<String,Hashtable<String,Object>>htInput=new Hashtable<>();
		Hashtable<String,Hashtable<String,Object>>htOutput=new Hashtable<>();
		
		for(File file:new File(folder).listFiles()) {
			if(!file.getName().contains(".sdf")) continue;
			if(!file.getName().contains("50k")) continue;
			
			if(file.getName().equals("50k_missing.sdf")) continue;
			if(file.getName().equals("50k_missing_2.sdf")) continue;
			if(file.getName().equals("50k_missing_3.sdf")) continue;
			if(file.getName().equals("50k_missing_4.sdf")) continue;

			if(file.getName().equals("50k_chunk_from_1_with_error_fields.sdf")) continue;
			
//			if(!file.getName().equals("50k_chunk_from_50001_out.sdf")) continue;
			
			System.out.println(file.getName());
						
			String encoding=encodingUTF8;
			
			List<APIMolecule>molecules=RunFromSDF.readSDF_to_API_Molecules(file.getAbsolutePath(), -1, false,encoding);
			
			for(APIMolecule mol:molecules) {
				htInput.put(mol.htProperties.get("DTXCID")+"",mol.htProperties);
			}
		}
		
		
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		System.out.println(gson.toJson(htOutput.get("DTXCID201776444")));
		saveJson(htInput, folder+"htInput.json");
		
	}

	private void storeOutputNames(Hashtable<String, Hashtable<String, Object>> htOutput, File file,
			List<APIMolecule> molecules) {
		
		for(APIMolecule mol:molecules) {
			
			String dtxcid=mol.htProperties.get("DTXCID")+"";
			mol.htProperties.put("FILE_NAME",file.getName());
			
			
			String IUPAC_NAME_NEW=(String)mol.htProperties.get("IUPAC_NAME");
			String INDEX_NAME_NEW=(String)mol.htProperties.get("INDEX_NAME");
			

//			if(IUPAC_NAME_NEW!=null && IUPAC_NAME_NEW.equals("NA")) {
//				mol.htProperties.remove("IUPAC_NAME");
//				IUPAC_NAME_NEW=null;
//			}
//			if(INDEX_NAME_NEW!=null && INDEX_NAME_NEW.equals("NA")) {
//				mol.htProperties.remove("INDEX_NAME");
//				INDEX_NAME_NEW=null;
//			}

			if(htOutput.containsKey(dtxcid)) {
				
				Hashtable<String,Object>props=htOutput.get(dtxcid);
				
				String IUPAC_NAME=(String)props.get("IUPAC_NAME");
				String INDEX_NAME=(String)props.get("INDEX_NAME");
				String FILE_NAME=(String)props.get("FILE_NAME");
				
//				if(dtxcid.equals("DTXCID40373609")) {
//					System.out.println(file.getName()+"\t"+IUPAC_NAME_NEW+"\t"+INDEX_NAME_NEW);
//				}

				if((IUPAC_NAME_NEW!=null || INDEX_NAME_NEW!=null) && (IUPAC_NAME==null || INDEX_NAME==null)) {
					htOutput.put(dtxcid,mol.htProperties);
					
					if(file.getName().equals("50k_missing_5_out.sdf")) {
						System.out.println("50k_missing_5_out.sdf\t"+dtxcid);
					}
					
				} else {
					System.out.println(dtxcid+"\tkeeping values from "+FILE_NAME+" instead of "+file.getName());
				}
				
			} else {
				
				if(INDEX_NAME_NEW!=null || IUPAC_NAME_NEW!=null) {
					htOutput.put(dtxcid,mol.htProperties);	
				}
				
				
//				if(dtxcid.equals("DTXCID40373609")) {
//					System.out.println(file.getName()+"\t"+IUPAC_NAME_NEW+"\t"+INDEX_NAME_NEW);
//				}

				
			}
		}
	}

	private void fixName(File file, APIMolecule mol, String dtxcid, String nameField) {
		
		if(mol.htProperties.containsKey(nameField)) {
			
			String nameValue=mol.htProperties.get(nameField)+"";
			
			if(nameValue.equals("NA"))nameValue="";
			nameValue=fixSymbolsInName(dtxcid,nameValue);
			nameValue=fixName(dtxcid,nameValue);
			
			mol.htProperties.put(nameField,nameValue);
			
			Set<Character>badChars=getBadChars(nameValue);
			
			if(badChars.size()>0) {
				System.out.println(badChars+"\t"+nameField+"\t"+file.getName()+"\t"+dtxcid+"\t"+nameValue);
				
		        for (char ch:badChars) {
		        	System.out.println("\t*"+ch+"*\t"+String.format("\\u%04X", (int) ch));
		        }

				
				
			}
			
//						if(dtxcid.equals("DTXCID101323046")) System.out.println("DTXCID101323046\t"+IUPAC_NAME);
			
		}
	}
	
	private String fixName(String nameField, String nameValue,String dtxcid,String filename) {

		if(nameValue==null || nameValue.equals("NA"))return "";
		
//		if (nameValue.equals("NA"))	return "";
		
		
		nameValue = fixSymbolsInName(dtxcid, nameValue);
		nameValue = fixName(dtxcid, nameValue);
		nameValue = fixNameRegex(dtxcid, nameValue);
		

		Set<Character> badChars = getBadChars(nameValue);

		if (badChars.size() > 0) {
			
			System.out.println(nameField + "\t" + filename + "\t" + dtxcid + "\t" + nameValue);

			for (char ch : badChars) {
				System.out.println(ch + "\t" + String.format("\\u%04X", (int) ch));
			}
			
			System.out.println("");

		}

//						if(dtxcid.equals("DTXCID101323046")) System.out.println("DTXCID101323046\t"+IUPAC_NAME);

		return nameValue;
	}

	
	
	public static String saveJson(Object obj, String filepath)  {
		try {

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			FileWriter fw=new FileWriter(filepath);			
			
			String json=gson.toJson(obj);
			fw.write(json);
			fw.flush();
			fw.close();
			
			return json;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private List<APIMolecule> getMissingMolecules(HashSet<String>dtxcidsMissing, String folder) {
		Hashtable<String,Hashtable<String,Object>>htInput=new Hashtable<>();
		Hashtable<String,Hashtable<String,Object>>htOutput=new Hashtable<>();
		
		List<APIMolecule>missingMolecules=new ArrayList<>();
		
		HashSet<String>dtxcidsRetrieved=new HashSet<>();
		
		for(File file:new File(folder).listFiles()) {
			if(!file.getName().contains(".sdf")) continue;
			if(!file.getName().contains("50k")) continue;
			if(file.getName().contains("_out")) continue;//use input sdfs
				
			
//			if(!file.getName().equals("50k_chunk_from_50001_out.sdf")) continue;
			
			System.out.println(file.getName());
			List<APIMolecule>molecules=RunFromSDF.readSDF_to_API_Molecules(file.getAbsolutePath(), -1, false);
			
			for(APIMolecule mol:molecules) {
				String dtxcid=mol.htProperties.get("DTXCID")+"";
				
				if(dtxcidsMissing.contains(dtxcid)) {
					
					if(!dtxcidsRetrieved.contains(dtxcid)) {
						missingMolecules.add(mol);
						dtxcidsRetrieved.add(dtxcid);
						mol.htProperties.put("FILENAME", file.getName());
					}
				}
			}
		}
		
		return missingMolecules;
	}

	
	void createMissingFile2(String folder) {
		
		String sdfInput=folder+"50k_missing_2.sdf";
		String sdfOutput=folder+"50k_missing_2_out.sdf";
		List<APIMolecule>moleculesIn=RunFromSDF.readSDF_to_API_Molecules(sdfInput, -1, false);
		List<APIMolecule>moleculesOut=RunFromSDF.readSDF_to_API_Molecules(sdfOutput, -1, false);

		
		Hashtable<String,APIMolecule>htInput=new Hashtable<>();
		Hashtable<String,APIMolecule>htOutput=new Hashtable<>();
		
		
		for(APIMolecule mol:moleculesIn) {
			String DTXCID=mol.htProperties.get("DTXCID")+"";
			htInput.put(DTXCID, mol);
		}
		
		for(APIMolecule mol:moleculesOut) {
			String DTXCID=mol.htProperties.get("DTXCID")+"";
			htOutput.put(DTXCID, mol);
		}
		
		
		List<APIMolecule>moleculesMissing=new ArrayList<>();
		
		for(String dtxcid:htInput.keySet()) {
			
			if(!htOutput.containsKey(dtxcid)) {
				moleculesMissing.add(htInput.get(dtxcid));
			}
		}
		
		System.out.println(htInput.size()+"\t"+htOutput.size()+"\t"+moleculesMissing.size());
         
		try {
			FileWriter fw = new FileWriter(folder+"50k_missing_3.sdf");
	        for(APIMolecule mol:moleculesMissing) {
	        	fw.write(mol.strStructure);
	        	for(String prop:mol.htProperties.keySet()) {
	        		fw.write("><"+prop+">\r\n");
	        		fw.write(mol.htProperties.get(prop)+"\r\n\r\n");
	        	}
	    		fw.write("$$$$\r\n");
	        }
	        
	        fw.flush();
	        fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}

	
	void test() {
		String name="Bis(acetato-κ%{O})[3^{3},3^{4}-diethyl-1^{3},5^{3}-bis(3-hydroxypropyl)-8^{4},8^{5}-bis{2-[2-(2-methoxyethoxy)ethoxy]ethoxy}-1^{4},5^{4}-dimethyl-7,9-diaza-1,3,5(2,5)-tripyrrola-8(1,2)-benzenacyclodec\r\n"
				+ "aphane-1,4,6,9-tetraen-3^{1}-ido-κ^{5}%{N}^{1^{1}},%{N}^{3^{1}},%{N}^{5^{1}},%{N}^{7},%{N}^{9}]gadolinium";
		
		name=fixName("name", name, "DTXCID", "bob.sdf");
		
		System.out.println(name);

	}
	
	public static void main(String[] args) {
		DSSTOX_Name_Script d=new DSSTOX_Name_Script();
		
		//TODO have "comments" at the end of the name. See below : (incorrect configuration definition!) and (non-preferred name)
//		//Also make sure 

//		d.convertSDF_To_Excel();
		
//		d.loadNameFile();
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dsstox\\snapshot-2025-07-30\\";
		File file=new File(folder+"50k_chunk_from_1_out.sdf");
//		d.getNamesInFile("tmarti02", "2024.1.2", file);
		
		String folderOut=folder+"Unformatted ANSI\\";
		
		
//		d.getInputMoleculeHashtable(folder);
//		d.getOutputMoleculeHashtable(folderOut);
//		d.getMissingMolecules(folder,folderOut);
		d.exportNames(folderOut);
		
//		d.createMissingFile2(folder);
		
//		d.lookForFieldInSDFs();
//		d.lookForFieldInSDFs2();
//		d.lookForChemicalInSDFs();
//		d.backupCompoundsNames();
		
		
//        String inputString = "This is a test ^{example^{inner}} string ^{123^{456}} with ^{patterns^{nested}}.";
//        String resultString = inputString.replaceAll("\\^\\{([^\\{]+)\\^\\{([^}]+)\\}\\}", "~$1~$2~~");
//        System.out.println("Resulting string: " + resultString);
	    

	}
	
	
}


