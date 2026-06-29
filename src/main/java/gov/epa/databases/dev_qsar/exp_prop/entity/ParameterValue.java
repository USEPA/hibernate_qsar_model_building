package gov.epa.databases.dev_qsar.exp_prop.entity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
//import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
	private String value_qualifier;
	
	@Column(name="value_point_estimate")
	private Double value_point_estimate;
	
	@Column(name="value_min")
	private Double value_min;
	
	@Column(name="value_max")
	private Double value_max;
	
	@Column(name="value_error")
	private Double value_error;
	
	@Column(name="value_text")
	private String value_text;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date created_at;
	
	@NotNull(message="ParameterValue creator required")
	@Column(name="created_by")
	private String created_by;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated_at;

	@Column(name="updated_by")
	private String updated_by;
	
	public ParameterValue() {}
	
	public String generateConciseValueString() {
		if (value_text!=null) {
			return value_text;
		}
		
		if (value_point_estimate!=null) {
			String qual = value_qualifier==null ? "" : value_qualifier;
			String error = value_error==null ? "" : ("+/-" + String.valueOf(value_error));
			return qual + String.valueOf(value_point_estimate) + error;
		}
		
		if (value_min!=null || value_max!=null) {
			return String.valueOf(value_min) + "-" + String.valueOf(value_max);
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
		return value_point_estimate;
	}

	public void setValuePointEstimate(Double valuePointEstimate) {
		this.value_point_estimate = valuePointEstimate;
	}

	public Double getValueMin() {
		return value_min;
	}

	public void setValueMin(Double valueMin) {
		this.value_min = valueMin;
	}

	public Double getValueMax() {
		return value_max;
	}

	public void setValueMax(Double valueMax) {
		this.value_max = valueMax;
	}

	public Double getValueError() {
		return value_error;
	}

	public void setValueError(Double valueError) {
		this.value_error = valueError;
	}

	public String getValueText() {
		return value_text;
	}

	public void setValueText(String valueText) {
		this.value_text = valueText;
	}

	public Date getCreatedAt() {
		return created_at;
	}

	public void setCreatedAt(Date createdAt) {
		this.created_at = createdAt;
	}

	public String getCreatedBy() {
		return created_by;
	}

	public void setCreatedBy(String createdBy) {
		this.created_by = createdBy;
	}

	public Date getUpdatedAt() {
		return updated_at;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updated_at = updatedAt;
	}

	public String getUpdatedBy() {
		return updated_by;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updated_by = updatedBy;
	}

	public String getValueQualifier() {
		return value_qualifier;
	}

	public void setValueQualifier(String valueQualifier) {
		this.value_qualifier = valueQualifier;
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
		String pointEstimate=getFormattedValue(value_point_estimate,n);
		String strValMin=getFormattedValue(value_min,n);
		String strValMax=getFormattedValue(value_max,n);

		String unitAbbreviation=unit.getAbbreviation();
		
		if(value_point_estimate!=null) {
			if(value_qualifier!=null) {
				return value_qualifier+" "+pointEstimate+" "+unitAbbreviation;
			} else {
				return pointEstimate+" "+unitAbbreviation;
			}
		} else if (value_min!=null && value_max!=null) {
			return strValMin+ " "+unitAbbreviation+" < value < " +strValMax+ " "+unitAbbreviation;
		} else if (value_min!=null) {
			return " > "+strValMin+" "+unitAbbreviation;
		} else if (value_max!=null) {
			return " < "+strValMax+" "+unitAbbreviation;	
		} else if (value_text!=null) {
			return value_text+" "+unitAbbreviation;
		} else {
			return null;
		}
	}

	
	public String getDataType() {
		
		if(value_point_estimate !=null || value_max!=null || value_min!=null) {
			return "Double";
		} else if(value_text!=null) {
			return "String";
		} else return "Unknown";//TODO make class constructor not allow this to happen		
	}
	
	
	public String toStringNoUnits() {
		
		int n=3;
		String pointEstimate=getFormattedValue(value_point_estimate,n);
		String strValMin=getFormattedValue(value_min,n);
		String strValMax=getFormattedValue(value_max,n);

		if(value_point_estimate!=null) {
			if(value_qualifier!=null) {
				return value_qualifier+" "+pointEstimate;
			} else {
				return pointEstimate;
			}
		} else if (value_min!=null && value_max!=null) {
			return strValMin+ " < value < " +strValMax;
		} else if (value_min!=null) {
			return " > "+strValMin;
		} else if (value_max!=null) {
			return " < "+strValMax;	
		} else if (value_text!=null) {
			return value_text;
		} else {
			return null;
		}
	}
}
