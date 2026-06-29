package gov.epa.endpoints.splittings;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.web_services.SplittingWebService;
import gov.epa.web_services.SplittingWebService.SplittingCalculationResponse;
import kong.unirest.Unirest;

public class Splitter {
	
	private SplittingWebService splittingWebService;
	private Splitting splitting;
	private String lanId;
	
	private SplittingServiceImpl splittingService = new SplittingServiceImpl();
	private DatasetServiceImpl datasetService = new DatasetServiceImpl();
//	private DescriptorValuesServiceImpl descriptorValuesService = new DescriptorValuesServiceImpl();
	private DataPointServiceImpl dataPointService = new DataPointServiceImpl();
	private static DataPointInSplittingServiceImpl dataPointInSplittingService = new DataPointInSplittingServiceImpl();
	
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
	
	
	
	private  Hashtable<Integer,List<String>> createSplitHashtable(List<String> ids, int numSplits) {
		Hashtable<Integer,List<String>>htSplits=new Hashtable<>();

		
		while (true) {
			for (int fold=1;fold<=numSplits;fold++) {

				if(htSplits.get(fold)==null) {
					List<String>ids_i=new ArrayList<>();
					htSplits.put(fold, ids_i);
					ids_i.add(ids.remove(0));
				} else {
					List<String>ids_i=htSplits.get(fold);
					ids_i.add(ids.remove(0));
				}
				
				if(ids.size()==0) {
					return htSplits;
				}

			}
		}
		
	}
	
	public void splitCV(String datasetName, int numSplits) {

		Dataset dataset=datasetService.findByName(datasetName);
		
//		System.out.println(countDPIS);

		String modelSplitting=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		Splitting splittingModel=splittingService.findByName(modelSplitting);
		
		List<String>ids=ModelData.getTrainingIds(dataset, splittingModel, false);
		Collections.shuffle(ids);
		
//		for (String id:ids) {
//			System.out.println(id);
//		}
//		System.out.println(idCount);
		
		Hashtable<Integer,List<String>>htSplits=createSplitHashtable(ids, numSplits);
		
		
		List<DataPoint> dataPoints = 
				dataPointService.findByDatasetName(dataset.getName());

		Map<String, DataPoint> dpMap = dataPoints.stream()
				.collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));

		for (int fold=1;fold<=numSplits;fold++) {
			
			List<String>idsTrain=new ArrayList<>();
			List<String>idsTest=new ArrayList<>();
			
			for (int i=1;i<=numSplits;i++) {
				List<String>idsFold=htSplits.get(i);
				
				if (i!=fold) {
					idsTrain.addAll(idsFold);						
				} else {
					idsTest.addAll(idsFold);
				}
			}
			
//			System.out.println(fold+"\t"+idsTrain.size()+"\t"+idsTest.size());
			
			Splitting splittingFold=splittingService.findByName(modelSplitting+"_CV"+fold);
			List<DataPointInSplitting> dpisTrain = createDPIS(dpMap, idsTrain, splittingFold,DevQsarConstants.TRAIN_SPLIT_NUM);
			List<DataPointInSplitting> dpisTest = createDPIS(dpMap, idsTest, splittingFold,DevQsarConstants.TEST_SPLIT_NUM);
			
//			System.out.println(fold+"\t"+dpisTrain.size()+"\t"+dpisTest.size());
		}
	}

	
	private List<DataPointInSplitting> createDPIS(Map<String, DataPoint> dpMap, List<String> idsSet,
			Splitting splittingFold, int splitNum) {
		List<DataPointInSplitting>dpisTrain=new ArrayList<>();
		for(String id:idsSet) {
			DataPointInSplitting dpis=new DataPointInSplitting(dpMap.get(id), splittingFold, splitNum, lanId);
			dpisTrain.add(dpis);
		}				
		dataPointInSplittingService.createSQL(dpisTrain);
		return dpisTrain;
	}

		
	public String split(String datasetName, String descriptorSetName, int n_threads) {
		System.out.println("Splitting " + datasetName);
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		
		System.out.println("Found " + dataPoints.size() + " data points");
		
		//Be sure to use smiles as the ID field when creating tsv or it cant create the splittings later by smiles
		String tsv = ModelData.generateInstancesWithoutSplitting(datasetName,descriptorSetName);
		
		System.out.println("tsv generated");
		
//		System.out.println(tsv);
//		
//		try {
//			FileWriter fw = new FileWriter("data/tempOverallSet.tsv");
//			fw.write(tsv);
//			fw.flush();
//			fw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
				
	    SplittingCalculationResponse[] splittingResponse = 
	            splittingWebService.callCalculation(tsv, false, n_threads).getBody();
	    
	    if (splittingResponse==null) {
	    	System.out.println("Splitting failed for "+datasetName);
	        return "splitting failed";
	    } else {
	    	System.out.println("Splitting succeeded");
//	    	if(true) return;
	    }
	    
	    int countTrain = 0;
	    int countTest = 0;
	    Map<String, DataPoint> dataPointsMap = dataPoints.stream().collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));
	    
	    List<DataPointInSplitting>dpisList=new ArrayList<>();
	    
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
//	            dataPointInSplittingService.create(dpis);	        	
	        	dpisList.add(dpis);	        	
	        } else {
	        	System.out.println("Cant create datapoint for smiles:"+smiles);
	        	return "Failed to create datapoint for smiles:"+smiles;
	        }
	    }
	    dataPointInSplittingService.createSQL(dpisList);
	    System.out.println("Training size: " + countTrain + ", test size:  " + countTest);
	    
	    return "splitting succeeded";
	}
	
	/**
	 * Just clones the REPRESENTATIVE_SPLIT (fk_splitting_id=1)
	 * @param datasetNameSrc
	 * @param datasetNameDest
	 * @param lanId
	 */
	public static void cloneSplit(String datasetNameSrc,String datasetNameDest,String lanId) {

		System.out.println("Splitting " + datasetNameDest);
	    
	    
	    String sqlDatasetSource="select id from qsar_datasets.datasets where name='"+datasetNameSrc+"';";
	    String sqlDatasetDest="select id from qsar_datasets.datasets where name='"+datasetNameDest+"';";
	    
	    String datasetIdSrc=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlDatasetSource);
	    String datasetIdDest=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlDatasetDest);
	    System.out.println(datasetIdSrc);
	    System.out.println(datasetIdDest);
	    
	    
	    String sqlDPIS="select dp.id, split_num,dpis.fk_splitting_id from  qsar_datasets.data_points dp\r\n"
	    		+ "	    join qsar_datasets.data_points dp2 on dp.canon_qsar_smiles=dp2.canon_qsar_smiles and dp2.fk_dataset_id="+datasetIdSrc+"\r\n"
	    		+ "	    join qsar_datasets.data_points_in_splittings dpis on dp2.id = dpis.fk_data_point_id \r\n"
	    		+ "	    where dp.fk_dataset_id="+datasetIdDest+" and dpis.fk_splitting_id=1;";

	    
