package de.wesim.imapnotes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HasLogger {

	public default Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}
	
	
}
