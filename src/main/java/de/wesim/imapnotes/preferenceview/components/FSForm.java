package de.wesim.imapnotes.preferenceview.components;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.services.I18NService;
import java.io.File;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class FSForm extends GridPane {

    @Autowired
    private I18NService i18N;

    final TextField nameField;
    final TextField pathField;

    final Hyperlink removeMe = new Hyperlink();
    private final Label pathLabel;
    private final Label nameLabel;

    public FSForm() {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(5, 5, 5, 5));

        this.nameLabel = new Label();
        this.add(nameLabel, 0, 0, 1, 1);
        nameField = new TextField();
        this.add(nameField, 1, 0, 2, 1);

        this.pathLabel = new Label();
        this.add(pathLabel, 0, 1, 1, 1);

        pathField = new TextField();
        Button dirButton = new Button("...");
        this.add(pathField, 1, 1, 1, 1);
        this.add(dirButton, 2, 1, 1, 1);
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

    @PostConstruct
    void init() {
        this.removeMe.setText(i18N.getTranslation("remove"));
        this.nameLabel.setText(i18N.getTranslation("name"));
        this.pathLabel.setText(i18N.getTranslation("file_path"));
    }
}
