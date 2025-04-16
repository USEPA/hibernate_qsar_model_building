package gov.epa.run_from_java.scripts;

import java.util.List;

public class ExpPropFromCSV {

	String sourceName;
	String sourceDescription;
	String sourceURL;
	
	String propertyName;
	String propertyUnits;
	
	List<PropertyValue>propertyValues;
	
	
	class PropertyValue {
		
		String DSSTOXSID;
		Double propertyValue;
		
	}
	
	
	ExpPropFromCSV loadFromJson(String filepathJson) {
		
		ExpPropFromCSV e=new ExpPropFromCSV();//TODO load from json
		return e;
		
	}
	
	
}
