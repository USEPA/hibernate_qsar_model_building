package ToxPredictor.Application.model;

public class CancerStats {

    private String concordance;
    private int correctCount;
    private int predCount;
    
    private String posConcordance;
    private int posCorrectCount;
    private int posPredCount;
    
    private String negConcordance;
    private int negCorrectCount;
    private int negPredCount;

    public String getConcordance() {
        return concordance;
    }

    public void setConcordance(String concordance) {
        this.concordance = concordance;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getPredCount() {
        return predCount;
    }

    public void setPredCount(int predCount) {
        this.predCount = predCount;
    }

    public String getPosConcordance() {
        return posConcordance;
    }

    public void setPosConcordance(String posConcordance) {
        this.posConcordance = posConcordance;
    }

    public int getPosCorrectCount() {
        return posCorrectCount;
    }

    public void setPosCorrectCount(int posCorrectCount) {
        this.posCorrectCount = posCorrectCount;
    }

    public int getPosPredCount() {
        return posPredCount;
    }

    public void setPosPredCount(int posPredCount) {
        this.posPredCount = posPredCount;
    }

    public String getNegConcordance() {
        return negConcordance;
    }

    public void setNegConcordance(String negConcordance) {
        this.negConcordance = negConcordance;
    }

    public int getNegCorrectCount() {
        return negCorrectCount;
    }

    public void setNegCorrectCount(int negCorrectCount) {
        this.negCorrectCount = negCorrectCount;
    }

    public int getNegPredCount() {
        return negPredCount;
    }

    public void setNegPredCount(int negPredCount) {
        this.negPredCount = negPredCount;
    }
}
