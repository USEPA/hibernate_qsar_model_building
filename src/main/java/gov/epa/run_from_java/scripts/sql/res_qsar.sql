-- Distinct qsar ready standardizers:
select distinct (c.standardizer) from qsar_descriptors.compounds c


-- Mapped records for a dataset
select * from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id=dp.id
join qsar_datasets.datasets d on d.id=dp.fk_dataset_id
where d.id=91

-- Count of flattened records for a dataset
select count(dp.id) from qsar_datasets.data_points dp
where dp.fk_dataset_id=91

-- Count of mapped records for a dataset
select count(dp.id) from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id=dp.id
join qsar_datasets.datasets d on d.id=dp.fk_dataset_id
where d.id=91

-- Export predictions
select *from qsar_models.predictions p
      join qsar_models.models m on p.fk_model_id = m.id
         join qsar_datasets.datasets d on d.name=m.dataset_name
            join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
                join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
         where fk_model_id=285 and p.fk_splitting_id=1 and dpis.split_num=1 and dp.canon_qsar_smiles=p.canon_qsar_smiles
         and dpis.fk_splitting_id=p.fk_splitting_id
        order by p.canon_qsar_smiles

-- Look at size of model bytes
select id,fk_model_id, length(bytes) from qsar_models.model_bytes where fk_model_id=275  order by id


select count(id) from qsar_datasets.dsstox_records dr where dr.fk_compounds_id is null

select count(id) from qsar_datasets.dsstox_records dr

select * from qsar_datasets.dsstox_records dr where dr.dtxsid='DTXSID3039242'

update qsar_models.models m set details_text= convert_from(details, 'UTF8');

------------------------------------------------------------------------------------------------------------------------
-- delete parameter values for LogBCF_Whole_body
delete from exp_prop.parameter_values pv2 using exp_prop.property_values pv
       where pv2.fk_property_value_id=pv.id and pv.fk_property_id=19;

-- delete property values
delete from exp_prop.property_values pv where fk_property_id=19;

-- Delete source chemicals
delete from exp_prop.source_chemicals sc where sc.fk_public_source_id=73 or sc.fk_public_source_id=74 ;


-- delete from exp_prop.literature_sources where created_by='tmarti02';


--------------------------------------------------------------------------------------------------------------
select count(id) from exp_prop.source_chemicals sc where sc.fk_public_source_id=79

select * from exp_prop.source_chemicals sc where sc.fk_public_source_id=79

select * from exp_prop.source_chemicals sc where sc.fk_public_source_id=79 and source_dtxsid is null

select count(id) from exp_prop.property_values pv where pv.value_qualifier ='~'

update exp_prop.property_values pv set value_qualifier=null where value_qualifier=''


select count(id) from exp_prop.property_values pv where pv.fk_property_id=20

select count(id) from exp_prop.parameter_values pv

select count(id) from exp_prop.source_chemicals sc where sc.fk_public_source_id=79

select * from exp_prop.source_chemicals sc where sc.fk_public_source_id=79
order by source_casrn

-- select count(id) from exp_prop.property_values pv where pv.fk_literature_source_id=21
-- select count(id) from exp_prop.source_chemicals sc where sc.fk_public_source_id=79
select * from exp_prop.source_chemicals sc where sc.fk_literature_source_id>=20
order by sc.source_casrn

-- select * from exp_prop.property_values pv
-- join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
-- where pv.fk_literature_source_id is not null and pv.fk_literature_source_id>=20


select count(dp.id) from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id=d.id
where d.name='WS v1 modeling'
-- where d.name='WS v1 res_qsar'



delete from qsar_descriptors.compounds where smiles='[H][C@@]12O[C]3([H])(C[C@@](C)(OC)[C@@H](O)[C@H](C)O3)[C@H]1C(=O)O[C@]([H])(CC)[C@@](C)(O)[C@H](O)[C@@H](C)C(=O)[C@H](C)C[C@@](C)(O)[C@]([H])(O[C@]1([H])O[C@H](C)[C@@H](O)[C@@]([H])([C@H]1O)N(C)C)[C@H]2C' and canon_qsar_smiles is null and standardizer='SCI_DATA_EXPERTS_QSAR_READY';

--------------------------------------------------------------------------------------------------------
-- Dataset deletion
delete from qsar_datasets.data_point_contributors dpc using qsar_datasets.data_points dp
where dp.fk_dataset_id=112 and dpc.fk_data_point_id =dp.id;

delete from qsar_datasets.data_points dp
where dp.fk_dataset_id=112;

delete from qsar_datasets.datasets d
where d.id=112;

select d.name from qsar_datasets.datasets d
join qsar_datasets.datasets_in_cheminformatics_modules dicm on d.id = dicm.fk_datasets_id;



select count(id) from qsar_datasets.data_points dp
where dp.fk_dataset_id=112;


select * from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id =d.id
where d.id=305;


--------------------------------------------------------------------------------------------------------


select embedding_tsv from qsar_models.descriptor_embeddings where qsar_method = 'rf' and descriptor_set_name = 'WebTEST-default' and dataset_name = 'HLC v1 modeling' and splitting_name ='T=PFAS only, P=PFAS';



select count(dp.id) from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
where d.name='MP v1 modeling' and dv.values_tsv is not null and dv.fk_descriptor_set_id=6


