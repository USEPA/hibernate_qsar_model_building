package gov.epa.databases.dev_qsar.qsar_datasets.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
//import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="data_points_in_splittings", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_splitting_id", "fk_data_point_id"})})
public class DataPointInSplitting {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	@NotNull(message="Splitting required")
	@JoinColumn(name="fk_splitting_id")
	private Splitting splitting;
	
	@OneToOne
	@NotNull(message="Data point required")
	@JoinColumn(name="fk_data_point_id")
	private DataPoint dataPoint;
	
	@NotNull(message="Split required")
	@Column(name="split_num")
	private Integer splitNum;
	
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
	
	public DataPointInSplitting() {}
	
	public DataPointInSplitting(DataPoint dataPoint, Splitting splitting, Integer splitNum, String createdBy) {
		this.setDataPoint(dataPoint);
		this.setSplitting(splitting);
		this.setSplitNum(splitNum);
		this.setCreatedBy(createdBy);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Splitting getSplitting() {
		return splitting;
	}

	public void setSplitting(Splitting splitting) {
		this.splitting = splitting;
	}

	public DataPoint getDataPoint() {
		return dataPoint;
	}

	public void setDataPoint(DataPoint dataPoint) {
		this.dataPoint = dataPoint;
	}

	public Integer getSplitNum() {
		return splitNum;
	}

	public void setSplitNum(Integer splitNum) {
		this.splitNum = splitNum;
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
