package gov.epa.run_from_java.scripts;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
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
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import gov.epa.web_services.standardizers.Standardizer;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponse;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponseWithStatus;
import kong.unirest.Unirest;
import javax.validation.Validator;

public class PFAS_SplittingGenerator {

	public static final String splittingPFASOnly="T=PFAS only, P=PFAS";
	public static final String splittingAll="T=all, P=PFAS";		
	public static final String splittingAllButPFAS="T=all but PFAS, P=PFAS";		

	ChemicalListService chemicalListService = new ChemicalListServiceImpl();
	SourceSubstanceService sourceSubstanceService = new SourceSubstanceServiceImpl();
	SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(DevQsarConstants.QSAR_READY);
	DataPointInSplittingService dataPointInSplittingService = new DataPointInSplittingServiceImpl();
	CompoundService compoundService = new CompoundServiceImpl();
//	Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
	Validator validator = DevQsarValidator.getValidator();
	
	public PFAS_SplittingGenerator() {
		System.setProperty("org.jboss.logging.provider", "log4j");
		System.setProperty("com.mchange.v2.log.MLog", "log4j");
		
		// Make sure Unirest is configured
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
//			logger.debug("Unirest already configured, ignoring");
		}
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
			for (DsstoxRecord dr:dsstoxRecords) {
//				System.out.println(dr.dsstoxCompoundId+"\t"+dr.smiles+"\t"+dr.qsarReadySmiles);
				String standardizedSmiles=standardize(dr,standardizer);
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
			session.flush();
//			session.refresh(dpis);//need to turn this off or it hangs!
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return dpis;
	}
	
	private String standardize(DsstoxRecord dr,Standardizer standardizer) {

		//		System.out.println(dr.dsstoxCompoundId+"\t"+standardizer.standardizerName);
		Compound compound = compoundService.findByDtxcidAndStandardizer(dr.dsstoxCompoundId, standardizer.standardizerName);

		if (compound!=null) {
			// If already standardized, set standardization and mark record as standardized
			// Additionally store mapped compound so we don't have to query it later
			System.out.println(dr.dsstoxCompoundId+"\tAlready have qsar ready smiles="+compound.getCanonQsarSmiles());
			return compound.getCanonQsarSmiles();
		} else {
			//			System.out.println(dr.dsstoxCompoundId+"\tNeed to standardize="+dr.smiles);

			StandardizeResponseWithStatus standardizeResponse = standardizer.callStandardize(dr.smiles);
			if (standardizeResponse.status==200) {
				StandardizeResponse standardizeResponseData = standardizeResponse.standardizeResponse;

				if (standardizeResponseData.success) {
					
					if (standardizeResponseData.qsarStandardizedSmiles.length()>255) {
						System.out.println(dr.dsstoxCompoundId+"\tsmiles too long="+dr.smiles);
						return "error: smiles too long to store in db";
					}
					
					compound = new Compound(dr.dsstoxCompoundId, standardizeResponseData.qsarStandardizedSmiles, standardizer.standardizerName, "tmarti02");
					System.out.println(dr.dsstoxCompoundId+"\tSDE qsar ready smiles="+compound.getCanonQsarSmiles());

					try {
						compoundService.create(compound);
						return standardizeResponseData.qsarStandardizedSmiles;
					} catch (ConstraintViolationException e) {
						System.out.println(e.getMessage());
						return "error: constraint violation";
					}

				} else {
					System.out.println(dr.dsstoxCompoundId+"\tCan't standardize="+dr.smiles);
					return "error: can't standardize";
					//					logger.warn(mpv.id + ": Standardization failed for SMILES: " + dr.smiles);
				}
			} else {
				System.out.println(": Standardizer HTTP response failed for SMILES: "
						+ dr.smiles + " with code " + standardizeResponse.status);
				return "error: "+standardizeResponse.status;
			}

		}
	}
	
	 void createSplitting(String splittingName, ArrayList<String>smilesArray) {
		String lanid="tmarti02";
		
		String datasetName="Standard Water solubility from exp_prop";
		
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(datasetName, "RND_REPRESENTATIVE");
		
		SplittingService splittingService = new SplittingServiceImpl();
		
		Splitting splitting = splittingService.findByName(splittingName);
		
		System.out.println(splitting.getId()+"\t"+splitting.getDescription());
		
		for (DataPointInSplitting dpis:dataPointsInSplitting) {
			DataPointInSplitting dpisNew = new DataPointInSplitting(dpis.getDataPoint(), splitting, dpis.getSplitNum(), lanid);
						
			if (dpis.getSplitNum()==DevQsarConstants.TRAIN_SPLIT_NUM) {
				if (splittingName.equals(splittingAll)) {
					createDataPointInSplitting(dpisNew);
				} else if (splittingName.equals(splittingPFASOnly)) {					
					if (isPFAS(smilesArray,dpis)) {
						System.out.println(dpis.getDataPoint().getId()+"\t"+dpis.getDataPoint().getCanonQsarSmiles()+"\t"+dpis.getSplitNum()+"\t"+dpis.getSplitting().getName());
						createDataPointInSplitting(dpisNew);		
					}
				} else if (splittingName.equals(splittingAllButPFAS)) {
					if (!isPFAS(smilesArray,dpis)) {
						createDataPointInSplitting(dpisNew);
//						System.out.println(dpis.getDataPoint().getCanonQsarSmiles()+"\t"+dpis.getSplitNum()+"\t"+dpis.getSplitting().getName());
					}					
				}
			}else if (dpis.getSplitNum()==DevQsarConstants.TEST_SPLIT_NUM) {
				if (isPFAS(smilesArray,dpis)) {
					System.out.println(dpis.getDataPoint().getCanonQsarSmiles()+"\t"+dpis.getSplitNum()+"\t"+dpis.getSplitting().getName());
					createDataPointInSplitting(dpisNew);
				}
			}
		}
//		session.close();
	}
	
	
	 void createDataPointInSplitting (DataPointInSplitting dpisNew) {
		try {
//			dataPointInSplittingService.create(dpisNew);
			create(dpisNew);
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			System.out.println(e.getMessage());
		}		
	}
	
	 boolean isPFAS(ArrayList<String>smilesArray,DataPointInSplitting dpis) {
		return smilesArray.contains(dpis.getDataPoint().getCanonQsarSmiles());
	}
	
	
	ArrayList<String> getPFASSmiles(String filepath) {
		try {
			
			List<String> Lines = Files.readAllLines(Paths.get(filepath));
		
			ArrayList<String>smilesArray=new ArrayList<>();
			
			for (String Line:Lines) {
				String [] values=Line.split("\t");
				String smiles=values[1];
				
				if (!smiles.contains("error") && !smiles.isBlank()) {
					smilesArray.add(smiles);
				}
			}
			return smilesArray;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	
	public static void main(String[] args) {
		PFAS_SplittingGenerator p=new PFAS_SplittingGenerator();
			
		String listName="PFASSTRUCTV4";
		String filePath="data/dev_qsar/dataset_files/"+listName+"_qsar_ready_smiles.txt";
//		generateQSAR_ReadyPFAS_STRUCT(listName,filePath);
		
		ArrayList<String>smilesArray=p.getPFASSmiles(filePath);
		
//		String splittingName=splittingPFASOnly;
//		String splittingName=splittingAll;		
		String splittingName=splittingAllButPFAS;		
		p.createSplitting(splittingName,smilesArray);
	}

}
