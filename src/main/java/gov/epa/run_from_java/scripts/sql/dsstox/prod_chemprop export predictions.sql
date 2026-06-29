select m.name, e.name, e.abbreviation,standard_unit  from prod_qsar.models m
join prod_chemprop.endpoints e on m.efk_chemprop_endpoint_id=e.id
# where m.name like 'ACD_%'
where e.name='Water Solubility'
order by m.name;


select m.name, count(qpp.id) from prod_chemprop.qsar_predicted_properties qpp
join prod_qsar.models m on qpp.efk_qsar_model_id=m.id
where m.name like 'ACD_%'
group by m.name;



select gs.dsstox_substance_id as dtxsid,  m.name as modelName, c.smiles,qpp.result_value as predictedValue from prod_chemprop.qsar_predicted_properties qpp
join prod_qsar.models m on qpp.efk_qsar_model_id=m.id
join prod_chemprop.endpoints e on m.efk_chemprop_endpoint_id=e.id
join prod_dsstox.compounds c on qpp.efk_dsstox_compound_id=c.id
join prod_dsstox.generic_substance_compounds gsc on c.id = gsc.fk_compound_id
join prod_dsstox.generic_substances gs on gsc.fk_generic_substance_id = gs.id
# JOIN (SELECT CEIL(RAND() * ( SELECT MAX(id) FROM prod_chemprop.qsar_predicted_properties )) AS id ) AS qpp2 on qpp.id>=qpp2.id
# where m.name='ACD_Prop_Dielectric_Constant'
# where m.name='ACD_Sol' and smiles like '%.%'
# where m.name='ACD_Prop_Parachor'
where m.name like 'ACD_%' and gs.dsstox_substance_id='DTXSID00404564'
order by m.name;
#   and gs.dsstox_substance_id='DTXSID80838161'
# limit 100;



