SELECT column_name
FROM information_schema.columns
WHERE table_schema = 'qsar_datasets'
  AND table_name  = 'data_points_in_splittings'
ORDER BY ordinal_position;

select m.id, m2.name, de.embedding_tsv  from qsar_models.models m
join qsar_models.methods m2 on m.fk_method_id =m2.id
join qsar_models.descriptor_embeddings de on de.id = m.fk_descriptor_embedding_id 
where m.dataset_name  = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3a modeling'
order by m2.name;


SELECT distinct fk_splitting_id,s.name  from qsar_models.predictions
join qsar_datasets.splittings s  on s.id=fk_splitting_id 
where fk_model_id =1643
order by fk_splitting_id;


UPDATE qsar_models.models 
SET details_text = convert_from(details, 'UTF8')
WHERE details IS NOT NULL AND details_text IS NULL;

select m.id, m.dataset_name ,m2."name", de.embedding_tsv   from qsar_models.models m 
join qsar_models.methods m2 on m2.id=m.fk_method_id
join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id =de.id
where m.dataset_name = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3a modeling' or m.dataset_name = 'KOC v1 modeling'
order by m.id desc;

select details_text from  qsar_models.models
where dataset_name  = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3a modeling';



SELECT DISTINCT dr.dtxcid, dr.smiles 
FROM qsar_models.dsstox_records dr
WHERE dr.fk_dsstox_snapshot_id = 4
  AND NOT EXISTS (
    SELECT 1
    FROM public.mv_predicted_data mpd
    WHERE mpd.dtxcid = dr.dtxcid
      AND mpd.source_name = 'OPERA2.8'
  );



WITH ms AS (
  SELECT m.id
  FROM qsar_models.models m
  JOIN qsar_models.sources s ON s.id = m.fk_source_id
  WHERE s.name = 'OPERA2.8'
)
SELECT pd.dtxcid
FROM qsar_models.predictions_dashboard pd
JOIN ms ON ms.id = pd.fk_model_id
GROUP BY pd.dtxcid
HAVING COUNT(pd.fk_model_id) >= 1;


WITH ms AS (
  SELECT m.id
  FROM qsar_models.models m
  JOIN qsar_models.sources s ON s.id = m.fk_source_id
  WHERE s.name = 'OPERA2.8'
)
SELECT DISTINCT pd.dtxcid
FROM qsar_models.predictions_dashboard pd
JOIN ms ON ms.id = pd.fk_model_id;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pd_created_dtxcid
  ON qsar_models.predictions_dashboard (created_at, dtxcid);


SELECT COUNT(DISTINCT pd.dtxcid)
FROM qsar_models.predictions_dashboard pd
WHERE pd.created_at >= TIMESTAMP '2026-03-02 00:00:00.000000';


select m.name_ccd, pd.fk_dsstox_records_id   FROM qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id 
join qsar_models.dsstox_records dr on dr.id= pd.fk_dsstox_records_id 
where canon_qsar_smiles  = 'CCCC' and dr.fk_dsstox_snapshot_id =4;




	WITH ms AS (
				  SELECT m.id
				  FROM qsar_models.models m
				  JOIN qsar_models.sources s ON s.id = m.fk_source_id
				  WHERE s.name = 'OPERA2.8'
				)
				SELECT count(pd.id)
				FROM qsar_models.predictions_dashboard pd				
				JOIN ms ON ms.id = pd.fk_model_id;
			
CREATE INDEX IF NOT EXISTS idx_pd_fk_model_id_dtxcid
  ON qsar_models.predictions_dashboard(fk_model_id, dtxcid);


	-- get dtxcids w/o predictions for OPERA
		SELECT COUNT(*) AS missing_count
	        FROM qsar_models.dsstox_records dr
	        WHERE dr.fk_dsstox_snapshot_id = 4
	          AND NOT EXISTS (
	            SELECT 1
	            FROM qsar_models.predictions_dashboard pd
	            JOIN qsar_models.models m ON m.id = pd.fk_model_id
	            WHERE m.fk_source_id = (
	              SELECT id
	              FROM qsar_models.sources
	              WHERE name = 'OPERA2.8'
	            )
	            AND pd.dtxcid = dr.dtxcid
	          );

