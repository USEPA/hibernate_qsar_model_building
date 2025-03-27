select  am.name,qpad.applicability_value,m.name from chemprop_new.qsar_predicted_properties qpp
join dsstox.compounds c on c.id=qpp.efk_dsstox_compound_id
join dsstox.generic_substance_compounds gsc on c.id = gsc.fk_compound_id
join dsstox.generic_substances gs on gsc.fk_generic_substance_id = gs.id
join qsar.models m on qpp.efk_qsar_model_id=m.id
join chemprop_new.qsar_predicted_ad_estimates qpad on qpp.id = qpad.fk_qsar_predicted_property_id
join qsar.ad_methods am on am.id=qpad.efk_qsar_ad_method_id
-- where gs.dsstox_substance_id='DTXSID7020182' and m.name = 'OPERA_PKAA';
where gs.dsstox_substance_id='DTXSID7020182' ;
