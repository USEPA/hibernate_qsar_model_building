package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.RunDashboardPredictions;

/**
* @author TMARTI02
*/
public class DSSTOX_Name_Script {

	
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
			this.software_version=software_version;
		}
		
		
		
		
					
	}
	
	
	
	
	private void loadNamesInFile(String lanId, String software_version, File file) {
		AtomContainerSet acs=RunDashboardPredictions.readSDFV3000(file.getAbsolutePath());
		
		boolean skipMissingSID=false;
		int maxCount=99999999;
//		int maxCount=1;
		
		AtomContainerSet acs2 = RunDashboardPredictions.filterAtomContainerSet(acs, skipMissingSID, maxCount);

		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

		
		System.out.println(acs2.getAtomContainerCount());

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

			if (DTXCID==null) {
				System.out.println("Null CID for "+DTXSID);
				continue;
			}
			names_create.add(new DSSTOX_Name(DTXSID,DTXCID,IUPAC_Name, INDEX_Name,smiles,file.getName(), lanId,software_version));
		}

//		System.out.println(Utilities.gson.toJson(names_create));
		
		createSQL(names_create);
		
//		System.out.println(names_create.size()+"\t"+names_update.size());
//		System.out.println(names_create.size());
	}
	
	public void createSQL (List<DSSTOX_Name> names) {

		Connection conn=SqlUtilities.getConnectionDSSTOX();
		
		String [] fieldNames= {"dtxsid","dtxcid","IUPAC_Name","INDEX_Name","smiles",
				"filename","created_by","software_version","created_at"};
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
		System.out.println(sql);
		
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
				prep.setString(8, name.software_version);
				
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
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
	
	public static void main(String[] args) {
		DSSTOX_Name_Script d=new DSSTOX_Name_Script();
		d.loadNames();
	}
	
	
}
