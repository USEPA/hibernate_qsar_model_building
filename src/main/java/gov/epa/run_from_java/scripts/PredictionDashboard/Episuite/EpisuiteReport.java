package gov.epa.run_from_java.scripts.PredictionDashboard.Episuite;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.run_from_java.scripts.EpiSuite.EpisuiteResults.Parameters;
import gov.epa.run_from_java.scripts.EpiSuite.EpisuiteResults.PropertyResult2;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;

/**
* @author TMARTI02
*/
public class EpisuiteReport extends PredictionReport{
	
	Parameters parameters;
	
	
	public EpisuiteReport(PropertyResult2 pr,PredictionDashboard pd,Dataset dataset) {
		setChemicalIdentifiers(pd);
					
		setModelDetails(pd,dataset);
		setModelResults(pd, dataset.getUnitContributor().getAbbreviation_ccd());
		
		parameters=pr.parameters;		
		modelDetails.individualModels=pr.estimatedValue.model;//TODO convert to a format similar to the way models are stored in TEST reports
		
//	    EstimatedValue2 estimatedValue;
		//	    public String error;
		//	    public String applicabilityDomainConclusion;
		//	    public String notes;

//	    public ArrayList<ExperimentalValue> experimentalValues;//are there ever more than 1?
		
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
	
	
	
	private void setModelDetails(PredictionDashboard pd,Dataset dataset) {
		this.modelDetails.modelId=pd.getModel().getId();

		Property property=dataset.getProperty();
		
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
		
		this.modelDetails.category="QSAR";		
		this.modelDetails.hasQmrfPdf=0;
		this.modelDetails.hasPlots=0;//TODO should we add some plots?
		
		
	}
	

}
