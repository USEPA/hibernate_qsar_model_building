package gov.epa.databases.dev_qsar;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class DevQsarValidator {
	
	public static Validator getValidator() {
	    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    return factory.getValidator();
    }

}
