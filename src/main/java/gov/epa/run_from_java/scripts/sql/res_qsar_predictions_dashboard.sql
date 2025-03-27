-- Find count of distinct chemicals for TEST predictions in predictions_dashboard
select count(distinct(pd.fk_dsstox_records_id)) from qsar_models.predictions_dashboard pd
where fk_model_id>=223 and fk_model_id<=240;

-- Find count of distinct chemicals in predictions_dashboard
select count(distinct(pd.fk_dsstox_records_id)) from qsar_models.predictions_dashboard pd;

-- Find count of distinct prediction errors in predictions_dashboard
select distinct(pd.prediction_error) from qsar_models.predictions_dashboard pd;

-- Todo change error string for path finding to "Timeout while finding paths"
-- Todo change error string FindRings to "Timeout while finding rings"
-- TODO update predictions_dashboard to remove extra columns

-- Find records that dont have a fk to dsstox_records table
select count(id) from qsar_models.predictions_dashboard pd where fk_dsstox_records_id is null;

-- ******************************************************************************************
-- Deleting percepta predictions:
delete from qsar_models.predictions_dashboard pd using qsar_models.models m
where pd.fk_model_id = m.id and m.fk_source_id=4;

-- Or
delete from qsar_models.predictions_dashboard pd where fk_model_id>=136 and fk_model_id<=150; -- percepta models


-- Deleting TEST reports:
delete from qsar_models.prediction_reports pr using qsar_models.predictions_dashboard pd
where fk_model_id>=223 and fk_model_id<=240;

-- Deleting TEST predictions:
delete from qsar_models.predictions_dashboard pd where fk_model_id>=223 and fk_model_id<=240; -- TEST models


-- ******************************************************************************************
-- Find TEST predictions with missing reports
select count(pd.id) from qsar_models.predictions_dashboard pd
left join qsar_models.prediction_reports pr on pd.id = pr.fk_predictions_dashboard_id
where fk_model_id>=223 and fk_model_id<=240 and pr.file is null;
-- order by pd.id

----------------------------------------------------------------------------------------------------------------------
-- Get prediction report using cross schema query
select pr.file as report from qsar_models.prediction_reports pr
         join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id
         join qsar_models.models m on pd.fk_model_id = m.id
         join qsar_datasets.datasets d on d.name = m.dataset_name
         join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id=dr.id
         join qsar_models.sources s on m.fk_source_id = s.id
where dr.dtxsid = 'DTXSID80177704' and p.name = 'Boiling point' and s.name='TEST5.1.3';


--------------------------------------------------------------------------------------------------
--Export Percepta predictions
-- unit is associated with the dataset not the property (different datasets can have different units for same property)
select p."name" as property, s.name,m."name" as model_name ,canon_qsar_smiles, smiles, dtxsid,dtxcid,prediction_value,u.abbreviation as prediction_units, prediction_string ,prediction_error from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id=dr.id
    join qsar_models.models m on m.id=pd.fk_model_id
join qsar_datasets.datasets d on d."name" =m.dataset_name
join qsar_datasets.properties p on p.id=d.fk_property_id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor
join qsar_models.sources s on m.fk_source_id = s.id
where s.name ='Percepta2020.2.1' and dtxcid='DTXCID50690839' and dr.fk_dsstox_snapshot_id=1
-- order by prediction_value desc
-- order by smiles,p."name"
order by dtxcid,p."name";

--------------------------------------------------------------------------------------------------
--Export Percepta predictions using dsstox fields from dsstox_records table (snapshot)
-- unit is associated with the dataset not the property (different datasets can have different units for same property)
select p."name" as property_name, s.name,m."name" as model_name ,canon_qsar_smiles,
       dr.smiles, dr.dtxsid,dr.dtxcid,
       prediction_value,u.abbreviation as prediction_units,
       prediction_string ,prediction_error
from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
join qsar_models.sources s on m.fk_source_id = s.id
join qsar_datasets.datasets d on d."name" =m.dataset_name
join qsar_datasets.properties p on p.id=d.fk_property_id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id=dr.id
where s.name ='Percepta2020.2.1' and dr.dtxcid='DTXCID70411028'
-- where dr.dtxcid='DTXCID70411028' and dr.fk_dsstox_snapshot_id=1
-- order by prediction_value desc
-- order by smiles,p."name"
order by dr.dtxcid,p."name";

