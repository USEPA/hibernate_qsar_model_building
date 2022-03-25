package gov.epa.endpoints.reports.WebTEST;
import java.awt.Color;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import gov.epa.endpoints.reports.WebTEST.GenerateWebTestReport.Dataset;
import gov.epa.endpoints.reports.WebTEST.fraChart.JLabelChart;
import gov.epa.endpoints.reports.WebTEST.ApplicabilityDomain.Analog;
import gov.epa.endpoints.reports.WebTEST.ApplicabilityDomain.AnalogFinder;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.*;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.QsarPredictedValue;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelMetadata;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportModelStatistic;
import gov.epa.util.wekalite.Instance;
import gov.epa.util.wekalite.Instances;

import java.text.DecimalFormat;
//
public class PredictToxicityJSONCreator {
//
//	private static final Logger logger = LogManager.getLogger(PredictToxicityJSONCreator.class);
//
//	//	public static boolean isBinaryEndpoint = false;
//	//	public static boolean isLogMolarEndpoint = true;
//
//	private Lookup lookup = new Lookup();
//
//
	
	static Hashtable<String,String> htVarDef;
	public static final int maxSimilarCount = 10;// max similar chemicals displayed in results

	public static final double SCmin = 0.5;

	public static int imgSize = 100;// img size displayed in web pages
	public static String messageMissingFragments = "A prediction could not be made " + "because the test chemical contains atoms which could not be assigned to "
			+ "<a href=\"../StructureData/AssignedFragments.html\">fragments</a>.";
	public static String messageNoStatisticallyValidModels = "<font color=darkred>(none of the closest clusters have statistically valid models)</font>\r\n";

//	Lookup lookup = new Lookup();

//	public static String webPathImage = "https://ccte-api-ccd.epa.gov/ccdapp1/chemical-files/image/by-dtxcid/";
//	public static String webPath2 = "https://comptox.epa.gov/dashboard/dsstoxdb/results?search=";
	public static String webPathImage = "https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/";
		
	public static String webpathDetails="https://comptox.epa.gov/dashboard/chemical/details/";
	
	static DecimalFormat d2 = new DecimalFormat("0.00");
	static DecimalFormat d3 = new DecimalFormat("0.000");
	static DecimalFormat d2exp = new DecimalFormat("0.00E00");
//
//	public static boolean forGUI=false;
//	//	private PredictionResults predictionResults = new PredictionResults();
//
//	Hashtable<String,String>htVarDefs=null;
//
//	public PredictToxicityJSONCreator() {
//		htVarDefs=LoadDefinitions();
//	}
//
//
//	public PredictionResults writeConsensusResultsJSON(double predToxVal, double predToxUnc, String method, String CAS, String endpoint, String abbrev, boolean isBinaryEndpoint,
//			boolean isLogMolarEndpoint, Lookup.ExpRecord er, double MW, String message, Hashtable<Double, Instance> htTestMatch, Hashtable<Double, Instance> htTrainMatch, ArrayList methods,
//			ArrayList predictions, ArrayList uncertainties, boolean createDetailedConsensusReport, String gsid, 
//			ReportOptions options) {
//		try {;
//
//			PredictionResults pr=new PredictionResults();
//
//			IndividualPredictionsForConsensus individualPredictionsForConsensus = pr.getIndividualPredictionsForConsensus();
//			pr.setCreateDetailedReport(createDetailedConsensusReport);
//			PredictionResultsPrimaryTable predictionResultsPrimaryTable = pr.getPredictionResultsPrimaryTable();
//
//			String outputfilename = "PredictionResults";
//
//			outputfilename += method.replaceAll(" ", "");
//			outputfilename += ".json";
//
//			String jsonFilePath = Paths.get(options.reportBase, outputfilename).toFile().getAbsolutePath();
//			FileWriter fw = new FileWriter(jsonFilePath);
//
//
//			pr.setCAS(CAS);
//
//			pr.setEndpoint(endpoint);
//			pr.setBinaryEndpoint(isBinaryEndpoint);
//			pr.setLogMolarEndpoint(isLogMolarEndpoint);
//
//			pr.setMethod(method);
//
//			pr.setSCmin(PredictToxicityWebPageCreator.SCmin);
//
//			int predCount = 0;
//			for (int i = 0; i < predictions.size(); i++) {
//				if ((Double) predictions.get(i) != -9999)
//					predCount++;
//			}
//
//			if (predCount == 0) {
//				message = "No prediction could be made";
//			} else if (predCount < TaskCalculations.minPredCount) {
//				message = "The consensus prediction for this chemical is considered unreliable since only one prediction can only be made";
//			}
//
//
//			this.writeIndividualPredictionsForConsensus(pr,methods, predictions, uncertainties, createDetailedConsensusReport);
//
//			predictionResultsPrimaryTable.setGsid(gsid);
//			pr.setImgSize(PredictToxicityWebPageCreator.imgSize);
//			pr.setWebPath(PredictToxicityWebPageCreator.webPath);
//			pr.setWebPath2(PredictToxicityWebPageCreator.webPath2);
//
//
//			if (gsid == null) {
//				//Following code doesnt seem to work:		
//				//				individualPredictionsForConsensus.setImageUrl(ReportUtils.convertImageToBase64(ReportUtils.getImageSrc(options, "../StructureData/structure.png")));
//				//				logger.debug("valeryURL="+ReportUtils.getImageSrc(options, "../StructureData/structure.png"));
//
//				//Using brute force to make sure path is right:
//				File outputFolder=new File(options.reportBase);
//				File imageFile=new File(outputFolder.getParentFile().getAbsolutePath()+File.separator+"StructureData/structure.png");
//				String strURL = imageFile.toURI().toURL().toString();
//				pr.setImageURL(ReportUtils.convertImageToBase64(strURL));
//
//			} else {
//				//use dashboard image if available:
//				pr.setImageURL(PredictToxicityWebPageCreator.webPath + gsid);
//				//				individualPredictionsForConsensus.setImageUrl(ReportUtils.convertImageToBase64(PredictToxicityWebPageCreator.webPath + gsid));
//			}
//
//			//			if (createDetailedConsensusReport) {
//			//				fw.write("<p><a href=\"../StructureData/descriptordata.html\">Descriptor values for " + "test chemical</a></p>\n");
//			//			}
//
//			this.writeSimilarChemicals(pr,"test", htTestMatch, abbrev, er.expToxValue, predToxVal,  gsid, options);
//
//			this.writeSimilarChemicals(pr,"training", htTrainMatch, abbrev, er.expToxValue, predToxVal, gsid, options);
//
//			Gson gson = new GsonBuilder().setPrettyPrinting().create();
//			gson.toJson(pr, fw);
//
//			fw.close();
//
//			return pr;
//
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//
//		return null;
//	}
//
	

//	static TESTPredictedValue getConsensusTPV(List<TESTPredictedValue> tpvs) {
////		System.out.println(tpv.predValMolarLog);
//		for (TESTPredictedValue tpv:tpvs) {
//			if (tpv.method.toLowerCase().contains("consensus")) 
//				return tpv;
//		}
//		return null;
//	}
//
//	
//	
//	
//	static Object getConsensusToxicityValue(TESTPredictedValue tpv) {
//		if (tpv.predValMolarLog!=null) {
//			return tpv.predValMolarLog;
//		} else if (tpv.predValMass!=null) {
//			return tpv.predValMass;
//		} else if (tpv.predActive!=null) {
//			//TODO is binary string such as "Mutagenicity Negative" stored in message?
//			return tpv.predActive;
//		} else {
//			return null;	
//		}
//	}
	
	
//	private List <TESTPredictedValue> getTESTPredictedValueArray(PredictionReportData.PredictionReportDataPoint testDataPoint,
//			boolean isBinaryEndpoint, boolean isLogMolarEndpoint) {
//		
//		List <TESTPredictedValue> preds=new ArrayList<>();
//
//		for (int i=0;i<testDataPoint.qsarPredictedValues.size();i++) {
//			TESTPredictedValue tpv=new TESTPredictedValue();
//			preds.add(tpv);
//			
//			QSARPredictedValue qpv=testDataPoint.qsarPredictedValues.get(i);
//			double predVal=qpv.qsarPredictedValue;
//			
//			tpv.method=qpv.qsarMethodName;
//			
//			System.out.println("pred for individual method: "+tpv.method+"\t"+predVal);
//			
//			if (isBinaryEndpoint) {
//				if (predVal>=0.5) tpv.predActive=true;
//				else tpv.predActive=false;
////				tpv.predActive= //TODO
//			} else {
//				if (isLogMolarEndpoint) {
//					tpv.predValMolarLog=predVal;
//					
//					//tpv.predValMass=//TODO convert units
//				} else {
//					tpv.predValMass=predVal;
////					tpv.predValMolarLog=//TODO convert units
//				}
//			}
//		}
//		return preds;
//	}
	
