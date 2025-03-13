package gov.epa.run_from_java.scripts.PredictionDashboard.Percepta;

import java.io.StringWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import gov.epa.run_from_java.scripts.PredictionDashboard.HTMLReportCreator;
import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionReport;

/**
* @author TMARTI02
*/
public class HTMLReportCreatorPercepta extends HTMLReportCreator {
	

	public String createReport(PredictionReport or) {
		
		try {
			
			if (or.modelResults.standardUnit!=null && or.modelResults.standardUnit.equals("Binary")) or.modelResults.standardUnit="";
			if (or.modelResults.standardUnit==null) or.modelResults.standardUnit="";
			
			StringWriter fw=new StringWriter();

			writeHtmlHead(or, fw);
			
			fw.write("<title>Percepta results for " + or.chemicalIdentifiers.dtxcid + " for " + or.modelDetails.modelName+" model");
			fw.write("</title>\n");
			fw.write("</head>\n");
			
			fw.write("<h3>Percepta Model Calculation Details: "+or.modelDetails.propertyName+"</h3>\r\n");
						
			fw.write("<table border=0 width=100%>\n");
			fw.write("\t<tr><td>\n");
			writeFirstRow(or, fw);
			fw.write("\t</td></tr>\n");
			fw.write("</table></html>\r\n");
			fw.flush();
			
//            return fw.toString();
			return toPrettyHtml(fw);			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	

}
