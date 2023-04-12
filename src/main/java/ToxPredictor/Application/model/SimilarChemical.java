package ToxPredictor.Application.model;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTable;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.epa.util.StructureImageUtil;

public class SimilarChemical {

    
    private String DSSTOXSID;
    private String DSSTOXCID;
    public String getDSSTOXCID() {
		return DSSTOXCID;
	}

	public void setDSSTOXCID(String dSSTOXCID) {
		DSSTOXCID = dSSTOXCID;
	}

	private String CAS;
    private String backgroundColor;
    private String similarityCoefficient;
    private String expVal;
    private String predVal;
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
//		lhm.put("Predicted value", predVal);
//		lhm.put("backgroundColor", backgroundColor);
//		return lhm;
//	}
}
