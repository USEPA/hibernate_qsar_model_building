package gov.epa.endpoints.datasets;

import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;

public class BoundParameterValue {
	public String parameterName;
	private Double valueMin;
	private Double valueMax;
	private Double valuePointEstimate;
	private String valueText;
	private boolean allowUndefined;
	
	public BoundParameterValue(String parameterName, Double valueMin, Double valueMax, boolean allowUndefined) {
		this.parameterName = parameterName;
		this.valueMin = valueMin;
		this.valueMax = valueMax;
		this.allowUndefined = allowUndefined;
	}
	
	public BoundParameterValue(String parameterName, Double valuePointEstimate, boolean allowUndefined) {
		this.parameterName = parameterName;
		this.valuePointEstimate = valuePointEstimate;
		this.allowUndefined = allowUndefined;
	}
	
	public BoundParameterValue(String parameterName, String valueText, boolean allowUndefined) {
		this.parameterName = parameterName;
		this.valueText = valueText;
		this.allowUndefined = allowUndefined;
	}
	
	public ExplainedResponse test(ParameterValue pv) {
		ExplainedResponse testResponse = null;
		if (pv==null && !allowUndefined) {
			// If no parameter value for specified parameter name, and bound does not permit undefined values, should fail
			testResponse = new ExplainedResponse(false, "Undefined value not permitted for parameter");
			return testResponse;
		} else if (pv==null) {
			testResponse = new ExplainedResponse(true, "Undefined value permitted for parameter");
			return testResponse;
		}
		
		if (valuePointEstimate!=null) {
			testResponse = testValuePointEstimate(pv);
			if (!testResponse.response) { 
				return testResponse;
			}
		}
		
		if (valueText!=null) {
			testResponse = testValueText(pv);
			if (!testResponse.response) { 
				return testResponse;
			}
		}
		
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

	private ExplainedResponse testValuePointEstimate(ParameterValue parameterValue) {
		String parameterValueQualifier = parameterValue.getValueQualifier();
		Double parameterValuePointEstimate = parameterValue.getValuePointEstimate();
		Double boundPointEstimate = valuePointEstimate;
		
		if (parameterValuePointEstimate==null && allowUndefined) {
			return new ExplainedResponse(true, "Undefined value permitted for parameter");
		} else if (parameterValuePointEstimate==null) {
			return new ExplainedResponse(false, "Undefined value not permitted for parameter");
		}
		
		if (parameterValueQualifier!=null && (parameterValueQualifier.contains(">") || parameterValueQualifier.contains("<"))) {
			return new ExplainedResponse(false, "Point estimate bound does not permit value qualifier");
		}
		
		if (parameterValuePointEstimate.equals(boundPointEstimate)) {
			return new ExplainedResponse(true, "Parameter value satisfies point estimate bound");
		} else {
			return new ExplainedResponse(false, "Parameter value failed point estimate bound");
		}
	}

	private ExplainedResponse testValueText(ParameterValue parameterValue) {
		String parameterValueText = parameterValue.getValueText();
		String boundText = valueText;
		
		if (parameterValueText==null && allowUndefined) {
			return new ExplainedResponse(true, "Undefined value permitted for parameter");
		} else if (parameterValueText==null) {
			return new ExplainedResponse(false, "Undefined value not permitted for parameter");
		}
		
		if (parameterValueText.equalsIgnoreCase(boundText)) {
			return new ExplainedResponse(true, "Parameter value satisfies text bound");
		} else {
			return new ExplainedResponse(false, "Parameter value does not match text bound");
		}
	}

	private ExplainedResponse testValueMin(ParameterValue parameterValue) {
		String parameterValueQualifier = parameterValue.getValueQualifier();
		Double parameterValuePointEstimate = parameterValue.getValuePointEstimate();
		Double parameterValueMin = parameterValue.getValueMin();
		Double parameterValueMax = parameterValue.getValueMax();
		Double boundMin = valueMin;
		
		if (parameterValuePointEstimate==null 
				&& parameterValueMin==null
				&& parameterValueMax==null
				&& allowUndefined) {
			return new ExplainedResponse(true, "Undefined value permitted for parameter");
		} else if (parameterValuePointEstimate==null
				&& parameterValueMin==null
				&& parameterValueMax==null) {
			return new ExplainedResponse(false, "Undefined value not permitted for parameter");
		}
		
		if (parameterValueQualifier!=null && parameterValueQualifier.contains("<")) {
			// Value qualifier with < means no lower limit, which should fail if there is a min bound
			return new ExplainedResponse(false, "Parameter value failed min bound");
		}
		
		if (parameterValuePointEstimate!=null && parameterValuePointEstimate < boundMin) {
			return new ExplainedResponse(false, "Parameter value failed min bound");
		}
		
		if (parameterValueMin!=null && parameterValueMin < boundMin) {
			return new ExplainedResponse(false, "Parameter value failed min bound");
		}
		
		return new ExplainedResponse(true, "Parameter value satisfies min bound");
	}

	private ExplainedResponse testValueMax(ParameterValue parameterValue) {
		String parameterValueQualifier = parameterValue.getValueQualifier();
		Double parameterValuePointEstimate = parameterValue.getValuePointEstimate();
		Double parameterValueMin = parameterValue.getValueMin();
		Double parameterValueMax = parameterValue.getValueMax();
		Double boundMax = valueMax;
		
		if (parameterValuePointEstimate==null 
				&& parameterValueMin==null
				&& parameterValueMax==null
				&& allowUndefined) {
			return new ExplainedResponse(true, "Undefined value permitted for parameter");
		} else if (parameterValuePointEstimate==null
				&& parameterValueMin==null
				&& parameterValueMax==null) {
			return new ExplainedResponse(false, "Undefined value not permitted for parameter");
		}
		
		if (parameterValueQualifier!=null && parameterValueQualifier.contains(">")) {
			// Value qualifier with > means no upper limit, which should fail if there is a max bound
			return new ExplainedResponse(false, "Parameter value failed max bound");
		}
		
		if (parameterValuePointEstimate!=null && parameterValuePointEstimate > boundMax) {
			return new ExplainedResponse(false, "Parameter value failed max bound");
		}
		
		if (parameterValueMax!=null && parameterValueMax > boundMax) {
			return new ExplainedResponse(false, "Parameter value failed max bound");
		}
		
		return new ExplainedResponse(true, "Parameter value satisfies max bound");
	}
}