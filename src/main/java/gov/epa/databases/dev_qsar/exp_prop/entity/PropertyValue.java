package gov.epa.databases.dev_qsar.exp_prop.entity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

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
//import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.gson.JsonObject;


@Entity
@Table(name = "property_values")
public class PropertyValue {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Source chemical required to create property value")
	@ManyToOne
	@JoinColumn(name = "fk_source_chemical_id")
	private SourceChemical sourceChemical;

	@NotNull(message = "Property required to create property value")
	@ManyToOne
	@JoinColumn(name = "fk_property_id")
	@JsonManagedReference
	private ExpPropProperty property;

	@NotNull(message = "Unit required to create property value")
	@ManyToOne
	@JoinColumn(name = "fk_unit_id")
	private ExpPropUnit unit;

	@ManyToOne
	@JoinColumn(name = "fk_public_source_id")
	@JsonManagedReference
	private PublicSource publicSource;

	@ManyToOne
	@JoinColumn(name = "fk_public_source_original_id")
	@JsonManagedReference
	private PublicSource publicSourceOriginal;

	@ManyToOne
	@JoinColumn(name = "fk_literature_source_id")
	@JsonManagedReference
	private LiteratureSource literatureSource;

//	@Column(name="id_source_database")
//	private Long id_source_database;

	// If there is a direct link to a static page with chemical information
	// available
	@Column(name = "page_url", length = 1000)
	private String pageUrl;

	@Column(name = "document_name", length = 100)
	private String documentName;

	@Column(name = "file_name", length = 100)
	private String fileName;

	@Column(name = "value_qualifier")
	private String valueQualifier;

	@Column(name = "value_point_estimate")
	private Double valuePointEstimate;

	@Column(name = "value_min")
	private Double valueMin;

	@Column(name = "value_max")
	private Double valueMax;

	@Column(name = "value_error")
	private Double valueError;

	@Column(name = "value_text")
	private String valueText;

	@Column(name = "value_original", length = 1000)
	private String valueOriginal;
	
	@Column(name = "value_original_parsed", length = 1000)
	private String valueOriginalParsed;


	@Column(name = "notes", length = 1000)
	private String notes;

