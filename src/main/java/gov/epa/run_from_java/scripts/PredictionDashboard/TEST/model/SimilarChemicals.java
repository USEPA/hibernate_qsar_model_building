package gov.epa.run_from_java.scripts.PredictionDashboard.TEST.model;

import java.util.Vector;

public class SimilarChemicals {

    private String similarChemicalsSet;
    private int similarChemicalsCount;
    private String units;
    private Double expVal;
    private Double predVal;
//    private String imageUrl;
    
    private Vector<SimilarChemical> similarChemicalsList = new Vector<>();
    private ExternalPredChart externalPredChart;
    private CancerStats cancerStats;

    public String getSimilarChemicalsSet() {
        return similarChemicalsSet;
    }

    public void setSimilarChemicalsSet(String similarChemicalsSet) {
        this.similarChemicalsSet = similarChemicalsSet;
    }

    public int getSimilarChemicalsCount() {
        return similarChemicalsCount;
    }

    public void setSimilarChemicalsCount(int similarChemicalsCount) {
        this.similarChemicalsCount = similarChemicalsCount;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Double getExpVal() {
        return expVal;
    }

    public void setExpVal(Double expVal) {
        this.expVal = expVal;
    }

    public Double getPredVal() {
        return predVal;
    }

    public void setPredVal(Double predVal) {
        this.predVal = predVal;
    }

    public Vector<SimilarChemical> getSimilarChemicalsList() {
        return similarChemicalsList;
    }

    public void setSimilarChemicalsList(Vector<SimilarChemical> similarChemicalsList) {
        this.similarChemicalsList = similarChemicalsList;
    }

    public ExternalPredChart getExternalPredChart() {
        return externalPredChart;
    }

    public void setExternalPredChart(ExternalPredChart externalPredChart) {
        this.externalPredChart = externalPredChart;
    }

//    public String getImageUrl() {
//        return imageUrl;
//    }
//
//    public void setImageUrl(String imageUrl) {
//        this.imageUrl = imageUrl;
//    }

    public CancerStats getCancerStats() {
        return cancerStats;
    }

    public void setCancerStats(CancerStats cancerStats) {
        this.cancerStats = cancerStats;
    }
}
