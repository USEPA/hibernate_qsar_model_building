package gov.epa.endpoints.datasets.dsstox_mapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.ChemicalList;
import gov.epa.databases.dsstox.entity.SourceSubstance;
import gov.epa.databases.dsstox.entity.SourceSubstanceIdentifier;
import gov.epa.databases.dsstox.service.ChemicalListService;
import gov.epa.databases.dsstox.service.ChemicalListServiceImpl;
import gov.epa.databases.dsstox.service.DsstoxCompoundService;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.databases.dsstox.service.GenericSubstanceService;
import gov.epa.databases.dsstox.service.GenericSubstanceServiceImpl;
import gov.epa.databases.dsstox.service.SourceSubstanceService;
import gov.epa.databases.dsstox.service.SourceSubstanceServiceImpl;
import gov.epa.endpoints.datasets.DatasetCreator;
import gov.epa.endpoints.datasets.DatasetParams;
import gov.epa.endpoints.datasets.ExplainedResponse;
import gov.epa.endpoints.datasets.MappedPropertyValue;
import gov.epa.run_from_java.scripts.GetExpPropInfo.ExcelCreator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.GetExpPropInfo;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.util.StructureUtil;
import gov.epa.util.StructureUtil.SimpleOpsinResult;
import gov.epa.web_services.standardizers.Standardizer;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponse;
import gov.epa.web_services.standardizers.Standardizer.StandardizeResponseWithStatus;

public class DsstoxMapper {
	
	// Column headers for ChemReg import
	private static final String EXTERNAL_ID_HEADER = "EXTERNAL_ID";
	private static final String SOURCE_DTXSID_HEADER = "SOURCE_DTXSID";
	private static final String SOURCE_DTXCID_HEADER = "SOURCE_DTXCID";
	private static final String SOURCE_DTXRID_HEADER = "SOURCE_DTXRID";
	private static final String SOURCE_CASRN_HEADER = "SOURCE_CASRN";
	private static final String SOURCE_CHEMICAL_NAME_HEADER = "SOURCE_CHEMICAL_NAME";
	private static final String SOURCE_SMILES_HEADER = "SOURCE_SMILES";

	// "Connection reason" strings (i.e. mapping bin labels) in ChemReg
	static final String CASRN_MATCH = "CAS-RN matched " + SOURCE_CASRN_HEADER;
	static final String PREFERRED_NAME_MATCH = "Preferred Name matched " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String VALID_SYNONYM_MATCH = "Valid Synonym matched " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String UNIQUE_SYNONYM_MATCH = "Unique Synonym matched " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String AMBIGUOUS_SYNONYM_MATCH = "Ambiguous Synonym matched " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String NAME2STRUCTURE_MATCH = "Name2Structure matched " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String MAPPED_IDENTIFIER_MATCH = "Mapped Identifier matched " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String STRUCTURE_MATCH = "Structure matched " + SOURCE_SMILES_HEADER;
	static final String DTXSID_MATCH = "DTXSID matched " + SOURCE_DTXSID_HEADER;
	static final String DTXCID_MATCH = "DTXCID matched " + SOURCE_DTXCID_HEADER;
	static final String DTXRID_MATCH = "DTXRID matched " + SOURCE_DTXRID_HEADER;
	static final String DTXSID_CONFLICT = "DTXSID matched other record:  " + SOURCE_DTXSID_HEADER;
	static final String CASRN_CONFLICT = "CAS-RN matched other record:  " + SOURCE_CASRN_HEADER;
	static final String OTHER_CASRN_CONFLICT = "Other CAS-RN matched other record:  " + SOURCE_CASRN_HEADER;
	static final String PREFERRED_NAME_CONFLICT = "Preferred Name matched other record:  " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String VALID_SYNONYM_CONFLICT = "Valid Synonym matched other record:  " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String UNIQUE_SYNONYM_CONFLICT = "Unique Synonym matched other record:  " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String AMBIGUOUS_SYNONYM_CONFLICT = "Ambiguous Synonym matched other record:  " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String NAME2STRUCTURE_CONFLICT = "Name2Structure matched other record:  " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String MAPPED_IDENTIFIER_CONFLICT = "Mapped Identifier matched other record:  " + SOURCE_CHEMICAL_NAME_HEADER;
	static final String STRUCTURE_CONFLICT = "Structure matched other record:  " + SOURCE_SMILES_HEADER;

	public static final double DSSTOX_ID_SCORE = 5.0;
	public static final double CASRN_OR_STRICT_NAME_SCORE = 1.0;
	public static final double LENIENT_NAME_SCORE = 0.5;

	private DatasetParams datasetParams;
	
	private String datasetFileName;
	private String datasetFolderPath;
	
	private Set<String> acceptableAtoms;
	private String finalUnitName;
	private boolean omitSalts;
	private String lanId;
	
	private Standardizer standardizer;
	private String standardizerName;
	
	private Map<String, List<PropertyValue>> propertyValuesMap;
	private Map<String, DsstoxRecord> dsstoxRecordsMap;
	private List<DiscardedPropertyValue> discardedPropertyValues;
	private List<DsstoxConflict> conflicts;
	
	private static final Logger logger = LogManager.getLogger(DsstoxMapper.class);
	
	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	private CompoundService compoundService;
	
	private DsstoxCompoundService dsstoxCompoundService;
	private ChemicalListService chemicalListService;
	private SourceSubstanceService sourceSubstanceService;
	private GenericSubstanceService genericSubstanceService;
	
	public DsstoxMapper(DatasetParams datasetParams, Standardizer standardizer, String finalUnitName, 
			boolean omitSalts, Set<String> acceptableAtoms, String lanId) throws IOException {
		this.compoundService = new CompoundServiceImpl();
		
		this.dsstoxCompoundService = new DsstoxCompoundServiceImpl();
		this.chemicalListService = new ChemicalListServiceImpl();
		this.sourceSubstanceService = new SourceSubstanceServiceImpl();
		this.genericSubstanceService = new GenericSubstanceServiceImpl();
		
		this.datasetParams = datasetParams;
		
		this.datasetFileName = datasetParams.datasetName.replaceAll("[^A-Za-z0-9-_]+","_");
		this.datasetFolderPath = DevQsarConstants.OUTPUT_FOLDER_PATH + File.separator + datasetFileName;
		
		this.finalUnitName = finalUnitName;
		this.omitSalts = omitSalts;
		this.acceptableAtoms = acceptableAtoms;
		this.lanId = lanId;
		
		this.standardizer = standardizer;
		this.standardizerName = this.standardizer==null ? 
				DevQsarConstants.STANDARDIZER_NONE : this.standardizer.standardizerName;
		
		this.propertyValuesMap = new HashMap<String, List<PropertyValue>>();
		this.dsstoxRecordsMap = new HashMap<String, DsstoxRecord>();
		this.discardedPropertyValues = new ArrayList<DiscardedPropertyValue>();
		this.conflicts = new ArrayList<DsstoxConflict>();
		
		Logger opsinLogger = LogManager.getLogger("uk.ac.cam.ch.wwmm.opsin");
		opsinLogger.setLevel(Level.WARN);
		logger.setLevel(Level.ERROR);
	}
	
	public List<DsstoxRecord> getDsstoxRecords(String input, String inputType) {
		List<DsstoxRecord> dsstoxRecords = null;
		switch (inputType) {
		case DevQsarConstants.INPUT_DTXCID:
			dsstoxRecords = dsstoxCompoundService.findAsDsstoxRecordsByDtxcid(input);
			break;
		case DevQsarConstants.INPUT_INCHIKEY:
			dsstoxRecords = dsstoxCompoundService.findAsDsstoxRecordsByInchikey(input);
			break;
		case DevQsarConstants.INPUT_SMILES:
			String inchikey = StructureUtil.indigoInchikeyFromSmiles(input);
			dsstoxRecords = dsstoxCompoundService.findAsDsstoxRecordsByInchikey(inchikey);
			break;
		case DevQsarConstants.INPUT_DTXSID:
			dsstoxRecords = genericSubstanceService.findAsDsstoxRecordsByDtxsid(input);
			break;
		case DevQsarConstants.INPUT_CASRN:
			dsstoxRecords = genericSubstanceService.findAsDsstoxRecordsByCasrn(input);
			break;
		case DevQsarConstants.INPUT_PREFERRED_NAME:
			dsstoxRecords = genericSubstanceService.findAsDsstoxRecordsByPreferredName(input);
			break;
		case DevQsarConstants.INPUT_OTHER_CASRN:
			dsstoxRecords = genericSubstanceService.findAsDsstoxRecordsByOtherCasrn(input);
			break;
		case DevQsarConstants.INPUT_SYNONYM:
			dsstoxRecords = genericSubstanceService.findAsDsstoxRecordsWithSynonymQualityBySynonym(input);
			break;
		case DevQsarConstants.INPUT_MAPPED_IDENTIFIER:
			dsstoxRecords = sourceSubstanceService.findAsDsstoxRecordsWithSourceSubstanceByIdentifier(input);
			break;
		}
		
		return dsstoxRecords;
	}
	
