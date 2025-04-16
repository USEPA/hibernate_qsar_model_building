package gov.epa.databases.dsstox.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.dao.DsstoxCompoundDao;
import gov.epa.databases.dsstox.dao.DsstoxCompoundDaoImpl;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class DsstoxCompoundServiceImpl implements DsstoxCompoundService {

	@Override
	public DsstoxCompound findById(Long id) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findById(id, session);
	}
	
	@Override
	public DsstoxCompound findById(Long id, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		DsstoxCompound compound = compoundDao.findById(id, session);
		t.rollback();
		return compound;
	}
	
	@Override
	public List<DsstoxCompound> findByIdIn(Collection<Long> ids) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByIdIn(ids, session);
	}
	
	@Override
	public List<DsstoxCompound> findByIdIn(Collection<Long> ids, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxCompound> compounds = compoundDao.findByIdIn(ids, session);
		t.rollback();
		return compounds;
	}
	

	
	@Override
	public List<DsstoxCompound> findAll() {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	@Override
	public List<DsstoxCompound> findAll(int offset,int limit) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAll(session,offset,limit);
	}

	
	@Override
	public List<DsstoxCompound> findAll(Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxCompound> compounds = compoundDao.findAll(session);
		t.rollback();
		return compounds;
	}
	
	
	@Override
	public List<DsstoxCompound> findAll(Session session,int offset,int limit) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxCompound> compounds = compoundDao.findAll(session,offset,limit);
		t.rollback();
		return compounds;
	}
	
	@Override
	public DsstoxCompound findByDtxcid(String dtxcid) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByDtxcid(dtxcid, session);
	}
	
	@Override
	public DsstoxCompound findByDtxcid(String dtxcid, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		DsstoxCompound compound = compoundDao.findByDtxcid(dtxcid, session);
		t.rollback();
		return compound;
	}
	
	@Override
	public List<DsstoxCompound> findByDtxcidIn(Collection<String> dtxcids) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByDtxcidIn(dtxcids, session);
	}
	
	@Override
	public List<DsstoxCompound> findByDtxcidIn(Collection<String> dtxcids, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxCompound> compounds = compoundDao.findByDtxcidIn(dtxcids, session);
		t.rollback();
		return compounds;
	}
	
	@Override
	public DsstoxCompound findByInchikey(String inchikey) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByInchikey(inchikey, session);
	}
	
	@Override
	public DsstoxCompound findByInchikey(String inchikey, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		DsstoxCompound compound = compoundDao.findByInchikey(inchikey, session);
		t.rollback();
		return compound;
	}
	
	@Override
	public List<DsstoxCompound> findByInchikeyIn(Collection<String> inchikeys) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findByInchikeyIn(inchikeys, session);
	}
	
	@Override
	public List<DsstoxCompound> findByInchikeyIn(Collection<String> inchikeys, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxCompound> compounds = compoundDao.findByInchikeyIn(inchikeys, session);
		t.rollback();
		return compounds;
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxcidIn(Collection<String> dtxcids) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAsDsstoxRecordsByDtxcidIn(dtxcids, session);
	}
	
	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxcidIn(Collection<String> dtxcids, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxRecord> compounds = compoundDao.findAsDsstoxRecordsByDtxcidIn(dtxcids, session);
		t.rollback();
		return compounds;
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByInChiKeyIn(Collection<String> inChiKeys) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAsDsstoxRecordsByInChiKeyIn(inChiKeys, session);
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByInChiKeyIn(Collection<String> inChiKeys, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxRecord> compounds = compoundDao.findAsDsstoxRecordsByInChiKeyIn(inChiKeys, session);
		t.rollback();
		return compounds;
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxcid(String dtxcid) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAsDsstoxRecordsByDtxcid(dtxcid, session);
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByDtxcid(String dtxcid, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxRecord> compounds = compoundDao.findAsDsstoxRecordsByDtxcid(dtxcid, session);
		t.rollback();
		return compounds;
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByInchikey(String inchikey) {
		Session session = DsstoxSession.getSessionFactory().getCurrentSession();
		return findAsDsstoxRecordsByInchikey(inchikey, session);
	}

	@Override
	public List<DsstoxRecord> findAsDsstoxRecordsByInchikey(String inchikey, Session session) {
		Transaction t = session.beginTransaction();
		DsstoxCompoundDao compoundDao = new DsstoxCompoundDaoImpl();
		List<DsstoxRecord> compounds = compoundDao.findAsDsstoxRecordsByInchikey(inchikey, session);
		t.rollback();
		return compounds;
	}
	
	
	List<DsstoxCompound> getCompoundsBySQL(int offset,int limit) {

		Connection conn=SqlUtilities.getConnectionDSSTOX();
		
		List<DsstoxCompound>compounds=new ArrayList<>();

		String sql="SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,"
				+ "gs.dsstox_substance_id, gs.casrn, gs.preferred_name\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null)\n";
		sql+="ORDER BY dsstox_compound_id\n";
		sql+="LIMIT "+limit+" OFFSET "+offset;

		System.out.println(sql);

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
					compound.setJchemInchikey(rs.getString(4));

				if (rs.getString(5)!=null)
					compound.setIndigoInchikey(rs.getString(5));				

				if (rs.getString(6)!=null)
					compound.setMolWeight(Double.parseDouble(rs.getString(6)));

				if (rs.getString(7)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(7));

					if (rs.getString(8)!=null) {
						gs.setCasrn(rs.getString(8));
					}
					
					if (rs.getString(9)!=null) {
						gs.setPreferredName(rs.getString(9));
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
	
	
	public List<DsstoxCompound> getCompoundsBySQL(List<String>dtxcids) {

		Connection conn=SqlUtilities.getConnectionDSSTOX();
		
		List<DsstoxCompound>compounds=new ArrayList<>();

		String sql="SELECT dsstox_compound_id,mol_file,smiles, jchem_inchi_key,indigo_inchi_key,mol_weight,"
				+ "gs.dsstox_substance_id, gs.casrn, gs.preferred_name, gs.updated_at\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null)\n";
		
		Iterator<String> it=dtxcids.iterator();
		String strDtxcids="";
		while (it.hasNext()) {
			strDtxcids+="'"+it.next()+"'";
			if(it.hasNext()) strDtxcids+=",";
		}
		
		sql+="where dsstox_compound_id in ("+strDtxcids+")\n";
		sql+="ORDER BY dsstox_compound_id\n";
		

		System.out.println(sql);

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
					compound.setJchemInchikey(rs.getString(4));

				if (rs.getString(5)!=null)
					compound.setIndigoInchikey(rs.getString(5));				

				if (rs.getString(6)!=null)
					compound.setMolWeight(Double.parseDouble(rs.getString(6)));

				if (rs.getString(7)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(7));

					if (rs.getString(8)!=null) {
						gs.setCasrn(rs.getString(8));
					}
					
					if (rs.getString(9)!=null) {
						gs.setPreferredName(rs.getString(9));
					}

					if (rs.getTimestamp(10)!=null) {
						gs.setUpdatedAt(rs.getTimestamp(10));
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
	
	
	
	public static void main(String[] args) {
//		DsstoxCompoundServiceImpl d=new DsstoxCompoundServiceImpl();
		
//		System.out.println("here");
		
//		List<String> dtxcids=new ArrayList<>();
//		dtxcids.add("DTXCID501515091");
//		dtxcids.add("DTXCID301515095");
//		dtxcids.add("DTXCID201323318");
//		dtxcids.add("DTXCID901475302");
//		dtxcids.add("DTXCID701508652");
//		dtxcids.add("DTXCID701508769");
//		dtxcids.add("DTXCID101509002");
//		dtxcids.add("DTXCID001508651");
//		dtxcids.add("DTXCID201508807");
//		dtxcids.add("DTXCID801766110");
//		dtxcids.add("DTXCID501506236");
//		dtxcids.add("DTXCID401513880");
//		dtxcids.add("DTXCID30509096");//not markush
		
		
//		List<DsstoxRecord>recs=d.findAsDsstoxRecordsByDtxcidIn(dtxcids);
//		for (DsstoxRecord rec:recs) {
//		System.out.println(rec.dsstoxCompoundId+"\t"+rec.qsarReadySmiles);
		//	}
		
		
		DsstoxCompoundServiceImpl d=new DsstoxCompoundServiceImpl();

//		String dtxcid="DTXCID20135";
//		List<DsstoxRecord>recs=d.findAsDsstoxRecordsByDtxcid(dtxcid);
//		System.out.println(Utilities.gson.toJson(recs));
//		
//		DsstoxCompound c=d.findByDtxcid(dtxcid);
//		System.out.println(c.getChemspiderId());
		
		List<DsstoxCompound>compounds=d.findAll(0, 1);
		DsstoxCompound c=compounds.get(0);
		System.out.println(c.getDsstoxCompoundId());
		
		
		
	}

}
