package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreator;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.ExperimentalValue;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Model;

/**
* @author TMARTI02
*/
public class PredictionReport {
	
	public ChemicalIdentifiers chemicalIdentifiers=new ChemicalIdentifiers();
	
	public ModelDetails modelDetails=new ModelDetails();
	public ModelResults modelResults=new ModelResults();
	
	public NeighborResults neighborResultsTraining=null;
	public NeighborResults neighborResultsPrediction=null;
	
	
	public class NeighborResults {
		public NeighborResults(String unitNeighbor,String set) {
			this.unitNeighbor=unitNeighbor;
			this.set=set;			
		}

		public String title;
		public String set;
		public String predictedValueToolTip;
		public String missingExperimentalValueNote;


		//For TEST reports:
		public String chartImgSrc;
		public Double MAE;
		public Double MAEEntireTestSet;
		public String unitNeighbor;
		public String Concordance;
		public String Sensitivity;
		public String Specificity;
		public Double Coverage;
		
		public List<Neighbor>neighbors=new ArrayList<>();

	}
	
	
	public Boolean updated=false;
		
	public class ChemicalIdentifiers{
		public String preferredName;
		public String dtxsid;
		public String dtxcid;
		public String casrn;
		public String smiles;
		public Double molWeight;//TODO add from dsstox record
	}
	
	public class ModelDetails {

		public Long modelId;
		public Long modelIdLegacy;
		public boolean useModelIdLegacy;
		public boolean hasScatterPlot;//TODO change to binary?
		public boolean hasHistogram;
		public boolean hasQmrfPdf;//TODO change to binary?

		public boolean loadPlotsFromDB=false;
		
//		public String urlHistogramAPI;
//		public String urlHistogramAPI_Legacy;
//		public String urlScatterPlotAPI;
//		public String urlScatterPlotAPI_Legacy;
//		public String urlQMRF_API;
		
		public String urlModelAPI;

		public String modelName;
		public String modelSourceName;
		public String modelSourceURL;
		public String modelSourceDescription;

//		public String modelVersion;
		
		public String propertyName;
		public String propertyDescription;
		public boolean propertyIsBinary;

		public String dataAccessability;
		public String category;
//		public String source;
//		public String qmrfReportUrl;

		
//		public String urlHistogram;
//		public String urlScatterPlot;

		//TODO these are episuite models- needs to have local class instead 
//		public ArrayList<Object> individualModels;//TODO add simplified Model class to this class
		
		public List<Model>individualModels;//EPISUITE- TODO store as predictionsIndividualMethod instead
		
		public Performance performance;
		
	}
	
	public class ConsensusPredictions {
		public String unitsPrediction;
		public List<PredictionIndividualMethod>predictionsIndividualMethod;//TEST
		
	}
	
	public class PredictionIndividualMethod {
		public String method;
		public String predictedValue;
	}

	
	public String prediction;

	
	
	public class ModelResults {

		public Double experimentalValue;
		public String experimentalSet;//if experimental value appears in training or test set
		public String experimentalString;
		public String experimentalSource;
//		public ArrayList<ExperimentalValue> experimentalValues;//episuite

		public Double predictedValue;
		public Double predictedUncertainty;//+/- value for software like Percepta
		public String predictedString;
		public String predictedError;
		
		//experimental conditions
		public Double pH;
		public Double pressure;
		public String pressureUnits;
		public Double temperature;
		public String temperatureUnits;


		//Percepta properties
		public String equation;
		public String dissType_Apparent;
		public String accuracyExplanation_Apparent;
		
		public String standardUnit;
		public String originalUnit;
		public String neighborUnit;

		public List<ADEstimate>adEstimates;
		
		public Boolean useCombinedApplicabilityDomain;
		public ConsensusPredictions consensusPredictions;
		
		
//		double adValue;
//		String adReliability;
//		String adReasoning;

		//TODO add neighbors for AD estimate 
		

		//		msgs msgs=new msgs();
		
	}
	
	public class ADEstimate {
		
		public ADMethod adMethod;
		public String value;
		public String conclusion;
		public String reasoning;
		public String reasoningHtml;
		public String reliability;
		
		public ADEstimate(QsarPredictedADEstimate adEstimate) {
			
			if (adEstimate.getApplicabilityValue()!=null)
				this.value=adEstimate.getApplicabilityValue().toString();
			
			this.conclusion=adEstimate.getConclusion();
			this.reasoning=adEstimate.getReasoning();
			
			ADMethod adMethod=new ADMethod();
			adMethod.name=adEstimate.getMethodAD().getName_display();
			adMethod.description=adEstimate.getMethodAD().getDescription();			
			this.adMethod=adMethod;
		}

		
	}

