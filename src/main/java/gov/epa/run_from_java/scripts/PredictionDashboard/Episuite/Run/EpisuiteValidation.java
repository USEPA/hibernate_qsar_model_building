package gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.json.CDL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ToxPredictor.Application.WebTEST4;
import ToxPredictor.MyDescriptors.DescriptorData;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.service.GenericSubstanceServiceImpl;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.run_from_java.scripts.ApplicabilityDomainScript;
import gov.epa.run_from_java.scripts.PredictScript;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.ApplicabilityDomainScript.ApplicabilityDomainPrediction;
import gov.epa.run_from_java.scripts.DatasetCreatorScript;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.EstimatedValue;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.EstimatedValue2;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Factor;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.ModelResult;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteValidation.DatasetUtilities.DescriptorsByCAS;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteValidation.DatasetUtilities.EpisuiteSmilesToQsarSmiles;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteValidation.DatasetUtilities.SmilesToQsarSmiles;
import gov.epa.util.ExcelSourceReader;
import gov.epa.util.MatlabChart;
import gov.epa.util.StructureImageUtil;
import gov.epa.util.StructureUtil;
import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfo;
import gov.epa.web_services.standardizers.SciDataExpertsStandardizer;
import kong.unirest.HttpResponse;


/**
 * @author TMARTI02
 */
public class EpisuiteValidation {

	private static boolean displayPlots=true;


	boolean debug=false;


	Episuite episuite=new Episuite();
	Opera opera=new Opera();
	Percepta percepta=new Percepta();
	CheminformaticsModules cm=new CheminformaticsModules();	
	Plot plot=new Plot();

	EpisuiteWebserviceScript ewss = new EpisuiteWebserviceScript();
	PredictScript ps = new PredictScript();

	public DatasetUtilities du=new DatasetUtilities();

//	String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Euclidean;
	String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;

//	String workflow = "qsar-ready";
	
	static String workflowDefault="qsar-ready";
	static String workflowCharlie="qsar-ready_04242025";
	static String workflowCharlieRevised="qsar-ready_04242025_0";
	
	
	String serverHost = "https://hcd.rtpnc.epa.gov";
//	String serverHost="https://hazard-dev.sciencedataexperts.com";
//	String workflow = "qsar-ready_04242025";
	String workflow = "qsar-ready";
	boolean useFullStandardize=false;
	
	static String propertyName96HR_Fish_LC50="96HR_Fish_LC50";
	

//	SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(serverHost);

	Utils utils=new Utils();
	
	
	public class Utils {

		void writeToJson(Object obj,String filepath) {
			
			try {
				FileWriter fw = new FileWriter(filepath);
				fw.write(Utilities.gson.toJson(obj));
				fw.flush();
				fw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private String writePredComparisonTable(String source1, String source2, DecimalFormat df,
				ModelPrediction mp1,ModelPrediction mp2) {

			StringWriter sw=new StringWriter();

			sw.write("<table border=1 cellspacing=0 cellpadding=2>");

			if(mp1.qsarClass!=null) {
				sw.write("<tr bgcolor=lightgray><th>Model</th><th>Exp</th><th>Pred</th><th>Error</th><th>InsideAD</th><th>QSAR Class</th></tr>");
			} else {
				sw.write("<tr bgcolor=lightgray><th>Model</th><th>Exp</th><th>Pred</th><th>Error</th><th>InsideAD</th></tr>");
			}


			sw.write("<tr>"
					+ "<td>"+source1+"</td>"
					+ "<td>"+df.format(mp1.exp)+"</td>"
					+ "<td>"+df.format(mp1.pred)+"</td>"
					+ "<td>"+df.format(mp1.absError())+"</td>"
					+"<td>"+mp1.insideAD+"</td>");

			if(mp1.qsarClass!=null) {
				sw.write("<td>"+mp1.qsarClass+"</td>");	
			}

			sw.write("</tr>");


			if(mp2!=null) {

				sw.write("<tr>"
						+ "<td>"+source2+"</td>"
						+ "<td>"+df.format(mp2.exp)+"</td>"
						+ "<td>"+df.format(mp2.pred)+"</td>");

				if(mp2.absError()<mp1.absError()) {
					sw.write("<td bgcolor=lightgreen>"+df.format(mp2.absError())+"</td>");
				} else {
					sw.write("<td>"+df.format(mp2.absError())+"</td>");	
				}

				sw.write("<td>"+mp2.insideAD+"</td>");

				if(mp1.qsarClass!=null) {
					sw.write("<td>N/A</td>");
				}

				sw.write("</tr>");
			}

			sw.write("</table>");

			return sw.toString();
		}
		
	}
	public class CheckStructure {

		String dp_canon_qsar_smiles;
		String dpc_dtxsid;
		String dpc_dtxcid;
		String dpc_smiles;
		public Double mwDPC_smiles;

		String dsstox_casrn;
		String dsstox_smiles;
		String dsstox_dtxcid;
		String dsstox_canon_qsar_smiles;
		public String qc_level;
		public Double mwDsstox_smiles;

		public String episuite_smiles;
		public String episuite_canon_qsar_smiles;

		String scifinder_casrn;
		String scifinder_smiles;
		public Double mwScifinder;

	}
	
	
	class DatasetUtilities {

		
//		String serverHost="https://hazard-dev.sciencedataexperts.com";
		//		String serverHost = "https://hazard-dev.sciencedataexperts.com";
//		SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer();
		

		Hashtable<String,CheckStructure> getCheckStructureHashtableByQsarSmiles(String filepath) {
			Type type = new TypeToken<Hashtable<String,CheckStructure>>() {}.getType();
			try {
				Hashtable<String,CheckStructure>htCS_scifinder_by_qsar_smiles=Utilities.gson.fromJson(new FileReader(filepath), type);
				return htCS_scifinder_by_qsar_smiles;
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}

		String standardize(String smiles) {

//			System.out.println(serverHost);
			
			HttpResponse<String> standardizeResponse = SciDataExpertsStandardizer.callQsarReadyStandardizePost(smiles, useFullStandardize, workflow,serverHost);
//			System.out.println(standardizeResponse.getBody().toString());
//			System.out.println("status=" + standardizeResponse.getStatus());
			
			if (standardizeResponse.getStatus() == 200) {
				String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
				String qsarSmiles = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
						useFullStandardize);
				return qsarSmiles;
			}

			return null;
		}

		
		String standardize(String smiles,String workflow) {

//			System.out.println(serverHost);
			
			HttpResponse<String> standardizeResponse = SciDataExpertsStandardizer.callQsarReadyStandardizePost(smiles, useFullStandardize, workflow,serverHost);
//			System.out.println(standardizeResponse.getBody().toString());
//			System.out.println("status=" + standardizeResponse.getStatus());
			
			if (standardizeResponse.getStatus() == 200) {
				String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse, useFullStandardize);
				String qsarSmiles = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
						useFullStandardize);
				return qsarSmiles;
			}

			return null;
		}

		


		class CheckStructureComparator implements Comparator<CheckStructure> {
			@Override
			public int compare(CheckStructure p1, CheckStructure p2) {

				if(p1.mwScifinder==null && p2.mwScifinder==null) {
					return p1.dpc_dtxsid.compareTo(p2.dpc_dtxsid);
				}
				if(p1.mwScifinder==null) return -1;
				if(p2.mwScifinder==null) return 1;

				double diff1=Math.abs(p1.mwScifinder-p1.mwDsstox_smiles);
				double diff2=Math.abs(p2.mwScifinder-p2.mwDsstox_smiles);
				double mwCompare = diff1-diff2;
				if (mwCompare != 0) {
					return -Double.compare(diff1, diff2);
				} else {
					return p1.dpc_dtxsid.compareTo(p2.dpc_dtxsid);
				}
			}
		}

		private void writeCasDiscrepancies(String propertyName,List<CheckStructure> badCS) {

			try {

				String of = "data\\episuite\\episuite validation\\" + propertyName;
				FileWriter fw = new FileWriter(of+"/discrepancies between dsstox and scifinder.html");

				fw.write("<html><table border=1 cellspacing=0 cellpadding=10><caption>CAS discrepances for test set for  "
						+ propertyName + " model </caption>");

				fw.write("<tr bgcolor=lightgray>" + "<th>Structure Dsstox</th><th>Structure Scifinder CAS</th></tr>");

				int counter = 0;

				DecimalFormat df = new DecimalFormat("0.00");


				for (CheckStructure cs : badCS) {
					counter++;


					// System.out.println(key+"\n"+Utilities.gson.toJson(tmPreds.get(key)));

					fw.write("<tr>");

					String imgSrc=null;

					try {
						imgSrc = StructureImageUtil.generateImgSrc(cs.dsstox_smiles);
					} catch (Exception e) {
						e.printStackTrace();
					} 

					fw.write("<td valign=\"bottom\"><img src=\"" + imgSrc + "\" width=300><br>" +  
							episuite.addCarriageReturns(cs.dsstox_smiles,50) + "<br>" + 
							cs.dpc_dtxsid + "<br>"+
							"MW="+df.format(cs.mwDsstox_smiles) + "<br>" +
							"QC level="+cs.qc_level + "<br>" +
							"</td>\n");

					try {
						if(cs.scifinder_smiles!=null)
							imgSrc = StructureImageUtil.generateImgSrc(cs.scifinder_smiles);
						else imgSrc="";
					} catch (Exception e) {
						e.printStackTrace();
					} 

					String strMW="N/A";
					String strDiffMW="N/A";

					if(cs.mwScifinder!=null) {
						strMW=df.format(cs.mwScifinder);
						strDiffMW=df.format(Math.abs(cs.mwScifinder-cs.mwDsstox_smiles));
					}

					fw.write("<td valign=\"bottom\">");

					if(cs.scifinder_smiles!=null) {
						fw.write("<img src=\"" + imgSrc + "\" width=300><br>");
						fw.write(episuite.addCarriageReturns(cs.scifinder_smiles,50) + "<br>");
					} else {
						fw.write("No structure<br>N/A<br>");  
					}

					fw.write("dsstox casrn="+cs.dsstox_casrn + "<br>"
							+ "scifinder casrn="+cs.scifinder_casrn + "<br>"
							+ "MW=" + strMW + "<br>" + 
					"Diff MW=" + strDiffMW + "<br></td>\n");

					fw.write("</tr>");

					// if(counter==10) break;
					//				if (Math.abs(key) < 3)
					//					break;

				}

				fw.write("</table></html>");

				fw.flush();
				fw.close();


			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

		public String convertToInClause(HashSet<String>items) {
			StringBuilder sb = new StringBuilder();

			int count = 0;
			for (String item : items) {
				sb.append("'").append(item).append("'");
				if (count < items.size() - 1) {
					sb.append(", ");
				}
				count++;
			}

			return sb.toString();
		}


		/**
		 * Simple sql based search that works
		 * 
		 * @param dtxsids
		 * @return
		 */
		public List<DsstoxRecord> getDsstoxRecords(HashSet<String>dtxsids) {

			String sql="SELECT gs.dsstox_substance_id,gs.casrn,c.dsstox_compound_id,c.smiles,ql.name,gs.updated_at FROM generic_substances gs\r\n"
					+ "	         join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id\r\n"
					+"			join qc_levels ql on gs.fk_qc_level_id = ql.id\r\n"
					+ "	join compounds c on gsc.fk_compound_id = c.id\r\n"
					+ "	where gs.dsstox_substance_id in ("+convertToInClause(dtxsids)+");";

//			System.out.println(sql);

			List<DsstoxRecord>recs=new ArrayList<>();

			try {
				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);
				while (rs.next()) {
					DsstoxRecord dr=new DsstoxRecord();					
					dr.dsstoxSubstanceId=rs.getString(1);
					dr.casrn=rs.getString(2);
					dr.dsstoxCompoundId=rs.getString(3);
					dr.smiles=rs.getString(4);
					dr.qcLevel=rs.getString(5);
					dr.updatedAt=rs.getString(6);
					recs.add(dr);

					//					if(dr.getDsstoxSubstanceId().equals("DTXSID30199335")) {
					//						System.out.println("Found1:"+Utilities.gson.toJson(dr));
					//					}
				}


			}catch (Exception ex) {
				ex.printStackTrace();
			}


			//			if(dtxsids.contains("DTXSID30199335")) {
			//				System.out.println(Utilities.gson.toJson(recs));
			//			}
			//			System.out.println(recs.size());

			return recs;

		}


		void getScifinderStructures() {

//			String propertyName="LogKow";
			String propertyName="WS";
			String of = "data\\episuite\\episuite validation\\" + propertyName+"\\scifinder";

			File folder=new File(of);

			ExcelSourceReader e=new ExcelSourceReader();

			JsonArray jaAll=new JsonArray();

			for (File file:folder.listFiles()) {

				if(!file.getName().contains(".xlsx"))continue;

				System.out.println(file.getName());

				JsonArray ja=e.parseRecordsFromExcel(file.getAbsolutePath(), 0, 4, true);

				jaAll.addAll(ja);

				//				e.convertJsonArrayToExcel(ja, of+File.separator+"converted"+File.separator+file.getName());
				//				System.out.println(Utilities.gson.toJson(ja)+"\n");
			}
			
			new File(of+File.separator+"converted").mkdirs();

			e.convertJsonArrayToExcel(jaAll, of+File.separator+"converted"+File.separator+"scifinder records.xlsx");


		}

		void createStructureHashtableFile(String propertyName, long fk_dataset_id, String of) {

			HashSet<String>dtxsids=new HashSet<>();

			Hashtable<String,CheckStructure>htCS_byDTXSID=new Hashtable<>();

			//			System.out.println(Utilities.gson.toJson(ja));

			try {

				String sql="select distinct  dp.canon_qsar_smiles,dpc.dtxsid,dpc.dtxcid,smiles from qsar_datasets.data_points dp\r\n"
						+ "join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id\r\n"
						+ "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
						+"join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
						+"where dpis.fk_splitting_id=1 and d.id="+fk_dataset_id+" and dpis.split_num=1;";

				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
				GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
				int counter=0;
				List<DsstoxRecord>dsstoxRecordsAll=new ArrayList<>();

				while (rs.next()) {

					counter++;

					if(counter%1000==0) System.out.println(counter);

					CheckStructure cs=new CheckStructure();

					cs.dp_canon_qsar_smiles=rs.getString(1);
					cs.dpc_dtxsid=rs.getString(2);
					cs.dpc_dtxcid=rs.getString(3);
					cs.dpc_smiles=rs.getString(4);


					dtxsids.add(cs.dpc_dtxsid);

					htCS_byDTXSID.put(cs.dpc_dtxsid, cs);

					if(dtxsids.size()==1000) {
						//GenericSubstanceServiceImpl only seems to work for small batches (<100)
						//						List<DsstoxRecord>dsstoxRecords=gss.findAsDsstoxRecordsByDtxsidIn(dtxsids);

						List<DsstoxRecord>dsstoxRecords=getDsstoxRecords(dtxsids);
						dsstoxRecordsAll.addAll(dsstoxRecords);
						dtxsids.clear();
					}
				}//end overall loop

				//do what's left
				List<DsstoxRecord>dsstoxRecords=getDsstoxRecords(dtxsids);
				dsstoxRecordsAll.addAll(dsstoxRecords);

				//				if(true)return;

				for(DsstoxRecord dsstoxRecord:dsstoxRecordsAll) {
					CheckStructure cs2=htCS_byDTXSID.get(dsstoxRecord.getDsstoxSubstanceId());
					cs2.dsstox_casrn=dsstoxRecord.getCasrn();
					cs2.dsstox_dtxcid=dsstoxRecord.getDsstoxCompoundId();
					cs2.dsstox_smiles=dsstoxRecord.getSmiles();
					cs2.qc_level=dsstoxRecord.qcLevel;
				}


				FileWriter fw=new FileWriter(of+File.separator+"check structures.json");
				fw.write(Utilities.gson.toJson(htCS_byDTXSID));
				fw.flush();
				fw.close();

				writeCheckStructures(htCS_byDTXSID, of);



			} catch (Exception ex) {
				ex.printStackTrace();

			}

		}

		Hashtable<String, DataPoint> createRevisedDataPointHashtableByQsarSmiles (Hashtable<String,DataPoint> htDP_by_dp_qsar_smiles,Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles, LinkedHashMap<String, SmilesToQsarSmiles>htSmilesToQsarSmiles) {
			
			Hashtable<String, DataPoint> htDP_by_dsstox_qsar_smiles=new Hashtable<>();
			
			int countChanged=0;
			
			for(String dp_qsar_smiles:htDP_by_dp_qsar_smiles.keySet()) {
				DataPoint dp=htDP_by_dp_qsar_smiles.get(dp_qsar_smiles);
				
				
				if(!htCS_by_DP_QsarSmiles.containsKey(dp_qsar_smiles)) {
					if(debug)
						System.out.println("Dont have "+dp_qsar_smiles+" in htCS_by_DP_QsarSmiles");
					continue;
				}
				
				CheckStructure cs=htCS_by_DP_QsarSmiles.get(dp_qsar_smiles);
				
				cs.dsstox_canon_qsar_smiles=htSmilesToQsarSmiles.get(cs.dsstox_smiles).qsarSmiles;
				
				
//				if(dsstox_qsar_smiles.equals("CCCCCCCCOC(=O)C1=CC=C(C=C1C(=O)OCCCCCCCC)C(=O)OCCCCCCCC")) {
//					System.out.println(Utilities.gson.toJson(cs));;
//					System.out.println(dp.getQsar_dtxcid());;
//				}
				
				
//				if(cs.dpc_dtxsid.equals("DTXSID701004543")) {
//					System.out.println(Utilities.gson.toJson(cs));
//				}

				
				if(!dp_qsar_smiles.equals(cs.dsstox_canon_qsar_smiles)) {
//					System.out.println(cs.dpc_dtxsid+"\t"+ dp_qsar_smiles+"\t"+dsstox_qsar_smiles);
					countChanged++;
				}

				dp.checkStructure=cs;
				dp.setCanonQsarSmiles(cs.dsstox_canon_qsar_smiles);
				
				if(cs.dsstox_canon_qsar_smiles!=null) 				
					htDP_by_dsstox_qsar_smiles.put(cs.dsstox_canon_qsar_smiles, dp);
				
//				if(dp.checkStructure==null) {
//					System.out.println("Here dp.structure==null for "+dp.getCanonQsarSmiles());
//				}
				
				
			}

			if(debug)
				System.out.println("countChanged="+countChanged);

			return htDP_by_dsstox_qsar_smiles;
		}
		
		

		Hashtable<String, DataPoint> createRevisedDataPointHashtableByOriginalSmiles (Hashtable<String,DataPoint> htDP_by_dp_qsar_smiles,Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles) {
			
			Hashtable<String, DataPoint> htDP_by_smiles=new Hashtable<>();
			
			int countChanged=0;
			
			for(String dp_qsar_smiles:htDP_by_dp_qsar_smiles.keySet()) {
				DataPoint dp=htDP_by_dp_qsar_smiles.get(dp_qsar_smiles);
				
				
				if(!htCS_by_DP_QsarSmiles.containsKey(dp_qsar_smiles)) {
					System.out.println("Dont have "+dp_qsar_smiles+" in htCS_by_DP_QsarSmiles");
					continue;
				}
				
				CheckStructure cs=htCS_by_DP_QsarSmiles.get(dp_qsar_smiles);
				
				if(!dp_qsar_smiles.equals(cs.dsstox_canon_qsar_smiles)) {
//					System.out.println(cs.dpc_dtxsid+"\t"+ dp_qsar_smiles+"\t"+dsstox_qsar_smiles);
					countChanged++;
				}

				dp.checkStructure=cs;
				
				String smiles=cs.dsstox_smiles;
				

				if(smiles!=null) 	 {
					dp.setCanonQsarSmiles(smiles);	//TODO needed?				
					htDP_by_smiles.put(smiles, dp);
				}
					
//				if(dp.checkStructure==null) {
//					System.out.println("Here dp.structure==null for "+dp.getCanonQsarSmiles());
//				}
				
			}

			System.out.println("countChanged="+countChanged);

			return htDP_by_smiles;
		}
		
		Hashtable<String,CheckStructure> createHashtableCheckStructureByDPQsarSmiles(long fk_dataset_id) {

			HashSet<String>dtxsids=new HashSet<>();

			Hashtable<String,CheckStructure>htCS_byDTXSID=new Hashtable<>();

			//			System.out.println(Utilities.gson.toJson(ja));

			try {

				String sql="select distinct  dp.canon_qsar_smiles,dpc.dtxsid,dpc.dtxcid,smiles from qsar_datasets.data_points dp\r\n"
						+ "join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id\r\n"
						+ "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
						+"join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
						+"where dpis.fk_splitting_id=1 and d.id="+fk_dataset_id+" and dpis.split_num=1;";

				
//				System.out.println(sql);
				
				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
				GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
				int counter=0;
				List<DsstoxRecord>dsstoxRecordsAll=new ArrayList<>();

				while (rs.next()) {

					counter++;

//					if(counter%1000==0) System.out.println(counter);

					CheckStructure cs=new CheckStructure();

					cs.dp_canon_qsar_smiles=rs.getString(1);
					cs.dpc_dtxsid=rs.getString(2);
					cs.dpc_dtxcid=rs.getString(3);
					cs.dpc_smiles=rs.getString(4);
					
					
//					System.out.println(rs.getString(1)+"\t"+rs.getString(2)+"\t"+rs.getString(3)+"\t"+rs.getString(4));
					
					
//					if(cs.dpc_dtxsid.equals("DTXSID701004543")) {
//						System.out.println("For DTXSID701004543, original qsar_smiles="+cs.dp_canon_qsar_smiles);
//					}

//					if(cs.dp_canon_qsar_smiles.equals("C1C2CC3CC1(CC(C2)C3)C1C=CC=CC=1")) {
//						System.out.println("Found it 1:"+Utilities.gson.toJson(cs));
//					}

					dtxsids.add(cs.dpc_dtxsid);

					htCS_byDTXSID.put(cs.dpc_dtxsid, cs);

					if(dtxsids.size()==1000) {
						//GenericSubstanceServiceImpl only seems to work for small batches (<100)
						//						List<DsstoxRecord>dsstoxRecords=gss.findAsDsstoxRecordsByDtxsidIn(dtxsids);

						List<DsstoxRecord>dsstoxRecords=getDsstoxRecords(dtxsids);
						dsstoxRecordsAll.addAll(dsstoxRecords);
						dtxsids.clear();
					}
				}//end overall loop

				//do what's left
				List<DsstoxRecord>dsstoxRecords=getDsstoxRecords(dtxsids);
				dsstoxRecordsAll.addAll(dsstoxRecords);
				
				
//				System.out.println(dsstoxRecords.size());

				//				if(true)return;

				Hashtable<String,CheckStructure>htCS_byQsarSmiles=new Hashtable<>();

				for(DsstoxRecord dsstoxRecord:dsstoxRecordsAll) {
					CheckStructure cs2=htCS_byDTXSID.get(dsstoxRecord.getDsstoxSubstanceId());
					cs2.dsstox_casrn=dsstoxRecord.getCasrn();
					cs2.dsstox_dtxcid=dsstoxRecord.getDsstoxCompoundId();
					cs2.dsstox_smiles=dsstoxRecord.getSmiles();
					cs2.qc_level=dsstoxRecord.qcLevel;
					
//					if(htCS_byQsarSmiles.containsKey(cs2.dp_canon_qsar_smiles)) {
//						CheckStructure cs=htCS_byQsarSmiles.get(cs2.dp_canon_qsar_smiles);
//						System.out.println("\n"+cs2.dp_canon_qsar_smiles+"\n"+Utilities.gson.toJson(cs)+"\t"+Utilities.gson.toJson(cs2));
//					}
					
//					if(cs2.dsstox_smiles.equals("CCCCCCCCCC(O)CCCCCCCC(O)=O")) {
//						System.out.println("\n"+cs2.dp_canon_qsar_smiles+"\n"+Utilities.gson.toJson(cs2));
//					}
					
					//it only stores one of them if there are duplicates by dpc with same qsar ready smiles but different CAS
					htCS_byQsarSmiles.put(cs2.dp_canon_qsar_smiles,cs2);
					
					
//					System.out.println(cs2.dp_canon_qsar_smiles);
					
//					if(cs2.dp_canon_qsar_smiles.equals("C1C2CC3CC1(CC(C2)C3)C1C=CC=CC=1")) {
//						System.out.println(Utilities.gson.toJson(cs2));
//					}
				}
				
//				System.out.println(Utilities.gson.toJson(htCS_byQsarSmiles));
//				System.out.println(htCS_byQsarSmiles.size());
				

//				FileWriter fw=new FileWriter(of+File.separator+"check structures by dp_canon_qsar_smiles.json");
//				fw.write(Utilities.gson.toJson(htCS_byQsarSmiles));
//				fw.flush();
//				fw.close();


				return htCS_byQsarSmiles;


			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}
		
		
		Hashtable<String,CheckStructure> createHashtableCheckStructureByDPQsarSmilesExternal(long fk_dataset_id) {

			HashSet<String>dtxsids=new HashSet<>();

			Hashtable<String,CheckStructure>htCS_byDTXSID=new Hashtable<>();

			//			System.out.println(Utilities.gson.toJson(ja));

			try {

				String sql="select distinct  dp.canon_qsar_smiles,dpc.dtxsid,dpc.dtxcid,smiles from qsar_datasets.data_points dp\r\n"
						+ "join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id\r\n"
						+ "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
						+"where d.id="+fk_dataset_id+";";
				
//				System.out.println(sql);
				
				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
				GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
				int counter=0;
				List<DsstoxRecord>dsstoxRecordsAll=new ArrayList<>();

				while (rs.next()) {

					counter++;

//					if(counter%1000==0) System.out.println(counter);

					CheckStructure cs=new CheckStructure();

					cs.dp_canon_qsar_smiles=rs.getString(1);
					cs.dpc_dtxsid=rs.getString(2);
					cs.dpc_dtxcid=rs.getString(3);
					cs.dpc_smiles=rs.getString(4);


//					if(cs.dp_canon_qsar_smiles.equals("C1C2CC3CC1(CC(C2)C3)C1C=CC=CC=1")) {
//						System.out.println("Found it 1:"+Utilities.gson.toJson(cs));
//					}

					dtxsids.add(cs.dpc_dtxsid);

					htCS_byDTXSID.put(cs.dpc_dtxsid, cs);

					if(dtxsids.size()==1000) {
						//GenericSubstanceServiceImpl only seems to work for small batches (<100)
						//						List<DsstoxRecord>dsstoxRecords=gss.findAsDsstoxRecordsByDtxsidIn(dtxsids);

						List<DsstoxRecord>dsstoxRecords=getDsstoxRecords(dtxsids);
						dsstoxRecordsAll.addAll(dsstoxRecords);
						dtxsids.clear();
					}
				}//end overall loop

				//do what's left
				List<DsstoxRecord>dsstoxRecords=getDsstoxRecords(dtxsids);
				dsstoxRecordsAll.addAll(dsstoxRecords);

				//				if(true)return;

				Hashtable<String,CheckStructure>htCS_byQsarSmiles=new Hashtable<>();

				for(DsstoxRecord dsstoxRecord:dsstoxRecordsAll) {
					CheckStructure cs2=htCS_byDTXSID.get(dsstoxRecord.getDsstoxSubstanceId());
					cs2.dsstox_casrn=dsstoxRecord.getCasrn();
					cs2.dsstox_dtxcid=dsstoxRecord.getDsstoxCompoundId();
					cs2.dsstox_smiles=dsstoxRecord.getSmiles();
					cs2.qc_level=dsstoxRecord.qcLevel;
					htCS_byQsarSmiles.put(cs2.dp_canon_qsar_smiles,cs2);
					
//					if(cs2.dp_canon_qsar_smiles.equals("C1C2CC3CC1(CC(C2)C3)C1C=CC=CC=1")) {
//						System.out.println(Utilities.gson.toJson(cs2));
//					}
				}

//				FileWriter fw=new FileWriter(of+File.separator+"check structures by dp_canon_qsar_smiles.json");
//				fw.write(Utilities.gson.toJson(htCS_byQsarSmiles));
//				fw.flush();
//				fw.close();


				return htCS_byQsarSmiles;


			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}
		
		Hashtable<String,CheckStructure> createHashtableCheckStructureByDPQsarSmilesNoSplit(String propertyName, long fk_dataset_id,String of) {

			HashSet<String>dtxsids=new HashSet<>();

			Hashtable<String,CheckStructure>htCS_byDTXSID=new Hashtable<>();

			//			System.out.println(Utilities.gson.toJson(ja));

			try {

				String sql="select distinct  dp.canon_qsar_smiles,dpc.dtxsid,dpc.dtxcid,smiles from qsar_datasets.data_points dp\r\n"
						+ "join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id\r\n"
						+ "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
						+"where d.id="+fk_dataset_id+";";

//				System.out.println(sql);
				
				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
				GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
				int counter=0;
				List<DsstoxRecord>dsstoxRecordsAll=new ArrayList<>();

				while (rs.next()) {

					counter++;

//					if(counter%1000==0) System.out.println(counter);

					CheckStructure cs=new CheckStructure();

					cs.dp_canon_qsar_smiles=rs.getString(1);
					cs.dpc_dtxsid=rs.getString(2);
					cs.dpc_dtxcid=rs.getString(3);
					cs.dpc_smiles=rs.getString(4);


//					if(cs.dp_canon_qsar_smiles.equals("C1C2CC3CC1(CC(C2)C3)C1C=CC=CC=1")) {
//						System.out.println("Found it 1:"+Utilities.gson.toJson(cs));
//					}

					dtxsids.add(cs.dpc_dtxsid);

					htCS_byDTXSID.put(cs.dpc_dtxsid, cs);

					if(dtxsids.size()==1000) {
						//GenericSubstanceServiceImpl only seems to work for small batches (<100)
						//						List<DsstoxRecord>dsstoxRecords=gss.findAsDsstoxRecordsByDtxsidIn(dtxsids);

						List<DsstoxRecord>dsstoxRecords=getDsstoxRecords(dtxsids);
						dsstoxRecordsAll.addAll(dsstoxRecords);
						dtxsids.clear();
					}
				}//end overall loop

				//do what's left
				List<DsstoxRecord>dsstoxRecords=getDsstoxRecords(dtxsids);
				dsstoxRecordsAll.addAll(dsstoxRecords);

				//				if(true)return;

				Hashtable<String,CheckStructure>htCS_byQsarSmiles=new Hashtable<>();


				for(DsstoxRecord dsstoxRecord:dsstoxRecordsAll) {
					CheckStructure cs2=htCS_byDTXSID.get(dsstoxRecord.getDsstoxSubstanceId());
					cs2.dsstox_casrn=dsstoxRecord.getCasrn();
					cs2.dsstox_dtxcid=dsstoxRecord.getDsstoxCompoundId();
					cs2.dsstox_smiles=dsstoxRecord.getSmiles();
					cs2.qc_level=dsstoxRecord.qcLevel;
					htCS_byQsarSmiles.put(cs2.dp_canon_qsar_smiles,cs2);
					
//					if(cs2.dp_canon_qsar_smiles.equals("C1C2CC3CC1(CC(C2)C3)C1C=CC=CC=1")) {
//						System.out.println(Utilities.gson.toJson(cs2));
//					}
				}

//				FileWriter fw=new FileWriter(of+File.separator+"check structures by dp_canon_qsar_smiles.json");
//				fw.write(Utilities.gson.toJson(htCS_byQsarSmiles));
//				fw.flush();
//				fw.close();


				return htCS_byQsarSmiles;


			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}


		/**
		 * For checking by Tony and Charlie
		 * 
		 */
		void getRecordsInDatasets() {

			HashSet<String>dtxsids=new HashSet<>();

			String of = "data\\episuite\\episuite validation\\";

			//			System.out.println(Utilities.gson.toJson(ja));

			try {
				String sql="select distinct dpc.dtxsid from qsar_datasets.data_points dp\r\n"
						+ "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
						+ "join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id\r\n"
						+ "where d.id in (116,536,107);";//LogP v1 modeling, exp_prop_LOG_KOW_external_validation, WS v1 modeling

				ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
				GenericSubstanceServiceImpl gss=new GenericSubstanceServiceImpl();
				int counter=0;
				List<DsstoxRecord>dsstoxRecordsAll=new ArrayList<>();

				while (rs.next()) {
					counter++;
					if(counter%1000==0) System.out.println(counter);

					String dtxsid=rs.getString(1);
					dtxsids.add(dtxsid);

					if(dtxsids.size()==1000) {
						List<DsstoxRecord>dsstoxRecords=getDsstoxRecords(dtxsids);
						dsstoxRecordsAll.addAll(dsstoxRecords);
						dtxsids.clear();
					}
				}//end overall loop

				//do what's left
				List<DsstoxRecord>dsstoxRecords=getDsstoxRecords(dtxsids);
				dsstoxRecordsAll.addAll(dsstoxRecords);

				String json=Utilities.gson.toJson(dsstoxRecordsAll);
				JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
				ExcelSourceReader.convertJsonArrayToExcel(ja, of+"LogKow and WS dsstox records.xlsx");

			} catch (Exception ex) {
				ex.printStackTrace();

			}

		}

		void findBadStructures() {

//			String propertyName="LogKow";
			String propertyName="WS";
			String of = "data\\episuite\\episuite validation\\" + propertyName;

			
			String propertyNameDataset = propertyName;
			if (propertyName.equals("LogKow"))
				propertyNameDataset = "LogP";
			String datasetName = propertyNameDataset + " v1 modeling";
			if(propertyName.equals("96HR_Fish_LC50")) {
				datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
			}
			long fk_dataset_id = du.getDatasetId(datasetName);

//			createStructureHashtableFile(propertyName, fk_dataset_id,  of);

//			if(true)return;


			//			boolean loadCheckstructureHashtable=true;
			//			boolean createCheckstructureHashtable=true;
			//			if(loadCheckstructureHashtable) {
			//				if(createCheckstructureHashtable) {
			//					createStructureHashtableFile();
			//				} else {
			//					Type type = new TypeToken<Hashtable<String,CheckStructure>>() {}.getType();
			//					String filepath=of+File.separator+"check structures.json";
			//					try {
			//						Hashtable<String,CheckStructure>htCS_byDTXSID=Utilities.gson.fromJson(new FileReader(filepath), type);
			//					} catch (Exception e) {
			//						e.printStackTrace();
			//					}
			//				}
			//			}

			Hashtable<String,CheckStructure>htCS_scifinder_by_sid=new Hashtable<>();
			Hashtable<String,CheckStructure>htCS_scifinder_by_qsar_smiles=new Hashtable<>();

			ExcelSourceReader e=new ExcelSourceReader();
			JsonArray ja=e.parseRecordsFromExcel(of+File.separator+propertyName+" check structures.xlsx", 1, 0, true);

			for(int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				CheckStructure cs=Utilities.gson.fromJson(jo, CheckStructure.class);
				//				System.out.println(Utilities.gson.toJson(cs));

				if(cs.scifinder_smiles!=null) {
					htCS_scifinder_by_sid.put(cs.dpc_dtxsid,cs);
				}

				htCS_scifinder_by_qsar_smiles.put(cs.dp_canon_qsar_smiles,cs);
			}

			FileWriter fw;
			try {
				fw = new FileWriter(of+File.separator+"scifinder structures.json");
				fw.write(Utilities.gson.toJson(htCS_scifinder_by_qsar_smiles));
				fw.flush();
				fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			//Check where dsstox structure doesnt match my smiles
			//Check where scifinder structure doesnt match smiles

			//			System.out.println(Utilities.gson.toJson(ja));

			compareStructuresToScifinder(propertyName, htCS_scifinder_by_sid);


		}

		private void compareStructuresToScifinder(String propertyName,
				Hashtable<String, CheckStructure> htCS_scifinder) {

			List<CheckStructure>badCS=new ArrayList<>();

			DecimalFormat df=new DecimalFormat("0.00");


			for (String dtxsid:htCS_scifinder.keySet()) {
				CheckStructure cs=htCS_scifinder.get(dtxsid);
				if(cs.scifinder_smiles.isBlank())continue;

				//				String inchiKey1DPC_smiles=StructureUtil.indigoInchikey1FromSmiles(cs.dpc_smiles);
				String inchiKey1Dsstox_smiles=StructureUtil.indigoInchikey1FromSmiles(cs.dsstox_smiles);

				cs.mwDPC_smiles=StructureUtil.molecularWeight(cs.dpc_smiles);
				cs.mwDsstox_smiles=StructureUtil.molecularWeight(cs.dsstox_smiles);

				//				System.out.println(cs.scifinder_smiles);

				if(cs.scifinder_smiles.equals("0.0")) {
					cs.scifinder_smiles=null;
					badCS.add(cs);
					continue;
				}

				String inchiKey1Scifinder=StructureUtil.indigoInchikey1FromSmiles(cs.scifinder_smiles);
				cs.mwScifinder=StructureUtil.molecularWeight(cs.scifinder_smiles);


				double diff=Math.abs(cs.mwScifinder-cs.mwDsstox_smiles);

				if(!inchiKey1Dsstox_smiles.equals(inchiKey1Scifinder)) {
					//				if(diff>0.1) {
					//					System.out.println("\n"+(++count)+"\t"+mwDsstox_smiles+"\t"+mwScifinder+"\n"+Utilities.gson.toJson(cs));
					//					System.out.println(++count+"\t"+cs.dpc_dtxsid+"\t"+cs.dsstox_casrn+"\t"+df.format(diff));
					badCS.add(cs);
				}
			}

			badCS.sort(new CheckStructureComparator());

			int count=0;
			System.out.println("count\tdpc_dtxsid\tdsstox_casrn\tscifinder_casrn\tMW_diff\tqc_level");
			for(CheckStructure cs:badCS) {

				if(cs.mwScifinder==null) {
					System.out.println(++count+"\t"+cs.dpc_dtxsid+"\t"+cs.dsstox_casrn+"\t"+cs.scifinder_casrn+"\tN/A"+"\t"+cs.qc_level);
				} else {
					double diff=Math.abs(cs.mwScifinder-cs.mwDsstox_smiles);
					System.out.println(++count+"\t"+cs.dpc_dtxsid+"\t"+cs.dsstox_casrn+"\t"+cs.scifinder_casrn+"\t"+df.format(diff)+"\t"+cs.qc_level);
				}
			}

			writeCasDiscrepancies(propertyName, badCS);


		}


		private void writeCheckStructures(Hashtable<String, CheckStructure> htCS_byDTXSID, String of)
				throws IOException {
			FileWriter fw=new FileWriter(of+File.separator+"check structures.txt");

			fw.write("dp_canon_qsar_smiles\tdpc_smiles\tdsstox_smiles\tdpc_dtxsid\tdsstox_casrn\tqc_level\r\n");

			for (String dtxsid:htCS_byDTXSID.keySet()) {
				CheckStructure cs=htCS_byDTXSID.get(dtxsid);

				//				String inchiKey1DPC_smiles=StructureUtil.indigoInchikey1FromSmiles(cs.dpc_smiles);
				//				String inchiKey1Dsstox_smiles=StructureUtil.indigoInchikey1FromSmiles(cs.dsstox_smiles);
				//
				////					if(!cs2.dpc_dtxcid.equals(cs2.dsstox_dtxcid)) {
				//				
				//				if(!inchiKey1DPC_smiles.equals(inchiKey1Dsstox_smiles)) {	
				//					System.out.println(Utilities.gson.toJson(cs));
				//				}

				fw.write(cs.dp_canon_qsar_smiles+"\t"+cs.dpc_smiles+"\t"+cs.dsstox_smiles+"\t"+cs.dpc_dtxsid+"\t"+cs.dsstox_casrn+"\t"+cs.qc_level+"\r\n");
			}

			fw.flush();
			fw.close();
		}



		Hashtable<String, DataPoint> getDatapoints(String datapointsFilePath) {

			Type listType = new TypeToken<List<DataPoint>>() {}.getType();

			try {
				List<DataPoint> dps = Utilities.gson.fromJson(new FileReader(datapointsFilePath), listType);
				//			System.out.println(Utilities.gson.toJson(dps));

				Hashtable<String, DataPoint> htDP = new Hashtable<>();

				for (DataPoint dp : dps) {
					htDP.put(dp.getCanonQsarSmiles(), dp);
				}
				return htDP;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		long getDatasetId(String datasetName) {
			String sql = "Select id from qsar_datasets.datasets where name='" + datasetName + "';";
			return Long.parseLong(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql));
		}

		void createTestSetDatapointsFile(String outputFilePath, long fk_dataset_id,boolean writeFile) {

			String sql = "select distinct  dp.canon_qsar_smiles, dp.qsar_dtxcid, dp.qsar_property_value from qsar_datasets.data_points dp\r\n"
					+ "		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
					+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
					+ "		where dpis.fk_splitting_id=1 and d.id=" + fk_dataset_id + " and dpis.split_num=1;";


//			System.out.println(sql);

			try {

				ResultSet rs = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

				int counter = 0;


				Hashtable<String, Double> htExps = new Hashtable<>();

				List<DataPoint> dps = new ArrayList<>();

				while (rs.next()) {
					String qsarSmiles = rs.getString(1);
					String dtxcid = rs.getString(2);
					Double exp = rs.getDouble(3);

					DataPoint dp = new DataPoint();

					dp.setCanonQsarSmiles(qsarSmiles);
					dp.setQsar_dtxcid(dtxcid);
					dp.setQsarPropertyValue(exp);

					dps.add(dp);
					//				System.out.println(++counter+"\t"+qsarSmiles+"\t"+dtxcid+"\t"+exp);
					htExps.put(qsarSmiles, exp);
				}

				System.out.println("Number of test set datapoints="+dps.size());
				
				if(writeFile) {
					FileWriter fw = null;
					fw = new FileWriter(outputFilePath);
					fw.write(Utilities.gson.toJson(dps));
					fw.flush();
					fw.close();
				}
				

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		
		/**
		 * This version gets test set compounds that dont have same qsarSmiles or inchiKey1 in training set
		 * @param outputFilePath
		 * @param fk_dataset_id
		 * 
		 */
		void createTestSetDatapointsFile2(String outputFilePath, long fk_dataset_id) {

			try {

				List<DataPoint>dpsTest=getDataPoints(fk_dataset_id, 1);
				List<DataPoint>dpsTrain=getDataPoints(fk_dataset_id, 0);
				
				omitBySmilesAndInchikey1(dpsTest, dpsTrain);

				FileWriter fw = null;
				fw = new FileWriter(outputFilePath);
				fw.write(Utilities.gson.toJson(dpsTest));
				fw.flush();
				fw.close();
				System.out.println("Number of test set datapoints="+dpsTest.size());

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		
		/**
		 * Gets test set datapoints but omits test set chemicals if inchiKey1 matches training chemical
		 * 
		 * @param fk_dataset_id
		 * @return
		 */
		List<DataPoint> getTestSetDatapoints2(long fk_dataset_id) {

			try {

				List<DataPoint>dpsTest=getDataPoints(fk_dataset_id, 1);
				List<DataPoint>dpsTrain=getDataPoints(fk_dataset_id, 0);
				return omitBySmilesAndInchikey1(dpsTest, dpsTrain);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}
		
		List<DataPoint> getTestSetDatapoints(long fk_dataset_id) {

			String sql = "select distinct  dp.canon_qsar_smiles, dp.qsar_dtxcid, dp.qsar_property_value from qsar_datasets.data_points dp\r\n"
					+ "		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
					+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
					+ "		where dpis.fk_splitting_id=1 and d.id=" + fk_dataset_id + " and dpis.split_num=1;";


//			System.out.println(sql);

			try {

				ResultSet rs = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

				int counter = 0;

				Hashtable<String, Double> htExps = new Hashtable<>();

				List<DataPoint> dps = new ArrayList<>();

				while (rs.next()) {
					String qsarSmiles = rs.getString(1);
					String dtxcid = rs.getString(2);
					Double exp = rs.getDouble(3);

					DataPoint dp = new DataPoint();
					dp.setCanonQsarSmiles(qsarSmiles);
					dp.setQsar_dtxcid(dtxcid);
					dp.setQsarPropertyValue(exp);
					dps.add(dp);
					//				System.out.println(++counter+"\t"+qsarSmiles+"\t"+dtxcid+"\t"+exp);
					htExps.put(qsarSmiles, exp);
				}
				System.out.println("Number of test set datapoints="+dps.size());
				return dps;
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}

		private Hashtable<String, String> getDtxcidToQsarSmilesHashtableForDataset(long fk_dataset_id) throws SQLException {
			String sql2 = "select distinct dp.canon_qsar_smiles, dpc.dtxcid from qsar_datasets.data_points dp\r\n"
					+ "		join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n"
					+"		join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id\r\n"				
					+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
					+ "		where dpis.fk_splitting_id=1 and d.id=" + fk_dataset_id + " and dpis.split_num=1;";


			//			System.out.println(sql);

			ResultSet rs = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql2);


			Hashtable<String, String> htDtxcidToSmiles = new Hashtable<>();

			int counter = 0;

			while (rs.next()) {
				String qsarSmiles = rs.getString(1);
				String dtxcid = rs.getString(2);

				//				System.out.println(++counter+"\t"+qsarSmiles+"\t"+dtxcid2+"\t"+exp);
				htDtxcidToSmiles.put(dtxcid, qsarSmiles);
			}
			return htDtxcidToSmiles;
		}

		/**
		 * Create prediction set using chemicals in fk_dataset_id but omit training
		 * chemicals in fk_dataset_id_omit_training_data
		 * 
		 * @param fk_dataset_external_id
		 * @param fk_dataset_id_omit
		 * @param descriptorSetName
		 * @param splittingName
		 * @return
		 */
		public void createTestSetExternalDatapointsFile(String outputFilePath, long fk_dataset_external_id,
				long fk_dataset_id_omit,boolean omitOnlyTraining) {

			Connection conn = SqlUtilities.getConnectionPostgres();

			try {

				FileWriter fw = new FileWriter(outputFilePath);

				String sql=null;

				if(omitOnlyTraining) {
					sql = "select distinct dp.canon_qsar_smiles,dp.qsar_dtxcid,dp.qsar_property_value from qsar_datasets.data_points dp\r\n"
							+ "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n" + "where dp.fk_dataset_id="
							+ fk_dataset_external_id + "\r\n" + "and dp.canon_qsar_smiles not in\r\n"
							+ "(select dp.canon_qsar_smiles from qsar_datasets.data_points dp\r\n"
							+ "join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
							+ "where  dp.fk_dataset_id=" + fk_dataset_id_omit + " and fk_splitting_id=1\r\n"
							+ " and split_num=0);";

				} else {
					sql = "select distinct dp.canon_qsar_smiles,dp.qsar_dtxcid,dp.qsar_property_value from qsar_datasets.data_points dp\r\n"
							+ "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n" + "where dp.fk_dataset_id="
							+ fk_dataset_external_id + "\r\n" + "and dp.canon_qsar_smiles not in\r\n"
							+ "(select dp.canon_qsar_smiles from qsar_datasets.data_points dp\r\n"
							+ "where  dp.fk_dataset_id=" + fk_dataset_id_omit+");";
				}

				//			System.out.println("\n" + sql);

				int counterTest = 0;

				ResultSet rs = SqlUtilities.runSQL2(conn, sql);

				List<DataPoint> dps = new ArrayList<>();

				while (rs.next()) {
					String canon_qsar_smiles = rs.getString(1);
					String dtxcid = rs.getString(2);
					double exp = rs.getDouble(3);
					//				fw.write(canon_qsar_smiles+"\t"+dtxcid+"\r\n");
					//				fw.flush();

					DataPoint dp = new DataPoint();

					dp.setCanonQsarSmiles(canon_qsar_smiles);
					dp.setQsar_dtxcid(dtxcid);
					dp.setQsarPropertyValue(exp);

					dps.add(dp);

					counterTest++;
				}

				fw.write(Utilities.gson.toJson(dps));
				fw.flush();
				fw.close();

				System.out.println("CountPrediction=" + counterTest);

			} catch (Exception ex) {
				ex.printStackTrace();

			}

			// System.out.println("CountTraining="+countTraining);

		}
		
		
		/**
		 * Omits by qsarSmiles and inchiKey1
		 * 
		 * @param outputFilePath
		 * @param fk_dataset_external_id
		 * @param fk_dataset_id_omit
		 * @param omitOnlyTraining
		 * @return
		 */
		public List<DataPoint> createTestSetExternalDatapointsFile2(String outputFilePath, long fk_dataset_external_id,
				long fk_dataset_id_omit,boolean omitOnlyTraining) {

			try {

				//First get datapointsfrom original dataset:

				List<DataPoint>dpsExternal=getDataPoints(fk_dataset_external_id);
				List<DataPoint>dpsOmit=getDataPoints(fk_dataset_id_omit);
				omitBySmilesAndInchikey1(dpsExternal, dpsOmit);
		        
				FileWriter fw = new FileWriter(outputFilePath);
				fw.write(Utilities.gson.toJson(dpsExternal));
				fw.flush();
				fw.close();

		        return dpsExternal;
		        
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			// System.out.println("CountTraining="+countTraining);
		}

		private List<DataPoint> omitBySmilesAndInchikey1(List<DataPoint> dpsExternal, List<DataPoint> dpsOmit) {
			Hashtable<String,DataPoint>htDataPointsByQsarSmiles=new Hashtable<>();
			for (DataPoint dp:dpsOmit) htDataPointsByQsarSmiles.put(dp.getCanonQsarSmiles(), dp);

			Hashtable<String,DataPoint>htDataPointsByInchikey1=new Hashtable<>();
			for (DataPoint dp:dpsOmit) htDataPointsByInchikey1.put(dp.inchiKey1_qsarSmiles, dp);

			Iterator<DataPoint> iterator = dpsExternal.iterator();
			
			int countRemovedBySmiles=0;
			int countRemovedByInchiKey1=0;
			for (int i=0;i<dpsExternal.size();i++) {
				DataPoint dp=dpsExternal.get(i);
				if(htDataPointsByQsarSmiles.containsKey(dp.getCanonQsarSmiles())) {
					countRemovedBySmiles++;
					dpsExternal.remove(i--);
				}
			}
			for (int i=0;i<dpsExternal.size();i++) {
				DataPoint dp=dpsExternal.get(i);
				if(htDataPointsByInchikey1.containsKey(dp.inchiKey1_qsarSmiles)) {
					countRemovedByInchiKey1++;
					dpsExternal.remove(i--);
				}
			}
			
	        System.out.println("countRemovedBySmiles="+countRemovedBySmiles);
	        System.out.println("countRemovedByInchiKey1="+countRemovedByInchiKey1);
//	        System.out.println("dpsExternal.size()="+dpsExternal.size()+"\n");
	        return dpsExternal;
		}

		private List<DataPoint> getDataPoints(long fk_dataset_id) throws SQLException {
			String sql = "select distinct dp.canon_qsar_smiles,dp.qsar_dtxcid,dp.qsar_property_value from qsar_datasets.data_points dp\r\n"
					+ "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n" 
					+ "where dp.fk_dataset_id="+fk_dataset_id+";";

			
			Connection conn=SqlUtilities.getConnectionPostgres();
			ResultSet rs = SqlUtilities.runSQL2(conn, sql);

			List<DataPoint> dps = new ArrayList<>();

			while (rs.next()) {
				String canon_qsar_smiles = rs.getString(1);
				String dtxcid = rs.getString(2);
				double exp = rs.getDouble(3);

				DataPoint dp = new DataPoint();
				dp.setCanonQsarSmiles(canon_qsar_smiles);
				dp.setQsar_dtxcid(dtxcid);
				dp.setQsarPropertyValue(exp);
				dp.inchiKey1_qsarSmiles=StructureUtil.indigoInchikey1FromSmiles(canon_qsar_smiles);
				dps.add(dp);
			}
			return dps;
		}
		
		private List<DataPoint> getDataPoints(long fk_dataset_id, int splitNum) throws SQLException {
			String sql = "select distinct dp.canon_qsar_smiles,dp.qsar_dtxcid,dp.qsar_property_value from qsar_datasets.data_points dp\r\n"
					+ "join qsar_datasets.datasets d on dp.fk_dataset_id = d.id\r\n" 
					+ "join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
					+ "where dp.fk_dataset_id="+fk_dataset_id+" and dpis.fk_splitting_id=1 and dpis.split_num="+splitNum+";";

//			System.out.println(sql);
			
			Connection conn=SqlUtilities.getConnectionPostgres();
			
			ResultSet rs = SqlUtilities.runSQL2(conn, sql);

			List<DataPoint> dps = new ArrayList<>();

			while (rs.next()) {
				String canon_qsar_smiles = rs.getString(1);
				String dtxcid = rs.getString(2);
				double exp = rs.getDouble(3);

				DataPoint dp = new DataPoint();
				dp.setCanonQsarSmiles(canon_qsar_smiles);
				dp.setQsar_dtxcid(dtxcid);
				dp.setQsarPropertyValue(exp);
				dp.inchiKey1_qsarSmiles=StructureUtil.indigoInchikey1FromSmiles(canon_qsar_smiles);
				dps.add(dp);
			}
			return dps;
		}

		public  boolean isValidCAS(String casInput) {
			long t1=System.currentTimeMillis();
			if(casInput.toUpperCase().contains("CHEMBL")) return false;
			if(casInput.toUpperCase().contains("SRC")) return false;
			if(casInput.toUpperCase().contains("NO")) return false;

			String regex = "[0-9\\-]+"; //only has numbers and dashes
			Pattern p = Pattern.compile(regex); 
			Matcher m = p.matcher(casInput);

			if(!m.matches()) return false;

			if(casInput.substring(0,1).equals("-")) return false;


			String[] casArray = casInput.split("\\||;|,");
			boolean valid = true;
			for (String cas:casArray) {
				String casTemp = cas.replaceAll("[^0-9]","");//do we really want to discard non numbers???
				int len = casTemp.length();
				if (len > 10 || len <= 0) { return false; }
				int check = Character.getNumericValue(casTemp.charAt(len-1));
				int sum = 0;
				for (int i = 1; i <= len-1; i++) {
					sum += i*Character.getNumericValue(casTemp.charAt(len-1-i));
				}
				if (sum % 10 != check) {
					valid = false;
					break;
				}
				// There are no valid CAS RNs with bad formatting in the current data set, but if that happens in other sources, could add format correction here
				//			else if (!cas.contains("-")) {
				//				System.out.println("Valid CAS with bad format: "+cas);
				//			}
			}
			long t2=System.currentTimeMillis();
			//		System.out.println((t2-t1)+" millisecs to check cas");

			return valid;
		}

		//		public HashMap<String, Compound> getQsarSmilesLookupFromDB() {
		//			
		//			CompoundService compoundService = new CompoundServiceImpl();
		//			
		//
		//			List<Compound> standardizedCompounds = compoundService.findAllWithStandardizerSmilesNotNull(standardizer.standardizerName);
		//
		//			System.out.println("Number of standardized compounds in db:" + standardizedCompounds.size());
		//
		//			HashMap<String, Compound> hmQsarSmiles=new HashMap<>();
		//			
		//			for (Compound compound : standardizedCompounds) {
		//				if (!compound.getStandardizer().equals(standardizer.standardizerName)) {
		//					//				System.out.println("skip "+compound.getStandardizer()+"\t"+standardizer.standardizerName);
		//					continue;
		//				}
		//				//			System.out.println(compound.getKey());
		//				hmQsarSmiles.put(compound.getKey(), compound);
		//			}
		//			return hmQsarSmiles;
		//
		//		}

		class EpisuiteSmilesToQsarSmiles {
			String cas;
			String smilesEpisuite;
			String qsarSmiles;
		}
		
		class SmilesToQsarSmiles {
			String smiles;
			String qsarSmiles;
		}

		Gson gson=new Gson();
		
		private LinkedHashMap<String, EpisuiteSmilesToQsarSmiles> getQsarSmilesHashtable(String filepath, Hashtable<String, String> smilesByCAS,String workflow) {

			File file=new File(filepath);

			try {

				LinkedHashMap<String,EpisuiteSmilesToQsarSmiles>htSmiles=new LinkedHashMap<>();

				if(file.exists()) {
					htSmiles=getEpisuiteSmilesToQsarSmiles(filepath);
				}

				if(debug)
					System.out.println("Already standardized="+htSmiles.size());
				//				System.out.println(Utilities.gson.toJson(htSmiles));
				FileWriter fw=new FileWriter(filepath,file.exists());
				int counter=0;

				for(String CAS:smilesByCAS.keySet()) {

					counter++;

					if(htSmiles.containsKey(CAS)) {
						//						System.out.println("Skipping "+CAS);
						continue;
					}

					
					EpisuiteSmilesToQsarSmiles e=new EpisuiteSmilesToQsarSmiles();
					e.smilesEpisuite=smilesByCAS.get(CAS);
					e.qsarSmiles=du.standardize(e.smilesEpisuite);
					e.cas=CAS;
					
					
//					if(CAS.equals("741250-20-4")) {
//						System.out.println("741250-20-4\t"+e.qsarSmiles);
//					}


					htSmiles.put(CAS, e);
					
					System.out.println(counter+"\t"+CAS+"\t"+e.smilesEpisuite+"\t"+e.qsarSmiles);

					fw.write(gson.toJson(e)+"\r\n");
					fw.flush();
				}


				fw.close();

				return htSmiles;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		private LinkedHashMap<String, EpisuiteSmilesToQsarSmiles> getEpisuiteSmilesToQsarSmiles(String filepath) {

			try {
				BufferedReader br = new BufferedReader(new FileReader(filepath));

				LinkedHashMap<String, EpisuiteSmilesToQsarSmiles> htSmiles = new LinkedHashMap<>();

				while (true) {
					String Line = br.readLine();
					if (Line == null)
						break;
					EpisuiteSmilesToQsarSmiles e = gson.fromJson(Line, EpisuiteSmilesToQsarSmiles.class);
					htSmiles.put(e.cas, e);
				}
				br.close();

				return htSmiles;
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}

		
		private LinkedHashMap<String, SmilesToQsarSmiles> getQsarSmilesHashtableRevised(String filepath, Hashtable<String,CheckStructure>htCS_byQsarSmiles,String workflow) {

			File file=new File(filepath);

			try {

				LinkedHashMap<String,SmilesToQsarSmiles>htSmiles=new LinkedHashMap<>();
				if(file.exists()) {

					BufferedReader br=new BufferedReader (new FileReader(filepath));

					while (true) {
						String Line=br.readLine();
						if(Line==null)break;
						SmilesToQsarSmiles e=gson.fromJson(Line,SmilesToQsarSmiles.class);
						htSmiles.put(e.smiles,e);
					}
					br.close();
				}

				if(debug)
					System.out.println("Already standardized="+htSmiles.size());
				//				System.out.println(Utilities.gson.toJson(htSmiles));

				FileWriter fw=new FileWriter(filepath,file.exists());

				int counter=0;

				for(String dp_qsarSmiles:htCS_byQsarSmiles.keySet()) {

					counter++;
					
					CheckStructure cs=htCS_byQsarSmiles.get(dp_qsarSmiles);
					

					if(htSmiles.containsKey(cs.dsstox_smiles)) {
						//						System.out.println("Skipping "+CAS);
						continue;
					}

					SmilesToQsarSmiles e=new SmilesToQsarSmiles();
					
					e.smiles=cs.dsstox_smiles;
					e.qsarSmiles=du.standardize(e.smiles);
					
					htSmiles.put(e.smiles,e);

					System.out.println(counter+"\t"+e.smiles+"\t"+e.qsarSmiles);

					fw.write(gson.toJson(e)+"\r\n");
					fw.flush();
				}

				fw.close();

				return htSmiles;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		class DescriptorsByCAS {
			String CAS;
			String qsarSmiles;
			String strDescriptors;
		}
		


		private LinkedHashMap<String, DescriptorsByCAS> getDescriptorsHashtable(String filepath, LinkedHashMap<String, EpisuiteSmilesToQsarSmiles> htEpismilesToQsarSmiles) {

			DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
			String descriptorSetName="WebTEST-default";

			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
			if (descriptorSet==null) {
				System.out.println("No such descriptor set: " + descriptorSetName);
			}

			String server="https://hcd.rtpnc.epa.gov/";
			//			String server = "https://hazard-dev.sciencedataexperts.com";

			
			SciDataExpertsDescriptorValuesCalculator.configUnirest=false;
			SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");

			Gson gson=new Gson();

			File file=new File(filepath);

			try {

				LinkedHashMap<String,DescriptorsByCAS>htDescriptorsByQsarSmiles=new LinkedHashMap<>();
				if(file.exists()) {

					BufferedReader br=new BufferedReader (new FileReader(filepath));

					while (true) {
						String Line=br.readLine();
						if(Line==null)break;
						DescriptorsByCAS d=gson.fromJson(Line,DescriptorsByCAS.class);
						htDescriptorsByQsarSmiles.put(d.qsarSmiles,d);
					}
					br.close();
				}

				if(debug)
					System.out.println("Already got descriptors="+htDescriptorsByQsarSmiles.size());

				//				System.out.println(Utilities.gson.toJson(htSmiles));


				FileWriter fw=new FileWriter(filepath,file.exists());

				int counter=0;

				for(String CAS:htEpismilesToQsarSmiles.keySet()) {

					EpisuiteSmilesToQsarSmiles e=htEpismilesToQsarSmiles.get(CAS);

					counter++;

					if(htDescriptorsByQsarSmiles.containsKey(e.qsarSmiles)) {
						continue;
					}

					DescriptorsByCAS d=new DescriptorsByCAS();
					d.CAS=CAS;
					d.qsarSmiles=e.qsarSmiles;

					d.strDescriptors=calc.calculateDescriptors(d.qsarSmiles, descriptorSet);

					if(d.strDescriptors==null) {
						System.out.println(counter+"\t"+CAS+"\t"+d.qsarSmiles+"\tError");
						continue;
					}

					htDescriptorsByQsarSmiles.put(d.qsarSmiles, d);

					System.out.println(counter+"\t"+CAS+"\t"+d.qsarSmiles+"\tDesc");

					fw.write(gson.toJson(d)+"\r\n");
					fw.flush();
				}


				fw.close();

				return htDescriptorsByQsarSmiles;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}
		
		
		private LinkedHashMap<String, DescriptorsByCAS> getDescriptorsHashtableByQsarSmiles(String filepath, Set<String> setQsarSmiles) {

			DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
			String descriptorSetName="WebTEST-default";

			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
			if (descriptorSet==null) {
				System.out.println("No such descriptor set: " + descriptorSetName);
			}

			String server="https://hcd.rtpnc.epa.gov/";
			//			String server = "https://hazard-dev.sciencedataexperts.com";

			SciDataExpertsDescriptorValuesCalculator.configUnirest=false;
			
			SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");

			Gson gson=new Gson();

			File file=new File(filepath);

			try {

				LinkedHashMap<String,DescriptorsByCAS>htDescriptors=new LinkedHashMap<>();
				if(file.exists()) {

					BufferedReader br=new BufferedReader (new FileReader(filepath));

					while (true) {
						String Line=br.readLine();
						if(Line==null)break;
						DescriptorsByCAS d=gson.fromJson(Line,DescriptorsByCAS.class);
						htDescriptors.put(d.qsarSmiles,d);
					}
					br.close();
				}

				if(debug)
					System.out.println("Already got descriptors="+htDescriptors.size());
				//				System.out.println(Utilities.gson.toJson(htSmiles));


				FileWriter fw=new FileWriter(filepath,file.exists());

				int counter=0;

				for(String qsarSmiles:setQsarSmiles) {


					counter++;

					if(htDescriptors.containsKey(qsarSmiles)) {
						continue;
					}

					DescriptorsByCAS d=new DescriptorsByCAS();
					d.qsarSmiles=qsarSmiles;
					d.strDescriptors=calc.calculateDescriptors(d.qsarSmiles, descriptorSet);

					if(d.strDescriptors==null) {
						System.out.println(counter+"\t"+d.qsarSmiles+"\tError");
						continue;
					}

					htDescriptors.put(d.qsarSmiles, d);

					System.out.println(counter+"\t"+d.qsarSmiles+"\tDesc");

					fw.write(gson.toJson(d)+"\r\n");
					fw.flush();
				}


				fw.close();

				return htDescriptors;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		public String getPredictionTsvByCAS(Hashtable<String, DataPoint> htDP_CAS,
				LinkedHashMap<String, DescriptorsByCAS> htDescByQsarSmiles, LinkedHashMap<String, EpisuiteSmilesToQsarSmiles> htCAStoEpiQsarSmiles,
				Hashtable<String, String>htCAS_to_episuite_smiles) {

			DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
			String descriptorSetName="WebTEST-default";

			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);

			String tsv="ID\tExp\t"+descriptorSet.getHeadersTsv()+"\r\n";

			for(String CAS:htDP_CAS.keySet()) {
				DataPoint dpc=htDP_CAS.get(CAS);
				Double exp=dpc.getQsarPropertyValue();
				
				if(!htCAS_to_episuite_smiles.containsKey(CAS)) {
//					System.out.println(CAS+"\tNo episuite smiles");
					continue;
				}
				
				if(!htCAStoEpiQsarSmiles.containsKey(CAS)) {
					System.out.println(CAS+"\tNo qsar smiles");
					continue;
				}				
				
				String qsarSmiles=htCAStoEpiQsarSmiles.get(CAS).qsarSmiles;
				

				if(!htDescByQsarSmiles.containsKey(qsarSmiles)) {
					//					System.out.println(CAS+"\tMissing descriptors");
					continue;
				}

				String line=CAS+"\t"+exp+"\t"+htDescByQsarSmiles.get(qsarSmiles).strDescriptors;
				tsv+=line+"\r\n";
			}

			return tsv;

		}
		
		
		public String getPredictionTsv2(Hashtable<String, DataPoint> htDP_by_qsar_smiles,
				LinkedHashMap<String, DescriptorsByCAS> htDescByQsarSmiles) {

			DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
			String descriptorSetName="WebTEST-default";

			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);

			String tsv="ID\tExp\t"+descriptorSet.getHeadersTsv()+"\r\n";

			for(String qsarSmiles:htDP_by_qsar_smiles.keySet()) {
				DataPoint dp=htDP_by_qsar_smiles.get(qsarSmiles);
				Double exp=dp.getQsarPropertyValue();

				if(!htDescByQsarSmiles.containsKey(qsarSmiles)) {
					//					System.out.println(CAS+"\tMissing descriptors");
					continue;
				}

				String line=qsarSmiles+"\t"+exp+"\t"+htDescByQsarSmiles.get(qsarSmiles).strDescriptors;
				tsv+=line+"\r\n";
			}

			return tsv;

		}
		
		
		public String getPredictionTsv(String smiles) {

			DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
			String descriptorSetName="WebTEST-default";

			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);

			String tsv="ID\tExp\t"+descriptorSet.getHeadersTsv()+"\r\n";
			
//			String server="https://hcd.rtpnc.epa.gov/";
			String server = "https://hazard-dev.sciencedataexperts.com";
			SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");

			
			String strDesc=calc.calculateDescriptors(smiles, descriptorSet);
			
			String line=smiles+"\t-9999\t"+strDesc;
			tsv+=line+"\r\n";

			return tsv;

		}

		private void createSDF_dsstox_smiles(String filePathSDF, Hashtable<String, DataPoint> htDP_by_dsstox_qsar_smiles) {
			try {
				FileWriter fileWriter = new FileWriter(filePathSDF);			
				SDFWriter sdfWriter = new SDFWriter(fileWriter);
				SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		
				for(String qsarSmiles:htDP_by_dsstox_qsar_smiles.keySet()) {
					DataPoint dp=htDP_by_dsstox_qsar_smiles.get(qsarSmiles);
					String smiles=dp.checkStructure.dsstox_smiles;
					
					IAtomContainer molecule = smilesParser.parseSmiles(smiles);
					molecule.setProperty("qsarSmiles", qsarSmiles);
					molecule.setProperty("SMILES", smiles);
					molecule.setProperty("dtxcid", dp.checkStructure.dsstox_dtxcid);
					// Write molecule to SDF
					sdfWriter.write(molecule);
					
				}
				sdfWriter.flush();
				sdfWriter.close();
		
			} catch (Exception e) {
				e.printStackTrace();
			}
		}



		

	}

	class Plot {

		//		void plotResQsarExternalModelPredictions(String title, String propertyName, String units,
		//					Results resultsEpi, long fk_model_id, long fk_dataset_external_id,
		//					long fk_dataset_id_omit_training_data,boolean omitOnlyTraining) {
		//		
		//		
		//				PredictScript ps = new PredictScript();
		//				List<ModelPrediction> modelPredictions = ps.runExternalSet(fk_model_id, fk_dataset_external_id,
		//						fk_dataset_id_omit_training_data,omitOnlyTraining);
		//		
		//				List<ModelPrediction> mps = new ArrayList<>();
		//		
		//				List<Double> exps = new ArrayList<>();
		//				List<Double> preds = new ArrayList<>();
		//		
		//				for (int i = 0; i < modelPredictions.size(); i++) {
		//					ModelPrediction mp = modelPredictions.get(i);
		//		
		//					if (!resultsEpi.htModelPredictions.containsKey(mp.id)) {
		//						//				System.out.println("Skipping\t"+qsarSmiles);
		//						continue;
		//					} 
		//		
		//		
		//					mps.add(mp);
		//					exps.add(mp.exp);
		//					preds.add(mp.pred);
		//				}
		//		
		//				Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(mps,
		//						-9999.0, DevQsarConstants.TAG_TEST);
		//		
		//				modelTestStatisticValues.remove("Coverage_Test");
		//				modelTestStatisticValues.remove("Q2_Test");
		//		
		//		//		System.out.println("\nWebTEST2.0 results for " + propertyName);
		//				System.out.println(title);
		//				System.out.println(Utilities.gson.toJson(modelTestStatisticValues));
		//				System.out.println("countPlotted=" + mps.size());
		//		
		//				//		String titleExt = "WebTest2.0 results for " + propertyName + " for ext. set";
		//		
		//				DecimalFormat df=new DecimalFormat("0.00");
		//		
		//				title += " (RMSE=" + df.format(modelTestStatisticValues.get("RMSE_Test")) + ", n=" + exps.size() + ")";
		//		
		//		
		//				createPlot(title, propertyName, units, exps, preds, modelTestStatisticValues, resultsEpi.minVal, resultsEpi.maxVal);
		//		
		//			}

		//		Results plotResQsarModelPredictions(String title, String propertyName, String units, Results resultsEpi,
		//				Hashtable<String, DataPoint> htDP, Hashtable<String, Double> htPredResQsar,boolean createPlot,boolean omitChemicalsNotPredictedByEpisuite) {
		//		
		//			try {
		//		
		//				List<Double> exps = new ArrayList<>();
		//				List<Double> preds = new ArrayList<>();
		//		
		//				List<ModelPrediction> mps = new ArrayList<>();
		//				Hashtable<String,ModelPrediction> htModelPredictions = new Hashtable<>();
		//		
		//				//			System.out.println("resultsEpi.htModelPredictions==null\t"+resultsEpi.htModelPredictions==null);
		//		
		//		
		//				for (String qsarSmiles : htDP.keySet()) {
		//		
		//					if (omitChemicalsNotPredictedByEpisuite &&  !resultsEpi.htModelPredictions.containsKey(qsarSmiles)) {
		//						//					System.out.println("Skipping\t"+qsarSmiles);
		//						continue;
		//					} 
		//		
		//					//				System.out.println(qsarSmiles+"\t"+resultsEpi.htModelPredictions.contains(qsarSmiles));
		//		
		//		
		//					//				if (resultsEpi.htModelPredictions != null && !resultsEpi.htModelPredictions.contains(qsarSmiles)) {
		//					////					System.out.println("Skipping "+qsarSmiles);
		//					//					continue;
		//					//				}
		//		
		//					Double exp = htDP.get(qsarSmiles).getQsarPropertyValue();
		//					Double pred = htPredResQsar.get(qsarSmiles);
		//		
		//					exps.add(exp);
		//					preds.add(pred);
		//		
		//					ModelPrediction mp = new ModelPrediction(qsarSmiles, exp, pred, 1);
		//					mps.add(mp);
		//		
		//					htModelPredictions.put(qsarSmiles, mp);
		//		
		//					//				System.out.println(qsarSmiles + "\t" + exp + "\t" + pred);
		//		
		//				}
		//		
		//				Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(mps,
		//						-9999.0, DevQsarConstants.TAG_TEST);
		//		
		//				modelTestStatisticValues.remove("Coverage_Test");
		//				modelTestStatisticValues.remove("Q2_Test");
		//		
		//				if(debug) {
		//					System.out.println("\nWebTEST2.0 results for " + propertyName);
		//					System.out.println(Utilities.gson.toJson(modelTestStatisticValues));
		//					System.out.println("countPlotted=" + exps.size());
		//				}
		//		
		//				DecimalFormat df = new DecimalFormat("0.00");
		//		
		//		
		//				title += " (RMSE=" + df.format(modelTestStatisticValues.get("RMSE_Test")) + ", n=" + exps.size() + ")";
		//		
		//				if(createPlot) createPlot(title, propertyName, units, exps, preds, modelTestStatisticValues, resultsEpi.minVal,
		//						resultsEpi.maxVal);
		//		
		//				Results results=new Results();
		//		
		//				results.modelTestStatisticValues=modelTestStatisticValues;
		//				results.modelPredictions=mps;
		//				results.htModelPredictions=htModelPredictions;
		//		
		//				return results;
		//		
		//			} catch (Exception ex) {
		//				ex.printStackTrace();
		//				return null;
		//			}
		//		
		//		}

//		Results plotPredictions(String title, String propertyName,  String units, 
//				 Hashtable<String, ModelPrediction> htMP,boolean createPlot,String outputFolder) {
//
//			List<Double> exps = new ArrayList<>();
//			List<Double> preds = new ArrayList<>();
//
//			Results results=new Results();
//			results.htModelPredictions=htMP;
//
//			List<ModelPrediction> mps = new ArrayList<>();
//			results.modelPredictions=mps;
//
//			int countInEpiTraining = 0;
//			int countNoPred = 0;
//
//			for (String key : htMP.keySet()) {
//
//				ModelPrediction mpEpi=htMP.get(key);
//				
//				if(omitChemicalsNotPredictedByEpisuite) {
//					if(mpEpi.split==0) {
//						countInEpiTraining++;
//						continue;
//					}
//					
//					if (mpEpi.pred.equals(Double.NaN) || mpEpi.pred==null) {
//						continue;
//					}
//				}
//
//				if(!htMP.containsKey(key)) {
//					continue;
//				}
//				
//				ModelPrediction mp=htMP.get(key);
//				
//				if(mp.pred==null || mp.pred.isNaN()) {
//					countNoPred++;
//					continue;
//				}
//				
//				mps.add(mp);
//				preds.add(mp.pred);
//				exps.add(mp.exp);
//				
//				if(mp.exp==null) {
//					System.out.println(mp.id+"\tnull exp\t"+title);
//				}
//				
//				                 
////				if(mp.id.equals("CCCC1C=CC(=CC=1)C1CCC(CCCCC)CC1")) {
////					System.out.println(Utilities.gson.toJson(mp));
////				}
//				
//				
////				if(mp.exp==null || mp.pred==null) {
////					System.out.println(Utilities.gson.toJson(mp));
////				}
//				
////				mp.dtxcid=dpCAS.checkStructure.dsstox_dtxcid;//this might not correspond to what episuite smiles is 
//			}
//
//
//			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(mps,
//					-9999.0, DevQsarConstants.TAG_TEST);
//
//			modelTestStatisticValues.remove("Coverage_Test");
//			modelTestStatisticValues.remove("Q2_Test");
//
//			if(debug) {
//				System.out.println("\n***********\n"+title);
//				System.out.println(Utilities.gson.toJson(modelTestStatisticValues));
//				System.out.println("countInEpiTraining=" + countInEpiTraining);
//				System.out.println("countNoPred=" + countNoPred);
//				System.out.println("countPlotted=" + mps.size());
//			}
//
//			DecimalFormat df = new DecimalFormat("0.00");
//			String subtitle = "RMSE=" + df.format(modelTestStatisticValues.get("RMSE_Test")) + ", n=" + exps.size();
//
//			if(createPlot)
//				createPlot(title,subtitle, propertyName, units, exps, preds, modelTestStatisticValues, resultsEpi.minVal,
//						resultsEpi.maxVal,outputFolder);
//
//			results.modelTestStatisticValues=modelTestStatisticValues;
//			return results;
//
//		}
		
		
		Results plotPredictions(String title, String propertyName, String units, 
				HashSet<String>smilesInCommon,AxesBounds axesBounds,
				 Hashtable<String, ModelPrediction> htMP,boolean createPlot,String outputFolder,String filename) {

			List<Double> exps = new ArrayList<>();
			List<Double> preds = new ArrayList<>();

			Results results=new Results();
			results.htModelPredictions=htMP;

			List<ModelPrediction> mps = new ArrayList<>();
			results.modelPredictions=mps;


			for (String qsarSmiles : htMP.keySet()) {
				
				ModelPrediction mp=htMP.get(qsarSmiles);
				
				if(!smilesInCommon.contains(qsarSmiles)) continue;
								
				mps.add(mp);
				preds.add(mp.pred);
				exps.add(mp.exp);
				
				if(mp.exp==null) {
					System.out.println(mp.id+"\tnull exp\t"+title);
				}
				
				                 
//				if(mp.id.equals("CCCC1C=CC(=CC=1)C1CCC(CCCCC)CC1")) {
//					System.out.println(Utilities.gson.toJson(mp));
//				}
				
				
//				if(mp.exp==null || mp.pred==null) {
//					System.out.println(Utilities.gson.toJson(mp));
//				}
				
//				mp.dtxcid=dpCAS.checkStructure.dsstox_dtxcid;//this might not correspond to what episuite smiles is 
			}


			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(mps,
					-9999.0, DevQsarConstants.TAG_TEST);

			modelTestStatisticValues.remove("Coverage_Test");
			modelTestStatisticValues.remove("Q2_Test");

			if(debug) {
				System.out.println("\n***********\n"+title);
				System.out.println(Utilities.gson.toJson(modelTestStatisticValues));
				System.out.println("countPlotted=" + mps.size());
			}

			DecimalFormat df = new DecimalFormat("0.00");
			String subtitle = "RMSE=" + df.format(modelTestStatisticValues.get("RMSE_Test")) + ", n=" + exps.size();
			
			if(createPlot)
				createPlot(filename, title,subtitle, propertyName, units, exps, preds, modelTestStatisticValues, axesBounds.minVal,
						axesBounds.maxVal,outputFolder);

			results.modelTestStatisticValues=modelTestStatisticValues;
			return results;

		}

//		Results plotPredictionsByCAS(String title, String propertyName, String units, Results resultsEpi,
//				Hashtable<String, ModelPrediction> htMP_test_by_CAS,boolean createPlot,boolean omitChemicalsNotPredictedByEpisuite,String outputFolder) {
//
//			List<Double> exps = new ArrayList<>();
//			List<Double> preds = new ArrayList<>();
//
//			Results results=new Results();
//			
//			results.htModelPredictions=htMP_test_by_CAS;
//
//			List<ModelPrediction> mps = new ArrayList<>();
//			results.modelPredictions=mps;
//
//			int countInEpiTraining = 0;
//			int countNoPred = 0;
//
//			for (String CAS : resultsEpi.htModelPredictions.keySet()) {
//
//				ModelPrediction mpEpi=resultsEpi.htModelPredictions.get(CAS);
//				
//				if(omitChemicalsNotPredictedByEpisuite) {
//					if(mpEpi.split==0) {
//						countInEpiTraining++;
//						continue;
//					}
//					if (mpEpi.pred.equals(Double.NaN) || mpEpi.pred==null) continue;
//				}
//				
//				
//				if(!htMP_test_by_CAS.containsKey(CAS))continue; 
//				
//				ModelPrediction mp=htMP_test_by_CAS.get(CAS);
//				
//				if (mp.pred.equals(Double.NaN) || mp.pred==null) {
//					countNoPred++;
//					continue;
//				}
//
//				exps.add(mp.exp);
//				preds.add(mp.pred);
//				mps.add(mp);
//
//			}
//			
//			Collections.sort(mps);
//			
//			
////			calculateStatsNo5BondedN(results);
//			
//
//			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(mps,
//					-9999.0, DevQsarConstants.TAG_TEST);
//
//			modelTestStatisticValues.remove("Coverage_Test");
//			modelTestStatisticValues.remove("Q2_Test");
//
//			if(debug) {
//				System.out.println("\n***********\n"+title);
//				System.out.println(Utilities.gson.toJson(modelTestStatisticValues));
//				System.out.println("countInEpiTraining=" + countInEpiTraining);
//				System.out.println("countNoPred=" + countNoPred);
//				System.out.println("countPlotted=" + mps.size());
//			}
//
//			DecimalFormat df = new DecimalFormat("0.00");
//			
//			String subtitle="RMSE="+df.format(modelTestStatisticValues.get("RMSE_Test")) + ", n=" + exps.size();
//			
//			if(createPlot)
//				createPlot(title, subtitle, propertyName, units, exps, preds, modelTestStatisticValues, resultsEpi.minVal,
//						resultsEpi.maxVal,outputFolder);
//
//			results.modelTestStatisticValues=modelTestStatisticValues;
//			
//			return results;
//
//		}

		Results plotEpisuitePredictions(String title, String propertyName, String modelNameEpi, String units, 
				Hashtable<String, ModelPrediction> htMP_epi, boolean omitTraining, boolean createPlot,String outputFolder) {

			//		System.out.println(hsSmilesEpisuiteTraining);


			Results results = new Results();
			results.htModelPredictions = htMP_epi;

			List<ModelPrediction> mps = new ArrayList<>();
			results.modelPredictions=mps;
			
			int countInEpiTraining = 0;
			int countNoPred = 0;

			
			List<Double> exps = new ArrayList<>();
			List<Double> preds = new ArrayList<>();

			for (String CAS : htMP_epi.keySet()) {

				ModelPrediction mp=htMP_epi.get(CAS);
				
				if(mp.split==0 && omitTraining) {
					countInEpiTraining++;
					continue;
				}
				
				if (mp.pred.equals(Double.NaN) || mp.pred==null) {
					countNoPred++;
					continue;
				}
				mps.add(mp);
				preds.add(mp.pred);
				exps.add(mp.exp);
				
//				if(mp.exp==null || mp.pred==null) {
//					System.out.println(Utilities.gson.toJson(mp));
//				}
				
//				mp.dtxcid=dpCAS.checkStructure.dsstox_dtxcid;//this might not correspond to what episuite smiles is 
			}
			Collections.sort(mps);
			
//			System.out.println(Utilities.gson.toJson(mps));

			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(mps,
					-9999.0, DevQsarConstants.TAG_TEST);

			modelTestStatisticValues.remove("Coverage_Test");
			modelTestStatisticValues.remove("Q2_Test");

			if(debug) {
				System.out.println("\n******\n"+title);
				System.out.println(Utilities.gson.toJson(modelTestStatisticValues));
				System.out.println("countInEpiTraining=" + countInEpiTraining);
				System.out.println("countNoPred=" + countNoPred);
				System.out.println("countPlotted=" + mps.size());
			}

			Double minVal = null;
			Double maxVal = null;

			for (ModelPrediction mp : mps) {
				if (minVal == null || mp.pred < minVal)
					minVal = mp.pred;
				if (maxVal == null || mp.pred > maxVal)
					maxVal = mp.pred;
			}

			if (propertyName.equals("BP") || propertyName.equals("MP")) {
				int num=50;
				minVal = (double) Math.round(minVal / num) * num - num;
				maxVal = (double) Math.round(maxVal / num) * num + num;
			} else {
				minVal = Math.floor(minVal) - 1;
				maxVal = Math.ceil(maxVal) + 1;
			}

			DecimalFormat df = new DecimalFormat("0.00");

			String subtitle ="RMSE="+df.format(modelTestStatisticValues.get("RMSE_Test")) + ", n=" + exps.size();

			
			if(createPlot)
				createPlot(null, title, subtitle, propertyName, units, exps, preds, modelTestStatisticValues, minVal, maxVal,outputFolder);

			results.minVal = minVal;
			results.maxVal = maxVal;
			results.modelTestStatisticValues=modelTestStatisticValues;
			return results;

		}
		
		
		Results plotEpisuitePredictionsByCAS(String title, String propertyName, String units, 
				Hashtable<String, ModelPrediction> htMP_epi_by_CAS, boolean omitTraining, boolean createPlot,String outputFolder) {

			List<Double> exps = new ArrayList<>();
			List<Double> preds = new ArrayList<>();

			Results results = new Results();

			List<ModelPrediction> mps = new ArrayList<>();
			results.htModelPredictions = htMP_epi_by_CAS;
			results.modelPredictions=mps;

			int countInEpiTraining = 0;
			int countNoPred = 0;

			for (String CAS : htMP_epi_by_CAS.keySet()) {

				ModelPrediction mp=htMP_epi_by_CAS.get(CAS);
				
				if(mp.split==0 && omitTraining) {
					countInEpiTraining++;
					continue;
				}
				
				if (mp.pred.equals(Double.NaN) || mp.pred==null) {
					countNoPred++;
					continue;
				}
				mps.add(mp);
				preds.add(mp.pred);
				exps.add(mp.exp);
				
				if(mp.exp==null || mp.pred==null) {
					System.out.println(Utilities.gson.toJson(mp));
				}
				
//				mp.dtxcid=dpCAS.checkStructure.dsstox_dtxcid;//this might not correspond to what episuite smiles is 
			}
			
			Collections.sort(mps);
			
//			System.out.println(Utilities.gson.toJson(mps));
			
			if(mps.size()==0) {
				System.out.println(title);
				System.out.println("countInEpiTraining=" + countInEpiTraining);
				System.out.println("countNoPred=" + countNoPred);
				System.out.println("countPlotted=" + mps.size());
				return results;
			}
			

			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(mps,
					-9999.0, DevQsarConstants.TAG_TEST);

			modelTestStatisticValues.remove("Coverage_Test");
			modelTestStatisticValues.remove("Q2_Test");

			if(debug) {
				System.out.println("\n******\n"+title);
				System.out.println(Utilities.gson.toJson(modelTestStatisticValues));
				System.out.println("countInEpiTraining=" + countInEpiTraining);
				System.out.println("countNoPred=" + countNoPred);
				System.out.println("countPlotted=" + mps.size());
			}

			Double minVal = null;
			Double maxVal = null;

			for (ModelPrediction mp : mps) {
				if (minVal == null || mp.pred < minVal)
					minVal = mp.pred;
				if (maxVal == null || mp.pred > maxVal)
					maxVal = mp.pred;
			}

			if (propertyName.equals("BP") || propertyName.equals("MP")) {
				int num=50;
				minVal = (double) Math.round(minVal / num) * num - num;
				maxVal = (double) Math.round(maxVal / num) * num + num;
			} else {
				minVal = Math.floor(minVal) - 1;
				maxVal = Math.ceil(maxVal) + 1;
			}

			DecimalFormat df = new DecimalFormat("0.00");

			String subtitle = "RMSE="+df.format(modelTestStatisticValues.get("RMSE_Test")) + ", n=" + exps.size();

			if(createPlot)
				createPlot(null, title,subtitle, propertyName, units, exps, preds, modelTestStatisticValues, minVal, maxVal,outputFolder);

			results.minVal = minVal;
			results.maxVal = maxVal;
			results.modelTestStatisticValues=modelTestStatisticValues;
			return results;

		}

		
		
		
		public void createPlot(String filename, String title, String subtitle, String property, String units, List<Double> vals1, List<Double> vals2,
				Map<String, Double> modelTestStatisticValues, Double minVal, Double maxVal,String outputFolder) {
			double[] x = makeArray(vals1);
			double[] y = makeArray(vals2);

			List<Double>yeqx=Arrays.asList(minVal,maxVal);
			double[] yeqx2=makeArray(yeqx);

			gov.epa.util.MatlabChart fig = new MatlabChart(); // figure('Position',[100 100 640 480]);

			//		String bob="data ("+modelTestStatisticValues.get("PearsonRSQ_Test")+")";

			fig.plot(x, y, "-r", 2.0f, "data"); // plot(x,y1,'-r','LineWidth',2);
			fig.plot(yeqx2, yeqx2, "-k", 2.0f, "Y=X"); // plot(x,y1,'-r','LineWidth',2);


			// fig.plot(x, y2, ":k", 3.0f, "BAC"); // plot(x,y2,':k','LineWidth',3);

			fig.RenderPlot(); // First render plot before modifying
			fig.title(title); // title('Stock 1 vs. Stock 2');
			// fig.xlim(10, 100); // xlim([10 100]);
			// fig.ylim(200, 300); // ylim([200 300]);

			// TODO for some properties it wont be logged units in labels

			
			String titleX="Experimental " + property + " " + units;
			if(titleX.length()>30) titleX="Exp. " + property + " " + units;
			
			String titleY="Predicted " + property + " " + units;
			if(titleY.length()>30) titleY="Pred. " + property + " " + units;
			
			
			fig.xlabel(titleX); // xlabel('Days');
			fig.ylabel(titleY); // ylabel('Price');
			fig.grid("on", "on"); // grid on;
			//		fig.legend("southeast");             // legend('AAPL','BAC','Location','northeast')

			fig.legend("northwest");

			int fontSize=40;
			
			
			fig.font("Helvetica", fontSize); // .. 'FontName','Helvetica','FontSize',15
			// fig.saveas("MyPlot.jpeg",640,480); // saveas(gcf,'MyPlot','jpeg');


			XYLineAndShapeRenderer xy = (XYLineAndShapeRenderer) fig.chart.getXYPlot().getRenderer();

			xy.setSeriesShapesVisible(0, true);
			xy.setSeriesLinesVisible(0, false);
			xy.setSeriesOutlineStroke(0, null);

			xy.setUseOutlinePaint(true);
			xy.setSeriesOutlinePaint(0, Color.BLACK);

			xy.setSeriesShapesVisible(1, false);
			xy.setSeriesLinesVisible(1, true);

			if (minVal != null) {
				NumberAxis xAxis = (NumberAxis) fig.chart.getXYPlot().getDomainAxis();
				NumberAxis yAxis = (NumberAxis) fig.chart.getXYPlot().getRangeAxis();
				xAxis.setRange(minVal, maxVal);
				yAxis.setRange(minVal, maxVal);
			}


			//		XYTextAnnotation annotation = new XYTextAnnotation("Sample Label", 4.0, 5.0);
			//	        annotation.setFont(new Font("SansSerif", Font.PLAIN, 12));
			//	        annotation.setTextAnchor(TextAnchor.CENTER);
			//	    xy.addAnnotation(annotation);        

			
	        // Add the TextTitle to the chart
	        
//			TextTitle textTitle = new TextTitle(subtitle);
//	        textTitle.setPosition(RectangleEdge.BOTTOM);
//	        textTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
//	        textTitle.setVerticalAlignment(VerticalAlignment.BOTTOM);
//	        textTitle.setPadding(5, 5, 5, 5); // Optional padding
//			fig.chart.addSubtitle(textTitle);
			
			
			 XYTextAnnotation annotation = new XYTextAnnotation(subtitle, maxVal-1, minVal+1);
		     annotation.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
		     annotation.setTextAnchor(org.jfree.chart.ui.TextAnchor.BOTTOM_RIGHT);
		     xy.addAnnotation(annotation);
			
			ChartPanel cp = new ChartPanel(fig.chart);

			File imageFile=null;
			
			if(filename==null) {
				imageFile = new File(outputFolder+File.separator+title+".png");
			} else {
				imageFile = new File(outputFolder+File.separator+filename);	
			}
			
			
			
			int sizeSave=800;
			int sizeDisplay=800;
			
			// Save the chart as a PNG image
			try {
				ChartUtils.saveChartAsPNG(imageFile, fig.chart, sizeSave,sizeSave);
				System.out.println("Chart saved as PNG to " + imageFile.getAbsolutePath());
			} catch (IOException e) {
				System.err.println("Error saving chart to PNG: " + e.getMessage());
			}

			if(displayPlots) {
				JFrame jframe = new JFrame();
				jframe.add(cp);
				cp.setLayout(new FlowLayout(FlowLayout.LEFT));

				jframe.setSize(sizeDisplay,sizeDisplay);
				jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				jframe.setLocationRelativeTo(null);
				jframe.setVisible(true);
			}

		}

		private double[] makeArray(List<Double> vals1) {
			double[] x = new double[vals1.size()];
			int i = 0;
			for (Double val : vals1) {
				x[i++] = val;
			}
			return x;
		}


		public Results plotPredictions(String title, String propertyName, String units, Results resultsEpi,
				Hashtable<String, ModelPrediction> htMP, boolean createPlot, boolean omitChemicalsNotPredictedByEpisuite, String outputFolder) {
			List<Double> exps = new ArrayList<>();
			List<Double> preds = new ArrayList<>();

			Results results=new Results();
			results.htModelPredictions=htMP;

			List<ModelPrediction> mps = new ArrayList<>();
			results.modelPredictions=mps;

			int countInEpiTraining = 0;
			int countNoPred = 0;

			for (String key : resultsEpi.htModelPredictions.keySet()) {

				ModelPrediction mpEpi=resultsEpi.htModelPredictions.get(key);
				
				if(omitChemicalsNotPredictedByEpisuite) {
					if(mpEpi.split==0) {
						countInEpiTraining++;
						continue;
					}
					
					if (mpEpi.pred.equals(Double.NaN) || mpEpi.pred==null) {
						continue;
					}
				}

				if(!htMP.containsKey(key)) {
					continue;
				}
				
				ModelPrediction mp=htMP.get(key);
				
				if(mp.pred==null || mp.pred.isNaN()) {
					countNoPred++;
					continue;
				}
				
				mps.add(mp);
				preds.add(mp.pred);
				exps.add(mp.exp);
				
				if(mp.exp==null) {
					System.out.println(mp.id+"\tnull exp\t"+title);
				}
				
				                 
//				if(mp.id.equals("CCCC1C=CC(=CC=1)C1CCC(CCCCC)CC1")) {
//					System.out.println(Utilities.gson.toJson(mp));
//				}
				
				
//				if(mp.exp==null || mp.pred==null) {
//					System.out.println(Utilities.gson.toJson(mp));
//				}
				
//				mp.dtxcid=dpCAS.checkStructure.dsstox_dtxcid;//this might not correspond to what episuite smiles is 
			}


			Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(mps,
					-9999.0, DevQsarConstants.TAG_TEST);

			modelTestStatisticValues.remove("Coverage_Test");
			modelTestStatisticValues.remove("Q2_Test");

			if(debug) {
				System.out.println("\n***********\n"+title);
				System.out.println(Utilities.gson.toJson(modelTestStatisticValues));
				System.out.println("countInEpiTraining=" + countInEpiTraining);
				System.out.println("countNoPred=" + countNoPred);
				System.out.println("countPlotted=" + mps.size());
			}

			DecimalFormat df = new DecimalFormat("0.00");
			String subtitle = "RMSE=" + df.format(modelTestStatisticValues.get("RMSE_Test")) + ", n=" + exps.size();

			if(createPlot)
				createPlot(null, title,subtitle, propertyName, units, exps, preds, modelTestStatisticValues, resultsEpi.minVal,
						resultsEpi.maxVal,outputFolder);

			results.modelTestStatisticValues=modelTestStatisticValues;
			return results;

		}

	}


	class Opera {

		private HashSet<String> getOperaDtxcids(String filename) {

			String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\OPERA2.8\\OPERA_SDFS\\";
			String filepathInput = folder + filename;//TODO pass variable

			try {

				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepathInput),
						DefaultChemObjectBuilder.getInstance());

				int counter = 0;


				HashSet<String>hsDtxcids=new HashSet<>();

				while (mr.hasNext()) {
					counter++;
					IAtomContainer m = mr.next();

					String Tr_1_Tst_0=m.getProperty("Tr_1_Tst_0");

					//if present in opera training or test set, it will use exp value as the predicted value
					//So in order to get external predictions from opera, need to exclude both- so dont just use training ones

					//				if(Tr_1_Tst_0.equals("0")) {
					//					continue;
					//				}

					String dtxcid=m.getProperty("dsstox_compound_id");
					hsDtxcids.add(dtxcid);
					//				System.out.println(CAS+"\t"+Train);
				}

				mr.close();
				return hsDtxcids;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}
		
		private HashSet<String> getOperaInchiKey1s(String filename) {

			String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\OPERA2.8\\OPERA_SDFS\\";
			String filepathInput = folder + filename;//TODO pass variable

			try {

				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepathInput),
						DefaultChemObjectBuilder.getInstance());

				int counter = 0;


				HashSet<String>hsInchikey1s=new HashSet<>();

				while (mr.hasNext()) {
					counter++;
					IAtomContainer m = mr.next();

					String Tr_1_Tst_0=m.getProperty("Tr_1_Tst_0");

					//if present in opera training or test set, it will use exp value as the predicted value
					//So in order to get external predictions from opera, need to exclude both- so dont just use training ones
					//				if(Tr_1_Tst_0.equals("0")) {
					//					continue;
					//				}

					String dtxcid=m.getProperty("dsstox_compound_id");
					String originalSmiles=m.getProperty("Original_SMILES");
					String qsarSmiles=m.getProperty("Canonical_QSARr");
					
					
					String inchiKey1=StructureUtil.indigoInchikey1FromSmiles(originalSmiles);
//					String inchiKey1=StructureUtil.indigoInchikey1FromSmiles(qsarSmiles);
					hsInchikey1s.add(inchiKey1);
					//				System.out.println(CAS+"\t"+Train);
				}

				mr.close();
				return hsInchikey1s;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}

		private Hashtable<String,ModelPrediction> getOperaModelPredictionsFromResQsar(String propertyName, Hashtable<String, DataPoint> htDP, long fk_dataset_id) {

			String modelNameCCD=null;
			if (propertyName.equals("LogKow")) modelNameCCD="OPERA_LogP";
			else if (propertyName.equals("HLC")) modelNameCCD="OPERA_HL";
			else  modelNameCCD="OPERA_"+propertyName;

			String sql="select id from qsar_models.models where fk_source_id=6 and name_ccd='"+modelNameCCD+"';";
			long modelId=Long.parseLong(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql));

			//		if(debug) System.out.println("modelIdPercepta="+modelIdPercepta);


			try {

				Hashtable<String, String> htDtxcidToSmiles = du.getDtxcidToQsarSmilesHashtableForDataset(fk_dataset_id);
//				System.out.println("htDtxcidToSmiles.size()="+htDtxcidToSmiles.size());

				String strDtxcids = "(";
				for (String dtxcid : htDtxcidToSmiles.keySet()) {
					strDtxcids += "'" + dtxcid + "',";
				}
				strDtxcids += ")";
				strDtxcids = strDtxcids.replace(",)", ")");


				//			System.out.println(strDtxcids);

				sql = "select distinct dr.dtxcid,pd.prediction_value from qsar_models.predictions_dashboard pd\r\n"
						+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
						+ "join qsar_models.models m on pd.fk_model_id = m.id\r\n" + "where m.id=" + modelId
						+ " and dr.fk_dsstox_snapshot_id=2\r\n" + "and dr.dtxcid in " + strDtxcids
						+ " and pd.prediction_value is not null;";

				//			System.out.println(sql);

				ResultSet rs = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

				Hashtable<String, Double> htPred = new Hashtable<>();

				HashSet <String>hsDtxcidsTraining=getOperaDtxcids("LogP_QR.sdf");//seems to work better than trying to generate our own qsar smiles and mapping that way

				Hashtable<String,ModelPrediction>htMPs=new Hashtable<>();

				while (rs.next()) {

					String dtxcid = rs.getString(1);
					Double pred = rs.getDouble(2);

					String qsarSmiles = htDtxcidToSmiles.get(dtxcid);

					if (propertyName.equals("VP")) {
						pred = Math.log10(pred);
					} else if (propertyName.equals("WS")) {
						pred = -Math.log10(pred);
					}

					//				System.out.println(dtxcid+"\t"+qsarSmiles+"\t"+prediction);


					Double exp=htDP.get(qsarSmiles).getQsarPropertyValue();

					Integer splitNum=1;
					if(hsDtxcidsTraining.contains(dtxcid)) splitNum=0; 

					ModelPrediction mp=new ModelPrediction(qsarSmiles, exp, pred, splitNum);
					htMPs.put(qsarSmiles, mp);
//					mp.dtxcid=dtxcid;

					//				System.out.println(dtxcid+"\t"+prediction);
				}


				return htMPs;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}		
		}
		
		private Hashtable<String,ModelPrediction> getOperaModelPredictionsFromOutputFile(String of, String propertyName, String set, Hashtable<String, DataPoint> htDP_by_qsar_smiles) {


			try {

				Hashtable<String,ModelPrediction>htMPs=new Hashtable<>();

				String propertyNameOpera=propertyName;
				if(propertyName.equals("LogKow"))propertyNameOpera="LogP";
				
				String filename=propertyName+" "+set+" set OPERA.csv";
				String outputFilePath=of+File.separator+filename;
				
//				System.out.println(new File(outputFilePath).exists());
				
				
				InputStream inputStream= new FileInputStream(outputFilePath);
				BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
				String csvAsString = br.lines().collect(Collectors.joining("\n"));
//				System.out.println(csvAsString);
				

				String json = CDL.toJSONArray(csvAsString).toString();
				JsonArray ja=Utilities.gson.fromJson(json, JsonArray.class);
				
				Hashtable<String,JsonObject>htPredObjectFromMoleculeID=new Hashtable<>();
				
				for (JsonElement element : ja) {
		            if (element.isJsonObject()) {
		                JsonObject jsonObject = element.getAsJsonObject();
		                String id = jsonObject.get("MoleculeID").getAsString();
		                htPredObjectFromMoleculeID.put(id, jsonObject);
		            }
		        }
				
//				System.out.println(Utilities.gson.toJson(htPredObjectFromMoleculeID));
				
				String filepathSDF=of+File.separator+propertyName+" "+set+" set dsstox smiles.sdf";//for running percepta
				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepathSDF),
						DefaultChemObjectBuilder.getInstance());
				
				int counter = 1;
				
				
				HashSet <String>hsDtxcids=getOperaDtxcids(propertyNameOpera+"_QR.sdf");//seems to work better than trying to generate our own qsar smiles and mapping that way
				HashSet <String>hsInchiKey1s=getOperaInchiKey1s(propertyNameOpera+"_QR.sdf");//seems to work better than trying to generate our own qsar smiles and mapping that way

				int countByInchikey1notdtxcid=0;
				int countByDTXCIDnotbyInchikey1=0;
				int countByInchikey1=0;
				int countByDTXCID=0;
				int countByEither=0;
				
				while (mr.hasNext()) {
					IAtomContainer m = mr.next();
					String qsarSmiles=m.getProperty("qsarSmiles");
					String originalSmiles=m.getProperty("SMILES");
					String dtxcid=m.getProperty("dtxcid");
					
					String moleculeID="Molecule_"+counter;
					
					if(!htDP_by_qsar_smiles.containsKey(qsarSmiles)) {
						System.out.println("No exp entry for "+qsarSmiles);
						continue;
					}
					
					Double exp=htDP_by_qsar_smiles.get(qsarSmiles).getQsarPropertyValue();
					
					Double pred=null;
					
					if(htPredObjectFromMoleculeID.containsKey(moleculeID)) {
						JsonObject joPred=htPredObjectFromMoleculeID.get(moleculeID).getAsJsonObject();
						
						String fieldName=propertyNameOpera+"_pred";
						if(propertyNameOpera.equals("WS")) fieldName="LogWS_pred";
						
						if(joPred.get(fieldName)!=null) {
							pred=joPred.get(fieldName).getAsDouble();
							if(propertyNameOpera.equals("WS"))pred=-pred;//convert to -Log10(M)
						} else {
							System.out.println("pred missing:"+Utilities.gson.toJson(joPred));
						}
					}
					
					String inchiKey1=StructureUtil.indigoInchikey1FromSmiles(originalSmiles);

					
					if(hsInchiKey1s.contains(inchiKey1)) countByInchikey1++;
					if(hsDtxcids.contains(dtxcid)) countByDTXCID++;
					if(!hsDtxcids.contains(dtxcid) && hsInchiKey1s.contains(inchiKey1)) {
						countByInchikey1notdtxcid++;
//						System.out.println(countByInchikey1notdtxcid+"\t"+dtxcid+"\tmatch by inchiKey1 but not dtxcid");
					}
					if(hsDtxcids.contains(dtxcid) && !hsInchiKey1s.contains(inchiKey1)) {
						countByDTXCIDnotbyInchikey1++;
//						System.out.println(countByDTXCIDnotbyInchikey1+"\t"+dtxcid+"\t"+originalSmiles+"\tmatch by dtxcid but not by inchiKey1");
					}
					
					Integer splitNum=null;
					
					if(hsInchiKey1s.contains(inchiKey1)) {
						splitNum=0;
					} else {
						splitNum=1;
					}
					
					ModelPrediction mp=new ModelPrediction(qsarSmiles, exp, pred, splitNum);
					htMPs.put(qsarSmiles, mp);
					counter++;
//					System.out.println(moleculeID+"\t"+Utilities.gson.toJson(mp));
				}
				
//				System.out.println("countByInchikey1notdtxcid="+countByInchikey1notdtxcid);
//				System.out.println("countByDTXCIDnotbyInchikey1="+countByDTXCIDnotbyInchikey1);
//				System.out.println("countByInchikey1="+countByInchikey1);
//				System.out.println("countByDTXCID="+countByDTXCID);
//				System.out.println(Utilities.gson.toJson(htMPs));

				return htMPs;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}		
		}


		@Deprecated
		private HashSet<String> getOperaTrainingSmiles() {

			HashSet<String> hsSmiles = new HashSet<>();

			try {
				String filepath = "data\\episuite\\episuite validation\\LogKow\\LogKow opera training set smiles.txt";

				BufferedReader br = new BufferedReader(new FileReader(filepath));
				br.readLine();
				while (true) {
					String Line = br.readLine();
					if (Line == null)
						break;

					hsSmiles.add(Line);
				}
				br.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return hsSmiles;
		}

		/**
		 * TODO is it better to get the dtxsids instead of generating qsar smiles? 
		 * @param filename
		 */
		@Deprecated
		private void getOperaTrainingSmilesFile(String filename,String workflow) {

			String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\OPERA2.8\\OPERA_SDFS\\";
			String filepathInput = folder + filename;//TODO pass variable

			try {


				String filepathOut = "data\\episuite\\episuite validation\\LogKow\\LogKow opera training set smiles.txt";

				FileWriter fw = new FileWriter(filepathOut);


				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepathInput),
						DefaultChemObjectBuilder.getInstance());


				int counter = 0;

				//			DescriptorFactory df=new DescriptorFactory(false);

//				String workflow = "qsar-ready";
//				String serverHost = "https://hcd.rtpnc.epa.gov";
//				SciDataExpertsStandardizer standardizer = new SciDataExpertsStandardizer(workflow, serverHost);

				boolean useFullStandardize=false;

				SmilesGenerator sg= new SmilesGenerator(SmiFlavor.Unique);

				HashSet<String>hsSmiles=new HashSet<>();

				while (mr.hasNext()) {
					counter++;
					IAtomContainer m = mr.next();


					String Tr_1_Tst_0=m.getProperty("Tr_1_Tst_0");;
					if(Tr_1_Tst_0.equals("0")) {
						continue;
					}


					String Canonical_QSARr=m.getProperty("Canonical_QSARr");//TODO we could just generate the qsar ready smiles from the smiles to be exact
					String CAS=m.getProperty("CAS");

					String Smiles=null;

					try {
						Smiles=sg.create(m);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					if(Smiles==null) {
						System.out.println("Cant generate smiles for "+CAS);
						continue;
					}


					String qsarSmiles = du.standardize(Smiles);
					
					if(qsarSmiles==null) {
						System.out.println("Cant generate qsarSmiles for "+Smiles);
						continue;
					}


					System.out.println(counter+"\t"+Canonical_QSARr+"\t"+qsarSmiles);

					//				System.out.println(Canonical_QSARr+"\t"+Tr_1_Tst_0);

					fw.write(qsarSmiles + "\r\n");
					fw.flush();


					//				System.out.println(CAS+"\t"+Train);
				}

				System.out.println(hsSmiles.size());

				mr.close();
				fw.close();


			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

	class CheminformaticsModules {

		
		Hashtable<String, ModelPrediction> getResQsarModelPredictionsByCAS(String of,String filepathEpisuiteResultsTestSet,long modelId,Hashtable<String, DataPoint> htDP_CAS, boolean calculateAD,String workflow) {


			
			Hashtable<String, String>htCAS_to_episuite_smiles=episuite.getEpisuiteSmilesByCAS(filepathEpisuiteResultsTestSet);

//			System.out.println(Utilities.gson.toJson(htDP_CAS));
//			System.out.println(Utilities.gson.toJson(htCAS_to_episuite_smiles));
			
			if(debug)
				System.out.println("smilesByCAS.size()="+htCAS_to_episuite_smiles.size());
			//		System.out.println(Utilities.gson.toJson(smilesByCAS));
			
			LinkedHashMap<String, EpisuiteSmilesToQsarSmiles>htCAStoEpiQsarSmiles=du.getQsarSmilesHashtable(of+File.separator+"episuite smiles to qsar smiles "+workflow+".json",htCAS_to_episuite_smiles,workflow);
//			System.out.println(Utilities.gson.toJson(htCAStoEpiQsarSmiles));

			
//			System.out.println(Utilities.gson.toJson(htDP_CAS.get("741250-20-4").checkStructure));
//			System.out.println(htCAStoEpiQsarSmiles.get("741250-20-4").qsarSmiles);
			
			LinkedHashMap<String, DescriptorsByCAS>htDescByQsarSmiles=du.getDescriptorsHashtable(of+File.separator+"episuite qsarSmiles to descriptors.json", htCAStoEpiQsarSmiles);			
			
			String predictionTsv=du.getPredictionTsvByCAS(htDP_CAS,htDescByQsarSmiles,htCAStoEpiQsarSmiles,htCAS_to_episuite_smiles);
//					System.out.println(predictionTsv);
			
//			Hashtable<String, Double> htPredResQsar = cm.getPredictions(modelId,predictionTsv);
//			System.out.println(predictionTsv);
			
			List<ModelPrediction> modelPredictions = ps.run(modelId, predictionTsv);
			Hashtable<String, ModelPrediction> htModelPredictions = new Hashtable<>();
			
//			CC=C(C)C(=O)OC1CC(C2(COC3C2C41COC(C4C(C)(C3O)C56C7CC(C5(C)O6)C8(C=COC8O7)O)(C(=O)OC)O)C(=O)OC)OC(=O)C
			
			
			for(ModelPrediction mp:modelPredictions) {
				htModelPredictions.put(mp.id, mp);
				mp.split=1;
				
				DataPoint dp=htDP_CAS.get(mp.id);
				mp.checkStructure=dp.checkStructure;
				
//				System.out.println(htCAS_to_smiles.get(mp.id));
//				System.out.println(htEpismilesToQsarSmiles.get(htCAS_to_smiles.get(mp.id)).qsarSmiles);
				
				if(htCAS_to_episuite_smiles.containsKey(mp.id) && htCAStoEpiQsarSmiles.containsKey(mp.id)) {
					mp.checkStructure.episuite_smiles=htCAS_to_episuite_smiles.get(mp.id);
					mp.checkStructure.episuite_canon_qsar_smiles=htCAStoEpiQsarSmiles.get(mp.id).qsarSmiles;
				} else {
					System.out.println("Missing smiles entries for "+mp.id);
				}
			}
			
//			System.out.println(Utilities.gson.toJson(htModelPredictions));

			if(calculateAD) {
				Hashtable<String, ApplicabilityDomainPrediction> htAD = getAD_Hashtable_prediction_set(modelId, applicability_domain, predictionTsv);
//				System.out.println(Utilities.gson.toJson(htAD));
				assignAD(htModelPredictions, htAD);
			}

			
//			System.out.println(Utilities.gson.toJson(htModelPredictions));
			return htModelPredictions;
		}


		private void assignAD(Hashtable<String, ModelPrediction> htModelPredictions,
				Hashtable<String, ApplicabilityDomainPrediction> htAD) {
			//Assign AD from hashtable;
			for(String key:htModelPredictions.keySet()) {
				ModelPrediction mp=htModelPredictions.get(key);
				if(!htAD.containsKey(key)) continue;
				mp.insideAD=htAD.get(key).AD;
			}
		}


		

		Hashtable<String, ModelPrediction> getResQsarModelPredictions(long modelId, boolean calculateAD,Hashtable<String, CheckStructure> htCS_by_DP_QsarSmiles,String applicabilityDomain) {

			//Get res_qsar model id that matches the datasetName that uses an embedding:
			//		System.out.println("ResQsar modelId="+fk_model_id);

			Hashtable<String, ModelPrediction> htModelPredictions = new Hashtable<>();

			String sql = "select distinct p.canon_qsar_smiles,dp.qsar_dtxcid, dp.qsar_property_value,qsar_predicted_value from qsar_models.predictions p\r\n"
					+ "		join qsar_models.models m on p.fk_model_id = m.id\r\n"
					+ "		join qsar_datasets.datasets d on d.name=m.dataset_name\r\n"
					+ "		join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id and dp.canon_qsar_smiles=p.canon_qsar_smiles\r\n"
					+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
					+ "		join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id\r\n"
					+ "		where  p.fk_model_id=" + modelId
					+ " and p.fk_splitting_id=1 and dpis.fk_splitting_id=1 and split_num=1;";
			//		System.out.println(sql);

			try {

				ResultSet rs = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

				
//				System.out.println(Utilities.gson.toJson(htCS_by_DP_QsarSmiles));
				
				while (rs.next()) {
					String qsarSmiles = rs.getString(1);
					String dtxcid = rs.getString(2);
					Double exp = rs.getDouble(3);
					Double pred = rs.getDouble(4);

					ModelPrediction mp=new ModelPrediction(qsarSmiles, exp, pred, 1, null);
//					mp.dtxcid=dtxcid;
					htModelPredictions.put(qsarSmiles, mp);
					
					if(htCS_by_DP_QsarSmiles.containsKey(qsarSmiles)) {
						CheckStructure cs=htCS_by_DP_QsarSmiles.get(qsarSmiles);
						mp.checkStructure=cs;
					}
					
				}

				if(calculateAD) {
					Hashtable<String, ApplicabilityDomainPrediction> htAD = getAD_Hashtable_prediction_set(modelId, applicability_domain);
					assignAD(htModelPredictions, htAD);
				}
				//			System.out.println(Utilities.gson.toJson(htModelPredictions));
				return htModelPredictions;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;

		}
		
		
//		Hashtable<String,Double> getExternalPredictions(long modelId,
//				long fk_dataset_id_external,long fk_dataset_id,
//				boolean omitOnlyTrainingFromOurDataset) {
//
//			List<ModelPrediction> modelPredictions = ps.runExternalSet(modelId, fk_dataset_id_external,
//					fk_dataset_id,omitOnlyTrainingFromOurDataset);
//
//			Hashtable<String,Double>htPredsCMext=new Hashtable<>();
//			for (ModelPrediction mp:modelPredictions) {
//				htPredsCMext.put(mp.id, mp.pred);
//			}
//
//			return htPredsCMext;
//
//		}
		
		
		Hashtable<String,ModelPrediction> getExternalModelPredictions(long modelId,
				long fk_dataset_id_external,long fk_dataset_id,
				boolean omitOnlyTrainingFromOurDataset) {

			List<ModelPrediction> modelPredictions = ps.runExternalSet(modelId, fk_dataset_id_external,
					fk_dataset_id,omitOnlyTrainingFromOurDataset);

			Hashtable<String,ModelPrediction>htPredsCMext=new Hashtable<>();
			for (ModelPrediction mp:modelPredictions) {
				htPredsCMext.put(mp.id, mp);
			}

			return htPredsCMext;

		}



		Hashtable<String, ModelPrediction> getResQsarModelPredictionsRevisedDsstoxSmiles(String of, String propertyName, long modelId, boolean calculateAD, Hashtable<String, DataPoint> htDP_by_dsstox_qsar_smiles) {

			try {
				
				LinkedHashMap<String, DescriptorsByCAS>htDescByQsarSmiles=du.getDescriptorsHashtableByQsarSmiles(of+File.separator+"dsstox qsarSmiles to descriptors.json", htDP_by_dsstox_qsar_smiles.keySet());
				String predictionTsv=du.getPredictionTsv2(htDP_by_dsstox_qsar_smiles,htDescByQsarSmiles);
				List<ModelPrediction> modelPredictions = ps.run(modelId, predictionTsv);

				Hashtable<String, ModelPrediction> htModelPredictions = new Hashtable<>();
				for(ModelPrediction mp:modelPredictions) {
					DataPoint dp=htDP_by_dsstox_qsar_smiles.get(mp.id);
					mp.checkStructure=dp.checkStructure;
					htModelPredictions.put(mp.id, mp);
					mp.split=1;
				}

				if(calculateAD) {
//					System.out.println(applicability_domain);
					//			String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;
					Hashtable<String, ApplicabilityDomainPrediction> htAD = getAD_Hashtable_prediction_set(modelId, applicability_domain,predictionTsv);
					assignAD(htModelPredictions, htAD);
				}
				//			System.out.println(Utilities.gson.toJson(htModelPredictions));
				return htModelPredictions;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;

		}

		Hashtable<String, Double> getResQsarPredictions(String propertyName, long fk_model_id) {

			Hashtable<String, Double> htPred = new Hashtable<>();

			String sql = "select distinct p.canon_qsar_smiles,dp.qsar_dtxcid, dp.qsar_property_value,qsar_predicted_value from qsar_models.predictions p\r\n"
					+ "		join qsar_models.models m on p.fk_model_id = m.id\r\n"
					+ "		join qsar_datasets.datasets d on d.name=m.dataset_name\r\n"
					+ "		join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id and dp.canon_qsar_smiles=p.canon_qsar_smiles\r\n"
					+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
					+ "		join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id\r\n"
					+ "		where  p.fk_model_id=" + fk_model_id
					+ " and p.fk_splitting_id=1 and dpis.fk_splitting_id=1 and split_num=1;";
			//		System.out.println(sql);

			try {

				ResultSet rs = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

				while (rs.next()) {
					String qsarSmiles = rs.getString(1);
					String dtxcid = rs.getString(2);
					Double exp = rs.getDouble(3);
					Double pred = rs.getDouble(4);
					htPred.put(qsarSmiles, pred);
				}
				return htPred;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;

		}

		Hashtable<String, Double> getResQsarPredictions(String propertyName, String datasetName) {


			String sqlModelId = "select * from qsar_models.models where dataset_name like '" + datasetName
					+ "' and splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is not null;";
			String strModelId = SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlModelId);
			Long fk_model_id = Long.parseLong(strModelId);

			//		System.out.println("fk_model_id="+fk_model_id);


			Hashtable<String, Double> htPred = new Hashtable<>();

			String sql = "select distinct p.canon_qsar_smiles,dp.qsar_dtxcid, dp.qsar_property_value,qsar_predicted_value from qsar_models.predictions p\r\n"
					+ "		join qsar_models.models m on p.fk_model_id = m.id\r\n"
					+ "		join qsar_datasets.datasets d on d.name=m.dataset_name\r\n"
					+ "		join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id and dp.canon_qsar_smiles=p.canon_qsar_smiles\r\n"
					+ "		join qsar_datasets.data_points_in_splittings dpis on dp.id = dpis.fk_data_point_id\r\n"
					+ "		join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id\r\n"
					+ "		where  p.fk_model_id=" + fk_model_id
					+ " and p.fk_splitting_id=1 and dpis.fk_splitting_id=1 and split_num=1;";
			//		System.out.println(sql);

			try {

				ResultSet rs = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

				while (rs.next()) {
					String qsarSmiles = rs.getString(1);
					String dtxcid = rs.getString(2);
					Double exp = rs.getDouble(3);
					Double pred = rs.getDouble(4);
					htPred.put(qsarSmiles, pred);
				}
				return htPred;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;

		}

		/**
		 * TODO is this still being used?
		 * 
		 * @param endpoint
		 * @param createPlot
		 * @param omitChemicalsNotPredictedByEpisuite
		 * @param units
		 * @param datasetName
		 * @param htDP
		 */
//		private void getResQsarResults(String propertyName, boolean createPlot, boolean omitChemicalsNotPredictedByEpisuite,
//				String units, String datasetName, Hashtable<String, DataPoint> htDP,String outputFolder) {
//			Results results;
//			String sqlModelId = "select * from qsar_models.models where dataset_name like '" + datasetName
//					+ "' and splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is not null;";
//			String strModelId = SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlModelId);
//			Long modelId = Long.parseLong(strModelId);
//
//			if(debug)
//				System.out.println("ResQsar modelID=" + strModelId);
//
//			Hashtable<String, Double> htPredResQsar = getResQsarPredictions(propertyName, modelId);
//
//			results=plot.plotPredictions(propertyName+" WebTest2.0",propertyName, units, null, htDP, htPredResQsar,createPlot,omitChemicalsNotPredictedByEpisuite,outputFolder);
//		}



		private Hashtable<String, ApplicabilityDomainPrediction> getAD_Hashtable_prediction_set(Long fk_model_id,
				String applicability_domain) {
			String strResponse=null;

			ModelService modelService=new ModelServiceImpl();
			Model model=modelService.findById(fk_model_id);

			CalculationInfo ci = new CalculationInfo();
			ci.remove_log_p = false;
			ci.qsarMethodEmbedding = null;//TODO
			ci.datasetName=model.getDatasetName();
			ci.descriptorSetName=model.getDescriptorSetName();
			ci.splittingName=model.getSplittingName();

			ModelData data = ModelData.initModelData(ci,false);


			ModelWebService modelWebService = new ModelWebService(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

			if (model.getDescriptorEmbedding()!=null) {
				strResponse=modelWebService.callPredictionApplicabilityDomain(data.trainingSetInstances,data.predictionSetInstances,
						false,model.getDescriptorEmbedding().getEmbeddingTsv(),applicability_domain).getBody();
			} else {
				strResponse=modelWebService.callPredictionApplicabilityDomain(data.trainingSetInstances,data.predictionSetInstances,
						false,applicability_domain).getBody();
			}

			boolean storeNeighbors=false;
			Hashtable<String, ApplicabilityDomainPrediction>htAD =  ApplicabilityDomainScript.convertResponse(strResponse,storeNeighbors);
			return htAD;
		}
		
		
		private Hashtable<String, ApplicabilityDomainPrediction> getAD_Hashtable_prediction_set(Long fk_model_id,
				String applicability_domain,String predictionTsv) {
			String strResponse=null;

			ModelService modelService=new ModelServiceImpl();
			Model model=modelService.findById(fk_model_id);

			CalculationInfo ci = new CalculationInfo();
			ci.remove_log_p = false;
			ci.qsarMethodEmbedding = null;//TODO
			ci.datasetName=model.getDatasetName();
			ci.descriptorSetName=model.getDescriptorSetName();
			ci.splittingName=model.getSplittingName();

			ModelData data = ModelData.initModelData(ci,false);


			ModelWebService modelWebService = new ModelWebService(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

			if (model.getDescriptorEmbedding()!=null) {
				strResponse=modelWebService.callPredictionApplicabilityDomain(data.trainingSetInstances, predictionTsv,
						false,model.getDescriptorEmbedding().getEmbeddingTsv(),applicability_domain).getBody();
			} else {
				strResponse=modelWebService.callPredictionApplicabilityDomain(data.trainingSetInstances, predictionTsv,
						false,applicability_domain).getBody();
			}

			boolean storeNeighbors=false;
			Hashtable<String, ApplicabilityDomainPrediction>htAD =  ApplicabilityDomainScript.convertResponse(strResponse,storeNeighbors);
			return htAD;
		}


		private String writeBadPredictionTable(String propertyName, List<ModelPrediction> mps, LinkedHashMap<String, EpisuiteSmilesToQsarSmiles> htEpismilesToQsarSmiles)
				throws IOException, CDKException {

			StringWriter sw = new StringWriter();

			sw.write("<html><table border=1 cellspacing=0 cellpadding=10><caption>Worst ResQsar predictions for "
					+ propertyName + " using episuite structures from CAS</caption>");

			sw.write("<tr bgcolor=lightgray>" + "<th>Results by QsarSmiles</th></tr>");

			int counter = 0;

			DecimalFormat df = new DecimalFormat("0.00");

			Collections.sort(mps);


			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

			for (ModelPrediction mp : mps) {

				//						if(mp.absError()<1.5) break;

				counter++;

				// System.out.println(key+"\n"+Utilities.gson.toJson(tmPreds.get(key)));

				sw.write("<tr>");

				String smiles=htEpismilesToQsarSmiles.get(mp.id).qsarSmiles;
				IAtomContainer ac=sp.parseSmiles(smiles);
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(ac);
				if(!have5BondedNitrogen(mp, ac,smiles)) continue;

				String imgSrc=null;
				try {
					imgSrc = StructureImageUtil.generateImgSrc(smiles);
				} catch (Exception e) {
					e.printStackTrace();
				} 
				//						double MW = StructureUtil.molWeightFromSmiles(smiles);

				sw.write("<td><img src=\"" + imgSrc + "\" width=300><br>" +  
						mp.id + "<br>" +
						smiles + "<br>" + 
						"exp="+ mp.exp + "<br>" + "pred=" + df.format(mp.pred) + "<br>" + 
						"error ="+ df.format(mp.absError()) 
						+"</td>\n");		
				sw.write("</tr>");

				// if(counter==10) break;
				//				if (Math.abs(key) < 3)
				//					break;

			}

			sw.write("</table></html>");
			return sw.toString();
		}
		
		
		private String writeBadPredictions(String set, String of, String propertyName, String source1,String source2, Results results1,Results results2,Hashtable<String, String>smilesByCAS,LinkedHashMap<String, EpisuiteSmilesToQsarSmiles>htEpismilesToQsarSmiles) {
			

			StringWriter sw = new StringWriter();

			sw.write("<html><table border=1 cellspacing=0 cellpadding=10><caption>Worst "+source1+" predictions for "
					+ propertyName + "</caption>");

			sw.write("<tr bgcolor=lightgray>" + "<th>Chemical</th><th>Results</th></tr>");

			int counter = 0;

			DecimalFormat df = new DecimalFormat("0.00");
			
			List<ModelPrediction>mps=results1.modelPredictions;
			Hashtable<String,ModelPrediction>htModelPredictions=results2.htModelPredictions;
			
			Collections.sort(mps);


			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

			for (ModelPrediction mp1 : mps) {

				if(mp1.absError()<1.5) break;

				counter++;

				// System.out.println(key+"\n"+Utilities.gson.toJson(tmPreds.get(key)));

				sw.write("<tr>");

				String imgSrc=null;
				
				String smiles=null;
				
				if(htEpismilesToQsarSmiles!=null) {
					smiles=htEpismilesToQsarSmiles.get(mp1.id).qsarSmiles;
				} else {
					smiles=smilesByCAS.get(mp1.id);
				}
				
				IAtomContainer ac;
				
				try {
					ac = sp.parseSmiles(smiles);
//					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(ac);
//					if(!have5BondedNitrogen(mp, ac)) continue;
					imgSrc = StructureImageUtil.generateImgSrc(smiles);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//						double MW = StructureUtil.molWeightFromSmiles(smiles);

				
				String smiles2=episuite.addCarriageReturns(smiles, 50);
				
//				System.out.println(smiles2+"\n");
				
				if(htModelPredictions.containsKey(mp1.id)) {
					ModelPrediction mp2=htModelPredictions.get(mp1.id);
					if(mp1.absError()<mp2.absError()) continue;
				}
				
				sw.write("<td><img src=\"" + imgSrc + "\" width=300><br>" +  
						mp1.id + "<br>" +
						smiles2+"</td>\n");		
				

				ModelPrediction mp2=null;
				
				if(htModelPredictions.containsKey(mp1.id)) {
					mp2=htModelPredictions.get(mp1.id);
				}
				
				sw.write(utils.writePredComparisonTable(source1, source2, df, mp1,mp2));
				
//				"exp="+ mp.exp + "<br>" + "pred=" + df.format(mp.pred) + "<br>" + 
//				"error ="+ df.format(mp.absError()) 

				// if(counter==10) break;
				//				if (Math.abs(key) < 3)
				//					break;
				
				sw.write("</tr>");


			}

			sw.write("</table></html>");
			
			try {
				FileWriter fw = new FileWriter(of + File.separator + propertyName +" "+set+ " bad "+source1+" predictions.html");
				Document doc = Jsoup.parse(sw.toString());
				doc.outputSettings().indentAmount(2);
				fw.write(doc.toString());
				fw.flush();
				fw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			return sw.toString();
		}


		private String writeBadPredictions(String set, String of, String propertyName, String source1,String source2, Results results1,Results results2) {
			

			StringWriter sw = new StringWriter();

			sw.write("<html><table border=1 cellspacing=0 cellpadding=10><caption>Worst "+source1+" predictions for "
					+ propertyName + "</caption>");

			sw.write("<tr bgcolor=lightgray>" + "<th>Chemical</th><th>Results</th></tr>");

			int counter = 0;

			DecimalFormat df = new DecimalFormat("0.00");
			
			List<ModelPrediction>mps=results1.modelPredictions;
			Hashtable<String,ModelPrediction>htModelPredictions=results2.htModelPredictions;
			
			Collections.sort(mps);


			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

			for (ModelPrediction mp1 : mps) {

				if(mp1.absError()<1) break;

				counter++;

				// System.out.println(key+"\n"+Utilities.gson.toJson(tmPreds.get(key)));

				sw.write("<tr>");

				String imgSrc=null;

//				String smiles=mp1.id;
//				
//				if(mp1.checkStructure!=null) {
//					if(source1.contains("EpiSuite") ||  source1.contains("Percepta")) {
//						if(mp1.checkStructure.episuite_smiles!=null) {
//							smiles=mp1.checkStructure.episuite_smiles;	
////							System.out.println(smiles);
//						}
//					}
//					
//					if(source1.contains("TEST")&& mp1.checkStructure.episuite_canon_qsar_smiles!=null) {
//						smiles=mp1.checkStructure.episuite_canon_qsar_smiles;
//					}
//				}
				
				String smiles=mp1.id;
				if(mp1.checkStructure!=null) {
					if(source1.contains("EpiSuite") ||  source1.contains("Percepta")) {
						if(mp1.checkStructure.dsstox_smiles!=null) {
							smiles=mp1.checkStructure.dsstox_smiles;	
//							System.out.println(smiles);
						}
					}
				}
				
				if(mp1.id.equals("CCCCCCCCCC(O)CCCCCCCC(O)=O")) {
					System.out.println(Utilities.gson.toJson(mp1));
				}
				
				IAtomContainer ac;
				
				try {
					ac = sp.parseSmiles(smiles);
//					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(ac);
//					if(!have5BondedNitrogen(mp, ac)) continue;
					imgSrc = StructureImageUtil.generateImgSrc(smiles);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//						double MW = StructureUtil.molWeightFromSmiles(smiles);

				String smiles2=episuite.addCarriageReturns(smiles, 50);
				
//				System.out.println(smiles2+"\n");
				
				ModelPrediction mp2=null;
				
				if(htModelPredictions.containsKey(mp1.id)) {
					mp2=htModelPredictions.get(mp1.id);
//					if(mp1.absError()<mp2.absError()) continue;
				}
				
				if(imgSrc==null) {
					sw.write("<td>No image<br>"+smiles2+"</td>\n");		
				} else {
					sw.write("<td><img src=\""+imgSrc+"\" width=300><br>"+smiles2+"</td>\n");		
				}
				
				sw.write("<td>"+utils.writePredComparisonTable(source1, source2, df, mp1,mp2)+"</td>");
												
//				"exp="+ mp.exp + "<br>" + "pred=" + df.format(mp.pred) + "<br>" + 
//				"error ="+ df.format(mp.absError()) 

				// if(counter==10) break;
				//				if (Math.abs(key) < 3)
				//					break;
				
				sw.write("</tr>");


			}

			sw.write("</table></html>");
			
			try {
				FileWriter fw = new FileWriter(of + File.separator + propertyName+ " "+set + " bad "+source1+" vs "+source2+".html");
				Document doc = Jsoup.parse(sw.toString());
				doc.outputSettings().indentAmount(2);
				fw.write(doc.toString());
				fw.flush();
				fw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			return sw.toString();
		}

		private boolean have5BondedNitrogen(ModelPrediction mp, IAtomContainer ac,String smiles) {
			
			
			
			for (IAtom atom : ac.atoms()) {

				if ("N".equals(atom.getSymbol())) {
					double bondOrderSum = 0.0;
					
					List<Integer>bos=new ArrayList<>();
					
					for (IBond bond : ac.getConnectedBondsList(atom)) {
						bondOrderSum += bond.getOrder().numeric();
						bos.add(bond.getOrder().numeric());
//						System.out.println("\t"+ac.getAtomNumber(atom)+"\t"+bond.getOrder().numeric());
					}
					
					if(bondOrderSum==5) {
//						System.out.println(smiles+"\t"+bos);
						return true;
					}
				}
			}

			return false;
		}

	}

	class Percepta {

		void createInputSDF (String propertyName, String of,Hashtable<String, DataPoint> htDP) {

			String filePath=of+File.separator+propertyName+" test set qsar smiles.sdf";

			try {
				FileWriter fileWriter = new FileWriter(filePath);			
				SDFWriter sdfWriter = new SDFWriter(fileWriter);

				SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

				for (String qsarSmiles:htDP.keySet()) {
					// Parse SMILES to create molecule
					IAtomContainer molecule = smilesParser.parseSmiles(qsarSmiles);
					molecule.setProperty("qsarSmiles", qsarSmiles);
					// Write molecule to SDF
					sdfWriter.write(molecule);
				}
				sdfWriter.flush();
				sdfWriter.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}




		private Hashtable<String,ModelPrediction> getPerceptaModelPredictionsFromDB_Original_Smiles(String propertyName, Hashtable<String, DataPoint> htDP, long fk_dataset_id) {

			if(propertyName.equals("MP") || propertyName.equals("HLC") || propertyName.equals("96HR_Fish_LC50")) 
				return null;

			String modelNamePercepta=null;
			if(propertyName.equals("WS")) {
				modelNamePercepta="ACD_SolInPW";
			} else if (propertyName.equals("LogKow")) {
				modelNamePercepta="ACD_LogP_Consensus";
			} else if(propertyName.equals("BP") || propertyName.equals("VP")) {
				modelNamePercepta="ACD_"+propertyName;
			}

			String sql="select id from qsar_models.models where fk_source_id=7 and name='"+modelNamePercepta+"';";
			//			System.out.println(sql);

			long modelIdPercepta=Long.parseLong(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql));

			//		if(debug) System.out.println("modelIdPercepta="+modelIdPercepta);

			Hashtable<String, Double> htPredPercepta = getPerceptaPredictions(propertyName, fk_dataset_id, modelIdPercepta);

			Hashtable<String,ModelPrediction>htMPs=new Hashtable<>();

			for (String qsarSmiles:htDP.keySet()) {
				Double exp=htDP.get(qsarSmiles).getQsarPropertyValue();
				Double pred=htPredPercepta.get(qsarSmiles);
				ModelPrediction mp=new ModelPrediction(qsarSmiles, exp, pred, null);
				htMPs.put(qsarSmiles, mp);
			}

			return htMPs;

		}


		private Hashtable<String,ModelPrediction> getPerceptaModelPredictionsFromFile(String propertyName, String folder, Hashtable<String, DataPoint> htDP,String smilesType) {

			if(propertyName.equals("MP") || propertyName.equals("HLC") || propertyName.equals("96HR_Fish_LC50")) 
				return null;


			//		if(debug) System.out.println("modelIdPercepta="+modelIdPercepta);

			String filenameInput=propertyName+" test set "+smilesType+".smi";
			String filenameOutput=propertyName+" test set "+smilesType+" percepta.OUT";

			Hashtable<String, Double> htPredPercepta = getPerceptaPredictions(folder,filenameInput,filenameOutput);


			System.out.println(Utilities.gson.toJson(htPredPercepta));

			Hashtable<String,ModelPrediction>htMPs=new Hashtable<>();

			for (String key:htDP.keySet()) {
				Double exp=htDP.get(key).getQsarPropertyValue();
				Double pred=htPredPercepta.get(key);
				ModelPrediction mp=new ModelPrediction(key, exp, pred, null);
				htMPs.put(key, mp);
			}

			return htMPs;

		}

		private Hashtable<String,ModelPrediction> getPerceptaModelPredictionsFromSDF_ByCAS(String propertyName, String set, String folder, Hashtable<String, DataPoint> htDP_CAS,String smilesType) {
			//		if(debug) System.out.println("modelIdPercepta="+modelIdPercepta);

			String filenameOutput=propertyName+" "+set+" set "+smilesType+" percepta.sdf";
//			Hashtable<String, Double> htPredPercepta = getPerceptaPredictionsFromSDF_By_CAS(folder,filenameOutput);

			
			try {

				
				String filepath=folder+File.separator+filenameOutput;
				
				File file=new File(filepath);
				
				if(!file.exists()) {
					System.out.println(filepath+" exists="+file.exists());
					return null;
				}
				
				InputStream inputStream = new FileInputStream(filepath);
				IteratingSDFReader reader = new IteratingSDFReader(inputStream, DefaultChemObjectBuilder.getInstance());

				Hashtable<String,ModelPrediction>htMPs=new Hashtable<>();
				SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Canonical);

				while (reader.hasNext()) {
					IAtomContainer molecule = reader.next();
					
//					System.out.println(Utilities.gson.toJson(molecule.getProperties()));
					
					String cas = (String) molecule.getProperty("CAS");
					String smiles = (String) molecule.getProperty("SMILES");
					Double pred=Double.NaN;
					

					String fieldPred=null;
					if(propertyName.equals("LogKow")) fieldPred="ACD_LogP";
					else if(propertyName.equals("WS")) fieldPred="ACD_SolInPW";
						
					if(molecule.getProperty(fieldPred)==null) {
						if(debug) System.out.println(cas+"\tError\t"+smiles);
					} else {
						pred = Double.parseDouble(molecule.getProperty(fieldPred));
					}
					
//					System.out.println(cas+"\t"+smiles+"\t"+pred);
					
					if(cas==null) {
//						System.out.println(smilesGenerator.create(molecule)+"\t"+smiles+"\tnullCAS");
						continue;
					}
					
					if(!htDP_CAS.containsKey(cas)) {
//						System.out.println("No datapoint for "+cas);
						continue;
					}
					
					Double exp=htDP_CAS.get(cas).getQsarPropertyValue();
					ModelPrediction mp=new ModelPrediction(cas, exp, pred, null);
					
					DataPoint dp=htDP_CAS.get(mp.id);
					mp.checkStructure=dp.checkStructure;
//					System.out.println(smiles+"\t"+mp.checkStructure.episuite_smiles);
					htMPs.put(cas, mp);
				}
				reader.close();
				return htMPs;
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
//			System.out.println(Utilities.gson.toJson(htMPs));

		}
		
		
		private Hashtable<String,ModelPrediction> getPerceptaModelPredictionsFromSDF_ByQsarSmiles(String propertyName,String set, String folder, Hashtable<String, DataPoint> htDP,String smilesType) {
			//		if(debug) System.out.println("modelIdPercepta="+modelIdPercepta);
			String filenameOutput=propertyName+" "+set+" set "+smilesType+" percepta.sdf";
//			Hashtable<String, Double> htPredPercepta = getPerceptaPredictionsFromSDF_By_CAS(folder,filenameOutput);

			try {

				InputStream inputStream = new FileInputStream(folder+File.separator+filenameOutput);
				IteratingSDFReader reader = new IteratingSDFReader(inputStream, DefaultChemObjectBuilder.getInstance());

				Hashtable<String,ModelPrediction>htMPs=new Hashtable<>();
				
				while (reader.hasNext()) {
					IAtomContainer molecule = reader.next();
					
					String qsarSmiles = (String) molecule.getProperty("qsarSmiles");
					Double pred=Double.NaN;
					
					String fieldPred=null;
					if(propertyName.equals("LogKow")) fieldPred="ACD_LogP";
					else if(propertyName.equals("WS")) fieldPred="ACD_SolInPW";
						
					if(molecule.getProperty(fieldPred)==null) {
						if(debug) System.out.println(qsarSmiles+"\tError");
					} else {
						pred = Double.parseDouble(molecule.getProperty(fieldPred));
					}

					if(!htDP.containsKey(qsarSmiles)) {
						System.out.println("No datapoint for "+qsarSmiles);
						continue;
					}
					
					Double exp=htDP.get(qsarSmiles).getQsarPropertyValue();
					ModelPrediction mp=new ModelPrediction(qsarSmiles, exp, pred, null);
					
					DataPoint dp=htDP.get(mp.id);
					mp.checkStructure=dp.checkStructure;
//					System.out.println(smiles+"\t"+mp.checkStructure.episuite_smiles);
					htMPs.put(qsarSmiles, mp);
				}
				reader.close();
				return htMPs;
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
//			System.out.println(Utilities.gson.toJson(htMPs));

		}



		private Hashtable<String, Double> getPerceptaPredictionsFromSDF_By_CAS(String folder, String filenameOutput) {
			Hashtable<String, Double> casToLogP = new Hashtable<>();

			try {

				InputStream inputStream = new FileInputStream(folder+File.separator+filenameOutput);
				IteratingSDFReader reader = new IteratingSDFReader(inputStream, DefaultChemObjectBuilder.getInstance());

				while (reader.hasNext()) {
					IAtomContainer molecule = reader.next();
					String casNumber = (String) molecule.getProperty("CAS");
					String smiles = (String) molecule.getProperty("SMILES");
					
					if(molecule.getProperty("ACD_LogP")==null) {
						System.out.println(casNumber+"\tError\t"+smiles);
						casToLogP.put(casNumber, Double.NaN);
					} else {
						Double acdLogP = Double.parseDouble(molecule.getProperty("ACD_LogP"));
						casToLogP.put(casNumber, acdLogP);
					}
					
				}
				reader.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Print the Hashtable contents
			//				casToLogP.forEach((cas, logP) -> System.out.println("CAS: " + cas + ", ACD_LogP: " + logP));		
			return casToLogP;
		}

		Hashtable<String, Double> getPerceptaPredictions(String propertyName, long fk_dataset_id, long fk_model_id) {
			try {
				//			List<DataPoint> dps = new ArrayList<>();


				Hashtable<String, String> htDtxcidToSmiles = du.getDtxcidToQsarSmilesHashtableForDataset(fk_dataset_id);

				String strDtxcids = "(";
				for (String dtxcid : htDtxcidToSmiles.keySet()) {
					strDtxcids += "'" + dtxcid + "',";
				}
				strDtxcids += ")";
				strDtxcids = strDtxcids.replace(",)", ")");
				//			System.out.println(strDtxcids);

				String sql = "select distinct dr.dtxcid,pd.prediction_value from qsar_models.predictions_dashboard pd\r\n"
						+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
						+ "join qsar_models.models m on pd.fk_model_id = m.id\r\n" + "where m.id=" + fk_model_id
						+ " and dr.fk_dsstox_snapshot_id=2\r\n" + "and dr.dtxcid in " + strDtxcids
						+ " and pd.prediction_value is not null;";

				//			System.out.println(sql);

				ResultSet rs = SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);

				Hashtable<String, Double> htPred = new Hashtable<>();

				while (rs.next()) {

					String dtxcid = rs.getString(1);
					Double prediction = rs.getDouble(2);
					String qsarSmiles = htDtxcidToSmiles.get(dtxcid);

					if (propertyName.equals("VP")) {
						prediction = Math.log10(prediction);
					} else if (propertyName.equals("WS")) {
						prediction = -Math.log10(prediction);
					}

					//				System.out.println(dtxcid+"\t"+qsarSmiles+"\t"+prediction);

					htPred.put(qsarSmiles, prediction);
					//				System.out.println(dtxcid+"\t"+prediction);
				}

				//			System.out.println(Utilities.gson.toJson(htPred));

				return htPred;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}
		
		Hashtable<String, Double> getPerceptaPredictions(String folder, String filenameInput,String filenameOutput) {

			try {
				//			List<DataPoint> dps = new ArrayList<>();

				Hashtable<String, Double> htPred = new Hashtable<>();
				
				List<String>listSmiles=new ArrayList<>();
				List<Double>listPred=new ArrayList<>();
				
				BufferedReader brInput=new BufferedReader(new FileReader(folder+File.separator+filenameInput));
				while (true) {
					String line=brInput.readLine();
					if(line==null)break;
					listSmiles.add(line);
				}
				brInput.close();
				

				BufferedReader brOutput=new BufferedReader(new FileReader(folder+File.separator+filenameOutput));
				for(int i=1;i<=3;i++)brOutput.readLine();
				
				while (true) {
					String line=brOutput.readLine();
					if(line==null)break;

					String [] vals=line.split("\t");
					
					if(vals[1].isEmpty()) {
						System.out.println(line);
						listPred.add(Double.NaN);
						continue;
					}
					
//					if(line.contains("cannot be calculated") || line.contains("not supported")) {
//						listPred.add(Double.NaN);
//						continue;
//					}
					
					Double dval=Double.parseDouble(vals[1]);
//					System.out.println(dval);
					listPred.add(dval);
				}
				brOutput.close();

				if(listSmiles.size()!=listPred.size()) {
					System.out.println("Size mismatch:"+"\t"+listSmiles.size()+"\t"+listPred.size());
					return null;
				}
				
				for (int i=0;i<listSmiles.size();i++) {
					htPred.put(listSmiles.get(i), listPred.get(i));	
				}
				//			System.out.println(Utilities.gson.toJson(htPred));

				return htPred;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}

//		private Results getPerceptaResults(String propertyName, boolean createPlot,
//				boolean omitChemicalsNotPredictedByEpisuite, String units, long fk_dataset_id,
//				Hashtable<String, DataPoint> htDP,String outputFolder) {
//
//			if(propertyName.equals("MP") || propertyName.equals("HLC") || propertyName.equals("96HR_Fish_LC50")) 
//				return null;
//
//			String modelNamePercepta=null;
//			if(propertyName.equals("WS")) {
//				modelNamePercepta="ACD_SolInPW";
//			} else if (propertyName.equals("LogKow")) {
//				modelNamePercepta="ACD_LogP_Consensus";
//			} else if(propertyName.equals("BP") || propertyName.equals("VP")) {
//				modelNamePercepta="ACD_"+propertyName;
//			}
//
//			String sql="select id from qsar_models.models where fk_source_id=7 and name='"+modelNamePercepta+"';";
//			long modelIdPercepta=Long.parseLong(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql));
//			if(debug) System.out.println("modelIdPercepta="+modelIdPercepta);
//			Hashtable<String, Double> htPredPercepta = getPerceptaPredictions(propertyName, fk_dataset_id, modelIdPercepta);
//
//			return plot.plotPredictions(propertyName+" Percepta", propertyName, units, null, htDP, htPredPercepta,createPlot,omitChemicalsNotPredictedByEpisuite,outputFolder);
//
//		}

		private String getModelNamePercepta(String propertyName) {
			String modelNamePercepta=null;
			if(propertyName.equals("WS")) {
				modelNamePercepta="ACD_SolInPW";
			} else if (propertyName.equals("LogKow")) {
				modelNamePercepta="ACD_LogP_Consensus";
			} else if(propertyName.equals("BP") || propertyName.equals("VP")) {
				modelNamePercepta="ACD_"+propertyName;
			}
			return modelNamePercepta;
		}

	}

	class Episuite {

		private void getEpisuiteIsisTrainingSet(String propertyName,String modelNameEpi) {

			boolean useFullStandardize = false;

			String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\EpisuiteISIS\\EPI_SDF_Data\\";
			String filename=null;

			if(propertyName.equals("LogKow")) {
				filename="EPI_Kowwin_Data_SDF.sdf";
			} else if(modelNameEpi.equals("WaterNT")) {
				filename="EPI_WaterFrag_Data_SDF.sdf";
			}

			String filepathInput = folder + filename;

			try {

				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepathInput),
						DefaultChemObjectBuilder.getInstance());

				String filepathOut = "data\\episuite\\episuite validation\\"+propertyName+"\\"+propertyName+" episuite training set.txt";

				if(modelNameEpi!=null) {
					filepathOut = "data\\episuite\\episuite validation\\"+propertyName+"\\"+modelNameEpi+" episuite training set.txt";
				}

				FileWriter fw = new FileWriter(filepathOut);

				int counter = 0;

				//			DescriptorFactory df=new DescriptorFactory(false);

				int countT = 0;
				int countOther = 0;


				fw.write("CAS\tset\tSmiles\tqsarSmiles\r\n");

				SmilesGenerator sg=new SmilesGenerator(SmiFlavor.Canonical);

				while (mr.hasNext()) {
					counter++;
					IAtomContainer m = mr.next();

					String CAS = m.getProperty("CAS");

					while (CAS.substring(0, 1).equals("0")) {
						CAS = CAS.substring(1, CAS.length());
					}

					String Smiles = m.getProperty("Smiles");

					if(Smiles==null) {
						Smiles=sg.create(m);
					}

					String set = "";

					String Train = m.getProperty("Train");

					String DataSet = m.getProperty("DataSet");

					if(DataSet!=null) {
						if (DataSet.equals("T") || DataSet.equals("S")) {
							set=DataSet;
							countT++;
						} else {
							countOther++;
						}
					} else if (Train!=null) {
						if (Train.equals("T") || Train.equals("S")) {
							countT++;
							set = Train;
						} else {
							countOther++;
						}
					} else {
						countOther++;
					}

					//				String inchi=StructureUtil.indigoInchikeyFromSmiles(Smiles);

					if (set.isBlank())
						continue;

					HttpResponse<String> standardizeResponse = SciDataExpertsStandardizer.callQsarReadyStandardizePost(Smiles,false, workflow,serverHost);

					//				System.out.println("status=" + standardizeResponse.getStatus());

					String qsarSmiles = null;

					if (standardizeResponse.getStatus() == 200) {
						String jsonResponse = SciDataExpertsStandardizer.getResponseBody(standardizeResponse,
								useFullStandardize);
						qsarSmiles = SciDataExpertsStandardizer.getQsarReadySmilesFromPostJson(jsonResponse,
								useFullStandardize);
					}

					//				System.out.println(CAS+"\t"+set+"\t"+inchi+"\t"+Smiles);
					System.out.println(counter + "\t" + CAS + "\t" + set + "\t" + Smiles + "\t" + qsarSmiles);

					fw.write(CAS + "\t" + set + "\t" + Smiles + "\t" + qsarSmiles + "\r\n");
					fw.flush();

					//				System.out.println(CAS+"\t"+Train);
				}

				System.out.println("CountT=" + countT);
				System.out.println("CountOther=" + countOther);

				fw.close();
				mr.close();

			} catch (Exception ex) {
				ex.printStackTrace();

			}

		}

		private HashSet<String> getEpisuiteIsisTrainingSetCAS(String propertyName,String modelNameEpi) {

			HashSet<String>hsCAS=new HashSet<>();

			boolean useFullStandardize = false;

			String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\EpisuiteISIS\\EPI_SDF_Data\\";

			String filename=null;

			if(propertyName.equals("LogKow")) filename="EPI_Kowwin_Data_SDF.sdf";
			else if(propertyName.equals("WS")) {
				if(modelNameEpi.equals("WaterNT")) filename="EPI_WaterFrag_Data_SDF.sdf";
				else if (modelNameEpi.equals("WSKOW")) 	filename="EPI_Wskowwin_Data_SDF.sdf";
			} 

			if(filename==null) {
				System.out.println("getEpisuiteIsisTrainingSetCAS(), Need to handle "+propertyName+" "+modelNameEpi+" in getEpisuiteIsisTrainingSetCAS()");
				return null;
			}


			String filepathInput = folder + filename;

			try {

				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepathInput),
						DefaultChemObjectBuilder.getInstance());

				String filepathOut = "data\\episuite\\episuite validation\\LogKow\\LogKow episuite training set.txt";


				int counter = 0;

				//			DescriptorFactory df=new DescriptorFactory(false);

				int countT = 0;
				int countOther = 0;



				while (mr.hasNext()) {
					counter++;
					IAtomContainer m = mr.next();

					String CAS = m.getProperty("CAS");

					while (CAS.substring(0, 1).equals("0")) {
						CAS = CAS.substring(1, CAS.length());
					}

					String Train = m.getProperty("Train");
					String DataSet = m.getProperty("DataSet");

					String set = "";

					if(DataSet!=null) {

//						if (DataSet.equals("T") || DataSet.equals("S")) {
						if (DataSet.equals("T")) {
							set=DataSet;
							countT++;
						} else {
							countOther++;
						}
					} else if (Train!=null) {

//						if (Train.equals("T") || Train.equals("S")) {
						if (Train.equals("T")) {
							countT++;
							set = Train;
						} else {
							countOther++;
						}
					} else {
						countOther++;
					}

					//				String inchi=StructureUtil.indigoInchikeyFromSmiles(Smiles);

					if (set.isBlank())
						continue;

					hsCAS.add(CAS);

					//				System.out.println(CAS+"\t"+Train);
				}

				//				System.out.println("CountT=" + countT);
				//				System.out.println("CountOther=" + countOther);

				return hsCAS;		
			} catch (Exception ex) {
				ex.printStackTrace();
				return hsCAS;
			}

		}
		
		
		private HashSet<String> getEpisuiteIsisTrainingSetInchiKey1(String propertyName,String modelNameEpi) {

			HashSet<String>hsInchikey1=new HashSet<>();

			

			String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\EpisuiteISIS\\EPI_SDF_Data\\";

			String filename=null;

			if(propertyName.equals("LogKow")) filename="EPI_Kowwin_Data_SDF.sdf";
			else if(propertyName.equals("WS")) {
				if(modelNameEpi.equals("WaterNT")) filename="EPI_WaterFrag_Data_SDF.sdf";
				else if (modelNameEpi.equals("WSKOW")) 	filename="EPI_Wskowwin_Data_SDF.sdf";
			} 

			if(filename==null) {
				System.out.println("getEpisuiteIsisTrainingSetInchiKey1(), Need to handle "+propertyName+" "+modelNameEpi+" in getEpisuiteIsisTrainingSetCAS()");
				return null;
			}


			String filepathInput = folder + filename;

			try {

				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepathInput),
						DefaultChemObjectBuilder.getInstance());

				String filepathOut = "data\\episuite\\episuite validation\\LogKow\\LogKow episuite training set.txt";


				int counter = 0;

				//			DescriptorFactory df=new DescriptorFactory(false);

				int countT = 0;
				int countOther = 0;



				while (mr.hasNext()) {
					counter++;
					IAtomContainer m = mr.next();

					
					String Train = m.getProperty("Train");
					String DataSet = m.getProperty("DataSet");

					String set = "";

					if(DataSet!=null) {

//						if (DataSet.equals("T") || DataSet.equals("S")) {
						if (DataSet.equals("T")) {
							set=DataSet;
							countT++;
						} else {
							countOther++;
						}
					} else if (Train!=null) {

//						if (Train.equals("T") || Train.equals("S")) {
						if (Train.equals("T")) {
							countT++;
							set = Train;
						} else {
							countOther++;
						}
					} else {
						countOther++;
					}

					//				String inchi=StructureUtil.indigoInchikeyFromSmiles(Smiles);

					if (set.isBlank())
						continue;

					String inchiKey1=StructureUtil.indigoInchikey1FromAtomContainer(m);
					hsInchikey1.add(inchiKey1);

					//				System.out.println(CAS+"\t"+Train);
				}

				//				System.out.println("CountT=" + countT);
				//				System.out.println("CountOther=" + countOther);

				return hsInchikey1;		
			} catch (Exception ex) {
				ex.printStackTrace();
				return hsInchikey1;
			}

		}
		
		
		private Double [] getEpisuiteIsisTrainingSetMW_Range(String propertyName,String modelNameEpi) {

			HashSet<String>hsCAS=new HashSet<>();

			boolean useFullStandardize = false;

			String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\EpisuiteISIS\\EPI_SDF_Data\\";

			String filename=null;

			if(propertyName.equals("LogKow")) filename="EPI_Kowwin_Data_SDF.sdf";
			else if(propertyName.equals("WS")) {
				if(modelNameEpi.equals("WaterNT")) filename="EPI_WaterFrag_Data_SDF.sdf";
				else if (modelNameEpi.equals("WSKOW")) 	filename="EPI_Wskowwin_Data_SDF.sdf";
			} 

			if(filename==null) {
				System.out.println("getEpisuiteIsisTrainingSetMW_Range(), Need to handle "+propertyName+" "+modelNameEpi+" in getEpisuiteIsisTrainingSetCAS()");
				return null;
			}


			String filepathInput = folder + filename;

			try {

				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepathInput),
						DefaultChemObjectBuilder.getInstance());

				String filepathOut = "data\\episuite\\episuite validation\\LogKow\\LogKow episuite training set.txt";


				int counter = 0;
				double minMW=999999;
				double maxMW=0;
						

				while (mr.hasNext()) {
					counter++;
					IAtomContainer molecule = mr.next();

					String CAS = molecule.getProperty("CAS");

					while (CAS.substring(0, 1).equals("0")) {
						CAS = CAS.substring(1, CAS.length());
					}

					String Train = molecule.getProperty("Train");
					String DataSet = molecule.getProperty("DataSet");
					
					if(DataSet!=null) {
						if (DataSet.equals("T")) {
							double MW=AtomContainerManipulator.getNaturalExactMass(molecule);
							if(MW>maxMW) maxMW=MW;
							if(MW<minMW) minMW=MW;
							if(MW<10) System.out.println(CAS+"\t"+MW);
						} 
					} else if (Train!=null) {
						if (Train.equals("T")) {
							double MW=AtomContainerManipulator.getNaturalExactMass(molecule);
							if(MW>maxMW) maxMW=MW;
							if(MW<minMW) minMW=MW;
							if(MW<10) System.out.println(CAS+"\t"+MW);
						}
					} 
				}
				
				mr.close();
				
				Double [] range={minMW,maxMW};
				return range;	
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}




		private void getEpisuiteIsisTrainingChemicals(String propertyName,String modelNameEpi,String folderPath) {

			SmilesGenerator sg= new SmilesGenerator(SmiFlavor.Unique);

			//			boolean useFullStandardize = false;

			String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\EpisuiteISIS\\EPI_SDF_Data\\";

			String filename=null;

			if(propertyName.equals("LogKow")) {
				filename="EPI_Kowwin_Data_SDF.sdf";

			}if(propertyName.equals("WS")) {
				if(modelNameEpi.equals("WaterNT")) filename="EPI_WaterFrag_Data_SDF.sdf";
				else if(modelNameEpi.equals("LogKow")) filename="EPI_Wskowwin_Data_SDF.sdf";
			}

			String filepathInput = folder + filename;

			try {

				String fileNameOut="chemreg import "+propertyName+" episuite training set.txt";

				if(modelNameEpi!=null) {
					fileNameOut="chemreg import "+propertyName+" "+modelNameEpi+" episuite training set.txt";
				}



				FileWriter fw=new FileWriter(folderPath+File.separator+fileNameOut);

				IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepathInput),
						DefaultChemObjectBuilder.getInstance());

				int counter = 0;
				int countT = 0;
				int countOther = 0;

				fw.write("ExternalID\tCAS\tChemicalName\tsmilesCDK\r\n");

				while (mr.hasNext()) {
					counter++;

					IAtomContainer m = mr.next();


					String Train = m.getProperty("Train");
					String DataSet = m.getProperty("DataSet");

					String set = "";

					if(DataSet!=null) {
						if (DataSet.equals("T") || DataSet.equals("S")) {
							set=DataSet;
							countT++;
						} else {
							countOther++;
						}
					} else if (Train!=null) {
						if (Train.equals("T") || Train.equals("S")) {
							countT++;
							set = Train;
						} else {
							countOther++;
						}
					} else {
						countOther++;
					}

					//				String inchi=StructureUtil.indigoInchikeyFromSmiles(Smiles);

					if (set.isBlank())
						continue;

					String ChemicalName = m.getProperty("NAME");
					String CAS = m.getProperty("CAS");
					while (CAS.substring(0, 1).equals("0")) CAS = CAS.substring(1, CAS.length());
					String smilesCDK=sg.create(m);
					if(!du.isValidCAS(CAS)) CAS="";
					String externalID="C"+counter;

					fw.write(externalID+"\t"+CAS+"\t"+ChemicalName+"\t"+smilesCDK+"\r\n");
					//					System.out.println(externalID+"\t"+CAS+"\t"+ChemicalName+"\t"+smilesCDK);

					//				System.out.println(CAS+"\t"+Train);
				}

				fw.flush();
				fw.close();


				System.out.println("CountT=" + countT);
				System.out.println("CountOther=" + countOther);




			} catch (Exception ex) {
				ex.printStackTrace();

			}

		}


		private HashSet<String> getEpisuiteIsisTrainingSmiles(String propertyName,String modelNameEpi) {

			HashSet<String> hsSmiles = new HashSet<>();

			try {
				//				String filepath = "data\\episuite\\episuite validation\\"+propertyName+"\\"+propertyName+" episuite training set.txt";

				String filepathOut = "data\\episuite\\episuite validation\\"+propertyName+"\\"+propertyName+" episuite training set.txt";

				if(modelNameEpi!=null && !modelNameEpi.equals("Group")) {
					filepathOut = "data\\episuite\\episuite validation\\"+propertyName+"\\"+modelNameEpi+" episuite training set.txt";
				}


				BufferedReader br = new BufferedReader(new FileReader(filepathOut));
				br.readLine();
				while (true) {
					String Line = br.readLine();
					if (Line == null)
						break;
					String qsarSmiles = Line.split("\t")[3];
					hsSmiles.add(qsarSmiles);
				}
				br.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return hsSmiles;
		}


		/**
		 * This version uses a qsar smiles file generated by making a chemreg list from the episuite isis sdf
		 * 
		 * @param propertyName
		 * @param modelNameEpi
		 * @return
		 */
		private HashSet<String> getEpisuiteIsisTrainingSmiles2(String propertyName,String modelNameEpi) {

			HashSet<String> hsSmiles = new HashSet<>();

			try {

				String of="data\\episuite\\episuite validation\\" + propertyName;
				String filepathOut=of+File.separator+propertyName+" training set qsar smiles.smi";

				//				String filepath = "data\\episuite\\episuite validation\\"+propertyName+"\\"+propertyName+" episuite training set.txt";
				//				if(modelNameEpi!=null && !modelNameEpi.equals("Group")) {
				//					filepathOut = "data\\episuite\\episuite validation\\"+propertyName+"\\"+modelNameEpi+" episuite training set.txt";
				//				}

				BufferedReader br = new BufferedReader(new FileReader(filepathOut));
				br.readLine();
				while (true) {
					String Line = br.readLine();
					if (Line == null)
						break;

					if(Line.equals("null"))continue;

					String qsarSmiles = Line;
					hsSmiles.add(qsarSmiles);
				}
				br.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return hsSmiles;
		}



		Hashtable<String, Object> getEpisuiteEstimateValue(String propertyName, String modelNameEpi, String filepathJson,String key) {

			try {

				BufferedReader br = new BufferedReader(new FileReader(filepathJson));

				Hashtable<String, Object> htPreds = new Hashtable<>();

				while (true) {

					String Line = br.readLine();

					if (Line == null)
						break;
					EpisuiteResults er = null;

					er = Utilities.gson.fromJson(Line, EpisuiteResults.class);

					Object ev = null;

					//				System.out.println(Line);

					if (propertyName.equals("LogKow")) {
						if (er.logKow != null) {
							ev = er.logKow.estimatedValue;							
							er.logKow.estimatedValue.chemicalProperties=er.chemicalProperties;
						}
					} else if (propertyName.equals("HLC")) {

						if (er.henrysLawConstant != null) {
							ev = er.henrysLawConstant.estimatedValue;// not fair because some experimental values are
							// actually VP/WS
						} else
							System.out.println("Pred is null for " + er.canonQsarSmiles);

					} else if (propertyName.equals("VP")) {
						if (er.vaporPressure != null)
							ev = er.vaporPressure.estimatedValue;
					} else if (propertyName.equals("BP")) {
						if (er.boilingPoint != null)
							ev = er.boilingPoint.estimatedValue;

					} else if (propertyName.equals("WS")) {
						if(modelNameEpi.equalsIgnoreCase("waternt")) {
							if(er.waterSolubilityFromWaterNt!=null) {
								ev=er.waterSolubilityFromWaterNt.estimatedValue;
							}
						}else if(modelNameEpi.equalsIgnoreCase("logkow")) {
							if(er.waterSolubilityFromLogKow!=null) {
								ev=er.waterSolubilityFromLogKow.estimatedValue;
							}
						}

					} else if (propertyName.equals("96HR_Fish_LC50")) {
						// TODO
					}

					//				System.out.println(er.dtxcid+"\t"+er.canonQsarSmiles+"\t"+pred+"\t"+er.error);

					if (ev == null)
						continue;


					EstimatedValue ev2=(EstimatedValue)ev;
					ev2.chemicalProperties=er.chemicalProperties;


					if(key.equals("canonQsarSmiles"))
						htPreds.put(er.canonQsarSmiles, ev);
					else if (key.equals("cas")) {
						String CAS2=er.chemicalProperties.cas;
						while(CAS2.startsWith("0")) CAS2=CAS2.substring(1,CAS2.length());
						htPreds.put(CAS2, ev);
					}

				}

				br.close();

				return htPreds;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}


		class PredResult {
			Double pred;
			Boolean insideAD;
			String qsarClass;
		}

		public Hashtable<String, ModelPrediction> getEpisuiteModelPredictions(String propertyName,String modelName,
				Hashtable<String, DataPoint> htDP,Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles,String filepathEpisuiteOutput) {


			
			System.out.println("Enter getEpisuiteModelPredictions()");
			
			
			//			System.out.println("Here2:"+Utilities.gson.toJson(htDP.get("CC1C(CCC(=O)OC)C(N=C1C(C)=O)=C1CC(=O)C2=C1NC(C=C1N=C(CC3=NC(=O)C(C)=C3CC)C(C)=C1CC)=C2C")));

			try {

				BufferedReader br = new BufferedReader(new FileReader(filepathEpisuiteOutput));

				HashSet<String> hsSmilesEpisuiteTraining = new HashSet<>();
				HashSet<String> hsCASEpisuiteTraining = new HashSet<>();
				HashSet<String> hsInchikey1Training = new HashSet<>();

				if (propertyName.equals("LogKow") || propertyName.equals("WS")) {
					hsSmilesEpisuiteTraining = getEpisuiteIsisTrainingSmiles2(propertyName,modelName);
					hsCASEpisuiteTraining = getEpisuiteIsisTrainingSetCAS(propertyName, modelName);
					hsInchikey1Training=getEpisuiteIsisTrainingSetInchiKey1(propertyName, modelName);
					
				} else if (propertyName.equals("96HR_Fish_LC50")) {
					hsSmilesEpisuiteTraining = getEcosarTrainingSmiles(filepathEpisuiteOutput);
					System.out.println("Number of chemicals in episuite training set="+hsSmilesEpisuiteTraining.size());
					
				} else if (propertyName.equals("HLC")) {
					// get from Appendix G of EPI Suite user guide for HLC
				}

				Double [] rangeMW=getEpisuiteIsisTrainingSetMW_Range(propertyName, modelName);
				Double [] rangeMW_LogKow=getEpisuiteIsisTrainingSetMW_Range("LogKow", modelName);

				
				Hashtable<String, ModelPrediction> htPreds = new Hashtable<>();

				while (true) {

					String Line = br.readLine();
					if (Line == null)
						break;

					EpisuiteResults er = null;

					try {
						er = Utilities.gson.fromJson(Line, EpisuiteResults.class);
					} catch (Exception ex) {
						continue;
					}

					if(er.canonQsarSmiles==null)continue;

					if(er.error!=null && er.error.contains("500")) {
						System.out.println(er.error);
						continue;
					}

					if(!htDP.containsKey(er.canonQsarSmiles)) continue;

					DataPoint dp=htDP.get(er.canonQsarSmiles);

					if(dp.checkStructure==null) {
						System.out.println("Check structure null for "+dp.getCanonQsarSmiles());
					}

					Double exp=dp.getQsarPropertyValue();

					Integer splitNum = getSplitNum(htCS_by_DP_QsarSmiles, hsSmilesEpisuiteTraining,
							hsCASEpisuiteTraining, er);

//					Integer splitNum = getSplitNum(er.smiles, hsInchikey1Training);

					PredResult pr = getPredictionResult(propertyName, modelName, er,rangeMW);
					
					if(propertyName.equals("96HR_Fish_LC50")) {
						
						if(er.ecosar!=null && er.ecosar.parameters.logKow.valueType.equals("ESTIMATED")) {

							PredResult prLogKow = getPredictionResult("LogKow", modelName, er,rangeMW_LogKow);

							if(pr.insideAD==true && !prLogKow.insideAD) {
								pr.insideAD=false;
//								System.out.println(pr.insideAD+"\t"+prLogKow.insideAD);
							}
							
						}
					}
					
					
//					if(modelName.equals("WSKOW"))
//						System.out.println(er.canonQsarSmiles+"\t"+pr.pred+"\t"+pr.insideAD);
					

					ModelPrediction mp=new ModelPrediction(er.canonQsarSmiles,exp,pr.pred,splitNum,pr.insideAD);				

					mp.qsarClass=pr.qsarClass;
					
					
					//					mp.dtxcid=dp.getQsar_dtxcid();
					//					if(htCS_by_DP_QsarSmiles.containsKey(er.canonQsarSmiles)) {
					//						mp.dtxcid=htCS_by_DP_QsarSmiles.get(er.canonQsarSmiles).dsstox_dtxcid;	
					//					} else {
					//						System.out.println(er.canonQsarSmiles+"\tNo in htCS_by_DP_QsarSmiles");
					//					}

					mp.checkStructure=dp.checkStructure;

					if(er.canonQsarSmiles==null)
						continue;

					//					if(er.canonQsarSmiles.equals("OC(CO)C(O)CO")) {
					//						System.out.println(Utilities.gson.toJson(mp));
					//					}
					htPreds.put(er.canonQsarSmiles, mp);

				}

				br.close();
				return htPreds;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}

		private Integer getSplitNum(Hashtable<String, CheckStructure> htCS_by_DP_QsarSmiles,
				HashSet<String> hsSmilesEpisuiteTraining, HashSet<String> hsCASEpisuiteTraining, EpisuiteResults er) {

			Integer splitNum=1;

			if(hsSmilesEpisuiteTraining.contains(er.canonQsarSmiles)) splitNum=0;
			else {
				if(htCS_by_DP_QsarSmiles.containsKey(er.canonQsarSmiles)) {
					String casrn=htCS_by_DP_QsarSmiles.get(er.canonQsarSmiles).dsstox_casrn;
					if(casrn!=null && hsCASEpisuiteTraining.contains(casrn)) splitNum=0;
				} 
			}
			return splitNum;
		}

		
		private Integer getSplitNum(String smiles,HashSet<String> hsInchikey1EpisuiteTraining) {

			Integer splitNum=null;
			
			String inchikey1=StructureUtil.indigoInchikey1FromSmiles(smiles);
			if(hsInchikey1EpisuiteTraining.contains(inchikey1)) splitNum=0;
			else splitNum=1;
			
			return splitNum;
		}

		public Hashtable<String, ModelPrediction> getEpisuiteModelPredictionsByCAS(String propertyName,String modelName,
				Hashtable<String, DataPoint> htDP_CAS,String filepathEpisuiteOutput) {

			try {

				BufferedReader br = new BufferedReader(new FileReader(filepathEpisuiteOutput));

				HashSet<String> hsCASEpisuiteTraining = new HashSet<>();

				//HERE99
				if (propertyName.equals("LogKow") || propertyName.equals("WS")) {
					hsCASEpisuiteTraining = getEpisuiteIsisTrainingSetCAS(propertyName,modelName);
				} else if (propertyName.equals("HLC")) {
					//for Bond method see appendix G of HENRYWIN help file 
					//For group method see Hine, J. and Mookerjee, P.K.   1975.  The intrinsic hydrophilic character of organic compounds. Correlations in terms of structural contributions.   J. Org. Chem. 40: 292-298.
				} else if (propertyName.equals("BP")) {
					//See Stein, S.E. and Brown, R.L.   1994.   Estimation of normal boiling points from group contributions. J. Chem. Inf. Comput. Sci. 34: 581-7.
				} else if (propertyName.equals("VP")) {
					//hard to determine- calculated from Tb and Tm 
				} else if (propertyName.equals("HLC")) {
					// get from Appendix G of EPI Suite user guide for HLC
				} else if (propertyName.equals("96HR_Fish_LC50")) {
					hsCASEpisuiteTraining = getEcosarTrainingSmiles(filepathEpisuiteOutput);
				}
				
				
				Double [] rangeMW=getEpisuiteIsisTrainingSetMW_Range(propertyName, modelName);
				
				if(debug)
					System.out.println(modelName+"\t"+Utilities.gson.toJson(rangeMW));
				
//				System.out.println("hsCASEpisuiteTraining.size()="+hsCASEpisuiteTraining.size());

				Hashtable<String, ModelPrediction> htPreds = new Hashtable<>();

				while (true) {

					String Line = br.readLine();
					if (Line == null)
						break;

					EpisuiteResults er = null;

					try {
						er = Utilities.gson.fromJson(Line, EpisuiteResults.class);
					} catch (Exception ex) {
						continue;
					}

					if(er.chemicalProperties==null)	continue;

					if(er.error!=null && er.error.contains("500")) {
						//						System.out.println(er.error);
						continue;
					}



					String CAS=er.chemicalProperties.cas;
					while(CAS.startsWith("0")) CAS=CAS.substring(1,CAS.length());

					if(!htDP_CAS.containsKey(CAS)) continue;


					DataPoint dp=htDP_CAS.get(CAS);
					Double exp=dp.getQsarPropertyValue();
					
					if(dp.getQsarPropertyValue()==null) {
						System.out.println(CAS+"\tnull exp value");
						continue;
					}
 
//					Integer splitNum=null;
//					if(hsCASEpisuiteTraining.size()==0) {
//						splitNum=0;
//					} else {
//						if(hsCASEpisuiteTraining.contains(CAS)) splitNum=0;
//						else splitNum=1;
//					}
					
					Integer splitNum=null;
					if(hsCASEpisuiteTraining.contains(CAS)) splitNum=0;
					else splitNum=1;


					PredResult pr = getPredictionResult(propertyName, modelName, er,rangeMW);

					ModelPrediction mp=new ModelPrediction(CAS,exp,pr.pred,splitNum,pr.insideAD);
					mp.checkStructure=dp.checkStructure;

					mp.checkStructure.episuite_smiles=er.chemicalProperties.smiles;

					
					if(CAS.equals("638-67-5")) {
						System.out.println("Found 638-67-5:"+Utilities.gson.toJson(mp));
					}
					
					htPreds.put(CAS, mp);

				}

				br.close();
				return htPreds;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}

		private PredResult getPredictionResult(String propertyName, String modelName, EpisuiteResults er,Double []rangeMW_Training) {

			PredResult pr=new PredResult();

			pr.pred = null;
			pr.insideAD=null;

			if (propertyName.equals("LogKow")) {

				if (er.logKow != null) {
					pr.pred = er.logKow.estimatedValue.value;
					boolean insideFragmentDomain=insideFragmentDomain(er.logKow.estimatedValue);

					boolean insideMWDomain=true;
					Double MW=er.chemicalProperties.molecularWeight;
					
					
//					if(MW<18.02 || MW>719.92) insideMWDomain=false;
					if(MW<rangeMW_Training[0] || MW>rangeMW_Training[1]) insideMWDomain=false;
					
					
//					System.out.println(er.chemicalProperties.cas+"\t"+insideMWDomain+"\t"+insideFragmentDomain+"\t"+MW);
					
					pr.insideAD=insideFragmentDomain && insideMWDomain;
					//						if(!insideAD)
					//							System.out.println(er.dtxcid+"\t"+splitNum+"\t"+insideFragmentDomain+"\t"+insideMWDomain);

				}

			} else if (propertyName.equals("HLC")) {
				if (er.henrysLawConstant != null) {

					if(modelName.equals("Selected")) {
						pr.pred = -Math.log10(er.henrysLawConstant.estimatedValue.value);
					} else {
						for(gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Model model:er.henrysLawConstant.estimatedValue.model) {
							if(model.name==null) continue;
							if(model.name.equals(modelName)) {
								if(model.hlcAtm!=0) {
									pr.pred=-Math.log10(model.hlcAtm);	
								}
							}
						}
					}

					// Use bond method:
					//						pred=-Math.log10(er.henrysLawConstant.estimatedValue.model.get(1).hlcAtm);
					// use overall (not fair because some experimental values are actually VP/WS)
					//						pred = -Math.log10(er.henrysLawConstant.estimatedValue.value);
				} 

			} else if (propertyName.equals("VP")) {
				if (er.vaporPressure != null) {
					//						pred = Math.log10(er.vaporPressure.estimatedValue.value);

					for(gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Model model:er.vaporPressure.estimatedValue.model) {
						if(model.type.equals(modelName)) {
							pr.pred=Math.log10(model.mmHg);
						}
					}

				}
			} else if (propertyName.equals("MP")) {
				if (er.meltingPoint != null) {

					if(modelName.equals("Adapted Joback")) {
						pr.pred = er.meltingPoint.estimatedValue.model.meltingPointAdaptedJoback;	
					} else if(modelName.equals("Gold-Ogle")) {
						pr.pred = er.meltingPoint.estimatedValue.model.meltingPointGoldOgle;
					} else if(modelName.equals("Selected")) {
						pr.pred = er.meltingPoint.estimatedValue.model.meltingPointSelected;
					}

				}

			} else if (propertyName.equals("BP")) {
				if (er.boilingPoint != null)
					pr.pred = er.boilingPoint.estimatedValue.value;
			} else if (propertyName.equals("WS")) {

				if(modelName.equals("WaterNT") && er.waterSolubilityFromWaterNt != null) {
					pr.pred = er.waterSolubilityFromWaterNt.estimatedValue.value;// mg/L
					pr.pred /= 1000.0;// g/L
					pr.pred /= er.chemicalProperties.molecularWeight;// mol/L
					pr.pred = -Math.log10(pr.pred);// -Log(mol/L);

					/**
					 * Currently there is no universally accepted definition of model domain.
					 *  However, users may wish to consider the possibility that water solubility
					 * estimates are less accurate for compounds outside the MW range of the
					 * training set compounds, and/or that have more instances of a given fragment
					 * than the maximum for all training set compounds.  It is also possible that a
					 * compound may have a functional group(s) or other structural features not
					 * represented in the training set, and for which no fragment coefficient was
					 * developed.  These points should be taken into consideration when interpreting
					 * model results.
					 */
					boolean insideMWDomain=true;
					Double MW=er.chemicalProperties.molecularWeight;
//					if(MW<30.3 || MW>627.62) insideMWDomain=false;
					
					if(MW<rangeMW_Training[0] || MW>rangeMW_Training[1]) insideMWDomain=false;

					
					boolean insideFragmentDomain=insideFragmentDomain(er.waterSolubilityFromWaterNt.estimatedValue);
					pr.insideAD=insideFragmentDomain && insideMWDomain;


				} else if(modelName.equals("WSKOW") && er.waterSolubilityFromLogKow != null) {
					
					pr.pred = er.waterSolubilityFromLogKow.estimatedValue.value;
					pr.pred /= 1000.0;// g/L
					pr.pred /= er.chemicalProperties.molecularWeight;// mol/L
					pr.pred = -Math.log10(pr.pred);// -Log(mol/L);
					
					boolean insideMWDomain=true;
					Double MW=er.chemicalProperties.molecularWeight;
//					if(MW<30.3 || MW>627.62) insideMWDomain=false;//TODO determine range in training file
					
					if(MW<rangeMW_Training[0] || MW>rangeMW_Training[1]) insideMWDomain=false;
					
					//Use logKow fragments:
					boolean insideFragmentDomain=insideFragmentDomain(er.logKow.estimatedValue);	
					
					if(er.waterSolubilityFromLogKow.parameters.logKow.valueType.equals("EXPERIMENTAL")) {
						insideFragmentDomain=true;
					}
					pr.insideAD=insideFragmentDomain && insideMWDomain;
					
					
				}

			} else if (propertyName.equals("96HR_Fish_LC50")) {
				PredResult ecosarResult = getEcosar96hrFishToxicityMax(er);
//				PredResult ecosarResult = getEcosar96hrFishToxicityMedian(er);
				pr.pred=ecosarResult.pred;
				pr.insideAD=ecosarResult.insideAD;
				pr.qsarClass=ecosarResult.qsarClass;
			}

			//				System.out.println(er.dtxcid+"\t"+er.canonQsarSmiles+"\t"+pred+"\t"+er.error);


			if (pr.pred == null) {
				//					continue;
				pr.pred = Double.NaN;
			}
			return pr;
		}

		
		private PredResult getPredictionResult(String propertyName, String modelName, Object ev,Double []rangeMW_Training) {

			PredResult pr=new PredResult();

			pr.pred = null;
			pr.insideAD=null;
			
			if(ev==null)return pr;
			

			if (propertyName.equals("LogKow")) {

				EstimatedValue ev2=(EstimatedValue)ev;
				
				pr.pred = ev2.value;
				boolean insideFragmentDomain=insideFragmentDomain(ev2);

				boolean insideMWDomain=true;
				Double MW=ev2.chemicalProperties.molecularWeight;
				
//				if(MW<18.02 || MW>719.92) insideMWDomain=false;
				if(MW<rangeMW_Training[0] || MW>rangeMW_Training[0]) insideMWDomain=false;
				
				pr.insideAD=insideFragmentDomain && insideMWDomain;
				
				
			} else if (propertyName.equals("HLC")) {
				
				EstimatedValue2 ev2=(EstimatedValue2)ev;

				if(modelName.equals("Selected")) {
					pr.pred = -Math.log10(ev2.value);
				} else {

					for(gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Model model:ev2.model) {
						if(model.name==null) continue;
						if(model.name.equals(modelName)) {
							if(model.hlcAtm!=0) {
								pr.pred=-Math.log10(model.hlcAtm);	
							}
						}
					}
				}
				
				//TODO add AD

			} else if (propertyName.equals("VP")) {

				EstimatedValue2 ev2=(EstimatedValue2)ev;

				for(gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Model model:ev2.model) {
					if(model.type.equals(modelName)) {
						pr.pred=Math.log10(model.mmHg);
					}
				}
				
				//TODO add AD

			} else if (propertyName.equals("MP")) {
				
				EstimatedValue ev2=(EstimatedValue)ev;
				
				if(modelName.equals("Adapted Joback")) {
					pr.pred = ev2.model.meltingPointAdaptedJoback;	
				} else if(modelName.equals("Gold-Ogle")) {
					pr.pred = ev2.model.meltingPointGoldOgle;
				} else if(modelName.equals("Selected")) {
					pr.pred = ev2.model.meltingPointSelected;
				}
				
				//TODO add AD
				
			} else if (propertyName.equals("BP")) {
				EstimatedValue ev2=(EstimatedValue)ev;
				pr.pred = ev2.value;
				
				//TODO add AD
				
			} else if (propertyName.equals("WS")) {

				EstimatedValue ev2=(EstimatedValue)ev;
				
				if(modelName.equals("WaterNT")) {
					pr.pred = ev2.value;// mg/L
					pr.pred /= 1000.0;// g/L
					pr.pred /= ev2.chemicalProperties.molecularWeight;// mol/L
					pr.pred = -Math.log10(pr.pred);// -Log(mol/L);

					/**
					 * Currently there is no universally accepted definition of model domain.
					 *  However, users may wish to consider the possibility that water solubility
					 * estimates are less accurate for compounds outside the MW range of the
					 * training set compounds, and/or that have more instances of a given fragment
					 * than the maximum for all training set compounds.  It is also possible that a
					 * compound may have a functional group(s) or other structural features not
					 * represented in the training set, and for which no fragment coefficient was
					 * developed.  These points should be taken into consideration when interpreting
					 * model results.
					 */
					boolean insideMWDomain=true;
					Double MW=ev2.chemicalProperties.molecularWeight;
//					if(MW<30.3 || MW>627.62) insideMWDomain=false;
					
					if(MW<rangeMW_Training[0] || MW>rangeMW_Training[0]) insideMWDomain=false;

					
					boolean insideFragmentDomain=insideFragmentDomain(ev2);
					pr.insideAD=insideFragmentDomain && insideMWDomain;


				} else if(modelName.equals("WSKOW")) {

					pr.pred = ev2.value;
					pr.pred /= 1000.0;// g/L
					pr.pred /= ev2.chemicalProperties.molecularWeight;// mol/L
					pr.pred = -Math.log10(pr.pred);// -Log(mol/L);
					
					boolean insideMWDomain=true;
					Double MW=ev2.chemicalProperties.molecularWeight;
//					if(MW<30.3 || MW>627.62) insideMWDomain=false;//TODO determine range in training file
					
					if(MW<rangeMW_Training[0] || MW>rangeMW_Training[0]) insideMWDomain=false;
					
					//Use logKow fragments://TODO need ev for logKow
//					boolean insideFragmentDomain=insideFragmentDomain(er.logKow.estimatedValue);					pr.insideAD=insideFragmentDomain && insideMWDomain;
				}

			} else if (propertyName.equals("96HR_Fish_LC50")) {
//				EcosarResult ecosarResult = getEcosar96hrFishToxicityMax(er);
//				pr.pred=ecosarResult.pred;
//				pr.insideAD=ecosarResult.insideAD;
			}

			//				System.out.println(er.dtxcid+"\t"+er.canonQsarSmiles+"\t"+pred+"\t"+er.error);
			if (pr.pred == null) {
				//					continue;
				pr.pred = Double.NaN;
			}
			return pr;
		}

//		private PredResult getPredictionResult(EstimatedValue ev,String propertyName,String modelName,Double []rangeMW_Training) {
//
//			PredResult pr=new PredResult();
//
//			pr.pred = null;
//			pr.insideAD=null;
//
//			if (propertyName.equals("LogKow")) {
//				pr.pred = ev.value;
//				boolean insideFragmentDomain=insideFragmentDomain(ev);
//				boolean insideMWDomain=true;
//				Double MW=ev.chemicalProperties.molecularWeight;
////				if(MW<18.02 || MW>719.92) insideMWDomain=false;
//				
//				if(rangeMW_Training!=null)
//					if(MW < rangeMW_Training[0] || MW > rangeMW_Training[1]) insideMWDomain=false;
//				
//				pr.insideAD=insideFragmentDomain && insideMWDomain;
//
//			} else if (propertyName.equals("HLC")) {
//				//					if(modelName.equals("Selected")) {
//				//						pr.pred = -Math.log10(ev.value);
//				//					} else {
//				//						for(gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Model model:ev.model) {
//				//							if(model.name==null) continue;
//				//							if(model.name.equals(modelName)) {
//				//								if(model.hlcAtm!=0) {
//				//									pr.pred=-Math.log10(model.hlcAtm);	
//				//								}
//				//							}
//				//						}
//				//					}
//
//				// Use bond method:
//				//						pred=-Math.log10(er.henrysLawConstant.estimatedValue.model.get(1).hlcAtm);
//				// use overall (not fair because some experimental values are actually VP/WS)
//				//						pred = -Math.log10(er.henrysLawConstant.estimatedValue.value);
//				//				} 
//
//			} else if (propertyName.equals("VP")) {
//				//				if (er.vaporPressure != null) {
//				//					//						pred = Math.log10(er.vaporPressure.estimatedValue.value);
//				//
//				//					for(gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Model model:er.vaporPressure.estimatedValue.model) {
//				//						if(model.type.equals(modelName)) {
//				//							pr.pred=Math.log10(model.mmHg);
//				//						}
//				//					}
//				//
//				//				}
//			} else if (propertyName.equals("MP")) {
//				if(modelName.equals("Adapted Joback")) {
//					pr.pred = ev.model.meltingPointAdaptedJoback;	
//				} else if(modelName.equals("Gold-Ogle")) {
//					pr.pred = ev.model.meltingPointGoldOgle;
//				} else if(modelName.equals("Selected")) {
//					pr.pred = ev.model.meltingPointSelected;
//				}
//			} else if (propertyName.equals("BP")) {
//				pr.pred = ev.value;
//			} else if (propertyName.equals("WS")) {
//
//				if(modelName.equals("WaterNT")) {
//					pr.pred = ev.value;// mg/L
//					pr.pred /= 1000.0;// g/L
//					pr.pred /= ev.chemicalProperties.molecularWeight;// mol/L
//					pr.pred = -Math.log10(pr.pred);// -Log(mol/L);
//
//					boolean insideMWDomain=true;
//					Double MW=ev.chemicalProperties.molecularWeight;
//					
////					if(MW<30.3 || MW>627.62) insideMWDomain=false;
//
//					if(rangeMW_Training!=null)
//						if(MW < rangeMW_Training[0] || MW > rangeMW_Training[1]) insideMWDomain=false;
//					
//					boolean insideFragmentDomain=insideFragmentDomain(ev);
//					pr.insideAD=insideFragmentDomain && insideMWDomain;
//
//					if(ev.chemicalProperties!=null && ev.chemicalProperties.cas.contains("27541-88-4")) {
//						System.out.println(insideFragmentDomain+"\t"+insideMWDomain+"\t"+pr.insideAD);
//					}
//
//					//					if(ev.chemicalProperties!=null && ev.chemicalProperties.cas.contains("27541-88-4")) {
//					//						System.out.println(Utilities.gson.toJson(ev));
//					//					}
//
//
//					//					System.out.println(ev.chemicalProperties.cas+"\t"+insideFragmentDomain+"\t"+insideMWDomain);
//				} else if(modelName.equals("LogKow")) {
//					pr.pred = ev.value;
//					pr.pred /= 1000.0;// g/L
//					pr.pred /= ev.chemicalProperties.molecularWeight;// mol/L
//					pr.pred = -Math.log10(pr.pred);// -Log(mol/L);
//
//					System.out.println("Need to implement AD for WS LogKow model");
//				}
//
//			} else if (propertyName.equals("96HR_Fish_LC50")) {
//				//				EcosarResult ecosarResult = getEcosar96hrFishToxicityMax(er);
//				//				pr.pred=ecosarResult.pred;
//				//				pr.insideAD=ecosarResult.insideAD;
//			}
//
//			//				System.out.println(er.dtxcid+"\t"+er.canonQsarSmiles+"\t"+pred+"\t"+er.error);
//
//
//			if (pr.pred == null) {
//				//					continue;
//				pr.pred = Double.NaN;
//			}
//			return pr;
//		}

		void lookAtBadEpisuiteExternalPredictionsFishTox() {

			String propertyName = "96HR_Fish_LC50";
			String of = "data\\episuite\\episuite validation\\" + propertyName;

			String datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
			String datasetNameExternal = "QSAR_Toolbox_96HR_Fish_LC50_v3 modeling";

			String datapointsExternalFilePath = of + File.separator + datasetName + " external set.json";
			//		createTestSetExternalDatapointsFile(datapointsExternalFilePath,fk_dataset_id_external,fk_dataset_id );

			Hashtable<String, DataPoint> htDP_external = du.getDatapoints(datapointsExternalFilePath);
			String filepathEpisuiteExternalResultsTestSet = datapointsExternalFilePath.replace(".json",
					" episuite results.json");
			//		ewss.runSmilesFilePublicApi(htDP_external,filepathEpisuiteExternalResultsTestSet);

			LinkedHashMap<String, Double> htPredEpiExternal = getEpisuitePredictions(false, propertyName,null,
					filepathEpisuiteExternalResultsTestSet,"canonQsarSmiles");

			TreeMap<Double, ModelPrediction> tmPreds = new TreeMap<>();

			for (String qsarSmiles : htDP_external.keySet()) {

				if (!htPredEpiExternal.containsKey(qsarSmiles))
					continue;

				//			if(hsSmilesEpisuiteTraining.contains(qsarSmiles)) {
				//				continue;//skip if in episuite training set
				//			}

				// System.out.println(qsarSmiles+"\t"+strExp+"\t"+strPred);

				double exp = htDP_external.get(qsarSmiles).getQsarPropertyValue();

				double pred = htPredEpiExternal.get(qsarSmiles);

				ModelPrediction mp = new ModelPrediction(qsarSmiles, exp, pred, 1);

				double negAbsErr = -Math.abs(exp - pred);// sort can sort descending

				if (pred > 10)
					System.out.println(qsarSmiles + "\t" + exp + "\t" + pred + "\t" + negAbsErr);

				tmPreds.put(negAbsErr, mp);

			}
			try {

				StringWriter sw = new StringWriter();

				sw.write(
						"<html><table border=1 cellspacing=0 cellpadding=10><caption>Worst Epi Suite predictions for LogKow</caption>");

				sw.write("<tr bgcolor=lightgray>" + "<th>QsarSmiles</th>" + "<th>Results</th>" + "</tr>");

				int counter = 0;

				DecimalFormat df = new DecimalFormat("0.00");

				for (Double key : tmPreds.keySet()) {
					counter++;

					ModelPrediction mp = tmPreds.get(key);

					// System.out.println(key+"\n"+Utilities.gson.toJson(tmPreds.get(key)));

					sw.write("<tr>");

					String imgSrc = StructureImageUtil.generateImgSrc(mp.id);

					double MW = StructureUtil.molWeightFromSmiles(mp.id);

					sw.write("<td><img src=\"" + imgSrc + "\" width=300><br>"
							//						+ev.smiles+"<br>"
							+ "exp=" + mp.exp + "<br>" + "pred=" + df.format(mp.pred) + "</td>\n");

					sw.write("<td>" + "<br>MW=" + df.format(MW) + "</td>\n");

					sw.write("</tr>");

					// if(counter==10) break;
					if (Math.abs(mp.exp - mp.pred) < 3)
						break;

				}

				sw.write("</table></html>");

				//				StringWriter sw = writeBadPredictionTable(propertyName, null, tmPreds,
				//						tmPreds);


				FileWriter fw = new FileWriter(of + File.separator + "external bad episuite.html");

				Document doc = Jsoup.parse(sw.toString());
				doc.outputSettings().indentAmount(2);
				fw.write(doc.toString());
				fw.flush();
				fw.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

		private String writeResultsByQsarSmiles(String propertyName,ModelPrediction mpEpi, Hashtable<String, ModelPrediction> htPredResQsar, Hashtable<String, Object> htPredEpiEstimatedValue) {

			DecimalFormat df=new DecimalFormat("0.00");

			String imgSrc=null;
			try {
				imgSrc = StructureImageUtil.generateImgSrc(mpEpi.id);
			} catch (Exception e) {
				e.printStackTrace();
			} 
			double MW = StructureUtil.molWeightFromSmiles(mpEpi.id);

			String strFragmentTable = "";

			if (propertyName.equals("BP") || propertyName.equals("LogKow") || propertyName.equals("WS")) {
				EstimatedValue ev = (EstimatedValue) htPredEpiEstimatedValue.get(mpEpi.id);
				//					System.out.println(Utilities.gson.toJson(ev));
				if(ev!=null)
					strFragmentTable = getFragmentTable(ev);
			}

			String dtxcid="";

			if(mpEpi.checkStructure!=null) {
				dtxcid=mpEpi.checkStructure.dsstox_dtxcid;	
			}


			String result="<td><img src=\"" + imgSrc + "\" width=300><br>" +  
					addCarriageReturns(mpEpi.id,50) + "<br>" + 
					dtxcid + "<br>";

			ModelPrediction mpTest=htPredResQsar.get(mpEpi.id);

			result+=utils.writePredComparisonTable("Episuite", "Test", df, mpEpi, mpTest);
			result+="<br>"+strFragmentTable+"</td>";

			return result;


		}

		public String addCarriageReturns(String input, int interval) {
			StringBuilder formatted = new StringBuilder(input);

			// Insert a newline character every `interval` characters
			for (int i = interval; i < formatted.length(); i += interval + 1) {
				formatted.insert(i, "<br>");
			}

			return formatted.toString();
		}

		private String writeBadPredictionTable(String title, String propertyName, String modelNameEpi, Results resultsEpi,Results resultsTest,
				Hashtable<String, Object> htPredEpiEstimatedValue,Hashtable<String, Object> htPredEpiEstimatedValueByCAS)
						throws IOException, CDKException {

			StringWriter sw = new StringWriter();

			sw.write("<html><table border=1 cellspacing=0 cellpadding=10><caption>"+title + "</caption>");

			sw.write("<tr bgcolor=lightgray>" + "<th>Results by QsarSmiles</th><th>Results by CAS</th></tr>");

			int counter = 0;

			DecimalFormat df = new DecimalFormat("0.00");

			for (ModelPrediction mp : resultsEpi.modelPredictions) {
				counter++;

				if(mp.absError()<2)break;
				// System.out.println(key+"\n"+Utilities.gson.toJson(tmPreds.get(key)));

				String resultBySmiles=writeResultsByQsarSmiles(propertyName, mp, resultsTest.htModelPredictions, htPredEpiEstimatedValue);
				ResultCAS rc=writeResultsByCAS(propertyName, modelNameEpi, mp,htPredEpiEstimatedValueByCAS);

				//				if(rc.haveBetterPrediction) {
				//					sw.write("<tr>");
				//					sw.write(resultBySmiles);
				//					sw.write(rc.result);
				//					sw.write("</tr>");
				//				}

				sw.write("<tr>");
				sw.write(resultBySmiles);
				sw.write(rc.result);
				sw.write("</tr>");


				// if(counter==10) break;
				//				if (Math.abs(key) < 3)
				//					break;

			}

			sw.write("</table></html>");
			return sw.toString();
		}

		class ResultCAS {
			String result;
			boolean havePrediction;
			boolean haveBetterPrediction;
		}

		private ResultCAS writeResultsByCAS(String propertyName, String modelNameEpi, ModelPrediction mp, Hashtable<String, Object> htPredEpiEstimatedValueByCAS) {

			if(mp.checkStructure==null) {
				ResultCAS rc=new ResultCAS();
				rc.havePrediction=false;
				rc.result="<td>Missing dtxcid</td>";
				return rc;
			}
			
			Double [] rangeMW=getEpisuiteIsisTrainingSetMW_Range(propertyName, modelNameEpi);

			DecimalFormat df=new DecimalFormat("0.00");

			//			String dtxcid=mp.dtxcid.split("\\|")[0];

			//			String sql="select casrn from qsar_models.dsstox_records\r\n"
			//					+ "where dtxcid='"+dtxcid+"' and fk_dsstox_snapshot_id=2;";
			//			String CAS=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);

			//			String sql="SELECT gs.casrn  FROM generic_substances gs\r\n"
			//					+ "			join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id\r\n"
			//					+ "			join compounds c on gsc.fk_compound_id = c.id\r\n"
			//					+ "			where dsstox_compound_id='"+dtxcid+"';";
			//			String CAS=SqlUtilities.runSQL(SqlUtilities.getConnectionDSSTOX(), sql);

			String CAS=mp.checkStructure.dsstox_casrn;

			if(CAS==null) {
				ResultCAS rc=new ResultCAS();
				rc.havePrediction=false;
				rc.result="<td>CAS is null</td>";
				return rc;
			}

			if(CAS.contains("NOCAS")) {
				ResultCAS rc=new ResultCAS();
				rc.havePrediction=false;
				rc.result="<td>Bad CAS="+CAS+"</td>";
				return rc;
			}

			//Run in real time from api:
			//			String json=EpisuiteWebserviceScript.runEpiwinByCAS(CAS);
			//			EpisuiteResults er = null;
			//			if(json!=null) {
			//				try {
			//					er = Utilities.gson.fromJson(json, EpisuiteResults.class);
			//				} catch (Exception ex) {
			//					System.out.println("Couldnt get results for "+CAS);
			//				}
			//			}
			//			if(er==null) {
			//				sw.write("<td>"+CAS+" is not in EpiSuite's database"+"</td>");
			//				return;
			//			}
			//			EstimatedValue ev=null;
			//			if (propertyName.equals("LogKow")) {
			//				ev = (EstimatedValue) er.logKow.estimatedValue;
			//				//					System.out.println(Utilities.gson.toJson(ev));
			//			}


			if(htPredEpiEstimatedValueByCAS.containsKey(CAS)) {
				EstimatedValue ev=(EstimatedValue) htPredEpiEstimatedValueByCAS.get(CAS);

				//				String smiles=er.chemicalProperties.smiles;


				if(ev.chemicalProperties==null) {
					ResultCAS rc=new ResultCAS();
					rc=new ResultCAS();
					rc.havePrediction=false;
					rc.result="<td>Missing estimate value for CAS="+CAS+"</td>";
					return rc;
				}

				String smiles="";


				smiles=ev.chemicalProperties.smiles;

				String imgSrc=null;
				try {
					imgSrc = StructureImageUtil.generateImgSrc(smiles);
				} catch (Exception e) {
					e.printStackTrace();
				} 

				String strFragmentTable = getFragmentTable(ev);
//				PredResult pr=getPredictionResult(ev, propertyName,modelNameEpi,null);
				
				PredResult pr = getPredictionResult(propertyName, modelNameEpi, ev,rangeMW);


				//				System.out.println(Utilities.gson.toJson(ev));


				ResultCAS rc=new ResultCAS();


				ModelPrediction mp2=new ModelPrediction(mp.id, mp.exp, pr.pred, -1);
				mp2.insideAD=pr.insideAD;

				rc.result="<td><img src=\"" + imgSrc + "\" width=300><br>" +  
						addCarriageReturns(smiles,50) + "<br>" + 
						CAS + "<br>" +
						utils.writePredComparisonTable("Episuite by QsarSmiles", "Episuite by CAS", df, mp, mp2) + "<br>";

				double absErr=Math.abs(mp.exp-pr.pred);

				//				System.out.println(mp.id+"\t"+mp.absError()+"\t"+absErr);
				if(absErr<mp.absError()) {
					rc.haveBetterPrediction=true;
				} 



				rc.result+=("<br><br>"+strFragmentTable+"</td>\n");
				rc.havePrediction=true;

				//				System.out.println(CAS+"\t"+mp.exp+"\t"+df.format(pred));
				return rc;

			} else {
				ResultCAS rc=new ResultCAS();
				rc.havePrediction=false;
				rc.result="<td>"+CAS+" no prediction</td>";
				return(rc);
			}

		}



		private PredResult getEcosar96hrFishToxicityMax(EpisuiteResults er) {

			PredResult pr=new PredResult();
			if (er.ecosar == null) return pr;

			int predCount=0;

			//			System.out.println(er.smiles);
			//			inEcosarTrainingSet(er.ecosar.output);

			for (ModelResult mr : er.ecosar.modelResults) {
				if (!mr.organism.equals("Fish"))
					continue;
				if (!mr.endpoint.equals("LC50"))
					continue;
				if (!mr.duration.equals("96-hr"))
					continue;

				//							System.out.println(Utilities.gson.toJson(mr));

				double predNew = mr.concentration;// mg/L value
				predNew /= 1000.0;// g/L
				predNew /= er.chemicalProperties.molecularWeight;// mol/L
				predNew = -Math.log10(predNew);// -logM

				predCount++;

				if (pr.pred == null || predNew > pr.pred) {
					pr.pred = predNew;
					pr.qsarClass=mr.qsarClass;

					if(er.ecosar.parameters.logKow.value > mr.maxLogKow) {
						pr.insideAD=false;	
					} else {
						pr.insideAD=true;
					}
				}
			}

//			System.out.println(er.canonQsarSmiles+"\t"+predCount);
			//		System.out.println(er.smiles+"\t"+pred);
			return pr;
		}
		
		
		private PredResult getEcosar96hrFishToxicityMedian(EpisuiteResults er) {

			PredResult pr=new PredResult();
			if (er.ecosar == null) return pr;

			//			System.out.println(er.smiles);
			//			inEcosarTrainingSet(er.ecosar.output);
			
			List<Double>vals=new ArrayList<>();
			
			for (ModelResult mr : er.ecosar.modelResults) {
				if (!mr.organism.equals("Fish"))
					continue;
				if (!mr.endpoint.equals("LC50"))
					continue;
				if (!mr.duration.equals("96-hr"))
					continue;

				//							System.out.println(Utilities.gson.toJson(mr));

				double predNew = mr.concentration;// mg/L value
				predNew /= 1000.0;// g/L
				predNew /= er.chemicalProperties.molecularWeight;// mol/L
				predNew = -Math.log10(predNew);// -logM

				if(er.ecosar.parameters.logKow.value < mr.maxLogKow) {
					vals.add(predNew);
				}
			}
			
			if(vals.size()>0) pr.insideAD=true;
			else pr.insideAD=false;
			
			pr.pred=calculateMedian(vals);

//			System.out.println(er.canonQsarSmiles+"\t"+predCount);

			//		System.out.println(er.smiles+"\t"+pred);


			return pr;
		}

		
		public static Double calculateMedian(List<Double> vals) {
	        if (vals == null || vals.isEmpty()) {
	            return null; // Return null if the list is empty
	        }

	        // Sort the list
	        Collections.sort(vals);

	        int size = vals.size();
	        if (size % 2 == 1) {
	            // If the list size is odd, return the middle element
	            return vals.get(size / 2);
	        } else {
	            // If the list size is even, return the average of the two middle elements
	            double middle1 = vals.get((size / 2) - 1);
	            double middle2 = vals.get(size / 2);
	            return (middle1 + middle2) / 2.0;
	        }
	    }

		public LinkedHashMap<String, Double> getEpisuitePredictions(boolean writeFile, String propertyName,String modelName,
				String filepathJson,String key) {

			try {
				
				Double [] rangeMW=null;
				
				if(!propertyName.equals("96HR_Fish_LC50")) {
					rangeMW=getEpisuiteIsisTrainingSetMW_Range(propertyName, modelName);
				}


				BufferedReader br = new BufferedReader(new FileReader(filepathJson));

				FileWriter fw = null;

				if (writeFile) {
					fw = new FileWriter(filepathJson.replace(".json", ".tsv"));
					fw.write("dtxcid\tqsarSmiles\tpred\terror\r\n");
				}

				LinkedHashMap<String, Double> htPreds = new LinkedHashMap<>();


				int countPredicted=0;

				while (true) {

					String Line = br.readLine();

					if (Line == null)
						break;

					EpisuiteResults er = null;

					try {
						er = Utilities.gson.fromJson(Line, EpisuiteResults.class);
					} catch (Exception ex) {
						continue;
					}

					PredResult pr = getPredictionResult(propertyName, modelName, er, rangeMW);

					//				System.out.println(er.dtxcid+"\t"+er.canonQsarSmiles+"\t"+pred+"\t"+er.error);

					if (writeFile)
						fw.write(er.dtxcid + "\t" + er.canonQsarSmiles + "\t" + pr.pred + "\t" + er.error + "\r\n");


					if(key.equals("canonQsarSmiles")) {						
						if(er.canonQsarSmiles==null) continue;
						htPreds.put(er.canonQsarSmiles, pr.pred);
						countPredicted++;
					} else if (key.equalsIgnoreCase("cas")) {
						if(er.chemicalProperties==null)	continue;
						String CAS2=er.chemicalProperties.cas;
						while(CAS2.startsWith("0")) CAS2=CAS2.substring(1,CAS2.length());
						htPreds.put(CAS2, pr.pred);
						countPredicted++;
					}
				}

				br.close();

				if (writeFile) {
					fw.flush();
					fw.close();
				}

				System.out.println("countPredicted="+countPredicted);

				return htPreds;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}

		public Hashtable<String, String> getEpisuiteSmilesByCAS(String filepathJson) {

			try {

				BufferedReader br = new BufferedReader(new FileReader(filepathJson));

				Hashtable<String, String> htSmilesByCAS = new Hashtable<>();

				while (true) {

					String Line = br.readLine();

					if (Line == null)
						break;

					EpisuiteResults er = null;

					try {
						er = Utilities.gson.fromJson(Line, EpisuiteResults.class);
					} catch (Exception ex) {
						continue;
					}

					if(er.chemicalProperties==null) continue;

					String CAS2=er.chemicalProperties.cas;
					while(CAS2.startsWith("0")) CAS2=CAS2.substring(1,CAS2.length());

					if(er.chemicalProperties.smiles==null)continue;

					htSmilesByCAS.put(CAS2, er.chemicalProperties.smiles);
				}

				br.close();

				return htSmilesByCAS;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

		}


		void createSmilesFile(String propertyName) {

			String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run using our qsarSmiles";


			String propertyNameDataset=propertyName;

			if (propertyName.equals("LogKow"))
				propertyNameDataset = "LogP";

			new File(of).mkdirs();

			String datasetName = propertyNameDataset + " v1 modeling";

			if(propertyName.equals("96HR_Fish_LC50")) {
				datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
			}

			long fk_dataset_id = du.getDatasetId(datasetName);

			String datapointsFilePath = of + File.separator + datasetName + " test set.json";
			File fileDatapoints=new File(datapointsFilePath);

			if(!fileDatapoints.exists()) {
//				du.createTestSetDatapointsFile(datapointsFilePath, fk_dataset_id,true);	
				du.createTestSetDatapointsFile2(datapointsFilePath, fk_dataset_id);
			}

			Hashtable<String, DataPoint> htDP = du.getDatapoints(datapointsFilePath);

			try {
				FileWriter fw=new FileWriter(of+File.separator+propertyName+" test set qsar smiles.smi");

				//				fw.write("Smiles\tID\r\n");

				for(String smiles: htDP.keySet()) {

					DataPoint dp=htDP.get(smiles);
					fw.write(dp.getCanonQsarSmiles()+"\r\n");
					//					fw.write(dp.getCanonQsarSmiles()+"\t"+dp.getCanonQsarSmiles()+"\r\n");

				}
				fw.flush();
				fw.close();


			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}



		void createSmilesFileFromCAS (String of,String filepathEpisuiteResultsTestSet,Hashtable<String, DataPoint> htDP_CAS) {

			String propertyName="LogKow";

			Hashtable<String, String>htCAS_to_episuite_smiles=episuite.getEpisuiteSmilesByCAS(filepathEpisuiteResultsTestSet);

			try {
				FileWriter fw=new FileWriter(of+File.separator+propertyName+" test set episuite smiles.smi");

				//				fw.write("Smiles\tID\r\n");

				for (String CAS:htCAS_to_episuite_smiles.keySet()) {

					if(!htDP_CAS.containsKey(CAS)) {
						continue;
					}

					//					fw.write(htCAS_to_episuite_smiles.get(CAS)+"\t"+CAS+"\r\n");
					fw.write(htCAS_to_episuite_smiles.get(CAS)+"\r\n");

				}
				fw.flush();
				fw.close();


			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}

		void createSDFFromCAS (String propertyName, String of,String set, String filepathEpisuiteResultsTestSet,Hashtable<String, DataPoint> htDP_CAS) {

			Hashtable<String, String>htCAS_to_episuite_smiles=episuite.getEpisuiteSmilesByCAS(filepathEpisuiteResultsTestSet);


			String filePath=of+File.separator+propertyName+" "+set+" set episuite smiles.sdf";

			try {
				FileWriter fileWriter = new FileWriter(filePath);			
				SDFWriter sdfWriter = new SDFWriter(fileWriter);

				SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

				for (String CAS:htCAS_to_episuite_smiles.keySet()) {
					String smiles=htCAS_to_episuite_smiles.get(CAS);
					// Parse SMILES to create molecule
					IAtomContainer molecule = smilesParser.parseSmiles(smiles);
					molecule.setProperty("CAS", CAS);
					molecule.setProperty("SMILES", smiles);
					// Write molecule to SDF
					sdfWriter.write(molecule);
				}
				sdfWriter.flush();
				sdfWriter.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		private HashSet<String> getEcosarTrainingSmiles(String filepathEpisuiteOutput) {

			try {

				BufferedReader br = new BufferedReader(new FileReader(filepathEpisuiteOutput));

				HashSet<String> hs = new HashSet<>();

				while (true) {

					String Line = br.readLine();

					if (Line == null)
						break;
					EpisuiteResults er = null;

					er = Utilities.gson.fromJson(Line, EpisuiteResults.class);

					if (er.ecosar == null)
						continue;

					if (inEcosarTrainingSet(er.ecosar.output)) {
						hs.add(er.canonQsarSmiles);
					}

				}

				br.close();

				return hs;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}

		String getFragmentTable(EstimatedValue ev) {

			if(ev.model==null)return "";
			
			String table = "<table border=1 cellpadding=2 cellspacing=0>\n";

			if(ev.model.waterSolubility!=null) {
				table += "<tr bgcolor=lightgray><th>Fragment</th><th>Count</th><th>Training Count</th></tr>\n";
			} else {
				table += "<tr bgcolor=lightgray><th>Fragment</th><th>Count</th><th>Training Count</th><th>Validation Count</th></tr>\n";
			}
			
			for (Factor factor : ev.model.factors) {

				if (factor.description.equals("Equation Constant"))
					continue;

				if(ev.model.waterSolubility!=null) {
					if (factor.fragmentCount > factor.maxFragmentCount) table += "<tr bgcolor=pink>";
					else table += "<tr>";
					table += "<td>" + factor.description + "</td>";
					table += "<td>" + factor.fragmentCount + "</td>";
					table += "<td>" + factor.maxFragmentCount + "</td>";
					table += "</tr>";
				} else {
					if (factor.fragmentCount > factor.trainingCount) table += "<tr bgcolor=pink>";
					else table += "<tr>";
					table += "<td>" + factor.description + "</td>";
					table += "<td>" + factor.fragmentCount + "</td>";
					table += "<td>" + factor.trainingCount + "</td>";
					table += "<td>" + factor.validationCount + "</td>";
					table += "</tr>";

				}
			}
			table += "</table>";

			return table;

		}

		boolean inEcosarTrainingSet(String output) {

			String[] lines = output.split("\n");

			String strStart = "Available Measured Data from ECOSAR Training Set";
			String strStop = "ECOSAR v2.20 Class-specific Estimations";

			boolean start = false;

			for (String line : lines) {

				if (line.contains(strStart)) {
					start = true;
					continue;
				}

				if (!start)
					continue;
				if (line.contains("No Data Available"))
					break;
				if (line.contains(strStop))
					break;
				if (line.contains("---------"))
					continue;

				if (line.contains("(SW)"))
					continue;
				if (!line.contains("Fish") || !line.contains("96h") || !line.contains("LC50"))
					continue;

				//			System.out.println(line);

				return true;
			}

			return false;

		}

		boolean insideFragmentDomain(EstimatedValue ev) {

			//TODO need to check how this is handled for other models
			for (Factor factor : ev.model.factors) {
				if (factor.description.equals("Equation Constant"))continue;
				if(ev.model.waterSolubility!=null) {
//					if(ev.chemicalProperties!=null && ev.chemicalProperties.cas.contains("27541-88-4")) {
//						System.out.println(factor.description+"\t"+factor.fragmentCount+"\t"+factor.maxFragmentCount);
//					}
					if (factor.fragmentCount > factor.maxFragmentCount) {
						return false;
					}
				} else if (factor.fragmentCount > factor.trainingCount) {
					return false;
				}
			}
			return true;
		}

		private void writeBadEpisuitePredictions(String title, String propertyName,String datasetName, String modelNameEpi, String of, String filepathEpisuiteResultsTestSet,
				Results resultsEpi, Results resultsTest) {

			System.out.print("writeBadEpisuitePredictions...");

			try {
				Hashtable<String, Object> htPredEpiEstimatedValue = episuite.getEpisuiteEstimateValue(propertyName,modelNameEpi,
						filepathEpisuiteResultsTestSet,"canonQsarSmiles");


				String ofCAS="data\\episuite\\episuite validation\\"+propertyName+"\\run from episuite smiles from CAS\\";
				String filepathEpisuiteByCAS=ofCAS+datasetName+" test set episuite results.json";

				if(!new File(filepathEpisuiteByCAS).exists()) {
					System.out.println(filepathEpisuiteByCAS+" doesnt exist");
					return;
				}
				
				Hashtable<String, Object> htPredEpiEstimatedValueByCAS = episuite.getEpisuiteEstimateValue(propertyName,modelNameEpi,
						filepathEpisuiteByCAS,"cas");


//				System.out.println(Utilities.gson.toJson(htPredEpiEstimatedValueByCAS));

				System.out.print("writeBadPredictionTable...");
				String strTable = episuite.writeBadPredictionTable(title, propertyName,modelNameEpi, resultsEpi, resultsTest, htPredEpiEstimatedValue,htPredEpiEstimatedValueByCAS);
				FileWriter fw = new FileWriter(of + File.separator + propertyName+" "+modelNameEpi + " bad episuite predictions with fragments.html");

				Document doc = Jsoup.parse(strTable);
				doc.outputSettings().indentAmount(2);
				fw.write(doc.toString());
				fw.flush();
				fw.close();
				
				System.out.println("Done writing bad epi with frags");

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		void makeEpisuiteTrainingLookupByQsarSmiles(String workflow) {
		
		//		String propertyName="LogKow";
				
				String propertyName="WS";
//				String modelNameEpi="WaterNT";
				String modelNameEpi="LogKow";
		
				String listName="Episuite Isis "+propertyName+" training set";
				if(modelNameEpi!=null) listName="Episuite Isis "+propertyName+" "+modelNameEpi+" training set";

				System.out.println("listName="+listName);
				
				String of="data\\episuite\\episuite validation\\" + propertyName;
						
				episuite.getEpisuiteIsisTrainingChemicals(propertyName, modelNameEpi, of);
		
				String filePathOutMapping = of +"\\chemreg list mapping "+listName+".xlsx";
				DatasetCreatorScript dcs=new DatasetCreatorScript();
				dcs.getAutoMappingsFromChemRegList(propertyName, listName, filePathOutMapping);
		
				writeEpisuiteTrainingSmilesFile(propertyName, of, filePathOutMapping,workflow);
		
			}

		private void writeEpisuiteTrainingSmilesFile(String propertyName, String of, String filePathOutMapping,String workflow) {

			try {
				ExcelSourceReader esr=new ExcelSourceReader();
				JsonArray ja=esr.parseRecordsFromExcel(filePathOutMapping, 0, 0, true);

				FileWriter fw=new FileWriter(of+File.separator+propertyName+" training set qsar smiles.smi");
		
				int counter=0;
				
				for (JsonElement jsonElement : ja) {
		
					JsonObject jo=jsonElement.getAsJsonObject();
		
					String sourceSmiles=jsonElement.getAsJsonObject().get("SourceSmiles").getAsString();
		
					String mappedSmiles=null;
					if (!jo.get("MappedSmiles").isJsonNull()) mappedSmiles=jo.get("MappedSmiles").getAsString();	
		
					String acceptMapping=jo.get("AcceptMapping").getAsString();
		
					String finalSmiles=mappedSmiles;
					if(mappedSmiles==null) finalSmiles=sourceSmiles;
		
					String qsarSmiles=du.standardize(finalSmiles);
					//			System.out.println(acceptMapping+"\t"+sourceSmiles+"\t"+mappedSmiles+"\t"+finalSmiles+"\t"+qsarSmiles);
					System.out.println(++counter+"\t"+finalSmiles+"\t"+qsarSmiles);
		
					fw.write(qsarSmiles+"\r\n");
					fw.flush();
				}
		
				fw.close();
		
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		
		public Hashtable<String, ModelPrediction> getEpisuiteModelPredictionsDownloadableByCAS(String propertyName,
				String modelName, Hashtable<String, DataPoint> htDP_CAS, String filepathEpisuiteOutput) {
			
			try {

				BufferedReader br = new BufferedReader(new FileReader(filepathEpisuiteOutput));

				
				HashSet<String> hsCASEpisuiteTraining = new HashSet<>();

				if (propertyName.equals("LogKow") || propertyName.equals("WS")) {
					hsCASEpisuiteTraining = getEpisuiteIsisTrainingSetCAS(propertyName,modelName);
				} else if (propertyName.equals("96HR_Fish_LC50")) {
					hsCASEpisuiteTraining = getEcosarTrainingSmiles(filepathEpisuiteOutput);
				} else if (propertyName.equals("HLC")) {
					// get from Appendix G of EPI Suite user guide for HLC
				}
				
				Hashtable<String, ModelPrediction> htPreds = new Hashtable<>();

				while (true) {

					String Line = br.readLine();
					if (Line == null)
						break;

					if(Line.contains("NO CAS Match in SMILECAS")) continue;
					
					while (Line.contains("  "))
						Line=Line.replace("  "," ");
					Line=Line.trim();
					String [] vals=Line.split(" ");
					
					String cas=vals[0];
					while(cas.startsWith("0")) cas=cas.substring(1,cas.length());
					
					String strPred=vals[1].replace("(est)", "").trim();
					String strExp=vals[2].replace("(exp)", "").trim();
					String smiles=vals[3];
					
//					System.out.println(cas);
					
					if(!htDP_CAS.containsKey(cas)) continue;
//					
					DataPoint dp=htDP_CAS.get(cas);
					Double exp=dp.getQsarPropertyValue();

					int splitNum=1;
					if(hsCASEpisuiteTraining.contains(cas)) splitNum=0;
					
//					
//					if(!strExp.contains("--")) splitNum=0;
//					System.out.println(Line);
					
					double dpred=Double.parseDouble(strPred);
					
//					if(!strExp.contains("--") && splitNum==1) {
//						System.out.println(canonQsarSmiles+"\t"+splitNum+"\t"+vals[1]);	
//					}
					
					ModelPrediction mp=new ModelPrediction(cas,exp,dpred,splitNum,null);				
					mp.checkStructure=dp.checkStructure;
					htPreds.put(cas, mp);

				}

				br.close();
				return htPreds;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			
		}
		
		
		
		public Hashtable<String, ModelPrediction> getEpisuiteModelPredictionsDownloadable(String propertyName,
				String modelName, Hashtable<String, DataPoint> htDP, String filepathEpisuiteOutput) {

			
			try {

				BufferedReader br = new BufferedReader(new FileReader(filepathEpisuiteOutput));

				HashSet<String> hsSmilesEpisuiteTraining = new HashSet<>();
				HashSet<String> hsCASEpisuiteTraining = new HashSet<>();

				if (propertyName.equals("LogKow") || propertyName.equals("WS")) {
					hsSmilesEpisuiteTraining = getEpisuiteIsisTrainingSmiles2(propertyName,modelName);
					hsCASEpisuiteTraining = getEpisuiteIsisTrainingSetCAS(propertyName, modelName);
				} else if (propertyName.equals("96HR_Fish_LC50")) {
					hsSmilesEpisuiteTraining = getEcosarTrainingSmiles(filepathEpisuiteOutput);
				} else if (propertyName.equals("HLC")) {
					// get from Appendix G of EPI Suite user guide for HLC
				}
				
				Hashtable<String, ModelPrediction> htPreds = new Hashtable<>();

				while (true) {

					String Line = br.readLine();
					if (Line == null)
						break;

					
					if(Line.contains("SMILES NOTATION PROBLEM")) continue;
					
					while (Line.contains("  "))
						Line=Line.replace("  "," ");
					
					Line=Line.trim();
					
					String [] vals=Line.split(" ");
					
					String canonQsarSmiles=vals[2];
					
					if(!htDP.containsKey(canonQsarSmiles)) continue;
//					
					DataPoint dp=htDP.get(canonQsarSmiles);
					Double exp=dp.getQsarPropertyValue();

					int splitNum=1;
					if(hsSmilesEpisuiteTraining.contains(canonQsarSmiles)) splitNum=0;
					
//					String strExp=vals[1];
//					if(!strExp.contains("--")) splitNum=0;
					
					
//					System.out.println(Line);
					
					String strPred=vals[0].replace("(est)", "").trim();
					double dpred=Double.parseDouble(strPred);
					
//					if(!strExp.contains("--") && splitNum==1) {
//						System.out.println(canonQsarSmiles+"\t"+splitNum+"\t"+vals[1]);	
//					}
					
					ModelPrediction mp=new ModelPrediction(canonQsarSmiles,exp,dpred,splitNum,null);				
					mp.checkStructure=dp.checkStructure;
					htPreds.put(canonQsarSmiles, mp);

				}

				br.close();
				return htPreds;

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			
		}

		public void writeCasList(String filepath, Hashtable<String, DataPoint> htDP_CAS) {

			try {
				
				FileWriter fw=new FileWriter(filepath);
				
				for (String casrn:htDP_CAS.keySet()) {
					fw.write(casrn+"\r\n");
				}
				
				fw.flush();
				fw.close();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			
		}

		

	}



//	static class EcosarResult {
//		Double pred;
//		Boolean insideAD;
//		String qsarClass;
//	}


	class Results {

		Hashtable<String, ModelPrediction> htModelPredictions = null;
		Double minVal;
		Double maxVal;
		Map<String, Double> modelTestStatisticValues;
		List<ModelPrediction>modelPredictions;

	}

	public class Datapoint {

		String canonQsarSmiles;
		String dtxcid;
		Double exp;

		public Datapoint(String canonQsarSmiles, String dtxcid, Double exp) {
			this.canonQsarSmiles = canonQsarSmiles;
			this.dtxcid = dtxcid;
			this.exp = exp;
		}
	}

	void runAquaticTox() {

		boolean createPlot=true;
		boolean omitTrainingEpi = true;
		boolean omitNotPredictedByEpisuite=true;
		boolean omitOnlyTrainingFromOurDataset=false;//used for external set, if false omit anything that's in our modeling set

		String propertyName = "96HR_Fish_LC50";
		String of = "data\\episuite\\episuite validation\\" + propertyName;
		String units = "-Log10(M)";

		String datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		long fk_dataset_id = du.getDatasetId(datasetName);

		String datapointsFilePath = of + File.separator + datasetName + " test set.json";
		//		createTestSetDatapointsFile(datapointsFilePath,fk_dataset_id );
		Hashtable<String, DataPoint> htDP = du.getDatapoints(datapointsFilePath);
		//		System.out.println(Utilities.gson.toJson(htDP));

		String filepathEpisuiteResultsTestSet = datapointsFilePath.replace(".json", " episuite results.json");
		//		ewss.runSmilesFilePublicApi(propertyName, htDP,filepathEpisuiteResultsTestSet);		
		//		if(true)return;

		LinkedHashMap<String, Double> htPredEpi = episuite.getEpisuitePredictions(false, propertyName,null,
				filepathEpisuiteResultsTestSet,"canonQsarSmiles");
		String title = "EPI Suite API results for " + propertyName;

		//		List<ModelPrediction>mpsEpisuite=plotEpisuitePredictions(title, propertyName,units,htDP,htPredEpi,filepathEpisuiteResultsTestSet);
		//		plotResQsarModelPredictions(propertyName,"-Log10(M)",smilesPlotted,1525L);
		//		plotResQsarModelPredictions(propertyName,"-Log10(M)",mpsEpisuite,1522L);

		//		System.out.println(Utilities.gson.toJson(htPredEpi));

		long modelId=1522L;

		String modelNameEpi=null;
		
		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.createHashtableCheckStructureByDPQsarSmiles(fk_dataset_id);

		
		Hashtable<String,ModelPrediction>htMP_epi=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi, htDP,htCS_by_DP_QsarSmiles, filepathEpisuiteResultsTestSet);
		String titleEpi="Test set "+propertyName+" EPI Suite API";
		Results resultsEpi = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi, units, htMP_epi,
				 omitTrainingEpi,createPlot,of);

		Hashtable<String, Double> htPredResQsar = cm.getResQsarPredictions(propertyName, modelId);

		boolean calculateAD=false;
		Hashtable<String,ModelPrediction>htMP_test=cm.getResQsarModelPredictions(modelId,calculateAD,htCS_by_DP_QsarSmiles,applicability_domain);
		
//		Hashtable<String, Double> htPredResQsar = cm.getResQsarPredictions(propertyName, datasetName);
		//		System.out.println("htPredResQsar.size()="+htPredResQsar.size());

		String titleTest="Test set "+propertyName+" WebTest2.0";
		Results resultsTest=plot.plotPredictions(titleTest,propertyName, units, resultsEpi, htMP_test,createPlot,omitTrainingEpi,of);

		//		if(true)return;

		// ******************************************************************************************
		String datasetNameExternal = "QSAR_Toolbox_96HR_Fish_LC50_v3 modeling";
		long fk_dataset_id_external = du.getDatasetId(datasetNameExternal);
		String datapointsExternalFilePath = of + File.separator + datasetName + " external set.json";
		du.createTestSetExternalDatapointsFile(datapointsExternalFilePath,fk_dataset_id_external,fk_dataset_id ,omitOnlyTrainingFromOurDataset);

		Hashtable<String, DataPoint> htDP_external = du.getDatapoints(datapointsExternalFilePath);
		String filepathEpisuiteExternalResultsTestSet = datapointsExternalFilePath.replace(".json",
				" episuite results.json");
		//		ewss.runSmilesFilePublicApi(htDP_external,filepathEpisuiteExternalResultsTestSet);

//		LinkedHashMap<String, Double> htPredEpiExternal = episuite.getEpisuitePredictions(false, propertyName,null,
//				filepathEpisuiteExternalResultsTestSet,"canonQsarSmiles");

//		String titleExt = "EPI Suite external set";
//
//		Results resultsEpiExt = plot.plotEpisuitePredictions(propertyName+" external set: EPI Suite API", propertyName,null, units, htDP_external, htPredEpiExternal,
//				filepathEpisuiteResultsTestSet, omitTrainingEpi,createPlot,of);

		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles_ext=du.createHashtableCheckStructureByDPQsarSmiles(fk_dataset_id_external);

		
		Hashtable<String,ModelPrediction>htMP_epi_ext=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi, htDP_external,htCS_by_DP_QsarSmiles_ext, filepathEpisuiteExternalResultsTestSet);
		titleEpi="External set "+propertyName+" EPI Suite API";
		Results resultsEpiExt = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi, units, htMP_epi,
				omitTrainingEpi,createPlot,of);
		
		
		Hashtable<String,ModelPrediction>htMP_test_ext=cm.getExternalModelPredictions(modelId, fk_dataset_id_external, fk_dataset_id, omitOnlyTrainingFromOurDataset);

		
//				System.out.println(Utilities.gson.toJson(htMP_test_ext));

		Results resultsCM_Ext= plot.plotPredictions("External set "+propertyName+" WebTest2.0", propertyName, units, resultsEpiExt, htMP_test_ext,createPlot,omitTrainingEpi,of);
		
		
	}


	String runPropertyTestSet(String propertyName,String modelNameEpi, boolean createPlot,boolean omitTrainingEpi) {

		
//		episuite.getEpisuiteIsisTrainingSet(propertyName,modelNameEpi);
//		if(true)return"";
		//		boolean omitChemicalsNotPredictedByEpisuite=false;

		String units = getUnits(propertyName);
		String propertyNameDataset = propertyName;
		if (propertyName.equals("LogKow"))
			propertyNameDataset = "LogP";

		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run using our qsarSmiles";
		new File(of).mkdirs();

		String datasetName = propertyNameDataset + " v1 modeling";
		if(propertyName.equals("96HR_Fish_LC50")) {
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}
		
		long modelId = getModelID(datasetName);
		if(debug) System.out.println("ResQsar modelID=" + modelId);


		long fk_dataset_id = du.getDatasetId(datasetName);
		String datapointsFilePath = of + File.separator + datasetName + " test set.json";
		File fileDatapoints=new File(datapointsFilePath);
		if(!fileDatapoints.exists()) {
//			du.createTestSetDatapointsFile(datapointsFilePath, fk_dataset_id,true);	
			du.createTestSetDatapointsFile2(datapointsFilePath, fk_dataset_id);
		}
		Hashtable<String, DataPoint> htDP = du.getDatapoints(datapointsFilePath);
		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.createHashtableCheckStructureByDPQsarSmiles(fk_dataset_id);
		setCheckStructure(htDP, htCS_by_DP_QsarSmiles);
		
		
		//*************************************************************************************************
		//Episuite
		String filepathEpisuiteResultsTestSet = datapointsFilePath.replace(".json", " episuite results.json");
		ewss.runSmilesFilePublicApi(propertyName,modelNameEpi, htDP,filepathEpisuiteResultsTestSet);
		
		Hashtable<String,ModelPrediction>htMP_epi=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi, htDP,htCS_by_DP_QsarSmiles, filepathEpisuiteResultsTestSet);
		String titleEpi="Test set "+propertyName+" EPI Suite API";
		if(modelNameEpi!=null) {
			titleEpi="Test set "+propertyName+" EPI Suite API "+modelNameEpi;
		}
		Results resultsEpi = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi, units, htMP_epi,
				 omitTrainingEpi,createPlot,of);
//		System.out.println(Utilities.gson.toJson(resultsEpi.htModelPredictions.get("OC(CO)C(O)CO")));

		//*************************************************************************************************
		// Episuite downloadable
		episuite.createSmilesFile(propertyName);
		
		String filepathEpisuiteResultsTestSetText = of+File.separator+"LogKow test set episuite downloadable.OUT";
		Hashtable<String,ModelPrediction>htMP_epi_downloadable=episuite.getEpisuiteModelPredictionsDownloadable(propertyName, modelNameEpi, htDP,filepathEpisuiteResultsTestSetText);
		titleEpi="Test set "+propertyName+" EPI Suite Downloadable";
		if(modelNameEpi!=null) {
			titleEpi="Test set "+propertyName+" EPI Suite Downloadable "+modelNameEpi;
		}
		Results resultsEpiDownloadable = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi, units, htMP_epi_downloadable,
				 omitTrainingEpi,createPlot,of);

//		printEpisuiteDiff_API_vs_DL(resultsEpi, resultsEpiDownloadable);
		
		//*************************************************************************************************
		//TEST
		boolean calculateAD=false;
		Hashtable<String,ModelPrediction>htMP_test=cm.getResQsarModelPredictions(modelId,calculateAD,htCS_by_DP_QsarSmiles,applicability_domain);
//		Hashtable<String, Double> htPredResQsar = cm.getResQsarPredictions(propertyName, datasetName);
		//		System.out.println("htPredResQsar.size()="+htPredResQsar.size());

		String titleTest="Test set "+propertyName+" WebTest2.0";
		Results resultsTest=plot.plotPredictions(titleTest,propertyName, units, resultsEpi, htMP_test,createPlot,omitTrainingEpi,of);

		//*************************************************************************************************
		
		Double RMSE_epi=resultsEpi.modelTestStatisticValues.get("RMSE_Test");
		Double RMSE_test=resultsTest.modelTestStatisticValues.get("RMSE_Test");
		Double RMSE_percepta=null;
		DecimalFormat df=new DecimalFormat("0.00");
		
		
		String result=propertyName+"\t"+modelNameEpi+"\t"+df.format(RMSE_epi)+"\t"+df.format(RMSE_test);

		
		
		Results resultsPercepta=null;

		if(!propertyName.equals("MP") && !propertyName.equals("HLC") && !propertyName.equals("96HR_Fish_LC50")) {
//			String modelNamePercepta = percepta.getModelNamePercepta(propertyName);
//			String sql="select id from qsar_models.models where fk_source_id=7 and name='"+modelNamePercepta+"';";
//			long modelIdPercepta=Long.parseLong(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql));
//			if(debug) System.out.println("modelIdPercepta="+modelIdPercepta);
//			Hashtable<String, Double> htPredPercepta = percepta.getPerceptaPredictions(propertyName, fk_dataset_id, modelIdPercepta);

//			Hashtable<String,ModelPrediction>htMP_percepta=percepta.getPerceptaModelPredictionsFromDB_Original_Smiles(propertyName, htDP, fk_dataset_id);

			percepta.createInputSDF(propertyName, of, htDP);
			
			String smilesType="qsar smiles";
//			Hashtable<String,ModelPrediction>htMP_percepta=percepta.getPerceptaModelPredictionsFromFile(propertyName,of,htDP,smilesType);
			Hashtable<String,ModelPrediction>htMP_percepta=percepta.getPerceptaModelPredictionsFromSDF_ByQsarSmiles(propertyName,"test", of,htDP,smilesType);
			
			resultsPercepta=plot.plotPredictions("Test set "+propertyName+" Percepta", propertyName, units, resultsEpi, htMP_percepta,createPlot,omitTrainingEpi, of);
			RMSE_percepta=resultsPercepta.modelTestStatisticValues.get("RMSE_Test");

			result+="\t"+df.format(RMSE_percepta);
			//			System.out.println(Utilities.gson.toJson(htPredPercepta));
		} else {
			result+="\tN/A";
		}

		result+="\t"+resultsEpi.modelPredictions.size()+"\t"+resultsTest.modelPredictions.size()+"\t";

		if(resultsPercepta!=null) {
			result+=resultsPercepta.modelPredictions.size();
		} else {
			result+="N/A";
		}

		System.out.println(result);

		//Create text file that has predictions from all models:
		//createPredictionFile(propertyName, of, htDP,resultsEpi,resultsTest,resultsPercepta);

		//*****************************************************************************************************************************************
		
		String title="Worst Epi Suite predictions for "+ propertyName;
		episuite.writeBadEpisuitePredictions(title, propertyName,datasetName, modelNameEpi, of, filepathEpisuiteResultsTestSet, resultsEpi, resultsTest);

		this.cm.writeBadPredictions("test set",of, propertyName, "EpiSuite "+modelNameEpi, "TEST", resultsEpi, resultsTest);
		this.cm.writeBadPredictions("test set",of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi);
		this.cm.writeBadPredictions("test set",of, propertyName, "Percepta","TEST",resultsPercepta, resultsTest);

		utils.writeToJson(resultsEpi.modelPredictions,of+"\\Episuite "+modelNameEpi+" modelPredictions.json" );
		utils.writeToJson(resultsTest.modelPredictions,of+"\\Test modelPredictions.json" );

		return result;

	}


	private void printEpisuiteDiff_API_vs_DL(Results resultsEpi, Results resultsEpiDownloadable) {
		DecimalFormat df=new DecimalFormat("0.00");
		for(ModelPrediction mpDL:resultsEpiDownloadable.modelPredictions) {
			if(!resultsEpi.htModelPredictions.containsKey(mpDL.id)) continue;
			ModelPrediction mpAPI=resultsEpi.htModelPredictions.get(mpDL.id);
			double diff=Math.abs(mpDL.pred-mpAPI.pred);
			if(diff>0.5)
				System.out.println(mpDL.id+"\t"+mpDL.exp+"\t"+mpDL.pred+"\t"+df.format(mpAPI.pred));
		}
	}


	private void setCheckStructure(Hashtable<String, DataPoint> htDP,
			Hashtable<String, CheckStructure> htCS_by_DP_QsarSmiles) {
		for (String qsarSmiles:htDP.keySet()) {
			if(htCS_by_DP_QsarSmiles.containsKey(qsarSmiles)) {
				DataPoint dp=htDP.get(qsarSmiles);
				dp.checkStructure=htCS_by_DP_QsarSmiles.get(qsarSmiles);//set check structure
			}
		}
	}


	public static Long getModelID(String datasetName) {
//		String sqlModelId = "select * from qsar_models.models where dataset_name like '" + datasetName
//				+ "' and splitting_name='RND_REPRESENTATIVE' "
//				+ " and descriptor_set_name='WebTEST-default'"
//				+ "and fk_descriptor_embedding_id is not null;";
		
		String sqlModelId="select * from qsar_models.models\r\n"
				+ "         join qsar_models.methods m2 on models.fk_method_id = m2.id\r\n"
				+ "         where dataset_name ='"+datasetName+"'\r\n"
				+ "           and splitting_name='RND_REPRESENTATIVE'\r\n"
				+ "           and m2.name='xgb_regressor_1.4'\r\n"
				+ "           and descriptor_set_name='WebTEST-default'\r\n"
				+ "           and fk_descriptor_embedding_id is not null;";
				
		
		String strModelId = SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlModelId);
		
		
		System.out.println(sqlModelId);
		
//		System.out.println("TEST model id="+strModelId);
		
		Long modelId = Long.parseLong(strModelId);
		return modelId;
	}
	
	
	void runPropertyExternalSet(String propertyName,String modelNameEpi, boolean createPlot,boolean omitTrainingEpi) {

		
		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run using our qsarSmiles";
		
		boolean omitOnlyTrainingFromOurDataset=false;
		
		String propertyNameDataset = propertyName;
		if (propertyName.equals("LogKow"))	propertyNameDataset = "LogP";

		String units = getUnits(propertyName);
		
		String datasetName = propertyNameDataset + " v1 modeling";
		if(propertyName.equals("96HR_Fish_LC50")) datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		long fk_dataset_id = du.getDatasetId(datasetName);

		long modelId = getModelID(datasetName);
		
		String datasetNameExternal=null;

		if(propertyName.equals("LogKow")) {
			datasetNameExternal="exp_prop_LOG_KOW_external_validation";
		}

		long fk_dataset_id_external = du.getDatasetId(datasetNameExternal);
		String datapointsFilePathExternal = of + File.separator + datasetName + " external set.json";

		if(new File(datapointsFilePathExternal).exists()) {
			du.createTestSetExternalDatapointsFile(datapointsFilePathExternal,fk_dataset_id_external,fk_dataset_id ,omitOnlyTrainingFromOurDataset);
		}
		
		Hashtable<String, DataPoint> htDP_external = du.getDatapoints(datapointsFilePathExternal);
		String filepathEpisuiteExternalResultsTestSet = datapointsFilePathExternal.replace(".json",
				" episuite results.json");
		
//		System.out.println("htDP_external.size()="+htDP_external.size());
		
				
//		ewss.runSmilesFilePublicApi(propertyName, htDP_external,filepathEpisuiteExternalResultsTestSet);
		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles_ext=du.createHashtableCheckStructureByDPQsarSmilesExternal(fk_dataset_id_external);

		
		//***********************************************************************************************
		// EPI Suite
		Hashtable<String,ModelPrediction>htMP_epi=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi, htDP_external,htCS_by_DP_QsarSmiles_ext, filepathEpisuiteExternalResultsTestSet);
		String titleEpi="Test set "+propertyName+" EPI Suite API";
		if(modelNameEpi!=null) {
			titleEpi="Test set "+propertyName+" EPI Suite API "+modelNameEpi;
		}
		Results resultsEpi = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi, units, htMP_epi,
				 omitTrainingEpi,createPlot,of);

		boolean calculateAD=false;
		Hashtable<String,ModelPrediction>htMP_test=cm.getExternalModelPredictions(modelId, fk_dataset_id_external, fk_dataset_id, omitOnlyTrainingFromOurDataset);
		
//		Hashtable<String, Double> htPredResQsar = cm.getResQsarPredictions(propertyName, datasetName);
		//		System.out.println("htPredResQsar.size()="+htPredResQsar.size());

		String titleTest="Test set "+propertyName+" WebTest2.0";
		Results resultsTest=plot.plotPredictions(titleTest,propertyName, units, resultsEpi, htMP_test,createPlot,omitTrainingEpi,of);

		System.out.println(htMP_epi.size());
		System.out.println(htMP_test.size());
				
	}


	String runPropertyByRevisedDsstoxQsarReadySmiles(String propertyName,String modelNameEpi, boolean createPlot,boolean omitTrainingEpi,String workflow) {

		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run from revised dsstox smiles";
		new File(of).mkdirs();
		String units = getUnits(propertyName);

		String propertyNameDataset = propertyName;
		if (propertyName.equals("LogKow"))
			propertyNameDataset = "LogP";
		String datasetName = propertyNameDataset + " v1 modeling";
		if(propertyName.equals("96HR_Fish_LC50")) {
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}
		long fk_dataset_id = du.getDatasetId(datasetName);

		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.createHashtableCheckStructureByDPQsarSmiles(fk_dataset_id);
		

		String datapointsFilePath = of + File.separator + datasetName + " test set.json";
		
//		File fileDatapoints=new File(datapointsFilePath);
//		if(!fileDatapoints.exists()) {
//			du.createTestSetDatapointsFile(datapointsFilePath, fk_dataset_id);	
//		}
//		Hashtable<String, DataPoint> htDP_by_dp_qsar_smiles = du.getDatapoints(datapointsFilePath);

		List<DataPoint>testSetDPs=du.getTestSetDatapoints(fk_dataset_id);//just pull straight from database
		Hashtable<String, DataPoint> htDP_by_dp_qsar_smiles = new Hashtable<>();
		for (DataPoint dp : testSetDPs) htDP_by_dp_qsar_smiles.put(dp.getCanonQsarSmiles(), dp);

		LinkedHashMap<String, SmilesToQsarSmiles>htSmilesToQsarSmiles=du.getQsarSmilesHashtableRevised(of+File.separator+"dsstox_smiles to qsar smiles "+workflow+".json", htCS_by_DP_QsarSmiles,workflow);
		Hashtable<String, DataPoint> htDP_by_dsstox_qsar_smiles=du.createRevisedDataPointHashtableByQsarSmiles(htDP_by_dp_qsar_smiles, htCS_by_DP_QsarSmiles, htSmilesToQsarSmiles);
			
		
		String filepathEpisuiteResultsTestSet = datapointsFilePath.replace(".json", " episuite results.json");
		ewss.runSmilesFilePublicApi(propertyName,modelNameEpi, htDP_by_dsstox_qsar_smiles,filepathEpisuiteResultsTestSet);
		
		
		Hashtable<String,ModelPrediction>htMP_epi=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi, htDP_by_dsstox_qsar_smiles,htCS_by_DP_QsarSmiles, filepathEpisuiteResultsTestSet);

		String titleEpi="Test set "+propertyName+" EPI Suite API";
		if(modelNameEpi!=null) {
			titleEpi="Test set "+propertyName+" EPI Suite API "+modelNameEpi;
		}
		Results resultsEpi = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi, units, htMP_epi,
				 omitTrainingEpi,createPlot,of);

		String sqlModelId = "select * from qsar_models.models where dataset_name like '" + datasetName
				+ "' and splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is not null;";
		String strModelId = SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlModelId);
		Long modelId = Long.parseLong(strModelId);

		boolean calculateAD=true;
		Hashtable<String,ModelPrediction>htMP_test=cm.getResQsarModelPredictionsRevisedDsstoxSmiles(of, propertyName, modelId, calculateAD, htDP_by_dsstox_qsar_smiles);
		
		
		//		System.out.println(Utilities.gson.toJson(htPredResQsar));
		Results resultsTest=plot.plotPredictions(propertyName+" WebTest2.0",propertyName, units, resultsEpi, 
				htMP_test,createPlot,omitTrainingEpi,of);


		String title="Worst Epi Suite predictions for "+ propertyName+" using revised dsstox qsar ready smiles";
		episuite.writeBadEpisuitePredictions(title, propertyName,datasetName, modelNameEpi, of, filepathEpisuiteResultsTestSet, resultsEpi, resultsTest);

		this.cm.writeBadPredictions("test set", of, propertyName, "EpiSuite", "TEST", resultsEpi, resultsTest);
		this.cm.writeBadPredictions("test set", of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi);
		
		utils.writeToJson(resultsEpi.modelPredictions,of+"\\Episuite "+modelNameEpi+" modelPredictions.json" );
		utils.writeToJson(resultsTest.modelPredictions,of+"\\Test modelPredictions.json" );
		
		return "";

	}
	
	
	String runPropertyByRevisedOriginalSmiles(String propertyName,String modelNameEpi, String modelNameEpi2, boolean createPlot,boolean omitTrainingEpi,String workflow) {

		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run from revised dsstox original smiles";
		new File(of).mkdirs();
		String units = getUnits(propertyName);

		String propertyNameDataset = propertyName;
		if (propertyName.equals("LogKow"))
			propertyNameDataset = "LogP";
		String datasetName = propertyNameDataset + " v1 modeling";
		if(propertyName.equals("96HR_Fish_LC50")) {
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}
		long fk_dataset_id = du.getDatasetId(datasetName);


		String datapointsFilePath = of + File.separator + datasetName + " test set.json";
		
//		File fileDatapoints=new File(datapointsFilePath);
//		if(!fileDatapoints.exists()) {
//			du.createTestSetDatapointsFile(datapointsFilePath, fk_dataset_id);	
//		}
//		Hashtable<String, DataPoint> htDP_by_dp_qsar_smiles = du.getDatapoints(datapointsFilePath);

//		List<DataPoint>testSetDPs=du.getTestSetDatapoints(fk_dataset_id);//just pull straight from database
		List<DataPoint>testSetDPs=du.getTestSetDatapoints2(fk_dataset_id);//just pull straight from database
		
		
		Hashtable<String, DataPoint> htDP_by_dp_qsar_smiles = new Hashtable<>();
		for (DataPoint dp : testSetDPs) htDP_by_dp_qsar_smiles.put(dp.getCanonQsarSmiles(), dp);
		
		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.createHashtableCheckStructureByDPQsarSmiles(fk_dataset_id);


		LinkedHashMap<String, SmilesToQsarSmiles>htSmilesToQsarSmiles=du.getQsarSmilesHashtableRevised(of+File.separator+"dsstox_smiles to qsar smiles "+workflow+".json", htCS_by_DP_QsarSmiles,workflow);
		Hashtable<String, DataPoint> htDP_by_dsstox_qsar_smiles=du.createRevisedDataPointHashtableByQsarSmiles(htDP_by_dp_qsar_smiles, htCS_by_DP_QsarSmiles, htSmilesToQsarSmiles);
			
		String filepathSDF=of+File.separator+propertyName+" test set dsstox smiles.sdf";//for running percepta
		du.createSDF_dsstox_smiles( filepathSDF, htDP_by_dsstox_qsar_smiles);
		
		//******************************************************************************************************
		//EPI Suite API
		String filepathEpisuiteResultsTestSet = datapointsFilePath.replace(".json", " episuite results.json");
		ewss.runSmilesFilePublicApiUsingOriginalSmiles(propertyName,modelNameEpi, htDP_by_dsstox_qsar_smiles,filepathEpisuiteResultsTestSet);
		

		Hashtable<String,ModelPrediction>htMP_epi=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi, htDP_by_dsstox_qsar_smiles,htCS_by_DP_QsarSmiles, filepathEpisuiteResultsTestSet);
		String titleEpi="Test set "+propertyName+" EPI Suite API";
		if(modelNameEpi!=null) {
			titleEpi="Test set "+propertyName+" EPI Suite API "+modelNameEpi;
		}
		Results resultsEpi = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi, units, htMP_epi,
				 omitTrainingEpi,createPlot,of);
//		try {
//			FileWriter fw=new FileWriter (of+File.separator+"epi1.json");
//			fw.write(Utilities.gson.toJson(resultsEpi.modelPredictions));
//			fw.flush();
//			fw.close();
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}


		//******************************************************************************************************
		//EPI Suite API
		
		Results resultsEpi2=null;
		if(modelNameEpi2!=null) {
			Hashtable<String,ModelPrediction>htMP_epi2=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi2, htDP_by_dsstox_qsar_smiles,htCS_by_DP_QsarSmiles, filepathEpisuiteResultsTestSet);
			titleEpi="Test set "+propertyName+" EPI Suite API";
			if(modelNameEpi2!=null) {
				titleEpi="Test set "+propertyName+" EPI Suite API "+modelNameEpi2;
			}
			resultsEpi2 = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi2, units, htMP_epi2,
					 omitTrainingEpi,createPlot,of);
		}
		
		//******************************************************************************************************
		//WebTEST2.0
		String sqlModelId = "select * from qsar_models.models where dataset_name like '" + datasetName
				+ "' and splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is not null;";
		String strModelId = SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlModelId);
		Long modelId = Long.parseLong(strModelId);

		boolean calculateAD=true;
		Hashtable<String,ModelPrediction>htMP_test=cm.getResQsarModelPredictionsRevisedDsstoxSmiles(of, propertyName, modelId, calculateAD, htDP_by_dsstox_qsar_smiles);
		
//		System.out.println(Utilities.gson.toJson(htMP_test));
		
		//		System.out.println(Utilities.gson.toJson(htPredResQsar));
		Results resultsTest=plot.plotPredictions("Test set "+propertyName+" WebTest2.0",propertyName, units, resultsEpi, 
				htMP_test,createPlot,omitTrainingEpi,of);

		//******************************************************************************************************
		// Percepta
		Results resultsPercepta=null;
		
		String smilesType="dsstox smiles";
		Hashtable<String,ModelPrediction>htMP_percepta=percepta.getPerceptaModelPredictionsFromSDF_ByQsarSmiles(propertyName,"test", of,htDP_by_dsstox_qsar_smiles,smilesType);

//		System.out.println(Utilities.gson.toJson(htMP_percepta));
		
		resultsPercepta=plot.plotPredictions("Test set "+propertyName+" Percepta", propertyName, units, resultsEpi, htMP_percepta,createPlot,omitTrainingEpi, of);
		
		//******************************************************************************************************
		
		String title="Worst Epi Suite predictions for "+ propertyName+" using revised dsstox qsar ready smiles";
//		episuite.writeBadEpisuitePredictions(title, propertyName,datasetName, modelNameEpi, of, filepathEpisuiteResultsTestSet, resultsEpi, resultsTest);
		
		this.cm.writeBadPredictions("test set", of, propertyName, modelNameEpi, "TEST", resultsEpi, resultsTest);
		
		if(modelNameEpi2!=null)
			this.cm.writeBadPredictions("test set", of, propertyName, modelNameEpi2, "TEST", resultsEpi2, resultsTest);

		this.cm.writeBadPredictions("test set", of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi);

		if(resultsPercepta!=null)
			this.cm.writeBadPredictions("test set", of, propertyName, "Percepta", "TEST", resultsTest, resultsPercepta);
		
		utils.writeToJson(resultsEpi.modelPredictions,of+"\\Episuite "+modelNameEpi+" test set modelPredictions.json" );
		utils.writeToJson(resultsTest.modelPredictions,of+"\\Test test set modelPredictions.json" );
		
		return "";

	}


	String runPropertyByCAS_EpiSuite(String propertyName,String modelNameEpi,String modelNameEpi2, boolean createPlot,boolean omitTrainingEpi,String workflow) {

		String units = getUnits(propertyName);

		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run from episuite smiles from CAS";
		new File(of).mkdirs();
		
		String propertyNameDataset = propertyName;
		if (propertyName.equals("LogKow"))
			propertyNameDataset = "LogP";
		
		String datasetName = propertyNameDataset + " v1 modeling";
		if(propertyName.equals("96HR_Fish_LC50")) {
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}
		long fk_dataset_id = du.getDatasetId(datasetName);

		String datapointsFilePath = of + File.separator + datasetName + " test set.json";
		File fileDatapoints=new File(datapointsFilePath);

		if(!fileDatapoints.exists()) {
			du.createTestSetDatapointsFile(datapointsFilePath, fk_dataset_id,false);	
			du.createTestSetDatapointsFile2(datapointsFilePath, fk_dataset_id);
		}
		
		Hashtable<String, DataPoint> htDP = du.getDatapoints(datapointsFilePath);
		//		System.out.println(Utilities.gson.toJson(htDP));

		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.createHashtableCheckStructureByDPQsarSmiles(fk_dataset_id);
		setCheckStructure(htDP, htCS_by_DP_QsarSmiles);
		
		String filepathEpisuiteResultsTestSet = datapointsFilePath.replace(".json", " episuite results.json");

//		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.getCheckStructureHashtableByQsarSmiles(of+File.separator+"scifinder structures.json");
		Hashtable<String, DataPoint> htDP_CAS = getHashtableDP_CAS(htDP,htCS_by_DP_QsarSmiles);
		//		System.out.println(listDP_CAS.size());
		
//		System.out.println("htDP_CAS: "+htDP_CAS.size()+"\n"+Utilities.gson.toJson(htDP_CAS));

		//***********************************************************************************************
		//Run episuite api calculations by CAS using API;
		ewss.runSmilesFilePublicApiByCAS(propertyName,modelNameEpi, htDP_CAS,filepathEpisuiteResultsTestSet);
		//		if(true)return "";
		Hashtable<String, ModelPrediction>htMP_epi_by_CAS=episuite.getEpisuiteModelPredictionsByCAS(propertyName, modelNameEpi, htDP_CAS, filepathEpisuiteResultsTestSet);
//		System.out.println(Utilities.gson.toJson(htMP_epi));
		System.out.println("Number of episuite preds="+htMP_epi_by_CAS.size());
		
		String titleEpi="Test set "+propertyName+" EPI Suite API by CAS";
		if(modelNameEpi!=null)	titleEpi="Test set "+propertyName+" EPI Suite API "+modelNameEpi+" by CAS";
		
		Results resultsEpi = plot.plotEpisuitePredictionsByCAS(titleEpi, propertyName,units, htMP_epi_by_CAS,
				 omitTrainingEpi,createPlot,of);
		
//		episuite.createSmilesFileFromCAS(of, filepathEpisuiteResultsTestSet, htDP_CAS);
		episuite.createSDFFromCAS(propertyName, of,"test", filepathEpisuiteResultsTestSet, htDP_CAS);

		
		//***********************************************************************************************
		//Episuite api calculations model2:
		
		if(modelNameEpi2!=null) {
			System.out.println("Here\t"+modelNameEpi2);
			Hashtable<String, ModelPrediction>htMP_epi_by_CAS2=episuite.getEpisuiteModelPredictionsByCAS(propertyName, modelNameEpi2, htDP_CAS, filepathEpisuiteResultsTestSet);
			System.out.println("Number of episuite preds="+htMP_epi_by_CAS2.size());
			
			String titleEpi2="Test set "+propertyName+" EPI Suite API by CAS";
			if(modelNameEpi!=null)	titleEpi2="Test set "+propertyName+" EPI Suite API "+modelNameEpi2+" by CAS";
			
			Results resultsEpi2 = plot.plotEpisuitePredictionsByCAS(titleEpi2, propertyName,units, htMP_epi_by_CAS2,
					 omitTrainingEpi,createPlot,of);
//			Results resultsEpi2=plot.plotPredictions(titleEpi2,propertyName, units, resultsEpi, htMP_epi_by_CAS2,createPlot,omitTrainingEpi,of);
			
		}
		
		//***********************************************************************************************
		// Episuite downloadable:
		
		boolean runDL=false;
		
		if(runDL) {
			episuite.writeCasList(datapointsFilePath.replace(".json", " cas list.txt"),htDP_CAS);
			String filepathEpisuiteResultsTestSetText = of+File.separator+propertyName+ " "+modelNameEpi+" test set.OUT";
			Hashtable<String,ModelPrediction>htMP_epi_downloadable=episuite.getEpisuiteModelPredictionsDownloadableByCAS(propertyName, modelNameEpi, htDP_CAS, filepathEpisuiteResultsTestSetText);
			
			titleEpi="Test set "+propertyName+" EPI Suite Downloadable by CAS";
			if(modelNameEpi!=null)	titleEpi="Test set "+propertyName+" EPI Suite Downloadable "+modelNameEpi+" by CAS";
			
			Results resultsEpiDownloadable = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi, units, htMP_epi_downloadable,
					 omitTrainingEpi,createPlot,of);
			
			printEpisuiteDiff_API_vs_DL(resultsEpi, resultsEpiDownloadable);
		}
		
		//***********************************************************************************************
		// Webtest2.0:		
		String sqlModelId = "select * from qsar_models.models where dataset_name like '" + datasetName
				+ "' and splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is not null;";
		String strModelId = SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlModelId);
		Long modelIdResQsar = Long.parseLong(strModelId);

		if(debug)
			System.out.println("ResQsar modelID=" + strModelId);

		boolean calculateAD=true;
		Hashtable<String, ModelPrediction>htMP_test_by_CAS=cm.getResQsarModelPredictionsByCAS(of, filepathEpisuiteResultsTestSet, modelIdResQsar, htDP_CAS,calculateAD,workflow);
		
//		System.out.println("htMP_test_by_CAS:\n"+Utilities.gson.toJson(htMP_test_by_CAS));
		Results resultsTest=plot.plotPredictions("Test set "+propertyName+" WebTest2.0 by CAS",propertyName, units, resultsEpi, htMP_test_by_CAS,createPlot,omitTrainingEpi,of);
		
		//***********************************************************************************************
		
//			String strTable = this.cm.writeBadPredictionTable(propertyName, resultsTest.modelPredictions,htEpismilesToQsarSmiles);
//		this.cm.writeBadPredictions(of, propertyName, "EpiSuite", "TEST", resultsEpi, resultsTest, smilesByCAS,null);
//		this.cm.writeBadPredictions(of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi,  null,htEpismilesToQsarSmiles);
		
//		if(true)return "";

		//		System.out.println("htPredResQsar.size()="+htPredResQsar.size());

		Double RMSE_epi=resultsEpi.modelTestStatisticValues.get("RMSE_Test");
		Double RMSE_test=resultsTest.modelTestStatisticValues.get("RMSE_Test");
		Double RMSE_percepta=null;

		DecimalFormat df=new DecimalFormat("0.0000");

		String result=propertyName+"\t"+modelNameEpi+"\t"+df.format(RMSE_epi)+"\t"+df.format(RMSE_test);

		Results resultsPercepta=null;

		if(!propertyName.equals("MP") && !propertyName.equals("HLC") && !propertyName.equals("96HR_Fish_LC50")) {
//			String modelNamePercepta = percepta.getModelNamePercepta(propertyName);
//			String sql="select id from qsar_models.models where fk_source_id=7 and name='"+modelNamePercepta+"';";
//			long modelIdPercepta=Long.parseLong(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql));
//			if(debug) System.out.println("modelIdPercepta="+modelIdPercepta);
//			Hashtable<String, Double> htPredPercepta = percepta.getPerceptaPredictions(propertyName, fk_dataset_id, modelIdPercepta);
//			Hashtable<String,ModelPrediction>htMP_percepta=percepta.getPerceptaModelPredictionsFromDB_Original_Smiles(propertyName, htDP, fk_dataset_id);
			
			
//			System.out.println("Enter percepta block");
			
			String smilesType="episuite smiles";
			Hashtable<String,ModelPrediction>htMP_percepta=percepta.getPerceptaModelPredictionsFromSDF_ByCAS(propertyName,"test",of,htDP_CAS,smilesType);
			resultsPercepta=plot.plotPredictions("Test set "+propertyName+" Percepta by CAS", propertyName, units, resultsEpi, htMP_percepta,createPlot,omitTrainingEpi, of);
			RMSE_percepta=resultsPercepta.modelTestStatisticValues.get("RMSE_Test");

			result+="\t"+df.format(RMSE_percepta);
//			System.out.println(Utilities.gson.toJson(resultsPercepta.htModelPredictions));
		} else {
			result+="\tN/A";
		}

		result+="\t"+resultsEpi.modelPredictions.size()+"\t"+resultsTest.modelPredictions.size()+"\t";

		if(resultsPercepta!=null) {
			result+=resultsPercepta.modelPredictions.size();
		} else {
//			result+="N/A";
		}

		//Create text file that has predictions from all models:
		//createPredictionFile(propertyName, of, htDP,resultsEpi,resultsTest,resultsPercepta);

		utils.writeToJson(resultsEpi.modelPredictions,of+"\\Episuite "+modelNameEpi+" modelPredictions.json" );
		utils.writeToJson(resultsTest.modelPredictions,of+"\\Test modelPredictions.json" );
		
		this.cm.writeBadPredictions("test set", of, propertyName, "EpiSuite "+modelNameEpi, "TEST", resultsEpi, resultsTest);
		this.cm.writeBadPredictions("test set", of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi);
		this.cm.writeBadPredictions("test set", of, propertyName, "Percepta","TEST", resultsPercepta, resultsTest);

		
		System.out.println(result);
		return result;		
		
				
	}

	
	String runPropertyByCAS_EpiSuite(String propertyName,List<String> modelNamesEpi,boolean createPlot,boolean omitTrainingEpi,String workflow) {

		String units = getUnits(propertyName);

		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run from episuite smiles from CAS";
		new File(of).mkdirs();
		
		String propertyNameDataset = propertyName;
		if (propertyName.equals("LogKow"))
			propertyNameDataset = "LogP";
		
		String datasetName = propertyNameDataset + " v1 modeling";
		if(propertyName.equals("96HR_Fish_LC50")) {
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}
		long fk_dataset_id = du.getDatasetId(datasetName);

		String datapointsFilePath = of + File.separator + datasetName + " test set.json";
		File fileDatapoints=new File(datapointsFilePath);

		if(!fileDatapoints.exists()) {
//			du.createTestSetDatapointsFile(datapointsFilePath, fk_dataset_id,true);	
			du.createTestSetDatapointsFile2(datapointsFilePath, fk_dataset_id);
		}

		Hashtable<String, DataPoint> htDP = du.getDatapoints(datapointsFilePath);
		//		System.out.println(Utilities.gson.toJson(htDP));

		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.createHashtableCheckStructureByDPQsarSmiles(fk_dataset_id);
		setCheckStructure(htDP, htCS_by_DP_QsarSmiles);
		
		String filepathEpisuiteResultsTestSet = datapointsFilePath.replace(".json", " episuite results.json");

//		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.getCheckStructureHashtableByQsarSmiles(of+File.separator+"scifinder structures.json");
		Hashtable<String, DataPoint> htDP_CAS = getHashtableDP_CAS(htDP,htCS_by_DP_QsarSmiles);
		//		System.out.println(listDP_CAS.size());
		
//		System.out.println("htDP_CAS: "+htDP_CAS.size()+"\n"+Utilities.gson.toJson(htDP_CAS));

		//***********************************************************************************************
		//Run episuite api calculations by CAS using API;
		
		List<Results>listResultsEpi=new ArrayList<>();
		for(String modelNameEpi:modelNamesEpi) {

			if(listResultsEpi.size()==0)
				ewss.runSmilesFilePublicApiByCAS(propertyName,modelNameEpi, htDP_CAS,filepathEpisuiteResultsTestSet);
			//		if(true)return "";

			Hashtable<String, ModelPrediction>htMP_epi_by_CAS=episuite.getEpisuiteModelPredictionsByCAS(propertyName, modelNameEpi, htDP_CAS, filepathEpisuiteResultsTestSet);
//			System.out.println(Utilities.gson.toJson(htMP_epi_by_CAS));
			System.out.println("Number of episuite preds="+htMP_epi_by_CAS.size());
			
			String titleEpi="Test set "+propertyName+" EPI Suite API by CAS";
			if(modelNameEpi!=null)	titleEpi="Test set "+propertyName+" EPI Suite API "+modelNameEpi+" by CAS";
			
			Results resultsEpi = plot.plotEpisuitePredictionsByCAS(titleEpi, propertyName,units, htMP_epi_by_CAS,
					 omitTrainingEpi,createPlot,of);
			listResultsEpi.add(resultsEpi);
//			episuite.createSmilesFileFromCAS(of, filepathEpisuiteResultsTestSet, htDP_CAS);
			
		}
		
		episuite.createSDFFromCAS(propertyName, of,"test", filepathEpisuiteResultsTestSet, htDP_CAS);

		Results resultsEpi=listResultsEpi.get(0);
		String modelNameEpi=modelNamesEpi.get(0);
		
		//***********************************************************************************************
		// Episuite downloadable:
		
//		boolean runDL=false;
//		
//		if(runDL) {
//			episuite.writeCasList(datapointsFilePath.replace(".json", " cas list.txt"),htDP_CAS);
//			String filepathEpisuiteResultsTestSetText = of+File.separator+propertyName+ " "+modelNameEpi+" test set.OUT";
//			Hashtable<String,ModelPrediction>htMP_epi_downloadable=episuite.getEpisuiteModelPredictionsDownloadableByCAS(propertyName, modelNameEpi, htDP_CAS, filepathEpisuiteResultsTestSetText);
//			
//			titleEpi="Test set "+propertyName+" EPI Suite Downloadable by CAS";
//			if(modelNameEpi!=null)	titleEpi="Test set "+propertyName+" EPI Suite Downloadable "+modelNameEpi+" by CAS";
//			
//			Results resultsEpiDownloadable = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi, units, htMP_epi_downloadable,
//					 omitTrainingEpi,createPlot,of);
//			
//			printEpisuiteDiff_API_vs_DL(resultsEpi, resultsEpiDownloadable);
//		}
		
		//***********************************************************************************************
		// Webtest2.0:		
		String sqlModelId = "select * from qsar_models.models where dataset_name like '" + datasetName
				+ "' and splitting_name='RND_REPRESENTATIVE' and fk_descriptor_embedding_id is not null;";
		String strModelId = SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sqlModelId);
		Long modelIdResQsar = Long.parseLong(strModelId);

		if(debug)
			System.out.println("ResQsar modelID=" + strModelId);

		boolean calculateAD=false;
		Hashtable<String, ModelPrediction>htMP_test_by_CAS=cm.getResQsarModelPredictionsByCAS(of, filepathEpisuiteResultsTestSet, modelIdResQsar, htDP_CAS,calculateAD,workflow);
		
//		System.out.println("htMP_test_by_CAS:\n"+Utilities.gson.toJson(htMP_test_by_CAS));
		Results resultsTest=plot.plotPredictions("Test set "+propertyName+" WebTest2.0 by CAS",propertyName, units, resultsEpi, htMP_test_by_CAS,createPlot,omitTrainingEpi,of);
		
		//***********************************************************************************************
		
//			String strTable = this.cm.writeBadPredictionTable(propertyName, resultsTest.modelPredictions,htEpismilesToQsarSmiles);
//		this.cm.writeBadPredictions(of, propertyName, "EpiSuite", "TEST", resultsEpi, resultsTest, smilesByCAS,null);
//		this.cm.writeBadPredictions(of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi,  null,htEpismilesToQsarSmiles);
		
//		if(true)return "";

		//		System.out.println("htPredResQsar.size()="+htPredResQsar.size());

		Double RMSE_epi=resultsEpi.modelTestStatisticValues.get("RMSE_Test");
		Double RMSE_test=resultsTest.modelTestStatisticValues.get("RMSE_Test");
		Double RMSE_percepta=null;

		DecimalFormat df=new DecimalFormat("0.0000");

		String result=propertyName+"\t"+modelNameEpi+"\t"+df.format(RMSE_epi)+"\t"+df.format(RMSE_test);

		Results resultsPercepta=null;

		if(!propertyName.equals("MP") && !propertyName.equals("HLC") && !propertyName.equals("96HR_Fish_LC50")) {
//			String modelNamePercepta = percepta.getModelNamePercepta(propertyName);
//			String sql="select id from qsar_models.models where fk_source_id=7 and name='"+modelNamePercepta+"';";
//			long modelIdPercepta=Long.parseLong(SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql));
//			if(debug) System.out.println("modelIdPercepta="+modelIdPercepta);
//			Hashtable<String, Double> htPredPercepta = percepta.getPerceptaPredictions(propertyName, fk_dataset_id, modelIdPercepta);
//			Hashtable<String,ModelPrediction>htMP_percepta=percepta.getPerceptaModelPredictionsFromDB_Original_Smiles(propertyName, htDP, fk_dataset_id);
			
			
//			System.out.println("Enter percepta block");
			
			String smilesType="episuite smiles";
			Hashtable<String,ModelPrediction>htMP_percepta=percepta.getPerceptaModelPredictionsFromSDF_ByCAS(propertyName,"test",of,htDP_CAS,smilesType);
			resultsPercepta=plot.plotPredictions("Test set "+propertyName+" Percepta", propertyName, units, resultsEpi, htMP_percepta,createPlot,omitTrainingEpi, of);
			RMSE_percepta=resultsPercepta.modelTestStatisticValues.get("RMSE_Test");

			result+="\t"+df.format(RMSE_percepta);
			System.out.println(Utilities.gson.toJson(resultsPercepta.htModelPredictions));
		} else {
			result+="\tN/A";
		}

		result+="\t"+resultsEpi.modelPredictions.size()+"\t"+resultsTest.modelPredictions.size()+"\t";

		if(resultsPercepta!=null) {
			result+=resultsPercepta.modelPredictions.size();
		} else {
//			result+="N/A";
		}

		//Create text file that has predictions from all models:
		//createPredictionFile(propertyName, of, htDP,resultsEpi,resultsTest,resultsPercepta);

		utils.writeToJson(resultsEpi.modelPredictions,of+"\\Episuite "+modelNameEpi+" modelPredictions.json" );
		utils.writeToJson(resultsTest.modelPredictions,of+"\\Test modelPredictions.json" );
		
		this.cm.writeBadPredictions("test set", of, propertyName, "EpiSuite "+modelNameEpi, "TEST", resultsEpi, resultsTest);
		this.cm.writeBadPredictions("test set", of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi);
		this.cm.writeBadPredictions("test set", of, propertyName, "Percepta","TEST", resultsPercepta, resultsTest);

		
		System.out.println(result);
		return result;		
		
				
	}

	private String runPropertyExternalSetByCAS(String propertyName,String modelNameEpi,String modelNameEpi2, boolean createPlot,boolean omitTrainingEpi,String workflow) {
		

		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run from episuite smiles from CAS";
		new File(of).mkdirs();

		String units = getUnits(propertyName);

		String propertyNameDataset = propertyName;
		String datasetNameExternal=null;
		
		if (propertyName.equals("LogKow")) {
			propertyNameDataset = "LogP";
			datasetNameExternal="exp_prop_LOG_KOW_external_validation";
		} else if (propertyName.equals("WS")) {
			datasetNameExternal="exp_prop_WATER_SOLUBILITY_external_validation";
		}

		String datasetName = propertyNameDataset + " v1 modeling";
		if(propertyName.equals("96HR_Fish_LC50")) {
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}
		long fk_dataset_id = du.getDatasetId(datasetName);
		long modelId=getModelID(datasetName);

		if(debug)
			System.out.println("ResQsar modelID=" + modelId);

		boolean omitOnlyTrainingFromOurDataset=false;
		
		long fk_dataset_id_external = du.getDatasetId(datasetNameExternal);
		String datapointsExternalFilePath = of + File.separator + datasetName + " external set.json";
		File fileExternalDatapoints=new File(datapointsExternalFilePath);
		
		if(!fileExternalDatapoints.exists()) {
//			du.createTestSetExternalDatapointsFile(datapointsExternalFilePath,fk_dataset_id_external,fk_dataset_id ,omitOnlyTrainingFromOurDataset);		
			du.createTestSetExternalDatapointsFile2(datapointsExternalFilePath,fk_dataset_id_external,fk_dataset_id ,omitOnlyTrainingFromOurDataset);
		}
		
		Hashtable<String, DataPoint> htDP_external = du.getDatapoints(datapointsExternalFilePath);
			
		System.out.println("htDP_external.size()="+htDP_external.size());
		
		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmilesExternal=du.createHashtableCheckStructureByDPQsarSmilesNoSplit(propertyName,fk_dataset_id_external,of);
		
//		System.out.println(Utilities.gson.toJson(htCS_by_DP_QsarSmilesExternal));
//		System.out.println("htDP_external.size()="+htDP_external.size());
		
		
		Hashtable<String, DataPoint> htDP_CAS = getHashtableDP_CAS(htDP_external,htCS_by_DP_QsarSmilesExternal);
//				System.out.println("htDP_CAS.size()="+htDP_CAS.size());
		//		System.out.println(Utilities.gson.toJson(listDP_CAS));
		
		String filepathEpisuiteExternalResultsTestSet = datapointsExternalFilePath.replace(".json",
				" episuite results.json");

		//*****************************************************************************************
		//Episuite
		
		//Run episuite api calculations by CAS;
		ewss.runSmilesFilePublicApiByCAS(propertyName,modelNameEpi, htDP_CAS,filepathEpisuiteExternalResultsTestSet);

				
//		LinkedHashMap<String, Double> htPredEpiExternal = episuite.getEpisuitePredictions(false, propertyName,modelNameEpi,
//				filepathEpisuiteExternalResultsTestSet,"cas");
//		Results resultsEpi = plot.plotEpisuitePredictionsByCAS(propertyName+" external set EPI Suite API", propertyName, units, htDP_CAS, htPredEpiExternal,
//				filepathEpisuiteExternalResultsTestSet, omitTrainingEpi,createPlot,of);

		Hashtable<String, ModelPrediction>htMP_epi_by_CAS=episuite.getEpisuiteModelPredictionsByCAS(propertyName, modelNameEpi, htDP_CAS, filepathEpisuiteExternalResultsTestSet);
//		System.out.println(Utilities.gson.toJson(htMP_epi));
//		System.out.println("Number of episuite preds="+htMP_epi_by_CAS.size());
		
		System.out.println(Utilities.gson.toJson(htMP_epi_by_CAS.get("638-67-5")));
		
		
		String titleEpi="External set "+propertyName+" EPI Suite API by CAS";
		if(modelNameEpi!=null)	titleEpi="External set "+propertyName+" EPI Suite API "+modelNameEpi+" by CAS";
		
		Results resultsEpi = plot.plotEpisuitePredictionsByCAS(titleEpi, propertyName,units, htMP_epi_by_CAS,
				 omitTrainingEpi,createPlot,of);

		episuite.createSDFFromCAS(propertyName, of,"external", filepathEpisuiteExternalResultsTestSet, htDP_CAS);
		
		//************************************************************************************************
		//Episuite model2:
		if(modelNameEpi2!=null) {
			System.out.println("Here\t"+modelNameEpi2);
			Hashtable<String, ModelPrediction>htMP_epi_by_CAS2=episuite.getEpisuiteModelPredictionsByCAS(propertyName, modelNameEpi2, htDP_CAS, filepathEpisuiteExternalResultsTestSet);
			System.out.println("Number of episuite preds="+htMP_epi_by_CAS2.size());
			
			String titleEpi2="External set "+propertyName+" EPI Suite API by CAS";
			if(modelNameEpi!=null)	titleEpi2="External set "+propertyName+" EPI Suite API "+modelNameEpi2+" by CAS";
			
			Results resultsEpi2 = plot.plotEpisuitePredictionsByCAS(titleEpi2, propertyName,units, htMP_epi_by_CAS2,
					 omitTrainingEpi,createPlot,of);
//			Results resultsEpi2=plot.plotPredictions(titleEpi2,propertyName, units, resultsEpi, htMP_epi_by_CAS2,createPlot,omitTrainingEpi,of);
			
		}
		//************************************************************************************************
		//Webtest2.0
		
		boolean calculateAD=true;
		Hashtable<String, ModelPrediction>htMP_test_by_CAS=cm.getResQsarModelPredictionsByCAS(of, filepathEpisuiteExternalResultsTestSet, modelId, htDP_CAS,calculateAD,workflow);
		Results resultsTest=plot.plotPredictions("External set "+propertyName+" WebTest2.0 by CAS",propertyName, units, resultsEpi, htMP_test_by_CAS,createPlot,omitTrainingEpi,of);
		
		//************************************************************************************************
		// Percepta
		Results resultsPercepta=null;
		
		if(!propertyName.equals("MP") && !propertyName.equals("HLC") && !propertyName.equals("96HR_Fish_LC50")) {
			String smilesType="episuite smiles";
			Hashtable<String,ModelPrediction>htMP_percepta=percepta.getPerceptaModelPredictionsFromSDF_ByCAS(propertyName,"external", of,htDP_CAS,smilesType);
			resultsPercepta=plot.plotPredictions("External set "+propertyName+" Percepta by CAS", propertyName, units, resultsEpi, htMP_percepta,createPlot,omitTrainingEpi, of);
			double RMSE_percepta=resultsPercepta.modelTestStatisticValues.get("RMSE_Test");
//			result+="\t"+df.format(RMSE_percepta);
			//			System.out.println(Utilities.gson.toJson(htPredPercepta));
		} else {
//			result+="\tN/A";
		}
		
		this.cm.writeBadPredictions("external set", of, propertyName, "EpiSuite "+modelNameEpi, "TEST", resultsEpi, resultsTest);
		this.cm.writeBadPredictions("external set", of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi);

		
		return "";
	}


	private void calculateStatsNo5BondedN(Results resultsTest)  {

		List<ModelPrediction>mps=new ArrayList<>();

		SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		
		int count=0;
		for (ModelPrediction mp : resultsTest.modelPredictions) {
//			String smiles=htEpismilesToQsarSmiles.get(mp.id).qsarSmiles;
			
			String smiles=mp.checkStructure.episuite_canon_qsar_smiles;
			
			try {
				IAtomContainer ac=sp.parseSmiles(smiles);
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(ac);

				if(cm.have5BondedNitrogen(mp, ac,smiles)) {
					
					String smiles2=du.standardize(smiles, "qsar-ready_04242025");

					IAtomContainer ac2=sp.parseSmiles(smiles2);
					AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(ac2);	
					
					if(cm.have5BondedNitrogen(mp, ac2,smiles2)) {
//						System.out.println(smiles+"\t"+smiles2);
//						System.out.println(smiles2+"\tstill broke");
						System.out.println(smiles2);
					} else {
//						System.out.println(smiles+"\t"+smiles2+"\tfixed");
					}
					
					count++;
					continue;
				}
				
				mps.add(mp);
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}
		System.out.println("Count 5 bonded nitrogen:"+count);
		
		Map<String, Double> modelTestStatisticValues = ModelStatisticCalculator.calculateContinuousStatistics(mps,
				-9999.0, DevQsarConstants.TAG_TEST);
		System.out.println("Omit 5 bonded N, stats:\n"+Utilities.gson.toJson(modelTestStatisticValues));
	}


	class DataPointCAS {
		DataPoint datapoint;
		CheckStructure checkStructure;
	}

	private Hashtable<String, DataPoint> getHashtableDP_CAS(Hashtable<String, DataPoint> htDP,
			Hashtable<String, CheckStructure> htCSByQsarSmiles) {

		Hashtable<String, DataPoint>htDP_CAS=new Hashtable<>();

		for (String qsarSmiles:htDP.keySet()) {

			DataPoint dp=htDP.get(qsarSmiles);
			if(!htCSByQsarSmiles.containsKey(qsarSmiles))continue;

			dp.checkStructure=htCSByQsarSmiles.get(qsarSmiles);
			
						//			if(!du.isValidCAS(cs.scifinder_casrn)) continue;
			if(!du.isValidCAS(dp.checkStructure.dsstox_casrn)) continue;

			//			System.out.println(Utilities.gson.toJson(dp));
			//			System.out.println(Utilities.gson.toJson(cs)+"\n");

//			DataPointCAS dpCAS=new DataPointCAS();
//			dpCAS.datapoint=dp;

//			dpCAS.checkStructure=cs;

//			htDP_CAS.put(cs.scifinder_casrn,dpCAS);
			htDP_CAS.put(dp.checkStructure.dsstox_casrn,dp);

		}

		return htDP_CAS;
	}


	private String getUnits(String propertyName) {
		String units=null;

		if (propertyName.equals("BP")|| propertyName.equals("MP")) {
			units="°C";
		} else if (propertyName.equals("LogKow")) {
			units="";
		} else if (propertyName.equals("HLC")) {
			units="-log10(atm-m3/mol)";	
		} else if (propertyName.equals("VP")) {
			units="log10(mmHg)";
		} else if (propertyName.equals("WS") || propertyName.equals("96HR_Fish_LC50")) {
			units = "-log10(M)";
		}
		return units;
	}

	/**
	 * Just calculates all stats, no plots
	 * 
	 * @param propertyName
	 * @param modelSource
	 * @param modelName
	 * @param workflow2 
	 * @param createPlot
	 */
	Map<String,Map<String, Double>> runPropertyDetailed(String propertyName,String modelSource, String modelName,String subfolder, String workflow) {

		Hashtable<String, ModelPrediction>htMPs=getModelPredictions(propertyName, modelSource, modelName, subfolder, workflow);
//		System.out.println(Utilities.gson.toJson(htMPs));
		Map<String, Map<String, Double>> mapAll = calculateStats(propertyName, modelSource, modelName, htMPs);
		return mapAll;

	}


	private Map<String, Map<String, Double>> calculateStats(String propertyName, String modelSource, String modelName,
			Hashtable<String, ModelPrediction> htMPs) {
		Map<String,Map<String, Double>>mapAll=new LinkedHashMap<>();//keeps insertion order
		
//		for (String key:htMPs.keySet()) {
//			ModelPrediction mp=htMPs.get(key);
//			if((mp.pred==null || mp.pred.equals(Double.NaN)) && modelSource.equals(DevQsarConstants.sourceNameEPISuiteAPI)) 
//				System.out.println(mp.id);
//		}
		
//		mapAll.put("All", ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,null));
//		mapAll.put("In training set",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,null));
//		mapAll.put("In prediction set",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,null));
//		mapAll.put("In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,true));
//		mapAll.put("Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,false));
//		mapAll.put("In training set / In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,true));
//		mapAll.put("In training set / Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,false));
//		mapAll.put("In prediction set / In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,true));
//		mapAll.put("In prediction / Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,false));

		mapAll.put("All", ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,null));
		mapAll.put("T",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,null));
		mapAll.put("P",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,null));
		mapAll.put("In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,true));
		mapAll.put("Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,false));
		mapAll.put("T/In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,true));
		mapAll.put("T/Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,false));
		mapAll.put("P/In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,true));
		mapAll.put("P/Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,false));

//		System.out.println(Utilities.gson.toJson(mapAll.get("In prediction set / In AD")));
		
		if(modelName==null)	System.out.println("\n"+propertyName+"\t"+modelSource);
		else System.out.println("\n"+propertyName+"\t"+modelSource+"\t"+modelName);
		
//		System.out.println("\n"+propertyName+"\t"+modelSource+"\t"+subfolder);
		System.out.print("stat\t");

		for(String key:mapAll.keySet()) {
			System.out.print(key+"\t");
		}
		System.out.println("");

		List<String>statNames=Arrays.asList(DevQsarConstants.PEARSON_RSQ,"RMSE","countTotal","countPredicted");
		for (String statName:statNames) {
			printStat(mapAll, statName);			
		}
		return mapAll;
	}
	
	
	Hashtable<String, ModelPrediction> getModelPredictions(String propertyName,String modelSource, String modelName,String subfolder, String workflow) {

		String propertyNameDataset = propertyName;
		if (propertyName.equals("LogKow"))
			propertyNameDataset = "LogP";

		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\"+subfolder;

		new File(of).mkdirs();

		String datasetName = propertyNameDataset + " v1 modeling";
		if(propertyName.equals("96HR_Fish_LC50")) {
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}
		long fk_dataset_id = du.getDatasetId(datasetName);

		String datapointsFilePath = of + File.separator + datasetName + " test set.json";
		File fileDatapoints=new File(datapointsFilePath);
		if(!fileDatapoints.exists()) {
//			du.createTestSetDatapointsFile(datapointsFilePath, fk_dataset_id,true);	
			du.createTestSetDatapointsFile2(datapointsFilePath, fk_dataset_id);
		}

		Hashtable<String, DataPoint> htDP = du.getDatapoints(datapointsFilePath);
		Hashtable<String, ModelPrediction>htMPs=null;

		String filepathEpisuiteResultsTestSet=datapointsFilePath.replace(".json", " episuite results.json");

		long modelId=getModelID(datasetName);

		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.createHashtableCheckStructureByDPQsarSmiles(fk_dataset_id);
		setCheckStructure(htDP, htCS_by_DP_QsarSmiles);
		
		
		if(subfolder.equals("run using our qsarSmiles")) {
			
			if(modelSource.equals(DevQsarConstants.sourceNameEPISuiteAPI)) {
				htMPs=episuite.getEpisuiteModelPredictions(propertyName,modelName,htDP,htCS_by_DP_QsarSmiles,filepathEpisuiteResultsTestSet);
			} else if(modelSource.equals(DevQsarConstants.SOURCE_CHEMINFORMATICS_MODULES)) {
				htMPs=cm.getResQsarModelPredictions(modelId,true,htCS_by_DP_QsarSmiles,applicability_domain);	
			} else if(modelSource.equals(DevQsarConstants.sourceNameOPERA28)) {
				htMPs=opera.getOperaModelPredictionsFromResQsar(propertyName, htDP,fk_dataset_id);
			} else if(modelSource.equals(DevQsarConstants.sourceNamePercepta2023)) {
//				htMPs=percepta.getPerceptaModelPredictionsFromDB_Original_Smiles(propertyName, htDP,fk_dataset_id);
				htMPs=percepta.getPerceptaModelPredictionsFromSDF_ByQsarSmiles(propertyName,"test", of, htDP, "qsar smiles");
			}
			
		} else if(subfolder.equals("run from episuite smiles from CAS")) {
			
			Hashtable<String, DataPoint> htDP_CAS = getHashtableDP_CAS(htDP,htCS_by_DP_QsarSmiles);
			
			if(modelSource.equals(DevQsarConstants.sourceNameEPISuiteAPI)) {
				htMPs=episuite.getEpisuiteModelPredictionsByCAS(propertyName,modelName,htDP_CAS,filepathEpisuiteResultsTestSet);
			} else if(modelSource.equals(DevQsarConstants.SOURCE_CHEMINFORMATICS_MODULES)) {
				htMPs=cm.getResQsarModelPredictionsByCAS(of, filepathEpisuiteResultsTestSet, modelId, htDP_CAS, true,workflow);
			}  else if(modelSource.equals(DevQsarConstants.sourceNamePercepta2023)) {
				htMPs=percepta.getPerceptaModelPredictionsFromSDF_ByCAS(propertyName, "test", of, htDP_CAS, "episuite smiles");
			}
			
		} else if (subfolder.equals("run from revised dsstox original smiles")) {

			LinkedHashMap<String, SmilesToQsarSmiles>htSmilesToQsarSmiles=du.getQsarSmilesHashtableRevised(of+File.separator+"dsstox_smiles to qsar smiles "+workflow+".json", htCS_by_DP_QsarSmiles,workflow);
			Hashtable<String, DataPoint> htDP_by_dsstox_qsar_smiles=du.createRevisedDataPointHashtableByQsarSmiles(htDP, htCS_by_DP_QsarSmiles, htSmilesToQsarSmiles);

			if(modelSource.equals(DevQsarConstants.sourceNameEPISuiteAPI)) {
				htMPs=episuite.getEpisuiteModelPredictions(propertyName, modelName, htDP_by_dsstox_qsar_smiles,htCS_by_DP_QsarSmiles, filepathEpisuiteResultsTestSet);
			} else if(modelSource.equals(DevQsarConstants.SOURCE_CHEMINFORMATICS_MODULES)) {
				htMPs=cm.getResQsarModelPredictionsRevisedDsstoxSmiles(of, propertyName, modelId, true, htDP_by_dsstox_qsar_smiles);
//				System.out.println(Utilities.gson.toJson(htMPs));
			} else if(modelSource.equals(DevQsarConstants.sourceNameOPERA28)) {
				htMPs=this.opera.getOperaModelPredictionsFromOutputFile(of,propertyName, "test",htDP_by_dsstox_qsar_smiles);
				
			} else if(modelSource.equals(DevQsarConstants.sourceNamePercepta2023)) {
//				htMPs=percepta.getPerceptaModelPredictionsFromDB_Original_Smiles(propertyName, htDP,fk_dataset_id);
				htMPs=percepta.getPerceptaModelPredictionsFromSDF_ByQsarSmiles(propertyName, "test", of, htDP_by_dsstox_qsar_smiles, "dsstox smiles");
//				System.out.println(Utilities.gson.toJson(htMPs));
			}
			
			
		}
		
		return htMPs;
		
		


	}
	
	
	Map<String,Map<String, Double>> runPropertyDetailedExternal(String propertyName,String modelSource, String modelName,String subfolder,String workflow) {

		String units = getUnits(propertyName);

		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\"+subfolder;
		new File(of).mkdirs();
		
		String propertyNameDataset = propertyName;

		
		String datasetName = propertyNameDataset + " v1 modeling";

		String datasetNameExternal=null;
		
		if (propertyName.equals("LogKow")) {
			propertyNameDataset = "LogP";
			datasetNameExternal="exp_prop_LOG_KOW_external_validation";
		} else if (propertyName.equals("WS")) {
			datasetNameExternal="exp_prop_WATER_SOLUBILITY_external_validation";
		}else if (propertyName.equals("96HR_Fish_LC50")) {
			datasetNameExternal="QSAR_Toolbox_96HR_Fish_LC50_v3 modeling";
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}

		long fk_dataset_id = du.getDatasetId(datasetName);
		long modelId=getModelID(datasetName);

		if(debug)
			System.out.println("ResQsar modelID=" + modelId);

		boolean omitOnlyTrainingFromOurDataset=false;
		
		long fk_dataset_id_external = du.getDatasetId(datasetNameExternal);
		String datapointsFilePath = of + File.separator + datasetName + " external set.json";
		
		Hashtable<String, DataPoint> htDP = du.getDatapoints(datapointsFilePath);
		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.createHashtableCheckStructureByDPQsarSmilesNoSplit(propertyName,fk_dataset_id_external,of);
		setCheckStructure(htDP, htCS_by_DP_QsarSmiles);
		
//		System.out.println(Utilities.gson.toJson(htCS_by_DP_QsarSmilesExternal));
		
//		System.out.println("htDP_external.size()="+htDP.size());
		//		System.out.println(Utilities.gson.toJson(listDP_CAS));
		
		String filepathEpisuiteResultsTestSet = datapointsFilePath.replace(".json",
				" episuite results.json");

		
		Hashtable<String, ModelPrediction>htMPs=null;

		
		if(subfolder.equals("run using our qsarSmiles")) {
			
			if(modelSource.equals(DevQsarConstants.sourceNameEPISuiteAPI)) {
				htMPs=episuite.getEpisuiteModelPredictions(propertyName,modelName,htDP,htCS_by_DP_QsarSmiles,filepathEpisuiteResultsTestSet);
			} else if(modelSource.equals(DevQsarConstants.SOURCE_CHEMINFORMATICS_MODULES)) {
				htMPs=cm.getResQsarModelPredictions(modelId,true,htCS_by_DP_QsarSmiles,applicability_domain);	
			} else if(modelSource.equals(DevQsarConstants.sourceNameOPERA28)) {
				htMPs=opera.getOperaModelPredictionsFromResQsar(propertyName, htDP,fk_dataset_id);
			} else if(modelSource.equals(DevQsarConstants.sourceNamePercepta2023)) {
//				htMPs=percepta.getPerceptaModelPredictionsFromDB_Original_Smiles(propertyName, htDP,fk_dataset_id);
				htMPs=percepta.getPerceptaModelPredictionsFromSDF_ByQsarSmiles(propertyName,"external", of, htDP, "qsar smiles");
			}
			
		} else if(subfolder.equals("run from episuite smiles from CAS")) {
			
			Hashtable<String, DataPoint> htDP_CAS = getHashtableDP_CAS(htDP,htCS_by_DP_QsarSmiles);
			
			if(modelSource.equals(DevQsarConstants.sourceNameEPISuiteAPI)) {
				htMPs=episuite.getEpisuiteModelPredictionsByCAS(propertyName,modelName,htDP_CAS,filepathEpisuiteResultsTestSet);
			} else if(modelSource.equals(DevQsarConstants.SOURCE_CHEMINFORMATICS_MODULES)) {
				htMPs=cm.getResQsarModelPredictionsByCAS(of, filepathEpisuiteResultsTestSet, modelId, htDP_CAS, true,workflow);
			}  else if(modelSource.equals(DevQsarConstants.sourceNamePercepta2023)) {
				htMPs=percepta.getPerceptaModelPredictionsFromSDF_ByCAS(propertyName, "test", of, htDP_CAS, "episuite smiles");
			}
			
		} else if(subfolder.equals("run from revised dsstox original smiles")) {
			
			if(modelSource.equals(DevQsarConstants.sourceNameEPISuiteAPI)) {
				htMPs=episuite.getEpisuiteModelPredictions(propertyName, modelName, htDP,htCS_by_DP_QsarSmiles, filepathEpisuiteResultsTestSet);
			} else if(modelSource.equals(DevQsarConstants.SOURCE_CHEMINFORMATICS_MODULES)) {
				htMPs=cm.getResQsarModelPredictionsRevisedDsstoxSmiles(of, propertyName, modelId, true, htDP);
//				System.out.println(Utilities.gson.toJson(htMPs));
			} else if(modelSource.equals(DevQsarConstants.sourceNameOPERA28)) {
				htMPs=this.opera.getOperaModelPredictionsFromOutputFile(of,propertyName, "external",htDP);

			}  else if(modelSource.equals(DevQsarConstants.sourceNamePercepta2023)) {
				htMPs=percepta.getPerceptaModelPredictionsFromSDF_ByQsarSmiles(propertyName,"external", of,htDP,"dsstox smiles");
			}
			
		
			
		}
		
//		System.out.println(Utilities.gson.toJson(htMPs));

		Map<String,Map<String, Double>>mapAll=new LinkedHashMap<>();//keeps insertion order
		
//		for (String key:htMPs.keySet()) {
//			ModelPrediction mp=htMPs.get(key);
//			if((mp.pred==null || mp.pred.equals(Double.NaN)) && modelSource.equals(DevQsarConstants.sourceNameEPISuiteAPI)) 
//				System.out.println(mp.id);
//		}
		
//		mapAll.put("All", ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,null));
//		mapAll.put("In training set",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,null));
//		mapAll.put("In prediction set",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,null));
//		mapAll.put("In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,true));
//		mapAll.put("Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,false));
//		mapAll.put("In training set / In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,true));
//		mapAll.put("In training set / Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,false));
//		mapAll.put("In prediction set / In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,true));
//		mapAll.put("In prediction / Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,false));

		mapAll.put("All", ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,null));
		mapAll.put("T",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,null));
		mapAll.put("P",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,null));
		mapAll.put("In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,true));
		mapAll.put("Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,null,false));
		mapAll.put("T/In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,true));
		mapAll.put("T/Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,true,false));
		mapAll.put("P/In AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,true));
		mapAll.put("P/Out of AD",ModelStatisticCalculator.calculateContinuousStatistics(htMPs,false,false));

//		System.out.println(Utilities.gson.toJson(mapAll.get("In prediction set / In AD")));
		
		if(modelName==null)	System.out.println("\n"+propertyName+"\t"+modelSource);
		else System.out.println("\n"+propertyName+"\t"+modelSource+"\t"+modelName);
		
//		System.out.println("\n"+propertyName+"\t"+modelSource+"\t"+subfolder);
		System.out.print("stat\t");

		for(String key:mapAll.keySet()) {
			System.out.print(key+"\t");
		}
		System.out.println("");

		List<String>statNames=Arrays.asList(DevQsarConstants.PEARSON_RSQ,"RMSE","countTotal","countPredicted");
		for (String statName:statNames) {
			printStat(mapAll, statName);			
		}
		
		return mapAll;

	}
	
	
	
	private void printStat(Map<String, Map<String, Double>> mapAll, String statName) {

		DecimalFormat df=new DecimalFormat("0.00");
		DecimalFormat dfI=new DecimalFormat("0");

		System.out.print(statName+"\t");
		for(String key:mapAll.keySet()) {
			Map<String, Double>stats=mapAll.get(key);
			Double statValue=stats.get(statName);

			if(statValue.equals(Double.NaN)) 
				System.out.print("N/A\t");
			else if(!statName.contains("count"))			
				System.out.print(df.format(statValue)+"\t");
			else
				System.out.print(dfI.format(statValue)+"\t");
		}
		System.out.println("");
	}


	/**
	 * Makes a tsv with predictions from all models
	 * 
	 * @param propertyName
	 * @param outputFolderPath
	 * @param htDP
	 * @param resultsEpi
	 * @param resultsTest
	 * @param resultsPercepta
	 */
	private void createPredictionFile(String propertyName, String outputFolderPath, 
			Hashtable<String, DataPoint> htDP,
			Results resultsEpi, Results resultsTest, Results resultsPercepta) {

		DecimalFormat df=new DecimalFormat("0.000");


		try {
			FileWriter fw=new FileWriter(outputFolderPath+File.separator+propertyName+" predictions.tsv");

			String header="qsarSmiles\tdtxcid\texp\tPredEpiSuite\tPredWebTest2.0\tPredPercepta";;
			System.out.println(header);

			fw.write(header+"\r\n");

			for (String qsarSmiles:htDP.keySet()) {

				DataPoint dp=htDP.get(qsarSmiles);

				String line=qsarSmiles+"\t"+dp.getQsar_dtxcid()+"\t"+df.format(dp.getQsarPropertyValue())+"\t";

				if(resultsEpi.htModelPredictions.containsKey(qsarSmiles)) {
					line+=df.format(resultsEpi.htModelPredictions.get(qsarSmiles).pred)+"\t";
				} else {
					line+="N/A\t";
				}

				if(resultsTest.htModelPredictions.containsKey(qsarSmiles)) {
					line+=df.format(resultsTest.htModelPredictions.get(qsarSmiles).pred)+"\t";
				} else {
					line+="N/A\t";
				}

				if(resultsPercepta!=null && resultsPercepta.htModelPredictions.containsKey(qsarSmiles)) {
					line+=df.format(resultsPercepta.htModelPredictions.get(qsarSmiles).pred)+"\t";
				} else {
					line+="N/A\t";
				}

				fw.write(line+"\r\n");

				System.out.println(line);

			}

			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}




	}

	void runPropertiesSimplifiedResults() {

		debug=false;
		boolean displayPlot=false;

		//		List<String>properties=Arrays.asList("LogKow","BP","MP","VP","HLC","WS");
		//		List<String>properties=Arrays.asList("HLC","WS");
		//		List<String>properties=Arrays.asList("96HR_Fish_LC50");
		//		List<String>properties=Arrays.asList("MP");
		List<String>properties=Arrays.asList("LogKow","WS");

		System.out.println("property\tModelNameEpi\tRMSE_EpiSuite\tRMSE_WebTest2.0\tRMSE_Percepta\tnumPredEpi\tnumPredWebTest\tnumPredPercepta");

		boolean omitTrainingEpi=false;

		for (String property:properties) {

			if(property.equals("LogKow") || property.equals("BP")) {
				runPropertyTestSet(property,"Group", displayPlot,omitTrainingEpi);	
			} else if(property.equals("HLC")) {
				List<String> modelNames = Arrays.asList("VP/WSOL", "Bond", "Group","Selected");
				for (String modelName:modelNames) runPropertyTestSet(property,modelName, displayPlot,omitTrainingEpi);
			} else if (property.equals("MP")) {
				List<String> modelNames = Arrays.asList("Adapted Joback", "Gold-Ogle","Selected");
				for (String modelName:modelNames) runPropertyTestSet(property,modelName, displayPlot, omitTrainingEpi);
			} else if (property.equals("VP")) {
				List<String> modelNames = Arrays.asList("Antoine","Grain","Mackay","Selected");
				for (String modelName:modelNames) runPropertyTestSet(property,modelName, displayPlot, omitTrainingEpi);
			} else if (property.equals("WS")) {
				List<String> modelNames = Arrays.asList("WaterNt","LogKow");
				for (String modelName:modelNames) runPropertyTestSet(property,modelName, displayPlot, omitTrainingEpi);
			} else if (property.equals("96HR_Fish_LC50")) {
				runPropertyTestSet(property,"LogKow/Class", displayPlot, omitTrainingEpi);
			}
		}
	}


	void createDetailedComparisonTable (String subfolder, String propertyName,String modelNameEpi,String modelNameEpi2, String workflow) {

		
//		Map<String,Map<String, Double>>mapEpi=runPropertyDetailed(propertyName,DevQsarConstants.sourceNameEPISuiteAPI,modelNameEpi,subfolder,workflow);

		Hashtable<String, ModelPrediction>htMPsEpi=getModelPredictions(propertyName, DevQsarConstants.sourceNameEPISuiteAPI,modelNameEpi, subfolder, workflow);
		Map<String, Map<String, Double>> mapEpi = calculateStats(propertyName, DevQsarConstants.sourceNameEPISuiteAPI,modelNameEpi, htMPsEpi);
		
		
		Map<String,Map<String, Double>>mapEpi2=null;
		if(modelNameEpi2!=null)
			mapEpi2=runPropertyDetailed(propertyName,DevQsarConstants.sourceNameEPISuiteAPI,modelNameEpi2,subfolder,workflow);

//		Map<String,Map<String, Double>>mapTest=runPropertyDetailed(propertyName,DevQsarConstants.sourceNameCheminformaticsModules,null,subfolder,workflow);

		
		Hashtable<String, ModelPrediction>htMPsTest=getModelPredictions(propertyName, DevQsarConstants.sourceNameCheminformaticsModules,null, subfolder, workflow);
		Map<String, Map<String, Double>> mapTest = calculateStats(propertyName, DevQsarConstants.sourceNameCheminformaticsModules,null, htMPsTest);
		
		Hashtable<String, ModelPrediction> htMPsCon = getConsensusModelPredictions(htMPsEpi, htMPsTest);
		Map<String, Map<String, Double>> mapCon = calculateStats(propertyName, "Consensus",null, htMPsCon);
		
		System.out.println("\n\nModel\tRMSE all\tRMSE T\tRMSE P\t% in T");
		getSimpleStats("EpiSuite "+modelNameEpi, mapEpi);
		if(modelNameEpi2!=null) getSimpleStats("EpiSuite "+modelNameEpi2, mapEpi2);
		getSimpleStats("CM XGB", mapTest);
		getSimpleStats("Consensus", mapCon);
		
		
		if(!propertyName.equals(propertyName96HR_Fish_LC50)) {

			Map<String,Map<String, Double>>mapPercepta=runPropertyDetailed(propertyName,DevQsarConstants.sourceNamePercepta2023,null,subfolder,workflow);
			getSimpleStats("Percepta", mapPercepta);

			Map<String,Map<String, Double>>mapOpera=runPropertyDetailed(propertyName,DevQsarConstants.sourceNameOPERA28,null,subfolder,workflow);
			getSimpleStats("OPERA2.8", mapOpera);
		}
		
		
		
		

	}


	private Hashtable<String, ModelPrediction> getConsensusModelPredictions(Hashtable<String, ModelPrediction> htMPsEpi,
			Hashtable<String, ModelPrediction> htMPsTest) {
		Hashtable<String, ModelPrediction>htMPsCon=new Hashtable<>();

		for(String smiles:htMPsTest.keySet()) {
			
			ModelPrediction mpTest=htMPsTest.get(smiles);
			if(!htMPsEpi.containsKey(smiles)) continue;
			ModelPrediction mpEpi=htMPsEpi.get(smiles);
		
			double pred=(mpTest.pred+mpEpi.pred)/2.0;
			
			Boolean insideAD=null;
			
			if(mpEpi.insideAD==null) {
				insideAD=false;
			} else {
				insideAD=mpTest.insideAD && mpEpi.insideAD;
			}
			
			int split=1;
			if(mpEpi.split==0) split=0;			
//			System.out.println(smiles+"\t"+mpTest.exp+"\t"+mpEpi.pred+"\t"+mpTest.pred+"\t"+pred);
			ModelPrediction mpCon=new ModelPrediction(smiles, mpTest.exp, pred, split,insideAD);			
			htMPsCon.put(smiles, mpCon);
		}
		return htMPsCon;
	}
	

	void getConsensusResults (String subfolder, String propertyName,String modelNameEpi,String modelNameEpi2, String workflow) {

		
		Map<String,Map<String, Double>>mapEpi=runPropertyDetailed(propertyName,DevQsarConstants.sourceNameEPISuiteAPI,modelNameEpi,subfolder,workflow);
		Map<String,Map<String, Double>>mapTest=runPropertyDetailed(propertyName,DevQsarConstants.sourceNameCheminformaticsModules,null,subfolder,workflow);

		
		

	}
	
	
	
	String getSimpleStats(String modelName, Map<String,Map<String, Double>>stats) {
		
		String statName="RMSE";
		
		DecimalFormat df=new DecimalFormat("0.00");
		DecimalFormat dfP=new DecimalFormat("0");
		
		List<String>keys=Arrays.asList("All","T","P");
		String result=modelName+"\t";
		
		Double countPredictedTraining=stats.get("T").get("countPredicted");
		Double countPredictedAll=stats.get("All").get("countPredicted");
		double fracT=countPredictedTraining/countPredictedAll*100;
		
		
//		System.out.println(modelName+"\t"+countPredictedTraining+"\t"+countPredictedAll);
		
		for(String key:keys) {
			Map<String, Double>statMap=stats.get(key);
			
			if(statMap.get(statName)==null || statMap.get(statName).equals(Double.NaN)) result+="N/A\t";
			else result+=df.format(statMap.get(statName))+"\t";
			
//			System.out.println(key+"\t"+statMap.get(statName));
		}
		
		if(countPredictedTraining==0.0) {
			result+="N/A";
		} else {
			result+=dfP.format(fracT);	
		}
		
		System.out.println(result);
		return result;
		
	}
	

	void runAll() {

		/**
		 * 1. Run by CAS (dsstox CAS mapped to datapoint, but adjusted to use scifinder cas when cas is deleted)
		 * 	  Episuite: use structure from their db
		 *    ResQsar: use qsar ready form of episuite structure- we might need extra corrections to our rules
		 *    
		 *    TODO look at bad resQsar and find missing conversions
		 *    
		 *    In this case, there will be some structures where the episuite smiles cant be fixed using our standardizer rules
		 * 
		 * 2. Run using latest dsstox smiles that tony updated
		 * 	  ResQsar: use Qsar ready structure
		 * 	  Episuite: use original smiles? or qsar ready smiles?
		 * 
		 * 3. Run latest dsstox smiles that tony updated
		 * 	  ResQsar: use Qsar ready structure
		 * 	  Episuite: use episuite ready structure
		 */

	}
	
	
	String comparePredictions(long modelId, String smiles1,String smiles2,String type,boolean useDescriptorApi,boolean dontPrintMatch,boolean initModel) {
		
		SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		
		try {
			
			DescriptorSetService descriptorSetService = new DescriptorSetServiceImpl();
			String descriptorSetName="WebTEST-default";

			DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);

			String tsv="ID\tExp\t"+descriptorSet.getHeadersTsv()+"\r\n";

			String predictionTsv1=null;
			String predictionTsv2=null;
			
			if(useDescriptorApi) {
				predictionTsv1=du.getPredictionTsv(smiles1);
				predictionTsv2=du.getPredictionTsv(smiles2);
			} else {
				IAtomContainer ac1=sp.parseSmiles(smiles1);
				DescriptorData dd1=WebTEST4.goDescriptors(ac1);
				predictionTsv1=tsv+smiles1+"\t-9999\t"+dd1.toTsv();
				IAtomContainer ac2=sp.parseSmiles(smiles2);
				DescriptorData dd2=WebTEST4.goDescriptors(ac2);
				predictionTsv2=tsv+smiles2+"\t-9999\t"+dd2.toTsv();
			}
			
			// Use descriptor api:

			
//			System.out.println(predictionTsv1);
//			System.out.println(predictionTsv2);
//			System.out.println(predictionTsv2.equals(predictionTsv1));
			
			
			List<ModelPrediction> modelPredictions1 = ps.run(modelId, predictionTsv1,initModel);
			List<ModelPrediction> modelPredictions2 = ps.run(modelId, predictionTsv2,initModel);
			
			
			double pred1=modelPredictions1.get(0).pred;
			double pred2=modelPredictions2.get(0).pred;
			
//			if(dontPrintMatch && Math.abs(pred1-pred2)<0.001) return;			
//			if(type!=null) System.out.println(type);
//
//			System.out.println(smiles1+"\t"+pred1);
//			System.out.println(smiles2+"\t"+pred2);
//			System.out.println("");
			
			return pred1+"\t"+pred2;	
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		
	}
	
	void comparePredictionsUsingDifferentDescriptorSources() {

		boolean useDescriptorApi=false;
		boolean dontPrintMatch=false;
		boolean initModel=false;
		boolean haveEmbedding=true;
		
//		String abbrev="LogP";		
//		long modelId = getModelIdResQsar(abbrev,true);
		
//		comparePredictions(modelId, "CCC(C)C(C(=O)N)NC(=O)C(CC(C)C)NC(=O)C","CC(O)=NC(CC(C)C)C(O)=NC(C(C)CC)C(=N)O","keto/enol",useDescriptorApi,dontPrintMatch);//keto - enol
//		comparePredictions(modelId, "CC=1C(Cl)=NON1=O","CC=1C(Cl)=NO[N+]1[O-]","N=O, 5 membered ring",useDescriptorApi,dontPrintMatch);//
//		comparePredictions(modelId, "NC1=CC=CC=N1=O","NC1=CC=CC=[N+]1[O-]","N=O, 6 membered ring",useDescriptorApi,dontPrintMatch);//
//		comparePredictions(modelId, "O=N(C)(C)C","[O-][N+](C)(C)C","N=O, 3 single bonds attached",useDescriptorApi,dontPrintMatch);//
//		comparePredictions(modelId, "NC1=NC(N)=NC2=C1N=CN2C3CC(N=N#N)C(CO)O3","NC1=NC(N)=NC2=C1N=CN2C3CC(N=[N+]=[N-])C(CO)O3","N=N#N",useDescriptorApi,dontPrintMatch);
//		String vals=comparePredictions(modelId, "CC(O)=NC(C)C(O)=NC(CC1=CNC2=CC=CC=C21)C(O)=NC(C)C(O)=NC(C)(C)C","CC(C(=O)NC(CC1=CNC2=C1C=CC=C2)C(=O)NC(C)C(=O)NC(C)(C)C)NC(=O)C","keto/enol",useDescriptorApi,dontPrintMatch,initModel);
//		System.out.println(abbrev+"\t"+vals);
		
		List<String>abbrevs=Arrays.asList("HLC","VP","BP","WS","LogP","MP");
		
		for (String abbrev:abbrevs) {
			long modelId = getModelIdResQsar(abbrev,"RND_REPRESENTATIVE",haveEmbedding);
			
			String vals=comparePredictions(modelId, "CC(O)=NC(C)C(O)=NC(CC1=CNC2=CC=CC=C21)C(O)=NC(C)C(O)=NC(C)(C)C","CC(C(=O)NC(CC1=CNC2=C1C=CC=C2)C(=O)NC(C)C(=O)NC(C)(C)C)NC(=O)C","keto/enol",useDescriptorApi,dontPrintMatch,initModel);
			System.out.println(abbrev+"\t"+vals);
			
		}
		
		
	}
	
	
	


	public static long getModelIdResQsar(String abbrev,String splittingName, boolean haveEmbedding) {
		String datasetName=abbrev+" v1 modeling";
		
		String sql="";
		
		if (haveEmbedding) {
			sql="select m.id from qsar_models.models m\r\n"
					+ "where dataset_name = '"+datasetName+"'\r\n"
					+ "and splitting_name='"+splittingName+"' and descriptor_set_name='WebTEST-default' "
					+ "and fk_descriptor_embedding_id is not null\r\n"
					+ "order by dataset_name;";
			
		} else {
			sql="select m.id from qsar_models.models m\r\n"
					+ "where dataset_name = '"+datasetName+"'\r\n"
					+ "and splitting_name='"+splittingName+"' and descriptor_set_name='WebTEST-default' "
					+ "and fk_descriptor_embedding_id is null\r\n"
					+ "order by dataset_name;";

		}
		

//		System.out.println(sql);
		String modelId=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);
		return Long.parseLong(modelId);
	}
	
	public static long getModelIdResQsarFromDatasetName(String datasetName,String splittingName, boolean haveEmbedding) {
		
		String sql="";
		
		if (haveEmbedding) {
			sql="select m.id from qsar_models.models m\r\n"
					+ "where dataset_name = '"+datasetName+"'\r\n"
					+ "and splitting_name='"+splittingName+"' and descriptor_set_name='WebTEST-default' "
					+ "and fk_descriptor_embedding_id is not null\r\n"
					+ "order by dataset_name;";
			
		} else {
			sql="select m.id from qsar_models.models m\r\n"
					+ "where dataset_name = '"+datasetName+"'\r\n"
					+ "and splitting_name='"+splittingName+"' and descriptor_set_name='WebTEST-default' "
					+ "and fk_descriptor_embedding_id is null\r\n"
					+ "order by dataset_name;";

		}
		

//		System.out.println(sql);
		String modelId=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);
		return Long.parseLong(modelId);
	}
	
	void compareWorkflows() {
		String propertyName="LogKow";
		
		String folder="data\\episuite\\episuite validation\\"+propertyName+"\\run from episuite smiles from CAS\\";
		
		String workflow1=EpisuiteValidation.workflowDefault;
		String workflow2=EpisuiteValidation.workflowCharlie;
		
//		String workflow1=EpisuiteValidation.workflowCharlie;
//		String workflow2=EpisuiteValidation.workflowCharlieRevised;

		String filepath1=folder+"episuite smiles to qsar smiles "+workflow1+".json";
		String filepath2=folder+"episuite smiles to qsar smiles "+workflow2+".json";
		
		
		LinkedHashMap<String,EpisuiteSmilesToQsarSmiles>htSmiles1=du.getEpisuiteSmilesToQsarSmiles(filepath1);
		LinkedHashMap<String,EpisuiteSmilesToQsarSmiles>htSmiles2=du.getEpisuiteSmilesToQsarSmiles(filepath2);
		
		long modelId=1069;
		boolean useDescriptorApi=true;
		boolean dontPrintMatch=true;
		boolean initModel=true;

		
		for(String cas:htSmiles1.keySet()) {
			
			if(!htSmiles2.containsKey(cas)) continue;
			
			String qsarSmiles1=htSmiles1.get(cas).qsarSmiles;
			String qsarSmiles2=htSmiles2.get(cas).qsarSmiles;
			
			if(qsarSmiles1==null || qsarSmiles2==null) {
				System.out.println(cas+"\t"+qsarSmiles1+"\t"+qsarSmiles2);
				continue;
			}
			
			if(qsarSmiles1.equals(qsarSmiles2)) continue;
				
			comparePredictions(modelId, qsarSmiles1,qsarSmiles2,null,useDescriptorApi,dontPrintMatch,initModel);
//			System.out.println(qsarSmiles1+"\t"+qsarSmiles2);
			
		}
 
		
		

	}
	
	
	public static void main(String[] args) {

		SciDataExpertsDescriptorValuesCalculator.configUnirest=false;
		
		EpisuiteValidation ev = new EpisuiteValidation();
		//		EpisuiteValidation.displayPlots=false;

		boolean createPlot=true;
//		boolean createPlot=false;
		boolean omitTrainingEpi=true;
		
//		ev.episuite.makeEpisuiteTrainingLookupByQsarSmiles(workflowDefault);

//		String propertyName="LogKow";
//		String modelNameEpi="KOWWIN";
//		String modelNameEpi2=null;

//		String propertyName="WS";
//		String modelNameEpi="WaterNT";
//		String modelNameEpi2="WSKOW";
		
		String propertyName=propertyName96HR_Fish_LC50;
		String modelNameEpi="Ecosar_Fish_96hr";
		String modelNameEpi2=null;


//		String propertyName="HLC";
//		String modelNameEpi="Selected";
//		String modelNameEpi2="VP/WSOL";
//		String modelNameEpi2="Bond";
//		String modelNameEpi2="Group";
//		List<String>modelNamesEpi=Arrays.asList("Selected","VP/WSOL","Bond","Group");

//		String propertyName="BP";
//		String modelNameEpi="Stein & Brown";
//		String modelNameEpi2="null";
//		List<String>modelNamesEpi=Arrays.asList("Stein & Brown");
		
		
//		String propertyName="VP";
//		List<String>modelNamesEpi=Arrays.asList("Selected","Antoine","Grain","Mackay);
		
//		String propertyName="MP";
//		String modelNameEpi="Selected";
//		String modelNameEpi2="Adapted Joback";
//		String modelNameEpi2="Gold-Ogle";
//		List<String>modelNamesEpi=Arrays.asList("Selected","Adapted Joback","Gold-Ogle");
		
		String workflow=workflowDefault;
//		String workflow=workflowCharlie;
//		String workflow=workflowCharlieRevised;
		
//		ev.runPropertyTestSet(propertyName,modelNameEpi,createPlot,omitTrainingEpi);
//		ev.runPropertyExternalSet(propertyName,modelNameEpi,createPlot,omitTrainingEpi);
//		ev.runPropertyByRevisedDsstoxQsarReadySmiles(propertyName, modelNameEpi, createPlot, omitTrainingEpi,workflow);		
//		ev.runPropertyByRevisedOriginalSmiles(propertyName, modelNameEpi,modelNameEpi2, createPlot, omitTrainingEpi,workflow);
//		ev.runPropertyByRevisedOriginalSmiles2(propertyName, modelNameEpi,modelNameEpi2, createPlot, omitTrainingEpi,workflow);
		
//		ev.compareEpi(propertyName);
		
//		ev.runPropertyByRevisedOriginalSmilesExternal(propertyName, modelNameEpi,modelNameEpi2, createPlot, omitTrainingEpi,workflow);		
//		ev.runPropertyByRevisedOriginalSmilesExternal2(propertyName, modelNameEpi,modelNameEpi2, createPlot, omitTrainingEpi,workflow);		

		
		//************************************************************************
		//		ev.applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;
		ev.applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Euclidean;
		System.out.println("AD="+ev.applicability_domain);
		ev.createDetailedComparisonTable("run from revised dsstox original smiles",propertyName,modelNameEpi,modelNameEpi2,workflow);
//		ev.createDetailedComparisonTableExternal("run from revised dsstox original smiles",propertyName,modelNameEpi,modelNameEpi2,workflow);
		
		
		

		//************************************************************************
		
		//ev.runPropertyByCAS_EpiSuite(propertyName,modelNameEpi,modelNameEpi2, createPlot,omitTrainingEpi,workflow);
//		ev.runPropertyByCAS_EpiSuite(propertyName,modelNamesEpi,createPlot,omitTrainingEpi,workflow);
//		ev.runPropertyExternalSetByCAS(propertyName,modelNameEpi,modelNameEpi2,createPlot,omitTrainingEpi,workflow);

//		if(true)return;
		
//		ev.comparePredictionsUsingDifferentDescriptorSources();
//		ev.compareWorkflows();

//		ev.episuite.lookAtBadEpisuiteExternalPredictionsFishTox();
//		ev.runAquaticTox();

//		ev.du.findBadStructures();
//		ev.du.getScifinderStructures();
				
		//		ev.du.getRecordsInDatasets();
		//		String inchiKey1DPC_smiles=StructureUtil.indigoInchikey1FromSmilesFixedH("CCO");

		System.out.println("\nAll done");
	}


	private void compareEpi(String propertyName) {

		String of="data\\episuite\\episuite validation\\LogKow\\run from revised dsstox original smiles\\";
		
		
//		FileWriter fw=new FileWriter (of+File.separator+"epi1.json");
		
		Type typeListMP = new TypeToken<List<ModelPrediction>>() {}.getType();

        // Parse the JSON file into a list of Person objects
        try {
			List<ModelPrediction> mps1 = Utilities.gson.fromJson(new FileReader(of+"epi1.json"), typeListMP);
			List<ModelPrediction> mps2 = Utilities.gson.fromJson(new FileReader(of+"epi2.json"), typeListMP);
			
			Hashtable<String,ModelPrediction>htMP1=new Hashtable<>();
			for(ModelPrediction mp:mps1) {
				htMP1.put(mp.id,mp);
			}
			
			
			for(ModelPrediction mp2:mps2) {
				if(!htMP1.containsKey(mp2.id)) {
					System.out.println(Utilities.gson.toJson(mp2));
				}
			}
			
//			System.out.println(Utilities.gson.toJson(mps1));
			System.out.println(mps1.size());
			System.out.println(mps2.size());
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}


	/**
	 * This version only plots the smiles in common
	 * 
	 * @param propertyName
	 * @param modelNameEpi
	 * @param modelNameEpi2
	 * @param createPlot
	 * @param omitTrainingEpi
	 * @param workflow
	 */
	private void runPropertyByRevisedOriginalSmiles2(String propertyName, String modelNameEpi, String modelNameEpi2,
			boolean createPlot, boolean omitTrainingEpi, String workflow) {

		boolean includeOPERA=true;
		
		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run from revised dsstox original smiles";
		new File(of).mkdirs();
		String units = getUnits(propertyName);

		String propertyNameDataset = propertyName;
		if (propertyName.equals("LogKow"))
			propertyNameDataset = "LogP";
		String datasetName = propertyNameDataset + " v1 modeling";
		if(propertyName.equals("96HR_Fish_LC50")) {
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}
		long fk_dataset_id = du.getDatasetId(datasetName);


		String datapointsFilePath = of + File.separator + datasetName + " test set.json";
		
//		File fileDatapoints=new File(datapointsFilePath);
//		if(!fileDatapoints.exists()) {
//			du.createTestSetDatapointsFile(datapointsFilePath, fk_dataset_id);	
//		}
//		Hashtable<String, DataPoint> htDP_by_dp_qsar_smiles = du.getDatapoints(datapointsFilePath);

//		List<DataPoint>testSetDPs=du.getTestSetDatapoints(fk_dataset_id);//just pull straight from database
		List<DataPoint>testSetDPs=du.getTestSetDatapoints2(fk_dataset_id);//just pull straight from database
		
		
		Hashtable<String, DataPoint> htDP_by_dp_qsar_smiles = new Hashtable<>();
		for (DataPoint dp : testSetDPs) htDP_by_dp_qsar_smiles.put(dp.getCanonQsarSmiles(), dp);
		
		
		
		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles=du.createHashtableCheckStructureByDPQsarSmiles(fk_dataset_id);

//		System.out.println(Utilities.gson.toJson(htCS_by_DP_QsarSmiles));
//		if(true)return;
//		System.out.println(Utilities.gson.toJson(htCS_by_DP_QsarSmiles));
		
		LinkedHashMap<String, SmilesToQsarSmiles>htSmilesToQsarSmiles=du.getQsarSmilesHashtableRevised(of+File.separator+"dsstox_smiles to qsar smiles "+workflow+".json", htCS_by_DP_QsarSmiles,workflow);
		Hashtable<String, DataPoint> htDP_by_dsstox_qsar_smiles=du.createRevisedDataPointHashtableByQsarSmiles(htDP_by_dp_qsar_smiles, htCS_by_DP_QsarSmiles, htSmilesToQsarSmiles);
			
//		System.out.println(Utilities.gson.toJson(htDP_by_dsstox_qsar_smiles));
		
		
		String filepathSDF=of+File.separator+propertyName+" test set dsstox smiles.sdf";//for running percepta
		du.createSDF_dsstox_smiles( filepathSDF, htDP_by_dsstox_qsar_smiles);
		
		//****************************************************************************************

		List<Hashtable<String,ModelPrediction>>listHtMP=new ArrayList<>();
		
		//EPI Suite API
		String filepathEpisuiteResultsTestSet = datapointsFilePath.replace(".json", " episuite results.json");
		
		ewss.runSmilesFilePublicApiUsingOriginalSmiles(propertyName,modelNameEpi, htDP_by_dsstox_qsar_smiles,filepathEpisuiteResultsTestSet);
		
		Hashtable<String,ModelPrediction>htMP_epi=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi, htDP_by_dsstox_qsar_smiles,htCS_by_DP_QsarSmiles, filepathEpisuiteResultsTestSet);		
		listHtMP.add(htMP_epi);	
		
		Hashtable<String,ModelPrediction>htMP_epi2=null;
		if(modelNameEpi2!=null) {
			htMP_epi2=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi2, htDP_by_dsstox_qsar_smiles,htCS_by_DP_QsarSmiles, filepathEpisuiteResultsTestSet);
			listHtMP.add(htMP_epi2);
		}
		
		boolean calculateAD=true;
		long modelId=getModelID(datasetName);
		Hashtable<String,ModelPrediction>htMP_test=cm.getResQsarModelPredictionsRevisedDsstoxSmiles(of, propertyName, modelId, calculateAD, htDP_by_dsstox_qsar_smiles);
		listHtMP.add(htMP_test);

		Hashtable<String,ModelPrediction>htMP_opera=null;
		if(includeOPERA && !propertyName.equals("96HR_Fish_LC50")) { 
			htMP_opera=opera.getOperaModelPredictionsFromOutputFile(of, propertyName,"test", htDP_by_dsstox_qsar_smiles);
			listHtMP.add(htMP_opera);
		}

		String smilesType="dsstox smiles";
		Hashtable<String,ModelPrediction>htMP_percepta=null;
		if(!propertyName.equals("96HR_Fish_LC50")) {
			htMP_percepta=percepta.getPerceptaModelPredictionsFromSDF_ByQsarSmiles(propertyName,"test", of,htDP_by_dsstox_qsar_smiles,smilesType);
			listHtMP.add(htMP_percepta);		
		}

		
		//***********************************************************************************
		HashSet<String> hsSmilesInCommon = getSmilesInCommon(listHtMP);
		
		AxesBounds axesBounds=getAxesBounds(listHtMP,hsSmilesInCommon);
		
		String title="Test set "+propertyName+" EPI Suite";
		if(modelNameEpi!=null) {
			title="Test set "+propertyName+" EPI Suite "+modelNameEpi;
		}
		if(propertyName.equals("96HR_Fish_LC50")) title="Test set Ecosar";
		
		
		Results resultsEpi = plot.plotPredictions(title, propertyName, units, hsSmilesInCommon,axesBounds,  htMP_epi,
				 createPlot,of,title+" common.png");
		
//		try {
//			FileWriter fw=new FileWriter (of+File.separator+"epi2.json");
//			fw.write(Utilities.gson.toJson(resultsEpi.modelPredictions));
//			fw.flush();
//			fw.close();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		
		//******************************************************************************************************
		//EPI Suite API2
		if(modelNameEpi2!=null) {
			title="Test set "+propertyName+" EPI Suite "+modelNameEpi2;
			Results resultsEpi2 = plot.plotPredictions(title, propertyName, units, hsSmilesInCommon, axesBounds, htMP_epi2,
					 createPlot,of,title+" common.png");
		}

		//******************************************************************************************************
		//WebTEST2.0
		title="Test set "+propertyName+" WebTest2.0";
		if(propertyName.equals("96HR_Fish_LC50")) title="Test set WebTest2.0";
		
		Results resultsTest=plot.plotPredictions(title, propertyName, units, hsSmilesInCommon, axesBounds, htMP_test,
				 createPlot,of,title+" common.png");
		
		//******************************************************************************************************
		
		
		// Percepta
		Results resultsPercepta=null;
		title="Test set "+propertyName+" Percepta";
		

		if(!propertyName.equals("96HR_Fish_LC50")) {
			resultsPercepta=plot.plotPredictions(title, propertyName, units, hsSmilesInCommon,  axesBounds,htMP_percepta,
					 createPlot,of,title+" common.png");
		}

//		System.out.println(Utilities.gson.toJson(htMP_percepta));
		
		//******************************************************************************************************
		// OPERA
		
		if(includeOPERA && !propertyName.equals("96HR_Fish_LC50")) {
			Results resultsOPERA=null;
			title="Test set "+propertyName+" OPERA2.8";
			resultsOPERA=plot.plotPredictions(title, propertyName, units, hsSmilesInCommon, axesBounds, htMP_opera,
				 createPlot,of,title+" common.png");
//		System.out.println(Utilities.gson.toJson(htMP_percepta));
		}
		//******************************************************************************************************
		
//		String title="Worst Epi Suite predictions for "+ propertyName+" using revised dsstox qsar ready smiles";
////		episuite.writeBadEpisuitePredictions(title, propertyName,datasetName, modelNameEpi, of, filepathEpisuiteResultsTestSet, resultsEpi, resultsTest);
//		
		this.cm.writeBadPredictions("test set", of, propertyName, "EpiSuite", "TEST", resultsEpi, resultsTest);
		this.cm.writeBadPredictions("test set", of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi);
//
//		if(resultsPercepta!=null)
//			this.cm.writeBadPredictions("test set", of, propertyName, "Percepta", "TEST", resultsTest, resultsPercepta);
//		
//		utils.writeToJson(resultsEpi.modelPredictions,of+"\\Episuite "+modelNameEpi+" external set modelPredictions.json" );
//		utils.writeToJson(resultsTest.modelPredictions,of+"\\Test external set modelPredictions.json" );
		
	}


	private void createDetailedComparisonTableExternal(String subfolder, String propertyName, String modelNameEpi,
			String modelNameEpi2, String workflow) {
		
		
		Map<String,Map<String, Double>>mapEpi=runPropertyDetailedExternal(propertyName,DevQsarConstants.sourceNameEPISuiteAPI,modelNameEpi,subfolder,workflow);
		Map<String,Map<String, Double>>mapEpi2=null;
		
		if(modelNameEpi2!=null)
			mapEpi2=runPropertyDetailedExternal(propertyName,DevQsarConstants.sourceNameEPISuiteAPI,modelNameEpi2,subfolder,workflow);
		
		Map<String,Map<String, Double>>mapTest=runPropertyDetailedExternal(propertyName,DevQsarConstants.sourceNameCheminformaticsModules,null,subfolder,workflow);

		System.out.println("\n\nModel\tRMSE all\tRMSE T\tRMSE P\t% in T");
		getSimpleStats("EpiSuite "+modelNameEpi, mapEpi);
		if(modelNameEpi2!=null) getSimpleStats("EpiSuite "+modelNameEpi2, mapEpi2);
		getSimpleStats("CM XGB", mapTest);
		
		
		if(!propertyName.equals(propertyName96HR_Fish_LC50)) {

			Map<String,Map<String, Double>>mapPercepta=runPropertyDetailedExternal(propertyName,DevQsarConstants.sourceNamePercepta2023,null,subfolder,workflow);
			getSimpleStats("Percepta", mapPercepta);
			
			Map<String,Map<String, Double>>mapOpera=runPropertyDetailedExternal(propertyName,DevQsarConstants.sourceNameOPERA28,null,subfolder,workflow);
			getSimpleStats("OPERA2.8", mapOpera);
		}
		
		
	}


	private void runPropertyByRevisedOriginalSmilesExternal(String propertyName, String modelNameEpi,String modelNameEpi2,
			boolean createPlot, boolean omitTrainingEpi, String workflow) {
		
		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run from revised dsstox original smiles";
		new File(of).mkdirs();
		String units = getUnits(propertyName);
		
		String propertyNameDataset = propertyName;
		String datasetNameExternal=null;
		
		if (propertyName.equals("LogKow")) {
			propertyNameDataset = "LogP";
			datasetNameExternal="exp_prop_LOG_KOW_external_validation";
		} else if (propertyName.equals("WS")) {
			datasetNameExternal="exp_prop_WATER_SOLUBILITY_external_validation";
		}

		String datasetName = propertyNameDataset + " v1 modeling";
		if(propertyName.equals("96HR_Fish_LC50")) {
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}
		long fk_dataset_id = du.getDatasetId(datasetName);


		boolean omitOnlyTrainingFromOurDataset=false;
		
		long fk_dataset_id_external = du.getDatasetId(datasetNameExternal);
		String datapointsExternalFilePath = of + File.separator + datasetName + " external set.json";
		File fileExternalDatapoints=new File(datapointsExternalFilePath);
		
		if(!fileExternalDatapoints.exists()) {
//			du.createTestSetExternalDatapointsFile(datapointsExternalFilePath,fk_dataset_id_external,fk_dataset_id ,omitOnlyTrainingFromOurDataset);		
			du.createTestSetExternalDatapointsFile2(datapointsExternalFilePath,fk_dataset_id_external,fk_dataset_id ,omitOnlyTrainingFromOurDataset);
		}
		
		Hashtable<String, DataPoint> htDP_external = du.getDatapoints(datapointsExternalFilePath);
		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles_ext=du.createHashtableCheckStructureByDPQsarSmilesExternal(fk_dataset_id_external);
		setCheckStructure(htDP_external, htCS_by_DP_QsarSmiles_ext);
		
		String filepathSDF=of+File.separator+propertyName+" external set dsstox smiles.sdf";//for running percepta
		du.createSDF_dsstox_smiles( filepathSDF, htDP_external);
		
		//******************************************************************************************************
		//EPI Suite API
		String filepathEpisuiteResultsTestSet = datapointsExternalFilePath.replace(".json", " episuite results.json");
		ewss.runSmilesFilePublicApiUsingOriginalSmiles(propertyName,modelNameEpi, htDP_external,filepathEpisuiteResultsTestSet);
		
		Hashtable<String,ModelPrediction>htMP_epi=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi, htDP_external,htCS_by_DP_QsarSmiles_ext, filepathEpisuiteResultsTestSet);

		String titleEpi="External set "+propertyName+" EPI Suite API";
		if(modelNameEpi!=null) {
			titleEpi="External set "+propertyName+" EPI Suite API "+modelNameEpi;
		}
		Results resultsEpi = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi, units, htMP_epi,
				 omitTrainingEpi,createPlot,of);

		
		//******************************************************************************************************
		//EPI Suite API2
		
		if(modelNameEpi2!=null) {
			Hashtable<String,ModelPrediction>htMP_epi2=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi2, htDP_external,htCS_by_DP_QsarSmiles_ext, filepathEpisuiteResultsTestSet);
			titleEpi="External set "+propertyName+" EPI Suite API "+modelNameEpi2;
			Results resultsEpi2 = plot.plotEpisuitePredictions(titleEpi, propertyName, modelNameEpi2, units, htMP_epi2,
					 omitTrainingEpi,createPlot,of);
		}

		//******************************************************************************************************
		//WebTEST2.0
		
		long modelId=getModelID(datasetName);

		if(debug) System.out.println("ResQsar modelID=" + modelId);

		boolean calculateAD=true;
		Hashtable<String,ModelPrediction>htMP_test=cm.getResQsarModelPredictionsRevisedDsstoxSmiles(of, propertyName, modelId, calculateAD, htDP_external);
		
//		System.out.println(Utilities.gson.toJson(htMP_test));
		
		//		System.out.println(Utilities.gson.toJson(htPredResQsar));
		Results resultsTest=plot.plotPredictions("External set "+propertyName+" WebTest2.0",propertyName, units, resultsEpi, 
				htMP_test,createPlot,omitTrainingEpi,of);

		//******************************************************************************************************
		// Percepta
		Results resultsPercepta=null;
		
		String smilesType="dsstox smiles";
		Hashtable<String,ModelPrediction>htMP_percepta=percepta.getPerceptaModelPredictionsFromSDF_ByQsarSmiles(propertyName,"external", of,htDP_external,smilesType);
		resultsPercepta=plot.plotPredictions("External set "+propertyName+" Percepta", propertyName, units, resultsEpi, htMP_percepta,createPlot,omitTrainingEpi, of);
//		System.out.println(Utilities.gson.toJson(htMP_percepta));
		
		//******************************************************************************************************
		// OPERA
		Results resultsOPERA=null;
		
		Hashtable<String,ModelPrediction>htMP_opera=opera.getOperaModelPredictionsFromOutputFile(of, propertyName,"external", htDP_external);
		resultsOPERA=plot.plotPredictions("External set "+propertyName+" OPERA2.8", propertyName, units, resultsEpi, htMP_opera,createPlot,omitTrainingEpi, of);
//		System.out.println(Utilities.gson.toJson(htMP_percepta));
		//******************************************************************************************************
		
		String title="Worst Epi Suite predictions for "+ propertyName+" using revised dsstox qsar ready smiles";
//		episuite.writeBadEpisuitePredictions(title, propertyName,datasetName, modelNameEpi, of, filepathEpisuiteResultsTestSet, resultsEpi, resultsTest);
		
		this.cm.writeBadPredictions("external set", of, propertyName, "EpiSuite", "TEST", resultsEpi, resultsTest);
		this.cm.writeBadPredictions("external set", of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi);

		if(resultsPercepta!=null)
			this.cm.writeBadPredictions("test set", of, propertyName, "Percepta", "TEST", resultsTest, resultsPercepta);
		
		utils.writeToJson(resultsEpi.modelPredictions,of+"\\Episuite "+modelNameEpi+" external set modelPredictions.json" );
		utils.writeToJson(resultsTest.modelPredictions,of+"\\Test external set modelPredictions.json" );
		
		
	}
	
	/**
	 * Only include the overlapping chemicals not in any training sets in plots
	 * 
	 * @param propertyName
	 * @param modelNameEpi
	 * @param modelNameEpi2
	 * @param createPlot
	 * @param omitTrainingEpi
	 * @param workflow
	 */
	private void runPropertyByRevisedOriginalSmilesExternal2(String propertyName, String modelNameEpi,String modelNameEpi2,
			boolean createPlot, boolean omitTrainingEpi, String workflow) {
		
		String of = "data\\episuite\\episuite validation\\" + propertyName+"\\run from revised dsstox original smiles";
		new File(of).mkdirs();
		String units = getUnits(propertyName);
		
		String propertyNameDataset = propertyName;
		String datasetName = propertyNameDataset + " v1 modeling";
		String datasetNameExternal=null;
		
		if (propertyName.equals("LogKow")) {
			propertyNameDataset = "LogP";
			datasetNameExternal="exp_prop_LOG_KOW_external_validation";
		} else if (propertyName.equals("WS")) {
			datasetNameExternal="exp_prop_WATER_SOLUBILITY_external_validation";
		} else if (propertyName.equals("96HR_Fish_LC50")) {
			datasetNameExternal="QSAR_Toolbox_96HR_Fish_LC50_v3 modeling";
			datasetName = "ECOTOX_2024_12_12_96HR_Fish_LC50_v3 modeling";
		}
		
		long fk_dataset_id = du.getDatasetId(datasetName);


		boolean omitOnlyTrainingFromOurDataset=false;
		
		long fk_dataset_id_external = du.getDatasetId(datasetNameExternal);
		String datapointsExternalFilePath = of + File.separator + datasetName + " external set.json";
		File fileExternalDatapoints=new File(datapointsExternalFilePath);
		
		if(!fileExternalDatapoints.exists()) {
//			du.createTestSetExternalDatapointsFile(datapointsExternalFilePath,fk_dataset_id_external,fk_dataset_id ,omitOnlyTrainingFromOurDataset);		
			du.createTestSetExternalDatapointsFile2(datapointsExternalFilePath,fk_dataset_id_external,fk_dataset_id ,omitOnlyTrainingFromOurDataset);
		}
		
		Hashtable<String, DataPoint> htDP_external = du.getDatapoints(datapointsExternalFilePath);
		Hashtable<String,CheckStructure>htCS_by_DP_QsarSmiles_ext=du.createHashtableCheckStructureByDPQsarSmilesExternal(fk_dataset_id_external);
		setCheckStructure(htDP_external, htCS_by_DP_QsarSmiles_ext);
		
		String filepathSDF=of+File.separator+propertyName+" external set dsstox smiles.sdf";//for running percepta
		du.createSDF_dsstox_smiles( filepathSDF, htDP_external);
		
		//******************************************************************************************************
		//EPI Suite API
		String filepathEpisuiteResultsTestSet = datapointsExternalFilePath.replace(".json", " episuite results.json");
		ewss.runSmilesFilePublicApiUsingOriginalSmiles(propertyName,modelNameEpi, htDP_external,filepathEpisuiteResultsTestSet);

		List<Hashtable<String,ModelPrediction>>listHtMP=new ArrayList<>();
		
		Hashtable<String,ModelPrediction>htMP_epi=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi, htDP_external,htCS_by_DP_QsarSmiles_ext, filepathEpisuiteResultsTestSet);		
		listHtMP.add(htMP_epi);
		
		Hashtable<String,ModelPrediction>htMP_epi2=null;
		if(modelNameEpi2!=null) {
			htMP_epi2=episuite.getEpisuiteModelPredictions(propertyName, modelNameEpi2, htDP_external,htCS_by_DP_QsarSmiles_ext, filepathEpisuiteResultsTestSet);
			listHtMP.add(htMP_epi2);
		}
		
		boolean calculateAD=true;
		long modelId=getModelID(datasetName);
		Hashtable<String,ModelPrediction>htMP_test=cm.getResQsarModelPredictionsRevisedDsstoxSmiles(of, propertyName, modelId, calculateAD, htDP_external);
		listHtMP.add(htMP_test);

		Hashtable<String,ModelPrediction>htMP_opera=null;
		if (!propertyName.equals("96HR_Fish_LC50")) {
			htMP_opera=opera.getOperaModelPredictionsFromOutputFile(of, propertyName,"external", htDP_external);
			listHtMP.add(htMP_opera);
		}

		Hashtable<String,ModelPrediction>htMP_percepta=null;
		if (!propertyName.equals("96HR_Fish_LC50")) {
			String smilesType="dsstox smiles";
			htMP_percepta=percepta.getPerceptaModelPredictionsFromSDF_ByQsarSmiles(propertyName,"external", of,htDP_external,smilesType);
			listHtMP.add(htMP_percepta);
		}

		
		//***********************************************************************************
		HashSet<String> hsSmilesInCommon = getSmilesInCommon(listHtMP);
		AxesBounds axesBounds=getAxesBounds(listHtMP, hsSmilesInCommon);
		
//		if(true)return;
		
		//***********************************************************************************
		String title="External set "+propertyName+" EPI Suite API";
		if(modelNameEpi!=null) {
			title="External set "+propertyName+" EPI Suite "+modelNameEpi;
		}
		
		if(propertyName.equals("96HR_Fish_LC50")) title="External set Ecosar";
		
		
		Results resultsEpi = plot.plotPredictions(title, propertyName, units, hsSmilesInCommon,axesBounds,  htMP_epi,
				 createPlot,of,"External "+modelNameEpi+".png");
		
		//******************************************************************************************************
		//EPI Suite API2
		if(modelNameEpi2!=null) {
			title="External set "+propertyName+" EPI Suite "+modelNameEpi2;
			Results resultsEpi2 = plot.plotPredictions(title, propertyName, units, hsSmilesInCommon, axesBounds, htMP_epi2,
					 createPlot,of,"External "+modelNameEpi2+".png");
		}

		//******************************************************************************************************
		//WebTEST2.0
//		System.out.println(Utilities.gson.toJson(htMP_test));
		//		System.out.println(Utilities.gson.toJson(htPredResQsar));
		title="External set "+propertyName+" WebTest2.0";
		if(propertyName.equals("96HR_Fish_LC50")) title="External set WebTEST2.0";
		
		Results resultsTest=plot.plotPredictions(title, propertyName, units, hsSmilesInCommon, axesBounds, htMP_test,
				 createPlot,of,"External WebTEST2.0.png");
		
		
		//******************************************************************************************************
		// Percepta
		
		if (!propertyName.equals("96HR_Fish_LC50")) {
			Results resultsPercepta=null;
			title="External set "+propertyName+" Percepta";
			resultsPercepta=plot.plotPredictions(title, propertyName, units, hsSmilesInCommon,axesBounds, htMP_percepta,
					 createPlot,of,"External Percepta.png");
		}
		
//		System.out.println(Utilities.gson.toJson(htMP_percepta));
		
		//******************************************************************************************************
		// OPERA
		
		if (!propertyName.equals("96HR_Fish_LC50")) {
			Results resultsOPERA=null;
			title="External set "+propertyName+" OPERA2.8";		
			resultsOPERA=plot.plotPredictions(title, propertyName, units, hsSmilesInCommon,  axesBounds, htMP_opera,
					 createPlot,of,"External OPERA2.8.png");
		}
//		System.out.println(Utilities.gson.toJson(htMP_percepta));
		//******************************************************************************************************
		
//		String title="Worst Epi Suite predictions for "+ propertyName+" using revised dsstox qsar ready smiles";
////		episuite.writeBadEpisuitePredictions(title, propertyName,datasetName, modelNameEpi, of, filepathEpisuiteResultsTestSet, resultsEpi, resultsTest);
//		
		this.cm.writeBadPredictions("external set", of, propertyName, "EpiSuite", "TEST", resultsEpi, resultsTest);
		this.cm.writeBadPredictions("external set", of, propertyName, "TEST","EpiSuite", resultsTest, resultsEpi);
//
//		if(resultsPercepta!=null)
//			this.cm.writeBadPredictions("test set", of, propertyName, "Percepta", "TEST", resultsTest, resultsPercepta);
//		
//		utils.writeToJson(resultsEpi.modelPredictions,of+"\\Episuite "+modelNameEpi+" external set modelPredictions.json" );
//		utils.writeToJson(resultsTest.modelPredictions,of+"\\Test external set modelPredictions.json" );
		
		
	}


	private HashSet<String> getSmilesInCommon(List<Hashtable<String, ModelPrediction>> listHtMP) {
		HashSet<String>hsSmilesInCommon=new HashSet<>();
		
		Hashtable<String, ModelPrediction>htMP0=listHtMP.get(0);
		
		int countEpisuite=0;
				
		for(String qsarSmiles:htMP0.keySet()) {
			ModelPrediction mp=htMP0.get(qsarSmiles);
			if(mp.split==0) continue;
			if(mp.pred==null || mp.pred.equals(Double.NaN))continue;
			if(mp.exp==null)continue;
			
			countEpisuite++;
			
			boolean keep=true;
			
			int counter=0;
			
			for(int i=1;i<listHtMP.size();i++) { 
			
				Hashtable<String, ModelPrediction>htMPi=listHtMP.get(i);
				
				counter++;
				
				if(!htMPi.containsKey(qsarSmiles)) {
//					System.out.println(qsarSmiles+"\tNot in "+counter);
					keep=false;
					break;
				}
				ModelPrediction mpi=htMPi.get(qsarSmiles);
				if(mpi.split!=null && mpi.split==0) keep=false;
				if(mpi.pred==null || mpi.pred.equals(Double.NaN)) keep=false;
				if(!keep)break;
			}
			
			
			if(keep) {
				hsSmilesInCommon.add(qsarSmiles);
			}
			
//			System.out.println(qsarSmiles+"\t"+keep+"\t"+hsSmilesInCommon.size());

		}
		
		System.out.println("countEpisuite="+countEpisuite);
		return hsSmilesInCommon;
	}
	
	
	class AxesBounds {
		double minVal;
		double maxVal;
	}
	
	private AxesBounds getAxesBounds(List<Hashtable<String, ModelPrediction>> listHtMP,HashSet<String>hsSmilesInCommon) {
		Hashtable<String, ModelPrediction>htMP0=listHtMP.get(0);
		
		AxesBounds axesBounds=new AxesBounds();
		axesBounds.minVal=9999;
		axesBounds.maxVal=-9999;
		
		for(String qsarSmiles:htMP0.keySet()) {
			ModelPrediction mp=htMP0.get(qsarSmiles);
			if(mp.split==0) continue;
			if(mp.pred==null)continue;
			if(mp.exp==null)continue;
			
			boolean keep=true;
			
			int counter=0;
			
			if(!hsSmilesInCommon.contains(qsarSmiles)) continue;
			
			
			for(int i=1;i<listHtMP.size();i++) { 
				Hashtable<String, ModelPrediction>htMPi=listHtMP.get(i);
				ModelPrediction mpi=htMPi.get(qsarSmiles);
				if(mpi.pred>axesBounds.maxVal) axesBounds.maxVal=mpi.pred;
				if(mpi.exp>axesBounds.maxVal) axesBounds.maxVal=mpi.exp;
				if(mpi.pred<axesBounds.minVal) axesBounds.minVal=mpi.pred;
				if(mpi.exp<axesBounds.minVal) axesBounds.minVal=mpi.exp;
			}
			
			
			
//			System.out.println(qsarSmiles+"\t"+keep+"\t"+hsSmilesInCommon.size());

		}
		
		axesBounds.minVal-=1;
		axesBounds.maxVal+=1;
		
		return axesBounds;
	}


}


