package gov.epa.run_from_java.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;




import org.apache.commons.compress.parallel.FileBasedScatterGatherBackingStore;
import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ParameterService;
import gov.epa.databases.dev_qsar.exp_prop.service.ParameterServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;

public class FixExpPropRecordsScript {
	PropertyValueServiceImpl propertyValueService = new PropertyValueServiceImpl();
	String lanId="tmarti02";
	
	ExpPropUnitService unitService=new ExpPropUnitServiceImpl(); 
	ParameterService parameterService=new ParameterServiceImpl();
	

	void fixBP() {

		String propertyName="Boiling point";

		ExpPropUnit unitsMMHG=unitService.findByName("mmHg");
		Parameter paramPressure=parameterService.findByName("Pressure");
		

		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, true, true);
		int counter=0;
		
		for (PropertyValue propertyValue:propertyValues) {

			String value_text=propertyValue.getValueText();
			
			
			if (propertyValue.getValueText()==null || !propertyValue.getValueText().contains("mmHg")) continue;

			if (!value_text.contains("(")  && !value_text.contains("760")) {
				System.out.println(value_text+"\t"+propertyValue.getParameterValues().size());
			}
			
			if (!value_text.contains("(")) continue;
			
			counter++;

			//					System.out.println(Line);
			String [] values=value_text.split("\\(");

			String [] values0=values[0].split("\\/");
			String strTemp0=values0[0];
			String strPres0=values0[1];										
			String TempOriginal = getTemp(value_text, strTemp0);
			String PresOriginal = getPres(value_text, strPres0);
			double dPresOriginal=Double.parseDouble(PresOriginal);
			
			if(propertyValue.getParameterValues().size()>0) {
				System.out.println(counter+"\tAlready have parameter for "+propertyValue.getId()+"\t"+value_text+"\t"+propertyValue.getParameterValues().get(0).getValuePointEstimate());
				continue;
			}
						
			ParameterValue parameterValue=new ParameterValue();
			parameterValue.setCreatedBy(lanId);
			parameterValue.setUnit(unitsMMHG);
			parameterValue.setValuePointEstimate(dPresOriginal);
			parameterValue.setParameter(paramPressure);
			parameterValue.setPropertyValue(propertyValue);
			propertyValue.addParameterValue(parameterValue);//Add parameter so it gets excluded later
			
			System.out.println(counter+"\tAdding parameter value for "+value_text+"\t"+parameterValue.getValuePointEstimate()+"\t"+unitsMMHG.getAbbreviation());
			
			propertyValueService.update(propertyValue);//Just add the pressure so datapoints can be removed if Pressure < 740 or Pressure>780 			
						

//			boolean doUpdate=false;
//			if (dPresOriginal<740 || dPresOriginal>780) {
//				counter++;
////				System.out.println(counter+"\tExtrapolated value\t"+value_text);
//				continue;
//			}
//
//			String [] values1=values[1].split("\\/");
//			String strTemp1=values1[0];
//			String strPres1=values1[1];										
//			String TempNew = getTemp(value_text, strTemp1);
//			String PresNew = getPres(value_text, strPres1);
//			double dPresNew=Double.parseDouble(PresNew);
//
//			//	System.out.println(Line+"\t"+Temp0+"\t"+Pres0);
//			
//			if(TempOriginal.contains("-")) {
//				//						System.out.println(Temp1+"\t"+Pres1+"\t\t\t"+value_text);
//				if (TempOriginal.contains("--")) {							
//					propertyValue.setValueMin(Double.parseDouble(TempOriginal.substring(0,TempOriginal.indexOf("--"))));
//					propertyValue.setValueMax(Double.parseDouble(TempOriginal.substring(TempOriginal.indexOf("--")+1,TempOriginal.length())));
//					propertyValue.setValuePointEstimate(null);
//					doUpdate=true;
//
//				} else if (TempOriginal.indexOf("-")>0){
//					//							System.out.println("here1:"+Temp1);
//					String []Temps=TempOriginal.split("-");
//					propertyValue.setValueMin(Double.parseDouble(Temps[0]));
//					propertyValue.setValueMax(Double.parseDouble(Temps[1]));
//					propertyValue.setValuePointEstimate(null);
//					doUpdate=true;
//				} else {
//					System.out.println("here2 have dash at start:"+TempOriginal);
//				}
//			} else {
//				propertyValue.setValueMin(null);
//				propertyValue.setValueMax(null);
//				propertyValue.setValuePointEstimate(Double.parseDouble(TempOriginal));
//				doUpdate=true;
//			}
//
//			propertyValue.setQcFlag(true);
//			propertyValue.setQcNotes("Updated to use value at 760 mmHg");
//
//			//TODO update propertyValue
//
//			if(doUpdate) {
//				//	propertyValueService.update(propertyValue);
//				System.out.println(value_text+"\t"+propertyValue.getValueMin()+"\t"+propertyValue.getValueMax()+"\t"+propertyValue.getValuePointEstimate()+"\t"+PresOriginal);
//
//			} else {
//				System.out.println("dont update\t"+value_text+"\t"+propertyValue.getValueMin()+"\t"+propertyValue.getValueMax()+"\t"+propertyValue.getValuePointEstimate());						
//			}


		}

	}		


	void fixLookchem() {

		String propertyName="Boiling point";
//		String propertyName="Melting point";
		
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");

		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, true, true);
		int counter=0;

		System.out.println("id\tvalue_original\tvalue_point_estimate\tpage_url");

