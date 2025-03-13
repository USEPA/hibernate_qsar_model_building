package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
* @author TMARTI02
*/


import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "prediction_reports", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_predictions_dashboard_id"})})

public class PredictionReport {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message="PredictionDashboard required")
	@OneToOne
	@JoinColumn(name="fk_predictions_dashboard_id")
	private PredictionDashboard predictionDashboard;
	
//	@Column(name="file", length=32767)
//	private byte[] file;
	
	@Column(name="file_json")
	private byte[] fileJson;

	@Column(name="file_html")
	private byte[] fileHtml;
	
	
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
	
	public PredictionReport() {}
	
	public PredictionReport(PredictionDashboard predictionDashboard, String fileJson,String fileHtml, String createdBy) {
		this.setPredictionDashboard(predictionDashboard);
		this.predictionDashboard=predictionDashboard;
		
		if(fileJson!=null) this.fileJson=fileJson.getBytes();
		if (fileHtml!=null) this.fileHtml=fileHtml.getBytes();
		this.createdBy=createdBy;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public PredictionDashboard getPredictionDashboard() {
		return predictionDashboard;
	}

	public void setPredictionDashboard(PredictionDashboard predictionDashboard) {
		this.predictionDashboard = predictionDashboard;
	}
	
	public static byte[] compress(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            OutputStream out = new DeflaterOutputStream(baos);
            out.write(text.getBytes("ISO-8859-1"));
            out.close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return baos.toByteArray();
    }

    public static String decompress(byte[] bytes) {
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int len;
            while((len = in.read(buffer))>0)
                baos.write(buffer, 0, len);
            return new String(baos.toByteArray(), "ISO-8859-1");
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

	public byte[] getFileJson() {
		return fileJson;
	}

	public byte[] getFileHtml() {
		return fileHtml;
	}

	public void setFileJson(byte[] fileJson) {
		this.fileJson = fileJson;
	}

	public void setFileHtml(byte[] fileHtml) {
		this.fileHtml = fileHtml;
	}

}
