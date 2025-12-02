select dp.canon_qsar_smiles,dpis.split_num from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id and dpis.fk_splitting_id=1
where d.name='ECOTOX_2024_12_12_96HR_Fish_LC50_v1 modeling';




select count(dp.canon_qsar_smiles),dpis.split_num from  qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points dp2 on dp.canon_qsar_smiles=dp2.canon_qsar_smiles and dp2.fk_dataset_id=505
join qsar_datasets.data_points_in_splittings dpis on dp2.id = dpis.fk_data_point_id
where d.name='ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling' and dpis.fk_splitting_id=1
group by dpis.split_num;


-- how to clone the splitting:
select dp.id, split_num,dpis.fk_splitting_id from  qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points dp2 on dp.canon_qsar_smiles=dp2.canon_qsar_smiles and dp2.fk_dataset_id=505
join qsar_datasets.data_points_in_splittings dpis on dp2.id = dpis.fk_data_point_id
where d.name='ECOTOX_2024_12_12_96HR_FHM_LC50_v1 modeling';



select dp.id, split_num,dpis.fk_splitting_id from  qsar_datasets.data_points dp
	    join qsar_datasets.data_points dp2 on dp.canon_qsar_smiles=dp2.canon_qsar_smiles and dp2.fk_dataset_id=505
	    join qsar_datasets.data_points_in_splittings dpis on dp2.id = dpis.fk_data_point_id
	    where dp.fk_dataset_id=506;


select split_num, count(dpis) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where d.name='exp_prop_RBIODEG_RIFM_BY_CAS' and fk_splitting_id=1
group by split_num;


select qsar_property_value, count(dp) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
where d.name='exp_prop_RBIODEG_RIFM_BY_CAS'
group by qsar_property_value;



select count(dp) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
where d.name='exp_prop_RBIODEG_RIFM_BY_CAS';







select d.name,count(dp.id) from qsar_datasets.datasets d
         join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
where d.name like 'ECOT%'
group by d.name
order by d.name;


select m.id,m.dataset_name,m.fk_method_id,ms.statistic_value,s.name from qsar_models.models m
left join qsar_models.model_bytes mb on m.id = mb.fk_model_id
left join qsar_models.model_statistics ms on m.id = ms.fk_model_id and ms.fk_statistic_id=16
join qsar_models.sources s on m.fk_source_id = s.id
where mb.id is null and s.name ='Cheminformatics Modules';



-- create training/test sets for complicated model:
select dp.canon_qsar_smiles,-log10(dpc.property_value) as prop_value,dpis.split_num, pv2.value_text as species_common,pv3.value_text as exposure_type, dv.values_tsv from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
-- join qsar_datasets.data_points dpSplit on dp.canon_qsar_smiles=dpSplit.canon_qsar_smiles and dpSplit.fk_dataset_id=505
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id and dpis.fk_splitting_id=1
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11
join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13
join qsar_descriptors.descriptor_values dv on dv.canon_qsar_smiles=dp.canon_qsar_smiles and dv.fk_descriptor_set_id=4
where d.name='ECOTOX_2024_12_12_96HR_Fish_LC50_v1 modeling';


select dp.id, dpc.id,dp.canon_qsar_smiles,dpc.property_value,pv3.value_text as exposure_type from qsar_datasets.data_point_contributors dpc
join qsar_datasets.data_points dp on dpc.fk_data_point_id = dp.id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
join exp_prop.parameter_values pv2 on pv2.fk_property_value_id=pv.id and pv2.fk_parameter_id=11
join exp_prop.parameter_values pv3 on pv3.fk_property_value_id=pv.id and pv3.fk_parameter_id=13
-- join qsar_datasets.units u on d.fk_unit_id_contributor = u.id
where d.name='ECOTOX_2024_12_12_96HR_FHM_LC50_v1 modeling'
order by canon_qsar_smiles;




-- print model test set stats
select m.id,m.created_by,m.name, m.dataset_name,m.descriptor_set_name,ms.statistic_value  as R2_TEST,ms2.statistic_value as MAE_TEST,de.embedding_tsv from qsar_models.models m
join qsar_models.model_statistics ms on m.id = ms.fk_model_id and ms.fk_statistic_id=16
join qsar_models.model_statistics ms2 on m.id = ms2.fk_model_id and ms2.fk_statistic_id=8
left join qsar_models.descriptor_embeddings de on m.fk_descriptor_embedding_id = de.id
-- where dataset_name like 'ECOT%'
where m.dataset_name='ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling';
-- where m.dataset_name='ECOTOX_2024_12_12_96HR_FHM_LC50_v3 modeling';

-- export predictions from a given model
select p.canon_qsar_smiles,dp.qsar_property_value,qsar_predicted_value from qsar_models.predictions p
join qsar_models.models m on p.fk_model_id = m.id
join qsar_datasets.datasets d on d.name=m.dataset_name
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id and dp.canon_qsar_smiles=p.canon_qsar_smiles
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
-- where  p.fk_model_id=1507 and p.fk_splitting_id=1 and dpis.fk_splitting_id=1 and split_num=1;
where  p.fk_model_id=1069 and p.fk_splitting_id=1 and dpis.fk_splitting_id=1 and split_num=1
and p.canon_qsar_smiles='FC(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C=CC(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)F'


select p.canon_qsar_smiles,dp.qsar_property_value,qsar_predicted_value from qsar_models.predictions p
join qsar_models.models m on p.fk_model_id = m.id
join qsar_datasets.datasets d on d.name=m.dataset_name
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id and dp.canon_qsar_smiles=p.canon_qsar_smiles
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where  p.fk_model_id=1069 and p.fk_splitting_id=1 and dpis.fk_splitting_id=1 and split_num=1;


