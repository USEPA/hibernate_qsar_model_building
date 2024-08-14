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
@Table(name="models_in_consensus_models", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_consensus_model_id", "fk_model_id"})})
public class ModelInConsensusModel {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message="Consensus model required")
	@ManyToOne
	@JoinColumn(name="fk_consensus_model_id")
	private Model consensusModel;
	
	@NotNull(message="Model required")
	@ManyToOne
	@JoinColumn(name="fk_model_id")
	private Model model;
	
	@Column(name="model_weight")
	private Double weight;
	
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
	
	public ModelInConsensusModel() {}
	
	public ModelInConsensusModel(Model model, Model consensusModel, Double weight, String createdBy) {
		this.model = model;
		this.consensusModel = consensusModel;
		this.weight = weight;
		this.createdBy = createdBy;
	}
	
	public ModelInConsensusModel(Model model, Model consensusModel, String createdBy) {
		this.model = model;
		this.consensusModel = consensusModel;
		this.createdBy = createdBy;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Model getConsensusModel() {
		return consensusModel;
	}

	public void setConsensusModel(Model consensusModel) {
		this.consensusModel = consensusModel;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
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
