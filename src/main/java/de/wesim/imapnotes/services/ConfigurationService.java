package de.wesim.imapnotes.services;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.models.Configuration;

//@Component
public class ConfigurationService {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

	private static final String NO_ACCOUNT_KEY = String.valueOf(Integer.MAX_VALUE);

	private static void addProp(Properties props, String name, String value, int i) {
		if (value == null) return;
		if (value.trim().length() == 0) return;
		props.put(name + "." + String.valueOf(i), value);
	}

	public static void writeConfig(Configuration config) {
		logger.info("Writing config ...");
		Properties imapSettings = new Properties();
		for (int i=0; i< config.getAccountList().size(); i++) {
			Account acc = config.getAccountList().get(i);
			addProp(imapSettings, "account_name", acc.getAccount_name(), i);
			addProp(imapSettings, "account_type", acc.getType().toString(), i);
			addProp(imapSettings, "hostname", acc.getHostname(), i);
			addProp(imapSettings, "login", acc.getLogin(), i);
			addProp(imapSettings, "root_folder", acc.getRoot_folder(), i);
			addProp(imapSettings, "from_address", acc.getFrom_address(), i);
			addProp(imapSettings, "password", acc.getPassword(), i);

		}
		addProp(imapSettings, "font_size", config.getFontSize(), Integer.MAX_VALUE);
		try {
			imapSettings.store(Files.newOutputStream(Consts.USER_CONFIGURATION_FILE), 
						"Update ImapNotesFX configuration");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

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
            final String[] items = propertyName.split("\\.");
            if (items.length != 2) {
				logger.error("Invalid property name: {}", propertyName);
                continue;
            }

            String key = items[1];
        	//logger.debug("key:" + key);

            Account acc = accounts.get(key);
            if (acc == null && !key.equals(NO_ACCOUNT_KEY)) {
            	acc = new Account();
            	accounts.put(key, acc);
            }
			final String propertyValue = imapSettings.getProperty(propertyName);
            switch (items[0]) {
				case "font_size":
					newConfig.setFontSize(propertyValue);
					break;
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
            	case "password":
            		acc.setPassword(propertyValue);
            		break;
            	default:
            		logger.error("Unknown property name: {}", items[1]);
            		break;
            }
		}
		newConfig.setAccounts(accounts.values());

        return newConfig;
    }

}