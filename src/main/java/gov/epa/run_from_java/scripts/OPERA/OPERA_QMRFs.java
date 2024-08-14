package gov.epa.run_from_java.scripts.OPERA;


import java.io.File;
import java.util.TreeMap;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.run_from_java.scripts.QsarModelsScript;
import gov.epa.run_from_java.scripts.PredictionDashboard.CreatorScript;

/**
* @author TMARTI02
*/
public class OPERA_QMRFs {
	
	void storeQMRFsInPostgres() {
		QsarModelsScript q=new QsarModelsScript("tmarti02");
		
		File folder=new File("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\QMRFs\\");

		TreeMap<String, Model>mapModels=CreatorScript.getModelsMap();
		System.out.println(mapModels.size());
		
		for (File file:folder.listFiles()) {
			
			String endpoint=null;
			
			if(file.getName().contains("MP")) {
				endpoint=DevQsarConstants.MELTING_POINT;
			} else if(file.getName().contains("BP")) {
				endpoint=DevQsarConstants.BOILING_POINT;
			}  else if(file.getName().contains("WS")) {
				endpoint=DevQsarConstants.WATER_SOLUBILITY;
			}  else if(file.getName().contains("VP")) {
				endpoint=DevQsarConstants.VAPOR_PRESSURE;
			} else if(file.getName().contains("log P")) {
				endpoint=DevQsarConstants.LOG_KOW;
			} else if(file.getName().contains("KOA")) {
				endpoint=DevQsarConstants.LOG_KOA;
			}  else if(file.getName().contains("BioHL")) {
				endpoint=DevQsarConstants.BIODEG_HL_HC;
			} else if(file.getName().contains("HL")) {
				endpoint=DevQsarConstants.HENRYS_LAW_CONSTANT;
			} else if(file.getName().contains("AOH")) {
				endpoint=DevQsarConstants.OH;
			} else if(file.getName().contains("RBioDeg")) {
				endpoint=DevQsarConstants.RBIODEG;
			} else if(file.getName().contains("BCF")) {
				endpoint=DevQsarConstants.BCF;
			} else if(file.getName().contains("KOC")) {
				endpoint=DevQsarConstants.KOC;
			} else if(file.getName().contains("KM")) {
				endpoint=DevQsarConstants.KmHL;
			} else if (file.getName().contains("CATMoS")) {
				endpoint=DevQsarConstants.ORAL_RAT_LD50;
			} else {
				System.out.println(file.getName()+" doesnt have endpoint");
				continue;
			}
			
			
			String modelName=OPERA_csv_to_PostGres_DB.getModelName(endpoint);;
			
//			System.out.println("\n"+file.getName()+"\t"+endpoint+"\t"+modelName);
			
			if (mapModels.get(modelName)==null) {
				System.out.println("Missing "+modelName);
				continue;
			}
			Model model=mapModels.get(modelName);
			
			
			System.out.println(file.getName()+"\t"+endpoint+"\t"+model.getId());
			
			try {
				q.uploadModelFile(model.getId(),1L, file.getAbsolutePath());
				
//				q.downloadModelQmrf(model.getId(), "data\\dev_qsar\\model_qmrfs");
			
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		
	}
	
	
	void viewQMRF () {
		QsarModelsScript q=new QsarModelsScript("tmarti02");
		q.downloadModelFile(1022L, 1L, "data\\dev_qsar\\model_qmrfs");
		
	}
	
	public static void main(String[] args) {
		OPERA_QMRFs o=new OPERA_QMRFs();
		o.storeQMRFsInPostgres();
//		o.viewQMRF();
		
	}

}
