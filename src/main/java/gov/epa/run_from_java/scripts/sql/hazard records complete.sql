select distinct hazardName, category, rationale from HazardRecords hr
where source='DSL' order by hazardName;


select distinct hazardName,hazardCode, rationale from HazardRecords hr
where source='Japan' order by hazardName, hazardCode;