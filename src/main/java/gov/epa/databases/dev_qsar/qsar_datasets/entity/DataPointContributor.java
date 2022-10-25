package gov.epa.databases.dev_qsar.qsar_datasets.entity;

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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="data_point_contributors", uniqueConstraints={@UniqueConstraint(columnNames = {"exp_prop_id", "fk_data_point_id"})})
public class DataPointContributor {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message="Data point required")
	@JoinColumn(name="fk_data_point_id")
	@ManyToOne
	private DataPoint dataPoint;
	
	@NotBlank(message="Experimental property ID required")
	@Column(name="exp_prop_id")
	private String expPropId;
	
	
	private String DTXCID;
	
	public String getDTXCID() {
		return DTXCID;
	}

	public void setDTXCID(String dTXCID) {
		DTXCID = dTXCID;
	}

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
	
	public DataPointContributor() {}
	
	public DataPointContributor(DataPoint dataPoint, String expPropId, String DTXCID, String createdBy) {
		this.setDataPoint(dataPoint);
		this.setExpPropId(expPropId);
		this.setCreatedBy(createdBy);
		this.setDTXCID(DTXCID);
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

	public String getExpPropId() {
		return expPropId;
	}

	public void setExpPropId(String expPropId) {
		this.expPropId = expPropId;
	}
}
