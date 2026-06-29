package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class PropertyServiceImplSql implements PropertyService {

	public PropertyServiceImplSql() {
	}

//	
	@Override
	public List<Property> findAll() {

		String sql = "select id, name, description, abbreviation, name_ccd from qsar_datasets.properties p";
//		System.out.println(sql);
		try {

			ResultSet rs = new SqlUtilities().runSQL2(SqlUtilities.getConnectionPostgres(), sql);
			List<Property> properties = new ArrayList<>();
			while (rs.next()) {
				Property property = getPropertyFromRow(rs);
				properties.add(property);
//				System.out.println(JsonUtilities.gson.toJson(property));
			}
			return properties;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

private Property getPropertyFromRow(ResultSet rs) throws SQLException {
	
	Property property = new Property();
	
	int col=1;

	property.setId(rs.getLong(col++));
	property.setName(rs.getString(col++));
	property.setDescription(rs.getString(col++));
	property.setAbbreviation(rs.getString(col++));
	property.setName_ccd(rs.getString(col++));
	
	//TODO store rest of fields in table
	
	return property;
}

	@Override
	public Property findByName(String propertyName) {
		String sql = "select id, name, description, abbreviation, name_ccd from qsar_datasets.properties p where name = '"+propertyName+"'";

		ResultSet rs = new SqlUtilities().runSQL2(SqlUtilities.getConnectionPostgres(), sql);

		try {
			if (rs.next()) {
				return getPropertyFromRow(rs);
			}
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public Property findByName(String propertyName, Session session) {
		System.out.println("findByName(Session session) is N/A for sql based service");
		return null;
	}

	@Override
	public Property create(Property property) throws ConstraintViolationException {
		System.out.println("need to implement create(Property property) method for sql based service");
		return null;
	}

	@Override
	public Property create(Property property, Session session) throws ConstraintViolationException {
		System.out.println("create(Property property, Session session) is N/A for sql based service");
		return null;
	}

	@Override
	public List<Property> findAll(Session session) {
		System.out.println("findAll(Session session) is N/A for sql based service");
		return null;
	}

}
