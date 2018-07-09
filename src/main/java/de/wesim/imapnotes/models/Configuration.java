package de.wesim.imapnotes.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class.getName());

    private final List<Account> fsAccounts;
    private final List<Account> imapAccounts;
    private String fontSize;

    public String getFontSize() {
        return this.fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public Configuration() {
        this.fsAccounts = new ArrayList<>();
        this.imapAccounts = new ArrayList<>();
    }

    public List<Account> getIMAPAccounts() {
        return this.imapAccounts;
    }

    public List<Account> getFSAccounts() {
        return this.fsAccounts;
    }

    public void addFSAccount(Account fsAccount) {
        this.fsAccounts.add(fsAccount);
    }

    public void addIMAPAccount(Account fsAccount) {
        this.imapAccounts.add(fsAccount);
    }


    public List<Account> getAccountList() {
        List<Account> returnList = new ArrayList<>();
        returnList.addAll(fsAccounts);
        returnList.addAll(imapAccounts);
        return returnList;
    }

    // public  Account createNewAccount() {
    //     Account newAccount = new Account();
    //     newAccountCounter++;
    //     newAccount.setAccount_name("New account " + String.valueOf(newAccountCounter));
    //     newAccount.setType(Account_Type.FS);
    //     this.accounts.add(newAccount);
    //     return newAccount;
    // }

    // public void deleteAccount(String accountName) {
    //     logger.info("Trying to delete account:" + accountName);
    //     Iterator<Account> accountIterator = this.accounts.iterator();
    //     while (accountIterator.hasNext()) {
    //         Account next = accountIterator.next();
    //         if (next.getAccount_name().equals(accountName)) {
    //             accountIterator.remove();
    //             break;
    //         }
    //     }
    // }

	public void setAccounts(Collection<Account> values) {
        for (Account account : values) {
            if (account.getType() == Account_Type.FS) {
                this.fsAccounts.add(account);
            } else {
                this.imapAccounts.add(account);
            }
        }
	}

}