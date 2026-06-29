SELECT
       tv.toxval_type,
       c.dtxsid,
       c.casrn,
       c.name,
       tv.toxval_id,
       tv.source,
       tv.subsource,
       tv.toxval_type,
       tv.toxval_type_original,
       tv.toxval_subtype,
       tv.toxval_subtype_original,
       ttd.toxval_type_supercategory,
       tv.toxval_numeric_qualifier,
       tv.toxval_numeric_qualifier_original,
       tv.toxval_numeric,
       tv.toxval_numeric_original,
       tv.toxval_numeric_converted,
       tv.toxval_units,
       tv.toxval_units_original,
       tv.toxval_units_converted,
       tv.risk_assessment_class,
       tv.study_type,
       tv.study_type_original,
       tv.study_duration_class,
       tv.study_duration_class_original,
       tv.study_duration_value,
       tv.study_duration_value_original,
       tv.study_duration_units,
       tv.study_duration_units_original,
       tv.human_eco,
       tv.strain,
       tv.strain_original,
       tv.sex,
       tv.sex_original,
       tv.generation,
       s.species_id,
       tv.species_original,
       s.common_name,
       s.ecotox_group,
       s.habitat,
       tv.lifestage,
       tv.exposure_route,
       tv.exposure_route_original,
       tv.exposure_method,
       tv.exposure_method_original,
       tv.exposure_form,
       tv.exposure_form_original,
       tv.media,
       tv.media_original,
       tv.critical_effect,
       tv.year,
       tv.priority_id,
       tv.source_source_id,
       tv.details_text,
       tv.toxval_uuid,
       tv.toxval_hash,
       tv.datestamp,
       rs.long_ref,
       rs.url
FROM prod_toxval_v93.toxval tv
         INNER JOIN prod_toxval_v93.chemical c on c.dtxsid = tv.dtxsid
         LEFT JOIN prod_toxval_v93.species s on tv.species_id = s.species_id
         INNER JOIN prod_toxval_v93.toxval_type_dictionary ttd on tv.toxval_type = ttd.toxval_type
         LEFT JOIN prod_toxval_v93.record_source rs on rs.toxval_id = tv.toxval_id
WHERE s.common_name = 'fathead minnow'
  AND tv.toxval_type = 'LC50'
  AND tv.media in ('-', 'Fresh water')
  AND ttd.toxval_type_supercategory in ('Point of Departure', 'Toxicity Value', 'Lethality Effect Level')
  AND tv.toxval_numeric > 0



SELECT count(tv.toxval_id)
FROM prod_toxval_v93.toxval tv
         INNER JOIN prod_toxval_v93.chemical c on c.dtxsid = tv.dtxsid
         LEFT JOIN prod_toxval_v93.species s on tv.species_id = s.species_id
         INNER JOIN prod_toxval_v93.toxval_type_dictionary ttd on tv.toxval_type = ttd.toxval_type
WHERE s.common_name = 'fathead minnow'
  AND tv.toxval_type = 'LC50'
  AND tv.media in ('-', 'Fresh water')
  AND ttd.toxval_type_supercategory in ('Point of Departure', 'Toxicity Value', 'Lethality Effect Level')
  AND tv.toxval_numeric > 0;


SELECT count(distinct (c.dtxsid))
FROM prod_toxval_v93.toxval tv
         INNER JOIN prod_toxval_v93.chemical c on c.dtxsid = tv.dtxsid
         LEFT JOIN prod_toxval_v93.species s on tv.species_id = s.species_id
         INNER JOIN prod_toxval_v93.toxval_type_dictionary ttd on tv.toxval_type = ttd.toxval_type
WHERE s.common_name = 'fathead minnow'
  and tv.toxval_numeric_qualifier not like '>%' and tv.toxval_numeric_qualifier not like '<%'
  AND tv.toxval_type = 'LC50'
  AND tv.media in ('-', 'Fresh water')
  AND ttd.toxval_type_supercategory in ('Point of Departure', 'Toxicity Value', 'Lethality Effect Level')
  AND tv.toxval_numeric > 0;










SELECT
       tv.toxval_type,c.dtxsid,c.casrn,c.name,tv.toxval_id,tv.source,
       tv.subsource,
       tv.toxval_type,
       tv.toxval_type_original,
       tv.toxval_subtype,
       tv.toxval_subtype_original,
       ttd.toxval_type_supercategory,
       tv.toxval_numeric_qualifier,
       tv.toxval_numeric_qualifier_original,
       tv.toxval_numeric,
       tv.toxval_numeric_original,
       tv.toxval_numeric_converted,
       tv.toxval_units,
       tv.toxval_units_original,
       tv.toxval_units_converted,
       tv.risk_assessment_class,
       tv.study_type,
       tv.study_type_original,
       tv.study_duration_class,
       tv.study_duration_class_original,
       tv.study_duration_value,
       tv.study_duration_value_original,
       tv.study_duration_units,
       tv.study_duration_units_original,
       tv.human_eco,
       tv.strain,
       tv.strain_original,
       tv.sex,
       tv.sex_original,
       tv.generation,
       s.species_id,
       tv.species_original,
       s.common_name,
       s.ecotox_group,
       s.habitat,
       tv.lifestage,
       tv.exposure_route,
       tv.exposure_route_original,
       tv.exposure_method,
       tv.exposure_method_original,
       tv.exposure_form,
       tv.exposure_form_original,
       tv.media,
       tv.media_original,
       tv.critical_effect,
       tv.year,
       tv.priority_id,
       tv.source_source_id,
       tv.details_text,
       tv.toxval_uuid,
       tv.toxval_hash,
       tv.datestamp,
       rs.long_ref,
       rs.url
