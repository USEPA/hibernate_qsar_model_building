package gov.epa.databases.dev_qsar.exp_prop.entity;

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

@Entity
@Table(name="properties_acceptable_units", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_property_id", "fk_unit_id"})})
public class PropertyAcceptableUnit {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull(message="Property required to set acceptable unit")
	@ManyToOne
	@JoinColumn(name="fk_property_id")
	private ExpPropProperty property;
	
	@NotNull(message="Unit required to set acceptable unit")
	@ManyToOne
	@JoinColumn(name="fk_unit_id")
	private ExpPropUnit unit;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotNull(message="PropertyAcceptableUnit creator required")
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column(name="updated_by")
	private String updatedBy;
	
	public PropertyAcceptableUnit() {}
	
	public PropertyAcceptableUnit(ExpPropProperty p, ExpPropUnit u, String createdBy) {
		this.property = p;
		this.unit = u;
		this.createdBy = createdBy;
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

	public ExpPropUnit getUnit() {
		return unit;
	}

	public void setUnit(ExpPropUnit unit) {
		this.unit = unit;
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
