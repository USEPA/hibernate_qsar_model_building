package gov.epa.run_from_java.scripts.PredictionDashboard.valery;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import gov.epa.run_from_java.scripts.PredictionDashboard.valery.Dataset;

public class ValeryResponse {
	@Expose
    public Dataset dataset;
	@Expose
    public ArrayList<Prediction> predictions;
}
