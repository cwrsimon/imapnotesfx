package de.wesim.imapnotes.models;



public class Account {

    private Account_Type type;
    private String account_name;
    private String hostname;
    private String login;
    private String root_folder;
    private String from_address;
    private String password;
    
    public Account() {

    }
    
	public String getFrom_address() {
		return from_address;
	}



	public void setFrom_address(String from_address) {
		this.from_address = from_address;
	}



	public Account_Type getType() {
		return type;
	}

	public void setType(Account_Type type) {
		this.type = type;
	}

	public String getAccount_name() {
		return account_name;
	}

	public void setAccount_name(String account_name) {
		this.account_name = account_name;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getRoot_folder() {
		return root_folder;
	}

	public void setRoot_folder(String root_folder) {
		this.root_folder = root_folder;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account_name == null) ? 0 : account_name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (account_name == null) {
			if (other.account_name != null)
				return false;
		} else if (!account_name.equals(other.account_name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.account_name;
	}

	
    
}