package ToxPredictor.Application.model;


import java.util.Vector;

public class PredictionResultsPrimaryTable {

    private String source;//source for experimental data point
    private String endpointSubscripted;
    private Vector<EndpointSource> endpointSources;

    private String dtxcid;//CID for chemical to be predicted
    
    private String expCAS;//cas number corresponding to experimental value
    private String expSet;//set where the experimental value came from (training or prediction)

    private boolean writePredictionInterval;//whether or not to output a prediction confidence interval

    private String expToxValue;//experimental value (usually in molar units)
    private String predToxValue;//predicted value (usually in molar units)
    
    private String expToxValMass;//experimental value in mass units
    private String predToxValMass;//predicted value in mass units

    private String expToxValueEndpoint;//used for binary endpoint
    private String predValueEndpoint;//used for binary endpoint

    private String molarLogUnits;//molar units for the endpoint
    private String massUnits;//mass units for the endpoint
    
//    private String predictionInterval;
    
    private String predMinMaxVal;//predicted range in molar units
    private String predMinMaxValMass;//predicted range in mass units

    private String predictedValueSuperscript;//superscript for predicted value table header
    private String predictedValueNote;//text describing which data set chemical came from, superscript a
    private String message;//message if have error, superscript b

    private String expMOA;
    private String predMOA;
    private String maxScoreMOA;
    
    
    
    public String getMaxScoreMOA() {
		return maxScoreMOA;
	}

	public void setMaxScoreMOA(String maxScoreMOA) {
		this.maxScoreMOA = maxScoreMOA;
	}

	public String getExpMOA() {
		return expMOA;
	}

	public void setExpMOA(String expMOA) {
		this.expMOA = expMOA;
	}

	public String getPredMOA() {
		return predMOA;
	}

	public void setPredMOA(String predMOA) {
		this.predMOA = predMOA;
	}

	public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Vector<EndpointSource> getEndpointSources() {
        return endpointSources;
    }

    public void setEndpointSources(Vector<EndpointSource> endpointSources) {
        this.endpointSources = endpointSources;
    }

    public String getExpCAS() {
        return expCAS;
    }

    public void setExpCAS(String expCAS) {
        this.expCAS = expCAS;
    }

    public boolean isWritePredictionInterval() {
        return writePredictionInterval;
    }

    public void setWritePredictionInterval(boolean writePredictionInterval) {
        this.writePredictionInterval = writePredictionInterval;
    }

    public String getPredictedValueSuperscript() {
        return predictedValueSuperscript;
    }

    public void setPredictedValueSuperscript(String predictedValueSuperscript) {
        this.predictedValueSuperscript = predictedValueSuperscript;
    }

    public String getExpSet() {
        return expSet;
    }

    public void setExpSet(String expSet) {
        this.expSet = expSet;
    }

    public String getMolarLogUnits() {
        return molarLogUnits;
    }

    public void setMolarLogUnits(String molarLogUnits) {
        this.molarLogUnits = molarLogUnits;
    }

    public String getExpToxValue() {
        return expToxValue;
    }

    public void setExpToxValue(String expToxValue) {
        this.expToxValue = expToxValue;
    }

    public String getPredToxValue() {
        return predToxValue;
    }

    public void setPredToxValue(String predToxVal) {
        this.predToxValue = predToxVal;
    }

//    public String getPredictionInterval() {
//        return predictionInterval;
//    }
//
//    public void setPredictionInterval(String predictionInterval) {
//        this.predictionInterval = predictionInterval;
//    }

    public String getPredMinMaxVal() {
        return predMinMaxVal;
    }

    public void setPredMinMaxVal(String predMinMaxVal) {
        this.predMinMaxVal = predMinMaxVal;
    }


    public String getDtxcid() {
		return dtxcid;
	}

	public void setDtxcid(String dtxcid) {
		this.dtxcid = dtxcid;
	}

	public String getMassUnits() {
        return massUnits;
    }

    public void setMassUnits(String endpointMassUnits) {
        this.massUnits = endpointMassUnits;
    }

    public String getExpToxValMass() {
        return expToxValMass;
    }

    public void setExpToxValMass(String expToxValMass) {
        this.expToxValMass = expToxValMass;
    }

    public String getPredToxValMass() {
        return predToxValMass;
    }

    public void setPredToxValMass(String predToxValMass) {
        this.predToxValMass = predToxValMass;
    }

    public String getPredMinMaxValMass() {
        return predMinMaxValMass;
    }

    public void setPredMinMaxValMass(String predMinMaxValMass) {
        this.predMinMaxValMass = predMinMaxValMass;
    }

    public String getPredictedValueNote() {
        return predictedValueNote;
    }

    public void setPredictedValueNote(String predictedValueNote) {
        this.predictedValueNote = predictedValueNote;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExpToxValueEndpoint() {
        return expToxValueEndpoint;
    }

    public void setExpToxValueEndpoint(String expToxValueEndpoint) {
        this.expToxValueEndpoint = expToxValueEndpoint;
    }

    public String getPredValueEndpoint() {
        return predValueEndpoint;
    }

    public void setPredValueEndpoint(String predValueEndpoint) {
        this.predValueEndpoint = predValueEndpoint;
    }

    public String getEndpointSubscripted() {
        return endpointSubscripted;
    }

    public void setEndpointSubscripted(String endpointSubscripted) {
        this.endpointSubscripted = endpointSubscripted;
    }
}