	public PredictionResults generatePredictionResultsConsensus(					
			ExpRecord er,PredictionReportDataPoint  testDataPoint, Dataset dataset) {
		
		PredictionResults pr=new PredictionResults();

		try {
			String units=dataset.metadata.datasetUnit;
			boolean isBinaryEndpoint=dataset.metadata.datasetUnit.toLowerCase().contains("binary");
			
			//Do we want to set isLogMolar based on data set name or is this sufficient?
			boolean isLogMolarEndpoint=dataset.metadata.datasetUnit.toLowerCase().contains("log");
			
			String method="Consensus";

			pr.setCAS(testDataPoint.originalCompounds.get(0).casrn);
			pr.setDtxcid(testDataPoint.originalCompounds.get(0).dtxcid);
			pr.setEndpoint(dataset.metadata.datasetProperty);
			pr.setEndpointDescription(dataset.metadata.datasetPropertyDescription);
			
			pr.setBinaryEndpoint(isBinaryEndpoint);
			pr.setLogMolarEndpoint(isLogMolarEndpoint);
			pr.setMethod(method);
			
			pr.setSCmin(SCmin);
			pr.setImgSize(imgSize);
			pr.setWebPathImage(webPathImage);
			pr.setWebPathDetails(webpathDetails);
			
			String dtxcid=testDataPoint.originalCompounds.get(0).dtxcid;

			pr.setImageURL(webPathImage + dtxcid);

			long t1=System.currentTimeMillis();
			
			Double predToxVal=null;
			Double predToxUnc=null;
						
//			String smiles=testDataPoint.originalCompounds.get(0).smiles;//should only be one original compound for chemical being predicted
//			//Calculate molecular weight using original smiles (not qsar ready):
//			Indigo indigo = new Indigo();
//			IndigoObject mol1 = indigo.loadMolecule(smiles);
//			double MW=mol1.molecularWeight();
//			System.out.println("MW indigo from smiles="+MW);
			
			double MW=0;
			
			if (testDataPoint.originalCompounds.get(0).molWeight!=null)
				 MW=testDataPoint.originalCompounds.get(0).molWeight;
			else 
				System.out.println("MW missing for test compound");
			
//			System.out.println(MW);
			
			if (testDataPoint.errorMessage==null) {
				predToxVal=calculateConsensusToxicityValue(testDataPoint.qsarPredictedValues);
			}
				    	
	    	if (er.expToxValue!=null) testDataPoint.experimentalPropertyValue=er.expToxValue;
			Instance evalInstance2d = createInstance(testDataPoint,dataset.instancesTraining.getDescriptorNames(),"\t");					
			
//			System.out.println(evalInstance2d.numValues());
//			System.out.println(dataset.instancesTraining.numValues());			
			
			List<Analog>analogsTraining=AnalogFinder.findAnalogsWekalite(evalInstance2d, dataset.instancesTraining, 10, 0.5, true, dataset.simMeasure);
			List<Analog>analogsPrediction=AnalogFinder.findAnalogsWekalite(evalInstance2d, dataset.instancesPrediction, 10, 0.5, true, dataset.simMeasure);
			addPredictionsToAnalogs(analogsTraining, dataset.ht_datapoints);
			addPredictionsToAnalogs(analogsPrediction, dataset.ht_datapoints);
			writeSimilarChemicals(pr,"test", analogsPrediction, er.expToxValue, predToxVal,dtxcid,units,dataset.maePrediction);
			writeSimilarChemicals(pr,"training", analogsTraining, er.expToxValue, predToxVal,dtxcid,units,dataset.maeTraining);

			List<Analog>analogsAD=AnalogFinder.findAnalogsWekalite(evalInstance2d, dataset.instancesTraining, 3, 0, true, dataset.simMeasure);
			addPredictionsToAnalogs(analogsAD, dataset.ht_datapoints);
			double avgSim=0;
			for (Analog analog:analogsAD) avgSim+=analog.sim;
			avgSim/=(double)analogsAD.size();
			
//			System.out.println(avgSim+"\t"+dataset.scFracTraining);
			
			ApplicabilityDomainNN adn=new ApplicabilityDomainNN();
			adn.setScFracTraining(dataset.scFracTraining);
			adn.setAvgSCNN(avgSim);
			adn.setAnalogsAD(analogsAD);
			adn.setFracTrainingForAD(dataset.fracTrainingForAD);
			pr.setApplicabilityDomainNN(adn);

			long t2=System.currentTimeMillis();
			
			if (isBinaryEndpoint) {
//				System.out.println("isBinary in generatePredictionResultsConsensus");
				WriteBinaryPredictionTable(pr, dtxcid,er,predToxVal,testDataPoint.errorMessage);
			} else {
				writeMainTable(dataset.metadata, pr, dtxcid, predToxVal, predToxUnc, MW, er, testDataPoint.errorMessage);
			}
			this.writeIndividualPredictionsForConsensus(pr,testDataPoint.qsarPredictedValues,units,dataset.predictionReportModelMetadata);			
			return pr;

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return pr;
	}

	
	public static Instance createInstance(PredictionReportDataPoint dataPoint, String descriptorHeader, String del) {
		String strInstances="ID"+del+"Property"+del+descriptorHeader+"\r\n";    
//		System.out.println(strInstances);
		
		double propVal=-9999.0;
		
		if (dataPoint.experimentalPropertyValue!=null) propVal=dataPoint.experimentalPropertyValue;
		strInstances+=dataPoint.canonQsarSmiles+"\t"+dataPoint.experimentalPropertyValue+"\t"+dataPoint.descriptorValues+"\r\n";
		
//		System.out.println(strInstances);
		
		Instances instances = Instances.instancesFromString(strInstances);		
		return instances.firstInstance();
	}
	
	private static void addPredictionsToAnalogs(List<Analog> analogs,
			Hashtable<String, PredictionReportDataPoint> htPredsForDataSet) {
		for (Analog analog:analogs) {

			if (htPredsForDataSet.get(analog.ID)==null) {
				//			System.out.println(analog.ID);
				continue;
			}
			PredictionReportDataPoint dataPoint=htPredsForDataSet.get(analog.ID);		
			//		System.out.println(dataPoint.canonQsarSmiles+"\t"+dataPoint.qsarPredictedValues.size());
			analog.dtxcid=dataPoint.originalCompounds.get(0).dtxcid;
			analog.casrn=dataPoint.originalCompounds.get(0).casrn;

			if (dataPoint.qsarPredictedValues==null) continue;
			analog.pred=PredictToxicityJSONCreator.calculateConsensusToxicityValue(dataPoint.qsarPredictedValues);

		}
	}

	
	
//	private TESTPredictedValue createConsensusTPV(PredictionReportData data, PredictionReportData.PredictionReportDataPoint testDataPoint,
//			String endpoint, double MW,boolean isBinary) {
//		TESTPredictedValue tpv=new TESTPredictedValue();
//		
//		tpv.method=ConstantsQSAR.methodNameConsensus+" "+ConstantsQSAR.versionConsensus;
//		tpv.endpoint=endpoint;
//		tpv.molarLogUnits=data.predictionReportMetadata.datasetUnit;
//		
//		Double consensusToxicity=calculateConsensusToxicityValue(testDataPoint.qsarPredictedValues);
//		
//		if (isBinary) {
//			if (consensusToxicity>=0.5) tpv.predActive=true;
//			else tpv.predActive=false;		
//		} else {		
//			if (TESTConstants.isLogMolar(endpoint)) {
//				tpv.predValMolarLog=consensusToxicity;
//				tpv.predValMass=PredictToxicityJSONCreator.getToxValMass(endpoint, consensusToxicity, MW);
//			} else {
//				tpv.predValMass=consensusToxicity;
//			}
//		}	
//		return tpv;
//	}

	public static Double calculateConsensusToxicityValue(List<QsarPredictedValue>qsarPredictedValues) {
		Double pred = Double.valueOf(0.0);
		int predcount = 0;
		double minPredCount=2;

		
		int consensusCount=0;
		
		for (int i = 0; i < qsarPredictedValues.size(); i++) {
			//If already have a consensus value, use it:
			if (qsarPredictedValues.get(i).qsarMethodName.toLowerCase().contains("consensus")) {
				if (qsarPredictedValues.get(i).qsarPredictedValue !=null) {
					consensusCount++;
				}
			}
		}

		if (consensusCount==1) {//if prediction report only has a single consensus prediction use that
			for (int i = 0; i < qsarPredictedValues.size(); i++) {
			//If already have a consensus value, use it:
				if (qsarPredictedValues.get(i).qsarMethodName.toLowerCase().contains("consensus")) {
					if (qsarPredictedValues.get(i).qsarPredictedValue !=null) {
//						System.out.println("using "+qsarPredictedValues.get(i).qsarMethodName);
						return qsarPredictedValues.get(i).qsarPredictedValue;
					}
				}
			}
		}
		
		//If we didnt store consensus method in database, generate on the fly:
		
		for (int i = 0; i < qsarPredictedValues.size(); i++) {
			if (qsarPredictedValues.get(i).qsarPredictedValue !=null) {
				predcount++;
				pred += qsarPredictedValues.get(i).qsarPredictedValue;
			}
		}

		if (predcount < minPredCount)
			return null;

		pred /= (double) predcount;
		// System.out.println(pred);
		return pred;
	}
//
//
//	public PredictionResults generatePredictionResultsNearestNeighbor(DataForPredictionRun d,TESTPredictedValue tpv,double predToxVal,ReportOptions options,boolean createReports) {
//
//		try {
//			PredictionResults pr=new PredictionResults();
//			pr.setReportBase(options.reportBase);
//			setCommonValues(TESTConstants.ChoiceNearestNeighborMethod, d, tpv, pr, predToxVal,-9999,createReports);
//			return pr;
//
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//
//		return null;
//	}
//
//
//	//	public PredictionResults generatePredictionResultsError(DataForPredictionRun d,TESTPredictedValue tpv,String method) {		
//	//		try {
//	//			PredictionResults pr=new PredictionResults();
//	//			setCommonValues(method, d, tpv, pr, -9999,-9999);
//	//			return pr;
//	//
//	//		} catch (Exception ex) {
//	//			logger.catching(ex);
//	//		}
//	//		return null;
//	//	}
//
//
//	//	private void WriteClusterTableNN(String CAS, String endpoint, double ExpToxVal, Instances cc, Vector<Double> SimCoeffCluster,  ReportOptions options,PredictionResults pr)
//	//			throws IOException {
//	//
//	//		// FileWriter fw=new FileWriter (filepath);
//	//		if (cc == null)
//	//			return;
//	//
//	//		java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
//	//
//	//		SimilarChemicals similarChemicals=new SimilarChemicals();
//	//		
//	//		if (TESTConstants.isLogMolar(endpoint))
//	//			similarChemicals.setUnits(TESTConstants.getMolarLogUnits(endpoint));
//	//		else
//	//			similarChemicals.setUnits(TESTConstants.getMassUnits(endpoint));
//	//
//	//		similarChemicals.setExpVal(df.format(ExpToxVal));
//	//		similarChemicals.setImageUrl(ReportUtils.getImageSrc(options, "../StructureData/structure.png"));
//	//
//	//		for (int i = 0; i < cc.numInstances(); i++) {
//	//			SimilarChemical similarChemical = new SimilarChemical();
//	//			String CASi=cc.instance(i).getName();
//	//			similarChemical.setCAS(CASi);
//	//			similarChemical.setImageUrl(ReportUtils.getImageSrc(options, "../../images", CASi + ".png"));
//	//			similarChemical.setExpVal(df.format(cc.instance(i).classValue()));
//	//			similarChemical.setSimilarityCoefficient(df.format(SimCoeffCluster.get(i))+"");
//	//			
//	//			String strColor = PredictToxicityWebPageCreator.getColorString(SimCoeffCluster.get(i));
//	//			similarChemical.setBackgroundColor(strColor);
//	//			
//	//			similarChemicals.getSimilarChemicalsList().add(similarChemical);
//	//		}
//	//		pr.setSimilarChemicalsForNN(similarChemicals);
//	//		
//	//	}
//
//
//	public static Vector<EndpointSource> getSourceVector(String endpoint) {
//		//TODO needed?
//
//		Vector<EndpointSource> endpointSources = new Vector<>();
//
//		if (endpoint.equals(TESTConstants.ChoiceFHM_LC50) || endpoint.equals(TESTConstants.ChoiceDM_LC50) || endpoint.equals(TESTConstants.ChoiceGA_EC50)) {
//
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("ECOTOX");
//			endpointSource.setSourceURL("http://cfpub.epa.gov/ecotox");
//			endpointSources.add(endpointSource);
//		} else if (endpoint.equals(TESTConstants.ChoiceTP_IGC50)) {
//
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("TETRATOX");
//			endpointSource.setSourceURL("http://www.vet.utk.edu/TETRATOX/index.php");
//			endpointSources.add(endpointSource);
//		} else if (endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
//
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("ChemidPlus");
//			endpointSource.setSourceURL("http://chem.sis.nlm.nih.gov/chemidplus");
//			endpointSources.add(endpointSource);
//		} else if (endpoint.equals(TESTConstants.ChoiceBCF)) {
//			// String ref="<br>Source: <br><ul{list-style:none}>";
//
//			// ref+="<li>SAR QSAR Environ Res, 16, p. 531-554
//			// (2005)</li>";//http://www.tandfonline.com/doi/abs/10.1080/10659360500474623
//			// ref+="<li>Environ. Rev., 14:257-297
//			// (2006)</li>";//http://www.nrcresearchpress.com/doi/abs/10.1139/a06-005
//			// ref+="<li>Chemosphere, 73:1701-1707
//			// (2008)</li>";//http://www.sciencedirect.com/science/article/pii/S0045653508011922
//			// ref+="</ul>";
//
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Dimetrov 2005");
//			endpointSource.setSourceURL("http://www.tandfonline.com/doi/abs/10.1080/10659360500474623");
//			endpointSources.add(endpointSource);
//
//			endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Arnot 2006");
//			endpointSource.setSourceURL("http://www.nrcresearchpress.com/doi/abs/10.1139/a06-005");
//			endpointSources.add(endpointSource);
//
//			endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Zhao 2008");
//			endpointSource.setSourceURL("http://www.sciencedirect.com/science/article/pii/S0045653508011922");
//			endpointSources.add(endpointSource);
//
//		} else if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("CAESAR");
//			endpointSource.setSourceURL("http://www.caesar-project.eu/index.php?page=results&section=endpoint&ne=5");
//			endpointSources.add(endpointSource);
//		} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
//
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Toxicity Benchmark");
//			endpointSource.setSourceURL("http://doc.ml.tu-berlin.de/toxbenchmark");
//			endpointSources.add(endpointSource);
//
//		} else if (endpoint.equals(TESTConstants.ChoiceDensity) || endpoint.equals(TESTConstants.ChoiceFlashPoint)) {
//
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Lookchem.com");
//			endpointSource.setSourceURL("http://www.lookchem.com");
//			endpointSources.add(endpointSource);
//
//		} else if (endpoint.equals(TESTConstants.ChoiceViscosity)) {
//
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Viswanath 1989");
//			endpointSource.setSourceURL("http://www.worldcat.org/title/data-book-on-the-viscosity-of-liquids/oclc/18833753");
//			endpointSources.add(endpointSource);
//
//			endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Riddick 1996");
//			endpointSource.setSourceURL("http://www.wiley.com/WileyCDA/WileyTitle/productCd-0471084670.html");
//			endpointSources.add(endpointSource);
//
//		} else if (endpoint.equals(TESTConstants.ChoiceThermalConductivity)) {
//
//			// String ref="<br>Sources: <br><ul{list-style:none}>";
//			// ref+="<li>Jamieson, D.T.; Irving, J.B; Tudhope, J.S. <br>" +
//			// "\"Liquid Thermal Conductivity. A Data Survey to 1973,\"<br>" +
//			// "H. M. Stationary Office, Edinburgh, 1975</li>";
//			// ref+="<li>Vargaftik, N. B., Filippov, L. P., Tarzimanov, A. A.,
//			// <br>" +
//			// "and Totskii, E. E. 1994. Handbook of thermal conductivity<br>" +
//			// " of liquids and gases. Boca Raton: CRC Press.</li>";
//			// ref+="</ul>";
//
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Jamieson 1975");
//			endpointSource.setSourceURL("http://www.worldcat.org/title/liquid-thermal-conductivity-a-data-survey-to-1973/oclc/3090244");
//			endpointSources.add(endpointSource);
//
//			endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Vargaftik 1994");
//			endpointSource.setSourceURL("http://www.worldcat.org/title/handbook-of-thermal-conductivity-of-liquids-and-gases/oclc/28847166&referer=brief_results");
//			endpointSources.add(endpointSource);
//		} else if (endpoint.equals(TESTConstants.ChoiceSurfaceTension)) {
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Jaspar 1972");
//			endpointSource.setSourceURL("http://jpcrd.aip.org/resource/1/jpcrbu/v1/i4/p841_s1?isAuthorized=no");
//			endpointSources.add(endpointSource);
//
//		} else if (endpoint.equals(TESTConstants.ChoiceWaterSolubility) || endpoint.equals(TESTConstants.ChoiceBoilingPoint) || endpoint.equals(TESTConstants.ChoiceVaporPressure)
//				|| endpoint.equals(TESTConstants.ChoiceMeltingPoint)) {
//
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("EPI Suite v 4.00");
//			endpointSource.setSourceURL("http://www.epa.gov/opptintr/exposure/pubs/episuite.htm");
//			endpointSources.add(endpointSource);
//
//		} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor) || endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
//			EndpointSource endpointSource = new EndpointSource();
//			endpointSource.setSourceName("Cronin and Livingstone, 2004");
//			endpointSource.setSourceURL("http://www.worldcat.org/title/predicting-chemical-toxicity-and-fate/oclc/54073110&referer=brief_results");
//			endpointSources.add(endpointSource);
//		} else {
//		}
//
//		return endpointSources;
//	}
//
	public static void writeMainTable(PredictionReportMetadata metadata,PredictionResults pr,String imageID,Double predToxVal,Double predToxUnc, double MW, ExpRecord er, String errorMessage) throws Exception {

		String method=pr.getMethod();
		String endpoint=pr.getEndpoint();

		//TODO: our new methods such as random forest dont have uncertainty values and thus no prediction interval
//		boolean writePredictionInterval = true;
//		if (method.equals(TESTConstants.ChoiceConsensus) || method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
//			writePredictionInterval = false;
//		}
		
		boolean writePredictionInterval = false;
		PredictionResultsPrimaryTable predictionResultsPrimaryTable = pr.getPredictionResultsPrimaryTable();
		predictionResultsPrimaryTable.setImageID(imageID);

		predictionResultsPrimaryTable.setWritePredictionInterval(writePredictionInterval);

		String endpoint2 = endpoint.replace("50", "<sub>50</sub>");
		predictionResultsPrimaryTable.setEndpointSubscripted(endpoint2);

		// ************************************************
		// Header row

			
		if (er.expToxValue != null) {
			predictionResultsPrimaryTable.setExpCAS(er.expCAS);
//			System.out.println("here1, expCAS="+er.expCAS);
			
//			predictionResultsPrimaryTable.setSource(getSourceTag(endpoint));//TODO
		}

		predictionResultsPrimaryTable.setExpSet(er.expSet);

		if (er.expSet.equals("Training") || er.expSet.equals("Test")) {
			String predictedValue = "a";

			if (errorMessage!=null) {//TODO presumably this is AD violation- need to implement
				predictedValue += ",b";
			}
			predictionResultsPrimaryTable.setPredictedValueSuperscript(predictedValue);
		} else {
			if (errorMessage!=null) {
//				predictionResultsPrimaryTable.setPredictedValueSuperscript("b");//TODO presumably this is AD violation- need to implement
			}
		}

		String massUnits=UnitsConverter.getMassUnits(metadata.datasetUnit);
						
		// ************************************************************

		System.out.println("datasetunit="+metadata.datasetUnit);
		System.out.println("massunits="+massUnits);

		
		createQSARUnitsRow(metadata.datasetUnit,predToxVal, predToxUnc, er, endpoint, writePredictionInterval, predictionResultsPrimaryTable);
		
		// ************************************************************
		// mass units row:
		
		if(massUnits!=null)createMassUnitsRow(metadata,pr, er.expToxValue,predToxVal,predToxUnc, MW, endpoint, writePredictionInterval, predictionResultsPrimaryTable,metadata.datasetUnit, massUnits);

		// ************************************************************

		if (er.expSet.equals("Training")) {
			String trainingExpSetText = "Note: the test chemical was present in the training set.";
			if (predToxVal !=null) {
				trainingExpSetText += "  The prediction <i>does not</i> represent an external prediction.";
			}
			predictionResultsPrimaryTable.setPredictedValueNote(trainingExpSetText);
		} else if (er.expSet.equals("Test")) {
			predictionResultsPrimaryTable.setPredictedValueNote("Note: the test chemical was present in the external test set.");
		}

		// System.out.println(message);
		if (errorMessage!=null) {
			predictionResultsPrimaryTable.setMessage(errorMessage);
		}
	}
	
	private static void createMassUnitsRow(PredictionReportMetadata metadata, PredictionResults pr, Double expToxVal, Double predToxVal, Double predToxUnc, double MW, String endpoint,
			boolean writePredictionInterval, PredictionResultsPrimaryTable predictionResultsPrimaryTable,String molarUnits, String massUnits) {
		
				
		predictionResultsPrimaryTable.setMassUnits(massUnits);
		
		Double expToxValMass=null,predToxValMass=null;
		
		if (pr.isLogMolarEndpoint()) {
			if (expToxVal!=null) {						
				expToxValMass=UnitsConverter.getToxValMassFromLog(molarUnits, expToxVal, MW);
			}
			
			if (predToxVal!=null) {						
				predToxValMass=UnitsConverter.getToxValMassFromLog(molarUnits, predToxVal, MW);
			}
		} else {
			expToxValMass=expToxVal;
			predToxValMass=predToxVal;
		}
		
		
		predictionResultsPrimaryTable.setExpToxValMass(formatToxValMass(pr.isLogMolarEndpoint(),expToxValMass));		
		predictionResultsPrimaryTable.setPredToxValMass(formatToxValMass(pr.isLogMolarEndpoint(), predToxValMass));
		
		if (writePredictionInterval) {
			if (predToxVal == null) {
				predictionResultsPrimaryTable.setPredMinMaxValMass("N/A");
			} else {
				double minvalmass, maxvalmass;

				if (pr.isLogMolarEndpoint()) {
					double minval = predToxVal + predToxUnc; // molar value
					double maxval = predToxVal - predToxUnc; // molar value
					minvalmass = UnitsConverter.getToxValMassFromLog(metadata.datasetUnit,minval, MW);//convert molar value of minval to mass 
					maxvalmass = UnitsConverter.getToxValMassFromLog(endpoint, maxval, MW);//convert molar value of maxval to mass
				} else {
					minvalmass = predToxVal + predToxUnc; // molar value
					maxvalmass = predToxVal - predToxUnc; // molar value
				}

				if (maxvalmass < minvalmass) {// need for BCF
					double temp = minvalmass;
					minvalmass = maxvalmass;
					maxvalmass = temp;
				}

				if (Math.abs(maxvalmass) < 0.1 && pr.isLogMolarEndpoint()) {
					predictionResultsPrimaryTable.setPredMinMaxValMass(d2exp.format(minvalmass) + " &le; Tox &le; " + d2exp.format(maxvalmass));
				} else {
					predictionResultsPrimaryTable.setPredMinMaxValMass(d2.format(minvalmass) + " &le; Tox &le; " + d2.format(maxvalmass));
				}

			}
		}
	}


private static void createQSARUnitsRow(String units, Double predToxVal, Double predToxUnc, ExpRecord er, String endpoint,
		boolean writePredictionInterval, PredictionResultsPrimaryTable predictionResultsPrimaryTable) {
	
	predictionResultsPrimaryTable.setMolarLogUnits(units);

	
	if (er.expToxValue == null) {
		predictionResultsPrimaryTable.setExpToxValue("N/A");
	} else {
		predictionResultsPrimaryTable.setExpToxValue(d2.format(er.expToxValue));
	}

	if (predToxVal == null) {
		predictionResultsPrimaryTable.setPredToxValue("N/A");
	} else {
		predictionResultsPrimaryTable.setPredToxValue(d2.format(predToxVal));
	}

	if (writePredictionInterval) {
		if (predToxVal != null) {
			predictionResultsPrimaryTable.setPredMinMaxVal("N/A");
		} else {
			double minval = predToxVal - predToxUnc; // molar value
			double maxval = predToxVal + predToxUnc; // molar value
			predictionResultsPrimaryTable.setPredMinMaxVal(d2.format(minval) + " &le; Tox &le; " + d2.format(maxval));
		}
	}
}
//
	public static String formatToxValMass(boolean isLogMolarEndpoint, Double val) {

		String strVal="";

		if (val==null || val.isNaN()) {
			strVal="N/A";			
		} else {
			if (Math.abs(val) < 0.1 && isLogMolarEndpoint) {
				strVal=d2exp.format(val);
			} else {
				strVal=d2.format(val);
			}
		}
		return strVal;
	}
	
	
	
	
//	public static String getSourceTag(String endpoint) {
//		String sourceTag = null;
//				
//		if (endpoint.equals(TESTConstants.ChoiceFHM_LC50) || endpoint.equals(TESTConstants.ChoiceDM_LC50) || endpoint.equals(TESTConstants.ChoiceGA_EC50)) {
//			//link validated on 11/7/20:
//			sourceTag = ("<br>Source: <a href=\"http://cfpub.epa.gov/ecotox/\" target=\"_blank\">ECOTOX</a>");
//		} else if (endpoint.equals(TESTConstants.ChoiceTP_IGC50)) {
//			//link validated on 11/7/20:
//			sourceTag = ("<br>Source: <a href=\"https://www.tandfonline.com/doi/abs/10.1080/105172397243079\" target=\"_blank\">TETRATOX</a>");
//		} else if (endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
////			link validated on 11/7/20:
//			sourceTag = ("<br>Source: <a href=\"https://chem.nlm.nih.gov/chemidplus/\" target=\"_blank\">ChemidPlus</a>");
//		} else if (endpoint.equals(TESTConstants.ChoiceBCF)) {
////			links validated on 11/7/20:
//			String ref = "<br>Sources: ";
//			ref += "<a href=\"http://www.tandfonline.com/doi/abs/10.1080/10659360500474623\" target=\"_blank\">Dimetrov 2005</a>, ";
//			ref += "<a href=\"http://www.nrcresearchpress.com/doi/abs/10.1139/a06-005\" target=\"_blank\">Arnot 2006</a>, and ";
//			ref += "<a href=\"http://www.sciencedirect.com/science/article/pii/S0045653508011922\" target=\"_blank\">Zhao 2008</a>";
//			sourceTag = (ref);
//		} else if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//			//link validated on 11/7/20:
//			sourceTag = ("<br>Source: <a href=\"http://www.caesar-project.eu/index.php?page=results&section=endpoint&ne=5\" target=\"_blank\">CAESAR</a>");
//		} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
//			//link validated on 11/7/20:
//			sourceTag = ("<br>Source: <a href=\"http://doc.ml.tu-berlin.de/toxbenchmark/\" target=\"_blank\">Toxicity Benchmark</a>");
//		} else if (endpoint.equals(TESTConstants.ChoiceDensity) || endpoint.equals(TESTConstants.ChoiceFlashPoint)) {
////			link validated on 11/7/20:
//			sourceTag = ("<br>Source: <a href=\"http://www.lookchem.com/\" target=\"_blank\">Lookchem.com</a>");
//		} else if (endpoint.equals(TESTConstants.ChoiceViscosity)) {
////			links validated on 11/7/20:
//			String ref = "<br>Sources: ";
//			ref += "<a href=\"http://www.worldcat.org/title/data-book-on-the-viscosity-of-liquids/oclc/18833753\" target=\"_blank\">Viswanath 1989</a>, ";
//			ref += "<a href=\"https://www.worldcat.org/title/techniques-of-chemistry-2-organic-solvents-physical-properties-and-methods-of-purification-4ed/oclc/472811023\" target=\"_blank\">Riddick 1996</a>";
//			sourceTag = (ref);
//		} else if (endpoint.equals(TESTConstants.ChoiceThermalConductivity)) {
////			links validated on 11/7/20:
//			String ref = "<br>Sources: ";
//			ref += "<a href=\"http://www.worldcat.org/title/liquid-thermal-conductivity-a-data-survey-to-1973/oclc/3090244\" target=\"_blank\">Jamieson 1975</a>, ";
//			ref += "<a href=\"http://www.worldcat.org/title/handbook-of-thermal-conductivity-of-liquids-and-gases/oclc/28847166&referer=brief_results\" target=\"_blank\">Vargaftik 1994</a>";
//			sourceTag = (ref);
//		} else if (endpoint.equals(TESTConstants.ChoiceSurfaceTension)) {
////			link validated on 11/7/20:
//			String ref = "<br>Source: ";
//			ref += "<a href=\"https://doi.org/10.1063/1.3253106\" target=\"_blank\">Jaspar 1972</a>";
//			sourceTag = ref;
//		} else if (endpoint.equals(TESTConstants.ChoiceWaterSolubility) || endpoint.equals(TESTConstants.ChoiceBoilingPoint) || endpoint.equals(TESTConstants.ChoiceVaporPressure)
//				|| endpoint.equals(TESTConstants.ChoiceMeltingPoint)) {
////			link validated on 11/7/20:
//			sourceTag = ("<br>Source: <a href=\"" + "https://www.epa.gov/tsca-screening-tools/epi-suitetm-estimation-program-interface\" target=\"_blank\">" + "EPI Suite v 4.00</a> ");
//		} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor) || endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
//			String ref = "<br>Source: ";
//			ref += "<a href=\"http://www.worldcat.org/title/predicting-chemical-toxicity-and-fate/oclc/54073110&referer=brief_results\">Cronin and Livingstone, 2004</a>";
//			sourceTag = ref;
//		} else {
//			sourceTag = "<br>?";
//		}
//		return sourceTag;
//	}
	
	
	public static Color getColor(double SCi) {

		Color color = null;

		if (SCi >= 0.9) {
			color = Color.green;
		} else if (SCi < 0.9 && SCi >= 0.8) {
			// color=Color.blue;
			color = new Color(100, 100, 255);// light blue
		} else if (SCi < 0.8 && SCi >= 0.7) {
			color = Color.yellow;
		} else if (SCi < 0.7 && SCi >= 0.6) {
			color = Color.orange;
		} else if (SCi < 0.6) {
			// color=Color.red;//255,153,153
			color = new Color(255, 100, 100);// light red
		}

		if (color == null)
			System.out.println("null color for " + SCi);
		// System.out.println(SCi+"\t"+color.getRGB());
		return color;
	}
//
	public static String getColorString(double SC) {
		Color color = getColor(SC);
		String strColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
		return strColor;
	}
	
//
//
//	/**
//	 * 
//	 * Writes a table of the overall results from each of the QSAR models used
//	 * in the consensus prediction
//	 *
//	 * @param endpoint
//	 * @param methods
//	 * @param predictions
//	 * @param uncertainties
//	 * @param createDetailedConsensusReport
//	 * @throws Exception
//	 */
	private void writeIndividualPredictionsForConsensus(PredictionResults pr, List<QsarPredictedValue>preds,String units,List<PredictionReportModelMetadata> predictionReportModelMetadata)
					throws Exception {

		IndividualPredictionsForConsensus individualPredictionsForConsensus = new IndividualPredictionsForConsensus();
		pr.setIndividualPredictionsForConsensus(individualPredictionsForConsensus);

		if (pr.isLogMolarEndpoint())
			individualPredictionsForConsensus.setUnits(units);
		else
			individualPredictionsForConsensus.setUnits(units);

		for (QsarPredictedValue pred:preds) {
			
//			System.out.println(tpv.method+"\t"+tpv.predValMolarLog);
			
//			if (tpv.method.toLowerCase().contains(TESTConstants.ChoiceConsensus.toLowerCase()))
//				continue;

			IndividualPredictionsForConsensus.PredictionIndividualMethod predIndMethod = new IndividualPredictionsForConsensus().new PredictionIndividualMethod();

			predIndMethod.setMethod(pred.qsarMethodName);
			predIndMethod.setMethodDescription(getMethodDescription(predictionReportModelMetadata,pred.qsarMethodName));
			
			
			if (pred.qsarPredictedValue==null) {
				predIndMethod.setPrediction("N/A");
			} else {
				predIndMethod.setPrediction(d2.format(pred.qsarPredictedValue));
			}
			individualPredictionsForConsensus.getConsensusPredictions().add(predIndMethod);
		}

	}
	
	private String getMethodDescription(List<PredictionReportModelMetadata> predictionReportModelMetadata,String methodName) {

		for (PredictionReportModelMetadata modelMetadata:predictionReportModelMetadata) {
			if (modelMetadata.qsarMethodName.equals(methodName)) {
				return addHyperlinkToDescription(modelMetadata.qsarMethodDescription);
			}
		}
		
		return "N/A";
		
	}
	
	
	private String addHyperlinkToDescription (String description) {
		if (!description.contains("http")) {
			return description;
		} else {			
			//TODO later we might want to add a url field in the database for the qsar method
			
			String link=description.substring(description.indexOf("(")+1,description.indexOf(")"));
			String text = description.substring(0, description.indexOf("("));
			String result= "<a href=\""+link+"\" target=\"_blank\">"+text+"</a>";
//			System.out.println(result);
			return result;			
		}
	}

	
	public class MyComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2) {

