/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wesim.imapnotes.services;

import de.wesim.imapnotes.mainview.components.outliner.PasswordInputDialog;
import de.wesim.imapnotes.models.Account;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import net.east301.keyring.PasswordSaveException;

/**
 *
 * @author christian
 */
public class MyAuthenticator extends Authenticator {

    private final Account account;
    private final PasswordProvider passwordProvider;
    private boolean tryAgain = false;

    MyAuthenticator(Account account) {
        this.account = account;
        this.passwordProvider = new PasswordProvider();
        this.passwordProvider.init();
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if (!tryAgain) {
        String retrievedPassword = this.passwordProvider.retrievePassword(this.account.getAccount_name());
        // TODO was, wenn es nicht passt?
        if (retrievedPassword != null) {
            return new PasswordAuthentication(this.account.getLogin(), retrievedPassword);
        }
        }
        
        final PasswordInputDialog dialog = new PasswordInputDialog();
        dialog.setTitle("Title");
        if (tryAgain) {
            dialog.setHeaderText("Header Text");
        } else {
            dialog.setHeaderText("Try again!");
        }
        Optional<String> result = dialog.showAndWait();
        String entered = "N/A";
        if (result.isPresent()) {
            entered = result.get();
            try {
                this.passwordProvider.storePassword(this.account.getAccount_name(), entered);
            } catch (PasswordSaveException ex) {
                Logger.getLogger(MyAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
               return new PasswordAuthentication(this.account.getLogin(), entered);
            }
        }
        return null;
    }

    public void setTryAgain(boolean tryAgain) {
        this.tryAgain = tryAgain;
    }
}
