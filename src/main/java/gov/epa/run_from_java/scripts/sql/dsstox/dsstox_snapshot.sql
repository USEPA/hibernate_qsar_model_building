 update dsstox_qsar set software_version='OPERA' where original_smiles is not null

select smiles, mol_formula, length(mol_formula) lenformula from compounds
where mol_formula is not null
order by lenformula desc



update dsstox_msready set software_version='OPERA' where dsstox_msready.software_version is null


 select dtxsid,original_smiles, canonical_qsarr,smiles from dsstox_qsar where smiles != canonical_qsarr and dsstox_qsar.original_smiles like '%.%'

 select length(q.dtxcid) as lenCID,q.dtxsid, q.dtxcid,q.software_version,updated_by  from dsstox_msready q order by lenCID desc
 select length(q.dtxsid) as bob,q.dtxsid  from dsstox_msready q order by bob desc

SELECT dtxcid,dtxsid,software_version,COUNT(*)
FROM dsstox_msready
GROUP BY dtxcid,dtxsid,software_version
HAVING COUNT(*) > 1

select * from dsstox_msready where dtxcid like '%_%'


select * from dsstox_msready where dtxcid like '%\_%' and software_version = 'OPERA 2.8'

select * from dsstox_msready where dtxsid='DTXSID8040438' and software_version='OPERA'

# select * from dsstox_msready where dtxcid not like '%_%' and software_version='OPERA'

select * from dsstox_msready where  software_version='OPERA' order by dtxsid

delete from dsstox_msready where software_version = 'OPERA 2.8'

select COUNT(d.dtxsid) from dsstox_msready d where d.software_version = 'OPERA 2.8'

select COUNT(d.dtxsid) from dsstox_qsar d where d.software_version = 'OPERA 2.8'

select * from dsstox_qsar

select * from dsstox_qsar where dtxcid='DTXCID40196532' and software_version='OPERA 2.8'


select software_version, original_smiles, canonical_qsarr, dtxsid, dtxcid from dsstox_qsar order by dtxcid, software_version

select count(id) from compound_relationships where source = 'Kamel Workflow 202202 v2'

select count(id) from compound_relationships where source = 'Kamel Workflow 20191222 v2 cg'

select count(id) from compound_relationships where source = 'Kamel Workflow 20191222 v2'

SELECT source, COUNT(source)
FROM compound_relationships
where relationship='2D-QSAR-Ready'
GROUP BY source ;


select distinct  source from compound_relationships

# count where indigo inchiKeys match (959311)
select count(q.dtxcid) from dsstox_qsar q
join compounds on compounds.indigo_inchi_key=q.InChI_Key_QSARr
where software_version='OPERA 2.8'

# count where dont have match (254205)
select count(q.dtxcid) from dsstox_qsar q
left join compounds on compounds.indigo_inchi_key=q.InChI_Key_QSARr
where software_version='OPERA 2.8' and compounds.indigo_inchi_key is null

# Dont have a hit
select q.dtxcid, q.original_smiles,q.canonical_qsarr from dsstox_qsar q
left join compounds on compounds.indigo_inchi_key=q.InChI_Key_QSARr
where software_version='OPERA 2.8' and compounds.indigo_inchi_key is null

# count where jchem inchiKeys match (832062)
select count(q.dtxcid) from dsstox_qsar q
join compounds on compounds.jchem_inchi_key=q.InChI_Key_QSARr
where software_version='OPERA 2.8'


select c.dsstox_compound_id,gs.dsstox_substance_id, chemspider_id from compounds c
join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
join generic_substances gs on gsc.fk_generic_substance_id = gs.id
where chemspider_id is not null


select c.dsstox_compound_id,gs.dsstox_substance_id, chemspider_id from compounds c
join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
join generic_substances gs on gsc.fk_generic_substance_id = gs.id
where chemspider_id is not null
ORDER BY c.dsstox_compound_id
LIMIT 10 OFFSET 0

select count(c.dsstox_compound_id) from compounds c
left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
left join generic_substances gs on gsc.fk_generic_substance_id = gs.id
where chemspider_id is not null and gs.dsstox_substance_id is null

select c.dsstox_compound_id from compounds c
left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
left join generic_substances gs on gsc.fk_generic_substance_id = gs.id
where chemspider_id is not null and gs.dsstox_substance_id is null

select * from compounds c
left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
left join generic_substances gs on gsc.fk_generic_substance_id = gs.id
where gs.dsstox_substance_id='DTXSID70892974';



select * from dsstox_qsar d where d.software_version='OPERA'

update dsstox_qsar d set updated_by='tmarti02'
where d.software_version='OPERA 2.6'

