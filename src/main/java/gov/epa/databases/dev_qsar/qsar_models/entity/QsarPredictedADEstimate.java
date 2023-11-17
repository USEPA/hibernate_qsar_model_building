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
@Table(name="qsar_predicted_ad_estimates", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_predictions_dashboard_id", "fk_ad_method_id"})})
public class QsarPredictedADEstimate  {

	@NotNull(message="AD Method required")
	@ManyToOne
	@JoinColumn(name="fk_ad_method_id")
	private MethodAD methodAD;
			

	@NotNull(message="applicability_value required")
	@Column(name="applicability_value")
	private Double applicabilityValue;		
	
	
//	@NotNull(message="qsar_predicted_property required")
//	@JoinColumn(name="fk_qsar_predicted_property_id")
//	@ManyToOne
//	private QsarPredictedProperty qsarPredictedProperty;
	
	
	@NotNull(message="prediction_dashboard required")
	@JoinColumn(name="fk_predictions_dashboard_id")
	@ManyToOne
	private PredictionDashboard predictionDashboard;

	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;


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
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public QsarPredictedADEstimate() {
		
	}

//	public QsarPredictedADEstimate(MethodAD methodAD,
//			Double applicabilityValue,
//			QsarPredictedProperty qsarPredictedProperty,
//			String createdBy) {
//		
//		this.methodAD = methodAD;
//		this.applicabilityValue = applicabilityValue;
//		this.qsarPredictedProperty = qsarPredictedProperty;
//		this.createdBy = createdBy;
//	}
	
	public QsarPredictedADEstimate(MethodAD methodAD,
			Double applicabilityValue,
			PredictionDashboard predictionDashboard,
			String createdBy) {
		
		this.methodAD = methodAD;
		this.applicabilityValue = applicabilityValue;
		this.predictionDashboard = predictionDashboard;
		this.createdBy = createdBy;
	}

	public MethodAD getMethodAD() {
		return methodAD;
	}

	public void setMethodAD(MethodAD methodAD) {
		this.methodAD = methodAD;
	}

	public Double getApplicabilityValue() {
		return applicabilityValue;
	}

	public void setApplicabilityValue(Double applicabilityValue) {
		this.applicabilityValue = applicabilityValue;
	}

	public PredictionDashboard getPredictionDashboard() {
		return predictionDashboard;
	}

	public void setPredictionDashboard(PredictionDashboard predictionDashboard) {
		this.predictionDashboard = predictionDashboard;
	}

	
}
