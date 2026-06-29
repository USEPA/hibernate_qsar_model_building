-- Export of exp_prop data using asif format
--CREATE MATERIALIZED VIEW mv_experimental_data as
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
        '2.1.1' 				 as data_version
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
 and dtxsid='DTXSID7020182' and ps.name='OPERA2.8'
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
select * from public."mv_predicted_data" where dtxsid='DTXSID3039242';

create index mv_experimental_data_dtxsid_index  on "mv_experimental_data" (dtxsid);
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
comment on column "mv_experimental_data".source_url is 'URL of source';
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