select p.canon_qsar_smiles,dp.qsar_property_value, qsar_predicted_value from qsar_models.predictions p
join qsar_models.models m on p.fk_model_id = m.id
join qsar_datasets.datasets d on d.name=m.dataset_name
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id and dp.canon_qsar_smiles=p.canon_qsar_smiles
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where  p.fk_model_id=1502 and p.fk_splitting_id=1 and dpis.fk_splitting_id=1 and split_num=1;

select d.name,count(dpis) from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
left join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where d.name like '%ECOTOX%' and dpis.fk_splitting_id=1
group by d.name


-- get chemicals in ECHA reach set that arent in the ECOTOX dataset


select count(distinct (dp.canon_qsar_smiles))
from qsar_datasets.datasets d
		 join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
where d.name = 'QSAR_Toolbox_96HR_Fish_LC50_v3 modeling'
  and dp.canon_qsar_smiles not in (select dp2.canon_qsar_smiles
								   from qsar_datasets.datasets d2
											join qsar_datasets.data_points dp2 on d2.id = dp2.fk_dataset_id
								   where d2.name = 'ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling');


select count(distinct (dp.canon_qsar_smiles))
from qsar_datasets.datasets d
		 join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
where d.name = 'QSAR_Toolbox_96HR_Fish_LC50_v3 modeling';



select dp.canon_qsar_smiles from qsar_datasets.datasets d
join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
where d.name='ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling';


--look for chemicals in NITE_OPPT that arent in RIFM set
select dp.canon_qsar_smiles,dp.qsar_property_value , dv.values_tsv  from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
where dp.fk_dataset_id=533 and dv.fk_descriptor_set_id=6
  and dp.canon_qsar_smiles not in
(select dp.canon_qsar_smiles from qsar_datasets.data_points dp
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where  dp.fk_dataset_id=532 and fk_splitting_id=1 and split_num=0)
;

select headers_tsv from qsar_descriptors.descriptor_sets ds where ds.name='WebTEST-default';



-- get test set for LogKow XGB model
select distinct  dp.canon_qsar_smiles, dpc.dtxcid,dp.qsar_property_value from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
where dpis.fk_splitting_id=1 and d.id=116 and dpis.split_num=1;


-- get all test sets for XGB models
select distinct  dp.canon_qsar_smiles, dpc.dtxcid from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.datasets_in_cheminformatics_modules dicm on d.id = dicm.fk_datasets_id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
where dpis.fk_splitting_id=1 and dpis.split_num=1;


select * from qsar_datasets.datasets d
         join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id
         join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
where d.name='QSAR_Toolbox_96HR_Fish_LC50_v1 modeling' and dpc.dtxsid='DTXSID5020152';


select dp.canon_qsar_smiles,dp.qsar_property_value , dv.values_tsv  from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
where dp.fk_dataset_id=534 and dv.fk_descriptor_set_id=4
  and dp.canon_qsar_smiles not in
(select dp.canon_qsar_smiles from qsar_datasets.data_points dp
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where  dp.fk_dataset_id=512 and fk_splitting_id=1 and split_num=0);


select distinct dp.canon_qsar_smiles,dpc.dtxcid  from qsar_datasets.data_points dp
		join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where dp.fk_dataset_id=534
  and dp.canon_qsar_smiles not in
(select dp.canon_qsar_smiles from qsar_datasets.data_points dp
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where  dp.fk_dataset_id=512 and fk_splitting_id=1 and split_num=0)



select distinct p.canon_qsar_smiles,dp.qsar_dtxcid, dp.qsar_property_value,qsar_predicted_value from qsar_models.predictions p
		join qsar_models.models m on p.fk_model_id = m.id
		join qsar_datasets.datasets d on d.name=m.dataset_name
		join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id and dp.canon_qsar_smiles=p.canon_qsar_smiles
		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
		join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
		where  p.fk_model_id=1065 and p.fk_splitting_id=1 and dpis.fk_splitting_id=1 and split_num=1;


select dr.dtxcid,pd.prediction_value from qsar_models.predictions_dashboard pd
    join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
         join qsar_models.models m on pd.fk_model_id = m.id
		where  m.id=1068 and dr.fk_dsstox_snapshot_id=2
and dr.dtxcid='DTXCID20285993';


