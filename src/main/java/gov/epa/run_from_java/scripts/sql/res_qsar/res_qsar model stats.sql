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



select m.name, s.name,round(ms.statistic_value::numeric,3) from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id
join qsar_models.statistics s on ms.fk_statistic_id = s.id
where m.name like '%Martin%' and (s.name like '%AD%' or s.name = 'Coverage_Test')
order by m.name, s.name;




select m.id,convert_from(m.details, 'utf-8') from qsar_models.models m
where m.id=1065;




select m.id, m.dataset_name, s.name, round(ms.statistic_value::numeric,3) from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id
         join qsar_models.statistics s on ms.fk_statistic_id = s.id
join qsar_models.sources s2 on m.fk_source_id = s2.id
-- where s2.name='TEST5.1.3'
where m.id>1613
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


select m.id as model_id, m2.name,m.descriptor_set_name,m.fk_descriptor_embedding_id,  s.name, round(ms.statistic_value::numeric,2) as stat_value from qsar_models.models m
left join qsar_models.model_statistics ms on m.id = ms.fk_model_id
         left join qsar_models.statistics s on ms.fk_statistic_id = s.id
        left join qsar_models.methods m2 on m.fk_method_id = m2.id
where dataset_name='exp_prop_RBIODEG_RIFM_BY_CAS' and splitting_name='RND_REPRESENTATIVE'
--  and (s.name='PearsonRSQ_Test' or s.name='RMSE_Test')
-- and (s.name ='SN_Test' or s.name='SP_Test' or s.name='BA_Test')
and (s.name='BA_Test')
order by m.name, s.name;


select s.name, round(ms.statistic_value::numeric,2) as stat_value from qsar_models.models m
left join qsar_models.model_statistics ms on m.id = ms.fk_model_id
         left join qsar_models.statistics s on ms.fk_statistic_id = s.id
        left join qsar_models.methods m2 on m.fk_method_id = m2.id
where m.id=1569
order by m.name, s.name;



select m.name, s.name, ROUND(cast(ms.statistic_value as numeric),4) from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id
         join qsar_models.statistics s on ms.fk_statistic_id = s.id
        join qsar_models.sources s2 on m.fk_source_id = s2.id
where m.id=1615
order by m.name,s.name;



select m.name, s.name, ROUND(cast(ms.statistic_value as numeric),2) from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id
         join qsar_models.statistics s on ms.fk_statistic_id = s.id
        join qsar_models.sources s2 on m.fk_source_id = s2.id
where s2.name='TEST5.1.3' and s.name not like '%Q2%' and s.name not like '%R2%'
order by m.name,s.name;



select m.name_ccd, m2.name, s.name,m.external_dataset_name,   ROUND(cast(ms.statistic_value as numeric),3) as stat, m.created_at, de.embedding_tsv   from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id
         join qsar_models.statistics s on ms.fk_statistic_id = s.id
         join qsar_models.methods m2 on m.fk_method_id = m2.id
         join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id =de.id
where m.dataset_name='KOC v1 modeling' and (s.name = 'RMSE_Test')
order by name_ccd,s.name;



select m.name, s.name, ROUND(cast(ms.statistic_value as numeric),2) from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id
         join qsar_models.statistics s on ms.fk_statistic_id = s.id
        join qsar_models.sources s2 on m.fk_source_id = s2.id
where s2.name='OPERA2.8'
order by m.name,s.name;


select * from qsar_models.model_statistics ms
join qsar_models.models m on ms.fk_model_id = m.id
where m.fk_source_id=2;

-- DELETE FROM qsar_models.model_statistics
-- USING qsar_models.models
-- WHERE model_statistics.fk_model_id = models.id
--   AND models.fk_source_id = 2;

select * from qsar_models.models m


--look for models with no predictions
select * from qsar_models.models m
left join qsar_models.model_bytes mb on m.id = mb.fk_model_id
where mb.bytes is null and  fk_source_id=3;


delete from qsar_models.models where id=1376;



select m.id,m.dataset_name,m2.name,m.descriptor_set_name,fk_descriptor_embedding_id is not null as have_embedding,
       ROUND(cast(ms1.statistic_value as numeric),3) as MAE_TEST,
        ROUND(cast(ms2.statistic_value as numeric),3) as MAE_CV_TRAINING
from qsar_models.models m
join qsar_models.methods m2 on m.fk_method_id = m2.id
join qsar_models.model_statistics ms1 on m.id = ms1.fk_model_id and ms1.fk_statistic_id=8
join qsar_models.model_statistics ms2 on m.id = ms2.fk_model_id and ms2.fk_statistic_id=33
where dataset_name like 'exp_prop_96HR%'
order by dataset_name,m2.name,m.descriptor_set_name,fk_descriptor_embedding_id is not null;


