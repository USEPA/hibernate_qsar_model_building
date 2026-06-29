select count(id) from measured_properties


select count(distinct(mp.fk_endpoint_id)) from measured_properties mp


select distinct(mp.fk_endpoint_id),e.name from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
where e.name not like '%LD%' and e.name not like '%OEL%' and e.name not like '%AEL%' and e.name not like '%Acute%'
and e.name not like '%AEC%' and e.name not like '%NOEC%' and e.name not like '%Dose%' and e.name not like '%counts%'
and e.name not like '%LLNA%' and e.name not like '%Volume%' and e.name not like '%Domestic Manufacturing Production%'
and e.name not like '%In Vitro Intrinsic Hepatic Clearance' and e.name not like '%Fraction Unbound in Human Plasma'


select distinct(fk_measurement_method_id),cd.name from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
join collection_details cd on mp.fk_measurement_method_id = cd.id
where e.name='Bioconcentration Factor' and fk_measurement_method_id is not null
order by cd.name

select * from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
join collection_details cd on mp.fk_measurement_method_id = cd.id
where e.name='Bioconcentration Factor' and fk_measurement_method_id is not null
order by cd.name

select * from prod_chemprop.endpoints;


select distinct(fk_measurement_method_id),sd.name from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
join source_details sd on mp.fk_source_detail_id = sd.id
where e.name='Bioconcentration Factor' and mp.fk_source_detail_id!=null
order by sd.name


select * from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
left join source_details sd on mp.fk_source_detail_id = sd.id
left join collection_details cd on mp.fk_measurement_method_id = cd.id
where e.name='Soil Adsorp. Coeff. (Koc)' and cd.id is not null
order by cd.name desc;

select * from prod_chemprop.qsar_predicted_properties qpp where qpp.efk_qsar_model_id<=16 order by updated_at desc

select distinct (e.name) from measured_properties mp
join endpoints  e on mp.fk_endpoint_id=e.id


select distinct cd.name from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
join collection_details cd on mp.fk_measurement_method_id = cd.id
where e.name ='Vapor Pressure';

select * from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
join collection_details cd on mp.fk_measurement_method_id = cd.id
where e.name ='Biodegration Class';


select * from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
join collection_details cd on mp.fk_measurement_method_id = cd.id
where e.name ='LLNA-Binary'

select distinct source from prod_dsstox.compound_relationships


select distinct (cd.name) from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
join source_details cd on mp.fk_source_detail_id = cd.id
where e.name='In Vitro Intrinsic Hepatic Clearance'


select distinct (efk_qsar_model_id) from computational_experiments

# Determine which endpoints dont have measured_properties:
select * from endpoints
left join measured_properties mp on endpoints.id = mp.fk_endpoint_id
where  fk_endpoint_id is null;


select e.name, count(mp.id) from measured_properties mp
left join input_set_measured_properties ismp on mp.id = ismp.fk_measured_property_id
join endpoints e on mp.fk_endpoint_id = e.id
# where ismp.fk_measured_property_id is not null
group by e.name;


select * from measured_properties mp
left join input_set_measured_properties ismp on mp.id = ismp.fk_measured_property_id
join endpoints e on mp.fk_endpoint_id = e.id
# where ismp.fk_measured_property_id is not null
where e.name='Surface Tension'






select distinct  cd.name from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
join collection_details cd on mp.fk_measurement_method_id = cd.id
where e.name='pKa Acidic Apparent' and mp.fk_measurement_method_id is not null;

select * from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
where e.name='Biodegration Class'


select distinct  e.name, cd.name from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
left join source_details cd on mp.fk_source_detail_id = cd.id
where  e.name='LogKoa: Octanol-Air' ;

select distinct  cd.id, cd.name from prod_chemprop.measured_properties mp
join prod_chemprop.endpoints e on mp.fk_endpoint_id = e.id
join prod_chemprop.collection_details cd on mp.fk_measurement_method_id = cd.id
where  e.name='LogKow: Octanol-Water' ;

select count(mp.id) from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
join collection_details cd on mp.fk_measurement_method_id = cd.id
where  e.name='LogKoa: Octanol-Air'  and cd.name='Danish_EPA_PFOA_Report_2005';


select * from prod_chemprop.measured_properties mp
join prod_chemprop.endpoints e on mp.fk_endpoint_id = e.id
join prod_chemprop.collection_details cd on mp.fk_measurement_method_id = cd.id
where cd.name='PhysPropNCCT' and e.name='Vapor Pressure';


select distinct fk_source_detail_id from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
where e.name='Acute Oral LD50' and mp.fk_source_detail_id is not null

select * from collection_details
             where (collection_type is null or collection_type='PAPER') and name not like '%_Report%'
order by name




select distinct fk_source_detail_id,fk_measurement_method_id from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
where e.name='Acute Oral LD50'

select * from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
where mp.fk_endpoint_id=139


select * from measured_properties mp
join endpoints e on mp.fk_endpoint_id = e.id
# where e.name='Henry''s Law' and fk_measurement_method_id=1;
where e.name='Fish Biotrans. Half-Life (Km)' and fk_measurement_method_id=1 -- and fk_measurement_method_id=1;



select distinct (pv.value_text) from measured_properties mp
join parameter_values pv on mp.fk_parameter_set_id = pv.fk_parameter_set_id
where pv.fk_parameter_id=5 and mp.fk_endpoint_id=24;


select * from measured_properties mp
join parameter_values pv on mp.fk_parameter_set_id = pv.fk_parameter_set_id

where pv.fk_parameter_id=5 and mp.fk_endpoint_id=24 and value_text='Daucus carota ssp. sativus';



