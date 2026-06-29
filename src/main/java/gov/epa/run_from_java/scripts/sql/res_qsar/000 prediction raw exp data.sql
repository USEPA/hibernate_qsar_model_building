select	pv.id as property_values_id,
--		dp.canon_qsar_smiles, 
		sc.source_chemical_name,
		sc.source_smiles,sc.source_casrn,		
--		dr.preferred_name as mapped_name,
--		dr.smiles as mapped_smiles, 
--		dr.casrn as mapped_casrn, 
		p.name_ccd  as property_name, 
		dpc.property_value			as property_value,
		u.abbreviation_ccd 			as property_units,		
	    ps."name"                 as public_source_name,
		ps.description            as public_source_description,
		ps.url                    as public_source_url,
		pv.page_url 				as direct_url,
		ls."name" 					as literature_source_name,
		ls.citation 				as literature_source_citation,
	   	ls.doi						as literature_source_doi,
	   	pv.document_name			as brief_citation,--From OPERA2.9 usually
	    ps2."name"					as public_source_original_name, --For sources like toxval,pubchem, sander
        ps2.description				as public_source_original_description,
        ps2.url						as public_source_original_url
from qsar_datasets.datasets d
join qsar_datasets.properties p on p.id=d.fk_property_id 
join qsar_datasets.data_points dp on dp.fk_dataset_id =d.id
join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id =dp.id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor 
join exp_prop.property_values pv on pv.id = dpc.exp_prop_property_values_id 
join exp_prop.source_chemicals sc on sc.id=pv.fk_source_chemical_id 
--left join qsar_models.dsstox_records dr on dr.dtxsid = dpc.dtxsid 
left join exp_prop.literature_sources ls on ls.id=pv.fk_literature_source_id 
left join exp_prop.public_sources ps on ps.id=pv.fk_public_source_id
left join exp_prop.public_sources ps2 on ps2.id=pv.fk_public_source_original_id 
--where d.name='LogP v1 modeling' and dp.canon_qsar_smiles ='C1C=CC=CC=1' and dr.fk_dsstox_snapshot_id =3;
where d.name='LogP v1 modeling' and dp.canon_qsar_smiles ='C1C=CC=CC=1';


-- get exp_prop.property_values ids:
SELECT dpc.exp_prop_property_values_id, dp.canon_qsar_smiles     
from qsar_datasets.datasets d
join qsar_datasets.properties p on p.id=d.fk_property_id 
join qsar_datasets.data_points dp on dp.fk_dataset_id =d.id
join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id =dp.id
join exp_prop.property_values pv on pv.id = dpc.exp_prop_property_values_id 
--where d.name='BP v1 modeling' and dp.canon_qsar_smiles ='C1C=CC=CC=1';
where d.name='WS v1 modeling' and pv.page_url is not null;


-- get parameters
SELECT pv.id as property_values_id, p.name, pv2.value_point_estimate, 
pv2.value_min,pv2.value_max ,pv2.value_text,pv2.value_error, 
pv2.value_qualifier, u2.abbreviation_ccd
from qsar_datasets.datasets d
join qsar_datasets.data_points dp on dp.fk_dataset_id =d.id
join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id =dp.id
join exp_prop.property_values pv on pv.id = dpc.exp_prop_property_values_id 
join exp_prop.parameter_values pv2 on pv2.fk_property_value_id  = pv.id
join exp_prop.parameters p on p.id=pv2.fk_parameter_id 
join exp_prop.units u on u.id=pv2.fk_unit_id
left join qsar_datasets.units u2 on u2.name=u.name
where d.name='BP v1 modeling' and dp.canon_qsar_smiles ='C1C=CC=CC=1';


select dp.qsar_dtxcid  from qsar_datasets.datasets d
join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id
where d.name = 'BP v1 modeling'  and dp.canon_qsar_smiles ='CC1C=CC2CC(C)CCC=2C=1';

select dp.qsar_dtxcid  from qsar_datasets.datasets d
            join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id
            where d.name = ''  and dp.canon_qsar_smiles %(qsarSmiles)s;



