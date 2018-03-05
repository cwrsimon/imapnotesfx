package de.wesim.imapnotes.models;

import javax.mail.Message;

// TODO Factory Methoden anlegen !!!
public class Note {

	private String uuid;
	private String subject;
	private String content;
	private boolean isFolder;
	
	// FIXME
	private Object imapMessage;
	
	public Note(String uuid) {
		this.uuid = uuid;
	}
	
	public String getUuid() {
		return uuid;
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

	public Object getRawImapMessage() {
		return imapMessage;
	}
	
	public Message getImapMessage() {
		return (Message) imapMessage;
	}

	public void setImapMessage(Object imapMessage) {
		this.imapMessage = imapMessage;
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
		this.isFolder = true;
	}
	
	public boolean isFolder() {
		return this.isFolder;
	}
	
	
}