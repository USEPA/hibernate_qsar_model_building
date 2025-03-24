package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class DSSTOX_compare_prod_and_snapshot {

	
	static List<DsstoxCompound> loadCompoundExport(String filepath) {
		
		Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();
		List<DsstoxCompound> compounds=new ArrayList<>();

		try {
			JsonArray ja=Utilities.gson.fromJson(new FileReader(filepath), JsonArray.class);
					
			
			for (int i=0;i<ja.size();i++) {
				JsonObject jo=ja.get(i).getAsJsonObject();
				DsstoxCompound c=new DsstoxCompound();
				c.setId(jo.get("id").getAsLong());
								
				if (jo.get("dsstox_compound_id").isJsonNull()) {
//					System.out.println(c.getId()+"\tdtxcid is null");
					continue;//dont store it if dont have dtxcid
				}
				
				c.setDsstoxCompoundId(jo.get("dsstox_compound_id").getAsString());
 
				
				if (!jo.get("dsstox_substance_id").isJsonNull()) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance();
					c.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);					
					gs.setDsstoxSubstanceId(jo.get("dsstox_substance_id").getAsString());
				}
					
					
				if (!jo.get("smiles").isJsonNull())
					c.setSmiles(jo.get("smiles").getAsString());
				
				if (!jo.get("jchem_inchi_key").isJsonNull())
					c.setJchemInchikey(jo.get("jchem_inchi_key").getAsString());

				if (!jo.get("indigo_inchi_key").isJsonNull())
					c.setIndigoInchikey(jo.get("indigo_inchi_key").getAsString());
				
				compounds.add(c);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} 
		return compounds;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String folder="data\\dsstox\\json\\";
		String filepathProd=folder+"prod_dsstox_compounds_2023_12_06.json";
		String filepathSnap=folder+"snapshot_dsstox_compounds_2023_12_06.json";

		List<DsstoxCompound> compoundsProd=loadCompoundExport(filepathProd);
		List<DsstoxCompound> compoundsSnap=loadCompoundExport(filepathSnap);
		TreeMap <Long,DsstoxCompound>hmSnap=new TreeMap<>();
		
		List<DsstoxCompound> compoundsProdNotInSnap=new ArrayList<>();
		List<DsstoxCompound> compoundsProdDifferentFromSnapSmilesInchiKey=new ArrayList<>();
		List<DsstoxCompound> compoundsProdDifferentFromSnapSmiles=new ArrayList<>();
		
		for (DsstoxCompound cSnap:compoundsSnap) {
			hmSnap.put(cSnap.getId(), cSnap);
		}
		
		
		for (DsstoxCompound cProd:compoundsProd) {
			if(hmSnap.get(cProd.getId())==null) {
				compoundsProdNotInSnap.add(cProd);
				
//				if (cProd.getGenericSubstanceCompound()!=null) {
//					System.out.println(cProd.getDsstoxCompoundId()+"\t"+cProd.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
//				} else {
//					System.out.println(cProd.getDsstoxCompoundId());
//				}
				
			} else {
				
				DsstoxCompound cSnap=hmSnap.get(cProd.getId());

				if(cProd.getSmiles()==null) continue;
						
				if(cSnap.getSmiles()==null || !cSnap.getSmiles().contentEquals(cProd.getSmiles())) {
					
					compoundsProdDifferentFromSnapSmiles.add(cProd);

					if(cProd.getJchemInchikey()==null) continue;

					if(!cProd.getJchemInchikey().contentEquals(cSnap.getJchemInchikey())) {
//						System.out.println(cProd.getSmiles()+"\t"+cSnap.getSmiles()+"\t"+cProd.getJchemInchikey().contentEquals(cSnap.getJchemInchikey()));
						compoundsProdDifferentFromSnapSmilesInchiKey.add(cProd);
					} else {
//						System.out.println("Smiles diff but jchem inchiKey is same:\t"+cProd.getSmiles()+"\t"+cSnap.getSmiles()+"\t"+cProd.getJchemInchikey()+"\t"+cSnap.getJchemInchikey());						
					}
					
				}
				
			}
		}
		 
		
		try {
			fillInFieldsFromProd(compoundsProdDifferentFromSnapSmiles);
			DSSTOX_Compounds_Script.createSDF(compoundsProdDifferentFromSnapSmiles, "data\\dsstox\\sdf\\prod compounds with different smiles from snapshot.sdf",false,false);

			fillInFieldsFromProd(compoundsProdNotInSnap);
			DSSTOX_Compounds_Script.createSDF(compoundsProdNotInSnap, "data\\dsstox\\sdf\\prod compounds not in snapshot.sdf",false,false);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Count in prod:"+compoundsProd.size());
		System.out.println("Count in snap:"+compoundsSnap.size());
		System.out.println("Count not in snapshot:"+compoundsProdNotInSnap.size());
		System.out.println("Count with different smiles:"+compoundsProdDifferentFromSnapSmiles.size());
		System.out.println("Count with different smiles and jchem inchiKey:"+compoundsProdDifferentFromSnapSmilesInchiKey.size());

	}


	private static void fillInFieldsFromProd(List<DsstoxCompound> compounds) {

		System.out.println("before adding mol files size:"+compounds.size());
		
		for (int i=0;i<compounds.size();i++) {
			
			DsstoxCompound cProd=compounds.get(i);
			
//			System.out.println(cProd.getDsstoxCompoundId());
			
			DsstoxCompound cProdDB=DSSTOX_Compounds_Script.getCompoundByDTXCID(cProd.getDsstoxCompoundId());
			
			if(cProdDB.getMolFile()==null) {
				System.out.println(cProd.getDsstoxCompoundId()+" has null mol file");
				compounds.remove(i--);
			} else {
				cProd.setMolFile(cProdDB.getMolFile());
				cProd.setSmiles(cProdDB.getSmiles());
				
				if(cProdDB.getGenericSubstanceCompound()!=null) {
					if(cProdDB.getGenericSubstanceCompound().getGenericSubstance().getCasrn()!=null) {
						cProd.getGenericSubstanceCompound().getGenericSubstance().setCasrn(cProdDB.getGenericSubstanceCompound().getGenericSubstance().getCasrn());
					}
				} 
			}
			
//			System.out.println(cProd.getDsstoxCompoundId()+"\t"+cProdDB.getSmiles()+"\t"+cProdDB.getMolFile());
			
			if (i%100==0) System.out.println(i);
		}
		
		System.out.println("after adding mol files size:"+compounds.size());
	}

}
