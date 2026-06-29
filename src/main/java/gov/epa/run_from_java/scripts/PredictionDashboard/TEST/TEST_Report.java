package gov.epa.run_from_java.scripts.PredictionDashboard.TEST;

import java.util.ArrayList;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.Performance;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.Statistics;
import gov.epa.run_from_java.scripts.PredictionDashboard.TEST.model.*;

/**
* @author TMARTI02
*/
public class TEST_Report extends PredictionReport { 

	
	public TEST_Report(PredictionDashboard pd, PredictionResults pr, Property property, String unitAbbreviation,String unitAbbreviationNeighbor,boolean useLegacyModelIds) {

		
//		unitAbbreviationNeighbor=unitAbbreviationNeighbor.replace("^3","<sup>3</sup>");
		
		if(pd!=null) {
			setChemicalIdentifiers(pd,pr);
			
			setModelDetailsTEST(pd,property);
			
			setModelDetails(pd,property,useLegacyModelIds,pr.isBinaryEndpoint());
			
			modelDetails.performance=setStatistics(pr);
			
			setIndividualModels(pr,unitAbbreviationNeighbor);
			
			setModelResultsTest(pd, pr, unitAbbreviation);
			
			this.modelResults.useCombinedApplicabilityDomain=true;

			if(pr.getPredictionResultsPrimaryTable().getExpSet()!=null && !pr.getPredictionResultsPrimaryTable().getExpSet().isBlank()) {			
				this.modelResults.experimentalSet=pr.getPredictionResultsPrimaryTable().getExpSet();
				this.modelResults.experimentalCASRN=pr.getPredictionResultsPrimaryTable().getExpCAS();
			}
			
			if(pr.getPredictionResultsPrimaryTable().getSource()!=null && !pr.getPredictionResultsPrimaryTable().getSource().isBlank()) {
				modelResults.experimentalSource=pr.getPredictionResultsPrimaryTable().getSource();
				modelResults.experimentalSource=modelResults.experimentalSource.replace("<br>Source: ","").replace("<br>Sources: ", "");
				modelResults.experimentalSource=modelResults.experimentalSource.trim();
			}
			setNeighbors(pd,unitAbbreviationNeighbor);
			
			
//			for (Neighbor n:this.neighborResultsTraining.neighbors) {
//				System.out.println("Here Ack:"+n.dtxsid+"\t"+n.molImagePNGAvailable);
//			}
			
			if(unitAbbreviation.contains("Binary")) {
				this.modelDetails.propertyIsBinary=true;
			}
			
		}
		
		if(pr!=null) {
			for(int i=0;i<pr.getSimilarChemicals().size();i++) {
				SimilarChemicals sc=pr.getSimilarChemicals().get(i);

				
				//Following could be regenerated from the neighbor chemicals:
				if(sc.getExternalPredChart()!=null) {
					ExternalPredChart epc=sc.getExternalPredChart();
					if(i==0) getNeighborContinuousInfo(epc,this.neighborResultsPrediction);
					else if (i==1) getNeighborContinuousInfo(epc,this.neighborResultsTraining);
				}
								
				if(sc.getCancerStats()!=null) {
					CancerStats cs=sc.getCancerStats();
					if(i==0) getNeighborBinaryInfo(cs, neighborResultsPrediction);
					else if (i==1) getNeighborBinaryInfo(cs,this.neighborResultsTraining);
				}
			}
		}
		
		
//		System.out.println(this.modelDetails.description);

	}
	
	public void setChemicalIdentifiers(PredictionDashboard pd,gov.epa.run_from_java.scripts.PredictionDashboard.TEST.model.PredictionResults pr) {

		this.chemicalIdentifiers.dtxcid=pd.getDtxcid();

//		if(pd.getDsstoxRecord()==null) {
//			return;
//		}
//		
//		DsstoxRecord dr=pd.getDsstoxRecord();
//		
//		this.chemicalIdentifiers.dtxsid=dr.getDtxsid();
//		this.chemicalIdentifiers.casrn=dr.getCasrn();
//		this.chemicalIdentifiers.preferredName=dr.getPreferredName();
//		this.chemicalIdentifiers.smiles=dr.getSmiles();
//		this.chemicalIdentifiers.molWeight=dr.getMolWeight();
				
		//TODO store this info in predictionResults (from sdf that was ran)
		
		this.chemicalIdentifiers.dtxsid=pr.getDTXSID();
		this.chemicalIdentifiers.casrn=pr.getCAS();
		this.chemicalIdentifiers.preferredName=pr.getName();
		this.chemicalIdentifiers.smiles=pr.getSmiles();
		this.chemicalIdentifiers.molWeight=pr.getMolWeight(); 
		

	}
	