	public class ADMethod {
		public String name;
		public String description;
	}
	

	public class Neighbor {
		
		public int neighborNumber;//done
		
		public Double similarityCoefficient;
		
		public Double experimentalValue;//done
		public Double predictedValue;//done
		
		public String experimentalString;//done
		public String predictedString;//done
		
		public String dtxcid;
		public String dtxsid;
		public String casrn;
		public boolean molImagePNGAvailable;//default is false
		public String preferredName; //SCDCD had this as preferred_name which is inconsistent with naming scheme of other classes
		public String matchBy;

		public String backgroundColor;

//		long cid;
//		String gsid;//needed?
//		DsstoxRecord dsstoxRecord;

		public Neighbor(QsarPredictedNeighbor qpn) {
			
			this.neighborNumber=qpn.getNeighborNumber();
			
			this.experimentalValue=qpn.getExperimentalValue();
			this.experimentalString=qpn.getExperimentalString();
			
			this.predictedValue=qpn.getPredictedValue();
			this.predictedString=qpn.getPredictedString();
			
			this.matchBy=qpn.getMatchBy();
			
			if(qpn.getSimilarityCoefficient()!=null) {
				this.similarityCoefficient=qpn.getSimilarityCoefficient();
				this.backgroundColor=PredictToxicityWebPageCreator.getColorString(similarityCoefficient);
			}
			
			
			if(qpn.getDsstoxRecord()!=null) {
				DsstoxRecord dr=qpn.getDsstoxRecord();
				this.dtxsid=dr.getDtxsid();
				this.casrn=dr.getCasrn();
				this.dtxcid=dr.getDtxcid();
//				this.molImagePNGAvailable=dr.isMolImagePNGAvailable();
				
				if(dr.getDtxcid()!=null) {
					this.molImagePNGAvailable=true;	
				} else {
					this.molImagePNGAvailable=false;
				}
				this.preferredName=dr.getPreferredName();

			} else {
				this.dtxsid=qpn.getDtxsid();
				this.casrn=qpn.getCasrn();
				this.molImagePNGAvailable=false;
			}
		}

	}
	
	
	protected void setModelDetails(PredictionDashboard pd,Property property,boolean useLegacyModelIds,boolean propertyIsBinary) {

		this.modelDetails.urlModelAPI="https://ctx-api-dev.ccte.epa.gov/chemical/property/model/file/search/";
		
//		this.modelDetails.urlHistogramAPI="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/histo-graph/by-modelid/";
//		this.modelDetails.urlScatterPlotAPI="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/scatter-graph/by-modelid/";
//		this.modelDetails.urlHistogramAPI_Legacy="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/histo-graph/by-dtxsid/DTXSID3039242/";
//		this.modelDetails.urlScatterPlotAPI_Legacy="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/scatter-graph/by-dtxsid/DTXSID3039242/";
//		this.modelDetails.urlQMRF_API="https://comptox.epa.gov/dashboard-api/ccdapp1/qmrfdata/file/by-modelid/";

		this.modelDetails.modelId=pd.getModel().getId();
		this.modelDetails.useModelIdLegacy=useLegacyModelIds;
		if(modelDetails.modelIdLegacy==null) modelDetails.useModelIdLegacy=false;

		//		String datasetName=pd.getModel().getDatasetName();
		//		this.modelDetails.propertyName=datasetName.substring(0,datasetName.indexOf(" OPERA"));

		this.modelDetails.propertyName=property.getName_ccd();
		this.modelDetails.propertyDescription=property.getDescription();
		
		this.modelDetails.propertyIsBinary=propertyIsBinary;

		this.modelDetails.modelName=pd.getModel().getName_ccd();
		
		//Make model name shorter for display:
//		this.modelDetails.modelName=modelDetails.modelName.substring(0,modelDetails.modelName.indexOf(" OPERA"));//simplify for display

		this.modelDetails.category="QSAR";
		//		this.modelDetails.source=pd.getModel().getSource().getName();

		this.modelDetails.modelSourceName=pd.getModel().getSource().getName();
		this.modelDetails.modelSourceURL=pd.getModel().getSource().getUrl();
		this.modelDetails.modelSourceDescription=pd.getModel().getSource().getDescription();

		modelDetails.performance=setStatistics(pd);

	}

	
	protected Performance setStatistics(PredictionDashboard pd) {
		
		
		Performance performance=new Performance();
		
		for (ModelStatistic ms:pd.getModel().getModelStatistics()) {
			
			Statistics statistics=null;
			
			if (ms.getStatistic().getName().contains(DevQsarConstants.TAG_TEST)) {
				statistics=performance.external;
			} else if (ms.getStatistic().getName().contains(DevQsarConstants.TAG_CV)) {
				statistics=performance.fiveFoldICV;
			} else if (ms.getStatistic().getName().contains(DevQsarConstants.TAG_TRAINING)) {
				statistics=performance.train;
			}
						
			
			if (ms.getStatistic().getName().contains(DevQsarConstants.PEARSON_RSQ)) {
				if (ms.getStatistic().getName().contains(DevQsarConstants.TAG_CV)) {
					statistics.Q2=ms.getStatisticValue();					
				} else {
					statistics.R2=ms.getStatisticValue();					
				}
			} else if (ms.getStatistic().getName().contains(DevQsarConstants.COVERAGE)) {
				statistics.COVERAGE=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().contains(DevQsarConstants.MAE)) {
				statistics.MAE=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().contains(DevQsarConstants.RMSE)) {
				statistics.RMSE=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().contains(DevQsarConstants.BALANCED_ACCURACY)) {
				statistics.BA=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().contains(DevQsarConstants.SENSITIVITY)) {
				statistics.SN=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().contains(DevQsarConstants.SPECIFICITY)) {
				statistics.SP=ms.getStatisticValue();
			} 
		}
		
		return performance;
	}

	

