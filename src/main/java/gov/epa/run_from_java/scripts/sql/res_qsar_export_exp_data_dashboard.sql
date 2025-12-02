-- Export of exp_prop data using asif format
-- CREATE MATERIALIZED VIEW mv_experimental_data as
select
    row_number() over (order by dpc.dtxsid, p."name",ps.name,ls.citation) as id,
       dpc.dtxsid,
       dpc.dtxcid,
       dpc.smiles,
       p.name_ccd                as prop_name,
       'experimental'            as prop_type,
       pc.name                   as prop_category,
       d."name"                  as dataset,
       dpc.property_value        as prop_value,
--        u.abbreviation            as unit,
       u.abbreviation_ccd            as prop_unit,
       pv.id as prop_value_id,
       pv.value_original as prop_value_original,
       pv.value_text as prop_value_text,
--        pv.value_min as prop_value_min, -- may have different units than dpc.property_value
--        pv.value_max as prop_value_max,
--         pv.fk_unit_id,
       pvT.value_point_estimate  as exp_details_temperature_c,
       pvP.value_point_estimate  as exp_details_pressure_mmHg,
       pvpH.value_point_estimate as exp_details_pH,--note it will convert to lower case
       pvRS.value_text           as exp_details_response_site, -- for BCF, fish tox
       pvSL.value_text           as exp_details_species_latin, -- for BCF, fish tox
       pvSC.value_text           as exp_details_species_common,
       pvSS.value_text           as exp_details_species_supercategory,
       case when ps.name is not null then ps.name else ls.name end as source_name,
       case when ps.name is not null then ps.description else ls.citation end as source_description,
       case when ps.name is not null then ps.url else ls.doi end as source_url,
       ps."name"                 as public_source_name,
       ps.description            as public_source_description,
       ps.url                    as public_source_url,
       pv.page_url               as direct_url,
       ls."name"                 as ls_name,
       ls.citation               as ls_citation,
       ls.doi                    as ls_doi,
--        ls.url                    as ls_url,
       pv.document_name          as brief_citation,--From OPERA2.9 usually
       ps2."name"                as public_source_original_name, --For sources like toxval,pubchem, sander
       ps2.description           as public_source_original_description,
       ps2.url                   as public_source_original_url,
--        pv.file_name,
       current_date              as export_date,
        '2.1.0' as data_version

from qsar_datasets.data_points dp
         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
         join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id
         left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
         left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
         left join exp_prop.public_sources ps2 on pv.fk_public_source_original_id = ps2.id
         left join exp_prop.parameter_values pvT on pvT.fk_property_value_id = pv.id and pvT.fk_parameter_id = 2
         left join exp_prop.parameter_values pvP on pvP.fk_property_value_id = pv.id and pvP.fk_parameter_id = 1
         left join exp_prop.parameter_values pvpH on pvpH.fk_property_value_id = pv.id and pvpH.fk_parameter_id = 3
         left join exp_prop.parameter_values pvRS on pvRS.fk_property_value_id = pv.id and pvRS.fk_parameter_id = 22
         left join exp_prop.parameter_values pvSS on pvSS.fk_property_value_id = pv.id and pvSS.fk_parameter_id = 38
         left join exp_prop.parameter_values pvSL on pvSL.fk_property_value_id = pv.id and pvSL.fk_parameter_id = 21
         left join exp_prop.parameter_values pvSC on pvSC.fk_property_value_id = pv.id and pvSC.fk_parameter_id = 11
         join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
         join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.datasets_in_dashboard did on did.fk_property_id = d.fk_property_id
         join qsar_datasets.units u on u.id = d.fk_unit_id_contributor
        left join qsar_datasets.properties_in_categories pic on p.id = pic.fk_property_id
        left join qsar_datasets.property_categories pc on pic.fk_property_category_id = pc.id

