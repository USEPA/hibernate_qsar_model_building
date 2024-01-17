package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.DashboardPredictionUtilities;

/**
* @author TMARTI02
*/
public class DSSTOX_Name_Script {

	Connection conn=SqlUtilities.getConnectionDSSTOX();
	
	static class DSSTOX_Name {
		String dtxsid;
		String dtxcid;
		String IUPAC_Name;
		String INDEX_Name;
		String smiles;
		String filename;
		String created_by;
		String updated_by;
		String software_version;
		
		
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
	
	public static void main(String[] args) {
		DSSTOX_Name_Script d=new DSSTOX_Name_Script();
		
		//TODO have "comments" at the end of the name. See below : (incorrect configuration definition!) and (non-preferred name)
		//Also make sure dont have end of line characters in them
		d.loadNames();
		
//		d.loadNameFile();
//		d.lookForFieldInSDFs();
//		d.lookForFieldInSDFs2();
//		d.lookForChemicalInSDFs();
//		d.backupCompoundsNames();
	}
	
	
}
