package gov.epa.endpoints.datasets;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public class BoundPropertyValue {
	
	private Double valueMin;
	private Double valueMax;
//	public String unitName;//TODO we need this- this code assumes that value is already in same units as the bounds
	
	
	public BoundPropertyValue(Double valueMin, Double valueMax) {
		this.valueMin = valueMin;
		this.valueMax = valueMax;
//		this.unitName=unitName;
	}
	
	public ExplainedResponse test(PropertyValue pv) {
		ExplainedResponse testResponse = null;
		
		if (valueMin!=null) {
			testResponse = testValueMin(pv);
			if (!testResponse.response) { 
				return testResponse;
			}
		}
		
		if (valueMax!=null) {
			testResponse = testValueMax(pv);
			if (!testResponse.response) { 
				return testResponse;
			}
		}
		
		return testResponse;
	}
	private ExplainedResponse testValueMin(double value) {
		if (value < valueMin) {//important when min is zero and will later take a log to get qsar units
			return new ExplainedResponse(false, "property value failed min bound");
		}
		return new ExplainedResponse(true, "property value satisfies min bound");
	}

	private ExplainedResponse testValueMax(double value) {
		if (value > valueMax) {
			return new ExplainedResponse(false, "property value failed max bound");
		}
		return new ExplainedResponse(true, "property value satisfies max bound");
	}
	
	/**
	 * In DsstoxMapper the property value has already been converted to a point estimate so can use simpler method
	 * 
	 * @param value
	 * @return
	 */
	public ExplainedResponse test(Double value) {
		
		ExplainedResponse testResponse = null;
		
		if(valueMin==null && valueMax==null) {
			return new ExplainedResponse(true, "no bounds");
		}
		
		if (valueMin!=null) {
			testResponse = testValueMin(value);
			if (!testResponse.response) { 
				return testResponse;
			}
		}
		
		if (valueMax!=null) {
			testResponse = testValueMax(value);
			if (!testResponse.response) { 
				return testResponse;
			}
		}
		
		return testResponse;
	}

	
	private ExplainedResponse testValueMin(PropertyValue propertyValue) {
		String propertyValueQualifier = propertyValue.getValueQualifier();
		Double propertyValuePointEstimate = propertyValue.getValuePointEstimate();
		Double propertyValueMin = propertyValue.getValueMin();
		Double boundMin = valueMin;
		
		if (propertyValueQualifier!=null && propertyValueQualifier.contains("<")) {
			// Value qualifier with < means no lower limit, which should fail if there is a min bound
			return new ExplainedResponse(false, "property value failed min bound");
		}
		
		if (propertyValuePointEstimate!=null && propertyValuePointEstimate < boundMin) {
			return new ExplainedResponse(false, "property value failed min bound");
		}
		
		if (propertyValueMin!=null && propertyValueMin < boundMin) {
			return new ExplainedResponse(false, "property value failed min bound");
		}
		
		return new ExplainedResponse(true, "property value satisfies min bound");
	}
	

	private ExplainedResponse testValueMax(PropertyValue propertyValue) {
		String propertyValueQualifier = propertyValue.getValueQualifier();
		Double propertyValuePointEstimate = propertyValue.getValuePointEstimate();
		Double propertyValueMax = propertyValue.getValueMax();
		Double boundMax = valueMax;
		
		if (propertyValueQualifier!=null && propertyValueQualifier.contains(">")) {
			// Value qualifier with > means no upper limit, which should fail if there is a max bound
			return new ExplainedResponse(false, "property value failed max bound");
		}
		
		if (propertyValuePointEstimate!=null && propertyValuePointEstimate > boundMax) {
			return new ExplainedResponse(false, "property value failed max bound");
		}
		
		if (propertyValueMax!=null && propertyValueMax > boundMax) {
			return new ExplainedResponse(false, "property value failed max bound");
		}
		
		return new ExplainedResponse(true, "property value satisfies max bound");
	}

	public Double getValueMin() {
		return valueMin;
	}

	public void setValueMin(Double valueMin) {
		this.valueMin = valueMin;
	}

	public Double getValueMax() {
		return valueMax;
	}

	public void setValueMax(Double valueMax) {
		this.valueMax = valueMax;
	}
	
	
}