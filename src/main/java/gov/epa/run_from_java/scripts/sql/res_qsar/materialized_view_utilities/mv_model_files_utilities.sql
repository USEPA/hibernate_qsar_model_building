select m.id, ft.id,m.name, s.name,ft.name from qsar_models.model_files mf
join qsar_models.file_types ft on mf.fk_file_type_id = ft.id
join qsar_models.models m on mf.fk_model_id = m.id
join qsar_models.sources s on m.fk_source_id = s.id
-- where m.name like '%OPERA%'
where m.name like '%TEST%'  --and m.name='Developmental toxicity TEST5.1.3'
order by s.name,m.name,ft.name;
;


-- delete from qsar_models.model_files

delete from qsar_models.model_files using qsar_models.models where model_files.fk_model_id=models.id and models.name like '%TEST%';


delete from qsar_models.model_files where model_files.fk_model_id=235 and model_files.fk_file_type_id=4;

select * from qsar_models.model_files using qsar_models.models where model_files.fk_model_id=models.id and models.name like '%OPERA%'



select m.id, m.name as model_name, mf.fk_file_type_id from qsar_models.model_files mf
join qsar_models.models m on mf.fk_model_id = m.id
where m.dataset_name like '%v1 modeling' and (mf.fk_file_type_id=4)
order by m.name;
;


DELETE FROM qsar_models.model_files AS mf
USING qsar_models.models AS m
WHERE mf.fk_model_id = m.id
  AND m.dataset_name LIKE '%v1 modeling'
  AND mf.fk_file_type_id = 4;



UPDATE qsar_datasets.properties
SET
  description = 'Log10 of the ratio of the concentration of a chemical in n-octanol and water at equilibrium at a specified temperature',
  updated_at  = NOW(),              -- or CURRENT_TIMESTAMP
  updated_by  = 'tmarti02'
WHERE name_ccd = 'LogKow: Octanol-Water'
RETURNING name_ccd, description, updated_at, updated_by;




