--updated creation with speed up (dont use row_number() over (order by dr.dtxsid, p."name",s.name) as id)
--CREATE MATERIALIZED VIEW mv_predicted_data AS
SELECT
  pd.id AS prediction_id,                     -- stable unique key; enables concurrent refresh
  dr.dtxsid, dr.dtxcid, dr.smiles,
  pd.canon_qsar_smiles,
  'predicted' AS prop_type,
  pc.name AS prop_category,
  p.description AS property_description,
  m.name_ccd AS model_name,
  m.id AS model_id,
  s.description AS source_description,
  pd.experimental_value AS prop_value_experimental,
  pd.experimental_string AS prop_value_experimental_string,
  p.name_ccd AS prop_name,
  s.name AS source_name,
  pd.prediction_value AS prop_value,
  u.abbreviation_ccd AS prop_unit,
  pd.prediction_string AS prop_value_string,
  pd.prediction_error AS prop_value_error,
  adm.name AS AD_method,
  qpad.applicability_value AS AD_value,
  qpad.conclusion AS AD_conclusion,
  qpad.reasoning AS AD_reasoning,
  CASE WHEN s.name = 'OPERA2.8' THEN 'OPERA Global Index' END AS AD_method_global,
  qpad2.applicability_value AS AD_value_global,
  qpad2.conclusion AS AD_conclusion_global,
  qpad2.reasoning AS AD_reasoning_global,
  m.has_qmrf,
  CASE WHEN m.has_qmrf IS TRUE
       THEN CONCAT('https://comptox.epa.gov/ctx-api/chemical/property/model/file/search/?modelId=', m.id, '&typeId=1')
  END AS qmrf_url,
  current_date AS export_date,
  '3.1.0' AS data_version
FROM qsar_models.predictions_dashboard pd
JOIN qsar_models.models m
  ON m.id = pd.fk_model_id
JOIN qsar_models.sources s
  ON s.id = m.fk_source_id
 AND s.name IN ('Percepta2025.1.4', 'OPERA2.8', 'TEST5.1.3')
JOIN qsar_datasets.datasets d
  ON d."name" = m.dataset_name
JOIN qsar_datasets.properties p
  ON p.id = d.fk_property_id
 AND p.id <> 65
JOIN qsar_datasets.units u
  ON u.id = d.fk_unit_id_contributor
JOIN qsar_models.dsstox_records dr
  ON dr.dtxcid = pd.dtxcid
 AND dr.fk_dsstox_snapshot_id = 4
LEFT JOIN qsar_models.qsar_predicted_ad_estimates qpad
  ON qpad.fk_predictions_dashboard_id = pd.id
 AND qpad.fk_ad_method_id = m.fk_ad_method
LEFT JOIN qsar_models.qsar_predicted_ad_estimates qpad2
  ON qpad2.fk_predictions_dashboard_id = pd.id
 AND qpad2.fk_ad_method_id = 1
LEFT JOIN qsar_models.ad_methods adm
  ON adm.id = m.fk_ad_method
LEFT JOIN qsar_datasets.properties_in_categories pic
  ON pic.fk_property_id = p.id
LEFT JOIN qsar_datasets.property_categories pc
  ON pc.id = pic.fk_property_category_id
where
dtxsid='DTXSID7020182' and s.name='OPERA2.8' and (pd.experimental_value is not null or pd.experimental_string is not null)
;

-- After creation
CREATE UNIQUE INDEX mv_predicted_data_prediction_id_uq ON mv_predicted_data (prediction_id);

CREATE INDEX mv_predicted_data_order_idx
  ON mv_predicted_data (dtxsid, prop_name, source_name);

-- For faster lookups, add any additional non-unique indexes you routinely filter on:
CREATE INDEX mv_predicted_data_model_id_idx ON mv_predicted_data (model_id);
CREATE INDEX mv_predicted_data_prop_name_idx ON mv_predicted_data (prop_name);
CREATE INDEX mv_predicted_data_source_name_idx ON mv_predicted_data (source_name);

-- Refresh strategy (PostgreSQL)
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_predicted_data;


--physical ordering via CLUSTER (run after each refresh)
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_predicted_data;
CLUSTER mv_predicted_data USING mv_predicted_data_order_idx;
ANALYZE mv_predicted_data;


-- sql for creation:
--CREATE MATERIALIZED VIEW mv_predicted_data as
select
--       row_number() over (order by dr.dtxsid, p."name",s.name) as id,
       dr.dtxsid,dr.dtxcid,dr.smiles,
       pd.canon_qsar_smiles,
