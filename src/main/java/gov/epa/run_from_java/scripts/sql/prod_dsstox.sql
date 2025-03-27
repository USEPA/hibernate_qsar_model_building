select * from source_generic_substance_mappings sgsm
join source_substances s on sgsm.fk_source_substance_id = s.id
where connection_reason is null and curator_validated is null and s.fk_chemical_list_id=246;



select * from chemical_lists cl
where name like '%Arnot%';

select cl.name, count(sgsm.id) from chemical_lists cl
left join source_substances sc on cl.id = sc.fk_chemical_list_id
left join source_generic_substance_mappings sgsm on sc.id = sgsm.fk_source_substance_id
where name like 'exp_prop_2024_02_02_from_OPERA2.8%'
group by cl.name;



select * from source_generic_substance_mappings sgsm
join source_substances s on sgsm.fk_source_substance_id = s.id
join source_substance_identifiers ssi on s.id = ssi.fk_source_substance_id
where connection_reason is null and curator_validated is null and s.fk_chemical_list_id=246

select count(id) from other_casrns


select c.name,c.id from source_substances
join chemical_lists c on fk_chemical_list_id=c.id
where dsstox_record_id='DTXRID402663317'

select * from source_substances
where fk_chemical_list_id=246

select count(id) from compound_descriptor_sets where fk_descriptor_set_id=1500




select count(distinct dtxcid) from dsstox_qsar d where d.software_version='OPERA 2.8'

select distinct(d.software_version) from dsstox_qsar d


select dsstox_compound_id,smiles,mol_file,indigo_inchi from compounds where inchi is null and indigo_inchi is not null order by smiles

select dsstox_compound_id,smiles, jchem_inchi, inchi from compounds where inchi !=compounds.jchem_inchi

select c.id, dsstox_compound_id, gs.dsstox_substance_id,smiles, jchem_inchi_key, indigo_inchi_key from compounds c
left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
left join generic_substances gs on gsc.fk_generic_substance_id = gs.id;

select * from  compounds c
where c.dsstox_compound_id is null;


select count(c.id) from compounds c
left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
left join generic_substances gs on gsc.fk_generic_substance_id = gs.id;

select * from prod_qsar.compound_descriptor_sets where fk_descriptor_set_id=1448


SELECT DISTINCT prod_dsstox.generic_substances.dsstox_substance_id dtxsid,
                prod_dsstox.compounds.dsstox_compound_id as dtxcid,
                mol_formula
FROM prod_dsstox.compounds
         INNER JOIN prod_dsstox.compound_relationships
                    ON prod_dsstox.compound_relationships.fk_compound_id_successor = prod_dsstox.compounds.id
         INNER JOIN prod_dsstox.compound_relationships self_successor_relationships_compound_relationships
                    ON self_successor_relationships_compound_relationships.fk_compound_id_successor =
                       prod_dsstox.compound_relationships.fk_compound_id_successor
         INNER JOIN prod_dsstox.generic_substance_compounds
                    ON prod_dsstox.generic_substance_compounds.fk_compound_id =
                       self_successor_relationships_compound_relationships.fk_compound_id_predecessor
         INNER JOIN prod_dsstox.generic_substances
                    on generic_substances.id = prod_dsstox.generic_substance_compounds.fk_generic_substance_id
         INNER JOIN prod_dsstox.qc_levels ql
                    ON ql.id = generic_substances.fk_qc_level_id and ql.name not in ('Incomplete', 'Public_Untrusted')
WHERE compound_relationships.fk_compound_relationship_type_id = 2
  AND self_successor_relationships_compound_relationships.fk_compound_relationship_type_id = 2
order by dtxsid,dtxcid

select gs.dsstox_substance_id from compounds c
left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
left join generic_substances gs on gsc.fk_generic_substance_id = gs.id
where c.dsstox_compound_id is null

