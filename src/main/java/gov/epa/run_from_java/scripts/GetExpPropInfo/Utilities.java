package gov.epa.run_from_java.scripts.GetExpPropInfo;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class Utilities {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();


	/**
	 * Read a csv into a JsonArray
	 * 
	 * @param filepath
	 * @return
	 */
	public static JsonArray csvToGson(String filepath) {
		JsonArray ja=new JsonArray();
		 try {
			List<String>lines=Files.readAllLines(Path.of(filepath));
			
			String header=lines.get(0);
			String [] hvals = header.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			
			for (int i=1;i<lines.size();i++) {
				String line=lines.get(i);
				if (line.trim().length()==0) break;
				String [] vals = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				JsonObject jo=new JsonObject();
				for (int j=0;j<hvals.length;j++) {
					jo.addProperty(hvals[j], vals[j].replace("\"", ""));
				}
				ja.add(jo);
			}
//			System.out.println(gson.toJson(ja));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return ja;
	}
	
	public static void saveJson(Object obj, String filepath)  {
		try {

			FileWriter fw=new FileWriter(filepath);			
			fw.write(gson.toJson(obj));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	
	
	
	
	/**
	 * Get OPERA WS references (probably all from epiphys)
	 * 
	 */
	static void lookupOperaReferences() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\data\\experimental\\OPERA\\OPERA_SDFS\\";
		String filepath=folder+"WS_QR.sdf";

		try {
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());

			while (mr.hasNext()) {

				AtomContainer m=null;
				try {
					m = (AtomContainer)mr.next();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				//				if (m==null || m.getAtomCount()==0) break;
				if (m==null) break;

				String DTXSID=m.getProperty("dsstox_substance_id");
				String CAS=m.getProperty("CAS");
				String WS_Reference=m.getProperty("WS Reference");
				String LogMolar=m.getProperty("LogMolar");
				System.out.println(DTXSID+"~"+CAS+"~"+WS_Reference+"~"+LogMolar);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Loads JsonArray from filepath
	 * 
	 * @param filepath
	 * @return
	 */
	public static JsonArray getJsonArrayFromJsonFile(String filepath) {
		try {
			Gson gson=new Gson();
			Reader reader = Files.newBufferedReader(Paths.get(filepath));
			JsonArray ja=gson.fromJson(reader, JsonArray.class);
			return ja;
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;

		}
	}
	

	
	static Hashtable<String,String> createOpera_Reference_Lookup(String propertyAbbrev,String propertyAbbrev2) {
		Hashtable<String,String>ht=new Hashtable<>();
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\OPERA\\OPERA_SDFS\\";
		String filepath=folder+propertyAbbrev+"_QR.sdf";
		
		try {
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());
			while (mr.hasNext()) {
				AtomContainer m=null;
				try {
					m = (AtomContainer)mr.next();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				if (m==null) break;

				String DTXSID=m.getProperty("dsstox_substance_id");
				
				if (m.getProperty(propertyAbbrev2+" Reference")==null) {
					System.out.println(propertyAbbrev+"\t"+DTXSID+"\tref missing");
					
					continue;
				}
				
				String Reference=m.getProperty(propertyAbbrev2+" Reference");
				
				
				ht.put(DTXSID, Reference);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ht;
	}
	

		
	/**
	 * Get OPERA WS references (probably all from epiphys)
	 * 
	 */
	static void lookupEPISUITE_Isis_References() {

		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\data\\experimental\\EpisuiteISIS\\EPI_SDF_Data\\";
		String filepath=folder+"EPI_Wskowwin_Data_SDF.sdf";

		try {
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());

			while (mr.hasNext()) {

				AtomContainer m=null;
				try {
					m = (AtomContainer)mr.next();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				//				if (m==null || m.getAtomCount()==0) break;
				if (m==null) break;

				//				String DTXSID=m.getProperty("dsstox_substance_id");
				String CAS=m.getProperty("CAS");
				String WS_Reference=m.getProperty("WS Reference");
				String WS_Data_Type=m.getProperty("WS Data Type");
				String LogMolar=m.getProperty("LogMolar");


				System.out.println(CAS+"|"+WS_Reference+"|"+WS_Data_Type+"|"+LogMolar);


			}

		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}
	

	

	
}
