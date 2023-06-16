package gov.epa.databases.dev_qsar;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.web_services.embedding_service.CalculationInfo;


public class DevQsarConstants {
	


	public Gson gson;
	
	// Numerical constants for processing and modeling
	// Fraction agreement required to map a DSSTox conflict
	public static final Double CONFLICT_FRAC_AGREE = 1.0;
	// Fraction agreement required to merge binary data points
	public static final Double BINARY_FRAC_AGREE = 0.8;
	// Cutoff for binary classification
	public static final Double BINARY_CUTOFF = 0.5;
	
	// Multiple of dataset stdev required to exclude a property value based on its stdev
	public static final Double STDEV_WIDTH_TOLERANCE = 3.0;
	// Range tolerance values
	public static final Double LOG_RANGE_TOLERANCE = 1.0;
	public static final Double TEMP_RANGE_TOLERANCE = 10.0;
	public static final Double DENSITY_RANGE_TOLERANCE = 0.1;
	public static final Double ZERO_TOLERANCE = Math.pow(10.0, -6.0);
	
	// Max realistic water solubility (in g/L)

	public static final Double MIN_WATER_SOLUBILITY_MOLAR = 1.0E-14;
	public static final Double MAX_WATER_SOLUBILITY_MOLAR = 1.0e2;
	
	public static final Double MIN_WATER_SOLUBILITY_G_L = 1.0E-11;
	public static final Double MAX_WATER_SOLUBILITY_G_L = 990.0;

	public static final Double MIN_HENRYS_LAW_CONSTANT_ATM_M3_MOL = 1.0e-13;
	public static final Double MAX_HENRYS_LAW_CONSTANT_ATM_M3_MOL = 1.0e2;
	
	public static final Double MIN_LOG_KOW = -6.0;
	public static final Double MAX_LOG_KOW = 11.0;

	public static final Double MIN_MELTING_POINT_C = -250.0;
	public static final Double MAX_MELTING_POINT_C = 550.0;

	public static final Double MIN_BOILING_POINT_C = -150.0;
	public static final Double MAX_BOILING_POINT_C = 800.0;
	
	public static final Double MIN_VAPOR_PRESSURE_MMHG = 1.0e-14;
	public static final Double MAX_VAPOR_PRESSURE_MMHG = 1.0e6;

	
	// Data folder path
	public static final String OUTPUT_FOLDER_PATH = "data" + File.separator + "dev_qsar" + File.separator + "output";
	
	// Servers for other web services
	public static final String SERVER_819 = "http://v2626umcth819.rtord.epa.gov";
	public static final String SERVER_LOCAL = "http://localhost";
	
	// Ports for other web services
	public static final int PORT_DEFAULT = 5000;
	public static final int PORT_STANDARDIZER_OPERA = 5001;
	public static final int PORT_TEST_DESCRIPTORS = 5002;
	public static final int PORT_JAVA_MODEL_BUILDING = 5003;
	public static final int PORT_PYTHON_MODEL_BUILDING = 5004;
	public static final int PORT_REPRESENTATIVE_SPLIT = 5005;
	public static final int PORT_OUTLIER_DETECTION = 5006;
	public static final int PORT_STANDARDIZER_JAVA = 5010;
	
	// DSSTox mapping strategies
	public static final String MAPPING_BY_CASRN = "CASRN";
	public static final String MAPPING_BY_DTXCID = "DTXCID";
	public static final String MAPPING_BY_DTXSID = "DTXSID";
	public static final String MAPPING_BY_LIST = "LIST";
	
	// Standardizer types
	public static final String QSAR_READY = "QSAR_READY";
	public static final String MS_READY = "MS_READY";
	
	// Standardizers
	public static final String STANDARDIZER_NONE = "NONE"; // Default QSAR-ready SMILES from DSSTox
	public static final String STANDARDIZER_OPERA = "OPERA";
	public static final String STANDARDIZER_SCI_DATA_EXPERTS = "SCI_DATA_EXPERTS";
	
	// Descriptor set names
	public static final String DESCRIPTOR_SET_TEST = "T.E.S.T. 5.1";
	public static final String DESCRIPTOR_SET_WEBTEST = "WebTEST-default";
	public static final String DESCRIPTOR_SET_PADEL_SINGLE = "Padelpy webservice single";
	public static final String DESCRIPTOR_SET_PADEL_BATCH = "Padelpy_batch";
	
