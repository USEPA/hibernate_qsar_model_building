package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.sql.ResultSet;

import gov.epa.run_from_java.scripts.SqlUtilities;

/**
* @author TMARTI02
*/
public class backup_mv_predicted_data {

	static ResultSet getResultSet () {
		
		String sql="select\r\n"
				+ "	       row_number() over (order by dr.dtxsid, p.\"name\",s.name) as id,\r\n"
				+ "	       dr.dtxsid,dr.dtxcid,dr.smiles,\r\n"
				+ "	       pd.canon_qsar_smiles,\r\n"
				+ "	       dr.generic_substance_updated_at,\r\n"
				+ "	       p.name_ccd as prop_name,\r\n"
				+ "	       'predicted' as prop_type,\r\n"
				+ "	       pc.name as prop_category,\r\n"
				+ "	       p.description as property_description,\r\n"
				+ "	       m.name_ccd as model_name,\r\n"
				+ "	       m.id as model_id,\r\n"
				+ "	       s.name as source_name,\r\n"
				+ "	       s.description as source_description,\r\n"
				+ "	       pd.experimental_value as prop_value_experimental,\r\n"
				+ "	       pd.experimental_string as prop_value_experimental_string,\r\n"
				+ "	       pd.prediction_value as prop_value,\r\n"
				+ "	       u.abbreviation_ccd as prop_unit,\r\n"
				+ "	       prediction_string as prop_value_string,\r\n"
				+ "	       prediction_error as prop_value_error,\r\n"
				+ "	       adm.name as AD_method,\r\n"
				+ "	       qpad.applicability_value as AD_value,\r\n"
				+ "	       qpad.conclusion as AD_conclusion,\r\n"
				+ "	       qpad.reasoning as AD_reasoning,\r\n"
				+ "	       case when s.name ='OPERA2.8' then 'OPERA Global Index' end as AD_method_global,\r\n"
				+ "	       qpad2.applicability_value as AD_value_global,\r\n"
				+ "	       qpad2.conclusion as AD_conclusion_global,\r\n"
				+ "	       qpad2.reasoning as AD_reasoning_global,\r\n"
				+ "	       m.has_qmrf,\r\n"
				+ "	       case when m.has_qmrf is true then CONCAT('https://ctx-api-dev.ccte.epa.gov/chemical/property/model/file/search/?modelId=',m.id,'&typeId=1') end as qmrf_url,\r\n"
				+ "	       current_date as export_date,\r\n"
				+ "	       '2.0.0' as data_version\r\n"
				+ "	from qsar_models.predictions_dashboard pd\r\n"
				+ "	join qsar_models.models m on m.id=pd.fk_model_id\r\n"
				+ "	join qsar_models.sources s on m.fk_source_id = s.id\r\n"
				+ "	join qsar_datasets.datasets d on d.\"name\" =m.dataset_name\r\n"
				+ "	join qsar_datasets.properties p on p.id=d.fk_property_id\r\n"
				+ "	join qsar_datasets.units u on u.id=d.fk_unit_id_contributor\r\n"
				+ "	join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
				+ "	left join qsar_models.qsar_predicted_ad_estimates qpad on pd.id = qpad.fk_predictions_dashboard_id and qpad.fk_ad_method_id=m.fk_ad_method\r\n"
				+ "	left join qsar_models.qsar_predicted_ad_estimates qpad2 on pd.id = qpad2.fk_predictions_dashboard_id and qpad2.fk_ad_method_id=1\r\n"
				+ "	left join qsar_models.ad_methods adm on m.fk_ad_method = adm.id\r\n"
				+ "	left join qsar_datasets.properties_in_categories pic on p.id = pic.fk_property_id\r\n"
				+ "	left join qsar_datasets.property_categories pc on pic.fk_property_category_id = pc.id\r\n"
				+ "\r\n"
				+ "	where dr.fk_dsstox_snapshot_id=2 \r\n"
				+ "	  and (s.name='OPERA2.8' or s.name='Percepta2023.1.2');";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		return rs;
		
	}
	
	static void export() {
		
		ResultSet rs=getResultSet();
		
		
		
		
	}
	
	
	public static void main(String[] args) {
		export();

	}

}
