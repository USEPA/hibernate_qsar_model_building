package gov.epa.endpoints.reports.WebTEST;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


import gov.epa.endpoints.reports.WebTEST.ReportClasses.CancerStats;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.ClusterModel;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.ClusterTable;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.Descriptor;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.ExternalPredChart;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.IndividualPredictionsForConsensus;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.PredictionResults;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.PredictionResultsPrimaryTable;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.SimilarChemical;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.SimilarChemicals;
import gov.epa.endpoints.reports.WebTEST.ReportClasses.IndividualPredictionsForConsensus.PredictionIndividualMethod;

public class PredictToxicityWebPageCreatorFromJSON {

	
	private static final Logger logger = LogManager.getLogger(PredictToxicityWebPageCreatorFromJSON.class);
	
	
	private void writeHeaderInfo(FileWriter fw, String CAS, String endpoint, String method) throws IOException {

		fw.write("<html>\n");
		fw.write("<head>\n");

		// fw.write("<title>Prediction results from the "+method+" method\n");
		fw.write("<title>Predicted " + endpoint + " for " + CAS + " from " + method + " method");
		fw.write("</title>\n");
		fw.write("</head>\n");

	}
	
	
	private void writeBinaryPredictionTable(PredictionResults pr,FileWriter fw) throws Exception {
		PredictionResultsPrimaryTable prpt=pr.getPredictionResultsPrimaryTable();
		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		fw.write("<caption>Prediction results</caption>\r\n");

		// ************************************************
		// Header row
		fw.write("<tr bgcolor=\"#D3D3D3\">\n");

		fw.write("<th>Endpoint</th>\n");

		if (prpt.getExpToxValue().equals("N/A")) {
			fw.write("<th>Experimental value</th>\n");
		} else {
			fw.write("<th align=left>Experimental value (CAS= " + prpt.getExpCAS() + ")");
//			fw.write(prpt.getSource());
			fw.write("</th>\n");
		}
		
		fw.write("<th>Predicted value");
		if (prpt.getPredictedValueSuperscript()!=null) {
			fw.write("<sup>"+prpt.getPredictedValueSuperscript()+"</sup>");
		}
		fw.write("</th>\n");
		
		fw.write("</tr>\n");

		// ************************************************************
		// Value row
		fw.write("<tr>\n");
		fw.write("<td>" + pr.getEndpoint() + " value</td>\n");
		this.writeCenteredTD(fw,prpt.getExpToxValue());
		this.writeCenteredTD(fw,prpt.getPredToxValue());
		fw.write("</tr>\n");

		// ************************************************************
		// result row:
		fw.write("<tr>\n");
		fw.write("<td>" + pr.getEndpoint() + " result</td>\n");
		this.writeCenteredTD(fw, prpt.getExpToxValueEndpoint());
		this.writeCenteredTD(fw, prpt.getPredValueEndpoint());
		fw.write("</tr>\n");

		// ************************************************************

		fw.write("</table>\n");
		

		if (prpt.getPredictedValueNote()!=null)
			fw.write("<sup>a</sup><font color=blue>"+prpt.getPredictedValueNote()+"</font><br>\r\n");

		if (prpt.getMessage()!=null) {
			fw.write("<sup>b</sup><font color=darkred>" + prpt.getMessage() + "</font>\r\n");
		}

	}
	
	
	
	private void writeHierarchicalClusteringResultsWebpages(PredictionResults pr, String htmlOutputFilePath) {
		try {
			
			File of=new File(htmlOutputFilePath);
			
			String ToxRunsFolderPath=of.getParentFile().getParentFile().getParentFile().getAbsolutePath();
			String outputFolderPath=of.getParentFile().getAbsolutePath();
			
			//filewriter for web page:
			FileWriter fw=new FileWriter(htmlOutputFilePath);

			//*******************************************************************
			String units="";
			if (!pr.isBinaryEndpoint()) {
				if (pr.isLogMolarEndpoint()) units=pr.getPredictionResultsPrimaryTable().getMolarLogUnits();
				else units=pr.getPredictionResultsPrimaryTable().getMassUnits();
			}
			//*******************************************************************

			
			writeHeaderInfo(fw, pr.getCAS(), pr.getEndpoint(), pr.getMethod());

			fw.write("<h2>Predicted " + pr.getEndpoint() + " for <font color=\"blue\">" + pr.getCAS() + "</font> from " + pr.getMethod() + " method</h2>\n");

			if (pr.isBinaryEndpoint()) {
				this.writeBinaryPredictionTable(pr,fw);
			} else {
				this.writeMainTable(pr,fw);
			}
			
			fw.write("<BR><BR>\r\n");

			fw.write("\r\n<table border=1 cellpadding=10 cellspacing=0>\r\n");
			fw.write("<tr>\r\n");

			fw.write("<td>\r\n");
			writeClusterModelTables(fw,pr,ToxRunsFolderPath,outputFolderPath);
			fw.write("</td>\r\n");

			fw.write("<td><a href=\""+pr.getImageURL()+"\">"+
					"<img src=\"" +pr.getImageURL() 
					+ "\" width=" + pr.getImgSize()+ " border=0></a></td>\n");
			
			fw.write("</tr>\r\n");
			fw.write("</table>\r\n\r\n");

			
			if (pr.isCreateDetailedReport()) {
				fw.write("<p><a href=\"../StructureData/DescriptorData_"+pr.getCAS()+".html\">Descriptor values for " + "test chemical</a></p>\n");
			}
			fw.write("<br><hr>\n");
			
			
			for (int i=0;i<pr.getSimilarChemicals().size();i++) {
				this.writeSimilarChemicals(pr, pr.getSimilarChemicals().get(i), fw, units);
				if (i<pr.getSimilarChemicals().size()-1) fw.write("<br><hr>\n");
			}
			
			fw.flush();
			fw.write("</html>");
			fw.close();
			
			
		} catch (Exception ex) {
			logger.catching(ex);
		}
		
	}


