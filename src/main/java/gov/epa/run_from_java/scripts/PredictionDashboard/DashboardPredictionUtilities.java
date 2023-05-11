package gov.epa.run_from_java.scripts.PredictionDashboard;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DashboardPredictionUtilities {
	
	public Gson gson;
	
	public DashboardPredictionUtilities() {
		this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	}
	
	/**
	 * Writing my own V3000 reader because CDK sucks and cant read SDFs for all the dashboard chemicals and get the properties too
	 * 
	 * @param sdfFilePath
	 * @return
	 */
	public AtomContainerSet readSDFV3000(String sdfFilePath) {

		AtomContainerSet acs = new AtomContainerSet();

		MDLV3000Reader mr=new MDLV3000Reader();

		SmilesParser sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		
		try {

			FileInputStream fis=new FileInputStream(sdfFilePath);
			BufferedReader br=new BufferedReader (new InputStreamReader(fis, "UTF-8"));

			boolean stop=false;

			while (true) {

				String strStructure="";

				while (true) {
					String Line=br.readLine();

					if(Line==null) {
						stop=true;
						break;
					}

					//				System.out.println(Line);
					strStructure+=Line+"\r\n";
					if(Line.contains("M  END"))break;
				}

				if(stop)break;


				InputStream stream = new ByteArrayInputStream(strStructure.getBytes());
				mr.setReader(stream);

				IAtomContainer molecule=null;
				
				try {
					molecule = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());
				} catch (Exception ex) {
					molecule=new AtomContainer();
				}

				while (true) {
					String Line=br.readLine();
					//				System.out.println(Line);

					if(Line.contains(">  <")) {
						String field=Line.substring(Line.indexOf("<")+1,Line.length()-1);
						String value=br.readLine();
						molecule.setProperty(field, value);
						//					System.out.println(field);
					}

					if(Line.contains("$$$"))break;
				}

				if(molecule.getAtomCount()==0) {
				
					AtomContainer molecule2=null;
					
					String smiles=molecule.getProperty("smiles");
					
					if (smiles!=null) {
						try {
							molecule2 = (AtomContainer)sp.parseSmiles(smiles);
//						System.out.println(DTXCID+"\t"+smiles+"\t"+molecule2.getAtomCount());
						} catch (Exception ex) {
							molecule2=new AtomContainer();
						}
						
					}else {
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
	
	
	public AtomContainerSet filterAtomContainerSet(AtomContainerSet acs, boolean skipMissingSID, int maxCount) {

		
		int count=0;
		
		Iterator<IAtomContainer> iterator= acs.atomContainers().iterator();

		AtomContainerSet acs2=new AtomContainerSet();

		
		while (iterator.hasNext()) {
			AtomContainer ac=(AtomContainer) iterator.next();
			String SID=ac.getProperty("DTXSID");
			if(skipMissingSID && SID==null) continue;
			acs2.addAtomContainer(ac);
			count++;
//			System.out.println(ac.getProperty("DTXSID")+"\t"+ac.getProperty("smiles"));
//			WebTEST4.checkAtomContainer(ac);//theoretically the webservice has its own checking
			
			if(count==maxCount)break;
		}
		return acs2;
	}



}
