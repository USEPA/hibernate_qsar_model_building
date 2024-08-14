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
//import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


/**
 * TODO create service for creating records
 * 
 * @author TMARTI02
 *
 */
@Entity
@Table(name="datasets_in_dashboard", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_property_id"})})
public class DatasetInDashboard {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull(message="Property required to set property category")
	@ManyToOne
	@JoinColumn(name="fk_property_id")
	private Property property;
	
	@NotNull(message="Category required to set property category")
	@ManyToOne
	@JoinColumn(name="fk_datasets_id")
	private Dataset dataset;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotNull(message="DatasetInDashboard creator required")
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(name="updated_by")
	private String updatedBy;
	
	public DatasetInDashboard() {}
	
	public DatasetInDashboard(Property property,Dataset dataset,String createdBy) {
		this.property=property;
		this.dataset=dataset;
		this.createdBy=createdBy;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}


	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
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
}