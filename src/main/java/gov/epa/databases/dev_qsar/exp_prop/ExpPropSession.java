package gov.epa.databases.dev_qsar.exp_prop;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class ExpPropSession {
	private static SessionFactory sessionFactory = null;
 
    public static SessionFactory getSessionFactory() {
        if (sessionFactory==null) {
        	Configuration config = new Configuration();
        	
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.Parameter.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.ParameterAcceptableUnit.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.ParameterValue.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.Property.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableParameter.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableUnit.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.PropertyCategory.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.PropertyInCategory.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.exp_prop.entity.Unit.class);
        	
        	config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        	
        	config.setProperty("hibernate.connection.url","jdbc:postgresql://"+System.getenv("DEV_QSAR_HOST")
        	+ ":" + System.getenv("DEV_QSAR_PORT")
        	+ "/" + System.getenv("DEV_QSAR_DATABASE") + "?currentSchema=exp_prop");
            config.setProperty("hibernate.connection.username", System.getenv("DEV_QSAR_USER"));
            config.setProperty("hibernate.connection.password", System.getenv("DEV_QSAR_PASS"));
            
        	config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        	config.setProperty("hibernate.current_session_context_class", "thread");
        	config.setProperty("hibernate.show_sql", "false");
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
    
    public static void flushAndClearSession() {
    	Session session = getSessionFactory().getCurrentSession();
    	session.flush();
    	session.clear();
    }
}