FROM prod_toxval_v93.toxval tv
         INNER JOIN prod_toxval_v93.chemical c on c.dtxsid = tv.dtxsid
         LEFT JOIN prod_toxval_v93.species s on tv.species_id = s.species_id
         INNER JOIN prod_toxval_v93.toxval_type_dictionary ttd on tv.toxval_type = ttd.toxval_type
         LEFT JOIN prod_toxval_v93.record_source rs on rs.toxval_id = tv.toxval_id
WHERE
tv.toxval_type = 'LD50'
  AND ttd.toxval_type_supercategory in ('Point of Departure', 'Toxicity Value', 'Lethality Effect Level')
  AND tv.toxval_numeric > 0
 AND exposure_route ='oral'
  AND  s.common_name='Rat'

select distinct(rs.document_name) from prod_toxval_v93.record_source rs


SELECT
#        count(tv.toxval_id)
       count(distinct (c.dtxsid))
FROM prod_toxval_v93.toxval tv
         INNER JOIN prod_toxval_v93.chemical c on c.dtxsid = tv.dtxsid
         LEFT JOIN prod_toxval_v93.species s on tv.species_id = s.species_id
         INNER JOIN prod_toxval_v93.toxval_type_dictionary ttd on tv.toxval_type = ttd.toxval_type
         LEFT JOIN prod_toxval_v93.record_source rs on rs.toxval_id = tv.toxval_id
WHERE
tv.toxval_type = 'LD50'
  AND ttd.toxval_type_supercategory in ('Point of Departure', 'Toxicity Value', 'Lethality Effect Level')
  AND tv.toxval_numeric > 0
 AND exposure_route ='oral'
  AND  s.common_name='Rat'
# and tv.source = 'ECOTOX'

select * from bcfbaf


select distinct * from toxval t where toxval_type='BCF'

select distinct source,subsource from prod_toxval_v93.toxval
where media like '%Fresh water%' and toxval_type='LC50' and species_id=1;

select count(toxval_id) from prod_toxval_v93.toxval
where media like '%Fresh water%' and toxval_type='LC50' and species_id=1 and source='ECOTOX';
# where toxval_type='LC50' and species_id=1 and source='ECOTOX';

select * from prod_toxval_v93.toxval
where media like '%Fresh water%' and toxval_type='LC50' and species_id=1 and source='ECOTOX';



# Aquatic tox query
SELECT c.dtxsid, c.casrn, c.name, tv.toxval_id, tv.source, tv.subsource, tv.toxval_type, tv.toxval_type_original, tv.toxval_subtype, tv.toxval_subtype_original, ttd.toxval_type_supercategory,
			tv.toxval_numeric_qualifier, tv.toxval_numeric_qualifier_original, tv.toxval_numeric, tv.toxval_numeric_original,
			tv.toxval_numeric_converted, tv.toxval_units, tv.toxval_units_original, tv.toxval_units_converted, tv.risk_assessment_class,
			tv.study_type, tv.study_type_original, tv.study_duration_class, tv.study_duration_class_original, tv.study_duration_value,
			tv.study_duration_value_original, tv.study_duration_units, tv.study_duration_units_original, tv.human_eco,
			tv.strain, tv.strain_original, tv.sex, tv.sex_original, tv.generation,
			s.species_id, tv.species_original, s.common_name, s.ecotox_group, s.habitat,
			tv.lifestage, tv.exposure_route, tv.exposure_route_original, tv.exposure_method, tv.exposure_method_original,
			tv.exposure_form, tv.exposure_form_original, tv.media, tv.media_original, tv.critical_effect, tv.year, tv.priority_id,
			tv.source_source_id, tv.details_text, tv.toxval_uuid, tv.toxval_hash, tv.datestamp,
			rs.quality, rs.document_name,rs.long_ref, rs.title,rs.author, rs.journal,rs.volume,rs.year, rs.url
			FROM prod_toxval_v93.toxval tv
			INNER JOIN prod_toxval_v93.chemical c on c.dtxsid=tv.dtxsid
			LEFT JOIN prod_toxval_v93.species s on tv.species_id=s.species_id
			INNER JOIN prod_toxval_v93.toxval_type_dictionary ttd on tv.toxval_type=ttd.toxval_type
			LEFT JOIN prod_toxval_v93.record_source rs on rs.toxval_id=tv.toxval_id
			WHERE s.common_name = 'Fathead minnow' AND
			tv.toxval_type = 'LC50' AND
			rs.quality not like '3%' AND rs.quality not like '4%' AND
--			tv.toxval_units in ('mg/L', 'g/L', 'mol/L') and
			tv.media in ('-', 'Fresh water') AND
# 			c.dtxsid='DTXSID9020459' and
			toxval_numeric_qualifier !='' and toxval_numeric_qualifier !='=' and
# 			tv.source!='ECOTOX'  and tv.source!='EnviroTox_v2' and
			ttd.toxval_type_supercategory in ('Point of Departure', 'Toxicity Value', 'Lethality Effect Level') AND
			tv.toxval_numeric > 0;