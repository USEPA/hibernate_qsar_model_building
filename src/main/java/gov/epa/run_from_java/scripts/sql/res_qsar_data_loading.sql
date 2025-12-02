select count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where pv.fk_public_source_id=309 and keep=true; --PubChem_2024_11_27


-- Check loading of OChem counts by property
select p.name, count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where pv.fk_public_source_id=309 and keep=true --PubChem_2024_11_27
-- where pv.fk_public_source_id=10 and keep=true --OChem
group by p.name;

--Counts by public source original:
select ps.name, count(ps.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_original_id = ps.id
where pv.fk_public_source_id=309 and keep=true --PubChem_2024_11_27
-- where pv.fk_public_source_id=10 and keep=true --OChem
group by ps.name;



select sc.source_chemical_name, sc.source_casrn,p.name  from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_original_id = ps.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where pv.fk_public_source_id=309 and keep=true --PubChem_2024_11_27
-- where pv.fk_public_source_id=10 and keep=true --OChem
-- and ps.name='Burnham Center for Chemical Genomics';
and sc.source_chemical_name='1,4-DIOXANE' and pv.fk_public_source_original_id=18187;
;

delete from exp_prop.source_chemicals where created_at>'2025-02-13';

--Delete PubChem_2024_11_27 property values:
delete from exp_prop.property_values pv where fk_public_source_id=309; -- runs fast as long as parameter_values has index for fk_property_value_id

-- Check loading of OChem:
select count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where pv.fk_public_source_id=259 and keep=true;

select distinct  p.name from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where ps.name='PubChem_2024_11_27';


select  p.name,ps.name,count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name like'%Koc%'
group by p.name,ps.name
order by p.name;


-- Get count of property values by property from a given public source
select count(pv.id),  p.name from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where ps.name='PubChem_2024_11_27'
group by p.name;


select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where ps.name='PubChem_2024_03_20' and p.name='Boiling point';


-- Get count of literature sources for a public source
select count(distinct pv.fk_literature_source_id) from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where ps.name='PubChem_2024_11_27';

-- Find count of source chemicals for a given public source:
select count(sc.id) from exp_prop.source_chemicals sc
join exp_prop.public_sources ps on sc.fk_public_source_id = ps.id
where ps.name='PubChem_2024_11_27';



delete from exp_prop.property_values pv
where pv.fk_public_source_id=295 and pv.fk_property_id=1;



select * from exp_prop.public_sources where id=259;


delete from exp_prop.property_values pv where fk_public_source_id=259;

---------------------------------------------------------------------------------------------------------
-- Check loading of OPERA records- counts by property
select p.name, count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
-- where pv.fk_public_source_id=253 -- opera2.9
where pv.fk_public_source_id=12 --opera 2.8
group by p.name
order by p.name;

-- Check loading of non OPERA and lookchem records- counts by property
select p.name, count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where pv.fk_public_source_id!=253  and pv.fk_public_source_id!=12 and pv.fk_public_source_id!=9
group by p.name;


-- Check loading of ECOTOX by property
select p.name, count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where pv.fk_public_source_id=254
group by p.name;


select d.name,count(dp.id) from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where d.name like '%PubChem%'
group by d.name;


select d.name,count(dpc.id) from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where d.name like '%PubChem%'
group by d.name;



-- Check loading of sander
select p.name, count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where pv.fk_public_source_id=257
group by p.name;


select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where pv.fk_public_source_id=257
-- where pv.fk_public_source_id=257 and pv.fk_public_source_original_id=258





select * from exp_prop.property_values pv
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where pv.fk_public_source_id=254 and pv.fk_property_id=22 and sc.source_dtxsid='DTXSID4024149';


select distinct d.name from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where dpc.dtxsid='DTXSID4024149'
order by d.name;





---------------------------------------------------------------------------------------------------------
--property value counts -q1
select p.name as "Property", count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
group by p.name
order by p.name;


--Datasets get property value counts by property by public source-q2
select p.name as "Property", ps.name as "Source", count(pv.id),'Public' as "type" from exp_prop.property_values pv
join exp_prop.public_sources ps  on pv.fk_public_source_id = ps.id
join exp_prop.properties p on pv.fk_property_id = p.id
group by p.name,ps.name
order by p.name;

--Datasets get dpc counts by property by public source-q3p
select p.name as "Property", ps.name as "Source", count(dpc.id),'Public' as "type" from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
join exp_prop.public_sources ps  on pv.fk_public_source_id = ps.id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id
where d.name like 'exp_prop%'
group by p.name,ps.name
order by p.name;


--Datasets get dpc counts by property by literature source-q3l
select p.name as "Property", ls.name as "Source", count(dpc.id),'Literature' as "type" from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
join exp_prop.literature_sources ls  on pv.fk_literature_source_id = ls.id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id
where d.name like 'exp_prop%' and p.name !='Bioconcentration factor'
group by p.name,ls.name
order by p.name;




--Dashboard Datasets get dpc counts by property (mapped records) - q4
select p.name as "Property", count(dpc.id) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id
group by p.name
order by p.name;


-- mapped records omit bad sources
select p.name as "Property", count(dpc.id) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id
left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where  (ps.name is null or (ps.name!='eChemPortalAPI' and ps.name!='PubChem'
      and ps.name!='OFMPub' and ps.name!='Oxford University Chemical Safety Data (No longer updated)'))
group by p.name
order by p.name;


---------------------------------------------------------------------------------------------------------
-- Dataset get dp loading counts-q5
select p.name as "Property", count(dp.id) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id
group by p.name;


-- get modeling dataset sizes
select p.name as "Property", count(dp.id) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_cheminformatics_modules did on d.id = did.fk_datasets_id
group by p.name;


--Dashboard Datasets get dp counts by property by public source
select p.name, ps.name, count(distinct(dp.id)) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
join exp_prop.public_sources ps  on pv.fk_public_source_id = ps.id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id
where d.name like 'exp_prop%'
group by p.name,ps.name
order by p.name;
-- order by ps.name;

-- get datasets
select id, d.name from qsar_datasets.datasets d
where d.name like 'exp_prop%'


--Modeling Datasets get dp counts by property by public source
select p.name, ps.name, count(distinct(dp.id)) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
join exp_prop.public_sources ps  on pv.fk_public_source_id = ps.id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_cheminformatics_modules did on d.id = did.fk_datasets_id
where d.name like '%v1 modeling'
group by p.name,ps.name
order by p.name;
-- order by ps.name;


--Get list of properties for modules
select distinct p.name from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_cheminformatics_modules did on d.id = did.fk_datasets_id;


--Get list of properties for dashboard
select distinct p.name from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id;


--Get list of properties for dashboard
select distinct d.name,d.id from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id
order by d.name;



select p.name from qsar_models.models m
join qsar_datasets.datasets d on m.dataset_name=d.name
join qsar_datasets.properties p on d.fk_property_id = p.id
where m.id=1066;


--Get list of properties for dashboard
select distinct p.name from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id;


---- Look at counts by sources in BP data set
select ps.name, count(dpc.id) from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where d.id=239
group by ps.name;

---- Look at sources in BP property values
select ps.name, count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='Boiling point' and keep=true
group by ps.name
order by ps.name;

--Datasets get counts by property by literature source
select p.name, ls.name, count(dpc.id) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
join exp_prop.literature_sources ls  on pv.fk_literature_source_id = ls.id
join qsar_datasets.properties p on d.fk_property_id = p.id
where d.name like 'exp_prop%'
group by p.name,ls.name;


select count(dp.id) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
-- where d.id=300;
where d.id=303;


select pv2.value_text,count( pv2.value_text) from exp_prop.property_values pv
left join exp_prop.parameter_values pv2 on pv.id = pv2.fk_property_value_id
left join exp_prop.parameters p on pv2.fk_parameter_id = p.id
where pv.fk_property_id=22 and p.name='exposure_type'
group by pv2.value_text
;


select d.id, d.name, count(distinct(dp.id)) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
where d.name like 'exp_prop_%HR%'
group by d.id, d.name
order by d.name;



select distinct dpc.dtxsid,dp.canon_qsar_smiles, dp.qsar_property_value, dv.values_tsv from qsar_datasets.data_point_contributors dpc
				join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
				join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
				where dp.fk_dataset_id=377 and dv.fk_descriptor_set_id=6
				order by dp.canon_qsar_smiles;



VACUUM (ANALYZE, VERBOSE, FULL) exp_prop.property_values;
VACUUM (ANALYZE, VERBOSE, FULL) exp_prop.parameter_values;
VACUUM (ANALYZE, VERBOSE, FULL) exp_prop.source_chemicals;


select distinct  * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where ps.name='ECOTOX_2023_12_14' and p.name='48 hour Daphnia magna LC50';




select d.name, d.created_by, count(distinct (dp.id)) as datapoints,count (dpc.id) as contributors from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
where d.name like '%FHM%' or d.name like '%BG%' or d.name like '%RT%' or d.name like '%DM%'
group by d.name,d.created_by
order by d.name
;


select sc.source_dtxsid,pv.value_point_estimate,pv.created_by,pv.created_at,pv.fk_property_id from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
join exp_prop.properties p  on pv.fk_property_id = p.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
-- where ps.name='ECOTOX_2023_12_14' and p.name='96 hour fathead minnow LC50'
-- where ps.name='ECOTOX_2023_12_14' and p.name='96 hour bluegill LC50'
-- where ps.name='ECOTOX_2023_12_14' and p.name='96 hour rainbow trout LC50'
where ps.name='ECOTOX_2023_12_14' and p.name='48 hour Daphnia magna LC50'
order by source_dtxsid,pv.value_point_estimate,pv.created_by;


select * from exp_prop.property_values pv
where pv.fk_public_source_id=254 and pv.fk_property_id=43 and pv.created_by='lbatts';


delete from exp_prop.property_values pv
where pv.fk_public_source_id=254 and pv.fk_property_id=43 and pv.created_by='lbatts';


select * from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.properties p on d.fk_property_id = p.id
where dp.canon_qsar_smiles='FC(F)(F)C(F)(F)Cl' and p.name='Boiling point';

-- Delete pubchem datasets
delete from qsar_datasets.datasets d
where d.name like '%PubChem_2024_03_20';

select count(id) from qsar_datasets.data_points dp;

VACUUM (ANALYZE, VERBOSE, FULL) qsar_datasets.data_points;
VACUUM (ANALYZE, VERBOSE, FULL) qsar_datasets.data_point_contributors;
VACUUM (ANALYZE, VERBOSE, FULL) qsar_datasets.data_points_in_splittings;


select keep_reason from exp_prop.property_values pv
where keep=true and pv.qc_flag=true and keep_reason is not null;


--Distinct source chemicals with a given public source
select count(distinct sc.id) from exp_prop.property_values pv
join exp_prop.source_chemicals sc  on sc.id = pv.fk_source_chemical_id
where pv.fk_public_source_id=309;


--Delete PubChem_2024_11_27 property values:
delete from exp_prop.property_values pv where fk_public_source_id=309; -- runs fast as long as parameter_values has index for fk_property_value_id


select count(id) from exp_prop.source_chemicals sc
-- where sc.created_at>'1/01/2025';
where id>=1839858;




delete from exp_prop.source_chemicals sc
where id>=1839858;


--Delete orphaned new source chemicals:
DELETE FROM exp_prop.source_chemicals
USING  exp_prop.source_chemicals as sc
LEFT JOIN exp_prop.property_values  AS pv ON
   sc.id = pv.fk_source_chemical_id
WHERE
   sc.created_at>'1/01/2025' and pv.id is null;



select * FROM exp_prop.source_chemicals sc
LEFT JOIN exp_prop.property_values  AS pv ON  sc.id = pv.fk_source_chemical_id
WHERE sc.created_at>'1/01/2025' and pv.id is null
and source_chemical_name like 'SID%';


select * from exp_prop.source_chemicals where id=1870019;



select distinct  pv.value_text  from exp_prop.parameter_values pv where fk_parameter_id=14;


delete from qsar_datasets.datasets where name like '%EPI%';


select sc.source_dtxsid,  pv.value_point_estimate from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where ps.name='OPERA2.8' and p.name='Bioconcentration factor';


SELECT distinct sc.id,source_dtxcid,source_dtxsid,source_dtxrid,source_smiles, source_casrn,source_chemical_name,sc.fk_literature_source_id,sc.created_at,sc.created_by,sc.updated_at,sc.updated_by from exp_prop.source_chemicals sc
-- join exp_prop.property_values pv  on sc.id = pv.fk_source_chemical_id
where sc.fk_public_source_id=310


select d.id, d.name, count(dp.id) from qsar_datasets.datasets d
    join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.properties p on d.fk_property_id = p.id
where p.name='Bioconcentration factor' and d.name like '%modeling%'
group by d.name,d.id
order by d.id;


--BCF data
select distinct  p.name,count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
--     join exp_prop.public_sources ps2 on pv.fk_public_source_original_id = ps2.id
    where p.name like '%LC50%'
group by p.name;



select count(distinct sc.source_dtxsid)  from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.parameter_values pv2 on pv2.fk_property_value_id = pv.id and pv2.fk_parameter_id=16
join exp_prop.parameter_values pv3 on pv3.fk_property_value_id = pv.id and pv3.fk_parameter_id=11
    join exp_prop.parameter_values pv4 on pv4.fk_property_value_id = pv.id and pv4.fk_parameter_id=48
    join exp_prop.parameter_values pv5 on pv5.fk_property_value_id = pv.id and pv5.fk_parameter_id=38

    join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
-- where ps.name='ECOTOX_2024_12_12' and p.name='Acute aquatic toxicity' and pv3.value_text='Fathead Minnow'
where ps.name='ECOTOX_2024_12_12' and p.name='Acute aquatic toxicity'
  and pv4.value_point_estimate=4 and pv5.value_text='Fish'
;



VACUUM (ANALYZE, VERBOSE, FULL) exp_prop.property_values;

VACUUM (ANALYZE, VERBOSE, FULL) exp_prop.parameter_values;

VACUUM (ANALYZE, VERBOSE, FULL) qsar_datasets.data_points;
VACUUM (ANALYZE, VERBOSE, FULL) qsar_datasets.data_point_contributors;


select count(id) from exp_prop.property_values pv where fk_public_source_id=312 and fk_property_id=54;


delete from exp_prop.property_values pv where fk_public_source_id=312 and fk_property_id=54;


select * from exp_prop.property_values where fk_public_source_id=255;


select ps.name, count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where pv.fk_property_id=6 and keep=true --PubChem_2024_11_27
-- where pv.fk_public_source_id=10 and keep=true --OChem
group by ps.name;



select ps.name,count(pv.id) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where d.name='exp_prop_LOG_KOW_v2.0'
group by ps.name;


select dp.canon_qsar_smiles, dpc.property_value, smiles,p.name, pv2.value_text,pv.keep from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
    left join exp_prop.parameter_values pv2 on pv.id = pv2.fk_property_value_id
    left join exp_prop.parameters p on pv2.fk_parameter_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where d.name='exp_prop_LOG_KOW_v2.0'
--   and ps.name='OPERA2.8'
-- and ps.name='PubChem_2024_11_27'
and ps.name='eChemPortalAPI'
order by dp.canon_qsar_smiles,dpc.property_value;


select dp.canon_qsar_smiles, dp.qsar_property_value, dpc.property_value,ps.name from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where d.name='exp_prop_LOG_KOW_v2.0' and dp.canon_qsar_smiles='COC(=O)C1=CC(CCl)=NN1C1=NC=CC=C1Cl'
--   and ps.name='OPERA2.8'
;


select * from exp_prop.property_values pv
join exp_prop.parameter_values pv2 on pv.id = pv2.fk_property_value_id
where pv.fk_property_id=6 and pv.fk_public_source_id=6 and keep=true;



select ps.name,count(distinct (pv.id)) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
         join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
         join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
                              join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where  d.name='WS v1 modeling'
group by ps.name;

select ps.name,count(distinct (pv.id)) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
         join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
         join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
                              join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where  d.name='exp_prop_WATER_SOLUBILITY_v2.0'
group by ps.name;



select pv.id,sc.source_dtxsid,ps.name,pv.value_point_estimate from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where p.name='Soil Adsorption Coefficient (Koc)' and ps.name='OPERA2.9' and source_dtxsid like '%DTXSID2020686%';



select * from qsar_datasets.datasets d
where d.name like '%96%';


select * from qsar_models.models m
         where m.dataset_name like 'exp_prop_96HR%';
-- where m.dataset_name like '%96%';
-- where m.dataset_name='ECOTOX_2024_12_12_96HR_FHM_LC50_v3 modeling';



select * from qsar_datasets.datasets d
where name like 'exp_prop_96HR%v% modeling';


select d.name,count(dp.id) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
where name like '%96HR%v3%'
group by d.name
order by count(dp.id) desc;


select * from qsar_models.models m
where dataset_name like '%96HR%v3%';


select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where name='Acute aquatic toxicity' and fk_public_source_id=312;



delete from exp_prop.source_chemicals sc
-- select * from exp_prop.source_chemicals sc
where id>2051629;

select from exp_prop.property_values pv
where created_at>'2025-12-01' and created_at<'2025-12-03';


select * from exp_prop.source_chemicals sc
where id>2051629;


-- SELECT count(id) from exp_prop.source_chemicals sc
SELECT count(id) from exp_prop.source_chemicals sc
-- delete from exp_prop.source_chemicals sc
where created_at > '2025-12-01' and created_at < '2025-12-03'
and fk_literature_source_id is null and fk_public_source_id is null;
-- order by source_dtxsid;