	public List<DsstoxRecord> getDsstoxRecords(Collection<String> inputs, String inputType) {
		List<DsstoxRecord> dsstoxRecords = new ArrayList<DsstoxRecord>();
		if (inputs.size() <= 1000) {
			switch (inputType) {
			case DevQsarConstants.INPUT_DTXCID:
				dsstoxRecords = dsstoxCompoundService.findAsDsstoxRecordsByDtxcidIn(inputs);
				break;
			case DevQsarConstants.INPUT_DTXSID:
				dsstoxRecords = genericSubstanceService.findAsDsstoxRecordsByDtxsidIn(inputs);
				break;
			case DevQsarConstants.INPUT_CASRN:
				dsstoxRecords = genericSubstanceService.findAsDsstoxRecordsByCasrnIn(inputs);
				break;
			}
		} else {
			int count = 0;
			Set<String> subset = new HashSet<String>();
			for (String input:inputs) {
				subset.add(input);
				count++;
				if (count==1000) {
					dsstoxRecords.addAll(getDsstoxRecords(subset, inputType));
					subset = new HashSet<String>();
					count = 0;
				}
			}
			dsstoxRecords.addAll(getDsstoxRecords(subset, inputType));
		}
		
		return dsstoxRecords;
	}
	
	public List<MappedPropertyValue> map(List<PropertyValue> propertyValues) {
		initPropertyValuesMap(propertyValues);
		List<DsstoxRecord> dsstoxRecords = null;
		String checkChemicalList = null;
		if (datasetParams.mappingParams.isNaive) {	
			// For naive mapping strategies (CASRN, DTXSID, DTXCID), pull the DSSTox records and map them directly
			dsstoxRecords = getDsstoxRecords(propertyValuesMap.keySet(), datasetParams.mappingParams.dsstoxMappingId);
		} else if (datasetParams.mappingParams.chemicalListName!=null && datasetParams.mappingParams.chemRegListNameList==null) {
			checkChemicalList = datasetParams.mappingParams.chemicalListName;
			
			ChemicalList chemicalList = chemicalListService.findByName(checkChemicalList);
			if (chemicalList != null) {
				// If chemical list already added to DSSTox, queries all records from it
				dsstoxRecords = sourceSubstanceService
						.findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(checkChemicalList);
			} else {
				// If chemical list not in DSSTox, write the import file for the user to add
				
				writeChemRegImportFile(propertyValuesMap);
				return null;
			}

		} else if (datasetParams.mappingParams.chemRegListNameList!=null) {
			// here's where multiple lists are handled
			dsstoxRecords = new ArrayList<DsstoxRecord>();
			for (int i = 0; i < datasetParams.mappingParams.chemRegListNameList.size(); i++) {
				checkChemicalList = datasetParams.mappingParams.chemRegListNameList.get(i);
				ChemicalList chemicalList = chemicalListService.findByName(checkChemicalList);
				List<DsstoxRecord> records = null;
				ArrayList<DsstoxRecord> recordsAR = null; 
				if (chemicalList!=null) {
					// If chemical list already added to DSSTox, queries all records from it
					records = sourceSubstanceService.findAsDsstoxRecordsWithSourceSubstanceByChemicalListName(checkChemicalList);
					recordsAR = new ArrayList<DsstoxRecord>(records);
				}
				dsstoxRecords.addAll(recordsAR);
				System.out.println("dsstox records collected =" + dsstoxRecords.size());
			}

		
		}
		
		initDsstoxRecordsMap(dsstoxRecords);
		System.out.println("map entries:" +dsstoxRecordsMap.size());
		
		//Map the records to DSSTOX- where the magic happens:
		List<MappedPropertyValue> mappedPropertyValues = mapPropertyValuesToDsstoxRecords();

		if (mappedPropertyValues!=null) {
			if (!checkYourWork(mappedPropertyValues, propertyValues.size())) {
				logger.warn(datasetParams.datasetName + ": Number of property values or DSSTox records mapped and discarded do not add up");
			}
			
//			writeMappingFile(mappedPropertyValues);
			writeConflictFile();
			writeDiscardedPropertyValuesFile();
			writeDetailedDiscardedPropertyValuesFile();
		}
		
		return mappedPropertyValues;
	}
	
	private boolean checkYourWork(List<MappedPropertyValue> mappedPropertyValues, int countInputPropertyValues) {
		int countMappedPropertyValues = mappedPropertyValues.size();
		int countDiscardedPropertyValues = discardedPropertyValues.size();
		boolean checkPropertyValuesCount = (countMappedPropertyValues ==
				countInputPropertyValues - countDiscardedPropertyValues);
		
		System.out.println("Out of " + countInputPropertyValues + " input property values: "
				+ countDiscardedPropertyValues + " were discarded, "
				+ "leaving " + countMappedPropertyValues + " mapped");
		
		int countResolvedConflicts = 0;
		int countRejectedConflicts = 0;
		for (DsstoxConflict conflict:conflicts) {
			if (conflict.accept) {
				countResolvedConflicts++;
			} else {
				countRejectedConflicts++;
			}
		}
		
		System.out.println("Found " + conflicts.size() + " conflicts total: "
				+ countResolvedConflicts + " were accepted, " + countRejectedConflicts + " were rejected");
		
		return checkPropertyValuesCount;
	}
	
	private void initDsstoxRecordsMap(List<DsstoxRecord> dsstoxRecords) {
		dsstoxRecordsMap = new HashMap<String, DsstoxRecord>();
		for (DsstoxRecord dr:dsstoxRecords) {
			if (dr!=null) {
				String id = null;
				switch (datasetParams.mappingParams.dsstoxMappingId) {
				case DevQsarConstants.MAPPING_BY_CASRN:
					id = dr.casrn;
					break;
				case DevQsarConstants.MAPPING_BY_DTXSID:
					id = dr.dsstoxSubstanceId;
					break;
				case DevQsarConstants.MAPPING_BY_DTXCID:
					id = dr.dsstoxCompoundId;
					break;
				case DevQsarConstants.MAPPING_BY_LIST:
					id = dr.externalId;
					break;
				}
				
				if (id!=null) {
					dsstoxRecordsMap.put(id, dr);
				}
			}
		}
	}

	private void initPropertyValuesMap(List<PropertyValue> propertyValues) {
		propertyValuesMap = new HashMap<String, List<PropertyValue>>();
		for (PropertyValue pv:propertyValues) {
			SourceChemical sc = pv.getSourceChemical();
			if (sc!=null) {
				String id = null;
				switch (datasetParams.mappingParams.dsstoxMappingId) {
				case DevQsarConstants.MAPPING_BY_CASRN:
					id = sc.getSourceCasrn();
					break;
				case DevQsarConstants.MAPPING_BY_DTXSID:
					id = sc.getSourceDtxsid();
					break;
				case DevQsarConstants.MAPPING_BY_DTXCID:
					id = sc.getSourceDtxcid();
					break;
				case DevQsarConstants.MAPPING_BY_LIST:
					id = sc.generateSrcChemId();
					break;
				}
				
				if (id!=null) {
					List<PropertyValue> idPropertyValues = propertyValuesMap.get(id);
					if (idPropertyValues==null) { idPropertyValues = new ArrayList<PropertyValue>(); }
					idPropertyValues.add(pv);
					propertyValuesMap.put(id, idPropertyValues);
				} else {
					discardedPropertyValues.add(new DiscardedPropertyValue(pv, null, "Missing ID"));
				}
			} else {
				discardedPropertyValues.add(new DiscardedPropertyValue(pv, null, "Missing source chemical"));
			}
		}
	}
	
