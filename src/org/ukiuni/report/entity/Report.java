package org.ukiuni.report.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
@Table(indexes = { @Index(columnList = "key", unique = true) })
public class Report {
	public static String STATUS_PUBLISHED = "published";
	public static String STATUS_PRIVATE = "private";
	public static String STATUS_DRAFT = "draft";
	public static String STATUS_DELETED = "deleted";
	@EmbeddedId
	private ReportPK pk;
	@ManyToOne
	private Account account;
	@Column(columnDefinition = "TEXT")
	private String title;
	@Column(columnDefinition = "TEXT")
	private String content;
	@Column(nullable = false, unique = true)
	private String key;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	private String status;

	public ReportPK getPk() {
		return pk;
	}

	public void setPk(ReportPK pk) {
		this.pk = pk;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	@Override
	public int hashCode() {
		return new Long(pk.id).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Report && ((Report) obj).getPk().getId() == pk.id);
	}

	@SuppressWarnings("serial")
	@Embeddable
	public static class ReportPK implements Serializable {

		@GeneratedValue
		private long id;

		@Version
		private long version;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public long getVersion() {
			return version;
		}

		public void setVersion(long version) {
			this.version = version;
		}
	}
}