select  m.id,m.dataset_name,m2.name,m.descriptor_set_name,fk_descriptor_embedding_id is not null as feature_selection,
       ROUND(cast(ms1.statistic_value as numeric),5) as MAE_TEST,
        ROUND(cast(ms2.statistic_value as numeric),5) as MAE_CV_TRAINING,
    ROUND(cast(ms3.statistic_value as numeric),5) as PEARSON_R2_TEST,
    de.embedding_tsv
from qsar_models.models m
join qsar_models.methods m2 on m.fk_method_id = m2.id
join qsar_models.model_statistics ms1 on m.id = ms1.fk_model_id and ms1.fk_statistic_id=8
join qsar_models.model_statistics ms2 on m.id = ms2.fk_model_id and ms2.fk_statistic_id=33
join qsar_models.model_statistics ms3 on m.id = ms3.fk_model_id and ms3.fk_statistic_id=16
left join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id = de.id
-- where dataset_name like 'ECOTOX_2024_12_12_96HR%v3%'
-- where m.dataset_name = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling'
where m.dataset_name='ECOTOX_2024_12_12_96HR_BG_LC50_v3 modeling'
and fk_descriptor_embedding_id is not null and m2.name='xgb_regressor_1.4' and m.descriptor_set_name='WebTEST-default'
-- and m2.name='xgb_regressor_1.4'
order by m.dataset_name,m2.name,m.descriptor_set_name,fk_descriptor_embedding_id is not null;
-- order by m.id desc;


select  m.id,m.created_by,m2.name,m.descriptor_set_name,fk_descriptor_embedding_id is not null as feature_selection,
       ROUND(cast(ms1.statistic_value as numeric),5) as RMSE_TEST,
        ROUND(cast(ms2.statistic_value as numeric),5) as RMSE_CV_TRAINING,
    ROUND(cast(ms3.statistic_value as numeric),5) as PEARSON_R2_TEST,
    de.embedding_tsv
from qsar_models.models m
join qsar_models.methods m2 on m.fk_method_id = m2.id
join qsar_models.model_statistics ms1 on m.id = ms1.fk_model_id and ms1.fk_statistic_id=22
join qsar_models.model_statistics ms2 on m.id = ms2.fk_model_id and ms2.fk_statistic_id=34
join qsar_models.model_statistics ms3 on m.id = ms3.fk_model_id and ms3.fk_statistic_id=16
left join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id = de.id
-- where dataset_name like 'ECOTOX_2024_12_12_96HR%v3%'
-- where m.dataset_name = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling'
-- where dataset_name='ECOTOX_2024_12_12_96HR_RT_LC50_v3 modeling'
-- and fk_descriptor_embedding_id is null and m2.name='xgb_regressor_1.4'
-- and m2.name='xgb_regressor_1.4'
order by m.dataset_name,m2.name,m.descriptor_set_name,fk_descriptor_embedding_id is not null;


select dp.canon_qsar_smiles from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
-- where d.name='ECOTOX_2024_12_12_96HR_FHM_LC50_v3 modeling' and dpis.fk_splitting_id=1 and dpis.split_num=1;
where d.name='ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling' and dpis.fk_splitting_id=1 and dpis.split_num=1;


select d.name ,count(dp.id) from qsar_datasets.datasets d
            join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
where name like 'ECOTOX_2024_12_12_96HR%' and name not like '%BG%' and name not like '%RT%'
group by d.name
order by d.name

delete from qsar_models.models where id=1532;


select * from qsar_models.models
         join qsar_models.methods m2 on models.fk_method_id = m2.id
         where dataset_name like 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling'
           and splitting_name='RND_REPRESENTATIVE'
           and m2.name='xgb_regressor_1.4'
           and descriptor_set_name='WebTEST-default'
           and fk_descriptor_embedding_id is not null;



select dp.canon_qsar_smiles, dp.qsar_property_value as exp_species,  p.qsar_predicted_value as pred_species,p2.qsar_predicted_value as pred_fish from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
join qsar_models.predictions p on p.canon_qsar_smiles=dp.canon_qsar_smiles and p.fk_model_id=1538
join qsar_models.predictions p2 on p2.canon_qsar_smiles=dp.canon_qsar_smiles and p2.fk_model_id=1522
where d.name='ECOTOX_2024_12_12_96HR_BG_LC50_v3 modeling' and dpis.fk_splitting_id=1 and split_num=1

select d.name,count(dp.id) from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where d.name like 'ECOTOX_2024_12_12_96HR_%_LC50_v3 modeling' and dpis.fk_splitting_id=1 and split_num=0
group by d.name;


select s.name, ms.statistic_value from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id
join qsar_models.statistics s on ms.fk_statistic_id = s.id
where m.id=1065  and s.name like '%CV%';