	public List<MappedPropertyValue> mapPropertyValuesToDsstoxRecords() {
		// Associates property values with DSSTox records using ID
		List<MappedPropertyValue> mappedPropertyValues = new ArrayList<MappedPropertyValue>();
		Map<String, List<PropertyValue>> unmappedPropertyValuesMap = new HashMap<String, List<PropertyValue>>();
		
		int counter=0;
		
		for (String id:propertyValuesMap.keySet()) {
			
			counter++;
			
			if (counter%100==0) System.out.println(counter);
			
			List<PropertyValue> propertyValues = propertyValuesMap.get(id);
			DsstoxRecord dsstoxRecord = dsstoxRecordsMap.get(id);
			
			if (dsstoxRecord==null) {
				unmappedPropertyValuesMap.put(id, propertyValues);
				discardPropertyValues(propertyValues, null, "No DSSTox record found");
				continue;
			}
			
			SourceChemical sourceChemical = propertyValues.iterator().next().getSourceChemical();
			if (!datasetParams.mappingParams.isNaive) {
//				System.out.println(dsstoxRecord.casrn);
				ExplainedResponse acceptMapping = acceptMapping(dsstoxRecord, sourceChemical);
				if (!acceptMapping.response) {
					discardPropertyValues(propertyValues, dsstoxRecord, acceptMapping.reason);
					continue;
				} else if (acceptMapping.record!=null 
						&& !acceptMapping.record.dsstoxSubstanceId.equals(dsstoxRecord.dsstoxSubstanceId)) {
					dsstoxRecord = acceptMapping.record;
				}
			}
			
			if (dsstoxRecord.dsstoxSubstanceId==null || dsstoxRecord.dsstoxSubstanceId.isBlank()) {
				discardPropertyValues(propertyValues, dsstoxRecord, "Missing DSSTox generic substance mapping for source substance (no hit)");
				continue;
			}
			
			if (dsstoxRecord.dsstoxCompoundId==null || dsstoxRecord.dsstoxCompoundId.isBlank()) {
				discardPropertyValues(propertyValues, dsstoxRecord, "Missing DSSTox compound mapping for generic substance (no structure)");
				continue;
			}
			
			// Validates structure from DSSTox
			ExplainedResponse validStructure = dsstoxRecord.validateStructure(omitSalts, acceptableAtoms);
			if (!validStructure.response) {
				discardPropertyValues(propertyValues, dsstoxRecord, validStructure.reason);
				continue;
			}
			
			for (PropertyValue pv:propertyValues) {
				ExplainedResponse validValue = pv.validateValue();
				if (!validValue.response) {
					discardedPropertyValues.add(new DiscardedPropertyValue(pv, dsstoxRecord, validValue.reason));
					continue;
				}
				
				ExplainedResponse parametersWithinBounds = datasetParams.testParameterValues(pv);
				if (!parametersWithinBounds.response) {
					discardedPropertyValues.add(new DiscardedPropertyValue(pv, dsstoxRecord, parametersWithinBounds.reason));
					continue;
				}
				
				MappedPropertyValue mpv = new MappedPropertyValue(id, pv, dsstoxRecord, validValue.value, finalUnitName);
				if (mpv.qsarPropertyValue!=null) {
					mappedPropertyValues.add(mpv);
				} else {
					discardedPropertyValues.add(new DiscardedPropertyValue(pv, dsstoxRecord, "Unit conversion failed"));
				}
			}
		}
		
		if (!unmappedPropertyValuesMap.isEmpty()) {
			writeChemRegImportFile(unmappedPropertyValuesMap);
		}
		
		return mappedPropertyValues;
	}
	
	private void discardPropertyValues(List<PropertyValue> propertyValues, DsstoxRecord dsstoxRecord, String reason) {
		discardedPropertyValues.addAll(propertyValues.stream()
				.map(pv -> new DiscardedPropertyValue(pv, dsstoxRecord, reason))
				.collect(Collectors.toList()));
	}
	
	private ExplainedResponse acceptMapping(DsstoxRecord dr, SourceChemical sc) {
		String bin = dr.connectionReason.replaceAll("</?b>", "").replaceAll("<br/>", ", ");
		if (bin.equals("No Hits")) {
			return new ExplainedResponse(false, "No hit");
		}
		
		Gson gson=new Gson();
		
		if (bin.contains(DTXRID_MATCH)) {
			// If source chemical is identified by DTXRID only, fetch the source substance identifiers associated
			// with that DTXRID, and then go through mapping with those as usual using the bin string from that source substance mapping			
			
			if (!bin.equals("DTXRID matched SOURCE_DTXRID")) {//[TMM] 11/23/22: added if statement because fillInSourceSubstanceIdentifiers wont get connection reason from database
				bin=fillInSourceSubstanceIdentifiers(sc);
				bin = bin.replaceAll("</?b>", "").replaceAll("<br/>", ", ");
				bin = bin.replaceAll("(CAS-RN matched (other record:  )?)[^,]+", "$1" + SOURCE_CASRN_HEADER);
				bin = bin.replaceAll("(Preferred Name matched (other record:  )?)[^,]+", "$1" + SOURCE_CHEMICAL_NAME_HEADER);
				bin = bin.replaceAll("(Valid Synonym matched (other record:  )?)[^,]+", "$1" + SOURCE_CHEMICAL_NAME_HEADER);
				bin = bin.replaceAll("(Unique Synonym matched (other record:  )?)[^,]+", "$1" + SOURCE_CHEMICAL_NAME_HEADER);
				bin = bin.replaceAll("(Ambiguous Synonym matched (other record:  )?)[^,]+", "$1" + SOURCE_CHEMICAL_NAME_HEADER);
				bin = bin.replaceAll("(Name2Structure matched (other record:  )?)[^,]+", "$1" + SOURCE_CHEMICAL_NAME_HEADER);
				bin = bin.replaceAll("(Mapped Identifier matched (other record:  )?)[^,]+", "$1" + SOURCE_CHEMICAL_NAME_HEADER);
				bin = bin.replaceAll("(?<!2)(Structure matched (other record:  )?)[^,]+", "$1" + SOURCE_SMILES_HEADER);
//				System.out.println("here2\t"+bin);
//				System.out.println(gson.toJson(sc));
			} else {
				fillInSourceSubstanceIdentifiers2(sc);//just in case it might have other source identifiers- probably wont
			}
		}
		
		if (datasetParams.mappingParams.useCuratorValidation && dr.curatorValidated) {
			// Always accept curator validated matches
			return new ExplainedResponse(true, "DSSTox mapping validated by curator");
		}
		
		if (datasetParams.mappingParams.requireCuratorValidation && !dr.curatorValidated) {
			return new ExplainedResponse(false, "Curator validation required");
		}
		
		if (bin.contains(DTXSID_MATCH) || bin.contains(DTXCID_MATCH) || bin.contains(DTXRID_MATCH)) {//[TMM] 11/23/22 added or for RID match
			// Accept DTXSID, DTXCID, DTXRID matches
//			if (bin.equals(DTXRID_MATCH)) {
//				System.out.println(gson.toJson(sc));
//			}
			return new ExplainedResponse(true, "Matched source DTXSID, DTXCID, or DTXRID");
		}
		
		if (bin.contains(DTXSID_CONFLICT)) {
			// If another record matches the DTXSID, get that one instead
			List<DsstoxRecord> dtxsidMatches = getDsstoxRecords(sc.getSourceDtxsid(), DevQsarConstants.INPUT_DTXSID);		
			if (dtxsidMatches.size()==1) {
				DsstoxRecord dtxsidMatch = dtxsidMatches.iterator().next();
				return new ExplainedResponse(true, dtxsidMatch, "Matched source DTXSID or DTXCID");
			}
			
			// Otherwise reject
			return new ExplainedResponse(false, "Failed to match source DTXSID or DTXCID");
		}
		
		if (sc.getSourceChemicalName()!=null && !sc.getSourceChemicalName().isBlank()) {
			if (datasetParams.mappingParams.omitUvcbKeywords && DsstoxMapperStringUtil.hasUvcbKeywords(sc.getSourceChemicalName())) {
				// Reject any records with names containing Charlie's UVCB keywords
				return new ExplainedResponse(false, "UVCB keywords in name");
			}
			
			if (datasetParams.mappingParams.omitOpsinAmbiguousNames) {
				// Reject any records with ambiguous names
				SimpleOpsinResult result = StructureUtil.opsinSmilesFromChemicalName(sc.getSourceChemicalName());
				if (result.message!=null && result.message.startsWith("APPEARS_AMBIGUOUS")) {
					return new ExplainedResponse(false, "OPSIN ambiguous name");
				}
			}
		}
		
		if (datasetParams.mappingParams.autoResolveConflicts 
				&& (DsstoxMapperStringUtil.hasMappingConflict(bin) || DsstoxMapperStringUtil.hasDelimiters(sc))) {
			// If applying auto conflict resolution, do it
			DsstoxConflict conflict = new DsstoxConflict(sc, dr);
			
			if (DsstoxMapperStringUtil.hasMappingConflict(bin)) {
				boolean foundAllMappingConflicts = findMappingConflicts(conflict, bin);
				if (!foundAllMappingConflicts) {
					swapBetterMatch(conflict, bin);
					conflict.setAcceptAndReason(false, "Conflict resolution failed: The indicated alternative records were not found in DSSTox");
					conflicts.add(conflict);
					return new ExplainedResponse(false, "Conflict resolution failed: The indicated alternative records were not found in DSSTox");
				}
			}
			
			if (DsstoxMapperStringUtil.hasDelimiters(sc)) {
				boolean foundAllDelimiterConflicts = findDelimiterConflicts(conflict);
				if (!foundAllDelimiterConflicts) {
					swapBetterMatch(conflict, bin);
					conflict.setAcceptAndReason(false, "Conflict resolution failed: The indicated alternative records were not found in DSSTox");
					conflicts.add(conflict);
					return new ExplainedResponse(false, "Conflict resolution failed: The indicated alternative records were not found in DSSTox");
				}
			}
			
			// Pipe-delimited matches not found by ChemReg may be added above
			// So need to update the bin string to capture those
			bin = conflict.bestDsstoxRecord.connectionReason.replaceAll("</?b>", "").replaceAll("<br/>", ", ");
			conflicts.add(processConflict(conflict, bin));
			if (!conflict.accept) {
				// If conflict is not acceptable (i.e. QSAR-ready SMILES do not match), reject it
				return new ExplainedResponse(false, conflict.reason);
			} else {
				return new ExplainedResponse(true, conflict.bestDsstoxRecord, conflict.reason);
			}
		} else if (DsstoxMapperStringUtil.hasMappingConflict(bin) || DsstoxMapperStringUtil.hasDelimiters(sc)) {
			// Reject conflicted mappings
			return new ExplainedResponse(false, "Auto conflict resolution not selected");
		}
		
		return new ExplainedResponse(authoritativeMatch(bin, sc), bin);
	}
	
