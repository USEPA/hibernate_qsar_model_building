package ToxPredictor.Application.model;

import java.util.Vector;

public class SimilarChemicals {

    private String similarChemicalsSet;
    private int similarChemicalsCount;
    private String units;
    private String expVal;
    private String predVal;
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

    public String getExpVal() {
        return expVal;
    }

    public void setExpVal(String expVal) {
        this.expVal = expVal;
    }

    public String getPredVal() {
        return predVal;
    }

    public void setPredVal(String predVal) {
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
