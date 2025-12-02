package gov.epa.databases.dev_qsar.exp_prop.entity;

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

@Entity
@Table(name="properties_in_categories", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_property_id", "fk_property_category_id"})})
public class PropertyInCategory {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull(message="Property required to set property category")
	@ManyToOne
	@JoinColumn(name="fk_property_id")
	private ExpPropProperty property;
	
	@NotNull(message="Category required to set property category")
	@ManyToOne
	@JoinColumn(name="fk_property_category_id")
	private PropertyCategory propertyCategory;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotNull(message="PropertyInCategory creator required")
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(name="updated_by")
	private String updatedBy;
	
	public PropertyInCategory() {}

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

	public PropertyCategory getPropertyCategory() {
		return propertyCategory;
	}

	public void setPropertyCategory(PropertyCategory propertyCategory) {
		this.propertyCategory = propertyCategory;
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