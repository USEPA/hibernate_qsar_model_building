package gov.epa.endpoints.splittings;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.web_services.SplittingWebService;
import gov.epa.web_services.SplittingWebService.SplittingCalculationResponse;
import kong.unirest.Unirest;

public class Splitter {
	
	private SplittingWebService splittingWebService;
	private Splitting splitting;
	private String lanId;
	
	private SplittingService splittingService = new SplittingServiceImpl();
	private DatasetService datasetService = new DatasetServiceImpl();
	private DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
	private DataPointService dataPointService = new DataPointServiceImpl();
	private DataPointInSplittingService dataPointInSplittingService = new DataPointInSplittingServiceImpl();
	
	public Splitter(SplittingWebService splittingWebService, String lanId) {
		// Set logging providers for Hibernate and MChange
		System.setProperty("org.jboss.logging.provider", "log4j");
		System.setProperty("com.mchange.v2.log.MLog", "log4j");
		
		// Reduce logging output from Apache, Hibernate, and C3P0
//		String[] loggerNames = {"org.apache.http", "org.hibernate", "com.mchange"};
//		for (String loggerName:loggerNames) {
//			Logger thisLogger = LogManager.getLogger(loggerName);
//			thisLogger.setLevel(Level.ERROR);
//		}
		
		// Make sure Unirest is configured
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
//			logger.debug("Unirest already configured, ignoring");
		}
		
		this.splittingWebService = splittingWebService;
		this.lanId = lanId;
		
        this.splitting = splittingService.findByName(splittingWebService.splittingName);
        if (this.splitting==null) {
            this.splitting = new Splitting(splittingWebService.splittingName, 
                    splittingWebService.splittingName, 
                    splittingWebService.numSplits,
                    lanId);
            splittingService.create(this.splitting);
        }
        
        if (this.splitting==null) {
            throw new IllegalArgumentException("Failed splitting creation");
        }
	}
	
	public void split(Long datasetId, String descriptorSetName, int n_threads) {
		Dataset dataset = datasetService.findById(datasetId);
		split(dataset.getName(), descriptorSetName,n_threads);
	}
	
	public void split(String datasetName, String descriptorSetName, int n_threads) {
		System.out.println("Splitting " + datasetName);
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		
		System.out.println("Found " + dataPoints.size() + " data points");
		
		
		String tsv = ModelData.generateInstancesWithoutSplitting(datasetName,descriptorSetName,true);
		
//		System.out.println(tsv);
		
//		try {
//			FileWriter fw = new FileWriter("C:\\Users\\TMARTI02\\Documents\\0 python\\pf_python_modelbuilding\\datasets\\HLC from exp_prop and chemprop\\bob.tsv");
//			fw.write(tsv);
//			fw.flush();
//			fw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		

//	    String splittingResponse = 
//	            splittingWebService.callCalculation(tsv, false, n_threads).getBody();

	    SplittingCalculationResponse[] splittingResponse = 
	            splittingWebService.callCalculation(tsv, false, n_threads).getBody();
	    
	    if (splittingResponse==null) {
	    	System.out.println("Splitting failed");
	        return;
	    } else {
	    	System.out.println(splittingResponse);
	    	if(true) return;
	    }
	    
	    int countTrain = 0;
	    int countTest = 0;
	    Map<String, DataPoint> dataPointsMap = dataPoints.stream().collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));
	    for (SplittingCalculationResponse split:splittingResponse) {
	        String smiles = split.ID;
	        Integer splitNum = null;
            if (split.t_p.equals("t")) {
                splitNum = DevQsarConstants.TRAIN_SPLIT_NUM;
                countTrain++;
            } else if (split.t_p.equals("p")) {
                splitNum = DevQsarConstants.TEST_SPLIT_NUM;
                countTest++;
            }
	        
	        DataPoint dp = dataPointsMap.get(smiles);
	        if (dp!=null && splitNum!=null) {
	            DataPointInSplitting dpis = new DataPointInSplitting(dp, splitting, splitNum, lanId);
	            dataPointInSplittingService.create(dpis);
	        }
	    }
	    
	    System.out.println("Training size: " + countTrain + ", test size:  " + countTest);
	}
	
	public void unsplit(String datasetName) {
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(datasetName, splittingWebService.splittingName);
		for (DataPointInSplitting dpis:dataPointsInSplitting) {
			dataPointInSplittingService.delete(dpis);
		}
	}
	
	public void unsplit(Long datasetId) {
		Dataset dataset = datasetService.findById(datasetId);
		unsplit(dataset.getName());
	}
	
	public static void main(String[] args) {
		String lanId = "cramslan";
		String descriptorSetName = "WebTEST-default";
		String datasetName = "ExpProp BCF Fish WholeBody zeros omitted";
		
		SplittingWebService splittingWebService = new SplittingWebService(DevQsarConstants.SERVER_LOCAL, 4999, 
				DevQsarConstants.SPLITTING_RND_REPRESENTATIVE, 2);
		Splitter splitter = new Splitter(splittingWebService, lanId);
		
		splitter.split(datasetName, descriptorSetName, 4);
		for (Long l = 62L; l <= 62L; l++) {
//			splitter.unsplit(l);
		}
	}

}
