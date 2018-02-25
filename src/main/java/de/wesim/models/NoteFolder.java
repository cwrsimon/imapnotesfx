package de.wesim.models;

import java.nio.file.Path;
import javax.mail.Message;

public class NoteFolder extends Note {

	private Path parentDirectory;
		
	public NoteFolder(String uuid) {
		super(uuid);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getUuid() == null) ? 0 : getUuid().hashCode());
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
		if (getUuid() == null) {
			if (other.getUuid() != null)
				return false;
		} else if (!getUuid().equals(other.getUuid()))
			return false;
		return true;
	}
	
	
	
	
}
