-- What properties are in mv:
select distinct prop_name from mv_experimental_data mv
where prop_category='Env. Fate/transport' order by prop_name;

-----------------------------------------------------------------------------------------------------------
-- Properties that are available to export to dashboard so far:
select p.name as property,d.name as dataset from qsar_datasets.datasets_in_dashboard did
join qsar_datasets.properties p on did.fk_property_id = p.id
join qsar_datasets.datasets d on did.fk_datasets_id = d.id;

-----------------------------------------------------------------------------------------------------------
-- get data for list of chemicals
select * from mv_experimental_data where dtxsid in ('DTXSID00192353','DTXSID6067331','DTXSID30891564','DTXSID6062599',
'DTXSID90868151','DTXSID8031863','DTXSID8031865','DTXSID1037303','DTXSID8047553','DTXSID60663110','DTXSID70191136',
'DTXSID3037709','DTXSID3059921','DTXSID3031860','DTXSID8037706','DTXSID8059920','DTXSID3031862','DTXSID30382063',
'DTXSID00379268','DTXSID20874028','DTXSID3037707') and prop_name='LogKow: Octanol-Water';

select *
from public."mv_experimental_data" ved
where dtxsid in ('DTXSID1037303', 'DTXSID3031862',
                 'DTXSID4059916',
                 'DTXSID5044572',
                 'DTXSID6062599')
order by dtxsid, ved.prop_name;


-----------------------------------------------------------------------------------------------------------

-- See which properties have null unit abbreviations:
select distinct p.name_ccd
from qsar_datasets.data_points dp
         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
         join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.datasets_in_dashboard did on did.fk_property_id = d.fk_property_id
         join qsar_datasets.units u on u.id = d.fk_unit_id_contributor
where d.id = did.fk_datasets_id and keep=true
and u.abbreviation_ccd is null
order by p.name_ccd;

-----------------------------------------------------------------------------------------------------------

-- Data versioning scheme
-- . Data owner could use
--     · Calendar versioning (CalVer)
--     · 2024.1 Q1 of 2024
-- . Semantic Versioning (SemVer) - Major.Minor.Patch
--     . major -- when you are changing the structural changes
--     . minor -- when you are just loaded new data
--     . patch -- fixes in current version of data.



-- Record counts
SELECT reltuples AS estimate FROM pg_class where relname = 'v_experimental_data';


select  * from qsar_datasets.datasets_in_dashboard did
join qsar_datasets.properties p on did.fk_property_id = p.id;



SELECT public_source_name,public_source_url, COUNT(dtxsid)
FROM public."mv_experimental_data" ved
GROUP BY public_source_name,public_source_url
order by count(dtxsid) desc;


select count(*) from "mv_experimental_data";  ---- 16173839 for percepta
select count(distinct (dtxsid)) from "mv_experimental_data";



select p.name,pc.name,p.id as prop_id,pc.id as cat_id from qsar_datasets.properties p
left join qsar_datasets.properties_in_categories pic on p.id = pic.fk_property_id
left join qsar_datasets.property_categories pc on pic.fk_property_category_id = pc.id
order by p.name;



select distinct  p.name
from qsar_datasets.data_points dp
         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
         join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.datasets_in_dashboard did on did.fk_property_id = d.fk_property_id
where d.id = did.fk_datasets_id and keep=true
order by p.name;



-- Get counts by public source
select p.name, ps.name,count(dp.id)
from qsar_datasets.data_points dp
         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
         join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id
         left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.datasets_in_dashboard did on did.fk_property_id = d.fk_property_id
where d.id = did.fk_datasets_id and keep=true
group by ps.name,p.name
order by p.name,ps.name;

select * from qsar_datasets.datasets_in_dashboard did
join qsar_datasets.properties p on did.fk_property_id = p.id;


-- Get datapoints by public source
select p.name as property_name, ps.name as public_source_name,count(pv.id)
from exp_prop.property_values pv
         left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
         join exp_prop.properties p on pv.fk_property_id = p.id
where  keep=true and (pv.value_qualifier is null or pv.value_qualifier='~')
-- and ps.name like 'OPERA%'
group by ps.name,p.name
order by p.name,ps.name;


-- Get counts by literature source that dont have a public source
select p.name as prop_name, ls.name as lit_source,count(dp.id)
from qsar_datasets.data_points dp
         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
         join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id
         left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
         left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.datasets_in_dashboard did on did.fk_property_id = d.fk_property_id
where d.id = did.fk_datasets_id and keep=true and ps.name is null
group by ls.name,p.name
order by p.name,ls.name;




-- Get datapoints by literature source
select p.name as property_name, ls.name as literature_source_name,count(pv.id)
from exp_prop.property_values pv
         left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
         left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
         join exp_prop.properties p on pv.fk_property_id = p.id
where  keep=true and (pv.value_qualifier is null or pv.value_qualifier='~') and ps.name is null
group by ls.name,p.name
order by p.name,ls.name;


-- count of exp records
select count (dpc.id)
from qsar_datasets.data_points dp
         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
         join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.datasets_in_dashboard did on did.fk_property_id = d.fk_property_id
         join qsar_datasets.units u on u.id = d.fk_unit_id_contributor
where d.id = did.fk_datasets_id and keep=true;

--   and (ps.name is null or (ps.name!='eChemPortalAPI' and ps.name!='PubChem'
--       and ps.name!='OFMPub'))
--  and ps.name ='PubChem'
-- and pv.value_text is not null and dpc.property_value is null
-- and p.name_ccd ='Vapor Pressure'
-- and dtxsid='DTXSID3046613'
-- and dtxsid='DTXSID7020182'


    -- Get total count
select count(dp.id)
from qsar_datasets.data_points dp
         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
         join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id
         left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.datasets_in_dashboard did on did.fk_property_id = d.fk_property_id
where d.id = did.fk_datasets_id and keep=true;


select distinct source_name,source_description from mv_experimental_data order by source_name;

select d.id, p.id, d.name,p.name_ccd from qsar_datasets.datasets d
join qsar_datasets.properties p on d.fk_property_id = p.id
order by p.name_ccd;


-- determine which properties might be missing
select distinct p.name from qsar_datasets.properties p
left join qsar_datasets.datasets_in_dashboard did on p.id = did.fk_property_id
where did.id is null;


-- determine which properties we have datasets for
select p.name,p.id from qsar_datasets.datasets_in_dashboard did
join qsar_datasets.properties p on did.fk_property_id = p.id
order by p.name;



select d.name, p.name from qsar_datasets.datasets d 
join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id
join qsar_datasets.properties p on d.fk_property_id = p.id





-- select distinct prop_name, source_name from mv_experimental_data mv
-- where prop_category='Env. Fate/transport' order by prop_name,source_name;

