package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
//import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;

import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;

import gov.epa.run_from_java.scripts.QsarModelsScript;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.ADEstimate;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.PredictionIndividualMethod;



/**
 * TODO make html using Jsoup from the very beginning- rather than converting to pretty json at end
 * https://stackoverflow.com/questions/29196699/pretty-html-snippet-output
 * 
 * https://www.quora.com/What-is-the-best-way-to-tell-if-there-is-a-missing-closing-tag-in-HTML-CSS-or-JavaScript-code
 * https://validator.w3.org/#validate_by_upload
 * 
 * 
* @author TMARTI02
*/
public class HTMLReportCreator {

	
	protected String imgURLCid="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/";
	protected String imgURLSid="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxsid/";
	protected String detailsURL="https://comptox.epa.gov/dashboard/chemical/details/";

	protected boolean debug=false;

	QsarModelsScript qms=new QsarModelsScript("tmarti02");

	public static int tabindex=0;
	/**
	 * Checks to see if have valid cas number with check sum method
	 * 
	 * @param CAS
	 * @return
	 */
	public static boolean isCAS_OK(String CAS) {
		
		if (CAS.indexOf(" ")>-1) {
//			System.out.println("Space!");
			return false;
		}
		
		String [] part=CAS.split("-");
		
		if (part.length!=3) return false;
		
		String part1=part[0];
		String part2=part[1];
		String part3=part[2];
		
		int sum=0;
		
		for (int i=0;i<part1.length();i++) {
			String s=part1.substring(i, i+1);
			if (!Character.isDigit(s.charAt(0))) return false;
			sum+=(part1.length()+2-i)*Integer.parseInt(s);
		}
		
		String s1=part2.substring(0, 1);
		String s2=part2.substring(1, 2);
		
		if (!Character.isDigit(s1.charAt(0)) || !Character.isDigit(s2.charAt(0))) {
			return false;
		}
		
		int N2=Integer.parseInt(s1);
		int N1=Integer.parseInt(s2);
		int R=Integer.parseInt(part3);
		
		sum+=2*N2+N1;
		
		double bob=((double)sum)/10.0;
		double bob2=Math.floor(bob);
		double bob3=(bob-bob2)*10.0;
		
		int R2=(int)Math.round(bob3);
		
//		System.out.println(bob3);
//		System.out.println(R+"\t"+R2);
		
		return R2==R;
		
		
	}
	
	public static String getFormattedValue(boolean isPropertyBinary,Double dvalue,String propertyName) {
		return getFormattedValue( isPropertyBinary, dvalue, propertyName, 3);
	}
	
	
	
