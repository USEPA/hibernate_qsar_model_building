package gov.epa.run_from_java.scripts.PredictionDashboard.valery;

import java.util.ArrayList;
import java.util.List;


/**
* @author TMARTI02
*/
public class SDE_Prediction_Request {
	
	//{"chemicals":[{"smiles":"FC(F)OC(F)C(F)(F)F"}],"modelset_id":1,"datasets":[{"dataset_id":91}],"workflow":"qsar-ready"}

	List<Chemical>chemicals=new ArrayList<>();
	List<Dataset>datasets=new ArrayList<>();
	String modelset_id;
	String workflow;
	
	public SDE_Prediction_Request() {}

	
	public void getFromTSV(String predictionSetTsv,String modelSetId,String datasetId, String workflow) {
		
		this.modelset_id=modelSetId;
		this.workflow=workflow;
		
		String [] lines=predictionSetTsv.split("\n");
		
		for (int i=1;i<lines.length;i++) {
			String line=lines[i].trim();
			String smiles=line.substring(0,line.indexOf("\t"));
//			System.out.println(smiles);
			chemicals.add(new Chemical(smiles));
		}
		
		datasets.add(new Dataset(datasetId));

	}
			
	
	public SDE_Prediction_Request(String smiles,String modelSetId,String datasetId, String workflow) {
		this.modelset_id=modelSetId;
		this.workflow=workflow;
		chemicals.add(new Chemical(smiles));
		datasets.add(new Dataset(datasetId));
//		System.out.println(Utilities.gson.toJson(this));
	}
	
	class Dataset {
		String dataset_id;
		Dataset(String dataset_id) {
			this.dataset_id=dataset_id;
		}
	}
	
	class Chemical {
		String smiles;
		Chemical(String smiles) {
			this.smiles=smiles;
		}
	}
}