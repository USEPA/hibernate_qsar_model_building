package gov.epa.endpoints.datasets.descriptor_values;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.CDL;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class ToxPrintsTests {

//	@Test
	public void compareTop20HazardChemicalsToBatchSearch() {
		String filename="batch search toxprints top20 hazard chemicals.csv";
		String descriptorSetName="ToxPrints-default";
		runChemicalsFromBatchCSV(filename, descriptorSetName);
	}
	
	@Test
	public void compare5000_TSCA_ChemicalsToBatchSearch() {
		String filename="batch search toxprints 5000 random TSCA chemicals.csv";
		String descriptorSetName="ToxPrints-default";
		runChemicalsFromBatchCSV(filename, descriptorSetName);
	}


	private void runChemicalsFromBatchCSV(String filename, String descriptorSetName) {
		
		try {
			
			int mismatchCount=0;

			Connection conn=SqlUtilities.getConnectionPostgres();
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);	         

			String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
			String json = CDL.toJSONArray(csvAsString).toString();
			inputStream.close();
			
			JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
//			System.out.println("Number of records in csv:"+ja.size());


			String sqlHeader="select headers_tsv from qsar_descriptors.descriptor_sets ds\n"+
					"where ds.name='"+descriptorSetName+"';";
			
			String header=SqlUtilities.runSQL(conn, sqlHeader);
			String [] headers=header.split("\t");
			
			int count=0;
			
			System.out.println("Smiles\tToxPrint\tvalue_api\tvalue_dashboard");

			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				String SMILES=jo.get("SMILES").getAsString();
				
				if(SMILES.equals("N/A")) continue;
				
				//TODO should we check the single fingerprints from the batch search as well?
				String TOXPRINTS_FINGERPRINT=jo.get("TOXPRINTS_FINGERPRINT").getAsString();
				
//				System.out.println(SMILES);
				
				
				String sql = "select values_tsv from qsar_descriptors.descriptor_values\n"+
						"join qsar_descriptors.descriptor_sets ds on ds.id = descriptor_values.fk_descriptor_set_id\r\n"
						+ "where ds.name='"+descriptorSetName+"' and canon_qsar_smiles='"+SMILES+"'";
				
				String values_tsv=SqlUtilities.runSQL(conn, sql);
				
//				System.out.println("SMILES="+SMILES);
				

				if(values_tsv==null) {
					if (TOXPRINTS_FINGERPRINT.equals("N/A")) {
//						System.out.println("*1* Dont have fingerprint from both\t"+SMILES);
					} else {
						System.out.println("*2* Dont have fingerprint from our API but have in dashboard\t"+SMILES);				continue;
//						mismatchCount++;
					}
					continue;
					
				} else {
					if(TOXPRINTS_FINGERPRINT.equals("N/A")) {
//						System.out.println("*3* Have fingerprint from our API but not dashboard\t"+SMILES);
						continue;
					}
				}
				
				
				String [] values=values_tsv.split("\t");
				String [] values_dashboard=TOXPRINTS_FINGERPRINT.split("\t");
				
				for (int j=0;j<values.length;j++) {
					
					Double value_api=Double.parseDouble(values[j]);
					Double value_dashboard=Double.parseDouble(values_dashboard[j]);
					
//					if (value_api>0) {
//						System.out.println(SMILES+"\t"+headers[j]+"\t"+j+"\t"+value_api+"\t"+value_dashboard);
//					}
					
					if (value_api>0 || value_dashboard>0) {
						if(!value_api.equals(value_dashboard)) {
							System.out.println(SMILES+"\t"+headers[j]+"\t"+value_api+"\t"+value_dashboard);	
							mismatchCount++;
//							break;
						}
					}
				}
				
				
//				System.out.println(SMILES+"\t"+values_dashboard.length+"\t"+values.length);
				count++;
				
			}

			System.out.println(count+"\tcompared");
			assertEquals(0, mismatchCount);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
