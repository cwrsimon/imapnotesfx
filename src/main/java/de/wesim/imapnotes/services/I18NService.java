package de.wesim.imapnotes.services;

import java.text.MessageFormat;
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
	
	public String getTranslation(String key) {
            final String translation = this.bundle.getString(key);
            if (translation == null) return key;
            return translation;
	}
	
	public String getFormattedMessage(String pattern, String argument) {
        return MessageFormat.format(pattern, argument);
	}

	public String getMessageAndTranslation(String key, String argument) {
        return MessageFormat.format(getTranslation(key), argument);
	}

}
