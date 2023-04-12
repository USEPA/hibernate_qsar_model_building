package ToxPredictor.Application.model;

import java.util.Vector;

public class IndividualPredictionsForConsensus {

    
    private Vector<PredictionIndividualMethod> consensusPredictions = new Vector<PredictionIndividualMethod>();
    
//    private String imageUrl;
    private String units;

    public class PredictionIndividualMethod {
        String method;
        String prediction;
        String fileName;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPrediction() {
            return prediction;
        }

        public void setPrediction(String prediction) {
            this.prediction = prediction;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }

//    public boolean isCreateDetailedConsensusReport() {
//        return createDetailedConsensusReport;
//    }
//
//    public void setCreateDetailedConsensusReport(boolean createDetailedConsensusReport) {
//        this.createDetailedConsensusReport = createDetailedConsensusReport;
//    }

    public Vector<PredictionIndividualMethod> getConsensusPredictions() {
        return consensusPredictions;
    }

    public void setConsensusPredictions(Vector<PredictionIndividualMethod> consensusPredictions) {
        this.consensusPredictions = consensusPredictions;
    }

//    public String getImageUrl() {
//        return imageUrl;
//    }
//
//    public void setImageUrl(String imageUrl) {
//        this.imageUrl = imageUrl;
//    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
