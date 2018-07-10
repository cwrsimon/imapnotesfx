package de.wesim.imapnotes.ui.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

public class IMAPTab extends Tab {
	
	private static final Logger logger = LoggerFactory.getLogger(IMAPTab.class);

	private class IMAPForm extends GridPane {

		
		Hyperlink removeMe = new Hyperlink("Remove");

		final TextField nameField;
		final TextField pathField;
		final TextField hostField;
		final TextField loginField;
		final TextField pwField;
		final TextField pwConfField;
		final TextField addressField;
	
		// TODO Name als Property
		// UNd mit Name der TitledPane verbinden
		public IMAPForm() {
			//this.setAlignment(Pos.);
			this.setHgap(10);
			this.setVgap(10);
			this.setPadding(new Insets(5, 5, 5, 5));

			Label nameLabel = new Label("Name");
			this.add(nameLabel, 0,0);
			nameField = new TextField();
			this.add(nameField, 1,0);

			Label hostLabel = new Label("Host");this.add(hostLabel, 0,1);
			hostField = new TextField();this.add(hostField, 1,1);

			Label loginLabel = new Label("Login");this.add(loginLabel, 0,2);
			loginField = new TextField();this.add(loginField, 1,2);

			Label pwLabel = new Label("Password");this.add(pwLabel, 0,3);
			pwField = new PasswordField();this.add(pwField, 1,3);

			Label pwConfLabel = new Label("Password Confirmation");this.add(pwConfLabel, 0,4);
			pwConfField = new PasswordField();this.add(pwConfField, 1,4);

			Label addressLabel = new Label("From Address");this.add(addressLabel, 0,5);
			addressField = new TextField();this.add(addressField, 1,5);

			Label pathLabel = new Label("File Path");
			this.add(pathLabel, 0,6);

			pathField = new TextField();
			Button dirButton = new Button("...");
			HBox hbox = new HBox(pathField, dirButton);
			this.add(hbox, 1,6);

			this.add(removeMe, 0, 7);

			
			dirButton.setOnAction( e-> {
				DirectoryChooser dirChooser = new DirectoryChooser();
				File selectedDir = dirChooser.showDialog(getScene().getWindow());
				if (selectedDir != null) {
					pathField.setText(selectedDir.getAbsolutePath());
				}
			});
		}
		
		public Account getAccount() {
			Account account = new Account();
			account.setAccount_name(nameField.getText());
			account.setRoot_folder(pathField.getText());
			account.setType(Account_Type.IMAP);
			account.setFrom_address(addressField.getText());
			account.setHostname(hostField.getText());
			account.setLogin(loginField.getText());
			// TODO Sonderbehandlung für Passwörter
			account.setPassword(pwField.getText());
			return account;
		}
	}

	private TitledPane createTitledPane(Account account) {
		IMAPForm newForm = new IMAPForm();
		final TitledPane tp = new TitledPane();
		tp.setContent(newForm);
		tp.textProperty().bind(newForm.nameField.textProperty());
		newForm.nameField.textProperty().set(account.getAccount_name());
		newForm.pathField.textProperty().set(account.getRoot_folder());
		newForm.hostField.textProperty().set(account.getHostname());
		newForm.loginField.textProperty().set(account.getLogin());
		newForm.pwField.textProperty().set(account.getPassword());
		newForm.pwConfField.textProperty().set(account.getPassword());
		newForm.addressField.textProperty().set(account.getFrom_address());
		
		newForm.removeMe.setOnAction(e -> {
			acco.getPanes().remove(tp);
		});
		return tp;
	}

	final Accordion acco;


	public IMAPTab() {
		super("IMAP");

		final VBox vbox = new VBox();
		setContent(vbox);
		vbox.setPadding(new Insets(5, 5, 5, 5));
		vbox.setSpacing(5);
		

		acco = new Accordion();
		
		final Hyperlink button = new Hyperlink("New");
		final ToolBar toolbar = new ToolBar(button);

		vbox.getChildren().add(toolbar);
		vbox.getChildren().add(acco);

        button.setOnAction(e -> {
    		acco.getPanes().add(createTitledPane(new Account()));
        });
	}
	
	public void openAccordion() {
		if (acco.getPanes().isEmpty()) return;
		final TitledPane first = acco.getPanes().get(0);
		acco.setExpandedPane(first);
	}

	public void addAccount(Account account) {
		acco.getPanes().add(createTitledPane(account));
	}

	public List<Account> getAccounts() {
		final List<Account> accounts = new ArrayList<>();
		for ( TitledPane tp : acco.getPanes()) {
			final IMAPForm form = (IMAPForm) tp.getContent();
			accounts.add(form.getAccount());
		}
		return accounts;
	}

}
