SELECT * FROM qsar_models.dsstox_records dr 
where dr.fk_dsstox_snapshot_id =3;





select dp.canon_qsar_smiles , dp.qsar_dtxcid  from qsar_datasets.datasets d 
join qsar_datasets.data_points dp on dp.fk_dataset_id =d.id
join qsar_models.models m on m.dataset_name =d.name
where m.id=1065;



SELECT dp.canon_qsar_smiles as "canonicalSmiles", dr.dtxsid as sid, dr.dtxcid as cid, dr.casrn, dr.preferred_name as "name" , dr.smiles, dr.indigo_inchi_key as inchiKey
FROM qsar_datasets.datasets d
JOIN qsar_datasets.data_points dp ON dp.fk_dataset_id = d.id
JOIN qsar_models.models m ON m.dataset_name = d.name
LEFT JOIN qsar_models.dsstox_records dr ON dr.dtxcid = SUBSTRING(dp.qsar_dtxcid FROM 1 FOR POSITION('|' IN dp.qsar_dtxcid || '|') - 1)
WHERE m.id = 1065 and dr.fk_dsstox_snapshot_id =3;