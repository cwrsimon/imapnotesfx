package de.wesim.imapnotes.models;

import java.io.Serializable;
import java.util.Date;

// IDee:
// Folder haben vollständige nPFad als UUID
// Stattdessen Map von Note -> Path im NoteProvider
// TODO Factory Methoden anlegen !!!
public class Note implements Serializable, Comparable<Note>  {

	private static final long serialVersionUID = 1L;
	
	private String uuid;
	private String subject;
	private String content;
	private boolean isFolder;
	
	private Date date;
		
	public Note(String uuid) {
		this.uuid = uuid;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String newUUid) {
		this.uuid = newUUid;
	}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setContent(String newContent) {
		this.content = newContent;
	}
	
	public String getContent()  {
		return content;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Note other = (Note) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	public void setIsFolder(boolean b) {
		this.isFolder = b;
	}
	
	public boolean isFolder() {
		return this.isFolder;
	}

	public void setDate(Date newDate) {
		this.date = newDate;
	}

	public Date getDate() {
		return this.date;
	}

	@Override
	public int compareTo(Note o) {
		return o.getDate().compareTo(this.getDate());
	}

	@Override
	public String toString() {
		return "Note [uuid=" + uuid + ", subject=" + subject + ", content=" + content + ", isFolder=" + isFolder
				+ ", date=" + date + "]";
	}

	
	
	
}
