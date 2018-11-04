package de.wesim.imapnotes.preferenceview.components;

import java.util.ArrayList;
import java.util.List;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.services.I18NService;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class IMAPTab extends Tab implements HasLogger {


	// TODO check connection
    private class IMAPForm extends GridPane {

        private Hyperlink removeMe = new Hyperlink(i18N.getTranslation("remove"));

        final TextField nameField;
        final TextField pathField;
        final TextField hostField;
        final TextField loginField;
        final TextField addressField;

        // TODO Use Properties
        public IMAPForm() {
            this.setHgap(10);
            this.setVgap(10);
            this.setPadding(new Insets(5, 5, 5, 5));

            final Label nameLabel = new Label(i18N.getTranslation("name"));
            this.add(nameLabel, 0, 0);
            nameField = new TextField();
            this.add(nameField, 1, 0);

            final Label hostLabel = new Label(i18N.getTranslation("host"));
            this.add(hostLabel, 0, 1);
            hostField = new TextField();
            this.add(hostField, 1, 1);

            final Label loginLabel = new Label(i18N.getTranslation("login"));
            this.add(loginLabel, 0, 2);
            loginField = new TextField();
            this.add(loginField, 1, 2);

            final Label addressLabel = new Label(i18N.getTranslation("from_address"));
            this.add(addressLabel, 0, 3);
            addressField = new TextField();
            this.add(addressField, 1, 3);

            final Label pathLabel = new Label(i18N.getTranslation("path"));
            this.add(pathLabel, 0, 4);

            pathField = new TextField();
//            Button dirButton = new Button("...");
//            HBox hbox = new HBox(pathField, dirButton);
            this.add(pathField, 1, 4);

            this.add(removeMe, 0, 5);
            
            ColumnConstraints column1 = new ColumnConstraints();
            ColumnConstraints column2 = new ColumnConstraints();

            column2.setHgrow(Priority.ALWAYS);
            this.getColumnConstraints().addAll(column1, column2);
        }

        public Account getAccount() {
            Account account = new Account();
            account.setAccount_name(nameField.getText());
            account.setRoot_folder(pathField.getText());
            account.setType(Account_Type.IMAP);
            account.setFrom_address(addressField.getText());
            account.setHostname(hostField.getText());
            account.setLogin(loginField.getText());
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
        newForm.addressField.textProperty().set(account.getFrom_address());
        newForm.removeMe.setOnAction(e -> {
            acco.getPanes().remove(tp);
        });
        return tp;
    }

    final Accordion acco;
	private I18NService i18N;

    public IMAPTab(I18NService i18n) {
        super("IMAP");
        this.i18N = i18n;

        final VBox vbox = new VBox();
        setContent(vbox);
        vbox.setPadding(new Insets(5, 5, 5, 5));
        vbox.setSpacing(5);

        acco = new Accordion();

        final Hyperlink button = new Hyperlink(i18n.getTranslation("new"));
        final ToolBar toolbar = new ToolBar(button);

        vbox.getChildren().add(toolbar);
        vbox.getChildren().add(acco);

        button.setOnAction(e -> {
            acco.getPanes().add(createTitledPane(new Account()));
        });
    }

    public void openAccordion() {
        if (acco.getPanes().isEmpty()) {
            return;
        }
        final TitledPane first = acco.getPanes().get(0);
        acco.setExpandedPane(first);
    }

    public void addAccount(Account account) {
        acco.getPanes().add(createTitledPane(account));
    }

    public List<Account> getAccounts() {
        final List<Account> accounts = new ArrayList<>();
        for (TitledPane tp : acco.getPanes()) {
            final IMAPForm form = (IMAPForm) tp.getContent();
            accounts.add(form.getAccount());
        }
        return accounts;
    }

}
