package gov.epa.databases.dev_qsar;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

//import jakarta.validation.Validation;
//import jakarta.validation.Validator;
//import jakarta.validation.ValidatorFactory;

public class DevQsarValidator {
	
	public static Validator getValidator() {
	    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    return factory.getValidator();
    }

}
