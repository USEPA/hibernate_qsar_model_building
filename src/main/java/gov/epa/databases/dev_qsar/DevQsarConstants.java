package gov.epa.databases.dev_qsar;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;


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

//	public static final Double MIN_WATER_SOLUBILITY_MOLAR = 1.0E-14;
//	public static final Double MAX_WATER_SOLUBILITY_MOLAR = 1.0e2;
	
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
	public static final int PORT_OUTLIER_DETECTION = 5006;
	public static final int PORT_REPRESENTATIVE_SPLIT = 5005;
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
	public static final String WATER_SOLUBILITY = "Water solubility";//OPERA
	public static final String HENRYS_LAW_CONSTANT = "Henry's law constant";//OPERA
	public static final String MELTING_POINT = "Melting point";//OPERA
	public static final String LOG_KOW = "Octanol water partition coefficient";//OPERA
	public static final String VAPOR_PRESSURE = "Vapor pressure";//OPERA
	public static final String DENSITY = "Density";
	public static final String BOILING_POINT = "Boiling point";//OPERA
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
	
	public static final String APPEARANCE = "Appearance";
		
	public static final String ESTROGEN_RECEPTOR_RBA = "Estrogen receptor relative binding affinity";
	public static final String ESTROGEN_RECEPTOR_BINDING = "Estrogen receptor binding";//OPERA
	public static final String ESTROGEN_RECEPTOR_AGONIST = "Estrogen receptor agonist";//OPERA
	public static final String ESTROGEN_RECEPTOR_ANTAGONIST = "Estrogen receptor antagonist";//OPERA

	public static final String ANDROGEN_RECEPTOR_AGONIST = "Androgen receptor agonist";//OPERA
	public static final String ANDROGEN_RECEPTOR_ANTAGONIST = "Androgen receptor antagonist";//OPERA
	public static final String ANDROGEN_RECEPTOR_BINDING = "Androgen receptor binding";//OPERA
	
	
	public static final String PKA = "pKa";

	public static final String BCF = "Bioconcentration factor";//OPERA
	public static final String LOG_BCF = "Log10(Bioconcentration factor)";
	
	public static final String LOG_OH = "LogOH";//OLD OPERA
	public static final String LOG_KOC = "LogKOC";//OLD OPERA
	public static final String LOG_HALF_LIFE = "LogHalfLife";//OLD OPERA
	
	public static final String LOG_KM_HL = "LogKmHL";
	public static final String LOG_KOA = "Octanol air partition coefficient";//OPERA
	public static final String LOG_BCF_FISH_WHOLEBODY = "LogBCF_Fish_WholeBody";
	
	//Additional OPERA properties:
	public static final String KOC = "Soil Adsorption Coefficient (Koc)";//OPERA
	public static final String KM = "Fish biotransformation half-life (Km)";//OPERA
	public static final String OH = "Atmospheric hydroxylation rate";//OPERA
	public static final String BIODEG_HL_HC = "Biodegradation half-life for hydrocarbons";//OPERA
	public static final String RBIODEG = "Ready biodegradability";//OPERA
	public static final String FUB = "Fraction unbound in human plasma";//OPERA
	public static final String RT = "Liquid chromatography retention time";//OPERA
	public static final String CLINT = "Human hepatic intrinsic clearance";//OPERA
	public static final String CACO2 = "Caco-2 permeability (logPapp)";//OPERA
	public static final String LogD_pH_7_4 = "LogD at pH=7.4";//OPERA
	public static final String LogD_pH_5_5 = "LogD at pH=5.5";//OPERA
	public static final String PKA_A = "Strongest acidic acid dissociation constant";//OPERA
	public static final String PKA_B = "Strongest basic acid dissociation constant";//OPERA

	//Old versions for building sample models
	public static final String MUTAGENICITY ="Mutagenicity";
	public static final String LC50 ="LC50";
	public static final String LC50DM ="LC50DM";
	public static final String IGC50 ="IGC50";
	public static final String LD50 ="LD50";
	public static final String LLNA ="LLNA";
	public static final String DEV_TOX ="DevTox";

	public static final String SKIN_IRRITATION = "Skin irritation";//TODO This needs to be more specific
	public static final String EYE_IRRITATION = "Eye irritation";//TODO This needs to be more specific
	public static final String EYE_CORROSION = "Eye corrosion";//TODO This needs to be more specific
	
	
	//New versions for dashboard
	public static final String NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50 ="96 hour fathead minnow LC50";
	public static final String FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50 ="48 hour Daphnia magna LC50";
	public static final String FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50 ="48 hour Tetrahymena pyriformis IGC50";
	public static final String ORAL_RAT_LD50="Oral rat LD50";//OPERA
	public static final String AMES_MUTAGENICITY ="Ames Mutagenicity";
	public static final String DEVELOPMENTAL_TOXICITY ="Developmental toxicity";
	public static final String LOCAL_LYMPH_NODE_ASSAY ="Local lymph node assay";
	


	public static final String [] TEST_SOFTWARE_PROPERTIES = {NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50,FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50,
			FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50, ORAL_RAT_LD50,BCF, AMES_MUTAGENICITY,DEVELOPMENTAL_TOXICITY,
			ESTROGEN_RECEPTOR_BINDING,ESTROGEN_RECEPTOR_RBA,BOILING_POINT,MELTING_POINT,FLASH_POINT,
			VAPOR_PRESSURE,DENSITY, SURFACE_TENSION,THERMAL_CONDUCTIVITY, VISCOSITY,WATER_SOLUBILITY};	
	
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
	public static final String PPM = "ppm";
	


	
	public static final String PCT_VOLUME="%v";
	public static final String PCT_WEIGHT="%w";
	
	public static final String TEXT="Text";
	
	//ghs data gathering constants for reference (a lot of these get converted in ghs data gathering and dont make it into units table):
