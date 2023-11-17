package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;



@Entity
@Table(name="qsar_predicted_properties", uniqueConstraints={@UniqueConstraint(columnNames = {"dtxcid", "canon_qsar_smiles","fk_model_id"})})
public class QsarPredictedProperty {
	
	public QsarPredictedProperty() {}	

	public QsarPredictedProperty(String canonQsarSmiles, String dtxcid, Model model, Double resultValue,
			String resultError, String resultText, String createdBy) {
		this.canonQsarSmiles=canonQsarSmiles;
		this.dtxcid=dtxcid;
		this.model=model;
		this.resultValue=resultValue;
		this.resultError=resultError;
		this.resultText=resultText;
		this.createdBy=createdBy;
		this.updatedBy=createdBy;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message="Canonical QSAR-ready SMILES required")
	@Column(name="canon_qsar_smiles")
	private String canonQsarSmiles;

	
	@NotBlank(message="DTXCID required")
	@Column(name="dtxcid")
	private String dtxcid;

	
	@NotNull(message="Model required")
	@ManyToOne
	@JoinColumn(name="fk_model_id")
	private Model model;
		
	@OneToMany(mappedBy="qsarPredictedProperty", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<QsarPredictedNeighbor> qsarPredictedNeighbors;

	@OneToMany(mappedBy="qsarPredictedProperty", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<QsarPredictedADEstimate> qsarPredictedADEstimate;
	
	@Column(name="result_value")
	private Double resultValue;

	@Column(name="result_error")
	private String resultError;

	@Column(name="result_text")
	private String resultText;
	
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

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public List<QsarPredictedNeighbor> getQsarPredictedNeighbors() {
		return qsarPredictedNeighbors;
	}

	public void setQsarPredictedNeighbors(List<QsarPredictedNeighbor> qsarPredictedNeighbors) {
		this.qsarPredictedNeighbors = qsarPredictedNeighbors;
	}

	public List<QsarPredictedADEstimate> getQsarPredictedADEstimate() {
		return qsarPredictedADEstimate;
	}

	public void setQsarPredictedADEstimate(List<QsarPredictedADEstimate> qsarPredictedADEstimate) {
		this.qsarPredictedADEstimate = qsarPredictedADEstimate;
	}

	public Double getResultValue() {
		return resultValue;
	}

	public void setResultValue(Double resultValue) {
		this.resultValue = resultValue;
	}

	public String getResultError() {
		return resultError;
	}

	public void setResultError(String resultError) {
		this.resultError = resultError;
	}

	public String getResultText() {
		return resultText;
	}

	public void setResultText(String resultText) {
		this.resultText = resultText;
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