	public void setChemicalIdentifiers(PredictionDashboard pd) {
		
		if(pd.getDsstoxRecord()==null) {
			return;
		}
		
		DsstoxRecord dr=pd.getDsstoxRecord();
		
		this.chemicalIdentifiers.dtxsid=dr.getDtxsid();
		this.chemicalIdentifiers.dtxcid=dr.getDtxcid();
		this.chemicalIdentifiers.casrn=dr.getCasrn();
		this.chemicalIdentifiers.preferredName=dr.getPreferredName();
		this.chemicalIdentifiers.smiles=dr.getSmiles();
		this.chemicalIdentifiers.molWeight=dr.getMolWeight();
	}
	
	
	public class Performance {
		
		public Statistics train=new Statistics();
		public Statistics fiveFoldICV=new Statistics();
		public Statistics external=new Statistics();
		
	}
	
	public class Statistics {
		public Double R2;
		public Double Q2;
		public Double RMSE;
		public Double MAE;
		public Double BA;
		public Double SN;
		public Double SP;
		public Double COVERAGE;
	}

	
	public void setModelResults(PredictionDashboard pd,String unitAbbreviation) {

		modelResults.standardUnit=unitAbbreviation;

		//TODO make it pretty when converting HTML but best to keep units in the Json
		
//		if (modelResults.standardUnit!=null && modelResults.standardUnit.equals("Binary")) modelResults.standardUnit="";
//		if (modelResults.standardUnit==null) modelResults.standardUnit="";
		
		setExperimental(pd); 
		setPrediction(pd);
		setADEstimates(pd);
		
//		System.out.println(pd.getModel().getName()+"\t"+pd.getModel().getModelStatistics().size());
		
	}
	
	
	
	public void setADEstimates(PredictionDashboard pd) {

		if(pd.getQsarPredictedADEstimates()==null) return;
		
		modelResults.adEstimates=new ArrayList<ADEstimate>();
		
		for (QsarPredictedADEstimate qsarADEstimate: pd.getQsarPredictedADEstimates()) {
			ADEstimate adEstimate=new ADEstimate(qsarADEstimate);
			
			if(qsarADEstimate.getMethodAD().getName().contentEquals(DevQsarConstants.Applicability_Domain_Combined)) {
				setReasoningHTML(adEstimate, qsarADEstimate.getReasoning());
			}
			modelResults.adEstimates.add(adEstimate);
		}
	}
	
	private void setReasoningHTML(ADEstimate adEstimate, String reasoning) {
		String globalMethodDescription=DevQsarConstants.Applicability_Domain_OPERA_global_index_description;
		String localMethodDescription=DevQsarConstants.Applicability_Domain_OPERA_local_index_description;
		
		String fancyGlobal="<div class=\"tooltip\">Global AD&nbsp;"+
				"<span class=\"tooltiptext\">"+globalMethodDescription+"</span></div>";
		
		String fancyLocal="<div class=\"tooltip\">Local AD index<span class=\"tooltiptext\">"+localMethodDescription+"</span></div>";

		
		reasoning=reasoning.replace("Global AD", fancyGlobal);
		reasoning=reasoning.replace("Local AD index", fancyLocal);
		
		reasoning=addColor(reasoning,"Inside","green");
		reasoning=addColor(reasoning,"Outside","red");
		reasoning=addColor(reasoning,"poor","red");
		reasoning=addColor(reasoning,"fair","orange");
		reasoning=addColor(reasoning,"good","green");
		adEstimate.reasoningHtml=reasoning;
	}
	
