SELECT pd.prop_name, MIN(pd.prop_value) AS min_pred, MAX(pd.prop_value) AS max_pred, pd.prop_unit
FROM public.mv_predicted_data pd
where source_name='Percepta2023.1.2'
GROUP BY pd.prop_name,pd.prop_unit;

-------------------------------------------------------------------------------------------------
WITH params AS (
  SELECT 'Percepta2023.1.2'::text AS src
),
MinValues AS (
    SELECT prop_name, MIN(prop_value) AS min_value
    FROM mv_predicted_data, params
    WHERE source_name = params.src
    GROUP BY prop_name
),
MaxValues AS (
    SELECT prop_name, MAX(prop_value) AS max_value
    FROM mv_predicted_data, params
    WHERE source_name = params.src
    GROUP BY prop_name
)
SELECT
    mv.prop_name,
    mv.min_value,
    min_data.dtxsid AS min_dtxsid,
    mx.max_value,
    max_data.dtxsid AS max_dtxsid
FROM MinValues mv
JOIN mv_predicted_data min_data
    ON min_data.prop_name = mv.prop_name
    AND min_data.prop_value = mv.min_value
JOIN MaxValues mx
    ON mx.prop_name = mv.prop_name
JOIN mv_predicted_data max_data
    ON max_data.prop_name = mx.prop_name
    AND max_data.prop_value = mx.max_value
CROSS JOIN params
WHERE min_data.source_name = params.src
  AND max_data.source_name = params.src;


---------------------------------------------------------------------------------------------------------------
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

-- prop names:
select distinct p.name_ccd from qsar_models.models m  
JOIN qsar_models.sources s       ON m.fk_source_id = s.id
JOIN qsar_datasets.datasets d    ON d."name" = m.dataset_name
JOIN qsar_datasets.properties p  ON p.id = d.fk_property_id
WHERE s.name ='Percepta2025.1.4'
order by p.name_ccd ;


-- compare values over Percepta versions
SELECT
  pd.dtxcid,
  MAX(CASE WHEN s.name = 'Percepta2023.1.2' THEN pd.prediction_value END) AS prop_value_percepta_2023_1_2,
  MAX(CASE WHEN s.name = 'Percepta2025.1.4' THEN pd.prediction_value END)     AS prop_value_percepta_2025_1_4
FROM qsar_models.predictions_dashboard pd
JOIN qsar_models.models m        ON m.id = pd.fk_model_id
JOIN qsar_models.sources s       ON m.fk_source_id = s.id
JOIN qsar_datasets.datasets d    ON d."name" = m.dataset_name
JOIN qsar_datasets.properties p  ON p.id = d.fk_property_id
WHERE 
--p.name_ccd ='Boiling Point' -- no change
--p.name_ccd ='Density' -- no change
--p.name_ccd ='Dielectric Constant' -- no change
--p.name_ccd ='Flash Point' -- no change
--p.name_ccd ='Index of Refraction' -- no change
--p.name_ccd ='Molar Refractivity' -- no change
--p.name_ccd ='Molar Volume' -- no change
--p.name_ccd ='Polarizability' -- no change
--p.name_ccd ='Surface Tension' -- no change
--p.name_ccd ='Vapor Pressure' -- no change
--p.name_ccd = 'Water Solubility' -- change
--p.name_ccd = 'LogKow: Octanol-Water' -- slight change 
--p.name_ccd = 'pKa Acidic Apparent' -- slight change
--p.name_ccd = 'pKa Basic Apparent' -- slight change
--p.name_ccd = 'LogD5.5' -- slight change
p.name_ccd = 'LogD7.4' -- slight change
AND s.name IN ('Percepta2023.1.2', 'Percepta2025.1.4')
GROUP BY pd.dtxcid
limit 100;