SELECT dp.canon_qsar_smiles as "canonicalSmiles", dp.qsar_dtxcid , dr.dtxcid as cid,dr.dtxsid as sid,  dr.casrn, dr.preferred_name as "name" , dr.smiles, dr.indigo_inchi_key as "inchiKey"
--SELECT count(distinct (dp.canon_qsar_smiles, dp.qsar_dtxcid ))
FROM qsar_datasets.datasets d
JOIN qsar_datasets.data_points dp ON dp.fk_dataset_id = d.id
LEFT JOIN qsar_models.dsstox_records dr ON dr.dtxcid = SUBSTRING(dp.qsar_dtxcid FROM 1 FOR POSITION('|' IN dp.qsar_dtxcid || '|') - 1)
where d.name like '% v1 modeling' and dr.fk_dsstox_snapshot_id =3 ;


SELECT d.name, dp.qsar_dtxcid 
--SELECT count(distinct (dp.canon_qsar_smiles))
FROM qsar_datasets.datasets d
JOIN qsar_datasets.data_points dp ON dp.fk_dataset_id = d.id
where d.name like '% v1 modeling' and dp.canon_qsar_smiles  = 'CC1C=CC2CC(C)CCC=2C=1' ;



select distinct(split_part(dp.qsar_dtxcid, '|', 1))
--  dp.canon_qsar_smiles AS "canonicalSmiles", dp.qsar_dtxcid, dr.dtxcid AS cid, dr.dtxsid AS sid, dr.casrn, 
--  dr.preferred_name AS "name", dr.smiles, dr.indigo_inchi_key AS "inchiKey"
FROM qsar_datasets.datasets d
JOIN qsar_datasets.data_points dp ON dp.fk_dataset_id = d.id
LEFT JOIN qsar_models.dsstox_records dr ON dr.dtxcid = split_part(dp.qsar_dtxcid, '|', 1) AND dr.fk_dsstox_snapshot_id = 3
WHERE d.name LIKE '% v1 modeling' AND dr.dtxcid IS NULL;


SELECT DISTINCT dp.canon_qsar_smiles, split_part(dp.qsar_dtxcid, '|', 1) AS cid 
from qsar_models.models m                
join qsar_datasets.datasets d on d.name = m.dataset_name 
JOIN qsar_datasets.data_points dp ON dp.fk_dataset_id = d.id
LEFT JOIN qsar_models.dsstox_records dr ON dr.dtxcid = split_part(dp.qsar_dtxcid, '|', 1) AND dr.fk_dsstox_snapshot_id = 1
WHERE d.name LIKE '%v1 modeling' and dr.dtxcid IS null and m.is_public =true 
order by canon_qsar_smiles ;

SELECT DISTINCT dp.canon_qsar_smiles, split_part(dp.qsar_dtxcid, '|', 1) AS cid, m.dataset_name  
from qsar_models.models m                
join qsar_datasets.datasets d on d.name = m.dataset_name 
JOIN qsar_datasets.data_points dp ON dp.fk_dataset_id = d.id
LEFT JOIN qsar_models.dsstox_records dr ON dr.dtxcid = split_part(dp.qsar_dtxcid, '|', 1) AND dr.fk_dsstox_snapshot_id = 1
WHERE d.name LIKE '%v1 modeling' and m.is_public =true AND dr.dtxcid IS null
order by m.dataset_name ;


select * from qsar_models.predictions p 
where p.canon_qsar_smiles ='ClC1(Cl)CC2(C(Cl)CC1C2(CCl)C(Cl)Cl)C(Cl)Cl' and p.fk_model_id = 1069;


select dp.canon_qsar_smiles, dp.qsar_property_value,p.qsar_predicted_value
from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
join qsar_models.models m on m.dataset_name = d.name
join qsar_models.predictions p on p.canon_qsar_smiles=dp.canon_qsar_smiles and p.fk_model_id=m.id
where m.id = 1069 and split_num = 1  and dpis.fk_splitting_id=:fk_splitting_id and p.fk_splitting_id=:fk_splitting_id;




                    
                    
                    