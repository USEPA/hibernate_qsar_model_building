package gov.epa.run_from_java.scripts.PredictionDashboard.Episuite;

import java.util.ArrayList;



import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Parameters;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.PropertyResult2;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.ExperimentalValue;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Model;
/**
* @author TMARTI02
*/
public class EpisuiteReport extends PredictionReport{
	
	Parameters parameters;

//	ModelDetailsEpisuite modelDetails=new ModelDetailsEpisuite();
//	
//	public class ModelDetailsEpisuite extends ModelDetails {
//		ArrayList<Model>individualModels;
//		
//	}
		
	public EpisuiteReport(PropertyResult2 pr,PredictionDashboard pd,Dataset dataset) {
		
		setChemicalIdentifiers(pd);
		setModelDetails(pd,dataset);
		setModelResults(pd, dataset.getUnitContributor().getAbbreviation_ccd());
		
		parameters=pr.parameters;	
		
		//TODO store predicted values in a more general array not dependent on episuite classes
		modelDetails.individualModels=pr.estimatedValue.model;//TODO convert to a format similar to the way models are stored in TEST reports
		
		if(pr.experimentalValues!=null) {
			if(pr.experimentalValues.size()==1 && pr.experimentalValues.get(0).author!=null) {
				ExperimentalValue expValue=pr.experimentalValues.get(0);
				modelResults.experimentalSource=expValue.author+" ("+expValue.year+")";
			} else {
//				System.out.println(modelDetails.propertyName+"\t# expvalues="+pr.experimentalValues.size());
			}
		}
		
//		modelResults.experimentalValues=pr.experimentalValues;//should we just extract the data source?
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
		this.modelDetails.hasQmrfPdf=false;
		this.modelDetails.hasHistogram=false;//TODO should we add some plots?
		this.modelDetails.hasScatterPlot=false;//TODO should we add some plots?
		
		
		
	}
	

}