select cr.created_at, c.dsstox_compound_id, cr.fk_compound_id_predecessor,  cr.fk_compound_id_successor, c2.smiles, c2.mol_weight,c2.mol_formula, c2.jchem_inchi_key,c2.indigo_inchi_key from compounds c
left join compound_relationships cr on c.id = cr.fk_compound_id_predecessor
join compounds c2 on c2.id = cr.fk_compound_id_successor
where cr.fk_compound_relationship_type_id=2 -- type_id=2 gets the ms-ready successor
order by c.dsstox_compound_id, cr.created_at;



select count(c.dsstox_compound_id) from compounds c
left join compound_relationships cr on c.id = cr.fk_compound_id_predecessor
join compounds c2 on c2.id = cr.fk_compound_id_successor
where cr.fk_compound_relationship_type_id=2 -- type_id=2 gets the ms-ready successor
order by c.dsstox_compound_id, cr.created_at;

select count(id) from source_substances
where fk_chemical_list_id=2120;

select count(distinct (sgsm.id)) from source_generic_substance_mappings sgsm
join source_substances ss on sgsm.fk_source_substance_id = ss.id
where ss.fk_chemical_list_id=2120;

select * from source_generic_substance_mappings sgsm
join source_substances ss on sgsm.fk_source_substance_id = ss.id
where ss.fk_chemical_list_id=1968 order by external_id;


# Ochem 1 old list
select count(distinct (sgsm.id)) from source_generic_substance_mappings sgsm
join source_substances ss on sgsm.fk_source_substance_id = ss.id
where ss.fk_chemical_list_id=1990;


select external_id,ssi.identifier_type,ssi.identifier from source_generic_substance_mappings sgsm
join source_substances ss on sgsm.fk_source_substance_id = ss.id
join source_substance_identifiers ssi on ss.id = ssi.fk_source_substance_id
join chemical_lists cl on ss.fk_chemical_list_id = cl.id
where cl.name='exp_prop_2024_02_02_from_OChem_40000_1' and identifier_type not like '%_INCH%';

select dsstox_substance_id,s.identifier from generic_substances gs
join synonyms s on gs.id = s.fk_generic_substance_id


select * from generic_substances gs
join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
join compounds c on gsc.fk_compound_id = c.id


select e.name, gs.dsstox_substance_id from prod_chemprop.measured_properties mp
join prod_chemprop.endpoints e on mp.fk_endpoint_id = e.id
join prod_dsstox.source_substances ss on ss.id=mp.efk_dsstox_source_substance_id
join prod_dsstox.source_generic_substance_mappings sgsm on ss.id = sgsm.fk_source_substance_id
join prod_dsstox.generic_substances gs on sgsm.fk_generic_substance_id = gs.id
join prod_dsstox.chemical_lists cl on ss.fk_chemical_list_id = cl.id
join prod_chemprop.collection_details cd on mp.fk_measurement_method_id = cd.id
where cd.name='PhysPropNCCT' and sgsm.curator_validated=1
and connection_reason like '%molblock%'
order by  e.name, gs.dsstox_substance_id;



select cl.name as list_name, count(sgsm.id) as count_validated from prod_chemprop.measured_properties mp
join prod_dsstox.source_substances ss on ss.id=mp.efk_dsstox_source_substance_id
join prod_dsstox.source_generic_substance_mappings sgsm on ss.id = sgsm.fk_source_substance_id
join prod_dsstox.chemical_lists cl on ss.fk_chemical_list_id = cl.id
join prod_chemprop.collection_details cd on mp.fk_measurement_method_id = cd.id
where cd.name='PhysPropNCCT' and sgsm.curator_validated=1
group by cl.name;

select cl.name as list_name, count(sgsm.id) as count_not_validated from prod_chemprop.measured_properties mp
join prod_dsstox.source_substances ss on ss.id=mp.efk_dsstox_source_substance_id
join prod_dsstox.source_generic_substance_mappings sgsm on ss.id = sgsm.fk_source_substance_id
join prod_dsstox.chemical_lists cl on ss.fk_chemical_list_id = cl.id
join prod_chemprop.collection_details cd on mp.fk_measurement_method_id = cd.id
where cd.name='PhysPropNCCT' and sgsm.curator_validated=0
group by cl.name;

