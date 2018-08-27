package de.wesim.imapnotes.services;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import de.wesim.imapnotes.HasLogger;

@Component
public class I18NService implements HasLogger {


	private ResourceBundle bundle;

	public I18NService() {
		getLogger().info("Default Locale: {}", Locale.getDefault().toString());
		this.bundle = ResourceBundle.getBundle("LabelsBundle");
	}
	
	@PostConstruct
	public void init() {
		
	}
	
	// https://docs.oracle.com/javase/tutorial/i18n/format/messageFormat.html
	public String getTranslation(String key) {
		return this.bundle.getString(key);
	}
	
}
