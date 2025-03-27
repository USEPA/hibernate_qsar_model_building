-- Export of exp_prop raw data by property name and smiles
select p."name"                  as property,
       d."name"                  as dataset,
       dpc.dtxsid,
       dpc.dtxcid,
       dr.preferred_name                   as chemical_name,
       dr.casrn,
       dp.canon_qsar_smiles,
       dpc.smiles,
       dp.qsar_exp_prop_property_values_id as median_exp_prop_property_values_id,
       pv.id                     as exp_prop_property_values_id,
       dpc.property_value        as property_value,
       u.abbreviation_ccd            as property_units,
       dr.mol_weight,
       pv.value_text             as property_value_text, --to get meaning of binary property values
       pvT.value_point_estimate  as temperature_c,
       pvP.value_point_estimate  as pressure_mmHg,
       pvpH.value_point_estimate as pH,
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
       ps2.url                   as public_source_original_url
        from  qsar_datasets.datasets d
        join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id
        join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
        join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
        left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
        left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
        left join exp_prop.public_sources ps2 on pv.fk_public_source_original_id = ps.id
        left join exp_prop.parameter_values pvT on pvT.fk_property_value_id = pv.id and pvT.fk_parameter_id = 2
        left join exp_prop.parameter_values pvP on pvP.fk_property_value_id = pv.id and pvP.fk_parameter_id = 1
        left join exp_prop.parameter_values pvpH on pvpH.fk_property_value_id = pv.id and pvpH.fk_parameter_id = 3
        join qsar_datasets.properties p on d.fk_property_id = p.id
--select desired dataset for a given property:
         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id
         join qsar_datasets.units u on u.id = d.fk_unit_id_contributor
        left join qsar_models.dsstox_records dr on dr.dtxsid=dpc.dtxsid -- to look up chemical name
where d.id = did.fk_datasets_id and keep=true
-- Filter by property:
--     and p.name='Water solubility'
--     and p.name='Vapor pressure'
--     and p.name='Henry''s law constant'
--     and p.name='Density'
  -- Filter by chemical
--   and dpc.dtxsid='DTXSID4059916'
  and dpc.dtxsid='DTXSID3039242'
-- and dtxcid='DTXCID8031998'
-- AND dp.canon_qsar_smiles='CC=CCC' --get all values for a datapoint
-- AND dp.canon_qsar_smiles='C1C=CC=CC=1' --get all values for a datapoint
order by p.name,dp.canon_qsar_smiles, exp_prop_property_values_id;


-- Export of dataset flattened data by property name and smiles
select p."name"                  as property,
       d."name"                  as dataset,
       dp.qsar_dtxcid,
       dp.canon_qsar_smiles,
       dp.qsar_exp_prop_property_values_id as median_exp_prop_property_values_id,
       dp.qsar_property_value,
       u.abbreviation_ccd            as qsar_property_units
        from  qsar_datasets.datasets d
        join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id
        join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id
         join qsar_datasets.units u on u.id = d.fk_unit_id
where d.id = did.fk_datasets_id
order by p.name,dp.canon_qsar_smiles;

-- Get list of output units for flattened records:
select distinct p.name,u.abbreviation_ccd
        from  qsar_datasets.datasets d
        join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id
        join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id
         join qsar_datasets.units u on u.id = d.fk_unit_id
where d.id = did.fk_datasets_id;




