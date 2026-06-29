select avg(dp.qsar_property_value), count(dpis.id), dpis.fk_splitting_id   from qsar_datasets.data_points_in_splittings dpis 
join qsar_datasets.data_points dp on dpis.fk_data_point_id = dp.id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where d.name = 'exp_prop_RBIODEG_RIFM_BY_DTXSID' 
group by dpis.fk_splitting_id ;

