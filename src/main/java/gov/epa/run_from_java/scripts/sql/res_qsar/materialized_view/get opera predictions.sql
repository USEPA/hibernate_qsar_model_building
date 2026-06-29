SELECT
  pd.id AS prediction_id,                     -- stable unique key; enables concurrent refresh
  dr.dtxsid, dr.dtxcid,
  -- dr.smiles,
  -- pd.canon_qsar_smiles,
  -- 'predicted' AS prop_type,
  -- pc.name AS prop_category,
  --p.description AS property_description,
  m.name_ccd AS model_name,
  -- m.id AS model_id,
  -- s.description AS source_description,
  pd.experimental_value AS prop_value_experimental,
  pd.experimental_string AS prop_value_experimental_string,
  p.name_ccd AS prop_name,
  s.name AS source_name,
  pd.prediction_value AS prop_value,
  u.abbreviation_ccd AS prop_unit,
  pd.prediction_string AS prop_value_string,
  pd.prediction_error AS prop_value_error,
  adm.name AS AD_method,
 -- qpad.applicability_value AS AD_value,
 -- qpad.conclusion AS AD_conclusion,
  qpad.reasoning AS AD_reasoning
--  CASE WHEN s.name = 'OPERA2.8' THEN 'OPERA Global Index' END AS AD_method_global,
--  qpad2.applicability_value AS AD_value_global,
--  qpad2.conclusion AS AD_conclusion_global,
--  qpad2.reasoning AS AD_reasoning_global
 -- m.has_qmrf,
 -- CASE WHEN m.has_qmrf IS TRUE
 --      THEN CONCAT('https://comptox.epa.gov/ctx-api/chemical/property/model/file/search/?modelId=', m.id, '&typeId=1')
 -- END AS qmrf_url,
 -- current_date AS export_date,
 -- '3.1.0' AS data_version
FROM qsar_models.predictions_dashboard pd
JOIN qsar_models.models m
  ON m.id = pd.fk_model_id
JOIN qsar_models.sources s
  ON s.id = m.fk_source_id
 AND s.name IN ('OPERA2.8')
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
--LEFT JOIN qsar_models.qsar_predicted_ad_estimates qpad2
--  ON qpad2.fk_predictions_dashboard_id = pd.id
-- AND qpad2.fk_ad_method_id = 1
LEFT JOIN qsar_models.ad_methods adm
  ON adm.id = m.fk_ad_method
--LEFT JOIN qsar_datasets.properties_in_categories pic
--  ON pic.fk_property_id = p.id
--LEFT JOIN qsar_datasets.property_categories pc
--  ON pc.id = pic.fk_property_category_id
 WHERE pd.dtxcid = 'DTXCID20135'; -- need to search on dtxcid since pairings of dtxsid and dtxcid change over time
