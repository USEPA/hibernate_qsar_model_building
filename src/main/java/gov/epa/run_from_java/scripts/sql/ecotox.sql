-- Look at different conc types

select c.cas_number,c.chemical_name, t.test_id,conc1_mean, conc2_mean, ctc1.description as conc1type,ctc2.description as conc2type, conc1_unit from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
left join concentration_type_codes ctc1 on ctc1.code=conc1_type
left join concentration_type_codes ctc2 on ctc2.code=conc2_type
                       join exposure_type_codes etc on exposure_type=etc.code
where t.species_number=1 and media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
-- and conc1_mean is not null and conc1_mean_op !='>' and conc1_mean_op !='<'
and (measurement like '%MORT%' or measurement like '%SURV%')
and ((r.obs_duration_mean=96 and r.obs_duration_unit='h')  or (r.obs_duration_mean=4 and r.obs_duration_unit='d'))
-- and conc2_mean is not null and conc1_mean_op ='' and conc2_mean_op='' and conc1_unit=conc2_unit
-- and c.dtxsid='DTXSID20883276'
-- and t.test_id='1025982'
and conc1_mean is not null and conc2_mean is not null
order by cas_number
;



select distinct ctc1.code, ctc1.description, count(ctc1.code) from tests t
join results r on t.test_id=r.test_id
left join concentration_type_codes ctc1 on ctc1.code=conc1_type
-- where t.species_number=1 and media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
-- where t.species_number=2 and media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
-- where t.species_number=4 and media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
where t.species_number=5 and media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
and conc1_mean is not null and (conc1_mean_op ='~' or conc1_mean_op ='')
and measurement like '%MORT%'
-- and ((r.obs_duration_mean=96 and r.obs_duration_unit='h')  or (r.obs_duration_mean=4 and r.obs_duration_unit='d'))
and ((r.obs_duration_mean=48 and r.obs_duration_unit='h')  or (r.obs_duration_mean=2 and r.obs_duration_unit='d'))
group by ctc1.code;



-- Get chemical count by species for 96 hour duration
select s.species_number, s.common_name, count(distinct c.dtxsid) from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
-- join references_ r2 on r2.reference_number=t.reference_number
join species s on t.species_number = s.species_number
where media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
and conc1_mean is not null and conc1_mean_op !='>' and conc1_mean_op !='<'
and (measurement like '%MORT%')
and ((r.obs_duration_mean=96 and r.obs_duration_unit='h')  or (r.obs_duration_mean=4 and r.obs_duration_unit='d'))
and s.ecotox_group like '%Fish%'
-- and s.class='Actinopterygii' and exposure_type in ('F','S','R')
-- group by s.species_number, s.common_name,t.exposure_type
group by s.species_number, s.common_name
order by count(distinct c.dtxsid) desc
;

select count(distinct c.dtxsid) from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
join species s on t.species_number = s.species_number
where media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
and conc1_mean is not null and conc1_mean_op !='>' and conc1_mean_op !='<'
and (measurement like '%MORT%')
and ((r.obs_duration_mean=96 and r.obs_duration_unit='h')  or (r.obs_duration_mean=4 and r.obs_duration_unit='d'))
-- and ((r.obs_duration_mean<168 and r.obs_duration_unit='h')  or (r.obs_duration_mean<7 and r.obs_duration_unit='d'))
-- and (s.common_name='Fathead Minnow' or s.common_name='Rainbow Trout' or s.common_name='Bluegill')
and (s.common_name='Fathead Minnow' or s.common_name='Rainbow Trout' or s.common_name='Bluegill'
         or s.common_name='Channel Catfish' or s.common_name='Common Carp' or s.common_name='Goldfish'
    or s.common_name='Guppy' or s.common_name='Zebra Danio'
    or s.common_name='Western Mosquitofish'
    or s.common_name='Goldfish' or s.common_name='Silver Salmon'
         or s.common_name='Japanese Medaka'
    or s.common_name='Largemouth Bass'
	)
-- and s.ecotox_group like '%Fish%'
;

-- Species	unique dtxsid
-- All fish	2476
-- Top3	2014
-- Top 8	2239
-- Top 11 (>100 dtxsids)	2336