select dr.dtxcid,pd.prediction_value from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
where fk_model_id=1209 and dr.fk_dsstox_snapshot_id=2
and dr.dtxcid in ('DTXCID20285993','DTXCID80727829','DTXCID1035238','DTXCID4042963','DTXCID304077','DTXCID101584','DTXCID906612','DTXCID70818679','DTXCID10819084','DTXCID30142225','DTXCID50429','DTXCID701411403','DTXCID0034726','DTXCID1036333','DTXCID30819405','DTXCID1010189','DTXCID3048605','DTXCID202047','DTXCID1072892','DTXCID205312','DTXCID30818796','DTXCID202042','DTXCID1036335','DTXCID707241','DTXCID00818945','DTXCID8024650','DTXCID401874','DTXCID50124104','DTXCID602182','DTXCID401879','DTXCID7041594','DTXCID80818948','DTXCID70330806','DTXCID60811997','DTXCID0030114','DTXCID00818940','DTXCID7039040','DTXCID505843','DTXCID0070815','DTXCID6041715','DTXCID80333585','DTXCID10811144','DTXCID301505070','DTXCID8036869','DTXCID201325964','DTXCID3031159','DTXCID90138866','DTXCID1027635','DTXCID2041593','DTXCID9042962','DTXCID901012099','DTXCID9042964','DTXCID50135472','DTXCID40285056','DTXCID60498','DTXCID4042705','DTXCID6024383','DTXCID906837','DTXCID80835271','DTXCID50100160','DTXCID20116915','DTXCID801378254','DTXCID301820','DTXCID70818412','DTXCID204442','DTXCID20124772','DTXCID301374621','DTXCID2033732','DTXCID8095853','DTXCID60818128','DTXCID80818968','DTXCID605478','DTXCID3029071','DTXCID90161634','DTXCID30122820','DTXCID20818482','DTXCID2041597','DTXCID20818240','DTXCID40818606','DTXCID1035496','DTXCID70111819','DTXCID9017303','DTXCID40818843','DTXCID80818721','DTXCID00818965','DTXCID50911056','DTXCID2029406','DTXCID9074779','DTXCID0033203','DTXCID305361','DTXCID1084685','DTXCID308872','DTXCID5043423','DTXCID1031919','DTXCID10312500','DTXCID30818352','DTXCID304035','DTXCID709227','DTXCID9039369','DTXCID10819044','DTXCID401473949','DTXCID101380','DTXCID30106165','DTXCID50818915','DTXCID5030581','DTXCID90819289','DTXCID30818357','DTXCID804624','DTXCID40108857','DTXCID20113888','DTXCID50113250','DTXCID602140','DTXCID80102359','DTXCID606500','DTXCID60286','DTXCID5042588','DTXCID0021206','DTXCID30130021','DTXCID606746','DTXCID001771','DTXCID0043262','DTXCID80811030','DTXCID60818340','DTXCID4048222','DTXCID60818345','DTXCID50126320','DTXCID004800','DTXCID8024440','DTXCID106615','DTXCID1040409','DTXCID102250','DTXCID4039732','DTXCID5021413','DTXCID0047824','DTXCID1093381','DTXCID5024922','DTXCID80285378','DTXCID1041970','DTXCID10313613','DTXCID90811102','DTXCID30818574','DTXCID9039107','DTXCID4045','DTXCID20125820','DTXCID90811581','DTXCID50818935','DTXCID5095965','DTXCID5034723','DTXCID6036330','DTXCID203792','DTXCID80295174','DTXCID00811052','DTXCID9042750','DTXCID2031112','DTXCID3040061','DTXCID5042320','DTXCID10138601','DTXCID20911063','DTXCID2043581','DTXCID409178','DTXCID0043000','DTXCID30119215','DTXCID502515','DTXCID50778303','DTXCID001755','DTXCID3047926','DTXCID504931','DTXCID501666','DTXCID2047933','DTXCID60818320','DTXCID409173','DTXCID7037925','DTXCID1033458','DTXCID805751','DTXCID801016779','DTXCID1036967','DTXCID5087441','DTXCID701546','DTXCID804661','DTXCID106872','DTXCID105300','DTXCID40134035','DTXCID0048979','DTXCID60118110','DTXCID3038648','DTXCID6024757','DTXCID90132945','DTXCID5048762','DTXCID306655','DTXCID40335267','DTXCID201596','DTXCID101506789','DTXCID7021570','DTXCID5070442','DTXCID10109235','DTXCID3024499','DTXCID90103130','DTXCID30818711','DTXCID307509','DTXCID60104856','DTXCID60368734','DTXCID407579','DTXCID609655','DTXCID8024812','DTXCID605295','DTXCID1024754','DTXCID0048505','DTXCID2087189','DTXCID2089599','DTXCID3033197','DTXCID404063','DTXCID70205','DTXCID10124867','DTXCID1033450','DTXCID8012601','DTXCID9030123','DTXCID508715','DTXCID601937','DTXCID8012605','DTXCID7048823','DTXCID804200','DTXCID10809465','DTXCID50101657','DTXCID40127962','DTXCID5033042','DTXCID3021192','DTXCID80332098','DTXCID106652','DTXCID102297','DTXCID40134257','DTXCID3011393','DTXCID40450','DTXCID3090779','DTXCID70132963','DTXCID7024827','DTXCID102292','DTXCID306435','DTXCID8027084','DTXCID203996','DTXCID90812073','DTXCID901395174','DTXCID201575','DTXCID7013923','DTXCID30637','DTXCID8039293','DTXCID9029356','DTXCID80153326','DTXCID401816','DTXCID5043057','DTXCID60133520','DTXCID50112541','DTXCID407113','DTXCID901560','DTXCID2036611','DTXCID2033102','DTXCID9034947','DTXCID601717','DTXCID4016837','DTXCID30106569','DTXCID8024860','DTXCID30861','DTXCID9020112','DTXCID30818519','DTXCID3024449','DTXCID2031168','DTXCID703522','DTXCID5043265','DTXCID106432','DTXCID30320925','DTXCID105342','DTXCID5043269','DTXCID50322668','DTXCID40662','DTXCID50134369','DTXCID10818350','DTXCID30814157','DTXCID30818756','DTXCID30818514','DTXCID201795','DTXCID0038968','DTXCID7024403','DTXCID2021072','DTXCID70818250','DTXCID20810744','DTXCID8096089','DTXCID60448','DTXCID30232458','DTXCID7092987','DTXCID606102','DTXCID10117299','DTXCID20818462','DTXCID6021450','DTXCID506739','DTXCID80818983','DTXCID20109544','DTXCID0039807','DTXCID5091515','DTXCID001137','DTXCID8046872','DTXCID20818225','DTXCID201509900','DTXCID9031018','DTXCID006823','DTXCID90910987','DTXCID3099389','DTXCID8032467','DTXCID5043001','DTXCID4037950','DTXCID105363','DTXCID5043007','DTXCID0028957','DTXCID9032791','DTXCID309904','DTXCID0048767','DTXCID7089542','DTXCID302274','DTXCID70133854','DTXCID201774','DTXCID9034733','DTXCID1011492','DTXCID50813884','DTXCID401853','DTXCID50681','DTXCID20810961','DTXCID00321580','DTXCID2099184','DTXCID20320858','DTXCID5042166','DTXCID60158854','DTXCID4030095','DTXCID5044106','DTXCID60399664','DTXCID20818684','DTXCID002201','DTXCID20818442','DTXCID8046614','DTXCID4037952','DTXCID505628','DTXCID2047777','DTXCID0014542','DTXCID80818761','DTXCID9010317','DTXCID0094869','DTXCID00811456','DTXCID5036973','DTXCID3047768','DTXCID9032327','DTXCID60113180','DTXCID30101594','DTXCID50809606','DTXCID5035161','DTXCID0038411','DTXCID0036471','DTXCID907147','DTXCID4046529','DTXCID706716','DTXCID106238','DTXCID4030178','DTXCID305722','DTXCID90810711','DTXCID50109633','DTXCID50818319','DTXCID5039519','DTXCID208022','DTXCID40121243','DTXCID805117','DTXCID206088','DTXCID50818798','DTXCID4043486','DTXCID30257','DTXCID4091445','DTXCID6031205','DTXCID50123935','DTXCID9030092','DTXCID609257','DTXCID007832','DTXCID1020300','DTXCID2096968','DTXCID60321485','DTXCID80813933','DTXCID2037764','DTXCID7027700','DTXCID8088517','DTXCID4043010','DTXCID9037955','DTXCID50166423','DTXCID6022507','DTXCID20130651','DTXCID50320902','DTXCID6091976','DTXCID704997','DTXCID805133','DTXCID2048666','DTXCID3049122','DTXCID70133091','DTXCID701325307','DTXCID00149908','DTXCID4044329','DTXCID20200','DTXCID90811829','DTXCID50818334','DTXCID5027564','DTXCID301147','DTXCID601783831','DTXCID50818571','DTXCID10818456','DTXCID90818459','DTXCID6021668','DTXCID7024667','DTXCID90819542','DTXCID90818212','DTXCID806228','DTXCID20108911','DTXCID40159829','DTXCID1029384','DTXCID00620','DTXCID6043206','DTXCID20321582','DTXCID4030170','DTXCID901845','DTXCID20696','DTXCID0047119','DTXCID6043200','DTXCID1042578','DTXCID4021218','DTXCID1042576','DTXCID8099415','DTXCID3020708','DTXCID0048218','DTXCID00560619','DTXCID30210817','DTXCID3042714','DTXCID3020702','DTXCID602404','DTXCID2022337','DTXCID002125','DTXCID8027111','DTXCID00819018','DTXCID0042959','DTXCID4021426','DTXCID10818638','DTXCID705427','DTXCID9093850','DTXCID907341','DTXCID80818120','DTXCID6042787','DTXCID0039302','DTXCID30321619','DTXCID807570','DTXCID9098068','DTXCID4030380','DTXCID9037745','DTXCID1047817','DTXCID0048268','DTXCID5024471','DTXCID9011591','DTXCID3088300','DTXCID6038297','DTXCID704573','DTXCID2021236','DTXCID6017596','DTXCID8029367','DTXCID90818631','DTXCID605091','DTXCID7036408','DTXCID1036911','DTXCID4042595','DTXCID2024741','DTXCID50284858','DTXCID0039774','DTXCID607271','DTXCID502094','DTXCID9022021','DTXCID6030314','DTXCID5027728','DTXCID20159246','DTXCID80132691','DTXCID00153388','DTXCID2035144','DTXCID4032326','DTXCID6029339','DTXCID5048213','DTXCID1043207','DTXCID80893654','DTXCID00818344','DTXCID006307','DTXCID60811154','DTXCID00818349','DTXCID70577850','DTXCID5036476','DTXCID3048233','DTXCID702136','DTXCID807355','DTXCID6043412','DTXCID9046447','DTXCID40134570','DTXCID9030096','DTXCID301582','DTXCID70130621','DTXCID2039754','DTXCID30140701','DTXCID6020777','DTXCID2094554','DTXCID701287','DTXCID90818414','DTXCID90818651','DTXCID8088987','DTXCID1035862','DTXCID50819467','DTXCID10134921','DTXCID901808','DTXCID609231','DTXCID50123753','DTXCID9048851','DTXCID6032512','DTXCID5040704','DTXCID2036249','DTXCID7038600','DTXCID6032516','DTXCID30133193','DTXCID9033133','DTXCID3017739','DTXCID90134','DTXCID1024964','DTXCID001072','DTXCID601013824','DTXCID4030126','DTXCID002162','DTXCID20811094','DTXCID80810886','DTXCID20818725','DTXCID20811099','DTXCID005678','DTXCID60143881','DTXCID204065','DTXCID10818835','DTXCID207335','DTXCID902090','DTXCID40129128','DTXCID906450','DTXCID201770739','DTXCID907540','DTXCID7041629','DTXCID1031832','DTXCID0030530','DTXCID90144129','DTXCID902095','DTXCID6040321','DTXCID6028442','DTXCID7014490','DTXCID7037131','DTXCID90810559','DTXCID00206','DTXCID0032940','DTXCID50101071','DTXCID4029351','DTXCID6042739','DTXCID506098','DTXCID4043644','DTXCID1072393','DTXCID507183','DTXCID70115236','DTXCID30818170','DTXCID406921','DTXCID10278657','DTXCID9039735','DTXCID7038230','DTXCID604642','DTXCID9039731','DTXCID40142579','DTXCID30334320','DTXCID60104058','DTXCID7040788','DTXCID501886','DTXCID50930','DTXCID60818663','DTXCID501881','DTXCID409194','DTXCID501640','DTXCID50128783','DTXCID4042541','DTXCID207314','DTXCID00474','DTXCID4042549','DTXCID00249831','DTXCID6027135','DTXCID00333183','DTXCID906434','DTXCID801618','DTXCID401015979','DTXCID7017741','DTXCID1039343','DTXCID7039331','DTXCID8038647','DTXCID20124378','DTXCID80126873','DTXCID00488627','DTXCID30144269','DTXCID60161641','DTXCID6042999','DTXCID1032935','DTXCID3032759','DTXCID80101125','DTXCID20101689','DTXCID405611','DTXCID9036692','DTXCID7042980','DTXCID606602','DTXCID501629','DTXCID0040331','DTXCID1020728','DTXCID401251','DTXCID60818880','DTXCID00811931','DTXCID7093292','DTXCID6084347','DTXCID00289232','DTXCID7027124','DTXCID0041224','DTXCID8092398','DTXCID80113541','DTXCID6027553','DTXCID3097428','DTXCID907503','DTXCID2085224','DTXCID0014091','DTXCID50818995','DTXCID907749','DTXCID9037589','DTXCID00402','DTXCID90818237','DTXCID8027581','DTXCID60124276','DTXCID4039362','DTXCID50132603','DTXCID50113093','DTXCID40809751','DTXCID2088991','DTXCID2048294','DTXCID2048296','DTXCID30332588','DTXCID60290980','DTXCID10335214','DTXCID6027559','DTXCID601576','DTXCID60325488','DTXCID0043422','DTXCID10135392','DTXCID006386','DTXCID50118306','DTXCID401031','DTXCID00818980','DTXCID40810741','DTXCID90142367','DTXCID50909150','DTXCID9024011','DTXCID1041219','DTXCID6041630','DTXCID30157531','DTXCID70134381','DTXCID30322762','DTXCID9040500','DTXCID0042539','DTXCID50580266','DTXCID9037325','DTXCID9035389','DTXCID2042511','DTXCID0021880','DTXCID0016299','DTXCID2040575','DTXCID9035387','DTXCID30149911','DTXCID30294218','DTXCID301503','DTXCID30257060','DTXCID7042936','DTXCID10683','DTXCID705244','DTXCID704395','DTXCID0032526','DTXCID1030313','DTXCID402147','DTXCID20545408','DTXCID407833','DTXCID3037177','DTXCID80321225','DTXCID40819577','DTXCID501828','DTXCID10196509','DTXCID70115413','DTXCID60818608','DTXCID0032996','DTXCID3043603','DTXCID60818840','DTXCID2021604','DTXCID2095497','DTXCID90332887','DTXCID6036467','DTXCID701368812','DTXCID0029264','DTXCID701821','DTXCID9029643','DTXCID10102569','DTXCID2040361','DTXCID50135371','DTXCID4040426','DTXCID5043346','DTXCID8031073','DTXCID902134','DTXCID9021920','DTXCID001521233','DTXCID708459','DTXCID305049','DTXCID70818795','DTXCID804941','DTXCID204586','DTXCID8044383','DTXCID40127462','DTXCID90811001','DTXCID3024996','DTXCID40818949','DTXCID0028169','DTXCID605570','DTXCID00322577','DTXCID7032767','DTXCID8033485','DTXCID401756','DTXCID402600','DTXCID50111150','DTXCID5047954','DTXCID7042564','DTXCID6070148','DTXCID502219','DTXCID70111950','DTXCID1096973','DTXCID70223305','DTXCID1096977','DTXCID501124','DTXCID1074963','DTXCID7035804','DTXCID104998','DTXCID0047903','DTXCID30322166','DTXCID90105856','DTXCID0042458','DTXCID8072403','DTXCID4041739','DTXCID902113','DTXCID2041468','DTXCID0043553','DTXCID1018800','DTXCID202149','DTXCID80233894','DTXCID70818533','DTXCID0035954','DTXCID70818775','DTXCID7014070','DTXCID8036736','DTXCID801404766','DTXCID00378357','DTXCID8034796','DTXCID605591','DTXCID605355','DTXCID8096821','DTXCID4096235','DTXCID2042569','DTXCID3030183','DTXCID70286096','DTXCID6074962','DTXCID9042839','DTXCID8096825','DTXCID80302998','DTXCID10332208','DTXCID40818727','DTXCID10129812','DTXCID2030350','DTXCID60819574','DTXCID60818486','DTXCID60818244','DTXCID2020711','DTXCID7046704','DTXCID8043024','DTXCID00818602','DTXCID9027029','DTXCID8027610','DTXCID304394','DTXCID20819050','DTXCID809195','DTXCID206729','DTXCID2035803','DTXCID106712','DTXCID7030777','DTXCID102111','DTXCID90812775','DTXCID60161','DTXCID6026614','DTXCID4027654','DTXCID7042518','DTXCID5027061','DTXCID806922','DTXCID0031130','DTXCID401311','DTXCID90119195','DTXCID40322410','DTXCID8037885','DTXCID9040479','DTXCID404822','DTXCID20418204','DTXCID20142017','DTXCID2046703','DTXCID10126725','DTXCID1035151','DTXCID7040108','DTXCID6039714','DTXCID102137','DTXCID90145479','DTXCID50103358','DTXCID9042883','DTXCID0043347','DTXCID7042772','DTXCID306118','DTXCID9041318','DTXCID2042359','DTXCID70818977','DTXCID802106','DTXCID205613','DTXCID30107112','DTXCID2030140','DTXCID70818730','DTXCID7027174','DTXCID20100430','DTXCID0099360','DTXCID401332','DTXCID6035106','DTXCID606880','DTXCID604464','DTXCID901248','DTXCID602284','DTXCID1047158','DTXCID80210524','DTXCID5044865','DTXCID2017748','DTXCID20818164','DTXCID60818209','DTXCID0045545','DTXCID605318','DTXCID7032509','DTXCID80813110','DTXCID10297','DTXCID001873','DTXCID6036625','DTXCID80818680','DTXCID801032','DTXCID5027938','DTXCID9047916','DTXCID00134036','DTXCID903622','DTXCID20335149','DTXCID2070843','DTXCID1047738','DTXCID00150756','DTXCID90818717','DTXCID10819120','DTXCID1030076','DTXCID10143275','DTXCID1036836','DTXCID7034547','DTXCID60529','DTXCID501021357','DTXCID4048777','DTXCID50124866','DTXCID4048779','DTXCID70285625','DTXCID607593','DTXCID70159059','DTXCID1047730','DTXCID20818780','DTXCID00819073','DTXCID1043128','DTXCID2042147','DTXCID301510702','DTXCID507507','DTXCID1037931','DTXCID601814','DTXCID0094528','DTXCID1093628','DTXCID0037946','DTXCID6031170','DTXCID6040901','DTXCID30818817','DTXCID30328789','DTXCID107865','DTXCID701648','DTXCID0027935','DTXCID1042079','DTXCID305227','DTXCID40134373','DTXCID302198','DTXCID50818177','DTXCID209170','DTXCID4038714','DTXCID40569','DTXCID707186','DTXCID805659','DTXCID20106578','DTXCID1035527','DTXCID30752','DTXCID0035792','DTXCID70819000','DTXCID9020667','DTXCID607373','DTXCID7036741','DTXCID901206','DTXCID9020665','DTXCID40328856','DTXCID10102509','DTXCID0024424','DTXCID601830','DTXCID00814649','DTXCID80818443','DTXCID001438','DTXCID70582','DTXCID1031177','DTXCID1070983','DTXCID7046548','DTXCID804585','DTXCID907184','DTXCID806765','DTXCID70151139','DTXCID1047780','DTXCID106796','DTXCID3036311','DTXCID90104309','DTXCID6042020','DTXCID706915','DTXCID6021321','DTXCID70108587','DTXCID203852','DTXCID704971','DTXCID00331028','DTXCID209154','DTXCID3024572','DTXCID2031293','DTXCID8043498','DTXCID4088478','DTXCID502010','DTXCID30730','DTXCID7021491','DTXCID6032699','DTXCID6042496','DTXCID605135','DTXCID20322933','DTXCID8034530','DTXCID20321603','DTXCID30161451','DTXCID70332749','DTXCID7048906','DTXCID4037613','DTXCID6047945','DTXCID9030048','DTXCID9089992','DTXCID6099144','DTXCID0072516','DTXCID002109','DTXCID40811031','DTXCID006946','DTXCID0038837','DTXCID4024517','DTXCID0036215','DTXCID704955','DTXCID50811108','DTXCID50101334','DTXCID5020285','DTXCID0084935','DTXCID10125819','DTXCID2043294','DTXCID3031078','DTXCID109600','DTXCID40133088','DTXCID20161162','DTXCID70819429','DTXCID8032590','DTXCID201897','DTXCID305662','DTXCID10818254','DTXCID1014402','DTXCID50811103','DTXCID0024472','DTXCID0030900','DTXCID30818610','DTXCID9035701','DTXCID10818259','DTXCID4047880','DTXCID6030073','DTXCID10131016','DTXCID3047893','DTXCID3047891','DTXCID20819419','DTXCID6043129','DTXCID70818351','DTXCID20811938','DTXCID60320211','DTXCID1048887','DTXCID70115099','DTXCID3036521','DTXCID401976','DTXCID00322315','DTXCID604066','DTXCID406183','DTXCID8042137','DTXCID60351','DTXCID5042291','DTXCID20145947','DTXCID407037','DTXCID40811298','DTXCID40759','DTXCID504656','DTXCID40519','DTXCID1095828','DTXCID8040193','DTXCID004986','DTXCID6032481','DTXCID4042046','DTXCID60333361','DTXCID40285753','DTXCID30149890','DTXCID806325','DTXCID80819132','DTXCID907265','DTXCID704413','DTXCID10107216','DTXCID40819491','DTXCID107200','DTXCID6034847','DTXCID30321538','DTXCID1043540','DTXCID9048726','DTXCID90109637','DTXCID4034659','DTXCID5024392','DTXCID2035695','DTXCID2034124','DTXCID40120276','DTXCID701384','DTXCID0036346','DTXCID5033094','DTXCID00161822','DTXCID1028154','DTXCID00112758','DTXCID4047969','DTXCID10130226','DTXCID901947','DTXCID00320831','DTXCID6043545','DTXCID90115071','DTXCID10100828','DTXCID90118582','DTXCID20377286','DTXCID90116164','DTXCID2035221','DTXCID20377281','DTXCID30115631','DTXCID2033289','DTXCID9035418','DTXCID7021287','DTXCID101908','DTXCID80819117','DTXCID9038923','DTXCID3043940','DTXCID8029656','DTXCID50810732','DTXCID209219','DTXCID7024790','DTXCID90320901','DTXCID3038353','DTXCID00131020','DTXCID00132592','DTXCID0030827','DTXCID201978','DTXCID1089760','DTXCID8018754','DTXCID30357756','DTXCID20319','DTXCID9030171','DTXCID60459696','DTXCID10817000','DTXCID306710','DTXCID0024397','DTXCID2024404','DTXCID10810612','DTXCID2024402','DTXCID50284954','DTXCID402066','DTXCID00809570','DTXCID50818470','DTXCID901968','DTXCID0033091','DTXCID30125417','DTXCID30402957','DTXCID203917','DTXCID80133688','DTXCID8038358','DTXCID40149907','DTXCID8040438','DTXCID404000','DTXCID2024878','DTXCID5075947','DTXCID20292501','DTXCID201399656','DTXCID40819017','DTXCID401324312','DTXCID101940','DTXCID806126','DTXCID705309','DTXCID60232744','DTXCID806121','DTXCID6040246','DTXCID5048558','DTXCID501022917','DTXCID40145','DTXCID2033075','DTXCID801914','DTXCID1043338','DTXCID9033262','DTXCID2033079','DTXCID4035700','DTXCID501765606','DTXCID90818999','DTXCID1032694','DTXCID101017445','DTXCID5071339','DTXCID206164','DTXCID90818510','DTXCID40144270','DTXCID50120343','DTXCID50120101','DTXCID40815913','DTXCID60818906','DTXCID6032855','DTXCID60600','DTXCID6032857','DTXCID607153','DTXCID407537','DTXCID40809478','DTXCID607158','DTXCID3039408','DTXCID60818901','DTXCID005330','DTXCID2095318','DTXCID002065','DTXCID40819032','DTXCID006661','DTXCID402087','DTXCID7031084','DTXCID00818465','DTXCID30334805','DTXCID00818223','DTXCID208323','DTXCID00129386','DTXCID40285536','DTXCID206148','DTXCID309182','DTXCID40120291','DTXCID30161759','DTXCID0085747','DTXCID0021915','DTXCID9048982','DTXCID8030681','DTXCID107268','DTXCID60118337','DTXCID5026548','DTXCID20761','DTXCID70320343','DTXCID80224','DTXCID5027643','DTXCID4033502','DTXCID2038782','DTXCID201936','DTXCID201931','DTXCID90108524','DTXCID3027661','DTXCID40113527','DTXCID7026526','DTXCID4040050','DTXCID2070263','DTXCID7026524','DTXCID4043301','DTXCID90818792','DTXCID10133954','DTXCID4043303','DTXCID606089','DTXCID50119378','DTXCID40211219','DTXCID1042499','DTXCID6028155','DTXCID7043081','DTXCID0048139','DTXCID00142590','DTXCID005791','DTXCID2019497','DTXCID1030284','DTXCID506797','DTXCID509821','DTXCID4034607','DTXCID101741','DTXCID807258','DTXCID3042471','DTXCID4040262','DTXCID40119680','DTXCID401515355','DTXCID8026567','DTXCID40290029','DTXCID5015272','DTXCID9024352','DTXCID60288746','DTXCID807494','DTXCID101982','DTXCID10545235','DTXCID9037666','DTXCID00122973','DTXCID2086874','DTXCID70321491','DTXCID0029','DTXCID80332437','DTXCID705587','DTXCID301647','DTXCID8048105','DTXCID60335148','DTXCID10813264','DTXCID0024','DTXCID90601','DTXCID2041969','DTXCID30810795','DTXCID70818831','DTXCID6086678','DTXCID6038118','DTXCID20812208','DTXCID0042402','DTXCID606940','DTXCID201915','DTXCID00196513','DTXCID2037003','DTXCID30112761','DTXCID40142216','DTXCID00810702','DTXCID502617','DTXCID7041754','DTXCID30161719','DTXCID50154704','DTXCID10818739','DTXCID2030851','DTXCID7026574','DTXCID9022154','DTXCID3040271','DTXCID00599','DTXCID805094','DTXCID0040674','DTXCID90248590','DTXCID10379577','DTXCID6041551','DTXCID309145','DTXCID1042863','DTXCID0042616','DTXCID3017498','DTXCID70818811','DTXCID2039673','DTXCID1044801','DTXCID70107712','DTXCID0011906','DTXCID301663','DTXCID301668','DTXCID2086616','DTXCID401474135','DTXCID10132826','DTXCID702055','DTXCID9048776','DTXCID1027001','DTXCID5039010','DTXCID2072467','DTXCID5039012','DTXCID8035009','DTXCID4039029','DTXCID3039034','DTXCID20198913','DTXCID406824','DTXCID60323121','DTXCID7039462','DTXCID701409963','DTXCID60818527','DTXCID3021518','DTXCID404880','DTXCID00818440','DTXCID1039216','DTXCID1018771','DTXCID5017478','DTXCID4042882','DTXCID60334252','DTXCID5021916','DTXCID5021910','DTXCID2048169','DTXCID10818916','DTXCID7041708','DTXCID0038176','DTXCID3030682','DTXCID2023937','DTXCID7038781','DTXCID001365643','DTXCID00767','DTXCID301600','DTXCID704057','DTXCID4027490','DTXCID10818911','DTXCID40811955','DTXCID6042814','DTXCID406609','DTXCID30285424','DTXCID00818869','DTXCID00818627','DTXCID70332188','DTXCID609171','DTXCID30294319','DTXCID0039011','DTXCID70111192','DTXCID50162147','DTXCID10196605','DTXCID40818343','DTXCID7072672','DTXCID3042897','DTXCID10596','DTXCID402480','DTXCID3042899','DTXCID9026558','DTXCID5039224','DTXCID1049061','DTXCID40131105','DTXCID6040666','DTXCID9096866','DTXCID2027046','DTXCID4042620','DTXCID4018798','DTXCID30102506','DTXCID8040648','DTXCID4042624','DTXCID0041567','DTXCID809250','DTXCID70134265','DTXCID80285651','DTXCID8094677','DTXCID0043505','DTXCID20334798','DTXCID0086','DTXCID7094216','DTXCID50818410','DTXCID9036359','DTXCID80111224','DTXCID8036150','DTXCID5031979','DTXCID001376547','DTXCID9048350','DTXCID90819466','DTXCID20126872','DTXCID90667','DTXCID5030400','DTXCID9024148','DTXCID8027664','DTXCID40158817','DTXCID9068166','DTXCID9027655','DTXCID8027668','DTXCID506394','DTXCID5074894','DTXCID30334860','DTXCID601238','DTXCID1020649','DTXCID00818884','DTXCID00818400','DTXCID40144412','DTXCID6015001','DTXCID90333617','DTXCID604508','DTXCID4026391');
;


