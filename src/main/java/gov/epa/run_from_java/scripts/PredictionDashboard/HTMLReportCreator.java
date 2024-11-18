package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.ADEstimate;



/**
* @author TMARTI02
*/
public class HTMLReportCreator {

	
	protected String imgURLCid="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/";
	protected String imgURLSid="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxsid/";
	protected String detailsURL="https://comptox.epa.gov/dashboard/chemical/details/";

	protected boolean debug=false;

	
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
	
	
	protected void writeExperimental(PredictionReport or, Writer fw) throws IOException {
		
//		String strExp="<b>Experimental value: </b>";
		
		String strExp=("<div class=\"tooltip\"><b>Experimental value: </b> "+
				  "<span class=\"tooltiptext\">Experimental value from "+or.modelDetails.modelSourceName+"</span></div>");

		if (or.modelResults.experimentalValue!=null) {
			String formattedValue=getFormattedValue(or.modelResults.experimentalValue,or.modelDetails.propertyName);
			strExp+="&nbsp;"+formattedValue+"&nbsp;"+or.modelResults.standardUnit;//add units
			
			if(or.modelResults.experimentalString!=null) {
				strExp+=("&nbsp;("+or.modelResults.experimentalString+")");
			}
		} else if(or.modelResults.experimentalString!=null) {
			strExp+=("&nbsp;"+or.modelResults.experimentalString+"");
		} else {
			strExp+="&nbsp;N/A";
		}

		fw.write(strExp+"<br>");
	}

	
	protected void writeExpNeighbor(PredictionReport or, Writer fw, Double experimentalValue,String experimentalString)
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
	

	
	protected void writePredNeighbor(PredictionReport or, Writer fw, Double predictedValue,String predictedString) throws IOException {
		
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

	

	protected void writeStyles(Writer fw) throws IOException {
		
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
	
	protected void writePrediction(PredictionReport or, Writer fw) throws IOException {
		fw.write("<div class=\"tooltip\"><b>Predicted value: </b> "+
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

	
	public void writeCenteredTD(Writer fw, String text) throws IOException {
		fw.write("<td align=center>"+text+"</td>\n");
	}
	
	public void writeCenteredTD(Writer fw, Double value) throws IOException {
		
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
		fw.write("<b>Model source:</b> "+or.modelDetails.modelSourceName+"<br>\n");

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
		fw.write("</table>");
	}

	
	protected void writeFirstRow(PredictionReport or, Writer fw) throws IOException {
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
	
	protected void writeAD(PredictionReport or, Writer fw) throws IOException {

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

	public void writeChemicalInfo(PredictionReport or, Writer fw) throws IOException {
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
		
		if (or.chemicalIdentifiers.smiles!=null)		
			fw.write("<b>SMILES:</b> "+or.chemicalIdentifiers.smiles+"<br>");

		
		fw.write("</td>\n");
		
				
		fw.write("\t</tr>\n");
		fw.write("</table>");
	}
	
	protected String createReport(PredictionReport or) {
		return "Need to override";
	}
	
	
	public void toHTMLFile(PredictionReport or, String folder,String filename) {
		
		try {
			FileWriter fw=new FileWriter(folder+File.separator+filename);
			String htmlReport=createReport(or);
			fw.write(htmlReport);
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	
	public void toHTMLFile(PredictionReport or, String folder) {
		toHTMLFile(or, folder, or.chemicalIdentifiers.dtxcid+"_"+or.modelDetails.modelName+".html");
	}


}