	private boolean findMappingConflicts(DsstoxConflict conflict, String bin) {
		SourceChemical sourceChemical = conflict.sourceChemical;
		DsstoxRecord bestDsstoxRecord = conflict.bestDsstoxRecord;
		String srcChemId = sourceChemical.generateSrcChemId();
		
		Set<String> conflictDtxsids = conflict.dsstoxConflictRecords.stream()
				.map(dcr -> dcr.dsstoxRecord.dsstoxSubstanceId)
				.collect(Collectors.toSet());
		if (bestDsstoxRecord.dsstoxSubstanceId!=null) {
			conflictDtxsids.add(bestDsstoxRecord.dsstoxSubstanceId);
		}
		
		if (bin.contains(OTHER_CASRN_CONFLICT)) {
			String conflictType = DevQsarConstants.INPUT_OTHER_CASRN;
			List<DsstoxRecord> otherCasrnConflicts = getDsstoxRecords(sourceChemical.getSourceCasrn(), conflictType);
			if (otherCasrnConflicts!=null && !otherCasrnConflicts.isEmpty()) {
				for (DsstoxRecord dr:otherCasrnConflicts) {
					addDsstoxConflictRecord(dr, conflict, conflictDtxsids, conflictType);
				}
				logger.trace(srcChemId + ": ChemReg flagged a deprecated CASRN mapping conflict, " + otherCasrnConflicts.size() + " records found");
			} else {
				logger.debug(srcChemId + ": ChemReg flagged a deprecated CASRN mapping conflict, but no records were found");
				return false;
			}
		} else if (bin.contains(CASRN_CONFLICT)) {
			String conflictType = DevQsarConstants.INPUT_CASRN;
			List<DsstoxRecord> casrnConflicts = getDsstoxRecords(sourceChemical.getSourceCasrn(), conflictType);
			if (casrnConflicts!=null && !casrnConflicts.isEmpty()) {
				for (DsstoxRecord dr:casrnConflicts) {
					addDsstoxConflictRecord(dr, conflict, conflictDtxsids, conflictType);
				}
				logger.trace(srcChemId + ": ChemReg flagged a CASRN mapping conflict, " + casrnConflicts.size() + " records found");
			} else {
				logger.debug(srcChemId + ": ChemReg flagged a CASRN mapping conflict, but no records were found");
				return false;
			}
		} 
		
		if (bin.contains(PREFERRED_NAME_CONFLICT)) {
			String conflictType = DevQsarConstants.INPUT_PREFERRED_NAME;
			List<DsstoxRecord> preferredNameConflicts = 
					getDsstoxRecords(sourceChemical.getSourceChemicalName(), conflictType);
			if (preferredNameConflicts!=null && !preferredNameConflicts.isEmpty()) {
				for (DsstoxRecord dr:preferredNameConflicts) {
					addDsstoxConflictRecord(dr, conflict, conflictDtxsids, conflictType);
				}
				logger.trace(srcChemId + ": ChemReg flagged a preferred name mapping conflict, " + preferredNameConflicts.size() + " records found");
			} else {
				logger.debug(srcChemId + ": ChemReg flagged a preferred name mapping conflict, but no records were found");
				return false;
			}
		}
		
		if (DsstoxMapperStringUtil.hasSynonymConflict(bin)) {
			List<DsstoxRecord> synonymConflicts = getDsstoxRecords(sourceChemical.getSourceChemicalName(), DevQsarConstants.INPUT_SYNONYM);
			if (synonymConflicts!=null && !synonymConflicts.isEmpty()) {
				for (DsstoxRecord dr:synonymConflicts) {
					String upperCaseSynonymQuality = dr.synonymQuality.toUpperCase().replaceAll(" ", "_");
					String conflictType = upperCaseSynonymQuality + "_" + DevQsarConstants.INPUT_SYNONYM;
					addDsstoxConflictRecord(dr, conflict, conflictDtxsids, conflictType);
				}
				logger.trace(srcChemId + ": ChemReg flagged a synonym mapping conflict, " + synonymConflicts.size() + " records found");
			} else {
				logger.debug(srcChemId + ": ChemReg flagged a synonym mapping conflict, but no records were found");
				return false;
			}
		}
		
		if (bin.contains(NAME2STRUCTURE_CONFLICT)) {
			String conflictType = DevQsarConstants.INPUT_NAME2STRUCTURE;
			SimpleOpsinResult result = StructureUtil.opsinSmilesFromChemicalName(sourceChemical.getSourceChemicalName());
			if (result.smiles==null) {
				logger.warn(srcChemId + ": Conflict resolution failed: ChemReg flagged a Name2Structure mapping conflict, "
						+ "but OPSIN parsing failed with error: " + result.message);
				conflict.setAcceptAndReason(false, "Conflict resolution failed: ChemReg flagged a Name2Structure mapping conflict, "
						+ "but OPSIN parsing failed");
				return false;
			} else if (result.message!=null) {
				logger.warn(srcChemId + ": OPSIN parsing succeeded with warning: " + result.message);
			}
			
			List<DsstoxRecord> structureConflicts = getDsstoxRecords(result.smiles, DevQsarConstants.INPUT_SMILES);
			if (structureConflicts!=null && !structureConflicts.isEmpty()) {
				for (DsstoxRecord dr:structureConflicts) {
					addDsstoxConflictRecord(dr, conflict, conflictDtxsids, conflictType);
				}
				logger.trace(srcChemId + ": ChemReg flagged a Name2Structure mapping conflict, " + structureConflicts.size() + " records found");
			} else {
				logger.debug(srcChemId + ": ChemReg flagged a Name2Structure mapping conflict, but no records were found");
				return false;
			}
		}
		
		if (bin.contains(MAPPED_IDENTIFIER_CONFLICT)) {
			String conflictType = DevQsarConstants.INPUT_MAPPED_IDENTIFIER;
			List<DsstoxRecord> mappedIdentifierConflicts = getDsstoxRecords(sourceChemical.getSourceChemicalName(), conflictType);
			if (mappedIdentifierConflicts!=null && !mappedIdentifierConflicts.isEmpty()) {
				for (DsstoxRecord dr:mappedIdentifierConflicts) {
					addDsstoxConflictRecord(dr, conflict, conflictDtxsids, conflictType);
				}
				logger.trace(srcChemId + ": ChemReg flagged a mapped identifier mapping conflict, " + mappedIdentifierConflicts.size() + " records found");
			} else {
				logger.debug(srcChemId + ": ChemReg flagged a mapped identifier mapping conflict, but no records were found");
				return false;
			}
		}
		
		if (bin.contains(STRUCTURE_CONFLICT)) {
			String conflictType = DevQsarConstants.INPUT_SMILES;
			List<DsstoxRecord> structureConflicts = getDsstoxRecords(sourceChemical.getSourceSmiles(), conflictType);
			if (structureConflicts!=null && !structureConflicts.isEmpty()) {
				for (DsstoxRecord dr:structureConflicts) {
					addDsstoxConflictRecord(dr, conflict, conflictDtxsids, conflictType);
				}
				logger.trace(srcChemId + ": ChemReg flagged a structure mapping conflict, " + structureConflicts.size() + " records found");
			} else {
				logger.debug(srcChemId + ": ChemReg flagged a structure mapping conflict, but no records were found");
				return false;
			}
		}
		
		return true;
	}
	