select distinct dp.canon_qsar_smiles, dpc.dtxcid from qsar_datasets.data_points dp
		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
    join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
		where dpis.fk_splitting_id=1 and d.id=116 and dpis.split_num=1;



select dr.dtxsid,qpn.dtxsid from qsar_models.predictions_dashboard pd
join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id
join qsar_models.qsar_predicted_neighbors qpn on pd.id = qpn.fk_predictions_dashboard_id
where dr.dtxcid='DTXCID001016068' and pd.fk_model_id=1173;


select distinct dp.id, dp.qsar_property_value,dpc.property_value, dp.canon_qsar_smiles,dpc.dtxsid,smiles,sc.source_casrn,sc.source_chemical_name,sc.source_smiles,sc.source_dtxsid from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
		    join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
        join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id
        join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
		where dpis.fk_splitting_id=1 and d.id=116 and dpis.split_num=1;


select distinct  dp.canon_qsar_smiles,dpc.dtxsid,smiles from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
		    join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
		where  d.id=116 and dpc.dtxsid='DTXSID30199335';



select distinct dpc.dtxsid from qsar_datasets.data_points dp
		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
    join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
		where d.id in (116,536,107);



select distinct  dp.canon_qsar_smiles,dpc.dtxsid,dpc.dtxcid,smiles from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where d.id=536;


