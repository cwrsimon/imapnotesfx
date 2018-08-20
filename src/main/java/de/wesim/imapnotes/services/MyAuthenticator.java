/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wesim.imapnotes.services;

import de.wesim.imapnotes.mainview.components.outliner.PasswordInputDialog;
import de.wesim.imapnotes.models.Account;
import java.util.Optional;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 *
 * @author christian
 */
public class MyAuthenticator extends Authenticator {

    private final Account account;
    private final PasswordProvider passwordProvider;

    MyAuthenticator(Account account) {
        this.account = account;
        this.passwordProvider = new PasswordProvider();
        this.passwordProvider.init();
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String retrievedPassword = this.passwordProvider.retrievePassword(this.account.getAccount_name());
        // TODO was, wenn es nicht passt?
        if (retrievedPassword != null) {
            return new PasswordAuthentication(this.account.getLogin(), retrievedPassword);
        }
        
        final PasswordInputDialog dialog = new PasswordInputDialog();
        dialog.setTitle("Title");
        dialog.setHeaderText("Header Text");
        Optional<String> result = dialog.showAndWait();
        String entered = "N/A";
        if (result.isPresent()) {
            entered = result.get();
        }
        // TODO Verbindung prüfen...
        return new PasswordAuthentication(this.account.getLogin(), entered);
    }

}