	private boolean findDelimiterConflicts(DsstoxConflict conflict) {
		SourceChemical sourceChemical = conflict.sourceChemical;
		String srcChemId = sourceChemical.generateSrcChemId();
		DsstoxRecord bestDsstoxRecord = conflict.bestDsstoxRecord;
		
		Set<String> conflictDtxsids = conflict.dsstoxConflictRecords.stream()
				.map(dcr -> dcr.dsstoxRecord.dsstoxSubstanceId)
				.collect(Collectors.toSet());
		if (bestDsstoxRecord.dsstoxSubstanceId!=null) {
			conflictDtxsids.add(bestDsstoxRecord.dsstoxSubstanceId);
		}
		
		String sourceCasrnStr = DsstoxMapperStringUtil.cleanDelimiters(sourceChemical.getSourceCasrn(), false);
		if (sourceCasrnStr!=null && sourceCasrnStr.contains("|")) {
			String conflictType = DevQsarConstants.INPUT_CASRN;
			if (conflict.bestDsstoxRecord.casrn!=null && sourceCasrnStr.contains(conflict.bestDsstoxRecord.casrn) 
					&& !conflict.bestDsstoxRecord.connectionReason.contains(CASRN_MATCH)) {
				conflict.bestDsstoxRecord.connectionReason = conflict.bestDsstoxRecord.connectionReason + "<br/>" + CASRN_MATCH;
			}
			
			String[] sourceCasrns = sourceCasrnStr.split("\\|");
			Set<String> sourceCasrnSet = new HashSet<String>(Arrays.asList(sourceCasrns));
			List<DsstoxRecord> casrnConflicts = getDsstoxRecords(sourceCasrnSet, conflictType);
			if (casrnConflicts!=null && casrnConflicts.size()==sourceCasrnSet.size()) {
				for (DsstoxRecord dr:casrnConflicts) {
					addDsstoxConflictRecord(dr, conflict, conflictDtxsids, conflictType);
				}
			} else {
				logger.debug(srcChemId + ": Missing records for pipe-delimited CASRNs");
				return false;
			}
		}
		
		String sourceDtxcidStr = DsstoxMapperStringUtil.cleanDelimiters(sourceChemical.getSourceDtxcid(), true);
		if (sourceDtxcidStr!=null && sourceDtxcidStr.contains("|")) {
			String conflictType = DevQsarConstants.INPUT_DTXCID;
			if (conflict.bestDsstoxRecord.dsstoxCompoundId!=null && sourceDtxcidStr.contains(conflict.bestDsstoxRecord.dsstoxCompoundId) 
					&& !conflict.bestDsstoxRecord.connectionReason.contains(DTXCID_MATCH)) {
				conflict.bestDsstoxRecord.connectionReason = conflict.bestDsstoxRecord.connectionReason + "<br/>" + DTXCID_MATCH;
			}
			
			String[] sourceDtxcids = sourceDtxcidStr.split("\\|");
			Set<String> sourceDtxcidSet = new HashSet<String>(Arrays.asList(sourceDtxcids));
			List<DsstoxRecord> dtxcidConflicts = getDsstoxRecords(sourceDtxcidSet, conflictType);
			if (dtxcidConflicts!=null && dtxcidConflicts.size()==sourceDtxcidSet.size()) {
				for (DsstoxRecord dr:dtxcidConflicts) {
					addDsstoxConflictRecord(dr, conflict, conflictDtxsids, conflictType);
				}
			} else {
				logger.debug(srcChemId + ": Missing records for pipe-delimited DTXCIDs");
				return false;
			}
		}
		
		String sourceDtxsidStr = DsstoxMapperStringUtil.cleanDelimiters(sourceChemical.getSourceDtxsid(), true);
		if (sourceDtxsidStr!=null && sourceDtxsidStr.contains("|")) {
			String conflictType = DevQsarConstants.INPUT_DTXSID;
			if (conflict.bestDsstoxRecord.dsstoxSubstanceId!=null && sourceDtxsidStr.contains(conflict.bestDsstoxRecord.dsstoxSubstanceId) 
					&& !conflict.bestDsstoxRecord.connectionReason.contains(DTXSID_MATCH)) {
				conflict.bestDsstoxRecord.connectionReason = conflict.bestDsstoxRecord.connectionReason + "<br/>" + DTXSID_MATCH;
			}
			
			String[] sourceDtxsids = sourceDtxsidStr.split("\\|");
			Set<String> sourceDtxsidSet = new HashSet<String>(Arrays.asList(sourceDtxsids));
			List<DsstoxRecord> dtxsidConflicts = getDsstoxRecords(sourceDtxsidSet, conflictType);
			if (dtxsidConflicts!=null && dtxsidConflicts.size()==sourceDtxsidSet.size()) {
				for (DsstoxRecord dr:dtxsidConflicts) {
					addDsstoxConflictRecord(dr, conflict, conflictDtxsids, conflictType);
				}
			} else {
				logger.debug(srcChemId + ": Missing records for pipe-delimited DTXSIDs");
				return false;
			}
		}
		
		return true;
	}
	
	private void addDsstoxConflictRecord(DsstoxRecord dr, DsstoxConflict conflict, Set<String> conflictDtxsids, String conflictType) {
		if (dr.dsstoxSubstanceId!=null && conflictDtxsids.add(dr.dsstoxSubstanceId)) {
			conflict.dsstoxConflictRecords.add(new DsstoxConflictRecord(dr, conflictType));
		} else if (dr.dsstoxSubstanceId!=null) {
			for (DsstoxConflictRecord dcr:conflict.dsstoxConflictRecords) {
				if (dcr.dsstoxRecord.dsstoxSubstanceId.equals(dr.dsstoxSubstanceId) && !dcr.conflictType.contains(conflictType)) {
					dcr.conflictType = dcr.conflictType + "; " + conflictType;
					break;
				}
			}
		}
	}
	
	private DsstoxConflict processConflict(DsstoxConflict conflict, String bin) {
		String srcChemId = conflict.sourceChemical.generateSrcChemId();
		
		if (perfectMatch(bin, conflict.sourceChemical)) {
			// Accept matches where every piece of source information agrees
			conflict.setAcceptAndReason(true, "Conflict overridden: All source information matched");
			return conflict;
		}
		
		if (conflict.dsstoxConflictRecords.isEmpty()) {
			logger.warn(srcChemId + ": Conflict resolution failed: The indicated alternative records were not found in DSSTox");
			conflict.setAcceptAndReason(false, "Conflict resolution failed: The indicated alternative records were not found in DSSTox");
			return conflict;
		}
		
		double maxScore = swapBetterMatch(conflict, bin);
		if (maxScore < 1.0 && !datasetParams.mappingParams.validateConflictsTogether) {
			conflict.setAcceptAndReason(false, "Conflict rejected: No authoritative match found");
			return conflict;
		}
		
		String drQsarReadySmiles = null;
		String drStandardizedInchikey = null;
		String drQsarReadyInchikey = null;
		boolean needStandardizer = false;
		if (conflict.bestDsstoxRecord.qsarReadySmiles!=null && !conflict.bestDsstoxRecord.qsarReadySmiles.isBlank()) {
			drQsarReadySmiles = conflict.bestDsstoxRecord.qsarReadySmiles;
		} else if (standardizer!=null && conflict.bestDsstoxRecord.isWellDefined()) {
			needStandardizer = true;
			conflict.bestStandardizedSmiles = standardizeSmiles(srcChemId, conflict.bestDsstoxRecord);
		}
		
		if (drQsarReadySmiles==null && conflict.bestStandardizedSmiles==null) {
			conflict.setAcceptAndReason(false, "Conflict resolution failed: QSAR-ready SMILES or standardization unavailable for best record");
			return conflict;
		}
		
		if (drQsarReadySmiles!=null) {
			drQsarReadyInchikey = StructureUtil.indigoInchikeyFromSmiles(drQsarReadySmiles);
		}
		
		if (conflict.bestStandardizedSmiles!=null) {
			drStandardizedInchikey = StructureUtil.indigoInchikeyFromSmiles(conflict.bestStandardizedSmiles);
		}
		
		conflict.setAcceptAndReason(true, 
				"Conflict resolved: QSAR-ready or standardized SMILES agree");
		for (DsstoxConflictRecord dsstoxConflictRecord:conflict.dsstoxConflictRecords) {
			DsstoxRecord dcr = dsstoxConflictRecord.dsstoxRecord;
			String dcrQsarReadySmiles = null;
			String dcrQsarReadyInchikey = null;
			String dcrStandardizedInchikey = null;
			// Ensure we always use the same standardization method for comparison
			// Use the standardized version if:
			// a) No QSAR-ready SMILES was available for the best record
			// b) No QSAR-ready SMILES is available for the current conflict record
			if (needStandardizer || dcr.qsarReadySmiles==null || dcr.qsarReadySmiles.isBlank()) {
				if (dcr.isWellDefined()) {
					// If conflict record has a structure, standardize it
					dsstoxConflictRecord.standardizedSmiles = standardizeSmiles(srcChemId, dcr);
				} else {
					// Otherwise, it's a no-structure record; move on
					continue;
				}
				
				if (dsstoxConflictRecord.standardizedSmiles==null) {
					conflict.setAcceptAndReason(false, "Conflict resolution failed: Standardization unavailable for conflict record");
					return conflict;
				} else {
					// Calculate inchikey from the standardized SMILES for an additional check
					dcrStandardizedInchikey = StructureUtil.indigoInchikeyFromSmiles(dsstoxConflictRecord.standardizedSmiles);
				}
				
				// If best record hasn't already been standardized, do it
				// Relatively rarely necessary, so better to do it here than to repeat it for every single conflict "just in case"
				if (conflict.bestStandardizedSmiles==null && conflict.bestDsstoxRecord.isWellDefined()) {
					conflict.bestStandardizedSmiles = standardizeSmiles(srcChemId, conflict.bestDsstoxRecord);
				}
				
				// Check if standardization worked
				if (conflict.bestStandardizedSmiles==null) {
					conflict.setAcceptAndReason(false, "Conflict resolution failed: Standardization unavailable for best record");
					return conflict;
				} else {
					drStandardizedInchikey = StructureUtil.indigoInchikeyFromSmiles(conflict.bestStandardizedSmiles);
				}
				
				boolean inchikeysDisagree = drStandardizedInchikey!=null && dcrStandardizedInchikey!=null &&
						!drStandardizedInchikey.equals(dcrStandardizedInchikey);
				if (!conflict.bestStandardizedSmiles.equals(dsstoxConflictRecord.standardizedSmiles) && inchikeysDisagree) {
					conflict.setAcceptAndReason(false, "Conflict rejected: Standardized SMILES do not match");
					return conflict;
				}
			} else {
				// If both best and current conflict record have QSAR-ready SMILES, use that instead
				dcrQsarReadySmiles = dcr.qsarReadySmiles;
				dcrQsarReadyInchikey = StructureUtil.indigoInchikeyFromSmiles(dcrQsarReadySmiles);
				
				boolean inchikeysDisagree = drQsarReadyInchikey!=null && dcrQsarReadyInchikey!=null &&
						!drQsarReadyInchikey.equals(dcrQsarReadyInchikey);
				if (!drQsarReadySmiles.equals(dcrQsarReadySmiles) && inchikeysDisagree) {
					conflict.setAcceptAndReason(false, "Conflict rejected: QSAR-ready SMILES do not match");
					return conflict;
				}
			}
		}
		
		return conflict;
	}
	
