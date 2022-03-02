package gov.epa.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.smiles.SmilesParser;

import com.epam.indigo.IndigoException;



public class StructureImageUtil {
	
	private static final SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

	
	public static void writeImageFile(AtomContainer ac, String filepath) throws IOException, CDKException {
		new DepictionGenerator().withAtomColors().withZoom(1.5).depict(ac).writeTo(filepath);
	}
	
	public static byte[] writeImageBytes(AtomContainer ac) throws IOException, CDKException {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		new DepictionGenerator().withAtomColors().withZoom(1.5).depict(ac).writeTo(Depiction.PNG_FMT,baos);
		return baos.toByteArray();
	}
	public static String generateImgSrc(String smiles) throws IOException, CDKException, IndigoException {
//		String inchikey = StructureUtil.indigoInchikeyFromSmiles(smiles);
		
		AtomContainer ac = (AtomContainer) parser.parseSmiles(smiles);
		
//		String filepath="image.png";		
//		writeImageFile(ac, inchikey,filepath);//write temp image file						
//		byte[] bytes = Files.readAllBytes(Path.of(filepath));//read back in as bytes
		
		byte[] bytes=writeImageBytes(ac);
		
        String base64 = Base64.getEncoder().encodeToString(bytes);//convert to base 64
   		String imgURL="data:image/png;base64, "+base64;
   		return imgURL;
	}
}
