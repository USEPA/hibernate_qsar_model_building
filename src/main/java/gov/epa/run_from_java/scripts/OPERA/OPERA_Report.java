package gov.epa.run_from_java.scripts.OPERA;

import java.awt.Desktop;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;

/**
* @author TMARTI02
*/
public class OPERA_Report {

	String urlHistogramAPI="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/histo-graph/by-modelid/";
	String urlScatterPlotAPI="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/scatter-graph/by-modelid/";
	
	public static String urlHistogramAPIOld="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/histo-graph/by-dtxsid/DTXSID3039242/";
	public static String urlScatterPlotAPIOld="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/scatter-graph/by-dtxsid/DTXSID3039242/";
	
	String urlQMRF_API="https://comptox.epa.gov/dashboard-api/ccdapp1/qmrfdata/file/by-modelid/";
	
	ChemicalIdentifiers chemicalIdentifiers=new ChemicalIdentifiers();
	ModelDetails modelDetails=new ModelDetails();
	ModelResults modelResults=new ModelResults();
	
	List<Neighbor>neighbors=new ArrayList<>();
	
	class ChemicalIdentifiers{
		String preferredName;
		String dtxsid;
		String dtxcid;
		String casrn;
	}
	
	class ModelDetails {
		long modelId;
		String modelName;
		String modelSource;
		String modelSourceURL;
		String propertyName;
		String propertyDescription;
		String dataAccessability;
		String category;
		String source;
		String sourceDescription;
		int hasQmrfPdf;
		int hasPlots;
		String qmrfReportUrl;
		String description;
		String urlHistogram;
		String urlScatterPlot;
	}
	
	class ADEstimate {
		ADMethod adMethod;
		String value;
		String conclusion;
		String reasoning;
		String reasoningHtml;
		String reliability;
		
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

	class ADMethod {
		String name;
		String description;
	}
	
	class ModelResults {

		Double experimentalValue;
		String experimentalString;

		Double predictedValue;
		String predictedString;

		String operaVersion;
		String standardUnit;

		
//		int global;
//		String local;
//		String confidence;
//		Msgs msgs=new Msgs();
//
//		class Msgs {
//			String globalTitle;
//			String localTitle;
//			String confidenceTitle;
//		}
		
		List<ADEstimate>adEstimates;
		
//		double adValue;
//		String adReliability;
//		String adReasoning;
		
		Performance performance=new Performance();
		
		class Performance {
			
			Train train=new Train();
			FiveFoldICV fiveFoldICV=new FiveFoldICV();
			External external=new External();
			
			class Train {
				Double R2;
				Double RMSE;
				
				Double BA;
				Double SN;
				Double SP;
				
			}
			
			class FiveFoldICV {
				Double Q2;
				Double RMSE;
				
				Double BA;
				Double SN;
				Double SP;
			}
			
			class External {
				Double R2;
				Double RMSE;

				Double BA;
				Double SN;
				Double SP;
			}
		}
		
		
//		msgs msgs=new msgs();
		
	}
	
//	class msgs {
//		String confidenceTitle;
//		String globalTitle;
//		String localTitle;
//	}
	
	class Neighbor {
		
		int neighborNumber;//done
		
		Double experimentalValue;//done
		Double predictedValue;//done
		
		String experimentalString;//done
		String predictedString;//done
		
		String dtxcid;
		String dtxsid;
		String casrn;
		boolean molImagePNGAvailable;//default is false
		String preferredName; //SCDCD had this as preferred_name which is inconsistent with naming scheme of other classes
		String matchBy;

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
	
	
	public OPERA_Report(PredictionDashboard pd, Property property, String unitAbbreviation,boolean useLatestModelIds) {
		setChemicalIdentifiers(pd);
		setModelDetails(pd,property,useLatestModelIds);
		setModelResults(pd, unitAbbreviation,property);
		setNeighbors(pd);
//		System.out.println(this.modelDetails.description);
	}

	private void setNeighbors(PredictionDashboard pd) {
		for (QsarPredictedNeighbor n:pd.getQsarPredictedNeighbors()) {
			this.neighbors.add(new Neighbor(n));
		}
	}