	public static final String Applicability_Domain_TEST_Embedding_Cosine = "TEST Cosine Similarity Embedding Descriptors";
	public static final String Applicability_Domain_TEST_Embedding_Euclidean = "TEST Euclidean Distance Embedding Descriptors";
	public static final String Applicability_Domain_TEST_All_Descriptors_Cosine = "TEST Cosine Similarity All Descriptors";
	public static final String Applicability_Domain_OPERA_local_index = "OPERA Local Index";

	
	// Property names
	public static final String WATER_SOLUBILITY = "Water solubility";
	public static final String HENRYS_LAW_CONSTANT = "Henry's law constant";
	public static final String MELTING_POINT = "Melting point";
	public static final String LOG_KOW = "Octanol water partition coefficient";
	public static final String VAPOR_PRESSURE = "Vapor pressure";
	public static final String DENSITY = "Density";
	public static final String BOILING_POINT = "Boiling point";
	public static final String FLASH_POINT = "Flash point";
	public static final String VISCOSITY = "Viscosity";
	public static final String SURFACE_TENSION = "Surface tension";
	public static final String THERMAL_CONDUCTIVITY="Thermal conductivity";
	
	public static final String MOLAR_REFRACTIVITY = "Molar refractivity";
	public static final String MOLAR_VOLUME = "Molar volume";
	public static final String POLARIZABILITY = "Polarizability";
	public static final String PARACHOR = "Parachor";
	public static final String INDEX_OF_REFRACTION = "Index of refraction";
	public static final String DIELECTRIC_CONSTANT = "Dielectric constant";
	public static final String LogD_pH_7_4 = "LogD at pH=7.4";
	
	
	public static final String ESTROGEN_RECEPTOR_BINDING = "Estrogen receptor binding";
	public static final String ESTROGEN_RECEPTOR_RBA = "Estrogen receptor relative binding affinity";
	
	public static final String PKA = "pKa";
	public static final String PKA_A = "pKa_a";
	public static final String PKA_B = "pKa_b";

	public static final String BCF = "Bioconcentration factor";
	public static final String LOG_BCF = "Log10(Bioconcentration factor)";
	
	public static final String LOG_OH = "LogOH";
	public static final String LOG_KOC = "LogKOC";
	public static final String LOG_HALF_LIFE = "LogHalfLife";
	public static final String LOG_KM_HL = "LogKmHL";
	public static final String LOG_KOA = "LogKOA";
	public static final String LOG_BCF_FISH_WHOLEBODY = "LogBCF_Fish_WholeBody";

	//Old versions
	public static final String MUTAGENICITY ="Mutagenicity";
	public static final String LC50 ="LC50";
	public static final String LC50DM ="LC50DM";
	public static final String IGC50 ="IGC50";
	public static final String LD50 ="LD50";
	public static final String LLNA ="LLNA";
	public static final String DEV_TOX ="DevTox";
	
	
	//New versions for dashboard
	public static final String FORTY_EIGHT_HR_IGC50 ="48 hour Tetrahymena pyriformis IGC50";
	public static final String NINETY_SIX_HOUR_LC50 ="96 hour fathead minnow LC50";
	public static final String FORTY_EIGHT_HR_DM_LC50 ="48 hour Daphnia magna LC50";
	public static final String ORAL_RAT_LD50="Oral rat LD50";
	public static final String AMES_MUTAGENICITY ="Ames Mutagenicity";
	public static final String DEVELOPMENTAL_TOXICITY ="Developmental toxicity";
	public static final String LOCAL_LYMPH_NODE_ASSAY ="Local lymph node assay";

	
	// Unit names
	public static final String DIMENSIONLESS = "Dimensionless";
	public static final String BINARY = "Binary";
	public static final String G_L = "g/L";
	public static final String MG_L = "mg/L";
	public static final String MG_KG = "mg/kg";
	public static final String L_KG = "L/kg";
	public static final String DEG_C = "C";
	public static final String LOG_UNITS = "Log units";
	public static final String MOLAR = "M";
	public static final String LOG_M = "log10(M)";
	public static final String NEG_LOG_M = "-log10(M)";
	public static final String NEG_LOG_MOL_KG = "-log10(mol/kg)";
	public static final String MOL_KG = "mol/kg";
	public static final String LOG_L_KG = "log10(L/kg)";
	public static final String G_CM3 = "g/cm3";
	