//	public static final String str_mg_m3="mg/m^3";
//	public static final String str_g_m3="g/m^3";
//	public static final String str_mL_m3="mL/m^3";
//	public static final String str_mg_mL="mg/mL";
//	public static final String str_g_L="g/L";
//	public static final String str_ug_L="ug/L";
//	public static final String str_ug_mL="ug/mL";
//	public static final String str_g_100mL="g/100mL";
//	public static final String str_mg_100mL="mg/100mL";
//	public static final String str_ng_ml="ng/mL";
//	public static final String str_g_cm3="g/cm3";
//	public static final String str_kg_m3="kg/m3";
//	public static final String str_g_mL="g/mL";
//	public static final String str_kg_dm3="kg/dm3";
//	public static final String str_C="C";
//	public static final String str_F="F";
//	public static final String str_K="K";


//	public static final String str_atm_m3_mol="atm-m3/mol";
//	public static final String str_mol_m3_atm = "mol/m3-Pa";
//	public static final String str_Pa_m3_mol="Pa-m3/mol";
//	public static final String str_mmHg="mmHg";
//	public static final String str_atm="atm";
//	public static final String str_kpa="kPa";
//	public static final String str_hpa="hPa";
//	public static final String str_pa="Pa";
//	public static final String str_mbar="mbar";
//	public static final String str_bar="bar";
//	public static final String str_torr="Torr";
//	public static final String str_psi="psi";
//	public static final String str_M="M";
//	public static final String str_mM="mM";
//	public static final String str_nM="nM";
//	public static final String str_uM="uM";
//	public static final String str_log_M="log10(M)";
//	public static final String str_log_mg_L="log10(mg/L)";
//	public static final String str_log_mmHg="log10(mmHg)";
//	public static final String str_log_atm_m3_mol="log10(atm-m3/mol)";
//	public static final String str_dimensionless_H="dimensionless H";
//	public static final String str_dimensionless_H_vol="dimensionless H (volumetric)";
//	public static final String str_mg_kg="mg/kg";
//	public static final String str_g_kg="g/kg";
//	public static final String str_mL_kg="mL/kg";
//	public static final String str_iu_kg="iu/kg";
//	public static final String str_units_kg="units/kg";
//	public static final String str_mg="mg";
//	public static final String str_mg_kg_H20="mg/kg H2O";
//	public static final String str_g_Mg_H20="g/Mg H2O";
//	public static final String str_g_100g="g/100g";
//	public static final String str_mg_100g="mg/100g";
//	public static final String str_mol_m3_H20="mol/m3 H2O";
//	public static final String str_mol_kg_H20="mol/kg H2O";
//	public static final String str_kg_kg_H20="kg/kg H2O";
//	public static final String str_g_kg_H20="g/kg H2O";
//	public static final String str_ug_g_H20 ="ug/g H2O";
//	public static final String str_ug_100mL = "ug/100mL";
//	public static final String str_mg_10mL = "mg/10mL";
//	public static final String str_g_10mL = "g/10mL";
//	public static final String str_oz_gal = "oz/gal";
//	public static final String str_pii="PII";
	
	
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
	public static final String MINUTES = "min";
	
	public static final String LOG_CM3_MOLECULE_SEC="log10(cm3/molecule-sec)";
	public static final String CM3_MOLECULE_SEC="cm3/molecule-sec";
	
	public static final String LOG_CM_SEC="log10(cm/sec)";
	public static final String CM_SEC="cm/sec";
	
	public static final String UL_MIN_1MM_CELLS="ul/min/10^6 cells";

	
	
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
	
	public static final String R2 = "R2";
	public static final String Q2 = "Q2";
	
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

	public static final String SOURCE_CHEMINFORMATICS_MODULES = "Cheminformatics Modules";

	public static final String R2_TEST = R2+TAG_TEST;
	public static final String Q2_TEST = Q2+TAG_TEST;
	
	public static final String Q2_F3_TEST ="Q2_F3"+TAG_TEST;
	public static final String MAE_TEST = MAE+TAG_TEST;
	public static final String RMSE_TEST = RMSE+TAG_TEST;

	private static final String TAG_CV = "_CV";

	public static final String MAE_CV_TRAINING=MAE+TAG_CV+TAG_TRAINING;
	public static final String PEARSON_RSQ_CV_TRAINING=PEARSON_RSQ+TAG_CV+TAG_TRAINING;
	public static final String RMSE_CV_TRAINING=RMSE+TAG_CV+TAG_TRAINING;
	
	public static final String PEARSON_RSQ_TRAINING=PEARSON_RSQ+TAG_TRAINING;
	public static final String PEARSON_RSQ_TEST=PEARSON_RSQ+TAG_TEST;

	
	public static final String R2_TRAINING = R2+TAG_TRAINING;
	public static final String RMSE_TRAINING = RMSE+TAG_TRAINING;

	public static final String COVERAGE = "Coverage";
	
	public static final String BA_TRAINING=BALANCED_ACCURACY+TAG_TRAINING;
	public static final String SN_TRAINING=SENSITIVITY+TAG_TRAINING;
	public static final String SP_TRAINING=SPECIFICITY+TAG_TRAINING;

	public static final String BA_CV_TRAINING=BALANCED_ACCURACY+TAG_CV+TAG_TRAINING;
	public static final String SN_CV_TRAINING=SENSITIVITY+TAG_CV+TAG_TRAINING;
	public static final String SP_CV_TRAINING=SPECIFICITY+TAG_CV+TAG_TRAINING;

	public static final String BA_TEST=BALANCED_ACCURACY+TAG_TEST;
	public static final String SN_TEST=SENSITIVITY+TAG_TEST;
	public static final String SP_TEST=SPECIFICITY+TAG_TEST;

			
	
	// Acceptable atoms in structures for modeling
	public static HashSet<String> getAcceptableAtomsSet() {
		List<String> list = Arrays.asList("C", "H", "O", "N", "F", "Cl", "Br", "I", "S", "P", "Si", "As", "Hg", "Sn");
		return new HashSet<String>(list);
	}
	

	/**
	 * Assigns desired units for datapoint used in modeling 
	 * 
	 * @return
	 */
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
		
		map.put(NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50, "NEG_LOG_M");
		map.put(FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50, "NEG_LOG_M");
		map.put(FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50, "NEG_LOG_M");
		map.put(ORAL_RAT_LD50, "NEG_LOG_MOL_KG");
		map.put(BCF, "LOG_L_KG");
		
		map.put(ESTROGEN_RECEPTOR_RBA,"DIMENSIONLESS");
		map.put(ESTROGEN_RECEPTOR_BINDING,"BINARY");
		
		return map;
	}
	
	/**
	 * Assigns desired units for datapoint used in modeling 
	 * 
	 * @return
	 */
	public static HashMap<String, String> getDatasetFinalUnitsNameMapOPERA() {
		HashMap<String, String> map = new HashMap<String, String>();

		map.put(WATER_SOLUBILITY, "LOG_M");
		map.put(HENRYS_LAW_CONSTANT, "LOG_ATM_M3_MOL");//TODO
		map.put(MELTING_POINT, "DEG_C");
		map.put(BOILING_POINT, "DEG_C");
		map.put(VAPOR_PRESSURE, "LOG_MMHG");
		map.put(DENSITY, "G_CM3");

		map.put(LOG_KOW, "LOG_UNITS");
		map.put(LOG_KOA, "LOG_UNITS");
		map.put(LogD_pH_5_5, "LOG_UNITS");
		map.put(LogD_pH_7_4, "LOG_UNITS");
		map.put(PKA, "LOG_UNITS");
		map.put(PKA_A, "LOG_UNITS");
		map.put(PKA_B, "LOG_UNITS");

		map.put(BCF, "LOG_L_KG");
		map.put(KOC, "LOG_L_KG");

		map.put(RBIODEG, "BINARY");
		map.put(BIODEG_HL_HC, "LOG_DAYS");
		map.put(FUB, "DIMENSIONLESS");
		map.put(RT, "MINUTES");
		map.put(KM, "LOG_DAYS");
		map.put(OH, "LOG_CM3_MOLECULE_SEC");
		map.put(CACO2, "LOG_CM_SEC");
		map.put(CLINT, "UL_MIN_1MM_CELLS");
		
		
		map.put(ESTROGEN_RECEPTOR_AGONIST,"BINARY");
		map.put(ESTROGEN_RECEPTOR_ANTAGONIST,"BINARY");
		map.put(ESTROGEN_RECEPTOR_BINDING,"BINARY");
		
		map.put(ANDROGEN_RECEPTOR_AGONIST,"BINARY");
		map.put(ANDROGEN_RECEPTOR_ANTAGONIST,"BINARY");
		map.put(ANDROGEN_RECEPTOR_BINDING,"BINARY");
		
		map.put(ORAL_RAT_LD50, "MG_KG");
		
		return map;
	}
	

	public static String getPropertyDescription(String propertyNameDB) {
		
		if (propertyNameDB.equals(DevQsarConstants.NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50)) {
			return "The concentration of the test chemical in water that causes 50% of fathead minnow to die after 96 hours";
		} else if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50)) {
			return("The concentration of the test chemical in water that causes 50% of Daphnia magna to die after 48 hours");
		} else if (propertyNameDB.equals(DevQsarConstants.FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50)) {
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
		} else if (propertyNameDB.equals(DevQsarConstants.LOG_KOA)) {
			return "Log10 of the the ratio of the concentration of a chemical in n-octanol and air at equilibrium at a specified temperature";
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
		} else if(propertyNameDB.equals(FUB)) {
			return "Human plasma fraction unbound";
		} else if(propertyNameDB.equals(RT)) {
			return "HPLC retention time";
		} else if(propertyNameDB.equals(CLINT)) {
			return "Human hepatic intrinsic clearance";
		} else if(propertyNameDB.equals(CACO2)) {
			return "Caco-2 permeability (logPapp)";
		} else if(propertyNameDB.equals(LogD_pH_7_4)) {
			return "Octanol water partition coefficient at pH=7.4";
		} else if(propertyNameDB.equals(LogD_pH_5_5)) {
			return "Octanol water partition coefficient at pH=5.5";
		} else if(propertyNameDB.equals(PKA_A)) {
			return "strongest acidic acid dissociation constant";
		} else if(propertyNameDB.equals(PKA_B)) {
			return "strongest basic acid dissociation constant";
		} else if(propertyNameDB.equals(KM)) {
			return "The whole body primary biotransformation rate (half-life) constant for organic chemicals in fish";
		} else if(propertyNameDB.equals(KOC)) {
			return "soil adsorption coefficient of organic compounds";
		} else if(propertyNameDB.equals(OH)) {
			return "OH rate constant for the atmospheric, gas-phase reaction between photochemically produced hydroxyl radicals and organic chemicals";
		} else if(propertyNameDB.equals(RBIODEG)) {
			return "Ready biodegradability of organic chemicals";
		} else if(propertyNameDB.equals(BIODEG_HL_HC)) {
			return "biodegradation half-life for compounds containing only carbon and hydrogen";
		} else if (propertyNameDB.equals(DevQsarConstants.ESTROGEN_RECEPTOR_BINDING)) {
			return "Estrogen receptor binding";
		} else if (propertyNameDB.equals(DevQsarConstants.ESTROGEN_RECEPTOR_AGONIST)) {
			return "Estrogen receptor agonist";
		} else if (propertyNameDB.equals(DevQsarConstants.ESTROGEN_RECEPTOR_ANTAGONIST)) {
			return "Estrogen receptor antagonist";
		} else if (propertyNameDB.equals(DevQsarConstants.ANDROGEN_RECEPTOR_BINDING)) {
			return "Androgen receptor binding";
		} else if (propertyNameDB.equals(DevQsarConstants.ANDROGEN_RECEPTOR_AGONIST)) {
			return "Androgen receptor agonist";
		} else if (propertyNameDB.equals(DevQsarConstants.ANDROGEN_RECEPTOR_ANTAGONIST)) {
			return "Androgen receptor antagonist";
		} else {
			return "*"+propertyNameDB;
		}
		
	}
	
	
	/**
	 * Assigns desired units for raw experimental data 
	 * 
	 * @return
	 */
	public static HashMap<String, String> getContributorUnitsNameMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put(MELTING_POINT, "DEG_C");//Changed it to add quotes around the unit names so it stores the unit name and not the abbreviation (TMM, 6/2/23)
		map.put(BOILING_POINT, "DEG_C");
		map.put(FLASH_POINT, "DEG_C");
		map.put(WATER_SOLUBILITY, "MOLAR");//**
		map.put(HENRYS_LAW_CONSTANT, "ATM_M3_MOL");//***
		map.put(VAPOR_PRESSURE, "MMHG");//***
		map.put(DENSITY, "G_CM3");
		map.put(THERMAL_CONDUCTIVITY,"MW_MK");
		map.put(SURFACE_TENSION,"CP");
		map.put(VISCOSITY,"CP");
		
		map.put(LOG_KOW, "LOG_UNITS");
		map.put(LOG_KOA, "LOG_UNITS");
		map.put(LogD_pH_5_5, "LOG_UNITS");
		map.put(LogD_pH_7_4, "LOG_UNITS");
		map.put(PKA, "LOG_UNITS");
		map.put(PKA_A, "LOG_UNITS");
		map.put(PKA_B, "LOG_UNITS");
		
		map.put(FUB, "DIMENSIONLESS");
		map.put(RT, "MINUTES");
		map.put(RBIODEG, "BINARY");
		map.put(BIODEG_HL_HC, "DAYS");
		map.put(KM, "DAYS");
		map.put(BCF, "L_KG");
		map.put(KOC, "L_KG");
		map.put(LOG_BCF_FISH_WHOLEBODY, "LOG_L_KG");//TODO is this right? Property should just be BCF_FISH_WHOLEBODY
		map.put(OH, "CM3_MOLECULE_SEC");
		map.put(CACO2, "CM_SEC");
		map.put(CLINT, "UL_MIN_1MM_CELLS");
		
		//*********************************************************************************
		//Tox endpoints

		map.put(LOCAL_LYMPH_NODE_ASSAY, "BINARY");
		
		map.put(NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50,"MOLAR");
		map.put(FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50,"MOLAR");
		map.put(FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50, "MOLAR");		
		map.put(ORAL_RAT_LD50, "MOL_KG");

		
		map.put(AMES_MUTAGENICITY,"BINARY");
		map.put(DEVELOPMENTAL_TOXICITY,"BINARY");
		
		map.put(ESTROGEN_RECEPTOR_RBA,"DIMENSIONLESS");

		map.put(ESTROGEN_RECEPTOR_AGONIST,"BINARY");
		map.put(ESTROGEN_RECEPTOR_ANTAGONIST,"BINARY");
		map.put(ESTROGEN_RECEPTOR_BINDING,"BINARY");
		
		map.put(ANDROGEN_RECEPTOR_AGONIST,"BINARY");
		map.put(ANDROGEN_RECEPTOR_ANTAGONIST,"BINARY");
		map.put(ANDROGEN_RECEPTOR_BINDING,"BINARY");

		
		//Percepta properties:
		map.put(MOLAR_VOLUME, "CM3");
		map.put(MOLAR_REFRACTIVITY, "CM3");
		map.put(INDEX_OF_REFRACTION, "DIMENSIONLESS");
		map.put(POLARIZABILITY, "CUBIC_ANGSTROM");


		//https://www.sciencedirect.com/topics/chemistry/parachor#:~:text=The%20conventional%20numerical%20values%20of,(cm3%2Fmol).
		//(erg/cm2)1/4 × (cm3/mol)
		map.put(PARACHOR, "DIMENSIONLESS");//null in prod_chemprop, see https://www.epj-conferences.org/articles/epjconf/pdf/2015/11/epjconf_efm2014_02054.pdf
		map.put(DIELECTRIC_CONSTANT, "DIMENSIONLESS");//https://byjus.com/physics/dielectric-constant/#what-is-dielectric-constant		
		
		return map;
	}

	/*
	 * Returns the name of the units entry in the units table in the exp_prop schema
	 * 
	 * For example, for water solubility it should return G_L if the unitsAbbreviation is "g/L";
	 * 
	 * TODO should we just look this up from the database?
	 * 
	 * @param propertyName
	 * @param unitsAbbreviation
	 * @return
	 */
	public static String getExpPropUnitName(String propertyName,String unitsAbbreviation) {

		switch (propertyName) {
		
		// Properties with abbreviations that can be used to find units name by reflection of the constants in this class:
		case HENRYS_LAW_CONSTANT:
		case WATER_SOLUBILITY:
		case BOILING_POINT:
		case MELTING_POINT:			
		case THERMAL_CONDUCTIVITY:
		case SURFACE_TENSION:
		case FLASH_POINT:
		case VAPOR_PRESSURE:	
		case VISCOSITY:
		case DENSITY:
		case NINETY_SIX_HOUR_FATHEAD_MINNOW_LC50:
		case FORTY_EIGHT_HR_DAPHNIA_MAGNA_LC50:
		case FORTY_EIGHT_HR_TETRAHYMENA_PYRIFORMIS_IGC50:
			
			return getUnitNameByReflection(unitsAbbreviation);
			

		// BINARY properties:
		case LOCAL_LYMPH_NODE_ASSAY:
		case EYE_IRRITATION:
		case EYE_CORROSION:
		case SKIN_IRRITATION:
			return "BINARY";

		// LOG_UNITS:
		case PKA:
		case PKA_A:
		case PKA_B:
		case LOG_KOW:
		case LOG_OH:
		case LOG_KOC:
		case LOG_KOA:
		case LOG_HALF_LIFE:
		case LOG_KM_HL:
			return "LOG_UNITS";

		case LOG_BCF_FISH_WHOLEBODY:
		case LOG_BCF:
			return "LOG_L_KG";
		case APPEARANCE:
			return "TEXT";

		default:
			System.out.println("Unknown units in ExpPropData.getValues() for "+propertyName+"\t"+unitsAbbreviation);
			return "MISSING";
		}
	}
	
	/**
	 * Looks up the name of the Unit based on the value of the abbreviation from DevQsarConstants
	 * For example, if unitsAbbreviation="g/L", this method returns "G_L"
	 * 
	 * @param rec
	 * @return
	 */
	private static String getUnitNameByReflection(String unitsAbbreviation) {
		DevQsarConstants d=new DevQsarConstants();
		Field[] fields= d.getClass().getDeclaredFields();
		
		
		for(Field f : fields){
			if(!f.getType().getName().equals("java.lang.String")) continue;
			try {
				String value= (String) f.get(d);
				
				if(value.equals(unitsAbbreviation)) {
//					System.out.println(f.getName()+"\t"+value);
					return f.getName();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		return "MISSING";
	}
}

