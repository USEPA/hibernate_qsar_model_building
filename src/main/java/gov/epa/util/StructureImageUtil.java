package gov.epa.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

import com.epam.indigo.IndigoException;


/**
 * Class to create image files and embedded image link in html
 * 
 * @author TMARTI02
 *
 */
public class StructureImageUtil {
	
	private static final SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

	/**
	 * Write image to file
	 * 
	 * @param ac
	 * @param filepath
	 * @throws IOException
	 * @throws CDKException
	 */
	public static void writeImageFile(IAtomContainer ac, String filepath) throws IOException, CDKException {
		new DepictionGenerator().withAtomColors().withZoom(1.5).depict(ac).writeTo(filepath);
	}
	
	/**
	 * Write image to byte array
	 * 
	 * @param ac
	 * @return
	 * @throws IOException
	 * @throws CDKException
	 */
	public static byte[] writeImageBytes(IAtomContainer ac) throws IOException, CDKException {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		new DepictionGenerator().withAtomColors().withZoom(1.5).depict(ac).writeTo(Depiction.PNG_FMT,baos);
		return baos.toByteArray();
	}
	
	/**
	 * Creates image url so that image can be embedded inside html page using base64 encoded image
	 * 
	 * @param smiles
	 * @return
	 * @throws IOException
	 * @throws CDKException
	 * @throws IndigoException
	 */
	public static String generateImgSrc(String smiles) throws IOException, CDKException, IndigoException {
//		String inchikey = StructureUtil.indigoInchikeyFromSmiles(smiles);
		
		IAtomContainer ac = parser.parseSmiles(smiles);
		
//		String filepath="image.png";		
//		writeImageFile(ac, inchikey,filepath);//write temp image file						
//		byte[] bytes = Files.readAllBytes(Path.of(filepath));//read back in as bytes
		
		byte[] bytes=writeImageBytes(ac);
		
        String base64 = Base64.getEncoder().encodeToString(bytes);//convert to base 64
   		String imgURL="data:image/png;base64, "+base64;
   		return imgURL;
	}
	
	public static byte[] generateImageBytesFromSmiles(String smiles)  {
		try {		
			IAtomContainer ac = parser.parseSmiles(smiles);
			byte[] bytes=writeImageBytes(ac);

			return bytes;
		} catch (Exception ex) {
			return null;
		}	
	}
	
	
	
	
	
}
