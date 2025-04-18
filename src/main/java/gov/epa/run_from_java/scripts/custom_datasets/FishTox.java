package gov.epa.run_from_java.scripts.custom_datasets;

import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

//import gov.epa.exp_data_gathering.parse.CompareExperimentalRecords;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class FishTox {
	
	
	class DPC {
		
		String canon_qsar_smiles;
		Double qsar_prop_value;
		Integer split_num;
		String species_common;
		String exposure_type;
		String values_tsv;
		
		
	}
	
	void createFishToxDataSetWithCommonSpeciesAndExposureType() {
		
		String speciesCommon="all_fish";
		String datasetName="ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		String descriptorSetName="Mordred-default";

		String sqlDescriptorSetId="select id,headers_tsv from qsar_descriptors.descriptor_sets where name='"+descriptorSetName+"';";
		ResultSet rsDescriptorSet=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDescriptorSetId);
		Integer descriptorSetId=null;
		String headerTsv=null;
		try {
			rsDescriptorSet.next();
			descriptorSetId = rsDescriptorSet.getInt(1);
			headerTsv=rsDescriptorSet.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		String sql="select dp.canon_qsar_smiles,-log10(dpc.property_value) as qsar_prop_value,dpis.split_num, pv2.value_text as species_common,pv3.value_text as exposure_type, dv.values_tsv from qsar_datasets.data_point_contributors dpc\r\n"
				+ "		join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id\r\n"
				+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
				+ "		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
				+ "		join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id\r\n"
				+ "		join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11\r\n"
				+ "		join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13\r\n"
				+ "		join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles and dv.fk_descriptor_set_id="+descriptorSetId+"\r\n"
				+ "		where d.name='"+datasetName+"' and dpis.fk_splitting_id=1;";

//		System.out.println(sql);
		
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		List<DPC>dpcs=new ArrayList<>();
		
		List<String>list_species_common=new ArrayList<>();	
		List<String>list_exposure_type=new ArrayList<>();
		
		try {
			while (rs.next()) {
				DPC dpc=new DPC();
				dpc.canon_qsar_smiles=rs.getString(1);
				dpc.qsar_prop_value=rs.getDouble(2);
				dpc.split_num=rs.getInt(3);
				dpc.species_common=rs.getString(4);
				dpc.exposure_type=rs.getString(5);
				dpc.values_tsv=rs.getString(6);
				
				if(!list_species_common.contains(dpc.species_common)) 
					list_species_common.add(dpc.species_common);
				
				if(!list_exposure_type.contains(dpc.exposure_type))
					list_exposure_type.add(dpc.exposure_type);
				
				dpcs.add(dpc);
			}
			
			Collections.sort(list_exposure_type);
			Collections.sort(list_species_common);
			
//			for (String species_common:hs_species_common) System.out.println(species_common);
//			System.out.println("");
//			for (String exposure_type:hs_exposure_type) System.out.println(exposure_type);
			
			//determine median by factor
			TreeMap<String,List<DPC>>htByFactor=new TreeMap<>();
			
			for (DPC dpc:dpcs) {
				
				String key=dpc.canon_qsar_smiles+"\t"+dpc.species_common+"\t"+dpc.exposure_type;
				if(htByFactor.containsKey(key)) {
					List<DPC>dpcs2=htByFactor.get(key);
					dpcs2.add(dpc);
				} else {

					List<DPC>dpcs2=new ArrayList<>();
					dpcs2.add(dpc);
					htByFactor.put(key, dpcs2);
				}
				
			}
			
			List<DPC>dpcsFlat=new ArrayList<>();
			String folder="data\\modeling\\"+datasetName+"\\species_common_exposure_type_"+speciesCommon+"\\"+descriptorSetName+"\\";
			File Folder=new File(folder);
			Folder.mkdirs();
			
			FileWriter fwTrain=new FileWriter(folder+"/train.tsv");
			FileWriter fwTest=new FileWriter(folder+"/test.tsv");
			
			
			String header="canon_qsar_smiles\tqsar_property_value\t";
			for (String exposure_type:list_exposure_type) header+="exposure_type_"+exposure_type+"\t";
			for (String species_common:list_species_common) header+="species_common_"+species_common+"\t";
			header+=headerTsv+"\r\n";
			
			fwTrain.write(header);
			fwTest.write(header);
			
			for (String key:htByFactor.keySet()) {
				List<DPC>dpcs2=htByFactor.get(key);
				double median=getMedianValue(dpcs2);
				DPC dpc0=dpcs2.get(0);
				
				dpcsFlat.add(dpc0);
//				System.out.println(key+"\t"+htByFactor.get(key).size()+"\t"+median);
				
				String line=dpc0.canon_qsar_smiles+"\t"+median+"\t";
				
				for (int i=0;i<list_exposure_type.size();i++) {
					if(dpc0.exposure_type.equals(list_exposure_type.get(i))) {
						line+=1;
					} else {
						line+=0;
					}
					
					line+="\t";
				}
				
				for (int i=0;i<list_species_common.size();i++) {
					if(dpc0.species_common.equals(list_species_common.get(i))) {
						line+=1;
					} else {
						line+=0;
					}
					
					line+="\t";
				}
				
				line+=dpc0.values_tsv+"\r\n";
				
				if(dpc0.split_num==0) {
					fwTrain.write(line);
				} else {
					fwTest.write(line);
				}
				
			}

			System.out.println(dpcs.size());
			System.out.println(dpcsFlat.size());
			

			fwTrain.flush();
			fwTest.flush();
			fwTrain.close();
			fwTest.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
	}
	
	

	void createFishToxDataSetWithCommonSpecies() {
		
		String speciesCommon="all_fish";
		String datasetName="ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		String descriptorSetName="Mordred-default";

		String sqlDescriptorSetId="select id,headers_tsv from qsar_descriptors.descriptor_sets where name='"+descriptorSetName+"';";
		ResultSet rsDescriptorSet=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDescriptorSetId);
		Integer descriptorSetId=null;
		String headerTsv=null;
		try {
			rsDescriptorSet.next();
			descriptorSetId = rsDescriptorSet.getInt(1);
			headerTsv=rsDescriptorSet.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		String sql="select dp.canon_qsar_smiles,-log10(dpc.property_value) as qsar_prop_value,dpis.split_num, pv2.value_text as species_common,dv.values_tsv from qsar_datasets.data_point_contributors dpc\r\n"
				+ "		join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id\r\n"
				+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
				+ "		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
				+ "		join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id\r\n"
				+ "		join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11\r\n"
				+ "		join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13\r\n"
				+ "		join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles and dv.fk_descriptor_set_id="+descriptorSetId+"\r\n"
				+ "		where d.name='"+datasetName+"' and dpis.fk_splitting_id=1;";

//		System.out.println(sql);
		
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		List<DPC>dpcs=new ArrayList<>();
		
		List<String>list_species_common=new ArrayList<>();	
		List<String>list_exposure_type=new ArrayList<>();
		
		try {
			while (rs.next()) {
				DPC dpc=new DPC();
				dpc.canon_qsar_smiles=rs.getString(1);
				dpc.qsar_prop_value=rs.getDouble(2);
				dpc.split_num=rs.getInt(3);
				dpc.species_common=rs.getString(4);
				dpc.values_tsv=rs.getString(5);
				
				if(!list_species_common.contains(dpc.species_common)) 
					list_species_common.add(dpc.species_common);
				
				if(!list_exposure_type.contains(dpc.exposure_type))
					list_exposure_type.add(dpc.exposure_type);
				
				dpcs.add(dpc);
			}
			
			Collections.sort(list_exposure_type);
			Collections.sort(list_species_common);
			
//			for (String species_common:hs_species_common) System.out.println(species_common);
//			System.out.println("");
//			for (String exposure_type:hs_exposure_type) System.out.println(exposure_type);
			
			//determine median by factor
			TreeMap<String,List<DPC>>htByFactor=new TreeMap<>();
			
			for (DPC dpc:dpcs) {
				
				String key=dpc.canon_qsar_smiles+"\t"+dpc.species_common;
				if(htByFactor.containsKey(key)) {
					List<DPC>dpcs2=htByFactor.get(key);
					dpcs2.add(dpc);
				} else {

					List<DPC>dpcs2=new ArrayList<>();
					dpcs2.add(dpc);
					htByFactor.put(key, dpcs2);
				}
				
			}
			
			List<DPC>dpcsFlat=new ArrayList<>();
			String folder="data\\modeling\\"+datasetName+"\\species_common_"+speciesCommon+"\\"+descriptorSetName+"\\";
			File Folder=new File(folder);
			Folder.mkdirs();
			
			FileWriter fwTrain=new FileWriter(folder+"/train.tsv");
			FileWriter fwTest=new FileWriter(folder+"/test.tsv");
			FileWriter fwTest2=new FileWriter(folder+"/testFHM.tsv");

			
			String header="canon_qsar_smiles\tqsar_property_value\t";
//			for (String exposure_type:list_exposure_type) header+="exposure_type_"+exposure_type+"\t";
			for (String species_common:list_species_common) header+="species_common_"+species_common+"\t";
			header+=headerTsv+"\r\n";
			
			fwTrain.write(header);
			fwTest.write(header);
			fwTest2.write(header);
			
			for (String key:htByFactor.keySet()) {
				List<DPC>dpcs2=htByFactor.get(key);
				double median=getMedianValue(dpcs2);
				DPC dpc0=dpcs2.get(0);
				
				dpcsFlat.add(dpc0);
//				System.out.println(key+"\t"+htByFactor.get(key).size()+"\t"+median);
				
				String line=dpc0.canon_qsar_smiles+"\t"+median+"\t";
				
//				for (int i=0;i<list_exposure_type.size();i++) {
//					if(dpc0.exposure_type.equals(list_exposure_type.get(i))) {
//						line+=1;
//					} else {
//						line+=0;
//					}
//					
//					line+="\t";
//				}
				
				for (int i=0;i<list_species_common.size();i++) {
					if(dpc0.species_common.equals(list_species_common.get(i))) {
						line+=1;
					} else {
						line+=0;
					}
					
					line+="\t";
				}
				
				line+=dpc0.values_tsv+"\r\n";
				
				if(dpc0.split_num==0) {
					fwTrain.write(line);
				} else {
					fwTest.write(line);
					
					if(dpc0.species_common.equals("Fathead Minnow")) {
						fwTest2.write(line);
					}

				}
				
			}

			System.out.println(dpcs.size());
			System.out.println(dpcsFlat.size());
			

			fwTrain.flush();
			fwTest.flush();
			fwTest2.flush();
			
			fwTrain.close();
			fwTest.close();
			fwTest2.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
	}
	
	void createFHM_BG_RT_ToxDataSetWithCommonSpecies() {
		
		String speciesCommon="FHM_BG_RT";
		String datasetName="ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		String descriptorSetName="Mordred-default";

		String sqlDescriptorSetId="select id,headers_tsv from qsar_descriptors.descriptor_sets where name='"+descriptorSetName+"';";
		ResultSet rsDescriptorSet=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDescriptorSetId);
		Integer descriptorSetId=null;
		String headerTsv=null;
		try {
			rsDescriptorSet.next();
			descriptorSetId = rsDescriptorSet.getInt(1);
			headerTsv=rsDescriptorSet.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		String sql="select dp.canon_qsar_smiles,-log10(dpc.property_value) as qsar_prop_value,dpis.split_num, pv2.value_text as species_common,dv.values_tsv from qsar_datasets.data_point_contributors dpc\r\n"
				+ "		join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id\r\n"
				+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
				+ "		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
				+ "		join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id\r\n"
				+ "		join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11\r\n"
				+ "		join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13\r\n"
				+ "		join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles and dv.fk_descriptor_set_id="+descriptorSetId+"\r\n"
				+ "		where d.name='"+datasetName+"' and dpis.fk_splitting_id=1 and pv2.value_text in ('Fathead Minnow','Rainbow Trout','Bluegill');";

//		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		List<DPC>dpcs=new ArrayList<>();
		
		List<String>list_species_common=new ArrayList<>();	
		List<String>list_exposure_type=new ArrayList<>();
		
		try {
			while (rs.next()) {
				DPC dpc=new DPC();
				dpc.canon_qsar_smiles=rs.getString(1);
				dpc.qsar_prop_value=rs.getDouble(2);
				dpc.split_num=rs.getInt(3);
				dpc.species_common=rs.getString(4);
				dpc.values_tsv=rs.getString(5);
				
				if(!list_species_common.contains(dpc.species_common)) 
					list_species_common.add(dpc.species_common);
				
				if(!list_exposure_type.contains(dpc.exposure_type))
					list_exposure_type.add(dpc.exposure_type);
				
				dpcs.add(dpc);
			}
			
			Collections.sort(list_exposure_type);
			Collections.sort(list_species_common);
			
//			for (String species_common:hs_species_common) System.out.println(species_common);
//			System.out.println("");
//			for (String exposure_type:hs_exposure_type) System.out.println(exposure_type);
			
			//determine median by factor
			TreeMap<String,List<DPC>>htByFactor=new TreeMap<>();
			
			for (DPC dpc:dpcs) {
				
				String key=dpc.canon_qsar_smiles+"\t"+dpc.species_common;
				if(htByFactor.containsKey(key)) {
					List<DPC>dpcs2=htByFactor.get(key);
					dpcs2.add(dpc);
				} else {

					List<DPC>dpcs2=new ArrayList<>();
					dpcs2.add(dpc);
					htByFactor.put(key, dpcs2);
				}
				
			}
			
			List<DPC>dpcsFlat=new ArrayList<>();
			String folder="data\\modeling\\"+datasetName+"\\species_common_"+speciesCommon+"\\"+descriptorSetName+"\\";
			File Folder=new File(folder);
			Folder.mkdirs();
			
			FileWriter fwTrain=new FileWriter(folder+"/train.tsv");
			FileWriter fwTest=new FileWriter(folder+"/test.tsv");
			FileWriter fwTest2=new FileWriter(folder+"/testFHM.tsv");
			FileWriter fwCompareSpecies=new FileWriter(folder+"/compare_species.tsv");

			
			String header="canon_qsar_smiles\tqsar_property_value\t";
//			for (String exposure_type:list_exposure_type) header+="exposure_type_"+exposure_type+"\t";
			for (String species_common:list_species_common) header+="species_common_"+species_common+"\t";
			header+=headerTsv+"\r\n";
			
			fwTrain.write(header);
			fwTest.write(header);
			fwTest2.write(header);
			fwCompareSpecies.write("smiles\tFHM\tRT\tBG\r\n");

			
			Hashtable<String,Double>htFHM=new Hashtable<>();
			Hashtable<String,Double>htRT=new Hashtable<>();
			Hashtable<String,Double>htBG=new Hashtable<>();

			HashSet<String>hs_smiles=new HashSet<>();
			
			for (String key:htByFactor.keySet()) {
				List<DPC>dpcs2=htByFactor.get(key);
				double median=getMedianValue(dpcs2);
				DPC dpc0=dpcs2.get(0);
				
				dpcsFlat.add(dpc0);
				
				hs_smiles.add(dpc0.canon_qsar_smiles);
				
				if(dpc0.species_common.equals("Bluegill")) {
					htBG.put(dpc0.canon_qsar_smiles,median);
				} else if(dpc0.species_common.equals("Fathead Minnow")) {
					htFHM.put(dpc0.canon_qsar_smiles,median);
				} else if(dpc0.species_common.equals("Rainbow Trout")) {
					htRT.put(dpc0.canon_qsar_smiles,median);
				}
				
//				System.out.println(key+"\t"+htByFactor.get(key).size()+"\t"+median);
				
				String line=dpc0.canon_qsar_smiles+"\t"+median+"\t";
				
//				for (int i=0;i<list_exposure_type.size();i++) {
//					if(dpc0.exposure_type.equals(list_exposure_type.get(i))) {
//						line+=1;
//					} else {
//						line+=0;
//					}
//					
//					line+="\t";
//				}
				
				for (int i=0;i<list_species_common.size();i++) {
					if(dpc0.species_common.equals(list_species_common.get(i))) {
						line+=1;
					} else {
						line+=0;
					}
					
					line+="\t";
				}
				
				line+=dpc0.values_tsv+"\r\n";
				
				if(dpc0.split_num==0) {
					fwTrain.write(line);
				} else {
					fwTest.write(line);
					
					if(dpc0.species_common.equals("Fathead Minnow")) {
						fwTest2.write(line);
					}
					
				}
				
			}

			for (String smiles:hs_smiles) {
				fwCompareSpecies.write(smiles+"\t"+htFHM.get(smiles)+"\t"+htRT.get(smiles)+"\t"+htBG.get(smiles)+"\r\n");
			}

			
			System.out.println(dpcs.size());
			System.out.println(dpcsFlat.size());
			

			fwTrain.flush();
			fwTest.flush();
			fwTest2.flush();
			fwCompareSpecies.flush();

			fwTrain.close();
			fwTest.close();
			fwTest2.close();
			fwCompareSpecies.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
	}
	

	void compareToFHM_() {
		
//		CompareExperimentalRecords cer=new CompareExperimentalRecords();
		
		String speciesCommon="FHM_BG_RT";
		String datasetName="ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		String descriptorSetName="Mordred-default";

		String sqlDescriptorSetId="select id,headers_tsv from qsar_descriptors.descriptor_sets where name='"+descriptorSetName+"';";
		ResultSet rsDescriptorSet=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDescriptorSetId);
		Integer descriptorSetId=null;
		String headerTsv=null;
		try {
			rsDescriptorSet.next();
			descriptorSetId = rsDescriptorSet.getInt(1);
			headerTsv=rsDescriptorSet.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		String sql="select dp.canon_qsar_smiles,-log10(dpc.property_value) as qsar_prop_value,dpis.split_num, pv2.value_text as species_common,dv.values_tsv from qsar_datasets.data_point_contributors dpc\r\n"
				+ "		join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id\r\n"
				+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
				+ "		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
				+ "		join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id\r\n"
				+ "		join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11\r\n"
				+ "		join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13\r\n"
				+ "		join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles and dv.fk_descriptor_set_id="+descriptorSetId+"\r\n"
				+ "		where d.name='"+datasetName+"' and dpis.fk_splitting_id=1;";

//		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		List<DPC>dpcs=new ArrayList<>();
		
		List<String>list_species_common=new ArrayList<>();	
		
		try {
			while (rs.next()) {
				DPC dpc=new DPC();
				dpc.canon_qsar_smiles=rs.getString(1);
				dpc.qsar_prop_value=rs.getDouble(2);
				dpc.split_num=rs.getInt(3);
				dpc.species_common=rs.getString(4);
				dpc.values_tsv=rs.getString(5);
				
				if(!list_species_common.contains(dpc.species_common)) 
					list_species_common.add(dpc.species_common);
				
				
				dpcs.add(dpc);
			}
			
			Collections.sort(list_species_common);
			
//			for (String species_common:hs_species_common) System.out.println(species_common);
//			System.out.println("");
//			for (String exposure_type:hs_exposure_type) System.out.println(exposure_type);
			
			//determine median by factor
			TreeMap<String,TreeMap<String,List<DPC>>>htBySpecies=new TreeMap<>();
			
			for (DPC dpc:dpcs) {
				
				String key=dpc.canon_qsar_smiles+"\t"+dpc.species_common;
				if(htBySpecies.containsKey(dpc.species_common)) {
					TreeMap<String,List<DPC>>htBySmiles=htBySpecies.get(dpc.species_common);
					
					if(htBySmiles.containsKey(dpc.canon_qsar_smiles)) {
						List<DPC>dpcs2=htBySmiles.get(dpc.canon_qsar_smiles);
						dpcs2.add(dpc);
					} else {
						List<DPC>dpcs2=new ArrayList<>();
						htBySmiles.put(dpc.canon_qsar_smiles, dpcs2);
						dpcs2.add(dpc);
					}
				} else {
					
					TreeMap<String,List<DPC>>htBySmiles=new TreeMap<>();
					htBySpecies.put(dpc.species_common, htBySmiles);
					
					if(htBySmiles.containsKey(dpc.canon_qsar_smiles)) {
						List<DPC>dpcs2=htBySmiles.get(dpc.canon_qsar_smiles);
						dpcs2.add(dpc);
					} else {
						List<DPC>dpcs2=new ArrayList<>();
						htBySmiles.put(dpc.canon_qsar_smiles, dpcs2);
						dpcs2.add(dpc);
					}
				}
				
			}
			
//			System.out.println(Utilities.gson.toJson(htBySpecies));

			TreeMap<String,List<DPC>>htBySmilesFHM=htBySpecies.get("Fathead Minnow");
			
			for (String species_common:list_species_common) {
				
				if(species_common.equals("Fathead Minnow")) continue;
				
				TreeMap<String,List<DPC>>htBySmiles=htBySpecies.get(species_common);
				
				int count=0;
				double MAE=0;
				
				
				List<Double>valuesFHM=new ArrayList<>();
				List<Double>values=new ArrayList<>();
				
				for (String smiles:htBySmilesFHM.keySet()) {
					if(!htBySmiles.containsKey(smiles)) continue;
					Double medianFHM=getMedianValue(htBySmilesFHM.get(smiles));
					Double median=getMedianValue(htBySmiles.get(smiles));
					if(medianFHM==null || median==null) continue;
					MAE+=Math.abs(medianFHM-median);
					count++;
					
					valuesFHM.add(medianFHM);
					values.add(median);
				}
				MAE/=count;
				
				if(count>30) {				
					System.out.println(species_common+"\t"+MAE+"\t"+count+"\t"+htBySmiles.size());
//					cer.cm.createPlot("-logM", valuesFHM, values,"Fathead Minnow",species_common);
				}
			}
			
			List<DPC>dpcsFlat=new ArrayList<>();
			String folder="data\\modeling\\"+datasetName+"\\compare to fhm\\";
			File Folder=new File(folder);
			Folder.mkdirs();

			
			String header="canon_qsar_smiles\tqsar_property_value\t";
//			for (String exposure_type:list_exposure_type) header+="exposure_type_"+exposure_type+"\t";
			for (String species_common:list_species_common) header+="species_common_"+species_common+"\t";
			header+=headerTsv+"\r\n";
			
			FileWriter fwCompareSpecies=new FileWriter(folder+"/compare_species_to_fhm.tsv");

			fwCompareSpecies.write("smiles\tFHM\tRT\tBG\r\n");
			fwCompareSpecies.flush();
			fwCompareSpecies.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
	}
	
	/**
	 * Omit factors
	 */

	void createFishToxDataSetWithCommonSpeciesAndExposureType_no_factors() {
		
		String speciesCommon="all_fish";
		String datasetName="ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		String descriptorSetName="Mordred-default";

		String sqlDescriptorSetId="select id,headers_tsv from qsar_descriptors.descriptor_sets where name='"+descriptorSetName+"';";
		ResultSet rsDescriptorSet=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDescriptorSetId);
		Integer descriptorSetId=null;
		String headerTsv=null;
		try {
			rsDescriptorSet.next();
			descriptorSetId = rsDescriptorSet.getInt(1);
			headerTsv=rsDescriptorSet.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		String sql="select dp.canon_qsar_smiles,-log10(dpc.property_value) as qsar_prop_value,dpis.split_num, pv2.value_text as species_common,pv3.value_text as exposure_type, dv.values_tsv from qsar_datasets.data_point_contributors dpc\r\n"
				+ "		join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id\r\n"
				+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
				+ "		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
				+ "		join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id\r\n"
				+ "		join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11\r\n"
				+ "		join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13\r\n"
				+ "		join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles and dv.fk_descriptor_set_id="+descriptorSetId+"\r\n"
				+ "		where d.name='"+datasetName+"' and dpis.fk_splitting_id=1;";

//		System.out.println(sql);
		
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		List<DPC>dpcs=new ArrayList<>();
		
		List<String>list_species_common=new ArrayList<>();	
		List<String>list_exposure_type=new ArrayList<>();
		
		try {
			while (rs.next()) {
				DPC dpc=new DPC();
				dpc.canon_qsar_smiles=rs.getString(1);
				dpc.qsar_prop_value=rs.getDouble(2);
				dpc.split_num=rs.getInt(3);
				dpc.species_common=rs.getString(4);
				dpc.exposure_type=rs.getString(5);
				dpc.values_tsv=rs.getString(6);
				
				if(!list_species_common.contains(dpc.species_common)) 
					list_species_common.add(dpc.species_common);
				
				if(!list_exposure_type.contains(dpc.exposure_type))
					list_exposure_type.add(dpc.exposure_type);
				
				dpcs.add(dpc);
			}
			
			Collections.sort(list_exposure_type);
			Collections.sort(list_species_common);
			
//			for (String species_common:hs_species_common) System.out.println(species_common);
//			System.out.println("");
//			for (String exposure_type:hs_exposure_type) System.out.println(exposure_type);
			
			//determine median by factor
			TreeMap<String,List<DPC>>htByFactor=new TreeMap<>();
			
			for (DPC dpc:dpcs) {
				
				String key=dpc.canon_qsar_smiles+"\t"+dpc.species_common+"\t"+dpc.exposure_type;
				if(htByFactor.containsKey(key)) {
					List<DPC>dpcs2=htByFactor.get(key);
					dpcs2.add(dpc);
				} else {

					List<DPC>dpcs2=new ArrayList<>();
					dpcs2.add(dpc);
					htByFactor.put(key, dpcs2);
				}
				
			}
			
			List<DPC>dpcsFlat=new ArrayList<>();
			
			String folder2="species_common_exposure_type_all_fish_no_factors";
			
			String folder="data\\modeling\\"+datasetName+"\\"+folder2+"\\"+descriptorSetName+"\\";
			File Folder=new File(folder);
			Folder.mkdirs();
			
			FileWriter fwTrain=new FileWriter(folder+"/train.tsv");
			FileWriter fwTest=new FileWriter(folder+"/test.tsv");
			
			
			String header="canon_qsar_smiles\tqsar_property_value\t";
//			for (String exposure_type:list_exposure_type) header+="exposure_type_"+exposure_type+"\t";
//			for (String species_common:list_species_common) header+="species_common_"+species_common+"\t";
			header+=headerTsv+"\r\n";
			
			fwTrain.write(header);
			fwTest.write(header);
			
			for (String key:htByFactor.keySet()) {
				List<DPC>dpcs2=htByFactor.get(key);
				double median=getMedianValue(dpcs2);
				DPC dpc0=dpcs2.get(0);
				
				dpcsFlat.add(dpc0);
//				System.out.println(key+"\t"+htByFactor.get(key).size()+"\t"+median);
				
				String line=dpc0.canon_qsar_smiles+"\t"+median+"\t";
				
//				for (int i=0;i<list_exposure_type.size();i++) {
//					if(dpc0.exposure_type.equals(list_exposure_type.get(i))) {
//						line+=1;
//					} else {
//						line+=0;
//					}
//					
//					line+="\t";
//				}
//				
//				for (int i=0;i<list_species_common.size();i++) {
//					if(dpc0.species_common.equals(list_species_common.get(i))) {
//						line+=1;
//					} else {
//						line+=0;
//					}
//					
//					line+="\t";
//				}
				
				line+=dpc0.values_tsv+"\r\n";
				
				if(dpc0.split_num==0) {
					fwTrain.write(line);
				} else {
					fwTest.write(line);
				}
				
			}

			System.out.println(dpcs.size());
			System.out.println(dpcsFlat.size());
			

			fwTrain.flush();
			fwTest.flush();
			fwTrain.close();
			fwTest.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
	}
	
	
	void createFHMWithExposureType() {
		
		String speciesCommon="fhm";
		String datasetName="ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		String descriptorSetName="Mordred-default";

		String sqlDescriptorSetId="select id,headers_tsv from qsar_descriptors.descriptor_sets where name='"+descriptorSetName+"';";
		ResultSet rsDescriptorSet=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDescriptorSetId);
		Integer descriptorSetId=null;
		String headerTsv=null;
		try {
			rsDescriptorSet.next();
			descriptorSetId = rsDescriptorSet.getInt(1);
			headerTsv=rsDescriptorSet.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		String sql="select dp.canon_qsar_smiles,-log10(dpc.property_value) as qsar_prop_value,dpis.split_num, pv3.value_text as exposure_type, dv.values_tsv from qsar_datasets.data_point_contributors dpc\r\n"
				+ "		join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id\r\n"
				+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
				+ "		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
				+ "		join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id\r\n"
				+ "		join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11\r\n"
				+ "		join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13\r\n"
				+ "		join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles and dv.fk_descriptor_set_id="+descriptorSetId+"\r\n"
				+ "		where d.name='"+datasetName+"' and dpis.fk_splitting_id=1 and  pv2.value_text='Fathead Minnow';";

		System.out.println(sql);
		
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		List<DPC>dpcs=new ArrayList<>();
		
		List<String>list_exposure_type=new ArrayList<>();
		
		try {
			while (rs.next()) {
				DPC dpc=new DPC();
				dpc.canon_qsar_smiles=rs.getString(1);
				dpc.qsar_prop_value=rs.getDouble(2);
				dpc.split_num=rs.getInt(3);
				dpc.exposure_type=rs.getString(4);
				dpc.values_tsv=rs.getString(5);
				
				
				if(!list_exposure_type.contains(dpc.exposure_type))
					list_exposure_type.add(dpc.exposure_type);
				
				dpcs.add(dpc);
			}
			
			Collections.sort(list_exposure_type);
			
//			for (String species_common:hs_species_common) System.out.println(species_common);
//			System.out.println("");
//			for (String exposure_type:hs_exposure_type) System.out.println(exposure_type);
			
			//determine median by factor
			TreeMap<String,List<DPC>>htByFactor=new TreeMap<>();
			
			Hashtable<String,Double>htFT=new Hashtable<>();
			Hashtable<String,Double>htS=new Hashtable<>();
			Hashtable<String,Double>htR=new Hashtable<>();
			
			
			for (DPC dpc:dpcs) {
				
				String key=dpc.canon_qsar_smiles+"\t"+dpc.exposure_type;
				if(htByFactor.containsKey(key)) {
					List<DPC>dpcs2=htByFactor.get(key);
					dpcs2.add(dpc);
				} else {

					List<DPC>dpcs2=new ArrayList<>();
					dpcs2.add(dpc);
					htByFactor.put(key, dpcs2);
				}
				
			}
			
			List<DPC>dpcsFlat=new ArrayList<>();
			
			String folder2="exposure_type_"+speciesCommon;
			
			String folder="data\\modeling\\"+datasetName+"\\"+folder2+"\\"+descriptorSetName+"\\";
			File Folder=new File(folder);
			Folder.mkdirs();
			
			FileWriter fwTrain=new FileWriter(folder+"/train.tsv");
			FileWriter fwTest=new FileWriter(folder+"/test.tsv");
			FileWriter fwCompareExposureType=new FileWriter(folder+"/fhm_compare_exposure_type.tsv");
			
			String header="canon_qsar_smiles\tqsar_property_value\t";
			for (String exposure_type:list_exposure_type) header+="exposure_type_"+exposure_type+"\t";
//			for (String species_common:list_species_common) header+="species_common_"+species_common+"\t";
			header+=headerTsv+"\r\n";
			
			fwTrain.write(header);
			fwTest.write(header);
			fwCompareExposureType.write("smiles\tFlow-through\tStatic\tRenewal\r\n");

			HashSet<String>hs_smiles=new HashSet<>();
			
			for (String key:htByFactor.keySet()) {
				List<DPC>dpcs2=htByFactor.get(key);
				double median=getMedianValue(dpcs2);
				DPC dpc0=dpcs2.get(0);
				
				hs_smiles.add(dpc0.canon_qsar_smiles);
				
				if(dpc0.exposure_type.equals("Static")) {
					htS.put(dpc0.canon_qsar_smiles,median);
				} else if(dpc0.exposure_type.equals("Renewal")) {
					htR.put(dpc0.canon_qsar_smiles,median);
				} else if(dpc0.exposure_type.equals("Flow-through")) {
					htFT.put(dpc0.canon_qsar_smiles,median);
				}
				
				dpcsFlat.add(dpc0);
//				System.out.println(key+"\t"+htByFactor.get(key).size()+"\t"+median);
				
				String line=dpc0.canon_qsar_smiles+"\t"+median+"\t";
				
				for (int i=0;i<list_exposure_type.size();i++) {
					if(dpc0.exposure_type.equals(list_exposure_type.get(i))) {
						line+=1;
					} else {
						line+=0;
					}
					
					line+="\t";
				}
//				
//				for (int i=0;i<list_species_common.size();i++) {
//					if(dpc0.species_common.equals(list_species_common.get(i))) {
//						line+=1;
//					} else {
//						line+=0;
//					}
//					
//					line+="\t";
//				}
				
				line+=dpc0.values_tsv+"\r\n";
				
				if(dpc0.split_num==0) {
					fwTrain.write(line);
				} else {
					fwTest.write(line);
				}
				
			}
			
			for (String smiles:hs_smiles) {
				fwCompareExposureType.write(smiles+"\t"+htFT.get(smiles)+"\t"+htS.get(smiles)+"\t"+htR.get(smiles)+"\r\n");
			}
			

			System.out.println(dpcs.size());
			System.out.println(dpcsFlat.size());
			
			fwTrain.flush();
			fwTest.flush();
			fwCompareExposureType.flush();
			
			fwTrain.close();
			fwTest.close();
			fwCompareExposureType.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Still aggregates by exposure_type but doesnt include exposure_type columns
	 * 
	 */
	void createFHMWithExposureTypeNoFactor() {
		
		String speciesCommon="fhm";
		String datasetName="ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		String descriptorSetName="Mordred-default";

		String sqlDescriptorSetId="select id,headers_tsv from qsar_descriptors.descriptor_sets where name='"+descriptorSetName+"';";
		ResultSet rsDescriptorSet=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sqlDescriptorSetId);
		Integer descriptorSetId=null;
		String headerTsv=null;
		try {
			rsDescriptorSet.next();
			descriptorSetId = rsDescriptorSet.getInt(1);
			headerTsv=rsDescriptorSet.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		String sql="select dp.canon_qsar_smiles,-log10(dpc.property_value) as qsar_prop_value,dpis.split_num, pv3.value_text as exposure_type, dv.values_tsv from qsar_datasets.data_point_contributors dpc\r\n"
				+ "		join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id\r\n"
				+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
				+ "		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
				+ "		join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id\r\n"
				+ "		join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11\r\n"
				+ "		join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13\r\n"
				+ "		join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles and dv.fk_descriptor_set_id="+descriptorSetId+"\r\n"
				+ "		where d.name='"+datasetName+"' and dpis.fk_splitting_id=1 and  pv2.value_text='Fathead Minnow';";

		System.out.println(sql);
		
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		List<DPC>dpcs=new ArrayList<>();
		
		List<String>list_exposure_type=new ArrayList<>();
		
		try {
			while (rs.next()) {
				DPC dpc=new DPC();
				dpc.canon_qsar_smiles=rs.getString(1);
				dpc.qsar_prop_value=rs.getDouble(2);
				dpc.split_num=rs.getInt(3);
				dpc.exposure_type=rs.getString(4);
				dpc.values_tsv=rs.getString(5);
				
				
				if(!list_exposure_type.contains(dpc.exposure_type))
					list_exposure_type.add(dpc.exposure_type);
				
				dpcs.add(dpc);
			}
			
			Collections.sort(list_exposure_type);
			
//			for (String species_common:hs_species_common) System.out.println(species_common);
//			System.out.println("");
//			for (String exposure_type:hs_exposure_type) System.out.println(exposure_type);
			
			//determine median by factor
			TreeMap<String,List<DPC>>htByFactor=new TreeMap<>();
			
			for (DPC dpc:dpcs) {
				
				String key=dpc.canon_qsar_smiles+"\t"+dpc.exposure_type;
				if(htByFactor.containsKey(key)) {
					List<DPC>dpcs2=htByFactor.get(key);
					dpcs2.add(dpc);
				} else {

					List<DPC>dpcs2=new ArrayList<>();
					dpcs2.add(dpc);
					htByFactor.put(key, dpcs2);
				}
				
			}
			
			List<DPC>dpcsFlat=new ArrayList<>();
			
			String folder2="exposure_type_"+speciesCommon+"_no_factors";
			
			String folder="data\\modeling\\"+datasetName+"\\"+folder2+"\\"+descriptorSetName+"\\";
			File Folder=new File(folder);
			Folder.mkdirs();
			
			FileWriter fwTrain=new FileWriter(folder+"/train.tsv");
			FileWriter fwTest=new FileWriter(folder+"/test.tsv");
			
			
			String header="canon_qsar_smiles\tqsar_property_value\t";
//			for (String exposure_type:list_exposure_type) header+="exposure_type_"+exposure_type+"\t";
//			for (String species_common:list_species_common) header+="species_common_"+species_common+"\t";
			header+=headerTsv+"\r\n";
			
			fwTrain.write(header);
			fwTest.write(header);
			
			for (String key:htByFactor.keySet()) {
				List<DPC>dpcs2=htByFactor.get(key);
				double median=getMedianValue(dpcs2);
				DPC dpc0=dpcs2.get(0);
				
				dpcsFlat.add(dpc0);
//				System.out.println(key+"\t"+htByFactor.get(key).size()+"\t"+median);
				
				String line=dpc0.canon_qsar_smiles+"\t"+median+"\t";
				
//				for (int i=0;i<list_exposure_type.size();i++) {
//					if(dpc0.exposure_type.equals(list_exposure_type.get(i))) {
//						line+=1;
//					} else {
//						line+=0;
//					}
//					
//					line+="\t";
//				}
//				
//				for (int i=0;i<list_species_common.size();i++) {
//					if(dpc0.species_common.equals(list_species_common.get(i))) {
//						line+=1;
//					} else {
//						line+=0;
//					}
//					
//					line+="\t";
//				}
				
				line+=dpc0.values_tsv+"\r\n";
				
				if(dpc0.split_num==0) {
					fwTrain.write(line);
				} else {
					fwTest.write(line);
				}
				
			}

			System.out.println(dpcs.size());
			System.out.println(dpcsFlat.size());
			
			fwTrain.flush();
			fwTest.flush();
			fwTrain.close();
			fwTest.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	private Double getMedianValue(List<DPC>dpcs) {
		
		List<Double>vals=new ArrayList<>();
	
		for (DPC dpc:dpcs) {
			vals.add(dpc.qsar_prop_value);
		}

		Collections.sort(vals);
		
//		System.out.println(vals);
		
		if(vals.size()%2==0) {// even
			
			int middleVal2=vals.size()/2;
			int middleVal1=middleVal2-1;
			if(Math.abs(middleVal1-middleVal2)>1) return null;
			return (vals.get(middleVal1)+vals.get(middleVal2))/2.0;
		} else {//odd
			int middleVal=vals.size()/2;
			return vals.get(middleVal);
		}

	}

	public static void main(String[] args) {
		FishTox f=new FishTox();

		//		f.createFishToxDataSetWithCommonSpeciesAndExposureType();
//		f.createFishToxDataSetWithCommonSpeciesAndExposureType_no_factors();
//		f.createFHMWithExposureType();
//		f.createFHMWithExposureTypeNoFactor();
//		f.createFishToxDataSetWithCommonSpecies();
//		f.createFHM_BG_RT_ToxDataSetWithCommonSpecies();
		f.compareToFHM_();


	}

}
