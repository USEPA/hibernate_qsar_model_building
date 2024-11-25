package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA;

import java.io.File;
import java.io.Writer;
import java.io.IOException;
import java.io.StringWriter;

import gov.epa.run_from_java.scripts.PredictionDashboard.HTMLReportCreator;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.Neighbor;

/**
* @author TMARTI02
* 
* 
1.	HTML on the page has bigger problems using tables for presentation layout vs data table layout for screen reading.

2.	All images need alt text descriptors. You can programmatically add them by saying "This is a chemical structure image of <Chemical Name>", "This is a histogram of <XYZ>".
	Done

3.	The histogram blues are too close to each other. The better thing to do is to make them contrasting colors on the color wheel like Blue/Orange, Purple/Yellow. But add textures to it like diagonal lines or dots. (I think there are other render problems with the image and the columns lining up, but that is not an accessibility issue.)
4.	The scatterplots likely cannot be helped, but can still have alt text to say it's a scatterplot and possibly change one set of dots to X's or squares instead. How did you generate the scatterplots? Through a visualization tool like the one Jason Brown makes for CCD/RapidTox? Or is that from Django or R?
5.	Regarding grouped table columns (i.e. two headers stacked up), needs some help but I am asking other testers for guidance on a resolution. I'll have to get back to you about this one.
6.	Regarding keyboard accessibility: Please start using <hx> tags & tabindex attributes for keyboard navigation. Right now, if I hit tab, I only jump to the links on the page super old-school style, but really if you have visual headings on the page, tabbing should touch those headings, then jump to the links in the heading group before going to the next heading.
7.	You might find ARIA role tagging useful for addressing some of these concerns. https://www.w3.org/WAI/standards-guidelines/aria/
8.	You may want to work on responsiveness since zooming in 200% is an accessibility test. Scrolling side to side is ok, but I can tell there are problems with the underlying code since the black header bars don't stretch automatically across the screen. IIRC this is a viewport issue and setting to em for screen elements on the DOM so they stretch across the viewport. Will look this up for you if you need it.

* 
*/
public class HTMLReportCreatorOpera extends HTMLReportCreator {

	
	@Override
	public String createReport(PredictionReport or) {
		
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
			
			
			fw.write("<h3>OPERA Model Calculation Details: "+or.modelDetails.propertyName+"</h3>\r\n");
						
			
//			fw.write("<h2>OPERA Model Calculation Details: "+"<div class=\"tooltip\">"+or.modelDetails.propertyName+
//					  "<span class=\"tooltiptext\">"+or.modelDetails.propertyDescription+"</span></div>"+"</h2>\r\n");
			
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

	private void writeModelPerformance(PredictionReport or, Writer fw) throws IOException {
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Model Performance</color></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		
		fw.write("<table border=0 width=100%>\n");

		fw.write("\t<tr>\n");
		
		if (or.modelDetails.urlHistogram!=null) {
			fw.write("<td width=30%><img src=\""+or.modelDetails.urlHistogram+"\" alt=\"Histogram of property values for the training and test sets for "+or.modelDetails.propertyName+"\"></td>");
			fw.write("<td width=30%><img src=\""+or.modelDetails.urlScatterPlot+"\" alt=\"Scatter plot of experimental vs predicted values for the test set for "+or.modelDetails.propertyName+"\"></td>");
		} else {
			//do nothing?
		}
				
		
		fw.write("</tr></table>\n");

//		fw.write("\t<td>\n");
		writeStatsTable(or,fw);
		
		if (or.modelDetails.hasQmrfPdf==1) {
			fw.write("<span class=\"border\"><a href=\""+or.modelDetails.qmrfReportUrl+"\" target=\"_blank\"> "
					+ "Model summary in QSAR Model Reporting Format (QMRF)"
					+ "</a></span>"+
			"<style>.border {border: 2px solid darkblue; padding: 2px 4px 2px;}</style><br><br>");
		}
		
//		fw.write("\t</td>\n");

				
		fw.write("\t</tr>\n");
		fw.write("</table>");
	}

	
	private void writeStatsTable(PredictionReport or, Writer fw) throws IOException {

		if (or.modelDetails.performance.train.R2!=null) { 
			fw.write("<table width=40% border=1 cellpadding=0 cellspacing=0><caption>Weighted kNN model statistics</caption>\n");
			
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
			
			writeCenteredTD(fw, or.modelDetails.performance.fiveFoldICV.Q2);
			writeCenteredTD(fw, or.modelDetails.performance.fiveFoldICV.RMSE);

			writeCenteredTD(fw, or.modelDetails.performance.train.R2);
			writeCenteredTD(fw, or.modelDetails.performance.train.RMSE);
			
			writeCenteredTD(fw, or.modelDetails.performance.external.R2);
			writeCenteredTD(fw, or.modelDetails.performance.external.RMSE);
			
			fw.write("</tr>\n");
			
			fw.write("</table>\n");
			
			
		} else if (or.modelDetails.performance.train.BA!=null) {
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
			
			writeCenteredTD(fw, or.modelDetails.performance.fiveFoldICV.BA);
			writeCenteredTD(fw, or.modelDetails.performance.fiveFoldICV.SN);
			writeCenteredTD(fw, or.modelDetails.performance.fiveFoldICV.SP);
			
			writeCenteredTD(fw, or.modelDetails.performance.train.BA);
			writeCenteredTD(fw, or.modelDetails.performance.train.SN);
			writeCenteredTD(fw, or.modelDetails.performance.train.SP);
			
			
			writeCenteredTD(fw, or.modelDetails.performance.external.BA);
			writeCenteredTD(fw, or.modelDetails.performance.external.SN);
			writeCenteredTD(fw, or.modelDetails.performance.external.SP);
			fw.write("</tr>\n");
			
			fw.write("</table>\n");

		} else {
			fw.write("Statistics are unavailable for this endpoint");
			//Do nothing, no stats available
		}
		
		fw.write("<br>\n");
		
	}
	
	private void writeNeighbors(PredictionReport or, Writer fw) throws IOException {
		
		if (or.neighborsTraining.size()==0) return;
		
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Nearest Neighbors from Model Knowledge Base</color></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr><td>\n");
		writeNeighborsTableOpera(or, fw);
		fw.write("</td></tr>\n");		
		
		fw.write("</table>");
	}


	
	private void writeNeighborsTableOpera(PredictionReport or, Writer fw) throws IOException {
		
		boolean haveMissingExpVal=false;
				
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr>\n");
		
		for (int i=1;i<=5;i++) {
			
			Double experimentalValue=null;
			String experimentalString=null;
			
			Double predictedValue=null;
			String predictedString=null; 
			
			
			for (Neighbor n:or.neighborsTraining) {
				if(n.neighborNumber==i) {
					experimentalValue=n.experimentalValue;
					experimentalString=n.experimentalString;
					predictedValue=n.predictedValue;
					predictedString=n.predictedString;
					break;
				}
			}
			
			if(experimentalValue==null && predictedValue==null && experimentalString==null && predictedString==null) continue;//no match for neighbor number

			fw.write("\t\t<td valign=\"top\" width=20%>");
			fw.write("<b>Neighbor</b>: "+i+"<br>");
			
			if(experimentalValue==null && experimentalString==null) haveMissingExpVal=true;
			
			writeExpNeighbor(or, fw, experimentalValue,experimentalString);
			writePredNeighbor(or, fw, predictedValue,predictedString);
			
			for (Neighbor n:or.neighborsTraining) {
				if(n.neighborNumber!=i) continue;
								
				if(n.molImagePNGAvailable) {
					fw.write("<img src=\""+this.imgURLCid+n.dtxcid+"\" height=150 width=150 border=1 "
							+ "alt=\"Structural image of "+n.preferredName+"\"><br>");
					fw.write("<a href=\""+this.detailsURL+n.dtxsid+"\" target=\"_blank\">"+n.preferredName+"</a></figcaption><br>");
//					fw.write("<figure><img src=\""+imageURL+n.getDtxcid()+"\"\" height=150 width=150 border=1>");
//					fw.write("<figcaption><a href=\""+detailsURL+n.getDtxsid()+"\" target=\"_blank\">"+n.getPreferredName()+"</a></figcaption>");
//					fw.write("</figure>\n");
				} else if(n.dtxsid!=null) {
					if(!n.molImagePNGAvailable) {
						if(debug) System.out.println("Here1, for "+or.modelDetails.propertyName+ " for "+or.chemicalIdentifiers.dtxcid+ ", No dsstox record for "+n.dtxsid);
						
						if(n.preferredName!=null) {
							fw.write("No structure image<br><a href=\""+detailsURL+n.dtxsid+"\" target=\"_blank\">"+n.preferredName+"</a></figcaption><br>");	
							
						} else {
							fw.write("No structure image<br><a href=\""+detailsURL+n.dtxsid+"\" target=\"_blank\">"+n.dtxsid+"</a></figcaption><br>");	
						}
						
						
					} else {
						if(n.dtxcid!=null)	 {					
							if(debug) System.out.println("Here 2, For "+or.modelDetails.propertyName+" for "+or.chemicalIdentifiers.dtxcid+", have dsstox record for "+n.dtxsid+" (" +n.preferredName+")but no image for neighbor");
						}
						fw.write("No structure image<br><a href=\""+detailsURL+n.dtxsid+"\" target=\"_blank\">"+n.preferredName+"</a></figcaption><br>");
					}
					
				} else {
					
					if(n.casrn!=null && isCAS_OK(n.casrn))//just print the ones that have a valid cas but no still no dtxsid
						if(debug)System.out.println("Here 3, for "+or.modelDetails.propertyName+" for "+or.chemicalIdentifiers.dtxcid+", only have casrn="+n.casrn+" for neighbor (no dsstox record match)");
					else {
						if(debug) System.out.println("Here 4, for "+or.modelDetails.propertyName+" for "+or.chemicalIdentifiers.dtxcid+", only have casrn="+n.casrn+" for neighbor (no dsstox record match), bad CAS");
					}
					fw.write("No structure image<br>"+n.casrn+"<br>\n");
				}
				
				fw.write("<br>");
			}
			
			fw.write("</td>\n");
		}
		
		fw.write("\t</tr>\n");
		fw.write("</table>");
		
		if(haveMissingExpVal) {
			fw.write("* Some chemicals in the evaluation set do not have readily available experimental values");
		}
	}



//	private void writeConfidenceIndex(OPERA_Report or, Writer fw, DecimalFormat df) throws IOException {
//		
//		if (or.modelResults.confidence==null) return;
//		
//		fw.write("<div class=\"tooltip\"><b>Confidence level:</b> "+
//				  "<span class=\"tooltiptext\">Confidence level is calculated based on the\r\n"
//				  + "accuracy of the predictions of the five nearest neighbors\r\n"
//				  + "weighted by their distance to the query chemical</span></div>");
//		fw.write(" "+df.format(Double.parseDouble(or.modelResults.confidence))+"<br>");
//	}
//
//	private void writeLocalAD(OPERA_Report or, Writer fw, DecimalFormat df) throws IOException {
//		
//		if (or.modelResults.local==null) return;
//		
//		fw.write("<div class=\"tooltip\"><b>Local applicability domain index:</b> "+
//				  "<span class=\"tooltiptext\">Local applicability domain is relative to the similarity of the query chemical to its\r\n"
//				  + "five nearest neighbors</span></div>");
//
//		fw.write(" "+df.format(Double.parseDouble(or.modelResults.local))+"<br>");
//	}
//
//	void writeGlobalAD(OPERA_Report or, Writer fw) throws IOException {
//		
//		if (or.modelResults.msgs.globalTitle==null) {
//			return;
//		}
//		
//		fw.write("<div class=\"tooltip\"><b>Global applicability domain:</b> "+
//				  "<span class=\"tooltiptext\">Global applicability domain via the leverage approach</span></div>&nbsp;&nbsp;");
//				
//		if (or.modelResults.msgs.globalTitle.equals("Inside")) {
//			fw.write("<span class=\"borderAD\">Inside</span>"+
//			"<style>.borderAD {border: 2px solid green; padding: 0px 4px 0px}</style>");
//			
//		} else if (or.modelResults.msgs.globalTitle.equals("Outside")) {
//			fw.write("<span class=\"borderAD\">Outside</span>"+
//			"<style>.borderAD {border: 2px solid red; padding: 0px 4px 0px}</style>");
//		}
// 		
//		fw.write("<br>\n");
//		
//		
//	}
	
	
	public static void main(String[] args) {

		HTMLReportCreatorOpera h=new HTMLReportCreatorOpera();
		
		File folder=new File("data\\opera\\reports");
		
		for (File file:folder.listFiles()) {
			
			if (file.isDirectory()) {

				for (File file2:file.listFiles()) {
					if (file2.getName().contains(".json")) {
						PredictionReport or=OPERA_Report.fromJsonFile(file2.getAbsolutePath());
						h.toHTMLFile(or, file.getAbsolutePath());
						System.out.println(file2.getName());
					}
					
				}
				
			}
			
			if (file.getName().contains(".json")) {
				PredictionReport or=OPERA_Report.fromJsonFile(file.getAbsolutePath());
				h.toHTMLFile(or, folder.getAbsolutePath());
				System.out.println(file.getName());
			}
			
		}
		
		
	}
}