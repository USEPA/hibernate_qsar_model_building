package gov.epa.run_from_java.scripts.PredictionDashboard.Percepta;



import java.util.List;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;

/**
* @author TMARTI02
*/
public class Percepta_Report extends PredictionReport {

	public Percepta_Report(PredictionDashboard pd, Dataset dataset) {
		setChemicalIdentifiers(pd);
		
		Property property=dataset.getProperty();
		String unitAbbreviation=dataset.getUnitContributor().getAbbreviation_ccd();
		
		setModelDetails(pd,property);
		setModelResults(pd, unitAbbreviation);
		setNeighbors(pd);
//		System.out.println(this.modelDetails.description);
	}
	
	private void setModelDetails(PredictionDashboard pd,Property property) {
		this.modelDetails.modelId=pd.getModel().getId();

//		String datasetName=pd.getModel().getDatasetName();
		this.modelDetails.propertyName=property.getName_ccd();
		this.modelDetails.propertyDescription=property.getDescription();
				
		this.modelDetails.dataAccessability=getDataAccessibility(pd);
		this.modelDetails.modelName=pd.getModel().getName();
		
		//Make model name shorter for display:
//		this.modelDetails.modelName=modelDetails.modelName.substring(0,modelDetails.modelName.indexOf(" OPERA"));//simplify for display
		this.modelDetails.modelName=modelDetails.modelName;//simplify for display
		
		this.modelDetails.modelSourceName=pd.getModel().getSource().getName();
		this.modelDetails.modelSourceURL=pd.getModel().getSource().getUrl();
		this.modelDetails.modelSourceDescription=pd.getModel().getSource().getDescription();
		
//		this.modelDetails.modelVersion=pd.getModel().getSource().getCreatedBy();
//		this.modelDetails.source=pd.getModel().getSource().getName();	

		
		this.modelDetails.category="QSAR";
		
		this.modelDetails.hasQmrfPdf=0;
		this.modelDetails.hasPlots=0;
		
		
	}
	
	/**
	 * Fix things
	 * 
	 * @param json
	 * @return
	 */
	public static PredictionReport updatePerceptaReport(String json) {

		PredictionReport pr=Utilities.gson.fromJson(json, PredictionReport.class);
		pr.updated=false;
		
		if(pr.modelDetails.propertyName.equals("Molar Volume") && pr.modelResults.standardUnit.equals("cm^3")) {
			pr.modelResults.standardUnit="cm^3/mol";			
			pr.updated=true;
		}
		
		if(pr.modelResults.temperatureUnits!=null && pr.modelResults.temperatureUnits.equals("Celsius")) {
			pr.modelResults.temperatureUnits="°C";
			pr.updated=true;			
		}
		
//		if(pr.updated) {
//			System.out.println(Utilities.gson.toJson(pr));
//		}
		return pr;
	}

	
	/**
	 * Whether or not to expose to dashboard- should this be stored in the database somewhere?
	 * 
	 * @param pd
	 * @return
	 */
	private String getDataAccessibility(PredictionDashboard pd) {
		if(pd.getModel().getIs_public()) return "Public";
		else return "Private";
	}
	
    
}