//	    System.out.println(sqlDPIS);

	    ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDPIS);
	    	    
	    try {
	    	
		    int countTrain = 0;
		    int countTest = 0;

		    List<DataPointInSplitting>dpisList=new ArrayList<>();

			while (rs.next()) {
				DataPoint dp=new DataPoint();
				dp.setId(rs.getLong(1));
				
				int splitNum=rs.getInt(2);
				
				if(splitNum==0) countTrain++;
				if(splitNum==1) countTest++;
				
				Splitting splitting=new Splitting();
				splitting.setId(rs.getLong(3));
				
				DataPointInSplitting dpis = new DataPointInSplitting(dp, splitting, splitNum, lanId);
        	
				dpisList.add(dpis);
			}
			
		    System.out.println("Training size: " + countTrain + ", test size:  " + countTest);
		
		    dataPointInSplittingService.createSQL(dpisList);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	    
	}
	
	
	public static void cloneAllSplits(String datasetNameSrc, String datasetNameDest, String lanId, boolean writeToDB) {

	    System.out.println("Splitting " + datasetNameDest);

	    String sqlDatasetSource = "select id from qsar_datasets.datasets where name='" + datasetNameSrc + "';";
	    String sqlDatasetDest   = "select id from qsar_datasets.datasets where name='" + datasetNameDest + "';";

	    String datasetIdSrc  = SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlDatasetSource);
	    String datasetIdDest = SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlDatasetDest);
	    System.out.println("Source dataset id: " + datasetIdSrc);
	    System.out.println("Dest dataset id: " + datasetIdDest);

	    // Clone ALL splits from source to destination for matching SMILES.
	    // Skip rows that already exist in destination (avoid duplicates).
	    String sqlDPIS =
	        "select dp_dest.id as dest_dp_id, dpis_src.split_num, dpis_src.fk_splitting_id " +
	        "from qsar_datasets.data_points dp_dest " +
	        "join qsar_datasets.data_points dp_src " +
	        "  on dp_dest.canon_qsar_smiles = dp_src.canon_qsar_smiles " +
	        " and dp_src.fk_dataset_id = " + datasetIdSrc + " " +
	        "join qsar_datasets.data_points_in_splittings dpis_src " +
	        "  on dp_src.id = dpis_src.fk_data_point_id " +
	        "left join qsar_datasets.data_points_in_splittings dpis_dest " +
	        "  on dpis_dest.fk_data_point_id = dp_dest.id " +
	        " and dpis_dest.fk_splitting_id = dpis_src.fk_splitting_id " +
	        "where dp_dest.fk_dataset_id = " + datasetIdDest + " " +
	        "  and dpis_dest.fk_data_point_id is null " + // only new pairs
	        "order by dpis_src.fk_splitting_id, dp_dest.id;";
	    
	    System.out.println(sqlDPIS);
	    

	    ResultSet rs = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDPIS);

	    try {
	        List<DataPointInSplitting> dpisList = new ArrayList<>();

	        // Track counts per splitting id: [0] = train (split_num==0), [1] = test (split_num==1)
	        Map<Long, int[]> countsBySplitId = new LinkedHashMap<>();

	        while (rs.next()) {
	            long destDpId      = rs.getLong(1);
	            int splitNum       = rs.getInt(2);
	            long splittingId   = rs.getLong(3);

	            // Build objects for insertion
	            DataPoint dp = new DataPoint();
	            dp.setId(destDpId);

	            Splitting splitting = new Splitting();
	            splitting.setId(splittingId);

	            DataPointInSplitting dpis = new DataPointInSplitting(dp, splitting, splitNum, lanId);
	            dpisList.add(dpis);

	            // Count by splitting id
	            int[] cnt = countsBySplitId.computeIfAbsent(splittingId, k -> new int[2]);
	            if (splitNum == 0) cnt[0]++; // train
	            if (splitNum == 1) cnt[1]++; // test
	        }

	        // Report counts
	        if (countsBySplitId.isEmpty()) {
	            System.out.println("No new split assignments to clone (all up to date or no matches).");
	        } else {
	            for (Map.Entry<Long, int[]> e : countsBySplitId.entrySet()) {
	                long splittingId = e.getKey();
	                int[] cnt = e.getValue();
	                System.out.println("Splitting " + splittingId + " -> Training size: " + cnt[0] + ", test size: " + cnt[1]);
	            }
	        }

	        // Bulk insert
	        if (!dpisList.isEmpty() && writeToDB) {
	            dataPointInSplittingService.createSQL(dpisList);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
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
