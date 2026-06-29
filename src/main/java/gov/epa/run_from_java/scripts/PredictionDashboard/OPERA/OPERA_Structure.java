package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA;

import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.opencsv.CSVReader;

import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA_Old.SqliteUtilities;

/**
* @author TMARTI02
*/
public class OPERA_Structure {
	
//	String DSSTOX_COMPOUND_ID;
	String Molecule_name;
	String Original_SMILES;
	int  Number_of_connected_components;
	String Canonical_QSARr;
	String Salt_Solvent;
	String InChI_Code_QSARr;
	String InChI_Key_QSARr;
	String Salt_Solvent_ID;
	
	public static List<OPERA_Structure> readStructureCSV(String filepath,int count) {

		List<OPERA_Structure>operaStructures=new ArrayList<>();

		try {
			CSVReader reader = new CSVReader(new FileReader(filepath));
			String []colNames=reader.readNext();
			//			List<String>colNamesAll=Arrays.asList(colNames);

			int linesRead=0;

			while (true) {
				String []values=reader.readNext();
				if (values==null || values.length<=1) break;
				linesRead++;

				OPERA_Structure s=new OPERA_Structure();
				
				Hashtable<String,String>ht=new Hashtable<>();
				
				for(int i=0;i<colNames.length;i++) {
					String colName=colNames[i];
					String colValue=values[i];
					ht.put(colName,colValue);
				}
				
				s.Molecule_name=ht.get("Molecule name");
				s.Original_SMILES=ht.get("Original_SMILES");
				s.Number_of_connected_components=Integer.parseInt(ht.get("Number of connected components"));
				s.Canonical_QSARr=ht.get("Canonical_QSARr");
				s.InChI_Code_QSARr=ht.get("InChI_Code_QSARr");
				s.InChI_Key_QSARr=ht.get("InChI Key_QSARr");
				s.Salt_Solvent=ht.get("Salt_Solvent");
				s.Salt_Solvent_ID=ht.get("Salt_Solvent_ID");
				operaStructures.add(s);

				//				System.out.println(values[0]);
				if(linesRead==count) break;
			}

			reader.close();

		}catch (Exception  ex) {
			ex.printStackTrace();
		}

		return operaStructures;


	}
	public static List<OPERA_Structure> readStructureTableFromSqlite(Statement sqliteStatement) {
	
		List<OPERA_Structure>operaStructures=new ArrayList<>();
	
		try {
	
			String sql="select * from Structure";
			ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);
	
			while (rs.next()) {
	
	
				OPERA_Structure s=new OPERA_Structure();
	
				s.Molecule_name=rs.getString(2);
				s.Original_SMILES=rs.getString(3);
				s.Number_of_connected_components=rs.getInt(4);
				s.Canonical_QSARr=rs.getString(5);
				s.Salt_Solvent=rs.getString(6);
				s.InChI_Code_QSARr=rs.getString(7);
				s.InChI_Key_QSARr=rs.getString(8);
				s.Salt_Solvent_ID=rs.getString(9);
				operaStructures.add(s);
				//				System.out.println(values[0]);
			}
	
		}catch (Exception  ex) {
			ex.printStackTrace();
		}
	
		return operaStructures;
	
	
	}
	
	public static OPERA_Structure readStructureTableFromSqlite(Statement sqliteStatement,String dtxcid) {
		
		
	
		try {
	
			String sql="select * from Structure where DSSTOX_COMPOUND_ID='"+dtxcid+"'";
			ResultSet rs=SqliteUtilities.getRecords(sqliteStatement, sql);
	
			while (rs.next()) {
				OPERA_Structure s=new OPERA_Structure();
				s.Molecule_name=rs.getString(2);
				s.Original_SMILES=rs.getString(3);
				s.Number_of_connected_components=rs.getInt(4);
				s.Canonical_QSARr=rs.getString(5);
				s.Salt_Solvent=rs.getString(6);
				s.InChI_Code_QSARr=rs.getString(7);
				s.InChI_Key_QSARr=rs.getString(8);
				s.Salt_Solvent_ID=rs.getString(9);
				return s;
			}
	
		}catch (Exception  ex) {
			ex.printStackTrace();
		}
	
		return null;
	
	
	}

}