package de.wesim.imapnotes.services;

import java.util.logging.Level;
import java.util.logging.Logger;

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
        	// TODO
        	ex.printStackTrace();
            return;
        }
    }

    public String retrievePassword(String accountName) {
        try {
            String password = keyring.getPassword(Consts.KEYSTORE_SERVICE_NAME, accountName);
            return password;
        } catch (LockException | PasswordRetrievalException ex) {
            Logger.getLogger(PasswordProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void storePassword(String accountName, String pw) throws PasswordSaveException {
        try {
            keyring.setPassword(Consts.KEYSTORE_SERVICE_NAME, accountName, pw);
        } catch (LockException ex) {
            Logger.getLogger(PasswordProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    public static void main(String [] args) {
    	PasswordProvider provider = new PasswordProvider();
    	provider.init();
    	System.out.println(provider.retrievePassword("Wesim"));
    }

}
