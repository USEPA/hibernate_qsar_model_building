package gov.epa.databases.dev_qsar.exp_prop.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.datasets.ExplainedResponse;

@Entity
@Table(name="property_values")
public class PropertyValue {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message="Source chemical required to create property value")
	@ManyToOne
	@JoinColumn(name="fk_source_chemical_id")
	private SourceChemical sourceChemical;
	
	@NotNull(message="Property required to create property value")
	@ManyToOne
	@JoinColumn(name="fk_property_id")
	@JsonManagedReference
	private ExpPropProperty property;
	
	@NotNull(message="Unit required to create property value")
	@ManyToOne
	@JoinColumn(name="fk_unit_id")
	private ExpPropUnit unit;
	
	@ManyToOne
	@JoinColumn(name="fk_public_source_id")
	@JsonManagedReference
	private PublicSource publicSource;
	
	@ManyToOne
	@JoinColumn(name="fk_literature_source_id")
	@JsonManagedReference
	private LiteratureSource literatureSource;
	
	// If there is a direct link to a static page with chemical information available
	@Column(name="page_url", length=1000)
	private String pageUrl;
	
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
	
	@Column(name="value_original", length=1000)
	private String valueOriginal;
	
	@Column(name="notes", length=1000)
	private String notes;
	
	@OneToMany(mappedBy="propertyValue", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private List<ParameterValue> parameterValues;
	
	@NotNull(message="Keep required to create property value")
	@Column(name="keep")
	private Boolean keep;
	
	@Column(name="keep_reason")
	private String keepReason;
	
	@NotNull(message="Flag required to create property value")
	@Column(name="qc_flag")
	private Boolean qcFlag;
	
	@Column(name="qc_notes")
	private String qcNotes;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotBlank(message="PropertyValue creator required")
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column(name="updated_by")
	private String updatedBy;
	
	public PropertyValue() {}
	
	public String generateConciseValueString() {
		if (valueText!=null) {
			return valueText;
		}
		
		String unitAbbr = unit.getAbbreviation();
		if (unitAbbr==null) { unitAbbr = ""; }
		
		if (valuePointEstimate!=null) {
			String qual = valueQualifier==null ? "" : valueQualifier;
			String error = valueError==null ? "" : ("+/-" + String.valueOf(valueError));
			return qual + String.valueOf(valuePointEstimate) + error + " " + unitAbbr;
		}
		
		if (valueMin!=null || valueMax!=null) {
			return String.valueOf(valueMin) + "-" + String.valueOf(valueMax) + " " + unitAbbr;
		}
		
		return null;
	}
	
	public ExplainedResponse validateValue() {
		Double pointEstimate = getValuePointEstimate();
		String propertyName = getProperty().getName();
		Double value = null;
		if (pointEstimate!=null) {
			// If point estimate available, set as candidate value
			value = pointEstimate;
		} else {
			// If min and max exist...
			Double max = getValueMax();
			Double min = getValueMin();
			
			if (max==null && min==null) {
				return new ExplainedResponse(false, "No numerical data");
			} else if (max==null || min==null) {
				return new ExplainedResponse(false, "Range with null max or min");
			}
		
			// ...and are within defined tolerance...
			Boolean rangeCheck = checkRangeForProperty(min, max, propertyName);
			if (rangeCheck==null || !rangeCheck) {
				return new ExplainedResponse(false, "Range width outside tolerance");
			}
		
			// ...then set mean as candidate value
			value = (min + max) / 2.0;
		}
		
		// Check if binary value is actually binary
		String unitName = getUnit().getName();
		if (unitName.equals(DevQsarConstants.BINARY) && !(value==1 || value==0)) {
			return new ExplainedResponse(false, "Binary value indicated, but property value is not binary");
		}
		
		// Check if property value (in original units) is realistic for property in question
		Boolean realisticValueCheck = checkRealisticValueForProperty(value, propertyName, unitName);
		if (realisticValueCheck==null || !realisticValueCheck) {
			return new ExplainedResponse(false, "Unrealistic value for property");
		}
		
		return new ExplainedResponse(true, value, "Convertible QSAR property value available");
	}
	
	public static Boolean checkRangeForProperty(double min, double max, String propertyName) {
		if (propertyName.equals(DevQsarConstants.PKA) 
				|| propertyName.equals(DevQsarConstants.LOG_KOW)) {
			return isRangeWithinTolerance(min, max, DevQsarConstants.LOG_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.MELTING_POINT) 
				|| propertyName.equals(DevQsarConstants.BOILING_POINT) 
				|| propertyName.equals(DevQsarConstants.FLASH_POINT)) {
			return isRangeWithinTolerance(min, max, DevQsarConstants.TEMP_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.DENSITY)) {
			return isRangeWithinTolerance(min, max, DevQsarConstants.DENSITY_RANGE_TOLERANCE);
		} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE) 
				|| propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT) 
				|| propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
			return isRangeWithinLogTolerance(min, max, DevQsarConstants.LOG_RANGE_TOLERANCE, DevQsarConstants.ZERO_TOLERANCE);
		} else {
			return null;
		}
	}
	
	private static boolean isRangeWithinTolerance(double min, double max, double rangeTolerance) {
		return max - min <= rangeTolerance;
	}
	
	private static boolean isRangeWithinLogTolerance(double min, double max, double logRangeTolerance, double zeroTolerance) {
		if (Math.abs(min) > zeroTolerance) {
			return Math.log10(max / min) <= logRangeTolerance;
		} else {
			return false;
		}
	}
	
	// Check if property value (in original units) is realistic for property in question
	public static Boolean checkRealisticValueForProperty(Double candidateValue, String propertyName, String unitName) {
		if (propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
			if (candidateValue <= 0.0
					|| (unitName.equals(DevQsarConstants.G_L) && candidateValue > DevQsarConstants.MAX_WATER_SOLUBILITY)) {
				return false;
			} else {
				return true;
			}
		} else if (propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
			return candidateValue > 0.0;
		} else {
			// TBD
			return true;
		}
	}
	
	public ParameterValue getParameterValue(String parameterName) {
		if (parameterValues==null || parameterValues.size()==0) {
			return null;
		}
		
		for (ParameterValue pav:parameterValues) {
			if (pav.getParameter().getName().equals(parameterName)) {
				return pav;
			}
		}
		
		return null;
	}
	
	@AssertTrue(message = "Public or literature source is required")
	private boolean isPublicSourceOrLiteratureSourceExists() {
	    return publicSource != null || literatureSource != null;
	}
	
	public String generateExpPropId() {
		return String.format("EXP%012d", id);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ExpPropProperty getProperty() {
		return property;
	}

	public void setProperty(ExpPropProperty property) {
		this.property = property;
	}

	public List<ParameterValue> getParameterValues() {
		return parameterValues;
	}

	public void setParameterValues(List<ParameterValue> parameterValues) {
		this.parameterValues = parameterValues;
	}
	
	public void addParameterValue(ParameterValue parameterValue) {
		if (this.parameterValues==null) {
			this.parameterValues = new ArrayList<ParameterValue>();
		}
		
		this.parameterValues.add(parameterValue);
	}
	
	public void addParameterValues(List<ParameterValue> parameterValues) {
		if (this.parameterValues==null) {
			this.parameterValues = new ArrayList<ParameterValue>();
		}
		
		this.parameterValues.addAll(parameterValues);
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

	public String getValueNotes() {
		return getNotes();
	}

	public void setValueNotes(String valueNotes) {
		this.setNotes(valueNotes);
	}

	public boolean isKeep() {
		return getKeep();
	}

	public void setKeep(boolean keep) {
		this.keep = keep;
	}

	public String getKeepReason() {
		return keepReason;
	}

	public void setKeepReason(String keepNotes) {
		this.keepReason = keepNotes;
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

	public String getValueOriginal() {
		return valueOriginal;
	}

	public void setValueOriginal(String valueOriginal) {
		this.valueOriginal = valueOriginal;
	}

	public Boolean getKeep() {
		return keep;
	}

	public void setKeep(Boolean keep) {
		this.keep = keep;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getValueQualifier() {
		return valueQualifier;
	}

	public void setValueQualifier(String valueQualifier) {
		this.valueQualifier = valueQualifier;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public SourceChemical getSourceChemical() {
		return sourceChemical;
	}

	public void setSourceChemical(SourceChemical sourceChemical) {
		this.sourceChemical = sourceChemical;
	}

	public PublicSource getPublicSource() {
		return publicSource;
	}

	public void setPublicSource(PublicSource publicSource) {
		this.publicSource = publicSource;
	}

	public LiteratureSource getLiteratureSource() {
		return literatureSource;
	}

	public void setLiteratureSource(LiteratureSource literatureSource) {
		this.literatureSource = literatureSource;
	}

	public Boolean getQcFlag() {
		return qcFlag;
	}

	public void setQcFlag(Boolean qcFlag) {
		this.qcFlag = qcFlag;
	}

	public String getQcNotes() {
		return qcNotes;
	}

	public void setQcNotes(String qcNotes) {
		this.qcNotes = qcNotes;
	}
}
