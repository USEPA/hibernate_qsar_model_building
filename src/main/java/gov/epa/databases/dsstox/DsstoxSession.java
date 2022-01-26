package gov.epa.databases.dsstox;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class DsstoxSession {
	private static SessionFactory sessionFactory = null;
 
    public static SessionFactory getSessionFactory() {
        if (sessionFactory==null) {
        	Configuration config = new Configuration();
        	
        	config.addAnnotatedClass(gov.epa.databases.dsstox.entity.DsstoxCompound.class);
        	config.addAnnotatedClass(gov.epa.databases.dsstox.entity.CompoundRelationship.class);
        	config.addAnnotatedClass(gov.epa.databases.dsstox.entity.CompoundRelationshipType.class);
        	config.addAnnotatedClass(gov.epa.databases.dsstox.entity.GenericSubstance.class);
        	config.addAnnotatedClass(gov.epa.databases.dsstox.entity.GenericSubstanceCompound.class);
        	config.addAnnotatedClass(gov.epa.databases.dsstox.entity.QcLevel.class);
        	config.addAnnotatedClass(gov.epa.databases.dsstox.entity.ChemicalList.class);
        	config.addAnnotatedClass(gov.epa.databases.dsstox.entity.SourceSubstance.class);
        	config.addAnnotatedClass(gov.epa.databases.dsstox.entity.SourceSubstanceIdentifier.class);
        	config.addAnnotatedClass(gov.epa.databases.dsstox.entity.SourceGenericSubstanceMapping.class);
        	
        	config.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        	
            config.setProperty("hibernate.connection.url","jdbc:mysql://"+System.getenv("DSSTOX_HOST")
            	+ ":" + System.getenv("DSSTOX_PORT")
            	+ "/prod_dsstox");
            config.setProperty("hibernate.connection.username", System.getenv("DSSTOX_USER"));
            config.setProperty("hibernate.connection.password", System.getenv("DSSTOX_PASS"));
            
        	config.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        	config.setProperty("hibernate.current_session_context_class", "thread");
        	config.setProperty("hibernate.show_sql", "true");
        	config.setProperty("hibernate.hbm2ddl.auto", "none");
            
            config.setProperty("hibernate.c3p0.acquire_increment", "1");
            config.setProperty("hibernate.c3p0.idle_test_period", "300"); // Must be <= timeout
//          config.setProperty("hibernate.c3p0.testConnectionOnCheckout", "true"); // Bad for performance
            config.setProperty("hibernate.c3p0.preferredTestQuery", "SELECT 1;");
            config.setProperty("hibernate.c3p0.min_size", "5");
            config.setProperty("hibernate.c3p0.max_size", "30");
            config.setProperty("hibernate.c3p0.max_statements", "50");
            config.setProperty("hibernate.c3p0.timeout", "300");
        	
        	ServiceRegistry sr = new StandardServiceRegistryBuilder().applySettings(config.getProperties()).build();

            sessionFactory = config.buildSessionFactory(sr);
        }
        
        return sessionFactory;
    }
    
    public static Session getSession() {
    	return getSessionFactory().getCurrentSession();
    }
}
