package gov.epa.endpoints.datasets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public class DatasetParams {
	
	public static class MappingParams {
		public String dsstoxMappingId;
		public String chemicalListName;
		public ArrayList<String> chemRegListNameList;
		public boolean isNaive;
		public boolean useCuratorValidation;
		public boolean requireCuratorValidation;
		public boolean autoResolveConflicts;
		public boolean validateConflictsTogether;
		public boolean omitOpsinAmbiguousNames;
		public boolean omitUvcbKeywords;
		public boolean omitSalts;
		
		public MappingParams(String dsstoxMappingId, String chemicalListName, boolean isNaive, boolean useValidation, 
				boolean requireValidation, boolean resolveConflicts,
				boolean validateConflictsTogether, boolean omitOpsinAmbiguousNames, boolean omitUvcbNames,
				ArrayList<String> chemRegListNameList) {
			this.dsstoxMappingId = dsstoxMappingId;
			this.isNaive = isNaive;
			this.useCuratorValidation = useValidation;
			this.requireCuratorValidation = requireValidation;
			this.autoResolveConflicts = resolveConflicts;
			this.validateConflictsTogether = validateConflictsTogether;
			this.omitOpsinAmbiguousNames = omitOpsinAmbiguousNames;
			this.omitUvcbKeywords = omitUvcbNames;
			this.chemicalListName = chemicalListName;
			this.chemRegListNameList = chemRegListNameList;
		}
		
		public MappingParams(String dsstoxMappingId, boolean isNaive, boolean useValidation, boolean requireValidation, boolean resolveConflicts,
				boolean validateConflictsTogether, boolean omitOpsinAmbiguousNames, boolean omitUvcbKeywords, ArrayList<String> chemRegListNameList) {
			this(dsstoxMappingId, null, isNaive, useValidation, requireValidation, resolveConflicts, validateConflictsTogether,
					omitOpsinAmbiguousNames, omitUvcbKeywords, chemRegListNameList);
		}
	}
	
	public String datasetName;
	public String datasetDescription;
	
	public String propertyName;
	
	public MappingParams mappingParams;
	
	public List<BoundParameterValue> bounds;
	
	public DatasetParams(String datasetName, String datasetDescription, String propertyName, 
			MappingParams mappingParams, List<BoundParameterValue> bounds) {
		this.datasetName = datasetName;
		this.datasetDescription = datasetDescription;
		this.propertyName = propertyName;
		this.mappingParams = mappingParams;
		this.bounds = bounds;
	}
	
	public DatasetParams(String datasetName, String datasetDescription, String propertyName, 
			MappingParams mappingParams) {
		this(datasetName, datasetDescription, propertyName, mappingParams, null);
	}
	
	/**
	 * Test a list of parameter values against the provided bounds
	 * TODO handle different units (or at least give a warning!)
	 * 
	 * @param parameterValues
	 * @param bounds
	 * @return
	 */
	public ExplainedResponse testParameterValues(PropertyValue propertyValue) {
		if (bounds==null) {
			// If no parameter value bounds, don't eliminate anything
			return new ExplainedResponse(true, "No bounds to test");
		}
		
		List<ParameterValue> parameterValues = propertyValue.getParameterValues();
		HashMap<String, ParameterValue> parameterValuesMap = new HashMap<String, ParameterValue>();
		for (ParameterValue pv:parameterValues) {
			parameterValuesMap.put(pv.getParameter().getName(), pv);
		}
		
		ExplainedResponse testResponse = null;
		Iterator<BoundParameterValue> it = bounds.iterator();
		while (it.hasNext()) {
			BoundParameterValue bound = it.next();
			ParameterValue pv = parameterValuesMap.get(bound.parameterName);
			testResponse = bound.test(pv);
			if (!testResponse.response) {
				break;
			}
		}
		
		return testResponse;
	}
}
