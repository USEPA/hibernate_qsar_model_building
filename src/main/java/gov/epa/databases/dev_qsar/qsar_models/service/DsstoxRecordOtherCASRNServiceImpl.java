package gov.epa.databases.dev_qsar.qsar_models.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.DsstoxOtherCASRN_DaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.dao.DsstoxRecordDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxOtherCASRN;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class DsstoxRecordOtherCASRNServiceImpl  {

	private Validator validator;
	
	public DsstoxRecordOtherCASRNServiceImpl () {
		this.validator = DevQsarValidator.getValidator();
	}
	



	public List<DsstoxOtherCASRN> findAll() {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<DsstoxOtherCASRN> findAllSql() {
		
		List<DsstoxOtherCASRN>recs=new ArrayList<>();
		
		String sql="Select casrn,fk_dsstox_record_id from qsar_models.dsstox_other_casrns";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		try {
			while (rs.next()) {
				DsstoxOtherCASRN rec=new DsstoxOtherCASRN();
				rec.setCasrn(rs.getString(1));
				rec.setFk_dsstox_record_id(rs.getLong(2));
				recs.add(rec);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return recs;
		
	}

	
	public List<DsstoxOtherCASRN> findAll(Session session) {
		Transaction t = session.beginTransaction();
		DsstoxOtherCASRN_DaoImpl dao = new DsstoxOtherCASRN_DaoImpl();
		List<DsstoxOtherCASRN> recs = dao.findAll(session);
		t.rollback();
		return recs;
	}
	

}
