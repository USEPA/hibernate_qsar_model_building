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
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;



/**
* @author TMARTI02
*/
public class RawExpDataTableGenerator {

	Connection conn=SqlUtilities.getConnectionPostgres();	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	
	public static final String sqlGetFields="select p.\"name\"    as property,\r\n"
			+ "d.\"name\"    as dataset,\r\n"
			+ "dr.preferred_name  as chemical_name,\r\n"
			+ "dpc.dtxsid,\r\n"
			+ "dpc.dtxcid,\r\n"
			+ "dp.canon_qsar_smiles,\r\n"
			+ "dpc.smiles,\r\n"
			
			+ "dp.qsar_exp_prop_property_values_id as median_exp_prop_property_values_id,\r\n"
			+ "pv.id exp_prop_property_values_id,\r\n"
			
			+ "dpc.property_value as property_value,\r\n"
			+ "u.abbreviation     as property_units,\r\n"
			+ "pv.value_text      as property_value_text, --to get meaning of binary property values\r\n"
			
			+ "pvT.value_point_estimate  as temperature_c,\r\n"
			+ "pvP.value_point_estimate  as pressure_mmHg,\r\n"
			+ "pvpH.value_point_estimate as pH,\r\n"
			
			+ "ps.\"name\"   as public_source_name,\r\n"
			+ "ps.description     as public_source_description,\r\n"
			+ "ps.url      as public_source_url,\r\n"
			
			+ "ls.\"name\"   as literature_source_name,\r\n"
			+ "ls.citation as literature_source_description,\r\n"
			+ "ls.url      as literature_source_url,\r\n"
			
			//Note: right now there arent any property value records in the database with a public_source_original but there will be- for example toxval (AKA public source) sometimes pulls from ecotox (AKA the public source original) which then also has a literature source
			+ "ps2.\"name\"       as public_source_original_name,\r\n"
		    +"ps2.description    as public_source_original_description,\r\n"
		    + "ps2.url          as public_source_original_url,\r\n"

		    + "pv.page_url      as direct_link,\r\n"
			+ "pv.file_name     as file_name_loaded,\r\n"
			+ "pv.document_name  as short_citation\r\n";
	
	public static final String sqlParameters = "left join exp_prop.parameter_values pvT on pvT.fk_property_value_id = pv.id and pvT.fk_parameter_id = 2\r\n"
			+ "left join exp_prop.parameter_values pvP on pvP.fk_property_value_id = pv.id and pvP.fk_parameter_id = 1\r\n"
			+ "left join exp_prop.parameter_values pvpH on pvpH.fk_property_value_id = pv.id and pvpH.fk_parameter_id = 3\r\n";
	
	
	public static final String sqlSources="left join exp_prop.literature_sources ls on pv.fk_literature_source_id = ls.id\r\n"
			+ "left join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id\r\n"
	        +"left join exp_prop.public_sources ps2 on pv.fk_public_source_original_id = ps.id\r\n";

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
	+"and p.name=? and dpc.dtxsid=?\r\norder by dpc.property_value;";


	
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
			prep.setString(1,  propertyName);
			prep.setString(2, dtxsid);
			
			ResultSet rs = prep.executeQuery();
			
			JsonArray ja = getJsonArray(rs);
			
			System.out.println(Utilities.gson.toJson(ja));
			return ja;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
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
				return dfInt.format(dvalue);
			
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
	
	
	String convertJsonToHTML(JsonArray ja,boolean flagMedian,String set) {
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
			html+="\t\t<td width=150px valign=\"top\"><img src=\""+imgURL+"\" height=150 width=150 border=2>"
					+ jo.get("chemical_name").getAsString()+ "</td>\n";
			
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
				String source_url=jo.get("literature_source_url").getAsString();

				sourceHtml+="<a href=\""+source_url+"\" target=\"_blank\"><div class=\"tooltip\">"+source_name+
				  "<span class=\"tooltiptext\">"+source_description+"</span></div></a><br>";
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


	

	public static void main(String[] args) {
		RawExpDataTableGenerator r=new RawExpDataTableGenerator();
//		System.out.println(sqlCheminformaticsModulesByModelIDandQsarSmiles);

		String set=r.getDatasetSplit(1066L,"CC=CCC");
		JsonArray ja=r.getRawRecordsCheminformaticsModulesByModelIDandQsarSmiles(1066L,"CC=CCC" );
		String html=r.convertJsonToHTML(ja,true,set);
		String filepath=r.toHTMLFile("data\\reports", "raw data for cheminformatics modules.html", html);
		r.viewInWebBrowser(filepath);

//		String property=DevQsarConstants.LOG_KOW;
//		JsonArray ja=r.getRawRecordsCheminformaticsModulesByPropertyNameandQsarSmiles(property,"CC=CCC" );
//		JsonArray ja=r.getRawRecordsCheminformaticsModulesByPropertyNameandQsarSmiles("LogKow: Octanol-Water","C1C=CC=CC=1" );
//		String html=r.convertJsonToHTML(ja,true,null);
//		String filepath=r.toHTMLFile("data\\reports", "raw data for cheminformatics modules.html", html);
//		r.viewInWebBrowser(filepath);

		String property=DevQsarConstants.LOG_KOW;
		JsonArray ja2=r.getRawRecordsChemicalsDashboardByPropertyNameAndDTXSID(property,"DTXSID3039242" );		
		String html2=r.convertJsonToHTML(ja2,false,null);
		String filepath2=r.toHTMLFile("data\\reports", "raw data for chemicals dashboard.html", html2);
		r.viewInWebBrowser(filepath2);
		
		
		
	}

}
