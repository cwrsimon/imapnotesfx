package de.wesim.imapnotes.preferenceview.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.components.PrefixedAlertBox;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.services.I18NService;
import de.wesim.imapnotes.services.IMAPBackend;
import de.wesim.imapnotes.services.IMAPUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javax.mail.Folder;
import javax.mail.MessagingException;

// TODO Format für Spezifikation des Folder-Pfads definieren
// TODO Konfiguration des Ports
// TODO Konfiguration von SSL
// TODO check connection
// TODO add support for more specific connection settings (ports, etc.)
// TODO Wenn der Folder nicht existiert, dann anlegen !!!
// TODO Zur Spring-Komponente machen...
// TODO Schauen, ob man die Mail-Adresse wegreduzieren kann ...
public class IMAPTab extends Tab implements HasLogger {

    private class IMAPForm extends GridPane {

        private Hyperlink removeMe = new Hyperlink(i18N.getTranslation("remove"));
        private Button testMe = new Button(i18N.getTranslation("testIMAP"));

        final TextField nameField;
        final TextField pathField;
        final TextField hostField;
        final TextField loginField;
        final TextField addressField;
        private TextField portField;
        private CheckBox sslCheck;

        public IMAPForm() {
            this.setHgap(10);
            this.setVgap(10);
            this.setPadding(new Insets(5, 5, 5, 5));

            final Label nameLabel = new Label(i18N.getTranslation("name"));
            this.add(nameLabel, 0, 0);
            nameField = new TextField();
            this.add(nameField, 1, 0, 2, 1);

            final Label hostLabel = new Label(i18N.getTranslation("host"));
            this.add(hostLabel, 0, 1);
            hostField = new TextField();
            this.add(hostField, 1, 1, 2, 1);

            final Label portLabel = new Label(i18N.getTranslation("port"));
            this.add(portLabel, 0, 3);
            portField = new TextField("993");
            this.add(portField, 1, 3, 2, 1);

            final Label sslLabel = new Label(i18N.getTranslation("ssl"));
            this.add(sslLabel, 0, 4);
            sslCheck = new CheckBox();
            sslCheck.setTextAlignment(TextAlignment.LEFT);
            sslCheck.setSelected(true);
            this.add(sslCheck, 1, 4, 2, 1);

            final Label loginLabel = new Label(i18N.getTranslation("login"));
            this.add(loginLabel, 0, 2);
            loginField = new TextField();
            this.add(loginField, 1, 2, 2, 1);

            final Label addressLabel = new Label(i18N.getTranslation("from_address"));
            this.add(addressLabel, 0, 5, 1, 1);
            addressField = new TextField();
            this.add(addressField, 1, 5, 2, 1);

            final Label pathLabel = new Label(i18N.getTranslation("path"));
            this.add(pathLabel, 0, 6, 1, 1);

            pathField = new TextField();
            final Button dirButton = new Button("...");
            this.add(pathField, 1, 6, 1, 1);
            this.add(dirButton, 2, 6, 1, 1);

            this.add(testMe, 1, 7, 2, 1);
            GridPane.setHalignment(testMe, HPos.RIGHT);
            this.add(removeMe, 0, 8);

            ColumnConstraints column1 = new ColumnConstraints();
            ColumnConstraints column2 = new ColumnConstraints();

            column2.setHgrow(Priority.ALWAYS);
            this.getColumnConstraints().addAll(column1, column2);

            dirButton.setOnAction(e -> {
                final IMAPBackend backend = context.getBean(IMAPBackend.class, getAccount());
                try {
                    backend.initNotesFolder();
                } catch (Exception ex) {
                    Logger.getLogger(IMAPTab.class.getName()).log(Level.SEVERE, null, ex);
                }
                final List<String> availableFolders = IMAPUtils.getFlatList(backend.getStore());
                final ChoiceDialog<String> folderChooser = new ChoiceDialog<>(availableFolders.get(0), availableFolders);
                final Optional<String> choice = folderChooser.showAndWait();
                if (!choice.isPresent()) {
                    return;
                }
                final String chosenFolderName = choice.get();
                pathField.setText(chosenFolderName);
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
            account.setPort(portField.getText());
            account.setSsl(sslCheck.isSelected());
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
        newForm.portField.textProperty().set(account.getPort());
        newForm.sslCheck.selectedProperty().set(account.isSsl());
        newForm.removeMe.setOnAction(e -> {
            acco.getPanes().remove(tp);
        });
        newForm.testMe.setOnAction(e -> {
            final Account newAccount = newForm.getAccount();
            IMAPBackend backend = context.getBean(IMAPBackend.class, newAccount);
            boolean testOK = false;
            try {
                testOK = backend.initNotesFolder();
            } catch (Exception e1) {
                testOK = false;
                e1.printStackTrace();
                final Alert alert = context.getBean(PrefixedAlertBox.class, "imap_conn_failed");
                alert.setAlertType(AlertType.ERROR);
                alert.showAndWait();
            }
            final Alert alert;
            if (testOK) {
                alert = context.getBean(PrefixedAlertBox.class, "imap_conn_success");
                alert.setAlertType(AlertType.INFORMATION);
                alert.showAndWait();
                return;
            }
            // TODO Neuen Ordner erstellen ...
            alert = context.getBean(PrefixedAlertBox.class, "imap_conn_create_dir");
            alert.setAlertType(AlertType.CONFIRMATION);
            Optional<ButtonType> response = alert.showAndWait();
            if (!response.isPresent()) {
                return;
            }
            if (response.get() == ButtonType.CANCEL) {
                return;
            }
            try {
             backend.createNotesFolder(newAccount.getRoot_folder());
            } catch (MessagingException exc) {
                // TODO Do something smart here!!!
                exc.printStackTrace();
            }
        });
        return tp;
    }

    final Accordion acco;
    private I18NService i18N;
    private ApplicationContext context;

    private static int new_counter = 0;

    public IMAPTab(I18NService i18n, ApplicationContext context) {
        super("IMAP");
        this.i18N = i18n;
        this.context = context;

        final VBox vbox = new VBox();
        setContent(vbox);
        vbox.setPadding(new Insets(5, 5, 5, 5));
        vbox.setSpacing(5);

        acco = new Accordion();
        var scrollPane = new ScrollPane(acco);
        scrollPane.setFitToWidth(true);
        final Hyperlink button = new Hyperlink(i18n.getTranslation("new"));
        final ToolBar toolbar = new ToolBar(button);

        vbox.getChildren().add(toolbar);
        vbox.getChildren().add(scrollPane);

        button.setOnAction(e -> {
            // a new account with some default values
            final Account newAccount = new Account();
            new_counter++;
            newAccount.setAccount_name("New_" + String.valueOf(new_counter));
            newAccount.setRoot_folder("/Notes");
            newAccount.setPort("993");
            newAccount.setSsl(true);
            acco.getPanes().add(0, createTitledPane(newAccount));
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