	public static final String CM3 = "cm^3";
	public static final String CUBIC_ANGSTROM = "Å^3";

//	public static final String CM3 = "cm3";
//	public static final String CUBIC_ANGSTROM = "Å3";

	
	public static final String ATM_M3_MOL = "atm-m3/mol";
	public static final String NEG_LOG_ATM_M3_MOL = "-log10(atm-m3/mol)";
	public static final String LOG_ATM_M3_MOL = "log10(atm-m3/mol)";
	public static final String MMHG = "mmHg";
	public static final String NEG_LOG_MMHG = "-log10(mmHg)";
	public static final String LOG_MMHG = "log10(mmHg)";
	public static final String LOG_HR = "log10(hr)";
	public static final String LOG_DAYS = "log10(days)";
	public static final String DAYS = "days";
	public static final String HOUR = "hr";
	public static final String LOG_CM3_MOLECULE_SEC="log10(cm3/molecule-sec)";
	public static final String CM3_MOLECULE_SEC="cm3/molecule-sec";
	
	public static final String MW_MK="mW/mK";
	public static final String DYN_CM="dyn/cm";
	
	public static final String LOG_CP = "log10(cP)";
	public static final String CP = "cP";
	
	
	// Integer codes for train/test splitting
	public static final Integer TRAIN_SPLIT_NUM = 0;
	public static final Integer TEST_SPLIT_NUM = 1;
	
	// Splitting names
	public static final String SPLITTING_RND_REPRESENTATIVE = "RND_REPRESENTATIVE";
	public static final String SPLITTING_TRAINING_CROSS_VALIDATION = "TRAINING_CROSS_VALIDATION";
	public static final String SPLITTING_OPERA = "OPERA";
	public static final String SPLITTING_TEST = "TEST";
	
	// Input types for DSSTox queries
	public static final String INPUT_CASRN = "CASRN";
	public static final String INPUT_OTHER_CASRN = "OTHER_CASRN";
	public static final String INPUT_PREFERRED_NAME = "PREFERRED_NAME";
	public static final String INPUT_SYNONYM = "SYNONYM";
	public static final String INPUT_NAME2STRUCTURE = "NAME2STRUCTURE";
	public static final String INPUT_MAPPED_IDENTIFIER = "MAPPED_IDENTIFIER";
	public static final String INPUT_DTXCID = "DTXCID";
	public static final String INPUT_DTXSID = "DTXSID";
	public static final String INPUT_SMILES = "SMILES";
	public static final String INPUT_INCHIKEY = "INCHIKEY";
	
	// QSAR method codes for modeling web services
	public static final String KNN = "knn";
	public static final String XGB = "xgb";
	public static final String SVM = "svm";
	public static final String RF = "rf";
	public static final String DNN = "dnn";
	public static final String REG = "reg";
	public static final String CONSENSUS = "consensus";
	
	// Statistic names in qsar_models database
	public static final String Q2_TEST = "Q2_Test";
	public static final String R2_TRAINING = "R2_Training";
	public static final String COVERAGE = "Coverage";
	public static final String MAE = "MAE";
	public static final String RMSE = "RMSE";
	public static final String BALANCED_ACCURACY = "BA";
	public static final String SENSITIVITY = "SN";
	public static final String SPECIFICITY = "SP";
	public static final String CONCORDANCE = "Concordance";
	public static final String POS_CONCORDANCE = "PosConcordance";
	public static final String NEG_CONCORDANCE = "NegConcordance";
	public static final String PEARSON_RSQ = "PearsonRSQ";
	public static final String TAG_TEST = "_Test";
	public static final String TAG_TRAINING = "_Training";

	public static final String SOURCE_WEBTEST = "WebTEST2.0";

	


	
	// Acceptable atoms in structures for modeling
	public static HashSet<String> getAcceptableAtomsSet() {
		List<String> list = Arrays.asList("C", "H", "O", "N", "F", "Cl", "Br", "I", "S", "P", "Si", "As", "Hg", "Sn");
		return new HashSet<String>(list);
	}
	
