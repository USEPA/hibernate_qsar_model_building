package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.run_from_java.scripts.QsarModelsScript;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.CreatorScript;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.ADEstimate;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;

/**
 * @author TMARTI02
 */
public class OPERA_Report extends PredictionReport {


	/**
	 * Creates report from PredictionDashboard object
	 * @param pd
	 * @param property
	 * @param unitAbbreviation
	 * @param useLegacyModelIds
	 */
	public OPERA_Report(PredictionDashboard pd, Property property, String unitAbbreviation,boolean useLegacyModelIds) {

		if(pd!=null) {
			setChemicalIdentifiers(pd);
			
			boolean isPropertyBinary=false;
			if(property.getName_ccd().contains("Receptor") || property.getName_ccd().contains("Binary") ) {
				isPropertyBinary=true;
			}
						
			setModelDetailsOpera(pd, property);			
			setModelDetails(pd,property,useLegacyModelIds,isPropertyBinary);
			setModelResults(pd, unitAbbreviation);
			
			this.modelResults.useCombinedApplicabilityDomain=true;
			
			setNeighbors(pd, unitAbbreviation);
			
			neighborResultsPrediction=null;//dont have them
			this.neighborResultsTraining.predictedValueToolTip="Five fold cross validation prediction for the neighbor";
			this.neighborResultsTraining.title="Nearest Neighbors from Model Knowledge Base";
			this.neighborResultsTraining.missingExperimentalValueNote="Some chemicals in the evaluation set do not have readily available experimental values";
			
		}
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





	public static Long getOldModelID(String propertyName) {
		switch (propertyName) {
		case DevQsarConstants.BOILING_POINT:
			return 27L;
		case DevQsarConstants.HENRYS_LAW_CONSTANT:
			return 19L;
		case DevQsarConstants.LOG_KOA:
			return 26L;
		case DevQsarConstants.LOG_KOW:
			return 22L;
		case DevQsarConstants.MELTING_POINT:
			return 18L;
		case DevQsarConstants.VAPOR_PRESSURE:
			return 30L;
		case DevQsarConstants.WATER_SOLUBILITY:
			return 24L;
		case DevQsarConstants.OH:
			return 29L;
		case DevQsarConstants.BIODEG_HL_HC:
			return 17L;
		case DevQsarConstants.RBIODEG:
			return 20L;
		case DevQsarConstants.KmHL:
			return 28L;
		case DevQsarConstants.KOC:
			return 25L;
		case DevQsarConstants.BCF:
			return 23L;

		default:
			return null;

		}

	}


	/**
	 * Whether or not to expose to dashboard- should this be stored in the database somewhere?
	 * 
	 * @param pd
	 * @return
	 */
	String getDataAccessibility(PredictionDashboard pd) {
		
		List<String>privSources=Arrays.asList("CACO2","CLINT","FUB");
		
		for(String privSource:privSources) {
			if(pd.getModel().getName().contains(privSource)) {
				return "Private";
			}
		}
	
		return "Public";
	
//	case "CLINT":
//		case "FUB":	
//		case "pKa_a":
//		case "pKa_b":
//			return "Private";
//
//		default:
//			return "Public";
//		}


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
	boolean hasQMRF(PredictionDashboard pd) {

		//		System.out.println(pd.getModel().getName());

		String n=pd.getModel().getDatasetName();
		n=n.substring(0,n.indexOf(" OPERA"));

		switch (n) {
		case DevQsarConstants.ORAL_RAT_LD50://a QMRF exists but not in the qmrf api yet
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
			return true;
		default:
			return false;
		}
	}

	public static boolean hasPlots(String propertyName) {

		//		System.out.println(pd.getModel().getName());


		switch (propertyName) {
		//		case DevQsarConstants.ORAL_RAT_LD50://For right now not available
		case DevQsarConstants.MELTING_POINT:
		case DevQsarConstants.BOILING_POINT:
		case DevQsarConstants.WATER_SOLUBILITY:
		case DevQsarConstants.VAPOR_PRESSURE:
		case DevQsarConstants.FUB:
		case DevQsarConstants.CLINT:
		case DevQsarConstants.RT:
		case DevQsarConstants.LOG_KOW:
		case DevQsarConstants.LOG_KOA:
		case DevQsarConstants.HENRYS_LAW_CONSTANT:
		case DevQsarConstants.OH:
		case DevQsarConstants.BIODEG_HL_HC:
		case DevQsarConstants.BCF:
		case DevQsarConstants.ORAL_RAT_LD50:
		case DevQsarConstants.KOC:
		case DevQsarConstants.KmHL:
			return true;
		default:
			return false;
		}
	}




	private void setModelDetailsOpera(PredictionDashboard pd,Property property) {

		this.modelDetails.modelIdLegacy=getOldModelID(property.getName());
		this.modelDetails.dataAccessability=getDataAccessibility(pd);
		this.modelDetails.hasQmrfPdf=hasQMRF(pd);
		
		this.modelDetails.hasHistogram=hasPlots(property.getName());
		
		this.modelDetails.hasScatterPlot=this.modelDetails.hasHistogram;
		if(property.getName().equals(DevQsarConstants.FUB) ||
				property.getName().equals(DevQsarConstants.RT) ||
				property.getName().equals(DevQsarConstants.CLINT)) {
			this.modelDetails.hasScatterPlot=false;	
		}

	}


	public static byte[] downloadUrl(URL toDownload) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	
		try {
			byte[] chunk = new byte[4096];
			int bytesRead;
			InputStream stream = toDownload.openStream();
	
			while ((bytesRead = stream.read(chunk)) > 0) {
				outputStream.write(chunk, 0, bytesRead);
			}
	
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	
		return outputStream.toByteArray();
	}



	public static void uploadPlotImages() {
		
		QsarModelsScript qms=new QsarModelsScript("tmarti02");

		TreeMap<String, Model>mapModels=CreatorScript.getModelsMap();

		String urlHistogramAPI_Legacy="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/histo-graph/by-dtxsid/DTXSID3039242/";
		String urlScatterPlotAPI_Legacy="https://comptox.epa.gov/dashboard-api/ccdapp2/opera-image/scatter-graph/by-dtxsid/DTXSID3039242/";

		PredictionDashboardScriptOPERA p=new PredictionDashboardScriptOPERA();
		
		for (String propertyName:DevQsarConstants.getOPERA_PropertyNames()) {

			Long modelID_old=OPERA_Report.getOldModelID(propertyName);

			if(!OPERA_Report.hasPlots(propertyName)) continue;

			if (modelID_old==-1) continue;

			System.out.println(propertyName);


			if (modelID_old!=-1 )  {
				String urlHistogram=urlHistogramAPI_Legacy+modelID_old;
				String urlScatterPlot=urlScatterPlotAPI_Legacy+modelID_old;

				try {

					byte[] scatterFile=downloadUrl(new URL(urlScatterPlot));
					byte[] histogramFile=downloadUrl(new URL(urlHistogram));

					Model model=mapModels.get(p.initializeDB.getModelName(propertyName));

					qms.uploadModelFile(model.getId(), 3L, scatterFile);
					qms.uploadModelFile(model.getId(), 4L, histogramFile);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}



	public static void main(String[] args) {

		OPERA_Report.uploadPlotImages();

	}

}
