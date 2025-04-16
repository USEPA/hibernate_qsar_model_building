package gov.epa.run_from_java.scripts.PredictionDashboard.Episuite;

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
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Factor;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Model;

/**
 * @author TMARTI02
 */
public class HTMLReportCreatorEPISUITE extends HTMLReportCreator {

	@Override
	public String createReport(PredictionReport or) {

		try {

			if (or.modelResults.standardUnit != null && or.modelResults.standardUnit.equals("Binary"))
				or.modelResults.standardUnit = "";
			if (or.modelResults.standardUnit == null)
				or.modelResults.standardUnit = "";

			StringWriter fw = new StringWriter();

			fw.write("<html>\n");
			fw.write("<head>\n");
			this.writeStyles(fw);

			fw.write("<title>EPI Suite results for " + or.chemicalIdentifiers.dtxcid + " for " + or.modelDetails.modelName
					+ " model");
			fw.write("</title>\n");
			fw.write("</head>\n");

			fw.write("<h3>EPI Suite Model Calculation Details: " + or.modelDetails.propertyName + "</h3>\r\n");

			fw.write("<table border=0 width=100%>\n");

			fw.write("\t<tr><td>\n");
			writeFirstRow(or, fw);
			fw.write("\t</td></tr>\n");
			
//			fw.write("\t<tr><td>\n");
//			writeIndividualModels(or, fw);
//			fw.write("\t</td></tr>\n");
			
			fw.write("\t<tr><td>\n");
			writeIndividualModelsSummary(or, fw);
//			fw.write("\t</td></tr>\n");

//			fw.write("\t<tr><td>\n");
			writeIndividualModelsFactors(or, fw);
			fw.write("\t</td></tr>\n");
			

//			fw.write("\t<tr><td>\n");
//			writeModelPerformance(or, fw);
//			fw.write("\t</td></tr>\n");

			fw.write("</table></html>\r\n");
			fw.flush();

			return fw.toString();

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
	private void writeIndividualModelsFactors (PredictionReport er,Writer fw) throws IOException {
	
		if(er.modelDetails.individualModels==null) return;
		
		for(Model model:er.modelDetails.individualModels) {

			if(model.factors==null) continue;
			boolean hasTotalCoefficient=false;


			for(Factor factor:model.factors) {
				if(factor.totalCoefficient!=null) {
					hasTotalCoefficient=true;
					break;
				}
			}


			if(hasTotalCoefficient) {
				fw.write("<table border=1 cellpadding=0 cellspacing=0 width=67%>\n");
				fw.write("<caption>"+model.name+"</caption>\n");

				fw.write("\t<tr bgcolor=\"lightgray\">\n");

				fw.write("\t<th>Type</th>\n");
				fw.write("\t<th>Description</th>\n");
				fw.write("\t<th>Fragment Count</th>\n");
				fw.write("\t<th>Training Count</th>\n");
				fw.write("\t<th>Max Fragment Count</th>\n");
				fw.write("\t<th>Total Coefficient</th>\n");
				fw.write("\t</tr>\n");

				for(Factor factor:model.factors) {

					fw.write("\t<td>"+factor.type+"</td>\n");
					fw.write("\t<td>"+factor.description+"</td>\n");
					writeCenteredTD(fw, factor.fragmentCount+"");
					writeCenteredTD(fw, factor.trainingCount+"");
					writeCenteredTD(fw, factor.maxFragmentCount+"");
					if(factor.totalCoefficient==null) {
						writeCenteredTD(fw, getFormattedValue(false, factor.value,"prop"));
					} else {
						writeCenteredTD(fw, getFormattedValue(false, factor.totalCoefficient,"prop"));	
					}
					fw.write("\t</tr>\n");
				}
			}
			
			

			fw.write("\t<td><b>Model Estimate</b></td>\n");
			fw.write("\t<td><br></td>\n");
			fw.write("\t<td><br></td>\n");
			fw.write("\t<td><br></td>\n");
			fw.write("\t<td><br></td>\n");
			writeCenteredTD(fw,getFormattedValue(false, model.value,er.modelDetails.propertyName));
			fw.write("\t</tr>\n");

			//				fw.write("\t<td><b>"+er.modelDetails.propertyName+"</b></td>\n");
			//				fw.write("\t<td><br></td>\n");
			//				fw.write("\t<td><br></td>\n");
			//				fw.write("\t<td><br></td>\n");
			//				fw.write("\t<td><br></td>\n");
			//				
			//				if(er.modelDetails.propertyName.toLowerCase().equals(DevQsarConstants.HENRYS_LAW_CONSTANT.toLowerCase())) {
			//					
			//					fw.write("\t<td>");	
			//
			////					fw.write(getFormattedValue(model.hlcUnitless,DevQsarConstants.HENRYS_LAW_CONSTANT)+" Unitless<br>");
			//					fw.write(getFormattedValue(model.hlcAtm,DevQsarConstants.HENRYS_LAW_CONSTANT)+" "+DevQsarConstants.ATM_M3_MOL+"<br>");
			////					fw.write(getFormattedValue(model.hlcPaMol,DevQsarConstants.HENRYS_LAW_CONSTANT)+" "+DevQsarConstants.PA_M3_MOL+"<br>");
			//
			//					
			//					fw.write("\t</td>");
			//				}
			//				fw.write("\t</tr>\n");





			fw.write("</table><p>\n");


		}
		
	}
	
	

	

	private void addPlot(int pixels, Long modelId, Long fileType, Writer fw, String altText, QsarModelsScript qms)
			throws IOException {
		byte[]fileBytes=qms.downloadModelFile(modelId, fileType);
		String base64 = Base64.getEncoder().encodeToString(fileBytes);
		String imgSrc="data:image/png;base64, "+base64;
		
		fw.write("<td width=30%><img src=\"" + imgSrc
				+ "\" alt=\""+altText+"\" height="+pixels+" width="+pixels+"></td>");
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

		HTMLReportCreatorEPISUITE h = new HTMLReportCreatorEPISUITE();

//		String dtxsid="DTXSID7020182";
		String dtxsid="DTXSID3039242";//benzene
		String folder = "data\\episuite\\reports\\" + dtxsid + "\\";

//		String filepath = folder + dtxsid + "_96 hour fathead minnow LC50.json";
		
		for(File file:new File(folder).listFiles()) {
			
			if(!file.getName().contains(".json")) continue;
			if(file.getName().contains("episuiteReport")) continue;
			
			try {
				EpisuiteReport tr = Utilities.gson.fromJson(new FileReader(file), EpisuiteReport.class);
//				tr.modelDetails.loadPlotsFromDB=true;//load from postgres for testing
				h.toHTMLFile(tr, folder);
				
//				if(true)return;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}