# select ss.id, gs.dsstox_substance_id, ss.external_id, ss.warnings, ss.dsstox_record_id,
#                    sgsm.connection_reason, sgsm.curator_validated, ss.qc_notes, sgsm.linkage_score,
#                    ssi.identifier, ssi.identifier_type, ssi.label, ssi.fk_source_substance_identifier_parent, ss.fk_chemical_list_id
select (gs.dsstox_substance_id),count(gs.dsstox_substance_id)
  from prod_dsstox.source_substances ss
  join prod_dsstox.source_generic_substance_mappings sgsm on ss.id = sgsm.fk_source_substance_id
  join prod_dsstox.generic_substances gs on gs.id = sgsm.fk_generic_substance_id
  join prod_dsstox.source_substance_identifiers ssi on ssi.fk_source_substance_id = ss.id
where ssi.identifier_type = 'CASRN'
#    and identifier ~ '^[[:digit:]]{2,7}-[[:digit:]]{2}-[[:digit:]]{1}$'
   and sgsm.curator_validated = 1
and identifier ='33795-24-3'
group by gs.dsstox_substance_id;


select count(gs.id) from generic_substances gs

select dsstox_compound_id, pubchem_cid from compounds c where c.pubchem_cid is not null and dsstox_compound_id is not null


select  dtxsid,canonical_qsarr from dsstox_qsar d
where d.software_version='OPERA 2.8';

select * from prod_dsstox.compounds c
where smiles is null


select * from generic_substances gs
join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
join compounds c on gsc.fk_compound_id = c.id
where c.pubchem_cid is null and c.id is not null;

select count(id) from source_substances where fk_chemical_list_id=2117;

select count(m.id) from source_generic_substance_mappings m
join source_substances ss  on m.fk_source_substance_id = ss.id
where ss.fk_chemical_list_id=2117;


select cl.id, cl.name, count(m.id) from source_generic_substance_mappings m
join source_substances ss  on m.fk_source_substance_id = ss.id
join chemical_lists cl on ss.fk_chemical_list_id = cl.id
where cl.name like '%exp_prop_2024_04_03%'
group by cl.name;




select gs.dsstox_substance_id,  m.name, e.name, qpp.result_value from prod_chemprop.qsar_predicted_properties qpp
join prod_dsstox.compounds c on qpp.efk_dsstox_compound_id=c.id
join prod_dsstox.generic_substance_compounds gsc on c.id = gsc.fk_compound_id
join prod_dsstox.generic_substances gs on gsc.fk_generic_substance_id = gs.id
join prod_qsar.models m on m.id=qpp.efk_qsar_model_id
join prod_chemprop.endpoints e on m.efk_chemprop_endpoint_id=e.id
-- # where gs.dsstox_substance_id='DTXSID3039242' and e.name='Water Solubility';
-- where gs.dsstox_substance_id='DTXSID3039242' and m.name like '%BCF%';
#     where gs.dsstox_substance_id='DTXSID6020143' and e.name like '%pKA%';
where e.name like '%pKA B%';




select m.name,count(qpp.id) from prod_chemprop.qsar_predicted_properties qpp
# join prod_dsstox.compounds c on qpp.efk_dsstox_compound_id=c.id
# join prod_dsstox.generic_substance_compounds gsc on c.id = gsc.fk_compound_id
# join prod_dsstox.generic_substances gs on gsc.fk_generic_substance_id = gs.id
join prod_qsar.models m on m.id=qpp.efk_qsar_model_id
# join prod_chemprop.endpoints e on m.efk_chemprop_endpoint_id=e.id
group by m.name;