			double d1 = (Double) o1;
			double d2 = (Double) o2;
			if (d1 > d2)
				return -1;
			else if (d1 < d2)
				return 1;
			else
				return 0;

		}

	}
	
	
	private void writeSimilarChemicals(PredictionResults pr, String set, 
			List<Analog>analogs, double expVal, Object predVal, String gsid,String units,double maeEntireSet) throws Exception
	{

		if (analogs == null)
			return;

		SimilarChemicals similarChemicals = new SimilarChemicals();

		similarChemicals.setSimilarChemicalsCount(analogs.size());
		similarChemicals.setSimilarChemicalsSet(set);

		pr.getSimilarChemicals().add(similarChemicals);

		if (analogs.size() == 0) {
			return;
		}

		// ************************************************************
		// Calc stats:
		if (!pr.isBinaryEndpoint() && analogs.size() > 0) {

			String set2 = set.substring(0, 1).toUpperCase() + set.substring(1);// capitalize
			// first
			// letter
			String chartname = "PredictionResults" + pr.getMethod() + "-Similar" + set2 + "SetChemicals.png";
			
			boolean haveNullPred=false;
			
			for (Analog analog:analogs) {
				if (analog.pred==null) {
					haveNullPred=true;
					break;
				}
			}
			
			//TODO change code so it will work if have a null pred			
			if (!haveNullPred) this.writeExternalPredChart(pr, analogs,  chartname, similarChemicals, units,maeEntireSet);
		} else if (pr.isBinaryEndpoint()) {
			this.calcCancerStats(0.5, analogs, similarChemicals);
		}

		//			logger.debug(expVal+"\t"+predVal);

		writeSimilarChemicalsTable(pr, expVal, predVal, gsid, d2, analogs, similarChemicals, units,maeEntireSet);



	}
