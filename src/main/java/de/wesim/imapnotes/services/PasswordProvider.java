package de.wesim.imapnotes.services;

import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import de.wesim.imapnotes.HasLogger;
import java.nio.file.Path;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PasswordProvider implements HasLogger {

    private Keyring keyring;
            
    private static final String KEYSTORE_SERVICE_NAME = "imapnotesfx";
 
   
    public PasswordProvider() {

    }

    @PostConstruct
    void init() {
        try {
            keyring = Keyring.create();
        } catch (BackendNotSupportedException ex) {
        	getLogger().error("Initializing keyring backend has failed.", ex);
        }
    }

    public String retrievePassword(String accountName) {
        try {
            String password = keyring.getPassword(KEYSTORE_SERVICE_NAME, accountName);
            return password;
        } catch (PasswordAccessException  ex) {
            getLogger().error("Retrieving password for account {} has failed.", accountName, ex);
        }
        return null;
    }

    public void storePassword(String accountName, String pw) {
        try {
            keyring.setPassword(KEYSTORE_SERVICE_NAME, accountName, pw);
        } catch (PasswordAccessException ex) {
            getLogger().error("Storing password for account {} has failed.", accountName, ex);
        }
       
    }

}
