package de.wesim.imapnotes.services;

import java.util.Optional;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.components.outliner.PasswordInputDialog;
import de.wesim.imapnotes.models.Account;
import net.east301.keyring.PasswordSaveException;


public class MyAuthenticator extends Authenticator implements HasLogger {

    private final Account account;
    private final PasswordProvider passwordProvider;
    private boolean retry = false;

    MyAuthenticator(Account account) {
        this.account = account;
        this.passwordProvider = new PasswordProvider();
        this.passwordProvider.init();
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if (!retry) {
        String retrievedPassword = this.passwordProvider.retrievePassword(this.account.getAccount_name());
        // TODO was, wenn es nicht passt?
        if (retrievedPassword != null) {
            return new PasswordAuthentication(this.account.getLogin(), retrievedPassword);
        }
        }
        
        final PasswordInputDialog dialog = new PasswordInputDialog();
        // TODO translate me
        dialog.setTitle("Title");
        dialog.setContentText("Content");
        if (retry) {
            dialog.setHeaderText("Header Text");
        } else {
            dialog.setHeaderText("Try again!");
        }
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) return null;
        final String entered = result.get();
            try {
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