# Look up a value in chemprop for logKow and a given cas number:
select identifier,result_value from prod_chemprop.measured_properties
join prod_dsstox.source_substances ss on efk_dsstox_source_substance_id=ss.id
join prod_dsstox.source_substance_identifiers ssi on ss.id = ssi.fk_source_substance_id
where fk_endpoint_id=16 and ssi.identifier in ('6108-10-7','319-84-6','58-89-9',
'319-86-8','319-85-7','608-73-1','119911-69-2');


# Look up exp values in chemprop a given cas number:
select e.name, identifier,result_value,result_text from prod_chemprop.measured_properties
join prod_chemprop.endpoints e on measured_properties.fk_endpoint_id = e.id
    join prod_dsstox.source_substances ss on efk_dsstox_source_substance_id=ss.id
join prod_dsstox.source_substance_identifiers ssi on ss.id = ssi.fk_source_substance_id
where ssi.identifier ='541-05-9' and ssi.identifier_type='CASRN'
order by e.name;



select count(distinct c.dsstox_compound_id,c.smiles) from prod_chemprop.qsar_predicted_properties qpp
join prod_dsstox.compounds c on qpp.efk_dsstox_compound_id=c.id
where qpp.efk_qsar_model_id=31 and smiles  not like '%C%';

select c.dsstox_compound_id,c.smiles,qpp.result_value from prod_chemprop.qsar_predicted_properties qpp
join prod_dsstox.compounds c on qpp.efk_dsstox_compound_id=c.id
# where qpp.efk_qsar_model_id=31 and smiles  not like '%C%';
where qpp.efk_qsar_model_id=31 and smiles  is null;


select count(distinct c.dsstox_compound_id) from prod_chemprop.qsar_predicted_properties qpp
join prod_dsstox.compounds c on qpp.efk_dsstox_compound_id=c.id
# where qpp.efk_qsar_model_id=31 and smiles  not like '%C%';
where qpp.efk_qsar_model_id=33;


select dsstox_compound_id,mol_formula, smiles from prod_dsstox.compounds;
# join prod_dsstox.generic_substance_compounds gsc on compounds.id = gsc.fk_compound_id
# join prod_dsstox.generic_substances gs on gsc.fk_generic_substance_id = gs.id

select distinct qpp.efk_dsstox_compound_id, c.dsstox_compound_id,c.mol_formula,c.smiles from prod_chemprop.qsar_predicted_properties qpp
join prod_qsar.models m on qpp.efk_qsar_model_id=m.id
join prod_dsstox.compounds c on c.id=qpp.efk_dsstox_compound_id
where m.name like '%EPISUITE%';


select  count(distinct(efk_dsstox_compound_id)) as predcount,m.name   from prod_chemprop.qsar_predicted_properties qpp
join prod_qsar.models m on qpp.efk_qsar_model_id=m.id
join prod_dsstox.compounds c on c.id=qpp.efk_dsstox_compound_id
join prod_chemprop.endpoints e on e.id=m.efk_chemprop_endpoint_id
# where m.name like '%EPISUITE%'
where m.name like '%OPERA%'
group by m.name;


select  m.name,e.name  from prod_qsar.models m
join prod_chemprop.endpoints e on e.id=m.efk_chemprop_endpoint_id
# where m.name like '%EPISUITE%' and m.created_by='cgrulke';
where m.name like '%EPISUITE%' ;

select m.name, result_value,e.standard_unit from prod_chemprop.qsar_predicted_properties qpp
join prod_dsstox.compounds c on qpp.efk_dsstox_compound_id=c.id
join generic_substance_compounds gsc on c.id = gsc.fk_compound_id
join generic_substances gs on gsc.fk_generic_substance_id = gs.id
join prod_qsar.models m on efk_qsar_model_id=m.id
join prod_chemprop.endpoints e on m.efk_chemprop_endpoint_id=e.id
# where gs.dsstox_substance_id='DTXSID3039242';
where gs.dsstox_substance_id='DTXSID001027667';





select m.name, result_value,e.standard_unit from prod_chemprop.qsar_predicted_properties qpp
join prod_dsstox.compounds c on qpp.efk_dsstox_compound_id=c.id
join prod_qsar.models m on efk_qsar_model_id=m.id
join prod_chemprop.endpoints e on m.efk_chemprop_endpoint_id=e.id
# where c.dsstox_compound_id='DTXCID20152994' and m.name like '%ACD_Prop%' and m.efk_chemprop_endpoint_id=47
# where (m.efk_chemprop_endpoint_id=47 or m.efk_chemprop_endpoint_id=48) and m.name not like 'OPERA%';
where m.name = 'ACD_BP';
# where (m.efk_chemprop_endpoint_id=47 or m.efk_chemprop_endpoint_id=48) ;
-- where gs.dsstox_substance_id='DTXSID001027667'


select  am.name,qpad.applicability_value from prod_chemprop.qsar_predicted_properties qpp
join prod_dsstox.compounds c on c.id=qpp.efk_dsstox_compound_id
join prod_dsstox.generic_substance_compounds gsc on c.id = gsc.fk_compound_id
join prod_dsstox.generic_substances gs on gsc.fk_generic_substance_id = gs.id
join prod_qsar.models m on qpp.efk_qsar_model_id=m.id
join prod_chemprop.qsar_prediction_ad_estimates qpad on qpp.id = qpad.fk_qsar_predicted_property_id
join prod_qsar.ad_methods am on am.id=qpad.efk_qsar_ad_method_id
where gs.dsstox_substance_id='DTXSID7020182' and m.name = 'OPERA_PKAA';

