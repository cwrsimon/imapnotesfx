package de.wesim.imapnotes.services;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.models.Configuration;

@Service
public class ConfigurationService implements HasLogger {

	private static final String LAST_OPENED_ACCOUNT = "last_opened_account";

	private static final String FONT_SIZE = "font_size";

	private static final String FONT_FAMILY = "font_family";

	private static final String HOSTNAME = "hostname";

	private static final String LOGIN = "login";

	private static final String ROOT_FOLDER = "root_folder";

	private static final String FROM_ADDRESS = "from_address";

	private static final String PASSWORD = "password";

	private static final String ACCOUNT_TYPE = "account_type";

	private static final String ACCOUNT_NAME = "account_name";

	private static final String NO_ACCOUNT_KEY = String.valueOf(Integer.MAX_VALUE);

	private static void addProp(Properties props, String name, String value, int i) {
		if (value == null) return;
		if (value.trim().length() == 0) return;
		props.put(name + "." + String.valueOf(i), value);
	}

	public void writeConfig(Configuration config) {
		getLogger().info("Writing config ...");
		Properties imapSettings = new Properties();
		for (int i=0; i< config.getAccountList().size(); i++) {
			Account acc = config.getAccountList().get(i);
			addProp(imapSettings, ACCOUNT_NAME, acc.getAccount_name(), i);
			addProp(imapSettings, ACCOUNT_TYPE, acc.getType().toString(), i);
			addProp(imapSettings, HOSTNAME, acc.getHostname(), i);
			addProp(imapSettings, LOGIN, acc.getLogin(), i);
			addProp(imapSettings, ROOT_FOLDER, acc.getRoot_folder(), i);
			addProp(imapSettings, FROM_ADDRESS, acc.getFrom_address(), i);
			addProp(imapSettings, PASSWORD, acc.getPassword(), i);
		}
		addProp(imapSettings, FONT_FAMILY, config.getFontFamily(), Integer.MAX_VALUE);
		addProp(imapSettings, FONT_SIZE, config.getFontSize(), Integer.MAX_VALUE);
		addProp(imapSettings, LAST_OPENED_ACCOUNT, config.getLastOpenendAccount(), Integer.MAX_VALUE);
		try {
			imapSettings.store(Files.newOutputStream(Consts.USER_CONFIGURATION_FILE), 
						"Update ImapNotesFX configuration");
		} catch (IOException e) {
			getLogger().error("Writing configuration file to '{}' failed.", Consts.USER_CONFIGURATION_FILE, e );
		}

	}

    public Configuration readConfig() {

        final Configuration newConfig = new Configuration();

        final Properties imapSettings = new Properties();
		try {
			imapSettings.load(Files.newBufferedReader(Consts.USER_CONFIGURATION_FILE));
		} catch (IOException e) {
			getLogger().error("Reading configuration file from '{}' failed.", Consts.USER_CONFIGURATION_FILE, e);
            return newConfig;
        }
        final Map<String, Account> accounts = new LinkedHashMap<>();
        final Set<String> propertyNames = imapSettings.stringPropertyNames();
        for (String propertyName : propertyNames) {
            final String[] items = propertyName.split("\\.");
            if (items.length != 2) {
				getLogger().error("Invalid property name: {}", propertyName);
                continue;
            }

            final String key = items[1];
            Account acc = accounts.get(key);
            if (acc == null && !key.equals(NO_ACCOUNT_KEY)) {
            	acc = new Account();
            	accounts.put(key, acc);
            }
			final String propertyValue = imapSettings.getProperty(propertyName);
            switch (items[0]) {
				case FONT_SIZE:
					newConfig.setFontSize(propertyValue);
					break;
				case FONT_FAMILY:
					newConfig.setFontFamily(propertyValue);
					break;	
				case LAST_OPENED_ACCOUNT:
					newConfig.setLastOpenendAccount(propertyValue);
					break;
            	case ACCOUNT_TYPE:
            		acc.setType(Account_Type.valueOf(propertyValue));
            		break;
            	case ACCOUNT_NAME:
            		acc.setAccount_name(propertyValue);
            		break;
            	case HOSTNAME:
            		acc.setHostname(propertyValue);
            		break;
            	case LOGIN:
            		acc.setLogin(propertyValue);
            		break;
            	case ROOT_FOLDER:
            		acc.setRoot_folder(propertyValue);
            		break;
            	case FROM_ADDRESS:
            		acc.setFrom_address(propertyValue);
            		break;
            	case PASSWORD:
            		acc.setPassword(propertyValue);
            		break;
            	default:
            		getLogger().error("Unknown property name: {}", items[1]);
            		break;
            }
		}
		newConfig.setAccounts(accounts.values());

        return newConfig;
    }

}