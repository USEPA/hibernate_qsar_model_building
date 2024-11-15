package gov.epa.run_from_java.scripts.OPERA_Old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.json.CDL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class CompareOPERA2_6 {

	
	void goThroughCSVs() {
		String chemical="toluene";
//		String chemical="toluene aromatic";

		String folder="C:\\Users\\TMARTI02\\Program Files\\OPERA2.6\\testing\\"+chemical;
		
		File Folder=new File(folder);
		
		for (File file:Folder.listFiles()) {
			if(!file.getName().contains("Pred")) continue;
			
			if (file.getName().contains("StrP")  || file.getName().contains("pKa") || file.getName().contains("CERAPP") || file.getName().contains("CoMPARA")|| file.getName().contains("CATMoS")) continue;
			
			try {
				
				String property=file.getName().replace(chemical+"-smi_OPERA2.6Pred_","").replace(".csv", "");
				String property2=property;
				
//				System.out.println(property);
				
				InputStream inputStream = new FileInputStream(file);
				String csvAsString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
				String json = CDL.toJSONArray(csvAsString).toString();
				inputStream.close();
				
				JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);

				JsonObject jo=ja.get(0).getAsJsonObject();
				
				if(property.equals("AOH")) property2="LogOH";
				if(property.equals("BCF")) property2="LogBCF";
				if(property.equals("BioDeg")) property2="BioDeg_LogHalfLife";
				if(property.equals("HL")) property2="LogHL";
				if(property.equals("KM")) property2="LogKM";
				if(property.equals("KOA")) property2="LogKOA";
				if(property.equals("KOC")) property2="LogKoc";
				if(property.equals("LogD")) property2="LogD55";
				if(property.equals("RBioDeg")) property2="ReadyBiodeg";
				if(property.equals("VP")) property2="LogVP";
				if(property.equals("WS")) property2="LogWS";
				
				double pred=jo.get(property2+"_pred").getAsDouble();
				
				Double AD=null;
				Double localIndex=null;
				Double confidenceIndex=null;
				Double expNeighbor1=null;
				Double predNeighbor1=null;
				
				if(jo.get("AD_"+property)!=null) {
					AD=jo.get("AD_"+property).getAsDouble();	
				} else {
					AD=jo.get("AD_"+property2).getAsDouble();
				}
				
				if(jo.get("AD_index_"+property)!=null) {
					localIndex=jo.get("AD_index_"+property).getAsDouble();	
				} else {
					localIndex=jo.get("AD_index_"+property2).getAsDouble();
				}
				
				if(jo.get("Conf_index_"+property)!=null) {
					confidenceIndex=jo.get("Conf_index_"+property).getAsDouble();	
				} else {
					confidenceIndex=jo.get("Conf_index_"+property2).getAsDouble();
				}
				
				
				if(!property.equals("LogD")) {
					if(jo.get(property+"_Exp_neighbor_1")!=null) {
						expNeighbor1=jo.get(property+"_Exp_neighbor_1").getAsDouble();	
					} else {
						expNeighbor1=jo.get(property2+"_Exp_neighbor_1").getAsDouble();
					}
					
					if(jo.get(property+"_pred_neighbor_1")!=null) {
						predNeighbor1=jo.get(property+"_pred_neighbor_1").getAsDouble();	
					} else {
						predNeighbor1=jo.get(property2+"_pred_neighbor_1").getAsDouble();
					}

				}
				
//				
//				double confidenceIndex=jo.get("Conf_index_"+property).getAsDouble();
//				AD_LogKoc,AD_index_LogKoc,Conf_index_LogKoc
				System.out.println(property+"\t"+pred+"\t"+AD+"\t"+localIndex+"\t"+confidenceIndex+"\t"+expNeighbor1+"\t"+predNeighbor1);
				
//				System.out.println(file.getName());	
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			
			
			
		}
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CompareOPERA2_6 c=new CompareOPERA2_6();
		c.goThroughCSVs();
	}

}