select * from species s
where common_name in ('Bluegill','Channel Catfish','Common Carp','Fathead Minnow' ,'Rainbow Trout','Goldfish','Guppy',
                      'Japanese Medaka','Silver Salmon','Western Mosquitofish','Zebra Danio')
order by common_name;


-- Get chemical count by species  for 48 hour duration
select s.species_number, s.common_name, count(distinct c.dtxsid) from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
join species s on t.species_number = s.species_number
where media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
and conc1_mean is not null and conc1_mean_op !='>' and conc1_mean_op !='<'
and (measurement like '%MORT%')
and ((r.obs_duration_mean=48 and r.obs_duration_unit='h')  or (r.obs_duration_mean=2 and r.obs_duration_unit='d'))
group by s.species_number, s.common_name
order by count(distinct c.dtxsid) desc
;


select * from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
join species s on t.species_number = s.species_number
where media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
and conc1_mean is not null and conc1_mean_op !='>' and conc1_mean_op !='<'
and (measurement like '%MORT%')
-- and c.cas_number=50293
and ((r.obs_duration_mean=48 and r.obs_duration_unit='h')  or (r.obs_duration_mean=2 and r.obs_duration_unit='d'))
and s.species_number=5;




-- select distinct ctc1.description as conc1type,ctc2.description as conc2type from tests t
select ctc1.description, count(ctc1.description)   from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
left join concentration_type_codes ctc1 on ctc1.code=conc1_type
left join concentration_type_codes ctc2 on ctc2.code=conc2_type
where t.species_number=1 and media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
-- and conc1_mean is not null and conc1_mean_op !='>' and conc1_mean_op !='<'
and (measurement like '%MORT%' or measurement like '%SURV%')
and ((r.obs_duration_mean=96 and r.obs_duration_unit='h')  or (r.obs_duration_mean=4 and r.obs_duration_unit='d'))
group by ctc1.description
order by count(ctc1.description) desc
-- and conc2_mean is not null and conc1_mean_op ='' and conc2_mean_op='' and conc1_unit=conc2_unit
-- and c.dtxsid='DTXSID20883276'
;


select distinct exposure_type from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
where t.species_number=1 and media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
-- and conc1_mean is not null and conc1_mean_op !='>' and conc1_mean_op !='<'
and (measurement like '%MORT%' or measurement like '%SURV%')
and ((r.obs_duration_mean=96 and r.obs_duration_unit='h')  or (r.obs_duration_mean=4 and r.obs_duration_unit='d'))
;

select distinct exposure_type from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
where t.species_number=1 and media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
-- and conc1_mean is not null and conc1_mean_op !='>' and conc1_mean_op !='<'
and (measurement like '%MORT%' or measurement like '%SURV%')
and ((r.obs_duration_mean=96 and r.obs_duration_unit='h')  or (r.obs_duration_mean=4 and r.obs_duration_unit='d'))
;


select distinct conc1_type from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
where t.species_number=1 and media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%'
-- and conc1_mean is not null and conc1_mean_op !='>' and conc1_mean_op !='<'
and (measurement like '%MORT%' or measurement like '%SURV%')
and ((r.obs_duration_mean=96 and r.obs_duration_unit='h')  or (r.obs_duration_mean=4 and r.obs_duration_unit='d'))
;


-- Get fhm data
select * from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
where t.species_number=1 and
media_type like '%FW%' and test_location like '%LAB%' and endpoint like '%LC50%' and measurement like '%MORT%';


-- Get bcf data
-- select c.cas_number, s.common_name,s.latin_name, bcf1_mean_op, bcf1_mean, rsc.description, s.ecotox_group from tests t
select distinct  s.ecotox_group from tests t
    join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
join species s on t.species_number=s.species_number
left join response_site_codes rsc on rsc.code=r.response_site
-- where bcf1_mean is not null and media_type like '%FW%' and test_location like '%LAB%';
where bcf1_mean is not null and
--       cas_number=71432 and
      s.ecotox_group not like '%Flowers%' and s.ecotox_group not like '%Moss%' and
--       s.ecotox_group not like '%Algae%' and
--       r.response_site='WO' and
      (bcf1_mean_op ='~' or bcf1_mean_op='')
order by s.ecotox_group;

select bcf1_mean_op, bcf1_mean, s.common_name,s.ecotox_group, r.response_site from tests t
    join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join species s on t.species_number=s.species_number
