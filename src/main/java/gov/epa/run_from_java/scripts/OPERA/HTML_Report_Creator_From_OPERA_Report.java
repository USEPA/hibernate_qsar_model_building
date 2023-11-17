package gov.epa.run_from_java.scripts.OPERA;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Writer;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.List;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.OPERA.OPERA_Report.Neighbor;

/**
* @author TMARTI02
*/
public class HTML_Report_Creator_From_OPERA_Report {

	
	String imgURL="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/";

//	String imageURL="https://ccte-api-ccd-stg.epa.gov/ccdapp1/chemical-files/image/by-dtxcid/";
	String detailsURL="https://comptox.epa.gov/dashboard/chemical/details/";

	String createReport(OPERA_Report or) {
		
		try {
			
			if (or.modelResults.standardUnit!=null && or.modelResults.standardUnit.equals("Binary")) or.modelResults.standardUnit="";
			if (or.modelResults.standardUnit==null) or.modelResults.standardUnit="";
			
			StringWriter fw=new StringWriter();

			fw.write("<html>\n");
			fw.write("<head>\n");
			this.writeStyles(fw);
			
			fw.write("<title>OPERA results for " + or.chemicalIdentifiers.dtxcid + " for " + or.modelDetails.modelName+" model");
			fw.write("</title>\n");
			fw.write("</head>\n");
			
			
			fw.write("<h2>OPERA Model Calculation Details: "+or.modelDetails.propertyName+"</h2>\r\n");
			
			fw.write("<table border=0 width=100%>\n");
			
			fw.write("\t<tr><td>\n");
			writeFirstRow(or, fw);
			fw.write("\t</td></tr>\n");
			
			fw.write("\t<tr><td>\n");
			writeModelPerformance(or, fw);
			fw.write("\t</td></tr>\n");

			fw.write("\t<tr><td>\n");
			writeNeighbors(or, fw);
			fw.write("\t</td></tr>\n");
			
			fw.write("</table></html>\r\n");
			fw.flush();
			
            return fw.toString();

			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}

	void writeStyles(Writer fw) throws IOException {
		
		int width=400;
		
		fw.write("<style>\r\n" + "	.tooltip {\r\n" + "	  position: relative;\r\n" + "	  display: inline-block;\r\n"
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
	
	

	private void writeFirstRow(OPERA_Report or, Writer fw) throws IOException {
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr>\n");

		//Structure Image for test chemical:
		String imgURL=this.imgURL+or.chemicalIdentifiers.dtxcid;
		fw.write("\t\t<td width=150px><img src=\""+imgURL+"\" height=150 width=150 border=1></td>\n");
		
		fw.write("<td valign=\"top\">");
		writeChemicalInfo(or, fw);
		fw.write("</td>\r\n");
		
		fw.write("<td valign=\"top\">");
		writeModelResults(or, fw);
		fw.write("</td>\r\n");
		
		fw.write("\t</tr>\n");
		
		fw.write("</table>\n");
	}



	private void writeChemicalInfo(OPERA_Report or, Writer fw) throws IOException {
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Chemical Identifiers</color></td>\n");
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
		fw.write("</td>\n");
		
				
		fw.write("\t</tr>\n");
		fw.write("</table>");
	}
	
	
	String getFormattedValue(String value) {

		DecimalFormat df1=new DecimalFormat("0.00");
		DecimalFormat df2=new DecimalFormat("0.00E00");

		try {
			double dvalue=Double.parseDouble(value);
			
			

			if(dvalue<0.1 && dvalue!=0) {
				return df2.format(dvalue);
			} else  {
				return df1.format(dvalue);
			}

		} catch (Exception ex) {
			return value;
		}

		
	}
	
	
	private void writeModelPerformance(OPERA_Report or, Writer fw) throws IOException {
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Model Performance</color></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		
		fw.write("<table border=0 width=100%>\n");

		fw.write("\t<tr>\n");
		
		if (or.modelDetails.urlHistogram!=null) {
			fw.write("<td width=30%><img src=\""+or.modelDetails.urlHistogram+"\"></td>");
			fw.write("<td width=30%><img src=\""+or.modelDetails.urlScatterPlot+"\"></td>");
		} else {
			//do nothing?
		}
				
		
		fw.write("</tr></table>\n");

//		fw.write("\t<td>\n");
		writeStatsTable(or,fw);
		
		if (or.modelDetails.qmrfReportUrl!=null) {
			fw.write("<span class=\"border\"><a href=\""+or.modelDetails.qmrfReportUrl+"\"> QMRF</a></span>"+
			"<style>.border {border: 2px solid darkblue; padding: 2px 4px 2px;}</style><br><br>");
		}
		
//		fw.write("\t</td>\n");

				
		fw.write("\t</tr>\n");
		fw.write("</table>");
	}

	void  writeCenteredTD(Writer fw, String text) throws IOException {
		fw.write("<td align=center>"+text+"</td>\n");
	}
	
	
	private void writeStatsTable(OPERA_Report or, Writer fw) throws IOException {

		if (or.modelResults.performance.train.R2!=null) { 
			fw.write("<table width=100% border=1 cellpadding=0 cellspacing=0><caption>Weighted kNN model statistics</caption>\n");
			
			fw.write("<tr>\n");
			fw.write("<td colspan=2 align=center>5-fold CV (75%)</td>\n");
			fw.write("<td colspan=2 align=center>Training (75%)</td>\n");
			fw.write("<td colspan=2 align=center>Test (25%)</td>\n");
			fw.write("</tr>\n");
			
			fw.write("<tr>\n");
			writeCenteredTD(fw, "Q<sup>2</sup>");
			writeCenteredTD(fw, "RMSE");
			writeCenteredTD(fw, "R<sup>2</sup>");
			writeCenteredTD(fw, "RMSE");
			writeCenteredTD(fw, "R<sup>2</sup>");
			writeCenteredTD(fw, "RMSE");
			fw.write("</tr>\n");

			fw.write("<tr>\n");
			writeCenteredTD(fw, or.modelResults.performance.fiveFoldICV.Q2+"");
			
			if (or.modelResults.performance.fiveFoldICV.RMSE==null) {
				writeCenteredTD(fw, "NA");
			} else {
				writeCenteredTD(fw, or.modelResults.performance.fiveFoldICV.RMSE+"");
			}
			writeCenteredTD(fw, or.modelResults.performance.train.R2+"");
			writeCenteredTD(fw, or.modelResults.performance.train.RMSE+"");
			
			
			if (or.modelResults.performance.external.R2==null) {
				writeCenteredTD(fw, "NA");
			} else {
				writeCenteredTD(fw, or.modelResults.performance.external.R2+"");
			}
			

			if (or.modelResults.performance.external.RMSE==null) {
				writeCenteredTD(fw, "NA");
			} else {
				writeCenteredTD(fw, or.modelResults.performance.external.RMSE+"");
			}

			fw.write("</tr>\n");
			
			fw.write("</table>\n");
			
			
		} else if (or.modelResults.performance.train.BA!=null) {
			fw.write("<table width=40% border=1 cellpadding=0 cellspacing=0><caption>Weighted kNN model</caption>\n");
			
			fw.write("<tr>\n");
			fw.write("<td colspan=3 align=center>5-fold CV (75%)</td>\n");
			fw.write("<td colspan=3 align=center>Training (75%)</td>\n");
			fw.write("<td colspan=3 align=center>Test (25%)</td>\n");
			fw.write("</tr>\n");
			
			fw.write("<tr>\n");
			
			for(int i=1;i<=3;i++) {
				writeCenteredTD(fw, "BA");
				writeCenteredTD(fw, "SN");
				writeCenteredTD(fw, "SP");}

			fw.write("</tr>\n");

			fw.write("<tr>\n");
			writeCenteredTD(fw, or.modelResults.performance.fiveFoldICV.BA+"");
			writeCenteredTD(fw, or.modelResults.performance.fiveFoldICV.SN+"");
			writeCenteredTD(fw, or.modelResults.performance.fiveFoldICV.SP+"");
			writeCenteredTD(fw, or.modelResults.performance.train.BA+"");
			writeCenteredTD(fw, or.modelResults.performance.train.SN+"");
			writeCenteredTD(fw, or.modelResults.performance.train.SP+"");
			writeCenteredTD(fw, or.modelResults.performance.external.BA+"");
			writeCenteredTD(fw, or.modelResults.performance.external.SN+"");
			writeCenteredTD(fw, or.modelResults.performance.external.SP+"");
			fw.write("</tr>\n");
			
			fw.write("</table>\n");

		} else {
			fw.write("Statistics are unavailable for this endpoint");
			//Do nothing, no stats available
		}
		
		fw.write("<br>\n");
		
	}
	
	private void writeNeighbors(OPERA_Report or, Writer fw) throws IOException {
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Nearest neighbors from the training set</color></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr><td>\n");
		writeNeighborsTable(or, fw);
		fw.write("</td></tr>\n");		
		
		fw.write("</table>");
	}

	
	private void writeNeighborsTable(OPERA_Report or, Writer fw) throws IOException {

		
		
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr>\n");
		
		for (int i=1;i<=5;i++) {
			fw.write("\t\t<td valign=\"top\" width=20%>");
			fw.write("<b>Neighbor</b>: "+i+"<br>");
			
			String measured=null;
			String predicted=null; 
			
			for (Neighbor n:or.neighbors) {
				if(n.neighborNumber==i) {
					measured=n.measured;
					predicted=n.predicted;
					break;
				}
			}
			
			
			fw.write("<b>Measured</b>: "+getFormattedValue(measured)+" "+or.modelResults.standardUnit+"<br>");//add units
			
			
			fw.write("<div class=\"tooltip\"><b>Predicted:</b> "+
					  "<span class=\"tooltiptext\">Leave one out prediction for the neighbor</span></div>");
			
			fw.write(getFormattedValue(predicted)+" "+or.modelResults.standardUnit+"<br><br>");//add units
			
			for (Neighbor n:or.neighbors) {
				if(n.neighborNumber!=i) continue;
				
				if(n.molImagePNGAvailable) {
					fw.write("<img src=\""+this.imgURL+n.dtxcid+"\"\" height=150 width=150 border=1><br>");
					fw.write("<a href=\""+this.detailsURL+n.dtxsid+"\" target=\"_blank\">"+n.preferredName+"</a></figcaption><br>");
					
//					fw.write("<figure><img src=\""+imageURL+n.getDtxcid()+"\"\" height=150 width=150 border=1>");
//					fw.write("<figcaption><a href=\""+detailsURL+n.getDtxsid()+"\" target=\"_blank\">"+n.getPreferredName()+"</a></figcaption>");
//					fw.write("</figure>\n");
				} else if(n.dtxsid!=null) {
					fw.write("No structure image<br><a href=\""+detailsURL+n.dtxsid+"\" target=\"_blank\">"+n.preferredName+"</a></figcaption><br>");
				} else {
					fw.write(n.casrn+"<br>\n");
				}
				
				fw.write("<br>");
			}
			
			fw.write("</td>\n");
		}
		
		fw.write("\t</tr>\n");
		fw.write("</table>");
	}
	
	private void writeModelResults(OPERA_Report or, Writer fw) throws IOException {
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Model Results</color></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		fw.write("<td>");
		
		
		//TODO need to add units to prediction value:
		
		if (or.modelResults.experimental!=null) {
			fw.write("<b>Experimental value:</b> "+getFormattedValue(or.modelResults.experimental)+" "+or.modelResults.standardUnit+"<br>");
		} else {
			fw.write("<b>Experimental value: </b>N/A<br>");
		}
		
		
		fw.write("<div class=\"tooltip\"><b>Predicted value:</b> "+
				  "<span class=\"tooltiptext\">Predicted value from the weighted kNN model. "
				  + "If the chemical is present in the training set of the model, "
				  + "the experimental value will match the predicted value</span></div>");

		fw.write(getFormattedValue(or.modelResults.predicted)+" "+or.modelResults.standardUnit+"<br>");

		DecimalFormat df=new DecimalFormat("0.00");
		
		this.writeGlobalAD(or, fw);
		
		fw.write("<div class=\"tooltip\"><b>Local applicability domain index:</b> "+
				  "<span class=\"tooltiptext\">Local applicability domain is relative to the similarity of the query chemical to its\r\n"
				  + "five nearest neighbors in the p-dimensional space of the\r\n"
				  + "model using a weighted Euclidean distance </span></div>");

		fw.write(df.format(Double.parseDouble(or.modelResults.local))+"<br>");

		fw.write("<div class=\"tooltip\"><b>Confidence level:</b> "+
				  "<span class=\"tooltiptext\">Confidence level is calculated based on the\r\n"
				  + "accuracy of the predictions of the five nearest neighbors\r\n"
				  + "weighted by their distance to the query chemical</span></div>");
		fw.write(df.format(Double.parseDouble(or.modelResults.confidence))+"<br>");

		fw.write("<b>Model:</b> "+or.modelDetails.modelName+"<br>");
//		fw.write("<b>OPERA version:</b> "+or.modelDetails.source+"<br>");

		fw.write("</td>\n");
				
		fw.write("\t</tr>\n");
		fw.write("</table>");
	}

	void writeGlobalAD(OPERA_Report or, Writer fw) throws IOException {
		fw.write("<div class=\"tooltip\"><b>Global applicability domain:</b> "+
				  "<span class=\"tooltiptext\">Global applicability domain via the leverage approach</span></div>");
				
		if (or.modelResults.msgs.globalTitle.equals("Inside")) {
			fw.write("<span class=\"borderAD\">Inside</span>"+
			"<style>.borderAD {border: 2px solid green; padding: 0px 4px 0px}</style>");
			
		} else if (or.modelResults.msgs.globalTitle.equals("Outside")) {
			fw.write("<span class=\"borderAD\">Outside</span>"+
			"<style>.borderAD {border: 2px solid red; padding: 0px 4px 0px}</style>");
		}
 		
		fw.write("<br>\n");
		
		
	}
	

	public static void main(String[] args) {

		HTML_Report_Creator_From_OPERA_Report h=new HTML_Report_Creator_From_OPERA_Report();
		
		File folder=new File("data\\opera\\reports");
		
		for (File file:folder.listFiles()) {
			
			if (file.isDirectory()) {

				for (File file2:file.listFiles()) {
					if (file2.getName().contains(".json")) {
						OPERA_Report or=OPERA_Report.fromJsonFile(file2.getAbsolutePath());
						or.toHTMLFile(file.getAbsolutePath());
						System.out.println(file2.getName());
					}
					
				}
				
			}
			
			if (file.getName().contains(".json")) {
				OPERA_Report or=OPERA_Report.fromJsonFile(file.getAbsolutePath());
				or.toHTMLFile(folder.getAbsolutePath());
				
				System.out.println(file.getName());
			}
			
		}
		
		
	}
}
