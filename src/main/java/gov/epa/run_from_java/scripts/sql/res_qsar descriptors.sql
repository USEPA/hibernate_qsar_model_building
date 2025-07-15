-- select dp.canon_qsar_smiles,dv.values_tsv from qsar_datasets.datasets d
    select count (dp.id) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles
join qsar_descriptors.descriptor_sets ds on dv.fk_descriptor_set_id = ds.id
where d.name ='LogP v1 modeling' and ds.descriptor_service='padel'