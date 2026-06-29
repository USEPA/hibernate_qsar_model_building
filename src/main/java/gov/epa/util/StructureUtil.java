package gov.epa.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;

import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;


public class StructureUtil {
	
//	private static final Logger opsinLogger = LogManager.getLogger("uk.ac.cam.ch.wwmm.opsin");
	
	public static String strName = "name";
	public static String strCID="cid";
	public static String strSID = "sid";
	public static  String strSmiles = "smiles";
	public static String strCAS = "CAS";//Need it to be capitalized for things like the batch table
	
	public static IAtomContainer createMolecule(String smiles, String DTXSID, String DTXCID, String CAS) {
		IAtomContainer m = prepareSmilesMolecule(smiles);
		m.setProperty(strSID, DTXSID);// store sid so dont need to look up later
		m.setProperty(strCID, DTXCID);// store sid so dont need to look up later
		m.setProperty(strSmiles, smiles);// need original smiles NOT QSAR ready smiles
		
		if (CAS != null)
			m.setProperty(strCAS, CAS);// need CAS for doing nearest neighbor method (to exclude training
													// chemical by CAS)
		return m;
	}
	
	

	public static IAtomContainer prepareSmilesMolecule(String Smiles) {

		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		IAtomContainer m = null;
		try {
			// m=sp.parseSmiles(Smiles);
			m = sp.parseSmiles(Smiles);
			

			m.setProperty("Error", "");

			if (Smiles.indexOf(".") > -1) {
				m.setProperty("Error", "Molecules can only contain one fragment");
				m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
			} else {
				checkAtomContainer(m);
			}

		} catch (org.openscience.cdk.exception.InvalidSmilesException e) {
			m = new AtomContainer();
//			String error = e.getMessage() + ", SMILES=" + Smiles;
			
			String error="Could not parse "+Smiles;
			
			m.setProperty("Error", error);
			m.setProperty("ErrorCode", ERROR_CODE_STRUCTURE_ERROR);
//			logger.error(error);
		}
		return m;
	}
	
	
	public static class APIMolecule {
		APIMolecule(String strStructure,Hashtable<String,Object>htProperties) {
			this.strStructure=strStructure;
			this.htProperties=htProperties;
		}
		public String strStructure;
		public Hashtable<String,Object>htProperties;
		
		@Override
		public String toString() {
			String strMolecule=strStructure;
			strMolecule+="\n";
			for(String property:htProperties.keySet()) {
				strMolecule+="> <"+property+">\n";
				strMolecule+=htProperties.get(property)+"\n\n";
			}
			
			strMolecule+="$$$\n";
			return strMolecule;
		}

		public String getProperty(String property) {
			if (htProperties.get(property)==null) return null;
			return (String)htProperties.get(property);
		}
	}
	
	
	public static AtomContainerSet filterAtomContainerSet(AtomContainerSet acs, boolean skipMissingSID, int maxCount) {
		AtomContainerSet acs2 = new AtomContainerSet();

		Iterator<IAtomContainer> iterator = acs.atomContainers().iterator();

		int count = 0;

		while (iterator.hasNext()) {
			IAtomContainer ac = iterator.next();
			String SID = ac.getProperty("DTXSID");
			if (skipMissingSID && SID == null) {
				// System.out.println("Skipping");
				continue;
			}
			acs2.addAtomContainer(ac);
			count++;
			// System.out.println(ac.getProperty("DTXSID")+"\t"+ac.getProperty("smiles"));

			//TODO add flag parameter for checking:
			checkAtomContainer(ac);// theoretically the webservice has its own checking

			if (count == maxCount)
				break;
		}
		return acs2;
	}
	
	
	public static final String ERROR_CODE_STRUCTURE_ERROR = "SE";// TODO
	private static final String ERROR_CODE_APPLICABILITY_DOMAIN_ERROR = "AD";
	private static final String ERROR_CODE_DESCRIPTOR_CALCULATION_ERROR = "DE";
	private static final String ERROR_CODE_APPLICATION_ERROR = "AE";

	
	public static void checkAtomContainer(IAtomContainer m) {

		if (haveBadElement(m)) {
			m.setProperty("Error", "Molecule contains unsupported element");
			m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
		} else if (m.getAtomCount() == 1) {
			m.setProperty("Error", "Only one nonhydrogen atom");
			m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
		} else if (m.getAtomCount() == 0) {
			m.setProperty("Error", "Number of atoms equals zero");
			m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
		} else if (!haveCarbon(m)) {
			m.setProperty("Error", "Molecule does not contain carbon");
			m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
		}

		AtomContainerSet moleculeSet = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(m);
		if (moleculeSet.getAtomContainerCount() > 1) {
//			m.setProperty("Error","Multiple molecules, largest fragment retained");
			m.setProperty("Error", "Multiple molecules");
			m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
		}

		if (m.getProperty("Error") == null)
			m.setProperty("Error", "");

	}
	
	