select * from dsstox_qsar d
where d.software_version='OPERA 2.6'

select * from dsstox_qsar d
where d.software_version!='OPERA 2.6' and d.software_version!='OPERA 2.8'


select count(id) from compounds where chemspider_id is not null

select count(id) from chemspider_ids where version='v2'

SELECT cs.dsstox_compound_id,c.chemspider_id
FROM chemspider_ids cs
LEFT JOIN compounds c ON c.dsstox_compound_id = cs.dsstox_compound_id
WHERE cs.version='v2' and c.chemspider_id is null


SELECT dsstox_compound_id, COUNT(*) AS num
FROM  chemspider_ids
where version='v2'
GROUP BY  dsstox_compound_id
order by num desc





select * from dsstox_qsar d
where d.software_version='OPERA 2.8'

select count(canonical_qsarr) from dsstox_qsar d where software_version='OPERA 2.8'

select count(canonical_qsarr) from dsstox_qsar d where software_version='OPERA 2.6'


select count(c.dsstox_compound_id) from compounds c
where chemspider_id is not null

select count(id) from chemspider_ids
where version='v1'

select chemspider_id from compounds where dsstox_compound_id='DTXCID001399274'

# select * from compounds where updated_by='tmarti02'

select count(id) from compounds where chemspider_id is not null

delete from chemspider_ids where version='v1'

select * from compounds where indigo_inchi_key is null and jchem_inchi_key is not null


select smiles from compounds where indigo_inchi_key is null

select dsstox_compound_id,smiles,indigo_inchi_key, jchem_inchi_key,created_by,created_at, updated_by,updated_at from compounds where indigo_inchi_key='HQAXQLUVPWIKNO-LPBINRMYSA-N'



SELECT dsstox_compound_id,mol_file,smiles, inchi, jchem_inchi_key,indigo_inchi_key,mol_weight,gs.dsstox_substance_id, gs.casrn
FROM compounds c
left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
where (mol_formula is null) or (mol_formula like '%F%')
ORDER BY dsstox_compound_id;


SELECT count(dsstox_compound_id)
FROM compounds c
left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
where (mol_formula is null) or (mol_formula like '%F%')
ORDER BY dsstox_compound_id


select count(id) from compounds

select count(id) from compound_descriptor_sets

delete from compound_descriptor_sets where id>735

select count(id) from compound_descriptor_sets;


update compound_descriptor_sets set fk_descriptor_set_id=1500 where fk_descriptor_set_id=1445;


SELECT dq.canonical_qsarr
FROM compounds c
left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
left join dsstox_qsar dq on dtxsid=gs.dsstox_substance_id
where (mol_formula is not null) and gs.dsstox_substance_id in ('DTXSID9020112','DTXSID1020560','DTXSID90626970')
and software_version='OPERA 2.8'

SELECT table_name
FROM information_schema.tables
WHERE table_type='BASE TABLE'


select dsstox_compound_id, inchi,indigo_inchi,jchem_inchi,indigo_inchi_key,jchem_inchi_key from compounds where compounds.jchem_inchi_key is null and inchi is not null


select dsstox_compound_id, inchi,indigo_inchi,jchem_inchi,indigo_inchi_key,jchem_inchi_key from compounds where compounds.jchem_inchi is not null and indigo_inchi_key is not null


select * from compounds where dsstox_compound_id='DTXCID00819957'


select count(names.dtxcid) from dsstox_names names where names.software_version='ACD/Name Batch 2020.2.1'

select count(id) from compounds

select count(id) from compounds where acd_index_name is not null

select count(dsstox_compound_id) from compounds where acd_index_name is null

select dsstox_compound_id,smiles from compounds where acd_index_name is null



select count(id) from compounds where compounds.acd_iupac_name is not null


select * from compounds c
join dsstox_names n on dtxcid=c.dsstox_compound_id
where c.acd_iupac_name is not null and n.IUPAC_Name is null

select dsstox_compound_id,smiles, mol_weight,indigo_inchi_key,jchem_inchi_key from compounds c where (c.acd_iupac_name is null and c.acd_index_name is null)

delete from dsstox_names where filename='snapshot_compounds12_NAMES.sdf';
delete from dsstox_names where filename='snapshot_compounds27_NAMES.sdf';

select count(dtxcid) from dsstox_names where filename='snapshot_compounds12_NAMES.sdf' or filename='snapshot_compounds27_NAMES.sdf';


select c.dsstox_compound_id,c.smiles, n.smiles,c.acd_iupac_name,c.acd_index_name,
       n.IUPAC_Name,n.INDEX_Name, filename from compounds c
join dsstox_names n on dtxcid=c.dsstox_compound_id
where c.acd_iupac_name is not null and n.IUPAC_Name is null

