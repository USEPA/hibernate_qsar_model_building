package gov.epa.databases.dev_qsar.exp_prop.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="properties", indexes={@Index(name="property_name_idx", columnList="name", unique=true)})
public class Property {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull(message="Property name required")
	@Column(name="name", unique=true)
	private String name;
	
	@Column(name="description", length=1000)
	private String description;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotNull(message="Property creator required")
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column(name="updated_by")
	private String updatedBy;
	
	@OneToMany(mappedBy="property", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
	private List<PropertyValue> propertyValues;
	
	@ManyToMany(mappedBy="property", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<PropertyInCategory> propertiesInCategories;
	
	@ManyToMany(mappedBy="property", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<PropertyAcceptableParameter> propertiesAcceptableParameters;
	
	@ManyToMany(mappedBy="property", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<PropertyAcceptableUnit> propertiesAcceptableUnits;
	
	public Property() {}

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

	public List<PropertyValue> getPropertyValues() {
		return propertyValues;
	}
	
	

}
