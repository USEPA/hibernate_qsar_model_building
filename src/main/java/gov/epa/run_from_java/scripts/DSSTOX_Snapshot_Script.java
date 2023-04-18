package gov.epa.run_from_java.scripts;

import java.lang.reflect.Type;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.RunDashboardPredictions;

/**
* @author TMARTI02
*/
public class DSSTOX_Snapshot_Script {

	
	Connection conn=SqlUtilities.getConnectionDSSTOX();
	
	
	List<DsstoxCompound> getCompoundsBySQL(int offset,int limit) {
		
		List<DsstoxCompound>compounds=new ArrayList<>();
		
		String sql="SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,gs.dsstox_substance_id, gs.casrn\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null)\n";
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;
		
		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
		
		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				
				
				compound.setDsstoxCompoundId(rs.getString(1));
				
				if (rs.getString(2)!=null)
					compound.setMolFile(rs.getString(2));
				
				if (rs.getString(3)!=null)
					compound.setSmiles(rs.getString(3));
				
				if (rs.getString(4)!=null)
					compound.setJchemInchikey(rs.getString(4));
				
				if (rs.getString(5)!=null)
				compound.setIndigoInchikey(rs.getString(5));				
				
				if (rs.getString(6)!=null)
					compound.setMolWeight(Double.parseDouble(rs.getString(6)));
				
				if (rs.getString(7)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(7));
					
					if (rs.getString(8)!=null) {
						gs.setCasrn(rs.getString(8));
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
	
	
	
	
	void getallcompounds() {
		
//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
//		List<DsstoxCompound>compounds=compoundService.findAll();
		
		int batchSize=50000;
		int i=0;
		
		while(true) {

			File file=new File("data/dsstox/json/snapshot_compounds"+(i+1)+".json");

			if (file.exists()) {
				i++;
				continue;
			}
			
			List<DsstoxCompound>compounds=getCompoundsBySQL(i*batchSize, batchSize);
			
			if(compounds.size()==0) {
				break;
			} else {
				
				try {
					FileWriter fw=new FileWriter(file);
					fw.write(Utilities.gson.toJson(compounds));
					
					System.out.println((i+1)+"\t"+compounds.size());
					
					fw.flush();
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				i++;
			}
		}
		
	}
	
//	List<DsstoxCompound> assembleCompounds() {
//		
//		File folder=new File("data/dsstox/json");
//		
//		
//		List<DsstoxCompound>compoundsAll=new ArrayList<>();
//		
//		Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();
//		
//		for (File file:folder.listFiles()) {
//			if(!file.getName().contains("json")) {
//				continue;
//			}
//			
//			try {
//				List<DsstoxCompound>compounds=Utilities.gson.fromJson(new FileReader(file), listOfMyClassObject);
//			
//				for (DsstoxCompound compound:compounds) {
//					if (compound.getMolWeight()==null || compound.getMolWeight()==0) continue;
//					if(compound.getIndigoInchikey()==null && compound.getJchemInchikey()==null) continue;
//					compoundsAll.add(compound);
//				}
// 				
//				System.out.println(file.getName()+"\t"+compoundsAll.size());
//				
//			} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		return compoundsAll;
//		
//	}
	
	
	
	/**
	 * Converts files in json folder to sdf format and saves to sdf folder
	 * 
	 */
	void writeSDFs() {
		
		try {
		
			File folder=new File("data/dsstox/json");
			
			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();
			
			for (File file:folder.listFiles()) {

				if(!file.getName().contains("json")) {
					continue;
				}
				
				try {
					List<DsstoxCompound>compounds=Utilities.gson.fromJson(new FileReader(file), listOfMyClassObject);

					String filename=file.getName().replace(".json", ".sdf");
					
					System.out.println(filename);
					
					FileWriter fw=new FileWriter("data/dsstox/sdf/"+filename);
					
					for (DsstoxCompound compound:compounds) {
						fw.write(compound.getMolFile());
						fw.write(">  <DTXCID>\n");
						fw.write(compound.getDsstoxCompoundId()+"\r\n\r\n");
						
						if (compound.getSmiles()!=null) {
							fw.write(">  <smiles>\n");
							fw.write(compound.getSmiles()+"\r\n\r\n");
						}								
						
						if (compound.getGenericSubstanceCompound()!=null && compound.getGenericSubstanceCompound().getGenericSubstance()!=null) {
							if(compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()!=null) {
								fw.write(">  <DTXSID>\n");
								fw.write(compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+"\r\n\r\n");
							}
							
							if(compound.getGenericSubstanceCompound().getGenericSubstance().getCasrn()!=null) {
								fw.write(">  <CASRN>\n");
								fw.write(compound.getGenericSubstanceCompound().getGenericSubstance().getCasrn()+"\r\n\r\n");
							}

						} else {
//							System.out.println(compound.getDsstoxCompoundId()+"\tDTXSID=null");
						}

						fw.write("$$$$\r\n");
					}
					
					fw.flush();
					fw.close();
					
//					if (true) break;
					
	 				
				} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	static void checkmsReady() {
		String filePath="data/dsstox/NITRO_chemicals.txt";
		Connection conn=SqlUtilities.getConnectionDSSTOX();
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filePath));
			
			String header=br.readLine();
			while (true) {
				String line=br.readLine();

				if(line==null)break;
				
				String [] vals=line.split("\t");
				String cid=vals[2];
				String smiles=vals[3];
				
				String sql="select canonical_msr from dsstox_msready dm where dtxcid =\'"+cid+"_3'";
				
				String msreadySmiles=SqlUtilities.runSQL(conn, sql);
				
				System.out.println(cid+"\t"+smiles+"\t"+msreadySmiles);
				
				
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	
	public static void main(String[] args) {
		DSSTOX_Snapshot_Script d=new DSSTOX_Snapshot_Script();
//		checkmsReady();
//		d.getallcompounds();
//		d.writeSDFs();
		d.loadNames();
		

	}


	class dsstox_name {
		String dtxsid;
		String dtxcid;
		String IUPAC_Name;
		String INDEX_Name;
		String smiles;
		String filename;
		String created_by;
		String updated_by;
		
		String iupac_name_0423;
		String index_name_0423;
		
		//Constructor for create
		public dsstox_name(String dtxsid,String dtxcid,String iupac_name_0423, String index_name_0423,String smiles,String filename, String created_by) {

			this.dtxsid=dtxsid;
			this.dtxcid=dtxcid;
			this.smiles=smiles;
			
			this.index_name_0423=index_name_0423;
			this.iupac_name_0423=iupac_name_0423;
			
			this.filename=filename;
			this.created_by=created_by;
		}
		

		//Constructor for update
		public dsstox_name(String dtxcid,String iupac_name_0423, String index_name_0423,String smiles,String filename, String updated_by) {

			this.dtxcid=dtxcid;
			this.index_name_0423=iupac_name_0423;
			this.iupac_name_0423=index_name_0423;
			
			this.smiles=smiles;
			this.filename=filename;
			this.updated_by=updated_by;
		}

		
		
		
		
		public void createSQL (List<dsstox_name> names) {

			Connection conn=SqlUtilities.getConnectionPostgres();
			
			String [] fieldNames= {"dtxsid","dtxcid","IUPAC Name","INDEX Name","smiles","filename","created_by","created_at"};
			int batchSize=1000;
			
			String sql="INSERT INTO qsar_models.predictions (";
			
			for (int i=0;i<fieldNames.length;i++) {
				sql+=fieldNames[i];
				if (i<fieldNames.length-1)sql+=",";
				else sql+=") VALUES (";
			}
			
			for (int i=0;i<fieldNames.length-1;i++) {
				sql+="?";
				if (i<fieldNames.length-1)sql+=",";			 		
			}
			sql+="current_timestamp)";	
//			System.out.println(sql);
			
			try {
				conn.setAutoCommit(false);
				PreparedStatement prep = conn.prepareStatement(sql);
				prep.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
				long t1=System.currentTimeMillis();

				for (int counter = 0; counter < names.size(); counter++) {
					dsstox_name name=names.get(counter);
					prep.setString(1, name.dtxsid);
					prep.setString(2, name.dtxcid);
					prep.setString(3, name.IUPAC_Name);
					prep.setString(4, name.INDEX_Name);
					prep.setString(5, name.smiles);
					prep.setString(6, name.filename);
					prep.setString(7, name.created_by);
					prep.addBatch();
					
					if (counter % batchSize == 0 && counter!=0) {
						// System.out.println(counter);
						prep.executeBatch();
					}
				}

				int[] count = prep.executeBatch();// do what's left
				long t2=System.currentTimeMillis();
				System.out.println("time to post "+names.size()+" names using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
				conn.commit();
//				conn.setAutoCommit(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}			
		
		void update (String lanId,String DTXCID,String IUPAC_Name,String INDEX_Name) {
			String sql="UPDATE dsstox_names\r\n"
					+ "SET iupac_name_0423 = '"+IUPAC_Name+"', "
					+ "index_name_0423 = '"+INDEX_Name+"',"
					+ "updated_by = '"+lanId+"'\r\n"
					+ "WHERE dtxcid = '"+DTXCID+"123456';";
			
//			System.out.println(sql);
			SqlUtilities.runSQLUpdate(conn, sql);
		}
	
	}


	private void loadNames() {
		String lanId="tmarti02";
		String folder="data\\dsstox\\names";
		
		File [] files=new File(folder).listFiles();
		
		File file=files[0];
		
		AtomContainerSet acs=RunDashboardPredictions.readSDFV3000(file.getAbsolutePath());
		
		boolean skipMissingSID=false;
		int maxCount=99999999;
		
		AtomContainerSet acs2 = RunDashboardPredictions.filterAtomContainerSet(acs, skipMissingSID, maxCount);

		Iterator<IAtomContainer> iterator= acs2.atomContainers().iterator();

		
		System.out.println(acs2.getAtomContainerCount());

		int count=0;
		
		Connection conn=SqlUtilities.getConnectionDSSTOX();
		
		List<dsstox_name>names_update=new ArrayList<>();
		List<dsstox_name>names_create=new ArrayList<>();
		
		while (iterator.hasNext()) {
			count++;
			
			AtomContainer ac=(AtomContainer) iterator.next();
			String smiles=ac.getProperty("smiles");
			String DTXCID=ac.getProperty("DTXCID");
			String DTXSID=ac.getProperty("DTXSID");
			String IUPAC_Name=ac.getProperty("IUPAC_Name");
			String INDEX_Name=ac.getProperty("INDEX_Name");
			
			
			String sql1="select dtxcid from dsstox_names dn where dn.dtxcid ='"+DTXCID+"'";
			
			long t1=System.currentTimeMillis();
			String dtxcid=SqlUtilities.runSQL(conn, sql1);
			long t2=System.currentTimeMillis();
			
			if (dtxcid==null) {
				names_create.add(new dsstox_name(DTXSID,DTXCID,IUPAC_Name, INDEX_Name,smiles,file.getName(), lanId));
			} else {
				names_update.add(new dsstox_name(DTXCID,IUPAC_Name, INDEX_Name,smiles,file.getName(), lanId));
			}
			
			System.out.println(count+"\t"+DTXCID+"\t"+IUPAC_Name+"\t"+INDEX_Name+"\t"+(t2-t1));
			
		}

		System.out.println(names_create.size()+"\t"+names_update.size());
		
	}

}
