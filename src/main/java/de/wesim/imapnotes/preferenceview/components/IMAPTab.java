package de.wesim.imapnotes.preferenceview.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationContext;
import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.components.PrefixedAlertBox;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.services.I18NService;
import de.wesim.imapnotes.services.IMAPBackend;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class IMAPTab extends Tab implements HasLogger {

    final Accordion acco;

    @Autowired
    private I18NService i18N;

    @Autowired
    private ApplicationContext context;

    private static int new_counter = 0;
    private final Hyperlink button;

    public IMAPTab() {
        super("IMAP");

        final VBox vbox = new VBox();
        setContent(vbox);
        vbox.setPadding(new Insets(5, 5, 5, 5));
        vbox.setSpacing(5);

        acco = new Accordion();
        var scrollPane = new ScrollPane(acco);
        scrollPane.setFitToWidth(true);
        button = new Hyperlink();
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

    private TitledPane createTitledPane(Account account) {
        final IMAPForm newForm = context.getBean(IMAPForm.class);
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
        newForm.testMe.setOnAction((ActionEvent e) -> {
            final Account newAccount = newForm.getAccount();
            IMAPBackend backend = context.getBean(IMAPBackend.class, newAccount);
            boolean testOK;
            try {
                testOK = backend.initNotesFolder();
            } catch (Exception e1) {
                getLogger().error("{}", e1.getMessage(), e1);
                testOK = false;
                final Alert alert = context.getBean(PrefixedAlertBox.class, "imap_conn_failed");
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setContentText(e1.getLocalizedMessage());
                alert.showAndWait();
            }
            final Alert alert;
            if (testOK) {
                alert = context.getBean(PrefixedAlertBox.class, "imap_conn_success");
                alert.setAlertType(Alert.AlertType.INFORMATION);
                alert.showAndWait();
                return;
            }
            // Create new folder 
            alert = context.getBean(PrefixedAlertBox.class, "imap_conn_create_dir");
            alert.setAlertType(Alert.AlertType.CONFIRMATION);
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
                final Alert alertBox = new Alert(Alert.AlertType.ERROR);
                alertBox.setContentText(exc.getLocalizedMessage());
                alertBox.showAndWait();
                getLogger().error("{}", exc.getMessage(), exc);
            }
        });
        return tp;
    }
    
    @PostConstruct
    void init() {
        button.setText(i18N.getTranslation("new"));
    }

}