	protected Performance setStatistics(PredictionResults pr) {
		
		Performance performance=new Performance();
		
		if(pr.getHmStats()==null) {
			return null;
		}
		
		for (String statName:pr.getHmStats().keySet()) {
			
			Statistics statistics=null;
			
			if (statName.contains(DevQsarConstants.TAG_TEST)) {
				statistics=performance.external;
			} else if (statName.contains(DevQsarConstants.TAG_CV)) {
				statistics=performance.fiveFoldICV;
			} else if (statName.contains(DevQsarConstants.TAG_TRAINING)) {
				statistics=performance.train;
			}
						
			Double statValue=pr.getHmStats().get(statName);
			
			if (statName.contains(DevQsarConstants.PEARSON_RSQ)) {
				if (statName.contains(DevQsarConstants.TAG_CV)) {
					statistics.Q2=statValue;					
				} else {
					statistics.R2=statValue;					
				}
			} else if (statName.contains(DevQsarConstants.COVERAGE)) {
				statistics.COVERAGE=statValue;
			} else if (statName.contains(DevQsarConstants.MAE)) {
				statistics.MAE=statValue;
			} else if (statName.contains(DevQsarConstants.RMSE)) {
				statistics.RMSE=statValue;
			} else if (statName.contains(DevQsarConstants.BALANCED_ACCURACY)) {
				statistics.BA=statValue;
			} else if (statName.contains(DevQsarConstants.SENSITIVITY)) {
				statistics.SN=statValue;
			} else if (statName.contains(DevQsarConstants.SPECIFICITY)) {
				statistics.SP=statValue;
			} 
		}
		
		return performance;
	}

	
	public void setModelResultsTest(PredictionDashboard pd,PredictionResults pr, String unitAbbreviation) {

		modelResults.standardUnit=unitAbbreviation;
		
//		System.out.println(pr.getEndpoint()+"\t"+modelResults.standardUnit);

//		modelResults.experimentalValue=getValueInCCD_Units(pr, pd.getExperimentalValue());
//		modelResults.predictedValue=getValueInCCD_Units(pr, pd.getPredictionValue());

		modelResults.experimentalValue=pd.getExperimentalValue();
		modelResults.predictedValue=pd.getPredictionValue();

		setADEstimates(pd);
		
	}

	private Double getValueInCCD_Units(PredictionResults pr, Double predValue) {
		
		String e=pr.getEndpoint();
		
		Double valueInCCD_units=null;
		
		if(predValue!=null) {
			
			if(e.equals(TESTConstants.ChoiceFHM_LC50) || e.equals(TESTConstants.ChoiceDM_LC50) || 
					e.equals(TESTConstants.ChoiceTP_IGC50) || e.equals(TESTConstants.ChoiceRat_LD50)) {
				valueInCCD_units=Math.pow(10,-predValue);
			} else if (e.equals(TESTConstants.ChoiceBCF) || e.equals(TESTConstants.ChoiceViscosity) || 
					e.equals(TESTConstants.ChoiceVaporPressure)) {
				valueInCCD_units=Math.pow(10,predValue);
			} else {
				valueInCCD_units=predValue;
			}
			
		}
		
		return valueInCCD_units;
	}

	
	private void setIndividualModels(PredictionResults pr,String units) {
		
		if(pr.getIndividualPredictionsForConsensus()==null) return;
		
		IndividualPredictionsForConsensus ipfc=pr.getIndividualPredictionsForConsensus();
		
		modelResults.consensusPredictions=new ConsensusPredictions();
		
		//Following doesnt work since might have encoding issues:
//		modelResults.consensusPredictions.unitsPrediction=pr.getIndividualPredictionsForConsensus().getUnits();		
		modelResults.consensusPredictions.unitsPrediction=units;
		
		modelResults.consensusPredictions.predictionsIndividualMethod=new ArrayList<>();
		
		for(IndividualPredictionsForConsensus.PredictionIndividualMethod pim:ipfc.getConsensusPredictions()) {
			PredictionIndividualMethod pimNew=new PredictionIndividualMethod();
			pimNew.method=pim.getMethod();
			pimNew.predictedValue=pim.getPrediction();	
			modelResults.consensusPredictions.predictionsIndividualMethod.add(pimNew);
		}

		PredictionIndividualMethod pimNew=new PredictionIndividualMethod();
		pimNew.method="Consensus";
		pimNew.predictedValue=pr.getPredValueInModelUnits();	
		modelResults.consensusPredictions.predictionsIndividualMethod.add(pimNew);
		
		
//		System.out.println(Utilities.gson.toJson(modelDetails.predictionsIndividualMethod));
	}
	

