package ToxPredictor.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import com.openhtmltopdf.extend.FSUriResolver;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.swing.NaiveUserAgent;

public class HtmlUtils {
	private static final Logger logger = LogManager.getLogger(HtmlUtils.class);

	/**
	 * Creates a tidy version of HTML
	 * 
	 * @param htmlPath
	 *            - Path to HTML file
	 * @param xhtmlPath
	 *            - Path to XHTML file. If null then XHTML path will be original
	 *            file name with *.xhtml extension.
	 * @return Path to XHTML file if success or null otherwise
	 * @throws IOException
	 */
	public static String HtmlToXhtml(String htmlPath, String xhtmlPath) {
		FileInputStream fis = null;
		FileOutputStream fos = null;

		if (Strings.isEmpty(xhtmlPath))
			xhtmlPath = FileUtils.replaceExtension(htmlPath, ".xhtml");

		logger.debug("Converting {} to {}", htmlPath, xhtmlPath);

		try {
			fis = new FileInputStream(htmlPath);

			Tidy tidy = new Tidy();
			tidy.setShowWarnings(false);
			tidy.setXmlTags(false);
			tidy.setInputEncoding("UTF-8");
			tidy.setOutputEncoding("UTF-8");
			tidy.setXHTML(true);
			tidy.setMakeClean(true);

			Document xmlDoc = tidy.parseDOM(fis, null);

			fos = new FileOutputStream(xhtmlPath);
			tidy.pprint(xmlDoc, fos);
		} catch (IOException e) {
			logger.catching(e);
			xhtmlPath = null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
		}

		return xhtmlPath;
	}

	/**
	 * Converts HTML to PDF
	 * @param htmlPath - Path to HTML file to be converted
	 * @param pdfPath - Path to PDF file to be generated. If null then path of HTML file is used as a basis with extension replaced. 
	 * @return Returns path to generated PDF file if success or null otherwise
	 * @throws IOException
	 */
	public static String HtmlToPdf(String htmlPath, String pdfPath) throws IOException {
		if (Strings.isEmpty(pdfPath))
			pdfPath = FileUtils.replaceExtension(htmlPath, ".pdf");

		logger.debug("Converting {} to {}", htmlPath, pdfPath);

		Path xhtmlPath = Files.createTempFile("html2pdf", ".xhtml");
		String path = HtmlToXhtml(htmlPath, xhtmlPath.toFile().getAbsolutePath());

		OutputStream os = null;
		try {
			os = new FileOutputStream(pdfPath);

			// There are more options on the builder than shown below.
			final NaiveUserAgent.DefaultUriResolver defaultUriResolver = new NaiveUserAgent.DefaultUriResolver();
			PdfRendererBuilder builder = new PdfRendererBuilder();

			String uri = new File(path).toURI().toString();
			builder.withUri(uri);
			builder.useUriResolver(new FSUriResolver() {
				@Override
				public String resolveURI(String baseUri, String uri) {
					// First get an absolute version.
					String supResolved = defaultUriResolver.resolveURI(baseUri, uri);
					try {
						File f = new File(new URI(supResolved).getPath());
						if (!f.exists()) {
							String newBase = new File(htmlPath).toURI().toString();
							supResolved = defaultUriResolver.resolveURI(newBase, uri);
						}
					} catch (URISyntaxException e1) {
						// We can mute as defaultUriResolver should have done its job 
					}
					return supResolved;
				}
			});
			builder.toStream(os);
			builder.run();

		} catch (Exception e) {
			logger.catching(e);
			pdfPath = null;
		} finally {
			try {
				os.close();
			} catch (IOException e) {
			}
		}

		return pdfPath;
	}
}
