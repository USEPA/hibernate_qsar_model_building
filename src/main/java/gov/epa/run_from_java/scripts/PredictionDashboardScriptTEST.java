package gov.epa.run_from_java.scripts;

import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.google.gson.reflect.TypeToken;


import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.PropertyServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.UnitServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;


/**
* @author TMARTI02
*/
public class PredictionDashboardScriptTEST {
	
	PropertyServiceImpl propertyService=new PropertyServiceImpl();
	UnitServiceImpl unitService=new UnitServiceImpl();
	ExpPropUnitServiceImpl unitServiceExpProp=new ExpPropUnitServiceImpl();
	ExpPropPropertyServiceImpl propertyServiceExpProp=new ExpPropPropertyServiceImpl();
	MethodServiceImpl methodService=new MethodServiceImpl();
	ModelServiceImpl modelService=new ModelServiceImpl();
	DatasetServiceImpl datasetService=new DatasetServiceImpl();
	PredictionDashboardServiceImpl predictionDashboardService=new PredictionDashboardServiceImpl();
	
	
	String lanId="tmarti02";
	
	String[] propertyNames = { "Fathead minnow LC50 (96 hr)", "Daphnia magna LC50 (48 hr)",
			"T. pyriformis IGC50 (48 hr)", "Oral rat LD50", "Bioconcentration factor", "Developmental Toxicity",
			"Mutagenicity", "Estrogen Receptor Binding", "Estrogen Receptor RBA", "Normal boiling point",
			"Melting point", "Flash point", "Vapor pressure at 25?C", "Density", "Surface tension at 25?C",
			"Thermal conductivity at 25?C", "Viscosity at 25?C", "Water solubility at 25?C" };

	
	void runFromSampleJsonFileHashtable(String filepathJson,String versionTEST) {
		
		Type listOfMyClassObject = new TypeToken<Hashtable<String,List<PredictionResults>>>() {}.getType();
		
		try {
			Hashtable<String,List<PredictionResults>>htResultsAll=Utilities.gson.fromJson(new FileReader(filepathJson), listOfMyClassObject);

			//TODO add code to create automatically new methods if they arent in the methods table in db
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			
			HashMap<String, Model> hmModels = createModels(versionTEST, hmMethods);
			
//			if(true)return;
			
			for (String DTXSID:htResultsAll.keySet()) {
				
				List<PredictionResults>listPredictionResults=htResultsAll.get(DTXSID);
				
				for (PredictionResults pr:listPredictionResults) {
					PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(pr,versionTEST,hmModels);
					predictionDashboardService.create(pd);
//					System.out.println(Utilities.gson.toJson(pd));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	void runFromSampleJsonFile(String filepathJson,String versionTEST) {
		
		Type listOfMyClassObject = new TypeToken<List<PredictionResults>>() {}.getType();
		
		try {
			List<PredictionResults>htResultsAll=Utilities.gson.fromJson(new FileReader(filepathJson), listOfMyClassObject);

			//TODO add code to create automatically new methods if they arent in the methods table in db
			HashMap<String,Method> hmMethods=new HashMap<>();
			hmMethods.put("consensus_regressor",methodService.findByName("consensus_regressor"));
			hmMethods.put("consensus_classifier",methodService.findByName("consensus_classifier"));
			
			HashMap<String, Model> hmModels = createModels(versionTEST, hmMethods);
			
//			if(true)return;
			
			for (PredictionResults pr:htResultsAll) {
				PredictionDashboard pd=convertPredictionResultsToPredictionDashboard(pr,versionTEST,hmModels);
				predictionDashboardService.create(pd);
//				System.out.println(Utilities.gson.toJson(pd));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}


	private HashMap<String, Model> createModels(String versionTEST, HashMap<String, Method> hmMethods) {
		HashMap<String,Model> hmModels=new HashMap<>();
		
		for (String propertyName:propertyNames) {
			String datasetName=getDatasetName(propertyName);
			String source="T.E.S.T. "+versionTEST;
			String descriptorSetName="T.E.S.T. "+versionTEST;

			String splittingName="TEST";
			String propertyNameDB=getPropertyNameDB(propertyName);
			
			
			String methodName=null;
			if (propertyNameDB.equals(DevQsarConstants.DEV_TOX) || propertyNameDB.equals(DevQsarConstants.MUTAGENICITY) || propertyNameDB.equals(DevQsarConstants.ESTROGEN_RECEPTOR_BINDING)) {
				methodName="consensus_classifier";
			} else {
				methodName="consensus_regressor";
			}

			Model model=new Model(hmMethods.get(methodName), null,descriptorSetName, datasetName, splittingName, source,lanId);
			model=createModel(model);
			hmModels.put(datasetName, model);
		}
		return hmModels;
	}
	
	private Model createModel(Model model) {
		List<Model>models=modelService.findByDatasetName(model.getDatasetName());

		if(models.size()==1) {
			return models.get(0);
		} else if (models.size()>1) {
			System.out.println("Multiple models");
			return null;
		}
		
		System.out.print("Need to create model for "+model.getDatasetName()+"...");
		model=modelService.create(model);
		System.out.println("done");
		return model;
	}

	String getDatasetName(String property_name) {

		if (property_name.equals("Fathead minnow LC50 (96 hr)")) {
			return "LC50 TEST";
		} else if (property_name.equals("Daphnia magna LC50 (48 hr)")) {
			return("LC50DM TEST");
		} else if (property_name.equals("T. pyriformis IGC50 (48 hr)")) {
			return("IGC50 TEST");
		} else if (property_name.equals("Oral rat LD50")) {
			return("LD50 TEST");
		} else if (property_name.equals("Developmental Toxicity")) {
			return "DevTox TEST";
		} else if (property_name.equals("Normal boiling point")) { 
			return DevQsarConstants.BOILING_POINT+" TEST";
		} else if (property_name.contains(" at 25")) { 
			property_name=property_name.substring(0,property_name.indexOf(" at 25"));
			return property_name+" TEST"; 
		} else if (property_name.contains("Bioconcentration factor")) {
			return("BCF TEST");//TODO doesnt exist			
		} else  {
			return property_name+ " TEST";
		} 

	}
	
	
	String getDatasetDescription(String datasetName) {
		
		String datasetName2=datasetName.replace(" TEST", "");

		if (datasetName2.equals("LC50")) {
			return "96 hour fathead minnow LC50 data compiled from ECOTOX for the TEST software";
		} else if (datasetName2.equals("LC50DM")) {
			return("48 hour Daphnia magna LC50 data compiled from ECOTOX for the TEST software");
		} else if (datasetName2.equals("IGC50")) {
			return("48 hour T. pyriformis IGC50 data compiled from Schultz et al for the TEST software");
		} else if (datasetName2.equals("LD50")) {
			return("Oral rat LD50 data compiled from ChemIDplus for the TEST software");
		} else if (datasetName2.equals(DevQsarConstants.THERMAL_CONDUCTIVITY)) {
			return "Thermal conductivity data compiled from Jamieson and Vargaftik for the TEST software";
		} else if (datasetName2.equals(DevQsarConstants.SURFACE_TENSION)) {
			return "Surface tension data compiled from Jaspar for the TEST software";
		} else if (datasetName2.equals(DevQsarConstants.VISCOSITY)) {
			return "Viscosity data compiled from Viswanath and Riddick for the TEST software";
		} else if (datasetName2.equals(DevQsarConstants.BOILING_POINT)) { 
			return "Boiling point data compiled from EPISUITE data for the TEST software"; 
		} else if (datasetName2.equals(DevQsarConstants.VAPOR_PRESSURE)) { 
			return "Vapor pressure data compiled from EPISUITE data for the TEST software"; 
		} else if (datasetName2.equals(DevQsarConstants.WATER_SOLUBILITY)) { 
			return "Water solubility data compiled from EPISUITE data for the TEST software"; 
		} else if (datasetName2.equals(DevQsarConstants.MELTING_POINT)) { 
			return "Melting point data compiled from EPISUITE data for the TEST software"; 
		} else if (datasetName2.equals(DevQsarConstants.DENSITY)) { 
			return "Density data compiled from LookChem.com for the TEST software"; 
		} else if (datasetName2.equals(DevQsarConstants.FLASH_POINT)) { 
			return "Flash point data compiled from LookChem.com for the TEST software"; 
		} else if (datasetName2.equals(DevQsarConstants.BCF)) {
			return("Bioconcentration factor data compiled from literature sources for the TEST software");//TODO doesnt exist			
		} else if (datasetName2.equals(DevQsarConstants.DEV_TOX)) {
			return("Developmental toxicity data compiled from Arena et al for the TEST software");//TODO doesnt exist
		} else if (datasetName2.equals(DevQsarConstants.MUTAGENICITY)) {
			return("Ames mutagenicity data  compiled from Hansen et al for the TEST software");//TODO doesnt exist

		} else  {
			return datasetName2;
		} 
			

	}

	
	
	String getPropertyNameDB(String propertyName) {
		
		if (propertyName.equals("Fathead minnow LC50 (96 hr)")) {
			return DevQsarConstants.LC50;
		} else if (propertyName.equals("Daphnia magna LC50 (48 hr)")) {
			return DevQsarConstants.LC50DM;
		} else if (propertyName.equals("T. pyriformis IGC50 (48 hr)")) {
			return DevQsarConstants.IGC50;
		} else if (propertyName.equals("Oral rat LD50")) {
			return DevQsarConstants.LD50;
		} else if (propertyName.equals("Developmental Toxicity")) {
			return DevQsarConstants.DEV_TOX;
		} else if (propertyName.equals("Normal boiling point")) { 
			return DevQsarConstants.BOILING_POINT;
		} else if (propertyName.contains("Bioconcentration factor")) {
			return DevQsarConstants.BCF;			
		} else if (propertyName.contains(" at 25")) { 
			propertyName=propertyName.substring(0,propertyName.indexOf(" at 25"));
			return propertyName; 
		} else  {
			return propertyName;
		} 
			

	}
	

	String getPropertyDescription(String propertyNameDB) {
		
		if (propertyNameDB.equals(DevQsarConstants.LC50)) {
			return "96 hour fathead minnow LC50: the concentration of the test chemical in water that causes 50% of fathead minnow to die after 96 hours";
		} else if (propertyNameDB.equals(DevQsarConstants.LC50DM)) {
			return("48 hour Daphnia magna LC50: the concentration of the test chemical in water that causes 50% of Daphnia magna to die after 48 hours");
		} else if (propertyNameDB.equals(DevQsarConstants.IGC50)) {
			return("48 hour Tetrahymena pyriformis  IGC50: the concentration of the test chemical in water that causes 50% growth inhibition to Tetrahymena pyriformis after 48 hours");
		} else if (propertyNameDB.equals(DevQsarConstants.LD50)) {
			return("Oral rat LD50: the amount of chemical that causes 50% of rats to die after oral ingestion");
		} else if (propertyNameDB.equals(DevQsarConstants.BCF)) {
			return "Bioconcentration factor: the ratio of the chemical concentration in fish as a result of absorption via the respiratory surface to that in water at steady state";
		} else if (propertyNameDB.equals(DevQsarConstants.DEV_TOX)) {
			return "Developmental Toxicity: whether or not a chemical causes developmental toxicity effects to humans or animals";
		} else if (propertyNameDB.equals(DevQsarConstants.MUTAGENICITY)) {
			return "Ames Mutagenicity: A compound is positive for mutagenicity if it induces revertant colony growth in any strain of Salmonella typhimurium";
		} else if (propertyNameDB.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
			return "Henry's law volatility constant (Hv). A common way to define a Hv is dividing the partial pressure by the aqueous-phase concentration";
		} else if (propertyNameDB.equals(DevQsarConstants.WATER_SOLUBILITY)) {
			return "The amount of a chemical that will dissolve in liquid water to form a homogeneous solution";
		} else if (propertyNameDB.equals(DevQsarConstants.BOILING_POINT)) {
			return "Temperature at which a chemical changes state from liquid to vapor at a given pressure";
		} else if (propertyNameDB.equals(DevQsarConstants.MELTING_POINT)) {
			return "The temperature at which a chemical changes state from solid to liquid";
		} else if (propertyNameDB.equals(DevQsarConstants.FLASH_POINT)) {
			return "The lowest temperature at which a chemical can vaporize to form an ignitable mixture in air";
		} else if (propertyNameDB.equals(DevQsarConstants.VAPOR_PRESSURE)) {
			return "The pressure exerted by a vapor in thermodynamic equilibrium with the liquid phase in a closed system at a given temperature";
		} else if (propertyNameDB.equals(DevQsarConstants.DENSITY)) {
			return "The mass per unit volume";
		} else if (propertyNameDB.equals(DevQsarConstants.SURFACE_TENSION)) {
			return "A property of the surface of a liquid (dyn/cm) that allows it to resist an external force";
		} else if (propertyNameDB.equals(DevQsarConstants.ESTROGEN_RECEPTOR_BINDING)) {
			return "Whether or not a chemical binds to the estrogen receptor";
		} else if (propertyNameDB.equals(DevQsarConstants.ESTROGEN_RECEPTOR_RBA)) {
			return "Binding to the estrogen receptor relative to 17ß-Estradiol (E2)";
		} else if (propertyNameDB.equals(DevQsarConstants.THERMAL_CONDUCTIVITY)) {
			return "The property of a material reflecting its ability to conduct heat";
		} else if (propertyNameDB.equals(DevQsarConstants.VISCOSITY)) {
			return "A measure of the resistance of a fluid to flow defined as the proportionality constant between shear rate and shear stress";
		} else {
			return propertyNameDB;
		} 
			

	}

	
	String getDatasetUnitsContributorName(String dataset_name) {

		switch (dataset_name.replace(" TEST", "")) {

		case "LC50":
		case "LC50DM":
		case "IGC50":
		case DevQsarConstants.WATER_SOLUBILITY:
			return "MOLAR";
		case "LD50":
			return "MOL_KG";
		case "BCF":
			return "L_KG";
		case DevQsarConstants.VAPOR_PRESSURE:
			return "MMHG";
		case DevQsarConstants.VISCOSITY:
			return "CP";
		case "Estrogen Receptor RBA":
			return "DIMENSIONLESS";
		
		case DevQsarConstants.MUTAGENICITY:
		case DevQsarConstants.DEV_TOX:
		case "Estrogen Receptor Binding":	
			return "BINARY";
		case DevQsarConstants.DENSITY:
			return "G_CM3";
		case DevQsarConstants.MELTING_POINT:
		case DevQsarConstants.BOILING_POINT:
		case DevQsarConstants.FLASH_POINT:
			return "DEG_C";
		case DevQsarConstants.SURFACE_TENSION:
			return "CP";
		case DevQsarConstants.THERMAL_CONDUCTIVITY:
			return "MW_MK";
		default:
			System.out.println("*** Unknown units for "+dataset_name);
			return "unknown units";	
		}

	}
	
	String getUnitAbbrev(String unitsNameInConstants)  {
		
		try {
			DevQsarConstants d=new DevQsarConstants();
			
			Field myField = d.getClass().getField(unitsNameInConstants);
			
			return (String)myField.get(d);

		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return "";
		
	}
	
	
	String getDatasetUnitsName(String dataset_name) {

		switch (dataset_name.replace(" TEST", "")) {

		case "LC50":
		case "LC50DM":
		case "IGC50":
		case DevQsarConstants.WATER_SOLUBILITY:
			return "NEG_LOG_M";
		case "LD50":
			return "NEG_LOG_MOL_KG";
		case "BCF":
			return "LOG_L_KG";
		case DevQsarConstants.VAPOR_PRESSURE:
			return "LOG_MMHG";
		case DevQsarConstants.VISCOSITY:
			return "LOG_CP";
		case "Estrogen Receptor RBA":
			return "DIMENSIONLESS";
		case DevQsarConstants.MUTAGENICITY:
		case DevQsarConstants.DEV_TOX:
		case "Estrogen Receptor Binding":	
			return "BINARY";
		case DevQsarConstants.DENSITY:
			return "G_CM3";
		case DevQsarConstants.MELTING_POINT:
		case DevQsarConstants.BOILING_POINT:
		case DevQsarConstants.FLASH_POINT:
			return "DEG_C";
		case DevQsarConstants.SURFACE_TENSION:
			return "LOG_CP";
		case DevQsarConstants.THERMAL_CONDUCTIVITY:
			return "MW_MK";

		default:
			System.out.println("*** Unknown units for "+dataset_name);
			return "unknown units";	
		}

	}
	
	PredictionDashboard convertPredictionResultsToPredictionDashboard(PredictionResults pr,String versionTEST,HashMap<String,Model>htModels) {

		PredictionDashboard pd=new PredictionDashboard();
		
		try {

			PredictionResultsPrimaryTable pt=pr.getPredictionResultsPrimaryTable();

			try {

				String propertyName=pr.getEndpoint();
				String datasetName=getDatasetName(propertyName);
				
				pd.setModel(htModels.get(datasetName));
				pd.setDtxsid(pr.getDTXSID());
				pd.setDtxcid(pr.getDTXCID());
				pd.setSmiles(pr.getSmiles());
				pd.setCreatedBy(lanId);

				if (pr.getError()!=null && !pr.getError().isBlank()) {
					pd.setPredictionError(pr.getError());
				} else {
					if (pr.isBinaryEndpoint()) {
						if (pt.getPredToxValue().equals("N/A")) {
							pd.setPredictionError(pt.getMessage());
						} else {
							pd.setPredictionValue(Double.parseDouble(pt.getPredToxValue()));
							pd.setPredictionString(pt.getPredValueEndpoint());
						}
					} else if (pr.isLogMolarEndpoint()) {

						if (pt.getPredToxValue().equals("N/A")) {
							pd.setPredictionError(pt.getMessage());
						} else {
							if (propertyName.equals("Fathead minnow LC50 (96 hr)") || propertyName.equals("Daphnia magna LC50 (48 hr)") || propertyName.equals("T. pyriformis IGC50 (48 hr)") || propertyName.equals("Water solubility at 25°C")) {								
								//Convert to M:
								pd.setPredictionValue(Math.pow(10.0,-Double.parseDouble(pt.getPredToxValue())));
							} else if (propertyName.equals("Oral rat LD50")) {
								//Convert to mol/kg:
								pd.setPredictionValue(Math.pow(10.0,-Double.parseDouble(pt.getPredToxValue())));
							} else if (propertyName.equals("Bioconcentration factor")) {
								//Convert to L/kg:
								pd.setPredictionValue(Math.pow(10.0,Double.parseDouble(pt.getPredToxValue())));
							} else if (propertyName.contains("Vapor pressure")) {
								//Convert mmHg:
								pd.setPredictionValue(Math.pow(10.0,Double.parseDouble(pt.getPredToxValue())));
							} else if (propertyName.contains("Viscosity")) {
								//Convert to cP:
								pd.setPredictionValue(Math.pow(10.0,Double.parseDouble(pt.getPredToxValue())));
							} else if (propertyName.equals("Estrogen Receptor RBA")) {
								pd.setPredictionValue(Math.pow(10.0,Double.parseDouble(pt.getPredToxValue())));
							}
						}
					} else {

						if (pt.getPredToxValMass().equals("N/A")) {
							pd.setPredictionError(pt.getMessage());
						} else {
							pd.setPredictionValue(Double.parseDouble(pt.getPredToxValMass()));
						}
					}
				}

				if (pd.getPredictionError()!=null) {
					if (pd.getPredictionError().equals("No prediction could be made")) {
						pd.setPredictionError("No prediction could be made due to applicability domain violation");
					} else if (pd.getPredictionError().contains("could not parse")) {
						pd.setPredictionError("Could not parse smiles");	
					}
				}

//				System.out.println(Utilities.gson.toJson(pd));
			} catch (Exception ex) {
				//					System.out.println(gson.toJson(pr));
				ex.printStackTrace();
			}



		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return pd;
	}

	
	

	void createDatasets() {
		
		
		
		for (String propertyName:propertyNames) {
			String datasetName=getDatasetName(propertyName);
			

			String unitName=getDatasetUnitsName(datasetName);
			String unitContributorName=getDatasetUnitsContributorName(datasetName);
			
			Unit unit=createUnit(unitName);
			Unit unitContributor=createUnit(unitContributorName);
			
			String propertyNameDB=getPropertyNameDB(propertyName);
			String propertyDescriptionDB=getPropertyDescription(propertyNameDB);
			
			Property property=createProperty(propertyNameDB, propertyDescriptionDB);
			
			String datasetDescription=getDatasetDescription(datasetName);
			
//			System.out.println(datasetName+"\t"+datasetDescription);

//			System.out.println(propertyName+"\t"+propertyNameDB+"\t"+datasetName+"\t"+unitContributorName);

			String dsstoxMappingStrategy="CASRN";
			
			
			
			Dataset dataset=new Dataset(datasetName, datasetDescription, property, unit, unitContributor,
					dsstoxMappingStrategy, lanId);
			
			createDataset(dataset);
			
			
//			System.out.println(Utilities.gson.toJson(dataset));
			
			
		}
		
	}

	private void createDataset(Dataset dataset) {
		if (datasetService.findByName(dataset.getName())!=null) 
			return;

		System.out.print("Need to create dataset for "+dataset.getName()+"...");
		datasetService.create(dataset);
		System.out.println("done");

	}

	private Property createProperty(String propertyNameDB, String propertyDescriptionDB) {
		Property property=propertyService.findByName(propertyNameDB);
		
		if (property!=null) {
//			System.out.println("Have dataset property:\t"+property.getName()+"\t"+property.getDescription());
		} else {
//				System.out.println("Creating property for "+propertyNameDB);
			
			ExpPropProperty propertyExpProp=propertyServiceExpProp.findByPropertyName(propertyNameDB);
			
			if (propertyExpProp!=null) {
//					System.out.println("we have exp_prop property="+propertyNameDB);
				property=Property.fromExpPropProperty(propertyExpProp, lanId);
				propertyService.create(property);
			} else {
				System.out.print("Need to create property for "+propertyNameDB+"\t"+propertyDescriptionDB+"...");;
				property=new Property(propertyNameDB,propertyDescriptionDB,lanId);
				property=propertyService.create(property);
				System.out.println("done");
			}
		}
		return property;
	}
	
	
	private Unit createUnit(String unitName) {
		Unit unit=unitService.findByName(unitName);
		
		if (unit!=null) {
//			System.out.println("Have unit:\t"+unit.getName()+"\t"+unit.getAbbreviation());
		} else {
//				System.out.println("Creating property for "+propertyNameDB);
			ExpPropUnit unitExpProp=unitServiceExpProp.findByName(unitName);
			
			if (unitExpProp!=null) {
//					System.out.println("we have exp_prop property="+propertyNameDB);
				unit=Unit.fromExpPropUnit(unitExpProp, lanId);
				unitService.create(unit);
			} else {
				System.out.print("Need to create unit for "+unitName+"...");
				unit=new Unit(unitName,getUnitAbbrev(unitName),lanId);
				unit=unitService.create(unit);
				System.out.println("done");
			}
		}
		return unit;
	}
	
	
	public static void main(String[] args) {
		PredictionDashboardScriptTEST pds=new PredictionDashboardScriptTEST();

//		String filePathJson="reports/sample_predictions.json";
//		String SoftwareVersion = "5.1.4";
//		pds.runFromSampleJsonFileHashtable(filePathJson,SoftwareVersion);

		String filePathJson="reports/TEST_results_all_endpoints_snapshot_compounds1.json";
		String SoftwareVersion = "5.1.3";
		pds.runFromSampleJsonFile(filePathJson,SoftwareVersion);
		
//		pds.runFromSampleJsonFile(filePathJson,SoftwareVersion);
		
//		pds.createDatasets();

		
		//TODO make a new sample file that is ran from SDF that has cids
		//TODO create createSQL (List<PredictionDashboard> predictions)- this way you can create predictions which arent in the models table
		//TODO make SQL query to assemble the results for displaying on dashboard...
	}

	

}