	private void writeClusterModelTables(FileWriter fw, PredictionResults pr,String ToxRunsFolderPath,String outputFolderPath) throws Exception {


		if (pr.getClusterTable()!=null) {
			writeClusterTable(fw, pr, pr.getClusterTable(),ToxRunsFolderPath,outputFolderPath);
			fw.write("<br><br>");
		} else {
			fw.write("No statistically valid models were selected by the "
					+ "hierarchical clustering algorithm for this compound<br><br>");
		}
		
		if (pr.getInvalidClusterTable()!=null) {
			writeClusterTable(fw, pr, pr.getInvalidClusterTable(),ToxRunsFolderPath,outputFolderPath);
		}
		
	}


	private void writeClusterTable(FileWriter fw, PredictionResults pr, ClusterTable ct,String ToxRunsFolderPath,String outputFolderPath) throws Exception {
		
		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>"+ct.getCaption()+"</caption>\r\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Cluster model</th>\n");
		if (pr.isCreateDetailedReport()) fw.write("<th>Test chemical <br>descriptor values</th>\n");

		if (pr.isBinaryEndpoint()) {
			fw.write(createTH("Predicted value<br>" + ct.getUnits()));
			fw.write(createTH("Concordance"));
			fw.write(createTH("Sensitivity"));
			fw.write(createTH("Specificity"));
		} else {
			fw.write(createTH("Prediction interval<br>" + ct.getUnits()));
			fw.write(createTH("r<sup>2</sup>"));
			fw.write(createTH("q<sup>2</sup>"));
		}

		fw.write("<th>#chemicals</th>\n");
		fw.write("<th>Applicability Domain</th>\n");

		fw.write("</tr>\n");

		for (ClusterModel clusterModel:ct.getClusterModels()) {
			fw.write("<tr>\n");
			
			if (pr.isCreateDetailedReport()) {
				fw.write(createTD("center",clusterModel.getUrl(),clusterModel.getClusterID()));				
				fw.write(createTD("center",clusterModel.getUrlDescriptors(),"Descriptors"));
				writeModelPage(pr,clusterModel);
				writeTestChemicalDescriptorsForClusterModel(pr,clusterModel);
			} else {
				fw.write(createTD("center",clusterModel.getClusterID()));
			}
			
			if (clusterModel.isOmitted()) fw.write(createTD("center",clusterModel.getMinMaxValue()+"*")+"\n");
			else fw.write(createTD("center",clusterModel.getMinMaxValue()));
						
			if (!pr.isBinaryEndpoint()) {
				fw.write(createTD("center", clusterModel.getR2()));
				fw.write(createTD("center", clusterModel.getQ2()));//TODO- what happens if q2 =-1?
			} else {
				fw.write(createTD("center", clusterModel.getConcordance()));
				fw.write(createTD("center", clusterModel.getSensitivity()));
				fw.write(createTD("center", clusterModel.getSpecificity()));
			}
			
			writeCenteredTD(fw,clusterModel.getNumChemicals()+"");
			writeCenteredTD(fw, clusterModel.getMessageAD());
			
			fw.write("</tr>\n");
		}

		fw.write("</table>\n");
		
		if (ct.getMessage()!=null) {
			fw.write("*Value omitted from calculation of toxicity since prediction uncertainty was zero\n");
		}
	}

