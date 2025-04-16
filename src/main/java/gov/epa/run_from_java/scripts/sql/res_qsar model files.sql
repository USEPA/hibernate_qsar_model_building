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