SELECT COUNT(*) AS missing_count
FROM qsar_models.dsstox_records dr
LEFT JOIN (
  SELECT DISTINCT pd.dtxcid
  FROM qsar_models.predictions_dashboard pd
  JOIN qsar_models.models m ON m.id = pd.fk_model_id
  WHERE m.fk_source_id = (SELECT id FROM qsar_models.sources WHERE name = 'OPERA2.8')
) src_pd ON src_pd.dtxcid = dr.dtxcid
WHERE dr.fk_dsstox_snapshot_id = 4
  AND src_pd.dtxcid IS NULL;
	
	
SELECT
  id,
  details_text AS before,
  regexp_replace(
    details_text,
    '("qsarReadyRuleSet"\s*:\s*")qsar-ready(")',
    '\1qsar-ready_04242025_0\2',
    'g'
  ) AS after
FROM qsar_models.models
WHERE dataset_name = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3a modeling'
  AND details_text ~ '\"qsarReadyRuleSet\"\s*:\s*\"qsar-ready\"'
LIMIT 20;

UPDATE qsar_models.models
SET details_text = jsonb_set(
    details_text::jsonb,
    '{qsarReadyRuleSet}',
    to_jsonb('qsar-ready_04242025_0'::text)
)::text
WHERE dataset_name = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3a modeling'
  AND (details_text::jsonb ->> 'qsarReadyRuleSet') = 'qsar-ready';


SELECT *  from qsar_models.predictions
where fk_model_id =1643

  
SELECT 
                d.id,
                d.name,
                u.abbreviation_ccd,
                u2.abbreviation_ccd,
                d.dsstox_mapping_strategy,
                p.name_ccd,
                p.description
            from qsar_datasets.datasets d 
            LEFT JOIN qsar_datasets.units u ON d.fk_unit_id = u.id
            LEFT JOIN qsar_datasets.units u2 ON d.fk_unit_id_contributor = u2.id
            LEFT JOIN qsar_datasets.properties p ON d.fk_property_id = p.id
            where d.name = 'KOC v1 modeling';


select dp.canon_qsar_smiles, dp.qsar_exp_prop_property_values_id , dp.qsar_dtxcid  from qsar_datasets.data_points dp 
join qsar_datasets.datasets d  on dp.fk_dataset_id = d.id
where d.name='KOC v1 modeling';


SELECT
  dp.canon_qsar_smiles,
  TRIM(SPLIT_PART(dp.qsar_exp_prop_property_values_id, '|', 1)) AS qsar_exp_prop_property_values_id_first,
  TRIM(SPLIT_PART(dp.qsar_dtxcid, '|', 1)) AS qsar_dtxcid_first
FROM qsar_datasets.data_points AS dp
JOIN qsar_datasets.datasets AS d 
  ON dp.fk_dataset_id = d.id
WHERE d.name = 'KOC v1 modeling';



WITH filtered_dp AS (
  SELECT
    dp.canon_qsar_smiles,
    TRIM(SPLIT_PART(dp.qsar_exp_prop_property_values_id, '|', 1)) AS qsar_exp_prop_property_values_id_first,
    TRIM(SPLIT_PART(dp.qsar_dtxcid, '|', 1)) AS dtxcid
  FROM qsar_datasets.data_points AS dp
  JOIN qsar_datasets.datasets AS d
    ON dp.fk_dataset_id = d.id
  WHERE d.name = 'KOC v1 modeling' and dp.canon_qsar_smiles ='ClC1C=C(C=CC=1Cl)C1C(Cl)=C(Cl)C(Cl)=C(Cl)C=1Cl'
)
SELECT
  fdp.canon_qsar_smiles,
  fdp.qsar_exp_prop_property_values_id_first,
  fdp.dtxcid AS qsar_dtxcid_first,
  r.dtxsid,
  r.casrn,
  r.preferred_name,
  r.smiles,
  r.mol_weight
 FROM filtered_dp AS fdp