select count(dp.id) from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where d.id=536;



select count(dp.id)  from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_descriptors.descriptor_values dv on dp.canon_qsar_smiles=dv.canon_qsar_smiles
where dp.fk_dataset_id=536 and dv.fk_descriptor_set_id=6
  and dp.canon_qsar_smiles not in
(select dp.canon_qsar_smiles from qsar_datasets.data_points dp
where  dp.fk_dataset_id=116);



select distinct  dp.canon_qsar_smiles,dpc.dtxsid,dpc.dtxcid,smiles from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where dpis.fk_splitting_id=1 and d.id=536 and dpis.split_num=1;


select * from exp_prop.property_values pv
         join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
         where pv.id in (981070,5094);




select trim(replace(d.name,'v1 modeling','')) as property, MAX(dp.qsar_property_value)-MIN(dp.qsar_property_value) AS range from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where dpis.fk_splitting_id=1 and d.name like '% v1 modeling' and dpis.split_num=0
group by d.name;


select count(distinct dp.canon_qsar_smiles) from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
join qsar_datasets.splittings s on dpis.fk_splitting_id = s.id
where s.name='T=PFAS only, P=PFAS' and d.name = 'HLC v1 modeling' and dpis.split_num=1
;


select distinct dp.canon_qsar_smiles,dp.qsar_dtxcid,dp.qsar_property_value from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
where dp.fk_dataset_id=116 and dpis.fk_splitting_id=1 and dpis.split_num=1;


