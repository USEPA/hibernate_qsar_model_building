package ToxPredictor.Application.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalPredChart {

    private String externalPredChartImageSrc;
    private double MAEEntireTestSet;
    private double MAE;

    public String getExternalPredChartImageSrc() {
        return externalPredChartImageSrc;
    }

    public void setExternalPredChartImageSrc(String externalPredChartImageSrc) {
        this.externalPredChartImageSrc = externalPredChartImageSrc;
    }

    public double getMAEEntireTestSet() {
        return MAEEntireTestSet;
    }

    @JsonProperty("MAEEntireTestSet")
    public void setMAEEntireTestSet(double MAEEntireTestSet) {
        this.MAEEntireTestSet = MAEEntireTestSet;
    }

    public double getMAE() {
        return MAE;
    }

    @JsonProperty("MAE")
    public void setMAE(double MAE) {
        this.MAE = MAE;
    }
}