	private void getNeighborBinaryInfo(CancerStats cs, NeighborResults nr) {

		if(cs.getPredCount()>0) 
			nr.Concordance=cs.getConcordance()+"<br>"+cs.getCorrectCount()+" of "+cs.getPredCount();
		else 
			nr.Concordance="N/A";
		
		if(cs.getPosPredCount()>0) 
			nr.Sensitivity=cs.getPosConcordance()+"<br>"+cs.getPosCorrectCount()+" of "+cs.getPosCorrectCount();
		else 
			nr.Sensitivity="N/A";

		if(cs.getNegPredCount()>0) 		
			nr.Specificity=cs.getNegConcordance()+"<br>"+cs.getNegCorrectCount()+" of "+cs.getNegPredCount();
		else 
			nr.Specificity="N/A";
	}

	private void getNeighborContinuousInfo(ExternalPredChart epc,NeighborResults nr) {
		nr.chartImgSrc=epc.getExternalPredChartImageSrc();
		nr.MAE=epc.getMAE();
		nr.MAEEntireTestSet=epc.getMAEEntireTestSet();
		
		//Note using PredictionDashboardScriptTEST.InitializeDB.createStatistics, the MAE values for consensus are 
		//recalculated to omit FDA method (doesnt use value in PredictionResults similar chemicals info)

		
		//8/21/25, the MAEs have been fixed in latest reports so dont need to take from stats 
		
//		if(nr.set.equals("Training")) {
//			nr.MAEEntireTestSet=modelDetails.performance.train.MAE;//use values from new stats and not value from pr
//		} else if(nr.set.equals("Test")) {
//			nr.MAEEntireTestSet=modelDetails.performance.external.MAE;
//		}

	}
	
//	public TEST_Report(PredictionResults pr,String preferredName) {
//
//		this.chemicalIdentifiers.casrn=pr.getCAS();
//		this.chemicalIdentifiers.dtxcid=pr.getDTXCID();
//		this.chemicalIdentifiers.dtxsid=pr.getDTXSID();
//		this.chemicalIdentifiers.preferredName=preferredName;
//		this.chemicalIdentifiers.smiles=pr.getSmiles();
//
//		System.out.println(Utilities.gson.toJson(this)+"\n\n");
//		System.out.println(Utilities.gson.toJson(pr));
//	}


	private void setModelDetailsTEST(PredictionDashboard pd,Property property) {
		
		this.modelDetails.dataAccessability=getDataAccessibility(pd);
		
		this.modelDetails.modelName=pd.getModel().getName();
		//Make model name shorter for display:
//		this.modelDetails.modelName=modelDetails.modelName.substring(0,modelDetails.modelName.indexOf(" TEST"));//simplify for display
		
		this.modelDetails.hasQmrfPdf=false;//TODO create qmrfs for legacy TEST models
		this.modelDetails.hasScatterPlot=true;//TODO create plots...
		this.modelDetails.hasHistogram=true;
		
		if(property.getName().equals(DevQsarConstants.AMES_MUTAGENICITY) || property.getName().equals(DevQsarConstants.DEVELOPMENTAL_TOXICITY)) {
			this.modelDetails.hasScatterPlot=false;
			this.modelDetails.hasHistogram=false;
		}
		
	}
	
	
	String getDataAccessibility(PredictionDashboard pd) {
		String modelName=pd.getModel().getName();
		String modelAbbrev=modelName.substring(0,modelName.indexOf(" TEST"));
		
		switch (modelAbbrev) {
			default:
				return "Public";//TODO should any be private
		}
		
		
	}
	
	
	
	
}
