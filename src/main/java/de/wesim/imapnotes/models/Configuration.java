package de.wesim.imapnotes.models;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private final List<Account> accounts;

    public Configuration() {
        this.accounts = new ArrayList<>();
    }

    public List<Account> getAccountList() {
        return this.accounts;
    }

}