select id, c.dsstox_compound_id,c.acd_index_name,acd_iupac_name from compounds c

# Null out old acd names:
UPDATE compounds SET acd_iupac_name = null, acd_index_name=null WHERE id>=1

Update compounds set acd_iupac_name = NULL,acd_index_name = NULL;

update dsstox_names set updated_by='tmarti02' where created_by='tmarti02'


delete from dsstox_names where created_by='tmarti02' and software_version='ACD/Name Batch 2020.2.1';

select count(created_by) from dsstox_names where created_by='tmarti02' and software_version='ACD/Name Batch 2020.2.1';

select length(acd_iupac_name) as bob, dsstox_compound_id from compounds order by bob desc


#update compounds table from dsstox_names table:
Update compounds c
inner join dsstox_names n on n.dtxcid=c.dsstox_compound_id
set c.acd_index_name=n.INDEX_Name,c.acd_iupac_name=n.IUPAC_Name, c.updated_by='tmarti02'
where software_version='ACD/Name Batch 2020.2.1';

# Count of records where we have a new name record but it's null:
select count(c.id) from  compounds c
left join dsstox_names n on n.dtxcid=c.dsstox_compound_id
where  n.INDEX_Name is null and software_version='ACD/Name Batch 2020.2.1';


select count(c.id) from  compounds c
left join dsstox_names n on n.dtxcid=c.dsstox_compound_id
where  n.IUPAC_Name is null and software_version='ACD/Name Batch 2020.2.1';

select count(c.id) from  compounds c
left join dsstox_names n on n.dtxcid=c.dsstox_compound_id
where  n.INDEX_Name is not null and software_version='ACD/Name Batch 2020.2.1';


# Count of records where we dont have a new name record:
select count(c.id)  from  compounds c
left join dsstox_names n on n.dtxcid=c.dsstox_compound_id
where  n.dtxcid is null;

select *  from  compounds c
left join dsstox_names n on n.dtxcid=c.dsstox_compound_id
where  n.dtxcid is null

select *  from  compounds c
where inchi like '%AUX%'
ORDER BY dsstox_compound_id


select c.dsstox_compound_id,c.smiles from  compounds c
left join dsstox_names n on n.dtxcid=c.dsstox_compound_id
where  n.INDEX_Name is null and software_version='ACD/Name Batch 2020.2.1';


select count(dtxcid) from dsstox_names where filename='snapshot_compounds35_NAMES.sdf'

# Get records where inchi is null but have indigo_inchi:
select dsstox_compound_id,smiles,indigo_inchi from compounds where inchi is null and indigo_inchi is not null  order by smiles

#use indigo_inchi to fill in missing inchi (only about 24 of these):
update compounds set inchi=indigo_inchi where inchi is null and indigo_inchi is not null and mol_weight is not null

# How many indigo_inchi_keys we have
select count(id) from compounds where indigo_inchi_key is not null
select count(id) from compounds where indigo_inchi_key is null
select count(id) from compounds where jchem_inchi_key is null

select count(id) from compounds where indigo_inchi is not null;

select dsstox_compound_id,indigo_inchi,smiles from compounds where indigo_inchi_key is not null and mol_weight is null

select smiles from compounds where indigo_inchi_key is not null and jchem_inchi_key is null order by smiles

Select c.dsstox_compound_id,gs.dsstox_substance_id,gs.preferred_name,replace(cds.descriptor_string_tsv,'\t','') as TOXPRINTS_FINGERPRINT, gs.casrn,
       if(c.jchem_inchi_key is null,c.indigo_inchi_key,c.jchem_inchi_key) as inchikey,c.smiles,cds.descriptor_string_tsv from compound_descriptor_sets cds
join compounds c on c.id=cds.efk_dsstox_compound_id
left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
where cds.fk_descriptor_set_id=1500 limit 10;


select gs.dsstox_substance_id,gs.preferred_name,gs.casrn,c.jchem_inchi_key,c.indigo_inchi_key,c.smiles,cds.descriptor_string_tsv from compound_descriptor_sets cds
join compounds c on c.id=cds.efk_dsstox_compound_id
left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
where cds.fk_descriptor_set_id=1500 limit 10 offset 100;


Select fk_generic_substance_id,
       fk_source_substance_id,
       connection_reason,
       linkage_score,
       curator_validated,
       qc_notes,
       count(*) as occurrence_count
from source_generic_substance_mappings
group by fk_generic_substance_id,
         fk_source_substance_id,
         connection_reason,
         linkage_score,
         curator_validated,
         qc_notes

having occurrence_count > 1
order by fk_source_substance_id



select count(id) from other_casrns

