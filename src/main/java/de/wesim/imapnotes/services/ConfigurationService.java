package de.wesim.imapnotes.services;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.models.Configuration;

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
        Map<String, Account> accounts = new LinkedHashMap<>();
        Set<String> propertyNames = imapSettings.stringPropertyNames();
        for (String propertyName : propertyNames) {
        	System.out.println("propertyName:" + propertyName);
            final String[] items = propertyName.split("\\.");
            if (items.length != 2) {
                // TODO Log error
                continue;
            }

            String key = items[1];
        	System.out.println("key:" + key);

            Account acc = accounts.get(key);
            if (acc == null) {
            	acc = new Account();
            	accounts.put(key, acc);
            }
			final String propertyValue = imapSettings.getProperty(propertyName);
            switch (items[0]) {
            	case "account_type":
            		acc.setType(Account_Type.valueOf(propertyValue));
            		break;
            	case "account_name":
            		acc.setAccount_name(propertyValue);
            		break;
            	case "hostname":
            		acc.setHostname(propertyValue);
            		break;
            	case "login":
            		acc.setLogin(propertyValue);
            		break;
            	case "root_folder":
            		acc.setRoot_folder(propertyValue);
            		break;
            	case "from_address":
            		acc.setFrom_address(propertyValue);
            		break;
            	default:
            		System.err.println("Unknown property name:" + items[1]);
            		break;
            }
		}
		newConfig.getAccountList().addAll(accounts.values());

        return newConfig;
    }

}