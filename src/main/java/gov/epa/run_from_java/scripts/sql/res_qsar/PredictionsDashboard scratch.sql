
--select count (distinct pd.dtxcid)
--select distinct dr.fk_dsstox_snapshot_id
select distinct pd.dtxcid
from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
join qsar_models.sources s on m.fk_source_id = s.id
left join qsar_models.dsstox_records dr on pd.dtxcid = dr.dtxcid 
--join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id

--where s.name='OPERA2.8'
where s.name='TEST5.1.3'
--limit 100
--where s.name='Percepta2023.1.2'
;


select count (dtxcid) from qsar_models.dsstox_records dr 
where dr.fk_dsstox_snapshot_id=4;


--select pd.canon_qsar_smiles, dr.id, m.id from qsar_models.predictions_dashboard pd
select count (distinct dr.fk_dsstox_snapshot_id) from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
join qsar_models.dsstox_records dr on pd.dtxcid = dr.id
where m.fk_source_id=6 


select pd.dtxcid, count(pd.dtxcid)
from qsar_models.predictions_dashboard pd
join qsar_models.models m on m.id=pd.fk_model_id
where m.fk_source_id =6
group by pd.dtxcid
having count(pd.dtxcid)>1;


select prop_name,mpd.prop_value_experimental ,  prop_value, mpd.prop_unit   from mv_predicted_data mpd 
where dtxsid ='DTXSID0037497' and source_name ='OPERA2.8' and (prop_name = 'LogKow: Octanol-Water' or prop_name like '%LogD%' or prop_name like 'pKa%');


