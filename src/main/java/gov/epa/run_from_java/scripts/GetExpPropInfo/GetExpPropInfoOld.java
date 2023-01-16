package gov.epa.run_from_java.scripts.GetExpPropInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundService;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.util.wekalite.CSVLoader;
import gov.epa.util.wekalite.Instances;

public class GetExpPropInfoOld {



	
	/**
	 * Old flawed way of getting records- which assumes you can get CID based on the qsar ready 
	 * smiles in the compounds table (instead of new value stored in data_points table
	 * 
	 * It does a series of sql queries which is inefficient but doesnt take that long
	 * 
	 */
	static void createCheckingSpreadsheetOld() {

		String folder="data\\dev_qsar\\dataset_files\\";
		Connection conn=DatabaseLookup.getConnection();
		Connection connDSSTOX=DatabaseLookup.getConnectionDSSTOX();					

		int id_dataset=31;
		String inputFileName="Standard Water solubility from exp_prop_T.E.S.T. 5.1_T=PFAS only, P=PFAS_prediction.tsv";
		String outputFileName="Water solubility PFAS prediction records.json";
		getRecords(id_dataset,conn, connDSSTOX, folder, inputFileName,outputFileName);

		inputFileName="Standard Water solubility from exp_prop_T.E.S.T. 5.1_T=PFAS only, P=PFAS_training.tsv";
		outputFileName="Water solubility PFAS training records.json";
		getRecords(id_dataset,conn, connDSSTOX, folder, inputFileName,outputFileName);

		JsonArray jaTraining=Utilities.getJsonArrayFromJsonFile(folder+"Water solubility PFAS training records.json");
		JsonArray jaPrediction=Utilities.getJsonArrayFromJsonFile(folder+"Water solubility PFAS prediction records.json");
		JsonArray jaOverall=new JsonArray();
		jaOverall.addAll(jaTraining);
		jaOverall.addAll(jaPrediction);
		System.out.println(jaOverall.size());

		String excelFilePath=folder+"Water solubility PFAS records.xlsx";
		ExcelCreator.createExcel(jaOverall, excelFilePath);

	}
	
	
	/**
	 * Creates excel file for a dataset (flattened)
	 * 
	 * @param id_dataset
	 * @param conn
	 * @param connDSSTOX
	 * @param folder
	 */
	public static void getDataSetDataFlat(long id_dataset,Connection conn,Connection connDSSTOX,String folder) {


		try {

			Statement st = conn.createStatement();

			String sql=DatabaseLookup.createDatapointsQuery(id_dataset);

			ResultSet rs = st.executeQuery(sql);
			JsonArray jaRecords=new JsonArray();
			String qsar_units=DatabaseLookup.lookupQSAR_units(id_dataset, conn);


			//			System.out.println(qsar_units);

			int counter=1;

			Hashtable<String,String>htMW=new Hashtable<>();

			while (rs.next()) {

				JsonObject jo=new JsonObject();

				jo.addProperty("id_data_points",rs.getString(1));
				jo.addProperty("canon_qsar_smiles",rs.getString(2));
				jo.addProperty("qsar_property_value",rs.getString(5));

				String dtxcid=rs.getString(10);
				jo.addProperty("dtxcid",dtxcid);
				jo.addProperty("qsar_property_units", qsar_units);

				if (htMW.get(dtxcid)==null) {					

					if (dtxcid.contains("|")) {
						dtxcid=dtxcid.substring(0,dtxcid.indexOf("|")-1);
						//						System.out.println(dtxcid+"\t"+lookupMolWt(dtxcid, connDSSTOX));
					}

					String mol_weight=DatabaseLookup.lookupMolWt(dtxcid, connDSSTOX);
					jo.addProperty("mol_weight",mol_weight);
					htMW.put(dtxcid, mol_weight);
				} else {
					jo.addProperty("mol_weight",htMW.get(dtxcid));
				}


				if (counter%10==0) System.out.println(counter);
				//				System.out.println(gson.toJson(jo));
				jaRecords.add(jo);
				counter++;

				//				if (counter==101) break;

			}

			String dataSetName=DatabaseLookup.getDataSetName(id_dataset, conn);
			//			System.out.println(dataSetName);

			
			FileWriter fw=new FileWriter(folder+dataSetName+"//"+dataSetName+"_flat.json");			
			fw.write(Utilities.gson.toJson(jaRecords));
			fw.flush();
			fw.close();

			String [] fields= {"id_data_points","canon_qsar_smiles","qsar_property_value","qsar_property_units",
					"dtxcid","mol_weight"};

			ExcelCreator.createExcel2(jaRecords, folder+dataSetName+"//"+dataSetName+"_flat.xlsx",fields,null);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}



	}
	
