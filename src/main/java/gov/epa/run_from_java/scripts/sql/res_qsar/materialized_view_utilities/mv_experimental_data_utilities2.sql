-- Get list of public sources
select distinct mv.public_source_name from mv_experimental_data mv where mv.public_source_name is not null;

-- Get counts by property:
select distinct mv.prop_name,count (mv.id) from mv_experimental_data mv
group by prop_name
order by prop_name;


-- Get sids by property:
select distinct mv.prop_name,count (distinct dtxsid) from mv_experimental_data mv
group by prop_name
order by prop_name;



--properties by public source
select mv.prop_name,mv.public_source_name , count (mv.id) from mv_experimental_data mv
where mv.public_source_name is not null
group by mv.public_source_name,mv.prop_name
order by mv.prop_name,mv.public_source_name;

--properties by lit source (chempprop literature sources)- TODO should have assigned public source to chemprop?
select mv.ls_name,  mv.prop_name, count (mv.id) from mv_experimental_data mv
where mv.public_source_name is null
group by mv.ls_name, mv.prop_name;


select * from mv_experimental_data
-- where public_source_name='ADDoPT';
where public_source_name='AqSolDB';


select * from mv_experimental_data mv
-- where mv.dtxsid='DTXSID3039242' ;--benzene
-- where mv.dtxsid='DTXSID7020182';--BPA
where mv.dtxsid='DTXSID0021965'  and exp_details_species_supercategory='Fish' ;


--counts by property for chemical
select prop_name,count(id) from mv_experimental_data mv
where mv.dtxsid='DTXSID3039242'--benzene
-- where mv.dtxsid='DTXSID7020182'--BPA
group by prop_name;

--counts of properties by chemical
select dtxsid,count(distinct prop_name) from mv_experimental_data mv
group by dtxsid
order by count(distinct prop_name) desc;


select ps.name, count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='Bioconcentration factor'
group by ps.name;

select count(distinct dtxcid) from mv_experimental_data
where prop_name='Henry''s Law Constant';