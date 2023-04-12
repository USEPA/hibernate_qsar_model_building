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


@Entity()
@Table(name = "predictions_dashboard", uniqueConstraints={@UniqueConstraint(columnNames = {"canon_qsar_smiles","smiles", "dtxcid", "dtxsid", "fk_model_id"})})
public class PredictionDashboard {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="smiles")
	private String smiles;

	@NotBlank(message="Canonical QSAR-ready SMILES required")
	@Column(name="canon_qsar_smiles")
	private String canonQsarSmiles;
	
//	@NotBlank(message="dtxcid required")
	@Column(name="dtxcid")
	private String dtxcid;
	
	
//	@NotBlank(message="dtxsid required")
	@Column(name="dtxsid")
	private String dtxsid;

	@ManyToOne
	@NotNull(message="Model required")
	@JoinColumn(name="fk_model_id")
	private Model model;
	
	
	@Column(name="prediction_value")
	private Double predictionValue;
	
	@Column(name="prediction_string")
	private String predictionString;
	
	@Column(name="prediction_error")
	private String predictionError;
	
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public String getCanonQsarSmiles() {
		return canonQsarSmiles;
	}

	public void setCanonQsarSmiles(String canonQsarSmiles) {
		this.canonQsarSmiles = canonQsarSmiles;
	}

	public String getDtxcid() {
		return dtxcid;
	}

	public void setDtxcid(String dtxcid) {
		this.dtxcid = dtxcid;
	}

	public String getDtxsid() {
		return dtxsid;
	}

	public void setDtxsid(String dtxsid) {
		this.dtxsid = dtxsid;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Double getPredictionValue() {
		return predictionValue;
	}

	public void setPredictionValue(Double predictionValue) {
		this.predictionValue = predictionValue;
	}

	public String getPredictionString() {
		return predictionString;
	}

	public void setPredictionString(String predictionString) {
		this.predictionString = predictionString;
	}

	public String getPredictionError() {
		return predictionError;
	}

	public void setPredictionError(String predictionError) {
		this.predictionError = predictionError;
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
