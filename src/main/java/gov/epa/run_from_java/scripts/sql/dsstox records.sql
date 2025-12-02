select casrn,smiles from qsar_models.dsstox_records
where dtxcid='DTXCID00364093' and fk_dsstox_snapshot_id=2;

VACUUM (ANALYZE, VERBOSE, FULL) qsar_models.dsstox_records;


-- Check if have running processes:
SELECT
    pg_stat_activity.pid,
    pg_locks.mode,
    pg_stat_activity.usename,
    pg_stat_activity.state,
    pg_stat_activity.query
FROM
    pg_locks
JOIN
    pg_stat_activity ON pg_locks.pid = pg_stat_activity.pid
WHERE
    pg_locks.relation = 'qsar_models.dsstox_records'::regclass
    AND pg_stat_activity.pid != pg_backend_pid();

-- Kill a process:
SELECT pg_terminate_backend(23078);

select * from qsar_models.dsstox_records where dtxsid='DTXSID8023892';


select * from qsar_models.dsstox_other_casrns oc
join qsar_models.dsstox_records dr on oc.fk_dsstox_record_id = dr.id
where dr.fk_dsstox_snapshot_id=3;

DELETE FROM qsar_models.dsstox_other_casrns
WHERE fk_dsstox_record_id IN (
    SELECT oc.fk_dsstox_record_id
    FROM qsar_models.dsstox_other_casrns oc
    JOIN qsar_models.dsstox_records dr ON oc.fk_dsstox_record_id = dr.id
    WHERE dr.fk_dsstox_snapshot_id = 3
);


delete from qsar_models.dsstox_records dr where id=4037881;

-- for exporting to json file:
select * from qsar_models.dsstox_records dr
where dr.fk_dsstox_snapshot_id=3
order by dr.dtxsid;


delete from qsar_models.dsstox_records dr
where dr.fk_dsstox_snapshot_id=3;


select count (id) from qsar_models.dsstox_records dr
where dr.fk_dsstox_snapshot_id=3;


select dr.dtxsid,oc.casrn from qsar_models.dsstox_other_casrns oc
join qsar_models.dsstox_records dr on oc.fk_dsstox_record_id = dr.id
where dr.fk_dsstox_snapshot_id=3;
