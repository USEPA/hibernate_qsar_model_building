select dataset_name, s.name, ms.statistic_value from qsar_models.models
join qsar_models.model_statistics ms on models.id = ms.fk_model_id
         join qsar_models.statistics s on ms.fk_statistic_id = s.id
where dataset_name like '%v1 modeling%' and splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is null
and dataset_name not like '%exp_prop%' and s.name='PearsonRSQ_Test'
order by dataset_name;


select m.dataset_name, s.name, ms.statistic_value from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id
         join qsar_models.statistics s on ms.fk_statistic_id = s.id
where dataset_name like '%v4 modeling%' and splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is null
and s.name='RMSE_Test'
order by dataset_name;



select m.name,d.name,p.abbreviation,p.name_ccd from qsar_models.models m
join qsar_datasets.datasets d on m.dataset_name=d.name
join qsar_datasets.properties p on d.fk_property_id = p.id
where fk_source_id=1;


-- ALTER TABLE qsar_models.models DROP CONSTRAINT models_pk



select m.id as model_id, m.dataset_name, m.descriptor_set_name,  s.name, round(ms.statistic_value::numeric,2) as stat_value from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id
         join qsar_models.statistics s on ms.fk_statistic_id = s.id
where dataset_name like '%BCF%' and splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is null
--  and (s.name='PearsonRSQ_Test' or s.name='RMSE_Test')
and (s.name like '%CV%')
order by s.name,dataset_name;