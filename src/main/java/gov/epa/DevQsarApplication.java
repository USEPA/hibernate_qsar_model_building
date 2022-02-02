package gov.epa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class DevQsarApplication {
	
	@Component
	public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
	    @Override
	    public void customize(ConfigurableWebServerFactory factory) {
	        factory.setPort(9090);
	    }
	}
	
	public static void main(String[] args) {
		try {
	        SpringApplication.run(DevQsarApplication.class, args);
	    } catch (Exception e) {
	        e.printStackTrace(); 
	    }
	}

}
