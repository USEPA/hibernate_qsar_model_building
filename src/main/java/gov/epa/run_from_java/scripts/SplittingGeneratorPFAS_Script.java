package gov.epa.run_from_java.scripts;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.ChemicalList;
import gov.epa.databases.dsstox.service.ChemicalListService;
import gov.epa.databases.dsstox.service.ChemicalListServiceImpl;
import gov.epa.databases.dsstox.service.SourceSubstanceService;
import gov.epa.databases.dsstox.service.SourceSubstanceServiceImpl;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer.StandardizeResult;
import gov.epa.web_services.standardizers.Standardizer;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponse;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponseWithStatus;
import kong.unirest.Unirest;
import javax.validation.Validator;

public class SplittingGeneratorPFAS_Script {

	public static final String splittingPFASOnly="T=PFAS only, P=PFAS";
	public static final String splittingAll="T=all, P=PFAS";		
	public static final String splittingAllButPFAS="T=all but PFAS, P=PFAS";		

	ChemicalListService chemicalListService = new ChemicalListServiceImpl();
	SourceSubstanceService sourceSubstanceService = new SourceSubstanceServiceImpl();
	SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY,"qsar-ready","https://hcd.rtpnc.epa.gov");
	DataPointInSplittingService dataPointInSplittingService = new DataPointInSplittingServiceImpl();
	CompoundService compoundService = new CompoundServiceImpl();
