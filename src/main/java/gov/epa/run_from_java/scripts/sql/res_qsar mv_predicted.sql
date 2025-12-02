select data.dtxsid,data.model_name, data.prop_name,data.model_id,data.prop_value,data.prop_unit,report.report_html,report.report_json from mv_predicted_data data
join mv_predicted_reports report on data.dtxsid=report.dtxsid and data.model_id=report.model_id
where data.dtxsid='DTXSID3039242'



SELECT pd.prop_name, MIN(pd.prop_value) AS min_pred, MAX(pd.prop_value) AS max_pred, pd.prop_unit
FROM public.mv_predicted_data pd
where source_name='Percepta2023.1.2'
GROUP BY pd.prop_name,pd.prop_unit;




WITH MinValues AS (
    SELECT prop_name, MIN(prop_value) AS min_value
    FROM mv_predicted_data
    WHERE source_name = 'Percepta2023.1.2'
    GROUP BY prop_name
),
MaxValues AS (
    SELECT prop_name, MAX(prop_value) AS max_value
    FROM mv_predicted_data
    WHERE source_name = 'Percepta2023.1.2'
    GROUP BY prop_name
)
SELECT
    mv.prop_name,
    mv.min_value,
    min_data.dtxsid AS min_dtxsid,
    mx.max_value,
    max_data.dtxsid AS max_dtxsid
FROM MinValues mv
JOIN mv_predicted_data min_data
    ON min_data.prop_name = mv.prop_name
    AND min_data.prop_value = mv.min_value
    AND min_data.source_name = 'Percepta2023.1.2'
JOIN MaxValues mx
    ON mx.prop_name = mv.prop_name
JOIN mv_predicted_data max_data
    ON max_data.prop_name = mx.prop_name
    AND max_data.prop_value = mx.max_value
    AND max_data.source_name = 'Percepta2023.1.2';


WITH RankedMin AS (
    SELECT prop_name, prop_value, dtxsid,
           ROW_NUMBER() OVER (PARTITION BY prop_name ORDER BY prop_value ASC, dtxsid ASC) AS rn
    FROM mv_predicted_data
    WHERE source_name = 'Percepta2023.1.2' and prop_value is not null
),
RankedMax AS (
    SELECT prop_name, prop_value, dtxsid,
           ROW_NUMBER() OVER (PARTITION BY prop_name ORDER BY prop_value DESC, dtxsid ASC) AS rn
    FROM mv_predicted_data
    WHERE source_name = 'Percepta2023.1.2' and prop_value is not null
)
SELECT
    min.prop_name,
    min.prop_value AS min_value,
    min.dtxsid AS min_dtxsid,
    max.prop_value AS max_value,
    max.dtxsid AS max_dtxsid,

FROM RankedMin min
JOIN RankedMax max ON min.prop_name = max.prop_name
WHERE min.rn = 1 AND max.rn = 1;


select * from mv_predicted_data
where dtxsid='DTXSID001015191'
order by prop_name;