package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.awt.Desktop;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.run_from_java.scripts.EpiSuite.EpisuiteResults.Model;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.OPERA_Report;

/**
* @author TMARTI02
*/
public class PredictionReport {
	
	public ChemicalIdentifiers chemicalIdentifiers=new ChemicalIdentifiers();
	
	public ModelDetails modelDetails=new ModelDetails();
	public ModelResults modelResults=new ModelResults();
	public List<Neighbor>neighborsTraining=null;
	public List<Neighbor>neighborsPrediction=null;
	
	public Boolean updated=false;
		
	public class ChemicalIdentifiers{
		public String preferredName;
		public String dtxsid;
		public String dtxcid;
		public String casrn;
		public String smiles;
	}
	
	public class ModelDetails {
		public long modelId;
		public String modelName;
		public String modelSourceName;
		public String modelSourceURL;
		public String modelSourceDescription;

//		public String modelVersion;
		
		public String propertyName;
		public String propertyDescription;
		public String dataAccessability;
		public String category;
//		public String source;
		public int hasQmrfPdf;//TODO change to binary?
		public String qmrfReportUrl;
		
		public int hasPlots;//TODO change to binary?
		public String urlHistogram;
		public String urlScatterPlot;

		public ArrayList<Model> individualModels;//TODO add simplified Model class to this class
		
		public Performance performance;
		
	}
	
	
	
	public class ModelResults {

		public Double experimentalValue;
		public String experimentalString;

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

		public List<ADEstimate>adEstimates;
		
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
			
			
			if(qpn.getDsstoxRecord()!=null) {
				DsstoxRecord dr=qpn.getDsstoxRecord();
				this.dtxsid=dr.getDtxsid();
				this.casrn=dr.getCasrn();
				this.dtxcid=dr.getDtxcid();
				this.molImagePNGAvailable=dr.isMolImagePNGAvailable();
				this.preferredName=dr.getPreferredName();

			} else {
				this.dtxsid=qpn.getDtxsid();
				this.casrn=qpn.getCasrn();
				this.molImagePNGAvailable=false;
			}
		}

	}
	
	protected Performance setStatistics(PredictionDashboard pd) {
		
		
		Performance performance=new Performance();
		
		for (ModelStatistic ms:pd.getModel().getModelStatistics()) {
			
			if (ms.getStatistic().getName().equals(DevQsarConstants.PEARSON_RSQ_TRAINING)) {
				performance.train.R2=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.RMSE_TRAINING)) {
				performance.train.RMSE=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.PEARSON_RSQ_CV_TRAINING)) {
				performance.fiveFoldICV.Q2=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.RMSE_CV_TRAINING)) {
				performance.fiveFoldICV.RMSE=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.PEARSON_RSQ_TEST)) {
				performance.external.R2=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.RMSE_TEST)) {
				performance.external.RMSE=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.BA_TRAINING)) {
				performance.train.BA=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SN_TRAINING)) {
				performance.train.SN=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SP_TRAINING)) {
				performance.train.SP=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.BA_CV_TRAINING)) {
				performance.fiveFoldICV.BA=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SN_CV_TRAINING)) {
				performance.fiveFoldICV.SN=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SP_CV_TRAINING)) {
				performance.fiveFoldICV.SP=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.BA_TEST)) {
				performance.external.BA=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SN_TEST)) {
				performance.external.SN=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SP_TEST)) {
				performance.external.SP=ms.getStatisticValue();
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
	}
	
	
	public class Performance {
		
		public Train train=new Train();
		public FiveFoldICV fiveFoldICV=new FiveFoldICV();
		public External external=new External();
		
		public class Train {
			public Double R2;
			public Double RMSE;
			
			public Double BA;
			public Double SN;
			public Double SP;
			
		}
		
		public class FiveFoldICV {
			public Double Q2;
			public Double RMSE;
			
			public Double BA;
			public Double SN;
			public Double SP;
		}
		
		public class External {
			public Double R2;
			public Double RMSE;

			public Double BA;
			public Double SN;
			public Double SP;
		}
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
	
	private void setADEstimates(PredictionDashboard pd) {

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

	
	public void setNeighbors(PredictionDashboard pd) {
		
		if(pd.getQsarPredictedNeighbors()==null) return;
		
		neighborsTraining=new ArrayList<>();
		for (QsarPredictedNeighbor n:pd.getQsarPredictedNeighbors()) {
			this.neighborsTraining.add(new Neighbor(n));
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
		toJsonFile(folder,this.chemicalIdentifiers.dtxcid+"_"+this.modelDetails.modelName+".json");
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