	public static boolean haveCarbon(IAtomContainer mol) {
		
		try {
			
		for (int i=0; i<mol.getAtomCount();i++) {

			String var = mol.getAtom(i).getSymbol();

			// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As, Hg, Sn

			if (var.equals("C")) {
				return true;
			}
		}
	
		return false;

	} catch (Exception e) {
		return true;
	}
		
	}
	

	public static boolean haveBadElement(IAtomContainer mol) {
		
		try {
						
			for (int i=0; i<mol.getAtomCount();i++) {

				String var = mol.getAtom(i).getSymbol();

				// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As, Hg, Sn

				if (!var.equals("C") && !var.equals("H") && !var.equals("O")
						&& !var.equals("N") && !var.equals("F")
						&& !var.equals("Cl") && !var.equals("Br")
						&& !var.equals("I") && !var.equals("S")
						&& !var.equals("P") && !var.equals("Si")
						&& !var.equals("As") && !var.equals("Hg")
						&& !var.equals("Sn")) {

					return true;
			
			
				}
			}
		
			
			return false;

		} catch (Exception e) {
			return true;
		}
		
		
	}
	
	/**
	 * Writing my own V3000 reader because CDK sucks and cant read SDFs for all the
	 * dashboard chemicals and get the properties too
	 * 
	 * @param sdfFilePath
	 * @return
	 */
	public static AtomContainerSet readSDFV3000(String sdfFilePath) {

		AtomContainerSet acs = new AtomContainerSet();

		MDLV3000Reader mr = new MDLV3000Reader();

		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		try {

			FileInputStream fis = new FileInputStream(sdfFilePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

			boolean stop = false;

			while (true) {

				String strStructure = "";

				while (true) {
					String Line = br.readLine();

					if (Line == null) {
						stop = true;
						break;
					}

					// System.out.println(Line);
					strStructure += Line + "\r\n";
					if (Line.contains("M  END"))
						break;
				}

				if (stop)
					break;

				InputStream stream = new ByteArrayInputStream(strStructure.getBytes());
				mr.setReader(stream);

				IAtomContainer molecule = null;

				try {
					molecule = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());
				} catch (Exception ex) {
					molecule = new AtomContainer();
				}

				while (true) {
					String Line = br.readLine();
					// System.out.println(Line);

					if (Line.contains(">  <")) {
						String field = Line.substring(Line.indexOf("<") + 1, Line.length() - 1);
						String value = br.readLine();
						molecule.setProperty(field, value);
						// System.out.println(field);
					}

					if (Line.contains("$$$"))
						break;
				}

				if (molecule.getAtomCount() == 0) {

					AtomContainer molecule2 = null;

					String smiles = molecule.getProperty("smiles");

					if (smiles != null) {
						try {
							molecule2 = (AtomContainer) sp.parseSmiles(smiles);
							// System.out.println(DTXCID+"\t"+smiles+"\t"+molecule2.getAtomCount());
						} catch (Exception ex) {
							molecule2 = new AtomContainer();
						}

					} else {
						molecule2 = new AtomContainer();
					}

					molecule2.setProperties(molecule.getProperties());
					acs.addAtomContainer(molecule2);

				} else {
					acs.addAtomContainer(molecule);
				}

			}

			br.close();
			mr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return acs;
	}
	
	public static List<APIMolecule> readSDF_to_API_Molecules(String sdfFilePath, int count) {
		return readSDF_to_API_Molecules(sdfFilePath, count, false,null, null);
	}
	
	/**
	 * Make it so that it doesnt convert to IAtomContainer object, just parse sdf into a better formatted mol string
	 * 
	 * @param sdfFilePath
	 * @param count
	 * @param insideJar
	 * @param encoding "UTF-8", "Windows-1252", etc

	 * 
	 * @return
	 */
	public static List<APIMolecule> readSDF_to_API_Molecules(String sdfFilePath, int count, boolean insideJar,Class clazz, String encoding) {
		
		List<APIMolecule>molecules=new ArrayList<>();
		
		try {

			BufferedReader br = null;

			if (insideJar) {
				// System.out.println(sdfFilePath);
				java.io.InputStream ins = clazz.getClassLoader().getResourceAsStream(sdfFilePath);
				InputStreamReader isr = new InputStreamReader(ins);
				br = new BufferedReader(isr);
			} else {
				FileInputStream fis = new FileInputStream(sdfFilePath);
				
				if(encoding!=null) {
					br = new BufferedReader(new InputStreamReader(fis, encoding));
				} else {
					br = new BufferedReader(new InputStreamReader(fis));	
				}
//				
			}

			while (true) {
				String strStructure = getStringStructure(br);
				Hashtable<String,Object>htProperties=getPropertiesHashtable(br);
				if(strStructure==null || htProperties==null) break;
				molecules.add(new APIMolecule(strStructure,htProperties));
				
				if(molecules.size()==count)break;
				
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return molecules;
	}
	
	
	public static Hashtable<String,Object> getPropertiesHashtable(BufferedReader br) throws IOException {
		String value = null;
		String field = null;

		Hashtable<String,Object>htProperties=new Hashtable<>();
		
		while (true) {

			String Line = br.readLine();
			
			if(Line==null)return null;
			
//			System.out.println(Line);

			if (Line.contains(">  <") || Line.contains("><") || Line.contains("> <")) {
				if (field != null && value != null) {
					htProperties.put(field, value);
//					System.out.println(field+"\t"+value+"\n");
				}
				field = Line.substring(Line.indexOf("<") + 1, Line.length() - 1);
				value = null;
				// System.out.println(field);
			} else if (Line.contains("$$$")) {
				htProperties.put(field, value);
				break;
			} else if (Line.trim().length() > 0) {

				if (value == null)
					value = Line;
				else
					value += "\n" + Line;
			}
			
		}
		//		System.out.println(gson.toJson(molecule.getProperties()));

		return htProperties;
	}
	
	public static String getStringStructure(BufferedReader br) {


		try {
			String type="V2000";

			String strStructure = "";

			while (true) {
				String Line = br.readLine();

				if (Line == null) return null;
				//			System.out.println(Line);
				if(Line.contains("V30 BEGIN CTAB")) type="V3000";

				// System.out.println(Line);
				strStructure += Line + "\r\n";
				if (Line.contains("M  END"))
					break;
			}

			return strStructure;

		} catch (Exception ex) {
			//				ex.printStackTrace();
			return null;
		}
	}
	
	public static boolean isSalt(IAtomContainer molecule) {
		AtomContainerSet  AtomContainerSet2 = (AtomContainerSet)ConnectivityChecker.partitionIntoMolecules(molecule);
		return AtomContainerSet2.getAtomContainerCount() > 1; 
	}
	
	public static class SimpleOpsinResult {
		public String smiles;
		public String message;
		
		public SimpleOpsinResult(String smiles, String message) {
			this.smiles = smiles;
			this.message = message;
		}
		
		public static SimpleOpsinResult fromOpsinResult(OpsinResult or) {
			String getMessage = or.getMessage();
			String message = (getMessage==null || getMessage.isBlank()) ? null : getMessage;
			
			String getSmiles = or.getSmiles();
			String smiles = (getSmiles==null || getSmiles.isBlank()) ? null : getSmiles;
			
			return new SimpleOpsinResult(smiles, message);
		}
	}
	
	public static String indigoInchikeyFromSmiles(String smiles) throws IndigoException {
		Indigo indigo = new Indigo();
		indigo.setOption("ignore-stereochemistry-errors", true);
		IndigoInchi indigoInchi = new IndigoInchi(indigo);

		try {
		
			IndigoObject molecule = indigo.loadMolecule(smiles);
			String inchi = indigoInchi.getInchi(molecule);
			String inchikey = indigoInchi.getInchiKey(inchi);
		
			return inchikey;
			
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static String indigoInchikey1FromSmiles(String smiles) throws IndigoException {
		Indigo indigo = new Indigo();
		indigo.setOption("ignore-stereochemistry-errors", true);
		IndigoInchi indigoInchi = new IndigoInchi(indigo);

		try {
		
			IndigoObject molecule = indigo.loadMolecule(smiles);
			String inchi = indigoInchi.getInchi(molecule);
			
			
			String inchikey = indigoInchi.getInchiKey(inchi);
			if(inchikey!=null) return inchikey.substring(0,14);
			
		} catch (Exception ex) {
		}

		return null;

	}
	
	public static String indigoInchikey1FromAtomContainer(IAtomContainer ac) throws IndigoException {
		Indigo indigo = new Indigo();
		indigo.setOption("ignore-stereochemistry-errors", true);
		IndigoInchi indigoInchi = new IndigoInchi(indigo);

		try {
			SmilesGenerator sg= new SmilesGenerator(SmiFlavor.Unique);
			String smiles=sg.create(ac);
		
			IndigoObject molecule = indigo.loadMolecule(smiles);
			String inchi = indigoInchi.getInchi(molecule);
			
			
			String inchikey = indigoInchi.getInchiKey(inchi);
			if(inchikey!=null) return inchikey.substring(0,14);
			
		} catch (Exception ex) {
		}

		return null;

	}
	
	public static Double molecularWeight(String smiles) throws IndigoException {
		Indigo indigo = new Indigo();
		indigo.setOption("ignore-stereochemistry-errors", true);

		try {
			IndigoObject molecule = indigo.loadMolecule(smiles);
			return molecule.molecularWeight();
		} catch (Exception ex) {
		}
		return null;

	}
	
	/**
	 * TODO this doesnt work yet
	 * 
	 * @param smiles
	 * @return
	 * @throws IndigoException
	 */
	public static String indigoInchikey1FromSmilesFixedH(String smiles) throws IndigoException {
		Indigo indigo = new Indigo();
		
		indigo.setOption("ignore-stereochemistry-errors", true);
		
//		indigo.setOption("/FixedH",true);
		
		indigo.setOption("inchi-options", "/FixedH");//TODO doesnt work
		
		IndigoInchi indigoInchi = new IndigoInchi(indigo);

		try {
		
			IndigoObject molecule = indigo.loadMolecule(smiles);
			String inchi = indigoInchi.getInchi(molecule);
			
			String inchikey = indigoInchi.getInchiKey(inchi);
			if(inchikey!=null) return inchikey.substring(0,14);
			
		} catch (Exception ex) {
		}

		return null;

	}

	
	public static Double molWeightFromSmiles(String smiles) throws IndigoException {

		Indigo indigo = new Indigo();
		indigo.setOption("ignore-stereochemistry-errors", true);
		
		try {
			IndigoObject molecule = indigo.loadMolecule(smiles);
			return molecule.molecularWeight();
		} catch (Exception ex) {
			return null;
		}
	}
	public static class Inchi {
		public String inchi, inchiKey, inchiKey1,warning; 
	}
	
	public static Inchi toInchiIndigo(String mol) {
		try {
			Indigo indigo = new Indigo();
			indigo.setOption("ignore-stereochemistry-errors", true);

			IndigoInchi indigoInchi = new IndigoInchi(indigo);

			IndigoObject m = indigo.loadMolecule(mol);

			Inchi inchi = new Inchi();
			inchi.inchi = indigoInchi.getInchi(m);
			inchi.inchiKey = indigoInchi.getInchiKey(inchi.inchi);
			inchi.inchiKey1 = inchi.inchiKey != null ? inchi.inchiKey.substring(0, 14) : null;

			return inchi;

		} catch (IndigoException ex) {
			//			log.error(ex.getMessage());
			return null;
		}
	}

	public static SimpleOpsinResult opsinSmilesFromChemicalName(String chemicalName) {
		if (chemicalName==null) {
			return null;
		}
		
//		opsinLogger.setLevel(Level.OFF);
		NameToStructure nts = NameToStructure.getInstance();
		OpsinResult or = nts.parseChemicalName(chemicalName);
		return SimpleOpsinResult.fromOpsinResult(or);
	}

	

	
	
	static void testInchiKeyFromSmiles() {
		
//		String inchiKey=indigoInchikeyFromSmiles("ClC1=C(Cl)[C@]2(Cl)[C@@H]3[C@@H]4CC(C=C4)[C@@H]3C1(Cl)C2(Cl)Cl");
//		System.out.println(inchiKey);
		
//		String [] smilesList= {"C1Oc2ccc(NC(=O)ONC3C(=O)N=C4C=CC=CC=34)cc2O1",
//				 "C1C=CC2=NC(C(NOC(=O)Nc3ccc4OCOc4c3)=C2C=1)=O",
//				 "N1C(C(NOC(=O)Nc2ccc3OCOc3c2)=C2C=1C=CC=C2)=O"};
		
		String [] smilesList= {"O=C1N=CNC2NC(=S)NC1=2",
				"S=C1NC2NC=NC(=O)C=2N1",
				"C12NC=NC(=O)C=1NC(N2)=S"};
		
		for (String smiles:smilesList) {
			String inchiKey=indigoInchikeyFromSmiles(smiles);
			System.out.println(inchiKey);
		}
		
		
	}
	
	public static void main(String[] args) {
		
		StructureUtil su=new StructureUtil();
		su.testInchiKeyFromSmiles();
		
	}
	
		
	public static IAtomContainer fromMolString(String mol3000) throws Exception {
		MDLV3000Reader mr=new MDLV3000Reader();
		StringReader reader = new StringReader(mol3000);
		mr.setReader(new BufferedReader(reader));
		return mr.readMolecule(DefaultChemObjectBuilder.getInstance());
			// TODO Auto-generated catch block
		
	}
	
}