where d.id = did.fk_datasets_id and keep=true
--   and (ps.name is null or (ps.name!='eChemPortalAPI' and ps.name!='PubChem'
--       and ps.name!='OFMPub'))
--  and ps.name ='PubChem'
-- and pv.value_text is not null and dpc.property_value is null
-- and p.name_ccd ='Vapor Pressure'
-- and dtxsid='DTXSID3046613'
-- and dtxsid='DTXSID7020182'
-- and dtxsid in ('DTXSID0024135','DTXSID2020684',
-- 				'DTXSID2020686', 'DTXSID5024134', 'DTXSID7020685', 'DTXSID7020687', 'DTXSID901310407')
-- 	and p."name" ='Vapor pressure'
-- and u.abbreviation_ccd is null
-- and p.name_ccd is null
-- and dtxsid='DTXSID4059916'

--   and dpc.dtxsid='DTXSID5020281'
-- and p.name_ccd='Bioconcentration Factor'
-- and dtxsid='DTXSID0037522'
-- and ps2."name"  is not null
-- and dtxsid='DTXSID8031865'
-- order by dtxsid, p."name";
;


-- update the view:
refresh materialized view "mv_experimental_data";

select count(v.prop_name)from public."mv_experimental_data" v;

select * from public."mv_experimental_data" where dtxsid='DTXSID3039242';

create index mv_experimental_data_dtxsid_index  on "mv_experimental_data" (dtxsid);
GRANT SELECT ON "mv_experimental_data" TO arashid;
GRANT SELECT ON "mv_experimental_data" TO app_pentaho;
comment on materialized view "mv_experimental_data" is 'Experimental data materialized view';
comment on column "mv_experimental_data".id is 'Autogenerated id for sorting';

comment on column "mv_experimental_data".dtxsid is 'DSSTox Substance id';
comment on column "mv_experimental_data".dtxcid is 'DSSTox Compound id';
comment on column "mv_experimental_data".smiles is 'SMILES structure';
comment on column "mv_experimental_data".prop_type is 'Property type (e.g. experimental)';
comment on column "mv_experimental_data".prop_name is 'Property name (e.g. Boiling Point)';
comment on column "mv_experimental_data".prop_category is 'Property category (e.g. Physchem)';
comment on column "mv_experimental_data".dataset is 'Dataset in qsar_datasets.datasets';
comment on column "mv_experimental_data".prop_value_id is 'exp_prop.property_values.id';
comment on column "mv_experimental_data".prop_value is 'Numerical experimental property value';
comment on column "mv_experimental_data".prop_value_original is 'The original unparsed property value with units (e.g. Solubility in water, mg/l: 0.71 (very poor))';
comment on column "mv_experimental_data".prop_value_text is 'Qualitative experimental property value (e.g. slightly soluble) ';
comment on column "mv_experimental_data".prop_unit is 'Units for experimental property value';
comment on column "mv_experimental_data".exp_details_pH is 'experimental parameter: pH ';
comment on column "mv_experimental_data".exp_details_temperature_c is 'experimental parameter: temperature in C';
comment on column "mv_experimental_data".exp_details_pressure_mmhg is 'experimental parameter: pressure in mmHg';
comment on column "mv_experimental_data".exp_details_response_site is 'experimental parameter: response site (e.g. Whole body)';
comment on column "mv_experimental_data".exp_details_species_latin is 'experimental parameter: species latin (e.g. Pimephales promelas)';
comment on column "mv_experimental_data".exp_details_species_common is 'experimental parameter: species common (e.g. fathead minnow)';
comment on column "mv_experimental_data".exp_details_species_supercategory is 'experimental parameter: type of animal (e.g. Fish)';
comment on column "mv_experimental_data".source_name is 'Name of source that the data was pulled from';
comment on column "mv_experimental_data".source_description is 'Description of source';
comment on column "mv_experimental_data".public_source_name is 'Name of public source that the data was pulled from';
comment on column "mv_experimental_data".public_source_description is 'Description of public source';
comment on column "mv_experimental_data".public_source_url is 'URL of public source';
comment on column "mv_experimental_data".public_source_original_name is 'Name of original public source that the public source cites';
comment on column "mv_experimental_data".public_source_original_description is 'Description of original public source';
comment on column "mv_experimental_data".public_source_original_url is 'URL of original public source';
comment on column "mv_experimental_data".ls_name is 'Name of literature source (e.g. Author, Year)';
comment on column "mv_experimental_data".ls_citation is 'Complete citation for the literature source';
comment on column "mv_experimental_data".ls_doi is 'DOI for the literature source';
comment on column "mv_experimental_data".brief_citation is 'Incomplete citation provided by source';
comment on column "mv_experimental_data".direct_url is 'Direct URL to the experimental data for the given chemical';
comment on column "mv_experimental_data".export_date is 'When the materialized view was updated';
comment on column "mv_experimental_data".data_version is 'Version of the materialized view (see public.materialized_view_version_history for description of changes made)';




