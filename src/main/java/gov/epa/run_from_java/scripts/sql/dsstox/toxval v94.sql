SELECT
a.dtxsid, a.casrn,a.name,
b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,
b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,
b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,
b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,
b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,
b.strain,b.strain_original,b.sex,b.sex_original,b.generation,
d.species_id,b.species_original,
d.common_name,d.ecotox_group,d.habitat,
b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,
b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.priority_id,
b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp

FROM `20230419_toxval_v94`.toxval b
INNER JOIN `20230419_toxval_v94`.chemical a on a.dtxsid=b.dtxsid
LEFT JOIN `20230419_toxval_v94`.species d on b.species_id=d.species_id
INNER JOIN `20230419_toxval_v94`.toxval_type_dictionary e on b.toxval_type=e.toxval_type
WHERE
b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND b.toxval_numeric>0
# AND b.source='ECOTOX';
;


# SELECT count(b.toxval_id)
# FROM `20230419_toxval_v94`.toxval b
# INNER JOIN `20230419_toxval_v94`.chemical a on a.dtxsid=b.dtxsid
# INNER JOIN `20230419_toxval_v94`.toxval_type_dictionary e on b.toxval_type=e.toxval_type
# WHERE
# b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
# AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
# AND b.toxval_numeric>0
# # AND b.source='ECOTOX';
# ;


SELECT t.source, count(t.toxval_id)
FROM `20230419_toxval_v94`.toxval t
LEFT JOIN `20230419_toxval_v94`.species s on t.species_id= s.species_id
JOIN `20230419_toxval_v94`.toxval_type_dictionary ttd on t.toxval_type=ttd.toxval_type
JOIN `20230419_toxval_v94`.record_source rs ON t.toxval_id=rs.toxval_id
WHERE
t.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND ttd.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND t.toxval_numeric>0
group by t.source;

select distinct ecotox_group from species

select * from species s where s.ecotox_group='Algae' or s.ecotox_group='Fish' or s.ecotox_group='Crustaceans'

select distinct human_eco from toxval

select distinct source from toxval;

select distinct toxval_type from toxval where source like '%ECHA%' order by toxval_type

select count(bcfbaf_id)  from bcfbaf b where b.dtxsid is not null


SELECT distinct(b.dtxsid)
FROM bcfbaf b
left JOIN chemical c ON b.dtxsid = c.dtxsid
where c.dtxsid is null;


SELECT distinct(cs.dtxsid)
FROM cancer_summary cs
left JOIN chemical c ON cs.dtxsid = c.dtxsid
where c.dtxsid is null;

SELECT count(distinct(gs.dtxsid))
FROM genetox_summary gs
left JOIN chemical c ON gs.dtxsid = c.dtxsid
where c.dtxsid is null;

select distinct(gs.genetox_call) from genetox_summary gs

select count(distinct (genetox_summary_id)) from genetox_summary gs

SELECT count(bcfbaf.bcfbaf_id) from bcfbaf;

select count(dtxsid) from chemical


SELECT b.dtxsid, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,
b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,
b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,
b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,
b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,
b.strain,b.strain_original,b.sex,b.sex_original,b.generation,
d.species_id,b.species_original,
d.latin_name as species_scientific, d.common_name as species_common,d.ecotox_group as species_supercategory,
b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,
b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.priority_id,
b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp

FROM toxval b
LEFT JOIN species d on b.species_id=d.species_id
INNER JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type
WHERE
b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND b.toxval_numeric>0
AND b.dtxsid="DTXSID3039242";


-- BCF whole body
select distinct b.bcfbaf_id, casrn,c.name, b.dtxsid, logbcf, units, tissue, calc_method, comments, b.water_conc, exposure_type, exposure_duration,  media, temperature, pH, b.species_common, author,title, b.year,b.journal from bcfbaf b
join chemical c on c.dtxsid=b.dtxsid
join species s on s.common_name=b.species_common
where s.ecotox_group like '%fish%' and b.logbcf is not null and b.tissue='Whole body'
# and c.dtxsid='DTXSID3025382'
# and logbcf>3.1227 and logbcf< 3.12276
order by dtxsid, logbcf;



