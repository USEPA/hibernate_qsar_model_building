package gov.epa.endpoints.splittings;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.web_services.SplittingWebService;
import gov.epa.web_services.SplittingWebService.SplittingCalculationResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class Splitter {
	
	private SplittingWebService splittingWebService;
	private Splitting splitting;
	private String lanId;
	
	private SplittingService splittingService;
	private DataPointInSplittingService dataPointInSplittingService;
	
	private static Logger logger = LogManager.getLogger(Splitter.class);
	
	public Splitter(SplittingWebService splittingWebService, String lanId) {
		// Set logging providers for Hibernate and MChange
		System.setProperty("org.jboss.logging.provider", "log4j");
		System.setProperty("com.mchange.v2.log.MLog", "log4j");
		
		// Reduce logging output from Apache, Hibernate, and C3P0
		String[] loggerNames = {"org.apache.http", "org.hibernate", "com.mchange"};
		for (String loggerName:loggerNames) {
			Logger thisLogger = LogManager.getLogger(loggerName);
			thisLogger.setLevel(Level.WARN);
		}
		
		// Make sure Unirest is configured
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
			logger.debug("Unirest already configured, ignoring");
		}
		
		this.splittingWebService = splittingWebService;
		this.lanId = lanId;
		
		this.splittingService = new SplittingServiceImpl();
		this.dataPointInSplittingService = new DataPointInSplittingServiceImpl();
		
        Splitting splitting = splittingService.findByName(splittingWebService.splittingName);
        if (splitting==null) {
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
	
	public void split(String datasetName, String descriptorSetName) {
		String tsv = InstanceUtil.generateTsv(datasetName, descriptorSetName);
		
	    SplittingCalculationResponse[] splittingResponse = 
	            splittingWebService.callCalculation(tsv, false).getBody();
	    
	    if (splittingResponse==null) {
	    	System.out.println("Splitting failed");
	        return;
	    }
	    
	    HashMap<String, DataPoint> dataPointsMap = InstanceUtil.getDataPoints(datasetName);
	    for (SplittingCalculationResponse split:splittingResponse) {
	        String smiles = split.ID;
	        Integer splitNum = null;
            if (split.t_p.equals("t")) {
                splitNum = DevQsarConstants.TRAIN_SPLIT_NUM;
            } else if (split.t_p.equals("p")) {
                splitNum = DevQsarConstants.PREDICT_SPLIT_NUM;
            }
	        
	        DataPoint dp = dataPointsMap.get(smiles);
	        if (dp!=null && splitNum!=null) {
	            DataPointInSplitting dpis = new DataPointInSplitting(dp, splitting, splitNum, lanId);
	            dataPointInSplittingService.create(dpis);
	        }
	    }
	}
	
	public static void main(String[] args) {
		String lanId = "gsincl01";
		String datasetName = "GFBS_HLC_StartToFinish_122221_1";
		String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_TEST;
		
		SplittingWebService splittingWebService = new SplittingWebService(DevQsarConstants.SERVER_LOCAL, 4999, 
				DevQsarConstants.SPLITTING_RND_REPRESENTATIVE, 2);
		Splitter splitter = new Splitter(splittingWebService, lanId);
		splitter.split(datasetName, descriptorSetName);
	}

}
