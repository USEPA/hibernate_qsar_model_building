package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA;

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
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.ADEstimate;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;

/**
* @author TMARTI02
*/
public class OPERA_Report extends PredictionReport {

	String urlHistogramAPI="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/histo-graph/by-modelid/";
	String urlScatterPlotAPI="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/scatter-graph/by-modelid/";
	
	public static String urlHistogramAPIOld="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/histo-graph/by-dtxsid/DTXSID3039242/";
	public static String urlScatterPlotAPIOld="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/scatter-graph/by-dtxsid/DTXSID3039242/";
	
	String urlQMRF_API="https://comptox.epa.gov/dashboard-api/ccdapp1/qmrfdata/file/by-modelid/";
	
	
	
	/**
	 * Creates report from PredictionDashboard object
	 * @param pd
	 * @param property
	 * @param unitAbbreviation
	 * @param useLatestModelIds
	 */
	public OPERA_Report(PredictionDashboard pd, Property property, String unitAbbreviation,boolean useLatestModelIds) {
		setChemicalIdentifiers(pd);
		setModelDetails(pd,property,useLatestModelIds);
		setModelResults(pd, unitAbbreviation);
		setNeighbors(pd);
//		System.out.println(this.modelDetails.description);
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
		
		this.modelDetails.category="QSAR";
//		this.modelDetails.source=pd.getModel().getSource().getName();

		this.modelDetails.modelSourceName=pd.getModel().getSource().getName();
		this.modelDetails.modelSourceURL=pd.getModel().getSource().getUrl();
		this.modelDetails.modelSourceDescription=pd.getModel().getSource().getDescription();
		
//		this.modelDetails.description=pd.getModel().getSource().getDescription();
		
//		this.modelDetails.sourceDescription="OPERA is a free and open source/open "
//				+ "data suite of QSAR Models providing predictions and additional "
//				+ "information including applicability domain and accuracy assessment, "
//				+ "as described in the <a href=\"http://dx.doi.org/10.1186/s13321-018-0263-1\">OPERA publication</a>. "
//				+ "All models were built on curated data and standardized chemical "
//				+ "structures as described in <a href=\"http://dx.doi.org/10.1080/1062936X.2016.1253611\">Mansouri et al, 2016</a>. All OPERA properties are "
//				+ "predicted under ambient conditions of 760mm of Hg at 25 degrees Celsius.";
				
		this.modelDetails.hasQmrfPdf=hasQMRF(pd);
		this.modelDetails.hasPlots=hasPlots(property.getName());
		
		
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
		
		modelDetails.performance=setStatistics(pd);
		
	}
	
	
	public static void main(String[] args) {
		
	}
	
}