SELECT count(b.bcfbaf_id)
FROM bcfbaf b
left JOIN chemical c on c.dtxsid = b.dtxsid



SELECT * FROM toxval b
WHERE b.dtxsid="DTXSID3039242";


SELECT count(toxval_id) FROM toxval


select count(distinct(gs.dtxsid)) from genetox_summary gs;


select count(toxval_id) from toxval where source='ECHA IUCLID' and human_eco!='human health' ;

select distinct(s.ecotox_group) from toxval t
join species s on t.species_id=s.species_id
where t.human_eco!='human health' and (t.toxval_type='NOAEL' or t.toxval_type='LOAEL');


select count(dtxsid) from models

select distinct source,source_url from toxval



select *
from genetox_details gd
left join chemical c on gd.dtxsid=c.dtxsid;


select *
from genetox_summary gd
left join chemical c on gd.dtxsid=c.dtxsid;



select distinct b.bcfbaf_id, casrn,c.name, b.dtxsid, logbcf, units, tissue, calc_method, comments, b.water_conc, exposure_type, exposure_duration,  media, temperature, pH, b.species_common, author,title, b.year,b.journal from `20230419_toxval_v94`.bcfbaf b
    join `20230419_toxval_v94`.chemical c on c.dtxsid=b.dtxsid
	join `20230419_toxval_v94`.species s on s.common_name=b.species_common
	where s.ecotox_group like '%fish%' and b.logbcf is not null and b.tissue='Whole body'
	order by dtxsid, logbcf;


select distinct b.species_scientific, s.species_supercategory,b.tissue, b.bcfbaf_id, casrn,c.name, b.dtxsid, logbcf, units, tissue, calc_method, comments, b.water_conc, exposure_type, exposure_duration,  media, temperature, pH, b.species_common, author,title, b.year,b.journal from prod_toxval.bcfbaf b
    join prod_toxval.chemical c on c.dtxsid=b.dtxsid
	join prod_toxval.species s on s.species_common=b.species_common and s.species_scientific=b.species_scientific
	where s.species_supercategory like '%fish%' and b.logbcf is not null and b.tissue like '%Whole body%'
	order by dtxsid, logbcf;


select distinct count(distinct (b.dtxsid)) from prod_toxval.bcfbaf b
	join prod_toxval.species s on s.species_common=b.species_common
	where s.species_supercategory like '%fish%' and b.logbcf is not null and b.tissue='Whole body';


select count((b.dtxsid)) from prod_toxval.bcfbaf b
	join prod_toxval.species s on s.species_common=b.species_common
	where s.species_supercategory like '%fish%' and b.logbcf is not null and b.tissue='Whole body';



select count(distinct (b.dtxsid)) from `20230419_toxval_v94`.bcfbaf b
	join `20230419_toxval_v94`.species s on s.common_name=b.species_common
	where s.ecotox_group like '%fish%' and b.logbcf is not null and b.tissue='Whole body';



select count(b.dtxsid) from `20230419_toxval_v94`.bcfbaf b
	join `20230419_toxval_v94`.species s on s.common_name=b.species_common
	where s.ecotox_group like '%fish%' and b.logbcf is not null and b.tissue='Whole body';


SELECT models.dtxsid, models.model_id, models.chemical_id, models.model, models.metric, models.value, models.units, models.qualifier
From models where model!='OPERA'


select count(distinct t.critical_effect)
FROM `20230419_toxval_v94`.toxval t
LEFT JOIN `20230419_toxval_v94`.species s on t.species_id= s.species_id
JOIN `20230419_toxval_v94`.toxval_type_dictionary ttd on t.toxval_type=ttd.toxval_type
WHERE
t.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND ttd.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND t.toxval_numeric>0;



