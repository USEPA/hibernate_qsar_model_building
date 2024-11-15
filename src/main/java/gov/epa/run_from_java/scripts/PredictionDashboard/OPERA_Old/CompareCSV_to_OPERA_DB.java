package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA_Old;

import org.json.CDL;
import org.openscience.cdk.qsar.descriptors.bond.BondSigmaElectronegativityDescriptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
/**
* @author TMARTI02
*/
public class CompareCSV_to_OPERA_DB {

	Connection conn=SqlUtilities.getConnectionDSSTOX();
	
//Following properties not in SCDCD's OPERA??? 	
//OPERA_CACO2
//	OPERA_Clint
//	OPERA_FUB

	boolean printCIDNotFound=false;
	boolean printModelUnitsMismatch=false;
	boolean printNeighborExpMismatch=false;
	boolean printExpMismatch=false;
	boolean printNeighborPredMismatch=false;
	
	void sampleCSV_to_Json(File file) {
		
		
		try {
		
			InputStream inputStream = new FileInputStream(file);
			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
			
			System.out.println(file.getName()+"\t"+ja.size());
			
			int count=100;
			
//			Connection connDsstox=SqlUtilities.getConnectionDSSTOX();
			
			JsonArray ja2=new JsonArray();
			
			DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
			
			for (int i=1;i<=count;i++) {
				int rnd = new Random().nextInt(ja.size());
				JsonObject jo=(JsonObject)ja.remove(rnd);
				
				String cid=jo.get("DSSTOX_COMPOUND_ID").getAsString();

//				List<DsstoxRecord>recs=compoundService.findAsDsstoxRecordsByDtxcid(cid);
//				String sid=recs.get(0).getDsstoxSubstanceId();

				jo.addProperty("DSSTOX_SUBSTANCE_ID", getSID(cid));
				ja2.add(jo);
			}
			
//			System.out.println(file.getName()+"\t"+ja2.size());
//			System.out.println(json);
        
			String fileNameNew=file.getName().replace(".csv", ".json");
			
            Utilities.saveJson(ja2, "data\\opera\\csv2\\json random sample\\"+fileNameNew);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	void createRandomSamples(String folder) {
		
		File Folder=new File(folder);
		
		for (File file:Folder.listFiles()) {
			
			if (file.getName().contains("CACO2") || file.getName().contains("Clint") || 
					file.getName().contains("FUB") || file.getName().contains("liq_chrom_Retention_Time")) continue; 
			
			if(!file.getName().contains(".csv")) {
				continue;
			}
			sampleCSV_to_Json(file);
			
			System.out.println(file.getName());
 		}
	}
	
	
	void compareFiles(String folder) {
		
		File Folder=new File(folder);
		
		for (File file:Folder.listFiles()) {
			
			if(!file.getName().contains(".json")) {
				continue;
			}
			compareToOPERA_DB(file);
			
 		}
	}
	
	void compareToOPERA_DB(File file) {
		Reader reader;
		try {
			reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
			JsonArray ja=Utilities.gson.fromJson(reader, JsonArray.class);
			
			Connection connPG=SqlUtilities.getConnectionPostgres();
			
			String property=file.getName().substring(0,file.getName().indexOf("."));
			
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				
				
				RecordOpera ro=Utilities.gson.fromJson(jo, RecordOpera.class);				
				String sid=ro.DSSTOX_SUBSTANCE_ID;
				String cid=ro.DSSTOX_COMPOUND_ID;
				

				String modelName=file.getName().substring(0,file.getName().indexOf("."));
				
				String sql="select \"modelResults\" ,\"nearestNeighbors\" from opera.opera_data od "
						+ "where od.dtxsid ='"+sid+"' and \"modelName\" ='"+modelName+"'";
				
//				System.out.println(sql);
				
				ResultSet rs=SqlUtilities.runSQL2(connPG, sql);
				
				if (rs.next()) {
					String modelResults=rs.getString(1);
					String nearestNeighbors=rs.getString(2);
					
					JsonObject joModelResults=Utilities.gson.fromJson(modelResults, JsonObject.class);
					
					if (joModelResults==null) {
						System.out.println(property+"\t"+sid+"\tjoModelResults==null");
						continue;
					}
					
					String pred="NA";
					
					if (!joModelResults.get("predicted").isJsonNull()) {
						pred=joModelResults.get("predicted").getAsString();
					}
//					String pred=joModelResults.get("predicted").getAsString();
					
//					System.out.println(ro.exp);
					
					
					String dbUnits="NA";
					
					if (!joModelResults.get("standardUnit").isJsonNull()) {
						dbUnits=joModelResults.get("standardUnit").getAsString();
					}
 					
					if(!ro.model_units.equals(dbUnits) && printModelUnitsMismatch) {
						System.out.println(property+"\t"+sid+"\tmodel_units mismatch:"+ro.model_units+"\t"+dbUnits);
					}
					
					if (!ro.Conf_index.equals(joModelResults.get("confidence").getAsString())) {
						System.out.println(property+"\t"+sid+"\tconf_index mismatch");
					}
					

					if (!ro.AD.equals(joModelResults.get("global").getAsString())) {
						System.out.println(property+"\t"+sid+"\tAD mismatch");
					}
					
					if (!ro.AD_index.equals(joModelResults.get("local").getAsString())) {
						System.out.println(property+"\t"+sid+"\tAD_index mismatch");
					}

					
					if (!ro.exp.isEmpty()) {
						
						if (joModelResults.get("experimental").isJsonNull()) {
							if (!ro.exp.equals("NA") && printExpMismatch)
								System.out.println(property+"\t"+sid+"\t"+ro.exp+"\tdb_exp=null");
						} else {
							String exp=joModelResults.get("experimental").getAsString();
							if (!ro.exp.equals(exp)) {
								System.out.println(property+"\t"+sid+"\texp mismatch");
							}
						}
						
					}
					
					if(ro.pred.isEmpty())ro.pred="NA";
					
					if (!ro.pred.equals(pred)) {
						System.out.println(property+"\t"+sid+"\tpred mismatch:"+"*"+ro.pred+"*"+"\t*"+pred+"*");
					}
					
					
					checkNeighbors(file, ro, sid, nearestNeighbors);
					
					//TODO look at model stats
					
//					System.out.println(sid);
//					System.out.println(modelResults);
//					System.out.println(nearestNeighbors);
				} else {
					if(printCIDNotFound) System.out.println(property+"\t"+cid+"\tnot found");
				}
				
//				if(true) break;
				
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

	private void checkNeighbors(File file, RecordOpera ro, String sid, String nearestNeighbors)
			throws NoSuchFieldException, IllegalAccessException {
		JsonArray jaNeighbors=Utilities.gson.fromJson(nearestNeighbors, JsonArray.class);
		
		for (int j=1;j<=5;j++) {
			compareNeighborExpAndPred(file, ro, sid, jaNeighbors, j);
			compareNeighborSID(file, ro, sid, jaNeighbors, j);
		}
	}

	private void compareNeighborExpAndPred(File file, RecordOpera ro, String sid, JsonArray jaNeighbors, int num)
			throws NoSuchFieldException, IllegalAccessException {
		
		String property=file.getName().substring(0,file.getName().indexOf("."));

		
		Field field2=ro.getClass().getField("Exp_neighbor_"+num);
		String exp=(String) field2.get(ro);
		
		if (exp.isEmpty()) exp="NA";

		field2=ro.getClass().getField("pred_neighbor_"+num);
		String pred=(String) field2.get(ro);

		
		String measured="NA";
		String result_value="NA";
		
		for (int k=0;k<jaNeighbors.size();k++) {
			JsonObject joNeighbor=jaNeighbors.get(k).getAsJsonObject();
			
			int neighbor_number=Integer.parseInt(joNeighbor.get("neighbor_number").getAsString());
			
			if(neighbor_number==num) {
				if (!joNeighbor.get("measured").isJsonNull()) {
					measured=joNeighbor.get("measured").getAsString();
				}
				
				if (!joNeighbor.get("result_value").isJsonNull()) {
					result_value=joNeighbor.get("result_value").getAsString();
					if (result_value.isEmpty()) result_value="NA";
				}
				
				break;
			}
		}

		if (!exp.equals(measured) && printNeighborExpMismatch) {
			System.out.println(property+"\t"+sid+"\tneighbor exp mismatch for num="+num+":*"+exp+"*\t"+"*"+measured+"*");	
			
		}
		
		if (pred.isEmpty()) pred="NA";
		
		if (!pred.equals(result_value) && printNeighborPredMismatch) {
			System.out.println(property+"\t"+sid+"\tneighbor pred mismatch for num="+num+":"+"*"+pred+"*"+"\t"+"*"+result_value+"*");	
		}
	}

	private void compareNeighborSID(File file, RecordOpera ro, String sid, JsonArray jaNeighbors, int j)
			throws NoSuchFieldException, IllegalAccessException {
		
		String property=file.getName().substring(0,file.getName().indexOf("."));

		
		Field field=ro.getClass().getField("DTXSID_neighbor_"+j);
		String SID=(String) field.get(ro);

		String [] SIDs=SID.split("\\|");
		List<String>listSID=new ArrayList<>();
		for (String strSID:SIDs) listSID.add(strSID);
		Collections.sort(listSID);

		List<String>listSID2=new ArrayList<>();
		
		for (int k=0;k<jaNeighbors.size();k++) {
			JsonObject joNeighbor=jaNeighbors.get(k).getAsJsonObject();
			
			int neighbor_number=Integer.parseInt(joNeighbor.get("neighbor_number").getAsString());
			
			if(neighbor_number==j) {
				listSID2.add(joNeighbor.get("dtxsid").getAsString());
			}
		}
		
		Collections.sort(listSID2);
		
		
		boolean neighborSIDsMatch=true;
		
		for (int k=0;k<listSID.size();k++) {
			
			String strSID1=listSID.get(k);
			
			if(strSID1.isEmpty()) continue;
			
			if (listSID2.size()==0) {
				System.out.println(property+"\t"+sid+"\tSomething wrong with neighbor SID in DB for num = "+j);
				
				return;
			}


			String strSID2=listSID2.get(k);

			if (!strSID1.equals(strSID2)) {
				neighborSIDsMatch=false;
				break;
			}
			
		}
		
		if(!neighborSIDsMatch) {
			System.out.println(property+"\t"+sid+"\tneighbor SID mismatch");
		}
	}
	
	
	String getSID(String cid) {
		String sql="select distinct gs.dsstox_substance_id\r\n"
		 		+ "from compounds c\r\n"
		 		+ "left join generic_substance_compounds gsc2 on gsc2.fk_compound_id =c.id\r\n"
		 		+ "left join generic_substances gs on gs.id =gsc2.fk_generic_substance_id  \r\n"
		 		+ "where c.dsstox_compound_id = '"+cid+"'";
		 
		 String sid=SqlUtilities.runSQL(conn, sql);
		 return sid;
		
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		 CompareCSV_to_OPERA_DB c=new  CompareCSV_to_OPERA_DB();
		 c.createRandomSamples("data\\opera\\csv2");
		 
//		 String sid=c.getSID("DTXCID8071063");
//		 System.out.println(sid);
		 
		 c.compareToOPERA_DB(new File("data\\opera\\csv2\\json random sample\\OPERA_AOH.json"));

		 c.compareFiles("data\\opera\\csv2\\json random sample");
		 
	}

}
