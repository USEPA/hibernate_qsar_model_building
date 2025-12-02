package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Bob;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;

public class BobServiceImpl implements BobService {
	
	@Override
	public Bob create(Bob bob) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(bob, session);
	}



	@Override
	public Bob create(Bob bob, Session session) throws ConstraintViolationException {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public List<Bob> batchCreate(List<Bob> bobs) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return batchCreate(bobs, session);

	}

	@Override
	public List<Bob> batchCreate(List<Bob> bobs, Session session) throws ConstraintViolationException {
		Transaction tx = session.beginTransaction();
		try {
		for (int i = 0; i < bobs.size(); i++) {
			Bob bob = bobs.get(i);
			session.persist(bob);
		    if ( i % 1000 == 0 ) { //20, same as the JDBC batch size
		        //flush a batch of inserts and release memory:
		        session.flush();
		        session.clear();
		    }
		}
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			tx.rollback();
		}
		
		tx.commit();
		session.close();

		return bobs;

	}
	
	
	
	


}