	public static String getFormattedValue(boolean formatAsInteger, Double dvalue,String propertyName,int nsig) {

		if(dvalue==null || dvalue.isNaN()) {
			return "N/A";
		}
		
		DecimalFormat dfSci=new DecimalFormat("0.00E00");
		DecimalFormat dfInt=new DecimalFormat("0");

//		DecimalFormat df1=new DecimalFormat("0.00");
//		DecimalFormat df4=new DecimalFormat("0.0");
		
		try {
			
			if(formatAsInteger)
				return dfInt.format(dvalue);
			
//			if(propertyName.equals(DevQsarConstants.BOILING_POINT) || propertyName.equals(DevQsarConstants.MELTING_POINT)) 
//				return df4.format(dvalue);
			
			if(dvalue!=0 && (Math.abs(dvalue)<0.01 || Math.abs(dvalue)>1e3)) {
				return dfSci.format(dvalue);
			}
//			System.out.println(dvalue+"\t"+setSignificantDigits(dvalue, nsig));
			return setSignificantDigits(dvalue, nsig);

		} catch (Exception ex) {
//			System.out.println("exception");
			return null;
		}

		
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
	
	
	public static String getFormattedValue(boolean isBinary, Double dvalue,int nsig) {

		if(dvalue==null || dvalue.isNaN()) {
			return "N/A";
		}
		
		DecimalFormat dfSci=new DecimalFormat("0.00E00");
		DecimalFormat df2=new DecimalFormat("0.00");
//		DecimalFormat dfInt=new DecimalFormat("0");

		try {
			
			if(isBinary)
				return df2.format(dvalue);
			
//			if(propertyName.equals(DevQsarConstants.BOILING_POINT) || propertyName.equals(DevQsarConstants.MELTING_POINT)) 
//				return df4.format(dvalue);
			
			if(dvalue!=0 && (Math.abs(dvalue)<0.01 || Math.abs(dvalue)>1e3)) {
				return dfSci.format(dvalue);
			}
//			System.out.println(dvalue+"\t"+setSignificantDigits(dvalue, nsig));
			return HTMLReportCreator.setSignificantDigits(dvalue, nsig);

		} catch (Exception ex) {
//			System.out.println("exception");
			return null;
		}

		
	}
	
	
	protected void writeExperimental(gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport or, Writer fw) throws IOException {
		
//		String strExp="<b>Experimental value: </b>";

		if (or.modelResults.experimentalValue!=null || or.modelResults.experimentalString!=null) {
			if (or.modelDetails.propertyName.equals("Ready Binary Biodegradability")) {
				System.out.println(or.chemicalIdentifiers.dtxsid+"\t"+or.modelDetails.propertyName+"\t"+or.modelResults.experimentalValue+"\t"+or.modelResults.experimentalString);
			}
		}
		String strExp=("<div class=\"tooltip\"><b>Experimental value: </b> "+
				  "<span class=\"tooltiptext\">Experimental value from "+or.modelDetails.modelSourceName+"</span></div>");

		if (or.modelResults.experimentalValue!=null && !or.modelResults.experimentalValue.isNaN()) {
			
			String formattedValue=getFormattedValue(or.modelDetails.propertyIsBinary, or.modelResults.experimentalValue, or.modelDetails.propertyName);
			strExp+="&nbsp;"+formattedValue+"&nbsp;"+or.modelResults.standardUnit;//add units
			
			if(or.modelResults.experimentalString!=null) {
				strExp+=("&nbsp;("+or.modelResults.experimentalString+")");
			}
		} else if(or.modelResults.experimentalString!=null) {
			 try {
				 double dexp=Double.parseDouble(or.modelResults.experimentalString);				 
				 String formattedValue=getFormattedValue(or.modelDetails.propertyIsBinary,dexp,or.modelDetails.propertyName);
				 strExp+="&nbsp;"+formattedValue;//add units
			 } catch (Exception ex) {
				strExp+=("&nbsp;"+or.modelResults.experimentalString+"");				 
			 }
			
			if(!or.modelDetails.propertyIsBinary) {
				strExp+="&nbsp;"+or.modelResults.standardUnit;
			}
			
		} else {
			strExp+="&nbsp;N/A";
		}

//		fw.write(strExp+"<br>");
//		if (or.modelResults.experimentalSource!=null) {
//			fw.write("<b>Experimental source: </b>"+or.modelResults.experimentalSource+"<br>\n");
//		}
//		if (or.modelResults.experimentalSet!=null) {
//			fw.write("<b>Experimental set: </b>"+or.modelResults.experimentalSet+"<br>\n");
//		}
		
		fw.write(strExp);
		
		if (or.modelResults.experimentalSource!=null) {
			fw.write(" ("+or.modelResults.experimentalSource+", set="+or.modelResults.experimentalSet+", CASRN="+or.modelResults.experimentalCASRN+")");
		}

		fw.write("<br>");

	}

	
	protected void writeExpNeighbor(PredictionReport or, Writer fw, Double experimentalValue,String experimentalString)
			throws IOException {

		fw.write("<b>Measured:</b> ");
		
		String strExp="";
		
		if(experimentalValue!=null) {
			String formattedValue=getFormattedValue(or.modelDetails.propertyIsBinary, experimentalValue,or.modelDetails.propertyName);
//			strExp+="&nbsp;"+formattedValue+"&nbsp;"+or.modelResults.standardUnit;
			strExp+="&nbsp;"+formattedValue;
			
			if(experimentalString!=null) {
				strExp+="&nbsp;("+experimentalString+")";
			}

		} else {
			if(experimentalString!=null) {
				strExp+="&nbsp;"+experimentalString;
			}
		}
		
		if(strExp.isBlank()) strExp="N/A*";
		
		fw.write(strExp+"<br>");
		
		
	}
	

	
	protected void writePredNeighbor(gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport or, Writer fw, Double predictedValue,String predictedString,String predictionToolTip) throws IOException {

		fw.write("<div class=\"tooltip\"><b>Predicted:</b>"+
				  "<span class=\"tooltiptext\">"+predictionToolTip+"</span></div>");
		
		String strPred="";
		
		if(predictedValue!=null) {
			String formattedValue=getFormattedValue(or.modelDetails.propertyIsBinary, predictedValue,or.modelDetails.propertyName);
//			strPred+="&nbsp;"+formattedValue+"&nbsp;&nbsp;"+or.modelResults.standardUnit;
			strPred+="&nbsp;"+formattedValue+"&nbsp;&nbsp;";
//			System.out.println("pred formattedValue="+formattedValue);
			
			if(predictedString!=null) {
				strPred+=("&nbsp;("+predictedString+")");
			}

		} else {
			if(predictedString!=null) {
				strPred+=("&nbsp;"+predictedString);
			}
			
		}
		
		if(strPred.isBlank()) strPred="&nbsp;N/A";
		
		fw.write(strPred+"<br><br>");
	}

	

	protected void writeStyles(Writer fw) throws IOException {
		
		int width=400;
		
		String style="<style>\r\n" + 
					 
					 "	.tooltip {\r\n" +
				     "	  position: relative;\r\n" + 
					 "	  display: inline-block;\r\n" +
					 "	  border-bottom: 1px dotted black;\r\n" + 
					 "	}\r\n" + "\r\n" + 
					 
					 "	.tooltip .tooltiptext {\r\n"+
					 "	  visibility: hidden;\r\n" + 
					 "	  width: "+width+"px;\r\n" + 
					 "	  background-color: #555;\r\n"+
					 "	  color: #fff;\r\n" + 
					 "	  text-align: center;\r\n" + 
					 "	  border-radius: 6px;\r\n"+
					 "	  padding: 5px 0;\r\n" + 
					 "	  position: absolute;\r\n" +
					 "	  z-index: 1;\r\n"+
					 "	  bottom: 125%;\r\n" + 
					 "	  left: 50%;\r\n" + 
					 "	  margin-left: -60px;\r\n"+
					 "	  opacity: 0;\r\n" + 
					 "	  transition: opacity 0.3s;\r\n" + 
					 "	}\r\n" + "\r\n"+
					 
					 "	.tooltip .tooltiptext::after {\r\n" +
					 "	  content: \"\";\r\n" + "	  position: absolute;\r\n"+
					 "	  top: 100%;\r\n" + 
					 "	  left: 50%;\r\n" + 
					 "	  margin-left: -5px;\r\n"+
					 "	  border-width: 5px;\r\n" + 
					 "	  border-style: solid;\r\n"+
					 "	  border-color: #555 transparent transparent transparent;\r\n" + 
					 "	}\r\n" + "\r\n"+
					 
					 "	.tooltip:hover .tooltiptext {\r\n" + 
					 "	  visibility: visible;\r\n" + 
					 "	  opacity: 1;\r\n"+
					 "	}\r\n" + "</style>\n";
		
				
		fw.write(style);

	}
	
	protected void writePrediction(gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport or, Writer fw) throws IOException {
		fw.write("<div class=\"tooltip\"><b>Predicted value: </b> "+
				  "<span class=\"tooltiptext\">Predicted value from the model</span></div>");

		String strPred="&nbsp;N/A";
		
		if(or.modelResults.predictedValue!=null && !or.modelResults.predictedValue.isNaN()) {
			String formattedValue=getFormattedValue(or.modelDetails.propertyIsBinary, or.modelResults.predictedValue,or.modelDetails.propertyName);
			strPred=" "+formattedValue+"&nbsp;"+or.modelResults.standardUnit;
//			System.out.println("pred formattedValue="+formattedValue);
			
//			System.out.println(or.modelDetails.propertyName+"\t"+strPred);
		}
		
		if(or.modelResults.predictedString!=null) {
			strPred+=("&nbsp;("+or.modelResults.predictedString+")");
		}
		
		if(or.modelResults.pressure!=null) {
			strPred+=" @ "+or.modelResults.pressure+" "+or.modelResults.pressureUnits;
		}
		
		if(or.modelResults.temperature!=null) {
			strPred+=" @ "+or.modelResults.temperature+" "+or.modelResults.temperatureUnits.replace("Celsius","°C");
		}

		if(or.modelResults.pH!=null) {
			strPred+=" @ pH="+or.modelResults.pH;
		}

		
		fw.write(strPred+"<br>");

	}
	
	protected void writePredictionError(PredictionReport or, Writer fw) throws IOException {
		
		if(or.modelResults.predictedError==null) return;
		
		fw.write("<div class=\"tooltip\"><b>Predicted error: </b> "+
				  "<span class=\"tooltiptext\">Explanation for lack of predicted value</span></div>");

		fw.write(or.modelResults.predictedError+"<br>");

	}


	
	

	
	protected void writeCenteredTD(Writer fw, String text) throws IOException {
		fw.write("<td align=center>"+text+"</td>\n");
	}
	
	
//	public String writeHtmlHead(PredictionReport or) throws IOException {
//		
//		StringWriter fw=new StringWriter();
//		
//		fw.write("<!DOCTYPE html><html>\n");
//		fw.write("<head>\n");
////		fw.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\n");
//		this.writeStyles(fw);
//		fw.write("<title>"+or.modelDetails.modelSourceName+" results for " + or.chemicalIdentifiers.dtxcid + " for " + or.modelDetails.modelName+" model");
//		fw.write("</title>\n");
//		fw.write("</head>\n");
//		return fw.toString();
//		
//	}
	
	
	protected void writeHtmlHead(PredictionReport or,StringWriter fw) throws IOException {
		
		fw.write("<!DOCTYPE html><html>\n");
		fw.write("<head>\n");
		fw.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\n");
		this.writeStyles(fw);
		fw.write("<title>"+or.modelDetails.modelSourceName+" results for " + or.chemicalIdentifiers.dtxcid + " for " + or.modelDetails.modelName+" model");
		fw.write("</title>\n");
		fw.write("</head>\n");
	}

	
	protected void writeCenteredTD(Writer fw, Double value) throws IOException {
		
		if(value==null) {
			fw.write("<td align=center>N/A</td>\n");	
		} else {
			fw.write("<td align=center>"+value+"</td>\n");
		}
	}

	protected void writeModelResults(PredictionReport or, Writer fw) throws IOException {
		
		DecimalFormat df=new DecimalFormat("0.00");
		
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Model Results</color></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		fw.write("<td>");

		fw.write("<b>Model name:</b> "+or.modelDetails.modelName+"<br>");
		
		fw.write("<b>Model source:</b> <a href=\""+or.modelDetails.modelSourceURL+"\">"+or.modelDetails.modelSourceName+"</a><br>\n");

		
//		fw.write("<b>Model source description:</b> "+or.modelDetails.sourceDescription+"<br>\n");
		
//		fw.write("<b>Model source:</b>");
//		fw.write("<div class=\"tooltip\">"+or.modelDetails.modelSource+
//		  "<span class=\"tooltiptext\">"+or.modelDetails.sourceDescription+"</span></div><br>");

		fw.write("<b>Property name:</b> "+or.modelDetails.propertyName+"<br>\n");
		fw.write("<b>Property description:</b> "+or.modelDetails.propertyDescription+"<br>\n");

		writeExperimental(or, fw);
		writePrediction(or, fw);
		writePredictionError(or, fw);
		writeAD(or,fw);
		
		
//		this.writeGlobalAD(or, fw);
//		this.writeLocalAD(or, fw, df);
//		this.writeConfidenceIndex(or, fw, df);

//		fw.write("<b>OPERA version:</b> "+or.modelDetails.source+"<br>");

		fw.write("</td>\n");
		fw.write("\t</tr>\n");
		fw.write("</table>\n");
	}

	
	protected void addPlotFromResQsarDB(Integer pixels, Long modelId, Long fileType, Writer fw, String altText)
			throws IOException {
		byte[]fileBytes=qms.downloadModelFile(modelId, fileType);
		String base64 = Base64.getEncoder().encodeToString(fileBytes);
		String imgSrc="data:image/png;base64,"+base64;
		
		
		if(pixels==null) {
			fw.write("<td width=30%><img src=\"" + imgSrc
					+ "\" alt=\""+altText+"\"></td>");
		} else {
//			fw.write("<td width=30%><img src=\"" + imgSrc
//					+ "\" alt=\""+altText+"\" height="+pixels+" width="+pixels+"></td>");
			
			fw.write("<td width=30%><img src=\"" + imgSrc
					+ "\" alt=\""+altText+"\" height="+pixels+"></td>");

			
		}
	}

	protected void addPlotsTable(PredictionReport or, Writer fw) throws IOException {

//		fw.write("<table border=0 width=100%>\n");
		fw.write("<table border=0>\n");
		
//		fw.write("<colgroup>\r\n"
//				+ "<col span=\"1\" style=\"width: 50%;\">\r\n"
//				+ "<col span=\"1\" style=\"width: 50%;\">\r\n"
//				+ "</colgroup>\r\n");
		
		fw.write("\t<tr>\n");
		
		String altTextScatterPlot=or.modelDetails.propertyName+" Model Results";
		String altTextHistogram="Histogram of "+or.modelDetails.propertyName+" Data";

		if(or.modelDetails.hasScatterPlot) {

//			if(or.modelDetails.loadPlotsFromDB) {
//				addPlotFromResQsarDB(500, or.modelDetails.modelId, 3L, fw, altTextScatterPlot);
//			} else if (or.modelDetails.useModelIdLegacy) {
//				fw.write("<td width=30%><img src=\"" + or.modelDetails.urlScatterPlotAPI_Legacy
//						+ or.modelDetails.modelIdLegacy + "\"" + " alt=\"" + altTextScatterPlot + "\"></td>");
//			} else {
//				fw.write("<td width=30%><img src=\""+or.modelDetails.urlScatterPlotAPI + 
//						or.modelDetails.modelId+"\" alt=\""+altTextScatterPlot+"\"></td>");
//			}

			
			fw.write("<td align=center><img src=\""+or.modelDetails.urlModelAPI+"?modelId="+
					or.modelDetails.modelId+"&typeId=3\" alt=\""+altTextScatterPlot+"\" height=400></td>");

		}
		
		if(or.modelDetails.hasHistogram) {
//			if(or.modelDetails.loadPlotsFromDB) {
//				addPlotFromResQsarDB(500,or.modelDetails.modelId, 4L, fw, altTextHistogram);
//			} else if (or.modelDetails.useModelIdLegacy) {
//				fw.write("<td width=30%><img src=\""+or.modelDetails.urlHistogramAPI_Legacy +
//						or.modelDetails.modelIdLegacy+"\" alt=\""+altTextHistogram+"\"></td>");
//
//			} else {
//				fw.write("<td width=30%><img src=\""+or.modelDetails.urlHistogramAPI +
//						or.modelDetails.modelId+"\" alt=\""+altTextHistogram+"\"></td>");
//			}
			
			fw.write("<td align=center><img src=\""+or.modelDetails.urlModelAPI+"?modelId="+
					or.modelDetails.modelId+"&typeId=4\" alt=\""+altTextHistogram+"\" height=400></td>");

		}
		
		
		fw.write("</tr></table>\n");
	}
	
	
	protected void writeQmrfLink(PredictionReport or, Writer fw) throws IOException {
		
//		if (or.modelDetails.hasQmrfPdf) {
//			
////			if(or.modelDetails.useModelIdLegacy) {
////				fw.write("<span class=\"border\"><a href=\""+or.modelDetails.urlQMRF_API+or.modelDetails.modelIdLegacy+"\" target=\"_blank\"> "
////						+ "Model summary in QSAR Model Reporting Format (QMRF)"
////						+ "</a></span>"+
////				"<style>.border {border: 2px solid darkblue; padding: 2px 4px 2px;}</style><br><br>");
////				
////			} else {
////				fw.write("<span class=\"border\"><a href=\""+or.modelDetails.urlQMRF_API+or.modelDetails.modelId+"\" target=\"_blank\"> "
////						+ "Model summary in QSAR Model Reporting Format (QMRF)"
////						+ "</a></span>"+
////				"<style>.border {border: 2px solid darkblue; padding: 2px 4px 2px;}</style><br><br>");
////				
////			}
//
//			
//		}
		
		fw.write("<span class=\"border\"><a href=\"" + or.modelDetails.urlModelAPI + "?modelId="
				+ or.modelDetails.modelId + "&typeId=1\" target=\"_blank\"> "
				+ "Model summary in QSAR Model Reporting Format (QMRF)</a></span>"
				+ "<style>.border {border: 2px solid darkblue; padding: 2px 4px 2px;}</style><br><br>");

	}

	protected String toPrettyHtml(Writer w) throws IOException {
//		System.out.println(doc);
		
//		String head=writeHtmlHead(or);
//		String html=head+w.toString();
		
//		Document doc = Jsoup.parseBodyFragment(w.toString());
//		doc.outputSettings().indentAmount(2);
//		String body=doc.body().html().toString();
//		return head+"\r\n<body>\n"+body+"\n</body>\n</html>";
		
		Document doc = Jsoup.parse(w.toString());
		doc.outputSettings().indentAmount(2);
		return doc.toString();

	}

	

	public static void viewHTMLReportsFromMaterializedView(String id) {
		
		String idCol="dtxcid";
		if (id.contains("SID")) idCol="dtxsid";

		String sql="Select model_name, report_html from mv_predicted_reports where "+idCol+"='"+id+"'";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		HTMLReportCreator h=new HTMLReportCreator();
		
		String folder="data\\mv_predicted_reports\\";
		String destFolder=folder+File.separator+id;
		
		File DF=new File(destFolder);
		if(!DF.exists())DF.mkdirs();

		try {
			while (rs.next()) {
				
				String model_name=rs.getString(1);
				String file_html=new String(rs.getBytes(2));
				
				String filename=id+"_"+model_name+".html";
				h.writeStringToFile(file_html, destFolder, filename);
				
				System.out.println(filename);
				
				PredictionReport.viewInWebBrowser(destFolder+File.separator+filename);
				
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	protected void writeFirstRow(PredictionReport or, Writer fw) throws Exception {
		
		boolean haveIndividualModelSummary=or.modelResults.consensusPredictions!=null && or.modelResults.consensusPredictions.predictionsIndividualMethod.size()>1;
		
		fw.write("<table border=0 width=100%>\n");
		
//		fw.write("<table style=\"width: 100%; table-layout: fixed; border: 0\">\n");
		
		if(haveIndividualModelSummary) {
			fw.write("<colgroup><col style=\"width: 150px;\"><col style=\"width: 20%;\"><col style=\"width: 50%;\"><col style=\"width: 30%;\"></colgroup>");
		} else {
			fw.write("<colgroup><col style=\"width: 150px;\"><col style=\"width: 30%;\"><col style=\"width: 70%;\"></colgroup>");
		}
		
		
		fw.write("\t<tr>\n");

		//Structure Image for test chemical:
		String imgURL=this.imgURLCid+or.chemicalIdentifiers.dtxcid;
		
		String chemicalName=or.chemicalIdentifiers.preferredName;
		
		fw.write("\t\t<td valign=\"top\"><img src=\""+imgURL+"\" hescript_dir = os.path.dirname(os.path.abspath(__file__))ight=150 width=150 border=2 "
				+ "alt=\"Structural image of "+chemicalName+"\"></td>\n");
		
		fw.write("<td valign=\"top\">");
		writeChemicalInfo(or, fw);
		fw.write("</td>\r\n");
		
		fw.write("<td valign=\"top\">");
		writeModelResults(or, fw);
		fw.write("</td>\r\n");
		
		
		if(haveIndividualModelSummary) {
			fw.write("<td valign=\"top\">");
			writeIndividualModelsSummary(or, fw);
			fw.write("</td>\r\n");
		}
		
		
		fw.write("\t</tr>\n");
		
		fw.write("</table>\n");
	}
	
	protected void writeAD(PredictionReport or, Writer fw) throws IOException {

		if (or.modelResults.adEstimates==null) return;

		//		fw.write("<br>\n");

		for (ADEstimate adEstimate:or.modelResults.adEstimates) {

			String name=adEstimate.adMethod.name;
			String description=adEstimate.adMethod.description;
			
			//Dont need extra rows since have combined AD:
			
			if(or.modelResults.useCombinedApplicabilityDomain) {
				if(name.toLowerCase().contains("local") || name.toLowerCase().contains("global")) continue;
			} else {
				if (name.equals("Combined applicability domain")) continue;
			}
			
			fw.write("<div class=\"tooltip\"><b>"+name+":</b>&nbsp;"+
					"<span class=\"tooltiptext\">"+description+"</span></div>");

			if (name.equals("Combined applicability domain")) {
				fw.write(adEstimate.reasoningHtml+"<br>");
			} else {
				if (adEstimate.conclusion==null) {
					fw.write(" "+adEstimate.value+"<br>");
				} else {
					
					if (adEstimate.conclusion.contentEquals("Inside")) {
						fw.write("<span class=\"borderAD_Green\">Inside</span>"+
								"<style>.borderAD_Green {border: 2px solid green; padding: 0px 4px 0px}</style>");
						
						if(debug) System.out.println(adEstimate.adMethod.name+"\t"+adEstimate.conclusion+"\t"+
								adEstimate.conclusion.contentEquals("Inside"));
						

					} else if (adEstimate.conclusion.contentEquals("Outside")) {
						fw.write("<span class=\"borderAD_Red\">Outside</span>"+
								"<style>.borderAD_Red {border: 2px solid red; padding: 0px 4px 0px}</style>");
					}
					
//					if (name.equals("Combined applicability domain")) {
//						fw.write("&nbsp;("+adEstimate.reasoning+")");
//					} 
					
					fw.write("<br>\n");
				}

			}

//			if (name.equals("Overall applicability domain")) {
//				fw.write("<div class=\"tooltip\"><b>Overall applicability domain reasoning:</b> "+
//						"<span class=\"tooltiptext\">Reasoning for the overall applicability domain result</span></div>");
//				fw.write(" "+adEstimate.reasoning+"<br>");
//				//				fw.write("<div class=\"tooltip\"><b>Prediction reliability:</b> "+
//				//						"<span class=\"tooltiptext\">Reliability of the prediction based on the OPERA applicability domains</span></div>");
//				//				fw.write(" "+adEstimate.reliability+"<br>");
//			} 		
		}

	}

	protected void writeChemicalInfo(PredictionReport or, Writer fw) throws IOException {
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Chemical Identifiers</font></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		fw.write("<td>");
		
		if (or.chemicalIdentifiers.preferredName!=null)
			fw.write("<b>Preferred name:</b> "+or.chemicalIdentifiers.preferredName+"<br>");
		
		if (or.chemicalIdentifiers.dtxsid!=null)
			fw.write("<b>DTXSID:</b> "+or.chemicalIdentifiers.dtxsid+"<br>");
		
		if (or.chemicalIdentifiers.dtxcid!=null)
			fw.write("<b>DTXCID:</b> "+or.chemicalIdentifiers.dtxcid+"<br>");
		
		if (or.chemicalIdentifiers.casrn!=null)		
			fw.write("<b>CASRN:</b> "+or.chemicalIdentifiers.casrn+"<br>");
		
//		if (or.chemicalIdentifiers.smiles!=null) {		
//			fw.write("<b>SMILES:</b> "+or.chemicalIdentifiers.smiles+"<br>");
//			
////			String smiles=or.chemicalIdentifiers.smiles;
////			String smilesShort=smiles;
////			if(smiles.length()>20) {
////				smilesShort=smiles.substring(0,20)+"...";
////			}
////			fw.write("<b>SMILES:</b> "+"<div class=\"tooltip\">"+smilesShort+"</b> "+
////					  "<span class=\"tooltiptext\">"+smiles+"</span></div>"+"<br>");
//
//		}

		if (or.chemicalIdentifiers.molWeight!=null)		
			fw.write("<b>Molecular weight:</b> "+or.chemicalIdentifiers.molWeight+"<br>");
		
		fw.write("</td>\n");
		
				
		fw.write("\t</tr>\n");
		fw.write("</table>");
	}
	
	protected String createReport(PredictionReport or) {
		return "Need to override";
	}
	
	
	protected void writeStatsTable(PredictionReport or, Writer fw) throws IOException {
		System.out.println("need to override");
	}
	
//		System.out.println("Enter toHTMLFile with filename");
	public String toHTMLFile(PredictionReport or, String folder,String filename) {
		
		try {
			String filepath=folder+File.separator+filename;
			FileWriter fw=new FileWriter(filepath);
			
//			System.out.println(folder+File.separator+filename);
			String htmlReport=createReport(or);
			
			writeStringToFile(htmlReport, folder, filename);
			return filepath;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}
	
	public static void writeStringToFile(String strFileHtml, String folder,String filename) {
//		System.out.println("Enter toHTMLFile with filename");
		
		try {
			
			
			new File(folder).mkdirs();
			
			String filepath=folder+File.separator+filename;
			FileWriter fw=new FileWriter(filepath,Charset.forName("UTF-8"));
			
//			System.out.println(folder+File.separator+filename);
			fw.write(strFileHtml);
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	protected void writeIndividualModelsSummary (PredictionReport pr,Writer fw) throws Exception {
	
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Consensus predictions</color></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr><td>\n");
		
		fw.write("<table border=1 cellpadding=3 cellspacing=0>\n");
		fw.write("<caption>Predictions used in consensus prediction</caption>\n");

		fw.write("\t<tr bgcolor=\"lightgray\">\n");
		fw.write("\t<th>Method</th>\n");
		
		fw.write("\t<th>Predicted value "+pr.modelResults.consensusPredictions.unitsPrediction+"</th>\n");
		
		fw.write("\t</tr>\n");
		
		for(PredictionIndividualMethod pim:pr.modelResults.consensusPredictions.predictionsIndividualMethod) {
			
			if(pim.method!=null && pim.method.equals("Consensus")) {
				fw.write("\t<tr bgcolor=\"lightgray\">\n");	
			} else {
				fw.write("\t<tr>\n");
			}
			
			writeCenteredTD(fw, pim.method);
			
			if(pr.modelDetails.propertyIsBinary) {
				DecimalFormat df=new DecimalFormat("0.00");
				
				if(pim.predictedValue==null) {
					writeCenteredTD(fw, "N/A");
				} else {
//					System.out.println("pim.predictedValue="+pim.predictedValue);
					writeCenteredTD(fw, df.format(pim.predictedValue));	
				}
				
				
			} else {
				writeCenteredTD(fw, getFormattedValue(false, pim.predictedValue,"prop"));	
			}
			
			
			
//			String formattedValue=getFormattedValue(false,Double.parseDouble(pim.predictedValue),null);
//			writeCenteredTD(fw, formattedValue);
			
			fw.write("\t</tr>\n");
		}
		
		fw.write("</table>\n");
		
		fw.write("\t</td></tr>\n");

		fw.write("</table>\n");
	}


	public static void viewInWebBrowser(String filepath) {
		
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new File(filepath).toURI());
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	public String toHTMLFile(PredictionReport or, String folder) {
//		System.out.println("Enter toHTMLFile");
		return toHTMLFile(or, folder, or.chemicalIdentifiers.dtxsid+"_"+or.modelDetails.modelName+".html");
	}

	void getSamplePlotFromMaterializedView() {
		
		String sql="select mf.file_bytes from mv_model_files mf\n"+
                     "where model_name='OPERA_AOH' and file_type_name='Histogram plot';";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		try {
			rs.next();
			byte[] fileBites=rs.getBytes(1);
			FileOutputStream fos = new FileOutputStream("test.png");
			fos.write(fileBites);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	void getHtmlReportsFromPredictionReportsTable(long fk_source_id,String dtxsid,String folder) {
		
		String sql="select file_html,m.name_ccd\r\n"
				+ "from qsar_models.prediction_reports pr\r\n"
				+ "join qsar_models.predictions_dashboard  pd on pr.fk_predictions_dashboard_id = pd.id\r\n"
				+ "join qsar_models.models m on pd.fk_model_id = m.id\r\n"
				+ "join qsar_models.dsstox_records dr on pd.fk_dsstox_records_id = dr.id\r\n"
				+ "where m.fk_source_id="+fk_source_id+" and dr.dtxsid='"+dtxsid+"';";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		try {
			
			while (rs.next()) {
				
				byte[] fileBites=rs.getBytes(1);
				String modelName=rs.getString(2);
				
				File folderOut=new File(folder+dtxsid);
				folderOut.mkdirs();
				
				File fout=new File(folderOut.getAbsolutePath()+File.separator+dtxsid+"_"+modelName+".html");
				FileOutputStream fos = new FileOutputStream(fout);
				fos.write(fileBites);
				fos.close();
				
				viewInWebBrowser(fout.getAbsolutePath());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public String writeTabbedWebpage(String title, List<PredictionDashboard> listPredictionDashboard,TreeMap<String, Dataset>mapDatasets) {


    	for(PredictionDashboard pd:listPredictionDashboard) {
    		if(pd.getEndpoint()==null) {
    			gov.epa.databases.dev_qsar.qsar_models.entity.Model model=pd.getModel();
    			Dataset dataset=mapDatasets.get(model.getDatasetName());
    			pd.setEndpoint(dataset.getProperty().getName_ccd());
    		}
    	}
		
		listPredictionDashboard.sort((p1, p2) -> p1.getEndpoint().compareTo(p2.getEndpoint()));

		StringBuffer sb=new StringBuffer();
		sb.append(getHeader(title));
		sb.append("<div class=\"tab-container\">\r\n");
		sb.append("<div class=\"tab active\" data-tab=\"Summary\">Summary</div>\r\n");
    	for(PredictionDashboard pd:listPredictionDashboard) {
			sb.append("<div class=\"tab\" data-tab=\""+pd.endpoint+"\">"+pd.endpoint+"</div>\r\n");
    	}
		sb.append("</div>\r\n");


//		String strSummary=webpageCreator.writeEndpointSummaryTable(listPredictionResults);
		String strSummary=writeEndpointSummaryTable(listPredictionDashboard,mapDatasets);

		sb.append("<div id=\"Summary\" class=\"tab-content active\">"+strSummary+"</div>\n");
		
    	for (PredictionDashboard pd:listPredictionDashboard) {
    		
    		if(pd.getPredictionReport()==null) {
    			System.out.println(pd.getModel().getName()+"\tReport is null");
    			continue;
    		}
    		
    		String html=new String(pd.getPredictionReport().getFileHtml());
    		
			Document doc = Jsoup.parse(html);
			doc.outputSettings().indentAmount(2);
//       		System.out.println(doc.body().toString());

			sb.append("<div id=\""+pd.endpoint+"\" class=\"tab-content\">\n");
			sb.append(doc.body().html());
			sb.append("</div>\n");

    	}
    	
		sb.append(getFooter());
		Document doc = Jsoup.parse(sb.toString());
		doc.outputSettings().indentAmount(2);
//        		System.out.println(doc.toString());
		
		String html=doc.html();
		return html;
	}
	
	
	private String getFooter() {
	    String footer = """
	<script>
	    // JavaScript to handle tab switching
	    const tabs = document.querySelectorAll('.tab');
	    const tabContents = document.querySelectorAll('.tab-content');

	    tabs.forEach(tab => {
	        tab.addEventListener('click', () => {
	            // Remove active class from all tabs and contents
	            tabs.forEach(t => t.classList.remove('active'));
	            tabContents.forEach(tc => tc.classList.remove('active'));

	            // Add active class to the clicked tab and corresponding content
	            tab.classList.add('active');
	            document.getElementById(tab.dataset.tab).classList.add('active');
	        });
	    });
	</script>

	</body>
	</html>
	""";
	    return footer;
	}
	
	
	private String getFooterFancy() {
	    String footer = """
	<script>
	(function() {
	  const activateTab = (tabName) => {
	    const tabs = document.querySelectorAll('.tab');
	    const tabContents = document.querySelectorAll('.tab-content');
	    const targetTab = Array.from(tabs).find(t => t.dataset.tab === tabName);
	    if (!targetTab) return;

	    tabs.forEach(t => t.classList.remove('active'));
	    tabContents.forEach(tc => tc.classList.remove('active'));

	    targetTab.classList.add('active');
	    const content = document.getElementById(tabName);
	    if (content) content.classList.add('active');

	    // Keep the tab button visible
	    try { targetTab.scrollIntoView({ block: 'nearest', inline: 'nearest', behavior: 'smooth' }); } catch (e) {}
	  };

	  // Existing tab click behavior
	  document.querySelectorAll('.tab').forEach(tab => {
	    tab.addEventListener('click', () => activateTab(tab.dataset.tab));
	  });

	  // New: clicking a property link in the summary table activates its tab
	  document.addEventListener('click', (e) => {
	    const link = e.target.closest('.prop-link');
	    if (!link) return;
	    e.preventDefault();
	    activateTab(link.dataset.tab);
	  });
	})();
	</script>

	</body>
	</html>
	""";
	    return footer;
	}
	
	private String getHeader(String title) {
	    int width = 400;

	    String header = String.format("""
	<!DOCTYPE html>
	<html lang="en">
	<head>
	    <meta charset="UTF-8">
	    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <title>%s</title>
	    <style>
	        /* Tabs: 6 buttons per row with CSS Grid */
	        .tab-container {
	            display: grid;
	            grid-template-columns: repeat(6, minmax(0, 1fr));
	            gap: 8px;
	            position: sticky; /* stays at top without overlapping content */
	            top: 0;
	            width: 100%%;
	            background-color: #f7f7f9;
	            border-bottom: 1px solid #d6d6d6;
	            z-index: 1000;
	            padding: 8px;
	            box-sizing: border-box;
	        }

	        .tab {
	            display: inline-flex;
	            align-items: center;
	            justify-content: center;
	            padding: 8px 12px;
	            cursor: pointer;
	            border: 1px solid #b9b9b9;
	            border-radius: 6px;
	            background: linear-gradient(#ffffff, #ececec);
	            color: #333;
	            box-shadow: 0 1px 2px rgba(0,0,0,0.08);
	            box-sizing: border-box;
	            white-space: nowrap;
	            overflow: hidden;
	            text-overflow: ellipsis;
	            transition: background 0.2s ease, box-shadow 0.2s ease, transform 0.05s ease, border-color 0.2s ease;
	            user-select: none;
	            text-align: center;
	        }

	        .tab:hover {
	            background: linear-gradient(#ffffff, #e7e7e7);
	            box-shadow: 0 2px 3px rgba(0,0,0,0.1);
	        }

	        .tab:active {
	            transform: translateY(1px);
	        }

	        .tab:focus-visible {
	            outline: 2px solid #7aa7ff;
	            outline pipelines: 2px;
	            outline-offset: 2px;
	        }

	        .tab.active {
	            background: linear-gradient(#eaf2ff, #dae7ff);
	            border-color: #7aa7ff;
	            color: #1a4fb3;
	            box-shadow: 0 0 0 2px rgba(122,167,255,0.2) inset, 0 1px 2px rgba(0,0,0,0.08);
	        }

	        body {
	            margin: 0;
	        }

	        .tab-content {
	            display: none;
	            padding: 20px;
	            border: 1px solid #ccc;
	        }

	        .tab-content.active {
	            display: block;
	        }

	        .tooltip {
	            position: relative;
	            display: inline-block;
	            border-bottom: 1px dotted black;
	        }
	        .tooltip .tooltiptext {
	            visibility: hidden;
	            width: %dpx;
	            background-color: #555;
	            color: #fff;
	            text-align: center;
	            border-radius: 6px;
	            padding: 5px 0;
	            position: absolute;
	            z-index: 1;
	            bottom: 125%%;
	            left: 50%%;
	            margin-left: -60px;
	            opacity: 0;
	            transition: opacity 0.3s;
	        }
	        .tooltip .tooltiptext::after {
	            content: "";
	            position: absolute;
	            top: 100%%;
	            left: 50%%;
	            margin-left: -5px;
	            border-width: 5px;
	            border-style: solid;
	            border-color: #555 transparent transparent transparent;
	        }
	        .tooltip:hover .tooltiptext {
	            visibility: visible;
	            opacity: 1;
	        }
	    </style>
	</head>
	<body>""", title, width);

	    return header;
	}
	
	public String writeTabbedWebpage(String title, List<gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport> listPredictionReport) {

	    StringBuffer sb = new StringBuffer();
	    sb.append(getHeader(title));

	    // Build (endpoint, report) pairs and sort by endpoint (case-insensitive)
	    Gson gson = new Gson();
	    List<java.util.Map.Entry<String, gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport>> items = new ArrayList<>();
	    for (gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport prDB : listPredictionReport) {
	        String json = new String(prDB.getFileJson());
	        gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport pr =
	                gson.fromJson(json, gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.class);
	        String endpoint = pr.modelDetails.propertyName == null ? "" : pr.modelDetails.propertyName;
	        items.add(new java.util.AbstractMap.SimpleEntry<>(endpoint, prDB));
	    }
	    items.sort(java.util.Comparator.comparing(java.util.Map.Entry::getKey, String.CASE_INSENSITIVE_ORDER));

	    // Tab buttons (10 per row handled by CSS in getHeader)
	    sb.append("<div class=\"tab-container\">\n");
	    sb.append("<div class=\"tab active\" data-tab=\"Summary\" title=\"Summary\">Summary</div>\n");
	    for (java.util.Map.Entry<String, gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport> e : items) {
	        String endpoint = e.getKey();
	        sb.append("<div class=\"tab\" data-tab=\"").append(endpoint).append("\" title=\"")
	          .append(endpoint).append("\">").append(endpoint).append("</div>\n");
	    }
	    sb.append("</div>\n"); // end .tab-container

	    // Sorted summary
	    String strSummary = writeEndpointSummaryTableFancy(items);
	    sb.append("<div id=\"Summary\" class=\"tab-content active\">").append(strSummary).append("</div>\n");

	    // Endpoint contents (sorted)
	    for (java.util.Map.Entry<String, gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport> e : items) {
	        String endpoint = e.getKey();
	        gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport prDB = e.getValue();

	        String html = new String(prDB.getFileHtml());
	        Document doc = Jsoup.parse(html);
	        doc.outputSettings().indentAmount(2);

	        sb.append("<div id=\"").append(endpoint).append("\" class=\"tab-content\">\n");
	        sb.append(doc.body().html());
	        sb.append("</div>\n");
	    }

	    sb.append(getFooterFancy());

	    Document doc = Jsoup.parse(sb.toString());
	    doc.outputSettings().indentAmount(2);

	    return doc.html();
	}
	
	public String writeEndpointSummaryTableFancy(
		    List<java.util.Map.Entry<String, gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport>> items
		) {

		    StringWriter fw = new StringWriter();

		    fw.write("""
		<style>
		/* Centered container with constrained width so panels don't span full page */
		.summary-wrap {
		  display: grid;
		  grid-template-columns: minmax(520px, 680px) minmax(260px, 360px);
		  gap: 16px;
		  align-items: start;
		  max-width: 1100px;
		  margin: 12px auto 16px;
		  padding: 0 8px;
		}
		@media (max-width: 900px) {
		  .summary-wrap { grid-template-columns: 1fr; }
		}

		/* Panel styles (Predictions and Structure) */
		.table-card, .chem-card {
		  background: #fff;
		  border: 1px solid #e5e7eb;
		  border-radius: 8px;
		  padding: 12px;
		  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
		}
		.table-card { width: 100%; max-width: 680px; }
		.chem-card  { width: 100%; max-width: 360px; }

		/* Modern table */
		.table-modern {
		  width: 100%;
		  border-collapse: collapse;
		  background: transparent; /* panel provides bg */
		  table-layout: fixed;     /* enforce column widths */
		}

		/* Column widths: keep Property narrower and numeric columns consistent */
		.table-modern col.col-property { width: clamp(160px, 28%, 300px); }
		.table-modern col.col-num      { width: 140px; }
		.table-modern col.col-units    { width: 120px; }

		.table-modern caption {
		  caption-side: top;
		  padding: 0 4px 8px 4px;
		  font-weight: 600;
		  text-align: left;
		  color: #111827;
		}

		.table-modern thead th {
		  background: #f9fafb;
		  border-bottom: 1px solid #e5e7eb;
		  padding: 10px 12px;
		  font-size: 13px;
		  letter-spacing: .01em;
		  color: #374151;
		}

		.table-modern tbody td {
		  padding: 10px 12px;
		  border-bottom: 1px solid #f3f4f6;
		  vertical-align: top;
		  font-size: 14px;
		  color: #1f2937;
		}
		.table-modern tbody tr:last-child td { border-bottom: 0; }
		.table-modern tbody tr:nth-child(even) { background: #fcfcfd; }
		.table-modern tbody tr:hover { background: #f5faff; }

		.table-modern th.num, .table-modern td.num {
		  text-align: right;
		  font-variant-numeric: tabular-nums;
		}

		/* Allow long Property names to wrap within constrained column */
		.table-modern th.prop, .table-modern td.prop {
		  word-break: break-word;
		  overflow-wrap: anywhere;
		  hyphens: auto;
		}

		/* Clickable property link */
		.prop-link {
		  color: #1a4fb3;
		  text-decoration: none;
		}
		.prop-link:hover {
		  text-decoration: underline;
		}

		/* Chemical info card */
		.chem-card img {
		  width: 100%;
		  max-width: 240px;
		  height: auto;
		  border-radius: 4px;
		  display: block;
		  margin: 0 auto 12px;
		}
		.chem-card h3 {
		  margin: 0 0 8px 0;
		  font-size: 16px;
		  color: #111827;
		}
		.chem-card dl { margin: 0; }
		.chem-card dt {
		  font-weight: 600;
		  color: #374151;
		}
		.chem-card dd {
		  margin: 0 0 8px 0;
		  color: #1f2937;
		  word-break: break-word;
		}
		</style>
		""");

		    fw.write("<div class=\"summary-wrap\">");

		    // Left: Predictions table inside a bordered panel
		    fw.write("<section class=\"table-card\">");
		    fw.write("<table class=\"table-modern\">");
		    fw.write("<caption>Predictions</caption>");

		    // Column sizing
		    fw.write("<colgroup>");
		    fw.write("<col class=\"col-property\">");
		    fw.write("<col class=\"col-num\">");
		    fw.write("<col class=\"col-num\">");
		    fw.write("<col class=\"col-units\">");
		    fw.write("</colgroup>");

		    fw.write("<thead><tr>");
		    fw.write("<th class=\"prop\">Property</th>");
		    fw.write("<th class=\"num\">Experimental value</th>");
		    fw.write("<th class=\"num\">Predicted value</th>");
		    fw.write("<th>Units</th>");
		    fw.write("</tr></thead>");
		    fw.write("<tbody>");

		    Gson gson = new Gson();

		    for (java.util.Map.Entry<String, gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport> e : items) {
		        String endpoint = e.getKey();
		        gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport prDB = e.getValue();

		        String json = new String(prDB.getFileJson());
		        gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport pr =
		            gson.fromJson(json, gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.class);

		        Double exp = pr.modelResults.experimentalValue;
		        Double pred = pr.modelResults.predictedValue;

		        String strExp = getFormattedValue(false, exp, 3);
		        String strPred = getFormattedValue(false, pred, 3);
		        String unitAbbreviation = pr.modelResults.standardUnit;

		        fw.write("<tr>");
//		        fw.write("<td>" + endpoint +"</td>");
		        
		        // Clickable property name: activates its tab (handled by footer JS)
		        fw.write("<td class=\"prop\"><a href=\"#\" class=\"prop-link\" data-tab=\"" + endpoint + "\" title=\"Open " + endpoint + " tab\">" + endpoint + "</a></td>");
		        fw.write("<td class=\"num\">" + strExp + "</td>");
		        fw.write("<td class=\"num\">" + strPred + "</td>");
		        fw.write("<td>" + unitAbbreviation + "</td>");
		        fw.write("</tr>");
		    }

		    fw.write("</tbody>");
		    fw.write("</table>");
		    fw.write("</section>");

		    // Right: Structure panel (chemical info from the first item, if present)
		    if (!items.isEmpty()) {
		        gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport prDB0 = items.get(0).getValue();
		        String json0 = new String(prDB0.getFileJson());
		        gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport pr0 =
		            gson.fromJson(json0, gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.class);

		        String imageUrl = "https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/" + pr0.chemicalIdentifiers.dtxcid;

		        fw.write("<aside class=\"chem-card\">");
		        fw.write("<a href=\"" + imageUrl + "\"><img src=\"" + imageUrl + "\" alt=\"Structure\"></a>");
		        fw.write("<h3>Chemical</h3>");
		        fw.write("<dl>");
		        fw.write("<dt>DTXSID</dt><dd>" + pr0.chemicalIdentifiers.dtxsid + "</dd>");
		        fw.write("<dt>DTXCID</dt><dd>" + pr0.chemicalIdentifiers.dtxcid + "</dd>");
		        fw.write("<dt>CASRN</dt><dd>" + pr0.chemicalIdentifiers.casrn + "</dd>");
		        fw.write("<dt>Name</dt><dd>" + pr0.chemicalIdentifiers.preferredName + "</dd>");
		        fw.write("<dt>SMILES</dt><dd>" + pr0.chemicalIdentifiers.smiles + "</dd>");
		        fw.write("<dt>MW</dt><dd>" + pr0.chemicalIdentifiers.molWeight + "</dd>");
		        fw.write("</dl>");
		        fw.write("</aside>");
		    }

		    fw.write("</div>"); // end summary-wrap

		    return fw.toString();
		}
	
	public String writeEndpointSummaryTable(List<PredictionDashboard> pds,TreeMap<String, Dataset> mapDatasets)  {

		StringWriter fw=new StringWriter();
		
		fw.write("<p><p>");
		
		
		fw.write("<table cellpadding=\"3\" cellspacing=\"0\"><tr><td>\n");
		
		
		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>Predictions</caption>\r\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Property</th>\n");

//		fw.write("<th>Experimental value<br>" + units + "</th>\n");
//		fw.write("<th>Predicted value<br>" + units + "</th>\n");
		
		fw.write("<th>Experimental value</th>\n");
		fw.write("<th>Predicted value</th>\n");
		fw.write("<th>Units</th>\n");

		fw.write("</tr>\n");
		
		DecimalFormat df=new DecimalFormat("0.00");
		
		for (PredictionDashboard pd:pds) {

			fw.write("<tr>\n");
			
			String unitAbbreviation=null;
			String strExp=null;
			String strPred=null;
			
//			if(pd.getModel().getSource().getName().contains("TEST")) {
//				unitAbbreviation=TESTConstants.getDashboardUnits(pd.getEndpoint());//TODO fix
//				Double exp=getValueInCCD_Units(pd, pd.getExperimentalValue());
//				Double pred=getValueInCCD_Units(pd, pd.getPredictionValue());
//				strExp=getFormattedValue(TESTConstants.isBinary(pd.getEndpoint()), exp, 3);
//				strPred=getFormattedValue(TESTConstants.isBinary(pd.getEndpoint()), pred, 3);
//				
//				fw.write("<td>" + pd.getEndpoint() + "</td>\n");
//
//			
//			}else if (pd.getModel().getSource().getName().contains("OPERA")) {
//				
//				gov.epa.databases.dev_qsar.qsar_models.entity.Model model=pd.getModel();
//				Dataset dataset=maps.mapDatasets.get(model.getDatasetName());
//				unitAbbreviation=dataset.getUnitContributor().getAbbreviation_ccd();
//				
//				Double exp=pd.getExperimentalValue();
//				Double pred=pd.getPredictionValue();
//				
//				strExp=getFormattedValue(false, exp, 3);
//				strPred=getFormattedValue(false, pred, 3);
//
//				fw.write("<td>" + dataset.getProperty().getName_ccd() + "</td>\n");
//			}
			
			gov.epa.databases.dev_qsar.qsar_models.entity.Model model=pd.getModel();
			Dataset dataset=mapDatasets.get(model.getDatasetName());
			unitAbbreviation=dataset.getUnitContributor().getAbbreviation_ccd();
			
			Double exp=pd.getExperimentalValue();
			Double pred=pd.getPredictionValue();
			
			strExp=getFormattedValue(false, exp, 3);
			strPred=getFormattedValue(false, pred, 3);

			fw.write("<td>" + dataset.getProperty().getName_ccd() + "</td>\n");
			fw.write("<td>" + strExp + "</td>\n");
			fw.write("<td>" + strPred + "</td>\n");
			fw.write("<td>" + unitAbbreviation + "</td>\n");
			fw.write("</tr>\n");
		}
		fw.write("</table></td>\n");
		
		PredictionDashboard pd=pds.get(0);
		
//		String imageUrl="https://comptox.epa.gov/ctx-api/chemical/file/image/search/by-dtxcid/"+pd.getDsstoxRecord().getDtxcid();
		String imageUrl="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/"+pd.getDsstoxRecord().getDtxcid();
		
		
		fw.write("<td>"
				+ "<a href=\""+imageUrl+"\">"+"<img src=\"" +imageUrl+ "\" width=" + 200+ " border=0></a><br>"
				+"DTXSID: "+pd.getDsstoxRecord().getDtxsid()+"<br>"
				+"CASRN: "+pd.getDsstoxRecord().getCasrn()+"<br>"
				+"Name: "+pd.getDsstoxRecord().getPreferredName()+"<br>"
				+"Smiles: "+pd.getDsstoxRecord().getSmiles()+"<br>"
				+"MW: "+pd.getDsstoxRecord().getMolWeight()+"<br>"
				+ "</td>\n");
		
		fw.write("</tr></table>\n");
		
		return fw.toString();
		
	}
		
	
	public String writeEndpointSummaryTable(List<gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport> prs)  {

		StringWriter fw=new StringWriter();
		
		fw.write("<p><p>");
		
		
		fw.write("<table cellpadding=\"3\" cellspacing=\"0\"><tr><td>\n");
		
		
		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>Predictions</caption>\r\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Property</th>\n");

//		fw.write("<th>Experimental value<br>" + units + "</th>\n");
//		fw.write("<th>Predicted value<br>" + units + "</th>\n");
		
		fw.write("<th>Experimental value</th>\n");
		fw.write("<th>Predicted value</th>\n");
		fw.write("<th>Units</th>\n");

		fw.write("</tr>\n");
		
		DecimalFormat df=new DecimalFormat("0.00");//TODO use sig digits
		
		Gson gson=new Gson();
		
		for (gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport prDB:prs) {

			fw.write("<tr>\n");
			
			
			String json = new String(prDB.getFileJson());
			PredictionReport pr = gson.fromJson(json, PredictionReport.class);

			Double exp=pr.modelResults.experimentalValue;
			Double pred=pr.modelResults.predictedValue;
			
			System.out.println(pr.modelDetails.propertyName+"\t"+exp+"\t"+pred);
			
			String strExp=getFormattedValue(false, exp, 3);
			String strPred=getFormattedValue(false, pred, 3);
			String unitAbbreviation=pr.modelResults.standardUnit;

			fw.write("<td>" + pr.modelDetails.propertyName + "</td>\n");
			fw.write("<td>" + strExp + "</td>\n");
			fw.write("<td>" + strPred + "</td>\n");
			fw.write("<td>" + unitAbbreviation + "</td>\n");
			fw.write("</tr>\n");
		}
		fw.write("</table></td>\n");
		
		
		gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport pr0=prs.get(0);
		
		String json = new String(pr0.getFileJson());
		PredictionReport pr = gson.fromJson(json, PredictionReport.class);

		
//		String imageUrl="https://comptox.epa.gov/ctx-api/chemical/file/image/search/by-dtxcid/"+pd.getDsstoxRecord().getDtxcid();
		String imageUrl="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/"+pr.chemicalIdentifiers.dtxcid;
				
		fw.write("<td>"
				+ "<a href=\""+imageUrl+"\">"+"<img src=\"" +imageUrl+ "\" width=" + 200+ " border=0></a><br>"
				+"DTXSID: "+pr.chemicalIdentifiers.dtxsid+"<br>"
				+"DTXCID: "+pr.chemicalIdentifiers.dtxcid+"<br>"
				+"CASRN: "+pr.chemicalIdentifiers.casrn+"<br>"
				+"Name: "+pr.chemicalIdentifiers.preferredName+"<br>"
				+"Smiles: "+pr.chemicalIdentifiers.smiles+"<br>"
				+"MW: "+pr.chemicalIdentifiers.molWeight+"<br>"
				+ "</td>\n");
		
		fw.write("</tr></table>\n");
		
		return fw.toString();
		
	}
	
//	private Double getValueInCCD_Units(PredictionDashboard pd, Double predValue) {
//
//		if(predValue==null) return null; 
//
//		String endpoint=pd.getEndpoint();
//		Double valueInCCD_units=null;
//
//		//TODO make it work for both OPERA and TEST- use the units in the dataset and the converter
//		
//		if(endpoint.equals(TESTConstants.ChoiceFHM_LC50) || endpoint.equals(TESTConstants.ChoiceDM_LC50) || 
//				endpoint.equals(TESTConstants.ChoiceTP_IGC50) || endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
//			valueInCCD_units=Math.pow(10,-predValue);
//		} else if (endpoint.equals(TESTConstants.ChoiceBCF) || endpoint.equals(TESTConstants.ChoiceViscosity) || 
//				endpoint.equals(TESTConstants.ChoiceVaporPressure)) {
//			valueInCCD_units=Math.pow(10,predValue);
//		} else {
//			valueInCCD_units=predValue;
//		}
//		return valueInCCD_units;
//	}

	
	public static void main(String[] args) {
//		viewHTMLReportsFromMaterializedView("DTXSID7020182");
		HTMLReportCreator h=new HTMLReportCreator();
//		h.getSamplePlotFromMaterializedView();
		
		h.getHtmlReportsFromPredictionReportsTable(6, "DTXSID00943887", "data\\OPERA2.8\\reportsFromPredictionReportsTable\\");
		
	}	


}
