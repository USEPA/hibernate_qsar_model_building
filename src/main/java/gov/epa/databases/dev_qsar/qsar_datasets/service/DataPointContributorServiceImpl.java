package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.run_from_java.scripts.SqlUtilities;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public class DataPointContributorServiceImpl implements DataPointContributorService {

	private Validator validator;

	public DataPointContributorServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	@Override
	public DataPointContributor create(DataPointContributor dataPointContributor) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(dataPointContributor, session);
	}

	@Override
	public DataPointContributor create(DataPointContributor dataPointContributor, Session session)
			throws ConstraintViolationException {
		Set<ConstraintViolation<DataPointContributor>> violations = validator.validate(dataPointContributor);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}

		Transaction t = session.beginTransaction();

		try {
			session.persist(dataPointContributor);
			session.flush();
//			session.refresh(dataPointContributor);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}

		return dataPointContributor;
	}

	@Override
	public List<DataPointContributor> createBatch(List<DataPointContributor> dataPointContributors)
			throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return createBatch(dataPointContributors, session);
	}

	@Override
	public List<DataPointContributor> createBatch(List<DataPointContributor> dataPointContributors, Session session)
			throws ConstraintViolationException {
		Transaction tx = session.beginTransaction();
		try {
			for (int i = 0; i < dataPointContributors.size(); i++) {
				DataPointContributor dataPointContributor = dataPointContributors.get(i);
				session.persist(dataPointContributor);
				if (i % 1000 == 0) { // 50, same as the JDBC batch size
					// flush a batch of inserts and release memory:
					session.flush();
					session.clear();
				}
			}
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			tx.rollback();
		}

		tx.commit();
		session.close();
		return dataPointContributors;
	}

	/**
	 * This runs much faster. Only down side is need to do sql query to get
	 * datapoint id if they were created using sql
	 *
	 * @Override
	 */
	public void createBatchSQL(Dataset dataset, List<DataPointContributor> dataPointContributors) {

		Connection conn = SqlUtilities.getConnectionPostgres();

		String sqlDatapoints = "select canon_qsar_smiles, id from qsar_datasets.data_points dp \r\n"
				+ "where dp.fk_dataset_id=" + dataset.getId() + ";";

		ResultSet rs = SqlUtilities.runSQL2(conn, sqlDatapoints);

		HashMap<String, Long> hmSmilesToID = new HashMap<>();

		try {
			while (rs.next()) {
				hmSmilesToID.put(rs.getString(1), rs.getLong(2));
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		String[] fieldNames = { "fk_data_point_id", "exp_prop_property_values_id", "dtxcid", "dtxsid", "smiles",
				"property_value", "created_by", "created_at" };
		int batchSize = 1000;

		String sql = "INSERT INTO qsar_datasets.data_point_contributors (";

		for (int i = 0; i < fieldNames.length; i++) {
			sql += fieldNames[i];
			if (i < fieldNames.length - 1)
				sql += ",";
			else
				sql += ") VALUES (";
		}

		for (int i = 0; i < fieldNames.length - 1; i++) {
			sql += "?";
			if (i < fieldNames.length - 1)
				sql += ",";
		}
		sql += "current_timestamp)";
//		System.out.println(sql);

		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(sql);
			prep.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			long t1 = System.currentTimeMillis();

			for (int counter = 0; counter < dataPointContributors.size(); counter++) {
				DataPointContributor dpc = dataPointContributors.get(counter);

				String canon_smiles = dpc.getDataPoint().getCanonQsarSmiles();
				Long id_datapoint = hmSmilesToID.get(canon_smiles);

				prep.setLong(1, id_datapoint);
				prep.setLong(2, dpc.getExp_prop_property_values_id());
				prep.setString(3, dpc.getDtxcid());
				prep.setString(4, dpc.getDtxsid());
				prep.setString(5, dpc.getSmiles());
				prep.setDouble(6, dpc.getPropertyValue());
				prep.setString(7, dpc.getCreatedBy());
				prep.addBatch();

				if (counter % batchSize == 0 && counter != 0) {
					// System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2 = System.currentTimeMillis();
			System.out.println("time to post " + dataPointContributors.size()
					+ " dataPointContributors using batchsize=" + batchSize + ":\t" + (t2 - t1) / 1000.0 + " seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
