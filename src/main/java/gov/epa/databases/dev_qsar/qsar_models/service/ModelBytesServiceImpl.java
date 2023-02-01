package gov.epa.databases.dev_qsar.qsar_models.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelBytesDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelBytesDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class ModelBytesServiceImpl implements ModelBytesService {

	public static int chunkSize = 26214400;
	
	private Validator validator;
	
	public ModelBytesServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public ModelBytes findByModelId(Long modelId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelId(modelId, session);
	}
	
	public ModelBytes findByModelId(Long modelId, Session session) {
		Transaction t = session.beginTransaction();
		ModelBytesDao modelBytesDao = new ModelBytesDaoImpl();
		List<ModelBytes> modelBytesList = modelBytesDao.findByModelId(modelId, session);
		ModelBytes modelBytes = new ModelBytes();
		modelBytes.setCreatedAt(modelBytesList.get(0).getCreatedAt());
		modelBytes.setCreatedBy(modelBytesList.get(0).getCreatedBy());
		modelBytes.setUpdatedAt(modelBytesList.get(0).getUpdatedAt());
		modelBytes.setUpdatedBy(modelBytesList.get(0).getUpdatedBy());
		modelBytes.setModel(modelBytesList.get(0).getModel());
		modelBytes.setId(modelBytesList.get(0).getId());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		for (int i = 0; i < modelBytesList.size(); i++) {
			try {
				outputStream.write( modelBytesList.get(i).getBytes() );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		modelBytes.setBytes(outputStream.toByteArray());
		t.rollback();
		return modelBytes;
	}

	@Override
	public ModelBytes create(ModelBytes modelBytes) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelBytes, session);
	}

	@Override
	public ModelBytes create(ModelBytes modelBytes, Session session) throws ConstraintViolationException {
		byte[] bytes = modelBytes.getBytes();
		
		
		List<byte[]> partitions = divideArray(bytes, chunkSize);
		
		
		Transaction t = session.beginTransaction();
		for (int i = 0; i < partitions.size(); i++) {
			ModelBytes modelBytesPartitioned = new ModelBytes(modelBytes.getModel(), partitions.get(i), modelBytes.getCreatedBy());

			Set<ConstraintViolation<ModelBytes>> violations = validator.validate(modelBytesPartitioned);
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}
		
			try {
				session.save(modelBytesPartitioned);
				session.flush();
				session.clear();
//				session.refresh(modelBytesPartitioned);
			} catch (org.hibernate.exception.ConstraintViolationException e) {
				t.rollback();
				throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
			}
			}
		t.commit();
		return modelBytes;
		}

	@Override
	public void delete(ModelBytes modelBytes) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		delete(modelBytes, session);
	}

	@Override
	public void delete(ModelBytes modelBytes, Session session) {
		if (modelBytes.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.delete(modelBytes);
		session.flush();
		t.commit();
	}
	
	public static List<byte[]> divideArray(byte[] source, int chunksize) {
		
		System.out.println("Size of model bytes="+source.length);
		
		List <byte[]> ret=new ArrayList<>();
		
		if (source.length<chunksize) {
			ret.add(new byte[source.length]);
		} else {
			
			int numPieces=(int) Math.ceil(source.length / (double) chunksize);
			
			for (int i=0;i<numPieces-1;i++) {
				ret.add(new byte[chunksize]);	
			}
			
			int remainingBytesLength=source.length-(numPieces-1)*chunksize;
			
			ret.add(new byte[remainingBytesLength]);
				
		}
		
		int totalCount=0;
		
		for (byte[] bytes:ret) {
			totalCount+=bytes.length;
		}

		if(totalCount!=source.length) {
			System.out.println("byte length mismatch:"+totalCount+"\t"+source.length);
			return null;
		}
		
		
        int start = 0;
        int parts = 0;

        for (int i = 0; i < ret.size(); i++) {
            if (start + chunksize > source.length) {
                System.arraycopy(source, start, ret.get(i), 0, source.length - start);
            } else {
                System.arraycopy(source, start, ret.get(i), 0, chunksize);
            }
            start += chunksize;
            parts++;
        }

        System.out.println("Parts" + parts + "");

        return ret;
    }


}