//	Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
	Validator validator = DevQsarValidator.getValidator();
	
	public SplittingGeneratorPFAS_Script() {
//		System.setProperty("org.jboss.logging.provider", "log4j");
//		System.setProperty("com.mchange.v2.log.MLog", "log4j");
//		
//		// Make sure Unirest is configured
//		try {
//			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
//		} catch (Exception e) {
////			logger.debug("Unirest already configured, ignoring");
//		}
	}
	void generateQSAR_ReadyPFAS_STRUCT (String listName,String filepathOutput) {
		

		ChemicalList chemicalList = chemicalListService.findByName(listName);
		
		if (chemicalList==null) {
			System.out.println("Cant access list="+listName);
			return;
		}

		List<DsstoxRecord> dsstoxRecords = sourceSubstanceService.findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(listName);
		
		System.out.println(dsstoxRecords.size());
		
		try {
			
			FileWriter fw=new FileWriter(filepathOutput);
			fw.write("DTXCID\tstandardizedSmiles\r\n");
			
			int counter=0;
			for (DsstoxRecord dr:dsstoxRecords) {
				counter++;
//				System.out.println(dr.dsstoxCompoundId+"\t"+dr.smiles+"\t"+dr.qsarReadySmiles);
				String standardizedSmiles=standardize(dr,standardizer,counter);
				fw.write(dr.getDsstoxCompoundId()+"\t"+standardizedSmiles+"\r\n");
				fw.flush();				
			}
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	/**
	 * Creating a local copy of one of Gabriel's methods because i needed to turn of refresh to get it to not hang up after 1 chemical
	 * @param dpis
	 * @return
	 * @throws ConstraintViolationException
	 */
	public DataPointInSplitting create(DataPointInSplitting dpis) throws ConstraintViolationException {
	
		Set<ConstraintViolation<DataPointInSplitting>> violations = validator.validate(dpis);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		Transaction t = session.beginTransaction();
		
		try {
			session.save(dpis);
			
//			session.flush();
//			session.refresh(dpis);//need to turn this off or it hangs!
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return dpis;
	}
	
	/**
	 * Todo this needs to be fixed to use code in datasetcreator for standardization
	 * 
	 * @param dr
	 * @param standardizer
	 * @param counter
	 * @return
	 */
	private String standardize(DsstoxRecord dr,SciDataExpertsStandardizer standardizer, int counter) {

		//		System.out.println(dr.dsstoxCompoundId+"\t"+standardizer.standardizerName);
		Compound compound = compoundService.findByDtxcidSmilesAndStandardizer(dr.dsstoxCompoundId, dr.smiles,standardizer.standardizerName);

		if (compound!=null) {
			// If already standardized, set standardization and mark record as standardized
			// Additionally store mapped compound so we don't have to query it later
			System.out.println(counter+"\t"+dr.dsstoxCompoundId+"\tAlready have qsar ready smiles="+compound.getCanonQsarSmiles());
			return compound.getCanonQsarSmiles();
		} else {
			//			System.out.println(dr.dsstoxCompoundId+"\tNeed to standardize="+dr.smiles);

			StandardizeResult standardizeResult = standardizer.runStandardize(dr.smiles,false);
			
			if (standardizeResult.status==200) {
					
				compound = new Compound(dr.dsstoxCompoundId, dr.smiles, standardizeResult.qsarReadySmiles, standardizer.standardizerName, "tmarti02");
				System.out.println(counter+"\t"+dr.dsstoxCompoundId+"\tSDE qsar ready smiles="+compound.getCanonQsarSmiles());

				try {
					compoundService.create(compound);
					return standardizeResult.qsarReadySmiles;
				} catch (ConstraintViolationException e) {
					System.out.println(e.getMessage());
					return "error: constraint violation";
				}

			} else {
				System.out.println(": Standardizer HTTP response failed for SMILES: "
						+ dr.smiles + " with code " + standardizeResult.status);
				return "error: "+standardizeResult.status;
			}

		}
	}
	
	 void createSplitting(String datasetName, String splittingName, HashSet<String>smilesArray) {
		String lanid="tmarti02";
		
		
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(datasetName, "RND_REPRESENTATIVE");
		
		SplittingService splittingService = new SplittingServiceImpl();
		
		Splitting splitting = splittingService.findByName(splittingName);
		
		if(splitting==null) {
			System.out.println(splittingName+" is null, creating...");
			if(splittingName.equals(splittingPFASOnly)) {
				splitting=new Splitting(splittingName,"train using only PFAS",2,lanid);
			} else if (splittingName.equals(splittingAllButPFAS)) {
				splitting=new Splitting(splittingName,"train using all but PFAS",2,lanid);
			} else {
				System.out.println("Invalid splittingName:"+splittingName);
				return;
			}
			splitting=splittingService.create(splitting);
			System.out.println(splittingName+" created");
		} 
		
		System.out.println(splitting.getId()+"\t"+splitting.getDescription());
		
		List<DataPointInSplitting>dpisList=new ArrayList<>();
		
		
		for (DataPointInSplitting dpis:dataPointsInSplitting) {
			DataPointInSplitting dpisNew = new DataPointInSplitting(dpis.getDataPoint(), splitting, dpis.getSplitNum(), lanid);
			
			if (dpis.getSplitNum()==DevQsarConstants.TRAIN_SPLIT_NUM) {

				if (splittingName.equals(splittingAll)) {
					//Leave splitNum as is
				} else if (splittingName.equals(splittingPFASOnly)) {					
					if (!isPFAS(smilesArray,dpis)) 
						dpisNew.setSplitNum(2);
				} else if (splittingName.equals(splittingAllButPFAS)) {
					if (isPFAS(smilesArray,dpis)) 						
						dpisNew.setSplitNum(2);
				} else {
					dpisNew.setSplitNum(2);
				}
			
			} else if (dpis.getSplitNum()==DevQsarConstants.TEST_SPLIT_NUM) {
				if (!isPFAS(smilesArray,dpis)) 
					dpisNew.setSplitNum(2);//dont use				
			}
			
			//Only add ones that are in training and test set (ignore ones with SplitNum=2):
			//TODO- does this break report generation?
			
			if (dpisNew.getSplitNum()!=2)
				dpisList.add(dpisNew);
			
		}
		
		DataPointInSplittingServiceImpl dpisService=new DataPointInSplittingServiceImpl();
		dpisService.createSQL(dpisList);
		
		
//		int counter=0;
//		for (DataPointInSplitting dpis:dataPointsInSplitting) {
//			DataPointInSplitting dpisNew = new DataPointInSplitting(dpis.getDataPoint(), splitting, dpis.getSplitNum(), lanid);
//						
//			if (dpis.getSplitNum()==DevQsarConstants.TRAIN_SPLIT_NUM) {
//				if (splittingName.equals(splittingAll)) {
//					counter=createDataPointInSplitting(dpisNew,counter);
//				} else if (splittingName.equals(splittingPFASOnly)) {					
//					if (isPFAS(smilesArray,dpis)) {						
////						System.out.println(dpis.getDataPoint().getId()+"\t"+dpis.getDataPoint().getCanonQsarSmiles()+"\t"+dpis.getSplitNum()+"\t"+dpis.getSplitting().getName());
//						counter=createDataPointInSplitting(dpisNew,counter);					
//					} else {						
//						dpisNew.setSplitNum(2);
//						counter=createDataPointInSplitting(dpisNew,counter);		
//					}
//				} else if (splittingName.equals(splittingAllButPFAS)) {
//					if (!isPFAS(smilesArray,dpis)) {						
////						System.out.println(dpis.getDataPoint().getCanonQsarSmiles()+"\t"+dpis.getSplitNum()+"\t"+dpis.getSplitting().getName());
//						counter=createDataPointInSplitting(dpisNew,counter);		
//					} else {
//						dpisNew.setSplitNum(2);
//						counter=createDataPointInSplitting(dpisNew,counter);		
//					}
//				} else {
//					dpisNew.setSplitNum(2);
//					counter=createDataPointInSplitting(dpisNew,counter);
//				}
//			}else if (dpis.getSplitNum()==DevQsarConstants.TEST_SPLIT_NUM) {
//				if (isPFAS(smilesArray,dpis)) {
////					System.out.println(dpis.getDataPoint().getCanonQsarSmiles()+"\t"+dpis.getSplitNum()+"\t"+dpis.getSplitting().getName());
//					counter=createDataPointInSplitting(dpisNew,counter);
//				} else {
//					dpisNew.setSplitNum(2);//dont use
//					counter=createDataPointInSplitting(dpisNew,counter);					
//				}
//			}
//			
//			if(counter%1000==0)
//				System.out.println(counter);
//			
//		}
//		session.close();
	}
	
	
	 int createDataPointInSplitting (DataPointInSplitting dpisNew, int counter) {
		try {
//			dataPointInSplittingService.create(dpisNew);
			create(dpisNew);
			return counter+1;
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			System.out.println(e.getMessage());
			return counter;
		}		
	}
	
	 boolean isPFAS(HashSet<String>smilesArray,DataPointInSplitting dpis) {
		return smilesArray.contains(dpis.getDataPoint().getCanonQsarSmiles());
	}
	
	

	public static HashSet<String> getPFASSmiles(String filepath) {

		try {
			
			List<String> Lines = Files.readAllLines(Paths.get(filepath));
		
			HashSet<String>smilesArray=new HashSet<>();
			
			for (String Line:Lines) {
				String [] values=Line.split("\t");
				String smiles=values[1];
				
				if (!smiles.contains("error") && !smiles.isBlank() && smiles.contains("F")) {
					smilesArray.add(smiles);
				}
			}
			return smilesArray;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	

	void createFiveFoldExternalSplittings(String folder,String datasetName,String descriptorSet, ArrayList<String>smilesArrayPFAS) {
		
		try {
			String filepath=folder+datasetName+"_"+descriptorSet+".tsv";
			
			List<String> Lines = Files.readAllLines(Paths.get(filepath));
		
			String header=Lines.remove(0);
			
			ArrayList<String>linesPFAS=new ArrayList<>();
			ArrayList<String>linesNonPFAS=new ArrayList<>();
			
			for (String Line:Lines) {
				String smiles=Line.substring(0,Line.indexOf("\t"));
				if (smilesArrayPFAS.contains(smiles)) linesPFAS.add(Line);
				else linesNonPFAS.add(Line);
//				System.out.println(smiles);
			}
			
			Random r=new Random();
			r.setSeed(42L);
			
			Collections.shuffle(linesNonPFAS,r);

			ArrayList<String>[] alPFAS=get5FoldArray(linesPFAS, r);
			ArrayList<String>[] alNonPFAS=get5FoldArray(linesNonPFAS, r);
			
			for (int i=0;i<=4;i++) {
				FileWriter fwPred=new FileWriter(folder+datasetName+"_"+descriptorSet+"_prediction_PFAS"+(i+1)+".tsv");
				FileWriter fwTrainPFASOnly=new FileWriter(folder+datasetName+"_"+descriptorSet+"_training_PFAS"+(i+1)+".tsv");
				FileWriter fwTrainAll=new FileWriter(folder+datasetName+"_"+descriptorSet+"_training_All"+(i+1)+".tsv");
				FileWriter fwTrainAllButPFAS=new FileWriter(folder+datasetName+"_"+descriptorSet+"_training_All_but_PFAS"+(i+1)+".tsv");

				fwPred.write(header+"\r\n");
				fwTrainAll.write(header+"\r\n");
				fwTrainAllButPFAS.write(header+"\r\n");
				fwTrainPFASOnly.write(header+"\r\n");

				
				for (int j=0;j<alPFAS[i].size();j++) {
					fwPred.write(alPFAS[i].get(j)+"\r\n");
				}				
				
				for (int k=0;k<=4;k++) {
					if (k==i) continue;
					
					for (int j=0;j<alPFAS[k].size();j++) {
						fwTrainPFASOnly.write(alPFAS[k].get(j)+"\r\n");
						fwTrainAll.write(alPFAS[k].get(j)+"\r\n");
					}
					
					for (int j=0;j<alNonPFAS[k].size();j++) {
						fwTrainAll.write(alNonPFAS[k].get(j)+"\r\n");
						fwTrainAllButPFAS.write(alNonPFAS[k].get(j)+"\r\n");
					}					
				}

			
				fwTrainAll.flush();
				fwTrainAll.close();

				fwTrainAllButPFAS.flush();
				fwTrainAllButPFAS.close();

				fwTrainPFASOnly.flush();
				fwTrainPFASOnly.close();
				
				fwPred.flush();
				fwPred.close();

			}
			
		} catch (Exception ex) {
			ex.printStackTrace();			
		}
		
		
		
	}
	private ArrayList<String>[] get5FoldArray(ArrayList<String> lines, Random r) {
		ArrayList<String>[] al = new ArrayList[5];

		Collections.shuffle(lines,r);
		int countFold=(int)Math.ceil((double)lines.size()/5.0);
		int fold=0;
		
		while (true) {
			if (al[fold]==null) al[fold]=new ArrayList<String>();
			al[fold].add(lines.remove(0));				
			if (al[fold].size()==countFold && fold!=4) {
				fold++;
			}
			
			if (lines.size()==0) break;
		}
		
		for (int i=0;i<=4;i++) {
			System.out.println(al[i].size());
		}
		return al;
	}

	void getPFASChemicalCountForDataSet(String datasetName,ArrayList<String>smilesArray) {
		
		DataPointService dataPointService = new DataPointServiceImpl();
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);		
		int count=0;		
		for (DataPoint dp:dataPoints) {
			if (smilesArray.contains(dp.getCanonQsarSmiles()))count++;
		}		
		System.out.println(datasetName+"\t"+count);

	}
	
	public static int getCount(Connection conn, String datasetName,String splitting, int splitNum) {
		
		String sql="select count(dp.id) from qsar_datasets.data_points dp\n"+  
		"inner join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id = dp.id\n"+
		"join qsar_datasets.datasets d on dp.fk_dataset_id =d.id\n"+
		"join qsar_datasets.splittings s on s.id = dpis.fk_splitting_id\n"+ 
		"where d.\"name\"='"+datasetName.replace("'","''")+"'\n"
		+ "and s.\"name\"='"+splitting+"' and dpis.split_num = "+splitNum+";";
		
		//TODO add descriptorSetName because can have null descriptors...
		
//		System.out.println(sql+"\n");
		
		return Integer.parseInt(DatabaseLookup.runSQL(conn, sql));

	}
	
	
	void createSplittings () {
		List<String>datasetNames=new ArrayList<>();
//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop");
//		datasetNames.add("ExpProp BCF Fish_TMM");
//		datasetNames.add("pKa_a from exp_prop and chemprop");
//		datasetNames.add("pKa_b from exp_prop and chemprop");
		
//		datasetNames.add("WS from exp_prop and chemprop v2");
//		datasetNames.add("BP from exp_prop and chemprop v3");
//		datasetNames.add("MP from exp_prop and chemprop v2");
		
//		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");
		
//		datasetNames.add("HLC v1 modeling");
//		datasetNames.add("WS v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("LogP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("MP v1 modeling");
		
		Connection conn = SqlUtilities.getConnectionPostgres();
		
//		for (String datasetName : datasetNames) {
//			p.getPFASChemicalCountForDataSet(datasetName, smilesArray);
//		}

		List<String>splittingNames=new ArrayList<>();
		splittingNames.add(splittingPFASOnly);
//		splittingNames.add(splittingAll);//Dont need since we use RND_REPRESENTATIVE split and limit to PFAS results later
		splittingNames.add(splittingAllButPFAS);

		String listName="PFASSTRUCTV4";		
//		String listName="PFASSTRUCTV5";
		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";
		
//		p.generateQSAR_ReadyPFAS_STRUCT(listName,filePath);		
		
		HashSet<String>smilesArray=getPFASSmiles(filePath);
		
		for (String datasetName : datasetNames) {
			for (String splittingName : splittingNames) {
				
				createSplitting(datasetName,splittingName,smilesArray);	
				int countTraining = getCount(conn, datasetName, splittingName, 0);
				int countPrediction = getCount(conn, datasetName, splittingName, 1);
				System.out.println(datasetName+"\t"+splittingName+"\t"+countTraining + "\t" + countPrediction);
			}
		}
	}
	
	private void write_exp_prop_datasets() {
		String lanId = "tmarti02";
		
		String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		List<String>datasetNames=new ArrayList<>();
//		datasetNames.add("HLC from exp_prop and chemprop");		
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop");
//		datasetNames.add("ExpProp BCF Fish_TMM");
		
		datasetNames.add("HLC v1 res_qsar");
//		datasetNames.add("WS v1 res_qsar");
		datasetNames.add("VP v1 res_qsar");
		datasetNames.add("LogP v1 res_qsar");
		datasetNames.add("BP v1 res_qsar");
		datasetNames.add("MP v1 res_qsar");

		
		String folderMain="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\modeling services\\pf_python_modelbuilding\\datasets\\";

		List<String>splittingNames=new ArrayList<>();
		splittingNames.add(splittingPFASOnly);
		splittingNames.add(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE);
		splittingNames.add(splittingAllButPFAS);
		
		for (String datasetName : datasetNames) {
			for (String splittingName : splittingNames) {				
				System.out.println(datasetName+"\t"+splittingName);
				String folder=folderMain+datasetName+"\\PFAS\\";
				DatasetFileWriter.writeWithSplitting(descriptorSetName, splittingName, datasetName, folder,true,false);				
			}
		}
		
	}
	
	void deleteSplittings() {
		
//		String dataSetName= "pKa_a from exp_prop and chemprop";
		
		
//		String dataSetName= "pKa_b from exp_prop and chemprop";
//		String splittingName=splittingAll;
		String splittingName=splittingAllButPFAS;
//		String splittingName=splittingPFASOnly;
//		String splittingName=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
		
		DataPointInSplittingServiceImpl dpisService=new DataPointInSplittingServiceImpl();

		
		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");

		for(String dataSetName:datasetNames) {
			
			List<DataPointInSplitting>dpisList=dpisService.findByDatasetNameAndSplittingName(dataSetName, splittingName);

			System.out.println(dpisList.size());
			
			for (DataPointInSplitting dpis:dpisList) {
				
				System.out.println(dpis.getDataPoint().getCanonQsarSmiles()+"\t"+dpis.getSplitNum());
				//dpisService.delete(dpis);
				
				
			}

		}
		

		
	}
	void getCounts() {
		List<String>datasetNames=new ArrayList<>();

//		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop v2");
		
//		datasetNames.add("WS from exp_prop and chemprop v2");
//		datasetNames.add("BP from exp_prop and chemprop v3");
//		datasetNames.add("MP from exp_prop and chemprop v2");

//		datasetNames.add("HLC v1");
//		datasetNames.add("VP v1");
//		datasetNames.add("WS v1");
//		datasetNames.add("BP v1");
//		datasetNames.add("LogP v1");
//		datasetNames.add("MP v1");
		
		datasetNames.add("HLC v1 modeling");
		datasetNames.add("VP v1 modeling");
		datasetNames.add("BP v1 modeling");
		datasetNames.add("WS v1 modeling");
		datasetNames.add("LogP v1 modeling");
		datasetNames.add("MP v1 modeling");

		
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		List<String>splittings=new ArrayList<>();
		splittings.add(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE);
		splittings.add(splittingPFASOnly);
		splittings.add(splittingAllButPFAS);
		
		
//		String splittingName=splittingPFASOnly;
//		String splittingName=DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		String splittingName=splittingAllButPFAS;

		for (String splitting:splittings) {
			for (String datasetName:datasetNames) {			
				int countTR=getCount(conn, datasetName, splitting, 0);
				int countTest=getCount(conn, datasetName, splitting, 1);
				System.out.println(datasetName+"\t"+countTR+"\t"+countTest);
			}
			System.out.println("");
		}
		
	}
	
	
	public static void main(String[] args) {
		SplittingGeneratorPFAS_Script p=new SplittingGeneratorPFAS_Script();
		p.getCounts();
//		p.createSplittings();
//		p.deleteSplittings();
//		p.write_exp_prop_datasets();		
//		p.createFiveFoldExternalSplittings(folder, datasetName,"T.E.S.T. 5.1", smilesArray);
		
	}
	
	

}

