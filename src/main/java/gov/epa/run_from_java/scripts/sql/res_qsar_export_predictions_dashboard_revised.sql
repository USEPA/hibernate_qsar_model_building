-- Export all predictions for asif:
-- CREATE MATERIALIZED VIEW mv_predicted_data as
select
       row_number() over (order by dr.dtxsid, p."name",s.name) as id,
       dr.dtxsid,dr.dtxcid,dr.smiles,
       pd.canon_qsar_smiles,
       dr.generic_substance_updated_at,
       p.name_ccd as prop_name,
       'predicted' as prop_type,
       pc.name as prop_category,
       p.description as property_description,
       m.name_ccd as model_name,
       m.id as model_id,
       s.name as source_name,
       s.description as source_description,
       pd.experimental_value as prop_value_experimental,
       pd.experimental_string as prop_value_experimental_string,
--        pd.prediction_value as prediction_value,
       pd.prediction_value as prop_value,
       u.abbreviation_ccd as prop_unit,
--        prediction_string,
--        prediction_error,
       prediction_string as prop_value_string,
       prediction_error as prop_value_error,
       adm.name as AD_method,
       qpad.applicability_value as AD_value,
       qpad.conclusion as AD_conclusion,
       qpad.reasoning as AD_reasoning,
       case when s.name ='OPERA2.8' then 'OPERA Global Index' end as AD_method_global,
       qpad2.applicability_value as AD_value_global,
       qpad2.conclusion as AD_conclusion_global,
       qpad2.reasoning as AD_reasoning_global,
       m.has_qmrf,
       case when m.has_qmrf is true then CONCAT('https://ctx-api-dev.ccte.epa.gov/chemical/property/model/file/search/?modelId=',m.id,'&typeId=1') end as qmrf_url,
       current_date as export_date,
       '2.0.0' as data_version
from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
join qsar_models.sources s on m.fk_source_id = s.id
join qsar_datasets.datasets d on d."name" =m.dataset_name
join qsar_datasets.properties p on p.id=d.fk_property_id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor
join qsar_models.dsstox_records dr on pd.dtxcid = dr.dtxcid --join based on dtxcid instead of fk so that have up to date dtxsids
-- join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
left join qsar_models.qsar_predicted_ad_estimates qpad on pd.id = qpad.fk_predictions_dashboard_id and qpad.fk_ad_method_id=m.fk_ad_method
left join qsar_models.qsar_predicted_ad_estimates qpad2 on pd.id = qpad2.fk_predictions_dashboard_id and qpad2.fk_ad_method_id=1
left join qsar_models.ad_methods adm on m.fk_ad_method = adm.id
left join qsar_datasets.properties_in_categories pic on p.id = pic.fk_property_id
left join qsar_datasets.property_categories pc on pic.fk_property_category_id = pc.id
where dr.fk_dsstox_snapshot_id=3
--   and s.name='Percepta2023.1.2'
--   and (s.name='OPERA2.8' or s.name='Percepta2023.1.2')
--   and (s.name='TEST5.1.3')
--   and (s.name='OPERA2.8' or s.name='Percepta2023.1.2') --omit sample records from other software
--   and dr.dtxsid='DTXSID7021360'
-- and dr.dtxsid='DTXSID7020182' -- bisphenol-a
-- and dr.dtxcid='DTXCID505'
-- and dr.dtxsid='DTXSID40166952'
and dr.dtxsid='DTXSID3039242' -- benzene
-- and dr.dtxsid='DTXSID50281842' -- percepta not in mongo
-- and dr.dtxsid='DTXSID001000007' -- null values in summary table
-- and dr.dtxsid='DTXSID7020005'-- has TEST prediction outside AD for ST
-- and dr.dtxcid='DTXCID3054' -- salt
-- and p.name in ('Water solubility','Vapor pressure','Melting point','Boiling point','LogKow: Octanol-Water','Henry''s law constant')
-- order by dr.dtxsid, p."name",s.name -- this line slows things down when getting all records
-- order by dr.dtxsid -- this line slows things down when getting all records
-- limit 100
;


select count(distinct fk_dsstox_records_id) from qsar_models.predictions_dashboard where dtxcid is not null


select dr.dtxsid,dr.dtxcid,pd.dtxcid from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id;


UPDATE qsar_models.predictions_dashboard pd SET dtxcid = dr.dtxcid
FROM qsar_models.dsstox_records dr
WHERE pd.fk_dsstox_records_id = dr.id;