select sc.source_smiles,sc.source_chemical_name,sc.source_casrn from exp_prop.property_values pv
join exp_prop.source_chemicals sc on pv.fk_source_chemical_id = sc.id
-- where pv.id=48293 or pv.id=556107;
where pv.id=10713;


select * from qsar_datasets.data_points dp
    join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
where d.name='WS v1 modeling' and dp.canon_qsar_smiles='CC(C)CC(N)C(O)=NC(CC1C=CC(O)=CC=1)C(O)=O'


select dp.canon_qsar_smiles from qsar_datasets.data_points dp
join qsar_datasets.datasets d on dp.fk_dataset_id = d.id
join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id
join qsar_datasets.splittings s on dpis.fk_splitting_id = s.id
where d.name='BP v1 modeling' and s.name='RND_REPRESENTATIVE' and dpis.split_num=0 and dp.canon_qsar_smiles like '%F%'
and dp.canon_qsar_smiles not in (select dp2.canon_qsar_smiles from qsar_datasets.data_points dp2
join qsar_datasets.datasets d on dp2.fk_dataset_id = d.id
join qsar_datasets.data_points_in_splittings dpis on dp2.id = dpis.fk_data_point_id
join qsar_datasets.splittings s on dpis.fk_splitting_id = s.id
where d.name='BP v1 modeling' and s.name='T=PFAS only, P=PFAS' and dpis.split_num=0)
;
