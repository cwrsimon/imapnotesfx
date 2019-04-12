package de.wesim.imapnotes.services;

import de.wesim.imapnotes.HasLogger;
import java.nio.file.Path;
import javax.annotation.PostConstruct;
import org.keyring.BackendNotSupportedException;
import org.keyring.Keyring;
import org.keyring.PasswordRetrievalException;
import org.keyring.PasswordSaveException;
import org.keyring.util.LockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PasswordProvider implements HasLogger {

    private Keyring keyring;

    @Autowired
    private Path keyStorePath;
            
    private static final String KEYSTORE_SERVICE_NAME = "imapnotesfx";
 
   
    public PasswordProvider() {

    }

    @PostConstruct
    private void init() {
        try {
            keyring = Keyring.create();
            if (keyring.isKeyStorePathRequired()) {
            	keyring.setKeyStorePath(keyStorePath.toAbsolutePath().toString());
            }
        } catch (BackendNotSupportedException ex) {
        	getLogger().error("Initializing keyring backend has failed.", ex);
            return;
        }
    }

    public String retrievePassword(String accountName) {
        try {
            String password = keyring.getPassword(KEYSTORE_SERVICE_NAME, accountName);
            return password;
        } catch (LockException | PasswordRetrievalException ex) {
            getLogger().error("Retrieving password for account {} has failed.", accountName, ex);
        }
        return null;
    }

    public void storePassword(String accountName, String pw) throws PasswordSaveException {
        try {
            keyring.setPassword(KEYSTORE_SERVICE_NAME, accountName, pw);
        } catch (LockException ex) {
            getLogger().error("Storing password for account {} has failed.", accountName, ex);
        }
       
    }

}