	/**
	 * Creates excel file for checking a dataset (not flattened)
	 * 
	 * This method is not needed since we have mapped records spreadsheet at data\dev_qsar\output\datasetName\datasetName_mapped_records.json
	 * 
	 * @param id_dataset
	 * @param conn
	 * @param connDSSTOX
	 * @param folder
	 */
	public static void getDataSetData(long id_dataset,Connection conn,Connection connDSSTOX,String folder) {

		try {

			Statement st = conn.createStatement();
			String sql=DatabaseLookup.createDatapointsQuery2(id_dataset);

			System.out.println(sql);

			ResultSet rs = st.executeQuery(sql);
			JsonArray jaRecords=new JsonArray();

			String qsar_units=DatabaseLookup.lookupQSAR_units(id_dataset, conn);
			//			System.out.println(qsar_units);
			int counter=1;
			Hashtable<String,String>htMW=new Hashtable<>();
			int max=1000;

			DsstoxCompoundService d=new DsstoxCompoundServiceImpl();

			while (rs.next()) {
				JsonObject jo=new JsonObject();				

				jo.addProperty("id_data_points",rs.getString(1));
				jo.addProperty("canon_qsar_smiles",rs.getString(2));

				String dtxcid_final=rs.getString(3);
				jo.addProperty("dtxcid_final",dtxcid_final);

				jo.addProperty("qsar_property_value",rs.getString(4));				

				String str_exp_prop_id=rs.getString(5);
				Integer exp_prop_id=Integer.parseInt(str_exp_prop_id.replace("EXP", ""));

				//				if (exp_prop_id!=460913) continue;

				String dtxcid_mapped=rs.getString(6);

				//				if (!dtxcid_final.equals(dtxcid_mapped)) {
				//					System.out.println("Final CID doesnt match CID for current original record:\t"+dtxcid_final+"\t"+dtxcid_mapped);
				//				}

				jo.addProperty("dtxcid_mapped",dtxcid_mapped);

				DsstoxCompound dc=d.findByDtxcid(dtxcid_mapped);
				jo.addProperty("smiles_mapped",dc.getSmiles());
				jo.addProperty("dtxsid_mapped",dc.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
				jo.addProperty("qsar_property_units", qsar_units);

				if (htMW.get(dtxcid_mapped)==null) {
					String mol_weight=DatabaseLookup.lookupMolWt(dtxcid_mapped, connDSSTOX);
					jo.addProperty("mol_weight_mapped",mol_weight);
					htMW.put(dtxcid_mapped, mol_weight);
				} else {
					jo.addProperty("mol_weight_mapped",htMW.get(dtxcid_mapped));
				}

				jo.addProperty("exp_prop_id",exp_prop_id);
				DatabaseLookup.getAllValues(exp_prop_id, "exp_prop.property_values",conn, jo);

				DatabaseLookup.lookupParameters(exp_prop_id, conn,jo);

				jo.addProperty("units",DatabaseLookup.lookupUnits(jo.get("fk_unit_id").getAsInt(), conn));

				if (!jo.get("fk_literature_source_id").isJsonNull()) {
					int id=jo.get("fk_literature_source_id").getAsInt();
					DatabaseLookup.getAllValues(id,"exp_prop.literature_sources", conn, jo);
				}

				if (!jo.get("fk_public_source_id").isJsonNull()) {
					int id=jo.get("fk_public_source_id").getAsInt();
					DatabaseLookup.getAllValues(id,"exp_prop.public_sources", conn, jo);
				}

				int id=jo.get("fk_source_chemical_id").getAsInt();
				DatabaseLookup.getAllValues(id,"exp_prop.source_chemicals", conn, jo);

				jo.remove("access_date");
				jo.remove("created_at");
				jo.remove("updated_at");
				jo.remove("created_by");
				jo.remove("keep");
				//				
				jo.remove("id");
				jo.remove("fk_unit_id");				
				jo.remove("fk_property_id");

				if (counter%10==0) System.out.println(counter);

				//				System.out.println(gson.toJson(jo));
				jaRecords.add(jo);
				counter++;

				//				if (counter==max) break;

			}

			String dataSetName=DatabaseLookup.getDataSetName(id_dataset, conn);
			//			System.out.println(dataSetName);

			String filepath=folder+dataSetName+"//"+dataSetName+".json";

			Utilities.saveJson(jaRecords, filepath);

			Hashtable<String,String>htDescriptions=ExcelCreator.getColumnDescriptions();
			ExcelCreator.createExcel2(jaRecords, folder+dataSetName+"//"+dataSetName+".xlsx",GetExpPropInfo.fieldsFinal,htDescriptions);

			//			System.out.println(gson.toJson(jaRecords));

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}



	}
	

	
	private static void getRecords(int  id_dataset, Connection conn, Connection connDSSTOX, String folder, String inputFileName,String outputFileName) {
		CSVLoader c=new CSVLoader();
		try {
			Instances instances=c.getDataSetFromFile(folder+inputFileName,"\t");
			JsonArray ja=new JsonArray();

			for (int i=0;i<instances.numInstances();i++) {
				System.out.println(i);
				String smiles=instances.instance(i).getName();
				DatabaseLookup.getDataForSmiles(conn, connDSSTOX, smiles,id_dataset,ja);				
			}

			FileWriter fw=new FileWriter(folder+outputFileName);			
			fw.write(Utilities.gson.toJson(ja));
			fw.flush();
			fw.close();

			//			System.out.println(ja.size());


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {

		String folder="data\\dev_qsar\\output\\";
		Connection conn=DatabaseLookup.getConnection();
		Connection connDSSTOX=DatabaseLookup.getConnectionDSSTOX();					
		long dataset_id=88L;
		getDataSetData(dataset_id,conn,connDSSTOX,folder);//pulls data from the database
		
		createCheckingSpreadsheetOld();
//		lookupIdentifierFromRIDs(connDSSTOX);  //TODO make the createChecking code use this method too look CAS, name
		//		lookupOperaReferences();
		//		lookupEPISUITE_Isis_References();

	}
}
