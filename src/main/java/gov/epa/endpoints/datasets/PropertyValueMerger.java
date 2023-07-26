package gov.epa.endpoints.datasets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.PropertyValueValidator;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public class PropertyValueMerger {
	
//	private static final Logger logger = LogManager.getLogger(PropertyValueMerger.class);
	
	public static Double mergeBinary(List<MappedPropertyValue> mappedPropertyValues) {
		Double consensusValue = 0.0;
		for (MappedPropertyValue mpv:mappedPropertyValues) {
			consensusValue += mpv.qsarPropertyValue;
		}
		consensusValue /= mappedPropertyValues.size();
		
		if (consensusValue < DevQsarConstants.BINARY_FRAC_AGREE && consensusValue > (1-DevQsarConstants.BINARY_FRAC_AGREE)) {
			System.out.println(mappedPropertyValues.iterator().next().standardizedSmiles + ": Ambiguous binary consensus value: " + consensusValue);
			return null;
		} else {
			return consensusValue > DevQsarConstants.BINARY_CUTOFF ? 1.0 : 0.0;
		}
	}
	
	public static String [] mergeBinaryIncludeFinalCIDsExpPropIDs(List<MappedPropertyValue> mappedPropertyValues) {
		Double consensusValue = 0.0;
		for (MappedPropertyValue mpv:mappedPropertyValues) {
			consensusValue += mpv.qsarPropertyValue;
		}
		consensusValue /= mappedPropertyValues.size();
		
		if (consensusValue < DevQsarConstants.BINARY_FRAC_AGREE && consensusValue > (1-DevQsarConstants.BINARY_FRAC_AGREE)) {
			System.out.println(mappedPropertyValues.iterator().next().standardizedSmiles + ": Ambiguous binary consensus value: " + consensusValue);
			return null;
		} else {
			
			Double finalValue=consensusValue > DevQsarConstants.BINARY_CUTOFF ? 1.0 : 0.0;
			Vector<String> vecFinalCIDs=new Vector<>();
			Vector<Long> vecFinalExpPropIds=new Vector<>();
			
			for (MappedPropertyValue mpv:mappedPropertyValues) {
				if (mpv.qsarPropertyValue==finalValue) {
//					if (!vecFinalCIDs.contains(mpv.compound.getDtxcid()))
//						vecFinalCIDs.add(mpv.compound.getDtxcid());					

					vecFinalCIDs.add(mpv.compound.getDtxcid());					
					vecFinalExpPropIds.add(mpv.propertyValue.getId());
				}
			}			

			String finalCIDs="";
			
			for (int i=0;i<vecFinalCIDs.size();i++) {
				finalCIDs+=vecFinalCIDs.get(i);
				if (i<vecFinalCIDs.size()-1) finalCIDs+="|";
			}
			
			String finalExpPropIDs="";
			
			for (int i=0;i<vecFinalExpPropIds.size();i++) {
				finalExpPropIDs+=vecFinalExpPropIds.get(i);
				if (i<vecFinalExpPropIds.size()-1) finalExpPropIDs+="|";
			}
			
			String [] result= new String[3];
			result[0]=finalValue+"";
			result[1]=finalCIDs;
			result[2]=finalExpPropIDs;		
			return result;			
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
			if (PropertyValueValidator.checkRangeForProperty(v1, v2, propertyName)) {
				return (v1 + v2) / 2.0;
			} else {
				System.out.println(mappedPropertyValues.iterator().next().standardizedSmiles + ": Range too wide to calculate consensus value");
				return null;
			}
		} else if (size==1) {
			return mappedPropertyValues.get(0).qsarPropertyValue;
		} else {
			return mappedPropertyValues.get(size / 2).qsarPropertyValue;
		}
	}
	
	
	public static String [] mergeContinuousIncludeFinalCIDsExpPropIDs(List<MappedPropertyValue> mappedPropertyValues, String propertyName) {
		Collections.sort(mappedPropertyValues, new Comparator<MappedPropertyValue>() {
			@Override
			public int compare(MappedPropertyValue mpv1, MappedPropertyValue mpv2) {
				return mpv1.qsarPropertyValue.compareTo(mpv2.qsarPropertyValue);
			}
		});
		
		int size = mappedPropertyValues.size();
		
		Double finalValue=null;
		String finalCIDs=null;
		String final_exp_prop_ids=null;
		
		
		if (size % 2 == 0) {//even number of records, need to determine average of middle 2 values
			MappedPropertyValue mpv1 = mappedPropertyValues.get(size / 2 - 1);
			MappedPropertyValue mpv2 = mappedPropertyValues.get(size / 2);
			Double v1 = mpv1.qsarPropertyValue;
			Double v2 = mpv2.qsarPropertyValue;
			
			if (PropertyValueValidator.checkRangeForProperty(v1, v2, propertyName)) {
				finalValue= (v1 + v2) / 2.0;

//				if (mpv1.compound.getDtxcid().equals(mpv2.compound.getDtxcid())) {
//					finalCIDs=mpv1.compound.getDtxcid();
//				} else {
//					finalCIDs=mpv1.compound.getDtxcid()+"|"+mpv2.compound.getDtxcid();	
//				}
				
				finalCIDs=mpv1.compound.getDtxcid()+"|"+mpv2.compound.getDtxcid();//Need both so can line up with CIDs
				final_exp_prop_ids=mpv1.propertyValue.getId()+"|"+mpv2.propertyValue.getId();	
								
			} else {
				System.out.println(mappedPropertyValues.iterator().next().standardizedSmiles + ": Range too wide to calculate consensus value");
				return null;
			}
		} else if (size==1) {//only 1 record
			finalValue = mappedPropertyValues.get(0).qsarPropertyValue;
			finalCIDs= mappedPropertyValues.get(0).compound.getDtxcid();
			final_exp_prop_ids=mappedPropertyValues.get(0).propertyValue.getId()+"";
			
		} else {//odd number of records, use middle one
			finalValue = mappedPropertyValues.get(size/2).qsarPropertyValue;
			finalCIDs= mappedPropertyValues.get(size/2).compound.getDtxcid();
			final_exp_prop_ids=mappedPropertyValues.get(size/2).propertyValue.getId()+"";
		}		
		String [] result= new String[3];
		result[0]=finalValue+"";
		result[1]=finalCIDs;		
		result[2]=final_exp_prop_ids;
		return result;
	}


}
