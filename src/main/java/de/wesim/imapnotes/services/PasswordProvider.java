package de.wesim.imapnotes.services;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.HasLogger;
import net.east301.keyring.BackendNotSupportedException;
import net.east301.keyring.Keyring;
import net.east301.keyring.PasswordRetrievalException;
import net.east301.keyring.PasswordSaveException;
import net.east301.keyring.util.LockException;

public class PasswordProvider implements HasLogger {

    Keyring keyring;

    public PasswordProvider() {

    }

    public void init() {
        try {
            keyring = Keyring.create();
            if (keyring.isKeyStorePathRequired()) {
            	keyring.setKeyStorePath(Consts.KEYSTORE_PATH.toAbsolutePath().toString());
            }
        } catch (BackendNotSupportedException ex) {
        	getLogger().error("Initializing keyring backend has failed.", ex);
            return;
        }
    }

    public String retrievePassword(String accountName) {
        try {
            String password = keyring.getPassword(Consts.KEYSTORE_SERVICE_NAME, accountName);
            return password;
        } catch (LockException | PasswordRetrievalException ex) {
            getLogger().error("Retrieving password for account {} has failed.", accountName, ex);
        }
        return null;
    }

    public void storePassword(String accountName, String pw) throws PasswordSaveException {
        try {
            keyring.setPassword(Consts.KEYSTORE_SERVICE_NAME, accountName, pw);
        } catch (LockException ex) {
            getLogger().error("Storing password for account {} has failed.", accountName, ex);
        }
       
    }
    
//    public static void main(String [] args) {
//    	PasswordProvider provider = new PasswordProvider();
//    	provider.init();
//    	System.out.println(provider.retrievePassword("Wesim"));
//    }

}
