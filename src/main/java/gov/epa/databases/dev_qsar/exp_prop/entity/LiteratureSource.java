package gov.epa.databases.dev_qsar.exp_prop.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name="literature_sources", indexes={@Index(name="litsrc_citation_idx", columnList="citation", unique=true)})
public class LiteratureSource {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
//	@NotNull(message="Source name required")
//	@Column(name="name", unique=true)
	@Column(name="name")//only citation needs to be unique, sometimes hard to set a unique name when only citation is provided
	private String name;
	
	// Exact fields TBD
	@Column(name="title")
	private String title;
	
	@Column(name="author")
	private String author;
	
	@NotNull(message="Citation required")
	@Column(name="citation", length=1000, unique=true)
	private String citation;


	@Column(name="journal")
	private String journal;

	@Column(name="year")
	private String year;

	
	@Column(name="volume")
	private String volume;

	@Column(name="issue")
	private String issue;

	@Column(name="pages")
	private String pages;

//	@Column(name="document_name")
//	public String documentName;
	
	@Column(name="url")
	private String url;
	
	@Column(name="doi")
	private String doi;
	
	@Column(name="notes")
	private String notes;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotNull(message="Creator name required")
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column(name="updated_by")
	private String updatedBy;
	
	@OneToMany(mappedBy="literatureSource", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
	@JsonBackReference
	private transient List<PropertyValue> propertyValues;
	
	public LiteratureSource() {}
	
	public LiteratureSource(String name, String citation, String createdBy) {
		this.name = name;
		this.citation = citation;
		this.createdBy = createdBy;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}
	
	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.doi = notes;
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

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getJournal() {
		return journal;
	}

	public void setJournal(String journal) {
		this.journal = journal;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public String getCitation() {
		return citation;
	}

	public void setCitation(String citation) {
		this.citation = citation;
	}

//	public String getDocumentName() {
//		return documentName;
//	}
//
//	public void setDocumentName(String documentName) {
//		this.documentName = documentName;
//	}

}