select count(dp.id) from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where d.name='MP v1 modeling'


select convert_from(bytes, 'utf-8') from qsar_models.model_bytes where fk_model_id=591


select * from qsar_models.model_statistics ms
join qsar_models.models on ms.fk_model_id = models.id
where splitting_name='RND_REPRESENTATIVE' and dataset_name like '%modeling%' and fk_method_id=9 and fk_statistic_id=8 and fk_descriptor_embedding_id is not null
order by dataset_name, ms.id asc;

select * from qsar_models.models m
where dataset_name ='LogP v1 modeling' and splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is not null and fk_method_id=9
order by dataset_name


select * from qsar_models.model_statistics ms
join qsar_models.models m on ms.fk_model_id=m.id
where m.dataset_name like '%modeling%' and fk_statistic_id=8 and fk_method_id=2 and m.splitting_name='T=PFAS only, P=PFAS'
order by m.id desc;

select s.name, statistic_value from qsar_models.model_statistics ms
join qsar_models.statistics s on ms.fk_statistic_id = s.id
where s.name='Q2_CV_Training'

select count(dp.id) from qsar_datasets.data_points dp
join qsar_datasets.datasets on dp.fk_dataset_id = datasets.id
join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles
where dp.fk_dataset_id=107 and dv.fk_descriptor_set_id=6 and dv.values_tsv is not null

select distinct pv.fk_public_source_id from exp_prop.property_values pv where fk_property_id=13 -- opera and 3m

select distinct pv.fk_public_source_id from exp_prop.property_values pv where fk_property_id=19 -- burkhard and toxval

select * from exp_prop.property_values pv where fk_property_id=13 and fk_public_source_id!=13


select * from exp_prop.property_values pv
where fk_property_id=19 and fk_public_source_id=73


select values_tsv from qsar_descriptors.descriptor_values dv where dv.canon_qsar_smiles='FC(F)(F)I' and fk_descriptor_set_id=6



select dp.qsar_dtxcid, dp.qsar_property_value, dv.values_tsv from qsar_datasets.data_points dp
inner join qsar_descriptors.descriptor_values dv
on dp.canon_qsar_smiles=dv.canon_qsar_smiles
where dp.fk_dataset_id=108 and dv.fk_descriptor_set_id=6

select dp.qsar_dtxcid, dp.qsar_property_value, dv.values_tsv from qsar_datasets.data_points dp
inner join qsar_descriptors.descriptor_values dv
on dp.canon_qsar_smiles=dv.canon_qsar_smiles
where dp.fk_dataset_id=115 and dv.fk_descriptor_set_id=6



select * from qsar_descriptors.descriptor_values dv
where dv.canon_qsar_smiles='CCCO'

select * from qsar_datasets.data_points dp
where dp.canon_qsar_smiles='CCCO'

select * from qsar_datasets.datasets
where name like '%v1 modeling%'

select m.id, dataset_name, ms.statistic_value
from qsar_models.models m
         inner join qsar_models.model_statistics ms on ms.fk_model_id = m.id
        join qsar_models.statistics s on ms.fk_statistic_id = s.id
where m.dataset_name like '%v1 modeling'
  and splitting_name = 'RND_REPRESENTATIVE'
  and fk_descriptor_embedding_id is not null -- all descriptors
  and fk_method_id = 5 --kNN
--   and s.name='MAE_CV_Training'
and s.name='MAE_Test'
order by m.id; --R2_CV_Training

select details from qsar_models.models m where m.dataset_name='WS v1 modeling' and splitting_name='RND_REPRESENTATIVE' and fk_method_id=7 and fk_descriptor_embedding_id is null


select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where p.name='LogBCF' and pv.fk_public_source_id!=12

select smiles from qsar_datasets.dsstox_records where mol_weight=0 and smiles not like '%;%' and smiles not like '%:%'

select smiles from qsar_datasets.dsstox_records where mol_weight is null

delete from qsar_models.models m where m.source='OPERA2.9'

select from qsar_models.models m where m.source='OPERA2.9'


update qsar_models.models m
set fk_source_id=s.id
from qsar_models.sources s
where s.name=m.source;


update qsar_models.models m
set source='Cheminformatics Modules'
where m.source='WebTEST2.0'

update qsar_models.models m
set source='Percepta2020.2.1'
where m.source='Percepta 2020.2.1'


select * from qsar_datasets.dsstox_records dr where dr.casrn ='91-20-3'

select count(id) from qsar_datasets.dsstox_records dr where dr.casrn is not null


DO
$$BEGIN
   ALTER TABLE qsar_models.qsar_predicted_neighbors DROP CONSTRAINT uk35ahofu4p2rjpa82tnde6j1n9;
EXCEPTION
   WHEN undefined_object
      THEN NULL;  -- ignore the error
END$$;



create table qsar_models.dsstox_records (like qsar_datasets.dsstox_records including all);


insert into qsar_models.dsstox_records
select *
from qsar_datasets.dsstox_records;

DROP TABLE if exists qsar_datasets.dsstox_records cascade;

update qsar_models.dsstox_records set mol_image_png_available = true where fk_dsstox_snapshot_id=1





select count(pd.id) from qsar_models.predictions_dashboard pd
join qsar_models.models m on pd.fk_model_id = m.id
join qsar_models.sources s on m.fk_source_id = s.id
where s.name ='OPERA2.9'

