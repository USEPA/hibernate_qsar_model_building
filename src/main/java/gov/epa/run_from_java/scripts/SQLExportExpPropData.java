package gov.epa.run_from_java.scripts;

/**
* @author TMARTI02
*/
public class SQLExportExpPropData {


	/**
	 * -- Following uses datasets_in_dashboard to look up dataset name based on property name
select p."name" as property,d."name" as dataset, dpc.dtxsid, dpc.dtxcid, dp.canon_qsar_smiles, dpc.smiles , pv.id as exp_prop_property_values_id,
dpc.property_value as property_value,u.abbreviation as property_units,
pvT.value_point_estimate as temperature_c,pvP.value_point_estimate as pressure_mmHg, pvpH.value_point_estimate as pH,pv.notes,
ps."name" as public_source_name,ps.description as public_source_description ,ps.url as public_source_url,
pv.page_url,ls."name" as ls_name,ls.description as ls_description,ls.url as ls_url
from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id =dp.id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id
left join exp_prop.literature_sources ls on pv.fk_literature_source_id=ls.id
left join exp_prop.public_sources ps on pv.fk_public_source_id =ps.id
left join exp_prop.parameter_values pvT on pvT.fk_property_value_id =pv.id and pvT.fk_parameter_id=2
left join exp_prop.parameter_values pvP on pvP.fk_property_value_id =pv.id and pvP.fk_parameter_id=1
left join exp_prop.parameter_values pvpH on pvpH.fk_property_value_id =pv.id and pvpH.fk_parameter_id=3
join qsar_datasets.datasets d on dp.fk_dataset_id =d.id
join qsar_datasets.properties p on d.fk_property_id =p.id
join qsar_datasets.datasets_in_dashboard did on did.fk_property_id =d.fk_property_id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor
--where p."name" ='Henry''s law constant'
where p."name" ='Water solubility'
--and pv.value_point_estimate is null
--and dtxsid='DTXSID3039242'
order by canon_qsar_smiles


-- Simpler version
select p."name" as property,dpc.dtxsid,  dpc.property_value as property_value,u.abbreviation as property_units,
ps."name" as public_source_name,ps.description as public_source_description ,ps.url as public_source_url,
ls."name" as ls_name,ls.description as ls_description,ls.url as ls_url
from qsar_datasets.data_points dp
join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id =dp.id
join exp_prop.property_values pv on dpc.exp_prop_property_values_id = pv.id
left join exp_prop.literature_sources ls on pv.fk_literature_source_id  =ls.id
left join exp_prop.public_sources ps on pv.fk_public_source_id =ps.id
join qsar_datasets.datasets d on dp.fk_dataset_id =d.id
join qsar_datasets.properties p on d.fk_property_id =p.id
join qsar_datasets.datasets_in_dashboard did on did.fk_property_id =d.fk_property_id
join qsar_datasets.units u on u.id=d.fk_unit_id_contributor
where p."name" ='Henry''s law constant'
--and pv.value_point_estimate is null
--where p."name" ='Water solubility'
order by canon_qsar_smiles

*/	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