//		List<Long>exp_prop_ids=new ArrayList<Long>();
		
		JsonArray ja=new JsonArray();
		
		for (PropertyValue propertyValue:propertyValues) {
			if(propertyValue.getPublicSource()==null) continue;
			if(!propertyValue.getPublicSource().getName().equals("LookChem")) continue;
			int countChar=StringUtils.countMatches(propertyValue.getValueOriginal(), "�");
			
//			if(propertyValue.getUpdatedBy()!=null && propertyValue.getUpdatedBy().equals("tmarti02")) continue;
			
			if(countChar<2) continue;
						
			JsonObject jo=new JsonObject();
			jo.addProperty("id", propertyValue.getId());
			jo.addProperty("value_original", propertyValue.getValueOriginal());
			jo.addProperty("value_point_estimate", propertyValue.getValuePointEstimate());
			jo.addProperty("value_min", propertyValue.getValueMin());
			jo.addProperty("value_max", propertyValue.getValueMax());
			jo.addProperty("page_url", propertyValue.getPageUrl());
			jo.addProperty("updated_by", propertyValue.getUpdatedBy());
						
			if(propertyValue.getParameterValue("Pressure")!=null)
				jo.addProperty("Pressure", propertyValue.getParameterValue("Pressure").getValuePointEstimate());

			if(propertyValue.getParameterValue("Temperature")!=null)
				jo.addProperty("Temperature", propertyValue.getParameterValue("Temperature").getValuePointEstimate());
			
			ja.add(jo);
			
			System.out.println(propertyValue.getId()+"\t"+propertyValue.getValueOriginal()+"\t"+propertyValue.getValuePointEstimate()+"\t"+propertyValue.getPageUrl());
//			exp_prop_ids.add(propertyValue.getId());
		}
		
		String pathout="data/reports/lookchem/"+propertyName+"_lookchem check.xlsx";
		String[] fieldsFinal= {"id","value_original","value_point_estimate","value_min","value_max","Temperature","Pressure","page_url","updated_by"};
		ExcelCreator.createExcel2(ja, pathout,fieldsFinal,null);
					
	}		
	
	void bpWithPressure() {

		String propertyName="Boiling point";

		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, true, true);
		int counter=0;

		System.out.println("id\tvalue_original\tvalue_point_estimate\tpage_url");

//		List<Long>exp_prop_ids=new ArrayList<Long>();
		
		int countPres=0;
		int countNoPres=0;
		
		for (PropertyValue propertyValue:propertyValues) {
			
			if (propertyValue.getPublicSource()!=null && propertyValue.getPublicSource().getName().equals("LookChem")) continue;
						
			Double pressure=null;
			if(propertyValue.getParameterValue("Pressure")!=null) {
				pressure=propertyValue.getParameterValue("Pressure").getValuePointEstimate();
				countPres++;
			} else {
				countNoPres++;
			}

			System.out.println(propertyValue.getId()+"\t"+pressure);
//			exp_prop_ids.add(propertyValue.getId());
		}
		
		System.out.println(countNoPres+"\t"+countPres);
		
					
	}		



	private String getPres(String Line, String strPres) {
		
		strPres=strPres.replace("Torr", "mmHg");
		String Pres="";

		if (strPres.indexOf("mm")>-1) {
			Pres=strPres.substring(0,strPres.indexOf("mm"));	
		} else {
			System.out.println(Line+"\t"+strPres);
		}
		return Pres.trim();
	}



	private String getTemp(String Line, String strTemp) {
		String Temp="";
		if(strTemp.indexOf("deg C")>-1) {
			Temp=strTemp.substring(0,strTemp.indexOf("deg C"));						
		} else if(strTemp.indexOf("°C")>-1) {
			Temp=strTemp.substring(0,strTemp.indexOf("°C"));
		} else if(strTemp.indexOf("°")>-1) {
			Temp=strTemp.substring(0,strTemp.indexOf("°"));
		} else if(strTemp.indexOf("C")>-1) {
			Temp=strTemp.substring(0,strTemp.indexOf("C"));
		} else if (strTemp.contains("F")) {
			System.out.println("Has F:"+Line);					
		}
		return Temp.trim();
	}



	public static void main(String[] args) {
		FixExpPropRecordsScript f=new FixExpPropRecordsScript ();
		f.fixBP();
//		f.fixLookchem();
//		f.bpWithPressure();
		

	}

}
