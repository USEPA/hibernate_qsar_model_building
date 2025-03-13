package gov.epa.run_from_java.scripts.PredictionDashboard.TEST;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import ToxPredictor.Application.model.CancerStats;
import ToxPredictor.Application.model.ExternalPredChart;
import ToxPredictor.Application.model.IndividualPredictionsForConsensus;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.SimilarChemicals;
import ToxPredictor.Application.model.IndividualPredictionsForConsensus.PredictionIndividualMethod;
import ToxPredictor.misc.StatisticsCalculator;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;

/**
* @author TMARTI02
*/
public class TEST_Report extends PredictionReport { 

	
	public TEST_Report(PredictionDashboard pd, PredictionResults pr, Property property, String unitAbbreviation,String unitAbbreviationNeighbor,boolean useLegacyModelIds) {

		if(pd!=null) {
			setChemicalIdentifiers(pd);
			
			setModelDetailsTEST(pd,property,useLegacyModelIds);
			
			setModelDetails(pd,property,useLegacyModelIds,pr.isBinaryEndpoint());
			
			setIndividualModels(pr);
			
			setModelResults(pd, unitAbbreviation);
			
			this.modelResults.useCombinedApplicabilityDomain=true;

			if(pr.getPredictionResultsPrimaryTable().getExpSet()!=null && !pr.getPredictionResultsPrimaryTable().getExpSet().isBlank())			
				this.modelResults.experimentalSet=pr.getPredictionResultsPrimaryTable().getExpSet();
			
			if(pr.getPredictionResultsPrimaryTable().getSource()!=null && !pr.getPredictionResultsPrimaryTable().getSource().isBlank()) {
				modelResults.experimentalSource=pr.getPredictionResultsPrimaryTable().getSource();
				modelResults.experimentalSource=modelResults.experimentalSource.replace("<br>Source: ","").replace("<br>Sources: ", "");
			}
			setNeighbors(pd,unitAbbreviationNeighbor);
			
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
	
	private void setIndividualModels(PredictionResults pr) {
		
		if(pr.getIndividualPredictionsForConsensus()==null) return;
		
		IndividualPredictionsForConsensus ipfc=pr.getIndividualPredictionsForConsensus();
		
		modelDetails.consensusPredictions=new ConsensusPredictions();
		modelDetails.consensusPredictions.unitsPrediction=pr.getIndividualPredictionsForConsensus().getUnits();
		
		modelDetails.consensusPredictions.predictionsIndividualMethod=new ArrayList<>();
		
		for(ToxPredictor.Application.model.IndividualPredictionsForConsensus.PredictionIndividualMethod pim:ipfc.getConsensusPredictions()) {
			PredictionIndividualMethod pimNew=new PredictionIndividualMethod();
			pimNew.method=pim.getMethod();
			pimNew.predictedValue=pim.getPrediction();
			modelDetails.consensusPredictions.predictionsIndividualMethod.add(pimNew);
		}
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
		
		if(nr.set.equals("Training")) {
			nr.MAEEntireTestSet=modelDetails.performance.train.MAE;//use values from new stats and not value from pr
		} else if(nr.set.equals("Test")) {
			nr.MAEEntireTestSet=modelDetails.performance.external.MAE;
		}

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


	private void setModelDetailsTEST(PredictionDashboard pd,Property property, boolean useLegacyModelIds) {
		
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
