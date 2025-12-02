package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA;

import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA_Old.SqliteUtilities;

/**
* @author TMARTI02
*/
public class OPERA_Structure {
	
	String DSSTOX_COMPOUND_ID;
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

				s.DSSTOX_COMPOUND_ID=values[0];
				s.Original_SMILES=values[1];
				s.Number_of_connected_components=Integer.parseInt(values[2]);
				s.Canonical_QSARr=values[3];
				s.Salt_Solvent=values[4];
				s.InChI_Code_QSARr=values[5];
				s.InChI_Key_QSARr=values[6];
				s.Salt_Solvent_ID=values[7];

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
	
				s.DSSTOX_COMPOUND_ID=rs.getString(2);
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
				s.DSSTOX_COMPOUND_ID=rs.getString(2);
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