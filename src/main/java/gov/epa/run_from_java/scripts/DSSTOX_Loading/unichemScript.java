package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.util.StructureUtil;
import gov.epa.util.StructureUtil.Inchi;

/**
* @author TMARTI02
*/
public class unichemScript {

	Connection conn=SqlUtilities.getConnectionDSSTOX();
	
	List<DsstoxCompound> getCompoundsBySQL(int offset,int limit) {

		List<DsstoxCompound>compounds=new ArrayList<>();

		String sql="SELECT dsstox_compound_id,mol_file,smiles, inchi, jchem_inchi_key,indigo_inchi_key,mol_weight,gs.dsstox_substance_id, gs.casrn\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is null)\n";
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				

				compound.setDsstoxCompoundId(rs.getString(1));

				if (rs.getString(2)!=null)
					compound.setMolFile(rs.getString(2));

				if (rs.getString(3)!=null)
					compound.setSmiles(rs.getString(3));

				if (rs.getString(4)!=null)
					compound.setInchi(rs.getString(4));

				if (rs.getString(5)!=null)
					compound.setJchemInchikey(rs.getString(5));

				if (rs.getString(6)!=null)
					compound.setIndigoInchikey(rs.getString(6));				

				if (rs.getString(7)!=null)
					compound.setMolWeight(Double.parseDouble(rs.getString(7)));

				if (rs.getString(8)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(8));

					if (rs.getString(9)!=null) {
						gs.setCasrn(rs.getString(9));
					}

				}

				compounds.add(compound);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return compounds;
	}

	
	void goThroughChemicalsWithNullIndigoInchiKey() {
		
		try {
		
			FileWriter fw=new FileWriter("data/dsstox/missing indigo inchis for compounds table.txt");
			
		List<DsstoxCompound>compounds=getCompoundsBySQL(0, 999999999);
		
		System.out.println(compounds.size());
		
		
		fw.write("dtxcid\tsmiles\tinchiKey\tinchi\tError\r\n");
		
		for (DsstoxCompound compound:compounds) {
			
			if (compound.getMolFile()==null) {
//				System.out.println(compound.getDsstoxCompoundId()+"\tNo mol file");
				fw.write(compound.getDsstoxCompoundId()+"\t"+compound.getSmiles()+"\t\tNo mol file\r\n");
				
			} else {
				Inchi inchi=StructureUtil.toInchiIndigo(compound.getMolFile());

				if (inchi==null) {
					fw.write(compound.getDsstoxCompoundId()+"\t"+compound.getSmiles()+"\t\t\tNull inchi\r\n");
				} else {
//					System.out.println(compound.getDsstoxCompoundId()+"\t"+inchi.inchiKey+"\t"+inchi.inchi+"\tOK\r\n");
//					fw.write(compound.getDsstoxCompoundId()+"\t"+inchi.inchiKey+"\t"+compound.getInchi()+"\tOK\r\n");
					fw.write(compound.getDsstoxCompoundId()+"\t"+compound.getSmiles()+"\t"+inchi.inchiKey+"\t"+inchi.inchi+"\tOK\r\n");
				}
				
			}
			fw.flush();
			
		}
		
		fw.flush();
		fw.close();
		
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String[] args) {
		unichemScript u=new unichemScript();
		u.goThroughChemicalsWithNullIndigoInchiKey();

	}

}
