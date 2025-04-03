package gov.epa.run_from_java.scripts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class CreateTablesForPaper {

	class DataPointContributorCount {
		String propertyName;
		String sourceName;
		int count;
	}
	
	
	void getCountDPCByPropertyAndSource() {
		
		
		String sqlCountDPC = "select p.name,\r\n"
				+ "       case when ps.name is not null then ps.name else ls.name end as source_name,\r\n"
				+ "       count(dpc.id)\r\n"
				+ "from qsar_datasets.data_points dp\r\n"
				+ "         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id\r\n"
				+ "         join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id\r\n"
				+ "         left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id\r\n"
				+ "         left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id\r\n"
				+ "         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
				+ "         join qsar_datasets.properties p on d.fk_property_id = p.id\r\n"
				+ "         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id\r\n"
				+ "where d.id = did.fk_datasets_id and keep = true\r\n"
				+ "group by source_name, p.name\r\n"
				+ "order by p.name, source_name;";
			
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlCountDPC);
		
		try {
			List<DataPointContributorCount>dpccs=new ArrayList<>();

			Hashtable<String,Hashtable<String,Integer>>ht=new Hashtable<>();
			
			Hashtable<String,Integer>sumByProperty=new Hashtable<>();
			
			
			while (rs.next()) {
				
				DataPointContributorCount dpcc=new DataPointContributorCount();
				dpcc.propertyName=rs.getString(1);
				dpcc.sourceName=rs.getString(2);
				dpcc.count=rs.getInt(3);
				dpccs.add(dpcc);
				
				if(ht.containsKey(dpcc.sourceName)) {
					Hashtable<String,Integer>htDPCC=ht.get(dpcc.sourceName);
					htDPCC.put(dpcc.propertyName, dpcc.count);
					
				} else {
					Hashtable<String,Integer>htDPCC=new Hashtable<>();
					htDPCC.put(dpcc.propertyName, dpcc.count);
					ht.put(dpcc.sourceName, htDPCC);
				}
				
			}
			

			List<String>properties=Arrays.asList(DevQsarConstants.HENRYS_LAW_CONSTANT,DevQsarConstants.VAPOR_PRESSURE,
					DevQsarConstants.BOILING_POINT,DevQsarConstants.WATER_SOLUBILITY,DevQsarConstants.LOG_KOW,DevQsarConstants.MELTING_POINT);
					
			
			System.out.print("Source\t");
			
			for(String property:properties) {
				System.out.print(property+"\t");
			}
			System.out.println("Sum");
			
			
			for(String sourceName:ht.keySet()) {
				System.out.print(sourceName+"\t");
				Hashtable<String,Integer>htDPCC=ht.get(sourceName);
				int sumSource=0;
				for(String property:properties) {
					if(htDPCC.containsKey(property)) {
						int count=htDPCC.get(property);
						sumSource+=count;
						System.out.print(count+"\t");
						
						if(sumByProperty.containsKey(property)) {
							sumByProperty.put(property, sumByProperty.get(property)+count);
						} else {
							sumByProperty.put(property, count);
						}
						
					} else {
						System.out.print(0+"\t");
					}
					
					
				}
				System.out.print(sumSource+"\n");
			}
			
			System.out.print("Sum\t");
			
			int sumTotal=0;
			
			for(String property:properties) {
				System.out.print(sumByProperty.get(property)+"\t");
				sumTotal+=sumByProperty.get(property);
			}
			System.out.println(sumTotal);
			
//			System.out.println(Utilities.gson.toJson(ht));
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		HLC	VP	BP	WS	LogP	MP
		
		
	}
	

	void getCountDPCByProperty() {
		
		
		try {
			
			//Get dpc counts by property name:
			String sqlCountDPC = "select p.name,\r\n"
					+ "       count(dpc.id)\r\n"
					+ "from qsar_datasets.data_points dp\r\n"
					+ "         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id\r\n"
					+ "         join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id\r\n"
					+ "         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
					+ "         join qsar_datasets.properties p on d.fk_property_id = p.id\r\n"
					+ "         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id\r\n"
					+ "where d.id = did.fk_datasets_id and keep = true\r\n"
					+ "group by p.name\r\n"
					+ "order by p.name;";

			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlCountDPC);
			Hashtable<String,Integer>htDPC=new Hashtable<>();
			while (rs.next()) {
				htDPC.put(rs.getString(1), rs.getInt(2));
			}

			//Get dp counts by property name:
			String sqlCountDP="select p.name,count(dp.id)\r\n"
					+ "from qsar_datasets.data_points dp\r\n"
					+ "         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
					+ "         join qsar_datasets.properties p on d.fk_property_id = p.id\r\n"
					+ "         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id\r\n"
					+ "            join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles\r\n"
					+ "join qsar_descriptors.descriptor_sets ds on dv.fk_descriptor_set_id = ds.id\r\n"
					+ "where d.id = did.fk_datasets_id and ds.name='WebTEST-default' and dv.values_tsv is not null\r\n"
					+ "group by p.name\r\n"
					+ "order by p.name;";
					

			rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlCountDP);
			Hashtable<String,Integer>htDP=new Hashtable<>();
			while (rs.next()) {
				htDP.put(rs.getString(1), rs.getInt(2));
			}
			

			List<String>properties=Arrays.asList(DevQsarConstants.HENRYS_LAW_CONSTANT,DevQsarConstants.VAPOR_PRESSURE,
					DevQsarConstants.BOILING_POINT,DevQsarConstants.WATER_SOLUBILITY,DevQsarConstants.LOG_KOW,DevQsarConstants.MELTING_POINT);
			
			System.out.println("Source\tCount");
			for(String property:properties) {
				System.out.println(property+"\t"+htDPC.get(property)+"\t"+htDP.get(property)+"\t");
			}
//			System.out.println(Utilities.gson.toJson(ht));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		HLC	VP	BP	WS	LogP	MP
		
		
	}
	
	
	
	
	
	public static void main(String[] args) {
		CreateTablesForPaper c=new CreateTablesForPaper();
		c.getCountDPCByPropertyAndSource();
//		c.getCountDPCByProperty();

	}

}
