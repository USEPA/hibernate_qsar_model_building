package gov.epa.databases.dev_qsar.qsar_datasets.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;

@Entity
@Table(name="units", indexes={@Index(name="unit_name_idx", columnList="name", unique=true)})
public class Unit {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull(message="Name required")
	@Column(name="name", unique=true)
	private String name;
	
	@Column(name="abbreviation")
	private String abbreviation;

	@Column(name="abbreviation_ccd")
	private String abbreviation_ccd;

	
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
	
	@NotNull(message="Creator required")
	@Column(name="created_by")
	private String createdBy;
	
	public Unit() {}
	
	public Unit(String name, String abbreviation, String createdBy) {
		this.setName(name);
		this.setAbbreviation(abbreviation);
		this.setCreatedBy(createdBy);
	}
	
	public static Unit fromExpPropUnit(ExpPropUnit expPropUnit, String lanId) {
		return new Unit(expPropUnit.getName(), expPropUnit.getAbbreviation(), lanId);
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

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
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

	public String getAbbreviation_ccd() {
		return abbreviation_ccd;
	}

	public void setAbbreviation_ccd(String abbreviation_ccd) {
		this.abbreviation_ccd = abbreviation_ccd;
	}
}
