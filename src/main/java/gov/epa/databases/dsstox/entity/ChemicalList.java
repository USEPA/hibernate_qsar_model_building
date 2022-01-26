package gov.epa.databases.dsstox.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table(name="chemical_lists")
public class ChemicalList {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="source_data_updated_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date sourceDataUpdatedAt;
	
	@Column(name="input_weighting")
	private String inputWeighting;
	
	@Column(name="list_accessibility")
	private String listAccessibility;
	
	@Column(name="curation_complete")
	private Boolean curationComplete;
	
	@Column(name="label")
	private String label;
	
	@Column(name="source_contact")
	private String sourceContact;
	
	@Column(name="source_contact_email")
	private String sourceContactEmail;
	
	@Column(name="source_reference")
	private String sourceReference;
	
	@Column(name="source_doi")
	private String sourceDoi;
	
	@Column(name="source_website")
	private String sourceUrl;
	
	@Column(name="ncct_contact")
	private String ncctContact;
	
	@Column(name="list_update_mechanism")
	private String listUpdateMechanism;

//	@ManyToOne
//	@JoinColumn(name="fk_list_type_id")
//	private ListType listTypeObj;
	
	@Column(name="list_type")
	private String listType;

	@Column(name="short_description")
	private String shortDescription;
	
	@Column(name="long_description")
	private String listDescription;

//	@Column(name="block_updates")
//	private Boolean blockUpdates;

	@Column(name="created_at")
	@Generated(value=GenerationTime.INSERT)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name="updated_at")
	@Generated(value=GenerationTime.ALWAYS)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(name="created_by")
	private String createdBy;

	@Column(name="updated_by")
	private String updatedBy;

	public ChemicalList() {
//		this.blockUpdates=false;
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

	public Date getSourceDataUpdatedAt() {
		return sourceDataUpdatedAt;
	}

	public void setSourceDataUpdatedAt(Date sourceDataUpdatedAt) {
		this.sourceDataUpdatedAt = sourceDataUpdatedAt;
	}

	public String getInputWeighting() {
		return inputWeighting;
	}

	public void setInputWeighting(String inputWeighting) {
		this.inputWeighting = inputWeighting;
	}

	public String getListAccessibility() {
		return listAccessibility;
	}

	public void setListAccessibility(String listAccessibility) {
		this.listAccessibility = listAccessibility;
	}

	public Boolean getCurationComplete() {
		return curationComplete;
	}

	public void setCurationComplete(Boolean curationComplete) {
		this.curationComplete = curationComplete;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSourceContact() {
		return sourceContact;
	}

	public void setSourceContact(String sourceContact) {
		this.sourceContact = sourceContact;
	}

	public String getSourceContactEmail() {
		return sourceContactEmail;
	}

	public void setSourceContactEmail(String sourceContactEmail) {
		this.sourceContactEmail = sourceContactEmail;
	}

	public String getSourceReference() {
		return sourceReference;
	}

	public void setSourceReference(String sourceReference) {
		this.sourceReference = sourceReference;
	}

	public String getSourceDoi() {
		return sourceDoi;
	}

	public void setSourceDoi(String sourceDoi) {
		this.sourceDoi = sourceDoi;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getNcctContact() {
		return ncctContact;
	}

	public void setNcctContact(String ncctContact) {
		this.ncctContact = ncctContact;
	}

	public String getListUpdateMechanism() {
		return listUpdateMechanism;
	}

	public void setListUpdateMechanism(String listUpdateMechanism) {
		this.listUpdateMechanism = listUpdateMechanism;
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getListDescription() {
		return listDescription;
	}

	public void setListDescription(String listDescription) {
		this.listDescription = listDescription;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

}