-- Search by model_id and qsar smiles
select p."name"                  as property,
       d."name"                  as dataset,
       dr.preferred_name         as chemical_name,
       dpc.dtxsid,
       dpc.dtxcid,
       dp.canon_qsar_smiles,
       dpc.smiles,
       dp.qsar_exp_prop_property_values_id as median_exp_prop_property_values_id,
       pv.id                     as exp_prop_property_values_id,
       dpc.property_value        as property_value,
       u.abbreviation            as property_units,
       pv.value_text             as property_value_text, --to get meaning of binary property values
       pvT.value_point_estimate  as temperature_c,
       pvP.value_point_estimate  as pressure_mmHg,
       pvpH.value_point_estimate as pH,
       ps."name"                 as public_source_name,
       ps.description            as public_source_description,
       ps.url                    as public_source_url,
       ls."name"                 as literature_source_name,
       ls.citation               as literature_source_description,
       ls.url                    as literature_source_url,
       pv.page_url             as direct_link,
       pv.file_name            as file_name_loaded,
       pv.document_name         as short_citation
        from qsar_models.models m
        join qsar_datasets.datasets d on m.dataset_name=d.name
            join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
         join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
        join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
         left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
         left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
         left join exp_prop.parameter_values pvT on pvT.fk_property_value_id = pv.id and pvT.fk_parameter_id = 2
         left join exp_prop.parameter_values pvP on pvP.fk_property_value_id = pv.id and pvP.fk_parameter_id = 1
         left join exp_prop.parameter_values pvpH on pvpH.fk_property_value_id = pv.id and pvpH.fk_parameter_id = 3
         join qsar_datasets.properties p on d.fk_property_id = p.id
         join qsar_datasets.units u on u.id = d.fk_unit_id_contributor
        left join qsar_models.dsstox_records dr on dr.dtxsid=dpc.dtxsid -- to look up chemical name
where keep=true and m.id=1066 AND dp.canon_qsar_smiles='CC=CCC' --get all values for a datapoint
order by dpc.property_value;


select split_num
        from  qsar_datasets.datasets d
        join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id
        join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id
        join qsar_datasets.properties p on d.fk_property_id = p.id
where d.id = did.fk_datasets_id and p.name='Water solubility' AND dp.canon_qsar_smiles='C1C=CC=CC=1'; --get all values for a datapoint

select split_num from qsar_models.models m
        join qsar_datasets.datasets d on m.dataset_name=d.name
            join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
        join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where m.id=1066 AND dp.canon_qsar_smiles='CC=CCC' --get all values for a datapoint





-- WS query to get median logWS_g_L:
select dpc.dtxsid, percentile_cont(0.5) WITHIN GROUP (ORDER BY log10(dpc.property_value*dr.mol_weight)) as median_logWS_g_L
        from  qsar_datasets.datasets d
        join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id
        join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
        join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
        join qsar_datasets.properties p on d.fk_property_id = p.id
--select desired dataset for a given property:
         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id
         join qsar_datasets.units u on u.id = d.fk_unit_id_contributor
        left join qsar_models.dsstox_records dr on dr.dtxsid=dpc.dtxsid -- to look up chemical name
where d.id = did.fk_datasets_id and keep=true and p.name='Water solubility'
-- and dpc.dtxsid='DTXSID001019905'
group by dpc.dtxsid;


-- Just to double check that previous query gets medians right
select dpc.dtxsid, log10(dpc.property_value*dr.mol_weight) as log10Ws_g_l
        from  qsar_datasets.datasets d
        join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id
        join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id
        join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
        join qsar_datasets.properties p on d.fk_property_id = p.id
--select desired dataset for a given property:
         join qsar_datasets.datasets_in_cheminformatics_modules did on did.fk_property_id = d.fk_property_id
         join qsar_datasets.units u on u.id = d.fk_unit_id_contributor
        left join qsar_models.dsstox_records dr on dr.dtxsid=dpc.dtxsid -- to look up chemical name
where d.id = did.fk_datasets_id and keep=true and p.name='Water solubility'
and dpc.dtxsid='DTXSID1020560'
order by dtxsid,log10Ws_g_l




select distinct ps.name from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where dp.fk_dataset_id=114;

select distinct ls.name from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
where dp.fk_dataset_id=114;



select value_point_estimate,sc.source_chemical_name, ps.name from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
-- where sc.source_casrn='678-26-2' and p.name='Vapor pressure'
where sc.source_chemical_name='Perflenapent' and p.name='Vapor pressure'