delete from qsar_models.qsar_predicted_neighbors where id>0

delete from qsar_models.qsar_predicted_ad_estimates where id>0

delete from qsar_models.predictions_dashboard pd using qsar_models.models m
where  pd.fk_model_id=m.id and m.fk_source_id=1

select fileJson from qsar_models.prediction_reports pr
join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id
join qsar_models.models m on pd.fk_model_id = m.id
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
where dr.dtxcid='DTXCID101' and dr.fk_dsstox_snapshot_id=1 and m.name='WS' and m.fk_source_id=1

-- Find OPERA reports
select pr.id from qsar_models.prediction_reports pr
join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id
join qsar_models.models m on pd.fk_model_id = m.id
where m.fk_source_id=1;

-- Find OPERA neighbors
select pn.id from qsar_models.qsar_predicted_neighbors pn
join qsar_models.predictions_dashboard pd on pn.fk_predictions_dashboard_id = pd.id
join qsar_models.models m on pd.fk_model_id = m.id
where m.fk_source_id=1;

-- Find AD estimates
select pn.id from qsar_models.qsar_predicted_ad_estimates pn
join qsar_models.predictions_dashboard pd on pn.fk_predictions_dashboard_id = pd.id
join qsar_models.models m on pd.fk_model_id = m.id
where m.fk_source_id=1;

Delete from qsar_models.qsar_predicted_ad_estimates where id=2531;


Delete from qsar_models.qsar_predicted_neighbors where id=3370;



-- Find OPERA predictions
select pd.id,pd.canon_qsar_smiles from qsar_models.predictions_dashboard pd
join qsar_models.models m on pd.fk_model_id = m.id
where m.fk_source_id=1;

-- Delete OPERA predictions
delete from qsar_models.predictions_dashboard pd using qsar_models.models m
where pd.fk_model_id = m.id and m.fk_source_id=1;



select * from exp_prop.property_values pv
left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
join exp_prop.properties p on pv.fk_property_id = p.id
where p.name='Henry''s law constant' and ps.name!='Sander' and ps.name!='PubChem' and ps.name!='eChemPortalAPI' and ps.name!='OChem' and ps.name!='ICF' and ps.name!='OPERA'



SELECT setval('qsar_models.dsstox_records_id_seq', (SELECT MAX(id) FROM qsar_models.dsstox_records));

delete from qsar_models.dsstox_records dr where dr.dtxcid is null;

select count(id) from qsar_models.dsstox_records;

ALTER TABLE qsar_models.dsstox_records
DROP CONSTRAINT dsstox_records_dtxcid_fk_dsstox_snapshot_id_key;

alter table qsar_models.dsstox_other_casrns
drop constraint  dsstox_other_casrns_pk;

delete from qsar_models.dsstox_other_casrns oc where oc.casrn is not null;

select count(oc.id) from qsar_models.dsstox_other_casrns oc

select n.fk_predictions_dashboard_id,casrn,dtxsid,match_by from qsar_models.qsar_predicted_neighbors n where match_by like '%None%' order by match_by

show tables;

Select id from qsar_models.models m
join qsar_models.sources s on m.fk_source_id = s.id
where m.name='BP' and s.name='OPERA2.9'


select * from qsar_models.models where fk_source_id=1



alter table qsar_models.qsar_predicted_neighbors
DROP CONSTRAINT uko8am5915w4br3pqpj8igm9o2o;



update qsar_models.models set name=concat(name,' OPERA2.9') where fk_source_id=1




select * from qsar_models.qsar_predicted_neighbors n
join qsar_models.predictions_dashboard pd on n.fk_predictions_dashboard_id = pd.id
join qsar_models.models m on pd.fk_model_id = m.id
where pd.dtxcid ='DTXCID101' and m.name='CERAPP-Antagonist OPERA2.9'


select distinct(ps.name) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
    join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='Strongest acidic dissociation constant' and ps.type='CHEMPROP';
-- where p.name='Henry''s law constant';

select distinct(ls.name) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
    join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
where p.name='Strongest acidic acid dissociation constant';


SELECT pg_size_pretty( pg_database_size('res_qsar') );

SELECT pg_size_pretty( pg_total_relation_size('descriptor_values') );

SELECT pg_size_pretty( pg_total_relation_size('qsar_models.model_bytes') );
SELECT pg_size_pretty( pg_total_relation_size('qsar_models.predictions_dashboard') );

SELECT pg_size_pretty( pg_total_relation_size('qsar_models.prediction_reports') );

-- WHERE schemaname = 'exp_prop' or schemaname='qsar_datasets' or schemaname='qsar_descriptors' or schemaname='qsar_models'

SELECT pg_size_pretty(sum(pg_total_relation_size(quote_ident(schemaname) || '.' || quote_ident(tablename)))::bigint) FROM pg_tables
WHERE schemaname = 'qsar_descriptors'

delete from qsar_models.model_qmrfs q where id>0


update qsar_datasets.properties_in_categories


select p.id,p.name from qsar_datasets.properties p

select * from qsar_models.model_files