-----------------------------------------------------------------------------------------------------------
-- Properties that are available to export to dashboard so far:
select p.name as property,d.name as dataset from qsar_datasets.datasets_in_dashboard did
join qsar_datasets.properties p on did.fk_property_id = p.id
join qsar_datasets.datasets d on did.fk_datasets_id = d.id;

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


-- Data versioning scheme
-- . Data owner could use
--     · Calendar versioning (CalVer)
--     · 2024.1 Q1 of 2024
-- . Semantic Versioning (SemVer) - Major.Minor.Patch
--     . major -- when you are changing the structural changes
--     . minor -- when you are just loaded new data
--     . patch -- fixes in current version of data.


select *
from public."mv_experimental_data" ved
where dtxsid in ('DTXSID1037303', 'DTXSID3031862',
                 'DTXSID4059916',
                 'DTXSID5044572',
                 'DTXSID6062599')
order by dtxsid, ved.prop_name;

-- Record counts
SELECT reltuples AS estimate FROM pg_class where relname = 'v_experimental_data';


select  * from qsar_datasets.datasets_in_dashboard did
join qsar_datasets.properties p on did.fk_property_id = p.id;



select *
from public."mv_experimental_data" ved
where dtxsid in ('DTXSID8047553', 'DTXSID90868151', 'DTXSID3059921', 'DTXSID8047553', 'DTXSID8059970', 'DTXSID4059916',
                 'DTXSID6062599', 'DTXSID3031862', 'DTXSID1037303', 'DTXSID8031865', 'DTXSID8031863', 'DTXSID3031860',
                 'DTXSID8031861', 'DTXSID3059921', 'DTXSID1070800', 'DTXSID1066071', 'DTXSID30870531', 'DTXSID5030030',
                 'DTXSID8062600', 'DTXSID7040150', 'DTXSID8059920', 'DTXSID3031864', 'DTXSID8071356', 'DTXSID3040148',
                 'DTXSID20873011', 'DTXSID3038939', 'DTXSID00408562', 'DTXSID70191136', 'DTXSID60500450',
                 'DTXSID60663110', 'DTXSID50892351', 'DTXSID20892348', 'DTXSID90723993', 'DTXSID50723994',
                 'DTXSID50379814')
order by dtxsid, ved.prop_name;


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







select * from mv_experimental_data where dtxsid in ('DTXSID00192353','DTXSID6067331','DTXSID30891564','DTXSID6062599','DTXSID90868151','DTXSID8031863','DTXSID8031865','DTXSID1037303','DTXSID8047553','DTXSID60663110','DTXSID70191136','DTXSID3037709','DTXSID3059921','DTXSID3031860','DTXSID8037706','DTXSID8059920','DTXSID3031862','DTXSID30382063','DTXSID00379268','DTXSID20874028','DTXSID3037707') and prop_name='LogKow: Octanol-Water';



-- select distinct prop_name, source_name from mv_experimental_data mv
-- where prop_category='Env. Fate/transport' order by prop_name,source_name;

    select distinct prop_name from mv_experimental_data mv
where prop_category='Env. Fate/transport' order by prop_name;