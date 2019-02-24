/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wesim.imapnotes.preferenceview.components;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.components.PrefixedAlertBox;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.services.I18NService;
import de.wesim.imapnotes.services.IMAPBackend;
import de.wesim.imapnotes.services.IMAPUtils;
import java.util.List;
import java.util.Optional;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author papa
 */
@Component
@Scope("prototype")
    public class IMAPForm extends GridPane implements HasLogger {

    @Autowired
    private I18NService i18N;

    @Autowired
    private ApplicationContext context;

    
        Hyperlink removeMe = new Hyperlink();
        Button testMe = new Button();

        final TextField nameField;
        final TextField pathField;
        final TextField hostField;
        final TextField loginField;
        final TextField addressField;
        final TextField portField;
        final CheckBox sslCheck;
    private Label nameLabel;
    private Label pathLabel;
    private Label addressLabel;
    private Label loginLabel;
    private Label sslLabel;
    private Label portLabel;
    private Label hostLabel;

        public IMAPForm() {
            this.setHgap(10);
            this.setVgap(10);
            this.setPadding(new Insets(5, 5, 5, 5));

            nameLabel = new Label();
            this.add(nameLabel, 0, 0);
            nameField = new TextField();
            this.add(nameField, 1, 0, 2, 1);

            hostLabel = new Label();
            this.add(hostLabel, 0, 1);
            hostField = new TextField();
            this.add(hostField, 1, 1, 2, 1);

            portLabel = new Label();
            this.add(portLabel, 0, 3);
            portField = new TextField("993");
            this.add(portField, 1, 3, 2, 1);

            sslLabel = new Label();
            this.add(sslLabel, 0, 4);
            sslCheck = new CheckBox();
            sslCheck.setTextAlignment(TextAlignment.LEFT);
            sslCheck.setSelected(true);
            this.add(sslCheck, 1, 4, 2, 1);

            this.loginLabel = new Label();
            this.add(loginLabel, 0, 2);
            loginField = new TextField();
            this.add(loginField, 1, 2, 2, 1);

            this.addressLabel = new Label();
            this.add(addressLabel, 0, 5, 1, 1);
            addressField = new TextField();
            this.add(addressField, 1, 5, 2, 1);

            this.pathLabel = new Label();
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
                final List<String> availableFolders;
                try {
                    backend.initNotesFolder();
                    availableFolders = IMAPUtils.getIMAPFoldersList(backend.getStore());
                } catch (Exception ex) {
                    final Alert alertBox = new Alert(Alert.AlertType.ERROR);
                    alertBox.setContentText(ex.getLocalizedMessage());
                    alertBox.showAndWait();
                    getLogger().error("{}", ex.getMessage(), ex);
                    return;
                }
                final ChoiceDialog<String> folderChooser = new ChoiceDialog<>(availableFolders.get(0), availableFolders);
                final Optional<String> choice = folderChooser.showAndWait();
                if (!choice.isPresent()) {
                    return;
                }
                final String chosenFolderName = choice.get();
                pathField.setText(chosenFolderName);
            });
        }
        
        @PostConstruct
        private void init() {
            removeMe.setText(i18N.getTranslation("remove"));
            testMe.setText(i18N.getTranslation("testIMAP"));
            this.nameLabel.setText(i18N.getTranslation("name"));
            this.pathLabel.setText(i18N.getTranslation("path"));
            this.addressLabel.setText(i18N.getTranslation("from_address"));
            this.loginLabel.setText(i18N.getTranslation("login"));
            this.sslLabel.setText(i18N.getTranslation("ssl"));
            this.portLabel.setText(i18N.getTranslation("port"));
            this.hostLabel.setText(i18N.getTranslation("host"));
        
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

