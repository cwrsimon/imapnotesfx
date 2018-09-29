package de.wesim.imapnotes.preferenceview.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

public class FSTab extends Tab {

    private static final Logger logger = LoggerFactory.getLogger(IMAPTab.class);

    private class FSForm extends GridPane {

        final TextField nameField;
        final TextField pathField;

        Hyperlink removeMe = new Hyperlink("Remove");

        public FSForm() {
            this.setHgap(10);
            this.setVgap(10);
            this.setPadding(new Insets(5, 5, 5, 5));

            Label nameLabel = new Label("Name");
            this.add(nameLabel, 0, 0);
            nameField = new TextField();
            this.add(nameField, 1, 0);
            

            Label pathLabel = new Label("File Path");
            this.add(pathLabel, 0, 1);

            pathField = new TextField();
            Button dirButton = new Button("...");
            HBox hbox = new HBox(pathField, dirButton);
            this.add(hbox, 1, 1);
            this.add(removeMe, 0, 2);
            ColumnConstraints column1 = new ColumnConstraints();
                 ColumnConstraints column2 = new ColumnConstraints();

             column2.setHgrow(Priority.ALWAYS);
     this.getColumnConstraints().addAll(column1, column2);
     
            dirButton.setOnAction(e -> {
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
            account.setType(Account_Type.FS);
            return account;
        }
    }

    private TitledPane createTitledPane(String name, String path) {
        FSForm newForm = new FSForm();
        final TitledPane tp = new TitledPane();
        tp.setContent(newForm);
        tp.textProperty().bind(newForm.nameField.textProperty());
        newForm.nameField.textProperty().set(name);
        newForm.pathField.textProperty().set(path);
        newForm.removeMe.setOnAction(e -> {
            acco.getPanes().remove(tp);
        });
        return tp;
    }

    final Accordion acco;

    public FSTab() {
        super("FS");

        final VBox vbox = new VBox();
        setContent(vbox);
        vbox.setPadding(new Insets(5, 5, 5, 5));

        acco = new Accordion();

        final Hyperlink button = new Hyperlink("New");
        final ToolBar toolbar = new ToolBar(button);

        vbox.getChildren().add(toolbar);
        vbox.getChildren().add(acco);

        button.setOnAction(e -> {
            acco.getPanes().add(createTitledPane("", ""));
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
        final String name = account.getAccount_name();
        final String path = account.getRoot_folder();
        acco.getPanes().add(createTitledPane(name, path));
    }

    public List<Account> getAccounts() {
        final List<Account> accounts = new ArrayList<>();
        for (TitledPane tp : acco.getPanes()) {
            final FSForm form = (FSForm) tp.getContent();
            accounts.add(form.getAccount());
        }
        return accounts;
    }

}
