package gov.epa.run_from_java.scripts;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxSnapshotServiceImpl;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import kong.unirest.HttpResponse;

/**
* @author TMARTI02
*/
public class DsstoxSnapshotCreatorScriptDSSTOX {

	Connection connDsstox=SqlUtilities.getConnectionDSSTOX();
	SmilesGenerator sg=new SmilesGenerator(SmiFlavor.Canonical);
	CompoundServiceImpl compoundService = new CompoundServiceImpl();
	DsstoxSnapshotServiceImpl  dsstoxSnapshotService=new DsstoxSnapshotServiceImpl ();
	public DsstoxRecordServiceImpl  dsstoxRecordService=new DsstoxRecordServiceImpl();
	
	String serverHost="https://hcd.rtpnc.epa.gov";
	String workflow="qsar-ready";
	
	static final String strIndigo="Indigo";
	static final String strCDK="CDK";
	String smilesGeneratorMethod=strIndigo;
	

	
	SciDataExpertsStandardizer sciDataExpertsStandardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,workflow,serverHost);
	String standardizerName=DevQsarConstants.STANDARDIZER_SCI_DATA_EXPERTS+"_"+DevQsarConstants.QSAR_READY;

	String lanId="tmarti02";
	
	/** 
	 * This script also gets the CASRN if available
	 * 
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<DsstoxCompound> getCompoundsBySQL(int offset,int limit, boolean requireSID) {

		List<DsstoxCompound>compounds=new ArrayList<>();

		String sql="SELECT dsstox_compound_id, mol_file, smiles, "
				+ "gs.dsstox_substance_id, gs.casrn, c.id, c.mol_weight, gs.preferred_name, length(c.mol_image_png)\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
//		sql+="where (mol_weight is not null and mol_weight !=0) \n";//slows down query
		
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(connDsstox, sql);

		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				

				compound.setDsstoxCompoundId(rs.getString(1));

				if (rs.getString(2)!=null)
					compound.setMolFile(rs.getString(2));

				if (rs.getString(3)!=null)
					compound.setSmiles(rs.getString(3));

				if (rs.getInt(9)>0) {
					compound.setMolImagePNGAvailable(true);
				}
				
				
				if (rs.getString(4)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(4));

					if (rs.getString(5)!=null) {
						gs.setCasrn(rs.getString(5));
//						System.out.println("CAS="+rs.getString(5));
					}
					
					if (rs.getString(8)!=null) {
						gs.setPreferredName(rs.getString(8));
//						System.out.println("preferredName="+rs.getString(8));

					}


				} else {
//					System.out.println("Missing sid"+compound.getDsstoxCompoundId());
					if(requireSID) continue;
				}

				compound.setId(rs.getLong(6));
				compound.setMolWeight(rs.getDouble(7));
				
//				System.out.println(compound.getDsstoxCompoundId()+"\t"+compound.getSmiles());
				
				
				compounds.add(compound);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return compounds;
	}
	
	
	public List<DsstoxRecord> getDsstoxRecordsNoCompound(DsstoxSnapshot snapshot,String lanId,String sqlUpdatedAt) {

		List<DsstoxRecord>records=new ArrayList<>();

		String sql="select dsstox_substance_id, casrn,preferred_name, gs.updated_at from generic_substances gs\r\n"
				+ "left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id\r\n"
				+ "where gsc.id is null";
		
		if(sqlUpdatedAt!=null) sql+=" and "+sqlUpdatedAt;
		sql+="order by dsstox_substance_id;";
		

//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(connDsstox, sql);

		try {
			while (rs.next()) {
				DsstoxRecord record=new DsstoxRecord();
				
				record.setDtxsid(rs.getString(1));
				
				if (rs.getString(2)!=null) {
					record.setCasrn(rs.getString(2));
//					System.out.println("CAS="+rs.getString(5));
				}
				
				if (rs.getString(3)!=null) {
					record.setPreferredName(rs.getString(3));
//					System.out.println("preferredName="+rs.getString(8));

				}
				
				if (rs.getTimestamp(4)!=null) {
					record.setGenericSubstanceUpdatedAt(rs.getTimestamp(4));
				}
				record.setDsstoxSnapshot(snapshot);
				record.setCreatedBy(lanId);
				
//				System.out.println(record.getDtxsid()+"\t"+record.getCasrn()+"\t"+record.getPreferredName()+"\t"+snapshot.getId());
				
				records.add(record);
				
				

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return records;
	}
	
	void addSmiles(DsstoxCompound compound) {
		
		if (compound.getSmiles()!=null) return;
		
		String dtxsid=null;
		if (compound.getGenericSubstanceCompound()!=null) {
			dtxsid=compound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId();
		}
		
		try {
			
			if(this.smilesGeneratorMethod.contentEquals(strIndigo)) {
				assignSmilesUsingIndigo(compound, dtxsid);	
			} else if(this.smilesGeneratorMethod.contentEquals(strCDK)) {
				assignSmilesUsingCDK(compound,dtxsid);
			}
			
						
		} catch (Exception e) {
			try {
				assignSmilesUsingCDK(compound, dtxsid);
			} catch (Exception ex) {
//				System.out.println(compound.getMolFile());
				System.out.println("Couldnt assign smiles for "+compound.getDsstoxCompoundId()+"\t"+dtxsid);
			}
		}
	}

	private void assignSmilesUsingCDK(DsstoxCompound compound, String dtxsid) throws CDKException, IOException {
		MDLV3000Reader mr=new MDLV3000Reader();
		Reader reader = new StringReader(compound.getMolFile());	    
		mr.setReader(reader);
		IAtomContainer cdkMolecule = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());
		mr.close();

		String smilesCDK=sg.create(cdkMolecule);
//			System.out.println(smilesCDK+"\t"+smilesIndigo);
//			System.out.println(compound.getMolFile());
		compound.setSmiles(smilesCDK);
		System.out.println("CDK:"+compound.getDsstoxCompoundId()+"\t"+dtxsid+"\t"+smilesCDK);
	}

	private void assignSmilesUsingIndigo(DsstoxCompound compound, String dtxsid) throws Exception {
		Indigo indigo = new Indigo();
		indigo.setOption("ignore-stereochemistry-errors", true);

		IndigoObject indigoMolecule = indigo.loadMolecule(compound.getMolFile());		
		String smilesIndigo=indigoMolecule.smiles();		
		compound.setSmiles(smilesIndigo);
		System.out.println("Indigo:"+compound.getDsstoxCompoundId()+"\t"+dtxsid+"\t"+smilesIndigo);
	}
	
	DsstoxSnapshot getSnapshot(String name,String description) {
				
		DsstoxSnapshot snapshot=dsstoxSnapshotService.findByName(name);
		
		if (snapshot==null) {
			System.out.println("Snapshot not in db creating");
			snapshot=new DsstoxSnapshot(name, description, lanId);
			snapshot=this.dsstoxSnapshotService.create(snapshot);
		} else {
			System.out.println("Snapshot in db");
		}
		return snapshot;
	}
	
	public DsstoxSnapshot getSnapshot(String name) {
		
		DsstoxSnapshot snapshot=dsstoxSnapshotService.findByName(name);
		return snapshot;
	}

	
	HashSet<String> getCidsAlreadyInSnapshot(DsstoxSnapshot snapshot) {
		
		String sql2="select dtxcid from qsar_models.dsstox_records where fk_dsstox_snapshot_id="+snapshot.getId();

		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql2);
		
		HashSet<String>cids=new HashSet<>();
		
		try {
			while (rs.next()) {
				cids.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cids;
	}
	

	
	
	void createDsstoxRecordsUsingCompoundsRecords() {
		
		boolean requireSID=true;
		
		int batchSize=10000;
		
		int batch=0;//where to start loading from db
		
		int totalCount=0;
		
//		HashMap<String,String>hmStandardized=getQsarSmilesLookupFromDB("SCI_DATA_EXPERTS_QSAR_READY");
		
		String name="DSSTOX Snapshot 04/23";
		String description ="DSSTOX snapshot taken on 4/23";

		
		
		DsstoxSnapshot snapshot=getSnapshot(name,description);
		HashSet<String>cids=getCidsAlreadyInSnapshot(snapshot);
					
		while (true) {
		
			List<DsstoxCompound>dsstoxCompounds=getCompoundsBySQL(batch*batchSize, batchSize,requireSID);
			if (dsstoxCompounds.size()==0) break;
			totalCount+=dsstoxCompounds.size();

//			standardize(compounds, hmStandardized);
			
			for (int i=0;i<dsstoxCompounds.size();i++) {
				DsstoxCompound dsstoxCompound=dsstoxCompounds.get(i);

				if(cids.contains(dsstoxCompound.getDsstoxCompoundId())) {//already have in db so remove it
					dsstoxCompounds.remove(i--);
				} else {
					addSmiles(dsstoxCompound);
				}
			}

			if(dsstoxCompounds.size()>0) {//post them
				List<DsstoxRecord> records = DsstoxRecord.getRecords(dsstoxCompounds, snapshot,lanId);
				try {
					dsstoxRecordService.createBatchSQL(records);
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			} else {
//				System.out.println("Already have all compounds in records table from query");
			}
			
			batch++;
			System.out.println(batch+"\t"+batch*batchSize+"\t"+totalCount);

//			if(true) break;
		}
	}
	
	void createDsstoxRecordsUsingCompoundsRecordsFromJsons() {
		
		boolean requireSID=true;
		
		int totalCount=0;
		
//		HashMap<String,String>hmStandardized=getQsarSmilesLookupFromDB("SCI_DATA_EXPERTS_QSAR_READY");
		
		String name="DSSTOX Snapshot 11/12/2024";
		String description ="DSSTOX snapshot taken on 11/12/2024";

		DsstoxSnapshot snapshot=getSnapshot(name,description);
		
		HashSet<String>cids=getCidsAlreadyInSnapshot(snapshot);

		//TODO need to make sure we dont have 2 dtxsids with different dtxcids
					
		String folder="data\\dsstox\\snapshot-2024-11-12\\json\\";
		
		File Folder=new File(folder);

		Type listDsstoxCompoundType = new TypeToken<List<DsstoxCompound>>() {}.getType();
		
		for(File file:Folder.listFiles()) {

			if(!file.getName().contains("json")) {
				continue;
			}
			
//			if(file.getName().equals("prod_compounds_updated_gte_2023-04-04_and_lt_2024-11-12.json"))continue;

			if(file.getName().contains("gte"))continue;
			if(file.getName().contains("previous"))continue;
			if(file.getName().contains("dsstox_records"))continue;
			if(file.getName().contains("other_casrn"))continue;
			
//			if(!file.getName().equals("prod_compounds_updated_gte_2024-11-12_markush.json"))continue;
			
			
//			if(!file.getName().contains("markush"))continue;
			
			System.out.println(file.getName());
			
//			if(true) continue;
			
			totalCount += createDsstoxRecordsUsingCompoundsRecordsFromJson(snapshot, cids,
					listDsstoxCompoundType, file);

		}
	}


	private int createDsstoxRecordsUsingCompoundsRecordsFromJson(DsstoxSnapshot snapshot,
			HashSet<String> cids, Type listDsstoxCompoundType, File file) {
		try {
			List<DsstoxCompound>dsstoxCompounds=Utilities.gson.fromJson(new FileReader(file), listDsstoxCompoundType);

//				standardize(compounds, hmStandardized);

			for (int i=0;i<dsstoxCompounds.size();i++) {
				DsstoxCompound dsstoxCompound=dsstoxCompounds.get(i);
				
//					System.out.println(Utilities.gson.toJson(dsstoxCompound));

				if(cids.contains(dsstoxCompound.getDsstoxCompoundId())) {//already have in db so remove it
					dsstoxCompounds.remove(i--);
				} else {
					addSmiles(dsstoxCompound);
				}
			}

			if(dsstoxCompounds.size()>0) {//post them
				List<DsstoxRecord> records = DsstoxRecord.getRecords(dsstoxCompounds, snapshot,lanId);
				try {
					dsstoxRecordService.createBatchSQL(records);
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			} else {
				//System.out.println("Already have all compounds in records table from query");
			}

			System.out.println(file.getName()+"\t"+dsstoxCompounds.size());
		
			return dsstoxCompounds.size();
		
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
		
	}
	
	void createDsstoxRecordsUsingCompoundsRecordsNoDTXCID() {
		
		String name="DSSTOX Snapshot 04/23";
		String description ="DSSTOX snapshot taken on 4/23";

		DsstoxSnapshot snapshot=getSnapshot(name,description);
		List<DsstoxRecord>records=getDsstoxRecordsNoCompound(snapshot,lanId,null);
		
		try {
			dsstoxRecordService.createBatchSQLNoCompound(records);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		System.out.println(records.size());
		
	}
	
	void updateFkCompoundIdUsingCompoundsRecords() {
		boolean requireSID=true;
		
		int batchSize=10000;
		
		int batch=0;//where to start loading from db
		
		int totalCount=0;
		
		
		String name="DSSTOX Snapshot 04/23";
		DsstoxSnapshot snapshot=getSnapshot(name);
		
					
		while (true) {
		
			List<DsstoxCompound>dsstoxCompounds=getCompoundsBySQL(batch*batchSize, batchSize,requireSID);

			if (dsstoxCompounds.size()==0) break;
			
//			System.out.println(dsstoxCompounds.size());

			totalCount+=dsstoxCompounds.size();

			List<DsstoxRecord> records = DsstoxRecord.getRecords(dsstoxCompounds, snapshot,lanId);
			try {
				dsstoxRecordService.updateFkCompoundsIdBatchSQL(records,lanId);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			
//			if(true)break;

			batch++;
			System.out.println(batch+"\t"+batch*batchSize+"\t"+totalCount);

//			if(true) break;
		}
	}
	
	void updateMolWeightUsingCompoundsRecords() {
		boolean requireSID=true;
		
		int batchSize=10000;
		
		int batch=0;//where to start loading from db
		
		int totalCount=0;
		
		String name="DSSTOX Snapshot 04/23";
		DsstoxSnapshot snapshot=getSnapshot(name);
		
					
		while (true) {
		
			List<DsstoxCompound>dsstoxCompounds=getCompoundsBySQL(batch*batchSize, batchSize,requireSID);

			if (dsstoxCompounds.size()==0) break;
			
//			System.out.println(dsstoxCompounds.size());

			totalCount+=dsstoxCompounds.size();

			List<DsstoxRecord> records = DsstoxRecord.getRecords(dsstoxCompounds, snapshot,lanId);
			try {
				dsstoxRecordService.updateMolWeightBatchSQL(records, lanId);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			
//			if(true)break;

			batch++;
			System.out.println(batch+"\t"+batch*batchSize+"\t"+totalCount);

//			if(true) break;
		}
	}
	
	
	
	void updatePreferredNameCASRNUsingCompoundsRecords() {
		boolean requireSID=true;
		
		int batchSize=10000;
		
		int batch=0;//where to start loading from db
		
		int totalCount=0;
		
		String name="DSSTOX Snapshot 04/23";
		DsstoxSnapshot snapshot=getSnapshot(name);
		
					
		while (true) {
		
			List<DsstoxCompound>dsstoxCompounds=getCompoundsBySQL(batch*batchSize, batchSize,requireSID);

			if (dsstoxCompounds.size()==0) break;
			
//			System.out.println(dsstoxCompounds.size());

			totalCount+=dsstoxCompounds.size();

			List<DsstoxRecord> records = DsstoxRecord.getRecords(dsstoxCompounds, snapshot,lanId);
			try {
				dsstoxRecordService.updatePreferredNameCASRNBatchSQL(records, lanId);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			
//			if(true)break;

			batch++;
			System.out.println(batch+"\t"+batch*batchSize+"\t"+totalCount);

//			if(true) break;
		}
	}

	void standardize(List<DsstoxCompound>compounds,HashMap<String,String>hmStandardized) {
		
		boolean full=false;
		
		for(DsstoxCompound compound:compounds) {
			
			if (compound.getSmiles()==null) continue;
			
			if (hmStandardized.containsKey(compound.getSmiles())) {
//				System.out.println("Have in standardized smiles in DB:"+compound.getSmiles()+"\t"+hmStandardized.get(compound.getSmiles()));
			} else {
//				System.out.println("Don't Have in DB:"+compound.getSmiles());
//				smilesToBatchStandardize.add(compound.getSmiles());
				
				HttpResponse<String>response=sciDataExpertsStandardizer.callQsarReadyStandardizePost(compound.getSmiles(),full);
				
				if (response.getStatus()!=200) {
					System.out.println("Standardizer API failed");
					continue;
				}
				
				String standardizedSmiles=sciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(response.getBody(),full);
				
				Compound c = new Compound(compound.getDsstoxCompoundId(), compound.getSmiles(), standardizedSmiles, this.standardizerName, lanId);
				
				System.out.println(c.getDtxcid()+"\t"+c.getSmiles()+"\t"+c.getCanonQsarSmiles());
				
			}
		}

		
		
		//To do batch insert of compounds here
		
	}
	
	
//	private Map<String, String> batchStandardizeSmiles(List<String> smilesToBatchStandardize) {
//		Map<String, String> standardizedSmilesMap = new HashMap<String, String>();
//		BatchStandardizeResponseWithStatus batchStandardizeResponse = sciDataExpertsStandardizer.callBatchStandardize(smilesToBatchStandardize);
//		
//		if (batchStandardizeResponse.status==200) {
//			BatchStandardizeResponse batchStandardizeResponseData = batchStandardizeResponse.batchStandardizeResponse;
//			
//			if (batchStandardizeResponseData.success) {
//				List<Standardization> standardizations = batchStandardizeResponseData.standardizations;
//				for (Standardization standardization:standardizations) {
//					standardizedSmilesMap.put(standardization.smiles, standardization.standardizedSmiles);
//					
//					System.out.println(standardization.smiles+"\t"+standardization.standardizedSmiles);
//					
//				}
//			} else {
//				System.out.println("Batch standardization failed");
//			}
//		} else {
//			System.out.println("Batch standardizer HTTP response failed with code " + batchStandardizeResponse.status);
//		}
//		
//		return standardizedSmilesMap;
//	}

	
	
	private HashMap<String,String> getQsarSmilesLookupFromDB(String standardizerName) {
		HashMap<String,String>htQsarSmiles=new HashMap<>();

		List<Compound>standardizedCompounds=compoundService.findAllWithStandardizerSmilesNotNull(standardizerName);
		
		System.out.println("Number of standardized compounds in db:"+standardizedCompounds.size());
		
		for (Compound compound:standardizedCompounds) {
//			System.out.println(compound.getSmiles()+"\t"+compound.getCanonQsarSmiles());
			htQsarSmiles.put(compound.getSmiles(), compound.getCanonQsarSmiles());
		}
		return htQsarSmiles;
		
	}

	void testStandardize() {
//		String smiles="CCCC.CCCCCO";
		String smiles="XXXXsdf;ls;dlf";
		boolean full=false;
		HttpResponse<String>response=sciDataExpertsStandardizer.callQsarReadyStandardizePost(smiles,full);
		
		System.out.println(response.getStatus());
		
		String smilesQsarReady=sciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(response.getBody(),full);
		System.out.println(smilesQsarReady);
		

	}
	
	/**
	 * Adds foreign key to dsstox_records table
	 * This  method isnt needed anymore since the extra columns have been deleted from PredictionDashboard
	 */
	void updatePredictionsDashboardWithDsstoxRecordsFK() {
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String name="DSSTOX Snapshot 04/23";
		Hashtable<String,Long>htCID_to_Record_Id=dsstoxRecordService.getRecordIdHashtable(getSnapshot(name),"dtxcid");
		
		System.out.println("retrieved snapshot ht");
	
		String sql="select dtxcid from qsar_models.predictions_dashboard pd where fk_dsstox_records_id is null";

		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
				
		try {
			conn.setAutoCommit(false);

			PreparedStatement prep = conn.prepareStatement(
					"UPDATE qsar_models.predictions_dashboard SET fk_dsstox_records_id = ?, updated_by = ?, updated_at=current_timestamp WHERE dtxcid = ?");

			long t1=System.currentTimeMillis();
			
			int counter=0;
			int batchSize=10000;
			
			while (rs.next()) {
				
				counter++;
				
				String dtxcid=rs.getString(1);

				if (htCID_to_Record_Id.get(dtxcid)==null) {
					System.out.println(dtxcid +" not found in dsstox_records table");
					continue;
				}
				
				Long fk_dsstox_records_id=htCID_to_Record_Id.get(dtxcid);

				prep.setLong(1,fk_dsstox_records_id);
				prep.setString(2,lanId);
				prep.setString(3,dtxcid);
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					System.out.println("\t"+counter);
					prep.executeBatch();
					conn.commit();
				}
				
//				if (true) break;

			}
			
			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+counter+" update using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	

	/**
	 * Looks at old records in prev snapshot and compare to records taken just after new snapshot (so wont have missing records in dsstox_records table 
	 */
	void compareCompoundsInJsonWithDsstox() {
		//TODO Compare records in following jsons with what's in the previous snapshot
		//prod_compounds_updated_gte_2024-11-12.json
		//prod_compounds_updated_gte_2024-11-12_markush.json 
		
		try {
			String folder="data\\dsstox\\snapshot-2024-11-12\\json\\";
			String filepath=folder+"prod_compounds_updated_gte_2024-11-12.json";

			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();

			List<DsstoxCompound>dsstoxCompounds=Utilities.gson.fromJson(new FileReader(filepath), listOfMyClassObject);
		
			List<String>dtxsids=new ArrayList<>();
			List<String>dtxcids=new ArrayList<>();
			
			for (DsstoxCompound dsstoxCompound:dsstoxCompounds) {
				dtxsids.add(dsstoxCompound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
				dtxcids.add(dsstoxCompound.getDsstoxCompoundId());
			}			
			
//			GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
//			List<gov.epa.databases.dsstox.DsstoxRecord>dsstoxRecordsOldSnapshot=gss.findAsDsstoxRecordsByDtxsidIn(dtxsids);

			DsstoxCompoundServiceImpl dss=new DsstoxCompoundServiceImpl();
			List<gov.epa.databases.dsstox.entity.DsstoxCompound>dsstoxRecordsOldSnapshot=dss.getCompoundsBySQL(dtxcids);
			
			Hashtable<String,gov.epa.databases.dsstox.entity.DsstoxCompound>htOldSnapshot=new Hashtable<>();
			
			for(gov.epa.databases.dsstox.entity.DsstoxCompound dsstoxCompound:dsstoxRecordsOldSnapshot) {				
//				System.out.println(dsstoxCompound.getDsstoxCompoundId());
				htOldSnapshot.put(dsstoxCompound.getDsstoxCompoundId(),dsstoxCompound);
			}
			System.out.println("\n\n");
			
			List<gov.epa.databases.dsstox.entity.DsstoxCompound>oldRecords=new ArrayList<>();
			
			
			for (DsstoxCompound dsstoxCompound:dsstoxCompounds) {
//				System.out.println(Utilities.gson.toJson(dsstoxCompound));
//				System.out.println(dsstoxCompound.getJchemInchikey());
				
				String dtxcid=dsstoxCompound.getDsstoxCompoundId();
				
				if(htOldSnapshot.containsKey(dtxcid)) {
					
					gov.epa.databases.dsstox.entity.DsstoxCompound dsstoxRecordOld=htOldSnapshot.get(dtxcid);
					
					if(dsstoxRecordOld.getGenericSubstanceCompound()==null) {
						System.out.println(dtxcid+"\tOld substance is null for this dtxcid");
						continue;
					}

					boolean match=dsstoxRecordOld.getJchemInchikey().contentEquals(dsstoxCompound.getJchemInchikey());
					
					System.out.println(dtxcid+"\t"+dsstoxRecordOld.getJchemInchikey()+"\t"+dsstoxCompound.getJchemInchikey()+"\t"+match);
					
					oldRecords.add(dsstoxRecordOld);
					
				} else {
//					System.out.println(dtxcid+"\tNot in old snapshot");//new compounds
				}
			}
			
			System.out.println(dsstoxCompounds.size()+"\t"+htOldSnapshot.size()+"\t"+oldRecords.size());
			try {
				
				FileWriter fw=new FileWriter(folder+"previous snapshot compounds that were updated after 2024-11-12.json");
				fw.write(Utilities.gson.toJson(oldRecords));
				fw.flush();
				fw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void compareCompoundsInJsonWithDsstoxMarkush() {
		//TODO Compare records in following jsons with what's in the previous snapshot
		//prod_compounds_updated_gte_2024-11-12.json
		//prod_compounds_updated_gte_2024-11-12_markush.json 
		
		try {
			String folder="data\\dsstox\\snapshot-2024-11-12\\json\\";
			String filepath=folder+"prod_compounds_updated_gte_2024-11-12_markush.json";

			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();

			List<DsstoxCompound>dsstoxCompounds=Utilities.gson.fromJson(new FileReader(filepath), listOfMyClassObject);
		
			List<String>dtxsids=new ArrayList<>();
			List<String>dtxcids=new ArrayList<>();
			
			for (DsstoxCompound dsstoxCompound:dsstoxCompounds) {
				dtxsids.add(dsstoxCompound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
				dtxcids.add(dsstoxCompound.getDsstoxCompoundId());
			}			
			
//			GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
//			List<gov.epa.databases.dsstox.DsstoxRecord>dsstoxRecordsOldSnapshot=gss.findAsDsstoxRecordsByDtxsidIn(dtxsids);

			DsstoxCompoundServiceImpl dss=new DsstoxCompoundServiceImpl();
			List<gov.epa.databases.dsstox.entity.DsstoxCompound>dsstoxRecordsOldSnapshot=dss.getCompoundsBySQL(dtxcids);
			
			Hashtable<String,gov.epa.databases.dsstox.entity.DsstoxCompound>htOldSnapshot=new Hashtable<>();
			
			for(gov.epa.databases.dsstox.entity.DsstoxCompound dsstoxCompound:dsstoxRecordsOldSnapshot) {				
//				System.out.println(dsstoxCompound.getDsstoxCompoundId());
				htOldSnapshot.put(dsstoxCompound.getDsstoxCompoundId(),dsstoxCompound);
			}
//			System.out.println("\n\n");
			
			List<gov.epa.databases.dsstox.entity.DsstoxCompound>oldRecords=new ArrayList<>();
			
			
			for (DsstoxCompound dsstoxCompound:dsstoxCompounds) {
//				System.out.println(Utilities.gson.toJson(dsstoxCompound));
//				System.out.println(dsstoxCompound.getJchemInchikey());
				
				String dtxcid=dsstoxCompound.getDsstoxCompoundId();
				
				if(htOldSnapshot.containsKey(dtxcid)) {
					
					gov.epa.databases.dsstox.entity.DsstoxCompound dsstoxRecordOld=htOldSnapshot.get(dtxcid);
					
					if(dsstoxRecordOld.getGenericSubstanceCompound()==null) {
						System.out.println(dtxcid+"\tOld substance is null");
						continue;
					}

					boolean match=dsstoxRecordOld.getJchemInchikey().contentEquals(dsstoxCompound.getJchemInchikey());
					System.out.println(dtxcid+"\t"+dsstoxRecordOld.getJchemInchikey()+"\t"+dsstoxCompound.getJchemInchikey()+"\t"+match);
					oldRecords.add(dsstoxRecordOld);
					
				} else {
					System.out.println(dtxcid+"\tNot in old snapshot");//new compounds
				}
			}
			
			System.out.println(dsstoxCompounds.size()+"\t"+htOldSnapshot.size()+"\t"+oldRecords.size());
			try {
				
				FileWriter fw=new FileWriter(folder+"previous markush snapshot compounds that were updated after 2024-11-12.json");
				fw.write(Utilities.gson.toJson(oldRecords));
				fw.flush();
				fw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	void create_Snapshot_11_2024() {

//		compareCompoundsInJsonWithDsstox();//gets the records that were changed in the 1-2 days after the snapshot date and replace with prev snapshot record
//		compareCompoundsInJsonWithDsstoxMarkush();//no records
		
		//this uses the records that were taken from dsstox to create the dsstox_records for Nov 24 snapshot:
		createDsstoxRecordsUsingCompoundsRecordsFromJsons();
		
		
		//TODO add records where dtxcid are null - see createDsstoxRecordsUsingCompoundsRecordsNoDTXCID()
		// Might not need them since have no experimental/predicted values with no dtxcid in materialized views
		// and need for opera neighbors

		//compare records that were changed after the snapshot data with what's in dsstox_records:
//		compareJsonToDsstoxRecordsInDB();
		
	}


	/**
	 * Looks like chemicals changed after the snapshot date (Nov 12 &13, 2024), didnt change the structure
	 */
	private void compareJsonToDsstoxRecordsInDB() {
		try {
			Type listDsstoxCompoundType = new TypeToken<List<DsstoxCompound>>() {}.getType();
			String folder="data\\dsstox\\snapshot-2024-11-12\\json\\";
			File file=new File(folder+"/prod_compounds_updated_gte_2024-11-12.json");
			List<DsstoxCompound>dsstoxCompounds=Utilities.gson.fromJson(new FileReader(file), listDsstoxCompoundType);
//			System.out.println(dsstoxCompounds.size());
			
			Connection conn=SqlUtilities.getConnectionPostgres();
			
			for(DsstoxCompound dsstoxCompound:dsstoxCompounds) {
				
				String dtxsid=dsstoxCompound.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId();
				
				String sql="select * from qsar_models.dsstox_records where dtxsid='"+dtxsid+"' and fk_dsstox_snapshot_id=2;";
				ResultSet rs=SqlUtilities.runSQL2(conn, sql);
				
				if(rs.next()) {
					String smiles=rs.getString(6);
					String indigoInchikey=rs.getString(15);
					
					if(indigoInchikey==null || dsstoxCompound.getIndigoInchikey()==null) continue;

					if(smiles.contentEquals(dsstoxCompound.getSmiles())) continue;

					if(indigoInchikey.contentEquals(dsstoxCompound.getIndigoInchikey())) continue;
					
					System.out.println(dtxsid+"\t"+smiles+"\t"+	dsstoxCompound.getSmiles());
					
				} else {
//					System.out.println(dtxsid+"\tnot in dsstox_records table");
				}
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		DsstoxSnapshotCreatorScriptDSSTOX d=new DsstoxSnapshotCreatorScriptDSSTOX();

		d.create_Snapshot_11_2024();

//		d.getSnapshot();
		
//		List<DsstoxCompound>compounds=d.getCompoundsBySQL(0, 10,true);
		
//		 d.createDsstoxRecordsUsingCompoundsRecords();
//		 d.createDsstoxRecordsUsingCompoundsRecordsNoDTXCID();

		
		 
//		 d.updateFkCompoundIdUsingCompoundsRecords();
//		 d.updateMolWeightUsingCompoundsRecords();
//		d.updatePreferredNameCASRNUsingCompoundsRecords();
		
//		d.getRecordIdHashtable(d.getSnapshot());
		
//		d.updatePredictionsDashboardWithDsstoxRecordsFK();
		
//		d.testStandardize();
		
//		 d.getQsarSmilesLookupFromDB("SCI_DATA_EXPERTS_QSAR_READY");
		

	}

}