select gs.dsstox_substance_id,  substance_type, gsc.relationship, c.chemical_type, c.organic_form, c.smiles, c2.smiles as qsar_smiles_opera from generic_substances gs
left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
left join compounds c on gsc.fk_compound_id = c.id
left join compound_relationships cr on c.id = cr.fk_compound_id_predecessor
left join compounds c2 on c2.id=cr.fk_compound_id_successor
where gs.dsstox_substance_id='DTXSID5041302';
# where gs.dsstox_substance_id='DTXSID7026029'
order by cr.created_at desc;
# where gs.dsstox_substance_id='DTXSID2047517';


    select gs.dsstox_substance_id,  substance_type, gsc.relationship, c.chemical_type, c.organic_form, c.smiles, c2.smiles as qsar_smiles_opera from generic_substances gs
				left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
				left join compounds c on gsc.fk_compound_id = c.id
				left join compound_relationships cr on c.id = cr.fk_compound_id_predecessor
				left join compounds c2 on c2.id=cr.fk_compound_id_successor
				where gs.dsstox_substance_id='DTXSID9020794'
				order by cr.created_at desc;



    select gs.dsstox_substance_id, c.dsstox_compound_id,  substance_type, gsc.relationship, c.chemical_type, c.organic_form, c.smiles from generic_substances gs
				left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
				left join compounds c on gsc.fk_compound_id = c.id
				where gs.dsstox_substance_id='DTXSID9020794'



# SELECT gs.updated_at,dsstox_compound_id,smiles, mol_weight,
#        gs.dsstox_substance_id, gs.casrn
SELECT count(gs.id)
FROM compounds c
left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
-- where gs.updated_at > '2023-04-01'
  where gs.updated_at < '2024-11-12'
#   where gs.dsstox_substance_id='DTXSID101377893'
  and gsc.relationship = "Tested Chemical"
ORDER BY gs.updated_at desc
-- LIMIT "+limit+" OFFSET "+offset




SELECT *
FROM compounds c
left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
where
gs.updated_at >= '2023-04-04' and gs.updated_at < '2024-11-12' and gsc.relationship = "Tested Chemical"
and gs.dsstox_substance_id='DTXSID00202466'
ORDER BY dsstox_compound_id
LIMIT 50000 OFFSET 0;



SELECT gs.updated_at,gsc.relationship
FROM compounds c
left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
where gs.dsstox_substance_id='DTXSID00202466';

select oc.casrn, gs.dsstox_substance_id from other_casrns oc
join generic_substances gs on oc.fk_generic_substance_id = gs.id
where gs.updated_at>='2024-11-12';


select c.smiles from generic_substances gs
left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
left join compounds c on gsc.fk_compound_id = c.id
# where c.dsstox_compound_id='DTXCID001744318'
where gs.dsstox_substance_id='DTXSID001103405';


SELECT * FROM compounds c
left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
# where gs.dsstox_substance_id='DTXSID90865029';
where c.dsstox_compound_id='DTXCID30679783' and gsc.id is not null;


# Can you have multiple cids with the same sid?
SELECT dsstox_substance_id, COUNT(dsstox_compound_id)
FROM compounds c
join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
join generic_substances gs on gsc.fk_generic_substance_id = gs.id
GROUP BY dsstox_substance_id
HAVING COUNT(dsstox_compound_id) > 1;


# Can you have multiple sids with the same cid?
SELECT dsstox_compound_id, COUNT(dsstox_substance_id)
FROM compounds c
join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
join generic_substances gs on gsc.fk_generic_substance_id = gs.id
GROUP BY dsstox_compound_id
HAVING COUNT(dsstox_substance_id) > 1;


# Can you have multiple sids with the same cid?
SELECT dsstox_compound_id, COUNT(dsstox_substance_id)
FROM generic_substances gs
join generic_substance_compounds gsc on gsc.fk_generic_substance_id = gs.id
join compounds c on gsc.fk_compound_id = c.id
GROUP BY dsstox_compound_id
HAVING COUNT(dsstox_substance_id) > 1;


SELECT dsstox_substance_id
FROM generic_substances gs
left join generic_substance_compounds gsc on gsc.fk_generic_substance_id = gs.id
where gsc.id is null;