LEFT JOIN qsar_models.dsstox_records AS r
  ON r.dtxcid = fdp.dtxcid
 AND r.fk_dsstox_snapshot_id = 4;




SELECT 
            d.id,
            d.name as dataset_name,
            d.description as dataset_description, 
            u.abbreviation_ccd AS units_model,
            u2.abbreviation_ccd AS units_display,
            d.dsstox_mapping_strategy,
            p.name_ccd as property_name,
            p.description as property_description
        FROM qsar_datasets.datasets AS d
        LEFT JOIN qsar_datasets.units AS u ON d.fk_unit_id = u.id
        LEFT JOIN qsar_datasets.units AS u2 ON d.fk_unit_id_contributor = u2.id
        LEFT JOIN qsar_datasets.properties AS p ON d.fk_property_id = p.id
        where d.name='KOC v1 modeling';



SELECT 
            d.id,
            d.name as dataset_name,
            d.description as dataset_description, 
            u.abbreviation_ccd AS units_model,
            u2.abbreviation_ccd AS units_display,
            d.dsstox_mapping_strategy,
            p.name_ccd as property_name,
            p.description as property_description
        FROM qsar_datasets.datasets AS d
        LEFT JOIN qsar_datasets.units AS u ON d.fk_unit_id = u.id
        LEFT JOIN qsar_datasets.units AS u2 ON d.fk_unit_id_contributor = u2.id
        LEFT JOIN qsar_datasets.properties AS p ON d.fk_property_id = p.id
        where d.name = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling';



SELECT
  dr.dtxcid,
  dr.dtxsid,
  dr.smiles
FROM qsar_models.dsstox_records dr
WHERE dr.fk_dsstox_snapshot_id = 4
  AND NOT EXISTS (
    SELECT 1
    FROM qsar_models.predictions_dashboard pd
    JOIN qsar_models.models m
      ON m.id = pd.fk_model_id
    WHERE m.fk_source_id = (
            SELECT id
            FROM qsar_models.sources
            WHERE name = 'OPERA2.8'
          )
      AND pd.dtxcid = dr.dtxcid
  );


	select
	COUNT(*) as missing_count
	from qsar_models.dsstox_records dr
	where
		dr.fk_dsstox_snapshot_id = 4 
		and not exists (select	1 from qsar_models.predictions_dashboard pd
		join qsar_models.models m on
			m.id = pd.fk_model_id
		where
			m.fk_source_id = (select id	from qsar_models.sources where name = 'TEST5.1.3')
			and pd.dtxcid = dr.dtxcid);

	
select id, convert_from(pr.file_json, 'utf-8') as report_json  , convert_from(pr.file_html, 'utf-8') as report_html from qsar_models.prediction_reports pr 
limit 10;
	


SELECT
  c.table_name,
  c.column_name,
  c.data_type,
  c.udt_name,
  c.character_maximum_length,
  c.numeric_precision,
  c.numeric_scale,
  c.is_nullable,
  c.ordinal_position
FROM information_schema.columns c
WHERE c.table_schema = 'qsar_models' and c.udt_name ='varchar'
ORDER BY c.table_name, c.ordinal_position;
	

select m.name, ft.name, mf.created_at, ft.id from qsar_models.model_files mf
join qsar_models.file_types ft on ft.id=mf.fk_file_type_id 
join qsar_models.models m on m.id=mf.fk_model_id 
where mf.fk_model_id =1070
order by ft.id;


select m.* from qsar_models.models m 
join qsar_models.model_bytes mb on mb.fk_model_id =m.id;




select dp.canon_qsar_smiles, dp.qsar_property_value, p.qsar_predicted_value, p.fk_splitting_id from qsar_models.predictions p 
join qsar_datasets.data_points dp on dp.canon_qsar_smiles  = p.canon_qsar_smiles 
	on d.id = dp.fk_dataset_id
join qsar_models.models m
	on m.dataset_name = d.name
join qsar_models.predictions p