--       dr.generic_substance_updated_at,
       'predicted' as prop_type,
       pc.name as prop_category,
       p.description as property_description,
       m.name_ccd as model_name,
       m.id as model_id,
       s.description as source_description,
       pd.experimental_value as prop_value_experimental,
       pd.experimental_string as prop_value_experimental_string,
	   p.name_ccd as prop_name, --move next prediction_value for checking
	   s.name as source_name,
       pd.prediction_value as prop_value,
       u.abbreviation_ccd as prop_unit,
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
       case when m.has_qmrf is true then CONCAT('https://comptox.epa.gov/ctx-api/chemical/property/model/file/search/?modelId=',m.id,'&typeId=1') end as qmrf_url,
       current_date as export_date,
       '3.1.0' as data_version
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
where dr.fk_dsstox_snapshot_id=4
   and (s.name='Percepta2025.1.4' or s.name='OPERA2.8' or s.name='TEST5.1.3')
   and p.id!=65 -- omit Liquid Chromatography Retention Time  
--   and s.name='TEST5.1.3'
   
--   and s.name='Percepta2025.1.4'
-- and dr.dtxsid='DTXSID7020182' -- bisphenol-a
 and dr.dtxsid='DTXSID3039242' -- benzene
-- and dr.dtxsid='DTXSID6020482' -- new chem
-- and dr.dtxsid='DTXSID001000007' -- null values in summary table
-- and dr.dtxsid='DTXSID7020005'-- has TEST prediction outside AD for ST
-- and dr.dtxcid='DTXCID3054' -- salt
-- and p.name in ('Water solubility','Vapor pressure','Melting point','Boiling point','LogKow: Octanol-Water','Henry''s law constant')
-- order by dr.dtxsid, p."name",s.name -- this line slows things down when getting all records
-- order by dr.dtxsid -- this line slows things down when getting all records
-- limit 100
;

select p.name_ccd,p.id from qsar_datasets.properties p 
order by p.name_ccd;


select * from pg_stat_progress_vacuum;

refresh materialized view "mv_predicted_data";

VACUUM (ANALYZE, VERBOSE, FULL) qsar_models.predictions_dashboard;

VACUUM (ANALYZE, VERBOSE, FULL) qsar_models.prediction_reports;

VACUUM (ANALYZE, VERBOSE, FULL) qsar_models.qsar_predicted_neighbors;

VACUUM (ANALYZE, VERBOSE, FULL) qsar_models.qsar_predicted_ad_estimates;

VACUUM (ANALYZE, VERBOSE, FULL) qsar_models.dsstox_records;--done
VACUUM (ANALYZE, VERBOSE, FULL) qsar_models.models;--done

GRANT SELECT ON "mv_predicted_data" TO app_pentaho;


create index mv_predicted_data_id  on "mv_predicted_data" (id);

create index mv_predicted_data_dtxsid_index  on "mv_predicted_data" (dtxsid);

create index mv_predicted_data_prop_name_index  on "mv_predicted_data" (prop_name);
create index mv_predicted_data_source_name_index  on "mv_predicted_data" (source_name);

comment on materialized view "mv_predicted_data" is 'QSAR model predictions materialized view';
comment on column "mv_predicted_data".id is 'Autogenerated id for sorting';
comment on column "mv_predicted_data".dtxsid is 'DSSTox Substance id';
comment on column "mv_predicted_data".dtxcid is 'DSSTox Compound id';
comment on column "mv_predicted_data".smiles is 'SMILES structure used to generate the prediction';
comment on column "mv_predicted_data".canon_qsar_smiles is 'QSAR ready SMILES structure used to generate the prediction';
--comment on column "mv_predicted_data".generic_substance_updated_at is 'prod_dsstox.generic_substances.updated_at';
comment on column "mv_predicted_data".prop_type is 'Property type (e.g. predicted)';
comment on column "mv_predicted_data".prop_category is 'Property category (e.g. Physchem)';
comment on column "mv_predicted_data".prop_name is 'Property name (e.g. Boiling Point)';
comment on column "mv_predicted_data".property_description is 'Property description (e.g. Temperature at which a chemical changes state from liquid to vapor at a given pressure)';
comment on column "mv_predicted_data".model_id is 'ID of the model in qsar_models.models table (e.g. 123)';
comment on column "mv_predicted_data".model_name is 'Name of the model (e.g. ACD_Prop_Polarizability)';
comment on column "mv_predicted_data".source_name is 'Name of the model source (e.g. Percepta2023.1.2)';
comment on column "mv_predicted_data".source_description is 'Descrption of the model source (e.g. <a href="https://www.acdlabs.com/products/percepta-platform/">Percepta from ACD/Labs</a> predicts physicochemical properties.)';
comment on column "mv_predicted_data".prop_value is 'Numerical predicted value from the model';
comment on column "mv_predicted_data".prop_value_string is 'Text predicted value from the model (e.g. Inactive)';
comment on column "mv_predicted_data".prop_value_error is 'Error message from model output (e.g. Cannot calculate Boiling Point)';
comment on column "mv_predicted_data".prop_value_experimental is 'Numerical experimental value from the model output';
comment on column "mv_predicted_data".prop_value_experimental_string is 'Text experimental value from the model output (e.g. 501-2000)';
comment on column "mv_predicted_data".prop_unit is 'Units for the predicted value';
comment on column "mv_predicted_data".ad_method is 'Applicability domain method (e.g. Combined Applicability Domain)';
comment on column "mv_predicted_data".ad_value is 'Applicability domain numerical value (e.g. 1.0)';
comment on column "mv_predicted_data".ad_conclusion is 'Applicability domain conclusion (e.g. Inside AD)';
comment on column "mv_predicted_data".ad_reasoning is 'Reasoning for the applicability domain conclusion (e.g. Inside training set (Global AD = 1) and good local representation (Local AD index = 1.0 &gt; 0.6))';
comment on column "mv_predicted_data".ad_method_global is 'OPERA Global Index';
comment on column "mv_predicted_data".ad_value_global is 'Binary score for the OPERA Global Index (e.g. 0 or 1)';
comment on column "mv_predicted_data".ad_conclusion_global is 'Global AD conclusion (e.g. Inside or Outside)';
comment on column "mv_predicted_data".ad_reasoning_global is 'Reasoning for the Global AD conclusion (e.g. Inside since value=1)';
comment on column "mv_predicted_data".has_qmrf is 'Whether or not there is a link for qmrf document';
comment on column "mv_predicted_data".qmrf_url is 'URL for qmrf pdf from the model file API';
comment on column "mv_predicted_data".export_date is 'When the materialized view was updated';
comment on column "mv_predicted_data".data_version is 'Version of the materialized view (see public.materialized_view_version_history for description of changes made)';


