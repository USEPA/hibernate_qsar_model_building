package gov.epa.run_from_java.scripts.OPERA;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.io.MDLV3000Writer;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.util.StructureUtil;
import java.io.*;

/**
* @author TMARTI02
*/
public class SnapshotVsOPERA2_8_DB {
	
	IAtomContainer getMoleculeFromMolFile3000(String molFile3000) {
		
		MDLV3000Reader mr=new MDLV3000Reader();
		IAtomContainer molecule=null;
		
		try {
			InputStream stream = new ByteArrayInputStream(molFile3000.getBytes());
			mr.setReader(stream);
			molecule = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());
		} catch (Exception ex) {
			molecule=new AtomContainer();
		}
		
		return molecule;
		
	}
	
	
	void getSDF() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\";
		String inputFilePath=folder+"differences between snapshot and OPERA2.8.txt";
		String outputFilePath=inputFilePath.replace(".txt",".sdf");
		
		SmilesParser sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		SmilesGenerator sg= new SmilesGenerator(SmiFlavor.Unique);

		try {

			Connection conn=SqlUtilities.getConnectionDSSTOX();

			BufferedReader br=new BufferedReader(new FileReader(inputFilePath));
			FileWriter fw=new FileWriter(outputFilePath);
			FileWriter fwSmi=new FileWriter(outputFilePath.replace(".sdf",".smi"));
			
			int counter=0;
			
			StringWriter sw=new StringWriter();
			MDLV3000Writer mw=new MDLV3000Writer(sw);
			
			while (true) {
				String Line=br.readLine();
				counter++;
				
				if(Line==null) break;
				String [] vals=Line.split("\t");
				String dtxcid=vals[0];
				String dtxsid=vals[1];
				String smiles=vals[2];
				
				if(smiles.equals("null")) {
					smiles=null;
				}
				
				if(counter%1000==0) {
					System.out.println(counter);	
				}
				
				String sql="select c.mol_file from compounds c\r\n"
						+ "where c.dsstox_compound_id='"+dtxcid+"';";
				
				String molFile=SqlUtilities.runSQL(conn, sql);

				String [] molLines=molFile.split("\n");
				
				
//				System.out.println(dtxsid+"\t"+molLines.length);
				
				IAtomContainer molecule=getMoleculeFromMolFile3000(molFile);
				
				if(!molFile.contains("M  V30")) {
					if (smiles!=null) {
						try {
							molecule = (AtomContainer)sp.parseSmiles(smiles);
							if(molecule.getAtomCount()>0) {
								System.out.println(dtxsid+"\tmol generated\t"+smiles);
//								System.out.println(dtxsid+"\tmolFile:\n"+molFile);
								mw.write(molecule);	
								molLines=sw.getBuffer().toString().split("\n");
							}
						} catch (Exception ex) {
							molecule=new AtomContainer();
						}
					}							
				} 

				if(smiles==null && molecule.getAtomCount()>0) {
					smiles=sg.create(molecule);
					System.out.println(dtxsid+"\tsmiles generated\t"+smiles);
				}
				
//				fw.write(molFile+"\r\n");
//				fw.write(molFile);
				
				if(molLines.length>0) {
					fw.write(dtxsid+"\r\n");
					for(int i=1;i<molLines.length;i++) {
						fw.write(molLines[i]+"\r\n");
					}

					fw.write(">  <DTXCID>\r\n");
					fw.write(dtxcid+"\r\n\r\n");
					
					fw.write(">  <DTXSID>\r\n");
					fw.write(dtxsid+"\r\n\r\n");
					
					fw.write(">  <smiles>\r\n");
					fw.write(smiles+"\r\n\r\n");
					
					fw.write("$$$$\r\n");
					fw.flush();
				} else {
					System.out.println(dtxsid+"\tmolLines is empty");
				}
				

				if(smiles!=null) {
					fwSmi.write(smiles+"\t"+dtxsid+"\r\n");
					fwSmi.flush();
				} else {
					System.out.println(dtxsid+"\tnull smiles");
				}
				

			}
			br.close();
			fw.close();
			fwSmi.close();
			System.out.println(counter);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		

	}
	
	

	void getSmilesDifferences() {
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\OPERA\\OPERA 2.8\\";
		String databasePath=folder+"OPERA_2.8.db";
		
		Connection conn=SqlUtilities.getConnectionSqlite(databasePath);
		
		
		String outputFilePath=folder+"differences between snapshot and OPERA2.8.txt";
		
		try {
			
			FileWriter fw=new FileWriter(outputFilePath);
			
			int counter=0;

			//**************************************************************************
			//Where 2d structure is different:
			String sql="select sc.cid, sc.sid, s.Original_SMILES,sc.smiles from Structure s\r\n"
					+ "join IDs i on i.DSSTOX_COMPOUND_ID=s.DSSTOX_COMPOUND_ID\r\n"
					+ "join SnapshotCompounds sc on sc.sid=i.DSSTOX_SUBSTANCE_ID\r\n"
					+ "where s.Original_SMILES!=sc.smiles and sc.sid!='N/A';";
			ResultSet rs=SqlUtilities.runSQL2(conn, sql);

			while (rs.next())  {
				counter++;
				if (counter%1000==0) {
					System.out.println(counter);
				}

				String cid=rs.getString(1);
				String sid=rs.getString(2);
				String smilesOPERA=rs.getString(3);
				String smilesSnapshot=rs.getString(4);
				String inchiKeyOPERA=StructureUtil.indigoInchikey1FromSmiles(smilesOPERA);
				String inchiKeySnapshot=StructureUtil.indigoInchikey1FromSmiles(smilesSnapshot);
				
				if(!inchiKeyOPERA.equals(inchiKeySnapshot)) {
//					System.out.println(counter+"\t"+sid+"\t"+smilesOPERA+"\t"+smilesSnapshot+"\t"+inchiKeyOPERA+"\t"+inchiKeySnapshot);
					System.out.println(sid+"\t"+smilesSnapshot);
					fw.write(cid+"\t"+sid+"\t"+smilesSnapshot+"\r\n");
					fw.flush();
				}
			}

			//**************************************************************************
			//New DTXSIDS:- a lot of these were discarded by OPERA when ran before...
			sql="select sc.cid, sc.sid, sc.smiles from SnapshotCompounds sc\r\n"
					+ "left join IDs i on i.DSSTOX_SUBSTANCE_ID=sc.sid\r\n"
					+ "where i.DSSTOX_SUBSTANCE_ID is null and sc.sid!='N/A';";
			rs=SqlUtilities.runSQL2(conn, sql);
			while (rs.next())  {
				counter++;
				if (counter%1000==0) {
					System.out.println(counter);
				}
				String cid=rs.getString(1);
				String sid=rs.getString(2);
				String smilesSnapshot=rs.getString(3);
				System.out.println(sid+"\t"+smilesSnapshot);
				fw.write(cid+"\t"+sid+"\t"+smilesSnapshot+"\r\n");
				fw.flush();
			}
			
			fw.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SnapshotVsOPERA2_8_DB s=new SnapshotVsOPERA2_8_DB();
//		s.getSmilesDifferences();
		s.getSDF();
	}

}


