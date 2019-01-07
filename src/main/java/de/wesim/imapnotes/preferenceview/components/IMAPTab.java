package de.wesim.imapnotes.preferenceview.components;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.services.I18NService;
import de.wesim.imapnotes.services.IMAPBackend;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
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
import de.wesim.imapnotes.services.IMAPUtils;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO Format für Spezifikation des Folder-Pfads definieren
// TODO Konfiguration des Ports
// TODO Konfiguration von SSL
// TODO check connection
// TODO add support for more specific connection settings (ports, etc.)
// TODO Zur Spring-Komponente machen...
public class IMAPTab extends Tab implements HasLogger {


    private class IMAPForm extends GridPane {

        private Hyperlink removeMe = new Hyperlink(i18N.getTranslation("remove"));
        private Button testMe = new Button(i18N.getTranslation("testIMAP"));

        final TextField nameField;
        final TextField pathField;
        final TextField hostField;
        final TextField loginField;
        final TextField addressField;

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

            final Label loginLabel = new Label(i18N.getTranslation("login"));
            this.add(loginLabel, 0, 2);
            loginField = new TextField();
            this.add(loginField, 1, 2, 2, 1);

            final Label addressLabel = new Label(i18N.getTranslation("from_address"));
            this.add(addressLabel, 0, 3, 1, 1);
            addressField = new TextField();
            this.add(addressField, 1, 3, 2, 1);

            final Label pathLabel = new Label(i18N.getTranslation("path"));
            this.add(pathLabel, 0, 4, 1, 1);

            pathField = new TextField();
            Button dirButton = new Button("...");
//            this.add(pathField, 1, 1, 1, 1);
//            this.add(dirButton, 2, 1, 1, 1);
            
            this.add(pathField, 1, 4, 1, 1);
            this.add(dirButton, 2, 4, 1, 1);

            this.add(testMe, 1, 5, 2, 1);
            GridPane.setHalignment(testMe, HPos.RIGHT);
            this.add(removeMe, 0, 6);
            
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
                // TODO Gescheites Wording für den Dialog finden
            	final List<String> availableFolders = IMAPUtils.getFlatList(backend.getStore());
            	final ChoiceDialog<String> folderChooser = new ChoiceDialog<String>("", availableFolders);
            	final Optional<String> choice = folderChooser.showAndWait();
                if (!choice.isPresent()) return;
                final String chosenFolderName = choice.get();
                pathField.setText(chosenFolderName);
//                DirectoryChooser dirChooser = new DirectoryChooser();
//                File selectedDir = dirChooser.showDialog(getScene().getWindow());
//                if (selectedDir != null) {
//                    pathField.setText(selectedDir.getAbsolutePath());
//                }
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
        newForm.testMe.setOnAction(e-> {
        	IMAPBackend backend = context.getBean(IMAPBackend.class, newForm.getAccount());
        	boolean testOK = false;
        	try {
        		testOK = backend.initNotesFolder();
        	} catch (Exception e1) {
        		testOK = false;
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				// TODO Schöneres Wording finden!
				// TODO Fehlermeldung durchreichen ...
        		final Alert alert;
        		if (testOK) {
        			alert = new Alert(AlertType.INFORMATION);
        		} else {
        			alert = new Alert(AlertType.ERROR);
        		}
        		alert.showAndWait();
        		//exception.printStackTrace();
        	}
        });
        return tp;
    }

    final Accordion acco;
	private I18NService i18N;
	private ApplicationContext context;

    public IMAPTab(I18NService i18n, ApplicationContext context) {
        super("IMAP");
        this.i18N = i18n;
        this.context = context;

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
