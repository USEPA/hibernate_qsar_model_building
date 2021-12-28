package gov.epa.databases.dev_qsar.qsar_models.entity;

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
@Table(name="predictions", uniqueConstraints={@UniqueConstraint(columnNames = {"canon_qsar_smiles", "fk_model_id"})})
public class Prediction {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message="Canonical QSAR-ready SMILES required")
	@Column(name="canon_qsar_smiles")
	private String canonQsarSmiles;
	
	@NotNull(message="Model required")
	@ManyToOne
	@JoinColumn(name="fk_model_id")
	private Model model;
	
	@NotNull(message="Predicted value required")
	@Column(name="qsar_predicted_value")
	private Double qsarPredictedValue;
	
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
	
	public Prediction() {}
	
	public Prediction(String canonQsarSmiles, Model model, Double qsarPredictedValue, String createdBy) {
		this.setCanonQsarSmiles(canonQsarSmiles);
		this.setModel(model);
		this.setQsarPredictedValue(qsarPredictedValue);
		this.setCreatedBy(createdBy);
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

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Double getQsarPredictedValue() {
		return qsarPredictedValue;
	}

	public void setQsarPredictedValue(Double qsarPredictedValue) {
		this.qsarPredictedValue = qsarPredictedValue;
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
}
