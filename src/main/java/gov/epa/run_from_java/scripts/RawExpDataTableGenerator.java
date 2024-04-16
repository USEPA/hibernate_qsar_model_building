package gov.epa.run_from_java.scripts;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gov.epa.databases.dev_qsar.DevQsarConstants;
//import gov.epa.exp_data_gathering.parse.ExperimentalRecord;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;



/**
 * @author TMARTI02
 */
public class RawExpDataTableGenerator {

	Connection conn=SqlUtilities.getConnectionPostgres();	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();


	public static final String sqlGetFields="select p.name_ccd    as property,\r\n"
			+ "d.\"name\"    as dataset,\r\n"
			+ "dr.preferred_name  as chemical_name,\r\n"
			+ "dpc.dtxsid,\r\n"
			+ "dpc.dtxcid,\r\n"
			+ "dp.canon_qsar_smiles,\r\n"
			+ "dpc.smiles,\r\n"

			+ "dp.qsar_exp_prop_property_values_id as median_exp_prop_property_values_id,\r\n"
			+ "dpc.exp_prop_property_values_id as exp_prop_property_values_id,\r\n"
			+ "pv.id exp_prop_property_values_id,\r\n"

			+ "dpc.property_value as property_value,\r\n"
			+ "u.abbreviation_ccd     as property_units,\r\n"
			+ "pv.value_text      as property_value_text, --to get meaning of binary property values\r\n"

			+ "pvT.value_point_estimate  as temperature_c,\r\n"
			+ "pvP.value_point_estimate  as pressure_mmHg,\r\n"
			+ "pvpH.value_point_estimate as pH,\r\n"			
			+ "pvRS.value_text           as response_site,\r\n"
			+ "pvSL.value_text           as species_latin,\r\n"
			+ "pvSC.value_text           as species_common,\r\n"
			+ "ps.\"name\"   as public_source_name,\r\n"
			+ "ps.description     as public_source_description,\r\n"
			+ "ps.url      as public_source_url,\r\n"

			+ "ls.\"name\"   as literature_source_name,\r\n"
			+ "ls.citation as literature_source_description,\r\n"
			+ "ls.doi      as literature_source_doi,\r\n"

			//Note: right now there arent any property value records in the database with a public_source_original but there will be- for example toxval (AKA public source) sometimes pulls from ecotox (AKA the public source original) which then also has a literature source
			+ "ps2.\"name\"       as public_source_original_name,\r\n"
			+"ps2.description    as public_source_original_description,\r\n"
			+ "ps2.url          as public_source_original_url,\r\n"

		    + "pv.page_url      as direct_link,\r\n"
		    + "pv.file_name     as file_name_loaded,\r\n"
		    + "pv.document_name  as short_citation\r\n";

	public static final String sqlParameters = "left join exp_prop.parameter_values pvT on pvT.fk_property_value_id = pv.id and pvT.fk_parameter_id = 2\r\n"
			+ "left join exp_prop.parameter_values pvP on pvP.fk_property_value_id = pv.id and pvP.fk_parameter_id = 1\r\n"
			+ "left join exp_prop.parameter_values pvpH on pvpH.fk_property_value_id = pv.id and pvpH.fk_parameter_id = 3\r\n"
			+"left join exp_prop.parameter_values pvRS on pvRS.fk_property_value_id = pv.id and pvRS.fk_parameter_id = 22\r\n"
			+"left join exp_prop.parameter_values pvSL on pvSL.fk_property_value_id = pv.id and pvSL.fk_parameter_id = 21\r\n"
			+"left join exp_prop.parameter_values pvSC on pvSC.fk_property_value_id = pv.id and pvSC.fk_parameter_id = 11\r\n";


	public static final String sqlSources="left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id\r\n"
			+ "left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id\r\n"
			+"left join exp_prop.public_sources ps2 on pv.fk_public_source_original_id = ps2.id\r\n";

	public static final String sqlPropertyValues="join qsar_datasets.datasets d on m.dataset_name=d.name\r\n"
			+"join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id\r\n"
			+ "join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id\r\n"
			+ "join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id\r\n";

	public static final String sqlGetDatasetSplit="select split_num from qsar_models.models m\r\n"
			+ "join qsar_datasets.datasets d on m.dataset_name=d.name\r\n"
			+ "join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id\r\n"
			+ "join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"+
			"where m.id=? AND dp.canon_qsar_smiles=?;";



	public static final String sqlPropertyValuesCM="from  qsar_datasets.datasets d\r\n"
			+ "join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id\r\n"
			+ "join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id\r\n"
			+ "join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id\r\n"
			+" join qsar_datasets.datasets_in_cheminformatics_modules dcm on dcm.fk_property_id = d.fk_property_id\r\n";

	public static final String sqlPropertyValuesCD="from  qsar_datasets.datasets d\r\n"
			+ "join qsar_datasets.data_points dp on dp.fk_dataset_id = d.id\r\n"
			+ "join qsar_datasets.data_point_contributors dpc on dpc.fk_data_point_id = dp.id\r\n"
			+ "join exp_prop.property_values pv on dpc.exp_prop_property_values_id=pv.id\r\n"
			+" join qsar_datasets.datasets_in_dashboard did on did.fk_property_id = d.fk_property_id\r\n";

	public static final String sqlPropertyUnitsDsstoxRecords="join qsar_datasets.properties p on d.fk_property_id = p.id\r\n"
			+ "join qsar_datasets.units u on u.id = d.fk_unit_id_contributor\r\n"
			+ "left join qsar_models.dsstox_records dr on dr.dtxsid=dpc.dtxsid -- to look up chemical name\r\n";


