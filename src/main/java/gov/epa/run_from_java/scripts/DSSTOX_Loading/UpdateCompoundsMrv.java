package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class UpdateCompoundsMrv {
	
	
	void updateCompoundsMrvFromCSV(String filepathMrv) {
		
		try {
			
			BufferedReader br =new BufferedReader(new FileReader(filepathMrv));
			String header=br.readLine();
//			System.out.println(header);
			List<DsstoxCompound> compounds = new ArrayList<>();
			
			while (true) {

				String Line=br.readLine();
				if(Line==null) break;
				if(!Line.contains("DTXCID")) continue;

				Line=Line.substring(Line.indexOf(",")+1,Line.length());
				String cid=Line.substring(0,Line.indexOf(","));
				for(int i=1;i<=5;i++) Line=Line.substring(Line.indexOf(",")+1,Line.length());

				String mrv=null;
				//					System.out.println(Line.indexOf("<cml")+"\t"+Line.indexOf("</cml>"));

				if (Line.indexOf("<cml")>-1 && Line.indexOf("</cml>")>-1) {
					//	System.out.println(Line.indexOf("<cml>"));
					//	System.out.println(Line.indexOf("</cml>"));
					mrv=Line.substring(Line.indexOf("<cml"),Line.indexOf("</cml>")+"</cml>".length());
//					System.out.println(cid+"\t"+mrv);
				} else {
					System.out.println("here:"+cid+"\t"+Line);
					continue;
				}
								
				mrv=mrv.replace("\"\"", "\"");
				
				DsstoxCompound compound=new DsstoxCompound();
				compound.setDsstoxCompoundId(cid);
				compound.setMrvFile(mrv);
				compounds.add(compound);
//				System.out.println(cid+"\t"+mrv);
			}
			
//			System.out.println(Utilities.gson.toJson(compounds));

			updateSQL(compounds, "tmarti02");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public void updateSQL(List<DsstoxCompound> compounds, String lanId) {

		Connection conn = SqlUtilities.getConnectionDSSTOX();

		String SQL_UPDATE = "UPDATE compounds SET mrv_file=?, updated_by=? WHERE dsstox_compound_id=?";

		int batchSize = 1000;

		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(SQL_UPDATE);

			long t1 = System.currentTimeMillis();

			for (int counter = 0; counter < compounds.size(); counter++) {
				// for (int counter = 0; counter < 1; counter++) {

				// System.out.println(counter);

				DsstoxCompound compound = compounds.get(counter);
				prep.setString(1, compound.getMrvFile());
				prep.setString(2, lanId);
				prep.setString(3, compound.getDsstoxCompoundId());
				prep.addBatch();

				if (counter % batchSize == 0 && counter != 0) {
					System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
			}
			
			int[] count = prep.executeBatch();// do what's left
			long t2 = System.currentTimeMillis();
			System.out.println("time to update " + compounds.size() + " mrv_files using batchsize=" + batchSize + ":\t"
					+ (t2 - t1) / 1000.0 + " seconds");
			conn.commit();
			// conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UpdateCompoundsMrv u=new UpdateCompoundsMrv();
		String folder="data/dsstox/csv/";
		u.updateCompoundsMrvFromCSV(folder+"tritium_fixes.csv");
		u.updateCompoundsMrvFromCSV(folder+"deuterium_fixes.csv");
		//TODO run on prod dsstox
		
		
	}

}
