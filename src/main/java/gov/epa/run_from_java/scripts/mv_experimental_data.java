package gov.epa.run_from_java.scripts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
* @author TMARTI02
*/
public class mv_experimental_data {

	
	public String convertToInClause(List<String>items) {
		StringBuilder sb = new StringBuilder();

		int count = 0;
		for (String item : items) {
			sb.append("'").append(item).append("'");
			if (count < items.size() - 1) {
				sb.append(", ");
			}
			count++;
		}

		return sb.toString();
	}
	
	
	void getData(List<String>dtxsids,String propertyName) {
		
		String sql="select * from mv_experimental_data where dtxsid in ("+convertToInClause(dtxsids)+") and prop_name='"+propertyName+"';";
		
		
		
		
	}
	
	
	public static void main(String[] args) {
		mv_experimental_data m=new mv_experimental_data();
		
		List<String>dtxsids=Arrays.asList("DTXSID00192353","DTXSID6067331","DTXSID30891564","DTXSID6062599","DTXSID90868151","DTXSID8031863","DTXSID8031865","DTXSID1037303","DTXSID8047553","DTXSID60663110","DTXSID70191136","DTXSID3037709","DTXSID3059921","DTXSID3031860","DTXSID8037706","DTXSID8059920","DTXSID3031862","DTXSID30382063","DTXSID00379268","DTXSID20874028","DTXSID3037707");
		m.getData(dtxsids, "LogKow: Octanol-Water");

	}

}