//
	private String calcCancerStats(double cutoff, List<Analog>analogs, SimilarChemicals similarChemicals) {

		CancerStats cancerStats = new CancerStats();
		int predCount = 0;
		int posPredCount = 0;
		int negPredCount = 0;

		int correctCount = 0;
		int posCorrectCount = 0;
		int negCorrectCount = 0;

		for (int i = 0; i < analogs.size(); i++) {
			
			Analog analog=analogs.get(i);
			double exp = analog.exp;
			double pred = analog.pred;

			String strExp = "";

			if (cutoff == 0.5) {
				if (exp >= cutoff)
					strExp = "C";
				else
					strExp = "NC";
			} else if (cutoff == 30) {
				if (exp == 0)
					strExp = "N/A";
				else if (exp >= cutoff)
					strExp = "C";
				else
					strExp = "NC";
			}

			String strPred = "";

			if (pred >= cutoff)
				strPred = "C";
			else
				strPred = "NC";

			// if (strExp.equals("C"))
			// System.out.println(exp+"\t"+pred+"\t"+strExp+"\t"+strPred);

			predCount++;
			if (strExp.equals("C"))
				posPredCount++;
			else if (strExp.equals("NC"))
				negPredCount++;

			if (strExp.equals(strPred)) {
				correctCount++;
				if (strExp.equals("C"))
					posCorrectCount++;
				else if (strExp.equals("NC"))
					negCorrectCount++;

			}
		}

		double concordance = correctCount / (double) predCount;
		double posConcordance = posCorrectCount / (double) posPredCount;
		double negConcordance = negCorrectCount / (double) negPredCount;


		if (predCount > 0) {
			cancerStats.setConcordance(d2.format(concordance));
			cancerStats.setCorrectCount(correctCount);
			cancerStats.setPredCount(predCount);
		} else {
			cancerStats.setConcordance("N/A");
		}

		if (posPredCount > 0) {
			cancerStats.setPosConcordance(d2.format(posConcordance));
			cancerStats.setPosCorrectCount(posCorrectCount);
			cancerStats.setPosPredCount(posPredCount);
		}
		else
			cancerStats.setPosConcordance("N/A");

		if (negPredCount > 0) {
			cancerStats.setNegConcordance(d2.format(negConcordance));
			cancerStats.setNegCorrectCount(negCorrectCount);
			cancerStats.setNegPredCount(negPredCount);
		}
		else
			cancerStats.setNegConcordance("N/A");

		similarChemicals.setCancerStats(cancerStats);

		return null;
	}
