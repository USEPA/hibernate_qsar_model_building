select ms.id,fk_model_id, fk_statistic_id,set_type,value,created_by,created_at,updated_by,updated_at from qsar_model_statistics_tmartin ms
where fk_model_id=19
order by fk_model_id, set_type desc,fk_statistic_id


select ms.id,fk_model_id, fk_statistic_id,set_type,value,created_by,created_at,updated_by,updated_at from qsar_model_statistics_tmartin ms
where fk_model_id=20
order by ms.id


update qsar_model_statistics_tmartin
set value=0.71, updated_at=current_date,updated_by='tmarti02'
where id=25


update qsar_model_statistics_tmartin
set value=0.79, updated_at=current_date,updated_by='tmarti02'
where id=24

delete from qsar_model_statistics_tmartin ms
where ms.id>=55 and ms.id<=60

-- *******************************************************************************
-- Training stats for RBiodeg:
insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (79, 20, 4, 'Train', 0.80, 'tmarti02',current_date,'qsar')

insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (80, 20, 6, 'Train', 0.79, 'tmarti02',current_date,'qsar')

insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (81, 20, 5, 'Train', 0.82, 'tmarti02',current_date,'qsar')

-- *******************************************************************************
-- CV stats for RBiodeg:
insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (82, 20, 4, '5FoldICV', 0.80, 'tmarti02',current_date,'qsar')

insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (83, 20, 6, '5FoldICV', 0.78, 'tmarti02',current_date,'qsar')

insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (84, 20, 5, '5FoldICV', 0.82, 'tmarti02',current_date,'qsar')

-- *******************************************************************************
-- External stats for RBiodeg:
insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (85, 20, 4, 'External', 0.79, 'tmarti02',current_date,'qsar')

insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (86, 20, 6, 'External', 0.77, 'tmarti02',current_date,'qsar')

insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (87, 20, 5, 'External', 0.81, 'tmarti02',current_date,'qsar')

-- *******************************************************************************
-- Training stats for OPERA_CATMOS_LD50:
insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (88, 204, 2, 'Train', 0.85, 'tmarti02',current_date,'qsar')

insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (89, 204, 3, 'Train', 0.30, 'tmarti02',current_date,'qsar')

-- *******************************************************************************
-- CV stats for OPERA_CATMOS_LD50:
insert into qsar_model_statistics_tmartin(id, fk_model_id,fk_statistic_id, set_type,"value",created_by,created_at ,source_system)
values (90, 204, 1, '5FoldICV', 0.79, 'tmarti02',current_date,'qsar')

select * from opera.opera_data