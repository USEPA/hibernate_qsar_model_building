package gov.epa.endpoints.datasets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public class PropertyValueMerger {
	
	private static final Logger logger = LogManager.getLogger(PropertyValueMerger.class);
	
	public static Double mergeBinary(List<MappedPropertyValue> mappedPropertyValues) {
		Double consensusValue = 0.0;
		for (MappedPropertyValue mpv:mappedPropertyValues) {
			consensusValue += mpv.qsarPropertyValue;
		}
		consensusValue /= mappedPropertyValues.size();
		
		if (consensusValue < DevQsarConstants.BINARY_FRAC_AGREE && consensusValue > (1-DevQsarConstants.BINARY_FRAC_AGREE)) {
			logger.debug(mappedPropertyValues.iterator().next().standardizedSmiles + ": Ambiguous binary consensus value: " + consensusValue);
			return null;
		} else {
			return consensusValue > DevQsarConstants.BINARY_CUTOFF ? 1.0 : 0.0;
		}
	}
	
	public static Double mergeContinuous(List<MappedPropertyValue> mappedPropertyValues, String propertyName) {
		Collections.sort(mappedPropertyValues, new Comparator<MappedPropertyValue>() {
			@Override
			public int compare(MappedPropertyValue mpv1, MappedPropertyValue mpv2) {
				return mpv1.qsarPropertyValue.compareTo(mpv2.qsarPropertyValue);
			}
		});
		
		int size = mappedPropertyValues.size();
		if (size % 2 == 0) {
			MappedPropertyValue mpv1 = mappedPropertyValues.get(size / 2 - 1);
			MappedPropertyValue mpv2 = mappedPropertyValues.get(size / 2);
			Double v1 = mpv1.qsarPropertyValue;
			Double v2 = mpv2.qsarPropertyValue;
			if (PropertyValue.checkRangeForProperty(v1, v2, propertyName)) {
				return (v1 + v2) / 2.0;
			} else {
				logger.debug(mappedPropertyValues.iterator().next().standardizedSmiles + ": Range too wide to calculate consensus value");
				return null;
			}
		} else if (size==1) {
			return mappedPropertyValues.get(0).qsarPropertyValue;
		} else {
			return mappedPropertyValues.get(size / 2).qsarPropertyValue;
		}
	}

}
