package gov.epa.databases.dev_qsar.qsar_models;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import gov.epa.run_from_java.scripts.SqlUtilities;

public class QsarModelsSession {
	private static SessionFactory sessionFactory = null;
 
    public static SessionFactory getSessionFactory() {
        if (sessionFactory==null) {
        	Configuration config = new Configuration();
        	
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.Bob.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.Config.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.Method.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.Model.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.ModelInModelSet.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.Prediction.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.Statistic.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.ModelFile.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.FileType.class);
//        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.ModelInConsensusModel.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedADEstimate.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedNeighbor.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.Source.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxSnapshot.class);
        	config.addAnnotatedClass(gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxOtherCASRN.class);

        	
        	config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        	
String host = SqlUtilities.getEnv("DEV_QSAR_HOST");
                String port = SqlUtilities.getEnv("DEV_QSAR_PORT");
                String database = SqlUtilities.getEnv("DEV_QSAR_DATABASE");
                String user = SqlUtilities.getEnv("DEV_QSAR_USER");
                String password = SqlUtilities.getEnv("DEV_QSAR_PASS");
		config.setProperty("hibernate.connection.url","jdbc:postgresql://"+host
                	+ ":" + port
                	+ "/" + database + "?currentSchema=qsar_models");
            config.setProperty("hibernate.connection.username", user);
            config.setProperty("hibernate.connection.password", password);
            
        	config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        	config.setProperty("hibernate.current_session_context_class", "thread");
        	config.setProperty("hibernate.show_sql", "false");
        	config.setProperty("hibernate.hbm2ddl.auto", "none");//was "update" but was causing errors because there were materialized views that used the fields
        	config.setProperty("hibernate.jdbc.batch_size", "50");
            
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
