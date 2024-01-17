package gov.epa.run_from_java.scripts.OPERA;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;


import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.opencsv.CSVReader;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.PropertyServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.databases.dev_qsar.qsar_models.entity.Source;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.DsstoxRecordServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodADServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionReportServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceService;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.run_from_java.scripts.QsarModelsScript;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.OPERA.OPERA_lookups.OtherCAS;
import gov.epa.run_from_java.scripts.PredictionDashboard.CreatorScript;

/**
 * 
 * Class to take output from OPERA software and load it into postgres db
 * 
* @author TMARTI02
*/
public class OPERA_csv_to_PostGres_DB {
	
	static final String STR_DTXCID="DSSTOX_COMPOUND_ID";
	
	public static final String version="2.9";
	String userName="tmarti02";

	final String strGlobalAD="OPERA global applicability domain";
	final String strLocalAD="OPERA local applicability domain";
	final String strConfidenceIndex="OPERA confidence index";

//	OPERA_lookups lookups=null;
	
	String getSource() {
		return "OPERA"+version;
	}
	
	


	/**
	 * Gets an OPERA model name based on the property name in the database
	 * 
	 * @param propertyNameDB
	 * @return
	 */
	public static String getModelName(String propertyNameDB) {
		//using opera model names from following sql query: 
		//select distinct od."modelName" from stg_datahub.opera.opera_data od
		
		String abbrev="";
		
		
		switch (propertyNameDB) {
		
		case DevQsarConstants.BOILING_POINT:
			abbrev="BP";
			break;
		case DevQsarConstants.MELTING_POINT:
			abbrev="MP";
			break;
		case DevQsarConstants.HENRYS_LAW_CONSTANT:
			abbrev="HL";
			break;
		case DevQsarConstants.LOG_KOW:
			abbrev="LogP";
			break;
		case DevQsarConstants.LOG_KOA:
			abbrev="LogKOA";
			break;
		case DevQsarConstants.KOC:
			abbrev="KOC";
			break;
		case DevQsarConstants.KM:
			abbrev="KM";
			break;
		case DevQsarConstants.OH:
			abbrev="AOH";
			break;
		case DevQsarConstants.BIODEG_HL_HC:
			abbrev="BioDeg";
			break;
		case DevQsarConstants.RBIODEG:
			abbrev="RBioDeg";
			break;
		case DevQsarConstants.LogD_pH_5_5:
			abbrev="LogD-pH5.5";
			break;
		case DevQsarConstants.LogD_pH_7_4:
			abbrev="LogD-pH7.4";
			break;
		case DevQsarConstants.BCF:
			abbrev="BCF";
			break;
		case DevQsarConstants.CLINT:
			abbrev="Clint";
			break;
		case DevQsarConstants.FUB:
			abbrev="FUB";
			break;
		case DevQsarConstants.RT:
			abbrev="RT";
			break;
		case DevQsarConstants.ORAL_RAT_LD50:
			abbrev="CATMoS-LD50";
			break;
		case DevQsarConstants.ORAL_RAT_VERY_TOXIC:
			abbrev="CATMoS-VT";
			break;
		case DevQsarConstants.ORAL_RAT_NON_TOXIC:
			abbrev="CATMoS-NT";
			break;
		case DevQsarConstants.ORAL_RAT_GHS_CATEGORY:
			abbrev="CATMoS-GHS";
			break;
		case DevQsarConstants.ORAL_RAT_EPA_CATEGORY:
			abbrev="CATMoS-EPA";
			break;
		case DevQsarConstants.WATER_SOLUBILITY:
			abbrev="WS";
			break;
		case DevQsarConstants.VAPOR_PRESSURE:
			abbrev="VP";
			break;
		case DevQsarConstants.CACO2:
			abbrev="CACO2";
			break;
		case DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST:
			abbrev="CERAPP-Agonist";
			break;
		case DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST:
			abbrev="CERAPP-Antagonist";
			break;
		case DevQsarConstants.ESTROGEN_RECEPTOR_BINDING:
			abbrev="CERAPP-Binding";
			break;
		case DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST:
			abbrev="CoMPARA-Agonist";
			break;
		case DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST:
			abbrev="CoMPARA-Antagonist";
			break;
		case DevQsarConstants.ANDROGEN_RECEPTOR_BINDING:
			abbrev="CoMPARA-Binding";
			break;
		case DevQsarConstants.PKA_A:
			abbrev="pKa-Acidic";
			break;
		case DevQsarConstants.PKA_B:
			abbrev="pKa-Basic";
			break;
		
		default:
			return null;
		}
		
//		return abbrev;//get the version from the fk_source_id in models table instead
//		return "OPERA"+version+"_"+abbrev;
		return abbrev+" OPERA"+version;// can also get version from fk_source_id in models table
	
	}
	

	private String getDatasetName(String propertyNameDB) {
		String v=null;
		
		String p=propertyNameDB;
		
		//From OPERA_models_2.9.xlsx:
		
		if (p.equals(DevQsarConstants.BOILING_POINT) 
				|| p.equals(DevQsarConstants.MELTING_POINT)
				|| p.equals(DevQsarConstants.VAPOR_PRESSURE)
				|| p.equals(DevQsarConstants.LOG_KOW)
				|| p.equals(DevQsarConstants.LogD_pH_5_5)
				|| p.equals(DevQsarConstants.LogD_pH_7_4)
				|| p.equals(DevQsarConstants.WATER_SOLUBILITY)
				|| p.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
			v="2.9";
		} else if (p.equals(DevQsarConstants.CACO2)
				|| p.equals(DevQsarConstants.FUB)
				|| p.equals(DevQsarConstants.CLINT)) {
			v="2.8";
		} else {
			v="2.6";
		}
		return propertyNameDB+" OPERA"+v;

	}
	
	
	/**
	 * Reads results from OPERA csv output file and loads it in the postgres db
	 * 
	 * @param filepathPredictionsCSV file path for opera output file
	 * @param count number of chemicals to load from csv (set to -1 to read all)
	 */
	List<PredictionDashboard> readOPERA_output_csvs(String filepathPredictionsCSV,String filepathStructureCSV, int count,	OPERA_lookups lookups) {

		List<OPERA_Structure>operaStructures=readStructureCSV(filepathStructureCSV, count);
		
		Hashtable<String,OPERA_Structure>htDTXCIDToOperaStructure=new Hashtable<>();
		
		for (OPERA_Structure s:operaStructures) htDTXCIDToOperaStructure.put(s.DSSTOX_COMPOUND_ID, s);
		
		List<PredictionDashboard>predictionsDashboard=new ArrayList<>();

		try {
			CSVReader reader = new CSVReader(new FileReader(filepathPredictionsCSV));
			String []colNames=reader.readNext();

			//			for (int i=0;i<colNames.length;i++) {
			//				System.out.println(i+"\t"+colNames[i]);
			//			}

			List<String>colNamesAll=Arrays.asList(colNames);

			TreeMap<String, List<String>>htColNames=assignColumnsByProperty(colNames);

			int linesRead=0;

			while (true) {
				String []values=reader.readNext();
				if (values==null) break;
				linesRead++;
				
				if(linesRead%100==0) {
					System.out.println(linesRead);
				}
				
				//				System.out.println(values[0]);

				convertValuesToRecords(predictionsDashboard,colNamesAll,values, htColNames,lookups,htDTXCIDToOperaStructure);

				if(linesRead==count) break;
			}

		}catch (Exception  ex) {
			ex.printStackTrace();
		}
		
		return predictionsDashboard;

	}
			
//			printValues(predictionsDashboard);
			
