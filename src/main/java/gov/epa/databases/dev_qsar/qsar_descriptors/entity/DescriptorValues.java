package gov.epa.databases.dev_qsar.qsar_descriptors.entity;

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
@Table(name="descriptor_values", uniqueConstraints={@UniqueConstraint(columnNames = {"canon_qsar_smiles", "fk_descriptor_set_id"})})
public class DescriptorValues {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="canon_qsar_smiles")
	@NotNull(message="Compound required to create compound descriptors")
	private String canonQsarSmiles;

	@ManyToOne
	@JoinColumn(name="fk_descriptor_set_id")
	@NotNull(message="Descriptor set required to create compound descriptors")
	private DescriptorSet descriptorSet;
	
	@Column(length = 32767, name="values_tsv")
	private String valuesTsv;

	@Column(name="updated_by")
	private String updatedBy;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@NotBlank(message="Compound descriptor creator required")
	@Column(name="created_by")
	private String createdBy;
	
	public DescriptorValues() {}
	
	public DescriptorValues(String canonQsarSmiles, DescriptorSet descriptorSet, String valuesTsv, String createdBy) {
		this.setCanonQsarSmiles(canonQsarSmiles);
		this.setDescriptorSet(descriptorSet);
		this.setValuesTsv(valuesTsv);
		this.setCreatedBy(createdBy);
	}
	
	public void setValuesTsv(String valuesTsv) {
		this.valuesTsv = valuesTsv;
	}
	
	public String getValuesTsv() {
		return valuesTsv;
	}
	
	public void setCreatedBy(String createdBy) {
		this.createdBy=createdBy;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	public Date getUpdatedAt() {
		return updatedAt;
	}

	public DescriptorSet getDescriptorSet() {
		return descriptorSet;
	}

	public void setDescriptorSet(DescriptorSet descriptorSet) {
		this.descriptorSet = descriptorSet;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCanonQsarSmiles() {
		return canonQsarSmiles;
	}

	public void setCanonQsarSmiles(String canonQsarSmiles) {
		this.canonQsarSmiles = canonQsarSmiles;
	}
}
