package gov.epa.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
//import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

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
//		try (OutputStream os = new FileOutputStream(outputPdf)) {
//			PdfRendererBuilder builder = new PdfRendererBuilder();
//			builder.withUri(outputPdf);
//			builder.toStream(os);
//			builder.withW3cDocument(new W3CDom().fromJsoup(doc), null);
//			builder.run();
//		}
		
		
		try (OutputStream os = new FileOutputStream(outputPdf)) {
			PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
	        pdfBuilder.withUri(outputPdf); // Set the base URI (can be null if not needed)
	        pdfBuilder.toStream(os);
	        pdfBuilder.withW3cDocument(new W3CDom().fromJsoup(doc), null); // Pass the W3C Document and base URI
	        pdfBuilder.run();
		}

	}

	public static void main(String[] args) {
		try {
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\TEST5.1.3\\reports\\DTXSID3039242\\";
			String input=folder+"DTXSID3039242_TEST_BCF.html";
			String output=input.replace(".html", ".pdf");
			
			HtmlToPDF htmlToPdf = new HtmlToPDF();
			htmlToPdf.generateHtmlToPdf(input,output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
