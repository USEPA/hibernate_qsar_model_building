package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.DescriptorValuesDao;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.DescriptorValuesDaoImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class DescriptorValuesServiceImpl implements DescriptorValuesService {
	
	private Validator validator;
	
	public DescriptorValuesServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public DescriptorValues findByCanonQsarSmilesAndDescriptorSetName(String canonQsarSmiles, String descriptorValuesName) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorValuesName, session);
	}
	
	public DescriptorValues findByCanonQsarSmilesAndDescriptorSetName(String canonQsarSmiles, String descriptorSetName, Session session) {
		Transaction t = session.beginTransaction();
		DescriptorValuesDao descriptorValuesDao = new DescriptorValuesDaoImpl();
		DescriptorValues descriptorValues = 
				descriptorValuesDao.findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorSetName, session);
		t.rollback();
		return descriptorValues;
	}

	@Override
	public List<DescriptorValues> findByDescriptorSetName(String descriptorSetName) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByDescriptorSetName(descriptorSetName, session);
	}

	@Override
	public List<DescriptorValues> findByDescriptorSetName(String descriptorSetName, Session session) {
		Transaction t = session.beginTransaction();
		DescriptorValuesDao descriptorValuesDao = new DescriptorValuesDaoImpl();
		List<DescriptorValues> descriptorValues = 
				descriptorValuesDao.findByDescriptorSetName(descriptorSetName, session);
		t.rollback();
		return descriptorValues;
	}
	
	@Override
	public List<DescriptorValues> findByCanonQsarSmiles(String canonQsarSmiles) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByCanonQsarSmiles(canonQsarSmiles, session);
	}

	@Override
	public List<DescriptorValues> findByCanonQsarSmiles(String canonQsarSmiles, Session session) {
		Transaction t = session.beginTransaction();
		DescriptorValuesDao descriptorValuesDao = new DescriptorValuesDaoImpl();
		List<DescriptorValues> descriptorValues = 
				descriptorValuesDao.findByCanonQsarSmiles(canonQsarSmiles, session);
		t.rollback();
		return descriptorValues;
	}
	

	@Override
	public DescriptorValues create(DescriptorValues descriptorValues) throws ConstraintViolationException {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return create(descriptorValues, session);
	}

	@Override
	public DescriptorValues create(DescriptorValues descriptorValues, Session session) 
			throws ConstraintViolationException {
		Set<ConstraintViolation<DescriptorValues>> violations = validator.validate(descriptorValues);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(descriptorValues);
			session.flush();
			session.refresh(descriptorValues);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return descriptorValues;
	}
	
	@Override
	public void delete(DescriptorValues descriptorValues) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		delete(descriptorValues, session);
	}

	@Override
	public void delete(DescriptorValues descriptorValues, Session session) {
		if (descriptorValues.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.remove(descriptorValues);
		session.flush();
		t.commit();
	}

	@Override
	public  void createSql(List<DescriptorValues> valuesArray) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String [] fieldNames= {"canon_qsar_smiles", "fk_descriptor_set_id", "values_tsv",
				 "created_by", "created_at"};

		int batchSize=100;
		
		String sql="INSERT INTO qsar_descriptors.descriptor_values (";
		
		for (int i=0;i<fieldNames.length;i++) {
			sql+=fieldNames[i];
			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}
		
		for (int i=0;i<fieldNames.length-1;i++) {
			sql+="?";
			if (i<fieldNames.length-1)sql+=",";			 		
		}
		sql+="current_timestamp)";	
//		System.out.println(sql);
		
		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(sql);
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < valuesArray.size(); counter++) {
				DescriptorValues p=valuesArray.get(counter);
				prep.setString(1, p.getCanonQsarSmiles());
				prep.setLong(2, p.getDescriptorSet().getId());
				prep.setString(3, p.getValuesTsv());
				prep.setString(4, p.getCreatedBy());
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
//					System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+valuesArray.size()+" predictions using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}			

}
