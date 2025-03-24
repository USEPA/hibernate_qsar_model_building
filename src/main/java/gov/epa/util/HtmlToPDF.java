package gov.epa.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;


public class HtmlToPDF {


	private void generateHtmlToPdf(String HTML_INPUT,String PDF_OUTPUT ) throws IOException {
		File inputHTML = new File(HTML_INPUT);
		Document doc = createWellFormedHtml(inputHTML);
		
//		System.out.println(doc.toString());
		
		xhtmlToPdf(doc, PDF_OUTPUT);
	}

	private Document createWellFormedHtml(File inputHTML) throws IOException {
		Document document = Jsoup.parse(inputHTML, "UTF-8");
		document.outputSettings()
		.syntax(Document.OutputSettings.Syntax.xml);
		return document;
	}

	private void xhtmlToPdf(Document doc, String outputPdf) throws IOException {
		try (OutputStream os = new FileOutputStream(outputPdf)) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.withUri(outputPdf);
			builder.toStream(os);
			builder.withW3cDocument(new W3CDom().fromJsoup(doc), null);
			builder.run();
		}
	}

	public static void main(String[] args) {
		try {
			String folder="C:\\users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\MyToxicityBz\\ToxRuns\\ToxRun_67874-64-0\\Mutagenicity\\";
			String input=folder+"Mutagenicity_Consensus_67874-64-0.html";
			String output=input.replace(".html", ".pdf");
			
			HtmlToPDF htmlToPdf = new HtmlToPDF();
			htmlToPdf.generateHtmlToPdf(input,output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