	@OneToMany(mappedBy = "propertyValue", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<ParameterValue> parameterValues;

	@NotNull(message = "Keep required to create property value")
	@Column(name = "keep")
	private Boolean keep;

	@Column(name = "keep_reason")
	private String keepReason;

	@NotNull(message = "Flag required to create property value")
	@Column(name = "qc_flag")
	private Boolean qcFlag;

	@Column(name = "qc_notes")
	private String qcNotes;

	@Column(name = "created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@NotNull(message = "PropertyValue creator required")
	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(name = "updated_by")
	private String updatedBy;

	public PropertyValue() {
	}

	public String generateParameterValuesString() {

		ArrayList<String> vals = new ArrayList<>();
		DecimalFormat df1 = new DecimalFormat("0.0");

		if (getParameterValue("Temperature") != null) {
			try {
				vals.add("T = " + df1.format(getParameterValue("Temperature").getValuePointEstimate()) + " C");
			} catch (Exception ex) {
				vals.add("T = " + getParameterValue("Temperature").getValuePointEstimate() + " C");
			}
		}

		if (getParameterValue("Pressure") != null) {
			try {
				vals.add("P = " + df1.format(getParameterValue("Pressure").getValuePointEstimate()) + " mmHg");
			} catch (Exception ex) {
				vals.add("P = " + getParameterValue("Pressure").getValuePointEstimate() + " mmHg");
			}
		}

		if (getParameterValue("pH") != null) {
			try {
				vals.add("pH = " + df1.format(getParameterValue("pH").getValuePointEstimate()) + "");
			} catch (Exception ex) {
				vals.add("pH = " + getParameterValue("pH").getValuePointEstimate());
			}
		}

		String result = "";

		for (int i = 0; i < vals.size(); i++) {
			result += vals.get(i);
			if (i < vals.size() - 1)
				result += "; ";
		}

		return result;
	}

	public String generateConciseValueString() {
		if (valueText != null) {
			return valueText;
		}

		String unitAbbr = unit.getAbbreviation();
		if (unitAbbr == null) {
			unitAbbr = "";
		}

		if (valuePointEstimate != null) {
			String qual = valueQualifier == null ? "" : valueQualifier;
			String error = valueError == null ? "" : ("+/-" + String.valueOf(valueError));
			return qual + String.valueOf(valuePointEstimate) + error + " " + unitAbbr;
		}

		if (valueMin != null || valueMax != null) {
			return String.valueOf(valueMin) + "-" + String.valueOf(valueMax) + " " + unitAbbr;
		}

		return null;
	}

	public ParameterValue getParameterValue(String parameterName) {
		if (parameterValues == null || parameterValues.size() == 0) {
			return null;
		}

		for (ParameterValue pav : parameterValues) {
			if (pav.getParameter().getName().equals(parameterName)) {
				return pav;
			}
		}

		return null;
	}

	/**
	 * Used to try to remove duplicates from chemprop loading
	 * 
	 * @return
	 */
	public String getKey() {

		String keyValue = valueQualifier + "\t" + valuePointEstimate + "\t" + valueMin + "\t" + valueMax + "\t"
				+ valueError + "\t" + valueText + "\t" + valueOriginal;

		// Using sourceChemical.getKey() instead of its id because a chemical might have
		// been accidentally mapped to a new sourceChemical rather than using an
		// existing one that was exact match

		String key = sourceChemical.getKey() + "\t" + keyValue;

		if (publicSource != null) {
			key += "\t" + publicSource.getId();
		} else {
			key += "\tnull";
		}

		if (literatureSource != null) {
			key += "\t" + literatureSource.getId();
		} else {
			key += "\tnull";
		}

		return key;
	}

	@AssertTrue(message = "Public or literature source is required")
	private boolean isPublicSourceOrLiteratureSourceExists() {
		return publicSource != null || literatureSource != null;
	}

//	public String generateExpPropId() {
//		return String.format("EXP%012d", id);
//	}

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
		if (this.parameterValues == null) {
			this.parameterValues = new ArrayList<ParameterValue>();
		}

		this.parameterValues.add(parameterValue);
	}

	public void addParameterValues(List<ParameterValue> parameterValues) {
		if (this.parameterValues == null) {
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

	public PublicSource getPublicSourceOriginal() {
		return publicSourceOriginal;
	}

	public void setPublicSourceOriginal(PublicSource publicSourceOriginal) {
		this.publicSourceOriginal = publicSourceOriginal;
	}

	/**
	 * TODO probably dont need to use get methods- access directly by variables
	 * 
	 * @return
	 */
	public JsonObject createJsonObjectFromPropertyValue() {
		JsonObject jo = new JsonObject();

		if (getCreatedAt() != null)
			jo.addProperty("createdAt", getCreatedAt().toString());

		if (getCreatedBy() != null)
			jo.addProperty("createdBy", getCreatedBy());

//				jo.addProperty("created_by", pv.getCreatedBy());
		jo.addProperty("sourceChemicalName", getSourceChemical().getSourceChemicalName());
		jo.addProperty("sourceChemicalCASRN", getSourceChemical().getSourceCasrn());
		jo.addProperty("sourceChemicalDTXSID", getSourceChemical().getSourceDtxsid());
		jo.addProperty("sourceChemicalDTXRID", getSourceChemical().getSourceDtxrid());
		jo.addProperty("sourceChemicalSmiles", getSourceChemical().getSourceSmiles());

		if (getParameterValues() != null) {

			for (ParameterValue parameterValue : getParameterValues()) {
				if (parameterValue.getValueText() != null) {
					jo.addProperty("parameter_" + parameterValue.getParameter().getName(),
							parameterValue.getValueText());
				} else if (parameterValue.getValuePointEstimate() != null) {
					jo.addProperty("parameter_" + parameterValue.getParameter().getName(),
							parameterValue.getValuePointEstimate());
				}
			}
		}

		jo.addProperty("notes", getNotes());
		jo.addProperty("page_url", getPageUrl());

		if (getPublicSource() != null) {
			jo.addProperty("publicSourceName", getPublicSource().getName());
			jo.addProperty("publicSourceURL", getPublicSource().getUrl());
		}

		if (getPublicSourceOriginal() != null) {
			jo.addProperty("publicSourceNameOriginal", getPublicSourceOriginal().getName());
			jo.addProperty("publicSourceOriginalURL", getPublicSourceOriginal().getUrl());
		}

		if (getLiteratureSource() != null) {
			jo.addProperty("literatureSourceName", getLiteratureSource().getName());
			jo.addProperty("literatureSourceCitation", getLiteratureSource().getCitation());
			jo.addProperty("literatureSourceDOI", getLiteratureSource().getDoi());
		}

		jo.addProperty("propertyName", getProperty().getName());
		
		jo.addProperty("valueOriginal", getValueOriginal());
		jo.addProperty("valueOriginalParsed", getValueOriginalParsed());
		
		jo.addProperty("valueQualifier", getValueQualifier());
		jo.addProperty("valuePointEstimate", getValuePointEstimate());
		jo.addProperty("valueText", getValueText());
		jo.addProperty("unitsAbbreviation", getUnit().getAbbreviation());
		jo.addProperty("keep", getKeep());
		jo.addProperty("keep_reason", getKeepReason());

		jo.addProperty("page_url", getPageUrl());

		return jo;
	}

	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public static void getPropertyValuesBySource(List<PropertyValue>propertyValues) {
		TreeMap <String,List<PropertyValue>>map=new TreeMap<String,List<PropertyValue>>();

		for (PropertyValue pv:propertyValues) {
			
			String source=null;
			
			if(pv.getPublicSource()!=null) {
				source=pv.getPublicSource().getName();
			} else if(pv.getLiteratureSource()!=null) {
				source=pv.getLiteratureSource().getName();
			}
			
			if(map.get(source)==null) {
				List<PropertyValue> recs=new ArrayList<PropertyValue>();
				recs.add(pv);
				map.put(source, recs);
			} else {
				List<PropertyValue> recs=map.get(source);
				recs.add(pv);
			}
		}
		
		System.out.println("\nProperty values by source:");
		for(String property:map.keySet()) {
			System.out.println(property+"\t"+map.get(property).size());
		}
		System.out.println("");
	}

	public String getValueOriginalParsed() {
		return valueOriginalParsed;
	}

	public void setValueOriginalParsed(String valueOriginalParsed) {
		this.valueOriginalParsed = valueOriginalParsed;
	}

}