//
	private void writeSimilarChemicalsTable(PredictionResults pr, double expVal, Object predVal, 
			String gsid, java.text.DecimalFormat df, List<Analog>analogs,
			SimilarChemicals similarChemicals,String units,double mae) throws Exception   {

		// ***********************************************************
		// Write out table of exp and pred values for nearest chemicals:

		//		SimilarChemicals similarChemicals = predictionResults.getSimilarChemicals();


		similarChemicals.setUnits(units);

		if (expVal == -9999.00)
			similarChemicals.setExpVal("N/A");
		else
			similarChemicals.setExpVal(df.format(expVal));

		if (predVal !=null) {
			if (predVal instanceof Boolean) {
				similarChemicals.setPredVal(predVal+"");
			} else {
				similarChemicals.setPredVal(df.format(predVal));	
			}
			
		} else {
			similarChemicals.setPredVal("N/A");
		}

		for (int i = 0; i < analogs.size(); i++) {
			Analog analog=analogs.get(i);
			SimilarChemical similarChemical = new SimilarChemical();
			

			String DSSTOXSIDi = null;

			similarChemical.setDtxcid(analog.dtxcid);
			similarChemical.setDSSTOXSID(DSSTOXSIDi);
			similarChemical.setCAS(analog.casrn);
			
			if (analog.dtxcid!=null)
				similarChemical.setImageUrl(webPathImage+analog.dtxcid);

			String strColor = getColorString(analog.sim);
			similarChemical.setBackgroundColor(strColor);
			similarChemical.setSimilarityCoefficient(d2.format(analog.sim));
			similarChemical.setExpVal(d2.format(analog.exp));
			
			if (analog.pred!=null)	similarChemical.setPredVal(d2.format(analog.pred));
			else similarChemical.setPredVal("N/A");

			similarChemicals.getSimilarChemicalsList().add(similarChemical);

		} // end loop over elements

	}