select p.id,pc.id, p.name,pc.name from qsar_datasets.properties_in_categories
join qsar_datasets.properties p on properties_in_categories.fk_property_id = p.id
join qsar_datasets.property_categories pc on properties_in_categories.fk_property_category_id = pc.id
where pc.name='Toxicity';

--Look at model_files
select mf.fk_model_id, mf.fk_file_type_id, m.dataset_name, ft.name, length(mf.file) from qsar_models.model_files mf
join qsar_models.file_types ft on mf.fk_file_type_id = ft.id
join qsar_models.models m on mf.fk_model_id = m.id
where m.fk_source_id=1 --OPERA
-- where m.fk_source_id=3 --cheminformatics modules
order by mf.fk_model_id, mf.fk_file_type_id;



select * from exp_prop.literature_sources

select distinct (p.name) from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
join exp_prop.properties p on pv.fk_property_id = p.id
where ps.name='OPERA'

select count(distinct (pv.id)) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where p.name='LogKOC'

select distinct(p.name) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where fk_public_source_id=12 order by p.name



select distinct pv.fk_public_source_id,pv.fk_literature_source_id from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where p.name='LogKmHL'


select distinct pv.fk_unit_id from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where p.id=6 and pv.value_point_estimate<0


select distinct pv.fk_public_source_id from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where p.name='Acidic pKa';


select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where p.name='Acidic pKa' and source_dtxsid is not null;


select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where p.name='Biodegradation half-life for hydrocarbons';

select count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where p.name='Acidic pKa';

select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where fk_unit_id=7 and p.name='Vapor pressure'

select count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='LogKmHL' and ps.name='OPERA'

select count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='Ready biodegradability' and ps.name='OPERA'


select sc.source_dtxsid,pv.value_point_estimate,  ls.citation from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
where p.name='Octanol water partition coefficient' and pv.fk_public_source_id=12
order by pv.updated_at desc;

select  count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
-- where p.name='Fraction unbound in human plasma'
where p.name='Oral rat LD50'
and pv.fk_public_source_id=12;


select distinct p.name, u.name from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.units u on pv.fk_unit_id = u.id
order by p.name, u.name;

select distinct p.name, u.name from qsar_datasets.datasets d
join qsar_datasets.properties p on d.fk_property_id = p.id
join qsar_datasets.units u on d.fk_unit_id = u.id
order by p.name, u.name;

delete from exp_prop.source_chemicals where fk_public_source_id=12 and created_by='tmarti02';

select * from exp_prop.property_values pv
where fk_public_source_id=12

select * from exp_prop.literature_sources
where created_by='tmarti02' and citation!='Goss et al. "The Partition Behavior of Fluorotelomer Alcohols and Olefins" Environ. Sci. Technol. 2006, 40, 11, 3572-3577 (<a href="https://doi.org/10.1021/es060004p" target="_blank">Environ. Sci. Technol. 2006, 40, 11, 3572-3577</a>)'
order by id asc


update exp_prop.literature_sources set name=concat(name,' (via OPERA)')
where created_by='tmarti02' and citation!='Goss et al. "The Partition Behavior of Fluorotelomer Alcohols and Olefins" Environ. Sci. Technol. 2006, 40, 11, 3572-3577 (<a href="https://doi.org/10.1021/es060004p" target="_blank">Environ. Sci. Technol. 2006, 40, 11, 3572-3577</a>)'

select * from exp_prop.source_chemicals sc
where sc.fk_public_source_id=12 and sc.source_dtxrid='DTXRID5016290245'

select * from exp_prop.property_values pv
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where pv.fk_property_id=6 and pv.fk_public_source_id=16  and value_point_estimate is not null;

select * from exp_prop.literature_sources ls
left join exp_prop.property_values pv on ls.id = pv.fk_literature_source_id
where pv.fk_literature_source_id is null


delete from exp_prop.source_chemicals sc where sc.fk_public_source_id=12 and created_by='tmarti02';

update exp_prop.property_values pv set fk_literature_source_id=null where fk_public_source_id=12;

delete from exp_prop.literature_sources where created_by='tmarti02' and name not like 'Goss%'

select sc.source_dtxsid,fk_property_id from exp_prop.property_values pv
        join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
         where document_name='KWOK,ESC & ATKINSON,R (1994)'

-- Get a list of units for each opera property:
select distinct p.name,u.name,pv.created_by from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.units u on pv.fk_unit_id = u.id
where fk_public_source_id=12 --and p.created_by='tmarti02'
order by p.name

select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
         join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where p.name='LogKow: Octanol-Water' and sc.source_chemical_name='N-MeFOSE Alcohol'


select count(pd.id) from qsar_models.predictions_dashboard pd
join qsar_models.models m on pd.fk_model_id = m.id
where m.fk_source_id=4


select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
-- where p.name='Henry''s law constant' and fk_public_source_id=12;
where p.name='Fish biotransformation half-life (Km)' --and fk_public_source_id=12;

-- See if any gabriel 3M records make it into a dataset
select distinct d.name from exp_prop.property_values pv
join qsar_datasets.data_point_contributors dpc on  exp_prop_property_values_id=pv.id
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where fk_public_source_id=16 and pv.created_by='gsincl01' ;


select distinct ls.name,pv.created_by from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.literature_sources ls on pv.fk_public_source_id = ls.id
where p.name='LogKoa: Octanol-Air'
order by ls.name;--and fk_public_source_id=12;


