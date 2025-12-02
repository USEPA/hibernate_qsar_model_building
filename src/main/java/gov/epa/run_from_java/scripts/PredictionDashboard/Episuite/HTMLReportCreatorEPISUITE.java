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
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles.ReportCreator;
import ToxPredictor.Application.model.ExternalPredChart;
import ToxPredictor.Application.model.PredictionResults;
import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.run_from_java.scripts.QsarModelsScript;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.HTMLReportCreator;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.Factor;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.Neighbor;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.NeighborResults;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport.PredictionIndividualMethod;
//import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Factor;
//import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.Model;

/**
 * @author TMARTI02
 */
public class HTMLReportCreatorEPISUITE extends HTMLReportCreator {

	
	//TODO add set=T or P, add training/prediction plots + stats, applicability domain in model results
	
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
			
//			fw.write("\t<tr><td>\n");
//			writeIndividualModelsSummary(or, fw);
//			fw.write("\t</td></tr>\n");

			fw.write("\t<tr><td>\n");
			writeIndividualModelsFactors(or, fw);
			fw.write("\t</td></tr>\n");
			

//			fw.write("\t<tr><td>\n");
//			writeModelPerformance(or, fw);
//			fw.write("\t</td></tr>\n");

			fw.write("</table></html>\r\n");
			fw.flush();
			
			return toPrettyHtml(fw);
			

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	
	private void writeIndividualModelsFactors (PredictionReport er,Writer fw) throws IOException {
	
		if(er.modelResults.consensusPredictions==null) return;
		
		for(PredictionIndividualMethod pim:er.modelResults.consensusPredictions.predictionsIndividualMethod) {

			
			if(pim.factors==null) continue;
			
			
			boolean hasTotalCoefficient=false;
			boolean hasTrainingCount=false;
			boolean hasValidationCount=false;

			

			for(Factor factor:pim.factors) {
				if(factor.totalCoefficient!=null) hasTotalCoefficient=true;
				if(factor.trainingCount!=null) hasTrainingCount=true;
				if(factor.validationCount!=null) hasValidationCount=true;
			}
			
			int colCount=4;
			if(hasTotalCoefficient)colCount++;
			if(hasTrainingCount)colCount++;
			if(hasValidationCount)colCount++;
			
			
//			System.out.println(hasTotalCoefficient);

//			fw.write("<table border=1 cellpadding=0 cellspacing=0 width=67%>\n");
			
			fw.write("<table border=1 cellpadding=5 cellspacing=0>\n");
			fw.write("<caption>"+pim.method+"</caption>\n");

			fw.write("\t<tr bgcolor=\"lightgray\">\n");

			fw.write("\t<th>Type</th>\n");
			fw.write("\t<th>Description</th>\n");

			fw.write("\t<th>Number</th>\n");
			if(hasTrainingCount)  fw.write("\t<th>Training Count</th>\n");
			if(hasValidationCount) fw.write("\t<th>Validation Count</th>\n");
			
			
			fw.write("\t<th>Coefficient</th>\n");
			if(hasTotalCoefficient) fw.write("\t<th>Contribution</th>\n");

//			fw.write("\t<th>Max Fragment Count</th>\n");
			
			fw.write("\t</tr>\n");

			for(Factor factor:pim.factors) {

				if(factor.trainingCount!=null && factor.fragmentCount>factor.trainingCount) {
					fw.write("<tr bgcolor=pink>\n");
				} else {
					fw.write("\t<tr>\n");	
				}
				
				fw.write("\t<td>"+factor.type+"</td>\n");
				fw.write("\t<td>"+factor.description+"</td>\n");

				writeCenteredTD(fw, factor.fragmentCount+"");
				if(hasTrainingCount) {
					if(factor.trainingCount==null) writeCenteredTD(fw, "N/A");
					else writeCenteredTD(fw, factor.trainingCount+"");						
				}

				if(hasValidationCount) {
					if(factor.validationCount==null) writeCenteredTD(fw, "N/A");
					else writeCenteredTD(fw, factor.validationCount+"");	
				}
				
				writeCenteredTD(fw, getFormattedValue(false, factor.coefficient,"prop"));
				if(hasTotalCoefficient) writeCenteredTD(fw, getFormattedValue(false, factor.totalCoefficient,"prop"));

//				if(factor.totalCoefficient==null) {
//					writeCenteredTD(fw, getFormattedValue(false, factor.value,"prop"));
//				} else {
//					writeCenteredTD(fw, getFormattedValue(false, factor.totalCoefficient,"prop"));	
//				}
				fw.write("\t</tr>\n");
			}
			
			
			if(pim.uncorrectedValue!=null) {
				fw.write("\t</tr>\n");
				fw.write("<td align=left colspan="+(colCount-1)+">"+"<b>Uncorrected Value</b>"+"</td>\n");
				fw.write("<td align=center>"+getFormattedValue(false, pim.uncorrectedValue,"prop")+" "+pim.unitsFactor+"</td>\n");
				fw.write("\t</tr>\n");
				
				fw.write("\t</tr>\n");
				fw.write("<td align=left colspan="+(colCount-1)+">"+"<b>Corrected Value</b>"+"</td>\n");
				fw.write("<td align=center>"+getFormattedValue(false, pim.correctedValue,"prop")+" "+pim.unitsFactor+"</td>\n");
				fw.write("\t</tr>\n");

			} else {
				fw.write("\t</tr>\n");
				fw.write("<td align=left colspan="+(colCount-1)+">"+"<b>"+er.modelDetails.propertyName+"</b>"+"</td>\n");
				fw.write("<td align=center>"+getFormattedValue(false, pim.predictedValue,"prop")+" "+pim.unitsFactor+"</td>\n");
				fw.write("\t</tr>\n");
			}
			

//			fw.write("\t<td><b>Model Estimate</b></td>\n");
//			fw.write("\t<td><br></td>\n");
//			fw.write("\t<td><br></td>\n");
//			fw.write("\t<td><br></td>\n");
//			fw.write("\t<td><br></td>\n");
////			writeCenteredTD(fw,getFormattedValue(false, pim.podel.value,er.modelDetails.propertyName));
//			fw.write("\t</tr>\n");

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
		String htmlReport = ReportCreator.getReportAsHTMLString(predictionResults);
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
//		String dtxsid="DTXSID3039242";//benzene
		String dtxsid="DTXSID7024372";//TNT
		
		String folder = "data\\episuite\\reports\\" + dtxsid + "\\";

//		String filepath = folder + dtxsid + "_96 hour fathead minnow LC50.json";
		
		for(File file:new File(folder).listFiles()) {
			
			if(!file.getName().contains(".json")) continue;
			if(file.getName().contains("episuiteReport")) continue;
			
//			if(!file.getName().contains("BP")) continue;
			
			
			System.out.println(file.getName());
			
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



