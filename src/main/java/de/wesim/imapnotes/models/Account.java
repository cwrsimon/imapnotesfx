package de.wesim.imapnotes.models;

public class Account {

    private String type;
    private String account_name;
    private String hostname;
    // TODO Getter und Setter erstellen
    public String login;
    public String root_folder;

    public Account() {

    }

    public String getType(String type) {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccount_name(String accountName) {
        return this.account_name;
    }

    public void setAccount_name(String accountName) {
        this.account_name = accountName;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    
}