	private double swapBetterMatch(DsstoxConflict conflict, String bin) {
		DsstoxRecord lastBestRecord = conflict.bestDsstoxRecord;
		double maxScore = scoreDsstoxRecord(bin);
		int argMaxScore = -1;
		int size = conflict.dsstoxConflictRecords.size();
		for (int i = 0; i < size; i++) {
			DsstoxConflictRecord currentRecord = conflict.dsstoxConflictRecords.get(i);
			double score = currentRecord.score();
			if (score > maxScore || (score==maxScore && !lastBestRecord.isWellDefined() && currentRecord.dsstoxRecord.isWellDefined())) {
				lastBestRecord = currentRecord.dsstoxRecord;
				maxScore = score;
				argMaxScore = i;
			}
		}
		
		if (argMaxScore >= 0) {
			DsstoxConflictRecord newBestDsstoxRecord = conflict.dsstoxConflictRecords.get(argMaxScore);
			if (newBestDsstoxRecord.dsstoxRecord.isWellDefined()) { // Don't swap if the new record has no structure
				conflict.dsstoxConflictRecords.remove(argMaxScore);
				conflict.dsstoxConflictRecords.add(new DsstoxConflictRecord(conflict.bestDsstoxRecord, bin));
				conflict.bestDsstoxRecord = newBestDsstoxRecord.dsstoxRecord;
				conflict.bestDsstoxRecord.connectionReason = "Automatically added connection reason (non-ChemReg): " 
						+ newBestDsstoxRecord.conflictType;
			}
		}
		
		conflict.dsstoxConflictRecords.sort(Comparator.comparing(DsstoxConflictRecord::score).reversed());
		return maxScore;
	}
	
	private boolean authoritativeMatch(String bin, SourceChemical sc) {
		boolean sourceHasCasrn = sc.getSourceCasrn()!=null && !sc.getSourceCasrn().isBlank();
		boolean sourceHasChemicalName = sc.getSourceChemicalName()!=null && !sc.getSourceChemicalName().isBlank();
		boolean sourceHasSmiles = sc.getSourceSmiles()!=null && !sc.getSourceSmiles().isBlank();
		
		boolean accept = false;
		if (sourceHasCasrn && sourceHasChemicalName) {
			// Accept any chemical name match as long as CASRN supports it
			accept = bin.contains(CASRN_MATCH) && DsstoxMapperStringUtil.lenientChemicalNameMatch(bin);
		} else if (sourceHasChemicalName && sourceHasSmiles) {
			// If no CASRN, ignore SMILES and allow only preferred, valid, or unique synonym matches
			accept = DsstoxMapperStringUtil.strictChemicalNameMatch(bin);
		} else if (sourceHasCasrn && sourceHasSmiles) {
			accept = bin.contains(CASRN_MATCH);
		} else if (sourceHasCasrn) {
			accept = bin.contains(CASRN_MATCH);
		} else if (sourceHasChemicalName) {
			// If chemical name alone, allow only preferred, valid, or unique synonym matches
			accept = DsstoxMapperStringUtil.strictChemicalNameMatch(bin);
		} else if (sourceHasSmiles) {
			// Source-provided SMILES match is not good enough
			accept = false;
		}
		
		return accept;
	}
	
	private boolean perfectMatch(String bin, SourceChemical sc) {
		boolean sourceHasCasrn = sc.getSourceCasrn()!=null && !sc.getSourceCasrn().isBlank();
		boolean sourceHasChemicalName = sc.getSourceChemicalName()!=null && !sc.getSourceChemicalName().isBlank();
		boolean sourceHasSmiles = sc.getSourceSmiles()!=null && !sc.getSourceSmiles().isBlank();
		
		boolean match = false;
		if (sourceHasCasrn && sourceHasChemicalName && sourceHasSmiles) {
			match = bin.contains(CASRN_MATCH) && !sc.getSourceCasrn().contains("|")
					&& DsstoxMapperStringUtil.lenientChemicalNameMatch(bin) && bin.contains(STRUCTURE_MATCH);
		} else if (sourceHasCasrn && sourceHasChemicalName) {
			match = bin.contains(CASRN_MATCH) && !sc.getSourceCasrn().contains("|")
					&& DsstoxMapperStringUtil.lenientChemicalNameMatch(bin);
		} else if (sourceHasChemicalName && sourceHasSmiles) {
			match = DsstoxMapperStringUtil.strictChemicalNameMatch(bin) && bin.contains(STRUCTURE_MATCH);
		} else if (sourceHasCasrn && sourceHasSmiles) {
			match = bin.contains(CASRN_MATCH) && !sc.getSourceCasrn().contains("|")
					&& bin.contains(STRUCTURE_MATCH);
		} else if (sourceHasCasrn) {
			// Single identifiers are not "perfect" even if they match
			match = false;
		} else if (sourceHasChemicalName) {
			// Single identifiers are not "perfect" even if they match
			match = false;
		} else if (sourceHasSmiles) {
			// Single identifiers are not "perfect" even if they match
			match = false;
		}
		
		return match;
	}
	
	private double scoreDsstoxRecord(String bin) {
		double score = 0.0;
		
		if (bin.contains(DTXSID_MATCH)) {
			score += DSSTOX_ID_SCORE;
		}
		
		if (bin.contains(DTXCID_MATCH)) {
			score += DSSTOX_ID_SCORE;
		}
		
		if (bin.contains(CASRN_MATCH)) {
			score += CASRN_OR_STRICT_NAME_SCORE;
		}
		
		if (DsstoxMapperStringUtil.strictChemicalNameMatch(bin)) {
			score += CASRN_OR_STRICT_NAME_SCORE;
		} else if (DsstoxMapperStringUtil.lenientChemicalNameMatch(bin)) {
			score += LENIENT_NAME_SCORE;
		}
		
		return score;
	}
	