select distinct ps.name,pv.created_by from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='LogKoa: Octanol-Air'
order by ps.name;


select distinct ps.name,pv.created_by from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='LogKoa: Octanol-Air'order by ps.name;

select distinct u.name from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join  exp_prop.units u on pv.fk_unit_id = u.id
where p.name='LogKoa: Octanol-Air'

select count (pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where p.name='Henry''s law constant'  and pv.created_by='tmarti02';

select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where fk_property_id=8 and pv.created_by='tmarti02'
order by pv.id desc;


delete from exp_prop.property_values pv
where fk_property_id=8 and pv.created_by='tmarti02';



update exp_prop.property_values set fk_unit_id=28 where fk_unit_id=30;

update exp_prop.property_values set fk_property_id=30 where fk_property_id=34;

select distinct pv.fk_source_chemical_id  from exp_prop.property_values pv
                join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where pv.created_by='tmarti02' and sc.source_dtxrid is not null;



select distinct  p.id, ls.id, ls.name from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
where  p.name='LogKow: Octanol-Water'
order by ls.id;



select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
where p.name='LogKow: Octanol-Water' and ls.name='ANGUS Chemical Company (Chemical company)'
order by fk_source_chemical_id


select pv.id,pv.keep,pv.created_at,pv.created_by,value_point_estimate,fk_source_chemical_id from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where p.name='Henry''s law constant' and ls.name='Tetko et al. 2011'
order by sc.source_dtxrid,pv.created_at

select source_chemical_name from exp_prop.source_chemicals
where source_chemical_name like  '%00:%'

update exp_prop.source_chemicals set source_chemical_name=null, source_casrn=replace(source_chemical_name,'''',''),updated_by='tmarti02',updated_at=current_timestamp
where source_chemical_name like  '''%' and length(source_chemical_name)<15 and source_casrn is null


select * from exp_prop.source_chemicals
where source_chemical_name like  '''%' and length(source_chemical_name)<15 and source_casrn is null

select * from exp_prop.source_chemicals
where length(source_chemical_name)<15
order by length(source_chemical_name)


select pv.notes, pv.created_by, pv.keep, sc.id, sc.source_casrn,sc.source_chemical_name, pv.fk_property_id, pv.value_point_estimate from exp_prop.property_values pv
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where pv.fk_public_source_id=12 and sc.source_casrn is null and sc.source_dtxsid is null and fk_property_id!=33

select count(distinct sc.id) from exp_prop.property_values pv
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where pv.fk_public_source_id=12 and sc.source_dtxsid is null and fk_property_id!=33


select name, description,url from exp_prop.public_sources
where type='CHEMPROP'
order by name;


select name, 'journal article',doi,citation from exp_prop.literature_sources
order by name;

select sc.id,ps.name from exp_prop.property_values pv
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where pv.fk_public_source_id=10;


-- look at notes
select pv.id,pv.notes, pv.qc_flag from exp_prop.property_values pv
where pv.notes like '%Conversion to g/L not possible (need MW)%' and pv.id=15433;

select distinct pv.notes from exp_prop.property_values pv;

-- Look at page_urls to see if need updating:
select * from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where pv.fk_public_source_id!=15 and pv.fk_public_source_id!=6 and pv.fk_public_source_id!=9 and pv.fk_public_source_id!=14 and pv.fk_public_source_id!=13 and page_url is not null;

-- update page_urls for sander:
update exp_prop.property_values pv set page_url=REPLACE(page_url, 'http://satellite.mpic.de/henry/casrn/', 'https://henrys-law.org/henry/casrn/')
where pv.fk_public_source_id=15 and page_url is not null;





-- Store value_text which was accidentally not set during loading
update exp_prop.property_values pv
set value_text=pv.value_original
from exp_prop.properties p
where pv.fk_public_source_id=253 and p.name='Estrogen receptor antagonist' and pv.fk_property_id = p.id;

-- Look at property values
select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
where pv.fk_public_source_id=253 and p.name='Androgen receptor binding';


-- delete from exp_prop.property_values pv
-- where pv.fk_public_source_id=253 and pv.fk_property_id=40;


select distinct ls.name from exp_prop.property_values pv
join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id

select count(distinct (sc.source_dtxrid)) from exp_prop.property_values pv
join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id



select distinct ps.name from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id


INSERT into qsar_datasets.datasets_in_dashboard  (created_at,created_by,fk_property_id,fk_datasets_id) VALUES (CURRENT_TIMESTAMP,'tmarti02',61,226);

select * from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.properties p on d.fk_property_id = p.id
join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='Atmospheric hydroxylation rate' and ps.name='OPERA2.9'


select distinct p.name,pv.value_text,pv.value_point_estimate,ps.name from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name like '%Estrogen%' or p.name like '%Androgen%'
order by p.name


select count(pv.id)  from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name like '%Estrogen%' or p.name like '%Androgen%' and ps.name='OPERA2.9';
-- where ps.name='OPERA2.9';


-- delete from exp_prop.property_values pv
-- using exp_prop.properties p
-- where p.id=pv.fk_property_id and p.name like '%Estrogen%' or p.name like '%Androgen%';

update exp_prop.property_values pv set fk_unit_id=11 where fk_unit_id=31 and fk_public_source_id=249;

create table qsar_datasets.datasets_in_cheminformatics_modules as (select * from qsar_datasets.datasets_in_dashboard);

-- Compare model sizes
select m.id, dataset_name,m2.name,length(mb.bytes) from qsar_models.models m
join qsar_models.model_bytes mb on m.id = mb.fk_model_id
join qsar_models.methods m2 on m.fk_method_id = m2.id
where dataset_name like '%v1 modeling%' and splitting_name='RND_REPRESENTATIVE' and descriptor_set_name!='consensus' and fk_descriptor_embedding_id is not null
order by dataset_name,fk_method_id

select id, fk_model_id from qsar_models.model_bytes

select mf.fk_model_id,fk_file_type_id,length(mf.file) from qsar_models.model_files mf
where mf.fk_file_type_id<3;

select distinct pd.fk_model_id,m.name from qsar_models.predictions_dashboard pd
join qsar_models.models m on pd.fk_model_id = m.id
where m.name like '%TEST%'
order by pd.fk_model_id;


select count(pr.id) from qsar_models.prediction_reports pr
join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id
where pd.fk_model_id=223

delete from qsar_models.prediction_reports pr
using qsar_models.predictions_dashboard pd
where pd.fk_model_id>=223 and pd.fk_model_id<=240 and pr.fk_predictions_dashboard_id = pd.id;

delete from qsar_models.predictions_dashboard pd
where pd.fk_model_id>=223 and pd.fk_model_id<=240;


drop index qsar_models.ukly6h4arnnkjmf01ncj20v6086;

select count(pr.id) from qsar_models.prediction_reports pr
join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id
where pd.fk_model_id=223;

delete from qsar_models.predictions_dashboard pd
where pd.fk_model_id>=223 and pd.fk_model_id<=240;




alter table qsar_models.predictions_dashboard drop constraint fk52w7wh9e0tthby6tv27k1xnxn;

alter table qsar_models.predictions_dashboard drop constraint predictions_dashboard_dsstox_records_id_fk;



SELECT  * FROM  qsar_models.predictions_dashboard pd
WHERE  pd.fk_model_id>=223 and pd.fk_model_id<=240;

select distinct pd.fk_model_id from qsar_models.prediction_reports pr
join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id;

ALTER TABLE qsar_models.prediction_reports DROP CONSTRAINT fkalyfh136l0ytja2kcpnaq7v36;

drop index qsar_models.uknjon6q4t2xiwn39m7q7anjj5u;

select count(pr.id) from qsar_models.prediction_reports pr;

select count(pr.id) from qsar_models.predictions_dashboard pr;

select * from qsar_models.predictions_dashboard pd
limit 1;


select * from qsar_models.predictions_dashboard
    where  fk_model_id>=223 and fk_model_id<=240
order by id asc;




SELECT
    psa.pid,
    psa.query,
    psa.state,
    pg_locks.mode lock_mode,
    relation::regclass locked_relation
FROM
  pg_locks
  JOIN pg_stat_activity psa on pg_locks.pid = psa.pid
WHERE
  granted
  and 'qsar_models.predictions_dashboard'::regclass = relation;


SELECT
    pg_terminate_backend(24161)
FROM
  pg_locks
  JOIN pg_stat_activity psa on pg_locks.pid = psa.pid
WHERE
  granted
  and 'qsar_models.predictions_dashboard'::regclass = relation;


delete from qsar_models.prediction_reports pr where pr.id<1000000;


select pd.id from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
where pd.fk_model_id = m.id and m.fk_source_id=1;



delete from qsar_models.predictions_dashboard pd using qsar_models.models m
where pd.fk_model_id = m.id and m.fk_source_id=2;


select distinct prediction_error from qsar_models.predictions_dashboard;


update exp_prop.property_values set document_name=replace(document_name,'|?','');



select count(dr.id) from qsar_models.dsstox_records dr


select dr.dtxsid,dr.mol_weight from qsar_models.dsstox_records dr
where mol_weight is not null;

select count(distinct(sc.id)) from exp_prop.source_chemicals sc
where sc.fk_public_source_id=254;


select distinct(sc.id) from exp_prop.source_chemicals sc
where sc.fk_public_source_id=254 order by id;

delete from exp_prop.source_chemicals
where fk_public_source_id=254;

select dtxsid,dtxcid,mol_weight from qsar_models.dsstox_records where dtxcid is not null






select dp.qsar_dtxcid, dp.qsar_property_value, dv.values_tsv, dpis.split_num from qsar_datasets.data_points dp
join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id = dp.id
where dp.fk_dataset_id=277 and dv.fk_descriptor_set_id=6 and dpis.fk_splitting_id=1
order by dp.qsar_dtxcid;


select distinct dpc.dtxsid, dp.qsar_property_value, dv.values_tsv, dpis.split_num from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
    join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
join qsar_datasets.data_points_in_splittings dpis on dpis.fk_data_point_id = dp.id
where dp.fk_dataset_id=277 and dv.fk_descriptor_set_id=6 and dpis.fk_splitting_id=1
order by dpc.dtxsid;


-- Get overall set using all DTXSIDs
select distinct dpc.dtxsid, dp.qsar_property_value, dv.values_tsv from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
where dp.fk_dataset_id=277 and dv.fk_descriptor_set_id=6
order by dpc.dtxsid;

delete from qsar_datasets.data_points_in_splittings dis using qsar_datasets.data_points dp
where dp.id=dis.fk_data_point_id and dp.fk_dataset_id=277 and dis.id=1282939;


select * from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp  on dpc.fk_data_point_id = dp.id
where dp.fk_dataset_id=278 and dpc.dtxsid='DTXSID2022254';


select * from exp_prop.property_values pv
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where pv.fk_public_source_id=254 and sc.source_dtxsid='DTXSID2022254';


select p.name from qsar_datasets.datasets d
join qsar_datasets.properties p on d.fk_property_id = p.id
where d.name='exp_prop_96HR_FHM_LC50_v1 modeling'


select p.name from qsar_models.models m
join qsar_datasets.datasets d on m.dataset_name=d.name
join qsar_datasets.properties p on d.fk_property_id = p.id
where m.id=1066;




select distinct dpc.dtxsid,dp.canon_qsar_smiles, dp.qsar_property_value, dv.values_tsv from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
where dp.fk_dataset_id=285 and dv.fk_descriptor_set_id=6

order by dpc.dtxsid;


select * from exp_prop.property_values pv
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where sc.source_dtxsid='DTXSID1020778' and pv.fk_public_source_id=254;

select * from qsar_datasets.data_point_contributors dpc
where dpc.exp_prop_property_values_id=2755477;


select * from  exp_prop.property_values
where fk_public_source_id=254 AND keep=false and keep_reason='Toxicity value exceeds 10*baseline toxicity (logKow from XGB model'


select distinct p.name from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
join exp_prop.properties p on pv.fk_property_id = p.id
where ps.name='PubChem'


select distinct sc.source_chemical_name from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where ps.name='OChem'



CREATE TABLE categories (id SERIAL PRIMARY KEY,
    catname TEXT);


select  ps.name,count(pv.id) from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='Bioconcentration factor'
group by ps.name;

update exp_prop.property_values pv set value_text='Readily biodegradable' where fk_property_id=30 and value_point_estimate=1;
update exp_prop.property_values pv set value_text='Not readily biodegradable' where fk_property_id=30 and value_point_estimate=0;

select distinct  ps.name from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where pv.value_original like '%{%'


select distinct  ms.name from qsar_models.models m
         join qsar_models.models_in_model_sets mims on m.id = mims.fk_model_id
         join qsar_models.model_sets ms on mims.fk_model_set_id = ms.id
where m.is_public=true


select * from exp_prop.property_values pv
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where fk_property_id=6 and sc.source_casrn='375-22-4';




-- Datapoints for a dataset
select dp.id,canon_qsar_smiles, dp.qsar_property_value, u.abbreviation_ccd,qsar_dtxcid from qsar_datasets.data_points dp
join qsar_datasets.datasets d on d.id=dp.fk_dataset_id
join qsar_datasets.units u on d.fk_unit_id = u.id
where d.id=116;


select distinct canon_qsar_smiles from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id=dp.id
join qsar_datasets.datasets d on d.id=dp.fk_dataset_id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where d.id=116 and ps.name='OPERA2.8';

select fk_datasets_id from qsar_datasets.datasets_in_cheminformatics_modules dicm
join qsar_datasets.properties p on p.id=dicm.fk_property_id
where name='LogKow: Octanol-Water';


select * from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='Water solubility' and keep=false and keep_reason like '%Omit from manual literature check:%'


select count(sc.id) from exp_prop.source_chemicals sc
join exp_prop.public_sources ps on sc.fk_public_source_id = ps.id
where ps.name='OPERA2.8';


select distinct ps.name from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
where p.name='Boiling point' and keep=true;


update exp_prop.parameter_values pv set fk_unit_id=33 where fk_parameter_id=25;


select dp.canon_qsar_smiles,dv.id from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
left join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles
left join qsar_descriptors.descriptor_sets ds on dv.fk_descriptor_set_id = ds.id
where d.name='TTR_Binding_training_remove_bad_max_conc' and ds.name='WebTEST-default';


select distinct(dp.canon_qsar_smiles) from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where d.name='TTR_Binding_training_remove_bad_max_conc';


select m.id as modelId, m2.name as methodName, ROUND(ms.statistic_value::numeric,1) as RMSE_Training_CV, de.embedding_tsv,de.description from qsar_models.model_statistics ms
join qsar_models.models m on ms.fk_model_id = m.id
join qsar_models.statistics s on ms.fk_statistic_id = s.id
join qsar_models.methods m2 on m.fk_method_id = m2.id
left join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id = de.id
where m.dataset_name='TTR_Binding_training_remove_bad_max_conc' and s.name='RMSE_CV_Training'
order by m.id asc;


select p.canon_qsar_smiles,dp.qsar_property_value, qsar_predicted_value from qsar_models.predictions p
join qsar_datasets.data_points dp on dp.canon_qsar_smiles=p.canon_qsar_smiles
where p.fk_model_id=1128 and dp.fk_dataset_id=305;


select dp.canon_qsar_smiles,dp.qsar_property_value, dv.values_tsv, dpis.split_num from qsar_datasets.data_points dp
    join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
    join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
    join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
    join qsar_descriptors.descriptor_sets ds on dv.fk_descriptor_set_id = ds.id
    join qsar_datasets.splittings s on dpis.fk_splitting_id = s.id
where d.name='TTR_Binding_training_remove_bad_max_conc' and ds.name='WebTEST-default' and s.name='RND_REPRESENTATIVE_CV1';


select canon_qsar_smiles,qsar_property_value,replace(s.name,'RND_REPRESENTATIVE_CV','Fold') as Fold from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
join qsar_datasets.splittings s on dpis.fk_splitting_id = s.id
where fk_dataset_id=305 and s.name like '%CV%' and split_num=1
order by fold, canon_qsar_smiles;


select u.abbreviation from exp_prop.units u
where u.name='DEG_C';


select distinct ps.name from exp_prop.property_values pv
join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id
         where fk_property_id=27;

select sc.source_chemical_name  from exp_prop.property_values pv
join exp_prop.properties p on pv.fk_property_id = p.id
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
where p.name='96 hour scud LC50'



select dp.canon_qsar_smiles,qsar_property_value,values_tsv from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
join qsar_descriptors.descriptor_sets ds on dv.fk_descriptor_set_id = ds.id
where d.name='exp_prop_96HR_scud_v1 modeling' and ds.name='WebTEST-default';


select dp.canon_qsar_smiles,qsar_property_value,values_tsv from qsar_datasets.data_points dp
join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
where fk_dataset_id=402 and dv.fk_descriptor_set_id=6;



select dpc.dtxsid from qsar_datasets.data_point_contributors dpc
where dpc.dtxsid='DTXSID1028940';




select d.id,d.name,count(dp.id) from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id=d.id
where d.fk_property_id=84
group by d.name,d.id;

SELECT column_name
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = N'source_chemicals'


select * from exp_prop.parameter_values pv
join exp_prop.parameters p on pv.fk_parameter_id = p.id
    join exp_prop.property_values pv2 on pv.fk_property_value_id = pv2.id
join exp_prop.public_sources ps on pv2.fk_public_source_id = ps.id
         join exp_prop.properties p2 on pv2.fk_property_id = p2.id
where ps.name like '%PubChem_%' and p2.name='Henry''s law constant';


select count(id) from qsar_models.dsstox_records dr where fk_dsstox_snapshot_id=2;

delete from qsar_models.dsstox_records dr where fk_dsstox_snapshot_id=2;


VACUUM (ANALYZE, VERBOSE, FULL) qsar_models.dsstox_records;


select * from qsar_models.dsstox_records dr
where dr.fk_dsstox_snapshot_id=2 ;

-- Look for duplicate dtxsids
SELECT dtxsid, COUNT(dtxsid)
FROM qsar_models.dsstox_records dr
where dr.fk_dsstox_snapshot_id=2
GROUP BY dtxsid
HAVING COUNT(dtxsid) > 1

select * from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
where dr.fk_dsstox_snapshot_id=2 and dr.dtxsid='DTXSID90865029'


delete from qsar_models.predictions_dashboard pd
where pd.created_at>'2024-11-18';


delete from qsar_models.models m
where dataset_name like 'exp_prop_%LC50%modeling';

select m.id, m.dataset_name,m.descriptor_set_name,m2.name,m.fk_descriptor_embedding_id from qsar_models.models m
join qsar_models.methods m2 on m.fk_method_id = m2.id
left join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id = de.id
where m.dataset_name like 'exp_prop_%LC50%v5 modeling' and embedding_tsv is not null
order by m.dataset_name,descriptor_set_name,embedding_tsv;




select dataset_name,descriptor_set_name,splitting_name, m2.name,fk_descriptor_embedding_id from qsar_models.models m
join qsar_models.methods m2 on m.fk_method_id = m2.id

select distinct m2.name,m2.hyperparameter_grid from qsar_models.models m
join qsar_models.methods m2 on m.fk_method_id = m2.id
where dataset_name not like 'exp_prop_%LC50%modeling';



select distinct  m.hyperparameters,m2.name from qsar_models.models m
join qsar_models.methods m2 on m.fk_method_id = m2.id
where dataset_name like 'exp_prop_%LC50%modeling';


select n.dtxsid,n.casrn from qsar_models.qsar_predicted_neighbors n
left join qsar_models.dsstox_records dr on n.fk_dsstox_records_id = dr.id
where match_by='CASRN' and n.dtxsid is not null ;


select * from qsar_models.predictions_dashboard pd
join qsar_models.models m on pd.fk_model_id = m.id
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
where fk_source_id=6;


select pr.file_json,m.name from qsar_models.prediction_reports pr
join qsar_models.predictions_dashboard pd on pr.fk_predictions_dashboard_id = pd.id
join qsar_models.models m on pd.fk_model_id = m.id
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
where dr.dtxsid='DTXSID80943885' and dr.fk_dsstox_snapshot_id=2 ;


select smiles from qsar_models.dsstox_records dr
where fk_dsstox_snapshot_id=2


select * from qsar_models.dsstox_records where fk_dsstox_snapshot_id=2;


select * from qsar_models.models m where m.name like '%Martin%'


select embedding_tsv,m2.name from qsar_models.models m
join qsar_datasets.datasets d on m.dataset_name=d.name
join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id = de.id
join qsar_models.methods m2 on m.fk_method_id = m2.id
where d.name='WS v1 modeling' and m.splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is not null;

