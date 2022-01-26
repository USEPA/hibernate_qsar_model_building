package gov.epa.databases.dev_qsar.qsar_datasets.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;

@Entity
@Table(name="properties", indexes={@Index(name="property_name_idx", columnList="name", unique=true)})
public class Property {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message="Name required")
	@Column(name="name", unique=true)
	private String name;
	
	@NotBlank(message="Description required")
	@Column(name="description")
	private String description;
	
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
	
	@ManyToMany(mappedBy="property", fetch=FetchType.EAGER)
	@Fetch(value=FetchMode.SUBSELECT)
	private List<PropertyInCategory> propertyInCategories;
	
	public Property() {}
	
	public Property(String name, String description, String createdBy) {
		this.setName(name);
		this.setDescription(description);
		this.setCreatedBy(createdBy);
	}
	
	public static Property fromExpPropProperty(ExpPropProperty expPropProperty, String lanId) {
		return new Property(expPropProperty.getName(), expPropProperty.getDescription(), lanId);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public List<PropertyInCategory> getPropertyInCategories() {
		return propertyInCategories;
	}

	public void setPropertyInCategories(List<PropertyInCategory> propertyInCategories) {
		this.propertyInCategories = propertyInCategories;
	}
}