-- Export all predictions:
select p."name" as property_name, s.name,m."name" as model_name ,canon_qsar_smiles,
       dr.smiles, dr.dtxsid,dr.dtxcid,
       prediction_value,u.abbreviation as prediction_units,
       prediction_string ,prediction_error
from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
    join qsar_models.sources s on m.fk_source_id = s.id
join qsar_datasets.datasets d on d."name" =m.dataset_name
join qsar_datasets.properties p on p.id=d.fk_property_id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id=dr.id
where dr.dtxsid='DTXSID7020182' and dr.fk_dsstox_snapshot_id=1
order by dr.dtxcid,p."name";


-- Export all predictions for asif:
select dr.dtxsid,dr.dtxcid,dr.smiles, 'predicted' as prop_type,p.name_ccd as property_name,
       m.name as model_name,
       s.name as source_name,
          pd.prediction_value as prop_value,u.abbreviation_ccd as unit,
           prediction_string ,prediction_error,
           current_date as update_date,
           '1.0.0' as data_version
from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
join qsar_models.sources s on m.fk_source_id = s.id
join qsar_datasets.datasets d on d."name" =m.dataset_name
join qsar_datasets.properties p on p.id=d.fk_property_id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
where
dr.smiles like '%Mo%'
--     dr.dtxsid='DTXSID7020182'
--     dr.dtxsid='DTXSID6020143' -- benzoic acid
--   dr.dtxsid='DTXSID1024207'
--   and m.name='ACD_BP'
--   and dr.fk_dsstox_snapshot_id=1
order by dr.dtxcid,p."name";




---------------------------------------------------------------------------------------------------------------------
--Export TEST predictions using dsstox_records to get dsstox fields
select p."name" as property_name, s.name,m."name" as model_name,canon_qsar_smiles,
       dr.smiles, dr.dtxsid,dr.dtxcid,
       prediction_value,u.abbreviation as prediction_units, prediction_string,
       prediction_error from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
join qsar_models.sources s on m.fk_source_id = s.id
join qsar_datasets.datasets d on d."name" =m.dataset_name
join qsar_datasets.properties p on p.id=d.fk_property_id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id=dr.id
where s.name ='TEST5.1.3' and dr.dtxsid='DTXSID00223252';

--Export TEST reports
select p."name" as property_name, convert_from(pr.file,'UTF-8') as report_string from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
    join qsar_models.sources s on m.fk_source_id = s.id
join qsar_datasets.datasets d on d."name" =m.dataset_name
join qsar_datasets.properties p on p.id=d.fk_property_id
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id=dr.id
join qsar_models.prediction_reports pr on pd.id = pr.fk_predictions_dashboard_id
where s.name='TEST5.1.3' and dr.dtxsid='DTXSID00223252';



select pr.id, pr.file,pr.updated_by from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id=dr.id
join qsar_models.models m on m.id=pd.fk_model_id
    join qsar_models.sources s on m.fk_source_id = s.id
join qsar_models.prediction_reports pr on pd.id = pr.fk_predictions_dashboard_id
where dr.dtxsid='DTXSID00223252' and s.name ='TEST5.1.3';



select * from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id=dr.id
join qsar_models.models m on m.id=pd.fk_model_id
join qsar_models.sources s on m.fk_source_id = s.id
where dr.dtxsid='DTXSID00223252' and s.name ='TEST5.1.3';

select * from qsar_models.prediction_reports pr
order by id desc
limit 50;

-- Chemicals in snapshot with Test predictions:
select count(dr.id) from qsar_models.dsstox_records dr
left join qsar_models.predictions_dashboard pd on pd.fk_dsstox_records_id=dr.id
where pd.fk_model_id=223;


-- update qsar_models.models set fk_ad_method=10 where name like '%OPERA2.9%';


-- Get count loaded today:
select count(distinct (pd.fk_dsstox_records_id)) from qsar_models.predictions_dashboard pd
where date_trunc('day',created_at)::date =current_date;


-- Delete ones loaded today:
delete from qsar_models.predictions_dashboard pd
where date_trunc('day',created_at)::date =current_date;


ALTER TABLE qsar_models.qsar_predicted_neighbors DROP CONSTRAINT ukgc5ienluqj0hbf1qt22nwdtwe;