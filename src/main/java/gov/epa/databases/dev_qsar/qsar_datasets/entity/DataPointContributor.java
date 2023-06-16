package gov.epa.databases.dev_qsar.qsar_datasets.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.UpdateTimestamp;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.datasets.MappedPropertyValue;

@Entity
@Table(name="data_point_contributors", uniqueConstraints={@UniqueConstraint(columnNames = {"exp_prop_property_values_id", "fk_data_point_id"})})
public class DataPointContributor {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
//	@NotFound(action=NotFoundAction.IGNORE)
	@NotNull(message="Data point required")
	@JoinColumn(name="fk_data_point_id")
	@ManyToOne
	private DataPoint dataPoint;
	
	@NotNull(message="Experimental property ID required")
	@Column(name="exp_prop_property_values_id")
	private Long exp_prop_property_values_id;
	
	@Column(name="dtxcid")
	private String dtxcid;

	@Column(name="dtxsid")
	private String dtxsid;

	
	@Column(name="smiles")
	private String smiles;

	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column(name="updated_by")
	private String updatedBy;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotBlank(message="Creator required")
	@Column(name="created_by")
	private String createdBy;
	
	@Column (name="property_value")
	private Double propertyValue;
	
	public DataPointContributor() {}
	
	public DataPointContributor(DataPoint dataPoint, MappedPropertyValue mpv, Unit finalUnit,String lanId) {
		
		this.setDataPoint(dataPoint);
		this.setExp_prop_property_values_id(mpv.propertyValue.getId());
		this.setCreatedBy(lanId);

		this.setDtxcid(mpv.dsstoxRecord.getDsstoxCompoundId());
		this.setDtxsid(mpv.dsstoxRecord.getDsstoxSubstanceId());
		this.setSmiles(mpv.dsstoxRecord.getSmiles());
		
		if (finalUnit!=null) {
			this.setPropertyValue(mpv,finalUnit);
		}
	}
	
	/**
	 * Sets propertyValue in desired units in DataPointPointContributor object/table
	 * 
	 * @param mpv
	 * @param finalUnit
	 */
	public void setPropertyValue(MappedPropertyValue mpv, Unit finalUnit) {
		
		String propertyName=mpv.propertyValue.getProperty().getName();
		
		if (finalUnit.getName().equals(mpv.qsarPropertyUnits)) {
			this.propertyValue=mpv.qsarPropertyValue;
		} else if (propertyName.equals(DevQsarConstants.WATER_SOLUBILITY)) {
			if (finalUnit.getName().equals("MOLAR")) {
				if(mpv.qsarPropertyUnits.equals("NEG_LOG_M")) {
					this.propertyValue=Math.pow(10, -mpv.qsarPropertyValue);
				}
			} 
		} else if (propertyName.equals(DevQsarConstants.HENRYS_LAW_CONSTANT)) {
			if (finalUnit.getName().equals("ATM_M3_MOL")) {
				if (mpv.qsarPropertyUnits.equals(DevQsarConstants.NEG_LOG_ATM_M3_MOL)) {
					this.propertyValue=Math.pow(10, -mpv.qsarPropertyValue);
				}
			}
		} else if (propertyName.equals(DevQsarConstants.VAPOR_PRESSURE)) {
			if (finalUnit.getName().equals("MMHG")) {
				if (mpv.qsarPropertyUnits.equals(DevQsarConstants.LOG_MMHG)) {
					this.propertyValue=Math.pow(10, mpv.qsarPropertyValue);
				}
			}
		} else {
			System.out.println("*** Need to add code to DataPointContributor.setQsarPropertyValue() to assign property value for finalUnit:"+finalUnit+",qsarPropertyUnits="+mpv.qsarPropertyUnits);
		}
		
		
		if (this.propertyValue==null) {
			System.out.println("Couldnt set propertyValue in DataPointContributor.setQsarPropertyValue() to convert "+finalUnit.getName()+" to ");
		}
		
	}
	
	
//	public void setPropertyValue(Double propertyValue) {
//		this.propertyValue = propertyValue;
//	}

	
	public DataPointContributor(DataPoint dataPoint, Long exp_prop_property_values_id, String dtxcid, String lanId) {
		this.setDataPoint(dataPoint);
		this.setExp_prop_property_values_id(exp_prop_property_values_id);
		this.setCreatedBy(lanId);
		this.setDtxcid(dtxcid);
	}
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DataPoint getDataPoint() {
		return dataPoint;
	}

	public void setDataPoint(DataPoint dataPoint) {
		this.dataPoint = dataPoint;
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

	public Long getExp_prop_property_values_id() {
		return exp_prop_property_values_id;
	}

	public void setExp_prop_property_values_id(Long exp_prop_property_values_id) {
		this.exp_prop_property_values_id = exp_prop_property_values_id;
	}

	public Double getPropertyValue() {
		return propertyValue;
	}




	public String getDtxcid() {
		return dtxcid;
	}

	public void setDtxcid(String dtxcid) {
		this.dtxcid = dtxcid;
	}

	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public String getDtxsid() {
		return dtxsid;
	}

	public void setDtxsid(String dtxsid) {
		this.dtxsid = dtxsid;
	}

}
