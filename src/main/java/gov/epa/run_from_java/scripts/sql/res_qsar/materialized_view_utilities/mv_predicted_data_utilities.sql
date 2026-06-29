-- get predictions for chemical
select * from mv_predicted_data
where dtxsid='DTXSID001015191'
order by prop_name;


select data.dtxsid,data.model_name, data.prop_name,data.model_id,data.prop_value,data.prop_unit,report.report_html,report.report_json from mv_predicted_data data
join mv_predicted_reports report on data.dtxsid=report.dtxsid and data.model_id=report.model_id
where data.dtxsid='DTXSID3039242'






--------------------------------------------------------------------------------------------------------
-- find chemicals in snapshot missing in predictions_dashboard:
select distinct dr.dtxcid
from qsar_models.dsstox_records dr
where dr.fk_dsstox_snapshot_id = 4 and dr.dtxcid is not null
  and not exists (
    select 1
    from qsar_models.predictions_dashboard pd
    join qsar_models.models m on m.id = pd.fk_model_id
    join qsar_models.sources s on s.id = m.fk_source_id
    where pd.dtxcid = dr.dtxcid
--      and s.name = 'TEST5.1.3' -- has 4
--      and s.name = 'OPERA2.8' -- has 100K so far
      and s.name = 'Percepta2025.1.4' -- has 3
  );




select * from mv_predicted_data mpd where mpd.source_name ='OPERA2.8';


CREATE TABLE qsar_models.mv_predicted_data_opera AS SELECT * from mv_predicted_data mpd where mpd.source_name ='OPERA2.8';

CREATE TABLE qsar_models.mv_predicted_reports_opera AS SELECT * from mv_predicted_reports mpr where mpr.source_name ='OPERA2.8';


select * from qsar_datasets.properties p 
join qsar_datasets.properties_in_categories pic on pic.fk_property_id =p.id;