	/**
	 * Save to postgres database:
	 * 
	 * @param predictionsDashboard
	 * @param predictionReports - not really necessary to store in database because can regenerate from predictionDashboard objects
	 * @param useOPERA_Image_API
	 */
	void saveToDatabase(List<PredictionDashboard>predictionsDashboard,List<PredictionReport>predictionReports, boolean useOPERA_Image_API) {
		
		PredictionDashboardServiceImpl pds=new PredictionDashboardServiceImpl();
		PredictionReportServiceImpl prs=new PredictionReportServiceImpl();

		//Create the predictionsDashboard (neighbors and AD estimates get created automatically by hibernate
		predictionsDashboard=pds.createBatch(predictionsDashboard);

		//Create reports: (if create on the fly from predictionDashboard dont need to store in db
		//TODO if there was a PredictionReport object in PredictionDashboard it might create corresponding report automatically like it did with the neighbors and ad estimates
//		predictionReports=prs.createBatch(predictionReports);
	}


	/**
	 * Saves reports to hard drive in html/json
	 * @param predictionsDashboard
	 * @param useLatestModelIds
	 * @param lookups
	 * @return
	 */
private List<PredictionReport> createReports(List<PredictionDashboard> predictionsDashboard, boolean useLatestModelIds
		, OPERA_lookups lookups) {
	List<PredictionReport>predictionReports=new ArrayList<>();
	
	for (PredictionDashboard pd:predictionsDashboard) {
//			qsarPredictedADEstimates.addAll(pd.getQsarPredictedADEstimates());
//			qsarPredictedNeighbors.addAll(pd.getQsarPredictedNeighbors());
		
		String datasetName=pd.getModel().getDatasetName();
		Dataset dataset=lookups.mapDatasets.get(datasetName);
		String unitAbbreviation=dataset.getUnitContributor().getAbbreviation();
		Property property=dataset.getProperty();
		
		OPERA_Report or=new OPERA_Report(pd,property, unitAbbreviation,useLatestModelIds);

		String json=or.toJson();

		if(pd.getDsstoxRecord()==null) {
//			System.out.println("missing dsstoxrecord: "+pd.getDtxcid()+" for "+property.getName());
			continue;
		}
		
		String folder = "data\\opera\\reports\\"+pd.getDsstoxRecord().getDtxcid();
		
		File Folder=new File(folder);
		Folder.mkdirs();

		or.toHTMLFile(folder);
		or.toJsonFile(folder);
//			System.out.println(Utilities.gson.toJson(or)+"\n\n*******************\n");
		
		PredictionReport predictionReport=new PredictionReport(pd, json.getBytes(), userName);
		predictionReports.add(predictionReport);
		
	}
	return predictionReports;
}
			
			
		
		
	


	private void printValues(List<PredictionDashboard> predictionsDashboard) {
		int counter=0;
		
		for (PredictionDashboard pd:predictionsDashboard) {
			counter++;
			
			System.out.println(counter);
			System.out.println(pd.getCanonQsarSmiles());
			System.out.println(pd.getCreatedBy());
			System.out.println(pd.getExperimentalString());
			System.out.println(pd.getExperimentalValue());
			System.out.println(pd.getPredictionValue());
			System.out.println(pd.getDsstoxRecord().getId());
			System.out.println(pd.getModel().getId());
			System.out.println("");
			
		}
	}
	

	
	/**
	 * Takes values from a row in OPERA output and converts to predictionsDashboard
	 *  
	 * @param predictionsDashboard
	 * @param colNamesCSV
	 * @param values
	 * @param htColNames
	 * @param lookups
	 * @param htDTXCIDToOperaStructure 
	 */
	void convertValuesToRecords(List<PredictionDashboard>predictionsDashboard, List<String>colNamesCSV,String []values,TreeMap<String, List<String>>htColNames,OPERA_lookups lookups, Hashtable<String, OPERA_Structure> htDTXCIDToOperaStructure) {		
//		String regex = "^[-+]?\\d*[.]?\\d+|^[-+]?\\d+[.]?\\d*";//used to detect numerical values vs text
		
		List<String>propertyNamesOPERA=DevQsarConstants.getOPERA_PropertyNames();
		
		for (String propertyName:propertyNamesOPERA) {
			//			System.out.println(property);
			//		String modelName="LogBCF OPERA2.9";
			List<String>colNamesCSV_Property=htColNames.get(propertyName);//csv columns pertaining to selected property

			PredictionDashboard pd=new PredictionDashboard();
			
//			pd.setCanonQsarSmiles("N/A");//TODO get from separate summary file that OPERA generates at same time
			pd.setCreatedBy(userName);

			predictionsDashboard.add(pd);
			
			
			List<QsarPredictedADEstimate>qsarPredictedADEstimates=new ArrayList<>();
			pd.setQsarPredictedADEstimates(qsarPredictedADEstimates);

			List<QsarPredictedNeighbor>neighbors=new ArrayList<>();
			pd.setQsarPredictedNeighbors(neighbors);
			
			//Initialize neighbors
			for (int i=1;i<=5;i++) {
				QsarPredictedNeighbor n=new QsarPredictedNeighbor();
				n.setNeighborNumber(i);
				n.setCreatedBy(userName);
				n.setPredictionDashboard(pd);
//				n.setPredictionDashboard(pd);//causes "failed to lazily initialize a collection of role:"
				neighbors.add(n);
			}

			if(propertyName.toLowerCase().contains("pka")) {//only has 3 neighbors
				neighbors.remove(4);
				neighbors.remove(3);
			}
						
			Model model=lookups.mapModels.get(getModelName(propertyName));
			pd.setModel(model);
			
//			System.out.println(propertyName+"\t"+model.getModelStatistics().size());

			Dataset dataset=lookups.mapDatasets.get(model.getDatasetName());
			
//			System.out.println(model.getDatasetName()+"\t"+dataset.getUnit().getAbbreviation());
			
			String unitName=dataset.getUnit().getName();
			String unitNameContributor=dataset.getUnitContributor().getName();

			if (colNamesCSV_Property==null) {
				System.out.println("colnames are null for "+propertyName);
				continue;
			}
			
			for (String colName:colNamesCSV_Property) {
				
				if(colNamesCSV.indexOf(colName)==-1) {
					System.out.println(colName+" missing for "+propertyName);
					continue;
				}
				
				
				String value=values[colNamesCSV.indexOf(colName)];
				
				if(value!=null && value.length()>0 && value.substring(value.length()-1,value.length()).equals("|")) {
					value=value.substring(0,value.length()-1);
				}
						
				value=value.trim();

				if (value.isBlank() || value.equals("?")) value=null;

				if(value!=null && value.equals("NA")) value=null;

				if (value==null) {
					continue;
				}
				
				
//				if (pd.getDsstoxRecord()!=null && pd.getDsstoxRecord().getDtxcid().equals("DTXCID505")) {
//					if (propertyName.equals("Ready biodegradability")) {
//						System.out.println(colName+"\t"+value);
//					}
//				}

				if (colName.equals(STR_DTXCID)) {
					
					if(lookups.mapDsstoxRecordsByCID.get(value)!=null) {
						pd.setDsstoxRecord(lookups.mapDsstoxRecordsByCID.get(value));
						pd.setDtxcid(value);
					} else {
						pd.setDtxcid(value);
//						System.out.println("no matching  dsstoxRecord for "+value);
					}
					//Look up qsar ready smiles used by OPERA:
					pd.setCanonQsarSmiles(htDTXCIDToOperaStructure.get(value).Canonical_QSARr);
					
				} else if (colName.contains("AD_") || colName.contains("AD_index") || colName.contains("Conf_index") ) {
					
					MethodAD methodAD=null;
					
					if (colName.contains("AD_")) methodAD=lookups.mapMethodAD.get(strGlobalAD);
					if (colName.contains("AD_index")) methodAD=lookups.mapMethodAD.get(strLocalAD);
					if (colName.contains("Conf_index")) methodAD=lookups.mapMethodAD.get(strConfidenceIndex);
					
					Double dvalue=Double.parseDouble(value); 
					
					QsarPredictedADEstimate q=new QsarPredictedADEstimate();
					q.setCreatedBy(userName);
					q.setMethodAD(methodAD);
					q.setApplicabilityValue(dvalue);
					q.setPredictionDashboard(pd);
					qsarPredictedADEstimates.add(q);
					
					
				} else if (colName.contains("CAS_neighbor")) {
					int neighborNum=Integer.parseInt(colName.substring(colName.length()-1,colName.length()));
					QsarPredictedNeighbor neighbor=neighbors.get(neighborNum-1);
					
					if(value.substring(value.length()-1,value.length()).equals("|")) {
						value=value.substring(0,value.length()-1);
					}
					neighbor.setCasrn(value);

				} else if (colName.contains("InChiKey_neighbor")) {
					
					int neighborNum=Integer.parseInt(colName.substring(colName.length()-1,colName.length()));
					QsarPredictedNeighbor neighbor=neighbors.get(neighborNum-1);
					
					if(value.substring(value.length()-1,value.length()).equals("|")) {
						value=value.substring(0,value.length()-1);
					}
					neighbor.setInchiKey(value);

					
				} else if (colName.contains("DSSTOXMPID_neighbor")) {
					//dont need to store
				} else if (colName.contains("DTXSID_neighbor")) {
					int neighborNum=Integer.parseInt(colName.substring(colName.length()-1,colName.length()));
					QsarPredictedNeighbor neighbor=neighbors.get(neighborNum-1);
					if(value.substring(value.length()-1,value.length()).equals("|")) {
						value=value.substring(0,value.length()-1);
					}
					neighbor.setDtxsid(value);
				} else if (colName.contains("Exp_neighbor")) {
					int neighborNum=Integer.parseInt(colName.substring(colName.length()-1,colName.length()));
					QsarPredictedNeighbor neighbor=neighbors.get(neighborNum-1);

					Double dvalue = convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());

					if(dvalue!=null) {
						neighbor.setExp(dvalue+"");
					} else {
						neighbor.setExp(value);
					}
					
					
				} else if (colName.contains("pred_neighbor")) {
					int neighborNum=Integer.parseInt(colName.substring(colName.length()-1,colName.length()));
					QsarPredictedNeighbor neighbor=neighbors.get(neighborNum-1);

					Double dvalue = convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());

					if(dvalue!=null) {
						neighbor.setPred(dvalue+"");
					} else {
						neighbor.setPred(value);
					}
				} else if  (colName.contains("_exp")) {
					
//					if (propertyName.contains("Oral rat"))
//						System.out.println(propertyName+"\t"+value+"\t"+pd.getDsstoxRecord().getDtxcid());

//					if(propertyName.equals(DevQsarConstants.ORAL_RAT_VERY_TOXIC)|| 
//							propertyName.equals(DevQsarConstants.ORAL_RAT_NON_TOXIC) || 
//							propertyName.equals(DevQsarConstants.ORAL_RAT_EPA_CATEGORY) || 
//							propertyName.equals(DevQsarConstants.ORAL_RAT_GHS_CATEGORY) ) {

					if(propertyName.equals(DevQsarConstants.ORAL_RAT_LD50)) {
						pd.setExperimentalString(value);//always store as string because sometimes cant be saved as number due to -,<,> characters
					} else {
						Double dvalue = convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());
						if(dvalue!=null) {
							pd.setExperimentalValue(dvalue);
						} else {
							pd.setExperimentalString(value);
						}
						
					}
					
				} else if (colName.contains("_pred")) {
//					System.out.println(colName+"\t"+value);
					Double dvalue = convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());
					pd.setPredictionValue(dvalue);
//					System.out.println(fieldName+"\t"+value+"\t"+unitNameContributor);
				} else {
					//	System.out.println(colName);
					//	continue;
				}
			}//done iterating over col names for property
			
