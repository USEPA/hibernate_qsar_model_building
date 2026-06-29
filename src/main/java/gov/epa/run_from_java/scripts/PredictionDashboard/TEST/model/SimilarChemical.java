package gov.epa.run_from_java.scripts.PredictionDashboard.TEST.model;

import com.fasterxml.jackson.annotation.JsonProperty;

//import ToxPredictor.Utilities.FormatUtils;
//import ToxPredictor.Utilities.StructureImageUtil;

public class SimilarChemical {

    
    private String DSSTOXSID;
    private String DSSTOXCID;
    private String CAS;
    private String preferredName;
    private String backgroundColor;
    private String similarityCoefficient;
    private Double expVal;
    private Double predVal;
    private String imageUrl;


    public String getDSSTOXSID() {
        return DSSTOXSID;
    }

    @JsonProperty("DSSTOXSID")
    public void setDSSTOXSID(String DSSTOXSID) {
        this.DSSTOXSID = DSSTOXSID;
    }

    public String getCAS() {
        return CAS;
    }

    @JsonProperty("CAS")
    public void setCAS(String CAS) {
        this.CAS = CAS;
    }

    
    public String getDSSTOXCID() {
		return DSSTOXCID;
	}

	public void setDSSTOXCID(String dSSTOXCID) {
		DSSTOXCID = dSSTOXCID;
	}
    
    
    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getSimilarityCoefficient() {
        return similarityCoefficient;
    }

    public void setSimilarityCoefficient(String similarityCoefficient) {
        this.similarityCoefficient = similarityCoefficient;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    
    
    
//	public LinkedHashMap<String, Object> convertToLinkedHashMap(JTable table) {
//		// TODO Auto-generated method stub
//		LinkedHashMap<String,Object> lhm=new LinkedHashMap<>();
//		lhm.put("CAS",CAS);
//				
//		
//		int size=table.getColumnModel().getColumn(1).getWidth();
////		System.out.println(size);
//		
////		System.out.println("2022-05-09: "+CAS+"\t"+imageUrl);
//		
//		if (imageUrl.contains("base64")) {			
//			lhm.put("Structure",StructureImageUtil.decodeBase64ToImageIcon(imageUrl,size));			
//		} else {
//			lhm.put("Structure",StructureImageUtil.urlToImageIcon(imageUrl,size));	
//		}
//						
////		lhm.put("Structure",imageUrl);//previously we stored just url instead of imageIcon- which made scrolling the table slow
//		
//		
//		lhm.put("Similarity", similarityCoefficient);
//		lhm.put("Experimental value", expVal);
//		lhm.put("Predicted value", FormatUtils.setSignificantDigits(predVal,3));
//		lhm.put("backgroundColor", backgroundColor);
//		return lhm;
//	}

	public String getPreferredName() {
		return preferredName;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}
}