select oc.casrn,gs.dsstox_substance_id from other_casrns oc
join generic_substances gs on gs.id = oc.fk_generic_substance_id

select c.mol_image_png from compounds c
where dsstox_compound_id='DTXCID001782423'

select dsstox_substance_id, casrn,preferred_name from generic_substances gs
left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
where gsc.id is null

select distinct (software_version) from dsstox_qsar

select count(dtxcid) from dsstox_names where software_version='ACD/Name Batch 2020.2.1'

select count(dtxcid) from dsstox_qsar where software_version='OPERA 2.8'

select count(dtxcid) from dsstox_names_r2 where software_version='ACD/Name Batch 2020.2.1'

select created_at,created_by from dsstox_names_r2 order by created_at desc


select c.id, dsstox_compound_id, gs.dsstox_substance_id,smiles, jchem_inchi_key, indigo_inchi_key from compounds c
left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
left join generic_substances gs on gsc.fk_generic_substance_id = gs.id
where c.dsstox_compound_id is null;

select c.id, dsstox_compound_id, gs.dsstox_substance_id,smiles, jchem_inchi_key, indigo_inchi_key from compounds c
left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
left join generic_substances gs on gsc.fk_generic_substance_id = gs.id



select count(c.id) from compounds c
left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
left join generic_substances gs on gsc.fk_generic_substance_id = gs.id;

select smiles, jchem_inchi_key from compounds where jchem_inchi_key is not null and jchem_inchi_key!='' limit 20


select gs.dsstox_substance_id as dtxsid, oc.casrn, oc.cas_type,oc.source from other_casrns oc
join generic_substances gs on oc.fk_generic_substance_id = gs.id

select distinct(software_version) from dsstox_names n


select cr.created_at, c.dsstox_compound_id, cr.fk_compound_id_predecessor,  cr.fk_compound_id_successor, c2.smiles, c2.mol_weight,c2.mol_formula, c2.jchem_inchi_key,c2.indigo_inchi_key from compounds c
left join compound_relationships cr on c.id = cr.fk_compound_id_predecessor
join compounds c2 on c2.id = cr.fk_compound_id_successor
where cr.fk_compound_relationship_type_id=2 -- type_id=2 gets the ms-ready successor
order by c.dsstox_compound_id, cr.created_at;


select count(c.dsstox_compound_id) from compounds c
left join compound_relationships cr on c.id = cr.fk_compound_id_predecessor
join compounds c2 on c2.id = cr.fk_compound_id_successor
where cr.fk_compound_relationship_type_id=2  and c2.indigo_inchi_key is null-- type_id=2 gets the ms-ready successor
order by c.dsstox_compound_id, cr.created_at


select count(gs.id) from generic_substances gs

select c.mol_file from compounds c
where c.dsstox_compound_id='DTXCID001776000'


select c.mol_file from compounds c where c.dsstox_compound_id='DTXCID201765776';


select gs.dsstox_substance_id as "DTXSID",
      c.smiles as "SMILES" FROM generic_substances gs
      left join generic_substance_compounds gsc on gs.id=gsc.fk_generic_substance_id left join compounds c on gsc.fk_compound_id = c.id
      where gs.updated_at < '2024-11-12' and c.smiles is not null and gsc.relationship = 'Tested Chemical' order by gs.updated_at desc;


select gs.dsstox_substance_id as "DTXSID",gsc.relationship,
      c.smiles as "SMILES" FROM generic_substances gs
      left join generic_substance_compounds gsc on gs.id=gsc.fk_generic_substance_id left join compounds c on gsc.fk_compound_id = c.id
where relationship!='Tested Chemical';



select * from generic_substances gs
left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
left join compounds c on gsc.fk_compound_id = c.id
left join compound_relationships cr on c.id = cr.fk_compound_id_predecessor
left join compounds c2 on cr.fk_compound_id_successor = c2.id
where cr.fk_compound_relationship_type_id=1 and gs.dsstox_substance_id='DTXSID6021240'


select * from generic_substances gs
left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
left join compounds c on gsc.fk_compound_id = c.id
where c.dsstox_compound_id='DTXCID001744318';

select * from generic_substances gs
where gs.dsstox_substance_id in ('DTXSID00955829','DTXSID101377881','DTXSID101377974','DTXSID301378059','DTXSID30894510','DTXSID401378169','DTXSID40966212','DTXSID501011663','DTXSID501377819',
'DTXSID501377821','DTXSID501377883','DTXSID501378112','DTXSID601378034','DTXSID601378141','DTXSID70657580','DTXSID801378030','DTXSID8027905');




select * from compounds c
where c.dsstox_compound_id='DTXCID001744318';