-- dsstox_records: use the filter & join together
CREATE INDEX IF NOT EXISTS dsstox_records_snapshot_dtxcid_idx
  ON qsar_models.dsstox_records (fk_dsstox_snapshot_id, dtxcid);

SET work_mem = '512MB';
SHOW work_mem;

show max_parallel_workers_per_gather;
set max_parallel_workers_per_gather=8;


ANALYZE qsar_models.dsstox_records;
ANALYZE qsar_models.predictions_dashboard;
ANALYZE qsar_models.models;
ANALYZE qsar_models.qsar_predicted_ad_estimates;
ANALYZE qsar_datasets.datasets;
ANALYZE qsar_datasets.properties_in_categories;


select count(id) from public.mv_predicted_data pd;



SELECT
  pd.id AS prediction_id,            
  dr.dtxsid, dr.dtxcid, dr.smiles,
  pd.canon_qsar_smiles,
  'predicted' AS prop_type,
  pc.name AS prop_category,
  p.description AS property_description,
  m.name_ccd AS model_name,
  m.id AS model_id,
  s.description AS source_description,
  pd.experimental_value AS prop_value_experimental,
  pd.experimental_string AS prop_value_experimental_string,
  p.name_ccd AS prop_name,
  s.name AS source_name,
  pd.prediction_value AS prop_value,
  u.abbreviation_ccd AS prop_unit,
  pd.prediction_string AS prop_value_string,
  pd.prediction_error AS prop_value_error,
  adm.name AS AD_method,
  qpad.applicability_value AS AD_value,
  qpad.conclusion AS AD_conclusion,
  qpad.reasoning AS AD_reasoning,
  CASE WHEN s.name = 'OPERA2.8' THEN 'OPERA Global Index' END AS AD_method_global,
  qpad2.applicability_value AS AD_value_global,
  qpad2.conclusion AS AD_conclusion_global,
  qpad2.reasoning AS AD_reasoning_global,
  m.has_qmrf,
  CASE WHEN m.has_qmrf IS TRUE
       THEN CONCAT('https://comptox.epa.gov/ctx-api/chemical/property/model/file/search/?modelId=', m.id, '&typeId=1')
  END AS qmrf_url,
  current_date AS export_date,
  '3.1.0' AS data_version
FROM qsar_models.predictions_dashboard pd
JOIN qsar_models.models m
  ON m.id = pd.fk_model_id
JOIN qsar_models.sources s
  ON s.id = m.fk_source_id  
JOIN qsar_datasets.datasets d
  ON d."name" = m.dataset_name
JOIN qsar_datasets.properties p
  ON p.id = d.fk_property_id
 AND p.id <> 65
JOIN qsar_datasets.units u
  ON u.id = d.fk_unit_id_contributor
JOIN qsar_models.dsstox_records dr
  ON dr.dtxcid = pd.dtxcid
 AND dr.fk_dsstox_snapshot_id = 4
LEFT JOIN qsar_models.qsar_predicted_ad_estimates qpad
  ON qpad.fk_predictions_dashboard_id = pd.id
 AND qpad.fk_ad_method_id = m.fk_ad_method
LEFT JOIN qsar_models.qsar_predicted_ad_estimates qpad2
  ON qpad2.fk_predictions_dashboard_id = pd.id
 AND qpad2.fk_ad_method_id = 1
LEFT JOIN qsar_models.ad_methods adm
  ON adm.id = m.fk_ad_method
LEFT JOIN qsar_datasets.properties_in_categories pic
  ON pic.fk_property_id = p.id
LEFT JOIN qsar_datasets.property_categories pc
  ON pc.id = pic.fk_property_category_id
where s.name = 'OPERA2.8' and dr.dtxsid='DTXSID3039242';-- get opera predictions for benzene