	private void writeModelPage(PredictionResults pr,ClusterModel clusterModel) {
//		System.out.println(pr.get);
		
		try {
			
//			System.out.println(clusterModel.getUrl());
			
			File fileOutput=new File(pr.getReportBase()+File.separator+clusterModel.getUrl());		
			fileOutput.getParentFile().mkdirs();

			FileWriter fw=new FileWriter(fileOutput);
			fw.write("<html>\n");
			
			fw.write("<head><title>Model # "+clusterModel.getClusterID()+"</title></head>\n");
			
			fw.write("<h3>Model # "+clusterModel.getClusterID()+"</h3>\n");
			
			fw.write("<table>\n");
			fw.write("<tr>\n");
			
			fw.write("<td>\n");
						
			writeModelStats(clusterModel, pr.getEndpoint(), fw);
			fw.write("</td>\n");
			
			if (!clusterModel.isBinary()) {
				fw.write("<td><img src=\""+clusterModel.getPlotImage()+"\">\n");
				fw.write("</td>\n");
			}
			
			fw.write("</tr>\n");
			
			fw.write("</table>\n");
			
			//TODO add binary stats
			
			writeModelTable(clusterModel, fw);
			
			fw.write("<br><br><i>Model equation:<br></i>\n");
			fw.write(clusterModel.getModelEquation()+"\n");
			
			fw.write("</html>\n");
			fw.flush();
			fw.close();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	private void writeTestChemicalDescriptorsForClusterModel(PredictionResults pr,ClusterModel clusterModel) {

		try {

			File fileOutput=new File(pr.getReportBase()+File.separator+clusterModel.getUrlDescriptors());		
			fileOutput.getParentFile().mkdirs();

			FileWriter fw=new FileWriter(fileOutput);

			
			String title="";
			
			if (clusterModel.getClusterID().toLowerCase().contains("group")) {
				title="Descriptors for "+pr.getCAS()+" for group contribution model";
			} else {
				title="Descriptors for "+pr.getCAS()+" for cluster model #"+ clusterModel.getClusterID();
			}
			
			fw.write("<html>\n");
			fw.write("<head><title>"+title+"</title></head>\n");
			fw.write("<h3>"+title+"</h3>\r\n");

			fw.write("<table>\r\n");
			fw.write("<tr>\r\n");

			fw.write("<td>\r\n");
			writeDescriptorTable(fw, clusterModel);
			fw.write("</td>\r\n");

			fw.write("<td>\r\n");
			fw.write("<img src=\"../../StructureData/structure.png\">" + "\r\n");
			fw.write("</td>\r\n");

			fw.write("</tr>\r\n");
			fw.write("</table>\r\n");

			fw.write("</html>\n");
			fw.flush();

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}
	
	private static void writeDescriptorTable(FileWriter fw, ClusterModel clusterModel) {
		try {

			DecimalFormat df2 = new DecimalFormat("0.00");
			DecimalFormat df4 = new DecimalFormat("0.0000");

			fw.write("<table>\r\n"); // table to store descriptor table and
										// structure image
			fw.write("<tr>\r\n");

			fw.write("<td>\r\n");

			fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
			fw.write("<caption>Descriptor Values</caption>\n");

			fw.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw.write("<th>Descriptor</th>\n");
			fw.write("<th>Value</th>\n");
			fw.write("<th>Coefficient</th>\n");
			fw.write("<th>Value &#215; Coefficient</th>\n");
			fw.write("</tr>\n");


			Vector<Descriptor>descriptors=clusterModel.getDescriptors();
			
			for (int i = 0; i < descriptors.size(); i++) {
				Descriptor descriptor=descriptors.get(i);
				double descval = Double.parseDouble(descriptor.getValue());

				if (descval != 0) {
					fw.write("<tr style=\"background-color: lightgray\">\n");
					// if
					// (method.equals(TESTConstants.ChoiceGroupContributionMethod)
					// && chemical.value(or.getDescriptors()[i])>0) {
					// fw.write("<tr style=\"background-color: lightgray\">\n");
				} else {
					fw.write("<tr>\n");
				}

				fw.write("\t<td>" + descriptor.getName() + "</td>\n");

				// fw.write("<td align=\"center\">"
				// + df.format(chemical.value(or.getDescriptors()[i]))
				// + "</td>\n");

				fw.write("\t<td align=\"center\">" + descriptor.getValue() + "</td>\n");
				fw.write("\t<td align=\"center\">" + descriptor.getCoefficient() + "</td>\n");
				fw.write("\t<td align=\"center\">" + descriptor.getValue_x_coefficient() + "</td>\n");
				fw.write("</tr>\n");
			}

			// ************************************************************************************************
			// Predicted value:
			
			fw.write("<tr>\n");
			fw.write("\t<td><font color=\"blue\">"+clusterModel.getPredictedValueLabel() + "</font></td>\n");
			fw.write("\t<td><br></td>\n");
			fw.write("\t<td><br></td>\n");
			fw.write("\t<td align=\"center\"><font color=\"blue\">" + clusterModel.getPredictedValue() + "</font></td>\n");
			fw.write("</tr>\n");
			// ************************************************************************************************

			fw.write("</table>\n");
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	private void writeModelTable(ClusterModel clusterModel, FileWriter fw) throws IOException {
		fw.write("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<tr>\n");

		fw.write("<td>\n");
		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>Model coefficients</caption>\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Coefficient</th>\n");
		fw.write("<th>Definition</th>\n");
		fw.write("<th>Value</th>\n");
		fw.write("<th>Uncertainty*</th>\n");

		fw.write("</tr>\n");

		Vector<Descriptor> descriptors=clusterModel.getDescriptors();
		
		
		for (int i = 0; i < descriptors.size(); i++) {

			Descriptor d=descriptors.get(i);
			fw.write("<tr>\n");
			fw.write("<td>" + d.getName() + "</td>\n");
			fw.write("<td>" + d.getDefinition() + "</td>\n");
			fw.write("<td>" + d.getCoefficient() + "</td>\n");
			fw.write("<td>" + d.getCoefficientUncertainty() + "</td>\n");
			fw.write("</tr>\n");
		}

		fw.write("</table>\n");// close table for regression model details
		fw.write("* value for 90% confidence interval");

		
	}

	private void writeModelStats(ClusterModel clusterModel, String endpoint, FileWriter fw) throws IOException {
		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Parameter</th>\n");
		fw.write("<th>Value</th>\n");
		fw.write("</tr>\n");

		createRow("Endpoint",endpoint.replace("50", "<sub>50</sub>"),fw);

		if (!clusterModel.isBinary()) {
			createRow("r<sup>2</sup>",clusterModel.getR2(),fw);
			createRow("q<sup>2</sup>",clusterModel.getQ2(),fw);
		} else {
			createRow("Concordance",clusterModel.getConcordance(),fw);
			createRow("Sensitivity",clusterModel.getSensitivity(),fw);
			createRow("Specificity",clusterModel.getSpecificity(),fw);
		}

		// *********************************************************************************
		createRow("Number of chemicals",clusterModel.getNumChemicals()+"",fw);
		createRow("Model",clusterModel.getClusterID(),fw);


		fw.write("</table>\n");
	}


	private void createRow(String name,String value,FileWriter fw) throws IOException {
		fw.write("<tr>\n");
		fw.write("<td>"+name+"</td>\n");
		fw.write("<td align=\"center\">" + value + "</td>\n");
		fw.write("</tr>\n\n");
	}
	
	private String createTD(String align,String url,String text) {
		String str="<td align=\""+align+"\""+">";
		str+="<a href=\""+url+"\">"+text+"</a>";
		str+="</td>\n";
		return str;
	}
	
	private String createTD(String align,String text) {
		String str="<td align=\""+align+"\">";
		str+=text+"</td>\n";
		return str;
	}

	private String createTH(String text) {
		String str="<th>"+text+"</th>\n";
		return str;
	}
	
	private void writeCenteredTD(FileWriter fw,String value) throws Exception {
		writeTD(fw,"center",value);
	}
	
	private void writeTD(FileWriter fw,String align,String text) throws Exception {
		String str=createTD(align, text);
		fw.write(str);
	}
	
	private void writeTD(FileWriter fw,String text) throws Exception {
		fw.write("<td>"+text+"</td>\n");		
	}



	public void writeConsensusResultsWebPages(PredictionResults pr,String htmlOutputFilePath) {

		
		try {
			Path pathToFile = Paths.get(htmlOutputFilePath);
			Files.createDirectories(pathToFile.getParent());
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		
		
		try {
		//filewriter for web page:
		FileWriter fw=new FileWriter(htmlOutputFilePath);

		//*******************************************************************
		String units="";
		if (!pr.isBinaryEndpoint()) {
			if (pr.isLogMolarEndpoint()) units=pr.getPredictionResultsPrimaryTable().getMolarLogUnits();
			else units=pr.getPredictionResultsPrimaryTable().getMassUnits();
		}
		//*******************************************************************

		
		this.writeHeaderInfo(fw, pr.getCAS(), pr.getEndpoint(), pr.getMethod());

		fw.write("<h2>Predicted " + pr.getEndpoint() + " for <font color=\"blue\">" + pr.getCAS() + "</font> from " + pr.getMethod() + " method</h2>\n");

		
		
		if (pr.isBinaryEndpoint()) {
			this.writeBinaryPredictionTable(pr,fw);
		} else {
			this.writeMainTable(pr,fw);
		}
		
		fw.write("<BR><BR>\r\n");

		fw.write("\r\n<table border=1 cellpadding=10 cellspacing=0>\r\n");
		fw.write("<tr>\r\n");

		fw.write("<td>\r\n");
		this.writeIndividualPredictionsForConsensus(pr, fw,units);
		fw.write("</td>\r\n");

		fw.write("<td><a href=\""+pr.getImageURL()+"\">"+
				"<img src=\"" +pr.getImageURL() 
				+ "\" width=" + pr.getImgSize()+ " border=0></a></td>\n");
		
		fw.write("</tr>\r\n");
		fw.write("</table>\r\n\r\n");

		if (pr.isCreateDetailedReport()) {
			
			fw.write("<p><a href=\"../StructureData/DescriptorData_"+pr.getCAS()+".html\">Descriptor values for " + "test chemical</a></p>\n");
//			fw.write("<p><a href=\"../StructureData/descriptordata.html\">Descriptor values for " + "test chemical</a></p>\n");
		}
		fw.write("<br><hr>\n");
		
		
		for (int i=0;i<pr.getSimilarChemicals().size();i++) {
			this.writeSimilarChemicals(pr, pr.getSimilarChemicals().get(i), fw, units);
			if (i<pr.getSimilarChemicals().size()-1) fw.write("<br><hr>\n");
		}
		
		fw.flush();
		fw.write("</html>");
		fw.close();
		
		
	} catch (Exception ex) {
		ex.printStackTrace();
	}
	}

	
	private void writeNearestNeighborResultsWebPages(PredictionResults pr,String htmlOutputFilePath) {
		
		try {
			
			
			//filewriter for web page:
			FileWriter fw=new FileWriter(htmlOutputFilePath);

			//*******************************************************************
			String units="";
			if (!pr.isBinaryEndpoint()) {
				if (pr.isLogMolarEndpoint()) units=pr.getPredictionResultsPrimaryTable().getMolarLogUnits();
				else units=pr.getPredictionResultsPrimaryTable().getMassUnits();
			}
			//*******************************************************************

			
			this.writeHeaderInfo(fw, pr.getCAS(), pr.getEndpoint(), pr.getMethod());

			fw.write("<h2>Predicted " + pr.getEndpoint() + " for <font color=\"blue\">" + pr.getCAS() + "</font> from " + pr.getMethod() + " method</h2>\n");

			
			if (pr.isBinaryEndpoint()) {
				this.writeBinaryPredictionTable(pr,fw);
			} else {
				this.writeMainTable(pr,fw);
			}
			
			fw.write("<BR><BR>\r\n");

			writeNeighborsTable(units, fw,pr);
			

			if (pr.isCreateDetailedReport()) {
				fw.write("<p><a href=\"../StructureData/DescriptorData_"+pr.getCAS()+".html\">Descriptor values for " + "test chemical</a></p>\n");
//				fw.write("<p><a href=\"../StructureData/descriptordata.html\">Descriptor values for " + "test chemical</a></p>\n");
			}
			fw.write("<br><hr>\n");
			
			
			for (int i=0;i<pr.getSimilarChemicals().size();i++) {
				this.writeSimilarChemicals(pr, pr.getSimilarChemicals().get(i), fw, units);
				if (i<pr.getSimilarChemicals().size()-1) fw.write("<br><hr>\n");
			}
			
			fw.flush();
			fw.write("</html>");
			fw.close();
			
			
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	private void writeNeighborsTable(String units,FileWriter fw,PredictionResults pr) throws Exception {
		
		SimilarChemicals simChems=pr.getSimilarChemicals().get(1);
		fw.write("<table border=1 cellpadding=3 cellspacing=0>\n");

		fw.write("<caption>Nearest neighbors from the <font color=\"blue\">training<font> set</caption>\n");
		
		
		fw.write("\n<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>CAS</th>\n");
		fw.write("<th>Structure</th>\n");
		fw.write("<th>Experimental value<br>" + units + "</th>\n");
		fw.write("<th>Similarity<br>Coefficient</th>\n");
		fw.write("</tr>\n\n");
		
		fw.write("<tr>\n");
		// String CAS = chemical.stringValue(chemicalNameIndex);
		fw.write("<td><font color=\"blue\">" + pr.getCAS() + "<br>(test chemical)</font></td>\n");
		fw.write("<td><a href=\""+pr.getImageURL()+"\">"+
				"<img src=\"" +pr.getImageURL() 
				+ "\" width=" + pr.getImgSize()+ " border=0></a></td>\n");

		this.writeCenteredTD(fw, simChems.getExpVal());
		fw.write("<td align=\"center\"><br></td>\n");
		
		
		fw.write("</tr>\n\n");

		int neighborCount=Math.min(3, simChems.getSimilarChemicalsList().size());
		 
		for (int i = 0; i < neighborCount; i++) {

			SimilarChemical simChem = simChems.getSimilarChemicalsList().get(i);

			fw.write("<tr>\n");

			if (simChem.getDSSTOXSID() == null) {
				fw.write("<td>" + simChem.getCAS() + "</td>\n");
			} else {
				fw.write("<td><a href=\"" + pr.getWebPathDetails() + simChem.getDSSTOXSID() + "\" target=\"_blank\">" + simChem.getCAS() + "</td>\n");// TODD
			}

			
			fw.write("<td><a href=\"" + simChem.getImageUrl() + "\"><img src=\"" + simChem.getImageUrl() + "\" width="
					+ pr.getImgSize() + " border=0></a></td>\n");

			fw.write("<td align=\"center\">" + simChem.getExpVal() + "</td>\n");

			fw.write("<td bgcolor=" + "\"" + simChem.getBackgroundColor() + "\"" + " align=\"center\">" + simChem.getSimilarityCoefficient()
					+ "</td>\n");

			
			fw.write("</tr>\n\n");

		}//loop over similar chemicals list
		

		fw.write("</table>\n");
		
		
		
	}
	public void writeConsensusResultsWebPages(String jsonFilePath,String htmlOutputFilePath) {
		try {
			//Read to PredictionResults class:
			Gson gson = new Gson();
			PredictionResults pr = gson.fromJson(new FileReader(jsonFilePath), PredictionResults.class);
			this.writeConsensusResultsWebPages(pr, htmlOutputFilePath);
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}
	
	private void writeSimilarChemicals(PredictionResults pr, SimilarChemicals simChems, FileWriter fw,String units) throws Exception {

		String set = simChems.getSimilarChemicalsSet();

		if (set.equals("test")) {
			fw.write("<h2>Predictions for the test chemical and for the " + "most similar chemicals in the "
					+ "<font color=blue>external test set</font></h2>\n");
		} else {
			fw.write("<h2>Predictions for the test chemical and for the " + "most similar chemicals in the "
					+ "<font color=blue>" + set + " set</font></h2>\n");
		}

		if (simChems.getSimilarChemicalsList().size() == 0) {
			fw.write("<i>Note: No chemicals in the " + set + " set exceed a minimum similarity coefficient of "
					+ pr.getSCmin() + " for comparison purposes</i>\r\n");
			return;
		}

		fw.write("<h3>If the predicted value matches the experimental values for similar chemicals " + "in the " + set
				+ " set (and the similar chemicals were predicted well), "
				+ "one has greater confidence in the predicted value.</h3>\n");
		
		
		if (!pr.isBinaryEndpoint() && simChems.getSimilarChemicalsList().size() > 0) {
			this.writeExternalPredChart(pr, simChems.getExternalPredChart(),units, fw);
		} else if (pr.isBinaryEndpoint()) {
			this.writeCancerStats(simChems.getCancerStats(),fw);
		}
		

		// ***********************************************************
		// write out table of exp and pred values for nearest chemicals:
		fw.write("<table border=1 cellpadding=3 cellspacing=0>\n");
		
		fw.write("<caption>Results for similar chemicals</caption>\n");
		
		fw.write("\n<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>CAS</th>\n");
		fw.write("<th>Structure</th>\n");
		fw.write("<th>Similarity<br>Coefficient</th>\n");


		fw.write("<th>Experimental value<br>" + units + "</th>\n");
		fw.write("<th>Predicted value<br>" + units + "</th>\n");
		fw.write("</tr>\n\n");

		
		fw.write("<tr>\n");
		// String CAS = chemical.stringValue(chemicalNameIndex);
		fw.write("<td><font color=\"blue\">" + pr.getCAS() + "<br>(test chemical)</font></td>\n");
		fw.write("<td><a href=\""+pr.getImageURL()+"\">"+
				"<img src=\"" +pr.getImageURL() 
				+ "\" width=" + pr.getImgSize()+ " border=0></a></td>\n");

		fw.write("<td align=\"center\"><br></td>\n");
		this.writeCenteredTD(fw, simChems.getExpVal());
		this.writeCenteredTD(fw, simChems.getPredVal());
		fw.write("</tr>\n\n");

		for (int i = 0; i < simChems.getSimilarChemicalsList().size(); i++) {

			SimilarChemical simChem = simChems.getSimilarChemicalsList().get(i);

			fw.write("<tr>\n");
			
			if (simChem.getDtxcid() != null) {
				fw.write("<td><a href=\"" +  pr.getWebPathDetails() + simChem.getDtxcid() + "\" target=\"_blank\">" + simChem.getCAS() + "</td>\n");// TODD				
//			} else if (simChem.getDSSTOXSID() == null) {
//				fw.write("<td><a href=\"" + pr.getWebPath2() + simChem.getDSSTOXSID() + "\" target=\"_blank\">" + simChem.getCAS() + "</td>\n");// TODD
			} else {
				fw.write("<td>" + simChem.getCAS() + "</td>\n");
			} 

			
			fw.write("<td><a href=\"" + simChem.getImageUrl() + "\"><img src=\"" + simChem.getImageUrl() + "\" width="
					+ pr.getImgSize() + " border=0></a></td>\n");

			fw.write("<td bgcolor=" + "\"" + simChem.getBackgroundColor() + "\"" + " align=\"center\">" + simChem.getSimilarityCoefficient()
					+ "</td>\n");

			fw.write("<td align=\"center\">" + simChem.getExpVal() + "</td>\n");
			fw.write("<td align=\"center\">" + simChem.getPredVal() + "</td>\n");
			fw.write("</tr>\n\n");

		}//loop over similar chemicals list
		

		fw.write("</table>\n");
		
	}
	
	
	private void writeCancerStats(CancerStats cs,FileWriter fw) throws Exception {

		java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

		fw.write("<table border=1 cellpadding=3 cellspacing=0>\n");
		fw.write("<caption>Prediction statistics for similar chemicals</caption>\r\n");

		fw.write("\n<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Concordance</th>\r\n");
		fw.write("<th>Sensitivity</th>\r\n");
		fw.write("<th>Specificity</th>\r\n");
		fw.write("</tr>\r\n");

		fw.write("<tr>\r\n");

		if (cs.getPredCount() > 0) {
			this.writeCenteredTD(fw, cs.getConcordance() + "<br>(" + cs.getCorrectCount() + " out of " + cs.getPredCount() + ")");
		} else {
			this.writeCenteredTD(fw, "N/A");
		}

		if (cs.getPosPredCount()>0)
			this.writeCenteredTD(fw, cs.getPosConcordance() + "<br>(" + cs.getPosCorrectCount() + " out of " + cs.getPosPredCount() + ")");
		else
			this.writeCenteredTD(fw, "N/A");

		if (cs.getNegPredCount() > 0)
			this.writeCenteredTD(fw, cs.getNegConcordance() + "<br>(" + cs.getNegCorrectCount() + " out of " + cs.getNegPredCount() + ")");
		else
			this.writeCenteredTD(fw, "N/A");

		fw.write("</tr>\r\n");

		fw.write("</table>\r\n");
		this.writeSimilarityLegend(fw);
		fw.write("<br><br>\n");
	}
	
	
	/**
	 * writes a table of the overall results from each of the QSAR models used
	 * in the consensus prediction
	 * 
	 * @param fw
	 */
	private void writeIndividualPredictionsForConsensus(PredictionResults pr, FileWriter fw,String units) throws Exception {

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>Individual Predictions</caption>\r\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Method</th>\n");

		
		fw.write("<th>Predicted value<br>" + units + "</th>\n");
		
		fw.write("</tr>\n");

		IndividualPredictionsForConsensus iprc=pr.getIndividualPredictionsForConsensus();
		Vector<PredictionIndividualMethod>vecPredCons=iprc.getConsensusPredictions();
		
		for (int i = 0; i < vecPredCons.size(); i++) {
			
			PredictionIndividualMethod predConsensus=vecPredCons.get(i);
			
			String method=predConsensus.getMethod();
			String pred=predConsensus.getPrediction();
			
			if (method.equals("Consensus"))	continue;
			
			fw.write("<tr>\n");

			fw.write("<td>" + method + "</td>\n");

			if (pr.isCreateDetailedReport())
				this.writeCenteredTD(fw, "<a href=\"" + predConsensus.getFileName() + "\">"+pred+"</a>");
			else 
				this.writeCenteredTD(fw, pred);

			fw.write("</tr>\n");
		}
		fw.write("</table>\n");
	}
	private void writeMainTable(PredictionResults pr, FileWriter fw) throws Exception {
		PredictionResultsPrimaryTable prpt=pr.getPredictionResultsPrimaryTable();
		
		boolean isLogMolarEndpoint=pr.isLogMolarEndpoint();
		
		String source=prpt.getSource();
		String expCAS=prpt.getExpCAS();
		
		String expToxValue=prpt.getExpToxValue();
		String ExpToxValMass=prpt.getExpToxValMass();
		
		String PredToxValMass=prpt.getPredToxValMass();
		String predToxVal=prpt.getPredToxValue();

		boolean writePredictionInterval=prpt.isWritePredictionInterval();

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>Prediction results</caption>\r\n");

		// ************************************************
		// Header row
		fw.write("<tr bgcolor=\"#D3D3D3\">\n");

		fw.write("<th>Endpoint</th>\n");

		if (expCAS==null) {
			fw.write("<th>Experimental value</th>\n");
		} else {
			fw.write("<th align=left>Experimental value (CAS= " + expCAS + ")");
//			fw.write("<th align=left>Experimental value");
//			fw.write(source);
			fw.write("</th>\n");
		}

		fw.write("<th>Predicted value");
		if (prpt.getPredictedValueSuperscript()!=null) {
			fw.write("<sup>"+prpt.getPredictedValueSuperscript()+"</sup>");
		}
		fw.write("</th>\n");
		
		if (writePredictionInterval)
			fw.write("<th>Prediction interval</th>\n");

		fw.write("</tr>\n");

		// ************************************************************
		// Molar units row
		
		fw.write("<tr>\n");

		fw.write("<td>" + prpt.getEndpointSubscripted() + " " + prpt.getMolarLogUnits() + "</td>\n");
		writeCenteredTD(fw,expToxValue);  // molar units
		writeCenteredTD(fw,predToxVal);  // molar units

		if (writePredictionInterval) {
			writeCenteredTD(fw,prpt.getPredMinMaxVal());
		}

		fw.write("</tr>\n");
		
		// ************************************************************
		// mass units row:

		if (prpt.getMassUnits()!=null) {
			
			fw.write("<tr>\n");

			fw.write("<td>" + prpt.getEndpointSubscripted() + " " + prpt.getMassUnits() + "</td>\n");
			writeCenteredTD(fw,ExpToxValMass);//mass
			writeCenteredTD(fw,PredToxValMass);//mass 

			if (writePredictionInterval) {
				writeCenteredTD(fw,prpt.getPredMinMaxValMass());
			}

			fw.write("</tr>\n");
		}
		

		// ************************************************************
//		if (pr.getMethod().contentEquals(TESTConstants.ChoiceLDA)) {
//			fw.write("<tr>\n");
//			writeTD(fw,"Mode of action");
//			writeCenteredTD(fw,prpt.getExpMOA());
//			writeCenteredTD(fw,prpt.getPredMOA());			
//			fw.write("<td bgcolor=\"darkgrey\"><BR></td>\n");		
//			
//			fw.write("</tr>\n");
//		}
		
		
		
		fw.write("</table>\n");

		if (prpt.getPredictedValueNote()!=null)
			fw.write("<sup>a</sup><font color=blue>"+prpt.getPredictedValueNote()+"</font><br>\r\n");

		// System.out.println(message);
		if (prpt.getMessage()!=null) {
			fw.write("<sup>b</sup><font color=darkred>" + prpt.getMessage() + "</font>\r\n");
		}
		
	}
	
	private void writeExternalPredChart(PredictionResults pr,ExternalPredChart epc,String units,FileWriter fw) throws Exception {
//		System.out.println("writeExternalPredChart");
		
		if (epc==null) {
			fw.write("<font color=red>*** No similar chemicals in the could be predicted***</font><br><br>\n");
			return;
		}
		
		DecimalFormat df=new DecimalFormat("0.00");
		
		fw.write("<table><tr>\n");
		
		fw.write("<td><img src=\"" + epc.getExternalPredChartImageSrc() + "\"></td>\n");
		

		fw.write("<td>\n");

		fw.write("\t<table border=1 cellpadding=10 cellspacing=0>\n");
		
		fw.write("\t<caption>Results for entire set vs<br>results for similar chemicals</caption>\n");
		
		fw.write("\t<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("\t<th>Chemicals</th>\n");
		fw.write("\t<th>MAE*</th>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		fw.write("\t<td>Entire set</td>\n");
		fw.write("\t<td>" + df.format(epc.getMAEEntireTestSet()) + "</td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		fw.write("\t<td>Similarity coefficient &ge; " + pr.getSCmin() + "</td>\n");

		
		
		if (epc.getMAE() < epc.getMAEEntireTestSet()) {
			fw.write("\t<td BGCOLOR=\"#90EE90\">" + df.format(epc.getMAE()) + "</td>\n");
		} else {
			fw.write("\t<td BGCOLOR=LIGHTPINK>" + df.format(epc.getMAE()) + "</td>\n");
		}
		// fw.write("\t<td>"+df.format(MAE)+"</td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t</table>\n");

		fw.write("*Mean absolute error in " + units + "\n");

		writeSimilarityLegend(fw);
		
		fw.write("</td>\n");

		
		fw.write("</tr></table>\n");

	}

	void writeSimilarityLegend(FileWriter fw) throws Exception {
//		System.out.println("writeSimilarityLegend");
		
//		fw.write("<td>\n");
		fw.write("<br><br>\r\n");
		
		fw.write("\t<table border=1 cellpadding=1 cellspacing=0>\n");
		fw.write("\t<caption>Color legend</caption>\n");
		
		fw.write("\t<tr bgcolor=\"#D3D3D3\">\n");		
		fw.write("\t<th>Color</th>\n");
		fw.write("\t<th>Range*</th>\n");		
		fw.write("\t</tr>\n");
		
		fw.write("\t<tr>\n");		
		fw.write("\t<td><font color=green>Green</font></td>\n");
		fw.write("\t<td>SC &#8805; 0.9 </td>\n");
		fw.write("\t</tr>\n");
		
		fw.write("\t<tr>\n");		
		fw.write("\t<td><font color=blue>Blue</font></td>\n");
		fw.write("\t<td>0.8 &#8804; SC < 0.9</td>\n");
		fw.write("\t</tr>\n");
		
		fw.write("\t<tr>\n");		
		fw.write("\t<td><font color=yellow>Yellow</font></td>\n");
		fw.write("\t<td>0.7 &#8804; SC < 0.8</td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");		
		fw.write("\t<td><font color=orange>Orange</font></td>\n");
		fw.write("\t<td>0.6 &#8804; SC < 0.7</td>\n");
		fw.write("\t</tr>\n");
		
		fw.write("\t<tr>\n");		
		fw.write("\t<td><font color=red>Red</font></td>\n");
		fw.write("\t<td>0.6 < SC</td>\n");
		fw.write("\t</tr>\n");

		
		fw.write("\t</table>\n");
		fw.write("*SC = similarity coefficient\n");
					
		
//		fw.write("</td>\n");
		
	}
	 

}
