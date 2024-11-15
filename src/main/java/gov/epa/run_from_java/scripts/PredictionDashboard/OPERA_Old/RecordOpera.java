package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA_Old;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RecordOpera {

	
	public String model_name;
	public String model_units;
	
	public String DSSTOX_COMPOUND_ID;
	public String DSSTOX_SUBSTANCE_ID;
	
	public String Canonical_QSARr;
	public String Original_SMILES;
	
	public String exp;
	public String pred;
//	String predRange;
	public String AD;
	public String AD_index;
	public String Conf_index;
	
	public String DTXSID_neighbor_1;
	public String DTXSID_neighbor_2;
	public String DTXSID_neighbor_3;
	public String DTXSID_neighbor_4;
	public String DTXSID_neighbor_5;

	public String InChiKey_neighbor_1;
	public String InChiKey_neighbor_2;
	public String InChiKey_neighbor_3;
	public String InChiKey_neighbor_4;
	public String InChiKey_neighbor_5;
	
	public String DSSTOXMPID_neighbor_1;
	public String DSSTOXMPID_neighbor_2;
	public String DSSTOXMPID_neighbor_3;
	public String DSSTOXMPID_neighbor_4;
	public String DSSTOXMPID_neighbor_5;

//	public String DTXCID_neighbor_1;
//	public String DTXCID_neighbor_2;
//	public String DTXCID_neighbor_3;
//	public String DTXCID_neighbor_4;
//	public String DTXCID_neighbor_5;

	public String CAS_neighbor_1;
	public String CAS_neighbor_2;
	public String CAS_neighbor_3;
	public String CAS_neighbor_4;
	public String CAS_neighbor_5;

	public String Exp_neighbor_1;
	public String Exp_neighbor_2;
	public String Exp_neighbor_3;
	public String Exp_neighbor_4;
	public String Exp_neighbor_5;
	
	public String pred_neighbor_1;
	public String pred_neighbor_2;
	public String pred_neighbor_3;
	public String pred_neighbor_4;
	public String pred_neighbor_5;
	
	public String model_source;
	
	public static String [] fieldNames= {"DSSTOX_COMPOUND_ID","model_name","exp","pred","model_units","AD","AD_index","Conf_index",
			"DTXSID_neighbor_1","DTXSID_neighbor_2","DTXSID_neighbor_3","DTXSID_neighbor_4","DTXSID_neighbor_5",
//			"DSSTOXMPID_neighbor_1","DSSTOXMPID_neighbor_2","DSSTOXMPID_neighbor_3","DSSTOXMPID_neighbor_4","DSSTOXMPID_neighbor_5",
			"InChiKey_neighbor_1","InChiKey_neighbor_2","InChiKey_neighbor_3","InChiKey_neighbor_4","InChiKey_neighbor_5",
			"CAS_neighbor_1","CAS_neighbor_2","CAS_neighbor_3","CAS_neighbor_4","CAS_neighbor_5",
			"Exp_neighbor_1","Exp_neighbor_2","Exp_neighbor_3","Exp_neighbor_4","Exp_neighbor_5",
			"pred_neighbor_1","pred_neighbor_2","pred_neighbor_3","pred_neighbor_4","pred_neighbor_5","model_source"};

	
	public static String [] fieldNames2= {"DSSTOX_COMPOUND_ID","model_name","exp","pred","model_units","AD","AD_index","Conf_index",
			"DTXSID_neighbor_1","DTXSID_neighbor_2","DTXSID_neighbor_3","DTXSID_neighbor_4","DTXSID_neighbor_5",
//			"DSSTOXMPID_neighbor_1","DSSTOXMPID_neighbor_2","DSSTOXMPID_neighbor_3","DSSTOXMPID_neighbor_4","DSSTOXMPID_neighbor_5",
			"CAS_neighbor_1","CAS_neighbor_2","CAS_neighbor_3","CAS_neighbor_4","CAS_neighbor_5",
			"Exp_neighbor_1","Exp_neighbor_2","Exp_neighbor_3","Exp_neighbor_4","Exp_neighbor_5",
			"pred_neighbor_1","pred_neighbor_2","pred_neighbor_3","pred_neighbor_4","pred_neighbor_5","model_source"};

	
	
	public String toString(String del,String []fieldNames) {
		String result="";
		
		
		
		for (int i=0;i<fieldNames.length;i++) {
			String fieldName=fieldNames[i];
			try {
				Field myField = this.getClass().getDeclaredField(fieldName);
				
//				System.out.println(fieldName);
				
				
				String value=(String)myField.get(this);
				
				if (value!=null) {
					if (value.contains(del)) value="\""+value+"\"";					
				} else {
					value="";
				}
				
				result+=value;				
				
				if (i<fieldNames.length-1) result+=",";
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
		
	}
	
//	public String toString(String del) {
//		String result="";
//		
//		for (int i=0;i<fieldNames.length;i++) {
//			String fieldName=fieldNames[i];
//			try {
//				Field myField = this.getClass().getDeclaredField(fieldName);
//				
//				String value=(String)myField.get(this);
//				
//				if (value!=null) {
//					if (value.contains(del)) value="\""+value+"\"";					
//				} else {
//					value="";
//				}
//				
//				result+=value;				
//				
//				if (i<fieldNames.length-1) result+=",";
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return result;
//		
//	}
	
	public static String toHeaderString(String del,String []fieldNames) {		
		String result="";		
		for (int i=0;i<fieldNames.length;i++) {
			if (fieldNames[i].contains(del)) fieldNames[i]="\""+fieldNames[i]+"\"";			
			result+=fieldNames[i];								
			if (i<fieldNames.length-1) result+=",";
		}
		return result;
	}
}
