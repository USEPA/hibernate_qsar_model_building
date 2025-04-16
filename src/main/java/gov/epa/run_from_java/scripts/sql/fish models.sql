delete from qsar_models.models m
where dataset_name like 'exp_prop_%LC50%modeling';

delete from qsar_models.models  where id=1441;

select m.id, m.dataset_name,m.descriptor_set_name,m2.name,m.fk_descriptor_embedding_id from qsar_models.models m
join qsar_models.methods m2 on m.fk_method_id = m2.id
left join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id = de.id
where m.dataset_name like 'exp_prop_%LC50%v5 modeling' and embedding_tsv is not null
order by m.dataset_name,descriptor_set_name,embedding_tsv;


select m.id, m.dataset_name,m.descriptor_set_name,m2.name,m.fk_descriptor_embedding_id,ms.statistic_value from qsar_models.models m
join qsar_models.methods m2 on m.fk_method_id = m2.id
left join qsar_models.model_statistics ms on m.id = ms.fk_model_id
left join qsar_models.statistics s on ms.fk_statistic_id = s.id
where m.dataset_name like 'exp_prop_%LC50%v5 modeling'  and s.name='RMSE_Test' and descriptor_set_name='Mordred-default' and m2.name!='svm_regressor_1.4'
order by m.dataset_name,descriptor_set_name,m2.name, fk_descriptor_embedding_id;