	private void setChemicalIdentifiers(PredictionDashboard pd) {
		
		if(pd.getDsstoxRecord()==null) {
			return;
		}
		
		this.chemicalIdentifiers.dtxsid=pd.getDsstoxRecord().getDtxsid();
		this.chemicalIdentifiers.dtxcid=pd.getDsstoxRecord().getDtxcid();
		this.chemicalIdentifiers.casrn=pd.getDsstoxRecord().getCasrn();
		this.chemicalIdentifiers.preferredName=pd.getDsstoxRecord().getPreferredName();
	}

	
	private void setModelResults(PredictionDashboard pd,String unitAbbreviation,Property property) {

		modelResults.standardUnit=unitAbbreviation;
		
		if (modelResults.standardUnit!=null && modelResults.standardUnit.equals("Binary")) modelResults.standardUnit="";
		if (modelResults.standardUnit==null) modelResults.standardUnit="";
		
		setExperimental(pd, property); 
		setPrediction(pd, property);
		setADEstimates(pd);
//		System.out.println(pd.getModel().getName()+"\t"+pd.getModel().getModelStatistics().size());
		setStatistics(pd);
	}

	private void setStatistics(PredictionDashboard pd) {
		for (ModelStatistic ms:pd.getModel().getModelStatistics()) {
			
			if (ms.getStatistic().getName().equals(DevQsarConstants.PEARSON_RSQ_TRAINING)) {
				this.modelResults.performance.train.R2=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.RMSE_TRAINING)) {
				this.modelResults.performance.train.RMSE=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.PEARSON_RSQ_CV_TRAINING)) {
				this.modelResults.performance.fiveFoldICV.Q2=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.RMSE_CV_TRAINING)) {
				this.modelResults.performance.fiveFoldICV.RMSE=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.PEARSON_RSQ_TEST)) {
				this.modelResults.performance.external.R2=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.RMSE_TEST)) {
				this.modelResults.performance.external.RMSE=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.BA_TRAINING)) {
				this.modelResults.performance.train.BA=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SN_TRAINING)) {
				this.modelResults.performance.train.SN=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SP_TRAINING)) {
				this.modelResults.performance.train.SP=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.BA_CV_TRAINING)) {
				this.modelResults.performance.fiveFoldICV.BA=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SN_CV_TRAINING)) {
				this.modelResults.performance.fiveFoldICV.SN=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SP_CV_TRAINING)) {
				this.modelResults.performance.fiveFoldICV.SP=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.BA_TEST)) {
				this.modelResults.performance.external.BA=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SN_TEST)) {
				this.modelResults.performance.external.SN=ms.getStatisticValue();
			} else if (ms.getStatistic().getName().equals(DevQsarConstants.SP_TEST)) {
				this.modelResults.performance.external.SP=ms.getStatisticValue();
			}
		}
	}

	private void setExperimental(PredictionDashboard pd, Property property) {
		
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

	private void setPrediction(PredictionDashboard pd, Property property) {
		
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

	private void setADEstimates(PredictionDashboard pd) {

		modelResults.adEstimates=new ArrayList<ADEstimate>();
		
		for (QsarPredictedADEstimate qsarADEstimate: pd.getQsarPredictedADEstimates()) {
			ADEstimate adEstimate=new ADEstimate(qsarADEstimate);
			
			if(qsarADEstimate.getMethodAD().getName().contentEquals(DevQsarConstants.Applicability_Domain_Combined)) {
				String reasoning=qsarADEstimate.getReasoning();
				adEstimate.reasoning=reasoning;
				setReasoningHTML(adEstimate, reasoning);
			}
			modelResults.adEstimates.add(adEstimate);
		}

//Old way that matches what SCDCD used in json in their db:
//		for (QsarPredictedADEstimate ad:pd.getQsarPredictedADEstimates()) {
//		if(ad.getMethodAD().getName().equals(strGlobalAD)) {
//			modelResults.global=ad.getApplicabilityValue().intValue();
//			if (ad.getApplicabilityValue()==1) modelResults.msgs.globalTitle="Inside";	
//			else modelResults.msgs.globalTitle="Outside";
//		}
//
//		if(ad.getMethodAD().getName().equals(strLocalAD)) {
//			modelResults.local=ad.getApplicabilityValue()+"";
//			modelResults.msgs.localTitle=modelResults.local;
//		}
//
//		if(ad.getMethodAD().getName().equals(strConfidenceIndex)) {
//			modelResults.confidence=ad.getApplicabilityValue()+"";
//			modelResults.msgs.confidenceTitle=modelResults.confidence;
//		}
//	}

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
	

//	private ADEstimate setOverallAD(PredictionDashboard pd, ModelResults modelResults, ADEstimate adEstimateGlobal, ADEstimate adEstimateLocal, ADEstimate adEstimateConfidence) {
//		
//		int globalValue=Integer.parseInt(adEstimateGlobal.value);
//		double localValue=Double.parseDouble(adEstimateLocal.value);
//		double confidenceValue=Double.parseDouble(adEstimateConfidence.value);
//		
//		ADEstimate adEstimateOverall=new ADEstimate();
//		
//		ADMethod adMethod=new ADMethod();
//		adMethod.name="Overall applicability domain";
//		//					adMethod.description=ad.getMethodAD().getDescription();//from the database
//		adMethod.description="Overall applicability domain from multiple AD measures";
//		
//		adEstimateOverall.adMethod=adMethod;
//		
//		setOverallADResults(adEstimateOverall, globalValue, localValue, confidenceValue);
//		
//		System.out.println(chemicalIdentifiers.dtxcid+"\t"+  modelDetails.propertyName +"\t"+  globalValue+"\t"+localValue+"\t"+confidenceValue+"\t"+"AD conclusion="+adEstimateOverall.reasoning);
//
//		
////		System.out.println(adEstimateOverall.value+"\t"+adEstimateOverall.conclusion);
//		
//		return adEstimateOverall;
//	}

//	private ADEstimate setConfidenceAD(PredictionDashboard pd) {
//		
//		String strConfidenceIndex="OPERA confidence index";
//		
//		for (QsarPredictedADEstimate ad:pd.getQsarPredictedADEstimates()) {
//			if(ad.getMethodAD().getName().equals(strConfidenceIndex)) { 
//				
//				ADEstimate adEstimateConfidence=new ADEstimate();
//				ADMethod adMethod=new ADMethod();
//				adMethod.name="Confidence level";
//				adMethod.description="Confidence level is calculated based on the accuracy of the "
//						+ "predictions of the five nearest neighbors weighted by their distance "
//						+ "to the query chemicals";
//				
//				//TODO make this match what's in the database
//
//				adEstimateConfidence.adMethod=adMethod;
//				adEstimateConfidence.value=ad.getApplicabilityValue().toString();
//				return adEstimateConfidence;
//
//			}
//		}
//		
//		return null;
//	}

//	private ADEstimate setGlobalAD(PredictionDashboard pd) {
//		String strGlobalAD="OPERA global applicability domain";
//
//		for (QsarPredictedADEstimate ad:pd.getQsarPredictedADEstimates()) {
//			if(ad.getMethodAD().getName().equals(strGlobalAD)) {
//				ADEstimate adEstimateGlobal=new ADEstimate();
//
//				ADMethod adMethod=new ADMethod();
//				//					adMethod.name=ad.getMethodAD().getName();
//				adMethod.name="Global applicability domain";
//				//					adMethod.description=ad.getMethodAD().getDescription();//from the database
//				adMethod.description="Global applicability domain via the leverage approach";
//
//				//TODO make this match what's in the database
//				
//				adEstimateGlobal.adMethod=adMethod;
//				adEstimateGlobal.value=ad.getApplicabilityValue().intValue()+"";
//
//				if(ad.getApplicabilityValue().intValue()==1) {
//					adEstimateGlobal.conclusion="Inside";
//				} else {
//					adEstimateGlobal.conclusion="Outside";
//				}
//				return adEstimateGlobal;
//
//			}
//		}
//
//		return null;
//
//	}
//	
//	 private ADEstimate setLocalAD(PredictionDashboard pd) {
//
//		 String strLocalAD="OPERA local applicability domain";
//
//		 for (QsarPredictedADEstimate ad:pd.getQsarPredictedADEstimates()) {
//
//			 if(ad.getMethodAD().getName().equals(strLocalAD)) {
//
//				 ADEstimate adEstimateLocal;
//				 adEstimateLocal=new ADEstimate();
//
//				 ADMethod adMethod=new ADMethod();
//				 adMethod.name="Local applicability domain index";
//				 adMethod.description = "Local applicability domain is relative to the similarity of the "
//						 + "query chemical to its five nearest neighbors";
//				 
//				//TODO make this match what's in the database
//
//				 adEstimateLocal.adMethod=adMethod;
//				 adEstimateLocal.value=ad.getApplicabilityValue().toString();
//
//
//				 return adEstimateLocal;
//
//			 }
//
//		 } 
//
//		 return null;
//	 }


	private void setModelDetails(PredictionDashboard pd,Property property, boolean useLatestModelIds) {
		this.modelDetails.modelId=pd.getModel().getId();

//		String datasetName=pd.getModel().getDatasetName();
//		this.modelDetails.propertyName=datasetName.substring(0,datasetName.indexOf(" OPERA"));

		this.modelDetails.propertyName=property.getName_ccd();
		this.modelDetails.propertyDescription=property.getDescription();
		
		
		this.modelDetails.dataAccessability=getDataAccessibility(pd);
		this.modelDetails.modelName=pd.getModel().getName();
		
		//Make model name shorter for display:
		this.modelDetails.modelName=modelDetails.modelName.substring(0,modelDetails.modelName.indexOf(" OPERA"));//simplify for display
		this.modelDetails.modelSource=pd.getModel().getSource().getName();
		this.modelDetails.modelSourceURL="https://github.com/kmansouri/OPERA";
		
		this.modelDetails.category="QSAR";
		this.modelDetails.source=pd.getModel().getSource().getName();
		
		this.modelDetails.sourceDescription="OPERA is a free and open source/open "
				+ "data suite of QSAR Models providing predictions and additional "
				+ "information including applicability domain and accuracy assessment, "
				+ "as described in the <a href=\"http://dx.doi.org/10.1186/s13321-018-0263-1\">OPERA publication</a>. "
				+ "All models were built on curated data and standardized chemical "
				+ "structures as described in <a href=\"http://dx.doi.org/10.1080/1062936X.2016.1253611\">Mansouri et al, 2016</a>. All OPERA properties are "
				+ "predicted under ambient conditions of 760mm of Hg at 25 degrees Celsius.";
				
		this.modelDetails.hasQmrfPdf=hasQMRF(pd);
		this.modelDetails.hasPlots=hasPlots(property.getName());
		
		this.modelDetails.description=pd.getModel().getSource().getDescription();
		
		if (useLatestModelIds) {
			//TODO OPERA scatter plot and histograms are stored in model_files table with fileType=3 and 4- could get images from there instead
			if(this.modelDetails.hasPlots==1) {
				this.modelDetails.urlHistogram=urlHistogramAPI+pd.getModel().getId();				
				this.modelDetails.urlScatterPlot=urlScatterPlotAPI+pd.getModel().getId();
			}
			if(modelDetails.hasQmrfPdf==1) this.modelDetails.qmrfReportUrl=urlQMRF_API+pd.getModel().getId();//these need to be available from Asif's API for that model Id
		
		} else {
			int modelID_old=getOldModelID(property.getName());
			
			if(modelID_old==-1) modelDetails.hasQmrfPdf=0;//we dont have oral rat ld50 qmrf available 
			
			if (modelID_old!=-1 && modelDetails.hasPlots==1)  {
				this.modelDetails.urlHistogram=urlHistogramAPIOld+modelID_old;
				this.modelDetails.urlScatterPlot=urlScatterPlotAPIOld+modelID_old;
			}
			if(modelDetails.hasQmrfPdf==1) this.modelDetails.qmrfReportUrl=urlQMRF_API+modelID_old;//these need to be available from Asif's API for that model Id
		}
		
	}
	
	
	public static int getOldModelID(String propertyName) {
		switch (propertyName) {
			case DevQsarConstants.BOILING_POINT:
				return 27;
			case DevQsarConstants.HENRYS_LAW_CONSTANT:
				return 19;
			case DevQsarConstants.LOG_KOA:
				return 26;
			case DevQsarConstants.LOG_KOW:
				return 22;
			case DevQsarConstants.MELTING_POINT:
				return 18;
			case DevQsarConstants.VAPOR_PRESSURE:
				return 30;
			case DevQsarConstants.WATER_SOLUBILITY:
				return 24;
			case DevQsarConstants.OH:
				return 29;
			case DevQsarConstants.BIODEG_HL_HC:
				return 17;
			case DevQsarConstants.RBIODEG:
				return 20;
			case DevQsarConstants.KmHL:
				return 28;
			case DevQsarConstants.KOC:
				return 25;
			case DevQsarConstants.BCF:
				return 23;

			default:
				return -1;
		
		}
		
	}
	
	
	/**
	 * Whether or not to expose to dashboard- should this be stored in the database somewhere?
	 * 
	 * @param pd
	 * @return
	 */
	String getDataAccessibility(PredictionDashboard pd) {
		String modelAbbrev=pd.getModel().getName().replace("OPERA_", "");
		
		switch (modelAbbrev) {
			case "CACO2":
			case "CLINT":
			case "FUB":	
			case "pKa_a":
			case "pKa_b":
				return "Private";
		
			default:
				return "Public";
		}
		
		
	}
	
	/**
	 * Whether or not there is a QMRF
	 * 
	 * For now based on whether there is a QMRF in the OPERA2.8 "QMRFs.zip" file
	 * 
	 * TODO- query the db instead for the QMRF for the model???
	 * 
	 * @param pd
	 * @return
	 */
	int hasQMRF(PredictionDashboard pd) {
		
//		System.out.println(pd.getModel().getName());
		
		String n=pd.getModel().getDatasetName();
		n=n.substring(0,n.indexOf(" OPERA"));

		switch (n) {
		case DevQsarConstants.ORAL_RAT_LD50:
		case DevQsarConstants.ORAL_RAT_NON_TOXIC:
		case DevQsarConstants.ORAL_RAT_VERY_TOXIC:
		case DevQsarConstants.ORAL_RAT_GHS_CATEGORY:
		case DevQsarConstants.ORAL_RAT_EPA_CATEGORY:			
		case DevQsarConstants.MELTING_POINT:
		case DevQsarConstants.BOILING_POINT:
		case DevQsarConstants.WATER_SOLUBILITY:
		case DevQsarConstants.VAPOR_PRESSURE:
		case DevQsarConstants.LOG_KOW:
		case DevQsarConstants.LOG_KOA:
		case DevQsarConstants.HENRYS_LAW_CONSTANT:
		case DevQsarConstants.OH:
		case DevQsarConstants.RBIODEG:
		case DevQsarConstants.BIODEG_HL_HC:
		case DevQsarConstants.BCF:
		case DevQsarConstants.KOC:
		case DevQsarConstants.KmHL:
			return 1;
		default:
			return 0;
		}
	}

	public static int hasPlots(String propertyName) {
		
//		System.out.println(pd.getModel().getName());
		

		switch (propertyName) {
//		case DevQsarConstants.ORAL_RAT_LD50://For right now not available
		case DevQsarConstants.MELTING_POINT:
		case DevQsarConstants.BOILING_POINT:
		case DevQsarConstants.WATER_SOLUBILITY:
		case DevQsarConstants.VAPOR_PRESSURE:
		case DevQsarConstants.LOG_KOW:
		case DevQsarConstants.LOG_KOA:
		case DevQsarConstants.HENRYS_LAW_CONSTANT:
		case DevQsarConstants.OH:
		case DevQsarConstants.BIODEG_HL_HC:
		case DevQsarConstants.BCF:
		case DevQsarConstants.KOC:
		case DevQsarConstants.KmHL:
			return 1;
		default:
			return 0;
		}
	}

	String toJson() {
		return Utilities.gson.toJson(this);
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

	
	public static OPERA_Report fromJsonFile(String filepath) {
		try {
			return Utilities.gson.fromJson(new FileReader(filepath), OPERA_Report.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static OPERA_Report fromJson(String json) {
		try {
			return Utilities.gson.fromJson(json, OPERA_Report.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	
	void toJsonFile(String folder) {
		toJsonFile(folder,this.chemicalIdentifiers.dtxcid+"_"+this.modelDetails.modelName+".json");
	}
	
	String toHTMLString() {
		HTML_Report_Creator_From_OPERA_Report rc=new HTML_Report_Creator_From_OPERA_Report();
		return rc.createReport(this);
	}
	
	void toHTMLFile(String folder) {
		toHTMLFile(folder, this.chemicalIdentifiers.dtxcid+"_"+this.modelDetails.modelName+".html");
	}
	
	void toHTMLFile(String folder,String filename) {
		
		try {
			FileWriter fw=new FileWriter(folder+File.separator+filename);
			fw.write(toHTMLString());
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

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
	
	
	public static void main(String[] args) {
		
	}
	
}