//			System.out.println(pd.getDsstoxRecord().getDtxcid()+"\t"+pd.getCanonQsarSmiles());
			
//			if(propertyName.equals(DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST)) {
//				System.out.println(pd.getDtxcid()+"\t"+neighbors.get(0).getInchiKey());
//			}
			
			
			QsarPredictedNeighbor.removeEmptyNeighbors(neighbors);
			
			//Split the pipe delimited neighbors:
			QsarPredictedNeighbor.cloneNeighbors(neighbors,propertyName,lookups);
			
//			lookForMissingNeighbors(propertyName, pd, neighbors);
			
			//Add SIDs if possible based on snapshot lookups:
			QsarPredictedNeighbor.addNeighborMetadata(lookups, propertyName, neighbors);
			

		}//end loop over properties
		
		
	}




	private void lookForMissingNeighbors(String propertyName, PredictionDashboard pd,
			List<QsarPredictedNeighbor> neighbors) {
		
		boolean haveFifthNeighbor=false;
		for (QsarPredictedNeighbor n:neighbors) {
			if(n.getNeighborNumber()==5) haveFifthNeighbor=true;
		}
		
		if(!haveFifthNeighbor) {
			System.out.println("Dont have 5th neighbor for "+propertyName+"\t"+pd.getDtxcid());
		}
		
		if (neighbors.size()<5) {
			System.out.println("Have have "+neighbors.size()+" neighbors for "+propertyName+"\t"+pd.getDtxcid());
		}
		
//		for (QsarPredictedNeighbor n:neighbors) {
//		System.out.println("#\t"+n.getNeighborNumber());
//		System.out.println("DTXSID\t"+n.getDtxsid());
//		System.out.println("CAS\t"+n.getCasrn());
//		System.out.println("exp\t"+n.getExp());
//		System.out.println("pred\t"+n.getPred());
//	}
		
	}