	private String fillInSourceSubstanceIdentifiers(SourceChemical sc) {
		String dtxrid = sc.getSourceDtxrid();
		if (dtxrid==null) {
			return null;
		}
		
		SourceSubstance sourceSubstance = sourceSubstanceService.findByDtxrid(dtxrid);
		for (SourceSubstanceIdentifier ssi:sourceSubstance.getSourceSubstanceIdentifiers()) {
			String identifier = ssi.getIdentifier();
			switch (ssi.getIdentifierType()) {
			case "DTXSID":
				sc.setSourceDtxsid(identifier);
				break;
			case "NAME":
				sc.setSourceChemicalName(identifier);
				break;
			case "CASRN":
				sc.setSourceCasrn(identifier);
				break;
			case "SMILES":
			case "STRUCTURE":
				sc.setSourceSmiles(identifier);
				break;
			}
		}
		
		return sourceSubstance.getSourceGenericSubstanceMapping().getConnectionReason();
	}
	
	private void fillInSourceSubstanceIdentifiers2(SourceChemical sc) {
		
		SourceSubstance sourceSubstance = sourceSubstanceService.findByDtxrid(sc.getSourceDtxrid());
		for (SourceSubstanceIdentifier ssi:sourceSubstance.getSourceSubstanceIdentifiers()) {
			String identifier = ssi.getIdentifier();
			
//			System.out.println(sc.getSourceDtxrid()+"\t"+identifier);
			
			switch (ssi.getIdentifierType()) {
			case "DTXSID":
				sc.setSourceDtxsid(identifier);
				break;
			case "NAME":
				sc.setSourceChemicalName(identifier);
				break;
			case "CASRN":
				sc.setSourceCasrn(identifier);
				break;
			case "SMILES":
			case "STRUCTURE":
				sc.setSourceSmiles(identifier);
				break;
			}
		}
		
	}
	
	private String standardizeSmiles(String srcChemId, DsstoxRecord dr) {
		// Check (by DTXCID) if compound already has a standardization
		Compound compound = compoundService.findByDtxcidSmilesAndStandardizer(dr.dsstoxCompoundId, dr.smiles,standardizerName);
		
		if (compound==null) {
			StandardizeResponseWithStatus standardizeResponse = standardizer.callStandardize(dr.smiles);
			if (standardizeResponse.status==200) {
				StandardizeResponse standardizeResponseData = standardizeResponse.standardizeResponse;
				String standardizedSmiles = null;
				if (standardizeResponseData.success) {
					standardizedSmiles = standardizeResponseData.qsarStandardizedSmiles;
				} else {
					logger.warn(srcChemId + ": Standardization failed for SMILES: " + dr.smiles);
				}
				
				compound = new Compound(dr.dsstoxCompoundId, dr.smiles, standardizedSmiles, standardizerName, lanId);
				
				try {
					compoundService.create(compound);
				} catch (ConstraintViolationException e) {
					System.out.println(e.getMessage());
				}
			} else {
				// In case there's a server error that prevents standardization, don't save the null standardization
				// We want to try again later!
				logger.warn(srcChemId + ": Standardizer HTTP response failed for SMILES: " 
						+ dr.smiles + " with code " + standardizeResponse.status);
				return null;
			}
		}
		
		return compound.getCanonQsarSmiles();
	}

