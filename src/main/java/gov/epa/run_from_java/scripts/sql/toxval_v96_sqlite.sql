select distinct critical_effect from toxval_complete
where (toxval_type = 'NOAEL' or toxval_type = 'LOAEL')
and critical_effect like '%ataxia%'
;


select distinct study_duration_class from toxval_complete
where (toxval_type = 'NOAEL' or toxval_type = 'LOAEL')
;

select distinct critical_effect from toxval_complete
where (toxval_type = 'NOAEL' or toxval_type = 'LOAEL')
and (critical_effect like '%cholinesterase%')
;


select * from HazardRecords hr
join toxval_complete t on hr.dtxsid = t.dtxsid
where sourceOriginal='EFSA' and hazardName='Systemic Toxicity Repeat Exposure'
order by dtxsid;

select count(distinct (toxval_id)) from toxval_complete;

select count(toxval_id) from toxval_complete;


select distinct hazardName, study_duration_class, toxval_subtype, study_type, critical_effect from HazardRecords hr
join toxval_complete tc on tc.toxval_id=hr.toxvalID
where sourceTable='toxval'
--     and hazardName='Reproductive' and study_duration_class!='-' and study_duration_class like '%(%';
and hazardName='Developmental' and study_duration_class!='-' and study_duration_class like '%(%';


CREATE INDEX "HazardRecords_toxval_id" ON "HazardRecords" (
	"toxvalID"
);

CREATE INDEX "toxval_complete_toxval_id" ON "toxval_complete" (
	"toxval_id"
);


select hazardName,count(dtxsid) from HazardRecords
group by hazardName;


select distinct toxval_type from HazardRecords hr
join toxval_complete t on hr.toxvalID=t.toxval_id;


CREATE INDEX if not exists toxval_complete_toxval_id ON toxval_complete (toxval_id)



-- toxval table hazard records by hazardName:
select hazardName,count(hr.id) from HazardRecords hr
where sourceTable='toxval'
group by hazardName;

--Models records by source
select hazardName,source, count(hr.id) from HazardRecords hr
where sourceTable='models'
group by hazardName,source;

--genetox_summary and cancer_summary by sourceOriginal:
select sourceTable, hazardName,sourceOriginal, count(hr.id) from HazardRecords hr
where sourceTable='genetox_summary' or sourceTable='cancer_summary'
group by hazardName,sourceOriginal;


update HazardRecords set toxvalID=null where sourceTable='models';


CREATE INDEX if not exists HazardRecords_CAS ON HazardRecords (CAS);

vacuum;

select * from HazardRecords where source='OPERA2.8' and dtxsid='DTXSID00493599'

delete from HazardRecords where source='EpiWeb1.0';

select distinct source from HazardRecords where sourceTable='models' ;


CREATE INDEX HazardRecords_listType ON HazardRecords (listType);

select * from HazardRecords where source='OPERA2.8';

vacuum ;