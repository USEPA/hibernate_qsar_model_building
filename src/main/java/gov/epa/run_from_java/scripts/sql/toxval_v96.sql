SELECT t.dtxsid,t.toxval_id,t.source,t.subsource,t.toxval_type,t.toxval_type_original,t.toxval_subtype,t.toxval_subtype_original,ttd.toxval_type_supercategory,
       t.toxval_numeric_qualifier,t.toxval_numeric_qualifier_original,t.toxval_numeric,t.toxval_numeric_original,
       t.toxval_units,t.toxval_units_original,t.risk_assessment_class,
       t.study_type,t.study_type_original,t.study_duration_class,t.study_duration_class_original,t.study_duration_value,
       t.study_duration_value_original,t.study_duration_units,t.study_duration_units_original,t.human_eco,
       t.strain,t.strain_original,t.sex,t.sex_original,t.generation,
       s.species_id,t.species_original,
       s.latin_name as species_scientific,s.common_name as species_common,s.ecotox_group as species_supercategory,
       t.lifestage,t.exposure_route,t.exposure_route_original,t.exposure_method,t.exposure_method_original,
       t.exposure_form,t.exposure_form_original,t.media,t.media_original,
       t.toxicological_effect as critical_effect,-- do we need new key words?
       t.year,t.priority_id,
       t.source_source_id,t.details_text,t.toxval_uuid,t.toxval_hash,t.datestamp
#        rs.long_ref,rs.title,rs.author,rs.journal,rs.volume,rs.issue,rs.url,rs.document_name,rs.record_source_type,rs.record_source_hash
FROM toxval t
LEFT JOIN species s on t.species_id= s.species_id
JOIN toxval_type_dictionary ttd on t.toxval_type=ttd.toxval_type
# JOIN record_source rs ON t.toxval_id=rs.toxval_id
WHERE
t.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND ttd.toxval_type_supercategory in ('Dose Response Summary Value','Toxicity Value','Mortality Response Summary Value')
AND t.toxval_numeric>0;


select count(distinct t.toxicological_effect)
FROM toxval t
LEFT JOIN species s on t.species_id= s.species_id
JOIN toxval_type_dictionary ttd on t.toxval_type=ttd.toxval_type
JOIN record_source rs ON t.toxval_id=rs.toxval_id
WHERE
t.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND ttd.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND t.toxval_numeric>0;


SELECT t.source, count(t.toxval_id)
FROM toxval t
LEFT JOIN species s on t.species_id= s.species_id
JOIN toxval_type_dictionary ttd on t.toxval_type=ttd.toxval_type
JOIN record_source rs ON t.toxval_id=rs.toxval_id
WHERE
t.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND ttd.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND t.toxval_numeric>0
group by t.source
order by t.source;



SELECT rs.toxval_id, rs.long_ref,rs.title,rs.author,rs.journal,rs.volume,rs.issue,rs.url,rs.document_name,rs.record_source_type
FROM toxval t
       JOIN toxval_type_dictionary ttd on t.toxval_type=ttd.toxval_type
       JOIN record_source rs ON t.toxval_id=rs.toxval_id
WHERE
t.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND ttd.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND t.toxval_numeric>0;


SELECT distinct  t.toxval_units
FROM toxval t;



SELECT t.dtxsid,t.toxval_id,t.source,t.subsource,t.toxval_type,t.toxval_type_original,t.toxval_subtype,t.toxval_subtype_original,ttd.toxval_type_supercategory,
       t.toxval_numeric_qualifier,t.toxval_numeric_qualifier_original,t.toxval_numeric,t.toxval_numeric_original,
       t.toxval_units,t.toxval_units_original,t.risk_assessment_class,
       t.study_type,t.study_type_original,t.study_duration_class,t.study_duration_class_original,t.study_duration_value,
       t.study_duration_value_original,t.study_duration_units,t.study_duration_units_original,t.human_eco,
       t.strain,t.strain_original,t.sex,t.sex_original,t.generation,
       s.species_id,t.species_original,
       s.latin_name as species_scientific,s.common_name as species_common,s.ecotox_group as species_supercategory,
       t.lifestage,t.exposure_route,t.exposure_route_original,t.exposure_method,t.exposure_method_original,
       t.exposure_form,t.exposure_form_original,t.media,t.media_original,
       t.toxicological_effect as critical_effect,
       t.year,t.priority_id,
       t.source_source_id,t.details_text,t.toxval_uuid,t.toxval_hash,t.datestamp,
       rs.long_ref,rs.title,rs.author,rs.journal,rs.volume,rs.issue,rs.url,rs.document_name,rs.record_source_type,rs.record_source_hash
FROM toxval t
LEFT JOIN species s on t.species_id= s.species_id
JOIN toxval_type_dictionary ttd on t.toxval_type=ttd.toxval_type
JOIN record_source rs ON t.toxval_id=rs.toxval_id
WHERE
t.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND ttd.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND t.toxval_numeric>0



SELECT distinct ttd.toxval_type_supercategory
FROM toxval t
JOIN toxval_type_dictionary ttd on t.toxval_type=ttd.toxval_type
WHERE
t.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND t.toxval_numeric>0;

# ('Dose Response Summary Value','Toxicity Value','Mortality Response Summary Value')
# Acute Exposure Guidelines
# Media Exposure Guidelines


SELECT chemical.dtxsid, chemical.casrn, chemical.name
From chemical;

select distinct b.species_supercategory  from bcfbaf b


SELECT * from cancer_summary t
join chemical c on c.dtxsid=t.dtxsid
where t.dtxsid!='NODTXSID'
order by t.dtxsid asc;