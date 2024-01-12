package gov.epa.run_from_java.scripts.OPERA;

import java.io.File;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.run_from_java.scripts.OPERA.OPERA_Report.Neighbor;

/**
* @author TMARTI02
*/
public class HTML_Report_Creator_From_OPERA_Report {

	String imgURLCid="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/";
	String imgURLSid="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxsid/";
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
			
			
			fw.write("<h3>OPERA Model Calculation Details</h3>\r\n");
			
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
		String imgURL=this.imgURLCid+or.chemicalIdentifiers.dtxcid;
		fw.write("\t\t<td width=150px valign=\"top\"><img src=\""+imgURL+"\" height=150 width=150 border=2></td>\n");
		
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
		
		if (or.modelDetails.hasQmrfPdf==1) {
			fw.write("<span class=\"border\"><a href=\""+or.modelDetails.qmrfReportUrl+"\" target=\"_blank\"> QMRF</a></span>"+
			"<style>.border {border: 2px solid darkblue; padding: 2px 4px 2px;}</style><br><br>");
		}
		
//		fw.write("\t</td>\n");

				
		fw.write("\t</tr>\n");
		fw.write("</table>");
	}

	void  writeCenteredTD(Writer fw, String text) throws IOException {
		fw.write("<td align=center>"+text+"</td>\n");
	}
	
	void  writeCenteredTD(Writer fw, Double value) throws IOException {
		
		if(value==null) {
			fw.write("<td align=center>N/A</td>\n");	
		} else {
			fw.write("<td align=center>"+value+"</td>\n");
		}
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
			
			writeCenteredTD(fw, or.modelResults.performance.fiveFoldICV.Q2);
			writeCenteredTD(fw, or.modelResults.performance.fiveFoldICV.RMSE);

			writeCenteredTD(fw, or.modelResults.performance.train.R2);
			writeCenteredTD(fw, or.modelResults.performance.train.RMSE);
			
			writeCenteredTD(fw, or.modelResults.performance.external.R2);
			writeCenteredTD(fw, or.modelResults.performance.external.RMSE);
			
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
			
			writeCenteredTD(fw, or.modelResults.performance.fiveFoldICV.BA);
			writeCenteredTD(fw, or.modelResults.performance.fiveFoldICV.SN);
			writeCenteredTD(fw, or.modelResults.performance.fiveFoldICV.SP);
			
			writeCenteredTD(fw, or.modelResults.performance.train.BA);
			writeCenteredTD(fw, or.modelResults.performance.train.SN);
			writeCenteredTD(fw, or.modelResults.performance.train.SP);
			
			
			writeCenteredTD(fw, or.modelResults.performance.external.BA);
			writeCenteredTD(fw, or.modelResults.performance.external.SN);
			writeCenteredTD(fw, or.modelResults.performance.external.SP);
			fw.write("</tr>\n");
			
			fw.write("</table>\n");

		} else {
			fw.write("Statistics are unavailable for this endpoint");
			//Do nothing, no stats available
		}
		
		fw.write("<br>\n");
		
	}
	
	private void writeNeighbors(OPERA_Report or, Writer fw) throws IOException {
		
		if (or.neighbors.size()==0) return;
		
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Nearest Neighbors from Model Knowledge Base</color></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr><td>\n");
		writeNeighborsTable(or, fw);
		fw.write("</td></tr>\n");		
		
		fw.write("</table>");
	}


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
	private void writeNeighborsTable(OPERA_Report or, Writer fw) throws IOException {

		boolean haveMissingExpVal=false;
		
		
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr>\n");
		
		for (int i=1;i<=5;i++) {
			String measured=null;
			String predicted=null; 
			for (Neighbor n:or.neighbors) {
				if(n.neighborNumber==i) {
					measured=n.measured;
					predicted=n.predicted;
					break;
				}
			}
			
			if(measured==null && predicted==null) continue;//no match for neighbor number

			fw.write("\t\t<td valign=\"top\" width=20%>");
			fw.write("<b>Neighbor</b>: "+i+"<br>");
			
			measured=getFormattedValue(measured,or.modelDetails.propertyName);
			
			fw.write("<b>Measured:</b> ");

			if(measured==null) {
				fw.write("N/A*<br>");
				haveMissingExpVal=true;
			} else {
				fw.write(measured+" "+or.modelResults.standardUnit+"<br>");//add units
			}
			
			fw.write("<div class=\"tooltip\"><b>Predicted:</b>"+
					  "<span class=\"tooltiptext\">Leave one out prediction for the neighbor</span></div>");
			
			fw.write(" "+getFormattedValue(predicted, or.modelDetails.propertyName)+" "+or.modelResults.standardUnit+"<br><br>");//add units
			
			for (Neighbor n:or.neighbors) {
				if(n.neighborNumber!=i) continue;
				
				if(n.dsstoxRecord!= null && n.dsstoxRecord.isMolImagePNGAvailable()) {
					fw.write("<img src=\""+this.imgURLCid+n.dsstoxRecord.getDtxcid()+"\"\" height=150 width=150 border=1><br>");
					fw.write("<a href=\""+this.detailsURL+n.dtxsid+"\" target=\"_blank\">"+n.dsstoxRecord.getPreferredName()+"</a></figcaption><br>");
//					fw.write("<figure><img src=\""+imageURL+n.getDtxcid()+"\"\" height=150 width=150 border=1>");
//					fw.write("<figcaption><a href=\""+detailsURL+n.getDtxsid()+"\" target=\"_blank\">"+n.getPreferredName()+"</a></figcaption>");
//					fw.write("</figure>\n");
				} else if(n.dtxsid!=null) {
					if(n.dsstoxRecord==null) {
						System.out.println("For "+or.modelDetails.propertyName+ " for "+or.chemicalIdentifiers.dtxcid+ ", No dsstox record for "+n.dtxsid);
						fw.write("No structure image<br><a href=\""+detailsURL+n.dtxsid+"\" target=\"_blank\">"+n.dtxsid+"</a></figcaption><br>");
					} else {
						if(n.dsstoxRecord.getDtxcid()!=null)						
							System.out.println("For "+or.modelDetails.propertyName+" for "+or.chemicalIdentifiers.dtxcid+", have dsstox record for "+n.dtxsid+" (" +n.dsstoxRecord.getPreferredName()+")but no image for neighbor");
						
						fw.write("No structure image<br><a href=\""+detailsURL+n.dtxsid+"\" target=\"_blank\">"+n.dsstoxRecord.getPreferredName()+"</a></figcaption><br>");
					}
					
				} else {
					
					if(isCAS_OK(n.casrn))//just print the ones that have a valid cas but no still no dtxsid
						System.out.println("For "+or.modelDetails.propertyName+" for "+or.chemicalIdentifiers.dtxcid+", only have casrn="+n.casrn+" for neighbor (no dsstox record match)");
					
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
	
	private void writeModelResults(OPERA_Report or, Writer fw) throws IOException {
		
		DecimalFormat df=new DecimalFormat("0.00");

		
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");
		fw.write("\t\t<td><font color=\"white\">Model Results</color></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		fw.write("<td>");

		fw.write("<b>Model name:</b> "+or.modelDetails.modelName+"<br>");
		fw.write("<b>Model source:</b> "+or.modelDetails.modelSource+"<br>\n");
		fw.write("<b>Property name:</b> "+or.modelDetails.propertyName+"<br>\n");
		fw.write("<b>Property description:</b> "+or.modelDetails.propertyDescription+"<br>\n");

		
		writeExperimental(or, fw);

		writePrediction(or, fw);

		this.writeGlobalAD(or, fw);
		this.writeLocalAD(or, fw, df);
		this.writeConfidenceIndex(or, fw, df);

//		fw.write("<b>OPERA version:</b> "+or.modelDetails.source+"<br>");

		fw.write("</td>\n");
				
		fw.write("\t</tr>\n");
		fw.write("</table>");
	}

	private void writeExperimental(OPERA_Report or, Writer fw) throws IOException {
		
		if (or.modelResults.experimental!=null) {
			fw.write("<b>Experimental value:</b> "+getFormattedValue(or.modelResults.experimental,or.modelDetails.propertyName));

			fw.write(" "+or.modelResults.standardUnit);//add units
			
//			if(or.modelDetails.modelName.contains("CATMoS") && !or.modelDetails.modelName.equals("CATMoS-LD50")) {
//				fw.write("mg/kg");				
//			}

			if(or.modelResults.experimentalConclusion!=null) {
				fw.write(" ("+or.modelResults.experimentalConclusion+")");
			}
			
		} else {
			fw.write("<b>Experimental value: </b>N/A");
		}
		
		fw.write("<br>\n");
	}

	private void writePrediction(OPERA_Report or, Writer fw) throws IOException {
		fw.write("<div class=\"tooltip\"><b>Predicted value:</b> "+
				  "<span class=\"tooltiptext\">Predicted value from the weighted kNN model. "
				  + "If the chemical is present in the training set of the model, "
				  + "the experimental value will match the predicted value</span></div>");

		
		if(or.modelResults.predicted!=null) {
			fw.write(" "+getFormattedValue(or.modelResults.predicted,or.modelDetails.propertyName)+" "+or.modelResults.standardUnit);

			if(or.modelResults.predictedConclusion!=null) {
				fw.write("("+or.modelResults.predictedConclusion+")");
			}
			
		} else {
			fw.write(" N/A");
		}
		fw.write("<br>");
	}

	private void writeConfidenceIndex(OPERA_Report or, Writer fw, DecimalFormat df) throws IOException {
		
		if (or.modelResults.confidence==null) return;
		
		fw.write("<div class=\"tooltip\"><b>Confidence level:</b> "+
				  "<span class=\"tooltiptext\">Confidence level is calculated based on the\r\n"
				  + "accuracy of the predictions of the five nearest neighbors\r\n"
				  + "weighted by their distance to the query chemical</span></div>");
		fw.write(" "+df.format(Double.parseDouble(or.modelResults.confidence))+"<br>");
	}

	private void writeLocalAD(OPERA_Report or, Writer fw, DecimalFormat df) throws IOException {
		
		if (or.modelResults.local==null) return;
		
		fw.write("<div class=\"tooltip\"><b>Local applicability domain index:</b> "+
				  "<span class=\"tooltiptext\">Local applicability domain is relative to the similarity of the query chemical to its\r\n"
				  + "five nearest neighbors in the p-dimensional space of the\r\n"
				  + "model using a weighted Euclidean distance </span></div>");

		fw.write(" "+df.format(Double.parseDouble(or.modelResults.local))+"<br>");
	}

	void writeGlobalAD(OPERA_Report or, Writer fw) throws IOException {
		
		if (or.modelResults.msgs.globalTitle==null) {
			return;
		}
		
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
