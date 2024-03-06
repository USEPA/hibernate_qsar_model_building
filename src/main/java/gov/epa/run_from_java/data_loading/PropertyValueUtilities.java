package gov.epa.run_from_java.data_loading;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;




import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueService;
import gov.epa.databases.dev_qsar.exp_prop.service.PropertyValueServiceImpl;
import gov.epa.endpoints.datasets.DatasetCreator;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
 * 
 * Does things like:
 * 1. Compare the record counts for a property from chemprop and expprop for the diff sources
 * 2. Remove accidental chemprop duplicates in expprop
 * 
* @author TMARTI02
*/
public class PropertyValueUtilities {
	
	private PropertyValueService propertyValueService = new PropertyValueServiceImpl();


	private void getCountsExpPropPublicSources(String expPropPropertyName,boolean useKeep) {
		List<String>sourceNames=getExpPropPublicSourceNames(expPropPropertyName,useKeep);

		System.out.print("\nPublic sources from ExpProp\n");
		for (String sourceName:sourceNames) {
			//				System.out.println(sourceName);

			//				
			String sql="select count (pv.id) from exp_prop.property_values pv\n"+
					"join exp_prop.properties p on pv.fk_property_id = p.id\n"+
					"join exp_prop.public_sources ls on pv.fk_public_source_id = ls.id\n"+
					"where p.name='"+expPropPropertyName.replace("'","''")+"' and ls.name='"+sourceName+"'";

			if(useKeep) sql+=" and pv.keep=true;";
			else sql+=";";

			//				System.out.println(sql);

			String count=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);

			System.out.println(sourceName+"\t"+count+"\tPublic");
			//
		}
	}

	List<String> getChemPropSourceNames(String chemPropEndpointName) {
		List<String>sourceNames=new ArrayList<>();
		
		String sql="select distinct  cd.name from prod_chemprop.measured_properties mp\n"+
				"join prod_chemprop.endpoints e on mp.fk_endpoint_id = e.id\n"+
				"join prod_chemprop.collection_details cd on mp.fk_measurement_method_id = cd.id\n"+
				"where  e.name='"+chemPropEndpointName.replace("'","''")+"'\n"+
				"order by cd.name ASC;";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);
		
		try {
			while (rs.next()) {
				sourceNames.add(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
 		return sourceNames;
	}
	
	List<String> getExpPropLiteratureSourceNames(String expPropPropertyName,boolean useKeep) {
		List<String>sourceNames=new ArrayList<>();
		
		String sql="select distinct ls.name from exp_prop.property_values pv\n"+
				"join exp_prop.properties p on pv.fk_property_id = p.id\n"+
				"join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id\n"+
				"where p.name='"+expPropPropertyName.replace("'","''")+"'";
		if(useKeep) sql+=" and pv.keep=true\n";
		else sql+="\n";
		
		sql+="order by ls.name ASC;";

		

//		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		try {
			while (rs.next()) {
				sourceNames.add(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
 		return sourceNames;
	}
	
	List<String> getExpPropPublicSourceNames(String expPropPropertyName,boolean useKeep) {
		List<String>sourceNames=new ArrayList<>();
		
		String sql="select distinct ls.name from exp_prop.property_values pv\n"+
				"join exp_prop.properties p on pv.fk_property_id = p.id\n"+
				"join exp_prop.public_sources ls on pv.fk_public_source_id = ls.id\n"+
				"where p.name='"+expPropPropertyName.replace("'","''")+"'";

		if(useKeep) sql+=" and pv.keep=true\n";
		else sql+="\n";

		sql+="order by ls.name ASC;";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		try {
			while (rs.next()) {
				sourceNames.add(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
 		return sourceNames;
	}
	private void getSourceCountsExpProp(String expPropPropertyName,boolean useKeep) {
		getCountsExpPropLiteratureSources(expPropPropertyName,useKeep);
		getCountsExpPropPublicSources(expPropPropertyName,useKeep);
	}

	private void getCountsExpPropLiteratureSources(String expPropPropertyName,boolean useKeep) {
		List<String>sourceNames=getExpPropLiteratureSourceNames(expPropPropertyName,useKeep);

		System.out.print("\nLiterature sources from ExpProp\n");
		
		for (String sourceName:sourceNames) {
//			System.out.println(sourceName);
						//				
			String sql="select count (pv.id) from exp_prop.property_values pv\n"+
					"join exp_prop.properties p on pv.fk_property_id = p.id\n"+
					"join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id\n"+
					"where p.name='"+expPropPropertyName.replace("'","''")+"' and ls.name='"+sourceName+"'";
			if(useKeep) sql+=" and pv.keep=true;";
			else sql+=";";

			//				System.out.println(sql);

			String count=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);

			System.out.println(sourceName+"\t"+count+"\tLiterature");
			//
		}
	}
	void removeChemPropDuplicates(String userName, String propertyName, String sourceName) {
		
		System.out.println("Selecting experimental property data for " + propertyName+ "...");
		long t5 = System.currentTimeMillis();
		
		boolean useKeep=true;
		boolean omitValueQualifiers=false;
		
		List<PropertyValue> propertyValues = propertyValueService.findByPropertyNameWithOptions(propertyName, useKeep, omitValueQualifiers);
		long t6 = System.currentTimeMillis();
		System.out.println("Selection time = " + (t6 - t5)/1000.0 + " s");

		List<String>includedSources=Arrays.asList(sourceName);
		
		System.out.println("Raw records:"+propertyValues.size());		
		DatasetCreator.excludePropertyValues2(includedSources, propertyValues);
		if (includedSources.size()>0) System.out.println(sourceName+", Raw records after source exclusion:"+propertyValues.size());

		propertyValues.sort(new CustomComparator());

		
		for(int i=0;i<propertyValues.size()-1;i++) {
		
			PropertyValue pv1=propertyValues.get(i);
			PropertyValue pv2=propertyValues.get(i+1);
			
			if(!pv1.getKeep() || !pv2.isKeep()) continue;
			
			if (pv1.getKey().equals(pv2.getKey())) {

				System.out.println("\n*****************");
				if(pv2.getCreatedAt().getTime()>pv1.getCreatedAt().getTime()) {
					System.out.println("Second record is newer");
					propertyValues.remove(i);
					pv2.setKeep(false);
					pv2.setUpdatedBy(userName);
					pv2.setKeepReason("Chemprop loading duplicate");
				} else {
					System.out.println("First record is newer");
					propertyValues.remove(i+1);
					pv1.setKeep(false);
					pv1.setUpdatedBy(userName);
					pv1.setKeepReason("Chemprop loading duplicate");
				}
				
				System.out.println(Utilities.gson.toJson(pv1.createJsonObjectFromPropertyValue()));
				System.out.println(Utilities.gson.toJson(pv2.createJsonObjectFromPropertyValue()));
				
				i--;//go back one just in case
			}
						
		}
		
		
		for(int i=0;i<propertyValues.size();i++) {
			PropertyValue pv1=propertyValues.get(i);
			if(pv1.getKeep()) propertyValues.remove(i--);
		}
		
		System.out.println("*************\nRecords to omit:"+propertyValues.size()+"\n***********");		

		for (PropertyValue pv:propertyValues) {
			System.out.println(Utilities.gson.toJson(pv.createJsonObjectFromPropertyValue()));

		}
		
		//Update records:
		propertyValueService.update(propertyValues);
	}
	
	
	public class CustomComparator implements Comparator<PropertyValue> {
	    @Override
	    public int compare(PropertyValue o1, PropertyValue o2) {

	    	return o1.getKey().compareTo(o2.getKey());
	    }
	}
	
	void getSourceCountsChemProp(String chemPropEndpointName) {
		List<String>sourceNames=getChemPropSourceNames(chemPropEndpointName);
		
		System.out.print("\nCollection detail sources from ChemProp\n");
		
		for (String sourceName:sourceNames) {
			
			String sql="select count(mp.id) from prod_chemprop.measured_properties mp\n"+
					"join prod_chemprop.endpoints e on mp.fk_endpoint_id = e.id\n"+
					"join prod_chemprop.collection_details cd on mp.fk_measurement_method_id = cd.id\n"+
					"where e.name='"+chemPropEndpointName.replace("'","''")+"' and cd.name='"+sourceName+"';";
			
			
			
//			select * from measured_properties mp
//			join endpoints e on mp.fk_endpoint_id = e.id
//			join collection_details cd on mp.fk_measurement_method_id = cd.id
//			where  e.name='LogKoa: Octanol-Air'  and cd.name='Danish_EPA_PFOA_Report_2005';
			
//			System.out.println(sql);

			String count=SqlUtilities.runSQL(SqlUtilities.getConnectionDSSTOX(), sql);
			
			System.out.println(sourceName+"\t"+count);

		}
		
	}
	public static void main(String[] args) {
//		String[] endpoints = { "LogKow: Octanol-Water" };
//		String[] endpoints = { "Surface Tension" };
//		String[] endpoints = { "Water Solubility" };
//		String[] endpoints = { "Boiling Point" };
//		String[] endpoints = { "Vapor Pressure" };
//		String[] endpoints = { "Flash Point" };
		String[] endpoints = { "Melting Point" };
//		String[] endpoints = { "Density" };
//		String[] endpoints = { "Surface Tension"};
//		String[] endpoints = { "Acute Oral LD50"};
//		String[] endpoints = { "Fish Biotrans. Half-Life (Km)"};//days
//		String[] endpoints = { "Biodeg. Half-Life"};//days
//		String[] endpoints = { "Soil Adsorp. Coeff. (Koc)"};//
//		String[] endpoints = { "Atmos. Hydroxylation Rate"};//
//		String[] endpoints = { "Biodegration Class"};//
//		String[] endpoints = { "Henry's Law"};//
//		String[] endpoints = { "LogKow: Octanol-Water"};
//		String[] endpoints = { "LogKoa: Octanol-Air"};
//		String[] endpoints = { "Bioconcentration Factor"};
//		String[] endpoints = { "Fraction Unbound in Human Plasma"};
//		String[] endpoints = { "In Vitro Intrinsic Hepatic Clearance"};
//		String[] endpoints = { "pKa Basic Apparent"};
//		String[] endpoints = { "pKa Acidic Apparent"};
//		String[] endpoints = { "Log(air-water partition coefficient)"};
		
//		String[] endpoints = { "Bioconcentration Factor"};
		
		PropertyValueUtilities c=new PropertyValueUtilities();
		

		String endpoint=endpoints[0];
		String property=ChemPropLoader.correctPropertyName(endpoint);
		
		c.getSourceCountsChemProp(endpoint);
		c.getSourceCountsExpProp(property,true);
//		System.out.println("\n****************\nuseKeep=false");
//		c.getSourceCountsExpProp(property,false);

//		c.removeChemPropDuplicates("tmarti02",property,"ANGUS Chemical Company (Chemical company)");
//		c.removeChemPropDuplicates("tmarti02",property,"Braekevelt et al. Chemosphere 51 (2003) 563–567");
//		c.removeChemPropDuplicates("tmarti02",property,"Egon Willighagen");
//		c.removeChemPropDuplicates("tmarti02",property,"Hidalgo at al ");//note the space at end
//		c.removeChemPropDuplicates("tmarti02",property,"Kurz & Ballschmiter 1999");
//		c.removeChemPropDuplicates("tmarti02",property,"Life Chemicals (Chemical company)");
//		c.removeChemPropDuplicates("tmarti02",property,"Synthon-Lab (Chemical company)");
		
		


	}

}