left join response_site_codes rsc on rsc.code=r.response_site
-- where bcf1_mean is not null and media_type like '%FW%' and test_location like '%LAB%';
where bcf1_mean is not null  and   dtxsid='DTXSID3025382';



-- BCF
select  dtxsid, cas_number, chemical_name, bcf1_mean ,bcf1_unit,
        s.latin_name,s.ecotox_group, rsc.description as 'response_site',r.response_site as code,
        author, publication_year, title,source from tests t
    join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
left join references_ r2 on r2.reference_number=t.reference_number
left join species s on t.species_number=s.species_number
left join response_site_codes rsc on rsc.code=r.response_site
-- where bcf1_mean is not null and media_type like '%FW%' and test_location like '%LAB%';
where bcf1_mean is not null and (bcf1_mean_op ='~' or bcf1_mean_op='')
--        and s.ecotox_group like '%Fish%'
--       and s.ecotox_group like '%Standard Test Species%'
--       cas_number=72208 and
--       s.ecotox_group not like '%Flowers%' and s.ecotox_group not like '%Moss%' and
      and dtxsid='DTXSID7023801'
--       s.ecotox_group not like '%Algae%' and
--       r.response_site='WO' and
order by cas_number;


-- whole body standard fish species BCF data
-- select distinct  cas_number, dtxsid,s.common_name,  r.response_site,s.ecotox_group from tests t
-- select distinct  s.common_name, s.ecotox_group from tests t
-- select count(distinct dtxsid) from tests t
    select count(distinct r.test_id) from tests t
    --     select distinct  count(r.test_id) from tests t
-- select cas_number,bcf1_mean ,bcf1_unit,s.ecotox_group,s.common_name, rsc.description from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
left join references_ r2 on r2.reference_number=t.reference_number
join species s on t.species_number=s.species_number
left join response_site_codes rsc on rsc.code=r.response_site
-- where bcf1_mean is not null and media_type like '%FW%' and test_location like '%LAB%';
where r.bcf1_mean is not null and (bcf1_mean_op ='~' or bcf1_mean_op='')
--       and s.ecotox_group like '%Fish%'
--       and s.ecotox_group like '%Standard Test Species%'
--       and bcf1_unit ='L/kg'
--    and (rsc.description='Whole organism' or  rsc.description='Wall, body')
--   and rsc.description='Whole organism'
--     and dtxsid='DTXSID3025382'
-- order by cas_number;
;



select s.latin_name,s.ecotox_group from species s


-- BCF
select  bcf1_mean ,bcf1_unit,conc1_mean,conc1_unit,conc1_min,conc1_max
from tests t
    join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
left join references_ r2 on r2.reference_number=t.reference_number
left join species s on t.species_number=s.species_number
left join response_site_codes rsc on rsc.code=r.response_site
-- where bcf1_mean is not null and media_type like '%FW%' and test_location like '%LAB%';
where bcf1_mean is not null and (bcf1_mean_op ='~' or bcf1_mean_op='') and bcf1_unit !='L/kg' and bcf1_unit!='RA'
and conc1_mean is null
-- and dtxsid='DTXSID60881236'
order by cas_number;



select  t.test_radiolabel,t.test_radiolabel_comments,  t.test_id, dtxsid, cas_number, chemical_name, bcf1_mean ,bcf1_unit,
        r.obs_duration_mean, r.obs_duration_unit,r.obs_duration_comments,
        t.study_duration_mean, t.study_duration_unit,
 conc1_mean_op, conc1_mean, conc1_unit, conc1_min, conc1_max, conc1_min_op, conc1_max_op,media_type, test_location, exposure_type,chem_analysis_method, s.common_name, s.latin_name,s.ecotox_group, rsc.description as 'response_site',
 author, publication_year, title,source from tests t
	join results r on t.test_id=r.test_id
	join chemicals c on c.cas_number=t.test_cas
	left join references_ r2 on r2.reference_number=t.reference_number
	left join species s on t.species_number=s.species_number
	left join response_site_codes rsc on rsc.code=r.response_site
	where bcf1_mean is not null and (bcf1_mean_op ='~' or bcf1_mean_op='')
-- 	and test_radiolabel_comments !='/' and test_radiolabel_comments !=''
-- 	and publication_year>2006
	order by cas_number
