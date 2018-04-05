package de.wesim.imapnotes.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class.getName());

    private static int newAccountCounter = 0;

    private final List<Account> accounts;

    public Configuration() {
        this.accounts = new ArrayList<>();
    }

    public List<Account> getAccountList() {
        return this.accounts;
    }

    public  Account createNewAccount() {
        Account newAccount = new Account();
        newAccountCounter++;
        newAccount.setAccount_name("New account " + String.valueOf(newAccountCounter));
        this.accounts.add(newAccount);
        return newAccount;
    }

    public void deleteAccount(String accountName) {
        logger.info("Trying to delete account:" + accountName);
        Iterator<Account> accountIterator = this.accounts.iterator();
        while (accountIterator.hasNext()) {
            Account next = accountIterator.next();
            if (next.getAccount_name().equals(accountName)) {
                accountIterator.remove();
                break;
            }
        }
    }

}