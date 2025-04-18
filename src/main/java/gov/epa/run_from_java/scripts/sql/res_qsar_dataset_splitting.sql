select dp.canon_qsar_smiles,dpis.split_num from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id and dpis.fk_splitting_id=1
where d.name='ECOTOX_2024_12_12_96HR_Fish_LC50_v1 modeling';




select count(dp.canon_qsar_smiles),dpis.split_num from  qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points dp2 on dp.canon_qsar_smiles=dp2.canon_qsar_smiles and dp2.fk_dataset_id=505
join qsar_datasets.data_points_in_splittings dpis on dp2.id = dpis.fk_data_point_id
where d.name='ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling' and dpis.fk_splitting_id=1
group by dpis.split_num;


-- how to clone the splitting:
select dp.id, split_num,dpis.fk_splitting_id from  qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points dp2 on dp.canon_qsar_smiles=dp2.canon_qsar_smiles and dp2.fk_dataset_id=505
join qsar_datasets.data_points_in_splittings dpis on dp2.id = dpis.fk_data_point_id
where d.name='ECOTOX_2024_12_12_96HR_FHM_LC50_v1 modeling';



select dp.id, split_num,dpis.fk_splitting_id from  qsar_datasets.data_points dp
	    join qsar_datasets.data_points dp2 on dp.canon_qsar_smiles=dp2.canon_qsar_smiles and dp2.fk_dataset_id=505
	    join qsar_datasets.data_points_in_splittings dpis on dp2.id = dpis.fk_data_point_id
	    where dp.fk_dataset_id=506;


-- print model test set stats
select m.id,m.name, m.dataset_name,m.descriptor_set_name,ms.statistic_value  as R2_TEST,ms2.statistic_value as MAE_TEST,de.embedding_tsv from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id and ms.fk_statistic_id=16
join qsar_models.model_statistics ms2 on m.id = ms2.fk_model_id and ms2.fk_statistic_id=8
left join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id = de.id
-- where dataset_name like 'ECOT%'
where m.dataset_name='ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling'
order by m.dataset_name;



select d.name,count(dp.id) from qsar_datasets.datasets d
         join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
where d.name like 'ECOT%'
group by d.name
order by d.name;


select m.id,m.dataset_name,m.fk_method_id,ms.statistic_value,s.name from qsar_models.models m
left join qsar_models.model_bytes mb on m.id = mb.fk_model_id
left join qsar_models.model_statistics ms on m.id = ms.fk_model_id and ms.fk_statistic_id=16
join qsar_models.sources s on m.fk_source_id = s.id
where mb.id is null and s.name ='Cheminformatics Modules';



-- create training/test sets for complicated model:
select dp.canon_qsar_smiles,-log10(dpc.property_value) as prop_value,dpis.split_num, pv2.value_text as species_common,pv3.value_text as exposure_type, dv.values_tsv from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
-- join qsar_datasets.data_points dpSplit on dp.canon_qsar_smiles=dpSplit.canon_qsar_smiles and dpSplit.fk_dataset_id=505
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id and dpis.fk_splitting_id=1
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11
join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13
join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles and dv.fk_descriptor_set_id=4
where d.name='ECOTOX_2024_12_12_96HR_Fish_LC50_v1 modeling';


select dp.id, dpc.id,dp.canon_qsar_smiles,dpc.property_value,pv3.value_text as exposure_type from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11
join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13
-- join qsar_datasets.units u on d.fk_unit_id_contributor = u.id
where d.name='ECOTOX_2024_12_12_96HR_FHM_LC50_v1 modeling'
order by canon_qsar_smiles;