select  am.name,qpad.applicability_value from prod_chemprop.qsar_predicted_properties qpp
join prod_dsstox.compounds c on c.id=qpp.efk_dsstox_compound_id
join prod_dsstox.generic_substance_compounds gsc on c.id = gsc.fk_compound_id
join prod_dsstox.generic_substances gs on gsc.fk_generic_substance_id = gs.id
join prod_qsar.models m on qpp.efk_qsar_model_id=m.id
join prod_chemprop.qsar_prediction_ad_estimates qpad on qpp.id = qpad.fk_qsar_predicted_property_id
join prod_qsar.ad_methods am on am.id=qpad.efk_qsar_ad_method_id
where gs.dsstox_substance_id='DTXSID7020182' and m.name = 'OPERA_PKAA';



SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,
		gs.dsstox_substance_id, gs.casrn, gs.preferred_name,  gs.updated_at,gsc.relationship
		FROM compounds c
		left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
		left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
		where  gsc.relationship != 'Tested Chemical';


SELECT distinct gsc.relationship
		FROM compounds c
		left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
		left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
		where  gsc.relationship != 'Tested Chemical';


SELECT dsstox_substance_id, dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,
		gs.dsstox_substance_id, gs.casrn, gs.preferred_name,  gs.updated_at,gsc.relationship
		FROM compounds c
		left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
		left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
# 		where  dsstox_substance_id='DTXSID50281842';
		where  dsstox_substance_id='DTXSID0050479';

# select c.dsstox_compound_id, gs.dsstox_substance_id,c.smiles, cds.descriptor_string_tsv from prod_qsar.compound_descriptor_sets cds
select gs.dsstox_substance_id,cds.descriptor_string_tsv from prod_qsar.compound_descriptor_sets cds
    join compounds c on cds.efk_dsstox_compound_id = c.id
left join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
join prod_qsar.descriptor_sets dsc on cds.fk_descriptor_set_id = dsc.id
where dsc.name='toxprints-latest';




SELECT dsstox_substance_id,casrn, preferred_name, gs.updated_at FROM generic_substances gs
		left join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
		left join compounds c on gsc.fk_compound_id = c.id
# 		where  dsstox_substance_id='DTXSID50281842';
		where  c.id is null;



SELECT casrn, preferred_name, smiles FROM compounds c
left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id
left join generic_substances gs on gs.id=gsc.fk_generic_substance_id
where gs.dsstox_substance_id='DTXSID101382351';


select distinct  ssi.identifier_type,ssi.label  from prod_dsstox.generic_substances gs
  join prod_dsstox.source_generic_substance_mappings sgsm on sgsm.fk_generic_substance_id = gs.id
  join prod_dsstox.source_substances ss on ss.id = sgsm.fk_source_substance_id
  join prod_dsstox.chemical_lists cl on cl.id = ss.fk_chemical_list_id
  join prod_dsstox.source_substance_identifiers ssi on ssi.fk_source_substance_id = ss.id
   where lower(cl.name) in ('trirelease', 'scdm', 'nemilist')
    and ssi.identifier_type = 'EXTERNAL_LINK_ID';


select distinct  ssi.identifier_type,ssi.label  from prod_dsstox.source_substance_identifiers ssi
where identifier_type='EXTERNAL_LINK_ID';


SELECT dsstox_substance_id,casrn, preferred_name FROM generic_substances gs where casrn  in ('71-43-2','91-20-3','129-00-0');


select oc.casrn, dsstox_substance_id,gs.preferred_name from other_casrns oc
join generic_substances gs on oc.fk_generic_substance_id = gs.id
where oc.casrn='1001914-35-7';


SELECT dsstox_substance_id,casrn, preferred_name, c.mol_weight,c.smiles FROM generic_substances gs
join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id
join compounds c on gsc.fk_compound_id = c.id
where dsstox_substance_id='DTXSID00943887'