	private void writeChemRegImportFile(Map<String, List<PropertyValue>> propertyValuesMap) {
		String importFilePath = datasetFolderPath + File.separator + datasetParams.mappingParams.chemicalListName + "_ChemRegImport.txt";
		File importFile = new File(importFilePath);
		importFile.getParentFile().mkdirs();
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(importFile))) {
			bw.write(EXTERNAL_ID_HEADER + "\t"
					+ SOURCE_DTXSID_HEADER + "\t"
					+ SOURCE_DTXCID_HEADER + "\t"
					+ SOURCE_DTXRID_HEADER + "\t"
					+ SOURCE_CASRN_HEADER + "\t"
					+ SOURCE_CHEMICAL_NAME_HEADER + "\t"
					+ SOURCE_SMILES_HEADER + "\r\n");
			for (String srcChemId:propertyValuesMap.keySet()) {
				List<PropertyValue> propertyValues = propertyValuesMap.get(srcChemId);
				if (propertyValues!=null && !propertyValues.isEmpty()) {
					SourceChemical sc = propertyValues.iterator().next().getSourceChemical();
					String line = generateChemRegImportLine(sc);
					if (line!=null) {
						bw.write(line + "\r\n");
					}
				}
			}
		} catch (IOException e) {
			logger.error(datasetParams.datasetName + ": Failed to write ChemReg import file");
		}
	}
	
	private String generateChemRegImportLine(SourceChemical sc) {
		if (sc==null) {
			return null;
		}
		
		String externalId = sc.generateSrcChemId();
		String sourceDtxsid = sc.getSourceDtxsid();
		String sourceDtxcid = sc.getSourceDtxcid();
		String sourceDtxrid = sc.getSourceDtxrid();
		String sourceCasrn = sc.getSourceCasrn();
		String sourceChemicalName = sc.getSourceChemicalName();
		String sourceSmiles = sc.getSourceSmiles();
		
		if (sourceDtxsid!=null || sourceDtxcid!=null || sourceDtxrid!=null || sourceCasrn!=null || sourceChemicalName!=null || sourceSmiles!=null) {
			String line = externalId + "\t" 
					+ sourceDtxsid + "\t" 
					+ sourceDtxcid + "\t" 
					+ sourceDtxrid + "\t" 
					+ sourceCasrn + "\t" 
					+ sourceChemicalName + "\t" 
					+ sourceSmiles;
			return line.replaceAll("null", "");
		} else {
			return null;
		}
	}

	private void writeMappingFile(List<MappedPropertyValue> mappedPropertyValues) {
		String mappingJsonFilePath = datasetFolderPath + File.separator + datasetFileName + "_MappedPropertyValues.json";
		File mappingJsonFile = new File(mappingJsonFilePath);
		mappingJsonFile.getParentFile().mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(mappingJsonFile))) {
			bw.write(gson.toJson(mappedPropertyValues));
		} catch (IOException e) {
			logger.error(datasetParams.datasetName + ": Failed to write mapping file");
		}
		
		String mappingJsonFilePathWithUnmapped = datasetFolderPath + File.separator + datasetFileName + "_MappedPropertyValues_WithUnmapped.json";
		File mappingJsonFileWithUnmapped = new File(mappingJsonFilePathWithUnmapped);
		mappingJsonFileWithUnmapped.getParentFile().mkdirs();
		
		List<MappedPropertyValue> mappingsWithUnmapped = new ArrayList<MappedPropertyValue>(mappedPropertyValues);
		for (DiscardedPropertyValue dpv:discardedPropertyValues) {
			MappedPropertyValue mpv = new MappedPropertyValue();
			mpv.propertyValue = dpv.propertyValue;
			mappingsWithUnmapped.add(mpv);
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(mappingJsonFileWithUnmapped))) {
			bw.write(gson.toJson(mappingsWithUnmapped));
		} catch (IOException e) {
			logger.error(datasetParams.datasetName + ": Failed to write mapping file");
		}
	}
	
	private void writeConflictFile() {
		String conflictFilePath = datasetFolderPath + File.separator + datasetFileName + "_Conflicts.txt";
		File conflictFile = new File(conflictFilePath);
		conflictFile.getParentFile().mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(conflictFile))) {
			bw.write("SOURCE_DTXSID\tSOURCE_DTXCID\tSOURCE_CASRN\tSOURCE_CHEMICAL_NAME\tSOURCE_SMILES\t"
					+ "CONFLICT_DTXSID\tCONFLICT_CASRN\tCONFLICT_PREFERRED_NAME\tCONFLICT_SMILES\tCONFLICT_QSAR_READY_SMILES\t"
					+ "CONFLICT_STANDARDIZED_SMILES_IF_USED\tCONFLICT_TYPE\tFRAC_AGREE\tACCEPT\tREASON\r\n");
			for (DsstoxConflict conflict:conflicts) {
				SourceChemical sc = conflict.sourceChemical;
				DsstoxRecord bdr = conflict.bestDsstoxRecord;
				String bin = bdr.connectionReason;
				if (bin!=null) {
					bin = bin.replaceAll("</?b>", "").replaceAll("<br/>", ", ");
				}
				String s1 = sc.getSourceDtxsid() + "\t" + sc.getSourceDtxcid() + "\t" + sc.getSourceCasrn() + "\t" 
						+ sc.getSourceChemicalName() + "\t" + sc.getSourceSmiles() + "\t" 
						+ bdr.dsstoxSubstanceId + "\t" + bdr.casrn + "\t" + bdr.preferredName + "\t" 
						+ bdr.smiles + "\t" + bdr.qsarReadySmiles + "\t" + conflict.bestStandardizedSmiles + "\t"
						+ bin + "\t" + String.valueOf(conflict.fracAgree) + "\t" + conflict.accept + "\t" + conflict.reason + "\r\n";
				bw.write(s1.replaceAll("null", ""));
				for (DsstoxConflictRecord dsstoxConflictRecord:conflict.dsstoxConflictRecords) {
					DsstoxRecord dcr = dsstoxConflictRecord.dsstoxRecord;
					String s2 = sc.getSourceDtxsid() + "\t" + sc.getSourceDtxcid() + "\t" + sc.getSourceCasrn() + "\t" 
							+ sc.getSourceChemicalName() + "\t" + sc.getSourceSmiles() + "\t" 
							+ dcr.dsstoxSubstanceId + "\t" + dcr.casrn + "\t" + dcr.preferredName + "\t" 
							+ dcr.smiles + "\t" + dcr.qsarReadySmiles + "\t" + dsstoxConflictRecord.standardizedSmiles + "\t"
							+ dsstoxConflictRecord.conflictType + "\t" + String.valueOf(conflict.fracAgree) + "\t"  
							+ conflict.accept + "\t" + conflict.reason + "\r\n";
					bw.write(s2.replaceAll("null", ""));
				}
			}
		} catch (IOException e) {
			logger.error(datasetParams.datasetName + ": Failed to write conflict file");
		}
		
		String conflictJsonFilePath = datasetFolderPath + File.separator + datasetFileName + "_Conflicts.json";
		File conflictJsonFile = new File(conflictJsonFilePath);
		conflictJsonFile.getParentFile().mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(conflictJsonFile))) {
			bw.write(gson.toJson(conflicts));
		} catch (IOException e) {
			logger.error(datasetParams.datasetName + ": Failed to write conflict file");
		}
	}
	
	private void writeDiscardedPropertyValuesFile() {
		System.out.println("Writing discarded property values spreadsheet(s)");
		int fileNum=1;
		
		List<DiscardedPropertyValue>values=new ArrayList(discardedPropertyValues);
		
		int max=100000;
//		int max=500;
		
		while(values.size()>0) {

			Workbook wb = new XSSFWorkbook();

			List<DiscardedPropertyValue>values2=new ArrayList<>();
			
			for(int i=1;i<=max;i++) {
				
				values2.add(values.remove(0));
				if(values.size()==0) break;
			}
			
			
			XSSFSheet sheet = (XSSFSheet) wb.createSheet("DiscardedPropertyValues");
			createDiscardedRecordHeader(sheet);

			for (int i = 0; i < values2.size(); i++) {
				DiscardedPropertyValue dpv =values2.get(i);
				PropertyValue pv = dpv.propertyValue;
				DsstoxRecord dr = dpv.dsstoxRecord;
				SourceChemical sc = pv.getSourceChemical();			
				Row row = sheet.createRow(i + 1);
				writeDiscardedRecordRow(dpv, pv, dr, sc, row);
			}
			
			try {
				OutputStream fos=null;
				if(discardedPropertyValues.size()<max) {
					fos = new FileOutputStream(datasetFolderPath + File.separator + "DiscardedPropertyValues.xlsx");
				} else {
					fos = new FileOutputStream(datasetFolderPath + File.separator + "DiscardedPropertyValues"+fileNum+".xlsx");	
				}
				
				wb.write(fos);
				wb.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			fileNum++;
			if(values.size()==0) break;
		}
		

	}

	private void writeDiscardedRecordRow(DiscardedPropertyValue dpv, PropertyValue pv, DsstoxRecord dr,
			SourceChemical sc, Row row) {
		int col = 0;
		row.createCell(col++).setCellValue(pv.getId());
		row.createCell(col++).setCellValue(sc.generateSrcChemId());
		row.createCell(col++).setCellValue(sc.getSourceDtxsid());
		row.createCell(col++).setCellValue(sc.getSourceDtxcid());
		row.createCell(col++).setCellValue(sc.getSourceDtxrid());
		row.createCell(col++).setCellValue(sc.getSourceChemicalName());
		row.createCell(col++).setCellValue(sc.getSourceCasrn());
		row.createCell(col++).setCellValue(sc.getSourceSmiles());
		row.createCell(col++).setCellValue(pv.generateConciseValueString());
		row.createCell(col++).setCellValue(pv.generateParameterValuesString());
		row.createCell(col++).setCellValue(dr==null ? null : dr.dsstoxSubstanceId);
		row.createCell(col++).setCellValue(dr==null ? null : dr.preferredName);
		row.createCell(col++).setCellValue(dr==null ? null : dr.casrn);
		row.createCell(col++).setCellValue(dpv.reason);
	}

	private void createDiscardedRecordHeader(Sheet sheet) {
		Row headerRow = sheet.createRow(0);
		int headerCol = 0;

		headerRow.createCell(headerCol++).setCellValue("EXP_PROP_ID");
		headerRow.createCell(headerCol++).setCellValue("SRC_CHEM_ID");
		headerRow.createCell(headerCol++).setCellValue("SOURCE_DTXSID");
		headerRow.createCell(headerCol++).setCellValue("SOURCE_DTXCID");
		headerRow.createCell(headerCol++).setCellValue("SOURCE_DTXRID");
		headerRow.createCell(headerCol++).setCellValue("SOURCE_CHEMICAL_NAME");
		headerRow.createCell(headerCol++).setCellValue("SOURCE_CASRN");
		headerRow.createCell(headerCol++).setCellValue("SOURCE_SMILES");
		headerRow.createCell(headerCol++).setCellValue("PROPERTY_VALUE");
		headerRow.createCell(headerCol++).setCellValue("PARAMETER_VALUES");
		headerRow.createCell(headerCol++).setCellValue("MAPPED_DTXSID");
		headerRow.createCell(headerCol++).setCellValue("MAPPED_CHEMICAL_NAME");
		headerRow.createCell(headerCol++).setCellValue("MAPPED_CASRN");
		headerRow.createCell(headerCol++).setCellValue("REASON");
	}
	
	
	private void writeDetailedDiscardedPropertyValuesFile() {
		
		System.out.println("Writing detailed discarded property values spreadsheet(s)");
		int fileNum=1;
		List<DiscardedPropertyValue>values=new ArrayList(discardedPropertyValues);
		
		int max=100000;
//		int max=500;
		
		while(values.size()>0) {
		
			Workbook wb = new XSSFWorkbook();
			
			List<DiscardedPropertyValue>values2=new ArrayList<>();
			
			for(int i=1;i<=max;i++) {
				values2.add(values.remove(0));
				if(values.size()==0) break;
			}
			
			
			JsonArray ja=new JsonArray();
			
			for (int i = 0; i < values2.size(); i++) {
				DiscardedPropertyValue dpv = values2.get(i);
				PropertyValue pv = dpv.propertyValue;
				DsstoxRecord dr = dpv.dsstoxRecord;
				SourceChemical sc = pv.getSourceChemical();
				JsonObject jo=DatasetCreator.getDatapointAsJsonObject(null, pv, sc, dr);
				jo.addProperty("reason_discarded", dpv.reason);
				ja.add(jo);			
			}
					
			String[] fields = { "reason_discarded", "exp_prop_id", "source_dtxrid", "source_dtxrid",
					"source_dtxsid", "source_dtxcid", "source_casrn", "source_smiles", "source_chemical_name",				
					"source_name", "source_description", "source_authors", "source_title", "source_doi", "source_url",
					"source_type", "page_url", "notes", "qc_flag", "temperature_c", "pressure_mmHg", "pH",
					"value_qualifier", "value_original", "value_max", "value_min", "value_point_estimate",
					"value_units" };
			
			
			OutputStream fos=null;

			String filePath=null;
			
			if(discardedPropertyValues.size()<max) {
				filePath = datasetFolderPath + File.separator + datasetFileName+"_Discarded_Records"+".xlsx";
			} else {
				filePath = datasetFolderPath + File.separator + datasetFileName+"_Discarded_Records"+fileNum+".xlsx";
			}
			
			ExcelCreator.createExcel2(ja, filePath, fields,null);
		
			Utilities.saveJson(ja, filePath.replace(".xlsx", ".json"));//Save to json so we can limit to PFAS records later

			
			fileNum++;
			if(values.size()==0) break;
		}
		

		
		
		

	}
/*
	private String generateParameterValuesString(PropertyValue pv) {
		StringBuffer sb = new StringBuffer();
		if (pv.getParameterValues()!=null) {
			for (ParameterValue pav:pv.getParameterValues()) {
				if (!sb.isEmpty()) { sb.append("; "); }
				sb.append(pav.getParameter().getName() + ": " + pav.generateConciseValueString());
			}
		}
		
		return sb.toString();
	}
*/

}