SELECT b.dtxsid, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,
b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,
b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,
b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,
b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,
b.strain,b.strain_original,b.sex,b.sex_original,b.generation,
d.species_id,b.species_original,
d.latin_name as species_scientific, d.species_common, d.species_supercategory,
b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,
b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.priority_id,
b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp,
c.long_ref, c.title, c.author, c.journal, c.volume, c.issue, c.url, c.document_name, c.record_source_type, c.record_source_hash

FROM toxval b
LEFT JOIN species d on b.species_id=d.species_id
JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type
JOIN record_source c ON b.toxval_id=c.toxval_id
WHERE
b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND b.toxval_numeric>0;

Delete from HazardRecords where sourceTable='toxval';


select * from HazardRecords hr
join toxval t on hr.dtxsid = t.dtxsid
where sourceOriginal='EFSA' and hazardName='Systemic Toxicity Repeat Exposure'
order by dtxsid;


SELECT b.dtxsid, b.toxval_id, b.source,b.subsource,b.toxval_type,b.toxval_type_original,b.toxval_subtype,b.toxval_subtype_original,e.toxval_type_supercategory,
b.toxval_numeric_qualifier,b.toxval_numeric_qualifier_original,b.toxval_numeric,b.toxval_numeric_original,
b.toxval_numeric_converted, b.toxval_units,b.toxval_units_original,b.toxval_units_converted, b.risk_assessment_class,
b.study_type,b.study_type_original,b.study_duration_class,b.study_duration_class_original, b.study_duration_value,
b.study_duration_value_original,b.study_duration_units,b.study_duration_units_original,b.human_eco,
b.strain,b.strain_original,b.sex,b.sex_original,b.generation,
d.species_id,b.species_original,
d.latin_name as species_scientific, d.species_common, d.species_supercategory,
b.lifestage,b.exposure_route,b.exposure_route_original,b.exposure_method,b.exposure_method_original,
b.exposure_form,b.exposure_form_original, b.media,b.media_original,b.critical_effect,b.year,b.priority_id,
b.source_source_id,b.details_text,b.toxval_uuid,b.toxval_hash,b.datestamp,

FROM toxval b
LEFT JOIN species d on b.species_id=d.species_id
JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type
WHERE
toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND toxval_numeric>0

select distinct (toxval_type) from toxval_complete;

select count(distinct (toxval_id)) from toxval_complete;

select count(toxval_id) from toxval_complete;


SELECT toxval_id, count(toxval_id)

FROM toxval b
LEFT JOIN species d on b.species_id=d.species_id
JOIN toxval_type_dictionary e on b.toxval_type=e.toxval_type
WHERE
toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
AND toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND toxval_numeric>0
group by toxval_id



select distinct toxval_type from HazardRecords hr
join toxval_complete t on hr.toxvalID=t.toxval_id;


select  hazardName,sourceTable,sourceOriginal,t.datestamp from HazardRecords hr
join toxval_complete t on hr.toxvalID=t.toxval_id
where t.toxval_type='NOEC'-- ==> ECOTOX




SELECT count(b.toxval_id)
FROM toxval b
LEFT JOIN species d on b.species_id=d.species_id
WHERE
b.toxval_units in ('mg/kg-day','mg/kg','(mg/kg-day)-1','mg/L','mg/m3','(mg/m3)-1','mg/L','(ug/m3)-1','(g/m3)-1','(mg/L)-1')
-- AND e.toxval_type_supercategory in ('Point of Departure','Toxicity Value','Lethality Effect Level')
AND (toxval_type like '%cancer slope factor%' or toxval_type like '%cancer unit risk%' or
     toxval_type like '%LC50%' or toxval_type like '%LD50%' or
    toxval_type like '%NOAEL%' or toxval_type like '%LOAEL%')
  AND b.toxval_numeric>0;


select distinct toxval_type,source from toxval_complete
where toxval_type like '%LC50%'

select distinct toxval_type from toxval_complete;


select * from main.toxval_complete t
join chemical c on t.dtxsid = c.dtxsid
and toxval_type='LC50'



select t.source,count(distinct (t.dtxsid)) from main.toxval t
join species s on s.species_id=t.species_id
where toxval_type='LC50' and ecotox_group='Fish'
group by t.source

