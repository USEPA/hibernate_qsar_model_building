package gov.epa.run_from_java.scripts.PredictionDashboard.TEST;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.model.ExternalPredChart;
import ToxPredictor.Application.model.PredictionResults;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.run_from_java.scripts.QsarModelsScript;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.HTMLReportCreator;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.Neighbor;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.NeighborResults;

/**
 * @author TMARTI02
 */
public class HTMLReportCreatorTEST extends HTMLReportCreator {

	@Override
	public String createReport(PredictionReport or) {

		try {

			if (or.modelResults.standardUnit != null && or.modelResults.standardUnit.equals("Binary"))
				or.modelResults.standardUnit = "";
			if (or.modelResults.standardUnit == null)
				or.modelResults.standardUnit = "";

			StringWriter fw = new StringWriter();

			writeHtmlHead(or, fw);
			
			fw.write("<title>TEST results for " + or.chemicalIdentifiers.dtxcid + " for " + or.modelDetails.modelName
					+ " model");
			fw.write("</title>\n");
			fw.write("</head>\n");

			fw.write("<h3>TEST Model Calculation Details: " + or.modelDetails.propertyName + "</h3>\r\n");

			fw.write("<table border=0 width=100%>\n");

			fw.write("\t<tr><td>\n");
			writeFirstRow(or, fw);
			fw.write("\t</td></tr>\n");

			fw.write("\t<tr><td>\n");
			writeModelPerformance(or, fw);
			fw.write("\t</td></tr>\n");

			fw.write("<p>\n");

			//TODO add tooltip for how similarity is calculated...
			
			if (or.neighborResultsPrediction != null) {
				fw.write("\t<tr><td>\n");
				writeNeighbors(or, fw, 1);
				fw.write("\t</td></tr>\n");
				fw.write("<p>\n");
			}

			if (or.neighborResultsTraining != null) {
				fw.write("\t<tr><td>\n");
				writeNeighbors(or, fw, 0);
				fw.write("\t</td></tr>\n");

			}

			fw.write("</table></html>\r\n");
			fw.flush();

			
			return toPrettyHtml(fw);
//			return fw.toString();

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private void writeNeighbors(PredictionReport or, Writer fw, int splitNum) throws IOException {

		fw.write("<table border=0 width=100%>\n");
		fw.write("\t<tr bgcolor=\"black\">\n");

		if (splitNum == 1) {
			fw.write("\t\t<td><font color=\"white\">Nearest Neighbors from Test Set</color></td>\n");
		} else if (splitNum == 0) {
			fw.write("\t\t<td><font color=\"white\">Nearest Neighbors from Training Set</color></td>\n");
		}

		fw.write("\t</tr>\n");
		fw.write("\t</table>\n");

		String propertyName=or.modelDetails.propertyName;
		
		if (splitNum == 1) {
			
			if (!or.modelResults.standardUnit.contains(DevQsarConstants.BINARY)) { 
				writeNeighborContinuousResults(or.neighborResultsPrediction, fw);				
			} else {
				writeNeighborBinaryResults(propertyName, or.neighborResultsPrediction, fw);
			}

			writeNeighborsTableTEST_Horizontal(or, fw, or.neighborResultsPrediction);
		} else if (splitNum == 0) {
			
			if (!or.modelResults.standardUnit.contains(DevQsarConstants.BINARY)) {
				writeNeighborContinuousResults(or.neighborResultsTraining, fw);				
			} else {
				writeNeighborBinaryResults(propertyName, or.neighborResultsTraining, fw);
			}

			writeNeighborsTableTEST_Horizontal(or, fw, or.neighborResultsTraining);
		}

	}

	private void writeNeighborBinaryResults(String propertyName, NeighborResults nr, Writer fw) throws IOException {


		boolean haveNeighborPrediction=false;

		for (int i=1;i<nr.neighbors.size();i++) {
			Neighbor n=nr.neighbors.get(i);

			if(n.predictedValue!=null) {
				haveNeighborPrediction=true;
				break;
			}
		}

//		System.out.println(propertyName+"\t"+nr.neighbors.size()+"\thaveNeighborPrediction="+haveNeighborPrediction);
		
		if (nr.neighbors.size() == 0 || !haveNeighborPrediction) {
			fw.write("<font color=red>* Note: no sufficiently similar chemicals were predicted</font><br><br>\n");
			return;
		}


		fw.write("\t<p><table border=1 cellpadding=10 cellspacing=0>\n");
		fw.write("\t<caption>Prediction statistics for similar chemicals</caption>\n");

		fw.write("\t<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("\t<th>Concordance</th>\n");
		fw.write("\t<th>Sensitivity</th>\n");
		fw.write("\t<th>Specificity</th>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		writeCenteredTD(fw, nr.Concordance);
		writeCenteredTD(fw, nr.Sensitivity);
		writeCenteredTD(fw, nr.Specificity);
		fw.write("\t</tr>\n");

		fw.write("\t</table><p>\n");

	}

	private void writeNeighborContinuousResults(NeighborResults nr, Writer fw) throws IOException {
		
//		System.out.println("writeExternalPredChart");
		
		boolean haveNeighborPrediction=false;
		
		for (int i=1;i<nr.neighbors.size();i++) {
			Neighbor n=nr.neighbors.get(i);
			
			if(n.predictedValue!=null) {
				haveNeighborPrediction=true;
				break;
			}
		}
		
		if (nr.neighbors.size() == 0 || !haveNeighborPrediction) {
			fw.write("<font color=red>* Note: no sufficiently similar chemicals were predicted</font><br><br>\n");
			return;
		}

		DecimalFormat df = new DecimalFormat("0.00");

		fw.write("<table width=67%><tr>\n");
		fw.write("<td width=33%><img src=\"" + nr.chartImgSrc + "\"></td>\n");

		if(nr.MAEEntireTestSet!=null) {
			writeTableMAE(nr, fw, df);
		}
		
		writeSimilarityLegend(fw);
		
		fw.write("</tr></table>\n");
		

	}

	private void writeTableMAE(NeighborResults nr, Writer fw, DecimalFormat df) throws IOException {
		fw.write("<td width=33%>\n");

		fw.write("\t<table border=1 cellpadding=10 cellspacing=0>\n");

		fw.write("\t<caption>Results for entire set vs<br>results for similar chemicals</caption>\n");

		fw.write("\t<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("\t<th>Chemicals</th>\n");
		fw.write("\t<th>MAE*</th>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		fw.write("\t<td>Entire set</td>\n");
		
//		System.out.println(nr.MAEEntireTestSet);
		fw.write("\t<td>" + df.format(nr.MAEEntireTestSet) + "</td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		fw.write("\t<td>Similarity coefficient &ge; " + "0.5" + "</td>\n");

		if (nr.MAE < nr.MAEEntireTestSet) {
			fw.write("\t<td BGCOLOR=\"#90EE90\">" + df.format(nr.MAE) + "</td>\n");
		} else {
			fw.write("\t<td BGCOLOR=LIGHTPINK>" + df.format(nr.MAE) + "</td>\n");
		}
		// fw.write("\t<td>"+df.format(MAE)+"</td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t</table>\n");

		fw.write("*Mean absolute error in " + nr.unitNeighbor + "\n");

//			writeSimilarityLegend(fw);

		fw.write("</td>\n");
	}

//	/**
//	 * There was a fix I made to omit FDA prediction from consensus prediction for
//	 * neighbor (values pulled from text file inside TEST jar file) so the Dashboard reports 
//	 * sometimes have wrong neighbor predictions...
//	 * 
//	 * @param or
//	 * @param fw
//	 * @param nr
//	 * @throws IOException
//	 */
//	private void writeNeighborsTableTESTVertical(PredictionReport or, Writer fw, NeighborResults nr) throws IOException {
//
//		if(nr.unitNeighbor==null) System.out.println(or.modelDetails.propertyName);
//		
//		if (nr == null)
//			return;
//
//		boolean haveMissingExpVal = false;
//		
//		DecimalFormat df=new DecimalFormat("0.00");
//
////		fw.write("<table border=1 width=100%>\n");
//		fw.write("<table border=1 cellpadding=3 cellspacing=0>\n");
//
//		fw.write("\t<tr bgcolor=\"#D3D3D3\">\n");
//
//		fw.write("\t<th>CAS</th>\n");
//		fw.write("\t<th>Structure</th>\n");
//		fw.write("\t<th>Similarity Coefficient</th>\n");
//
////		if(nr.unitNeighbor!=null && nr.unitNeighbor.contains("Binary")) {
////			fw.write("\t<th>Experimental Value<br>" + nr.unitNeighbor + "</th>\n");
////			fw.write("\t<th>Predicted Value<br>" + nr.unitNeighbor + "</th>\n");
////		}
//		
//		fw.write("\t<th>Experimental Value<br>" + nr.unitNeighbor + "</th>\n");
//		fw.write("\t<th>Predicted Value<br>" + nr.unitNeighbor + "</th>\n");
//
//		fw.write("\t</tr>\n");
//
//		for (Neighbor n : nr.neighbors) {
//
//			fw.write("\t<tr>\n");
//
//			if (n.dtxsid != null) {
//				fw.write("<td><a href=\"" + this.detailsURL + n.dtxsid + "\" target=\"_blank\">" + n.casrn
//						+ "</a></figcaption><br></td>");
//			} else {
//				fw.write("<td>" + n.casrn + "</td>");
//			}
//
//			if (n.molImagePNGAvailable) {
//				fw.write("<td><img src=\"" + this.imgURLCid + n.dtxcid + "\" height=150 width=150 border=0 "
//						+ "alt=\"Structural image of " + n.preferredName + "\"></td>");
//			} else if (n.dtxsid != null) {
//				if (!n.molImagePNGAvailable) {
//					fw.write("<td>No structure image<br><a href=\"" + detailsURL + n.dtxsid + "\" target=\"_blank\">"
//							+ n.casrn + "</a></figcaption></td>");
//				} else {
//					fw.write("<td>No structure image<br><a href=\"" + detailsURL + n.dtxsid + "\" target=\"_blank\">"
//							+ n.casrn + "</a></figcaption></td>");
//				}
//
//			}
////			fw.write("<br>");
//
//			fw.write("\t</td>\n");
//			
//			String q="\"";
//			
//			fw.write("<td bgcolor="+q+n.backgroundColor+q+" align=center>"+n.similarityCoefficient+"</td>");
//			
////			if(nr.unitNeighbor!=null && nr.unitNeighbor.contains("Binary")) {
////				this.writeCenteredTD(fw, n.experimentalString);
////				this.writeCenteredTD(fw, n.predictedValue);
////			}
//			
//
//			if(n.experimentalValue==null) {
//				this.writeCenteredTD(fw,"N/A");
//			} else {
//				if(nr.unitNeighbor!=null && nr.unitNeighbor.contains("Binary")) {
//					this.writeCenteredTD(fw,df.format(n.experimentalValue));
//				} else {
//					this.writeCenteredTD(fw, getFormattedValue(n.experimentalValue, or.modelDetails.propertyName,3));
//				}
//			}
//			
//			if(n.predictedValue==null) {
//				this.writeCenteredTD(fw,"N/A");
//			} else {
//				if(nr.unitNeighbor!=null && nr.unitNeighbor.contains("Binary")) {
//					this.writeCenteredTD(fw, df.format(n.predictedValue));
//				} else {
//					this.writeCenteredTD(fw, getFormattedValue(n.predictedValue, or.modelDetails.propertyName,3));
//				}
//				
//			}				
//
//			
//			fw.write("\t</tr>\n");
//
//		}
//
////		fw.write("\t</tr>\n");
//		fw.write("</table>");
//
//		if (haveMissingExpVal) {
//			fw.write("* Some chemicals in the evaluation set do not have readily available experimental values");
//		}
//
//	}
	
	private void writeNeighborsTableTEST_Horizontal(PredictionReport or, Writer fw, NeighborResults nr) throws IOException {

		if(nr.unitNeighbor==null) System.out.println(or.modelDetails.propertyName);
		
		if (nr == null)
			return;

		boolean haveMissingExpVal = false;
		
		DecimalFormat df=new DecimalFormat("0.00");

//		fw.write("<table border=1 width=100%>\n");
//		fw.write("<table border=1 cellpadding=3 cellspacing=0>\n");
		
		int percentage1=(int)((double)nr.neighbors.size()/8*100);
		if(percentage1>100) percentage1=100;
		fw.write("<table cellspacing=10 width="+percentage1+"%>\n");
//		System.out.println(nr.neighbors.size()+"\t"+percentage1);
		
		fw.write("\t<tr>\n");
		int countInRow=0;
		int countInRowMax=6;
		
		int percentage=(int)(1/(double)countInRowMax*100);
		
		
		for (Neighbor n : nr.neighbors) {

			fw.write("<td valign=\"top\" width="+percentage+"%>\n");
			writeNeighborTile(or, fw, nr, df, n);	
			fw.write("\t</td>\n");
			
			countInRow++;
			
			if(countInRow==countInRowMax) {
				fw.write("\t</tr>\n");
				fw.write("\t<tr>\n");
				countInRow=0;
			}

		}

		fw.write("\t</tr>\n");
		fw.write("</table>");

		if (haveMissingExpVal) {
			fw.write("* Some chemicals in the evaluation set do not have readily available experimental values");
		}

	}
	
	void writeSimilarityLegend(Writer fw) throws IOException {
//		System.out.println("writeSimilarityLegend");
		
		fw.write("<td width=33%>\n");
		
		
		fw.write("\t<table border=1 cellpadding=5 cellspacing=0>\n");
		fw.write("\t<caption>Color legend</caption>\n");
		
		fw.write("\t<tr bgcolor=\"#D3D3D3\">\n");		
		fw.write("\t<th>Color</th>\n");
		fw.write("\t<th>Range*</th>\n");		
		fw.write("\t</tr>\n");
		
		fw.write("\t<tr>\n");		
		fw.write("\t<td bgcolor=green></td>\n");
		fw.write("\t<td>SC &#8805; 0.9 </td>\n");
		fw.write("\t</tr>\n");
		
		fw.write("\t<tr>\n");		
		fw.write("\t<td bgcolor=blue></td>\n");
		fw.write("\t<td>0.8 &#8804; SC < 0.9</td>\n");
		fw.write("\t</tr>\n");
		
		fw.write("\t<tr>\n");		
		fw.write("\t<td bgcolor=yellow></td>\n");
		fw.write("\t<td>0.7 &#8804; SC < 0.8</td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");		
		fw.write("\t<td bgcolor=orange></td>\n");
		fw.write("\t<td>0.6 &#8804; SC < 0.7</td>\n");
		fw.write("\t</tr>\n");
		
		fw.write("\t<tr>\n");		
		fw.write("\t<td bgcolor=red></td>\n");
		fw.write("\t<td>0.6 < SC</td>\n");
		fw.write("\t</tr>\n");

		
		fw.write("\t</table>\n");
		fw.write("*SC = similarity coefficient\n");
		
			
		
		fw.write("</td>\n");
		
	}

	private void writeNeighborTile(PredictionReport or, Writer fw, NeighborResults nr, DecimalFormat df, Neighbor n)
			throws IOException {
		
		
//		if (n.dtxsid != null) {
//			fw.write("<a href=\"" + this.detailsURL + n.dtxsid + "\" target=\"_blank\">" + n.casrn
//					+ "</a></figcaption><br>");
//		} else {
//			fw.write(n.casrn);
//		}
		
		String q="\"";
		
		if(n.similarityCoefficient==1.0) {
			fw.write("<b>Test chemical</b><br>");
//			fw.write("<b>Similarity</b>: "+n.similarityCoefficient+"<br>");
		} else {
//			fw.write("<b>Neighbor</b>: "+n.neighborNumber+"<br>");
			fw.write("<b>Similarity</b>: "+df.format(n.similarityCoefficient)+"<br>");	
		}
		
		
		if(n.experimentalValue==null) {
			fw.write("<b>Measured</b>: N/A");
			
		} else {
			if(nr.unitNeighbor!=null && or.modelDetails.propertyIsBinary) {
				fw.write("<b>Measured</b>: "+df.format(n.experimentalValue)+" "+nr.unitNeighbor);
				
			} else {
				fw.write("<b>Measured</b>: "+getFormattedValue(or.modelDetails.propertyIsBinary, n.experimentalValue, or.modelDetails.propertyName,3)+" "+nr.unitNeighbor);
			}
		}
		fw.write("<br>\n");
		
		if(n.predictedValue==null) {
			fw.write("<b>Predicted</b>: N/A");
			
		} else {
			if(nr.unitNeighbor!=null && or.modelDetails.propertyIsBinary) {
				fw.write("<b>Predicted</b>: "+df.format(n.predictedValue)+" "+nr.unitNeighbor);
				
			} else {
				fw.write("<b>Predicted</b>: "+getFormattedValue(or.modelDetails.propertyIsBinary, n.predictedValue, or.modelDetails.propertyName,3)+" "+nr.unitNeighbor);
			}
		}
		fw.write("<br>\n");

		
		String name=n.preferredName;
		
		if(name!=null) {
			if(name.length()>40) name=name.substring(0, 40)+"...";
		} else {
			name=n.casrn;
		}
		
		if (n.molImagePNGAvailable) {
			fw.write("<img src=\"" + this.imgURLCid + n.dtxcid + "\" height=150 width=150 "
//					+ "border=1 "
					+"style='border:3px solid "+n.backgroundColor+"'"
					
					+ "alt=\"Structural image of " + n.preferredName + "\"><br>");
			
		} else if (n.dtxsid != null) {
			if (!n.molImagePNGAvailable) {
				fw.write("No structure image<br><a href=\"" + detailsURL + n.dtxsid + "\" target=\"_blank\">"
						+ n.casrn + "</a></figcaption>");
			} else {
				fw.write("No structure image<br><a href=\"" + detailsURL + n.dtxsid + "\" target=\"_blank\">"
						+ n.casrn + "</a></figcaption>");
			}
		}
		
		if(n.dtxsid!=null) {
			fw.write("<a href="+q+detailsURL+n.dtxsid+q+" target="+q+"_blank"+q+">"+name+"</a>\n");
		} else {
			fw.write(name+"\n");
		}
		
//			fw.write("<br>");

		
		
		if(true)return;
		
//			if(nr.unitNeighbor!=null && nr.unitNeighbor.contains("Binary")) {
//				this.writeCenteredTD(fw, n.experimentalString);
//				this.writeCenteredTD(fw, n.predictedValue);
//			}
		
		
	}


	private void writeModelPerformance(PredictionReport or, Writer fw) throws IOException {
		fw.write("<table border=0 width=100%>\n");
		
		fw.write("\t<tr bgcolor=\"black\">\n");//Header bar row
		fw.write("\t\t<td><font color=\"white\">Model Performance</color></td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr><td>\n");
		addPlotsTable(or, fw);//from parent 
		fw.write("\t</td></tr>\n");

		fw.write("\t<tr><td>\n");
		writeStatsTable(or,fw);//local
		fw.write("\t</td></tr>\n");
		
		fw.write("\t<tr><td><br>\n");//blank row
		fw.write("\t</td></tr>\n");
		
		fw.write("\t<tr><td>\n");
		writeQmrfLink(or, fw);//parent
		fw.write("\t</td></tr>\n");
		
//		fw.write("\t</td>\n");

		fw.write("</table>");
	}


	@Override
	protected void writeStatsTable(PredictionReport or, Writer fw) throws IOException {

		if (or.modelDetails.performance.train.R2 != null) {
			fw.write(
					"<table width=40% border=1 cellpadding=1 cellspacing=0><caption>Consensus model statistics</caption>\n");

			fw.write("<tr>\n");
//			fw.write("<td colspan=2 align=center>5-fold CV (75%)</td>\n");
			fw.write("<td colspan=4 align=center>Training (80%)</td>\n");
			fw.write("<td colspan=4 align=center>Test (20%)</td>\n");
			fw.write("</tr>\n");

			fw.write("<tr>\n");
			
//			writeCenteredTD(fw, "Q<sup>2</sup>");
//			writeCenteredTD(fw, "RMSE");

			for (int i = 1; i <= 2; i++) {
				writeCenteredTD(fw, "R<sup>2</sup>");
				writeCenteredTD(fw, "RMSE");
				writeCenteredTD(fw, "MAE");
				writeCenteredTD(fw, "Coverage");
			}
			
			fw.write("</tr>\n");

			fw.write("<tr>\n");

//			writeCenteredTD(fw, or.modelDetails.performance.fiveFoldICV.Q2);
//			writeCenteredTD(fw, or.modelDetails.performance.fiveFoldICV.RMSE);

			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.train.R2,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.train.RMSE,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.train.MAE,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.train.COVERAGE,"",2));

			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.external.R2,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.external.RMSE,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.external.MAE,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.external.COVERAGE,"",2));

			fw.write("</tr>\n");

			fw.write("</table>\n");

		} else if (or.modelDetails.performance.train.BA != null) {
			fw.write("<table width=40% border=1 cellpadding=1 cellspacing=0><caption>Consensus model statistics</caption>\n");

			fw.write("<tr>\n");
//			fw.write("<td colspan=3 align=center>5-fold CV (75%)</td>\n");
			fw.write("<td colspan=4 align=center>Training (80%)</td>\n");
			fw.write("<td colspan=4 align=center>Test (20%)</td>\n");
			fw.write("</tr>\n");

			fw.write("<tr>\n");

			for (int i = 1; i <= 2; i++) {
				writeCenteredTD(fw, "Balanced Accuracy");
				writeCenteredTD(fw, "Sensitivity");
				writeCenteredTD(fw, "Specificity");
				writeCenteredTD(fw, "Coverage");
			}

			fw.write("</tr>\n");

			fw.write("<tr>\n");

//			writeCenteredTD(fw, or.modelDetails.performance.fiveFoldICV.BA);
//			writeCenteredTD(fw, or.modelDetails.performance.fiveFoldICV.SN);
//			writeCenteredTD(fw, or.modelDetails.performance.fiveFoldICV.SP);

			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.train.BA,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.train.SN,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.train.SP,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.train.COVERAGE,"",2));

			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.external.BA,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.external.SN,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.external.SP,"",2));
			writeCenteredTD(fw, getFormattedValue(false, or.modelDetails.performance.external.COVERAGE,"",2));
			fw.write("</tr>\n");

			fw.write("</table>\n");

		} else {
			fw.write("Statistics are unavailable for this endpoint");
			// Do nothing, no stats available
		}

		fw.write("<br>\n");

	}

	@Deprecated
	public static void displayHTMLReport(PredictionResults predictionResults, String fileName, String folder) {
		String htmlReport = RunFromSmiles.getReportAsHTMLString(predictionResults);
		// System.out.println(htmlReport);
		try {
			File file = new File(folder + fileName);
			FileWriter fw = new FileWriter(file);
			fw.write(htmlReport);
			fw.flush();
			fw.close();
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(file.toURI());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {

		HTMLReportCreatorTEST h = new HTMLReportCreatorTEST();

		String dtxsid="DTXSID7020182";
//		String dtxsid="DTXSID101256899";
		
		String folder = "data\\TEST5.1.3\\reports\\" + dtxsid + "\\";

		for(File file:new File(folder).listFiles()) {
			
			if(!file.getName().contains(".json")) continue;
			if(file.getName().contains("TEST_PredictionResults")) continue;
			
			try {
				TEST_Report tr = Utilities.gson.fromJson(new FileReader(file), TEST_Report.class);

				tr.modelDetails.loadPlotsFromDB=true;//load from postgres for testing
				
				String htmlPath=h.toHTMLFile(tr, folder);
				viewInWebBrowser(htmlPath);
//				if(true)break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}



