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
import gov.epa.run_from_java.scripts.OPERA.OPERA_Report.ADEstimate;
import gov.epa.run_from_java.scripts.OPERA.OPERA_Report.Neighbor;

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
public class HTML_Report_Creator_From_OPERA_Report {

	String imgURLCid="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/";
	String imgURLSid="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxsid/";
	String detailsURL="https://comptox.epa.gov/dashboard/chemical/details/";

	boolean debug=false;
	
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

	void writeStyles(Writer fw) throws IOException {
		
		int width=400;
		
		String style="<style>\r\n" + "	.tooltip {\r\n" + "	  position: relative;\r\n" + "	  display: inline-block;\r\n"
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
				+ "	}\r\n" + "	</style>";
		
				
		fw.write(style);

	}
	
	

	private void writeFirstRow(OPERA_Report or, Writer fw) throws IOException {
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr>\n");

		//Structure Image for test chemical:
		String imgURL=this.imgURLCid+or.chemicalIdentifiers.dtxcid;
		
		String chemicalName=or.chemicalIdentifiers.preferredName;
		
		fw.write("\t\t<td width=150px valign=\"top\"><img src=\""+imgURL+"\" height=150 width=150 border=2 "
				+ "alt=\"Structural image of "+chemicalName+"\"></td>\n");
		
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
	
	public static String getFormattedValue(Double dvalue,String propertyName) {

		int nsig=3;

		DecimalFormat dfSci=new DecimalFormat("0.00E00");
		DecimalFormat dfInt=new DecimalFormat("0");

//		DecimalFormat df1=new DecimalFormat("0.00");
//		DecimalFormat df4=new DecimalFormat("0.0");
		
		try {
			
			
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
//			System.out.println("exception");
			return null;
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
			Double experimentalValue=null;
			String experimentalString=null;
			
			Double predictedValue=null;
			String predictedString=null; 
			
			
			for (Neighbor n:or.neighbors) {
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
			
			for (Neighbor n:or.neighbors) {
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

//		fw.write("<b>Model source description:</b> "+or.modelDetails.sourceDescription+"<br>\n");
		
//		fw.write("<b>Model source:</b>");
//		fw.write("<div class=\"tooltip\">"+or.modelDetails.modelSource+
//		  "<span class=\"tooltiptext\">"+or.modelDetails.sourceDescription+"</span></div><br>");

		
		fw.write("<b>Property name:</b> "+or.modelDetails.propertyName+"<br>\n");
		fw.write("<b>Property description:</b> "+or.modelDetails.propertyDescription+"<br>\n");

		
		writeExperimental(or, fw);

		writePrediction(or, fw);

		writeAD(or,fw);
		
//		this.writeGlobalAD(or, fw);
//		this.writeLocalAD(or, fw, df);
//		this.writeConfidenceIndex(or, fw, df);

//		fw.write("<b>OPERA version:</b> "+or.modelDetails.source+"<br>");

		fw.write("</td>\n");
				
		fw.write("\t</tr>\n");
		fw.write("</table>");
	}

	private void writeAD(OPERA_Report or, Writer fw) throws IOException {

		if (or.modelResults.adEstimates==null) return;

		//		fw.write("<br>\n");

		for (ADEstimate adEstimate:or.modelResults.adEstimates) {

			String name=adEstimate.adMethod.name;
			String description=adEstimate.adMethod.description;

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

	private void writeExperimental(OPERA_Report or, Writer fw) throws IOException {
		
		String strExp="<b>Experimental value: </b>";

		if (or.modelResults.experimentalValue!=null) {
			String formattedValue=getFormattedValue(or.modelResults.experimentalValue,or.modelDetails.propertyName);
			strExp+="&nbsp;"+formattedValue+"&nbsp;"+or.modelResults.standardUnit;//add units
			
			if(or.modelResults.experimentalString!=null) {
				strExp+=("&nbsp;("+or.modelResults.experimentalString+")");
			}
		} else if(or.modelResults.experimentalString!=null) {
			strExp+=("&nbsp;"+or.modelResults.experimentalString+"");
		} else {
			strExp+="N/A";
		}

		fw.write(strExp+"<br>");
	}
	
	private void writeExpNeighbor(OPERA_Report or, Writer fw, Double experimentalValue,String experimentalString)
			throws IOException {

		fw.write("<b>Measured:</b> ");
		
		String strExp="";
		
		if(experimentalValue!=null) {
			String formattedValue=getFormattedValue(experimentalValue,or.modelDetails.propertyName);
			strExp+="&nbsp;"+formattedValue+"&nbsp;"+or.modelResults.standardUnit;
			
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
	

	private void writePrediction(OPERA_Report or, Writer fw) throws IOException {
		fw.write("<div class=\"tooltip\"><b>Predicted value:</b> "+
				  "<span class=\"tooltiptext\">Predicted value from the model</span></div>");

		String strPred="N/A";
		
		if(or.modelResults.predictedValue!=null) {
			String formattedValue=getFormattedValue(or.modelResults.predictedValue,or.modelDetails.propertyName);
			strPred=" "+formattedValue+"&nbsp;"+or.modelResults.standardUnit;
//			System.out.println("pred formattedValue="+formattedValue);
		}
		
		if(or.modelResults.predictedString!=null) {
			strPred+=("&nbsp;("+or.modelResults.predictedString+")");
		}

		
		fw.write(strPred+"<br>");

	}
	
	private void writePredNeighbor(OPERA_Report or, Writer fw, Double predictedValue,String predictedString) throws IOException {
		
		fw.write("<div class=\"tooltip\"><b>Predicted:</b>"+
				  "<span class=\"tooltiptext\">Five fold cross validation prediction for the neighbor</span></div>");
		
		String strPred="";
		
		if(predictedValue!=null) {
			String formattedValue=getFormattedValue(predictedValue,or.modelDetails.propertyName);
			strPred+="&nbsp;"+formattedValue+"&nbsp;&nbsp;"+or.modelResults.standardUnit;
//			System.out.println("pred formattedValue="+formattedValue);
			
			if(predictedString!=null) {
				strPred+=("&nbsp;("+predictedString+")");
			}

		} else {
			if(predictedString!=null) {
				strPred+=("&nbsp;"+predictedString);
			}
			
		}
		
		
		if(strPred.isBlank()) strPred="N/A";
		
		fw.write(strPred+"<br><br>");
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