//	private Double getCategoryEPA(String strvalue) {
//
//		if(strvalue.equals("501-5000")) return 3.0;
//				
//		double value=Double.parseDouble(strvalue);
//		if (value<=50) return 1.0;
//		else if (value<=500) return 2.0;
//		else if(value<=5000) return 3.0;
//		else if(value>5000) return 4.0;
//		else return null;
//	}
//	
//	private Double getCategoryGHS(String strvalue) {
//		double value=Double.parseDouble(strvalue);
//		if (value<=5) return 1.0;
//		else if (value<=50) return 2.0;
//		else if(value<=300) return 3.0;
//		else if(value<=2000) return 4.0;
//		else if(value>2000) return 5.0;
//		else return null;
//	}



	

	/**
	 * Converts units to units indicated in unitNameContributor
	 *  
	 * @param unitName
	 * @param unitNameContributor
	 * @param colName
	 * @param value
	 * @param dr
	 * @return
	 */
	private Double convertUnits(String unitName, String unitNameContributor, String colName, String value,DsstoxRecord dr) {
		
		Double dvalue=null;
		
		try {
			dvalue=Double.parseDouble(value);
		} catch (Exception ex) {
			return null;
		}
		
		if(unitName.contains("LOG") && !unitNameContributor.contains("LOG")) {
			dvalue=Math.pow(10, dvalue);						
//			System.out.println("Converted:"+colName+"\t"+dvalue+"\t"+unitNameContributor);
			
		} else if (!unitName.equals(unitNameContributor)) {//TODO need MW for oral rat LD50
			
			if (unitName.equals("MG_KG") && unitNameContributor.equals("MOL_KG")) {
				
				if(dr==null) return null;
				
				double MW=dr.getMolWeight();
				
				try {
				
					if(MW==0) {
						Indigo indigo = new Indigo();
						indigo.setOption("ignore-stereochemistry-errors", true);
						IndigoObject indigoMolecule = indigo.loadMolecule(dr.getSmiles());
						MW=indigoMolecule.molecularWeight();
					}
					
					if(MW!=0) {
						dvalue=dvalue/1000.0/MW;
//						System.out.println("Original:"+colName+"\t"+value+"\t"+unitName);
//						System.out.println("Converted:"+colName+"\t"+dvalue+"\t"+unitNameContributor);
					} else {
						System.out.println(dr.getDtxcid()+"\tCant Convert:"+colName+"\t"+dvalue+"\t"+unitName+"\t"+unitNameContributor);
					}
				} catch (Exception ex) {
					System.out.println(dr.getDtxcid()+"\tCant Convert:"+colName+"\t"+dvalue+"\t"+unitName+"\t"+unitNameContributor);
				}
				
			} else {
				System.out.println(unitName+" != "+unitNameContributor);
				System.out.println("Cant Convert:"+colName+"\t"+dvalue+"\t"+unitName+"\t"+unitNameContributor);
			}
			
		} else {
//			System.out.println("Dont need to convert:"+unitName+ " = "+unitNameContributor);
		}
		return dvalue;
	}
	
	
	/**
	 * 
	 * Returns a map with key = property name and value is list of columns that correspond to that property
	 * TreeMap sorts entry by key
	 * 
	 * This is necessary because columns are not consistently labeled in terms of 
	 * the property name and sometimes multiple properties are lumped into the 
	 * same block of columns (rather than just making a each property completely separate)
	 * 
	 * @param colNamesAll column names from the output prediction text file
	 * @return map of column names to property name
	 */
	private TreeMap<String, List<String>> assignColumnsByProperty(String [] colNamesAll) {
		
		
		TreeMap<String, List<String>> ht=new TreeMap<>();
		
		addSpecialPropertiesToColNameMap(colNamesAll, STR_DTXCID, ht);
		
		for (String colName:colNamesAll) {
			
			String propName = "";

			if (colName.contains("predRange") || !colName.contains("_") ||					
					colName.equals(STR_DTXCID) || colName.contains("pKa") || colName.contains("LogD")) {
				continue;//already handled earlier
			} else if (colName.contains("LogOH") || colName.contains("AOH")) {
				propName = DevQsarConstants.OH;
			} else if (colName.contains("BCF")) {
				propName = DevQsarConstants.BCF;				
			} else if (colName.contains("HL")) {
				propName = DevQsarConstants.HENRYS_LAW_CONSTANT;				
			} else if (colName.contains("KOA")) {
				propName = DevQsarConstants.LOG_KOA;				
			} else if (colName.contains("Koc")) {
				propName = DevQsarConstants.KOC;				
			} else if (colName.contains("KM")) {
				propName = DevQsarConstants.KM;				
			} else if (colName.contains("ReadyBiodeg")) {
				propName = DevQsarConstants.RBIODEG;				
			} else if (colName.contains("BioDeg")) {
				propName = DevQsarConstants.BIODEG_HL_HC;				
			} else if (colName.contains("BP")) {
				propName = DevQsarConstants.BOILING_POINT;				
			} else if (colName.contains("RT")) {
				propName = DevQsarConstants.RT;				
			} else if (colName.contains("WS")) {
				propName = DevQsarConstants.WATER_SOLUBILITY;				
			} else if (colName.contains("VP")) {
				propName = DevQsarConstants.VAPOR_PRESSURE;				
			} else if (colName.contains("LogP")) {
				propName = DevQsarConstants.LOG_KOW;				
			} else if (colName.contains("CERAPP_Bind")) {
				propName = DevQsarConstants.ESTROGEN_RECEPTOR_BINDING;				
			} else if (colName.contains("CERAPP_Ago")) {
				propName = DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST;				
			} else if (colName.contains("CERAPP_Anta")) {
				propName = DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST;				
			} else if (colName.contains("CoMPARA_Bind")) {
				propName = DevQsarConstants.ANDROGEN_RECEPTOR_BINDING;				
			} else if (colName.contains("CoMPARA_Ago")) {
				propName = DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST;				
			} else if (colName.contains("CoMPARA_Anta")) {
				propName = DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST;				
			} else if (colName.contains("CACO2")) {
				propName = DevQsarConstants.CACO2;				
			} else if (colName.contains("Clint")) {
				propName = DevQsarConstants.CLINT;				
			} else if (colName.contains("FUB")) {
				propName = DevQsarConstants.FUB;				
			} else if (colName.contains("MP")) {
				propName = DevQsarConstants.MELTING_POINT;								
//			} else if (colName.equals("CATMoS_VT_pred")) {
//				propName=DevQsarConstants.ORAL_RAT_VERY_TOXIC;//this will only end up storing the predicted value for these properties
//			} else if (colName.equals("CATMoS_NT_pred")) {
//				propName=DevQsarConstants.ORAL_RAT_NON_TOXIC;//this will only end up storing the predicted value for these properties
//			} else if (colName.equals("CATMoS_EPA_pred")) {
//				propName=DevQsarConstants.ORAL_RAT_EPA_CATEGORY;
//			} else if (colName.equals("CATMoS_GHS_pred")) {
//				propName=DevQsarConstants.ORAL_RAT_GHS_CATEGORY;//this will only end up storing the predicted value for these properties
			} else if (colName.contains("LD50") || colName.contains("CATMoS") || 
					colName.startsWith("CAS_neighbor_") || colName.startsWith("DTXSID_neighbor_") ||colName.startsWith("InChiKey_neighbor_")) {
				propName = DevQsarConstants.ORAL_RAT_LD50;
				//For catmos LD50, we have neighbors and exp value whereas the other catmos properties dont???
			} else {
				System.out.println(colName+"\tUnassigned");
			}

			if (ht.get(propName) == null) {
				List<String> colNames = new ArrayList<>();
				colNames.add(colName);
				ht.put(propName, colNames);
			} else {
				List<String> colNames = ht.get(propName);
				colNames.add(colName);
			}
		}//end loop over colNames
				
		
		//duplicate columns to other catmos properties:
//		duplicateCatmosColumns(ht);
		
		
		//Add CID to each list
		Set<String> keys=ht.keySet();		
		for (String key:keys) {
			List<String> colNames = ht.get(key);
			colNames.add(0,"DSSTOX_COMPOUND_ID");
//			if(colNames.size()!=26) {
//				System.out.println(key+"\t"+colNames.size());	
//			}
		}
		
//		List<String> colNames = ht.get("CATMOS_LD50");
//		for (int i=0;i<colNames.size();i++) {
//			System.out.println((i+1)+"\t"+colNames.get(i));
//		}
		
		ht.remove("");
		
//		for(String key:ht.keySet()) {
//			System.out.println("\n"+key);
//			
//			List<String> colNames = ht.get(key);
//			for (String colName:colNames) {
//				System.out.println("\t"+colName);
//			}
//		}

		return ht;

	}




	private void duplicateCatmosColumns(TreeMap<String, List<String>> ht) {
		String[] props = { DevQsarConstants.ORAL_RAT_VERY_TOXIC, DevQsarConstants.ORAL_RAT_NON_TOXIC,
				DevQsarConstants.ORAL_RAT_EPA_CATEGORY, DevQsarConstants.ORAL_RAT_GHS_CATEGORY };
		
		for(String prop:props) {
			List<String> colNames = ht.get(prop);
			colNames.add(0,"CATMoS_LD50_exp");

			//TODO do we want neighbor section for CATMoS models other than LD50? Causes issues because the experimental units aren't MG_KG for these models
//			for (int i=1;i<=5;i++) {
//				colNames.add("CAS_neighbor_"+i);	
//				colNames.add("DTXSID_neighbor_"+i);
//				colNames.add("InChiKey_neighbor_"+i);
//				colNames.add("LD50_Exp_neighbor_"+i);
//			}
		}
	}

	/**
	 * This method handles logD and pKa which essentially have multiple properties sharing the same fields
	 * 
	 * @param colNamesAll
	 * @param dtxcid
	 * @param ht
	 */
	private void addSpecialPropertiesToColNameMap(String[] colNamesAll, String dtxcid,
			TreeMap<String, List<String>> ht) {
		
		List<String> colNamesSpecial = new ArrayList<>();
		ht.put(DevQsarConstants.LogD_pH_5_5, colNamesSpecial);

		colNamesSpecial = new ArrayList<>();
		ht.put(DevQsarConstants.LogD_pH_7_4, colNamesSpecial);
		
		colNamesSpecial = new ArrayList<>();
		ht.put(DevQsarConstants.PKA_A, colNamesSpecial);

		colNamesSpecial = new ArrayList<>();
		ht.put(DevQsarConstants.PKA_B, colNamesSpecial);

		
		for (String colName:colNamesAll) {
						
			if (colName.contains("predRange") || !colName.contains("_")
					|| colName.equals(dtxcid)) 
				continue;

			
			if (colName.contains("LogD")) {
				String propName=DevQsarConstants.LogD_pH_5_5;
				List<String> colNames = ht.get(propName);
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else colNames.add(colName);				

				propName=DevQsarConstants.LogD_pH_7_4;
				colNames = ht.get(propName);
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else colNames.add(colName);				
				
			}
			
			if (colName.contains("pKa")) {
				String propName=DevQsarConstants.PKA_A;
				List<String> colNames = ht.get(propName);
				
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else if (!colName.contains("pKa_b")){
					colNames.add(colName);				
				}

				propName=DevQsarConstants.PKA_B;
				colNames = ht.get(propName);
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else if (!colName.contains("pKa_a")){
					colNames.add(colName);				
				}
				
			}
		}
	}
	
	/**
	 * Parses a line in OPERA output file
	 * @param line
	 * @return
	 */
	private List<String> getRecordFromLine(String line) {
	    List<String> values = new ArrayList<String>();
	    try (Scanner rowScanner = new Scanner(line)) {
	        rowScanner.useDelimiter(",");
	        while (rowScanner.hasNext()) {
	            values.add(rowScanner.next());
	        }
	    }
	    return values;
	}
	 

	void goThroughCSVPredictionFile(boolean saveToDB) {
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.9\\";
//		String filepathPredictionCsv=folder+"OPERA2.8_DSSTox_082021_1_sample.csv";
//		String filepathStructureCSV=folder+"OPERA2.8_DSSTox_082021_1_sample_Structures.csv";
		String filepathPredictionCsv=folder+"OPERA2.8_DSSTox_082021_1_first1000.csv";
		String filepathStructureCSV=folder+"OPERA2.8_DSSTox_082021_1_first1000_structures.csv";
		
		int count=10;//number of rows in the csv to use
//		int count=-1;
		boolean useLatestModelIds=false;//if false use plot images using legacy model ids 

		boolean useJsonForDsstoxRecords=false;//if false load dsstoxRecords from postgresdb- used to look up neighbor info
		
		OPERA_lookups lookups=new OPERA_lookups(useJsonForDsstoxRecords);//creates lookup maps for database objects so dont have to keep query the database
		
		//Note opera_lookups depends on two json files which make it run faster than pulling the info from a database:
//		data\\dsstox\\json\\2023_04_snapshot_dsstox_records.json
//		data\\dsstox\\json\\2023_04_snapshot_other_casrn lookup.json
// 		These files are used to fix the neighbors which are missing dtxsids- but might need to pull info from prod_dsstox instead???

		System.out.println("\nGoing through csv");
		List<PredictionDashboard>predictionsDashboard=readOPERA_output_csvs(filepathPredictionCsv, filepathStructureCSV,count,lookups);

		System.out.println("\nCreating reports");
		List<PredictionReport> predictionReports=createReports(predictionsDashboard, useLatestModelIds,lookups);
		
		if(saveToDB) {
			System.out.println("\nSaving to database");
			saveToDatabase(predictionsDashboard, predictionReports, useLatestModelIds);
		}
		
	}
	
	class OPERA_Structure {
		String DSSTOX_COMPOUND_ID;
		String Original_SMILES;
		int  Number_of_connected_components;
		String Canonical_QSARr;
		String Salt_Solvent;
		String InChI_Code_QSARr;
		String InChI_Key_QSARr;
		String Salt_Solvent_ID;
	}
	
	List<OPERA_Structure> readStructureCSV(String filepath,int count) {

		List<OPERA_Structure>operaStructures=new ArrayList<>();

		try {
			CSVReader reader = new CSVReader(new FileReader(filepath));
			String []colNames=reader.readNext();

			List<String>colNamesAll=Arrays.asList(colNames);

			int linesRead=0;

			while (true) {
				String []values=reader.readNext();
				if (values==null || values.length<=1) break;
				linesRead++;
				
				OPERA_Structure s=new OPERA_Structure();
				
				s.DSSTOX_COMPOUND_ID=values[0];
				s.Original_SMILES=values[1];
				s.Number_of_connected_components=Integer.parseInt(values[2]);
				s.Canonical_QSARr=values[3];
				s.Salt_Solvent=values[4];
				s.InChI_Code_QSARr=values[5];
				s.InChI_Key_QSARr=values[6];
				s.Salt_Solvent_ID=values[7];
				
				operaStructures.add(s);
				
				
				//				System.out.println(values[0]);

				if(linesRead==count) break;
			}

		}catch (Exception  ex) {
			ex.printStackTrace();
		}

		return operaStructures;


	}
	
	/**
	 * Compares database dsstoxrecords with json file
	 */
	void findMissingDsstoxRecords() {
		
		OPERA_lookups.getDsstoxRecordsFromJsonExport();
		
		List<DsstoxRecord>recsJson=OPERA_lookups.dsstoxRecords;
		
		System.out.println(recsJson.size());
		
		DsstoxRecordServiceImpl rs=new DsstoxRecordServiceImpl();
		List<DsstoxRecord>recsDB=rs.findAll();
		
		TreeMap<Long,DsstoxRecord> mapDsstoxRecordsByID_DB=new TreeMap<>();
		
		for (DsstoxRecord rec:recsDB) 	{
			if(rec.getDsstoxSnapshot().getId()!=1) continue;//only use records for desired snapshot
			mapDsstoxRecordsByID_DB.put(rec.getId(),rec);
		}

		System.out.println(recsDB.size());
		
		for (DsstoxRecord recJson:recsJson) {
			
			if(mapDsstoxRecordsByID_DB.get(recJson.getId())==null) {
				System.out.println(recJson.getId()+"\t"+recJson.getDtxsid()+"\t"+recJson.getDtxcid()+"\tmissing in db");
			}
			
		}
		

	}
	
	void compareLoadTimes() {
		
		long t1=System.currentTimeMillis();
		OPERA_lookups.getDsstoxRecordsFromJsonExport();
		long t2=System.currentTimeMillis();
		System.out.println("Json: "+(t2-t1)/1000.0+" seconds");
		
		OPERA_lookups.getDsstoxRecordsFromDatabase();
		long t3=System.currentTimeMillis();
		System.out.println("DB: "+(t3-t2)/1000.0+" seconds");
		
	}
	
	void printOtherCAS() {
//		OPERA_lookups.getOtherCAS_Map();
		OPERA_lookups.getDsstoxRecordsFromDatabase();
//		
//		for(DsstoxRecord dr:OPERA_lookups.dsstoxRecords) {
//			if(dr.getOthercasrns()!=null) {
//				for (DsstoxOtherCASRN oc:dr.getOthercasrns()) {
//					System.out.println(dr.getDtxsid()+"\t"+oc.getCasrn());
//				}
//			}
//		}
	}
	
	void transposeCSV_Row(String dtxcid) {
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.9\\";

		String filepathPredictionCsv=folder+"OPERA2.8_DSSTox_082021_1_first1000.csv";

		try {
			CSVReader reader = new CSVReader(new FileReader(filepathPredictionCsv));
			String []colNames=reader.readNext();


			while (true) {
				String []values=reader.readNext();
				
				if(values==null) break;
				
				if(values[0].equals(dtxcid)) {
					for (int i=0;i<colNames.length;i++) {
						System.out.println(colNames[i]+"\t"+values[i]);
					}
					break;
				}
				
			} 
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void lookAtSpecialCSVRows() {
		//Look at csv values to see why values are wonky:
//		transposeCSV_Row("DTXCID00452");//only 4 neighbors for Caco-2
//		transposeCSV_Row("DTXCID30615");//Dont have 5th neighbor for Caco-2
//		transposeCSV_Row("DTXCID301030411");//Has 0 neighbors for AOH
//		transposeCSV_Row("DTXCID101");//Has NA for all neighbor exp values for CoMPARA-Agonist
//		transposeCSV_Row("DTXCID50996");//For Estrogen receptor binding only have casrn=55963-80-9 for neighbor 5 (not in dsstox)
		transposeCSV_Row("DTXCID9033");//Has NA for all neighbor exp values for CoMPARA-Agonist
		
	}
	
	
	
	public static void main(String[] args) {
		OPERA_csv_to_PostGres_DB o= new OPERA_csv_to_PostGres_DB();

//		o.initializeOPERARecords();//create db entries in properties, datasets, models, statistics tables
//		o.goThroughCSVPredictionFile(false);//goes through csv and creates predictionDashboard objects

		//View reports by generating them on the fly from database:
//		OPERA_Report_API ora=new OPERA_Report_API();
//		ora.viewReportsFromDatabase();

		//Bookkeeping methods:
//		o.printOtherCAS();
//		o.findMissingDsstoxRecords();
//		o.compareLoadTimes();
//		o.deleteOPERA_Records();//delete from db so have fresh start
//		o.lookAtSpecialCSVRows();
		o.saveOperaPlotImages();
		
	}
	
	
	void saveOperaPlotImages() {
		QsarModelsScript qms=new QsarModelsScript("tmarti02");
		
		TreeMap<String, Model>mapModels=OPERA_lookups.getModelsMap();
		
		for (String propertyName:DevQsarConstants.getOPERA_PropertyNames()) {

			
			int modelID_old=OPERA_Report.getOldModelID(propertyName);
			
			if(OPERA_Report.hasPlots(propertyName)==0) continue;
			
			if (modelID_old==-1) continue;

			System.out.println(propertyName);

			
			if (modelID_old!=-1 )  {
				String urlHistogram=OPERA_Report.urlHistogramAPIOld+modelID_old;
				String urlScatterPlot=OPERA_Report.urlScatterPlotAPIOld+modelID_old;
				
				try {
					
					byte[] scatterFile=downloadUrl(new URL(urlScatterPlot));
					byte[] histogramFile=downloadUrl(new URL(urlHistogram));
					
					Model model=mapModels.get(getModelName(propertyName));
					
					qms.uploadModelFile(model.getId(), 3L, scatterFile);
					qms.uploadModelFile(model.getId(), 4L, histogramFile);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private byte[] downloadUrl(URL toDownload) {
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

	void deleteOPERA_Records() {
		deleteOPERA_Records("qsar_predicted_neighbors");
		deleteOPERA_Records("qsar_predicted_ad_estimates");
		deleteOPERA_Records("prediction_reports");		
		deleteOPERA_Predictions();
	}
	
	/**
	 * Instead of complicated delete sql, find the OPERA reports one by one and delete them
	 */
	void deleteOPERA_Records(String table) {
		System.out.print("Deleting from "+table);
		String sql="select pr.id from qsar_models."+table+" pr\n"+
		"join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id\n"+
		"join qsar_models.models m on pd.fk_model_id = m.id\n"+
		"where m.fk_source_id=1";//TODO maybe add join to sources table 
		
		System.out.print("\n"+sql+"\n");
		
		try {
			
			Connection conn=SqlUtilities.getConnectionPostgres();
			

			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
			
			while (rs.next()) {
				String id=rs.getString(1);
				
				sql="Delete from qsar_models."+table+" where id="+id+";";
				SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);
				
				System.out.println(id);
				
			}
						

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done");
		
	}
	
	/**
	 * Delete with single query because less complicated
	 */
	void deleteOPERA_Predictions() {
		System.out.print("Deleting from predictions_dashboard");

		String sql="delete from qsar_models.predictions_dashboard pd using qsar_models.models m\n"+
		"where pd.fk_model_id = m.id and m.fk_source_id=1;";
		
		SqlUtilities.runSQLUpdate(SqlUtilities.getConnectionPostgres(), sql);

		System.out.println("Done");
	}
	

	/**
	 * Creates properties, datasets, and models for OPERA in the postgres db
	 * 
	 */
	void initializeOPERARecords() {
		
		List<String>propertyNames=DevQsarConstants.getOPERA_PropertyNames();
		TreeMap <String,Property>mapProperties=createProperties(propertyNames);
		TreeMap<String,Dataset>mapDatasets=createDatasets(mapProperties,propertyNames);
		TreeMap<String, Model>mapModels=createModels(propertyNames);
		createStatistics(mapModels);
	}

	
	Model getModel(String propertyName,TreeMap <String,Model>mapModels) {
		String modelName=getModelName(propertyName);
		return mapModels.get(modelName);
	}
	
	/**
	 * Store the statistics for the OPERA models
	 * Values taken from the QMRFs
	 * 
	 * @param mapModels
	 */
	private void createStatistics(TreeMap <String,Model>mapModels) {
		
		StatisticService ps=new StatisticServiceImpl();
		ModelStatisticService ms=new ModelStatisticServiceImpl();
		
		List<Statistic>statistics=ps.getAll();
		
		System.out.println(statistics.size());

		TreeMap <String,Statistic>mapStatistics=new TreeMap<>();
		for (Statistic statistic:statistics) {
			mapStatistics.put(statistic.getName(), statistic);
		}

		Statistic statisticR2_Training=mapStatistics.get(DevQsarConstants.PEARSON_RSQ_TRAINING);//Train//R2
		Statistic statisticRMSE_Training=mapStatistics.get(DevQsarConstants.RMSE_TRAINING);//Train//RMSE
		
		Statistic statisticR2_CV_Training=mapStatistics.get(DevQsarConstants.PEARSON_RSQ_CV_TRAINING);//5FoldCV//R2
		Statistic statisticRMSE_CV_Training=mapStatistics.get(DevQsarConstants.RMSE_CV_TRAINING);//5FoldCV//RMSE

		Statistic statisticR2_Test=mapStatistics.get(DevQsarConstants.PEARSON_RSQ_TEST);//TEST//R2
		Statistic statisticRMSE_Test=mapStatistics.get(DevQsarConstants.RMSE_TEST);//TEST//RMSE

		Statistic statisticBA_Training=mapStatistics.get(DevQsarConstants.BA_TRAINING);//Train//R2
		Statistic statisticSN_Training=mapStatistics.get(DevQsarConstants.SN_TRAINING);//Train//RMSE
		Statistic statisticSP_Training=mapStatistics.get(DevQsarConstants.SP_TRAINING);//Train//RMSE
		
		Statistic statisticBA_CV_Training=mapStatistics.get(DevQsarConstants.BA_CV_TRAINING);//Train//R2
		Statistic statisticSN_CV_Training=mapStatistics.get(DevQsarConstants.SN_CV_TRAINING);//Train//RMSE
		Statistic statisticSP_CV_Training=mapStatistics.get(DevQsarConstants.SP_CV_TRAINING);//Train//RMSE

		Statistic statisticBA_Test=mapStatistics.get(DevQsarConstants.BA_TEST);//Train//R2
		Statistic statisticSN_Test=mapStatistics.get(DevQsarConstants.SN_TEST);//Train//RMSE
		Statistic statisticSP_Test=mapStatistics.get(DevQsarConstants.SP_TEST);//Train//RMSE
		
		Model modelHL=getModel(DevQsarConstants.HENRYS_LAW_CONSTANT, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelHL,0.84,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelHL,1.91,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelHL,0.84,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelHL,1.96,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelHL,0.85,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelHL,1.82,userName));

		Model modelLogP=getModel(DevQsarConstants.LOG_KOW, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelLogP,0.86,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelLogP,0.67,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelLogP,0.85,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelLogP,0.69,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelLogP,0.86,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelLogP,0.79,userName));

		Model modelKOA=getModel(DevQsarConstants.LOG_KOA, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelKOA,0.95,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelKOA,0.65,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelKOA,0.95,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelKOA,0.69,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelKOA,0.96,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelKOA,0.68,userName));

		
		Model modelMP=getModel(DevQsarConstants.MELTING_POINT, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelMP,0.74,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelMP,50.27,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelMP,0.71,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelMP,51.8,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelMP,0.73,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelMP,52.72,userName));

		Model modelBP=getModel(DevQsarConstants.BOILING_POINT, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelBP,0.93,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelBP,22.1,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelBP,0.93,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelBP,22.5,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelBP,0.93,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelBP,22.1,userName));

		Model modelVP=getModel(DevQsarConstants.VAPOR_PRESSURE, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelVP,0.91,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelVP,1.08,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelVP,0.91,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelVP,1.08,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelVP,0.92,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelVP,1.00,userName));
		
		Model modelWS=getModel(DevQsarConstants.WATER_SOLUBILITY, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelWS,0.87,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelWS,0.82,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelWS,0.87,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelWS,0.81,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelWS,0.86,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelWS,0.86,userName));


		Model modelAOH=getModel(DevQsarConstants.OH, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelAOH,0.85,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelAOH,1.12,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelAOH,0.85,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelAOH,1.14,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelAOH,0.83,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelAOH,1.23,userName));

		Model modelKOC=getModel(DevQsarConstants.KOC, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelKOC,0.81,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelKOC,0.54,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelKOC,0.81,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelKOC,0.55,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelKOC,0.71,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelKOC,0.61,userName));
		
		Model modelKM=getModel(DevQsarConstants.KM, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelKM,0.82,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelKM,0.5,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelKM,0.83,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelKM,0.49,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelKM,0.73,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelKM,0.62,userName));

		Model modelRBiodeg=getModel(DevQsarConstants.RBIODEG, mapModels);
		ms.create(new ModelStatistic(statisticBA_Training,modelRBiodeg,0.8,userName));		
		ms.create(new ModelStatistic(statisticSN_Training,modelRBiodeg,0.82,userName));
		ms.create(new ModelStatistic(statisticSP_Training,modelRBiodeg,0.79,userName));
		ms.create(new ModelStatistic(statisticBA_CV_Training,modelRBiodeg,0.8,userName));		
		ms.create(new ModelStatistic(statisticSN_CV_Training,modelRBiodeg,0.82,userName));
		ms.create(new ModelStatistic(statisticSP_CV_Training,modelRBiodeg,0.78,userName));
		ms.create(new ModelStatistic(statisticBA_Test,modelRBiodeg,0.79,userName));		
		ms.create(new ModelStatistic(statisticSN_Test,modelRBiodeg,0.81,userName));
		ms.create(new ModelStatistic(statisticSP_Test,modelRBiodeg,0.77,userName));
		
		Model modelBIODEG=getModel(DevQsarConstants.BIODEG_HL_HC, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelBIODEG,0.88,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelBIODEG,0.26,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelBIODEG,0.89,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelBIODEG,0.25,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelBIODEG,0.75,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelBIODEG,0.38,userName));

		
		Model modelBCF=getModel(DevQsarConstants.BCF, mapModels);
		ms.create(new ModelStatistic(statisticR2_Training,modelBCF,0.85,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelBCF,0.53,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelBCF,0.84,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelBCF,0.55,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelBCF,0.83,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelBCF,0.64,userName));


		Model modelCATMOS_LD50=getModel(DevQsarConstants.ORAL_RAT_LD50, mapModels);
		//Only stats in the QMRF:
		ms.create(new ModelStatistic(statisticR2_Training,modelCATMOS_LD50,0.85,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelCATMOS_LD50,0.30,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelCATMOS_LD50,0.79,userName));		
		

		Model modelCATMOS_VT=getModel(DevQsarConstants.ORAL_RAT_VERY_TOXIC, mapModels);
		//Only stats in the QMRF:
		ms.create(new ModelStatistic(statisticSN_Training,modelCATMOS_VT,0.87,userName));		
		ms.create(new ModelStatistic(statisticSP_Training,modelCATMOS_VT,0.99,userName));
		ms.create(new ModelStatistic(statisticBA_Training,modelCATMOS_VT,0.92,userName));
		ms.create(new ModelStatistic(statisticBA_CV_Training,modelCATMOS_VT,0.79,userName));		
		ms.create(new ModelStatistic(statisticSN_Test,modelCATMOS_VT,0.87,userName));		
		ms.create(new ModelStatistic(statisticSP_Test,modelCATMOS_VT,0.99,userName));
		ms.create(new ModelStatistic(statisticBA_Test,modelCATMOS_VT,0.93,userName));

		
		Model modelCATMOS_NT=getModel(DevQsarConstants.ORAL_RAT_NON_TOXIC, mapModels);
		//Only stats in the QMRF:
		ms.create(new ModelStatistic(statisticSN_Training,modelCATMOS_NT,0.88,userName));		
		ms.create(new ModelStatistic(statisticSP_Training,modelCATMOS_NT,0.97,userName));
		ms.create(new ModelStatistic(statisticBA_Training,modelCATMOS_NT,0.92,userName));
		ms.create(new ModelStatistic(statisticBA_CV_Training,modelCATMOS_NT,0.9,userName));		
		ms.create(new ModelStatistic(statisticSN_Test,modelCATMOS_NT,0.88,userName));		
		ms.create(new ModelStatistic(statisticSP_Test,modelCATMOS_NT,0.97,userName));
		ms.create(new ModelStatistic(statisticBA_Test,modelCATMOS_NT,0.92,userName));

		Model modelCATMOS_EPA=getModel(DevQsarConstants.ORAL_RAT_EPA_CATEGORY, mapModels);
		//Only stats in the QMRF:
		ms.create(new ModelStatistic(statisticSN_Training,modelCATMOS_EPA,0.81,userName));		
		ms.create(new ModelStatistic(statisticSP_Training,modelCATMOS_EPA,0.92,userName));
		ms.create(new ModelStatistic(statisticBA_Training,modelCATMOS_EPA,0.87,userName));
		ms.create(new ModelStatistic(statisticBA_CV_Training,modelCATMOS_EPA,0.79,userName));		
		ms.create(new ModelStatistic(statisticSN_Test,modelCATMOS_EPA,0.81,userName));		
		ms.create(new ModelStatistic(statisticSP_Test,modelCATMOS_EPA,0.92,userName));
		ms.create(new ModelStatistic(statisticBA_Test,modelCATMOS_EPA,0.87,userName));
		

		Model modelCATMOS_GHS=getModel(DevQsarConstants.ORAL_RAT_GHS_CATEGORY, mapModels);
		//Only stats in the QMRF:
		ms.create(new ModelStatistic(statisticSN_Training,modelCATMOS_GHS,0.8,userName));		
		ms.create(new ModelStatistic(statisticSP_Training,modelCATMOS_GHS,0.95,userName));
		ms.create(new ModelStatistic(statisticBA_Training,modelCATMOS_GHS,0.88,userName));
		ms.create(new ModelStatistic(statisticBA_CV_Training,modelCATMOS_GHS,0.78,userName));		
		ms.create(new ModelStatistic(statisticSN_Test,modelCATMOS_GHS,0.8,userName));		
		ms.create(new ModelStatistic(statisticSP_Test,modelCATMOS_GHS,0.95,userName));
		ms.create(new ModelStatistic(statisticBA_Test,modelCATMOS_GHS,0.88,userName));
		
		
