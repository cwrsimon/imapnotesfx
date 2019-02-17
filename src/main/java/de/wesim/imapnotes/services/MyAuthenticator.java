package de.wesim.imapnotes.services;

import java.util.Optional;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.components.PasswordInputDialog;
import de.wesim.imapnotes.models.Account;
import net.east301.keyring.PasswordSaveException;


@Component
@Scope("prototype")
public class MyAuthenticator extends Authenticator implements HasLogger {

    @Autowired
    private ApplicationContext context;

    private final Account account;
    private final PasswordProvider passwordProvider;
    private boolean retry = false;

    public MyAuthenticator(Account account) {
        this.account = account;
        this.passwordProvider = context.getBean(PasswordProvider.class);
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if (!retry) {
        	final String retrievedPassword = this.passwordProvider.retrievePassword(this.account.getAccount_name());
        	if (retrievedPassword != null) {
        		return new PasswordAuthentication(this.account.getLogin(), retrievedPassword);
        	}
        }
        
        final PasswordInputDialog dialog = context.getBean(PasswordInputDialog.class);
        
        final Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) return null;
        final String entered = result.get();
            try {
            	getLogger().info("Storing password in default keyring ...");
                this.passwordProvider.storePassword(this.account.getAccount_name(), entered);
            } catch (PasswordSaveException ex) {
                getLogger().error("Storing password failed.", ex);
            }
        return new PasswordAuthentication(this.account.getLogin(), entered);
    }

    public void setTryAgain(boolean tryAgain) {
        this.retry = tryAgain;
    }
}