//
//	
//	
	private void writeExternalPredChart(PredictionResults pr,List<Analog>analogs, 
			String chartname, SimilarChemicals similarChemicals,String units,double maeEntireSet) throws Exception {

		ExternalPredChart externalPredChart = new ExternalPredChart();

		// **************************************************************************************
		// since we can write encoded string to JSON we dont need to write image to file first:
		// String outputfolder2 = options.reportBase +
		// File.separator+"SimilarChemicals";
		//
		// File of = new File(outputfolder2);
		// if (!of.exists()) {
		// 		of.mkdir();
		// }
		//**************************************************************************************

		double[] x = new double[analogs.size()];
		double[] y = new double[analogs.size()];
		double[] SC = new double[analogs.size()];

		double MAE = 0;

		for (int i = 0; i < analogs.size(); i++) {
			Analog analog=analogs.get(i);
			x[i] = analog.exp;
			y[i] = analog.pred;
			SC[i] = analog.sim;
			MAE += Math.abs(x[i] - y[i]);
		}
		MAE /= (double) x.length;

		externalPredChart.setMAE(MAE);

		

//		if (pr.getMethod().contentEquals(TESTConstants.ChoiceLDA))
//			MAEEntireTestSet = lookup.CalculateMAE(predfilename, "Exp_Value:-Log10(mol/L)", "Pred_Value:-Log10(mol/L)", "|");
//		else
//			MAEEntireTestSet = lookup.CalculateMAE(predfilename, "expToxicValue", pr.getMethod(), "\t");
		
		
		
		externalPredChart.setMAEEntireTestSet(maeEntireSet);
		
		String title = pr.getEndpoint() + " " + units;

		String xtitle = "Exp. " + title;
		String ytitle = "Pred. " + title;

		// String charttitle = "Prediction results (redder = more similar)";
//		String charttitle = "Prediction results (colors defined in table below)";
		String charttitle = "Prediction results";

		JLabelChart fc = new JLabelChart(x, y, SC, charttitle, xtitle, ytitle);
		fc.doDrawLegend = false;
		fc.doDrawStatsR2 = false;
		fc.doDrawStatsMAE = true;

		//		fc.WriteImageToFile(chartname, outputfolder2);
		//		URL imageFileURL = new File(ReportUtils.getImageSrc(options, "SimilarChemicals", chartname)).toURI().toURL();
		//		externalPredChart.setExternalPredChartImageSrc(ReportUtils.convertImageToBase64(imageFileURL.toString()));

		//Don't need write to file- save directly to the Json object:
		externalPredChart.setExternalPredChartImageSrc(fc.createImgURL());


		similarChemicals.setExternalPredChart(externalPredChart);
	}
//
//
//	static String CreateQSARPlot(OptimalResults or, String endpoint) {
//
//		double[] Yexp = or.getObserved();
//		double[] Ycalc = or.getPredicted();
//
//		// System.out.println(or.clusterNumber);
//		// for (int i=0;i<or.observed.length;i++) {
//		// System.out.println(i+"\t"+Yexp[i]+"\t"+Ycalc[i]);
//		// }
//		// System.out.println(filename);
//
//		String title;
//
//		if (TESTConstants.isLogMolar(endpoint)) {
//			title = endpoint + " " + TESTConstants.getMolarLogUnits(endpoint);
//		} else {
//			title = endpoint + " " + TESTConstants.getMassUnits(endpoint);
//		}
//
//		String xtitle = "Exp. " + title;
//		String ytitle = "Pred. " + title;
//		fraChart.JLabelChart fc = new fraChart.JLabelChart(Yexp, Ycalc, xtitle, ytitle);
//
//		fc.doDrawLegend = true;
//		fc.doDrawStatsR2 = false;
//		fc.doDrawStatsMAE = false;
//
//		// fraChart fc=new fraChart(Yexp,Ycalc);
//		return fc.createImgURL();
//
//	}
//
	public static void WriteBinaryPredictionTable(PredictionResults pr, String dtxcid,ExpRecord er, Double predToxVal, String message) throws Exception {

		PredictionResultsPrimaryTable predictionResultsPrimaryTable = pr.getPredictionResultsPrimaryTable();
		predictionResultsPrimaryTable.setImageID(webPathImage+dtxcid);


		String endpoint=pr.getEndpoint();
		String method=pr.getMethod();


		double bound1 = 0.5;//cutoff for positive result

		// System.out.println(message);


		// ************************************************
		// Header row

		if (er.expToxValue != null) {
			predictionResultsPrimaryTable.setExpCAS(er.expCAS);
//			predictionResultsPrimaryTable.setSource(getSourceTag(endpoint));//TODO need to get data source from database?
		}

		predictionResultsPrimaryTable.setExpSet(er.expSet);

		if (er.expSet.equals("Training") || er.expSet.equals("Test")) {
			String predictedValueSuperscript = "a";

			if (message!=null) {
				predictedValueSuperscript += ",b";
			}
			predictionResultsPrimaryTable.setPredictedValueSuperscript(predictedValueSuperscript);
		} else {
			if (message!=null) {
				predictionResultsPrimaryTable.setPredictedValueSuperscript("b");
			}
		}

		// ************************************************************
		// Value row

		if (er.expToxValue == null) predictionResultsPrimaryTable.setExpToxValue("N/A");
		else predictionResultsPrimaryTable.setExpToxValue(er.expToxValue+"");
		
		if (predToxVal == null) predictionResultsPrimaryTable.setPredToxValue("N/A");
		else predictionResultsPrimaryTable.setPredToxValue(d2.format(predToxVal));


		// ************************************************************
		// result row:
		if (er.expToxValue == null) {
			predictionResultsPrimaryTable.setExpToxValueEndpoint("N/A");
		} else if (er.expToxValue < bound1) {
			predictionResultsPrimaryTable.setExpToxValueEndpoint("negative");
		} else {
			predictionResultsPrimaryTable.setExpToxValueEndpoint("positive");
		}

		if (predToxVal == null) {
			predictionResultsPrimaryTable.setPredValueEndpoint("N/A");
		} else if (predToxVal < bound1) {
			predictionResultsPrimaryTable.setPredValueEndpoint("negative");
		} else {
			predictionResultsPrimaryTable.setPredValueEndpoint("positive");
		}

		// ************************************************************

		if (er.expSet.equals("Training")) {
			String trainingExpSetText = "Note: the test chemical was present in the training set.";
			trainingExpSetText += "  The prediction " + "does not represent an external prediction.";			
			predictionResultsPrimaryTable.setPredictedValueNote(trainingExpSetText);
		} else if (er.expSet.equals("Test")) {
			predictionResultsPrimaryTable.setPredictedValueNote("Note: the test chemical was present in the external test set.");
		}

		// System.out.println(message);
		if (message!=null) {
			predictionResultsPrimaryTable.setMessage(message);
		}

		pr.setPredictionResultsPrimaryTable(predictionResultsPrimaryTable);

	}
