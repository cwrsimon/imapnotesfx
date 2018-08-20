package de.wesim.imapnotes.services;

import java.util.logging.Level;
import java.util.logging.Logger;
import de.wesim.imapnotes.Consts;
import net.east301.keyring.BackendNotSupportedException;
import net.east301.keyring.Keyring;
import net.east301.keyring.PasswordRetrievalException;
import net.east301.keyring.PasswordSaveException;
import net.east301.keyring.util.LockException;

public class PasswordProvider {

    Keyring keyring;

    public PasswordProvider() {

    }

    public void init() {

        // setup a Keyring instance
        //
        // create an instance of Keyring by invoking Keyring.create method
        //
        // Keyring.create method finds appropriate keyring backend, and sets it up for you.
        // On Mac OS X environment, OS X Keychain is used, and On Windows environment,
        // DPAPI is used for encryption of passwords.
        // If no supported backend is found, BackendNotSupportedException is thrown.
        try {
            keyring = Keyring.create();
        } catch (BackendNotSupportedException ex) {
            Logger.getLogger(PasswordProvider.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        // some backend directory handles a file to store password to disks.
        // in this case, we must set path to key store file by Keyring.setKeyStorePath
        // before using Keyring.getPassword and Keyring.getPassword.
//        if (keyring.isKeyStorePathRequired()) {
//            //try {
//            //  File keyStoreFile = File.createTempFile("keystore", ".keystore");
//            keyring.setKeyStorePath(Consts.KEYSTORE_PATH.toString());
//            //} catch (IOException ex) {
//            //    Logger.getLogger(PasswordProvider.class.getName()).log(Level.SEVERE, null, ex);
//            //}
//        }
    }

    public String retrievePassword(String accountName) {
        System.out.println("Retrieving password " + accountName);
        //
        // Retrieve password from key store
        //

        // Password can be retrieved by using Keyring.getPassword method.
        // PasswordRetrievalException is thrown when some error happened while getting password.
        // LockException is thrown when keyring backend failed to lock key store file.
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

}