//TODO add stats for following		
		
// Get some of these from papers?		
//		OPERA2.9_pKa_Basic
//		OPERA2.9_pKa_Acidic
//		OPERA2.9_LogD_ph7.4
//		OPERA2.9_LogD_ph5.5
		
//		OPERA2.9_CoMPARA_Binding
//		OPERA2.9_CoMPARA_Antagonist
//		OPERA2.9_CoMPARA_Agonist
		
//		OPERA2.9_CERAPP_Binding
//		OPERA2.9_CERAPP_Antagonist
//		OPERA2.9_CERAPP_Agonist
		
//		OPERA2.9_CACO2
//		OPERA2.9_liq_chrom_Retention_Time
//		OPERA2.9_Clint
//		OPERA2.9_FUB

		
	}


	/**
	 * Creates OPERA models in the database
	 * 
	 * @param htOperaToDatabaseProps
	 * @return
	 */
	private TreeMap<String, Model> createModels(List<String>propertyNames) {
		
		TreeMap<String,Model> mapModels=OPERA_lookups.getModelsMap();
		
		Method method=MethodCreator.createMethod(userName, true);
		
		SourceService ss=new SourceServiceImpl();
		Source source=ss.findByName(getSource());
		
		for (String propertyName:propertyNames) {

			String descriptorSetName="PaDEL";
			String splittingName="OPERA";
			String modelName=getModelName(propertyName);
			String datasetName=getDatasetName(propertyName);
			DescriptorEmbedding embedding=null;//TODO could add this from the QMRF

			
			if(mapModels.get(modelName)!=null) {
				Model modelInDB=mapModels.get(modelName);
				if(modelInDB.getSource().getName().equals(source.getName())) {
//					System.out.println(modelInDB.getName()+" is in db");
					continue;
				}
			}
			
			Model model=new Model(modelName, method, embedding,descriptorSetName, datasetName, splittingName, source,userName);
//			System.out.println(Utilities.gson.toJson(model));
			
			model=CreatorScript.createModel(model);
			
//			System.out.println(modelName+"\t"+model.getName());
			mapModels.put(modelName, model);
		}
		System.out.println("mapModels.size()="+mapModels.size());
		return mapModels;
	}

	/**
	 * Creates properties for OPERA if they arent in the db
	 * 
	 * @param htOperaToDatabaseProps
	 * @return
	 */
	private TreeMap <String,Property> createProperties(List<String>propertyNames) {
		
		PropertyServiceImpl ps=new PropertyServiceImpl();
		
		TreeMap <String,Property>mapProperties=OPERA_lookups.getPropertyMap();
		
		
		for (String propertyName:propertyNames) {
			
			
			boolean inDB=mapProperties.get(propertyName)!=null;
			
			if(!inDB) {
//				System.out.println(operaAbbrev+"\t"+propertyName+"\t"+inDB);
				
				Property property=new Property();
				property.setName(propertyName);
				property.setCreatedBy(userName);
				property.setDescription(DevQsarConstants.getPropertyDescription(propertyName));

				System.out.println(Utilities.gson.toJson(property));
				ps.create(property);
				mapProperties.put(propertyName,property);
			} 
			
		}
		
		System.out.println("propertyMapDB.size()="+mapProperties.size());
		return mapProperties;
		
	}
	
	/**
	 * Creates applicability domains for OPERA in db
	 */
	private void createADs() {

		MethodADServiceImpl servMAD=new MethodADServiceImpl();

		String strGlobalAD="OPERA global applicability domain";
		String strLocalAD="OPERA local applicability domain";
		String strConfidenceIndex="OPERA confidence index";

		MethodAD methodAD=null;
		
		methodAD = new MethodAD(strGlobalAD,strGlobalAD, "Global",	userName);
		servMAD.create(methodAD);
		
		methodAD = new MethodAD(strLocalAD,strLocalAD, "Local",	userName);
		servMAD.create(methodAD);

		methodAD = new MethodAD(strConfidenceIndex,strConfidenceIndex, "Local",	userName);
		servMAD.create(methodAD);

	}

	
	/**
	 * Creates OPERA datasets in the database
	 * 
	 * @param propertyMap
	 * @param htOperaToDatabaseProps
	 * @return
	 */
	private TreeMap<String,Dataset> createDatasets(TreeMap <String,Property>propertyMap,List<String>propertyNames) {
		
		TreeMap<String,Dataset>mapDatasets=OPERA_lookups.getDatasetsMap();
		
		HashMap<String, String>hmUnitsDataset=DevQsarConstants.getDatasetFinalUnitsNameMapOPERA();
		HashMap<String, String>hmUnitsDatasetContributor=DevQsarConstants.getContributorUnitsNameMap();

				
		DatasetServiceImpl datasetService=new DatasetServiceImpl();
		
		for (String propertyName:propertyNames) {
			String datasetName = getDatasetName(propertyName);
			
			boolean inDB=mapDatasets.get(datasetName)!=null;
			
			if(inDB) {
//				System.out.println(datasetName +" is in DB");
				continue;
			}

			String unitName=hmUnitsDataset.get(propertyName);
			String unitContributorName=hmUnitsDatasetContributor.get(propertyName);

			if (unitName==null || unitContributorName==null) {
				System.out.println(propertyName+"\t"+datasetName+"\t"+unitName+"\t"+unitContributorName);
			}
			
			Unit unit=CreatorScript.createUnit(unitName,userName);
			Unit unitContributor=CreatorScript.createUnit(unitContributorName,userName);
			
			String dsstoxMappingStrategy="OPERA";

			Property property=propertyMap.get(propertyName);
			
			Dataset dataset=new Dataset(datasetName, datasetName, property, unit, unitContributor, dsstoxMappingStrategy, userName);
			dataset=datasetService.create(dataset);
			
			mapDatasets.put(datasetName, dataset);

//			System.out.println(Utilities.gson.toJson(dataset));
			
		}
		
		
//		for (String propertyNameDB:propertyNames) {
////			String datasetDescription=getDatasetDescription(propertyNameDB);
//		}	
		
		System.out.println("mapDatasets.size()="+mapDatasets.size());
		
		return mapDatasets;
	}
	
	
	private static TreeMap<String,DsstoxRecord> getDsstoxRecordsMapSample() {
		TreeMap<String,DsstoxRecord>hmRecs=new TreeMap<>();
		
		DsstoxRecord dr=new DsstoxRecord();

		//Naphthalene
		dr.setDtxcid("DTXCID00913");
		dr.setDtxsid("DTXSID8020913");
		dr.setSmiles("C1=CC2=CC=CC=C2C=C1");
		dr.setMolWeight(128.174);
		dr.setCid(913L);
		dr.setId(113102L);
		//TODO add preferred name

//		dr.setDtxcid("DTXCID101");
//		dr.setDtxsid("DTXSID7020001");
//		dr.setSmiles("NC1=NC2=C(C=C1)C1=CC=CC=C1N2");
//		dr.setMolWeight(183.214);
//		dr.setId(114150L);
		
		hmRecs.put(dr.getDtxcid(),dr);
		
		return hmRecs;
	}


}