	public static final String sqlChemicalsDashboardByPropertyNameAndDTXSID=sqlGetFields
			+sqlPropertyValuesCD+sqlSources+sqlParameters+sqlPropertyUnitsDsstoxRecords
			+"where d.id = did.fk_datasets_id and keep=true\r\n"
			+"and p.name_ccd=? and dpc.dtxsid=?\r\norder by dpc.property_value;";



	public static final String sqlCheminformaticsModulesByPropertyNameAndQsarSmiles = sqlGetFields
			+ sqlPropertyValuesCM+ sqlSources + sqlParameters+sqlPropertyUnitsDsstoxRecords
			+ "where d.id = dcm.fk_datasets_id and keep=true\r\n"
			+ "and p.name=? AND dp.canon_qsar_smiles=?\r\n" + "order by dpc.property_value;";


	public static final String sqlCheminformaticsModulesByModelIDandQsarSmiles=sqlGetFields
			+ "from qsar_models.models m\r\n"+sqlPropertyValues+sqlSources+sqlParameters+sqlPropertyUnitsDsstoxRecords
			+"where keep=true and m.id=? and canon_qsar_smiles=?\r\norder by dpc.property_value;";


	JsonArray getRawRecordsCheminformaticsModulesByModelIDandQsarSmiles(Long model_id, String qsarSmiles) {


		try  {
			System.out.println(sqlCheminformaticsModulesByModelIDandQsarSmiles);
			PreparedStatement prep = conn.prepareStatement(sqlCheminformaticsModulesByModelIDandQsarSmiles);
			prep.setLong(1, model_id);
			prep.setString(2, qsarSmiles);

			ResultSet rs = prep.executeQuery();

			JsonArray ja = getJsonArray(rs);
			System.out.println(Utilities.gson.toJson(ja));
			return ja;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	String getDatasetSplit(Long model_id, String qsarSmiles) {


		try  {
			System.out.println(sqlGetDatasetSplit);
			PreparedStatement prep = conn.prepareStatement(sqlGetDatasetSplit);
			prep.setLong(1, model_id);
			prep.setString(2, qsarSmiles);

			ResultSet rs = prep.executeQuery();

			if (rs.next()) {
				String set=rs.getString(1);
				if(set.equals("1")) return "test";
				else if (set.equals("0")) return "training";
				else return null;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}




	JsonArray getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID(String propertyName, String dtxsid) {

		try  {
			PreparedStatement prep = conn.prepareStatement(sqlChemicalsDashboardByPropertyNameAndDTXSID);

			//			System.out.println(sqlChemicalsDashboardByPropertyNameAndDTXSID);

			prep.setString(1,  propertyName);
			prep.setString(2, dtxsid);

			ResultSet rs = prep.executeQuery();

			JsonArray ja = getJsonArray(rs);

			//			System.out.println(Utilities.gson.toJson(ja));
			return ja;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}



	JsonArray getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID_FromView(String propertyName, String dtxsid) {

		try  {

			String sql="select * from public.v_experimental_data\n"
					+"where name=? and dtxsid=?\r\norder by prop_value;";
			//		System.out.println(sql);

			PreparedStatement prep = conn.prepareStatement(sql);
			prep.setString(1,  propertyName);
			prep.setString(2, dtxsid);

			ResultSet rs = prep.executeQuery();
			JsonArray ja = getJsonArray(rs);
			convertViewFieldNames(ja);

			System.out.println(Utilities.gson.toJson(ja));
			
			
			return ja;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Converts field names from the materialized view to the field names used in the sql in this class
	 * 
	 * @param ja
	 */
	private void convertViewFieldNames(JsonArray ja) {
		Hashtable<String,String>ht=new Hashtable<>();

		ht.put("prop_value", "property_value");
		ht.put("unit", "property_units");
		ht.put("name", "property");
		ht.put("prop_value_text", "property_value_text");
		ht.put("direct_url", "direct_link");
		ht.put("ls_name", "literature_source_name");
		ht.put("brief_citation", "short_citation");
		ht.put("ls_citation", "literature_source_description");
		ht.put("ls_doi", "literature_source_doi");

		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			for (String key:ht.keySet()) {
				if(jo.get(key)!=null && !jo.get(key).isJsonNull()) {
					jo.addProperty(ht.get(key), jo.get(key).getAsString());	
				}
			}
		}
	}



	List<String> getPropertyNamesForDashboard() {
		
		String nameField="p.name_ccd";
		
		String sql="select distinct "+nameField+" from qsar_datasets.datasets d\r\n"
				+ "join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id\r\n"
				+ "join qsar_datasets.properties p on d.fk_property_id = p.id\r\n"
				+ "join qsar_datasets.datasets_in_dashboard did on d.id = did.fk_datasets_id\r\n"
				+"order by "+nameField+";";

//		System.out.println(sql);
		
		ResultSet rs= SqlUtilities.runSQL2(conn, sql);
		List<String>properties=new ArrayList<>();

		try {
			while (rs.next()) {
				properties.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(properties);
		return properties;

	}

	List<String> getPropertyNamesForModules() {
		String sql="select distinct p.name from qsar_datasets.datasets d\r\n"
				+ "join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id\r\n"
				+ "join qsar_datasets.properties p on d.fk_property_id = p.id\r\n"
				+ "join qsar_datasets.datasets_in_cheminformatics_modules did on d.id = did.fk_datasets_id;\r\n";

		ResultSet rs= SqlUtilities.runSQL2(conn, sql);
		List<String>properties=new ArrayList<>();

		try {
			while (rs.next()) {
				properties.add(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;

	}


	JsonArray getRawRecordsCheminformaticsModulesByPropertyNameandQsarSmiles(String propertyName, String qsarSmiles) {	

		try  {

			//			System.out.println(sqlCheminformaticsModulesByPropertyNameAndQsarSmiles);
			PreparedStatement prep = conn.prepareStatement(sqlCheminformaticsModulesByPropertyNameAndQsarSmiles);
			prep.setString(1, propertyName);
			prep.setString(2, qsarSmiles);

			ResultSet rs = prep.executeQuery();
			JsonArray ja = getJsonArray(rs);
			System.out.println(Utilities.gson.toJson(ja));
			return ja;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}


	private JsonArray getJsonArray(ResultSet rs) throws SQLException {
		JsonArray ja=new JsonArray();

		while (rs.next()) {

			JsonObject jo=new JsonObject();

			for (int i=1;i<=rs.getMetaData().getColumnCount();i++) {
				String fieldName=rs.getMetaData().getColumnLabel(i);
				String fieldValue=rs.getString(i);
				//					System.out.println(fieldName+"\t"+fieldValue);
				jo.addProperty(fieldName, fieldValue);
			}
			ja.add(jo);

			//				System.out.println(rs.getString(1));			
		}
		return ja;
	}

	public static String setSignificantDigits(double value, int significantDigits) {
		if (significantDigits < 0) throw new IllegalArgumentException();

		// this is more precise than simply doing "new BigDecimal(value);"
		BigDecimal bd = new BigDecimal(value, MathContext.DECIMAL64);
		bd = bd.round(new MathContext(significantDigits, RoundingMode.HALF_UP));
		final int precision = bd.precision();
		if (precision < significantDigits)
			bd = bd.setScale(bd.scale() + (significantDigits-precision));
		return bd.toPlainString();
	}  

	String getFormattedValue(String value,String propertyName) {

		int nsig=3;

		DecimalFormat dfSci=new DecimalFormat("0.00E00");
		DecimalFormat dfInt=new DecimalFormat("0");

		//		DecimalFormat df1=new DecimalFormat("0.00");
		//		DecimalFormat df4=new DecimalFormat("0.0");

		try {
			double dvalue=Double.parseDouble(value);

			if(propertyName.equals(DevQsarConstants.RBIODEG) || propertyName.contains("receptor"))
				return setSignificantDigits(dvalue, 2);

			//			if(propertyName.equals(DevQsarConstants.BOILING_POINT) || propertyName.equals(DevQsarConstants.MELTING_POINT)) 
			//				return df4.format(dvalue);

			if(Math.abs(dvalue)<0.01 && dvalue!=0) {
				return dfSci.format(dvalue);
			}
			//			System.out.println(dvalue+"\t"+setSignificantDigits(dvalue, nsig));
			return setSignificantDigits(dvalue, nsig);

		} catch (Exception ex) {
			return value;
		}


	}


	String writeStyles()  {

		int width=400;

		return ("<style>\r\n" + "	.tooltip {\r\n" + "	  position: relative;\r\n" + "	  display: inline-block;\r\n"
				+ "	  border-bottom: 1px dotted black;\r\n" + "	}\r\n" + "\r\n" + "	.tooltip .tooltiptext {\r\n"
				+ "	  visibility: hidden;\r\n" + "	  width: "+width+"px;\r\n" + "	  background-color: #555;\r\n"
				+ "	  color: #fff;\r\n" + "	  text-align: center;\r\n" + "	  border-radius: 6px;\r\n"
				+ "	  padding: 5px 0;\r\n" + "	  position: absolute;\r\n" + "	  z-index: 1;\r\n"
				+ "	  bottom: 125%;\r\n" + "	  left: 50%;\r\n" + "	  margin-left: -60px;\r\n"
				+ "	  opacity: 0;\r\n" + "	  transition: opacity 0.3s;\r\n" + "	}\r\n" + "\r\n"
				+ "	.tooltip .tooltiptext::after {\r\n" + "	  content: \"\";\r\n" + "	  position: absolute;\r\n"
				+ "	  top: 100%;\r\n" + "	  left: 50%;\r\n" + "	  margin-left: -5px;\r\n"
				+ "	  border-width: 5px;\r\n" + "	  border-style: solid;\r\n"
				+ "	  border-color: #555 transparent transparent transparent;\r\n" + "	}\r\n" + "\r\n"
				+ "	.tooltip:hover .tooltiptext {\r\n" + "	  visibility: visible;\r\n" + "	  opacity: 1;\r\n"
				+ "	}\r\n" + "	</style>");

	}


	String convertJsonToHTML(JsonArray ja,boolean flagMedian,String set, boolean displayStructureImages) {
		String imgURLSid="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxsid/";

		String html=writeStyles();

		html+="<table border=1>\r\n";
		html+="<tr>\r\n";		
		html+="<th>Chemical</th>\r\n";

		if (set!=null) {
			html+="<th>Property value (from "+set+" set)</th>\r\n";			
		} else {
			html+="<th>Property value</th>\r\n";
		}

		html+="<th>Source</th>\r\n";
		html+="<th>Experimental Conditions</th>\r\n";		
		html+="</tr>\r\n";

		for (int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();
			html+="<tr>\r\n";		

			String imgURL=imgURLSid+jo.get("dtxsid").getAsString();

			if(displayStructureImages) {
				html+="\t\t<td width=150px valign=\"top\"><img src=\""+imgURL+"\" height=150 width=150 border=2>"
						+ jo.get("chemical_name").getAsString()+ "</td>\n";

			} else {
				html+="\t\t<td width=150px valign=\"top\">"+ jo.get("chemical_name").getAsString()+ "</td>\n";
			}


			boolean flag=false;
			if(flagMedian) {
				String median_exp_prop_property_values_id=jo.get("median_exp_prop_property_values_id").getAsString();
				String exp_prop_property_values_id=jo.get("exp_prop_property_values_id").getAsString();
				String []ids=median_exp_prop_property_values_id.split("\\|");
				List<String>idList=Arrays.asList(ids);

				if(idList.contains(exp_prop_property_values_id)) {
					System.out.println("Flag "+exp_prop_property_values_id);
					flag=true;
				}

			}


			String propertyValue=jo.get("property_value").getAsString();
			String property=jo.get("property").getAsString();

			if(flag && flagMedian) {				
				String value1=getFormattedValue(propertyValue,property)+" "+jo.get("property_units").getAsString();
				String value2="<span class=\"borderAD\">"+value1+"</span>"+
						"<style>.borderAD {border: 2px solid green; padding: 0px 4px 0px}</style>";

				html+="<td><b><font color=darkgreen><div class=\"tooltip\">"+value2+
						"<span class=\"tooltiptext\">Median value used in dataset</span></div><br>";
				html+="</font></b></td>\r\n";

			} else {
				html+="<td>"+getFormattedValue(propertyValue,property)+" "+jo.get("property_units").getAsString();
				html+="</td>\r\n";
			}

			String sourceHtml="";

			if( !jo.get("public_source_name").isJsonNull()) {				
				String source_name=jo.get("public_source_name").getAsString();
				String source_description=jo.get("public_source_description").getAsString();
				String source_url=jo.get("public_source_url").getAsString();

				sourceHtml+="<a href=\""+source_url+"\" target=\"_blank\"><div class=\"tooltip\">"+source_name+
						"<span class=\"tooltiptext\">"+source_description+"</span></div></a><br>";
			}

			if( !jo.get("public_source_original_name").isJsonNull()) {				
				String source_name=jo.get("public_source_original_name").getAsString();
				String source_description=jo.get("public_source_original_description").getAsString();
				String source_url=jo.get("public_source_original_url").getAsString();

				sourceHtml+="<a href=\""+source_url+"\" target=\"_blank\"><div class=\"tooltip\">"+source_name+
						"<span class=\"tooltiptext\">"+source_description+"</span></div></a><br>";
			}


			if( !jo.get("literature_source_name").isJsonNull()) {				
				String source_name=jo.get("literature_source_name").getAsString();
				String source_description=jo.get("literature_source_description").getAsString();

				String source_url=null;
				if( !jo.get("literature_source_url").isJsonNull()) {	
					source_url=jo.get("literature_source_url").getAsString();

					sourceHtml+="<a href=\""+source_url+"\" target=\"_blank\"><div class=\"tooltip\">"+source_name+
							"<span class=\"tooltiptext\">"+source_description+"</span></div></a><br>";


				} else {
					sourceHtml+="<div class=\"tooltip\">"+source_name+
							"<span class=\"tooltiptext\">"+source_description+"</span></div><br>";

				}


			}


			if( !jo.get("direct_link").isJsonNull()) {
				String source_url=jo.get("direct_link").getAsString();
				sourceHtml+="<a href=\""+source_url+"\" target=\"_blank\"><div class=\"tooltip\">Direct link"+
						"<span class=\"tooltiptext\">Webpage for the specific property value</span></div></a><br>";

			}

			if( !jo.get("short_citation").isJsonNull()) {
				sourceHtml+="<div class=\"tooltip\">"+jo.get("short_citation").getAsString()+
						"<span class=\"tooltiptext\">Citation name for the property value. A complete citation is not available</span></div><br>";

			}



			html+="<td>"+sourceHtml+"</td>\r\n";

			String parametersHTML="Not specified";

			if (!jo.get("temperature_c").isJsonNull() || !jo.get("pressure_mmhg").isJsonNull() || !jo.get("ph").isJsonNull()) {
				parametersHTML="";

				if (!jo.get("temperature_c").isJsonNull()) {
					parametersHTML+="Temperature: "+jo.get("temperature_c").getAsString()+" C<br>";
				}

				if (!jo.get("pressure_mmhg").isJsonNull()) {
					parametersHTML+="Pressure: "+jo.get("pressure_mmhg").getAsString()+" mmHg<br>";
				}

				if (!jo.get("ph").isJsonNull()) {
					parametersHTML+="pH: "+jo.get("ph").getAsString()+"<br>";
				}
			}

			html+="<td>"+parametersHTML+"</td>\r\n";		
			html+="</tr>\r\n";
		}

		html+="</table>\r\n";
		return html;
	}

	String convertJsonToHTML2(String property, JsonArray ja,boolean flagMedian,String set, boolean forDashboard) {

		String imgURLSid="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxsid/";

		StringBuffer sb=new StringBuffer();

		sb.append(writeStyles());


		if(ja.size()==0) {
			sb.append("<h2>No data for "+property+"</h2>\n");
			return sb.toString();
		}

		JsonObject jo0=ja.get(0).getAsJsonObject();
		String dtxsid=jo0.get("dtxsid").getAsString();

		if (forDashboard) {
			sb.append("<h2>Raw data for "+property+" for "+dtxsid+"</h2>\n");
		} else {
			sb.append("<h2>Raw data for "+property+"</h2>\n");	
		}


		sb.append("<table border=1 cellpadding=3 cellspacing=0>\r\n");
		sb.append("<tr bgcolor=lightgray>\r\n");		

		if(!forDashboard) sb.append("<th>Chemical</th>\r\n");

		if (set!=null) {
			sb.append("<th>Property value (Note: in "+set+" set)</th>\r\n");			
		} else {
			sb.append("<th>Property value</th>\r\n");
		}
		sb.append("<th>Source</th>\r\n");
		sb.append("<th style=\"width:30%\">Citation</th>\r\n");
		sb.append("<th>Experimental Conditions</th>\r\n");		
		sb.append("</tr>\r\n");

		for (int i=0;i<ja.size();i++) {

			JsonObject jo=ja.get(i).getAsJsonObject();

			//			System.out.println(gson.toJson(jo));

			sb.append("<tr>\r\n");		

			String imgURL=imgURLSid+jo.get("dtxsid").getAsString();

			if(!forDashboard) {
				sb.append("\t\t<td width=150px valign=\"top\"><img src=\""+imgURL+"\" height=150 width=150 border=2>"
						+ jo.get("chemical_name").getAsString()+ "</td>\n");

			} else {
				//				sb.append("\t\t<td width=150px valign=\"top\">"+ jo.get("chemical_name").getAsString()+ "</td>\n");
			}


			addPropertyValue(flagMedian, sb, jo);
			addPublicSource(sb, jo);
			addCitation(sb, jo);
			addParameters(sb, jo);
			sb.append("</tr>\r\n");
		}

		sb.append("</table>\r\n");
		return sb.toString();
	}

	private void addParameters(StringBuffer sb, JsonObject jo) {
		//TODO store the parameters in the jo better so can just use a loop

		String parametersHTML="Not specified";

		List<String>params=new ArrayList<>();

		if(!jo.get("temperature_c").isJsonNull()) 
			params.add("Temperature: "+getFormattedValue(jo.get("temperature_c").getAsString(),"temperature")+" C");

		if (!jo.get("pressure_mmhg").isJsonNull()) {
			params.add("Pressure: "+getFormattedValue(jo.get("pressure_mmhg").getAsString(),"pressure")+" mmHg");
		}

		if (!jo.get("ph").isJsonNull()) {
			params.add("pH: "+getFormattedValue(jo.get("ph").getAsString(),"pH"));
		}

		if (!jo.get("response_site").isJsonNull()) {
			params.add("Response site: "+jo.get("response_site").getAsString());
		}

		if (!jo.get("response_site").isJsonNull()) {
			params.add("Species latin: "+jo.get("species_latin").getAsString());
		}

		if (!jo.get("species_common").isJsonNull()) {
			params.add("Species common: "+jo.get("species_common").getAsString());
		}

		if(params.size()==0) {
			sb.append("<td>Not specified</td>\r\n");		
		} else {

			String strParams="";
			for (int i=0;i<params.size();i++) {
				strParams+=params.get(i);
				if(i<params.size()-1) {
					strParams+="<br>";
				}
			}
			sb.append("<td>"+strParams+"</td>\r\n");

		}


	}

	private void addPropertyValue(boolean flagMedian, StringBuffer sb, JsonObject jo) {
		boolean flag=false;

		if(flagMedian) {
			String median_exp_prop_property_values_id=jo.get("median_exp_prop_property_values_id").getAsString();
			String exp_prop_property_values_id=jo.get("exp_prop_property_values_id").getAsString();
			String []ids=median_exp_prop_property_values_id.split("\\|");
			List<String>idList=Arrays.asList(ids);

			if(idList.contains(exp_prop_property_values_id)) {
				System.out.println("Flag "+exp_prop_property_values_id);
				flag=true;
			}

		}

		String propertyValue=jo.get("property_value").getAsString();
		String property=jo.get("property").getAsString();
		
		String units=null;
		
		if (jo.get("property_units")!=null && !jo.get("property_units").isJsonNull())		
			units=jo.get("property_units").getAsString();

		if(jo.get("property_value_text")!=null && !jo.get("property_value_text").isJsonNull() && units.toLowerCase().equals("binary")) {
			String propertyValueText=jo.get("property_value_text").getAsString();
			sb.append("<td>"+propertyValue+" ("+propertyValueText+")"+"</td>");

		} else {
			if(flag && flagMedian) {				
				String value1=getFormattedValue(propertyValue,property)+" "+units;
				String value2="<span class=\"borderAD\">"+value1+"</span>"+
						"<style>.borderAD {border: 2px solid green; padding: 0px 4px 0px}</style>";

				sb.append("<td><b><font color=darkgreen><div class=\"tooltip\">"+value2+
						"<span class=\"tooltiptext\">Median value used in dataset</span></div><br>");
				sb.append("</font></b></td>\r\n");

			} else {
				if(jo.get("property_units")!=null && !jo.get("property_units").isJsonNull()) {
					sb.append("<td>"+getFormattedValue(propertyValue,property)+" "+units);	
				} else {
					sb.append("<td>"+getFormattedValue(propertyValue,property));
				}

				sb.append("</td>\r\n");
			}

		}


	}


	private void addCitation(StringBuffer sb, JsonObject jo) {

		String citationHtml="";

		if( jo.get("literature_source_name")!=null && !jo.get("literature_source_name").isJsonNull()) {				
			String source_name=jo.get("literature_source_name").getAsString();
			String source_description=jo.get("literature_source_description").getAsString();

			String source_url=null;

			if(jo.get("literature_source_doi")!=null && !jo.get("literature_source_doi").isJsonNull()) {	
				source_url=jo.get("literature_source_doi").getAsString();

				citationHtml+="<a href=\""+source_url+"\" target=\"_blank\">"+source_description+"</a><br>";
			} else {
				citationHtml+=source_description+"<br>";
			}

		} else if( jo.get("short_citation")!=null && !jo.get("short_citation").isJsonNull()) {
			citationHtml+="<div class=\"tooltip\">"+jo.get("short_citation").getAsString()+
					"<span class=\"tooltiptext\">Citation name for the property value. A complete citation is not available</span></div><br>";
		}

		sb.append("<td>"+citationHtml+"</td>\r\n");
	}

	private void addPublicSource(StringBuffer sb, JsonObject jo) {
		String sourceHtml="";

		if( !jo.get("public_source_name").isJsonNull()) {				
			String source_name=jo.get("public_source_name").getAsString();
			String source_description=jo.get("public_source_description").getAsString();

			if(jo.get("public_source_url").isJsonNull()) {
				sourceHtml+="<div class=\"tooltip\">"+source_name+
						"<span class=\"tooltiptext\">"+source_description+"</span></div><br>";

			} else {
				String source_url=jo.get("public_source_url").getAsString();
				sourceHtml+="<a href=\""+source_url+"\" target=\"_blank\"><div class=\"tooltip\">"+source_name+
						"<span class=\"tooltiptext\">"+source_description+"</span></div></a><br>";

			}


		}

		if( !jo.get("public_source_original_name").isJsonNull()) {				
			String source_name=jo.get("public_source_original_name").getAsString();
			String source_description=jo.get("public_source_original_description").getAsString();
			String source_url=jo.get("public_source_original_url").getAsString();

			sourceHtml+="<a href=\""+source_url+"\" target=\"_blank\"><div class=\"tooltip\">"+source_name+
					"<span class=\"tooltiptext\">"+source_description+"</span></div></a><br>";
		}



		if( jo.get("direct_link")!=null && !jo.get("direct_link").isJsonNull()) {
			String source_url=jo.get("direct_link").getAsString();
			sourceHtml+="<a href=\""+source_url+"\" target=\"_blank\"><div class=\"tooltip\">Direct link"+
					"<span class=\"tooltiptext\">Webpage for the specific property value</span></div></a><br>";
		}
		sb.append("<td>"+sourceHtml+"</td>\r\n");

	}

	String toHTMLFile(String folder,String filename,String html) {

		try {
			String filepath=folder+File.separator+filename;
			FileWriter fw=new FileWriter(filepath);
			fw.write(html);
			fw.flush();
			fw.close();

			return filepath;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	void viewInWebBrowser(String filepath) {
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.browse(new File(filepath).toURI());
			return;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	List<Double>getValues(JsonArray ja) {
		List<Double>values=new ArrayList<>();

		for(int i=0;i<ja.size();i++) {
			JsonObject jo=ja.get(i).getAsJsonObject();

			if(!jo.get("property_value").isJsonNull()) {
				Double value=Double.parseDouble(jo.get("property_value").getAsString());
				values.add(value);
			}
		}

		return values;
	}

	void createReportsForDashboardAllProperties(String dtxsid) {
		List<String>properties=getPropertyNamesForDashboard();
		//		for(String property:properties) System.out.println(property);

		boolean forDashboard=true;

		for(String property:properties) {

			if(property.toLowerCase().contains("receptor")) continue;//fo

			JsonArray ja=getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID(property,dtxsid );			
			List<Double>values=getValues(ja);			
			Double median=calculateMedian(values);
			Double avg=calculateAverage(values);

			String units=null;

			if(ja.size()>0) {
				units=ja.get(0).getAsJsonObject().get("property_units").getAsString();
			} else {
				continue;
			}

			if(median!=null) {
				System.out.println(property+"\t"+getFormattedValue(median+"", property)+"\t"+units);
			}

			//			if(avg!=null) {
			//				System.out.println(property+"\t"+getFormattedValue(avg+"", property)+"\t"+units);
			//			}


			String html2=convertJsonToHTML2(property, ja,false,null,forDashboard);
			String filepath2=toHTMLFile("data\\reports", "Dashboard "+dtxsid+" "+property.replace(":", "_")+".html", html2);
			viewInWebBrowser(filepath2);
		}

	}

	class Result {
		String median;
		String average;
		String unit;
		int count;
	}

	void createReportsForDashboardAllPropertiesTabbed(String dtxsid,boolean useView) {

		List<String>properties=getPropertyNamesForDashboard();
		//		for(String property:properties) System.out.println(property);

		boolean forDashboard=true;

		TreeMap<String,String>htResults=new TreeMap<String,String>();
		TreeMap<String,Result>htResultsSummary=new TreeMap<String,Result>();

		for(String property:properties) {

			//			if(property.toLowerCase().contains("receptor")) continue;//fo

			JsonArray ja=null;

			if (useView) {
				ja=getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID_FromView(property,dtxsid );
			} else {
				ja=getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID(property,dtxsid );	
			}

			List<Double>values=getValues(ja);			
			Double median=calculateMedian(values);
			Double avg=calculateAverage(values);

			String units=null;

			if(ja.size()>0) {
				
				JsonObject jo0=ja.get(0).getAsJsonObject();
				
				if(jo0.get("property_units")!=null) {
					if(!jo0.get("property_units").isJsonNull()) { 
						units=ja.get(0).getAsJsonObject().get("property_units").getAsString();
					}
				}
			} else {
				htResults.put(property, "No data");
				continue;
			}

			String strMedian=getFormattedValue(median+"", property);
			String strAvg=getFormattedValue(avg+"", property);
			
			System.out.println(property+"\t"+strMedian);

			Result result=new Result();
			result.average=strAvg;
			result.median=strMedian;
			result.unit=units;
			result.count=ja.size();

			htResultsSummary.put(property, result);
			String html2=convertJsonToHTML2(property, ja,false,null,forDashboard);
			htResults.put(property, html2);

		}

		StringBuffer sb=new StringBuffer();
		
		sb.append(getHeader());

		sb.append("<div class=\"tab\">\n");
		sb.append(" <button class=\"tablinks\" onclick=\"openProperty(event, 'Summary')\">Summary</button>\n");
		for(String property:properties) {
			if(htResults.get(property).equals("No data")) continue;
			String property2=property.replace("'", "_");//fix HLC
			sb.append(" <button class=\"tablinks\" onclick=\"openProperty(event, '"+property2+"')\">"+property+"</button>\n");	
		}
		sb.append("</div>\n");

		writeSummary(sb,htResultsSummary,dtxsid);

		for(String property:properties) {

			if(htResults.get(property).equals("No data")) continue;

			String property2=property.replace("'", "_");
			sb.append("<div id=\""+property2+"\" class=\"tabcontent\">\n");
			sb.append(htResults.get(property));
			sb.append("</div>\n");
		}
		
		sb.append(getFooter());

		String filepath2=toHTMLFile("data\\reports", "Dashboard "+dtxsid+".html", sb.toString());
		viewInWebBrowser(filepath2);

	}

	private String getFooter() {
		String footer="<script>\r\n"
				+ "function openProperty(evt, cityName) {\r\n"
				+ "  var i, tabcontent, tablinks;\r\n"
				+ "  tabcontent = document.getElementsByClassName(\"tabcontent\");\r\n"
				+ "  for (i = 0; i < tabcontent.length; i++) {\r\n"
				+ "    tabcontent[i].style.display = \"none\";\r\n"
				+ "  }\r\n"
				+ "  tablinks = document.getElementsByClassName(\"tablinks\");\r\n"
				+ "  for (i = 0; i < tablinks.length; i++) {\r\n"
				+ "    tablinks[i].className = tablinks[i].className.replace(\" active\", \"\");\r\n"
				+ "  }\r\n"
				+ "  document.getElementById(cityName).style.display = \"block\";\r\n"
				+ "  evt.currentTarget.className += \" active\";\r\n"
				+ "}\r\n"
				+ "openProperty(null,\"Summary\");\n"
				+ "</script>\r\n"
				+ "   \r\n"
				+ "</body>\r\n"
				+ "</html>\n";
		return footer;
	}

	private String getHeader() {
		String header="<!DOCTYPE html>\r\n"
				+ "<html>\r\n"
				+ "<head>\r\n"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n"
				+ "<style>\r\n"
				+ "body {font-family: Arial;}\r\n"
				+ "\r\n"
				+ "/* Style the tab */\r\n"
				+ ".tab {\r\n"
				+ "  overflow: hidden;\r\n"
				+ "  border: 1px solid #ccc;\r\n"
				+ "  background-color: #f1f1f1;\r\n"
				+ "}\r\n"
				+ "\r\n"
				+ "/* Style the buttons inside the tab */\r\n"
				+ ".tab button {\r\n"
				+ "  background-color: inherit;\r\n"
				+ "  float: left;\r\n"
				+ "  border: none;\r\n"
				+ "  outline: none;\r\n"
				+ "  cursor: pointer;\r\n"
				+ "  padding: 14px 16px;\r\n"
				+ "  transition: 0.3s;\r\n"
				+ "  font-size: 17px;\r\n"
				+ "}\r\n"
				+ "\r\n"
				+ "/* Change background color of buttons on hover */\r\n"
				+ ".tab button:hover {\r\n"
				+ "  background-color: #ddd;\r\n"
				+ "}\r\n"
				+ "\r\n"
				+ "/* Create an active/current tablink class */\r\n"
				+ ".tab button.active {\r\n"
				+ "  background-color: #ccc;\r\n"
				+ "}\r\n"
				+ "\r\n"
				+ "/* Style the tab content */\r\n"
				+ ".tabcontent {\r\n"
				+ "  display: none;\r\n"
				+ "  padding: 6px 12px;\r\n"
				+ "  border: 1px solid #ccc;\r\n"
				+ "  border-top: none;\r\n"
				+ "}\r\n"
				+ "</style>\r\n"
				+ "</head>\n\n";
		return header;
	}

	private void writeSummary(StringBuffer sb,TreeMap<String,Result>htResultsSummary,
			String dtxsid) {

		sb.append("<div id=\"Summary\" class=\"tabcontent\">\n");

		sb.append("<h2>Summary of raw experimental data for "+dtxsid+"</h2>\n");

		sb.append("<table border=1 cellpadding=3 cellspacing=0>\r\n");

		sb.append("<tr bgcolor=lightgray>\r\n");				
		sb.append("<th>Property</th>\r\n");
		sb.append("<th>Count</th>\r\n");
		sb.append("<th>Average value</th>\r\n");
		sb.append("<th>Median value</th>\r\n");
		sb.append("<th>Unit</th>\r\n");
		sb.append("</tr>\r\n");

		for (String property:htResultsSummary.keySet()) {

			Result result=htResultsSummary.get(property);
			sb.append("<tr>\r\n");

			sb.append("<td>"+property+"</td>\r\n");
			sb.append("<td>"+result.count+"</td>\r\n");
			sb.append("<td>"+result.average+"</td>\r\n");
			sb.append("<td>"+result.median+"</td>\r\n");
			
			if(result.unit==null) {
				sb.append("<td></td>\r\n");
			} else {
				sb.append("<td>"+result.unit+"</td>\r\n");	
			}
			
			sb.append("</tr>\r\n");
		}

		sb.append("</table>\r\n");
		sb.append("</div>\n");
	}

	void createReportForDashboard(String dtxsid, String property) {
		boolean forDashboard=true;
		JsonArray ja2=getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID(property,dtxsid );		
		String html2=convertJsonToHTML2(property, ja2,false,null,forDashboard);
		String filepath2=toHTMLFile("data\\reports", "Dashboard "+dtxsid+" "+property.replace(":", "_")+".html", html2);
		viewInWebBrowser(filepath2);
	}

	void createReportsForModulesAllProperties(String qsarSmiles) {

		List<String>properties=getPropertyNamesForModules();
		boolean forDashboard=false;

		for(String property:properties) {
			JsonArray ja=getRawRecordsCheminformaticsModulesByPropertyNameandQsarSmiles(property,qsarSmiles);
			String html=convertJsonToHTML2(property,ja,true,null,forDashboard);
			String filepath=toHTMLFile("data\\reports", "raw data for cheminformatics modules "+property.replace(":", "_")+".html", html);
			viewInWebBrowser(filepath);
		}
	}


	private static Double calculateMedian(List<Double> values) {
		//		Collections.sort(values);

		int n=values.size();
		if(n==0) return null;

		if(n%2==1){			
			int index=((n+1)/2)-1;
			//			System.out.println(n+"\todd:\t"+index);
			return values.get(index);			
		} else	{			
			//			System.out.println(n+"\teven:\t"+(n/2-1)+"\t"+(n/2));
			return (values.get(n/2-1)+values.get(n/2))/2.0;
		}

	}

	private static Double calculateAverage(List<Double> values) {
		//		Collections.sort(values);

		if(values.size()==0) return null;

		double avg=0;
		for (Double value:values) {
			avg+=value;
		}
		return avg/=(double)values.size();
	}


	public static void main(String[] args) {
		RawExpDataTableGenerator r=new RawExpDataTableGenerator();

		//****************************************************************************************
		//Create reports for all properties for dashboard:
		String dtxsid="DTXSID3039242";//benzene
		//		String dtxsid="DTXSID7020182";//BPA
		//		String dtxsid="DTXSID7021360";//toluene
		//		String dtxsid="DTXSID8031865";//PFOA
		//		String dtxsid="DTXSID3031864";//PFOS
		//				String dtxsid="DTXSID0020573";//beta-estradiol - endocrine active

		//		String dtxsid="DTXSID0037522";//10311-84-9

		//		r.createReportsForDashboardAllProperties(dtxsid);

//		boolean useView=false;
//		r.createReportsForDashboardAllPropertiesTabbed(dtxsid,useView);

		//****************************************************************************************
		//Create reports for all properties for modules:
		//		String qsarSmiles="CC=CC";
		//		String qsarSmiles="CC(C)(C1C=CC(O)=CC=1)C1C=CC(O)=CC=1";//BPA
		//		r.createReportsForModulesAllProperties(qsarSmiles);

		//****************************************************************************************
		//Display the data associated with the dataset for the selected model:
//				String qsarSmiles="CC=CCC";
//				long modelID=1066L;
//				r.createReportForModules(qsarSmiles, modelID);
		//****************************************************************************************
		//Display the data associated with the modeling dataset for the property:
				String qsarSmiles="CC=CCC";
				String property=DevQsarConstants.WATER_SOLUBILITY;
//		String property=DevQsarConstants.BCF;
				createReportForModules(r, qsarSmiles, property);



	}

	private static void createReportForModules(RawExpDataTableGenerator r, String qsarSmiles, String property) {
		JsonArray ja=r.getRawRecordsCheminformaticsModulesByPropertyNameandQsarSmiles(property,qsarSmiles);
		String html=r.convertJsonToHTML2(property, ja,true,null,false);
		String filepath=r.toHTMLFile("data\\reports", "raw data for cheminformatics modules.html", html);
		r.viewInWebBrowser(filepath);
	}

	private void createReportForModules(String qsarSmiles, long modelID) {
		String set=getDatasetSplit(modelID,qsarSmiles);
		JsonArray ja=getRawRecordsCheminformaticsModulesByModelIDandQsarSmiles(1066L,qsarSmiles);
		String html=convertJsonToHTML2(DevQsarConstants.WATER_SOLUBILITY,ja, true,set,false);
		String filepath=toHTMLFile("data\\reports", "raw data for cheminformatics modules.html", html);
		viewInWebBrowser(filepath);
	}

}
