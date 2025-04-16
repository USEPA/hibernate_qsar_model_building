package gov.epa.databases.dev_qsar.exp_prop.entity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
//import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name="parameter_values", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_parameter_id", "fk_property_value_id"})})
public class ParameterValue {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message="Property value required to add parameter value")
	@ManyToOne
	@JoinColumn(name="fk_property_value_id")
	@JsonBackReference
	private PropertyValue propertyValue;
	
	@NotNull(message="Parameter required to add parameter value")
	@ManyToOne
	@JoinColumn(name="fk_parameter_id")
	private Parameter parameter;
	
	@NotNull(message="Unit required to add parameter value")
	@ManyToOne
	@JoinColumn(name="fk_unit_id")
	private ExpPropUnit unit;
	
	@Column(name="value_qualifier")
	private String valueQualifier;
	
	@Column(name="value_point_estimate")
	private Double valuePointEstimate;
	
	@Column(name="value_min")
	private Double valueMin;
	
	@Column(name="value_max")
	private Double valueMax;
	
	@Column(name="value_error")
	private Double valueError;
	
	@Column(name="value_text")
	private String valueText;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotNull(message="ParameterValue creator required")
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(name="updated_by")
	private String updatedBy;
	
	public ParameterValue() {}
	
	public String generateConciseValueString() {
		if (valueText!=null) {
			return valueText;
		}
		
		if (valuePointEstimate!=null) {
			String qual = valueQualifier==null ? "" : valueQualifier;
			String error = valueError==null ? "" : ("+/-" + String.valueOf(valueError));
			return qual + String.valueOf(valuePointEstimate) + error;
		}
		
		if (valueMin!=null || valueMax!=null) {
			return String.valueOf(valueMin) + "-" + String.valueOf(valueMax);
		}
		
		return null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PropertyValue getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(PropertyValue propertyValue) {
		this.propertyValue = propertyValue;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public ExpPropUnit getUnit() {
		return unit;
	}

	public void setUnit(ExpPropUnit unit) {
		this.unit = unit;
	}

	public Double getValuePointEstimate() {
		return valuePointEstimate;
	}

	public void setValuePointEstimate(Double valuePointEstimate) {
		this.valuePointEstimate = valuePointEstimate;
	}

	public Double getValueMin() {
		return valueMin;
	}

	public void setValueMin(Double valueMin) {
		this.valueMin = valueMin;
	}

	public Double getValueMax() {
		return valueMax;
	}

	public void setValueMax(Double valueMax) {
		this.valueMax = valueMax;
	}

	public Double getValueError() {
		return valueError;
	}

	public void setValueError(Double valueError) {
		this.valueError = valueError;
	}

	public String getValueText() {
		return valueText;
	}

	public void setValueText(String valueText) {
		this.valueText = valueText;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getValueQualifier() {
		return valueQualifier;
	}

	public void setValueQualifier(String valueQualifier) {
		this.valueQualifier = valueQualifier;
	}
	
	public static String getFormattedValue(Double dvalue,int nsig) {

		if(dvalue==null) {
			return "N/A";
		}
		DecimalFormat dfSci=new DecimalFormat("0.00E00");
		DecimalFormat dfInt=new DecimalFormat("0");
		try {
			if(dvalue!=0 && (Math.abs(dvalue)<0.01 || Math.abs(dvalue)>1e3)) {
				return dfSci.format(dvalue);
			}
//			System.out.println(dvalue+"\t"+setSignificantDigits(dvalue, nsig));
			return setSignificantDigits(dvalue, nsig);
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static String setSignificantDigits(double value, int significantDigits) {
	    if (significantDigits < 0) throw new IllegalArgumentException();

	    // this is more precise than simply doing "new BigDecimal(value);"
	    BigDecimal bd = new BigDecimal(value, MathContext.DECIMAL64);
	    bd = bd.round(new MathContext(significantDigits, RoundingMode.HALF_UP));
	    final int precision = bd.precision();
	    if (precision < significantDigits)
	    bd = bd.setScale(bd.scale() + (significantDigits-precision));
	    return bd.toPlainString();
	}  
	
	@Override
	public String toString() {
		
		if(getUnit()==null) {
			return toStringNoUnits();
		} else {
			return toStringWithUnits();
		}
	}
	
	
	public String toStringWithUnits() {
		
		int n=3;
		String pointEstimate=getFormattedValue(valuePointEstimate,n);
		String strValMin=getFormattedValue(valueMin,n);
		String strValMax=getFormattedValue(valueMax,n);

		String unitAbbreviation=unit.getAbbreviation();
		
		if(valuePointEstimate!=null) {
			if(valueQualifier!=null) {
				return valueQualifier+" "+pointEstimate+" "+unitAbbreviation;
			} else {
				return pointEstimate+" "+unitAbbreviation;
			}
		} else if (valueMin!=null && valueMax!=null) {
			return strValMin+ " "+unitAbbreviation+" < value < " +strValMax+ " "+unitAbbreviation;
		} else if (valueMin!=null) {
			return " > "+strValMin+" "+unitAbbreviation;
		} else if (valueMax!=null) {
			return " < "+strValMax+" "+unitAbbreviation;	
		} else {
			return null;
		}
	}

	
	public String toStringNoUnits() {
		
		int n=3;
		String pointEstimate=getFormattedValue(valuePointEstimate,n);
		String strValMin=getFormattedValue(valueMin,n);
		String strValMax=getFormattedValue(valueMax,n);

		if(valuePointEstimate!=null) {
			if(valueQualifier!=null) {
				return valueQualifier+" "+pointEstimate;
			} else {
				return pointEstimate;
			}
		} else if (valueMin!=null && valueMax!=null) {
			return strValMin+ " < value < " +strValMax;
		} else if (valueMin!=null) {
			return " > "+strValMin;
		} else if (valueMax!=null) {
			return " < "+strValMax;	
		} else {
			return null;
		}
	}
}
