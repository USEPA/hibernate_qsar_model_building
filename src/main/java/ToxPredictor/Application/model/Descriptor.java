package ToxPredictor.Application.model;



public class Descriptor {

	String name;
	String definition;
	String value;
	String value_x_coefficient;
	String coefficient;
	String coefficientUncertainty;
	
	public String getCoefficientUncertainty() {
		return coefficientUncertainty;
	}
	public void setCoefficientUncertainty(String coefficientUncertainty) {
		this.coefficientUncertainty = coefficientUncertainty;
	}
	
	
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getCoefficient() {
		return coefficient;
	}
	public void setCoefficient(String coefficient) {
		this.coefficient = coefficient;
	}
	public String getValue_x_coefficient() {
		return value_x_coefficient;
	}
	public void setValue_x_coefficient(String value_x_coefficient) {
		this.value_x_coefficient = value_x_coefficient;
	}
	
	
}
