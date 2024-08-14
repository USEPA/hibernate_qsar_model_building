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

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.endpoints.models.ModelPrediction;



@Entity
@Table(name="predictions", uniqueConstraints={@UniqueConstraint(columnNames = {"canon_qsar_smiles", "fk_model_id","fk_splitting_id"})})
public class Prediction {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message="Canonical QSAR-ready SMILES required")
	@Column(name="canon_qsar_smiles")
	private String canonQsarSmiles;
	
	@NotNull(message="Model required")
	@ManyToOne
	@JoinColumn(name="fk_model_id")
	private Model model;
	
	@NotNull(message="Predicted value required")
	@Column(name="qsar_predicted_value")
	private Double qsarPredictedValue;
	
//	@Column(name="qsar_experimental_value")
//	private Double qsarExperimentalValue;

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
	
//	@NotNull(message="Split required") //TODO later add this when we redo it but cant set it now because have predictions with it missing
//	@Column(name="split_num")
//	private Integer splitNum;
	
//	@OneToOne
//	@NotNull(message="Splitting required")  //TODO later add this when we redo it
//	private Splitting splitting;
	@JoinColumn(name="fk_splitting_id")
	private Long fk_splitting_id;


	public Prediction() {}
	
	public Prediction(Model model,String canonQsarSmiles, Double qsarExperimentalValue,Double qsarPredictedValue, Integer splitNum, Splitting splitting, String createdBy) {
		this.setCanonQsarSmiles(canonQsarSmiles);
		this.setModel(model);
//		this.setQsarExperimentalValue(qsarExperimentalValue);
		this.setQsarPredictedValue(qsarPredictedValue);
		this.setCreatedBy(createdBy);
//		this.setSplitNum(splitNum);
		this.setFk_splitting_id(splitting.getId());
	}
	
	
	public Prediction(ModelPrediction mp, Model model, Splitting splitting, String createdBy) {
		this.setCanonQsarSmiles(mp.id);
		this.setModel(model);
//		this.setQsarExperimentalValue(mp.exp);
		this.setQsarPredictedValue(mp.pred);
		this.setCreatedBy(createdBy);
//		this.setSplitNum(mp.split);
		this.setFk_splitting_id(splitting.getId());
	}


//	public Splitting getSplitting() {
//		return splitting;
//	}
//
//	public void setSplitting(Splitting splitting) {
//		this.splitting = splitting;
//	}

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

	public Long getFk_splitting_id() {
		return fk_splitting_id;
	}

	public void setFk_splitting_id(Long fk_splitting_id) {
		this.fk_splitting_id = fk_splitting_id;
	}
	

}
