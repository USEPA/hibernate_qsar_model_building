-- Get prediction count by snapshot for percepta
select fk_dsstox_snapshot_id, count(pd.id) from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
join qsar_models.models m on pd.fk_model_id = m.id
join qsar_models.sources s on m.fk_source_id = s.id
where s.name like '%Percepta%'
group by fk_dsstox_snapshot_id;

-- Get dsstoxrecord  count by snapshot for percepta
select fk_dsstox_snapshot_id, count(distinct dr.id) from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
join qsar_models.models m on pd.fk_model_id = m.id
join qsar_models.sources s on m.fk_source_id = s.id
where s.name like '%Percepta%'
group by fk_dsstox_snapshot_id;



-- Get chemicals with a certain number of percepta predictions:
select dr.dtxsid, count(dr.dtxsid)
from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
join qsar_models.sources s on m.fk_source_id = s.id
join qsar_datasets.datasets d on d."name" =m.dataset_name
join qsar_datasets.properties p on p.id=d.fk_property_id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
where s.name like '%Percepta%' and fk_dsstox_snapshot_id=2
and pd.prediction_error is null
-- and pd.prediction_error is not null
group by dr.dtxsid
having count(dr.dtxsid)=16
;

-- see what predictions arent there
-- DId the ones with * make it to the sdf that was ran?
select dtxsid, smiles from qsar_models.dsstox_records dr
left join qsar_models.predictions_dashboard pd on dr.id = pd.fk_dsstox_records_id
left join qsar_models.models m on m.id=pd.fk_model_id
left join qsar_models.sources s on m.fk_source_id = s.id
where dr.fk_dsstox_snapshot_id=2 and pd.id is null and smiles not like '%.%' and smiles not like '%|%';


-- Look at values for a chemical
select s.name, p.name, pd.prediction_value, u.name,pd.prediction_error
from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
join qsar_models.sources s on m.fk_source_id = s.id
join qsar_datasets.datasets d on d."name" =m.dataset_name
join qsar_datasets.properties p on p.id=d.fk_property_id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
where dr.dtxsid='DTXSID4060078' and dr.fk_dsstox_snapshot_id=2
order by p.name;

--Look at values in materialized view:
select m.name,pd.prediction_value,pd.prediction_error from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
where m.fk_source_id=7 and dr.fk_dsstox_snapshot_id=2 and dtxsid='DTXSID2062791';


-- Get counts by model other than percepta:
select m.name, count(pd.id) from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
    join qsar_models.models m on pd.fk_model_id = m.id
where fk_dsstox_snapshot_id=2 and m.fk_source_id!=7
group by m.name;

-- Get counts by source other than percepta:
select s.name, count(pd.id) from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
    join qsar_models.models m on pd.fk_model_id = m.id
join qsar_models.sources s on m.fk_source_id = s.id
where fk_dsstox_snapshot_id=2 and m.fk_source_id!=7
group by s.name;

-- Look at changed values
select m.name, pd1.prediction_value,m2.name, pd2.prediction_value from qsar_models.dsstox_records dr
left join qsar_models.predictions_dashboard pd1 on dr.id = pd1.fk_dsstox_records_id and dr.fk_dsstox_snapshot_id=1
left join qsar_models.models m on m.id=pd1.fk_model_id and m.fk_source_id=7
left join qsar_models.predictions_dashboard pd2 on dr.id = pd2.fk_dsstox_records_id and dr.fk_dsstox_snapshot_id=2
left join qsar_models.models m2 on m2.id=pd2.fk_model_id
where  dtxsid='DTXSID3039242';



update qsar_models.models set name_ccd=replace(name_ccd,' ','') where fk_source_id=6;



select m.name,s.name from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id
join qsar_models.statistics s on ms.fk_statistic_id = s.id
where fk_source_id=6 and s.name like '%CV%'
order by m.name;

