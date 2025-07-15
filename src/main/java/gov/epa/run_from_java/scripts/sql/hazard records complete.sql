select distinct hazardName, category, rationale from HazardRecords hr
where source='DSL' order by hazardName;


select distinct hazardName,hazardCode, rationale from HazardRecords hr
where source='Japan' order by hazardName, hazardCode;


select count(distinct dtxsid) from main.HazardRecords where listType in ('Authoritative','Screening');

-- select distinct( listType) from main.HazardRecords;



select * from HazardRecords hr
join PFASSTRUCTV5 p on p.dtxsid=hr.dtxsid
where listType!='QSAR Model'
order by hr.dtxsid, hazardName;


select hazardName, hr.source,  count (DISTINCT hr.dtxsid) from HazardRecords hr
join PFASSTRUCTV5 p on p.dtxsid=hr.dtxsid
where listType!='QSAR Model'
group by hazardName,hr.source
order by hazardName,hr.source;