;


select  r.additional_comments,s.common_name from tests t
	join results r on t.test_id=r.test_id
	join chemicals c on c.cas_number=t.test_cas
	left join species s on t.species_number=s.species_number
	where bcf1_mean is not null
	and c.cas_number='50328' and s.common_name='Bluegill';


select  distinct r.additional_comments from tests t
	join results r on t.test_id=r.test_id
	join chemicals c on c.cas_number=t.test_cas
	left join species s on t.species_number=s.species_number
--  where bcf1_mean is not null ;
where bcf1_mean is not null and
    (r.additional_comments like '%CONC/%');
-- 	(r.additional_comments like '%KINETIC%' or r.additional_comments like '%K1/K2%')




select t.test_id, r.result_id, conc1_mean, endpoint,common_name,obs_duration_mean, exposure_duration_mean, study_duration_mean
from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
join references_ r2 on r2.reference_number=t.reference_number
left join species s on t.species_number=s.species_number
where media_type like '%FW%' and test_location like '%LAB%' and
(endpoint like '%LC50%' or endpoint like '%EC50%') and
measurement like '%MORT%'
and t.test_id=1029162;
;


CREATE INDEX if not exists chemicals_cas_number ON chemicals (cas_number);
CREATE INDEX if not exists tests_test_id ON tests (test_id);
CREATE INDEX if not exists tests_species_number ON tests (species_number);
CREATE INDEX if not exists results_test_id ON results (test_id);
CREATE INDEX if not exists tests_reference_number ON tests (reference_number);
CREATE INDEX if not exists references_reference_number ON references_ (reference_number);


select  endpoint, bcf1_mean, t.test_id, dtxsid, cas_number, chemical_name, bcf1_mean ,bcf1_unit,
conc1_mean_op, conc1_mean, conc1_unit, conc1_min, conc1_max, conc1_min_op, conc1_max_op,
exposure_duration_mean_op,	exposure_duration_mean,exposure_duration_unit,
media_type, test_location, exposure_type,chem_analysis_method, s.common_name, s.latin_name,s.ecotox_group, rsc.description as 'response_site',
author, publication_year, title,source from tests t
join results r on t.test_id=r.test_id
join chemicals c on c.cas_number=t.test_cas
left join references_ r2 on r2.reference_number=t.reference_number
left join species s on t.species_number=s.species_number
left join response_site_codes rsc on rsc.code=r.response_site
where bcf1_mean is not null and (endpoint='BCF' or endpoint='BAF')
and dtxsid='DTXSID2035013'
;


select  r.endpoint,  t.test_id, dtxsid, cas_number, chemical_name, bcf1_mean ,bcf1_unit,
 conc1_mean_op, conc1_mean, conc1_unit, conc1_min, conc1_max, conc1_min_op, conc1_max_op,exposure_duration_mean_op,	exposure_duration_mean,exposure_duration_unit,media_type, test_location, exposure_type,chem_analysis_method, s.common_name, s.latin_name,s.ecotox_group, rsc.description as 'response_site',
 author, publication_year, title,source from tests t
	join results r on t.test_id=r.test_id
	join chemicals c on c.cas_number=t.test_cas
	left join references_ r2 on r2.reference_number=t.reference_number
	left join species s on t.species_number=s.species_number
	left join response_site_codes rsc on rsc.code=r.response_site
	where bcf1_mean is not null
	order by cas_number;


select  dtxsid,cas_number, bcf1_mean, obs_duration_mean,exposure_duration_mean, exposure_duration_unit from tests t
	join results r on t.test_id=r.test_id
	join chemicals c on c.cas_number=t.test_cas
	left join references_ r2 on r2.reference_number=t.reference_number
	left join species s on t.species_number=s.species_number
	left join response_site_codes rsc on rsc.code=r.response_site
	where bcf1_mean is not null and endpoint = 'BCF' and c.cas_number='7783008';


select * from tests t
    join results r on t.test_id=r.test_id
	join chemicals c on c.cas_number=t.test_cas
	join references_ r2 on r2.reference_number=t.reference_number
	left join species s on t.species_number=s.species_number
	where media_type like '%FW%' and test_location like '%LAB%' and
	(endpoint like '%LOEC%' or endpoint like '%NOEC%');