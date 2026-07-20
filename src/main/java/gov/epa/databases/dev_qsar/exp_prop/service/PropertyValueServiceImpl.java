package gov.epa.databases.dev_qsar.exp_prop.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyValueDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyValueDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.util.JsonUtilities;


public class PropertyValueServiceImpl implements PropertyValueService {
	
	private Validator validator;
	
	public PropertyValueServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	private static final Pattern EXP_PROP_ID_PATTERN = Pattern.compile("EXP0*([0-9]+)");
	
	@Override
	public PropertyValue findByExpPropId(String expPropId) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByExpPropId(expPropId, session);
	}

	@Override
	public PropertyValue findByExpPropId(String expPropId, Session session) {
		Matcher matcher = EXP_PROP_ID_PATTERN.matcher(expPropId);
		Long id = null;
		if (matcher.find()) {
			id = Long.parseLong(matcher.group(1));
		} else {
			return null;
		}
		
		Transaction t = session.beginTransaction();
		PropertyValueDao propertyValueDao = new PropertyValueDaoImpl();
		PropertyValue propertyValue = propertyValueDao.findById(id, session);
		t.rollback();
		return propertyValue;
	}

	@Override
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep,
			boolean omitValueQualifiers) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByPropertyNameWithOptions(propertyName, useKeep, omitValueQualifiers, session);
	}

	@Override
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep,
			boolean omitValueQualifiers, Session session) {
		Transaction t = session.beginTransaction();
		PropertyValueDao propertyValueDao = new PropertyValueDaoImpl();
		List<PropertyValue> propertyValues = propertyValueDao.findByPropertyNameWithOptions(propertyName, useKeep, omitValueQualifiers, session);
		t.rollback();
		return propertyValues;
	}

	@Override
	public PropertyValue create(PropertyValue propertyValue) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(propertyValue, session);
	}

	@Override
	public PropertyValue create(PropertyValue propertyValue, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PropertyValue>> violations = validator.validate(propertyValue);
		
		
		if (!violations.isEmpty()) {
			System.out.println(violations);
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(propertyValue);
			session.flush();
//			session.refresh(propertyValue);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			System.out.println(e);
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			System.out.println(e);
			t.rollback();
			throw e;
		}
		
//		System.out.println(propertyValue==null);
		return propertyValue;
	}

		
	@Override
	public PropertyValue update(PropertyValue propertyValue) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return update(propertyValue, session);
	}

	@Override
	public PropertyValue update(PropertyValue propertyValue, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PropertyValue>> violations = validator.validate(propertyValue);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t=session.getTransaction();
		if (!t.isActive()) session.beginTransaction();
		
		try {
			session.clear();
			session.merge(propertyValue);
			session.flush();
			session.refresh(propertyValue);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return propertyValue;
	}
	
	@Override
	public List<PropertyValue> update(List<PropertyValue> propertyValues) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return update(propertyValues, session);
	}

	@Override
	public List<PropertyValue> update(List<PropertyValue> propertyValues, Session session)
			throws ConstraintViolationException {

		Transaction tx = session.beginTransaction();
		for (int i = 0; i < propertyValues.size(); i++) {
			PropertyValue propertyValue = propertyValues.get(i);

			try {
//				Set<ConstraintViolation<PropertyValue>> violations = validator.validate(propertyValue);
//				if (!violations.isEmpty()) {
//					throw new ConstraintViolationException(violations);
//				}
				session.merge(propertyValue);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (i % 100 == 0) { // 50, same as the JDBC batch size
				// flush a batch of inserts and release memory:
				System.out.println(i);
				session.flush();
				session.clear();
			}
		}

		session.flush();
		session.clear();

		tx.commit();//things dont show up in db until this line. But if try to commit sooner it causes error
		session.close();

		return propertyValues;
	}

	@Override
	public void delete(List<PropertyValue> propertyValues) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		delete(propertyValues, session);
	}

	@Override
	public void delete(List<PropertyValue> propertyValues, Session session) {
		Transaction tx = session.beginTransaction();
		for (int i = 0; i < propertyValues.size(); i++) {
			PropertyValue propertyValue = propertyValues.get(i);

			try {
//				Set<ConstraintViolation<PropertyValue>> violations = validator.validate(propertyValue);
//				if (!violations.isEmpty()) {
//					throw new ConstraintViolationException(violations);
//				}
				session.remove(propertyValue);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (i % 100 == 0) { // 50, same as the JDBC batch size
				// flush a batch of inserts and release memory:
				System.out.println(i);
				session.flush();
				session.clear();
			}
		}

		session.flush();
		session.clear();

		tx.commit();//things dont show up in db until this line. But if try to commit sooner it causes error
		session.close();
		
	}

	@Override
	public boolean create(List<PropertyValue> propertyValues) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(propertyValues, session);
	}

	@Override
	public boolean create(List<PropertyValue> propertyValues, Session session)
			throws ConstraintViolationException {


		Transaction tx = session.beginTransaction();

		int batchSize=100;

		boolean success=true;
		
		try {

			long t1=System.currentTimeMillis();

			for (int i = 0; i < propertyValues.size(); i++) {
				PropertyValue propertyValue = propertyValues.get(i);
				session.persist(propertyValue);

				if ( i % batchSize == 0 ) { //20, same as the JDBC batch size
					//flush a batch of inserts and release memory:
					session.flush();
					session.clear();
					System.out.println("\t"+i);
				}
			}

			session.flush();//do the remaining ones
			session.clear();

			long t2=System.currentTimeMillis();

			System.out.println("using createBatch, time to post "+propertyValues.size()+" predictions using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");

			tx.commit();


		} catch (org.hibernate.exception.ConstraintViolationException e) {
			e.printStackTrace();
			tx.rollback();
			success=false;
		}

		session.close();
		return success;
	}
		
	

	@Override
	public int createSql(List<PropertyValue> propertyValues, Connection conn) {
	    final int batchSize = 1000;
	    int insertedParents = 0;

	    boolean oldAutoCommit;
	    try {
	        oldAutoCommit = conn.getAutoCommit();
	        conn.setAutoCommit(false);

	        long t1 = System.currentTimeMillis();
	        List<PropertyValue> chunk = new ArrayList<>(batchSize);

	        for (PropertyValue pv : propertyValues) {
	            chunk.add(pv);
	            if (chunk.size() == batchSize) {
	                insertedParents += saveToPropertyValuesTable(chunk, conn);
	                saveToParameterValuesTable(chunk, conn); // only uses rows with IDs
	                chunk.clear();
	            }
	        }
	        if (!chunk.isEmpty()) {
	            insertedParents += saveToPropertyValuesTable(chunk, conn);
	            saveToParameterValuesTable(chunk, conn);
	        }

	        conn.commit();
	        long t2 = System.currentTimeMillis();
	        // log timing if desired

	        conn.setAutoCommit(oldAutoCommit);
	    } catch (Exception e) {
	        try { conn.rollback(); } catch (Exception ignore) {}
	        e.printStackTrace();
	    }
	    return insertedParents;
	}
	

	private void saveToParameterValuesTable(List<PropertyValue> propertyValues, Connection conn) {
	    String[] fieldNames = {
	        "fk_property_value_id","fk_parameter_id","fk_unit_id","value_qualifier",
	        "value_point_estimate","value_min","value_max","value_error","value_text","created_by","created_at"
	    };
	    String sql = SqlUtilities.createSqlInsertWithTimeStamp(fieldNames, "parameter_values", "exp_prop");

	    try (PreparedStatement prep = conn.prepareStatement(sql)) {
	        for (PropertyValue parent : propertyValues) {
	            // Skip if parent was not inserted (no id)
	            if (parent.getId() == null) continue;
	            if (parent.getParameterValues() == null) continue;

	            for (ParameterValue pv : parent.getParameterValues()) {
	                int i = 0;
	                prep.setLong(++i, parent.getId()); // use the parent’s id we just set
	                prep.setLong(++i, pv.getParameter().getId());

					// System.out.println(parent.getId() + "\t" + pv.getParameter().getId() + "\t" + pv.getValueText());

	                prep.setLong(++i, pv.getUnit().getId());
	                i = SqlUtilities.setString(prep, pv.getValueQualifier(), i);
	                i = SqlUtilities.setDouble(prep, pv.getValuePointEstimate(), i);
	                i = SqlUtilities.setDouble(prep, pv.getValueMin(), i);
	                i = SqlUtilities.setDouble(prep, pv.getValueMax(), i);
	                i = SqlUtilities.setDouble(prep, pv.getValueError(), i);
	                i = SqlUtilities.setString(prep, pv.getValueText(), i);
	                i = SqlUtilities.setString(prep, pv.getCreatedBy(), i);
	                prep.addBatch();
	            }
	        }
	        prep.executeBatch();
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}
	
	
	private int saveToPropertyValuesTable(List<PropertyValue> propertyValues, Connection conn) {
	    String[] fieldNames = {
	        "fk_source_chemical_id","fk_property_id","fk_unit_id","fk_public_source_id",
	        "fk_public_source_original_id","fk_literature_source_id","page_url","document_name","file_name",
	        "value_qualifier","value_point_estimate","value_min","value_max","value_error","value_text",
	        "value_original","value_original_parsed","notes","keep","keep_reason","qc_flag","qc_notes",
	        "created_by","created_at"
	    };
	    String sql = SqlUtilities.createSqlInsertWithTimeStamp(fieldNames, "property_values", "exp_prop");

	    int inserted = 0;

	    try (PreparedStatement prep = conn.prepareStatement(sql, new String[] { "id" })) {
	        for (PropertyValue pv : propertyValues) {
	            int i = 0;
	            prep.setLong(++i, pv.getSourceChemical().getId());
	            prep.setLong(++i, pv.getProperty().getId());
	            prep.setLong(++i, pv.getUnit().getId());
	            if (pv.getPublicSource() == null) prep.setNull(++i, java.sql.Types.BIGINT);
	            else prep.setLong(++i, pv.getPublicSource().getId());
	            if (pv.getPublicSourceOriginal() == null) prep.setNull(++i, java.sql.Types.BIGINT);
	            else prep.setLong(++i, pv.getPublicSourceOriginal().getId());
	            if (pv.getLiteratureSource() == null) prep.setNull(++i, java.sql.Types.BIGINT);
	            else prep.setLong(++i, pv.getLiteratureSource().getId());
	            i = SqlUtilities.setString(prep, pv.getPageUrl(), i);
	            i = SqlUtilities.setString(prep, pv.getDocumentName(), i);
	            i = SqlUtilities.setString(prep, pv.getFileName(), i);
	            i = SqlUtilities.setString(prep, pv.getValueQualifier(), i);
	            i = SqlUtilities.setDouble(prep, pv.getValuePointEstimate(), i);
	            i = SqlUtilities.setDouble(prep, pv.getValueMin(), i);
	            i = SqlUtilities.setDouble(prep, pv.getValueMax(), i);
	            i = SqlUtilities.setDouble(prep, pv.getValueError(), i);
	            i = SqlUtilities.setString(prep, pv.getValueText(), i);
	            i = SqlUtilities.setString(prep, pv.getValueOriginal(), i);
	            i = SqlUtilities.setString(prep, pv.getValueOriginalParsed(), i);
	            i = SqlUtilities.setString(prep, pv.getNotes(), i);
	            i = SqlUtilities.setBoolean(prep, pv.getKeep(), i);
	            i = SqlUtilities.setString(prep, pv.getKeepReason(), i);
	            i = SqlUtilities.setBoolean(prep, pv.getQcFlag(), i);
	            i = SqlUtilities.setString(prep, pv.getQcNotes(), i);
	            i = SqlUtilities.setString(prep, pv.getCreatedBy(), i);
	            prep.addBatch();
	        }

	        int[] counts = prep.executeBatch();

	        try (ResultSet keys = prep.getGeneratedKeys()) {
	            int idx = 0;
	            int assigned = 0;
	            while (idx < counts.length) {
	                int c = counts[idx];
	                // Only advance keys for successful rows
	                if (c != java.sql.Statement.EXECUTE_FAILED) {
	                    if (keys != null && keys.next()) {
	                        PropertyValue pv = propertyValues.get(idx);
	                        pv.setId(keys.getLong(1));
	                        assigned++;
	                    } 
	                    // For SUCCESS_NO_INFO we still advance keys if present
	                } else {
	                    PropertyValue pv = propertyValues.get(idx);
	                    System.out.println("Failed to insert:"+JsonUtilities.gson.toJson(pv.createJsonObjectFromPropertyValue()));
	                }
	                idx++;
	            }
	            inserted = assigned;
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return inserted;
	}

}
