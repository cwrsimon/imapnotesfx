package de.wesim.imapnotes.services;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Configuration;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Set;

public class ConfigurationService {

    public static Configuration readConfig() {

        final Configuration newConfig = new Configuration();

        Properties imapSettings = new Properties();
		try {
			imapSettings.load(Files.newBufferedReader(Consts.USER_CONFIGURATION_FILE));
		} catch (IOException e) {
			// TODO Auto-generated catch block
            e.printStackTrace();
            return newConfig;
        }
        Account account;
        String currentCounter = "";
        Set<String> propertyNames = imapSettings.stringPropertyNames();
        for (String propertyName : propertyNames) {
            final String[] items = propertyName.split(".");
            if (items.length != 2) {
                // TODO Log error
                continue;
            }
        }
        return newConfig;
    }

}