select m.id from qsar_models.models m where fk_source_id=3 and is_public=true order by id



select dp.canon_qsar_smiles, dp.qsar_property_value,p.qsar_predicted_value
-- SELECT AVG(dp.qsar_property_value) AS mean_qsar_property_value,   COUNT(dp.qsar_property_value) AS count_qsar_property_value
from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
join qsar_models.models m on m.dataset_name = d.name
join qsar_models.predictions p on p.canon_qsar_smiles=dp.canon_qsar_smiles and p.fk_model_id=m.id
where m.id = 1065 and dpis.fk_splitting_id=1 and p.fk_splitting_id=1 and split_num=0;


select dp.canon_qsar_smiles, dp.qsar_property_value,p.qsar_predicted_value
from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
join qsar_models.models m on m.dataset_name = d.name
join qsar_models.predictions p on p.canon_qsar_smiles=dp.canon_qsar_smiles and p.fk_model_id=m.id
where m.id = 1065 and
      ((dpis.fk_splitting_id=2 and p.fk_splitting_id=2) or (dpis.fk_splitting_id=3 and p.fk_splitting_id=3)
          or (dpis.fk_splitting_id=4 and p.fk_splitting_id=4) or (dpis.fk_splitting_id=4 and p.fk_splitting_id=4)
          or (dpis.fk_splitting_id=5 and p.fk_splitting_id=5) or (dpis.fk_splitting_id=6 and p.fk_splitting_id=6))



SELECT dp.canon_qsar_smiles, dp.qsar_property_value, p.qsar_predicted_value
FROM qsar_datasets.datasets d
JOIN qsar_datasets.data_points dp ON d.id = dp.fk_dataset_id
JOIN qsar_datasets.data_points_in_splittings dpis ON dp.id = dpis.fk_data_point_id
JOIN qsar_models.models m ON m.dataset_name = d.name
JOIN qsar_models.predictions p ON p.canon_qsar_smiles = dp.canon_qsar_smiles AND p.fk_model_id = m.id
WHERE m.id = 1065
  AND dpis.fk_splitting_id = p.fk_splitting_id
  AND dpis.fk_splitting_id IN (2, 3, 4, 5, 6);



SELECT dp.canon_qsar_smiles, dp.qsar_property_value, p.qsar_predicted_value
FROM qsar_datasets.datasets d
JOIN qsar_datasets.data_points dp ON d.id = dp.fk_dataset_id
JOIN qsar_datasets.data_points_in_splittings dpis ON dp.id = dpis.fk_data_point_id
join qsar_datasets.splittings s on dpis.fk_splitting_id = s.id
JOIN qsar_models.models m ON m.dataset_name = d.name
JOIN qsar_models.predictions p ON p.canon_qsar_smiles = dp.canon_qsar_smiles AND p.fk_model_id = m.id
WHERE m.id = 1065  AND dpis.fk_splitting_id = p.fk_splitting_id  AND s.name like 'RND_REPRESENTATIVE_CV%'




select m.id, ms.*  from qsar_models.models m
join qsar_models.model_statistics ms on ms.fk_model_id =m.id
join qsar_models."statistics" s on s.id=ms.fk_statistic_id 
--where m.id>=1065 and m.id<=1070 and s."name" ='RMSE_CV_Training';
--where m.id>=1065 and m.id<=1070 and s."name" ='MAE_Test_outside_AD';
where m.id>=1065 and m.id<=1070 and s."name" ='Coverage_Test';





select m.id,m.created_at, m.created_by , m2.name, s.name, ms.statistic_value, Length(de.embedding_tsv) - Length(REPLACE(de.embedding_tsv, E'\t', ''))+1 as variables   from qsar_models.models m
join qsar_models.model_statistics ms on ms.fk_model_id=m.id
join qsar_models."statistics" s on s.id=ms.fk_statistic_id 
join qsar_models.methods m2 on m2.id=m.fk_method_id
join qsar_models.descriptor_embeddings de on de.id=m.fk_descriptor_embedding_id 
where m.dataset_name ='KOC v1 modeling' and s.name like 'RMSE_Test%' -- and m.created_at > '2026-04-21'
order by m2.name asc, variables desc;

select
  m.id,
  m.created_at,
--  m.created_by,
  m2.name as method_name,
  length(de.embedding_tsv) - length(replace(de.embedding_tsv, E'\t', '')) + 1 as variables,
  max(case when s.name = 'RMSE_Test'     then ms.statistic_value end) as rmse_test,
  max(case when s.name ='RMSE_CV_Training'     then ms.statistic_value end) as rmse_cv,
  max(case when s.name ='RMSE_External' then ms.statistic_value end) as rmse_external
