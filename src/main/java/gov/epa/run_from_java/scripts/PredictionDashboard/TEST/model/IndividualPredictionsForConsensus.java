package gov.epa.run_from_java.scripts.PredictionDashboard.TEST.model;

import java.util.Vector;

public class IndividualPredictionsForConsensus {

    
    private Vector<PredictionIndividualMethod> consensusPredictions = new Vector<PredictionIndividualMethod>();
    
//    private String imageUrl;
    private String units;

    public class PredictionIndividualMethod {
        String method;
        Double prediction;
        String fileName;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Double getPrediction() {
            return prediction;
        }

        public void setPrediction(Double prediction) {
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
    
    public Double getPrediction(String method) {
    	for(PredictionIndividualMethod pim:consensusPredictions) {
    		if(pim.getPrediction().equals(method)) return pim.prediction;
    	}
    	return null;
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
