package gov.epa.run_from_java.scripts.OPERA;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import gov.epa.databases.dev_qsar.qsar_models.service.MethodADServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionReportServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceService;
import gov.epa.databases.dev_qsar.qsar_models.service.SourceServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
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

	OPERA_lookups lookups=null;
	
	String getSource() {
		return "OPERA"+version;
	}
	
	
	public 	OPERA_csv_to_PostGres_DB() {
		lookups=new OPERA_lookups();		
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
			abbrev="BIODEG";
			break;
		case DevQsarConstants.RBIODEG:
			abbrev="RBiodeg";
			break;
		case DevQsarConstants.LogD_pH_5_5:
			abbrev="LogD_ph5.5";
			break;
		case DevQsarConstants.LogD_pH_7_4:
			abbrev="LogD_ph7.4";
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
			abbrev="liq_chrom_Retention_Time";
			break;
		case DevQsarConstants.ORAL_RAT_LD50:
			abbrev="CATMOS_LD50";
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
			abbrev="CERAPP_Agonist";
			break;
		case DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST:
			abbrev="CERAPP_Antagonist";
			break;
		case DevQsarConstants.ESTROGEN_RECEPTOR_BINDING:
			abbrev="CERAPP_Binding";
			break;
		case DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST:
			abbrev="CoMPARA_Agonist";
			break;
		case DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST:
			abbrev="CoMPARA_Antagonist";
			break;
		case DevQsarConstants.ANDROGEN_RECEPTOR_BINDING:
			abbrev="CoMPARA_Binding";
			break;
		case DevQsarConstants.PKA_A:
			abbrev="pKa_Acidic";
			break;
		case DevQsarConstants.PKA_B:
			abbrev="pKa_Basic";
			break;
		
		default:
			return null;
		}
		
		return "OPERA"+version+"_"+abbrev;
	
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
	 * @param filepath file path for opera output file
	 * @param count number of chemicals to load from csv
	 */
	void readOPERA_output_csv(String filepath,int count) {
		
		List<PredictionDashboard>predictionsDashboard=new ArrayList<>();
		
		try {
			CSVReader reader = new CSVReader(new FileReader(filepath));
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
//				System.out.println(values[0]);
				
				convertValuesToRecords(predictionsDashboard,colNamesAll,values, htColNames,lookups);
				
				if(linesRead==count) break;
			}
			
			
//			int counter=0;
//			for (PredictionDashboard pd:predictionsDashboard) {
//				counter++;
//				
//				System.out.println(counter);
//				System.out.println(pd.getCanonQsarSmiles());
//				System.out.println(pd.getCreatedBy());
//				System.out.println(pd.getExperimentalString());
//				System.out.println(pd.getExperimentalValue());
//				System.out.println(pd.getPredictionValue());
//				System.out.println(pd.getDsstoxRecord().getId());
//				System.out.println(pd.getModel().getId());
//				System.out.println("");
//				
//			}
			
			List<QsarPredictedADEstimate>qsarPredictedADEstimates=new ArrayList<>();
			List<QsarPredictedNeighbor>qsarPredictedNeighbors=new ArrayList<>();
			List<PredictionReport>predictionReports=new ArrayList<>();
			
			boolean useOPERA_Image_API=false;

			for (PredictionDashboard pd:predictionsDashboard) {
				qsarPredictedADEstimates.addAll(pd.getQsarPredictedADEstimates());
				qsarPredictedNeighbors.addAll(pd.getQsarPredictedNeighbors());
				
				String datasetName=pd.getModel().getDatasetName();
				
				String unitAbbreviation=lookups.mapDatasets.get(datasetName).getUnitContributor().getAbbreviation();
				
				OPERA_Report or=new OPERA_Report(pd,unitAbbreviation,useOPERA_Image_API);

				String json=or.toJson();

				String folder = "data\\opera\\reports\\"+pd.getDsstoxRecord().getDtxcid();
				
				File Folder=new File(folder);
				Folder.mkdirs();

				or.toHTMLFile(folder);
				or.toJsonFile(folder);
//				System.out.println(Utilities.gson.toJson(or)+"\n\n*******************\n");
				
				PredictionReport predictionReport=new PredictionReport(pd, json.getBytes(), userName);
				predictionReports.add(predictionReport);
				
			}


			//Save to database:

			PredictionDashboardServiceImpl pds=new PredictionDashboardServiceImpl();
			PredictionReportServiceImpl prs=new PredictionReportServiceImpl();

			//Create the predictionsDashboard (neighbors and AD estimates get created automatically by hibernate
			predictionsDashboard=pds.createBatch(predictionsDashboard);

			//Create reports:
			//TODO if there was a PredictionReport object in PredictionDashboard it might create corresponding report automatically like it did with the neighbors and ad estimates
			predictionReports=prs.createBatch(predictionReports);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	void readOPERA_output_csv(String dtxcid,String filepath) {
		
		List<PredictionDashboard>predictionsDashboard=new ArrayList<>();

		try {
			CSVReader reader = new CSVReader(new FileReader(filepath));
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
//				System.out.println(values[0]);
				
				if(values[0].equals(dtxcid)) {
					convertValuesToRecords(predictionsDashboard,colNamesAll,values, htColNames,lookups);
					break;
				}
			}
			
			
//			PredictionDashboardServiceImpl pds=new PredictionDashboardServiceImpl();
//			predictionsDashboard=pds.createBatch(predictionsDashboard);
			
//			QsarPredictedADEstimateServiceImpl q=new QsarPredictedADEstimateServiceImpl();
			
			List<QsarPredictedADEstimate>qsarPredictedADEstimates=new ArrayList<>();
			List<QsarPredictedNeighbor>qsarPredictedNeighbors=new ArrayList<>();
			List<PredictionReport>predictionReports=new ArrayList<>();
			
//			HTML_Report_Creator_From_OPERA_Report rc=new HTML_Report_Creator_From_OPERA_Report();

			String folder = "data\\opera\\reports";

			boolean useOPERA_Image_API=false;//for testing since we dont have model graphs for current model ids in the api
			
			for (PredictionDashboard pd:predictionsDashboard) {
				qsarPredictedADEstimates.addAll(pd.getQsarPredictedADEstimates());
				qsarPredictedNeighbors.addAll(pd.getQsarPredictedNeighbors());
				
				String datasetName=pd.getModel().getDatasetName();
				
				String unitAbbreviation=lookups.mapDatasets.get(datasetName).getUnitContributor().getAbbreviation();
				
				OPERA_Report or=new OPERA_Report(pd,unitAbbreviation,useOPERA_Image_API);

				String json=or.toJson();
				
				or.toHTMLFile(folder);
				or.toJsonFile(folder);
//				System.out.println(Utilities.gson.toJson(or)+"\n\n*******************\n");
				
				PredictionReport predictionReport=new PredictionReport(pd, json.getBytes(), userName);
				predictionReports.add(predictionReport);
				
			}
			

			//Save to database:

			PredictionDashboardServiceImpl pds=new PredictionDashboardServiceImpl();
			PredictionReportServiceImpl prs=new PredictionReportServiceImpl();
			
			predictionsDashboard=pds.createBatch(predictionsDashboard);
			predictionReports=prs.createBatch(predictionReports);
//			q.createBatch(qsarPredictedADEstimates);

			//TODO Save neighbors to database

			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
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
	 */
	void convertValuesToRecords(List<PredictionDashboard>predictionsDashboard, List<String>colNamesCSV,String []values,TreeMap<String, List<String>>htColNames,OPERA_lookups lookups) {
		
		String regex = "^[-+]?\\d*[.]?\\d+|^[-+]?\\d+[.]?\\d*";
		
		
		for (String propertyAbbrevOPERA:lookups.htPropNameOperaAbbrevToPropNameDB.keySet()) {
			//			System.out.println(property);

//			if(!propertyAbbrevOPERA.equals("LogVP")) continue;
			
			String propertyNameDB=lookups.htPropNameOperaAbbrevToPropNameDB.get(propertyAbbrevOPERA);

//			System.out.println("\n"+propertyAbbrevOPERA+"\t"+propertyNameDB);


			//		String modelName="LogBCF OPERA2.9";

			List<String>colNamesCSV_Property=htColNames.get(propertyAbbrevOPERA);//csv columns pertaining to selected property

			PredictionDashboard pd=new PredictionDashboard();
			pd.setCanonQsarSmiles("N/A");//TODO get from separate summary file that OPERA generates at same time
			pd.setCreatedBy(userName);

			predictionsDashboard.add(pd);
			
			
			List<QsarPredictedADEstimate>qsarPredictedADEstimates=new ArrayList<>();
			pd.setQsarPredictedADEstimates(qsarPredictedADEstimates);

			
			List<QsarPredictedNeighbor>neighbors=new ArrayList<>();
			pd.setQsarPredictedNeighbors(neighbors);
			
			
			for (int i=1;i<=5;i++) {
				QsarPredictedNeighbor n=new QsarPredictedNeighbor();
				n.setNeighborNumber(i);
				n.setCreatedBy(userName);
				n.setPredictionDashboard(pd);
//				n.setPredictionDashboard(pd);//causes "failed to lazily initialize a collection of role:"
				neighbors.add(n);
			}

			if(propertyAbbrevOPERA.toLowerCase().contains("pka")) {
				neighbors.remove(4);
				neighbors.remove(3);
			}
						
			Model model=lookups.mapModels.get(getModelName(propertyNameDB));
			pd.setModel(model);

			Dataset dataset=lookups.mapDatasets.get(model.getDatasetName());
			
//			System.out.println(model.getDatasetName()+"\t"+dataset.getUnit().getAbbreviation());
			
			String unitName=dataset.getUnit().getName();
			String unitNameContributor=dataset.getUnitContributor().getName();

			
			for (String colName:colNamesCSV_Property) {
				
				String value=values[colNamesCSV.indexOf(colName)];
				
				if(value.length()>0 && value.substring(value.length()-1,value.length()).equals("|")) {
					value=value.substring(0,value.length()-1);
				}
						
				value=value.trim();
				
				if (value.isBlank() || value.equals("?")) value=null;
				
				if (value==null) {
					continue;
				}

				if (colName.equals(STR_DTXCID)) {
					
					pd.setDsstoxRecord(lookups.mapDsstoxRecordsByCID.get(value));
//					System.out.println("DTXCID="+value+"\tdsstox_records_id ="+dr.getId());
//					pd.setFk_dsstox_records_id(xxx);//TODO look up from a hashtable of values from db table
					
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
					
					if(value.matches(regex)) {
						Double dvalue = convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());
						neighbor.setExp(dvalue+"");
					} else {
						neighbor.setExp(value);
					}
					
				} else if (colName.contains("pred_neighbor")) {
					int neighborNum=Integer.parseInt(colName.substring(colName.length()-1,colName.length()));
					QsarPredictedNeighbor neighbor=neighbors.get(neighborNum-1);
					
					if(value.matches(regex)) {
						Double dvalue = convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());
						neighbor.setPred(dvalue+"");
					} else {
						neighbor.setPred(value);
					}
				} else if  (colName.contains("_exp")) {					
					if(value.matches(regex)) {
						Double dvalue = convertUnits(unitName, unitNameContributor, colName, value,pd.getDsstoxRecord());
						pd.setExperimentalValue(dvalue);
					} else {
						pd.setExperimentalString(value);
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
			
			
			QsarPredictedNeighbor.removeEmptyNeighbors(neighbors);
			QsarPredictedNeighbor.cloneNeighbors(neighbors,propertyAbbrevOPERA,lookups);
			QsarPredictedNeighbor.addNeighborMetadata(lookups, propertyAbbrevOPERA, neighbors);
			
//			System.out.println(pd.getFk_dsstox_records_id()+"\t"+pd.getPredictionValue()+"\t"+pd.getExperimentalValue()+"\t"+pd.getExperimentalString());
//			
//			for (QsarPredictedNeighbor n:neighbors) {
//				System.out.println("#\t"+n.getNeighborNumber());
//				System.out.println("DTXSID\t"+n.getDtxsid());
//				System.out.println("exp\t"+n.getExp());
//				System.out.println("pred\t"+n.getPred());
//			}
						
//			System.out.println(Utilities.gson.toJson(pd.getQsarPredictedNeighbors()));

		}//end loop over properties
		
		
	}


	




	

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
		Double dvalue=Double.parseDouble(value);
		
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
	 * @param colNamesAll
	 * @return
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
				propName = "LogOH";
			} else if (colName.contains("BCF")) {
				propName = "LogBCF";				
			} else if (colName.contains("HL")) {
				propName = "LogHL";				
			} else if (colName.contains("KOA")) {
				propName = "LogKOA";				
			} else if (colName.contains("Koc")) {
				propName = "LogKoc";				
			} else if (colName.contains("KM")) {
				propName = "LogKM";				
			} else if (colName.contains("ReadyBiodeg")) {
				propName = "ReadyBiodeg";				
			} else if (colName.contains("BioDeg")) {
				propName = "BioDeg_LogHalfLife";				
			} else if (colName.contains("BP")) {
				propName = "BP";				
			} else if (colName.contains("RT")) {
				propName = "RT";				
			} else if (colName.contains("WS")) {
				propName = "WS";				
			} else if (colName.contains("VP")) {
				propName = "LogVP";				
			} else if (colName.contains("LogP")) {
				propName = "LogP";				
			} else if (colName.contains("CERAPP_Bind")) {
				propName = "CERAPP_Bind";				
			} else if (colName.contains("CERAPP_Ago")) {
				propName = "CERAPP_Ago";				
			} else if (colName.contains("CERAPP_Anta")) {
				propName = "CERAPP_Anta";				
			} else if (colName.contains("CoMPARA_Bind")) {
				propName = "CoMPARA_Bind";				
			} else if (colName.contains("CoMPARA_Ago")) {
				propName = "CoMPARA_Ago";				
			} else if (colName.contains("CoMPARA_Anta")) {
				propName = "CoMPARA_Anta";				
			} else if (colName.contains("CACO2")) {
				propName = "CACO2";				
			} else if (colName.contains("Clint")) {
				propName = "Clint";				
			} else if (colName.contains("FUB")) {
				propName = "FUB";				
			} else if (colName.contains("MP")) {
				propName = "MP";								
			} else if (colName.contains("LD50") || 
					colName.contains("CATMOS") || colName.contains("CATMoS") || 
					colName.startsWith("CAS_neighbor_") || colName.startsWith("DTXSID_neighbor_") ||colName.startsWith("InChiKey_neighbor_")) {
				
				propName = "CATMOS_LD50";	
				
				if (colName.equals("CATMoS_VT_pred") || colName.equals("CATMoS_NT_pred") || 
					colName.equals("CATMoS_EPA_pred") || colName.equals("CATMoS_GHS_pred")) {
					continue;//TODO to store these, treat as separate properties like LogD55 and LogD74 
				}
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
		}
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

	private void addSpecialPropertiesToColNameMap(String[] colNamesAll, String dtxcid,
			TreeMap<String, List<String>> ht) {
		
		List<String> colNamesSpecial = new ArrayList<>();
		ht.put("LogD55", colNamesSpecial);

		colNamesSpecial = new ArrayList<>();
		ht.put("LogD74", colNamesSpecial);
		
		colNamesSpecial = new ArrayList<>();
		ht.put("pKa_a", colNamesSpecial);

		colNamesSpecial = new ArrayList<>();
		ht.put("pKa_b", colNamesSpecial);

		
		for (String colName:colNamesAll) {
						
			if (colName.contains("predRange") || !colName.contains("_")
					|| colName.equals(dtxcid)) 
				continue;

			
			if (colName.contains("LogD")) {
				String propName="LogD55";
				List<String> colNames = ht.get(propName);
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else colNames.add(colName);				

				propName="LogD74";
				colNames = ht.get(propName);
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else colNames.add(colName);				
				
			}
			
			if (colName.contains("pKa")) {
				String propName="pKa_a";
				List<String> colNames = ht.get(propName);
				
				if(colName.contains("_pred")) {
					if (colName.contains(propName)) colNames.add(colName);
				} else if (!colName.contains("pKa_b")){
					colNames.add(colName);				
				}

				propName="pKa_b";
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
	 

	
	public static void main(String[] args) {
		OPERA_csv_to_PostGres_DB o= new OPERA_csv_to_PostGres_DB();
		
//		o.initializeOPERARecords();
				
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.9\\";
		String filepathCsv=folder+"OPERA2.8_DSSTox_082021_1.csv";
		
//		String dtxcid="DTXCID00913";//lots of exp values
		String dtxcid="DTXCID1011864";
//		String dtxcid="DTXCID301163";//CERAPP_Ago_DTXSID_neighbor_1 is null but has cas
//		String dtxcid="DTXCID601194";//CERAPP_Ago_DTXSID_neighbor_1 has | delimiter
		
		o.readOPERA_output_csv(filepathCsv, 10);
//		o.readOPERA_output_csv(filepathCsv, -1);
//		o.readOPERA_output_csv(dtxcid,filepathCsv);
		
		
//		
	}


	/**
	 * Creates properties, datasets, and models for OPERA
	 * 
	 */
	void initializeOPERARecords() {
		TreeMap<String,String>htPropNameOperaAbbrevToPropNameDB=OPERA_lookups.createOperaPropertyAbbreviationToDatabasePropertyNameHashtable();
//		TreeMap <String,Property>mapProperties=createProperties(htPropNameOperaAbbrevToPropNameDB);
//		TreeMap<String,Dataset>mapDatasets=createDatasets(mapProperties,htPropNameOperaAbbrevToPropNameDB);
		TreeMap<String, Model>mapModels=createModels(htPropNameOperaAbbrevToPropNameDB);
		createStatistics(mapModels);
		
		
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
		
		
//		Model modelMP=mapModels.get("OPERA2.9_MP");//GG
//		ms.create(new ModelStatistic(statisticR2_Training,modelMP,0.74,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Training,modelMP,50.27,userName));
//		ms.create(new ModelStatistic(statisticR2_CV_Training,modelMP,0.71,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelMP,51.8,userName));
//		ms.create(new ModelStatistic(statisticR2_Test,modelMP,0.73,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Test,modelMP,52.72,userName));
//
//		Model modelHL=mapModels.get("OPERA2.9_HL");//GG
//		ms.create(new ModelStatistic(statisticR2_Training,modelHL,0.84,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Training,modelHL,1.91,userName));
//		ms.create(new ModelStatistic(statisticR2_CV_Training,modelHL,0.84,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelHL,1.96,userName));
//		ms.create(new ModelStatistic(statisticR2_Test,modelHL,0.85,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Test,modelHL,1.82,userName));
//
//		Model modelLogP=mapModels.get("OPERA2.9_LogP");//GG
//		ms.create(new ModelStatistic(statisticR2_Training,modelLogP,0.86,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Training,modelLogP,0.67,userName));
//		ms.create(new ModelStatistic(statisticR2_CV_Training,modelLogP,0.85,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelLogP,0.69,userName));
//		ms.create(new ModelStatistic(statisticR2_Test,modelLogP,0.86,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Test,modelLogP,0.79,userName));
//
//		Model modelCATMOS_LD50=mapModels.get("OPERA2.9_CATMOS_LD50");
//		//Only stats in the QMRF:
//		ms.create(new ModelStatistic(statisticR2_Training,modelCATMOS_LD50,0.85,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Training,modelCATMOS_LD50,0.30,userName));
//		ms.create(new ModelStatistic(statisticR2_CV_Training,modelCATMOS_LD50,0.79,userName));		
//		
//		Model modelRBiodeg=mapModels.get("OPERA2.9_RBiodeg");
//		ms.create(new ModelStatistic(statisticBA_Training,modelRBiodeg,0.8,userName));		
//		ms.create(new ModelStatistic(statisticSN_Training,modelRBiodeg,0.82,userName));
//		ms.create(new ModelStatistic(statisticSP_Training,modelRBiodeg,0.79,userName));
//		ms.create(new ModelStatistic(statisticBA_CV_Training,modelRBiodeg,0.8,userName));		
//		ms.create(new ModelStatistic(statisticSN_CV_Training,modelRBiodeg,0.82,userName));
//		ms.create(new ModelStatistic(statisticSP_CV_Training,modelRBiodeg,0.78,userName));
//		ms.create(new ModelStatistic(statisticBA_Test,modelRBiodeg,0.79,userName));		
//		ms.create(new ModelStatistic(statisticSN_Test,modelRBiodeg,0.81,userName));
//		ms.create(new ModelStatistic(statisticSP_Test,modelRBiodeg,0.77,userName));
//
//		Model modelBP=mapModels.get("OPERA2.9_BP");//GG
//		ms.create(new ModelStatistic(statisticR2_Training,modelBP,0.93,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Training,modelBP,22.1,userName));
//		ms.create(new ModelStatistic(statisticR2_CV_Training,modelBP,0.93,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelBP,22.5,userName));
//		ms.create(new ModelStatistic(statisticR2_Test,modelBP,0.93,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Test,modelBP,22.1,userName));
//
//		Model modelVP=mapModels.get("OPERA2.9_VP");//GG
//		ms.create(new ModelStatistic(statisticR2_Training,modelVP,0.91,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Training,modelVP,1.08,userName));
//		ms.create(new ModelStatistic(statisticR2_CV_Training,modelVP,0.91,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelVP,1.08,userName));
//		ms.create(new ModelStatistic(statisticR2_Test,modelVP,0.92,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Test,modelVP,1.00,userName));
//
//		
//		Model modelKOA=mapModels.get("OPERA2.9_LogKOA");//GG
//		ms.create(new ModelStatistic(statisticR2_Training,modelKOA,0.95,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Training,modelKOA,0.65,userName));
//		ms.create(new ModelStatistic(statisticR2_CV_Training,modelKOA,0.95,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelKOA,0.69,userName));
//		ms.create(new ModelStatistic(statisticR2_Test,modelKOA,0.96,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Test,modelKOA,0.68,userName));
//
//		Model modelAOH=mapModels.get("OPERA2.9_AOH");//GG
//		ms.create(new ModelStatistic(statisticR2_Training,modelAOH,0.85,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Training,modelAOH,1.12,userName));
//		ms.create(new ModelStatistic(statisticR2_CV_Training,modelAOH,0.85,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelAOH,1.14,userName));
//		ms.create(new ModelStatistic(statisticR2_Test,modelAOH,0.83,userName));		
//		ms.create(new ModelStatistic(statisticRMSE_Test,modelAOH,1.23,userName));

		Model modelKOC=mapModels.get("OPERA2.9_KOC");//GG
		ms.create(new ModelStatistic(statisticR2_Training,modelKOC,0.81,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelKOC,0.54,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelKOC,0.81,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelKOC,0.55,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelKOC,0.71,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelKOC,0.61,userName));

		Model modelBIODEG=mapModels.get("OPERA2.9_BIODEG");//GG
		ms.create(new ModelStatistic(statisticR2_Training,modelBIODEG,0.88,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelBIODEG,0.26,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelBIODEG,0.89,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelBIODEG,0.25,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelBIODEG,0.75,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelBIODEG,0.38,userName));
		
		Model modelBCF=mapModels.get("OPERA2.9_BCF");//GG
		ms.create(new ModelStatistic(statisticR2_Training,modelBCF,0.85,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelBCF,0.53,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelBCF,0.84,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelBCF,0.55,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelBCF,0.83,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelBCF,0.64,userName));

		Model modelKM=mapModels.get("OPERA2.9_KM");//GG
		ms.create(new ModelStatistic(statisticR2_Training,modelKM,0.82,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelKM,0.5,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelKM,0.83,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelKM,0.49,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelKM,0.73,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelKM,0.62,userName));

		Model modelWS=mapModels.get("OPERA2.9_WS");//GG
		ms.create(new ModelStatistic(statisticR2_Training,modelWS,0.87,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Training,modelWS,0.82,userName));
		ms.create(new ModelStatistic(statisticR2_CV_Training,modelWS,0.87,userName));		
		ms.create(new ModelStatistic(statisticRMSE_CV_Training,modelWS,0.81,userName));
		ms.create(new ModelStatistic(statisticR2_Test,modelWS,0.86,userName));		
		ms.create(new ModelStatistic(statisticRMSE_Test,modelWS,0.86,userName));

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
	private TreeMap<String, Model> createModels(TreeMap<String,String>htOperaToDatabaseProps) {
		
		TreeMap<String,Model> mapModels=new TreeMap<>();
		
		Method method=MethodCreator.createMethod(userName, true);
		
		SourceService ss=new SourceServiceImpl();
		Source source=ss.findByName(getSource());
		
		
		for (String operaAbbrev:htOperaToDatabaseProps.keySet()) {
			String propertyNameDB=htOperaToDatabaseProps.get(operaAbbrev);
			String descriptorSetName="PaDEL";
			String splittingName="OPERA";
			String modelName=getModelName(propertyNameDB);
			String datasetName=getDatasetName(propertyNameDB);
			DescriptorEmbedding embedding=null;//TODO could add this from the QMRF
			
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
	private TreeMap <String,Property> createProperties(TreeMap<String,String>htOperaToDatabaseProps) {
		
		PropertyServiceImpl ps=new PropertyServiceImpl();
		
		List<Property>propertiesInDB=ps.findAll();
		
		TreeMap <String,Property>mapProperties=new TreeMap<>();
		
		for (Property propertyInDB:propertiesInDB) {
			mapProperties.put(propertyInDB.getName(),propertyInDB);
		}
		
		
		for (String operaAbbrev:htOperaToDatabaseProps.keySet()) {
			String propertyName=htOperaToDatabaseProps.get(operaAbbrev);
			
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
	private TreeMap<String,Dataset> createDatasets(TreeMap <String,Property>propertyMap,TreeMap<String,String>htOperaToDatabaseProps) {
		
		TreeMap<String,Dataset>mapDatasets=new TreeMap<>();
		

		HashMap<String, String>hmUnitsDataset=DevQsarConstants.getDatasetFinalUnitsNameMapOPERA();
		HashMap<String, String>hmUnitsDatasetContributor=DevQsarConstants.getContributorUnitsNameMap();

				
		DatasetServiceImpl datasetService=new DatasetServiceImpl();
		
		for (String operaAbbrev:htOperaToDatabaseProps.keySet()) {
			String propertyNameDB=htOperaToDatabaseProps.get(operaAbbrev);
			String datasetName = getDatasetName(propertyNameDB);
			Dataset dataset=datasetService.findByName(datasetName);
			
			if (dataset!=null)	{
				mapDatasets.put(datasetName, dataset);
				continue;
			}

			String unitName=hmUnitsDataset.get(propertyNameDB);
			String unitContributorName=hmUnitsDatasetContributor.get(propertyNameDB);

			if (unitName==null || unitContributorName==null) {
				System.out.println(operaAbbrev+"\t"+propertyNameDB+"\t"+datasetName+"\t"+unitName+"\t"+unitContributorName);
			}
			
			Unit unit=CreatorScript.createUnit(unitName,userName);
			Unit unitContributor=CreatorScript.createUnit(unitContributorName,userName);
			
			String dsstoxMappingStrategy="OPERA";

			Property property=propertyMap.get(propertyNameDB);
			
			dataset=new Dataset(datasetName, datasetName, property, unit, unitContributor, dsstoxMappingStrategy, userName);
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
