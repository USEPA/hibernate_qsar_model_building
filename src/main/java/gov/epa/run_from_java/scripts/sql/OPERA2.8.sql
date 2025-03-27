-- Where smiles doesnt match
select sc.sid, s.Original_SMILES,sc.smiles from Structure s
join IDs i on i.DSSTOX_COMPOUND_ID=s.DSSTOX_COMPOUND_ID
join SnapshotCompounds sc on sc.sid=i.DSSTOX_SUBSTANCE_ID
where s.Original_SMILES!=sc.smiles and sc.sid!='N/A';


-- where have dtxsids not in OPERA2.8:
select sc.sid, sc.smiles from SnapshotCompounds sc
left join IDs i on i.DSSTOX_SUBSTANCE_ID=sc.sid
where i.DSSTOX_SUBSTANCE_ID is null and sc.sid!='N/A' and sc.sid='DTXSID70177379';

select * from Results r where r.DSSTOX_COMPOUND_ID='DTXCID201151';

delete from Results where id>1096006;

select count(id) from Results where id>1096006;

-- select id from Results r where r.DSSTOX_COMPOUND_ID like 'DTXSID%';


select CATMoS_LD50_exp from Results where CATMoS_LD50_exp !='NA'


select  ids.CASRN,ids.DSSTOX_SUBSTANCE_ID, r.DSSTOX_COMPOUND_ID, CATMoS_LD50_pred from Results r
left join IDs ids on ids.DSSTOX_COMPOUND_ID=r.DSSTOX_COMPOUND_ID
where r.DSSTOX_COMPOUND_ID='DTXCID901026509';
-- where ids.CASRN is null;


select  ids.CASRN, r.DSSTOX_COMPOUND_ID, r.CATMoS_LD50_pred as CATMoS_LD50_pred_opera28, round(CMSS.CATMoS_LD50_pred) as CATMoS_LD50_pred_sdf, round(CMSS.Consensus_LD50) as Consensus_LD50_sdf  from Results r
left join IDs ids on ids.DSSTOX_COMPOUND_ID=r.DSSTOX_COMPOUND_ID
join CATMoS_SDF CMSS on ids.DSSTOX_COMPOUND_ID = CMSS.dsstox_compound_id
-- where r.DSSTOX_COMPOUND_ID='DTXCID901026509';


SELECT * from Results where DSSTOX_COMPOUND_ID='DTXCID30182';

select Clint_pred from Results where DSSTOX_COMPOUND_ID='DTXCID901125';

select DSSTOX_COMPOUND_ID, pKa_a_exp from Results where pKa_a_exp!='';

select * from Results where DSSTOX_COMPOUND_ID='DTXCID001372307';


SELECT * from Results
order by DSSTOX_COMPOUND_ID
LIMIT 10 OFFSET 10;


SELECT DSSTOX_COMPOUND_ID, COUNT(*) c FROM Results GROUP BY DSSTOX_COMPOUND_ID HAVING c > 1;