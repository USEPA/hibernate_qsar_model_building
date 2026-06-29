package gov.epa.run_from_java.scripts.GetExpPropInfo;

import java.io.FileInputStream;
import java.util.Hashtable;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

/**
* @author TMARTI02
*/
public class GetExpPropInfoUtilities {
	
	
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
	
	public static Hashtable<String,String> createOpera_Reference_Lookup(String propertyAbbrev,String refField) {
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

				String key=m.getProperty("dsstox_substance_id");
				
				if (key.isBlank()) {
					key=m.getProperty("CAS");

					if (!key.isBlank()) {
						System.out.println("cas reference key "+key);
					}
					
				}
				
				if (key.isBlank()) {
					System.out.println("Missing key for "+propertyAbbrev);
					continue;
				} 
				
				if (m.getProperty(refField)==null) {
					System.out.println(propertyAbbrev+"\t"+key+"\tref missing");
					
					continue;
				}
				
				String Reference=m.getProperty(refField);
				
				if (Reference.isBlank() || Reference.equals("?")) continue;
				
//				System.out.println(DTXSID+"\t"+Reference);
				ht.put(key, Reference);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ht;
	}

}