from qsar_models.models m
join qsar_models.methods m2 on m2.id = m.fk_method_id
join qsar_models.descriptor_embeddings de on de.id = m.fk_descriptor_embedding_id
left join qsar_models.model_statistics ms on ms.fk_model_id = m.id
left join qsar_models."statistics" s on s.id = ms.fk_statistic_id
where m.dataset_name = 'KOC v1 modeling'
group by m.id, m.created_at, m.created_by, m2.name, variables
order by m2.name asc, variables desc;


select de.embedding_tsv  from qsar_models.models m
join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id =de.id
where m.id = 1753


select m.id,m.dataset_name ,  m.created_at, m.created_by , m2.name, s.name, ms.statistic_value, Length(de.embedding_tsv) - Length(REPLACE(de.embedding_tsv, E'\t', ''))+1 as variables   from qsar_models.models m
join qsar_models.model_statistics ms on ms.fk_model_id=m.id
join qsar_models."statistics" s on s.id=ms.fk_statistic_id 
join qsar_models.methods m2 on m2.id=m.fk_method_id
join qsar_models.descriptor_embeddings de on de.id=m.fk_descriptor_embedding_id 
where s.name like '%External%'
order by m.id;



select m.dataset_name, m.id as model_id, -- m.created_at,m.created_by,
replace(m2.name, '_classifier',''), s.name, ms.statistic_value, Length(de.embedding_tsv) - Length(REPLACE(de.embedding_tsv, E'\t', ''))+1 as variables
join qsar_models.model_statistics ms on ms.fk_model_id=m.id
join qsar_models."statistics" s on s.id=ms.fk_statistic_id 
join qsar_models.methods m2 on m2.id=m.fk_method_id
join qsar_models.descriptor_embeddings de on de.id=m.fk_descriptor_embedding_id 
where (m.dataset_name ='exp_prop_RBIODEG_301F v1 modeling' or m.dataset_name='exp_prop_RBIODEG_RIFM_CHEMREG') and s.name like 'BA_Test' -- and m.created_at > '2026-04-21'
order by m.dataset_name desc, m2.name asc, m.id asc;


select d.name, count(dp.id) from qsar_datasets.datasets d 
join qsar_datasets.data_points dp on d.id=dp.fk_dataset_id 
where d.name like '%RIFM%' or d.name like '%301F%'
group by d.name;


select d.name, split_num, count(dpis.id) from qsar_datasets.datasets d 
join qsar_datasets.data_points dp on d.id=dp.fk_dataset_id 
join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id=dp.id
join qsar_datasets.splittings s on s.id=dpis.fk_splitting_id 
where (d.name like '%RIFM%' or d.name like '%301F%') and s.name='RND_REPRESENTATIVE' 
group by d.name, split_num;


select m2.name,m.id from qsar_models.models m
join qsar_models.methods m2 on m2.id=m.fk_method_id 
where m.dataset_name ='KOC v1 modeling';





delete from qsar_models.models where id>=1759 and dataset_name='KOC v1 modeling';



select  m.details_text ,   m.id as model_id, m2.name, ms.statistic_value, Length(de.embedding_tsv) - Length(REPLACE(de.embedding_tsv, E'\t', ''))+1 as variables 
from qsar_models.models m
join qsar_models.model_statistics ms on ms.fk_model_id=m.id
join qsar_models."statistics" s on s.id=ms.fk_statistic_id 
join qsar_models.methods m2 on m2.id=m.fk_method_id
join qsar_datasets.datasets d on d.name=m.dataset_name 
join qsar_datasets.properties p on p.id=d.fk_property_id 
join qsar_models.descriptor_embeddings de on de.id=m.fk_descriptor_embedding_id 
where (s.name like 'MAE_Test' and d.name = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3b modeling') -- and m.created_at > '2026-04-21'
order by m2."name" ;


select m2.name from qsar_models.models m
join qsar_models.methods m2 on m2.id=m.fk_method_id 
where m.id = '1897';




select  m.id, m2.name
from qsar_models.models m
join qsar_models.methods m2 on m2.id=m.fk_method_id
where (m.dataset_name  = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3b modeling') 


select distinct m.dataset_name
	from
	qsar_models.models m
where
	m.id in (1065,1066,1067,1068,1069,1070,1754,1756,1757,1758,1763,1832,1833,1834,1837,1845,
1849,1852,1862,1865,1869,1879,1880,1892,1895,1896,1897);


select d.name from qsar_datasets.data_points dp 
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where dp.qsar_dtxcid like '%DTXCID001783033%';

select distinct dpc.dtxsid from qsar_datasets.data_points dp 
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id =dp.id
where dp.qsar_dtxcid like '%DTXCID001783033%';


order by m2.name ;


select mf.* from qsar_models.models m
join qsar_models.model_files mf on m.id = mf.fk_model_id
limit 5;


select count (pv.id) from exp_prop.property_values pv where fk_public_source_id=310;