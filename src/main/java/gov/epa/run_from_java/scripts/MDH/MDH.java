package gov.epa.run_from_java.scripts.MDH;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.smiles.SmilesParser;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.service.GenericSubstanceServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.GetExpPropInfo;

public class MDH {
	
	public static AtomContainer prepareMoleculeFromSmiles(DsstoxRecord rec) {
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		AtomContainer m = null;
		try {
			// m=sp.parseSmiles(Smiles);
			m = (AtomContainer) sp.parseSmiles(rec.smiles);

		} catch (org.openscience.cdk.exception.InvalidSmilesException e) {
			m = new AtomContainer();
		}
		
		m.setProperty("CAS", rec.casrn);
		m.setProperty("DTXSID", rec.dsstoxSubstanceId);
		m.setProperty("Name", rec.preferredName);
		return m;
	}

	void createListFileHPV() {
		
		try {
			
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18\\MNDOH\\MDH list\\";
			String destFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18\\src\\main\\resources\\";
//			String filenameInput="cas list WebVersion_2022_Chemicals_of_High_Concern_List.txt";
//			String filenameOutput="WebVersion_2022_Chemicals_of_High_Concern_List.sdf";
			
			String filenameOutput="2022 CHC HPV.sdf";
			
			String listName="MNDOHTOXFREE2022";
			
			
			
			String filenameInput="cas list 2022 CHC HPV.txt";
			List<String> CASRNs = Files.readAllLines(Paths.get(folder+filenameInput));
			
			int batch=500;
			List<DsstoxRecord>recs=new ArrayList<>();
			GenericSubstanceServiceImpl gssi=new GenericSubstanceServiceImpl();
			
			while (CASRNs.size()>0) {
				List<String>IDs2=new ArrayList<String>();

				for (int i=1;i<=batch;i++) {
					IDs2.add(CASRNs.remove(0));
					if (CASRNs.isEmpty()) break;
				}
				
				List<DsstoxRecord>recsDR=null;
				
				recsDR=gssi.findAsDsstoxRecordsByCasrnIn(IDs2);				
				recs.addAll(recsDR);
				
				recsDR=gssi.findAsDsstoxRecordsByOtherCasrnIn(IDs2);//stored otherCAS in DSSTox record so could use to create hashtable later				
				recs.addAll(recsDR);
				
				if (CASRNs.isEmpty()) break;
				
//				if(true) break;
			}
			
			
			AtomContainerSet acs=new AtomContainerSet();
			
			
			
			
			for (DsstoxRecord rec:recs) {
				AtomContainer ac=prepareMoleculeFromSmiles(rec);
//				System.out.println(rec.casrn+"\t"+rec.smiles);
				acs.addAtomContainer(ac);				
			}
			
			FileWriter fw = new FileWriter(destFolder+filenameOutput);
			
			
			SDFWriter sw=new SDFWriter();
			sw.setWriter(fw);
			
			for (int i=0;i<acs.getAtomContainerCount();i++) {
				
				AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
//				System.out.println(ac.getAtomCount());				
				
				sw.write(acs.getAtomContainer(i));
				fw.flush();
			}

			fw.flush();
			sw.close();
					
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	void createListFileCHC() {
		
		try {
			
//			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18\\MNDOH\\MDH list\\";
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18\\src\\main\\resources\\";
						
			String filenameOutput="WebVersion_2022_Chemicals_of_High_Concern_List_2023_02_07.sdf";
			String listName="MNDOHTOXFREE2022";
			
			List<DsstoxRecord>dsstoxRecords=GetExpPropInfo.getChemicalsFromDSSTOXList(listName);

			AtomContainerSet acs=new AtomContainerSet();
			
			System.out.println(dsstoxRecords.size());
			
			List<String>casList=new ArrayList<>();
			
			for (DsstoxRecord rec:dsstoxRecords) {
//				if(rec.casrn.contains("NOCAS"))continue;
				
				if (!casList.contains(rec.casrn)) {
					casList.add(rec.casrn);
				} else {
					continue;
				}
				
				AtomContainer ac=prepareMoleculeFromSmiles(rec);
//				System.out.println(rec.casrn+"\t"+rec.smiles);
				acs.addAtomContainer(ac);				
			}
			
			FileWriter fw = new FileWriter(folder+filenameOutput);
			SDFWriter sw=new SDFWriter(fw);

			sw.setWriter(fw);
			
			for (int i=0;i<acs.getAtomContainerCount();i++) {
				
				AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
				System.out.println(i+"\t"+ac.getProperty("CAS")+"\t"+ac.getAtomCount());				
				sw.write(acs.getAtomContainer(i));
			}
			sw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		MDH mdh=new MDH();
//		mdh.createListFileHPV();
		mdh.createListFileCHC();
	}
	
}