	// Final modeling unit names for each property name
	public static HashMap<String, String> getDatasetFinalUnitsNameMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MELTING_POINT, "DEG_C");
		map.put(BOILING_POINT, "DEG_C");
		map.put(FLASH_POINT, "DEG_C");
		map.put(LOG_KOW, "LOG_UNITS");
		map.put(PKA, "LOG_UNITS");
		map.put(PKA_A, "LOG_UNITS");
		map.put(PKA_B, "LOG_UNITS");
		map.put(LOG_BCF_FISH_WHOLEBODY, "LOG_L_KG");
		map.put(WATER_SOLUBILITY, "NEG_LOG_M");
		map.put(HENRYS_LAW_CONSTANT, "NEG_LOG_ATM_M3_MOL");
		map.put(VAPOR_PRESSURE, "LOG_MMHG");
		map.put(DENSITY, "G_CM3");
		map.put(LOCAL_LYMPH_NODE_ASSAY, "BINARY");
		map.put(SURFACE_TENSION,"LOG_CP");
		map.put(THERMAL_CONDUCTIVITY,"MW_MK");
		map.put(VISCOSITY,"LOG_CP");
		
		map.put(MUTAGENICITY,"BINARY");
				
		map.put(DEV_TOX,"BINARY");
		map.put(LC50, "NEG_LOG_M");
		
		map.put(LC50DM, "NEG_LOG_M");
		map.put(IGC50, "NEG_LOG_M");
		map.put(LD50, "NEG_LOG_MOL_KG");
		
		map.put(AMES_MUTAGENICITY,"BINARY");
		map.put(DEVELOPMENTAL_TOXICITY,"BINARY");
		
		map.put(NINETY_SIX_HOUR_LC50, "NEG_LOG_M");
		map.put(FORTY_EIGHT_HR_DM_LC50, "NEG_LOG_M");
		map.put(FORTY_EIGHT_HR_IGC50, "NEG_LOG_M");
		map.put(ORAL_RAT_LD50, "NEG_LOG_MOL_KG");
		map.put(BCF, "LOG_L_KG");
		
		map.put(ESTROGEN_RECEPTOR_RBA,"DIMENSIONLESS");
		map.put(ESTROGEN_RECEPTOR_BINDING,"BINARY");
		
		return map;
	}
	

	public static String getPropertyDescription(String propertyNameDB) {
		
		if (propertyNameDB.equals(DevQsarConstants.NINETY_SIX_HOUR_LC50)) {
			return "The concentration of the test chemical in water that causes 50% of fathead minnow to die after 96 hours";
		} else if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_DM_LC50)) {
			return("The concentration of the test chemical in water that causes 50% of Daphnia magna to die after 48 hours");
		} else if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_IGC50)) {
			return("The concentration of the test chemical in water that causes 50% growth inhibition to Tetrahymena pyriformis after 48 hours");
		} else if (propertyNameDB.equals(DevQsarConstants.ORAL_RAT_LD50)) {
			return("The amount of chemical that causes 50% of rats to die after oral ingestion");
		} else if (propertyNameDB.equals(DevQsarConstants.BCF)) {
			return "Bioconcentration factor: the ratio of the chemical concentration in fish as a result of absorption via the respiratory surface to that in water at steady state";
		} else if (propertyNameDB.equals(DevQsarConstants.DEVELOPMENTAL_TOXICITY)) {
			return "Developmental Toxicity: whether or not a chemical causes developmental toxicity effects to humans or animals";
		} else if (propertyNameDB.equals(DevQsarConstants.AMES_MUTAGENICITY)) {
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
		} else if (propertyNameDB.equals(DevQsarConstants.LOG_KOW)) {
			return "Log10 of the the ratio of the concentration of a chemical in n-octanol and water at equilibrium at a specified temperature";
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
		} else if (propertyNameDB.equals(DevQsarConstants.INDEX_OF_REFRACTION)) {
			return "Measure of the bending of a ray of light when passing from one medium into another";
		} else if (propertyNameDB.equals(DevQsarConstants.DIELECTRIC_CONSTANT)) {
			return "The permittivity of a material expressed as a ratio with the electric permittivity of a vacuum";
		} else if (propertyNameDB.equals(DevQsarConstants.MOLAR_VOLUME)) {
			return "The ratio of the volume occupied by a substance to the amount of substance, usually given at a given temperature and pressure";
		} else if (propertyNameDB.equals(DevQsarConstants.MOLAR_REFRACTIVITY)) {
			return "A measure of the total polarizability of a mole of a substance and is dependent on the temperature, the index of refraction, and the pressure";
		} else if (propertyNameDB.equals(DevQsarConstants.POLARIZABILITY)) {
			return "The tendency of matter, when subjected to an electric field, to acquire an electric dipole moment in proportion to that applied field";
		} else if (propertyNameDB.equals(DevQsarConstants.PARACHOR)) {
			return "The molecular weight of the liquid multiplied by the fourth root of its surface tension divided by the difference between the densities of the liquid and the vapour in equilibrium with it";
		} else if (propertyNameDB.equals(DevQsarConstants.LogD_pH_7_4)) {
			return "Octanol water parition coefficient at a pH of 7.4";

		} else {
			return "*"+propertyNameDB;
		} 
	}
	
	
	
	public static HashMap<String, String> getContributorUnitsNameMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MELTING_POINT, "DEG_C");//Changed it to add quotes around the unit names so it stores the unit name and not the abbreviation (TMM, 6/2/23)
		map.put(BOILING_POINT, "DEG_C");
		map.put(FLASH_POINT, "DEG_C");
		
		map.put(LOG_KOW, "LOG_UNITS");
		map.put(WATER_SOLUBILITY, "MOLAR");//**
		map.put(HENRYS_LAW_CONSTANT, "ATM_M3_MOL");//***
		map.put(VAPOR_PRESSURE, "MMHG");//***
		map.put(DENSITY, "G_CM3");
		map.put(THERMAL_CONDUCTIVITY,"MW_MK");
		map.put(VISCOSITY,"CP");
		map.put(PKA, "LOG_UNITS");
		map.put(PKA_A, "LOG_UNITS");
		map.put(PKA_B, "LOG_UNITS");
		map.put(SURFACE_TENSION,"CP");

		//Percepta properties:
		map.put(MOLAR_VOLUME, "CM3");
		map.put(MOLAR_REFRACTIVITY, "CM3");
		
		map.put(INDEX_OF_REFRACTION, "DIMENSIONLESS");
		map.put(POLARIZABILITY, "CUBIC_ANGSTROM");
		map.put(LogD_pH_7_4, "LOG_UNITS");
		
		//https://www.sciencedirect.com/topics/chemistry/parachor#:~:text=The%20conventional%20numerical%20values%20of,(cm3%2Fmol).
		//(erg/cm2)1/4 × (cm3/mol)
		map.put(PARACHOR, "DIMENSIONLESS");//null in prod_chemprop, see https://www.epj-conferences.org/articles/epjconf/pdf/2015/11/epjconf_efm2014_02054.pdf
		map.put(DIELECTRIC_CONSTANT, "DIMENSIONLESS");//https://byjus.com/physics/dielectric-constant/#what-is-dielectric-constant		

		
		map.put(LOG_BCF_FISH_WHOLEBODY, "LOG_L_KG");//TODO is this right?
		map.put(BCF, "L_KG");
		map.put(LOCAL_LYMPH_NODE_ASSAY, "BINARY");
		map.put(NINETY_SIX_HOUR_LC50,"MOLAR");
		map.put(FORTY_EIGHT_HR_DM_LC50,"MOLAR");
		map.put(FORTY_EIGHT_HR_IGC50, "MOLAR");
		map.put(ORAL_RAT_LD50, "MOL_KG");
		map.put(AMES_MUTAGENICITY,"BINARY");
		map.put(DEVELOPMENTAL_TOXICITY,"BINARY");
		map.put(ESTROGEN_RECEPTOR_RBA,"DIMENSIONLESS");
		map.put(ESTROGEN_RECEPTOR_BINDING,"BINARY");
		
		
		return map;
	}

	
//	public final String GeneticAlgorithmDefaults = gson.toJson(CalculationInfo.createDefault());
}
