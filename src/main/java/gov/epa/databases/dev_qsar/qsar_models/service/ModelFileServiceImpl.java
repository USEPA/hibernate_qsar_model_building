package gov.epa.databases.dev_qsar.qsar_models.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelFileDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelFileDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelFile;
import gov.epa.run_from_java.scripts.SqlUtilities;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class ModelFileServiceImpl implements ModelFileService {
	
	private Validator validator;
	
	public ModelFileServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public ModelFile findByModelId(Long modelId, Long fileTypeId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelId(modelId, fileTypeId, session);
	}
	
	public ModelFile findByModelId(Long modelId, Long fileTypeId, Session session) {
		Transaction t = session.beginTransaction();
		ModelFileDao modelFileDao = new ModelFileDaoImpl();
		ModelFile modelFile = modelFileDao.findByModelId(modelId,fileTypeId, session);
		t.rollback();
		return modelFile;
	}

	@Override
	public ModelFile create(ModelFile modelFile) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelFile, session);
	}

	@Override
	public ModelFile create(ModelFile modelFile, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ModelFile>> violations = validator.validate(modelFile);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(modelFile);
			session.flush();
			session.refresh(modelFile);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return modelFile;
	}
	
	
	@Override
	public ModelFile update(ModelFile modelFile) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return update(modelFile,session);
	}

	@Override
	public ModelFile update(ModelFile modelFile, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<ModelFile>> violations = validator.validate(modelFile);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.clear();
			session.update(modelFile);
			session.flush();
			session.refresh(modelFile);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return modelFile;
		
	}

	@Override
	public void delete(ModelFile modelFile) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		delete(modelFile, session);
	}

	@Override
	public void delete(ModelFile modelFile, Session session) {
		if (modelFile.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.delete(modelFile);
		session.flush();
		t.commit();
	}


	public void updateSql(ModelFile modelFile) {

		try {
			Connection conn=SqlUtilities.getConnectionPostgres();
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE qsar_models.model_files SET file = ?, updated_by= ? WHERE fk_model_id = ? AND fk_file_type_id = ?");

			// set the preparedstatement parameters
			ps.setBytes(1, modelFile.getFile());
			ps.setString(2, modelFile.getUpdatedBy());
			ps.setLong(3, modelFile.getModel().getId());
			ps.setLong(4, modelFile.getFileType().getId());

			// call executeUpdate to execute our sql update statement
			ps.executeUpdate();
			ps.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
