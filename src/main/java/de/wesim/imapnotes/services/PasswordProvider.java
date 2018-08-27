package de.wesim.imapnotes.services;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.HasLogger;
import net.east301.keyring.BackendNotSupportedException;
import net.east301.keyring.Keyring;
import net.east301.keyring.PasswordRetrievalException;
import net.east301.keyring.PasswordSaveException;
import net.east301.keyring.util.LockException;
import net.revelc.code.gnome.keyring.GnomeKeyring;
import net.revelc.code.gnome.keyring.GnomeKeyringException;
import net.revelc.code.gnome.keyring.GnomeKeyringItem;
import net.revelc.code.gnome.keyring.GnomeKeyringItem.Attribute;

public class PasswordProvider implements HasLogger {

    Keyring keyring;
	private GnomeKeyring gk;
	private String linux_keyring;

    public PasswordProvider() {

    }

    public void init() {
    	
        try {
            keyring = Keyring.create();
        } catch (BackendNotSupportedException ex) {
        	getLogger().warn("Probably neither Win or MacOS environment ...", ex);
            return;
        }
        getLogger().info("{}", System.getProperty("os.name"));
        if (System.getProperty("os.name").startsWith("Linux")) {
        	 try {
            	this.gk = new GnomeKeyring(Consts.KEYSTORE_SERVICE_NAME);

				this.linux_keyring = gk.getDefaultKeyring();
			} catch (GnomeKeyringException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

    }

    public String retrievePassword(String accountName) {
        System.out.println("Retrieving password " + accountName);
        if (this.gk != null) {
        	Set<Integer> ids = this.gk.getIds(this.linux_keyring);
        	for (Integer id : ids) {
        		try {
        			getLogger().info("ID: {}", id);
					GnomeKeyringItem item = this.gk.getItem(this.linux_keyring, id, true);
					getLogger().info("Display Name:{}", item.getDisplayName());
					for (Attribute<?> attr : item.getAttributes()) {
						getLogger().info("Attr: {}, Value: {}", attr.getName(), attr.getValue().toString());
						if (attr.getName().equals("domain")) {
							if (attr.getValue() instanceof String) {
								final String castedDomain = (String) attr.getValue();
								if (castedDomain.equals(accountName)) {
									return item.getSecret();
								}
							}
						}
					}
				} catch (GnomeKeyringException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        	}
        }
        
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
        if (this.gk != null) {
        	 try {
				int id = gk.setNetworkPassword(linux_keyring, null, accountName, null, null, "file", null, 0, pw);
			} catch (GnomeKeyringException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

}