	String addColor(String reasoning, String text,String color)  {
		if (!reasoning.contains(text)) return reasoning;
		return reasoning.replace(text, "<font color="+color+"><b>"+text+"</b></font>");
	}

	
	
	public void setExperimental(PredictionDashboard pd) {
		
		modelResults.experimentalValue=pd.getExperimentalValue();
		modelResults.experimentalString=pd.getExperimentalString();
		
//		if (pd.getExperimentalString()!=null) {
//			modelResults.experimental=pd.getExperimentalString();
//		} else if (pd.getExperimentalValue()!=null) {
//			
//
//			String id=null;
//			
//			if(pd.getDsstoxRecord()!=null) {
//				id=pd.getDsstoxRecord().getDtxcid();
//			}
//			
//			modelResults.experimentalConclusion=OPERA_csv_to_PostGres_DB.getBinaryConclusion(property.getName(), pd.getExperimentalValue());
//			
//		}
	}
	
	private void setPrediction(PredictionDashboard pd) {

		modelResults.predictedValue=pd.getPredictionValue();
		modelResults.predictedString=pd.getPredictionString();

		//		if (pd.getPredictionString()!=null) {
		//			modelResults.predicted=pd.getPredictionString();
		//			
		//		} else if (pd.getPredictionValue()!=null) {
		//			modelResults.predicted=pd.getPredictionValue()+"";
		//			
		//			String id="";
		//
		//			if(pd.getDsstoxRecord()!=null) {
		//				if(pd.getDsstoxRecord().getDtxcid()!=null) {
		//					id=pd.getDsstoxRecord().getDtxcid();
		//				}
		//			}
		//			
		//			modelResults.predictedConclusion=pd.getPredictionString();
		//			
		////			} else if(property.getName().equals(DevQsarConstants.ORAL_RAT_VERY_TOXIC)) {
		////				if(pd.getPredictionValue()==0) modelResults.predictedConclusion="Not very toxic: oral rat LD50 > 50 mg/kg";
		////				else modelResults.predictedConclusion="Very toxic: oral rat LD50 ≤ 50 mg/kg";
		////			} else if(property.getName().equals(DevQsarConstants.ORAL_RAT_NON_TOXIC)) {
		////				if(pd.getPredictionValue()==1) modelResults.predictedConclusion="Nontoxic: oral rat LD50 > 2000 mg/kg";
		////				else modelResults.predictedConclusion="Not nontoxic: oral rat LD50 ≤ 2000 mg/kg";
		////			} else if(property.getName().equals(DevQsarConstants.ORAL_RAT_EPA_CATEGORY)) {
		////				modelResults.predictedConclusion=getConclusionEPA(pd.getPredictionValue());
		////			} else if(property.getName().equals(DevQsarConstants.ORAL_RAT_GHS_CATEGORY)) {
		////				modelResults.predictedConclusion=getConclusionGHS(pd.getPredictionValue());		
		////			}
		//			
		//		}
	}

	
	public void setNeighbors(PredictionDashboard pd,String unitNeighbor) {
		
		if(pd.getQsarPredictedNeighbors()==null) return;
		
		neighborResultsTraining=new NeighborResults(unitNeighbor,"Training");
		neighborResultsPrediction=new NeighborResults(unitNeighbor,"Test");
		
		for (QsarPredictedNeighbor n:pd.getQsarPredictedNeighbors()) {
			
			if(n.getSplitNum()==0)
				this.neighborResultsTraining.neighbors.add(new Neighbor(n));
			else if(n.getSplitNum()==1)
				this.neighborResultsPrediction.neighbors.add(new Neighbor(n));
		}
	}
	
	public static PredictionReport fromJson(String json) {
		return Utilities.gson.fromJson(json, PredictionReport.class);
	}
	
	public String toJson() {
		return Utilities.gson.toJson(this);
	}
	
	
	public static PredictionReport fromJsonFile(String filepath) {
		try {
			return Utilities.gson.fromJson(new FileReader(filepath), PredictionReport.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	void toJsonFile(String folder,String filename) {
		try {
			FileWriter fw=new FileWriter(folder+File.separator+filename);
			fw.write(this.toJson());
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	public static void toFile(String content, String folder,String filename) {
		try {
			FileWriter fw=new FileWriter(folder+File.separator+filename);
			fw.write(content);
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	void toJsonFile(String json, String folder,String filename) {
		try {
			FileWriter fw=new FileWriter(folder+File.separator+filename);
			fw.write(json);
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	
	public void toJsonFile(String folder) {		
		toJsonFile(folder,this.chemicalIdentifiers.dtxsid+"_"+this.modelDetails.modelName+".json");
	}
	
	
	
	
	public static void viewInWebBrowser(String filepath) {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new File(filepath).toURI());
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}


	
}