//
//
//	private Hashtable<String,String> LoadDefinitions() {
//
//		try {
//			//
//			Hashtable<String,String>htVarDef = new Hashtable<>();
//
//			String file = "variable definitions.txt"; // need to go up and then
//			// down into data folder
//			InputStream ins = this.getClass().getClassLoader().getResourceAsStream(file);
//
//			BufferedReader br = new BufferedReader(new InputStreamReader(ins));
//
//			String Line = "12345";
//			while (true) {
//				Line = br.readLine();
//				if (!(Line instanceof String))
//					break;
//
//				LinkedList<String> ll = Utilities.Parse(Line, "\t");
//
//				String strvar = (String) ll.get(0);
//				String strdef = (String) ll.get(1);
//
//				htVarDef.put(strvar, strdef);
//
//			}
//
//			br.close();
//			ins.close();
//
//			return htVarDef;
//
//		} catch (Exception ex) {
//			logger.catching(ex);
//			return null;
//		}
//	}
//
//
//	private  void createDescriptorTable(TestChemical chemical,OptimalResults or, double predVal, String endpoint,ClusterModel clusterModel) {
//		try {
//
//
//			DecimalFormat df4 = new DecimalFormat("0.0000");
//
//			Vector<Descriptor>descriptors=new Vector<>();
//			clusterModel.setDescriptors(descriptors);
//
//
//
//			double[] coeff = or.getBcoeff();
//			double[] coeffSE = or.getBcoeffSE();
//
//			int[] descriptorNumbers = or.getDescriptors();
//
//			for (int i = 0; i < or.getDescriptorNames().length; i++) {
//
//				Descriptor descriptor=new Descriptor();
//				descriptors.add(descriptor);
//
//				descriptor.setName(or.getDescriptorNames()[i]);
//				descriptor.setDefinition(this.htVarDefs.get(descriptor.getName()));
//
//				double descval = chemical.value(descriptorNumbers[i]);
//				descriptor.setValue (df4.format(descval));
//				descriptor.setCoefficient(df4.format(coeff[i]));
//				descriptor.setCoefficientUncertainty(df4.format(coeffSE[i]));
//
//				if (descval == 0) {
//					descriptor.setValue_x_coefficient(d2.format(0));
//				} else {
//					descriptor.setValue_x_coefficient(d2.format(descval * coeff[i]));
//				}
//			}
//
//			Descriptor descriptor=new Descriptor();
//			descriptors.add(descriptor);
//			descriptor.setName("Model intercept");
//			descriptor.setValue ("1.0000");
//			descriptor.setCoefficient(df4.format(coeff[coeff.length - 1]));
//			descriptor.setCoefficientUncertainty(df4.format(coeffSE[coeff.length - 1]));
//			descriptor.setValue_x_coefficient(df4.format(coeff[coeff.length - 1]));
//			descriptor.setDefinition("Intercept of multilinear regression model");
//
//			// ************************************************************************************************
//			// Predicted value:
//
//			String units;
//			if (TESTConstants.isLogMolar(endpoint)) {
//				units = TESTConstants.getMolarLogUnits(endpoint);
//			} else {
//				units = TESTConstants.getMassUnits(endpoint);
//			}
//			clusterModel.setPredictedValue(d2.format(predVal));
//			clusterModel.setPredictedValueLabel("Predicted value " + units);
//
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//	}
//	
//	
//	private ClusterModel createClusterModelLDA(PredictionResults pr,TestChemical chemical,String method,OptimalResults or,double predVal,double predUnc,
//			String message,String MOA,String type) {
//		ClusterModel clusterModel=new ClusterModel();
//		
//		if (pr.isCreateDetailedReport()) {
//			clusterModel.setClusterID(MOA + " "+type);
//			clusterModel.setUrl("../../ClusterFiles/" + MOA + " "+type+".html");
//			clusterModel.setUrlDescriptors("ClusterFiles/Descriptors " + MOA + " "+type+".html");
//		} 
//
//		if (type.contentEquals("LC50")) {
//			clusterModel.setBinary(false);
//			clusterModel.setMinMaxValue(d2.format(predVal)+" &plusmn; " + d2.format(predUnc));
//			if (predUnc == 0) {
//				clusterModel.setOmitted(true);
//			}
//			clusterModel.setR2(d3.format(or.getR2()));
//			clusterModel.setQ2(d3.format(or.getQ2()));
//
//			if (pr.isCreateDetailedReport()) clusterModel.setPlotImage(CreateQSARPlot(or, pr.getEndpoint()));
//
//		} else {
//			clusterModel.setBinary(true);
//			clusterModel.setMinMaxValue(d2.format(predVal));
//			clusterModel.setConcordance(d3.format(or.getConcordance()));			
//			clusterModel.setSensitivity(d3.format(or.getSensitivity()));
//			clusterModel.setSpecificity(d3.format(or.getSpecificity()));
//		}
//		clusterModel.setNumChemicals(or.getNumChemicals());
//		clusterModel.setMessageAD(message);
//
//		String dependentVariable="";
//		
//		if (type.contentEquals("LC50")) dependentVariable=pr.getEndpoint();
//		else dependentVariable="LDA Score "+MOA;
//		
//		clusterModel.setModelEquation(PredictToxicityWebPageCreator.GetModelEquation(or,dependentVariable));
//
//		//		System.out.println(clusterModel.getModelEquation());
//
//		if (pr.isCreateDetailedReport()) 
//			createDescriptorTable(chemical,or, predVal, pr.getEndpoint(), clusterModel);
//
//
//		return clusterModel;
//	}
//
//	private ClusterModel createClusterModel(PredictionResults pr,TestChemical chemical,String method,OptimalResults or,double predVal,double predUnc,String message) {
//		ClusterModel clusterModel=new ClusterModel();
//
//
//		if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
//			clusterModel.setClusterID("FDA model");
//		} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// GCM
//			clusterModel.setClusterID("Group Contribution");
//		} else {
//			clusterModel.setClusterID(or.getClusterNumber()+"");
//		}
//
//		if (pr.isCreateDetailedReport()) {
//			if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
//				clusterModel.setUrl("ClusterFiles/PredictionResultsFDACluster.html");
//				clusterModel.setUrlDescriptors("ClusterFiles/DescriptorsFDA.html");
//			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// GCM
//				clusterModel.setUrl("../../ClusterFiles/" + pr.getEndpoint() + "/GroupContribution.html");
//				clusterModel.setUrlDescriptors("ClusterFiles/DescriptorsGroupContribution.html");
//			} else if (method.contentEquals(TESTConstants.ChoiceLDA)) {
//				clusterModel.setUrl("../../ClusterFiles/" + pr.getEndpoint() + "/GroupContribution.html");
//				clusterModel.setUrlDescriptors("ClusterFiles/DescriptorsGroupContribution.html");
//				
//			} else {
//				clusterModel.setUrl("../../ClusterFiles/" + pr.getEndpoint() + "/" + or.getClusterNumber() + ".html");
//				clusterModel.setUrlDescriptors("ClusterFiles/Descriptors" + or.getClusterNumber()+ ".html");
//			} 
//		} 
//
//		if (!pr.isBinaryEndpoint()) {
//			clusterModel.setBinary(false);
//			clusterModel.setMinMaxValue(d2.format(predVal)+" &plusmn; " + d2.format(predUnc));
//			if (predUnc == 0) {
//				clusterModel.setOmitted(true);
//			}
//			clusterModel.setR2(d3.format(or.getR2()));
//			clusterModel.setQ2(d3.format(or.getQ2()));
//
//			if (pr.isCreateDetailedReport()) clusterModel.setPlotImage(CreateQSARPlot(or, pr.getEndpoint()));
//
//		} else {
//			clusterModel.setBinary(true);
//			clusterModel.setMinMaxValue(d2.format(predVal));
//			clusterModel.setConcordance(d3.format(or.getConcordance()));
//			clusterModel.setSensitivity(d3.format(or.getSensitivity()));
//			clusterModel.setSpecificity(d3.format(or.getSpecificity()));
//		}
//		clusterModel.setNumChemicals(or.getNumChemicals());
//		clusterModel.setMessageAD(message);
//
//		clusterModel.setModelEquation(PredictToxicityWebPageCreator.GetModelEquation(or,pr.getEndpoint()));
//
//		//		System.out.println(clusterModel.getModelEquation());
//
//		if (pr.isCreateDetailedReport()) 
//			createDescriptorTable(chemical,or, predVal, pr.getEndpoint(), clusterModel);
//
//
//		return clusterModel;
//	}
//
//	//	private ClusterModel createClusterModel(String method,DataForPredictionRun d,OptimalResults or) {
//	//		ClusterModel clusterModel=new ClusterModel();
//	//
//	//		if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
//	//			clusterModel.setClusterID("FDA model");
//	//		} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// GCM
//	//			clusterModel.setClusterID("Group Contribution");
//	//		} else {
//	//			clusterModel.setClusterID(or.getClusterNumber()+"");
//	//		}
//	//
//	//		if (d.createDetailedReport) {
//	//			if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
//	//				clusterModel.setUrl("ClusterFiles/PredictionResultsFDACluster.html");
//	//				clusterModel.setUrlDescriptors("ClusterFiles/DescriptorsFDA.html");
//	//			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// GCM
//	//				clusterModel.setUrl("../../ClusterFiles/" + d.endpoint + "/GroupContributionCluster.html");
//	//				clusterModel.setUrlDescriptors("ClusterFiles/DescriptorsGroupContribution.html");
//	//			} else {
//	//				clusterModel.setUrl("../../ClusterFiles/" + d.endpoint + "/" + or.getClusterNumber() + ".html");
//	//				clusterModel.setUrlDescriptors("ClusterFiles/Descriptors" + or.getClusterNumber()+ ".html");
//	//			}
//	//		} 
//	//				
//	//		if (!d.isBinaryEndpoint) {		
//	//			clusterModel.setR2(d3.format(or.getR2()));
//	//			clusterModel.setQ2(d3.format(or.getQ2()));		
//	//		} else {
//	//			clusterModel.setConcordance(d3.format(or.getConcordance()));
//	//			clusterModel.setSensitivity(d3.format(or.getSensitivity()));
//	//			clusterModel.setSpecificity(d3.format(or.getSpecificity()));
//	//		}
//	//		clusterModel.setNumChemicals(or.getNumChemicals());
//	//
//	//		return clusterModel;
//	//	}
//
//
//	public PredictionResults generatePredictionResultsHierarchicalClustering(TestChemical chemical,DataForPredictionRun d,
//			TESTPredictedValue tpv,double predToxVal, double predToxUnc,
//			Vector<OptimalResults>resultsVector,Vector<OptimalResults>invalidResultsVector,Vector<Double>predictions,Vector<Double>uncertainties,
//			Vector<Double>predictionsOutsideAD,Vector<Double>uncertaintiesOutsideAD,Vector<String>violationsAD,
//			String method,ReportOptions options,boolean createReports) {
//
//		try {
//
//			PredictionResults pr=new PredictionResults();
//			pr.setReportBase(options.reportBase);
//			setCommonValues(method, d, tpv, pr, predToxVal, predToxUnc,createReports);
//
//			if (createReports) {
//				if (resultsVector.size() > 0) {
//					pr.setClusterTable(createClusterTable(pr,chemical,resultsVector, predictions, uncertainties, null,method,"Cluster model predictions and statistics"));
//				}
//
//				if (invalidResultsVector.size() > 0) {
//					pr.setInvalidClusterTable(createClusterTable(pr,chemical,invalidResultsVector, predictionsOutsideAD, uncertaintiesOutsideAD,violationsAD, method,"Cluster models with <font color=\"red\">applicability domain violation</font>"));
//				}
//			}
//
//			return pr;
//
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//
//		return null;
//	}
//
//	private ClusterTable createClusterTable(PredictionResults pr,TestChemical chemical, Vector<OptimalResults> resultsVector,
//			Vector<Double> predictions, Vector<Double> uncertainties, Vector<String> violationAD,String method,String caption) {
//
//		ClusterTable clusterTable=new ClusterTable();
//
//		clusterTable.setCaption(caption);
//
//		if (pr.isBinaryEndpoint() || pr.isLogMolarEndpoint()) {
//			clusterTable.setUnits(TESTConstants.getMolarLogUnits(pr.getEndpoint()));
//		} else {
//			clusterTable.setUnits(TESTConstants.getMassUnits(pr.getEndpoint()));
//		}
//
//		Vector<ClusterModel>clusterModels=new Vector<>();
//		clusterTable.setClusterModels(clusterModels);
//
//		boolean HaveOmitted = false;
//
//
//		
//		for (int i = 0; i < resultsVector.size(); i++) {
//			OptimalResults or = (OptimalResults) resultsVector.get(i);
//
//			or.calculatePredictedValues();
//			
//
//			
//			if (pr.isBinaryEndpoint()) or.CalculateCancerStats(0.5);
//
//			//			if (d.isBinaryEndpoint)
//			//				or.CalculateCancerStats(0.5);// TODO add cutoff as passed
//
//			double predVal=predictions.get(i);
//			double predUnc=uncertainties.get(i);
//
//			String message;						
//			if (violationAD==null) message="OK";
//			else message=violationAD.get(i);
//
//			ClusterModel clusterModel=createClusterModel(pr,chemical,method, or,predVal,predUnc,message);
//			clusterModels.add(clusterModel);
//
//		}
//
//		if (HaveOmitted) 
//			clusterTable.setMessage("*Value omitted from calculation of toxicity since prediction uncertainty was zero");
//
//		if (clusterModels.size()==0) return null;
//
//		return clusterTable;
//	}
//
//
//	public PredictionResults generatePredictionResultsLDA(DataForPredictionRun d,
//			TESTPredictedValue tpv,double predToxVal, double predToxUnc,String predMOA,double maxScore,
//			Vector<String>vecMOA,String[] predArrayMOA, String[] predArrayLC50,
//			Hashtable<String, AllResults> htAllResultsMOA,Hashtable<String, AllResults> htAllResultsLC50,TestChemical chemical,ReportOptions options,boolean createReports) {
//		// TODO Auto-generated method stub
//
//
//		try {
//
//			PredictionResults pr=new PredictionResults();
//
//			PredictionResultsPrimaryTable prpt=pr.getPredictionResultsPrimaryTable();
//			prpt.setExpMOA(d.er.expMOA);
//
//			if (Strings.isBlank(predMOA) || maxScore<0.5)
//				predMOA="N/A";
//			
//			prpt.setPredMOA(predMOA);
//			prpt.setMaxScoreMOA(d2.format(maxScore));
//						
//			setCommonValues(TESTConstants.ChoiceLDA, d, tpv, pr, predToxVal, predToxUnc,createReports);
//			
//			pr.setReportBase(options.reportBase);
//			
//			
//			
//			writeSortedMOATable(pr, vecMOA, predArrayMOA, predArrayLC50, 
//					predMOA, d.er,chemical,htAllResultsMOA,htAllResultsLC50);
//
//
//			return pr;
//
//		} catch (Exception ex) {
//			logger.catching(ex);
//			return null;
//		}
//	}
//
//	private void writeSortedMOATable(PredictionResults pr,			
//			Vector<String> vecMOA, String[] predArrayMOA, String[] predArrayLC50, 
//			String bestMOA, Lookup.ExpRecord er,TestChemical chemical,
//			Hashtable<String, AllResults> htAllResultsMOA,Hashtable<String, AllResults> htAllResultsLC50)
//			throws IOException {
//
//
//		MOATable mt=new MOATable();
//		pr.setMoaTable(mt);
//
//		Vector<MOAPrediction> MOAPredictions=new Vector<>();		
//		mt.setMOAPredictions(MOAPredictions);
//
//
//		Hashtable<Double, String> htMOA = new Hashtable<Double, String>();
//
//		// System.out.println(vecMOA.size());
//
//		for (int i = 0; i < vecMOA.size(); i++) {
//			java.util.LinkedList<String> l_MOA = ToxPredictor.Utilities.Utilities.Parse(predArrayMOA[i], "\t");
//			java.util.LinkedList<String> l_LC50 = ToxPredictor.Utilities.Utilities.Parse(predArrayLC50[i], "\t");
//
//			double MOAScore = Double.parseDouble(l_MOA.get(0));
//
//			// System.out.println(vecMOA.get(i)+"\t"+MOAScore);
//
//			htMOA.put(MOAScore, vecMOA.get(i));
//		}
//
//		Vector v = new Vector(htMOA.keySet());
//		Collections.sort(v, new ToxPredictor.Utilities.MyComparator());
//
//		Vector<String> vecMOA2 = new Vector<String>();
//		Enumeration e = v.elements();
//		while (e.hasMoreElements()) {
//			double key = (Double) e.nextElement();
//			vecMOA2.add(htMOA.get(key));
//			// System.out.println(key+"\t"+htMOA.get(key));
//		}
//
//
//		//		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
//		//
//		//		fw.write("<caption>Results for each mode of action</caption>\r\n");
//		//
//		//		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
//		//		fw.write("<th>Mode of action</th>\n");
//		//		fw.write("<th>LDA model score</th>\n");
//		//		fw.write("<th>LC50 predicted value<br>" + TESTConstants.getMolarLogUnits(endpoint) + "</th>\n");
//		//		fw.write("</tr>\n");
//
//
//
//		// System.out.println(vecMOA2.size());
//
//		// System.out.println("*"+er.expMOA);
//
//		for (int i = 0; i < vecMOA2.size(); i++) {
//			String MOAi = vecMOA2.get(i);
//
//			MOAPrediction mp=new MOAPrediction();
//			MOAPredictions.add(mp);
//
//			mp.setMOA(MOAi);
//
//
//			String predsMOA = predArrayMOA[vecMOA.indexOf(MOAi)];
//			String predsLC50 = predArrayLC50[vecMOA.indexOf(MOAi)];
//
//			LinkedList<String> l_MOA = Utilities.Parse(predsMOA, "\t");
//			LinkedList<String> l_LC50 = Utilities.Parse(predsLC50, "\t");
//
//			double MOAScore = Double.parseDouble(l_MOA.get(0));
//			double MOAUnc = Double.parseDouble(l_MOA.get(1));
//			String MOAError = l_MOA.get(2);
//
//			double LC50Score = Double.parseDouble(l_LC50.get(0));
//			double LC50Unc = Double.parseDouble(l_LC50.get(1));
//			String LC50Error = l_LC50.get(2);
//
//			
//			if (MOAi.equals(bestMOA) && MOAScore >= 0.5) {
//				if (er.expMOA.equals(bestMOA)) {
//					mp.setColor("#00CC33");//green
//					mp.setTag("Experimental & predicted MOA");
//					
//				} else if (er.expMOA.equals("N/A") || er.expMOA.equals("")) {// experimental
//					mp.setColor("#00CC33");//green
//					mp.setTag("Predicted MOA");
//				} else {// predicted doesnt match experimental MOA
//					mp.setTag("Predicted MOA");
//					mp.setColor("#FF9900");//orange
//				}
//
//			} else if (MOAi.equals(er.expMOA)) {
//				mp.setColor("#00CC33");//green
//				mp.setTag("Experimental MOA");
//			} 
//			
//			mp.setMOAScore(d2.format(MOAScore));
//			mp.setLC50Score(d2.format(LC50Score));
//						
//			if (!MOAError.equals("OK")) {
//				mp.setMOAScoreMsg(MOAError);
//			} else {
//				if (MOAScore < 0.5) {
//					mp.setMOAScoreMsg("score < 0.5");
//				}
//			}
//
//			if (pr.isCreateDetailedReport()) {
//
//				AllResults arLDA = htAllResultsMOA.get(MOAi);
//				OptimalResults orLDA = arLDA.getResults().get(0);
//				orLDA.calculatePredictedValues();
//				ClusterModel clusterModelMOA=createClusterModelLDA(pr,chemical,pr.getMethod(), orLDA,MOAScore,MOAUnc,MOAError,MOAi,"LDA");
//				mp.setClusterModelMOA(clusterModelMOA);
//			
//				AllResults arLC50 = htAllResultsLC50.get(MOAi);
//				OptimalResults orLC50 = arLC50.getResults().get(0);
//				orLC50.calculatePredictedValues();
//				ClusterModel clusterModelLC50=createClusterModelLDA(pr,chemical,pr.getMethod(), orLC50,LC50Score,LC50Unc,LC50Error,MOAi,"LC50");
//				mp.setClusterModelLC50(clusterModelLC50);
//
//			}
//			
//			//fw.write("<a href=\"ClusterFiles/Descriptors " + MOAi + " LDA.html\">");
//			//fw.write("<a href=\"ClusterFiles/Descriptors " + MOAi + " LC50.html\">");
//
//			if (!LC50Error.equals("OK")) {
//				mp.setLC50ScoreMsg(LC50Error);
//			}
//		}
//	}
//
//
}
//
//
//
